package com.muziko.dialogs;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.muziko.R;
import com.muziko.manager.PrefsManager;

/**
 * Created by dev on 22/08/2016.
 */
public class RateUs implements View.OnClickListener {

	private Context context;
	private AlertDialog rateUsDialog = null;
	private TextView rateNow;
	private TextView later;

	public void open(Context context) {
		this.context = context;


		View view = LayoutInflater.from(context).inflate(R.layout.dialog_rate_us, null, false);
        rateNow = view.findViewById(R.id.rateNow);
        later = view.findViewById(R.id.later);

		rateNow.setOnClickListener(this);
		later.setOnClickListener(this);

		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this.context);
		dialogBuilder.setView(view);
		rateUsDialog = dialogBuilder.create();
		rateUsDialog.show();

		rateUsDialog.show();
	}

	public void close() {

		if (rateUsDialog != null) {
			rateUsDialog.dismiss();
		}
	}


	@Override
	public void onClick(View v) {
		if (v == rateNow) {
			try {
				context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + context.getPackageName())));
				rateUsDialog.dismiss();
			} catch (android.content.ActivityNotFoundException anfe) {
				context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + context.getPackageName())));
				rateUsDialog.dismiss();
			}

            PrefsManager.Instance().setRateShowDone(true);
        } else {
			rateUsDialog.dismiss();
		}
	}
}
