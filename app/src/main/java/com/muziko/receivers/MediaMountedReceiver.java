package com.muziko.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import com.crashlytics.android.Crashlytics;
import com.muziko.manager.AppController;
import com.muziko.manager.MediaHelper;
import com.muziko.manager.PrefsManager;

/**
 * Created by dev on 21/07/2016.
 */
public class MediaMountedReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		try {

			String action = intent.getAction();
			if (action.equalsIgnoreCase(Intent.ACTION_MEDIA_MOUNTED)) {

				if (System.currentTimeMillis() - SystemClock.elapsedRealtime() > 120000) {
                    AppController.toast(context, "SD Card inserted");
//					PrefsManager.Instance().setHasSD(context, true);
                    PrefsManager.Instance().setNeedsUpdate(true);
                    MediaHelper.Instance().loadMusicWrapper(false);
				}

			}
		} catch (Exception ex) {
			Crashlytics.logException(ex);
		}
	}
}
