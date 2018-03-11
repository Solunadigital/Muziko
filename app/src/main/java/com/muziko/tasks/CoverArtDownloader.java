package com.muziko.tasks;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.muziko.MyApplication;
import com.muziko.R;
import com.muziko.api.LastFM.lastFMApi;
import com.muziko.api.LastFM.models.Album;
import com.muziko.api.LastFM.models.AlbumSearch;
import com.muziko.api.LastFM.models.Image;
import com.muziko.common.events.RefreshEvent;
import com.muziko.common.models.QueueItem;
import com.muziko.database.TrackRealmHelper;
import com.muziko.helpers.ArtworkHelper;
import com.muziko.manager.AppController;
import com.muziko.manager.GsonManager;
import com.muziko.manager.MuzikoConstants;
import com.muziko.manager.NotificationController;
import com.muziko.receivers.NotificationBroadcast;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.greenrobot.eventbus.EventBus;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.muziko.MyApplication.IMAGE_LARGE_SIZE;
import static com.muziko.MyApplication.hasWifi;
import static com.muziko.MyApplication.networkState;

public class CoverArtDownloader extends AsyncTask<Void, Integer, Void> {

    private final Context mContext;
    private final ArrayList<QueueItem> fullAlbumList = new ArrayList<>();
    private final ArrayList<QueueItem> albumList = new ArrayList<>();
    private android.app.NotificationManager mNotifyManager;
    private NotificationCompat.Builder mBuilder;
    private int nID;
    private int total = 0;
    private int count = 0;
    private int mProgress = 0;
    private int successCount = 0;
    private QueueItem queueItem;
    private Target coverArtTarget = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            // loading of the bitmap was a success
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, MyApplication.JPEG_COMPRESS, stream);
            byte[] byteArray = stream.toByteArray();

            ArtworkHelper artworkHelper = new ArtworkHelper();
            artworkHelper.setArt(mContext, queueItem, byteArray);
            success();
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
            // loading of the bitmap failed
            // TODO do some action/warning/error message
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }
    };

    public CoverArtDownloader(Context ctx) {
        mContext = ctx;
    }

    @Override
    protected Void doInBackground(Void... params) {

        fullAlbumList.clear();
        fullAlbumList.addAll(TrackRealmHelper.getAlbums().values());
        Bitmap bitmap = null;
        for (final QueueItem queueItem : fullAlbumList) {

            bitmap = null;
            final QueueItem queue = TrackRealmHelper.getTrackByAlbum(queueItem.album);
//				if (AlbumArtRealmHelper.getStatus(queue.artist_name, queue.album_name)) {
//					continue;
//				}
            Uri uri = Uri.parse("content://media/external/audio/albumart/" + queue.album);
            try {
                bitmap = MediaStore.Images.Media.getBitmap(MyApplication.getInstance().getApplicationContext().getContentResolver(), uri);
            } catch (Exception e) {
                albumList.add(queue);

            }

            if (bitmap == null) {
                albumList.add(queue);
            } else {
                TrackRealmHelper.updateCoverArtforAlbum(queue, false);
            }

        }

        if (albumList.size() > 0) {
            total = albumList.size();

            for (final QueueItem queueItem : albumList) {

                autoPickAlbumArtTask(mContext, queueItem);
            }
        } else {
            mBuilder.setContentText("Download complete");
            // Removes the progress bar
            mBuilder.setProgress(0, 0, false);
            mNotifyManager.notify(nID, mBuilder.build());
            mNotifyManager.cancelAll();
        }

        ArrayList<QueueItem> queueItems = new ArrayList<>();
        queueItems.addAll(albumList);
        String tracks = GsonManager.Instance().getGson().toJson(queueItems);
        AppController.Instance().serviceUpdateCache(tracks);

        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        int min = 0;
        int max = 1000;

        Random r = new Random();
        nID = r.nextInt(max - min + 1) + min;

        mNotifyManager =
                (android.app.NotificationManager)
                        mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(mContext, "Muziko");
        mBuilder.setContentTitle("Getting missing cover art")
                .setContentText("Download in progress - Swipe to cancel")
                .setSmallIcon(NotificationController.Instance().getDownloadNotificationIcon());

        mBuilder.setProgress(100, 0, false);
        mBuilder.setDeleteIntent(getDeleteIntent());
        mNotifyManager.notify(nID, mBuilder.build());

    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);

    }

    private void autoPickAlbumArtTask(final Context context, final QueueItem queueItem) {
        this.queueItem = queueItem;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean prefArtworkDownloadWifi = prefs.getBoolean("prefArtworkDownloadWifi", false);

        if (prefArtworkDownloadWifi) {
            if (!hasWifi) {
                return;
            }
        } else {
            if (networkState != NetworkInfo.State.CONNECTED) {
                return;
            }
        }

        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(MuzikoConstants.lastfm_url)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        lastFMApi lastFMApi =
                retrofit.create(lastFMApi.class);

        if (queueItem.artist_name != null && !queueItem.artist_name.isEmpty() && queueItem.album_name != null && !queueItem.album_name.isEmpty()) {

            final String artistName = queueItem.artist_name;
            final String albumName = queueItem.album_name;

            Call<AlbumSearch> call = lastFMApi.SearchCoverArt(artistName, albumName);
            call.enqueue(new Callback<AlbumSearch>() {
                @Override
                public void onResponse(Call<AlbumSearch> call, Response<AlbumSearch> response) {

                    int statusCode = response.code();
                    AlbumSearch albumSearch = response.body();
                    Album album = albumSearch.getAlbum();
                    if (album != null) {

                        for (Image image : album.getImage()) {
                            if (image.getSize().equals("large")) {
                                if (!image.getText().isEmpty()) {

                                    Picasso.with(MyApplication.getInstance().getApplicationContext())
                                            .load(image.getText())
                                            .resize(IMAGE_LARGE_SIZE, IMAGE_LARGE_SIZE)
                                            .into(coverArtTarget);

                                } else {
                                    error();
                                }
                            } else {
                                error();
                            }
                        }

                    } else {
                        error();
                    }
                }

                @Override
                public void onFailure(Call<AlbumSearch> call, Throwable t) {
                    // Log error here since request failed
                    error();
                }
            });

        } else {
            error();
        }
    }

    private PendingIntent getDeleteIntent() {
        Intent intent = new Intent(mContext, NotificationBroadcast.class);
        intent.setAction(MyApplication.NOTIFY_CANCEL_COVERART_DOWNLOAD);
        return PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }


    private void success() {
        count++;
        successCount++;
        updateProgress();
    }

    private void error() {
        count++;
        updateProgress();
    }

    private void updateProgress() {
        if (count < total) {

            double progress = (100.0 * count / total);
            mProgress = (int) progress;
            mBuilder.setContentText(String.format(mContext.getString(R.string.coverart_dl_progress), count, total));
            mBuilder.setProgress(100, mProgress, false);
            mNotifyManager.notify(nID, mBuilder.build());
        } else {
            String text = mContext.getResources()
                    .getQuantityString(R.plurals.lyrics_dl_finished_desc, successCount, successCount);
            mBuilder.setContentText(text);
            mBuilder.setProgress(0, 0, false);
            mNotifyManager.notify(nID, mBuilder.build());
            mNotifyManager.cancel(nID);
            EventBus.getDefault().post(new RefreshEvent(1000));
        }
    }
}
