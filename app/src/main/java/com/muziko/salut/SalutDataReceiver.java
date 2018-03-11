package com.muziko.salut;

import android.app.Activity;
import android.content.Context;

import com.muziko.salut.Callbacks.SalutDataCallback;


public class SalutDataReceiver {

	SalutDataCallback dataCallback;
	Context context;
	Activity activity;

	public SalutDataReceiver(Activity activity, SalutDataCallback dataCallback) {
		this.dataCallback = dataCallback;
		this.context = activity.getApplicationContext();
		this.activity = activity;
	}
}
