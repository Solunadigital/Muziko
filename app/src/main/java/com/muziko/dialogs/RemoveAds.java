package com.muziko.dialogs;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.muziko.R;

class RemoveAds implements View.OnClickListener {
	private final Context context;
	private final RemoveAdsListener listener;
	private RelativeLayout buyButton;
	private MaterialDialog dialog = null;

	public RemoveAds(Context context, RemoveAdsListener listener) {
		this.context = context;
		this.listener = listener;
	}

	public void open() {
		View view = LayoutInflater.from(context).inflate(R.layout.dialog_remove_ads, null, false);

        ProgressBar progressBar = view.findViewById(R.id.progressBar);

        buyButton = view.findViewById(R.id.buyButton);
        buyButton.setOnClickListener(this);

		dialog = new MaterialDialog.Builder(context)
				.theme(Theme.LIGHT).titleColorRes(R.color.normal_blue)
				.negativeColorRes(R.color.dialog_negetive_button)
				.customView(view, false)
				.build();

		dialog.show();
	}

	private void close() {
		if (dialog != null) {
			dialog.dismiss();
		}
	}

	@Override
	public void onClick(View v) {
		if (v == buyButton) {
			if (listener != null) listener.onRemoveAdsOk();

			close();
		}
	}

	public interface RemoveAdsListener {
		void onRemoveAdsOk();
	}
}
