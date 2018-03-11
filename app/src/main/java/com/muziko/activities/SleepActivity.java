package com.muziko.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.badoo.mobile.util.WeakHandler;
import com.google.android.gms.ads.InterstitialAd;
import com.muziko.R;
import com.muziko.common.events.NetworkEvent;
import com.muziko.common.events.ProgressEvent;
import com.muziko.common.models.QueueItem;
import com.muziko.controls.MiniPlayer;
import com.muziko.controls.WheelPicker.WheelPicker;
import com.muziko.events.BufferingEvent;
import com.muziko.helpers.Utils;
import com.muziko.manager.AppController;
import com.muziko.manager.PrefsManager;
import com.muziko.mediaplayer.PlayerConstants;
import com.muziko.service.SongService;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import static com.muziko.service.SongService.ACTION_SLEEP_STOP;
import static com.muziko.service.SongService.ACTION_SLEEP_TIME;
import static com.muziko.service.SongService.SLEEP_TIMER_STOP;

public class SleepActivity extends BaseActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

	private final WeakHandler handler = new WeakHandler();
	private SlidingUpPanelLayout slidingUpPanelLayout;
	private MiniPlayer miniPlayer;
	private MainReceiver mainReceiver;
	private FrameLayout set10Button;
	private FrameLayout set20Button;
	private FrameLayout set30Button;
	private FrameLayout set40Button;
	private FrameLayout set50Button;
	private FrameLayout set60Button;
	private MaterialDialog addDialog = null;
	private WheelPicker wheelHours;
	private WheelPicker wheelMinutes;
	private RelativeLayout startButton;
	private ImageButton addButton;
	private CheckBox playlastsong;
	private CoordinatorLayout coordinatorlayout;
	private ProgressBar progressBar;
	private TextView timerText;
	private TextView startText;
	private stopTimerReceiver receiver;
	private InterstitialAd mInterstitialAd;
	private int timerIncrement;


	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		overridePendingTransition(R.anim.slide_up, R.anim.fade_out);
		this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		setContentView(R.layout.activity_sleep);
        coordinatorlayout = findViewById(R.id.coordinatorlayout);
        Toolbar toolbar = findViewById(R.id.toolbar);

		setSupportActionBar(toolbar);
		toolbar.setTitle("Sleep Timer");
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		findViewsById();

		setupMainPlayer();

		startButton.setOnClickListener(this);
		set10Button.setOnClickListener(this);
		set20Button.setOnClickListener(this);
		set30Button.setOnClickListener(this);
		set40Button.setOnClickListener(this);
		set50Button.setOnClickListener(this);
		set60Button.setOnClickListener(this);
		timerText.setOnClickListener(this);
		addButton.setOnClickListener(this);

		playlastsong.setOnCheckedChangeListener(this);

		timerIncrement = 100;
		progressBar.setMax(timerIncrement);
		progressBar.setProgress(0);

		EventBus.getDefault().register(this);

	}

    @Override
    public void onResume() {
        super.onResume();

        mainUpdate();
        register();
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        if (miniPlayer != null) miniPlayer.onDestroy();
        unregister();
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {

        if (slidingUpPanelLayout != null &&
                (slidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED || slidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.ANCHORED)) {
            miniPlayer.close();
			miniPlayer.open();
        } else {
            finish();
        }
    }

    @Override
    public void onPause() {
        if (miniPlayer != null) {
            miniPlayer.pause();
        }
        unregister();
        super.onPause();
    }

    @Override
    public void onStart() {
        super.onStart();

    }

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onBufferingEvent(BufferingEvent event) {

		if (miniPlayer != null) {
			miniPlayer.showBufferingMessage(event.getMessage(), event.isClose());
		}
	}

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNetworkEvent(NetworkEvent networkEvent) {

        if (miniPlayer != null) {
            miniPlayer.updateUI();
        }
    }

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onProgressEvent(ProgressEvent event) {

        miniPlayer.updateProgress(event.getProgress(), event.getDuration());
    }

	private void onLayoutChanged(Float bottomMargin) {
        RelativeLayout contentlayout = findViewById(R.id.contentlayout);

		Resources resources = getResources();
		ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) contentlayout.getLayoutParams();
		params.setMargins(0, 0, 0, Utils.toPixels(resources, bottomMargin));
		contentlayout.requestLayout();
	}

	private void setupMainPlayer() {
        RelativeLayout contentlayout = findViewById(R.id.contentlayout);
        slidingUpPanelLayout = findViewById(R.id.sliding_layout);
        final LinearLayout mainPlayerLayout = findViewById(R.id.mainPlayerLayout);

		miniPlayer = new MiniPlayer(this, mainPlayerLayout, slidingUpPanelLayout, contentlayout);
	}

	private void findViewsById() {
        timerText = findViewById(R.id.timerText);
        progressBar = findViewById(R.id.progressBar);
        set10Button = findViewById(R.id.set10Button);
        set20Button = findViewById(R.id.set20Button);
        set30Button = findViewById(R.id.set30Button);
        set40Button = findViewById(R.id.set40Button);
        set50Button = findViewById(R.id.set50Button);
        set60Button = findViewById(R.id.set60Button);
        startText = findViewById(R.id.startText);
        startButton = findViewById(R.id.startButton);
        addButton = findViewById(R.id.addButton);
        playlastsong = findViewById(R.id.playlastsong);
    }

	private void mainUpdate() {
		miniPlayer.updateUI();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.clear();
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.fragment_sleep, menu);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
			case android.R.id.home:
				onBackPressed();
				return true;
			case R.id.action_mediascan:
                AppController.Instance().scanMedia(this, coordinatorlayout);
                return true;

			case R.id.action_share:
				AppController.Instance().shareApp();
				return true;
			case R.id.sharing_wifi:
				Intent shareIntent =
						new Intent(SleepActivity.this, ShareWifiActivity.class);
				startActivity(shareIntent);
				return true;
			case R.id.action_exit:
				AppController.Instance().exit();
				return true;

			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
			case R.id.set10Button:
				timerIncrement = 600;
				timerText.setText(Utils.convertMillisecondstoDuration(timerIncrement * 1000));
				progressBar.setMax(timerIncrement);
				progressBar.setProgress(0);
				updateTimer();
				break;
			case R.id.set20Button:
				timerIncrement = 1200;
				timerText.setText(Utils.convertMillisecondstoDuration(timerIncrement * 1000));
				progressBar.setMax(timerIncrement);
				progressBar.setProgress(0);
				updateTimer();
				break;
			case R.id.set30Button:
				timerIncrement = 1800;
				timerText.setText(Utils.convertMillisecondstoDuration(timerIncrement * 1000));
				progressBar.setMax(timerIncrement);
				progressBar.setProgress(0);
				updateTimer();
				break;
			case R.id.set40Button:
				timerIncrement = 2400;
				timerText.setText(Utils.convertMillisecondstoDuration(timerIncrement * 1000));
				progressBar.setMax(timerIncrement);
				progressBar.setProgress(0);
				updateTimer();
				break;
			case R.id.set50Button:
				timerIncrement = 3000;
				timerText.setText(Utils.convertMillisecondstoDuration(timerIncrement * 1000));
				progressBar.setMax(timerIncrement);
				progressBar.setProgress(0);
				updateTimer();
				break;
			case R.id.set60Button:
				timerIncrement = 3600;
				timerText.setText(Utils.convertMillisecondstoDuration(timerIncrement * 1000));
				progressBar.setMax(timerIncrement);
				progressBar.setProgress(0);
				updateTimer();
				break;

			case R.id.set70Button:
				addDialog.dismiss();
				timerIncrement = 4200;
				timerText.setText(Utils.convertMillisecondstoDuration(timerIncrement * 1000));
				progressBar.setMax(timerIncrement);
				progressBar.setProgress(0);
				updateTimer();
				break;

			case R.id.set80Button:
				addDialog.dismiss();
				timerIncrement = 4800;
				timerText.setText(Utils.convertMillisecondstoDuration(timerIncrement * 1000));
				progressBar.setMax(timerIncrement);
				progressBar.setProgress(0);
				updateTimer();
				break;

			case R.id.set90Button:
				addDialog.dismiss();
				timerIncrement = 5400;
				timerText.setText(Utils.convertMillisecondstoDuration(timerIncrement * 1000));
				progressBar.setMax(timerIncrement);
				progressBar.setProgress(0);
				updateTimer();
				break;

			case R.id.set100Button:
				addDialog.dismiss();
				timerIncrement = 6000;
				timerText.setText(Utils.convertMillisecondstoDuration(timerIncrement * 1000));
				progressBar.setMax(timerIncrement);
				progressBar.setProgress(0);
				updateTimer();
				break;

			case R.id.set110Button:
				addDialog.dismiss();
				timerIncrement = 6600;
				timerText.setText(Utils.convertMillisecondstoDuration(timerIncrement * 1000));
				progressBar.setMax(timerIncrement);
				progressBar.setProgress(0);
				updateTimer();
				break;

			case R.id.set120Button:
				addDialog.dismiss();
				timerIncrement = 7200;
				timerText.setText(Utils.convertMillisecondstoDuration(timerIncrement * 1000));
				progressBar.setMax(timerIncrement);
				progressBar.setProgress(0);
				updateTimer();
				break;

			case R.id.timerText:
