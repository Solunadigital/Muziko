package com.muziko.activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
//import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.arasthel.asyncjob.AsyncJob;
import com.badoo.mobile.util.WeakHandler;
import com.evernote.android.job.JobManager;
import com.muziko.MyApplication;
import com.muziko.R;
import com.muziko.adapter.SettingsAdapter;
import com.muziko.common.events.ManageTabsEvent;
import com.muziko.common.events.RefreshEvent;
import com.muziko.common.models.SettingModel;
import com.muziko.common.models.TabModel;
import com.muziko.controls.SimpleSectionedRecyclerViewAdapter;
import com.muziko.controls.WheelPicker.WheelPicker;
import com.muziko.database.TabRealmHelper;
import com.muziko.dialogs.HideSongs;
import com.muziko.dialogs.ManageTabs;
import com.muziko.helpers.Utils;
import com.muziko.interfaces.SettingsRecyclerItemListener;
import com.muziko.jobs.CoverArtJob;
import com.muziko.jobs.LyricsJob;
import com.muziko.manager.AppController;
import com.muziko.manager.FirebaseManager;
import com.muziko.manager.MuzikoConstants;
import com.muziko.manager.PrefsManager;
import com.muziko.manager.SettingsManager;
import com.muziko.service.LyricsDownloaderService;
import com.muziko.tasks.CoverArtDownloader;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.muziko.MyApplication.coverArtDownloaders;
import static com.muziko.manager.MuzikoConstants.REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS;
import static com.muziko.manager.MuzikoConstants.REQUEST_CODE_STORAGE_ACCESS;
import static com.muziko.manager.MuzikoConstants.UPLOAD_SETTLE_DELAY;
import static com.muziko.manager.SettingsManager.prefLanguage;
import static com.muziko.manager.SettingsManager.prefScrobbling;
import static com.muziko.manager.SettingsManager.prefSyncLocation;
import static com.muziko.manager.SettingsManager.prefmanagetabs;

public class SettingsActivity extends BaseActivity implements SettingsRecyclerItemListener {

