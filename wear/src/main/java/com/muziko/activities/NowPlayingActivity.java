package com.muziko.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.badoo.mobile.util.WeakHandler;
import com.muziko.MuzikoWearApp;
import com.muziko.PlayerConstants;
import com.muziko.R;
import com.muziko.common.CommonConstants;
import com.muziko.common.events.ProgressEvent;
import com.muziko.common.events.RefreshEvent;
import com.muziko.common.events.buswear.WearActionEvent;
import com.muziko.controls.CircularSeekBar;
import com.muziko.controls.SemiCircleView;
import com.muziko.helpers.ImageUtils;
import com.muziko.helpers.Prefs;
import com.muziko.helpers.Utils;
import com.tr4android.support.extension.drawable.MediaControlDrawable;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import pl.tajchert.buswear.EventBus;
import pl.tajchert.buswear.events.ImageAssetEvent;

public class NowPlayingActivity extends Activity implements View.OnClickListener, View.OnLongClickListener, CircularSeekBar.OnCircularSeekBarChangeListener {

    private final WeakHandler handler = new WeakHandler();
    private ImageView blurredImage;
    private ImageView covertArtImage;
    private ImageView holderImage;
    private SemiCircleView innerCircle;
    private ImageButton shuffleButton;
    private ImageButton repeatButton;
    private TextView titleText;
    private TextView artistText;
    private ImageButton previousButton;
    private ImageView playpauseButton;
    private ImageButton nextButton;
    private CircularSeekBar progressBar;
    private TextView startText;
    private TextView endText;
    private boolean userTouch;
    private final Runnable allowProgress = new Runnable() {
        @Override
        public void run() {

            userTouch = false;
        }
    };
    private MediaControlDrawable mediaControlDrawable;
    private float scale;
    private Bitmap coverArtBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_now_playing);
        findViewsById();

        scale = getResources().getDisplayMetrics().density;

        innerCircle.setOnClickListener(this);
        innerCircle.setOnLongClickListener(this);
        shuffleButton.setOnClickListener(this);
        repeatButton.setOnClickListener(this);
        previousButton.setOnClickListener(this);
        playpauseButton.setOnClickListener(this);
        nextButton.setOnClickListener(this);
        progressBar.setOnSeekBarChangeListener(this);

        mediaControlDrawable =
                new MediaControlDrawable.Builder(this)
                        .setColor(ContextCompat.getColor(this, R.color.light_blue))
                        .setPadding(2 * scale)
                        .setInitialState(MediaControlDrawable.State.PLAY)
                        .build();
        playpauseButton.setImageDrawable(mediaControlDrawable);

        EventBus.getDefault(this).register(this);

        updateUI();
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault(this).unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onImageAssetEvent(ImageAssetEvent imageAssetEvent) {

        coverArtBitmap = imageAssetEvent.getBitmap();
        updateUI();
    }

    private void findViewsById() {
        blurredImage = (ImageView) findViewById(R.id.blurredImage);
        innerCircle = (SemiCircleView) findViewById(R.id.innerCircle);
        covertArtImage = (ImageView) findViewById(R.id.covertArtImage);
        holderImage = (ImageView) findViewById(R.id.holderImage);
        shuffleButton = (ImageButton) findViewById(R.id.shuffleButton);
        repeatButton = (ImageButton) findViewById(R.id.repeatButton);
        titleText = (TextView) findViewById(R.id.titleText);
        artistText = (TextView) findViewById(R.id.artistText);
        previousButton = (ImageButton) findViewById(R.id.previousButton);
        playpauseButton = (ImageView) findViewById(R.id.playpauseButton);
        nextButton = (ImageButton) findViewById(R.id.nextButton);
        progressBar = (CircularSeekBar) findViewById(R.id.progressBar);
        startText = (TextView) findViewById(R.id.startText);
        endText = (TextView) findViewById(R.id.endText);
    }

    private void updateUI() {
        titleText.setText(PlayerConstants.QUEUE_SONG.title);
        artistText.setText(PlayerConstants.QUEUE_SONG.artist_name);
        startText.setText(Utils.getDuration(PlayerConstants.QUEUE_TIME));
        endText.setText(Utils.getDuration(PlayerConstants.QUEUE_DURATION));

        progressBar.setMax(PlayerConstants.QUEUE_DURATION);
        progressBar.setProgress(PlayerConstants.QUEUE_TIME);

        updatePlayButton();
        toggleRepeat(false);
        toggleShuffle(false);

        if (coverArtBitmap != null) {

//            Bitmap resizedbitmap = Bitmap.createBitmap(coverArtBitmap, 0, 0, coverArtBitmap.getWidth(), coverArtBitmap.getHeight() / 2);
//            Bitmap cropped = ImageUtils.getMiniCropBitmap(coverArtBitmap);
            holderImage.setImageBitmap(coverArtBitmap);
            holderImage.buildDrawingCache();
            Bitmap bmap = holderImage.getDrawingCache();

            covertArtImage.setImageBitmap(bmap);

            setBlurredAlbumArt blurredAlbumArt = new setBlurredAlbumArt();
            blurredAlbumArt.execute(coverArtBitmap);
        } else {
            Drawable wearColor = ContextCompat.getDrawable(this, R.color.wear_background);
            Bitmap bitmap = ImageUtils.drawableToBitmap(wearColor);
            covertArtImage.setImageBitmap(bitmap);

            setBlurredAlbumArt blurredAlbumArt = new setBlurredAlbumArt();
            blurredAlbumArt.execute(bitmap);
        }


    }

    private void updatePlayButton() {
        if (PlayerConstants.QUEUE_STATE == PlayerConstants.QUEUE_STATE_PLAYING) {
            mediaControlDrawable.setMediaControlState(MediaControlDrawable.State.PAUSE);
        } else {
            mediaControlDrawable.setMediaControlState(MediaControlDrawable.State.PLAY);
        }
    }

    private void toggleRepeat(boolean change) {
        if (change) {
            int repeat = Prefs.getPlayRepeat(this);
            repeat++;
            if (repeat >= PlayerConstants.REPEAT_TOTAL) repeat = 0;
            Prefs.setPlayRepeat(this, repeat);
            EventBus.getDefault(NowPlayingActivity.this).postRemote(new WearActionEvent(CommonConstants.ACTION_WEAR_REPEAT, repeat));
        }

        switch (Prefs.getPlayRepeat(this)) {
            case PlayerConstants.REPEAT_OFF:
                repeatButton.setImageResource((R.drawable.repeat));
                break;

            case PlayerConstants.REPEAT_ALL:
                repeatButton.setImageResource((R.drawable.repeat_blue));
                break;

            case PlayerConstants.REPEAT_ONE:
                repeatButton.setImageResource((R.drawable.repeat_one));
                break;
        }
    }

    private void toggleShuffle(boolean change) {
        if (change) {
            Prefs.setPlayShuffle(this, !Prefs.getPlayShuffle(this));
            EventBus.getDefault(NowPlayingActivity.this).postRemote(new WearActionEvent(CommonConstants.ACTION_WEAR_SHUFFLE, Prefs.getPlayShuffle(this) ? 1 : 0));
        }

        if (Prefs.getPlayShuffle(this)) {
            shuffleButton.setImageResource((R.drawable.shuffle_blue));
        } else {
            shuffleButton.setImageResource((R.drawable.shuffle));
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRefreshEvent(RefreshEvent event) {

        updateUI();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onProgressEvent(ProgressEvent event) {

        if (!userTouch) {
            progressBar.setMax(event.getDuration());
            progressBar.setProgress(event.getPosition());

            startText.setText(Utils.getDuration(event.getPosition()));
        }
    }

    @Override
    public void onClick(View v) {

        if (v == playpauseButton) {
            EventBus.getDefault(NowPlayingActivity.this).postRemote(new WearActionEvent(CommonConstants.ACTION_WEAR_TOGGLE, MuzikoWearApp.wearPosition));
        } else if (v == previousButton) {
            EventBus.getDefault(NowPlayingActivity.this).postRemote(new WearActionEvent(CommonConstants.ACTION_WEAR_PREV, MuzikoWearApp.wearPosition));
        } else if (v == nextButton) {
            EventBus.getDefault(NowPlayingActivity.this).postRemote(new WearActionEvent(CommonConstants.ACTION_WEAR_NEXT, MuzikoWearApp.wearPosition));
        } else if (v == repeatButton) {
            toggleRepeat(true);
        } else if (v == shuffleButton) {
            toggleShuffle(true);
        } else if (v == innerCircle) {
            Intent activityIntent = new Intent(NowPlayingActivity.this, SongsActivity.class);
            startActivity(activityIntent);
        }
    }

    @Override
    public boolean onLongClick(View v) {

        if (v == innerCircle) {
            MuzikoWearApp.wearPosition = PlayerConstants.QUEUE_INDEX;
            Intent intent = new Intent(NowPlayingActivity.this, MenuOneActivity.class);
            startActivity(intent);
        }
        return false;
    }

    @Override
    public void onProgressChanged(CircularSeekBar circularSeekBar, int progress, boolean fromUser) {
        if (userTouch) {
            startText.setText(Utils.getDuration(progress));
        }
    }

    @Override
    public void onStopTrackingTouch(CircularSeekBar seekBar) {

        handler.removeCallbacksAndMessages(allowProgress);
        handler.postDelayed(allowProgress, 1000);

        EventBus.getDefault(NowPlayingActivity.this).postRemote(new WearActionEvent(CommonConstants.ACTION_WEAR_SEEK, seekBar.getProgress()));
    }

    @Override
    public void onStartTrackingTouch(CircularSeekBar seekBar) {
        userTouch = true;
    }

    private class setBlurredAlbumArt extends AsyncTask<Bitmap, Void, Drawable> {

        @Override
        protected Drawable doInBackground(Bitmap... loadedImage) {
            Drawable drawable = null;
            try {
                drawable = ImageUtils.createBlurredImageFromBitmap(loadedImage[0], NowPlayingActivity.this, 6);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return drawable;
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onPostExecute(Drawable result) {
            if (result != null) {
                if (blurredImage.getDrawable() != null) {
                    final TransitionDrawable td =
                            new TransitionDrawable(new Drawable[]{
                                    blurredImage.getDrawable(),
                                    result
                            });
                    blurredImage.setImageDrawable(td);
                    td.startTransition(200);

                } else {
                    blurredImage.setImageDrawable(result);
                }
            }
        }
    }

}