//					SleepTime st = new SleepTime();
//					st.open(this);

				long hours = 0;
				long minutes;
				int seconds = timerIncrement;
				if (seconds > 3600) {
					hours = seconds / 3600;
					minutes = (seconds % 3600) / 60;
				} else {
					minutes = seconds / 60;
				}

				ArrayList<String> hoursList = new ArrayList<>();
				for (int a = 0; a < 24; a++) {
					hoursList.add(String.format("%02d", a));
				}

				ArrayList<String> minutesList = new ArrayList<>();
				for (int a = 0; a < 60; a++) {
					minutesList.add(String.format("%02d", a));
				}

				ArrayList<String> middle = new ArrayList<>();
				middle.add(":");

				View customView = LayoutInflater.from(this).inflate(R.layout.dialog_sleep_custom, null, false);
                wheelHours = customView.findViewById(R.id.wheelHours);
                wheelHours.setData(hoursList);
				wheelHours.setSelectedItemPosition((int) hours);
                wheelMinutes = customView.findViewById(R.id.wheelMinutes);
                wheelMinutes.setData(minutesList);
				wheelMinutes.setSelectedItemPosition((int) minutes);
                WheelPicker wheelMiddle = customView.findViewById(R.id.wheelMiddle);
                wheelMiddle.setData(middle);

				MaterialDialog customDialog = new MaterialDialog.Builder(this)
						.theme(Theme.LIGHT)
						.customView(customView, false)
						.positiveText("DONE").onPositive((dialog, which) -> {
							int selectedHours = wheelHours.getCurrentItemPosition() * 3600;
							int selectedMins = wheelMinutes.getCurrentItemPosition() * 60;
							timerIncrement = selectedHours + selectedMins;
							timerText.setText(Utils.convertMillisecondstoDuration(timerIncrement * 1000));
							progressBar.setMax(timerIncrement);
							progressBar.setProgress(0);
							updateTimer();
						}).negativeText("Cancel")
						.build();
				customDialog.show();
				break;

			case R.id.addButton:

				View addView = LayoutInflater.from(this).inflate(R.layout.dialog_sleep_extra, null, false);
                FrameLayout set70Button = addView.findViewById(R.id.set70Button);
                FrameLayout set80Button = addView.findViewById(R.id.set80Button);
                FrameLayout set90Button = addView.findViewById(R.id.set90Button);
                FrameLayout set100Button = addView.findViewById(R.id.set100Button);
                FrameLayout set110Button = addView.findViewById(R.id.set110Button);
                FrameLayout set120Button = addView.findViewById(R.id.set120Button);

				set70Button.setOnClickListener(this);
				set80Button.setOnClickListener(this);
				set90Button.setOnClickListener(this);
				set100Button.setOnClickListener(this);
				set110Button.setOnClickListener(this);
				set120Button.setOnClickListener(this);

				addDialog = new MaterialDialog.Builder(this)
						.theme(Theme.LIGHT)
						.customView(addView, false)
						.build();
				addDialog.show();

				break;

			case R.id.startButton:

				if (Utils.isServiceRunning(SongService.class.getName(), this)) {

					if (startText.getText().equals("START")) {

						startText.setText(R.string.STOP);
						v.setBackgroundColor(ContextCompat.getColor(this, R.color.redColor));
						PlayerConstants.SLEEP_TIMER = System.currentTimeMillis() + timerIncrement * 1000;

					} else {
						startText.setText(R.string.START);
						v.setBackgroundColor(ContextCompat.getColor(this, R.color.normal_blue));
						PlayerConstants.SLEEP_TIMER = 0;
					}
				} else {

                    AppController.toast(SleepActivity.this, "No Music Playing!");

				}
				break;
		}

	}


	private void updateTimer() {
		if (Utils.isServiceRunning(SongService.class.getName(), this) && startText.getText().equals("STOP")) {
			PlayerConstants.SLEEP_TIMER = System.currentTimeMillis() + timerIncrement * 1000;
		}
	}

	private void register() {
		receiver = new stopTimerReceiver();

		IntentFilter timerfilter = new IntentFilter(SLEEP_TIMER_STOP);
		registerReceiver(receiver, timerfilter);

		IntentFilter filter = new IntentFilter();
		filter.addAction(AppController.INTENT_EXIT);
		filter.addAction(AppController.INTENT_CLEAR);
		filter.addAction(AppController.INTENT_TRACK_EDITED);
		filter.addAction(AppController.INTENT_TRACK_SEEKED);
		filter.addAction(AppController.INTENT_QUEUE_STOPPED);
		filter.addAction(AppController.INTENT_TRACK_SHUFFLE);
		filter.addAction(AppController.INTENT_TRACK_REPEAT);
		filter.addAction(AppController.INTENT_QUEUE_CHANGED);
		filter.addAction(AppController.INTENT_QUEUE_CLEARED);

		mainReceiver = new MainReceiver();
		registerReceiver(mainReceiver, filter);
	}

	private void unregister() {
		if (receiver != null) {
			unregisterReceiver(receiver);
			receiver = null;
		}

		if (mainReceiver != null) {
			unregisterReceiver(mainReceiver);
			mainReceiver = null;
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

		if (isChecked) {
            PrefsManager.Instance().setSleepTimeLastSong(true);
        } else {
            PrefsManager.Instance().setSleepTimeLastSong(false);
        }
	}

	public class stopTimerReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equalsIgnoreCase(SLEEP_TIMER_STOP)) {

				if (intent.getLongExtra(ACTION_SLEEP_TIME, 0) > 0) {

					long progress = intent.getLongExtra(ACTION_SLEEP_TIME, 0);
					progressBar.setProgress((int) (timerIncrement - progress / 1000));
					timerText.setText(Utils.convertMillisecondstoDuration(progress));
					startText.setText(R.string.STOP);
					startButton.setBackgroundColor(ContextCompat.getColor(SleepActivity.this, R.color.redColor));

				} else if (intent.getStringExtra(ACTION_SLEEP_STOP).equals("1")) {

                    AppController.toast(SleepActivity.this, "Sleep Timer Stoped Player");

					startText.setText(R.string.START);
					startButton.setBackgroundColor(ContextCompat.getColor(SleepActivity.this, R.color.normal_blue));

				}
			}
		}
	}

	private class MainReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent != null) {
				String action = intent.getAction();

				switch (action) {
					case AppController.INTENT_TRACK_SEEKED:
						mainUpdate();
						break;
					case AppController.INTENT_QUEUE_CHANGED:
						mainUpdate();

						break;
					case AppController.INTENT_QUEUE_STOPPED:
                        miniPlayer.layoutMiniPlayer();

						break;
					case AppController.INTENT_TRACK_REPEAT:
						mainUpdate();
						break;
					case AppController.INTENT_TRACK_SHUFFLE:
						mainUpdate();
						break;
					case AppController.INTENT_QUEUE_CLEARED:
                        miniPlayer.layoutMiniPlayer();

						break;
					case AppController.INTENT_CLEAR:

						finish();
						break;
					case AppController.INTENT_EXIT:

						finish();
						break;
					case AppController.INTENT_TRACK_EDITED:
						int index = intent.getIntExtra("index", -1);
						String tag = intent.getStringExtra("tag");
						QueueItem item = (QueueItem) intent.getSerializableExtra("item");
						if (item != null) {
							mainUpdate();
						}
						break;
				}
			}
		}
	}
}
