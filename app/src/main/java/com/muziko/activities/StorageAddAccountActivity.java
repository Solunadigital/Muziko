package com.muziko.activities;

import android.Manifest;
import android.accounts.AccountManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.badoo.mobile.util.WeakHandler;
import com.box.androidsdk.content.BoxConfig;
import com.github.simonpercic.aircycle.AirCycle;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.AccountPicker;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAuthIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.muziko.R;
import com.muziko.cloud.Amazon.AmazonApi;
import com.muziko.cloud.Box.BoxApi;
import com.muziko.cloud.Drive.DriveApi;
import com.muziko.cloud.Drive.DriveApiHelpers;
import com.muziko.cloud.DropBox.DropBoxApi;
import com.muziko.cloud.OneDrive.OneDriveApi;
import com.muziko.common.events.NetworkEvent;
import com.muziko.helpers.Utils;
import com.muziko.manager.AppController;
import com.muziko.manager.CloudManager;
import com.muziko.manager.SettingsManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.muziko.MyApplication.hasWifi;
import static com.muziko.manager.MuzikoConstants.REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS;
import static com.muziko.manager.MuzikoConstants.addCloudDelay;

public class StorageAddAccountActivity extends BaseActivity implements View.OnClickListener, DriveApi.DriveConnectionCallbacks, DropBoxApi.DropBoxConnectionCallbacks, BoxApi.BoxConnectionCallbacks, OneDriveApi.OneDriveConnectionCallbacks, AmazonApi.AmazonConnectionCallbacks {

    private static final int REQ_ACCPICK = 1;
    private static final int REQ_CONNECT = 2;
    private final String TAG = StorageAddAccountActivity.class.getSimpleName();
    private final WeakHandler handler = new WeakHandler();
    @AirCycle
    DropBoxApi.DropBoxAirCycleListener dropBoxLifecycleListener;
    private DropBoxApi dropBoxApi;
    private DriveApi driveApi;
    private BoxApi boxApi;
    private OneDriveApi oneDriveApi;
    private AmazonApi amazonApi;
    private LinearLayout driveLayout;
    private LinearLayout dropboxLayout;
    private LinearLayout boxLayout;
    private LinearLayout oneDriveLayout;
    private LinearLayout amazonLayout;
    private MenuItem menuItemWifi;
    private MaterialDialog progressDialog;
    private boolean disableBack;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        StorageAddAccountActivityAirCycle.bind(this);
        super.onCreate(savedInstanceState);
        setStartActivity(false);
        setContentView(R.layout.activity_storage_add_account);

        driveApi = new DriveApi();
        boxApi = new BoxApi();
        dropBoxApi = new DropBoxApi();
        dropBoxLifecycleListener = dropBoxApi.createActivityListener(this, this);
        oneDriveApi = new OneDriveApi();
        amazonApi = new AmazonApi();

