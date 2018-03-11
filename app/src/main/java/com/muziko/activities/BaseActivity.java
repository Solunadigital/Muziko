package com.muziko.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
//import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.akexorcist.localizationactivity.LocalizationActivity;
import com.arasthel.asyncjob.AsyncJob;
import com.badoo.mobile.util.WeakHandler;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;
import com.google.firebase.iid.FirebaseInstanceId;
import com.jaredrummler.android.device.DeviceName;
import com.muziko.R;
import com.muziko.billing.Premium;
import com.muziko.common.events.FirebaseCloudEvent;
import com.muziko.common.events.FirebaseRefreshEvent;
import com.muziko.common.models.QueueItem;
import com.muziko.common.models.firebase.Registration;
import com.muziko.database.TrackRealmHelper;
import com.muziko.fragments.Listening.TracksFragment;
import com.muziko.helpers.Utils;
import com.muziko.manager.AppController;
import com.muziko.manager.CloudManager;
import com.muziko.manager.FirebaseManager;
import com.muziko.manager.MuzikoConstants;
import com.muziko.manager.PrefsManager;
import com.muziko.manager.ThreadManager;
import com.muziko.service.SongService;

import org.aviran.cookiebar2.CookieBar;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import hugo.weaving.DebugLog;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static com.muziko.manager.AppController.ACTION_FIREBASE_OVERLIMIT;
import static com.muziko.manager.AppController.ACTION_FIREBASE_OVERLIMIT_NOW;
import static com.muziko.manager.AppController.INTENT_SHARE_DOWNLOADED;
import static com.muziko.manager.MuzikoConstants.REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS;
import static com.muziko.manager.MuzikoConstants.REQUEST_CODE_STORAGE_ACCESS;
import static com.muziko.manager.MuzikoConstants.dayMilliseconds;

public class BaseActivity extends LocalizationActivity implements Premium.onUpdatedListener, GoogleApiClient.OnConnectionFailedListener {
    private static BaseActivity instance;
    private final String TAG = BaseActivity.class.getSimpleName();
    private Runnable pendingRunnable;
    private WeakHandler handler = new WeakHandler();
    private PermissionsListener permissionsListener;
    private boolean startActivity = true;
    private Premium premium;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private GoogleApiClient mGoogleApiClient;
    private BaseReceiver baseReceiver;


    public static BaseActivity getBaseActivityInstance() {

        return instance;

    }

    public boolean isStartActivity() {
        return startActivity;
    }

    public void setStartActivity(boolean startActivity) {
        this.startActivity = startActivity;
    }

    public PermissionsListener getPermissionsListener() {
        return permissionsListener;
    }

    public void setPermissionsListener(PermissionsListener permissionsListener) {
        this.permissionsListener = permissionsListener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        overridePendingTransition(R.anim.slide_up, R.anim.fade_out);

        setupListeners();

        CloudManager.Instance().initActivityReference(this);
    }

    @DebugLog
    @Override
    public void onResume() {
        super.onResume();

        if (startActivity) {
            PrefsManager.Instance().setLastActivity(getClass().getName());
        }

        if (!AppController.Instance().isMyServiceRunning(SongService.class)) {
            startService(new Intent(getBaseContext(), SongService.class));
        }

        register();
    }

    @DebugLog
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @DebugLog
    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    public void onDestroy() {
        premium.destroy();
        super.onDestroy();
    }

    @DebugLog
    private void signInAnonymously() {

        mAuth.signInAnonymously()
                .addOnCompleteListener(
                        this,
                        task -> {
                            Log.d(TAG, "signInAnonymously:onComplete:" + task.isSuccessful());
                            // If sign in fails, display a message to the user. If sign in succeeds
                            // the auth state listener will be notified and logic to handle the
                            // signed in user can be handled in the listener.
                            if (!task.isSuccessful()) {
                                Log.w(TAG, "signInAnonymously", task.getException());
                            }
                        });
    }

