package com.muziko.api.LastFM.services;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.muziko.R;
import com.muziko.activities.SettingsActivity;


public class ForegroundHide extends Service {

	private Context mCtx = this;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent i, int flags, int startId) {
		//Bundle extras = i.getExtras();


		Intent targetIntent = new Intent(mCtx, SettingsActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(mCtx, 0, targetIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		NotificationCompat.Builder builder =
				new NotificationCompat.Builder(mCtx)
						.setContentTitle("")
//                        .setSmallIcon(R.mipmap.ic_notify)
						.setContentText("")
						.setContentIntent(contentIntent);

		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB_MR2) {
			builder.setLargeIcon(BitmapFactory.decodeResource(mCtx.getResources(),
					R.mipmap.ic_launcher));
		}

		this.startForeground(14619, builder.build());

		this.stopForeground(true);
		return Service.START_NOT_STICKY;
	}
}
