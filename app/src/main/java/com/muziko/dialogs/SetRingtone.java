package com.muziko.dialogs;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.arasthel.asyncjob.AsyncJob;
import com.crashlytics.android.Crashlytics;
import com.muziko.MyApplication;
import com.muziko.R;
import com.muziko.common.models.QueueItem;
import com.muziko.controls.RangeBar.RangeBar;
import com.muziko.cutter.ringtone_lib.WaveformView;
import com.muziko.cutter.ringtone_lib.soundfile.CheapSoundFile;
import com.muziko.manager.AppController;
import com.muziko.mediaplayer.MuzikoExoPlayer;
import com.muziko.mediaplayer.PlayerConstants;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/** Created by dev on 13/09/2016. */
public class SetRingtone {
    private Context mcontext;
    private WaveformView mWaveformView;
    private CheapSoundFile mSoundFile;
    private Timer timer = null;
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

        View view =
                LayoutInflater.from(mcontext).inflate(R.layout.dialog_set_ringtone, null, false);
        mWaveformView = view.findViewById(R.id.waveform);
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
                            if (Double.parseDouble(rangebar.getRightPinValue())
                                            - Double.parseDouble(rangebar.getLeftPinValue())
                                    < 10) {
                                AppController.toast(context, "Ringtone must be at least 10 seconds");
                            } else {

                                timerStop();
                                mediaPlayer.stop();
                                mediaPlayer.release();
                                mediaPlyerReleased = true;
                                dialog.dismiss();
                                new MaterialDialog.Builder(context)
                                        .theme(Theme.LIGHT)
                                        .titleColorRes(R.color.normal_blue)
                                        .negativeColorRes(R.color.dialog_negetive_button)
                                        .title("Save Ringtone")
                                        .positiveText("SAVE")
                                        .negativeText("CANCEL")
                                        .inputType(InputType.TYPE_CLASS_TEXT)
                                        .input(
                                                "Ringtone Name",
                                                "",
                                                (dialog1, input) -> {
                                                    // Do something
                                                    if (input.toString().length() > 0) {

                                                        saveRingtone(
                                                                context,
                                                                queue,
                                                                input,
                                                                rangebar.getLeftIndex(),
                                                                rangebar.getRightIndex());

                                                    } else {
                                                        AppController.toast(context, "Enter Something!");
                                                    }
                                                })
                                        .show();
                            }
                        })
                .neutralText("Cancel")
                .onNeutral((dialog, which) -> dialog.dismiss())
                .dismissListener(
                        dialog -> {
                            timerStop();
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

    private void saveRingtone(
            final Context context,
            final QueueItem queueItem,
            final CharSequence title,
            final int start,
            final int end) {
        File mFile = new File(queueItem.data);
        String mExtension = getExtensionFromFilename(queueItem.data);

        final String outPath = makeRingtoneFilename(title);

        if (outPath == null) {

            AppController.toast(context, context.getString(R.string.no_unique_filename));
            return;
        }

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

        //		createProgressDialog(context);

        new Thread() {
            public void run() {
                Handler mHandler = new Handler(context.getMainLooper());

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

                Runnable runnable =
                        () ->
                                afterSavingRingtone(
                                        context, title, outPath, outFile, duration * 1000);
                mHandler.post(runnable);
            }
        }.start();
    }

    private void afterSavingRingtone(
            Context context, CharSequence title, String outPath, File outFile, int duration) {

        long length = outFile.length();
        if (length <= 512) {
            outFile.delete();
            new AlertDialog.Builder(context)
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

        String artist = "" + context.getResources().getText(R.string.artist_name);

        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DATA, outPath);
        values.put(MediaStore.MediaColumns.TITLE, title.toString());
        values.put(MediaStore.MediaColumns.SIZE, fileSize);
        values.put(MediaStore.MediaColumns.MIME_TYPE, mimeType);

        values.put(MediaStore.Audio.Media.ARTIST, artist);
        values.put(MediaStore.Audio.Media.DURATION, duration);

        values.put(MediaStore.Audio.Media.IS_RINGTONE, true);
        values.put(MediaStore.Audio.Media.IS_NOTIFICATION, true);
        values.put(MediaStore.Audio.Media.IS_ALARM, true);
        values.put(MediaStore.Audio.Media.IS_MUSIC, false);

        // Insert it into the database
        Uri uri = MediaStore.Audio.Media.getContentUriForPath(outPath);
        final Uri newUri = context.getContentResolver().insert(uri, values);

        // If Ringdroid was launched to get content, just return

        // There's nothing more to do with music or an alarm.  Show a
        // success message and then quit.

        try {
            RingtoneManager.setActualDefaultRingtoneUri(
                    MyApplication.getInstance().getApplicationContext(),
                    RingtoneManager.TYPE_RINGTONE,
                    newUri);
        } catch (Throwable t) {
            AppController.toast(context, context.getString(R.string.set_ringtone_failed));
            Crashlytics.logException(t);
        }
        AppController.toast(context, context.getResources().getString(R.string.save_success_message));

        if (PlayerConstants.QUEUE_STATE != PlayerConstants.QUEUE_STATE_STOPPED && songPlaying) {
            AppController.Instance().serviceResume(false);
        }
    }

    private String getExtensionFromFilename(String filename) {
        try {
            return filename.substring(filename.lastIndexOf('.'), filename.length());
        } catch (Exception e) {
            return "mp3";
        }
    }

    private String makeRingtoneFilename(CharSequence title) {

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
        return parentdir + "/" + title + "." + "mp3";
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
