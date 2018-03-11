package com.muziko.receivers;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.crashlytics.android.Crashlytics;
import com.muziko.manager.AppController;
import com.muziko.manager.SettingsManager;
import com.muziko.mediaplayer.MuzikoExoPlayer;
import com.muziko.mediaplayer.PlayerConstants;

/** Created by dev on 23/08/2016. */
public class BluetoothReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        try {

            String action = intent.getAction();

            boolean prefBluetooth = SettingsManager.Instance().getPrefs().getBoolean("prefBluetooth", false);

            if (prefBluetooth) {
                if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                    // LOG - Connected
                    if (PlayerConstants.QUEUE_STATE != PlayerConstants.QUEUE_STATE_STOPPED) {
                        AppController.Instance().serviceResume(false);
                    }
                } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                    // LOG - Disconnected
                    if (MuzikoExoPlayer.Instance().isPlaying()) {
                        AppController.Instance().servicePause();
                    }
                }
            }

        } catch (Exception ex) {
            Crashlytics.logException(ex);
        }
    }
}