    private final ArrayList<SettingModel> items = new ArrayList<>();
    private boolean artworkChanged = false;
    private boolean lyricsChanged = false;
    private RecyclerView recyclerView;
    private SettingsAdapter adapter;
    private WeakHandler handler = new WeakHandler();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.dark_blue));
        }
        findViewsById();
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.settings));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        updateList();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public final void onActivityResult(final int requestCode, final int resultCode, final Intent resultData) {
        if (requestCode == REQUEST_CODE_STORAGE_ACCESS) {
            Uri treeUri;
            if (resultCode == Activity.RESULT_OK) {
                // Get Uri from Storage Access Framework.
                treeUri = resultData.getData();

                // Persist URI in shared preference so that you can use it later.
                // Use your own framework here instead of PreferenceUtil.
                String uri = String.valueOf(treeUri);
                PrefsManager.Instance().setStoragePermsURI(uri);

                grantUriPermission(getPackageName(), treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                getContentResolver().takePersistableUriPermission(treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            }
        }
    }

    @Override
    public void onBackPressed() {

        if (MyApplication.tabsChanged) {
            MyApplication.tabsChanged = false;

            EventBus.getDefault().post(new ManageTabsEvent());
        }


        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean prefArtworkDownload = prefs.getBoolean(SettingsManager.prefArtworkDownload, false);

        if (artworkChanged) {
            if (prefArtworkDownload) {

                CoverArtDownloader coverArtDownloader = new CoverArtDownloader(this);
                coverArtDownloaders.add(coverArtDownloader);
                coverArtDownloader.execute();

                CoverArtJob albumArtJob = new CoverArtJob();
                albumArtJob.scheduleJob();
            } else {
                JobManager.instance().cancelAllForTag(CoverArtJob.TAG);
            }
        }

        boolean prefLyricsDownload = prefs.getBoolean(SettingsManager.prefLyricsDownload, false);


        if (lyricsChanged) {
            if (prefLyricsDownload) {

                Intent intent = new Intent(this, LyricsDownloaderService.class);
                intent.setAction(MyApplication.ACTION_UPDATE_LYRICS);
                startService(intent);

                LyricsJob lyricsJob = new LyricsJob();
                lyricsJob.scheduleJob();
            } else {
                JobManager.instance().cancelAllForTag(LyricsJob.TAG);
            }
        }

        finish();
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

    private void check() {
        // Here, thisActivity is the current activity
        List<String> permissionsNeeded = new ArrayList<>();

        final List<String> permissionsList = new ArrayList<>();
        if (!addPermission(permissionsList, Manifest.permission.BLUETOOTH))
            permissionsNeeded.add(getString(R.string.bluetooth));
        if (permissionsList.size() > 0) {
            if (permissionsNeeded.size() > 0) {
                // Need Rationale
                String message = getString(R.string.bluetooth_rationale);

                Utils.alertNoDismiss(this, getString(R.string.app_name), message, () -> {
                    // No explanation needed, we can request the permission.
                    ActivityCompat.requestPermissions(SettingsActivity.this, permissionsList.toArray(new String[permissionsList.size()]),
                            REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
                });

                return;
            }
            ActivityCompat.requestPermissions(SettingsActivity.this, permissionsList.toArray(new String[permissionsList.size()]),
                    REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
        }
    }

    private void findViewsById() {
        recyclerView = findViewById(R.id.itemList);
    }

    @Override
    public void onItemClicked(final Context context, final SettingModel settingModel) {

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int prefHideSongDuration = prefs.getInt(SettingsManager.prefHideSongDuration, 0);
        int prefShakeThreshold = prefs.getInt(SettingsManager.prefShakeThreshold, 1);
        String prefStartScreen = prefs.getString(SettingsManager.prefStartScreen, "");
        String prefFaceDown = prefs.getString(SettingsManager.prefFaceDown, "");
        String prefFaceUp = prefs.getString(SettingsManager.prefFaceUp, "");

        int initialValue;
        switch (settingModel.key) {

            case prefLanguage:

                ArrayList<String> languageList = new ArrayList<>();
                languageList.add(getString(R.string.english));
                languageList.add(getString(R.string.thai));
                languageList.add(getString(R.string.turkish));
                languageList.add(getString(R.string.arabic));
                String currentLanguage = PrefsManager.Instance().getLanguage();
                initialValue = 0;
                switch (currentLanguage) {
                    case (MuzikoConstants.LANGUAGE_ENGLISH):
                        initialValue = 0;
                        break;
                    case (MuzikoConstants.LANGUAGE_THAI):
                        initialValue = 1;
                        break;
                    case (MuzikoConstants.LANGUAGE_TURKISH):
                        initialValue = 2;
                        break;
                    case (MuzikoConstants.LANGUAGE_ARABIC):
                        initialValue = 3;
                        break;
                }


                new MaterialDialog.Builder(context).theme(Theme.LIGHT).titleColorRes(R.color.normal_blue).negativeColorRes(R.color.dialog_negetive_button).positiveColorRes(R.color.normal_blue).title(R.string.select_language).items(languageList)
                        .itemsCallbackSingleChoice(initialValue, (dialog, view, which, text) -> {
                            if (!text.toString().isEmpty()) {
                                switch (text.toString()) {
                                    case ("English"):
                                        PrefsManager.Instance().setLanguage(MuzikoConstants.LANGUAGE_ENGLISH);
                                        setLanguage("en");
                                        break;
                                    case ("Thai"):
                                        PrefsManager.Instance().setLanguage(MuzikoConstants.LANGUAGE_THAI);
                                        setLanguage("th");
                                        break;
                                    case ("Turkish"):
                                        PrefsManager.Instance().setLanguage(MuzikoConstants.LANGUAGE_TURKISH);
                                        setLanguage("tr");
                                        break;
                                    case ("Arabic"):
                                        PrefsManager.Instance().setLanguage(MuzikoConstants.LANGUAGE_ARABIC);
                                        setLanguage("ar");
                                        break;
                                }

                            }
                            return true;
                        }).positiveText(R.string.choose)
                        .show();

                break;

            case prefmanagetabs:

                ArrayList<TabModel> tabModels = TabRealmHelper.getAllTabs();

                ManageTabs pl = new ManageTabs(tabModels);
                pl.open(this);

                break;

            case SettingsManager.prefHideSongDuration:

                HideSongs hideSongs = new HideSongs();
                hideSongs.open(this);

                break;

            case SettingsManager.prefShakeThreshold:

                initialValue = prefShakeThreshold;
                List<String> shakes = Arrays.asList(getResources().getStringArray(R.array.shake_threshold));

                final WheelPicker shakewheelPicker = new WheelPicker(this);
                shakewheelPicker.setData(shakes);
                shakewheelPicker.setSelectedItemPosition(initialValue);
                shakewheelPicker.setAtmospheric(true);
                shakewheelPicker.setCurved(true);
                shakewheelPicker.setIndicator(true);
                shakewheelPicker.setIndicatorColor(ContextCompat.getColor(this, R.color.normal_blue));
                shakewheelPicker.setSelectedItemTextColor(ContextCompat.getColor(this, R.color.normal_blue));

                new MaterialDialog.Builder(context).theme(Theme.LIGHT).titleColorRes(R.color.normal_blue).negativeColorRes(R.color.dialog_negetive_button).positiveColorRes(R.color.normal_blue).title(R.string.shake_sensitivity).customView(shakewheelPicker, false).positiveText(R.string.save).onPositive((dialog, which) -> prefs.edit().putInt(SettingsManager.prefShakeThreshold, shakewheelPicker.getCurrentItemPosition()).apply()).negativeText(R.string.cancel).show();

                break;

            case SettingsManager.prefStartScreen:
                ArrayList<String> startscreen = SettingsManager.Instance().getStartScreens();
                initialValue = startscreen.indexOf(prefStartScreen);
                if (initialValue == -1) {
                    initialValue = 0;
                }
                new MaterialDialog.Builder(context).theme(Theme.LIGHT).titleColorRes(R.color.normal_blue).negativeColorRes(R.color.dialog_negetive_button).positiveColorRes(R.color.normal_blue).title(R.string.start_screen).items(startscreen)
                        .itemsCallbackSingleChoice(initialValue, (dialog, view, which, text) -> {
                            if (!text.toString().isEmpty()) {
                                prefs.edit().putString(SettingsManager.prefStartScreen, text.toString()).apply();
                            }
                            return true;
                        }).positiveText(getString(R.string.choose))
                        .show();
                break;

            case prefScrobbling:

                Intent activityIntent = new Intent(SettingsActivity.this, LastFMSettingsActivity.class);
                startActivity(activityIntent);
                overridePendingTransition(R.anim.slide_up, R.anim.slide_down);

                break;

            case SettingsManager.prefFaceDown:

                if (prefFaceDown.equals(getString(R.string.none))) {
                    initialValue = 0;
                } else if (prefFaceDown.equals(getString(R.string.play))) {
                    initialValue = 1;
                } else if (prefFaceDown.equals(getString(R.string.pause))) {
                    initialValue = 2;
                } else {
                    initialValue = 0;
                }
                List<String> faceDown = Arrays.asList(getResources().getStringArray(R.array.phone_action));

                new MaterialDialog.Builder(context).theme(Theme.LIGHT).titleColorRes(R.color.normal_blue).negativeColorRes(R.color.dialog_negetive_button).positiveColorRes(R.color.normal_blue).title(getString(R.string.phone_face_down)).items(faceDown)
                        .itemsCallbackSingleChoice(initialValue, (dialog, view, which, text) -> {
                            if (!text.toString().isEmpty()) {
                                prefs.edit().putString(SettingsManager.prefFaceDown, text.toString()).apply();
                            }
                            return true;
                        }).positiveText(getString(R.string.choose))
                        .show();
                break;

            case SettingsManager.prefFaceUp:

                if (prefFaceUp.equals(getString(R.string.none))) {
                    initialValue = 0;
                } else if (prefFaceUp.equals(getString(R.string.play))) {
                    initialValue = 1;
                } else if (prefFaceUp.equals(getString(R.string.pause))) {
                    initialValue = 2;
                } else {
                    initialValue = 0;
                }
                List<String> faceUp = Arrays.asList(getResources().getStringArray(R.array.phone_action));

                new MaterialDialog.Builder(context).theme(Theme.LIGHT).titleColorRes(R.color.normal_blue).negativeColorRes(R.color.dialog_negetive_button).positiveColorRes(R.color.normal_blue).title(getString(R.string.phone_face_up)).items(faceUp)
                        .itemsCallbackSingleChoice(initialValue, (dialog, view, which, text) -> {
                            if (!text.toString().isEmpty()) {
                                prefs.edit().putString(SettingsManager.prefFaceUp, text.toString()).apply();
                            }
                            return true;
                        }).positiveText(getString(R.string.choose))
                        .show();
                break;

            case prefSyncLocation:
                initialValue = SettingsManager.Instance().getPrefSyncLocation();
                List<String> syncLocations = Arrays.asList(getResources().getStringArray(R.array.sync_location));

                new MaterialDialog.Builder(context).theme(Theme.LIGHT).titleColorRes(R.color.normal_blue).negativeColorRes(R.color.dialog_negetive_button).positiveColorRes(R.color.normal_blue).title(getString(R.string.sync_location)).items(syncLocations)
                        .itemsCallbackSingleChoice(initialValue, (dialog, view, which, text) -> {
                            if (!text.toString().isEmpty()) {
                                if (text.toString().equals(getString(R.string.internal_storage))) {
                                    SettingsManager.Instance().setPrefSyncLocation(0);
                                } else if (text.toString().equals(getString(R.string.sd_card))) {
                                    SettingsManager.Instance().setPrefSyncLocation(1);
                                }

                                items.clear();
                                items.addAll(SettingsManager.Instance().getSettings());
                                adapter.notifyDataSetChanged();
                            }
                            return true;
                        }).positiveText(getString(R.string.choose))
                        .show();
                break;
        }
    }

    @Override
    public void onItemChecked(final SettingModel settingModel) {

        AsyncJob.doInBackground(() -> processSwitch(settingModel));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.settings_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button

            case android.R.id.home:
                onBackPressed();
                return true;

            case R.id.settings_about:

                Intent activityIntent = new Intent(SettingsActivity.this, AboutActivity.class);
                startActivity(activityIntent);
                overridePendingTransition(R.anim.slide_up, R.anim.slide_down);

                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    private void processSwitch(SettingModel settingModel) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        switch (settingModel.key) {
            case SettingsManager.prefHideUnknown:
                boolean prefHideUnknown = prefs.getBoolean(SettingsManager.prefHideUnknown, false);
                prefs.edit().putBoolean(SettingsManager.prefHideUnknown, !prefHideUnknown).apply();
                EventBus.getDefault().post(new RefreshEvent(1000));
                break;
            case SettingsManager.prefStoragePerms:
                boolean prefStoragePerms = prefs.getBoolean(SettingsManager.prefStoragePerms, false);
                prefs.edit().putBoolean(SettingsManager.prefStoragePerms, !prefStoragePerms).apply();
                if (!prefStoragePerms) {
                    triggerStorageAccessFramework();
                }
                EventBus.getDefault().post(new RefreshEvent(1000));
                break;
            case SettingsManager.prefLyricsDownload:

                boolean prefLyricsDownload = prefs.getBoolean(SettingsManager.prefLyricsDownload, false);
                prefs.edit().putBoolean(SettingsManager.prefLyricsDownload, !prefLyricsDownload).apply();
                lyricsChanged = true;
                break;
            case SettingsManager.prefShowArtwork:
                boolean prefShowArtwork = prefs.getBoolean(SettingsManager.prefShowArtwork, false);
                prefs.edit().putBoolean(SettingsManager.prefShowArtwork, !prefShowArtwork).apply();
                MyApplication.showArtwork = !prefShowArtwork;
                EventBus.getDefault().post(new RefreshEvent(1000));
                break;
            case SettingsManager.prefArtworkDownload:
                boolean prefArtworkDownload = prefs.getBoolean(SettingsManager.prefArtworkDownload, false);
                prefs.edit().putBoolean(SettingsManager.prefArtworkDownload, !prefArtworkDownload).apply();
                artworkChanged = true;
                break;
            case SettingsManager.prefArtworkDownloadWifi:

                boolean prefArtworkDownloadWifi = prefs.getBoolean(SettingsManager.prefArtworkDownloadWifi, false);
                prefs.edit().putBoolean(SettingsManager.prefArtworkDownloadWifi, !prefArtworkDownloadWifi).apply();
                break;

            case SettingsManager.prefShake:

                boolean prefShake = prefs.getBoolean(SettingsManager.prefShake, false);
                prefs.edit().putBoolean(SettingsManager.prefShake, !prefShake).apply();
                adapter.notifyDataSetChanged();
                break;
            case SettingsManager.prefArtworkLock:
                boolean prefArtworkLock = prefs.getBoolean(SettingsManager.prefArtworkLock, false);
                prefs.edit().putBoolean(SettingsManager.prefArtworkLock, !prefArtworkLock).apply();
                AppController.Instance().serviceLockScreen();
                break;
            case SettingsManager.prefGapless:
                boolean prefGapless = prefs.getBoolean(SettingsManager.prefGapless, false);
                prefs.edit().putBoolean(SettingsManager.prefGapless, !prefGapless).apply();
                break;

            case SettingsManager.prefScrobbling:
                boolean prefScrobbling = prefs.getBoolean(SettingsManager.prefScrobbling, false);
                prefs.edit().putBoolean(SettingsManager.prefScrobbling, !prefScrobbling).apply();
                if (!prefScrobbling) {
                    Intent activityIntent = new Intent(SettingsActivity.this, LastFMSettingsActivity.class);
                    startActivity(activityIntent);
                    overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
                }

                break;

            case SettingsManager.prefHeadset:
                boolean prefHeadset = prefs.getBoolean(SettingsManager.prefHeadset, false);
                prefs.edit().putBoolean(SettingsManager.prefHeadset, !prefHeadset).apply();
                break;

            case SettingsManager.prefBluetooth:
                final boolean prefBluetooth = prefs.getBoolean(SettingsManager.prefBluetooth, false);
                prefs.edit().putBoolean(SettingsManager.prefBluetooth, !prefBluetooth).apply();
                break;

            case SettingsManager.prefLockScreen:
                final boolean prefLockScreen = prefs.getBoolean(SettingsManager.prefLockScreen, false);
                prefs.edit().putBoolean(SettingsManager.prefLockScreen, !prefLockScreen).apply();
                break;

            case SettingsManager.prefDownloadCloudWhenStreaming:
                final boolean prefDownloadCloudWhenStreaming = prefs.getBoolean(SettingsManager.prefDownloadCloudWhenStreaming, false);
                prefs.edit().putBoolean(SettingsManager.prefDownloadCloudWhenStreaming, !prefDownloadCloudWhenStreaming).apply();
                break;

            case SettingsManager.prefShowStreamDataWarning:
                final boolean prefShowStreamDataWarning = prefs.getBoolean(SettingsManager.prefShowStreamDataWarning, false);
                prefs.edit().putBoolean(SettingsManager.prefShowStreamDataWarning, !prefShowStreamDataWarning).apply();
                break;

            case SettingsManager.prefAutoSyncLibrary:
                final boolean preAutoSyncLibrary = prefs.getBoolean(SettingsManager.prefAutoSyncLibrary, false);
                prefs.edit().putBoolean(SettingsManager.prefAutoSyncLibrary, !preAutoSyncLibrary).apply();
                break;

            case SettingsManager.prefUpdateLibraryOnlyWifi:
                final boolean preUpdateLibraryOnlyWifi = prefs.getBoolean(SettingsManager.prefUpdateLibraryOnlyWifi, false);
                prefs.edit().putBoolean(SettingsManager.prefUpdateLibraryOnlyWifi, !preUpdateLibraryOnlyWifi).apply();
                break;

            case SettingsManager.prefSyncPlaylist:
                final boolean preSyncPlaylist = prefs.getBoolean(SettingsManager.prefSyncPlaylist, false);
                prefs.edit().putBoolean(SettingsManager.prefSyncPlaylist, !preSyncPlaylist).apply();
                handler.postDelayed(() -> {
                    FirebaseManager.Instance().checkforTransfers();
                }, UPLOAD_SETTLE_DELAY);
                break;

            case SettingsManager.prefSyncFavourites:
                final boolean preSyncFavourites = prefs.getBoolean(SettingsManager.prefSyncFavourites, false);
                prefs.edit().putBoolean(SettingsManager.prefSyncFavourites, !preSyncFavourites).apply();
                handler.postDelayed(() -> {
                    FirebaseManager.Instance().checkforTransfers();
                }, UPLOAD_SETTLE_DELAY);
                break;
        }

    }

    private void updateList() {

        items.clear();
        items.addAll(SettingsManager.Instance().getSettings());

        adapter = new SettingsAdapter(this, items, this);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        //This is the code to provide a sectioned list
        List<SimpleSectionedRecyclerViewAdapter.Section> sections =
                new ArrayList<>();
        //Sections
        sections.add(new SimpleSectionedRecyclerViewAdapter.Section(0, getString(R.string.general)));
        sections.add(new SimpleSectionedRecyclerViewAdapter.Section(5, getString(R.string.artwork_settings)));
        sections.add(new SimpleSectionedRecyclerViewAdapter.Section(9, getString(R.string.playback_settings)));
        sections.add(new SimpleSectionedRecyclerViewAdapter.Section(16, getString(R.string.cloud_settings)));
        //Add your adapter to the sectionAdapter
        SimpleSectionedRecyclerViewAdapter.Section[] dummy = new SimpleSectionedRecyclerViewAdapter.Section[sections.size()];
        SimpleSectionedRecyclerViewAdapter mSectionedAdapter = new
                SimpleSectionedRecyclerViewAdapter(this, R.layout.section_header, R.id.sectiontitle, adapter);
        mSectionedAdapter.setSections(sections.toArray(dummy));

        //Apply this adapter to the RecyclerView
        recyclerView.setAdapter(mSectionedAdapter);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void triggerStorageAccessFramework() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        startActivityForResult(intent, REQUEST_CODE_STORAGE_ACCESS);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS: {
                Map<String, Integer> perms = new HashMap<>();
                // Initial
                perms.put(Manifest.permission.BLUETOOTH, PackageManager.PERMISSION_GRANTED);
                // Fill with results
                for (int i = 0; i < permissions.length; i++)
                    perms.put(permissions[i], grantResults[i]);
                // Check for ACCESS_FINE_LOCATION
                if (perms.get(Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED) {
                    // All Permissions Granted

                } else {
                    // Permission Denied
                    AppController.toast(this, getString(R.string.some_permissions_were_denied));
                    finish();
                }
            }
            break;

            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
