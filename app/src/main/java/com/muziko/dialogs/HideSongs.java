package com.muziko.dialogs;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.muziko.R;
import com.muziko.common.events.RefreshEvent;

import org.greenrobot.eventbus.EventBus;

public class HideSongs implements View.OnClickListener {
	private static final String TAG = HideSongs.class.getSimpleName();


	private MaterialDialog createDialog = null;
	private Button zeroButton;
	private Button oneButton;
	private Button twoButton;
	private Button threeButton;
	private Button fourButton;
	private Button fiveButton;
	private Button sixButton;
	private Button sevenButton;
	private Button eightButton;
	private Context mContext;
	private int duration;

	public void open(Context context) {
		mContext = context;

		View view = LayoutInflater.from(context).inflate(R.layout.dialog_hide_songs, null, false);

        zeroButton = view.findViewById(R.id.zeroButton);
        oneButton = view.findViewById(R.id.oneButton);
        twoButton = view.findViewById(R.id.twoButton);
        threeButton = view.findViewById(R.id.threeButton);
        fourButton = view.findViewById(R.id.fourButton);
        fiveButton = view.findViewById(R.id.fiveButton);
        sixButton = view.findViewById(R.id.sixButton);
        sevenButton = view.findViewById(R.id.sevenButton);
        eightButton = view.findViewById(R.id.eightButton);

		zeroButton.setOnClickListener(this);
		oneButton.setOnClickListener(this);
		twoButton.setOnClickListener(this);
		threeButton.setOnClickListener(this);
		fourButton.setOnClickListener(this);
		fiveButton.setOnClickListener(this);
		sixButton.setOnClickListener(this);
		sevenButton.setOnClickListener(this);
		eightButton.setOnClickListener(this);

		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		duration = prefs.getInt("prefHideSongDuration", 0);

		updateUI();

		createDialog = new MaterialDialog.Builder(mContext)
				.theme(Theme.LIGHT).titleColorRes(R.color.normal_blue)
				.negativeColorRes(R.color.normal_blue)
				.customView(view, false)
				.positiveColorRes(R.color.normal_blue)
				.positiveText("Save").onPositive((dialog, which) -> {

					prefs.edit().putInt("prefHideSongDuration", duration).apply();
					EventBus.getDefault().post(new RefreshEvent(1000));

				}).neutralText("Cancel").build();

		createDialog.show();
	}

	private void close() {
		if (createDialog != null) {
			createDialog.dismiss();
		}
	}

	private void unHighlightButton(Button button) {
		button.setBackgroundColor(ContextCompat.getColor(mContext, R.color.transparent));
		button.setTextColor(ContextCompat.getColor(mContext, R.color.hidesongstextcolor));

	}

	private void highlightButton(Button button) {
		button.setBackgroundResource(R.drawable.hide_songs_circle_button);
		button.setTextColor(ContextCompat.getColor(mContext, R.color.white));
	}

	private void updateUI() {
		switch (duration) {
			case 0:
				highlightButton(zeroButton);
				unHighlightButton(oneButton);
				unHighlightButton(twoButton);
				unHighlightButton(threeButton);
				unHighlightButton(fourButton);
				unHighlightButton(fiveButton);
				unHighlightButton(sixButton);
				unHighlightButton(sevenButton);
				unHighlightButton(eightButton);
				break;
			case 1:
				unHighlightButton(zeroButton);
				highlightButton(oneButton);
				unHighlightButton(twoButton);
				unHighlightButton(threeButton);
				unHighlightButton(fourButton);
				unHighlightButton(fiveButton);
				unHighlightButton(sixButton);
				unHighlightButton(sevenButton);
				unHighlightButton(eightButton);
				break;
			case 2:
				unHighlightButton(zeroButton);
				unHighlightButton(oneButton);
				highlightButton(twoButton);
				unHighlightButton(threeButton);
				unHighlightButton(fourButton);
				unHighlightButton(fiveButton);
				unHighlightButton(sixButton);
				unHighlightButton(sevenButton);
				unHighlightButton(eightButton);
				break;
			case 3:
				unHighlightButton(zeroButton);
				unHighlightButton(oneButton);
				unHighlightButton(twoButton);
				highlightButton(threeButton);
				unHighlightButton(fourButton);
				unHighlightButton(fiveButton);
				unHighlightButton(sixButton);
				unHighlightButton(sevenButton);
				unHighlightButton(eightButton);
				break;
			case 4:
				unHighlightButton(zeroButton);
				unHighlightButton(oneButton);
				unHighlightButton(twoButton);
				unHighlightButton(threeButton);
				highlightButton(fourButton);
				unHighlightButton(fiveButton);
				unHighlightButton(sixButton);
				unHighlightButton(sevenButton);
				unHighlightButton(eightButton);
				break;
			case 5:
				unHighlightButton(zeroButton);
				unHighlightButton(oneButton);
				unHighlightButton(twoButton);
				unHighlightButton(threeButton);
				unHighlightButton(fourButton);
				highlightButton(fiveButton);
				unHighlightButton(sixButton);
				unHighlightButton(sevenButton);
				unHighlightButton(eightButton);
				break;
			case 6:
				unHighlightButton(zeroButton);
				unHighlightButton(oneButton);
				unHighlightButton(twoButton);
				unHighlightButton(threeButton);
				unHighlightButton(fourButton);
				unHighlightButton(fiveButton);
				highlightButton(sixButton);
				unHighlightButton(sevenButton);
				unHighlightButton(eightButton);
				break;
			case 7:
				unHighlightButton(zeroButton);
				unHighlightButton(oneButton);
				unHighlightButton(twoButton);
				unHighlightButton(threeButton);
				unHighlightButton(fourButton);
				unHighlightButton(fiveButton);
				unHighlightButton(sixButton);
				highlightButton(sevenButton);
				unHighlightButton(eightButton);
				break;
			case 8:
				unHighlightButton(zeroButton);
				unHighlightButton(oneButton);
				unHighlightButton(twoButton);
				unHighlightButton(threeButton);
				unHighlightButton(fourButton);
				unHighlightButton(fiveButton);
				unHighlightButton(sixButton);
				unHighlightButton(sevenButton);
				highlightButton(eightButton);
				break;
		}

	}

	@Override
	public void onClick(View v) {
		if (v == zeroButton) {
			duration = 0;
		} else if (v == oneButton) {
			duration = 1;
		} else if (v == twoButton) {
			duration = 2;
		} else if (v == threeButton) {
			duration = 3;
		} else if (v == fourButton) {
			duration = 4;
		} else if (v == fiveButton) {
			duration = 5;
		} else if (v == sixButton) {
			duration = 6;
		} else if (v == sevenButton) {
			duration = 7;
		} else if (v == eightButton) {
			duration = 8;
		}
		updateUI();
	}
}
