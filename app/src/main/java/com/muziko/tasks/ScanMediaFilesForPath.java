package com.muziko.tasks;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.badoo.mobile.util.WeakHandler;
import com.crashlytics.android.Crashlytics;
import com.muziko.R;
import com.muziko.common.models.QueueItem;
import com.muziko.database.TrackRealmHelper;
import com.muziko.helpers.StorageHelper;
import com.muziko.manager.AppController;
import com.muziko.manager.MediaHelper;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.muziko.manager.MuzikoConstants.extensions;

/**
 * Created by dev on 26/09/2016.
 */


public class ScanMediaFilesForPath extends AsyncTask<Void, String, Void> {

	private final Context context;
	private final List<File> mfiles = new ArrayList<>();
	private final String[] mExtensions;
	private boolean wasDismissed = false;
	private int countBefore = 0;
	private MaterialDialog scan;
	private TextView scantext;
	private String mPath;

	public ScanMediaFilesForPath(Context ctx, String path) {
		this.context = ctx;
		mPath = path;
		mExtensions = extensions;
	}

	@Override
	protected Void doInBackground(Void... params) {

		List<StorageHelper.StorageVolume> storageVolumes = StorageHelper.getStorages();

		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			try {


				mfiles.addAll(FileUtils.listFiles(new File(mPath), mExtensions, true));

				for (final File file : mfiles) {
					QueueItem existingTrack = TrackRealmHelper.getTrack(file.getAbsolutePath());
					if (existingTrack == null) {
						MediaHelper.Instance().loadMusicFromTrackAsync(file.getAbsolutePath(), false);
					}
					if (!wasDismissed) {
						publishProgress(file.getAbsolutePath());
					}

				}

			} catch (Exception e) {
				Crashlytics.logException(e);
			}
		}


		return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        countBefore = TrackRealmHelper.getCount();
        wasDismissed = false;


        scan = new MaterialDialog.Builder(context).theme(Theme.LIGHT).titleColorRes(R.color.normal_blue).negativeColorRes(R.color.dialog_negetive_button).positiveColorRes(R.color.normal_blue).title("Scan Media")
                .customView(R.layout.dialog_scan_media, false).positiveText("Background").onPositive((dialog, which) -> {
                    wasDismissed = true;
                    dialog.dismiss();
                }).build();

        View dialogView = scan.getView();
		CardView cardView = dialogView.findViewById(R.id.cardview);
		RelativeLayout scanlayout = cardView.findViewById(R.id.scanlayout);
		scantext = scanlayout.findViewById(R.id.scantext);
		scantext.setText(context.getString(R.string.starting_scan));
        scan.show();
    }

	@Override
	protected void onPostExecute(Void result) {
		super.onPostExecute(result);

		int countAfter = TrackRealmHelper.getCount();
		int diff = countAfter - countBefore;

		context.sendBroadcast(new Intent(AppController.INTENT_QUEUE_CHANGED));

		if (!wasDismissed) {
			scantext.setText("Scan complete. " + diff + String.format(" song%s added", diff != 1 ? "s" : ""));

			WeakHandler handler = new WeakHandler();
			handler.postDelayed(() -> scan.dismiss(), 3000);

		} else {
			AppController.toast(context, "Scan complete. " + diff + String.format(" song%s added", diff != 1 ? "s" : ""));
		}

	}

	@Override
	protected void onProgressUpdate(String... values) {

		scantext.setText("Scanning Media Library\n\n" + values[0]);

	}

}
