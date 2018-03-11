package com.muziko.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.badoo.mobile.util.WeakHandler;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.InterstitialAd;
import com.muziko.MyApplication;
import com.muziko.R;
import com.muziko.common.events.NetworkEvent;
import com.muziko.common.events.ProgressEvent;
import com.muziko.common.models.QueueItem;
import com.muziko.controls.MiniPlayer;
import com.muziko.events.BufferingEvent;
import com.muziko.manager.AppController;
import com.muziko.manager.PrefsManager;
import com.muziko.mediaplayer.MuzikoExoPlayer;
import com.muziko.models.EqualizerItem;
import com.nightonke.jellytogglebutton.JellyToggleButton;
import com.nightonke.jellytogglebutton.State;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import hugo.weaving.DebugLog;

public class EqualizerActivity extends BaseActivity implements SeekBar.OnSeekBarChangeListener, View.OnClickListener, View.OnLongClickListener, JellyToggleButton.OnStateChangeListener {

    private final WeakHandler handler = new WeakHandler();
    private SlidingUpPanelLayout slidingUpPanelLayout;
    private Toolbar toolbar;
    private MiniPlayer miniPlayer;
    private MainReceiver mainReceiver;
    private JellyToggleButton menuItemSwitch;
    private RelativeLayout reverbLayout;
    private RelativeLayout presetLayout;
    private TextView bassText;
    private TextView loudnessText;
    private TextView virtualizerText;
    private TextView textHz1, textHz2, textHz3, textHz4, textHz5;
    //, textHz6, textHz7, textHz8, textHz9, textHz10;
    private SeekBar seekBar1, seekBar2, seekBar3, seekBar4, seekBar5;
    //, seekBar6, seekBar7, seekBar8, seekBar9, seekBar10;
    private SeekBar bassSeekBar;
    private SeekBar loudnessSeekBar;
    private SeekBar virtualizerSeekBar;
    private MenuItem menuItemReset;
    private MenuItem menuItemAdd;
    private int currentPosition = -1;
    private InterstitialAd mInterstitialAd;
    private AdListener mAdlistener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStartActivity(true);
        setContentView(R.layout.activity_equalizer);
        overridePendingTransition(R.anim.slide_up, R.anim.fade_out);
        MuzikoExoPlayer.Instance().loadEqualizer(EqualizerActivity.this, true);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle(getString(R.string.equalizer));

        findViewsById();

        setupMainPlayer();

        boolean reverbReady = false;
        boolean spinnerReady = false;

        presetLayout.setOnClickListener(this);
        reverbLayout.setOnClickListener(this);
        presetLayout.setOnLongClickListener(this);
        reverbLayout.setOnLongClickListener(this);

        loudnessSeekBar.setMax(1000);
        loudnessSeekBar.setProgress(0);
        loudnessText.setText("0%");
        bassSeekBar.setMax(1000);
        bassSeekBar.setProgress(0);
        bassText.setText("0%");
        virtualizerSeekBar.setMax(1000);
        virtualizerSeekBar.setProgress(0);
        virtualizerText.setText("0%");

        seekBar1.setOnSeekBarChangeListener(this);
        seekBar2.setOnSeekBarChangeListener(this);
        seekBar3.setOnSeekBarChangeListener(this);
        seekBar4.setOnSeekBarChangeListener(this);
        seekBar5.setOnSeekBarChangeListener(this);
//		seekBar6.setOnSeekBarChangeListener(this);
//		seekBar7.setOnSeekBarChangeListener(this);
//		seekBar8.setOnSeekBarChangeListener(this);
//		seekBar9.setOnSeekBarChangeListener(this);
//		seekBar10.setOnSeekBarChangeListener(this);

        bassSeekBar.setOnSeekBarChangeListener(this);
        loudnessSeekBar.setOnSeekBarChangeListener(this);
        virtualizerSeekBar.setOnSeekBarChangeListener(this);

        int preset = PrefsManager.Instance().getEqualizerPreset();
        MuzikoExoPlayer.Instance().setEqualizer(this, preset);

        displayPreset(preset);

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

