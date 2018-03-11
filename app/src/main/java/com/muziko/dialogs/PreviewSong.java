package com.muziko.dialogs;

import android.content.Context;
import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.arasthel.asyncjob.AsyncJob;
import com.crashlytics.android.Crashlytics;
import com.muziko.R;
import com.muziko.common.models.QueueItem;
import com.muziko.helpers.Utils;
import com.muziko.manager.AppController;
import com.muziko.mediaplayer.MuzikoExoPlayer;
import com.muziko.mediaplayer.PlayerConstants;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/** Created by dev on 13/09/2016. */
public class PreviewSong {
    private static final int aimStart = 10000;
    private Context mcontext;
    private Timer timer = null;
    private int start = 0;
    private MediaPlayer mediaPlayer;
    private ImageButton btnPause;
    private ImageButton btnPlay;
    private TextView startText;
    private SeekBar playerSeekBar;
    private boolean userTouch = false;
    private boolean songPlaying = false;

    public void open(final Context context, final QueueItem queue) {
        mcontext = context;

        View view =
                LayoutInflater.from(mcontext).inflate(R.layout.dialog_preview_track, null, false);

        RelativeLayout buttonlayout = view.findViewById(R.id.buttonlayout);
        btnPause = buttonlayout.findViewById(R.id.btnPause);
        btnPlay = buttonlayout.findViewById(R.id.btnPlay);
        playerSeekBar = view.findViewById(R.id.playerSeekBar);
        startText = view.findViewById(R.id.startText);
        btnPlay.setVisibility(View.GONE);
        btnPause.setVisibility(View.VISIBLE);

        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(queue.data);
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        int aimEnd = 20000;
        int end = 0;
        if (Float.parseFloat(queue.duration) > aimEnd) {
            start = aimStart;
            end = aimEnd;
        } else {
            start = Integer.parseInt(queue.duration) - aimStart;
            end = Integer.parseInt(queue.duration);
        }

        playerSeekBar.setMax(aimStart);
        playerSeekBar.setProgress(0);
        playerSeekBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        // TODO Auto-generated method stub

                        startText.setText(Utils.getDuration(progress));

                        if (fromUser) {
                            mediaPlayer.seekTo(progress + aimStart);
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        userTouch = true;
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        userTouch = false;
                    }
                });

        btnPlay.setOnClickListener(
                v -> {
                    mediaPlayer.start();
                    btnPlay.setVisibility(View.GONE);
                    btnPause.setVisibility(View.VISIBLE);
                });

        btnPause.setOnClickListener(
                v -> {
                    mediaPlayer.pause();
                    btnPlay.setVisibility(View.VISIBLE);
                    btnPause.setVisibility(View.GONE);
                });

        mediaPlayer.seekTo(start);
        mediaPlayer.start();
        timerStart();

        if (MuzikoExoPlayer.Instance().isPlaying()) {
            songPlaying = true;
            AppController.Instance().servicePause();
        } else {
            songPlaying = false;
        }

        MaterialDialog previewdialog =
                new MaterialDialog.Builder(mcontext)
                        .theme(Theme.LIGHT)
                        .titleColorRes(R.color.normal_blue)
                        .negativeColorRes(R.color.dialog_negetive_button)
                        .positiveColorRes(R.color.normal_blue)
                        .customView(view, false)
                        .autoDismiss(false)
                        .neutralText("Cancel")
                        .onNeutral((dialog, which) -> dialog.dismiss())
                        .dismissListener(
                                dialog -> {
                                    timerStop();
                                    mediaPlayer.stop();
                                    mediaPlayer.release();
                                    if (PlayerConstants.QUEUE_STATE
                                                    != PlayerConstants.QUEUE_STATE_STOPPED
                                            && songPlaying) {
                                        AppController.Instance().serviceResume(false);
                                    }
                                })
                        .show();
    }

    private void timerStart() {
        if (timer == null) timer = new Timer();

        timer.scheduleAtFixedRate(new PreviewTimerTask(), 0, PlayerConstants.UPDATE_TIME);
    }

    private void timerStop() {
        try {
            if (timer != null) timer.cancel();

            timer = null;
        } catch (Exception e) {
            Crashlytics.logException(e);
        }
    }

    private class PreviewTimerTask extends TimerTask {
        public void run() {

            if (mediaPlayer != null && !userTouch) {
                if (mediaPlayer.isPlaying()) {
                    int progress = 0;
                    int position = mediaPlayer.getCurrentPosition();
                    progress = position - aimStart;
                    playerSeekBar.setProgress(progress);
                    if (progress > aimStart) {
                        mediaPlayer.pause();
                        mediaPlayer.seekTo(start);

                        AsyncJob.doOnMainThread(
                                () -> {
                                    btnPlay.setVisibility(View.VISIBLE);
                                    btnPause.setVisibility(View.GONE);
                                });
                    }
                }
            }
        }
    }
}
