package com.muziko.tasks;

import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.widget.TextView;

import com.badoo.mobile.util.WeakHandler;
import com.crashlytics.android.Crashlytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.muziko.R;
import com.muziko.manager.AppController;
import com.muziko.manager.FirebaseManager;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by dev on 27/10/2016.
 */

public class ProfilePhotoUploader extends AsyncTask<Void, String, Void> {

	private final Context mContext;
	private final WeakHandler handler = new WeakHandler();
	private Bitmap mBitmap;
	private Uri mUri;
	private boolean wasDismissed = false;
	private int countBefore = 0;
	private TextView scantext;
	private NotificationManager mNotifyManager;
    private NotificationCompat.Builder mBuilder;
    private int nID;

	public ProfilePhotoUploader(Context ctx, Bitmap bitmap, Uri uri) {
		mContext = ctx;
		mBitmap = bitmap;
		mUri = uri;
	}

	@Override
	protected Void doInBackground(Void... params) {


		FirebaseStorage storageRef = FirebaseStorage.getInstance();
		StorageReference photoRef = storageRef.getReferenceFromUrl("gs://" + mContext.getString(R.string.google_storage_bucket));


		Long timestamp = System.currentTimeMillis();
		final StorageReference fullSizeRef = photoRef.child("avatars").child(mUri.getLastPathSegment() + ".jpg");


		ByteArrayOutputStream fullSizeStream = new ByteArrayOutputStream();
		mBitmap.compress(Bitmap.CompressFormat.JPEG, 90, fullSizeStream);
		byte[] bytes = fullSizeStream.toByteArray();
		UploadTask uploadTask = fullSizeRef.putBytes(bytes);

		uploadTask.addOnSuccessListener(taskSnapshot -> {

			mBuilder.setContentText("Upload complete");
			// Removes the progress bar
			mBuilder.setProgress(0, 0, false);
			mNotifyManager.notify(nID, mBuilder.build());
			handler.postDelayed(() -> mNotifyManager.cancelAll(), 2000);

			final Uri fullSizeUrl = taskSnapshot.getDownloadUrl();

			Map<String, Object> updateValues = new HashMap<>();
            updateValues.put("photoUrl", fullSizeUrl.toString());

            FirebaseManager.Instance().getPeopleRef().child(FirebaseAuth.getInstance().getCurrentUser().getUid()).updateChildren(
                    updateValues,
					(firebaseError, databaseReference) -> {
						if (firebaseError != null) {
                            AppController.toast(mContext, "Couldn't save user data: " + firebaseError.getMessage());
                        }
					});

		}).addOnFailureListener(Crashlytics::logException).addOnProgressListener(taskSnapshot -> {
			double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
			System.out.println("Upload is " + progress + "% done");
			int currentprogress = (int) progress;
			mBuilder.setProgress(100, currentprogress, false);
			mNotifyManager.notify(nID, mBuilder.build());
		}).addOnPausedListener(taskSnapshot -> System.out.println("Upload is paused"));

		return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        int min = 0;
        int max = 1000;

        Random r = new Random();
        nID = r.nextInt(max - min + 1) + min;

        mNotifyManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(mContext, "Muziko");
        mBuilder.setContentTitle("Uploading Avatar")
                .setContentText("Upload in progress")
                .setSmallIcon(R.drawable.ic_cloud_upload_black_24dp);

        mBuilder.setProgress(100, 0, false);
        mNotifyManager.notify(nID, mBuilder.build());
    }

	@Override
	protected void onPostExecute(Void result) {
		super.onPostExecute(result);

	}

}
