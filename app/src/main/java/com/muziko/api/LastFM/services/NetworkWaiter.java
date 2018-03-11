package com.muziko.api.LastFM.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.PowerManager;
import android.util.Log;

import com.muziko.api.LastFM.Utils.AppSettings;
import com.muziko.api.LastFM.Utils.Util;


public class NetworkWaiter extends NetRunnable {

	private static final String TAG = "NetworkWaiter";
	private boolean mWait;
	private BroadcastReceiver mConnReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			synchronized (NetworkWaiter.this) {
				Log.d(TAG, "received broadcast: " + intent.getAction()); // check for extras
				if (Util.checkForOkNetwork(getContext()) == Util.NetworkStatus.OK) {
					NetworkWaiter.this.mWait = false;
					NetworkWaiter.this.notifyAll();
				}
			}
		}
	};

	NetworkWaiter(NetApp napp, Context ctx, Networker net) {
		super(napp, ctx, net);
	}

	@Override
	public void run() {
		// register receiver
		IntentFilter ifs = new IntentFilter();
		ifs.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		ifs.addAction(AppSettings.ACTION_NETWORK_OPTIONS_CHANGED);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			ifs.addAction(PowerManager.ACTION_DEVICE_IDLE_MODE_CHANGED);
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			ifs.addAction(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED);
		}
		getContext().registerReceiver(mConnReceiver, ifs);
		Log.d(TAG, "connectivity_action");
		synchronized (this) {
			mWait = Util.checkForOkNetwork(getContext()) != Util.NetworkStatus.OK;
			while (mWait) {
				try {
					Log.d(TAG, "waiting for network connection: "
							+ getNetApp().getName());
					this.wait();
					Log.d(TAG,
							"woke up, there's probably a network connection: "
									+ getNetApp().getName());
				} catch (InterruptedException e) {
					Log.i(TAG, "Got interrupted: " + getNetApp().getName());
					Log.i(TAG, e.getMessage());
				}
			}
		}

		// unregister receiver
		getContext().unregisterReceiver(mConnReceiver);
	}
}