        if (handler != null) handler.removeCallbacksAndMessages(null);
        if (miniPlayer != null) miniPlayer.onDestroy();
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
    protected void onPause() {

        unregister();
        if (miniPlayer != null) {
            miniPlayer.pause();
        }
        super.onPause();

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNetworkEvent(NetworkEvent networkEvent) {

        if (miniPlayer != null) {
            miniPlayer.updateUI();
        }
    }

    @DebugLog
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onBufferingEvent(BufferingEvent event) {

        if (miniPlayer != null) {
            miniPlayer.showBufferingMessage(event.getMessage(), event.isClose());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onProgressEvent(ProgressEvent event) {

        miniPlayer.updateProgress(event.getProgress(), event.getDuration());
    }

    private void findViewsById() {
        reverbLayout = findViewById(R.id.reverbLayout);
        presetLayout = findViewById(R.id.presetLayout);

        textHz1 = findViewById(R.id.slider1Layout);
        textHz2 = findViewById(R.id.slider2Layout);
        textHz3 = findViewById(R.id.slider3Layout);
        textHz4 = findViewById(R.id.slider4Layout);
        textHz5 = findViewById(R.id.slider5Layout);

//		textHz6 = (TextView) findViewById(R.id.slider6Layout);
//		textHz7 = (TextView) findViewById(R.id.slider7Layout);
//		textHz8 = (TextView) findViewById(R.id.slider8Layout);
//		textHz9 = (TextView) findViewById(R.id.slider9Layout);
//		textHz10 = (TextView) findViewById(R.id.slider10Layout);

        seekBar1 = findViewById(R.id.mySeekBar1);
        seekBar2 = findViewById(R.id.mySeekBar2);
        seekBar3 = findViewById(R.id.mySeekBar3);
        seekBar4 = findViewById(R.id.mySeekBar4);
        seekBar5 = findViewById(R.id.mySeekBar5);

        loudnessSeekBar = findViewById(R.id.loudnessSeekBar);

        loudnessText = findViewById(R.id.loudnessText);

        bassSeekBar = findViewById(R.id.bassSeekBar);

        bassText = findViewById(R.id.bassText);

        virtualizerSeekBar = findViewById(R.id.virtualizerSeekBar);

        virtualizerText = findViewById(R.id.virtualizerText);
    }

    private void saveDefault() {
        if (MyApplication.preset.title.equals("Select Preset")) {
            MyApplication.preset.addUpdateDefault(MyApplication.preset);
        }
    }

    private void createnewPreset() {

        if (MyApplication.preset.position != -1) {
            EqualizerItem equalizernew = new EqualizerItem();

            equalizernew.title = "Default";
            equalizernew.band1 = MyApplication.preset.band1;
            equalizernew.band2 = MyApplication.preset.band2;
            equalizernew.band3 = MyApplication.preset.band3;
            equalizernew.band4 = MyApplication.preset.band4;
            equalizernew.band5 = MyApplication.preset.band5;
            equalizernew.band6 = MyApplication.preset.band6;
            equalizernew.band7 = MyApplication.preset.band7;
            equalizernew.band8 = MyApplication.preset.band8;
            equalizernew.band9 = MyApplication.preset.band9;
            equalizernew.band10 = MyApplication.preset.band10;
            equalizernew.bass = MyApplication.preset.bass;
            equalizernew.threed = MyApplication.preset.threed;
            equalizernew.loudness = MyApplication.preset.loudness;
            equalizernew.reverb = MyApplication.preset.reverb;
            equalizernew.position = 0;
            MyApplication.presets.set(0, equalizernew);

            PrefsManager.Instance().setEqualizerPreset(0);

            MuzikoExoPlayer.Instance().setEqualizer(EqualizerActivity.this, 0);

            displayPreset(0);
//			equalizernew.update(equalizernew);
        }
    }

    private void setupMainPlayer() {
        RelativeLayout contentlayout = findViewById(R.id.contentlayout);
        slidingUpPanelLayout = findViewById(R.id.sliding_layout);
        final LinearLayout mainPlayerLayout = findViewById(R.id.mainPlayerLayout);

        miniPlayer = new MiniPlayer(this, mainPlayerLayout, slidingUpPanelLayout, contentlayout);
    }

    private void mainUpdate() {
        miniPlayer.updateUI();

    }

    private int getReverb() {
        int reverbIndex = 0;
        for (int i = 0; i < MyApplication.reverbs.size(); i++) {
            if (MyApplication.reverbs.get(i).id == MyApplication.preset.reverb) {
                reverbIndex = i;
                break;
            }
        }

        return reverbIndex;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        menu.clear();
        getMenuInflater().inflate(R.menu.equalizer_menu, menu);
        menuItemReset = menu.findItem(R.id.eqaulizer_reset);
        menuItemAdd = menu.findItem(R.id.eqaulizer_add);
        menuItemSwitch = (JellyToggleButton) menu.findItem(R.id.eqaulizer_toggle).getActionView();

        menuItemSwitch.setCheckedImmediately(PrefsManager.Instance().getEqualizer(), false);

        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        int preset = PrefsManager.Instance().getEqualizerPreset();

        displayPreset(preset);
        if (menuItemSwitch != null) {
            menuItemSwitch.setOnStateChangeListener(this);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case android.R.id.home:
                // this takes the user 'back', as if they pressed the left-facing triangle icon on the main android toolbar.
                // if this doesn't work as desired, another possibility is to call `finish()` here.
                onBackPressed();
                return true;

            case R.id.eqaulizer_reset:
                resetDefault();
                return true;

            case R.id.eqaulizer_add:
                addPreset();
                return true;

            case R.id.eqaulizer_toggle:

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        if (seekBar == bassSeekBar) {
            bassText.setText(progress / 10 + "%");
            MyApplication.preset.bass = seekBar.getProgress();
        } else if (seekBar == virtualizerSeekBar) {
            virtualizerText.setText(progress / 10 + "%");
            MyApplication.preset.threed = seekBar.getProgress();
        } else if (seekBar == loudnessSeekBar) {
            loudnessText.setText(progress / 10 + "%");
            MyApplication.preset.loudness = seekBar.getProgress();
        }

        saveDefault();
        AppController.Instance().serviceEqualizer(0);

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

        handler.postDelayed(this::createnewPreset, 500);

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

        try {

            if (seekBar == seekBar1) {
                MyApplication.preset.band1 = seekBar.getProgress() + MuzikoExoPlayer.equalizer.getBandLevelRange()[0];
            } else if (seekBar == seekBar2) {
                MyApplication.preset.band2 = seekBar.getProgress() + MuzikoExoPlayer.equalizer.getBandLevelRange()[0];
            } else if (seekBar == seekBar3) {
                MyApplication.preset.band3 = seekBar.getProgress() + MuzikoExoPlayer.equalizer.getBandLevelRange()[0];
            } else if (seekBar == seekBar4) {
                MyApplication.preset.band4 = seekBar.getProgress() + MuzikoExoPlayer.equalizer.getBandLevelRange()[0];
            } else if (seekBar == seekBar5) {
                MyApplication.preset.band5 = seekBar.getProgress() + MuzikoExoPlayer.equalizer.getBandLevelRange()[0];
            }
            saveDefault();
            AppController.Instance().serviceEqualizer(0);

//			else if (seekBar == seekBar6) {
//				MyApplication.preset.band6 = progress + MyApplication.mHQEqualizer.getBandLevelRange()[0];
//			} else if (seekBar == seekBar7) {
//				MyApplication.preset.band7 = progress + MyApplication.mHQEqualizer.getBandLevelRange()[0];
//			} else if (seekBar == seekBar8) {
//				MyApplication.preset.band8 = progress + MyApplication.mHQEqualizer.getBandLevelRange()[0];
//			} else if (seekBar == seekBar9) {
//				MyApplication.preset.band9 = progress + MyApplication.mHQEqualizer.getBandLevelRange()[0];
//			} else if (seekBar == seekBar10) {
//				MyApplication.preset.band10 = progress + MyApplication.mHQEqualizer.getBandLevelRange()[0];
//			}

        } catch (Exception ex) {
            Crashlytics.logException(ex);
        }
        saveDefault();
        AppController.Instance().serviceEqualizer(0);

    }


    private void displayPreset(int i) {
        virtualizerText.setText(MyApplication.preset.threed / 10 + "%");
        virtualizerSeekBar.setProgress(MyApplication.preset.threed);

        bassSeekBar.setProgress(MyApplication.preset.bass);
        bassText.setText(MyApplication.preset.bass / 10 + "%");

        loudnessSeekBar.setProgress(MyApplication.preset.loudness);
        loudnessText.setText(MyApplication.preset.loudness / 10 + "%");

        updateSeekBars();

        currentPosition = i;
    }

    private void updateSeekBars() {
        try {
            if (MuzikoExoPlayer.equalizer == null) {
                MuzikoExoPlayer.Instance().equalizerOn();
            }
            int range = MuzikoExoPlayer.equalizer.getBandLevelRange()[1] - MuzikoExoPlayer.equalizer.getBandLevelRange()[0];

            short numberOfFrequencyBands = MuzikoExoPlayer.equalizer.getNumberOfBands();
            for (int i = 0; i < numberOfFrequencyBands; i++) {
                String hz = MuzikoExoPlayer.equalizer.getCenterFreq((short) i) / 1000 > 1000 ? (MuzikoExoPlayer.equalizer.getCenterFreq((short) i) / 1000) / 1000 + "KHz" : (MuzikoExoPlayer.equalizer.getCenterFreq((short) i) / 1000) + "Hz";
                switch (i) {
                    case 0:
                        textHz1.setText(hz);
                        seekBar1.setMax(range);
                        seekBar1.setProgress(MyApplication.preset.band1 + 1500);
                        break;

                    case 1:
                        textHz2.setText(hz);
                        seekBar2.setMax(range);
                        seekBar2.setProgress(MyApplication.preset.band2 + 1500);
                        break;

                    case 2:
                        textHz3.setText(hz);
                        seekBar3.setMax(range);
                        seekBar3.setProgress(MyApplication.preset.band3 + 1500);
                        break;

                    case 3:
                        textHz4.setText(hz);
                        seekBar4.setMax(range);
                        seekBar4.setProgress(MyApplication.preset.band4 + 1500);
                        break;

                    case 4:
                        textHz5.setText(hz);
                        seekBar5.setMax(range);
                        seekBar5.setProgress(MyApplication.preset.band5 + 1500);
                        break;
//					case 5:
//						textHz6.setText(hz);
//						seekBar6.setMax(range);
//						seekBar6.setProgress(MyApplication.preset.band6 + 1500);
//						break;
//					case 6:
//						textHz7.setText(hz);
//						seekBar7.setMax(range);
//						seekBar7.setProgress(MyApplication.preset.band7 + 1500);
//						break;
//					case 7:
//						textHz8.setText(hz);
//						seekBar8.setMax(range);
//						seekBar8.setProgress(MyApplication.preset.band8 + 1500);
//						break;
//					case 8:
//						textHz9.setText(hz);
//						seekBar9.setMax(range);
//						seekBar9.setProgress(MyApplication.preset.band9 + 1500);
//						break;
//					case 9:
//						textHz10.setText(hz);
//						seekBar10.setMax(range);
//						seekBar10.setProgress(MyApplication.preset.band10 + 1500);
//						break;
                }
            }
        } catch (Exception ex) {
            Crashlytics.logException(ex);
        }
    }

    private void addPreset() {
        new MaterialDialog.Builder(EqualizerActivity.this).theme(Theme.LIGHT).titleColorRes(R.color.normal_blue).negativeColorRes(R.color.dialog_negetive_button).title("Add Equalizer Preset").positiveText("ADD").negativeText("CANCEL")
                //                .content()
                .inputType(InputType.TYPE_CLASS_TEXT).input("Preset Name", "", (dialog, input) -> {
            // Do something

            String title = input.toString().trim();

            final EqualizerItem equalizerItem = new EqualizerItem(EqualizerActivity.this);
            equalizerItem.title = title;
            equalizerItem.band1 = MyApplication.preset.band1;
            equalizerItem.band2 = MyApplication.preset.band2;
            equalizerItem.band3 = MyApplication.preset.band3;
            equalizerItem.band4 = MyApplication.preset.band4;
            equalizerItem.band5 = MyApplication.preset.band5;
            equalizerItem.band6 = MyApplication.preset.band6;
            equalizerItem.band7 = MyApplication.preset.band7;
            equalizerItem.band8 = MyApplication.preset.band8;
            equalizerItem.band9 = MyApplication.preset.band9;
            equalizerItem.band10 = MyApplication.preset.band10;

            equalizerItem.bass = MyApplication.preset.bass;
            equalizerItem.threed = MyApplication.preset.threed;
            equalizerItem.loudness = MyApplication.preset.loudness;
            equalizerItem.reverb = MyApplication.preset.reverb;

            if (equalizerItem.getByTitle(title)) {
                if (equalizerItem.update(equalizerItem)) {
                    for (int i = 0; i < MyApplication.presets.size(); i++) {
                        if (MyApplication.presets.get(i).id == equalizerItem.id) {
                            MyApplication.presets.set(i, equalizerItem);

                            AppController.toast(EqualizerActivity.this, "Preset updated!");
                            break;
                        }
                    }
                } else {
                    AppController.toast(EqualizerActivity.this, "Unable to update preset!");
                }
            } else {
                if (equalizerItem.insert(equalizerItem) > 0) {

                    List<EqualizerItem> list = EqualizerItem.loadAll();

                    MyApplication.presets.add(equalizerItem);

                    AppController.toast(EqualizerActivity.this, "Preset saved!");
                } else {
                    AppController.toast(EqualizerActivity.this, "Unable to save preset!");
                }
            }
        }).show();

    }


    private void resetDefault() {


        new MaterialDialog.Builder(this)
                .theme(Theme.LIGHT)
                .titleColorRes(R.color.normal_blue)
                .negativeColorRes(R.color.dialog_negetive_button)
                .positiveColorRes(R.color.normal_blue)
                .title("Reset to defaults")
                .content("Do you really want to reset to default values?")
                .positiveText("YES")
                .onPositive(
                        (dialog, which) -> {
                            MuzikoExoPlayer.Instance().setEqualizer(this, PrefsManager.Instance().getEqualizerPreset());
                            MuzikoExoPlayer.Instance().loadEqualizer(this, true);
                            displayPreset(PrefsManager.Instance().getEqualizerPreset());

                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    MuzikoExoPlayer.Instance().setEqualizer(EqualizerActivity.this, PrefsManager.Instance().getEqualizerPreset());
                                    MuzikoExoPlayer.Instance().loadEqualizer(EqualizerActivity.this, true);
                                    displayPreset(PrefsManager.Instance().getEqualizerPreset());
                                }
                            }, 1000);
                            dialog.dismiss();
                        })
                .negativeText("NO")
                .show();
    }

    private void deletePreset() {
        if (currentPosition < 0 || currentPosition > MyApplication.presets.size()) {
            AppController.toast(this, "Preset not found!");
            return;
        }

        final EqualizerItem item = MyApplication.presets.get(currentPosition);
        if (item == null || item.position != -1) {
            AppController.toast(this, "Cannot delete this preset!");
        }

        new MaterialDialog.Builder(this).theme(Theme.LIGHT).titleColorRes(R.color.normal_blue).negativeColorRes(R.color.dialog_negetive_button).positiveColorRes(R.color.normal_blue).title("Delete Preset").content("Do you really want to delete this preset").positiveText("YES").onPositive((dialog, which) -> {
            // remove the crash on delete presets
            if (EqualizerItem.deleteByTitle(item.title)) {
                MyApplication.presets.remove(currentPosition);
            } else {
                AppController.toast(EqualizerActivity.this, "Unable to delete preset!");
            }
            dialog.dismiss();

        }).negativeText("NO").show();
    }

    private void register() {
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
        if (mainReceiver != null) {
            unregisterReceiver(mainReceiver);
            mainReceiver = null;
        }
    }

    @Override
    public void onClick(View v) {
        if (v == presetLayout) {
            final CharSequence[] list = new CharSequence[MyApplication.presets.size()];

            for (int i = 0; i < MyApplication.presets.size(); i++) {
                list[i] = MyApplication.presets.get(i).title;
            }

            new MaterialDialog.Builder(this)
                    .title("Preset")
                    .items(list)
                    .itemsCallbackSingleChoice(
                            PrefsManager.Instance().getEqualizerPreset(),
                            (dialog, view, which, text) -> {
                                PrefsManager.Instance().setEqualizerPreset(which);

                                MuzikoExoPlayer.Instance()
                                        .setEqualizer(EqualizerActivity.this, which);

                                displayPreset(which);
                                return true;
                            })
                    .positiveText("Set")
                    .negativeText("Cancel")
                    .show();
        } else if (v == reverbLayout) {
            final CharSequence[] list = new CharSequence[MyApplication.reverbs.size()];
            for (int i = 0; i < MyApplication.reverbs.size(); i++) {
                list[i] = MyApplication.reverbs.get(i).title;
            }

            new MaterialDialog.Builder(this)
                    .title("Reverb")
                    .items(list)
                    .itemsCallbackSingleChoice(getReverb(), (dialog, view, which, text) -> {

                        MyApplication.preset.reverb = MyApplication.reverbs.get(which).id;

                        AppController.Instance().serviceEqualizer(0);
                        return true;
                    })
                    .positiveText("Set")
                    .negativeText("Cancel")
                    .show();
        }
    }

    @Override
    public boolean onLongClick(View v) {

        if (v == presetLayout) {
            deletePreset();
        }
        return false;
    }

    @Override
    public void onStateChange(float process, State state, JellyToggleButton jtb) {
        if (state.equals(State.LEFT)) {
            PrefsManager.Instance().setEqualizer(false);
            AppController.Instance().serviceEqualizer(-1);
            AppController.toast(this, "Equalizer Off!");
        }
        if (state.equals(State.RIGHT)) {
            PrefsManager.Instance().setEqualizer(true);
            AppController.Instance().serviceEqualizer(1);
            AppController.toast(this, "Equalizer On!");
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
                    case AppController.INTENT_TRACK_REPEAT:
                        mainUpdate();
                        break;
                    case AppController.INTENT_TRACK_SHUFFLE:
                        mainUpdate();
                        break;
                    case AppController.INTENT_QUEUE_STOPPED:
                        miniPlayer.layoutMiniPlayer();

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
