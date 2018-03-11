package com.muziko.dialogs;

import android.content.Context;
import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.muziko.R;
import com.muziko.common.models.QueueItem;
import com.muziko.controls.RangeBar.RangeBar;
import com.muziko.database.TrackRealmHelper;
import com.muziko.manager.AppController;
import com.muziko.mediaplayer.MuzikoExoPlayer;
import com.muziko.mediaplayer.PlayerConstants;

import java.io.IOException;

/** Created by dev on 13/09/2016. */
public class PlayFrom {
    private Context mcontext;
    private MediaPlayer mediaPlayer;
    private ImageButton btnPause;
    private ImageButton btnPlay;
    private RangeBar rangebar;
    private boolean songPlaying = false;
    private int start = 0;
    private int end = 0;
    private boolean mediaPlyerReleased = false;

    public void open(final Context context, final QueueItem queue) {
        mcontext = context;

        View view = LayoutInflater.from(mcontext).inflate(R.layout.dialog_play_from, null, false);
        RelativeLayout buttonlayout = view.findViewById(R.id.buttonlayout);
        btnPause = buttonlayout.findViewById(R.id.btnPause);
        btnPlay = buttonlayout.findViewById(R.id.btnPlay);
        rangebar = view.findViewById(R.id.rangebar);

        btnPlay.setVisibility(View.VISIBLE);
        btnPause.setVisibility(View.GONE);

        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(queue.data);
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        rangebar.setDisableRightThumb(true);

        rangebar.setTickEnd(Float.parseFloat(queue.duration) / 1000);

        QueueItem queueItem = TrackRealmHelper.getTrack(queue.data);

        if (queueItem.startFrom > 0) {
            start = queueItem.startFrom / 1000;
            rangebar.setRangePinsByValue(start, rangebar.getRightIndex());
        } else {
            start = rangebar.getLeftIndex() * 1000;
        }
        end = rangebar.getRightIndex() * 1000;
        rangebar.setOnRangeBarChangeListener(
                (rangeBar, leftPinIndex, rightPinIndex, leftPinValue, rightPinValue) -> {
                    start = leftPinIndex * 1000;
                    end = rightPinIndex * 1000;
                    int seekto = 0;
                    int currentposition = mediaPlayer.getCurrentPosition();
                    if (currentposition / 1000 != leftPinIndex) {
                        seekto = leftPinIndex * 1000;
                        mediaPlayer.seekTo(seekto);
                    }
                    if (mediaPlayer.getCurrentPosition() / 1000 > rightPinIndex) {
                        mediaPlayer.pause();
                        btnPlay.setVisibility(View.VISIBLE);
                        btnPause.setVisibility(View.GONE);
                        mediaPlayer.seekTo(leftPinIndex * 1000);
                    }
                });

        rangebar.setFormatter(
                s -> {
                    double d = Double.parseDouble(s);
                    int totalSecs = (int) d;
                    int hours = totalSecs / 3600;
                    int minutes = (totalSecs % 3600) / 60;
                    int seconds = totalSecs % 60;

                    return String.format("%02d:%02d", minutes, seconds);
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
                    mediaPlayer.seekTo(rangebar.getLeftIndex() * 1000);
                    btnPlay.setVisibility(View.VISIBLE);
                    btnPause.setVisibility(View.GONE);
                });

        if (MuzikoExoPlayer.Instance().isPlaying()) {
            songPlaying = true;
            AppController.Instance().servicePause();
        } else {
            songPlaying = false;
        }

        new MaterialDialog.Builder(context)
                .theme(Theme.LIGHT)
                .titleColorRes(R.color.normal_blue)
                .neutralColorRes(R.color.dialog_negetive_button)
                .positiveColorRes(R.color.normal_blue)
                .customView(view, false)
                .autoDismiss(true)
                .positiveText("Set")
                .onPositive(
                        (dialog, which) -> {
                            queue.startFrom = rangebar.getLeftIndex() * 1000;
                            TrackRealmHelper.updateStartTime(queue);

                            mediaPlayer.stop();
                            mediaPlayer.release();
                            mediaPlyerReleased = true;
                            dialog.dismiss();
                        })
                .neutralText("Cancel")
                .onNeutral((dialog, which) -> dialog.dismiss())
                .dismissListener(
                        dialog -> {
                            if (mediaPlayer != null) {
                                if (!mediaPlyerReleased) {
                                    if (mediaPlayer.isPlaying()) {
                                        mediaPlayer.stop();
                                    }
                                    mediaPlayer.release();
                                }
                            }

                            if (PlayerConstants.QUEUE_STATE != PlayerConstants.QUEUE_STATE_STOPPED
                                    && songPlaying) {
                                AppController.Instance().serviceResume(false);
                            }
                        })
                .show();
    }
}
