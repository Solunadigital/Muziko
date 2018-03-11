package com.muziko.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.crashlytics.android.Crashlytics;
import com.muziko.manager.AppController;
import com.muziko.manager.MediaHelper;
import com.muziko.manager.PrefsManager;

/**
 * Created by dev on 22/07/2016.
 */
public class MediaUnmountedReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		try {

			String action = intent.getAction();
			if (action.equalsIgnoreCase(Intent.ACTION_MEDIA_EJECT)) {

                AppController.toast(context, "SD Card removed");

//				PrefsManager.Instance().setHasSD(context, false);
                PrefsManager.Instance().setNeedsUpdate(true);

                PrefsManager.Instance().setNeedsUpdate(true);
                MediaHelper.Instance().loadMusicWrapper(false);

			}
		} catch (Exception ex) {
			Crashlytics.logException(ex);
		}

	}
}