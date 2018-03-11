package com.muziko.salut;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.Base64;
import android.util.Log;

import com.bluelinelabs.logansquare.LoganSquare;
import com.muziko.MyApplication;
import com.muziko.common.models.QueueItem;
import com.muziko.common.models.firebase.ShareSalut;
import com.muziko.database.TrackRealmHelper;
import com.muziko.manager.AppController;
import com.muziko.manager.NotificationController;
import com.muziko.receivers.NotificationBroadcast;
import com.muziko.salut.Callbacks.SalutUploadCallback;

import org.apache.commons.io.FileUtils;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Random;
import java.util.UUID;

import static android.R.attr.data;

/**
 * Created by dev on 27/10/2016.
 */

class SalutUploader extends AsyncTask<Void, Integer, Boolean> {

	private final Context mContext;
	private final int BUFFER_SIZE = 65536;
	private final Salut salutInstance;
	private final SalutUploadCallback mSalutUploadCallback;
	private final SalutDevice device;
	private ShareSalut shareSalut;
	private byte[] buffer;
    private android.app.NotificationManager mNotifyManager;
    private NotificationCompat.Builder mBuilder;
    private int nID;
	private int totalBytes;
	private int bufferSize;
	private String mData;
	private File mFile;
	private PowerManager.WakeLock mWakeLock;
	private int lastSentProgress = 0;

	public SalutUploader(Context ctx, SalutDevice device, Salut salutInstance, String data, SalutUploadCallback salutUploadCallback) {
		mContext = ctx;
		mData = data;
		this.device = device;
		this.salutInstance = salutInstance;
		mSalutUploadCallback = salutUploadCallback;
	}

	@Override
	protected Boolean doInBackground(Void... params) {

		boolean result = false;

		mFile = new File(mData);
		shareSalut = getShareData();

		Log.d(Salut.TAG, "\nAttempting to send data to a device.");
		Socket dataSocket = new Socket();

		try {
			dataSocket.connect(new InetSocketAddress(device.serviceAddress, device.servicePort));
			dataSocket.setReceiveBufferSize(BUFFER_SIZE);
			dataSocket.setSendBufferSize(BUFFER_SIZE);

			//If this code is reached, a client has connected and transferred data.
			Log.d(Salut.TAG, "Connected, transferring data...");
//			BufferedOutputStream dataStreamToOtherDevice = new BufferedOutputStream(dataSocket.getOutputStream());

			String dataToSend = LoganSquare.serialize(shareSalut);
			byte[] mybytearray = dataToSend.getBytes();

			InputStream byteArrayInputStream = new ByteArrayInputStream(mybytearray);
			DataInputStream dataInputStream = new DataInputStream(byteArrayInputStream);
			DataOutputStream dataOutputStream = new DataOutputStream(dataSocket.getOutputStream());
//			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

			totalBytes = mybytearray.length;
			bufferSize = 8 * 1024;
			buffer = new byte[bufferSize];

			int progress = 0;
			int bytesRead = 0;
			dataOutputStream.writeLong(totalBytes);

			while ((bytesRead = dataInputStream.read(buffer)) > -1) {
				dataOutputStream.write(buffer, 0, bytesRead);
				progress += bytesRead;
				this.publishProgress((progress * 100) / totalBytes);
			}
			dataOutputStream.flush();
			dataOutputStream.close();

			dataInputStream.close();
			byteArrayInputStream.close();

			Log.d(Salut.TAG, "Successfully sent data.");

		} catch (Exception ex) {
			Log.d(Salut.TAG, "An error occurred while sending data to a device.");
			result = false;
			ex.printStackTrace();
		} finally {
			result = true;
			try {
				dataSocket.close();
			} catch (Exception ex) {
				Log.e(Salut.TAG, "Failed to close data socket.");
			}

		}
		return result;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        // take CPU lock to prevent CPU from going off if the user
        // presses the power button during download
        PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                getClass().getName());
        mWakeLock.acquire();

        int min = 0;
        int max = 1000;

        Random r = new Random();
        nID = r.nextInt(max - min + 1) + min;

        mNotifyManager =
                (android.app.NotificationManager)
                        mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(mContext, "Muziko");
        mBuilder.setContentTitle("Sharing file")
                .setContentText("Upload in progress - Swipe to cancel")
                .setSmallIcon(NotificationController.Instance().getUploadNotificationIcon());

        mBuilder.setProgress(100, 0, false);
        mBuilder.setDeleteIntent(getDeleteIntent());
        mNotifyManager.notify(nID, mBuilder.build());

	}

	@Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);

		mWakeLock.release();

		String combined = device.deviceName + data;

		MyApplication.shareUploaderList.remove(combined);

		if (result) {
			mBuilder.setContentText("Upload complete");
			// Removes the progress bar
			mBuilder.setProgress(0, 0, false);
			mNotifyManager.notify(nID, mBuilder.build());
			mNotifyManager.cancel(nID);
			mNotifyManager.cancelAll();

			if (mSalutUploadCallback != null) {
				mSalutUploadCallback.onUploadSuccess("File shared sucessfully!");
			}
		} else {
			mBuilder.setContentText("Upload Error");
			// Removes the progress bar
			mBuilder.setProgress(0, 0, false);
			mNotifyManager.notify(nID, mBuilder.build());
			mNotifyManager.cancel(nID);

			Intent progressIntent = new Intent(AppController.INTENT_DOWNLOAD_PROGRESS);
			progressIntent.putExtra("url", device.deviceName);
			progressIntent.putExtra("progress", -1);
			mContext.sendBroadcast(progressIntent);

			if (mSalutUploadCallback != null) {
				mSalutUploadCallback.onUploadFailure("Problem sharing file");
			}
		}

	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		int progress = values[0];

		if (lastSentProgress + 3 < progress) {
			lastSentProgress = progress;

			mBuilder.setProgress(100, progress, false);
			mNotifyManager.notify(nID, mBuilder.build());

			if (progress > 97) progress = 100;

			Intent progressIntent = new Intent(AppController.INTENT_DOWNLOAD_PROGRESS);
			progressIntent.putExtra("url", device.deviceName);
			progressIntent.putExtra("progress", progress);
			mContext.sendBroadcast(progressIntent);
		}
	}

	private ShareSalut getShareData() {
		byte bytes[] = new byte[0];
		final String uuid = UUID.randomUUID().toString();
		String base64 = "";
		try {
			int flags = Base64.NO_WRAP | Base64.URL_SAFE;
			bytes = FileUtils.readFileToByteArray(mFile);
			base64 = Base64.encodeToString(bytes, flags);
		} catch (IOException e) {

			e.printStackTrace();
		}
		final String filename = mFile.getName();
		final QueueItem queueItem = TrackRealmHelper.getTrack(mData);
		shareSalut = new ShareSalut();
		shareSalut.uid = uuid;
		shareSalut.shareData = base64;
		shareSalut.filename = filename;
		shareSalut.title = queueItem.title;
		shareSalut.artist = queueItem.artist_name;
		shareSalut.album = queueItem.album_name;
		return shareSalut;

	}

	private PendingIntent getDeleteIntent() {
		Intent intent = new Intent(mContext, NotificationBroadcast.class);
		intent.setAction(AppController.NOTIFY_CANCEL_WIFI_UPLOAD);

		String combined = device.deviceName + data;

		intent.putExtra("data", combined);
		return PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
	}
}
