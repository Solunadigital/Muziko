package com.muziko.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.muziko.manager.AppController;
import com.muziko.manager.SettingsManager;
import com.muziko.mediaplayer.MuzikoExoPlayer;
import com.muziko.mediaplayer.PlayerConstants;

/** Created by dev on 23/08/2016. */
public class HeadsetReceiver extends BroadcastReceiver {

    private final String TAG = "HeadsetReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        try {

            boolean prefHeadset = SettingsManager.Instance().getPrefs().getBoolean("prefHeadset", false);

            if (prefHeadset) {
                if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
                    int state = intent.getIntExtra("state", -1);
                    switch (state) {
                        case 0:
                            Log.d(TAG, "Headset is unplugged");
                            if (MuzikoExoPlayer.Instance().isPlaying()) {
                                AppController.Instance().servicePause();
                            }
                            break;
                        case 1:
                            Log.d(TAG, "Headset is plugged");
                            if (PlayerConstants.QUEUE_STATE
                                    != PlayerConstants.QUEUE_STATE_STOPPED) {
                                AppController.Instance().serviceResume(false);
                            }
                            break;
                        default:
                            Log.d(TAG, "I have no idea what the headset state is");
                    }
                }
            }

        } catch (Exception ex) {
            Crashlytics.logException(ex);
        }
    }
}
