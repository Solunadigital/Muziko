package com.muziko.dialogs;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.muziko.R;

public class SleepTime implements AdapterView.OnItemClickListener, View.OnClickListener, TextWatcher {
	private static final String TAG = SleepTime.class.getSimpleName();

	private StringBuilder sb = new StringBuilder();

	private RelativeLayout createNewButton;
	private MaterialDialog createDialog = null;

	public void open(Context context) {
		Context context1 = context;

		View view = LayoutInflater.from(context).inflate(R.layout.dialog_sleep_timer_manual, null, false);

        EditText editTime = view.findViewById(R.id.editTime);

		editTime.addTextChangedListener(this);

        createNewButton = view.findViewById(R.id.createNewButton);
        createNewButton.setOnClickListener(this);

		createDialog = new MaterialDialog.Builder(context)
				.theme(Theme.LIGHT).titleColorRes(R.color.normal_blue)
				.negativeColorRes(R.color.dialog_negetive_button)
				//.title("Add To Playlist")
				.customView(view, false)
				.build();

		createDialog.show();
	}

	private void close() {
		if (createDialog != null) {
			createDialog.dismiss();
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
	}

	@Override
	public void onClick(View v) {
		if (v == createNewButton) {
			close();
		}
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		Log.e(TAG, "before:" + s + " s:" + start + " c: " + count + " a:" + after);
	}

	@Override

	public void onTextChanged(CharSequence s, int start, int before, int count) {
		Log.e(TAG, "change:" + s + " s:" + start + " c: " + count + " b:" + before);
	}

	@Override
	public void afterTextChanged(Editable editable) {
		Log.e(TAG, "after:" + editable.toString());

	}
}