        findViewsById();
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.add_account));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        driveLayout.setOnClickListener(this);
        dropboxLayout.setOnClickListener(this);
        boxLayout.setOnClickListener(this);
        oneDriveLayout.setOnClickListener(this);
        amazonLayout.setOnClickListener(this);

        BoxConfig.IS_LOG_ENABLED = true;

        EventBus.getDefault().register(this);

        if (!hasWifi && !SettingsManager.Instance().getPrefShowStreamDataWarning()) {
            new MaterialDialog.Builder(this)
                    .theme(Theme.LIGHT)
                    .titleColorRes(R.color.normal_blue)
                    .negativeColorRes(R.color.dialog_negetive_button)
                    .positiveColorRes(R.color.normal_blue)
                    .content(R.string.data_charges_may_apply)
                    .iconRes(R.drawable.boxsdk_dialog_warning)
                    .limitIconToDefaultSize()
                    .positiveText(R.string.continue_text)
                    .negativeText(R.string.dont_show_again)
                    .onNegative(
                            (dialog, which) -> {
                                SettingsManager.Instance().setPrefShowStreamDataWarning(true);
                            })
                    .show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int request, int result, Intent data) {
        switch (request) {
            case REQ_CONNECT:
                if (result == RESULT_OK) {
                    startActivityForResult(AccountPicker.newChooseAccountIntent(null,
                            null, new String[]{GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE}, true, null, null, null, null),
                            REQ_ACCPICK);
                } else {
                    AppController.toast(this, "Google Drive permission denied");
                }
                break;
            case REQ_ACCPICK:
                if (data != null && data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME) != null) {

                    disableBack = true;
                    progressDialog = new MaterialDialog.Builder(this)
                            .title(R.string.connecting_cloud_drive)
                            .content(R.string.google_drive)
                            .cancelable(false)
                            .progress(true, 0)
                            .show();

                    driveApi = new DriveApi();

                    if (driveApi.initialize(this, data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME))) {
                        driveApi.create(this);
                        CloudManager.Instance().addDriveApi(data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME), driveApi);
                    }
                }

                break;
        }
        super.onActivityResult(request, result, data);
    }


    @Override
    public void onBackPressed() {
        if (!disableBack) {
            super.onBackPressed();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    private void initAmazon() {

        amazonApi.initialize(this, this);
    }

    private void initOneDrive() {

        oneDriveApi.initialize(this, this);
    }

    private void initBox() {

        boxApi.initialize(this, null, this);
    }

    private void initDropBox() {

        dropBoxApi.initialize(this);
    }

    private void initGoogleDrive() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            checkPermission();
        } else {
            startActivityForResult(AccountPicker.newChooseAccountIntent(null,
                    null, new String[]{GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE}, true, null, null, null, null),
                    REQ_ACCPICK);
        }
    }

    private void checkPermission() {
        // Here, thisActivity is the current activity
        List<String> permissionsNeeded = new ArrayList<>();

        final List<String> permissionsList = new ArrayList<>();
        if (!addPermission(permissionsList, Manifest.permission.GET_ACCOUNTS))
            permissionsNeeded.add("Get Accounts");
        if (permissionsList.size() > 0) {
            if (permissionsNeeded.size() > 0) {
                // Need Rationale
                StringBuilder message = new StringBuilder("You need to grant access to " + permissionsNeeded.get(0));
                for (int i = 1; i < permissionsNeeded.size(); i++)
                    message.append(", ").append(permissionsNeeded.get(i));

                Utils.alertNoDismiss(this, getString(R.string.app_name), message.toString(), () -> {
                    // No explanation needed, we can request the permission.
                    ActivityCompat.requestPermissions(StorageAddAccountActivity.this, permissionsList.toArray(new String[permissionsList.size()]),
                            REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
                });

                return;
            }
            ActivityCompat.requestPermissions(StorageAddAccountActivity.this, permissionsList.toArray(new String[permissionsList.size()]),
                    REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
        } else {
            startActivityForResult(AccountPicker.newChooseAccountIntent(null,
                    null, new String[]{GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE}, true, null, null, null, null),
                    REQ_ACCPICK);
        }
    }

    private boolean addPermission(List<String> permissionsList, String permission) {
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);
            // Check for Rationale Option
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permission))
                return false;
        }
        return true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNetworkEvent(NetworkEvent networkEvent) {

        if (hasWifi) {
            menuItemWifi.setIcon(R.drawable.ic_wifi_white_24dp);
        } else {
            menuItemWifi.setIcon(R.drawable.ic_wifi_light_blue_24dp);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.storage_add_menu, menu);
        menuItemWifi = menu.findItem(R.id.wifi);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        if (hasWifi) {
            menuItemWifi.setIcon(R.drawable.ic_wifi_white_24dp);
        } else {
            menuItemWifi.setIcon(R.drawable.ic_wifi_light_blue_24dp);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button

            case android.R.id.home:
                onBackPressed();
                return true;

            case R.id.wifi:
                startActivity(new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK));
                return true;


            case R.id.settings_about:

                Intent activityIntent = new Intent(StorageAddAccountActivity.this, AboutActivity.class);
                startActivity(activityIntent);
                overridePendingTransition(R.anim.slide_up, R.anim.slide_down);

                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    private void findViewsById() {
        driveLayout = findViewById(R.id.driveLayout);
        dropboxLayout = findViewById(R.id.dropboxLayout);
        boxLayout = findViewById(R.id.boxLayout);
        oneDriveLayout = findViewById(R.id.oneDriveLayout);
        amazonLayout = findViewById(R.id.amazonLayout);
    }

    @Override
    public void onClick(View v) {
        if (v == driveLayout) {
            initGoogleDrive();
        } else if (v == dropboxLayout) {
            initDropBox();
        } else if (v == boxLayout) {
            initBox();
        } else if (v == oneDriveLayout) {
            initOneDrive();
        } else if (v == amazonLayout) {
            initAmazon();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS: {
                Map<String, Integer> perms = new HashMap<>();
                // Initial
                perms.put(Manifest.permission.GET_ACCOUNTS, PackageManager.PERMISSION_GRANTED);
                // Fill with results
                for (int i = 0; i < permissions.length; i++)
                    perms.put(permissions[i], grantResults[i]);
                // Check for ACCESS_FINE_LOCATION
                if (perms.get(Manifest.permission.GET_ACCOUNTS) == PackageManager.PERMISSION_GRANTED) {
                    // All Permissions Granted

                    startActivityForResult(AccountPicker.newChooseAccountIntent(null,
                            null, new String[]{GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE}, true, null, null, null, null),
                            REQ_ACCPICK);
                } else {
                    // Permission Denied
                    AppController.toast(this, "Some permissions were denied");
                }
            }
            break;

            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onDriveConnectionFailed(Exception ex) {
        runOnUiThread(() -> {
            if (progressDialog != null) {
                progressDialog.setContent(getString(R.string.drive_connection_failed));
            } else {
                AppController.toast(StorageAddAccountActivity.this, getString(R.string.drive_connection_failed));
            }
            handler.postDelayed(() -> {
                if (progressDialog != null) {
                    progressDialog.dismiss();
                }

            }, addCloudDelay);

            if (ex == null) {
                DriveApiHelpers.lg("connFail - UNSPECD 1");
                AppController.toast(StorageAddAccountActivity.this, "Google Drive connection failed");
                return;
            }
            if (ex instanceof UserRecoverableAuthIOException) {
                DriveApiHelpers.lg("connFail - has res");
                startActivityForResult((((UserRecoverableAuthIOException) ex).getIntent()), REQ_CONNECT);
            } else if (ex instanceof GoogleAuthIOException) {
                DriveApiHelpers.lg("connFail - SHA1?");
                AppController.toast(StorageAddAccountActivity.this, "Google Drive connection failed");
            } else {
                DriveApiHelpers.lg("connFail - UNSPECD 2");
                AppController.toast(StorageAddAccountActivity.this, "Google Drive connection failed");
            }
        });

    }

    @Override
    public void onDriveConnected(int cloudAccountId) {
        runOnUiThread(() -> {
            if (progressDialog != null) {
                progressDialog.setContent(getString(R.string.drive_account_connected));
            }
//        Utils.toast(this, getString(R.string.drive_account_connected));
            handler.postDelayed(() -> {
                if (progressDialog != null) {
                    progressDialog.dismiss();
                }
                StorageAddAccountActivity.this.finish();
            }, addCloudDelay);
        });

    }

    @Override
    public void onDropBoxConnectionFailed(Exception ex) {
        runOnUiThread(() -> {
            if (progressDialog != null) {
                progressDialog.setContent(getString(R.string.dropbox_connection_failed));
            } else {
                AppController.toast(StorageAddAccountActivity.this, getString(R.string.dropbox_connection_failed));
            }
            handler.postDelayed(() -> {
                if (progressDialog != null) {
                    progressDialog.dismiss();
                }

            }, addCloudDelay);
        });

    }

    @Override
    public void onDropBoxConnectionFailed() {
        runOnUiThread(() -> {
            if (progressDialog != null) {
                progressDialog.setContent(getString(R.string.dropbox_connection_failed));
            } else {
                AppController.toast(StorageAddAccountActivity.this, getString(R.string.dropbox_connection_failed));
            }
            handler.postDelayed(() -> {
                if (progressDialog != null) {
                    progressDialog.dismiss();
                }

            }, addCloudDelay);
        });

    }

    @Override
    public void onDropBoxConnected(int cloudAccountId) {
        runOnUiThread(() -> {
            if (progressDialog != null) {
                progressDialog.setContent(getString(R.string.dropbox_account_connected));
            }
            handler.postDelayed(() -> {
                if (progressDialog != null) {
                    progressDialog.dismiss();
                }
                StorageAddAccountActivity.this.finish();
            }, addCloudDelay);
        });

    }

    @Override
    public void onDropBoxConnectionStarted() {
        runOnUiThread(() -> {
            disableBack = true;
            progressDialog = new MaterialDialog.Builder(StorageAddAccountActivity.this)
                    .title(R.string.connecting_cloud_drive)
                    .content(R.string.dropbox)
                    .cancelable(false)
                    .progress(true, 0)
                    .show();
        });

    }

    @Override
    public void onBoxConnectionFailed(Exception ex) {
        runOnUiThread(() -> {
            if (progressDialog != null) {
                progressDialog.setContent(getString(R.string.box_connection_failed));
            } else {
                AppController.toast(StorageAddAccountActivity.this, getString(R.string.box_connection_failed));
            }
            handler.postDelayed(() -> {
                if (progressDialog != null) {
                    progressDialog.dismiss();
                }

            }, addCloudDelay);
        });

    }

    @Override
    public void onBoxConnectionFailed() {
        runOnUiThread(() -> {
            if (progressDialog != null) {
                progressDialog.setContent(getString(R.string.box_connection_failed));
            } else {
                AppController.toast(StorageAddAccountActivity.this, getString(R.string.box_connection_failed));
            }
            handler.postDelayed(() -> {
                if (progressDialog != null) {
                    progressDialog.dismiss();
                }

            }, addCloudDelay);
        });

    }

    @Override
    public void onBoxConnected(int cloudAccountId) {
        runOnUiThread(() -> {
            if (progressDialog != null) {
                progressDialog.setContent(getString(R.string.box_account_connected));
            }
            handler.postDelayed(() -> {
                if (progressDialog != null) {
                    progressDialog.dismiss();
                }
                StorageAddAccountActivity.this.finish();
            }, addCloudDelay);
        });

    }

    @Override
    public void onBoxConnectionStarted() {
        runOnUiThread(() -> {
            disableBack = true;
            progressDialog = new MaterialDialog.Builder(StorageAddAccountActivity.this)
                    .title(R.string.connecting_cloud_drive)
                    .content(R.string.box)
                    .cancelable(false)
                    .progress(true, 0)
                    .show();
        });

    }

    @Override
    public void onOneDriveConnectionFailed(String message) {
        runOnUiThread(() -> {
            if (progressDialog != null) {
                progressDialog.setContent(getString(R.string.onedrive_connection_failed));
            } else {
                AppController.toast(StorageAddAccountActivity.this, getString(R.string.onedrive_connection_failed));
            }
            handler.postDelayed(() -> {
                if (progressDialog != null) {
                    progressDialog.dismiss();
                }

            }, addCloudDelay);
        });

    }

    @Override
    public void onOneDriveConnectionFailed(Exception ex) {
        runOnUiThread(() -> {
            if (progressDialog != null) {
                progressDialog.setContent(getString(R.string.onedrive_connection_failed));
            } else {
                AppController.toast(StorageAddAccountActivity.this, getString(R.string.onedrive_connection_failed));
            }
            handler.postDelayed(() -> {
                if (progressDialog != null) {
                    progressDialog.dismiss();
                }

            }, addCloudDelay);
        });

    }

    @Override
    public void onOneDriveConnected(int cloudAccountId) {
        runOnUiThread(() -> {
            if (progressDialog != null) {
                progressDialog.setContent(getString(R.string.onedrive_account_connected));
            }
            handler.postDelayed(() -> {
                if (progressDialog != null) {
                    progressDialog.dismiss();
                }
                StorageAddAccountActivity.this.finish();
            }, addCloudDelay);
        });

    }

    @Override
    public void onOneDriveConnectionStarted() {
        runOnUiThread(() -> {
            disableBack = true;
            progressDialog = new MaterialDialog.Builder(StorageAddAccountActivity.this)
                    .title(R.string.connecting_cloud_drive)
                    .content(R.string.onedrive)
                    .cancelable(false)
                    .progress(true, 0)
                    .show();
        });

    }

    @Override
    public void onAmazonConnectionFailed(String message) {
        runOnUiThread(() -> AppController.toast(StorageAddAccountActivity.this, getString(R.string.amazon_connection_failed)));

    }

    @Override
    public void onAmazonConnectionFailed(Exception ex) {
        runOnUiThread(() -> AppController.toast(StorageAddAccountActivity.this, getString(R.string.amazon_connection_failed)));
    }

    @Override
    public void onAmazonConnected(int cloudAccountId) {
        runOnUiThread(() -> {
            if (progressDialog != null) {
                progressDialog.dismiss();
            }
            AppController.toast(StorageAddAccountActivity.this, StorageAddAccountActivity.this.getString(R.string.amazon_account_connected));
            handler.postDelayed(() -> StorageAddAccountActivity.this.finish(), addCloudDelay);
        });

    }
}
