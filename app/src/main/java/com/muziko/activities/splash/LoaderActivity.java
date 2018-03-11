package com.muziko.activities.splash;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import android.view.View;

import com.muziko.MyApplication;
import com.muziko.R;
import com.muziko.activities.MainActivity;
import com.muziko.activities.StorageActivity;
import com.muziko.helpers.Utils;
import com.muziko.manager.AppController;
import com.muziko.manager.PrefsManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hugo.weaving.DebugLog;

import static com.muziko.manager.MuzikoConstants.REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS;

public class LoaderActivity extends Activity {

    @DebugLog
    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			getWindow().setStatusBarColor(Color.TRANSPARENT);
		}
		MyApplication.showArtwork = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("prefShowArtwork", true);
		check();

	}

    @DebugLog
    @Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		switch (requestCode) {
            case REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS:
                {
                    Map<String, Integer> perms = new HashMap<>();
                    // Initial
                    perms.put(
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            PackageManager.PERMISSION_GRANTED);
                    perms.put(
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            PackageManager.PERMISSION_GRANTED);
                    perms.put(
                            Manifest.permission.READ_PHONE_STATE,
                            PackageManager.PERMISSION_GRANTED);
                    perms.put(Manifest.permission.RECORD_AUDIO, PackageManager.PERMISSION_GRANTED);
                    perms.put(
                            Manifest.permission.MODIFY_AUDIO_SETTINGS,
                            PackageManager.PERMISSION_GRANTED);
                    // Fill with results
                    for (int i = 0; i < permissions.length; i++)
                        perms.put(permissions[i], grantResults[i]);
                    // Check for ACCESS_FINE_LOCATION
                    if (perms.get(Manifest.permission.READ_EXTERNAL_STORAGE)
                                    == PackageManager.PERMISSION_GRANTED
                            &&perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                    == PackageManager.PERMISSION_GRANTED
                            && perms.get(Manifest.permission.READ_PHONE_STATE)
                                    == PackageManager.PERMISSION_GRANTED
                            && perms.get(Manifest.permission.RECORD_AUDIO)
                                    == PackageManager.PERMISSION_GRANTED
                            && perms.get(Manifest.permission.MODIFY_AUDIO_SETTINGS)
                                    == PackageManager.PERMISSION_GRANTED) {
                        // All Permissions Granted
                        MyApplication.getInstance().load(this);
                        load();
                    } else {
                        // Permission Denied
                        AppController.toast(this, "Some permissions were denied");
                        finish();
                    }
                }
                break;

			default:
				super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		}
	}

    @DebugLog
    private void load() {

//		Map<String, ?> prefs = PreferenceManager.getDefaultSharedPreferences(
//				this).getAll();
//		for (String key : prefs.keySet()) {
//			Object pref = prefs.get(key);
//			String printVal = "";
//			if (pref instanceof Boolean) {
//				printVal =  key + " : " + (Boolean) pref;
//			}
//			if (pref instanceof Float) {
//				printVal =  key + " : " + (Float) pref;
//			}
//			if (pref instanceof Integer) {
//				printVal =  key + " : " + (Integer) pref;
//			}
//			if (pref instanceof Long) {
//				printVal =  key + " : " + (Long) pref;
//			}
//			if (pref instanceof String) {
//				printVal =  key + " : " + (String) pref;
//			}
//			if (pref instanceof Set<?>) {
//				printVal =  key + " : " + (Set<String>) pref;
//			}
//
//			// create a TextView with printVal as text and add to layout
//		}


        if (PrefsManager.Instance().getLoginCount() > 0) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			String prefStartScreen = prefs.getString("prefStartScreen", "");

			Class<?> activityClass;
			Intent intent;
			switch (prefStartScreen) {
				case "Last opened":

					try {
                        activityClass = Class.forName(PrefsManager.Instance().getLastActivity());
                    } catch (ClassNotFoundException ex) {
						activityClass = MainActivity.class;
					}

					if (activityClass == MainActivity.class) {
						intent = new Intent(this, MainActivity.class);
						startActivity(intent);
						finish();
					} else {
						ContextCompat.startActivities(this,
								new Intent[]
										{
												new Intent(this, MainActivity.class),
												new Intent(this, activityClass),
										});
						finish();
					}
					break;
				case "Songs":
					intent = new Intent(this, MainActivity.class);
					startActivity(intent);
					finish();
					break;
				case "AlbumsFragment":
					intent = new Intent(this, MainActivity.class);
					startActivity(intent);
					finish();
					break;
				case "ArtistsFragment":
					intent = new Intent(this, MainActivity.class);
					startActivity(intent);
					finish();
					break;
				case "GenresFragment":
					intent = new Intent(this, MainActivity.class);
					startActivity(intent);
					finish();
					break;
				case "Folders":
					ContextCompat.startActivities(this,
							new Intent[]
									{
											new Intent(this, MainActivity.class),
                                            new Intent(this, StorageActivity.class),
                                    });
					finish();
					break;
				default:
					try {
                        activityClass = Class.forName(PrefsManager.Instance().getLastActivity());
                    } catch (ClassNotFoundException ex) {
						activityClass = MainActivity.class;
					}

					if (activityClass == MainActivity.class) {
						intent = new Intent(this, MainActivity.class);
						startActivity(intent);
						finish();
					} else {
						ContextCompat.startActivities(this,
								new Intent[]
										{
												new Intent(this, MainActivity.class),
												new Intent(this, activityClass),
										});
						finish();
					}
					break;
			}
		} else {

			Intent intent = new Intent(this, SplashActivity.class);
			startActivity(intent);
			finish();
		}
	}

    @DebugLog
    private void check() {
		// Here, thisActivity is the current activity
		List<String> permissionsNeeded = new ArrayList<>();

		final List<String> permissionsList = new ArrayList<>();
		if (!addPermission(permissionsList, Manifest.permission.READ_EXTERNAL_STORAGE))
			permissionsNeeded.add("Read Storage");
		if (!addPermission(permissionsList, Manifest.permission.WRITE_EXTERNAL_STORAGE))
			permissionsNeeded.add("Write Storage");
		if (!addPermission(permissionsList, Manifest.permission.READ_PHONE_STATE))
			permissionsNeeded.add("Read Phone");
		if (!addPermission(permissionsList, Manifest.permission.RECORD_AUDIO))
			permissionsNeeded.add("Microphone");
		if (!addPermission(permissionsList, Manifest.permission.MODIFY_AUDIO_SETTINGS))
			permissionsNeeded.add("Audio");
		if (permissionsList.size() > 0) {
			if (permissionsNeeded.size() > 0) {
				// Need Rationale
				String message = "You need to grant access to " + permissionsNeeded.get(0);
				for (int i = 1; i < permissionsNeeded.size(); i++)
					message = message + ", " + permissionsNeeded.get(i);

				Utils.alertNoDismiss(this, getString(R.string.app_name), message, () -> {
					// No explanation needed, we can request the permission.
					ActivityCompat.requestPermissions(LoaderActivity.this, permissionsList.toArray(new String[permissionsList.size()]),
							REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
				});

				return;
			}
			ActivityCompat.requestPermissions(LoaderActivity.this, permissionsList.toArray(new String[permissionsList.size()]),
					REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
		} else {
            MyApplication.getInstance().load(this);
			load();
		}
	}

    @DebugLog
    private boolean addPermission(List<String> permissionsList, String permission) {
		if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
			permissionsList.add(permission);
			// Check for Rationale Option
			if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permission))
				return false;
		}
		return true;
	}

}
