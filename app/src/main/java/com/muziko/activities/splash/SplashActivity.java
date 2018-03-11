package com.muziko.activities.splash;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import com.badoo.mobile.util.WeakHandler;
import com.muziko.R;
import com.muziko.helpers.TabsHelper;
import com.muziko.manager.SettingsManager;
import com.skyfishjy.library.RippleBackground;

import hugo.weaving.DebugLog;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public class SplashActivity extends Activity {

	private final WeakHandler handler = new WeakHandler();

    @DebugLog
    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

			getWindow().setStatusBarColor(Color.TRANSPARENT);
		}

		setContentView(R.layout.activity_splash);
        final RippleBackground rippleBackground = findViewById(R.id.content);

		handler.postDelayed(() -> {

			rippleBackground.startRippleAnimation();
			load();

		}, 500);

	}

    @DebugLog
    @Override
	public void onDestroy() {
		if (handler != null) handler.removeCallbacksAndMessages(null);

		super.onDestroy();
	}

    @DebugLog
    private void startApp() {
		// create initial settings
		TabsHelper tabsHelper = new TabsHelper();
        tabsHelper.saveInitalTabLayout(this);
		SettingsManager.Instance().createDefaultSettings();

		startActivity(new Intent(SplashActivity.this, IntroActivity.class));
		finish();
	}

    @DebugLog
    private void load() {
		long start = System.currentTimeMillis();

		long diff = System.currentTimeMillis() - start;

		if (diff >= 5000) {
			startApp();
		} else {
			handler.postDelayed(this::startApp, 1000);
		}
	}

	@DebugLog
	@Override
	protected void attachBaseContext(Context newBase) {
		super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
	}
}
