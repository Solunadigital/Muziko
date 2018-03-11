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

package com.muziko.cutter.ringtone_lib;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.muziko.R;
import com.muziko.cutter.ringtone_lib.soundfile.CheapSoundFile;
import com.muziko.manager.AppController;
import com.muziko.mediaplayer.PlayerConstants;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.util.Random;

/**
 * The activity for the Ringdroid main editor window.  Keeps track of
 * the waveform display, current horizontal offset, marker handles,
 * start / end text boxes, and handles all of the buttons and controls.
 */
public class RingdroidEditActivity extends AppCompatActivity
        implements MarkerView.MarkerListener,
        WaveformView.WaveformListener {
    /**
     * This is a special intent action that means "edit a sound file".
     */
    public static final String EDIT =
            "com.ringdroid.action.EDIT_TAGS";
    /**
     * Server url
     */
    public static final String STATS_SERVER_URL =
		    "http://ringdroid.appspot.com/add";
	public static final String ERR_SERVER_URL =
			"http://ringdroid.appspot.com/err";
	/**
	 * Preference names
     */
	private static final String PREF_SUCCESS_COUNT = "success_count";
	private static final String PREF_STATS_SERVER_CHECK =
			"stats_server_check";
	private static final String PREF_STATS_SERVER_ALLOWED =
			"stats_server_allowed";
	private static final String PREF_ERROR_COUNT = "error_count";
	private static final String PREF_ERR_SERVER_CHECK =
			"err_server_check";
	private static final String PREF_ERR_SERVER_ALLOWED =
			"err_server_allowed";
	private static final String PREF_UNIQUE_ID = "unique_id";
	/**
     * Possible codes for PREF_*_SERVER_ALLOWED
     */
    private static final int SERVER_ALLOWED_UNKNOWN = 0;
	private static final int SERVER_ALLOWED_NO = 1;
	private static final int SERVER_ALLOWED_YES = 2;
	// Result codes
    private static final int REQUEST_CODE_RECORD = 1;
    private static final int REQUEST_CODE_CHOOSE_CONTACT = 2;
	private ImageButton buttonZoomIn;
	private ImageButton buttonZoomOut;
	private long mLoadingStartTime;
    private long mLoadingLastUpdateTime;
    private boolean mLoadingKeepGoing;
    private ProgressDialog mProgressDialog;
    private CheapSoundFile mSoundFile;
    private File mFile;
    private String mFilename;
    private String mDstFilename;
    private String mArtist;
    private String mAlbum;
    private String mGenre;
    private String mTitle;
    private int mYear;
    private String mExtension;
    private String mRecordingFilename;
    private int mNewFileKind;
    private Uri mRecordingUri;
    private boolean mWasGetContentIntent;
    private WaveformView mWaveformView;
    private MarkerView mStartMarker;
    private MarkerView mEndMarker;
    private TextView mStartText;
    private TextView mEndText;
    //    private TextView mInfo;
    private ImageView mPlayButton;
    private ImageView mRewindButton;
    private ImageView mFfwdButton;
    private boolean mKeyDown;
    private String mCaption = "";
    private int mWidth;
    private int mMaxPos;
    private int mStartPos;
    private int mEndPos;
    private boolean mStartVisible;
    private boolean mEndVisible;
    private int mLastDisplayedStartPos;
    private int mLastDisplayedEndPos;
    private int mOffset;
    private int mOffsetGoal;
    private int mFlingVelocity;
    private int mPlayStartMsec;
    private int mPlayStartOffset;
    private int mPlayEndMsec;
    private Handler mHandler;
    private boolean mIsPlaying;
    private MediaPlayer mPlayer;
    private boolean mCanSeekAccurately;
    private boolean mTouchDragging;
    private float mTouchStart;
    private int mTouchInitialOffset;
    private int mTouchInitialStartPos;
    private int mTouchInitialEndPos;
    private long mWaveformTouchStartMsec;
    private float mDensity;
    private int mMarkerLeftInset;
    private int mMarkerRightInset;
    private int mMarkerTopOffset;
    private int mMarkerBottomOffset;
    private Toolbar toolbar;
    private ActionBar actionBar;
    private MaterialDialog mProgressDialogMaterial;

    //
    // Public methods and protected overrides
    //
    private Runnable mTimerRunnable = new Runnable() {
        public void run() {
            // Updating an EditText is slow on Android.  Make sure
            // we only do the update if the text has actually changed.
            if (mStartPos != mLastDisplayedStartPos) {
                mStartText.setText(formatTime(mStartPos));
                mLastDisplayedStartPos = mStartPos;
            }

            if (mEndPos != mLastDisplayedEndPos) {
                mEndText.setText(formatTime(mEndPos));
                mLastDisplayedEndPos = mEndPos;
            }

            mHandler.postDelayed(mTimerRunnable, 100);
        }
    };
	private OnClickListener zoomInListener = sender -> waveformZoomIn();
	private OnClickListener zoomOutnListener = sender -> waveformZoomOut();
	private OnClickListener mSaveListener = sender -> onSave();
	private OnClickListener mPlayListener = new OnClickListener() {
        public void onClick(View sender) {
            onPlay(mStartPos);
        }
    };
    private OnClickListener mRewindListener = new OnClickListener() {
        public void onClick(View sender) {
            if (mIsPlaying) {
                int newPos = mPlayer.getCurrentPosition() - 5000;
                if (newPos < mPlayStartMsec)
                    newPos = mPlayStartMsec;
                mPlayer.seekTo(newPos);
            } else {
                mStartMarker.requestFocus();
                markerFocus(mStartMarker);
            }
        }
    };
    private OnClickListener mFfwdListener = new OnClickListener() {
        public void onClick(View sender) {
            if (mIsPlaying) {
                int newPos = 5000 + mPlayer.getCurrentPosition();
                if (newPos > mPlayEndMsec)
                    newPos = mPlayEndMsec;
                mPlayer.seekTo(newPos);
            } else {
                mEndMarker.requestFocus();
                markerFocus(mEndMarker);
            }
        }
    };
    private OnClickListener mMarkStartListener = new OnClickListener() {
        public void onClick(View sender) {
            if (mIsPlaying) {
                mStartPos = mWaveformView.millisecsToPixels(
                        mPlayer.getCurrentPosition() + mPlayStartOffset);
                updateDisplay();
            }
        }
    };

    //
    // WaveformListener
    //
    private OnClickListener mMarkEndListener = new OnClickListener() {
        public void onClick(View sender) {
            if (mIsPlaying) {
                mEndPos = mWaveformView.millisecsToPixels(
                        mPlayer.getCurrentPosition() + mPlayStartOffset);
                updateDisplay();
                handlePause();
            }
        }
    };

    public static void onAbout(final Activity activity) {
        new AlertDialog.Builder(activity)
                .setTitle(R.string.about_title)
                .setMessage(R.string.about_text)
                .setPositiveButton(R.string.alert_ok_button, null)
                .setCancelable(false)
                .show();
    }

    /**
     * Called with the activity is first created.
     */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        mRecordingFilename = null;
        mRecordingUri = null;
        mPlayer = null;
        mIsPlaying = false;

        Intent intent = getIntent();

        if (intent.getBooleanExtra("privacy", false)) {
            showServerPrompt(true);
            return;
        }

        // If the Ringdroid media select activity was launched via a
        // GET_CONTENT intent, then we shouldn't display a "saved"
        // message when the user saves, we should just return whatever
        // they create.
        mWasGetContentIntent = intent.getBooleanExtra(
                "was_get_content_intent", false);

        mFilename = intent.getData().toString();

        mSoundFile = null;
        mKeyDown = false;

        if (mFilename.equals("record")) {
            try {
                Intent recordIntent = new Intent(
                        MediaStore.Audio.Media.RECORD_SOUND_ACTION);
                startActivityForResult(recordIntent, REQUEST_CODE_RECORD);
            } catch (Exception e) {
                showFinalAlert(e, R.string.record_error);
            }
        }

        mHandler = new Handler();

        loadGui();

        mHandler.postDelayed(mTimerRunnable, 100);

        if (!mFilename.equals("record")) {
            loadFromFile();
        }
    }

    /**
     * Called when the orientation changes and/or the keyboard is shown
     * or hidden.  We don't need to recreate the whole activity in this
     * case, but we do need to redo our layout somewhat.
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        final int saveZoomLevel = mWaveformView.getZoomLevel();
        super.onConfigurationChanged(newConfig);

        loadGui();
        enableZoomButtons();

        mHandler.postDelayed(() -> {
            mStartMarker.requestFocus();
            markerFocus(mStartMarker);

            mWaveformView.setZoomLevel(saveZoomLevel);
            mWaveformView.recomputeHeights(mDensity);

            updateDisplay();
        }, 500);
    }

    /**
     * Called with the activity is finally destroyed.
     */
    @Override
    protected void onDestroy() {
        Log.i("Ringdroid", "EditActivity OnDestroy");

        if (mPlayer != null && mPlayer.isPlaying()) {
            mPlayer.stop();
        }
        mPlayer = null;

        if (mRecordingFilename != null) {
            try {
                if (!new File(mRecordingFilename).delete()) {
                    showFinalAlert(new Exception(), R.string.delete_tmp_error);
                }

                getContentResolver().delete(mRecordingUri, null, null);
            } catch (SecurityException e) {
                showFinalAlert(e, R.string.delete_tmp_error);
            }
        }

        super.onDestroy();
    }

    /**
     * Called with an Activity we started with an Intent returns.
     */
    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode,
                                    Intent dataIntent) {
        if (requestCode == REQUEST_CODE_CHOOSE_CONTACT) {
            // The user finished saving their ringtone and they're
            // just applying it to a contact.  When they return here,
            // they're done.
            sendStatsToServerIfAllowedAndFinish();
            return;
        }

        if (requestCode != REQUEST_CODE_RECORD) {
            return;
        }

        if (resultCode != RESULT_OK) {
            finish();
            return;
        }

        if (dataIntent == null) {
            finish();
            return;
        }

        // Get the recorded file and open it, but save the uri and
        // filename so that we can delete them when we exit; the
        // recorded file is only temporary and only the edited & saved
        // ringtone / other sound will stick around.
        mRecordingUri = dataIntent.getData();
        mRecordingFilename = getFilenameFromUri(mRecordingUri);
        mFilename = mRecordingFilename;
        loadFromFile();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_SPACE) {
            onPlay(mStartPos);
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    //
    // MarkerListener
    //

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.fragment_cut_tone, menu);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
//        menu.findItem(R.id.action_save).setVisible(true);
//        menu.findItem(R.id.action_reset).setVisible(true);
//        menu.findItem(R.id.action_about).setVisible(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                RingdroidEditActivity.this.finish();
                //onSave();
                return true;
