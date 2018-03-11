package com.muziko.dialogs;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.muziko.R;
import com.muziko.common.models.QueueItem;
import com.muziko.helpers.QueueHelper;
import com.muziko.manager.AppController;
import com.muziko.manager.PrefsManager;
import com.muziko.mediaplayer.PlayerConstants;

public class RemoveAfterExisting implements View.OnClickListener {
	private static final String TAG = RemoveAfterExisting.class.getSimpleName();


	private MaterialDialog createDialog = null;
	private Button oneButton;
	private Button twoButton;
	private Button threeButton;
	private Button fourButton;
	private Button fiveButton;
	private Button sixButton;
	private Button sevenButton;
	private Button eightButton;
	private Context mContext;
	private int removeAfter;

	public void open(Context context, int index) {
		mContext = context;

		View view = LayoutInflater.from(context).inflate(R.layout.dialog_remove_after, null, false);

        oneButton = view.findViewById(R.id.oneButton);
        twoButton = view.findViewById(R.id.twoButton);
        threeButton = view.findViewById(R.id.threeButton);
        fourButton = view.findViewById(R.id.fourButton);
        fiveButton = view.findViewById(R.id.fiveButton);
        sixButton = view.findViewById(R.id.sixButton);
        sevenButton = view.findViewById(R.id.sevenButton);
        eightButton = view.findViewById(R.id.eightButton);

		oneButton.setOnClickListener(this);
		twoButton.setOnClickListener(this);
		threeButton.setOnClickListener(this);
		fourButton.setOnClickListener(this);
		fiveButton.setOnClickListener(this);
		sixButton.setOnClickListener(this);
		sevenButton.setOnClickListener(this);
		eightButton.setOnClickListener(this);

		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		removeAfter = prefs.getInt("prefHideSongDuration", 0);

		updateUI();

		createDialog = new MaterialDialog.Builder(mContext)
				.theme(Theme.LIGHT).titleColorRes(R.color.normal_blue)
				.negativeColorRes(R.color.normal_blue)
				.customView(view, false)
				.positiveColorRes(R.color.normal_blue)
				.positiveText("Save").onPositive((dialog, which) -> {

					QueueItem queueItem = PlayerConstants.QUEUE_LIST.get(index);

                    final long level = PrefsManager.Instance().getQueueLevel();
                    queueItem.level = level;
					queueItem.order = index;
					queueItem.played = 0;
					queueItem.removeafter = removeAfter;

					PlayerConstants.QUEUE_LIST.set(index, queueItem);
                    QueueHelper.saveQueue(context);

					context.sendBroadcast(new Intent(AppController.INTENT_QUEUE_CHANGED));
                    AppController.toast(context, "Remove after updated!");

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
		switch (removeAfter) {
			case 0:
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
		if (v == oneButton) {
			removeAfter = 1;
		} else if (v == twoButton) {
			removeAfter = 2;
		} else if (v == threeButton) {
			removeAfter = 3;
		} else if (v == fourButton) {
			removeAfter = 4;
		} else if (v == fiveButton) {
			removeAfter = 5;
		} else if (v == sixButton) {
			removeAfter = 6;
		} else if (v == sevenButton) {
			removeAfter = 7;
		} else if (v == eightButton) {
			removeAfter = 8;
		}
		updateUI();
	}
}
