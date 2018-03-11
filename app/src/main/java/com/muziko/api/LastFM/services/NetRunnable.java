package com.muziko.api.LastFM.services;

import android.content.Context;
import android.content.Intent;


abstract class NetRunnable implements Runnable {

	private final NetApp mNetApp;
	private final Context mContext;
	private final Networker mNetworker;

	NetRunnable(NetApp napp, Context ctx, Networker net) {
		super();
		this.mNetApp = napp;
		this.mContext = ctx;
		this.mNetworker = net;
	}

	NetApp getNetApp() {
		return mNetApp;
	}

	Context getContext() {
		return mContext;
	}

	Networker getNetworker() {
		return mNetworker;
	}

	void notifyStatusUpdate() {
		Intent i = new Intent(ScrobblingService.BROADCAST_ONSTATUSCHANGED);
		i.putExtra("netapp", mNetApp.getIntentExtraValue());
		mContext.sendBroadcast(i);
	}

	@Override
	public abstract void run();

}