    @DebugLog
    public void checkStoragePermissions(String message) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                && PrefsManager.Instance().getStoragePermsURi().isEmpty()) {
            new MaterialDialog.Builder(this)
                    .theme(Theme.LIGHT)
                    .titleColorRes(R.color.normal_blue)
                    .negativeColorRes(R.color.dialog_negetive_button)
                    .positiveColorRes(R.color.normal_blue)
                    .title("Grant SD card permissions")
                    .content(message)
                    .positiveText("OK")
                    .onPositive((dialog, which) -> triggerStorageAccessFramework())
                    .negativeText("Cancel")
                    .show();
        }
    }

    @DebugLog
    public void checkStoragePermissions(String message, Runnable runnable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                && PrefsManager.Instance().getStoragePermsURi().isEmpty()) {
            pendingRunnable = runnable;
            new MaterialDialog.Builder(this)
                    .theme(Theme.LIGHT)
                    .titleColorRes(R.color.normal_blue)
                    .negativeColorRes(R.color.dialog_negetive_button)
                    .positiveColorRes(R.color.normal_blue)
                    .title("Grant SD card permissions")
                    .content(message)
                    .positiveText("OK")
                    .onPositive((dialog, which) -> triggerStorageAccessFramework())
                    .negativeText("Cancel")
                    .show();

        } else {
            runnable.run();
        }
    }

    @DebugLog
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void triggerStorageAccessFramework() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        startActivityForResult(intent, REQUEST_CODE_STORAGE_ACCESS);
    }

    @DebugLog
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint("NewApi")
    @Override
    public void onActivityResult(
            final int requestCode, final int resultCode, final Intent resultData) {
        if (requestCode == REQUEST_CODE_STORAGE_ACCESS) {
            Uri treeUri;
            if (resultCode == Activity.RESULT_OK) {
                // Get Uri from Storage Access Framework.
                treeUri = resultData.getData();

                // Persist URI in shared preference so that you can use it later.
                // Use your own framework here instead of PreferenceUtil.
                String uri = String.valueOf(treeUri);
                PrefsManager.Instance().setStoragePermsURI(uri);

                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean("prefStoragePerms", true);
                editor.apply();

                grantUriPermission(
                        getPackageName(),
                        treeUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                getContentResolver()
                        .takePersistableUriPermission(
                                treeUri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                                        | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                if (pendingRunnable != null) {
                    pendingRunnable.run();
                }
            }
        }

        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            if (fragment instanceof TracksFragment) {
                fragment.onActivityResult(requestCode, resultCode, resultData);
            }
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregister();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFirebaseCloudEvent(FirebaseCloudEvent event) {

        /*new MaterialDialog.Builder(BaseActivity.this)
                .theme(Theme.LIGHT)
                .titleColorRes(R.color.normal_blue)
                .negativeColorRes(R.color.dialog_negetive_button)
                .positiveColorRes(R.color.normal_blue)
                .title(event.getTitle())
                .content(event.getMessage())
                .positiveText("OK")
                .show();*/
    }

    @DebugLog
    private void unregister() {
        if (baseReceiver != null) {
            unregisterReceiver(baseReceiver);
            baseReceiver = null;
        }
    }

    @DebugLog
    private void register() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(AppController.INTENT_SHARE_DOWNLOADED);
        filter.addAction(AppController.ACTION_FIREBASE_OVERLIMIT);
        filter.addAction(AppController.ACTION_FIREBASE_OVERLIMIT_NOW);
        baseReceiver = new BaseReceiver();
        registerReceiver(baseReceiver, filter);
    }

    @DebugLog
    private void setupListeners() {
        AsyncJob.doInBackground(
                () -> {
                    // Configure Google Sign In
                    GoogleSignInOptions gso =
                            new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                    .requestIdToken(getString(R.string.server_client_id))
                                    .requestEmail()
                                    .build();

                    mGoogleApiClient =
                            new GoogleApiClient.Builder(this)
                                    .enableAutoManage(
                                            this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                                    .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                                    .build();

                    mAuth = FirebaseAuth.getInstance();
                    mAuthListener =
                            firebaseAuth -> {
                                if (FirebaseManager.Instance().isAuthenticated() && FirebaseInstanceId.getInstance().getToken() != null) {
                                    FirebaseManager.sendRegistrationToServer(FirebaseInstanceId.getInstance().getToken());
                                    check();
                                    Log.d(TAG, "onAuthStateChanged:signed_in:" + FirebaseAuth.getInstance().getCurrentUser().getUid());
                                } else {
                                    signInAnonymously();
                                    Log.d(TAG, "onAuthStateChanged:signed_out");
                                }
                            };

                    mAuth.addAuthStateListener(mAuthListener);

                    premium = new Premium(BaseActivity.this, this);
                    premium.initBillProcessor();
                }, ThreadManager.Instance().getMuzikoBackgroundThreadPool());
    }


    @DebugLog
    private void check() {
        // Here, thisActivity is the current activity
        List<String> permissionsNeeded = new ArrayList<>();

        final List<String> permissionsList = new ArrayList<>();
        if (!addPermission(permissionsList, Manifest.permission.READ_CONTACTS))
            permissionsNeeded.add("Read Contacts");
        if (permissionsList.size() > 0) {
            if (permissionsNeeded.size() > 0) {
                // Need Rationale
                String message = "You need to grant access to " + permissionsNeeded.get(0);
                for (int i = 1; i < permissionsNeeded.size(); i++)
                    message = message + ", " + permissionsNeeded.get(i);

                Utils.alertNoDismiss(this, getString(R.string.app_name), message, () -> {
                    // No explanation needed, we can request the permission.
                    ActivityCompat.requestPermissions(
                            BaseActivity.this,
                            permissionsList.toArray(new String[permissionsList.size()]),
                            REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
                });

                return;
            }
            ActivityCompat.requestPermissions(
                    BaseActivity.this,
                    permissionsList.toArray(new String[permissionsList.size()]),
                    REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
        } else {
            if (!FirebaseManager.Instance().isAnonymous() && !FirebaseManager.Instance().isFirebaseStarted()) {
                FirebaseManager.Instance().startFirebase();
            }
            MainActivity mainActivity = MainActivity.getMainActivityInstance();
            if (mainActivity != null) {
                mainActivity.updateNavDrawerHeader();
            }
        }
    }


    @DebugLog
    private void processReferrerID() {


        // DEBUG
        //		androidID = "BRAD2";

        if (PrefsManager.Instance().getRegisterReferrer() != null && !PrefsManager.Instance().getPremium()) {
            if (PrefsManager.Instance().getRegisterReferrer().equals(AppController.Instance().getAndroidID())) {
                return;
            }

            DatabaseReference regoRef =
                    FirebaseManager.Instance().getRegistrationsRef().child(PrefsManager.Instance().getRegisterReferrer());

            Registration registration =
                    new Registration(
                            AppController.Instance().getAndroidID(),
                            PrefsManager.Instance().getRegisterReferrer(),
                            DeviceName.getDeviceName(),
                            ServerValue.TIMESTAMP);

            regoRef.child(AppController.Instance().getAndroidID())
                    .setValue(
                            registration,
                            (error, firebase) -> {
                                if (error != null) {
                                    AppController.toast(
                                            BaseActivity.this,
                                            getString(R.string.one_week_premium_error));
                                    PrefsManager.Instance().setRegisterTryAgain(true);
                                } else {
                                    new MaterialDialog.Builder(BaseActivity.this)
                                            .theme(Theme.LIGHT)
                                            .titleColorRes(R.color.normal_blue)
                                            .negativeColorRes(R.color.dialog_negetive_button)
                                            .positiveColorRes(R.color.normal_blue)
                                            .title(R.string.one_week_premium)
                                            .content(R.string.one_week_premium_desc)
                                            .cancelable(false)
                                            .positiveText(R.string.buy)
                                            .onPositive((dialog, which) -> premium.buyPremium())
                                            .negativeText(R.string.ok)
                                            .show();

                                    PrefsManager.Instance().setRegisterReferrer(null);
                                    PrefsManager.Instance().setRegisterTime(
                                            System.currentTimeMillis());
                                    PrefsManager.Instance().setRegisterNotifyTime(
                                            System.currentTimeMillis());
                                }
                            });
        }
    }

    @DebugLog
    @Override
    public void onPremiumChanged() {

        //DEBUG PREMIUM
        //		PrefsManager.Instance().setRegisterReferrer(this, "6060dacf6dc155b5");
        //		PrefsManager.Instance().setRegisterReferrer(this, null);
        //		PrefsManager.Instance().setRegisterTime(this, 0);
        //      PrefsManager.Instance().setPremium(this, false);
        //		SharedPreferences prefs = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        //		prefs.edit().remove("REGISTER_NOTIFY_TIME").apply();

        BaseActivity.this.sendBroadcast(new Intent(AppController.INTENT_PREMIUM_CHANGED));

        if (!PrefsManager.Instance().getPremium()) {

            if (PrefsManager.Instance().getRegisterReferrer() != null) {
                if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                    signInAnonymously();
                }
                processReferrerID();
            } else {

                if (PrefsManager.Instance().getRegisterTime() + MuzikoConstants.weekMilliseconds
                        > System.currentTimeMillis()) {
                    if (PrefsManager.Instance().getRegisterNotifyTime()
                            + MuzikoConstants.dayMilliseconds
                            < System.currentTimeMillis()) {
                        PrefsManager.Instance().setRegisterNotifyTime(System.currentTimeMillis());
                        new MaterialDialog.Builder(BaseActivity.this)
                                .theme(Theme.LIGHT)
                                .titleColorRes(R.color.normal_blue)
                                .negativeColorRes(R.color.dialog_negetive_button)
                                .positiveColorRes(R.color.normal_blue)
                                .title("One week of Premium!")
                                .content(
                                        "Congratulations, you are a premium user now. Enjoy using 7 days ad free.")
                                .positiveText("Buy")
                                .onPositive((dialog, which) -> premium.buyPremium())
                                .negativeText("Ok")
                                .show();
                    }
                }
            }
        }

//        MainActivity mainActivity = MainActivity.getMainActivityInstance();
//        if (mainActivity != null) {
//            mainActivity.setupDrawer();
//            mainActivity.setupMainPlayer();
//        }
    }

    @DebugLog
    @Override
    public void onPurchased() {

        MainActivity mainActivity = MainActivity.getMainActivityInstance();
        if (mainActivity != null) {
            mainActivity.setupDrawer();
            mainActivity.setupMainPlayer();
        }
    }

    @DebugLog
    private boolean addPermission(List<String> permissionsList, String permission) {
        if (ContextCompat.checkSelfPermission(this, permission)
                != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);
            // Check for Rationale Option
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permission))
                return false;
        }
        return true;
    }

    public void buyPremium() {
        premium.buyPremium();
    }

    public void signOut() {

        FirebaseManager.Instance().stopFirebase();

        // Firebase sign out
        mAuth.signOut();

        // Google sign out
        Auth.GoogleSignInApi.signOut(mGoogleApiClient)
                .setResultCallback(status -> {
                    MainActivity mainActivity = MainActivity.getMainActivityInstance();
                    if (mainActivity != null) {
                        FirebaseManager.Instance().setFirebaseMe(null);
                        EventBus.getDefault().post(new FirebaseRefreshEvent(1000));
                        mainActivity.updateNavDrawerHeader();
                    }
                });


    }

    @DebugLog
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onBoxConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }

    public interface PermissionsListener {
        void permissionGranted(Runnable runnable);
    }

    private class BaseReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();

                switch (action) {
                    case INTENT_SHARE_DOWNLOADED:
                        String data = intent.getStringExtra("data");
                        QueueItem queueItem = TrackRealmHelper.getTrack(data);

                        if (queueItem != null) {
                            CookieBar.Build(BaseActivity.this)
                                    .setTitle("New track downloaded")
                                    .setMessage(queueItem.title + " - " + queueItem.artist_name)
                                    .setIcon(R.drawable.vinyl)
                                    .setIconAnimation(R.animator.iconspin)
                                    .setTitleColor(R.color.yellow)
                                    .setActionColor(R.color.yellow)
                                    .setMessageColor(R.color.white)
                                    .setBackgroundColor(R.color.normal_blue)
                                    .setDuration(5000)
                                    .setLayoutGravity(Gravity.BOTTOM)
                                    .setAction("PLAY NOW", () -> {
                                        ArrayList<QueueItem> list = new ArrayList<>();
                                        AppController.Instance().playCurrentSong(queueItem);
                                    })
                                    .show();
                        }
                        break;

                    case ACTION_FIREBASE_OVERLIMIT:
                        if (PrefsManager.Instance().getLastFirebaseOverlimitWarning() + dayMilliseconds < System.currentTimeMillis()) {
                            PrefsManager.Instance().setLastFirebaseOverlimitWarning(System.currentTimeMillis());
                            AppController.Instance().showFirebaseOverlimitDialog(BaseActivity.this);
                        }

                        break;

                    case ACTION_FIREBASE_OVERLIMIT_NOW:
                        AppController.Instance().showFirebaseOverlimitDialog(BaseActivity.this);

                        break;
                }
            }
        }
    }

  /*  @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==R.id.menu_sharing_wifi){
            Intent shareIntent =
                    new Intent(BaseActivity.this, ShareWifiActivity.class);
            startActivity(shareIntent);
        }
        return super.onOptionsItemSelected(item);
    }*/
}

