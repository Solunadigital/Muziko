package com.muziko.activities.splash;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.muziko.R;
import com.muziko.activities.BaseActivity;
import com.muziko.activities.MainActivity;
import com.muziko.adapter.IntroViewPagerAdapter;

public class IntroActivity extends BaseActivity {
    private final String TAG = IntroActivity.class.getSimpleName();

	private RelativeLayout footer;
	private ImageView circle1;
	private ImageView circle2;
	private ImageView circle3;
	private ImageView circle4;
	private ImageView circle5;
	private int pagerCount = 0;
	private TextView nextText;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStartActivity(false);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

			getWindow().setStatusBarColor(Color.TRANSPARENT);
		}

		setContentView(R.layout.activity_intro);

        RelativeLayout skipButton = findViewById(R.id.skipButton);
        RelativeLayout nextButton = findViewById(R.id.nextButton);
        footer = findViewById(R.id.footer);

        nextText = findViewById(R.id.nextText);
        TextView skipText = findViewById(R.id.skipText);

        circle1 = findViewById(R.id.splashCircle1);
        circle2 = findViewById(R.id.splashCircle2);
        circle3 = findViewById(R.id.splashCircle3);
        circle4 = findViewById(R.id.splashCircle4);
        circle5 = findViewById(R.id.splashCircle5);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			Window window = getWindow();
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			window.setStatusBarColor(ContextCompat.getColor(IntroActivity.this, R.color.dark_blue));
		}

        final ViewPager pager = findViewById(R.id.vpPager);
        pager.setAdapter(new IntroViewPagerAdapter(getSupportFragmentManager()));

		float scale = getResources().getDisplayMetrics().density;
		int padding = (int) (10 * scale + 0.5f);

		pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

				switch (position) {
					case 0:
						pagerCount = 0;
						nextText.setText(R.string.next);

//						circle1.setPadding(0, 0, 0, 0);
//						circle2.setPadding(padding, padding, padding, padding);
//						circle3.setPadding(padding, padding, padding, padding);
//						circle4.setPadding(padding, padding, padding, padding);
//						circle5.setPadding(padding, padding, padding, padding);
						break;
					case 1:
						pagerCount = 1;
						nextText.setText(R.string.next);

//						circle1.setPadding(padding, padding, padding, padding);
//						circle2.setPadding(0, 0, 0, 0);
//						circle3.setPadding(padding, padding, padding, padding);
//						circle4.setPadding(padding, padding, padding, padding);
//						circle5.setPadding(padding, padding, padding, padding);

						break;
					case 2:
						pagerCount = 3;
						nextText.setText(R.string.next);

//						circle1.setPadding(padding, padding, padding, padding);
//						circle2.setPadding(padding, padding, padding, padding);
//						circle3.setPadding(0, 0, 0, 0);
//						circle4.setPadding(padding, padding, padding, padding);
//						circle5.setPadding(padding, padding, padding, padding);

						break;
					case 3:
						pagerCount = 4;
						nextText.setText(R.string.next);

//						circle1.setPadding(padding, padding, padding, padding);
//						circle2.setPadding(padding, padding, padding, padding);
//						circle3.setPadding(padding, padding, padding, padding);
//						circle4.setPadding(0, 0, 0, 0);
//						circle5.setPadding(padding, padding, padding, padding);

						break;
					case 4:
						pagerCount = 5;
						nextText.setText(R.string.gotit);

//						circle1.setPadding(padding, padding, padding, padding);
//						circle2.setPadding(padding, padding, padding, padding);
//						circle3.setPadding(padding, padding, padding, padding);
//						circle3.setPadding(padding, padding, padding, padding);
//						circle5.setPadding(0, 0, 0, 0);

						break;
				}

			}

			@Override
			public void onPageSelected(int position) {

			}

			@Override
			public void onPageScrollStateChanged(int state) {

			}
		});

		nextButton.setOnClickListener(v -> {
			if (pagerCount != 5) {
				pager.setCurrentItem(pagerCount + 1);
			} else {

				Log.e(TAG, "main start");

				footer.animate()
						.translationY(footer.getHeight())
						.setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime));

				startActivity(new Intent(IntroActivity.this, MainActivity.class));
				finish();
			}

		});

		skipButton.setOnClickListener(v -> {

			footer.animate()
					.translationY(footer.getHeight())
					.setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime));

			startActivity(new Intent(IntroActivity.this, MainActivity.class));
			finish();
		});

	}

}
