package com.muziko.dialogs;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.AppCompatButton;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.muziko.R;
import com.muziko.api.LastFM.Utils.AppSettings;
import com.muziko.api.LastFM.Utils.MD5;
import com.muziko.api.LastFM.services.NetApp;
import com.muziko.api.LastFM.services.ScrobblingService;
import com.muziko.manager.AppController;

/**
 * Created by dev on 28/08/2016.
 */

public class LastFMLogin {
	private final NetApp mNetApp;
	private final AppSettings msettings;
	private MaterialDialog loginDialog = null;

	public LastFMLogin(NetApp NetApp, AppSettings settings) {
		mNetApp = NetApp;
		msettings = settings;
	}

	public void open(final Context context) {
		Context context1 = context;

		View view = LayoutInflater.from(context).inflate(R.layout.dialog_lastfm_login, null, false);


		loginDialog = new MaterialDialog.Builder(context)
				.theme(Theme.LIGHT).titleColorRes(R.color.normal_blue)
				.negativeColorRes(R.color.dialog_negetive_button)
				.title("Login")
				.customView(view, false).negativeText("Cancel")
				.build();

		loginDialog.show();

		View customview = loginDialog.getCustomView();
        final EditText inputUsername = customview.findViewById(R.id.input_username);
        final EditText inputPassword = customview.findViewById(R.id.input_password);
        final AppCompatButton btnLogin = customview.findViewById(R.id.btn_login);
        btnLogin.setOnClickListener(v -> {

			if (inputUsername.getText().length() > 4 && inputPassword.getText().length() > 4) {

				Intent service = new Intent(context, ScrobblingService.class);
				service.setAction(ScrobblingService.ACTION_AUTHENTICATE);
				service.putExtra("netapp", mNetApp.getIntentExtraValue());

				msettings.setUsername(mNetApp, inputUsername.getText().toString());
				msettings.setSessionKey(mNetApp, "");
				msettings.setPassword(mNetApp, inputPassword.getText().toString());
				msettings.setPwdMd5(mNetApp, MD5.getHashString(inputPassword.getText().toString()));

				context.startService(service);
				loginDialog.dismiss();
			} else {
                AppController.toast(context, "Username/Password is too short");
            }

		});
		btnLogin.setEnabled(false);
		inputUsername.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
			                          int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
			                              int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				if (inputUsername.getText().length() > 4) {
					btnLogin.setEnabled(true);
				}
			}
		});

	}

	public void close() {

		if (loginDialog != null) {
			loginDialog.dismiss();
		}
	}


}