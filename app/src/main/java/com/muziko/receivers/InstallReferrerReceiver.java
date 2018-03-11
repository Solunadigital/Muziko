package com.muziko.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.muziko.manager.PrefsManager;

/**
 * Created by dev on 29/09/2016.
 */

public class InstallReferrerReceiver extends BroadcastReceiver {

	private final String TAG = InstallReferrerReceiver.class.getSimpleName();

	private String referrer;

	@Override
	public void onReceive(final Context context, Intent intent) {

		referrer = intent.getStringExtra("referrer");
		if (referrer != null) {
			if (!referrer.contains("utm_source=")) {
                PrefsManager.Instance().setRegisterReferrer(referrer);
            }
		}

	}
}