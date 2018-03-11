package com.muziko.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.arasthel.asyncjob.AsyncJob;
import com.badoo.mobile.util.WeakHandler;
import com.crashlytics.android.Crashlytics;
import com.google.firebase.auth.FirebaseAuth;
import com.muziko.MyApplication;
import com.muziko.R;
import com.muziko.activities.register.RegisterActivity;
import com.muziko.adapter.MaterialMenuAdapter;
import com.muziko.common.models.QueueItem;
import com.muziko.controls.RangeBar.RangeBar;
import com.muziko.cutter.ringtone_lib.WaveformView;
import com.muziko.cutter.ringtone_lib.soundfile.CheapSoundFile;
import com.muziko.helpers.FileHelper;
import com.muziko.manager.AppController;
import com.muziko.manager.MuzikoConstants;
import com.muziko.mediaplayer.MuzikoExoPlayer;
import com.muziko.mediaplayer.PlayerConstants;
import com.muziko.objects.MenuObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import static com.muziko.MyApplication.networkState;

/**
 * Created by dev on 13/09/2016.
 */
public class ShareRingtone {
    private final WeakHandler handler = new WeakHandler();
    private final ArrayList<QueueItem> selectedItems = new ArrayList<>();
    private Activity mcontext;
    private WaveformView mWaveformView;
    private CheapSoundFile mSoundFile;
    private Timer timer = null;
    private MediaPlayer mediaPlayer;
    private ImageButton btnPause;
    private ImageButton btnPlay;
    private RangeBar rangebar;
    private EditText ringtoneName;
    private CheckBox notSaveCheckbox;
    private boolean songPlaying = false;
    private int start = 0;
    private int end = 0;
    private boolean mediaPlayerReleased = false;
    private MaterialDialog shareDialog;
    private MaterialDialog progressDialog;
    private QueueItem selectedItem;
    private final MaterialMenuAdapter.Callback onSubMenuObjectItemSelected =
            new MaterialMenuAdapter.Callback() {
                @Override
                public void onMenuObjectItemSelected(MaterialDialog dialog, int index, MenuObject item) {

                    handler.postDelayed(
                            () -> {
                                switch (index) {
                                    case 0: //send
                                        selectedItems.clear();
                                        selectedItems.add(selectedItem);

                                        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                                            if (FirebaseAuth.getInstance().getCurrentUser().isAnonymous()) {
                                                if (networkState == NetworkInfo.State.CONNECTED) {
                                                    Intent registerIntent = new Intent(mcontext, RegisterActivity.class);
                                                    mcontext.startActivityForResult(registerIntent, MuzikoConstants.REQUEST_REGISTER_USER_TRACKS);
                                                } else {
                                                    AppController.toast(mcontext, mcontext.getString(R.string.no_internet_for_register));
                                                }

                                            } else {
                                                AppController.Instance().sendTracks(mcontext, selectedItems);
                                            }
                                        } else {
                                            if (networkState == NetworkInfo.State.CONNECTED) {
                                                Intent registerIntent = new Intent(mcontext, RegisterActivity.class);
                                                mcontext.startActivityForResult(registerIntent, MuzikoConstants.REQUEST_REGISTER_USER_TRACKS);
                                            } else {
                                                AppController.toast(mcontext, mcontext.getString(R.string.no_internet_for_register));
                                            }
                                        }

                                        break;

                                    case 1: //send wifi
                                        selectedItems.clear();
                                        selectedItems.add(selectedItem);

                                        AppController.Instance().sendTracksWifi(mcontext, selectedItems);

                                        break;
                                }

                                dialog.dismiss();
                            },
                            mcontext.getResources().getInteger(R.integer.ripple_duration_delay));
                }
            };

    public void open(final Activity context, final QueueItem queue) {
        mcontext = context;
        MyApplication.pauseDeletingTempRingtone = true;

        View view =
                LayoutInflater.from(mcontext).inflate(R.layout.dialog_share_ringtone, null, false);
        mWaveformView = view.findViewById(R.id.waveform);
        RelativeLayout buttonlayout = view.findViewById(R.id.buttonlayout);
        btnPause = buttonlayout.findViewById(R.id.btnPause);
        btnPlay = buttonlayout.findViewById(R.id.btnPlay);
        rangebar = view.findViewById(R.id.rangebar);

        View shareRingtoneLayout =
                LayoutInflater.from(mcontext)
                        .inflate(R.layout.dialog_save_share_ringtone, null, false);
        notSaveCheckbox = shareRingtoneLayout.findViewById(R.id.notSaveCheckbox);
        ringtoneName = shareRingtoneLayout.findViewById(R.id.ringtoneName);

        btnPlay.setVisibility(View.VISIBLE);
        btnPause.setVisibility(View.GONE);

        progressDialog =
                new MaterialDialog.Builder(mcontext)
                        .title("Sharing AU-Clip")
                        .content("Please wait for AU-Clip to be generated")
                        .progress(true, 0)
                        .build();

        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(queue.data);
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        timerStart();

        rangebar.setTickEnd(Float.parseFloat(queue.duration) / 1000);

        start = rangebar.getLeftIndex() * 1000;
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

        shareDialog =
                new MaterialDialog.Builder(context)
                        .theme(Theme.LIGHT)
                        .titleColorRes(R.color.normal_blue)
                        .neutralColorRes(R.color.dialog_negetive_button)
                        .positiveColorRes(R.color.normal_blue)
                        .negativeColorRes(R.color.normal_blue)
                        .customView(view, false)
                        .autoDismiss(false)
                        .positiveText("Share")
                        .onPositive(
                                (dialog, which) -> {
                                    if (Double.parseDouble(rangebar.getRightPinValue())
                                            - Double.parseDouble(rangebar.getLeftPinValue())
                                            < 10) {
                                        AppController.toast(context, "AU-Clip must be at least 10 seconds");
                                    } else {
                                        dialog.dismiss();
                                        timerStop();
                                        if (mediaPlayer != null) {
                                            if (!mediaPlayerReleased) {
                                                if (mediaPlayer.isPlaying()) {
                                                    mediaPlayer.stop();
                                                }
                                                mediaPlayer.release();
                                                mediaPlayerReleased = true;
                                            }
                                        }

                                        progressDialog.show();

                                        AsyncJob.doInBackground(
                                                () -> {
                                                    shareRingtone(
                                                            context,
                                                            queue,
                                                            "",
                                                            rangebar.getLeftIndex(),
                                                            rangebar.getRightIndex());
                                                });
                                    }
                                })
                        .negativeText("Send")
                        .onNegative(
                                (dialog, which) -> {
                                    if (Double.parseDouble(rangebar.getRightPinValue())
                                            - Double.parseDouble(rangebar.getLeftPinValue())
                                            < 10) {
                                        AppController.toast(context, "AU-Clip must be at least 10 seconds");
                                    } else {
                                        dialog.dismiss();
                                        timerStop();
                                        if (mediaPlayer != null) {
                                            if (!mediaPlayerReleased) {
                                                if (mediaPlayer.isPlaying()) {
                                                    mediaPlayer.stop();
                                                }
                                                mediaPlayer.release();
                                                mediaPlayerReleased = true;
                                            }
                                        }

                                        new MaterialDialog.Builder(context)
                                                .theme(Theme.LIGHT)
                                                .titleColorRes(R.color.normal_blue)
                                                .neutralColorRes(R.color.normal_blue)
                                                .positiveColorRes(R.color.normal_blue)
                                                .negativeColorRes(R.color.dialog_negetive_button)
                                                .title("Set AU-Clip name")
                                                .customView(shareRingtoneLayout, false)
                                                .autoDismiss(false)
                                                .negativeText("Cancel")
                                                .onNegative(
                                                        (namedialog, namewhich) -> {
                                                            namedialog.dismiss();
                                                        })
                                                .positiveText("Set")
                                                .onPositive(
                                                        (namedialog, namewhich) -> {
                                                            namedialog.dismiss();
                                                            progressDialog.show();

                                                            if (notSaveCheckbox.isChecked()) {
                                                                AsyncJob.doInBackground(
                                                                        () -> {
                                                                            sendRingtone(
                                                                                    context,
                                                                                    queue,
                                                                                    "",
                                                                                    rangebar
                                                                                            .getLeftIndex(),
                                                                                    rangebar
                                                                                            .getRightIndex());
                                                                        });
                                                            } else {
                                                                AsyncJob.doInBackground(
                                                                        () -> {
                                                                            sendRingtone(
                                                                                    context,
                                                                                    queue,
                                                                                    ringtoneName
                                                                                            .getText()
                                                                                            .toString(),
                                                                                    rangebar
                                                                                            .getLeftIndex(),
                                                                                    rangebar
                                                                                            .getRightIndex());
                                                                        });
                                                            }
                                                        })
                                                .show();
                                    }
                                })
                        .neutralText("Cancel")
                        .onNeutral(
                                (dialog, which) -> {
                                    MyApplication.pauseDeletingTempRingtone = false;
                                    timerStop();
                                    if (mediaPlayer != null) {
                                        if (!mediaPlayerReleased) {
                                            if (mediaPlayer.isPlaying()) {
                                                mediaPlayer.stop();
                                            }
                                            mediaPlayer.release();
                                            mediaPlayerReleased = true;
                                        }
                                    }
                                    dialog.dismiss();

                                    if (PlayerConstants.QUEUE_STATE
                                            != PlayerConstants.QUEUE_STATE_STOPPED
                                            && songPlaying) {
                                        AppController.Instance().serviceResume(false);
                                    }
                                })
                        .dismissListener(
                                dialog -> {
                                    timerStop();
                                    if (mediaPlayer != null) {
                                        if (!mediaPlayerReleased) {
                                            if (mediaPlayer.isPlaying()) {
                                                mediaPlayer.stop();
                                            }
                                            mediaPlayer.release();
                                            mediaPlayerReleased = true;
                                        }
                                    }
                                    dialog.dismiss();

                                    if (PlayerConstants.QUEUE_STATE
                                            != PlayerConstants.QUEUE_STATE_STOPPED
                                            && songPlaying) {
                                        AppController.Instance().serviceResume(false);
                                    }
                                })
                        .build();

        shareDialog.show();
    }

    private void timerStart() {
        if (timer == null) timer = new Timer();

        timer.scheduleAtFixedRate(new PreviewTimerTask(), 0, 1000);
    }

    private void timerStop() {
        try {
            if (timer != null) timer.cancel();

            timer = null;
        } catch (Exception e) {
            Crashlytics.logException(e);
        }
    }

    private void shareRingtone(
            final Context context,
            final QueueItem queueItem,
            final CharSequence title,
            final int start,
            final int end) {
        File mFile = new File(queueItem.data);
        String mExtension = FileHelper.getExtensionFromFilename(queueItem.data);

        final String outPath =
                FileHelper.makeRingtoneFilename(
                        mcontext,
                        title.length() == 0 ? mcontext.getString(R.string.au_clip) : title);

        final File deleteOldFile = new File(outPath);
        deleteOldFile.delete();

        mSoundFile = null;
        try {
            mSoundFile = CheapSoundFile.create(mFile.getAbsolutePath(), null);
        } catch (IOException e) {
            e.printStackTrace();
            Crashlytics.logException(e);
            AppController.toast(mcontext, "Unable to create ringtone from file");
            return;
        }

        mWaveformView.setSoundFile(mSoundFile);
        final int startFrame = mWaveformView.secondsToFrames((double) start);
        final int endFrame = mWaveformView.secondsToFrames((double) end);
        final int duration = (int) ((double) end - (double) start + 0.5);

        final File outFile = new File(outPath);
        try {

            // Write the new file
            mSoundFile.WriteFile(outFile, startFrame, endFrame - startFrame);

            // Try to load the new file to make sure it worked
            final CheapSoundFile.ProgressListener listener =
                    frac -> {
                        // Do nothing - we're not going to try to
                        // estimate when reloading a saved sound
                        // since it's usually fast, but hard to
                        // estimate anyway.
                        return true; // Keep going
                    };
            CheapSoundFile.create(outPath, listener);
        } catch (Exception ex) {
            Crashlytics.logException(ex);
            //					mProgressDialogMaterial.dismiss();
            CharSequence errorMessage;
            if (ex.getMessage().equals("No space left on device")) {
                errorMessage = context.getResources().getText(R.string.no_space_error);
                ex = null;
            } else {
                errorMessage = context.getResources().getText(R.string.write_error);
            }
            AppController.toast(context, errorMessage.toString());
            return;
        }

        long length = outFile.length();
        if (length <= 512) {
            outFile.delete();
            progressDialog.dismiss();

            new AlertDialog.Builder(context)
                    .setTitle(R.string.alert_title_failure)
                    .setMessage(R.string.too_small_error)
                    .setPositiveButton(R.string.alert_ok_button, null)
                    .setCancelable(false)
                    .show();
            return;
        }

        progressDialog.dismiss();

        try {
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("audio/*");
            share.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + outPath));
            share.putExtra(Intent.EXTRA_SUBJECT, "Shared Ringtone");
            share.putExtra(Intent.EXTRA_TEXT, queueItem.title);
            mcontext.startActivity(Intent.createChooser(share, "Share ringtone via..."));
        } catch (Exception ex) {
            AppController.toast(mcontext, "Error sharing ringtone! Contact Developer");
            Crashlytics.logException(ex);
        }

        shareDialog.dismiss();

        if (PlayerConstants.QUEUE_STATE != PlayerConstants.QUEUE_STATE_STOPPED && songPlaying) {
            AppController.Instance().serviceResume(false);
        }
    }

    private void sendRingtone(
            final Context context,
            final QueueItem queueItem,
            final CharSequence title,
            final int start,
            final int end) {
        File mFile = new File(queueItem.data);
        String mExtension = FileHelper.getExtensionFromFilename(queueItem.data);

        final String outPath =
                FileHelper.makeRingtoneFilename(
                        mcontext,
                        title.length() == 0 ? mcontext.getString(R.string.au_clip) : title);

        final File deleteOldFile = new File(outPath);
        deleteOldFile.delete();

        mSoundFile = null;
        try {
            mSoundFile = CheapSoundFile.create(mFile.getAbsolutePath(), null);
        } catch (IOException e) {
            e.printStackTrace();
            Crashlytics.logException(e);
            AppController.toast(mcontext, "Unable to create ringtone from file");
            return;
        }

        mWaveformView.setSoundFile(mSoundFile);
        final int startFrame = mWaveformView.secondsToFrames((double) start);
        final int endFrame = mWaveformView.secondsToFrames((double) end);
        final int duration = (int) ((double) end - (double) start + 0.5);

        final File outFile = new File(outPath);
        try {

            // Write the new file
            mSoundFile.WriteFile(outFile, startFrame, endFrame - startFrame);

            // Try to load the new file to make sure it worked
            final CheapSoundFile.ProgressListener listener =
                    frac -> {
                        // Do nothing - we're not going to try to
                        // estimate when reloading a saved sound
                        // since it's usually fast, but hard to
                        // estimate anyway.
                        return true; // Keep going
                    };
            CheapSoundFile.create(outPath, listener);
        } catch (Exception ex) {
            Crashlytics.logException(ex);
            //					mProgressDialogMaterial.dismiss();
            CharSequence errorMessage;
            if (ex.getMessage().equals("No space left on device")) {
                errorMessage = context.getResources().getText(R.string.no_space_error);
                ex = null;
            } else {
                errorMessage = context.getResources().getText(R.string.write_error);
            }
            AppController.toast(context, errorMessage.toString());
            return;
        }

        progressDialog.dismiss();

        QueueItem tempQueueItem = new QueueItem();
        tempQueueItem.data = outPath;
        tempQueueItem.title = queueItem.title + " - ringtone";
        tempQueueItem.album_name = queueItem.album_name;
        tempQueueItem.artist_name = queueItem.artist_name;

        selectedItem = tempQueueItem;

        final ArrayList<MenuObject> subMenuItems = new ArrayList<>();
        subMenuItems.add(new MenuObject(MenuObject.SEND_CONTACTS));
        subMenuItems.add(new MenuObject(MenuObject.SEND_WIFI));

        AsyncJob.doOnMainThread(
                () -> {
                    MaterialMenuAdapter MaterialMenuAdapter =
                            new MaterialMenuAdapter(subMenuItems, onSubMenuObjectItemSelected);

                    new MaterialDialog.Builder(mcontext)
                            .adapter(MaterialMenuAdapter, new LinearLayoutManager(mcontext))
                            .show();
                });
    }

    private class PreviewTimerTask extends TimerTask {
        public void run() {

            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying()) {
                    int progress = 0;
                    int position = mediaPlayer.getCurrentPosition();
                    if (position > end) {
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