//        case R.id.action_reset:
//            resetPositions();
//            mOffsetGoal = 0;
//            updateDisplay();
//            return true;
            case R.id.saveTone:
                onSave();
                //onAbout(this);
                return true;
            default:
                return false;
        }
    }

    public void waveformTouchStart(float x) {
        mTouchDragging = true;
        mTouchStart = x;
        mTouchInitialOffset = mOffset;
        mFlingVelocity = 0;
        mWaveformTouchStartMsec = System.currentTimeMillis();
    }

    public void waveformTouchMove(float x) {
        mOffset = trap((int) (mTouchInitialOffset + (mTouchStart - x)));
        updateDisplay();
    }

    public void waveformTouchEnd() {
        mTouchDragging = false;
        mOffsetGoal = mOffset;

        long elapsedMsec = System.currentTimeMillis() -
                mWaveformTouchStartMsec;
        if (elapsedMsec < 300) {
            if (mIsPlaying) {
                int seekMsec = mWaveformView.pixelsToMillisecs(
                        (int) (mTouchStart + mOffset));
                if (seekMsec >= mPlayStartMsec &&
                        seekMsec < mPlayEndMsec) {
                    mPlayer.seekTo(seekMsec - mPlayStartOffset);
                } else {
                    handlePause();
                }
            } else {
                onPlay((int) (mTouchStart + mOffset));
            }
        }
    }

    public void waveformFling(float vx) {
        mTouchDragging = false;
        mOffsetGoal = mOffset;
        mFlingVelocity = (int) (-vx);
        updateDisplay();
    }

    /**
     * Every time we get a message that our waveform drew, see if we need to
     * animate and trigger another redraw.
     */
    public void waveformDraw() {
        mWidth = mWaveformView.getMeasuredWidth();
        if (mOffsetGoal != mOffset && !mKeyDown)
            updateDisplay();
        else if (mIsPlaying) {
            updateDisplay();
        } else if (mFlingVelocity != 0) {
            updateDisplay();
        }
    }

    public void waveformZoomIn() {
        mWaveformView.zoomIn();
        mStartPos = mWaveformView.getStart();
        mEndPos = mWaveformView.getEnd();
        mMaxPos = mWaveformView.maxPos();
        mOffset = mWaveformView.getOffset();
        mOffsetGoal = mOffset;
        enableZoomButtons();
        updateDisplay();
    }

    //
    // Static About dialog method, also called from RingdroidSelectActivity
    //

    public void waveformZoomOut() {
        mWaveformView.zoomOut();
        mStartPos = mWaveformView.getStart();
        mEndPos = mWaveformView.getEnd();
        mMaxPos = mWaveformView.maxPos();
        mOffset = mWaveformView.getOffset();
        mOffsetGoal = mOffset;
        enableZoomButtons();
        updateDisplay();
    }

    //
    // Internal methods
    //

	public void markerTouchStart(float x) {
		mTouchDragging = true;
        mTouchStart = x;
        mTouchInitialStartPos = mStartPos;
        mTouchInitialEndPos = mEndPos;
    }

    public void markerTouchMove(MarkerView marker, float x) {
        float delta = x - mTouchStart;

        if (marker == mStartMarker) {
            mStartPos = trap((int) (mTouchInitialStartPos + delta));
            mEndPos = trap((int) (mTouchInitialEndPos + delta));
        } else {
            mEndPos = trap((int) (mTouchInitialEndPos + delta));
            if (mEndPos < mStartPos)
                mEndPos = mStartPos;
        }

        updateDisplay();
    }

    public void markerTouchEnd(MarkerView marker) {
        mTouchDragging = false;
        if (marker == mStartMarker) {
            setOffsetGoalStart();
        } else {
            setOffsetGoalEnd();
        }
    }

    public void markerFocus(MarkerView marker) {
        mKeyDown = false;
        if (marker == mStartMarker) {
            setOffsetGoalStartNoUpdate();
        } else {
            setOffsetGoalEndNoUpdate();
        }

        // Delay updaing the display because if this focus was in
        // response to a touch event, we want to receive the touch
        // event too before updating the display.
        mHandler.postDelayed(this::updateDisplay, 100);
    }

    public void markerLeft(MarkerView marker, int velocity) {
        mKeyDown = true;

        if (marker == mStartMarker) {
            int saveStart = mStartPos;
            mStartPos = trap(mStartPos - velocity);
            mEndPos = trap(mEndPos - (saveStart - mStartPos));
            setOffsetGoalStart();
        }

        if (marker == mEndMarker) {
            if (mEndPos == mStartPos) {
                mStartPos = trap(mStartPos - velocity);
                mEndPos = mStartPos;
            } else {
                mEndPos = trap(mEndPos - velocity);
            }

            setOffsetGoalEnd();
        }

        updateDisplay();
    }

    public void markerRight(MarkerView marker, int velocity) {
        mKeyDown = true;

        if (marker == mStartMarker) {
            int saveStart = mStartPos;
            mStartPos += velocity;
            if (mStartPos > mMaxPos)
                mStartPos = mMaxPos;
            mEndPos += (mStartPos - saveStart);
            if (mEndPos > mMaxPos)
                mEndPos = mMaxPos;

            setOffsetGoalStart();
        }

        if (marker == mEndMarker) {
            mEndPos += velocity;
            if (mEndPos > mMaxPos)
                mEndPos = mMaxPos;

            setOffsetGoalEnd();
        }

        updateDisplay();
    }

	public void markerEnter() {
	}

    public void markerKeyUp() {
        mKeyDown = false;
        updateDisplay();
    }

    public void markerDraw() {
    }

    /**
     * Called from both onCreate and onConfigurationChanged
     * (if the user switched layouts)
     */
    private void loadGui() {
        // Inflate our UI from its XML layout description.
        setContentView(R.layout.activity_cut_tones);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.dark_blue));
        }

        actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        Intent in = getIntent();
        if (in != null) {
            actionBar.setTitle(in.getStringExtra("SONG_NAME"));
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mDensity = metrics.density;

        mMarkerLeftInset = (int) (46 * mDensity);
        mMarkerRightInset = (int) (48 * mDensity);
        mMarkerTopOffset = (int) (10 * mDensity);
        mMarkerBottomOffset = (int) (10 * mDensity);

        mStartText = findViewById(R.id.startText);
        //mStartText.addTextChangedListener(mTextWatcher);
        mEndText = findViewById(R.id.endText);
        //mEndText.addTextChangedListener(mTextWatcher);

	    mPlayButton = (ImageButton) findViewById(R.id.buttonPlayMain);
	    mPlayButton.setOnClickListener(mPlayListener);
	    mRewindButton = (ImageButton) findViewById(R.id.buttonPrevMain);
	    mRewindButton.setOnClickListener(mRewindListener);
	    mFfwdButton = (ImageButton) findViewById(R.id.buttonNextMain);
	    mFfwdButton.setOnClickListener(mFfwdListener);
        buttonZoomIn = findViewById(R.id.button_zoom_in);
        buttonZoomOut = findViewById(R.id.button_zoom_out);
        buttonZoomIn.setOnClickListener(zoomInListener);
        buttonZoomOut.setOnClickListener(zoomOutnListener);
        //TextView markStartButton = (TextView) findViewById(R.id.startmarker);
        //markStartButton.setOnClickListener(mMarkStartListener);
        //TextView markEndButton = (TextView) findViewById(R.id.endmarker);
        //markEndButton.setOnClickListener(mMarkStartListener);

        enableDisableButtons();

        mWaveformView = findViewById(R.id.waveform);
        mWaveformView.setListener(this);

//        mInfo = (TextView)findViewById(R.id.info);
//        mInfo.setText(mCaption);

        mMaxPos = 0;
        mLastDisplayedStartPos = -1;
        mLastDisplayedEndPos = -1;

        if (mSoundFile != null && !mWaveformView.hasSoundFile()) {
            mWaveformView.setSoundFile(mSoundFile);
            mWaveformView.recomputeHeights(mDensity);
            mMaxPos = mWaveformView.maxPos();
        }

        mStartMarker = findViewById(R.id.startmarker);
        mStartMarker.setListener(this);
        mStartMarker.setAlpha(255);
        mStartMarker.setFocusable(true);
        mStartMarker.setFocusableInTouchMode(true);

        mStartVisible = true;

        mEndMarker = findViewById(R.id.endmarker);
        mEndMarker.setListener(this);
        mEndMarker.setAlpha(255);
        mEndMarker.setFocusable(true);
        mEndMarker.setFocusableInTouchMode(true);

        mEndVisible = true;

        updateDisplay();
    }

    private void loadFromFile() {
        mFile = new File(mFilename);
        mExtension = getExtensionFromFilename(mFilename);

        SongMetadataReader metadataReader = new SongMetadataReader(
                this, mFilename);
        mTitle = metadataReader.mTitle;
        mArtist = metadataReader.mArtist;
        mAlbum = metadataReader.mAlbum;
        mYear = metadataReader.mYear;
        mGenre = metadataReader.mGenre;

        String titleLabel = mTitle;
        if (mArtist != null && mArtist.length() > 0) {
            titleLabel += " - " + mArtist;
        }
        setTitle(titleLabel);

        mLoadingStartTime = System.currentTimeMillis();
        mLoadingLastUpdateTime = System.currentTimeMillis();
        mLoadingKeepGoing = true;
        mProgressDialog = new ProgressDialog(RingdroidEditActivity.this);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setTitle(R.string.progress_dialog_loading);
        mProgressDialog.setCancelable(true);
        mProgressDialog.setOnCancelListener(
		        dialog -> mLoadingKeepGoing = false);
	    mProgressDialog.show();

        final CheapSoundFile.ProgressListener listener =
		        fractionComplete -> {
			        long now = System.currentTimeMillis();
			        if (now - mLoadingLastUpdateTime > 100) {
				        mProgressDialog.setProgress(
						        (int) (mProgressDialog.getMax() *
								        fractionComplete));
				        mLoadingLastUpdateTime = now;
                    }
			        return mLoadingKeepGoing;
		        };

        // Create the M Player in a background thread
        mCanSeekAccurately = false;
        new Thread() {
            public void run() {
                mCanSeekAccurately = SeekTest.CanSeekAccurately(
                        getPreferences(Context.MODE_PRIVATE));

                System.out.println("Seek test done, creating media player.");
                try {
                    MediaPlayer player = new MediaPlayer();
                    player.setDataSource(mFile.getAbsolutePath());
                    player.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    player.prepare();
                    mPlayer = player;
                } catch (final java.io.IOException e) {
	                Runnable runnable = () -> handleFatalError(
			                "ReadError",
			                getResources().getText(R.string.read_error),
			                e);
	                mHandler.post(runnable);
                }
            }
        }.start();

        // Load the sound file in a background thread
        new Thread() {
            public void run() {
                try {
                    mSoundFile = CheapSoundFile.create(mFile.getAbsolutePath(),
                            listener);

                    if (mSoundFile == null) {
                        mProgressDialog.dismiss();
                        String name = mFile.getName().toLowerCase();
                        String[] components = name.split("\\.");
                        String err;
                        if (components.length < 2) {
                            err = getResources().getString(
                                    R.string.no_extension_error);
                        } else {
                            err = getResources().getString(
                                    R.string.bad_extension_error) + " " +
                                    components[components.length - 1];
                        }
                        final String finalErr = err;
	                    Runnable runnable = () -> handleFatalError(
			                    "UnsupportedExtension",
			                    finalErr,
			                    new Exception());
	                    mHandler.post(runnable);
                        return;
                    }
                } catch (final Exception e) {
                    mProgressDialog.dismiss();
                    e.printStackTrace();
                    //mInfo.setText(e.toString());

	                Runnable runnable = () -> handleFatalError(
			                "ReadError",
			                getResources().getText(R.string.read_error),
			                e);
	                mHandler.post(runnable);
                    return;
                }
                mProgressDialog.dismiss();
                if (mLoadingKeepGoing) {
	                Runnable runnable = RingdroidEditActivity.this::finishOpeningSoundFile;
	                mHandler.post(runnable);
                } else {
                    RingdroidEditActivity.this.finish();
                }
            }
        }.start();
    }

    private void finishOpeningSoundFile() {
        mWaveformView.setSoundFile(mSoundFile);
        mWaveformView.recomputeHeights(mDensity);

        mMaxPos = mWaveformView.maxPos();
        mLastDisplayedStartPos = -1;
        mLastDisplayedEndPos = -1;

        mTouchDragging = false;

        mOffset = 0;
        mOffsetGoal = 0;
        mFlingVelocity = 0;
        resetPositions();
        if (mEndPos > mMaxPos)
            mEndPos = mMaxPos;

        mCaption =
                mSoundFile.getFiletype() + ", " +
                        mSoundFile.getSampleRate() + " Hz, " +
                        mSoundFile.getAvgBitrateKbps() + " kbps, " +
                        formatTime(mMaxPos) + " " +
                        getResources().getString(R.string.time_seconds);
        // mInfo.setText(mCaption);

        updateDisplay();
    }

    private synchronized void updateDisplay() {
        if (mIsPlaying) {
            int now = mPlayer.getCurrentPosition() + mPlayStartOffset;
            int frames = mWaveformView.millisecsToPixels(now);
            mWaveformView.setPlayback(frames);
            setOffsetGoalNoUpdate(frames - mWidth / 2);
            if (now >= mPlayEndMsec) {
                handlePause();
            }
        }

        if (!mTouchDragging) {
            int offsetDelta;

            if (mFlingVelocity != 0) {
                float saveVel = mFlingVelocity;

                offsetDelta = mFlingVelocity / 30;
                if (mFlingVelocity > 80) {
                    mFlingVelocity -= 80;
                } else if (mFlingVelocity < -80) {
                    mFlingVelocity += 80;
                } else {
                    mFlingVelocity = 0;
                }

                mOffset += offsetDelta;

                if (mOffset + mWidth / 2 > mMaxPos) {
                    mOffset = mMaxPos - mWidth / 2;
                    mFlingVelocity = 0;
                }
                if (mOffset < 0) {
                    mOffset = 0;
                    mFlingVelocity = 0;
                }
                mOffsetGoal = mOffset;
            } else {
                offsetDelta = mOffsetGoal - mOffset;

                if (offsetDelta > 10)
                    offsetDelta = offsetDelta / 10;
                else if (offsetDelta > 0)
                    offsetDelta = 1;
                else if (offsetDelta < -10)
                    offsetDelta = offsetDelta / 10;
                else if (offsetDelta < 0)
                    offsetDelta = -1;
                else
                    offsetDelta = 0;

                mOffset += offsetDelta;
            }
        }

        mWaveformView.setParameters(mStartPos, mEndPos, mOffset);
        mWaveformView.invalidate();

        mStartMarker.setContentDescription(
                getResources().getText(R.string.start_marker) + " " +
                        formatTime(mStartPos));
        mEndMarker.setContentDescription(
                getResources().getText(R.string.end_marker) + " " +
                        formatTime(mEndPos));

        int startX = mStartPos - mOffset - mMarkerLeftInset;
        if (startX + mStartMarker.getWidth() >= 0) {
            if (!mStartVisible) {
                // Delay this to avoid flicker
	            mHandler.postDelayed(() -> {
		            mStartVisible = true;
		            mStartMarker.setAlpha(255);
	            }, 0);
            }
        } else {
            if (mStartVisible) {
                mStartMarker.setAlpha(0);
                mStartVisible = false;
            }
            startX = 0;
        }

        int endX = mEndPos - mOffset - mEndMarker.getWidth() +
                mMarkerRightInset;
        if (endX + mEndMarker.getWidth() >= 0) {
            if (!mEndVisible) {
                // Delay this to avoid flicker
	            mHandler.postDelayed(() -> {
		            mEndVisible = true;
		            mEndMarker.setAlpha(255);
	            }, 0);
            }
        } else {
            if (mEndVisible) {
                mEndMarker.setAlpha(0);
                mEndVisible = false;
            }
            endX = 0;
        }

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        params.setMargins(
                startX,
                mMarkerTopOffset + 10,
                -mStartMarker.getWidth(),
                -mStartMarker.getHeight());

        mStartMarker.setLayoutParams(params);
//        mStartMarker.getLayoutParams().width = pxToDp(100);
//        mStartMarker.getLayoutParams().height = pxToDp(200);

        params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        params.setMargins(
                endX,
                mWaveformView.getMeasuredHeight() - mEndMarker.getWidth() - mMarkerBottomOffset,
                -mEndMarker.getWidth(),
                -mEndMarker.getHeight());

        mEndMarker.setLayoutParams(params);
        mEndMarker.setPadding(0, 0, 0, 50);
    }

    private void enableDisableButtons() {
        if (mIsPlaying) {
	        mPlayButton.setImageResource(R.drawable.pause_icon);
	        mPlayButton.setContentDescription(getResources().getText(R.string.stop));
        } else {
	        mPlayButton.setImageResource(R.drawable.play_icon);
	        mPlayButton.setContentDescription(getResources().getText(R.string.play));
        }
    }

    private void resetPositions() {
        mStartPos = mWaveformView.secondsToPixels(0.0);
        mEndPos = mWaveformView.secondsToPixels(15.0);
    }

    private int trap(int pos) {
        if (pos < 0)
            return 0;
        if (pos > mMaxPos)
            return mMaxPos;
        return pos;
    }

    private void setOffsetGoalStart() {
        setOffsetGoal(mStartPos - mWidth / 2);
    }

    private void setOffsetGoalStartNoUpdate() {
        setOffsetGoalNoUpdate(mStartPos - mWidth / 2);
    }

    private void setOffsetGoalEnd() {
        setOffsetGoal(mEndPos - mWidth / 2);
    }

    private void setOffsetGoalEndNoUpdate() {
        setOffsetGoalNoUpdate(mEndPos - mWidth / 2);
    }

    private void setOffsetGoal(int offset) {
        setOffsetGoalNoUpdate(offset);
        updateDisplay();
    }

    private void setOffsetGoalNoUpdate(int offset) {
        if (mTouchDragging) {
            return;
        }

        mOffsetGoal = offset;
        if (mOffsetGoal + mWidth / 2 > mMaxPos)
            mOffsetGoal = mMaxPos - mWidth / 2;
        if (mOffsetGoal < 0)
            mOffsetGoal = 0;
    }

    private String formatTime(int pixels) {
        if (mWaveformView != null && mWaveformView.isInitialized()) {
            return formatDecimal(mWaveformView.pixelsToSeconds(pixels));
        } else {
            return "";
        }
    }

    private String formatDecimal(double x) {
        int xWhole = (int) x;
        int xFrac = (int) (100 * (x - xWhole) + 0.5);

        if (xFrac >= 100) {
            xWhole++; //Round up
            xFrac -= 100; //Now we need the remainder after the round up
            if (xFrac < 10) {
                xFrac *= 10; //we need a fraction that is 2 digits long
            }
        }

        if (xFrac < 10)
            return xWhole + ".0" + xFrac;
        else
            return xWhole + "." + xFrac;
    }

    private synchronized void handlePause() {
        if (mPlayer != null && mPlayer.isPlaying()) {
            mPlayer.pause();
        }
        mWaveformView.setPlayback(-1);
        mIsPlaying = false;
        enableDisableButtons();
    }

    private synchronized void onPlay(int startPosition) {

        if (PlayerConstants.QUEUE_STATE == PlayerConstants.QUEUE_STATE_PLAYING) {
            AppController.Instance().servicePause();
        }

        if (mIsPlaying) {
            handlePause();
            return;
        }

        if (mPlayer == null) {
            // Not initialized yet
            return;
        }

        try {
            mPlayStartMsec = mWaveformView.pixelsToMillisecs(startPosition);
            if (startPosition < mStartPos) {
                mPlayEndMsec = mWaveformView.pixelsToMillisecs(mStartPos);
            } else if (startPosition > mEndPos) {
                mPlayEndMsec = mWaveformView.pixelsToMillisecs(mMaxPos);
            } else {
                mPlayEndMsec = mWaveformView.pixelsToMillisecs(mEndPos);
            }

            mPlayStartOffset = 0;

            int startFrame = mWaveformView.secondsToFrames(
                    mPlayStartMsec * 0.001);
            int endFrame = mWaveformView.secondsToFrames(
                    mPlayEndMsec * 0.001);
            int startByte = mSoundFile.getSeekableFrameOffset(startFrame);
            int endByte = mSoundFile.getSeekableFrameOffset(endFrame);
            if (mCanSeekAccurately && startByte >= 0 && endByte >= 0) {
                try {
                    mPlayer.reset();
                    mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    FileInputStream subsetInputStream = new FileInputStream(
                            mFile.getAbsolutePath());
                    mPlayer.setDataSource(subsetInputStream.getFD(),
                            startByte, endByte - startByte);
                    mPlayer.prepare();
                    mPlayStartOffset = mPlayStartMsec;
                } catch (Exception e) {
                    System.out.println("Exception trying to play file subset");
                    mPlayer.reset();
                    mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mPlayer.setDataSource(mFile.getAbsolutePath());
                    mPlayer.prepare();
                    mPlayStartOffset = 0;
                }
            }

            mPlayer.setOnCompletionListener(new OnCompletionListener() {
                public synchronized void onCompletion(MediaPlayer arg0) {
                    handlePause();
                }
            });
            mIsPlaying = true;

            if (mPlayStartOffset == 0) {
                mPlayer.seekTo(mPlayStartMsec);
            }
            mPlayer.start();
            updateDisplay();
            enableDisableButtons();
        } catch (Exception e) {
            showFinalAlert(e, R.string.play_error);
        }
    }

    /**
     * Show a "final" alert dialog that will exit the activity
     * after the user clicks on the OK button.  If an exception
     * is passed, it's assumed to be an error condition, and the
     * dialog is presented as an error, and the stack trace is
     * logged.  If there's no exception, it's a success message.
     */
    private void showFinalAlert(Exception e, CharSequence message) {
        CharSequence title;
        if (e != null) {
            Log.e("Ringdroid", "Error: " + message);
            Log.e("Ringdroid", getStackTrace(e));
            title = getResources().getText(R.string.alert_title_failure);
            setResult(RESULT_CANCELED, new Intent());
        } else {
            Log.i("Ringdroid", "Success: " + message);
            title = getResources().getText(R.string.alert_title_success);
        }

        new AlertDialog.Builder(RingdroidEditActivity.this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(
                        R.string.alert_ok_button,
		                (dialog, whichButton) -> finish())
		        .setCancelable(false)
                .show();
    }

    private void showFinalAlert(Exception e, int messageResourceId) {

        showFinalAlert(e, getResources().getText(messageResourceId));

    }

    private String makeRingtoneFilename(CharSequence title, String extension) {

        String subdir;
        String externalRootDir = Environment.getExternalStorageDirectory().getPath();
        if (!externalRootDir.endsWith("/")) {
            externalRootDir += "/";
        }
        subdir = "Muziko/";
        String parentdir = externalRootDir + subdir;

        // Create the parent directory
        File parentDirFile = new File(parentdir);
        if (!parentDirFile.exists()) {
            parentDirFile.mkdirs();
        }
        return parentdir + "/" + title + "." + extension;

    }

    private void saveRingtone(final CharSequence title) {

        final String outPath = makeRingtoneFilename(title, mExtension);

        if (outPath == null) {

            showFinalAlert(new Exception(), R.string.no_unique_filename);
            return;

        }

        mDstFilename = outPath;

        double startTime = mWaveformView.pixelsToSeconds(mStartPos);
        double endTime = mWaveformView.pixelsToSeconds(mEndPos);
        final int startFrame = mWaveformView.secondsToFrames(startTime);
        final int endFrame = mWaveformView.secondsToFrames(endTime);
        final int duration = (int) (endTime - startTime + 0.5);

        // Create an indeterminate progress dialog

        createProgressDialog();

        // Save the sound file in a background thread
        new Thread() {
            public void run() {
                final File outFile = new File(outPath);
                try {
                    // Write the new file
                    mSoundFile.WriteFile(outFile,
                            startFrame,
                            endFrame - startFrame);

                    // Try to load the new file to make sure it worked
                    final CheapSoundFile.ProgressListener listener =
		                    frac -> {
			                    // Do nothing - we're not going to try to
			                    // estimate when reloading a saved sound
			                    // since it's usually fast, but hard to
			                    // estimate anyway.
			                    return true;  // Keep going
		                    };
                    CheapSoundFile.create(outPath, listener);
                } catch (Exception e) {
                    mProgressDialogMaterial.dismiss();

                    CharSequence errorMessage;
                    if (e.getMessage().equals("No space left on device")) {
                        errorMessage = getResources().getText(
                                R.string.no_space_error);
                        e = null;
                    } else {
                        errorMessage = getResources().getText(
                                R.string.write_error);
                    }

                    final CharSequence finalErrorMessage = errorMessage;
                    final Exception finalException = e;
	                Runnable runnable = () -> handleFatalError(
			                "WriteError",
			                finalErrorMessage,
			                finalException);
	                mHandler.post(runnable);
                    return;
                }

                mProgressDialogMaterial.dismiss();

	            Runnable runnable = () -> afterSavingRingtone(title,
			            outPath,
			            outFile,
			            duration * 1000);
	            mHandler.post(runnable);
            }
        }.start();
    }

    private void afterSavingRingtone(CharSequence title,
                                     String outPath,
                                     File outFile,
                                     int duration) {

        long length = outFile.length();
        if (length <= 512) {
            outFile.delete();
            new AlertDialog.Builder(this)
                    .setTitle(R.string.alert_title_failure)
                    .setMessage(R.string.too_small_error)
                    .setPositiveButton(R.string.alert_ok_button, null)
                    .setCancelable(false)
                    .show();
            return;
        }

        // Create the database record, pointing to the existing file path

        long fileSize = outFile.length();
        String mimeType = "audio/wav";

        String artist = "" + getResources().getText(R.string.artist_name);

        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DATA, outPath);
        values.put(MediaStore.MediaColumns.TITLE, title.toString());
        values.put(MediaStore.MediaColumns.SIZE, fileSize);
        values.put(MediaStore.MediaColumns.MIME_TYPE, mimeType);

        values.put(MediaStore.Audio.Media.ARTIST, artist);
        values.put(MediaStore.Audio.Media.DURATION, duration);

        values.put(MediaStore.Audio.Media.IS_RINGTONE,
                true);
        values.put(MediaStore.Audio.Media.IS_NOTIFICATION,
                true);
        values.put(MediaStore.Audio.Media.IS_ALARM,
                true);
        values.put(MediaStore.Audio.Media.IS_MUSIC,
                false);

        // Insert it into the database
        Uri uri = MediaStore.Audio.Media.getContentUriForPath(outPath);
        final Uri newUri = getContentResolver().insert(uri, values);
        setResult(RESULT_OK, new Intent().setData(newUri));

        // Update a preference that counts how many times we've
        // successfully saved a ringtone or other audio
        SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
        int successCount = prefs.getInt(PREF_SUCCESS_COUNT, 0);
        SharedPreferences.Editor prefsEditor = prefs.edit();
        prefsEditor.putInt(PREF_SUCCESS_COUNT, successCount + 1);
        prefsEditor.apply();

        // If Ringdroid was launched to get content, just return

        // There's nothing more to do with music or an alarm.  Show a
        // success message and then quit.

        AppController.toast(RingdroidEditActivity.this, getResources().getString(R.string.save_success_message));

        //sendStatsToServerIfAllowedAndFinish();
        finish();


        // If it's a notification, give the user the option of making
        // this their default notification.  If they say no, we're finished.

        // If we get here, that means the type is a ringtone.  There are
        // three choices: make this your default ringtone, assign it to a
        // contact, or do nothing.
    }

    private void chooseContactForRingtone(Uri uri) {
        try {
            Intent intent = new Intent(Intent.ACTION_EDIT, uri);
            intent.setClassName(
                    "com.ringdroid",
                    "com.ringdroid.ChooseContactActivity");
            startActivityForResult(intent, REQUEST_CODE_CHOOSE_CONTACT);
        } catch (Exception e) {
            Log.e("Ringdroid", "Couldn't open Choose Contact window");
        }
    }

    private void handleFatalError(
            final CharSequence errorInternalName,
            final CharSequence errorString,
            final Exception exception) {
        Log.i("Ringdroid", "handleFatalError");

        SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
        int failureCount = prefs.getInt(PREF_ERROR_COUNT, 0);
        final SharedPreferences.Editor prefsEditor = prefs.edit();
        prefsEditor.putInt(PREF_ERROR_COUNT, failureCount + 1);
        prefsEditor.apply();

        // Check if we already have a pref for whether or not we can
        // contact the server.
        int serverAllowed = prefs.getInt(PREF_ERR_SERVER_ALLOWED,
                SERVER_ALLOWED_UNKNOWN);

        if (serverAllowed == SERVER_ALLOWED_NO) {
            Log.i("Ringdroid", "ERR: SERVER_ALLOWED_NO");

            // Just show a simple "write error" message
            showFinalAlert(exception, errorString);
            return;
        }

        if (serverAllowed == SERVER_ALLOWED_YES) {
            Log.i("Ringdroid", "SERVER_ALLOWED_YES");

            new AlertDialog.Builder(RingdroidEditActivity.this)
                    .setTitle(R.string.alert_title_failure)
                    .setMessage(errorString)
                    .setPositiveButton(
                            R.string.alert_ok_button,
		                    (dialog, whichButton) -> sendErrToServerAndFinish(
		                    ))
		            .setCancelable(false)
                    .show();
            return;
        }

        // The number of times the user must have had a failure before
        // we'll ask them.  Defaults to 1, and each time they click "Later"
        // we double and add 1.
        final int allowServerCheckIndex =
                prefs.getInt(PREF_ERR_SERVER_CHECK, 1);
        if (failureCount < allowServerCheckIndex) {
            Log.i("Ringdroid", "failureCount " + failureCount +
                    " is less than " + allowServerCheckIndex);
            // Just show a simple "write error" message
            showFinalAlert(exception, errorString);
            return;
        }

        final SpannableString message = new SpannableString(
                errorString + ". " +
                        getResources().getText(R.string.error_server_prompt));
        Linkify.addLinks(message, Linkify.ALL);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.alert_title_failure)
                .setMessage(message)
                .setPositiveButton(
                        R.string.server_yes,
		                (dialog13, whichButton) -> {
			                prefsEditor.putInt(PREF_ERR_SERVER_ALLOWED,
					                SERVER_ALLOWED_YES);
			                prefsEditor.apply();
			                sendErrToServerAndFinish(
			                );
                        })
                .setNeutralButton(
                        R.string.server_later,
		                (dialog12, whichButton) -> {
			                prefsEditor.putInt(PREF_ERR_SERVER_CHECK,
					                1 + allowServerCheckIndex * 2);
			                Log.i("Ringdroid",
					                "Won't check again until " +
							                (1 + allowServerCheckIndex * 2) +
							                " errors.");
			                prefsEditor.apply();
			                finish();
                        })
                .setNegativeButton(
                        R.string.server_never,
		                (dialog1, whichButton) -> {
			                prefsEditor.putInt(PREF_ERR_SERVER_ALLOWED,
					                SERVER_ALLOWED_NO);
			                prefsEditor.apply();
			                finish();
                        })
                .setCancelable(false)
                .show();

        // Make links clicky
        ((TextView) dialog.findViewById(android.R.id.message))
                .setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void onSave() {
        if (mIsPlaying) {
            handlePause();
        }

        new MaterialDialog.Builder(RingdroidEditActivity.this)
                .theme(Theme.LIGHT)
                .titleColorRes(R.color.normal_blue)
                .negativeColorRes(R.color.dialog_negetive_button)
                .title("Save Ringtone")
                .positiveText("SAVE")
                .negativeText("CANCEL")
                .inputType(InputType.TYPE_CLASS_TEXT)
		        .input("Ringtone Name", "", (dialog, input) -> {
			        // Do something
			        if (input.toString().length() > 0) {

				        saveRingtone(input);

			        } else {
                        AppController.toast(RingdroidEditActivity.this, "Enter Something!");
                    }

                }).show();

//        final Handler handler = new Handler() {
//                public void handleMessage(Message response) {
//                    CharSequence newTitle = (CharSequence)response.obj;
//                    mNewFileKind = response.arg1;
//                    saveRingtone(newTitle);
//                }
//            };
//        Message message = Message.obtain(handler);
//        FileSaveDialog dlog = new FileSaveDialog(
//            this, getResources(), mTitle, message);
//        dlog.show();
    }

    private void enableZoomButtons() {
    }

    private String getStackTrace(Exception e) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(stream, true);
        e.printStackTrace(writer);
        return stream.toString();
    }

    /**
     * Return extension including dot, like ".mp3"
     */
    private String getExtensionFromFilename(String filename) {
        try {
            return filename.substring(filename.lastIndexOf('.'), filename.length());
        } catch (Exception e) {
            return "mp3";
        }
    }

    private String getFilenameFromUri(Uri uri) {
        Cursor c = managedQuery(uri, null, "", null, null);
        if (c.getCount() == 0) {
            return null;
        }
        c.moveToFirst();
        int dataIndex = c.getColumnIndexOrThrow(
                MediaStore.Audio.Media.DATA);

        return c.getString(dataIndex);
    }

    private void sendStatsToServerIfAllowedAndFinish() {
        Log.i("Ringdroid", "sendStatsToServerIfAllowedAndFinish");

        final SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);

        // Check if we already have a pref for whether or not we can
        // contact the server.
        int serverAllowed = prefs.getInt(PREF_STATS_SERVER_ALLOWED,
                SERVER_ALLOWED_UNKNOWN);
        if (serverAllowed == SERVER_ALLOWED_NO) {
            Log.i("Ringdroid", "SERVER_ALLOWED_NO");
            finish();
            return;
        }

        if (serverAllowed == SERVER_ALLOWED_YES) {
            Log.i("Ringdroid", "SERVER_ALLOWED_YES");
            sendStatsToServerAndFinish();
            return;
        }

        // Number of times the user has successfully saved a sound.
        int successCount = prefs.getInt(PREF_SUCCESS_COUNT, 0);

        // The number of times the user must have successfully saved
        // a sound before we'll ask them.  Defaults to 2, and doubles
        // each time they click "Later".
        final int allowServerCheckIndex =
                prefs.getInt(PREF_STATS_SERVER_CHECK, 2);
        if (successCount < allowServerCheckIndex) {
            Log.i("Ringdroid", "successCount " + successCount +
                    " is less than " + allowServerCheckIndex);
            finish();
            return;
        }

        showServerPrompt(false);
    }

	private void showServerPrompt(final boolean userInitiated) {
		final SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);

        final SpannableString message = new SpannableString(
                getResources().getText(R.string.server_prompt));
        Linkify.addLinks(message, Linkify.ALL);

        final AlertDialog dialog = new AlertDialog.Builder(RingdroidEditActivity.this)
                .setTitle(R.string.server_title)
                .setMessage(message)
                .setPositiveButton(
                        R.string.server_yes,
		                (dialog13, whichButton) -> {
			                SharedPreferences.Editor prefsEditor = prefs.edit();
			                prefsEditor.putInt(PREF_STATS_SERVER_ALLOWED,
					                SERVER_ALLOWED_YES);
			                prefsEditor.apply();
			                if (userInitiated) {
				                finish();
			                } else {
				                sendStatsToServerAndFinish();
                            }
                        })
                .setNeutralButton(
                        R.string.server_later,
		                (dialog12, whichButton) -> {
			                int allowServerCheckIndex =
					                prefs.getInt(PREF_STATS_SERVER_CHECK, 2);
			                int successCount = prefs.getInt(PREF_SUCCESS_COUNT, 0);
			                SharedPreferences.Editor prefsEditor = prefs.edit();
			                if (userInitiated) {
				                prefsEditor.putInt(PREF_STATS_SERVER_CHECK,
						                successCount + 2);

			                } else {
				                prefsEditor.putInt(PREF_STATS_SERVER_CHECK,
						                allowServerCheckIndex * 2);
			                }
			                prefsEditor.apply();
			                finish();
		                })
                .setNegativeButton(
                        R.string.server_never,
		                (dialog1, whichButton) -> {
			                SharedPreferences.Editor prefsEditor = prefs.edit();
			                prefsEditor.putInt(PREF_STATS_SERVER_ALLOWED,
					                SERVER_ALLOWED_NO);
			                if (userInitiated) {
				                // If the user initiated, err on the safe side and disable
				                // sending crash reports too. There's no way to turn them
				                // back on now aside from clearing data from this app, but
				                // it doesn't matter, we don't need error reports from every
				                // user ever.
				                prefsEditor.putInt(PREF_ERR_SERVER_ALLOWED,
                                        SERVER_ALLOWED_NO);
                            }
			                prefsEditor.apply();
			                finish();
		                })
                .setCancelable(false)
                .show();

        // Make links clicky
        ((TextView) dialog.findViewById(android.R.id.message))
                .setMovementMethod(LinkMovementMethod.getInstance());
    }

	private void sendStatsToServerAndFinish() {
		Log.i("Ringdroid", "sendStatsToServerAndFinish");
        new Thread() {
            public void run() {
                //sendToServer(STATS_SERVER_URL, null, null);
            }
        }.start();
        Log.i("Ringdroid", "sendStatsToServerAndFinish calling finish");
        finish();
    }

	private void sendErrToServerAndFinish() {
		Log.i("Ringdroid", "sendErrToServerAndFinish");
        new Thread() {
            public void run() {
                //sendToServer(ERR_SERVER_URL, errType, exception);
            }
        }.start();
        Log.i("Ringdroid", "sendErrToServerAndFinish calling finish");
        finish();
    }

    /**
     * Nothing nefarious about this; the purpose is just to
     * uniquely identify each user so we don't double-count the same
     * ringtone - without actually identifying the actual user.
     */
    long getUniqueId() {
        SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
        long uniqueId = prefs.getLong(PREF_UNIQUE_ID, 0);
        if (uniqueId == 0) {
            uniqueId = new Random().nextLong();

            SharedPreferences.Editor prefsEditor = prefs.edit();
            prefsEditor.putLong(PREF_UNIQUE_ID, uniqueId);
            prefsEditor.apply();
        }

        return uniqueId;
    }

    /**
     * If the exception is not null, will send the stack trace.
     */

    private void createProgressDialog() {

        mProgressDialogMaterial = new MaterialDialog.Builder(this)
                .theme(Theme.LIGHT)
                .progress(true, 0)
                .titleColorRes(R.color.normal_blue)
                .negativeColorRes(R.color.dialog_negetive_button)
                .title("Please Wait")
                .content("Saving Ringtone").show();

    }
}
