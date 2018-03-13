package com.muziko.tasks;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.muziko.MyApplication;
import com.muziko.common.models.QueueItem;
import com.muziko.database.TrackRealmHelper;
import com.muziko.helpers.Utils;
import com.muziko.manager.AppController;
import com.muziko.tageditor.metadata.ImageData;
import com.muziko.tageditor.metadata.MusicMetadata;
import com.muziko.tageditor.metadata.MusicMetadataSet;
import com.muziko.tageditor.myid3.MyID3;
import com.squareup.picasso.Picasso;


import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Vector;

public class ThumbLoader extends AsyncTask<Void, String[], Boolean> {
	private static final String TAG = ThumbLoader.class.getSimpleName();

	private static HashMap<String, String> hashMap = new HashMap<>();
	private ThumbLoaderListener listener;
	private Context ctx;

	public ThumbLoader(Context ctx, ThumbLoaderListener listener) {
		this.ctx = ctx;
		this.listener = listener;
	}

	public static void store(Context context, QueueItem queueItem, byte[] bitmapData) {
		if (queueItem.data == null || queueItem.data.length() == 0) return;

		String thumb = path(context, queueItem.data);

		File file = new File(thumb);

		try {
			if (bitmapData == null) {
				if (hashMap.containsKey(queueItem.data))
					hashMap.remove(queueItem.data);

				file.delete();

				TrackRealmHelper.updateCoverArt(queueItem, true);

				Picasso.with(context).invalidate("file://" + thumb);

			} else {
				file.delete();
				Picasso.with(context).invalidate("file://" + thumb);

				Bitmap bitmap = Utils.decodeBitmapArray(bitmapData, MyApplication.IMAGE_SIZE, MyApplication.IMAGE_SIZE);
				//Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapData, 0, bitmapData.length);
				if (bitmap != null) {
					FileOutputStream fos = new FileOutputStream(file);
					bitmap.compress(Bitmap.CompressFormat.JPEG, MyApplication.JPEG_COMPRESS, fos);
					fos.close();

					TrackRealmHelper.updateCoverArt(queueItem, false);
				} else {
					TrackRealmHelper.updateCoverArt(queueItem, true);
				}
			}
		} catch (Exception ex) {
			Crashlytics.logException(ex);
		}
	}

	private static String path(Context context, String data) {
		String hash = "";
		if (hashMap.containsKey(data)) {
			hash = hashMap.get(data);
		} else {
			hash = Utils.getSHA1(data);
			hashMap.put(data, hash);
		}

		return (new File(context.getCacheDir(), hash)).getAbsolutePath();
	}

	@Override
	protected Boolean doInBackground(Void... params) {
		int count = 0;

		Log.e(TAG, "thumb start");

		ArrayList<QueueItem> items = new ArrayList<>(TrackRealmHelper.getTracks(0).values());

		MyApplication.imageTotal = items.size();

		Collections.sort(items, (s1, s2) -> s1.title.compareToIgnoreCase(s2.title));

		for (QueueItem track : items) {
			if (!Long.valueOf(track.dateModified).equals(track.coverUpdated)) {

				File cache = new File(path(ctx, track.data));
//				if (cache.exists()) continue;

				File from = new File(track.data);

				//if(from.length() >= 15*1024*1024) continue;

				if (read(from, cache, track)) {
					MyApplication.imageCount++;
					count++;
				}

			}

		}

		items.clear();

		Log.e(TAG, "thumb done");
		return count > 0;
	}

    @Override
    protected void onPostExecute(Boolean s) {
        if (listener != null) {
            listener.onThumbLoaded();
        }

        MyApplication.imageCache = true;

        ctx.sendBroadcast(new Intent(AppController.INTENT_TRACK_EDITED));
        ctx.sendBroadcast(new Intent(AppController.INTENT_TRACK_EDITED_HOME));

        ctx.sendBroadcast(new Intent(AppController.INTENT_RECENT_CHANGED));

        ctx.sendBroadcast(new Intent(AppController.INTENT_TRACK_CHANGED));
        ctx.sendBroadcast(new Intent(AppController.INTENT_TRACK_CHANGED_HOME));

        super.onPostExecute(s);
    }

	private boolean read(File from, File cache, final QueueItem track) {
		boolean ret = false;
		try {

			String key = track.artist_name + track.title;
			final String hashCode = Utils.md5(key);

			MusicMetadataSet dataSet = new MyID3().read(from);      //read metadata
			if (dataSet != null) {
				MusicMetadata metadata = (MusicMetadata) dataSet.getSimplified();
				if (metadata != null) {
					Vector pictures = metadata.getPictureList();
					if (pictures.size() > 0) {
						try {
							byte[] bitmapData = ((ImageData) pictures.get(0)).imageData;

							if (cache.exists())
								cache.delete();

							Picasso.with(MyApplication.getInstance().getApplicationContext()).invalidate("file://" + cache.getAbsolutePath());

							//Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapData, 0, bitmapData.length);
							Bitmap bitmap = Utils.decodeBitmapArray(bitmapData, MyApplication.IMAGE_SIZE, MyApplication.IMAGE_SIZE);
							if (bitmap != null) {
								FileOutputStream fos = new FileOutputStream(cache);
								bitmap.compress(Bitmap.CompressFormat.PNG, MyApplication.JPEG_COMPRESS, fos);
								TrackRealmHelper.updateCoverArt(track, false);
								ret = true;
							} else {
								TrackRealmHelper.updateCoverArt(track, true);
							}
						} catch (Exception ex) {
							Crashlytics.logException(ex);
						}
					} else {
						TrackRealmHelper.updateCoverArt(track, true);
					}
				} else {
					TrackRealmHelper.updateCoverArt(track, true);
				}
			} else {
				TrackRealmHelper.updateCoverArt(track, true);
			}
		} catch (Exception ex) {
			Crashlytics.logException(ex);
		}

		return ret;
	}

	public interface ThumbLoaderListener {
		void onThumbLoaded();
	}
}
