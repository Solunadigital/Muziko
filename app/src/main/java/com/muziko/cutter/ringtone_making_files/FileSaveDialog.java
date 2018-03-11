/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.muziko.cutter.ringtone_making_files;

import android.app.Dialog;
import android.content.Context;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.muziko.R;

import java.util.ArrayList;

class FileSaveDialog extends Dialog {

	// File kinds - these should correspond to the order in which
	// they're presented in the spinner control
	private static final int FILE_KIND_MUSIC = 0;
	private static final int FILE_KIND_ALARM = 1;
	private static final int FILE_KIND_NOTIFICATION = 2;
	private static final int FILE_KIND_RINGTONE = 3;

	private Spinner mTypeSpinner;
	private EditText mFilename;
	private Message mResponse;
	private String mOriginalName;
	private ArrayList<String> mTypeArray;
	private int mPreviousSelection;
	private View.OnClickListener saveListener = new View.OnClickListener() {
		public void onClick(View view) {
			mResponse.obj = mFilename.getText();
			mResponse.arg1 = mTypeSpinner.getSelectedItemPosition();
			mResponse.sendToTarget();
			dismiss();
		}
	};
	private View.OnClickListener cancelListener = view -> dismiss();

	public FileSaveDialog(Context context,
	                      String originalName,
	                      Message response) {
		super(context);

		// Inflate our UI from its XML layout description.
		setContentView(R.layout.dialog_file_save);

		setTitle("save file");

		mTypeArray = new ArrayList<>();
		mTypeArray.add("Music Type");
		mTypeArray.add("Type Alarm");
		mTypeArray.add("Type Notification");
		mTypeArray.add("Type Ringtone");

        mFilename = findViewById(R.id.filename);
        mOriginalName = originalName;

		ArrayAdapter<String> adapter = new ArrayAdapter<>(
				context, android.R.layout.simple_spinner_item, mTypeArray);
		adapter.setDropDownViewResource(
				android.R.layout.simple_spinner_dropdown_item);
        mTypeSpinner = findViewById(R.id.ringtone_type);
        mTypeSpinner.setAdapter(adapter);
		mTypeSpinner.setSelection(FILE_KIND_RINGTONE);
		mPreviousSelection = FILE_KIND_RINGTONE;

		setFilenameEditBoxFromName(false);

		mTypeSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent,
			                           View v,
			                           int position,
			                           long id) {
				setFilenameEditBoxFromName(true);
			}

			public void onNothingSelected(AdapterView<?> parent) {
			}
		});

        Button save = findViewById(R.id.save);
        save.setOnClickListener(saveListener);
        Button cancel = findViewById(R.id.cancel);
        cancel.setOnClickListener(cancelListener);
		mResponse = response;
	}

	/**
	 * Return a human-readable name for a kind (music, alarm, ringtone, ...).
	 * These won't be displayed on-screen (just in logs) so they shouldn't
	 * be translated.
	 */
	public static String KindToName(int kind) {
		switch (kind) {
			default:
				return "Unknown";
			case FILE_KIND_MUSIC:
				return "Music";
			case FILE_KIND_ALARM:
				return "Alarm";
			case FILE_KIND_NOTIFICATION:
				return "Notification";
			case FILE_KIND_RINGTONE:
				return "Ringtone";
		}
	}

	private void setFilenameEditBoxFromName(boolean onlyIfNotEdited) {
		if (onlyIfNotEdited) {
			CharSequence currentText = mFilename.getText();
			String expectedText = mOriginalName + " " +
					mTypeArray.get(mPreviousSelection);

			if (!expectedText.contentEquals(currentText)) {
				return;
			}
		}

		int newSelection = mTypeSpinner.getSelectedItemPosition();
		String newSuffix = mTypeArray.get(newSelection);
		mFilename.setText(mOriginalName + " " + newSuffix);
		mPreviousSelection = mTypeSpinner.getSelectedItemPosition();
	}
}