package com.muziko.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.muziko.manager.AppController;
import com.muziko.service.SongService;

/**
 * Created by dev on 7/07/2016.
 */
public class MuzikoAlarmReceiver extends BroadcastReceiver {


    // Triggered by the Alarm periodically (starts the service to run task)
    @Override
    public void onReceive(Context context, Intent intent) {

        if (!AppController.Instance().isMyServiceRunning(SongService.class)) {
            context.startService(new Intent(context, SongService.class));
        }
    }
}
