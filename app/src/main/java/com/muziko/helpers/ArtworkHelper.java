package com.muziko.helpers;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.NetworkInfo;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.muziko.MyApplication;
import com.muziko.R;
import com.muziko.activities.MainActivity;
import com.muziko.api.AcrCloud.TrackModel;
import com.muziko.api.LastFM.lastFMApi;
import com.muziko.api.LastFM.models.Album;
import com.muziko.api.LastFM.models.AlbumSearch;
import com.muziko.api.LastFM.models.Image;
import com.muziko.common.models.QueueItem;
import com.muziko.database.AlbumArtRealmHelper;
import com.muziko.database.TrackRealmHelper;
import com.muziko.manager.AppController;
import com.muziko.manager.GsonManager;
import com.muziko.manager.ImageManager;
import com.muziko.manager.MuzikoConstants;
import com.muziko.manager.SettingsManager;
import com.muziko.mediaplayer.PlayerConstants;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.cmc.music.metadata.ImageData;
import org.cmc.music.metadata.MusicMetadata;
import org.cmc.music.metadata.MusicMetadataSet;
import org.cmc.music.myid3.MyID3;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Vector;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.muziko.MyApplication.IMAGE_LARGE_SIZE;
import static com.muziko.MyApplication.hasWifi;
import static com.muziko.MyApplication.networkState;
import static com.muziko.database.TrackRealmHelper.getTracksForAlbum;

/**
 * Created by dev on 8/08/2016.
 */
public class ArtworkHelper {

    private final String TAG = ArtworkHelper.class.getSimpleName();
    private QueueItem queueItem;
    private ArtworkTaskListener mArtworkTaskListener;
    private ACRArtworkTaskListener acrArtworkTaskListener;
    private Context mContext;
    private Target uploadTarget = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            // loading of the bitmap was a success
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, MyApplication.JPEG_COMPRESS, stream);
            byte[] byteArray = stream.toByteArray();

            setArt(mContext, queueItem, byteArray);
            if (mArtworkTaskListener != null) {
                mArtworkTaskListener.onSuccess();
            }
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

    public void autoPickAlbumArtTrackModel(final Context context, final TrackModel trackModel, ACRArtworkTaskListener acrArtworkTaskListener) {

        this.acrArtworkTaskListener = acrArtworkTaskListener;

        if (networkState != NetworkInfo.State.CONNECTED) {
            return;
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

        if (trackModel.artist_name != null && !trackModel.artist_name.isEmpty() && trackModel.album_name != null && !trackModel.album_name.isEmpty()) {

            final String artistName = trackModel.artist_name;
            final String albumName = trackModel.album_name;

            Call<AlbumSearch> call = lastFMApi.SearchCoverArt(artistName, albumName);
            call.enqueue(new Callback<AlbumSearch>() {
                @Override
                public void onResponse(Call<AlbumSearch> call, Response<AlbumSearch> response) {
                    Log.v(TAG, "Got response from Last.FM for " + artistName + " & " + albumName);

                    int statusCode = response.code();
                    AlbumSearch albumSearch = response.body();
                    Album album = albumSearch.getAlbum();
                    if (album != null) {
                        String coverUrl = "";
                        for (Image image : album.getImage()) {
//                            if (image.getSize().equals("mega")) {
//                                if (!image.getText().isEmpty()) {
//                                    coverUrl = image.getText();
//                                }
//                            } else

                            switch (image.getSize()) {
                                case "extralarge":
                                    if (!image.getText().isEmpty()) {
                                        coverUrl = image.getText();
                                    }
                                    break;
                                case "large":
                                    if (!image.getText().isEmpty()) {
                                        coverUrl = image.getText();
                                    }
                                    break;
                                case "medium":
                                    if (!image.getText().isEmpty()) {
                                        coverUrl = image.getText();
                                    }
                                    break;
                                case "small":
                                    if (!image.getText().isEmpty()) {
                                        coverUrl = image.getText();
                                    }
                                    break;
                            }
                        }


                        if (!coverUrl.isEmpty()) {
                            trackModel.setCoverUrl(coverUrl);
                            if (acrArtworkTaskListener != null) {
                                acrArtworkTaskListener.onCoverArtDownloadSuccess(trackModel);
                            }
                        } else {
                            if (acrArtworkTaskListener != null) {
                                acrArtworkTaskListener.onCoverArtDownloadError(trackModel);
                            }
                        }
                    } else {
                        if (acrArtworkTaskListener != null) {
                            acrArtworkTaskListener.onCoverArtDownloadError(trackModel);
                        }
                    }
                }

                @Override
                public void onFailure(Call<AlbumSearch> call, Throwable t) {
                    if (acrArtworkTaskListener != null) {
                        acrArtworkTaskListener.onCoverArtDownloadError(trackModel);
                    }
                }
            });

        }
    }


    public void autoPickAlbumArtTask(final Context context, final QueueItem queueItem, ArtworkTaskListener ArtworkTaskListener) {
        mContext = context;
        this.queueItem = queueItem;
        mArtworkTaskListener = ArtworkTaskListener;

        boolean prefArtworkDownloadWifi = SettingsManager.Instance().getPrefs().getBoolean("prefArtworkDownloadWifi", false);

        if (prefArtworkDownloadWifi) {
            if (!hasWifi) {
                return;
            }
        } else {
            if (networkState != NetworkInfo.State.CONNECTED) {
                AppController.toast(context, "No network connection");
                return;
            }
        }

        if (AlbumArtRealmHelper.getStatus(queueItem.artist_name, queueItem.album_name)) {
            return;
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
                    Log.v(TAG, "Got response from Last.FM for " + artistName + " & " + albumName);

                    int statusCode = response.code();
                    AlbumSearch albumSearch = response.body();
                    Album album = albumSearch.getAlbum();
                    if (album != null) {
                        for (Image image : album.getImage()) {
                            if (image.getSize().equals("large")) {
                                if (!image.getText().isEmpty()) {


                                    Picasso.with(mContext)
                                            .load(image.getText())
                                            .resize(IMAGE_LARGE_SIZE, IMAGE_LARGE_SIZE)
                                            .into(uploadTarget);

                                } else {
                                    AlbumArtRealmHelper.updateStatus(queueItem.artist_name, queueItem.album_name);
                                    if (mArtworkTaskListener != null) {
                                        mArtworkTaskListener.onError();
                                    }
                                }
                            }
                        }
                    } else {
                        AlbumArtRealmHelper.updateStatus(queueItem.artist_name, queueItem.album_name);
                        if (mArtworkTaskListener != null) {
                            mArtworkTaskListener.onError();
                        }
                    }
                }

                @Override
                public void onFailure(Call<AlbumSearch> call, Throwable t) {
                    // Log error here since request failed
                    AlbumArtRealmHelper.updateStatus(queueItem.artist_name, queueItem.album_name);
                    if (mArtworkTaskListener != null) {
                        mArtworkTaskListener.onError();
                    }
                }
            });

        }
    }

    public void autoPickAlbumArt(final Context context, final QueueItem queueItem, final boolean auto) {

        boolean prefArtworkDownloadWifi = SettingsManager.Instance().getPrefs().getBoolean("prefArtworkDownloadWifi", false);

        if (prefArtworkDownloadWifi) {
            if (!hasWifi) {
                return;
            }
        } else {
            if (networkState != NetworkInfo.State.CONNECTED) {
                AppController.toast(context, "No network connection");
                return;
            }
        }

        if (AlbumArtRealmHelper.getStatus(queueItem.artist_name, queueItem.album_name)) {
            return;
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
                    Log.v(TAG, "Got response from Last.FM for " + artistName + " & " + albumName);

                    int statusCode = response.code();
                    AlbumSearch albumSearch = response.body();
                    Album album = albumSearch.getAlbum();
                    if (album != null) {
                        for (Image image : album.getImage()) {
                            if (image.getSize().equals("large")) {
                                if (!image.getText().isEmpty()) {

                                    Picasso.with(context)
                                            .load(image.getText())
                                            .resize(IMAGE_LARGE_SIZE, IMAGE_LARGE_SIZE)
                                            .into(new com.squareup.picasso.Target() {
                                                @Override
                                                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {

                                                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                                    bitmap.compress(Bitmap.CompressFormat.JPEG, MyApplication.JPEG_COMPRESS, stream);
                                                    byte[] byteArray = stream.toByteArray();

                                                    setArt(context, queueItem, byteArray);
                                                }

                                                @Override
                                                public void onBitmapFailed(Drawable errorDrawable) {
                                                    if (!auto) {
                                                        AppController.toast(context, "Album Art not found");
                                                    } else {
                                                        AlbumArtRealmHelper.updateStatus(queueItem.artist_name, queueItem.album_name);
                                                    }
                                                }

                                                @Override
                                                public void onPrepareLoad(Drawable placeHolderDrawable) {

                                                }
                                            });

                                } else {
                                    if (!auto) {
                                        AppController.toast(context, "Album Art not found");
                                    } else {
                                        AlbumArtRealmHelper.updateStatus(queueItem.artist_name, queueItem.album_name);
                                    }
                                    return;
                                }
                            }
                        }
                    } else {
                        if (!auto) {
                            AppController.toast(context, "Album Art not found");
                        } else {
                            AlbumArtRealmHelper.updateStatus(queueItem.artist_name, queueItem.album_name);
                        }
                    }
                }

                @Override
                public void onFailure(Call<AlbumSearch> call, Throwable t) {
                    // Log error here since request failed
                    Log.v(TAG, "No response from Last.FM for " + artistName + " & " + albumName);
                    if (!auto) {
                        AppController.toast(context, "Album Art not found");
                    } else {
                        AlbumArtRealmHelper.updateStatus(queueItem.artist_name, queueItem.album_name);
                    }
                }
            });

        }
    }

    public byte[] pickAlbumArt(final Context context, final QueueItem queueItem) {


        MyID3 id3 = new MyID3();
        MusicMetadataSet dataset = null;
        MusicMetadata metadata = null;

        try {
            File from = new File(queueItem.data);
            dataset = id3.read(from);      //read metadata
        } catch (Exception e) {
            Crashlytics.logException(e);
        }

        if (dataset == null) {
            AppController.toast(context, "Unable to read song metadata!");
            return null;
        }

        metadata = (MusicMetadata) dataset.getSimplified();
        if (metadata == null) {
            AppController.toast(context, "Unable to read song tags!");
            return null;
        }

        if (metadata != null) {

            Vector converart = metadata.getPictureList();

            if (converart.size() > 0) {
                byte[] bitmapData = ((ImageData) converart.get(0)).imageData;

                Bitmap bm = Utils.decodeBitmapArray(bitmapData, MyApplication.IMAGE_SMALL_SIZE, MyApplication.IMAGE_SMALL_SIZE);
                if (bm != null) {

                    return bitmapData;

                } else {
                    AppController.toast(context, "Album Art not found");
                    return null;
                }
            }
        }

        AppController.toast(context, "Album Art not found");
        return null;
    }

    public void setArt(Context context, QueueItem queueItem, byte[] bitmapData) {

        for (QueueItem queueItemAlbumid : TrackRealmHelper.getTrackAlbumIdsForAlbum(queueItem.album_name).values()) {
            MediaStoreHelper.updateCoverArt(context, bitmapData, queueItemAlbumid);
        }

        MediaStoreHelper.updateCoverArt(context, bitmapData, queueItem);
        TrackRealmHelper.updateCoverArtforAlbum(queueItem, false);

        ArrayList<QueueItem> coverartList = new ArrayList<>();
        coverartList.addAll(getTracksForAlbum(queueItem.album_name));

        for (QueueItem queueItem1 : coverartList) {
            // Update queue list
            for (QueueItem queue : PlayerConstants.QUEUE_LIST) {
                if (queueItem1.data.equals(queue.data)) {
                    queue.setTags(queueItem1);
                }
            }

            // Update queue song
            if (PlayerConstants.QUEUE_SONG.data.equals(queueItem1.data)) {
                PlayerConstants.QUEUE_SONG = queueItem1;
            }

        }

        ImageManager.Instance().invalidateImage(queueItem);

        AppController.toast(context, context.getString(R.string.album_art_updated));

        context.sendBroadcast(new Intent(AppController.INTENT_RECENT_CHANGED));
        context.sendBroadcast(new Intent(AppController.INTENT_QUEUE_CHANGED));
        context.sendBroadcast(new Intent(AppController.INTENT_TRACK_CHANGED));
        context.sendBroadcast(new Intent(AppController.INTENT_TRACK_CHANGED_HOME));

        Intent trackEditIntent = new Intent(AppController.INTENT_TRACK_EDITED);
        trackEditIntent.putExtra("tag", MainActivity.class.toString());
        trackEditIntent.putExtra("index", -1);
        trackEditIntent.putExtra("item", queueItem);
        context.sendBroadcast(trackEditIntent);

        ArrayList<QueueItem> queueItems = new ArrayList<>();
        queueItems.addAll(getTracksForAlbum(queueItem.title));
        String tracks = GsonManager.Instance().getGson().toJson(queueItems);
        AppController.Instance().serviceUpdateCache(tracks);
    }

    public void removeArt(Context context, QueueItem queueItem) {

        for (QueueItem queueItemAlbumid : TrackRealmHelper.getTrackAlbumIdsForAlbum(queueItem.album_name).values()) {
            MediaStoreHelper.removeCoverArt(context, queueItemAlbumid.album);
        }

        TrackRealmHelper.updateCoverArtforAlbum(queueItem, true);

        ArrayList<QueueItem> coverartList = new ArrayList<>();
        coverartList.addAll(getTracksForAlbum(queueItem.album_name));

        for (QueueItem queueItem1 : coverartList) {
            // Update queue list
            for (QueueItem queue : PlayerConstants.QUEUE_LIST) {
                if (queueItem1.data.equals(queue.data)) {
                    queue.setTags(queueItem1);
                }
            }

            // Update queue song
            if (PlayerConstants.QUEUE_SONG.data.equals(queueItem1.data)) {
                PlayerConstants.QUEUE_SONG = queueItem1;
            }

        }

        AppController.toast(context, context.getString(R.string.album_art_cleared));

        context.sendBroadcast(new Intent(AppController.INTENT_RECENT_CHANGED));
        context.sendBroadcast(new Intent(AppController.INTENT_QUEUE_CHANGED));
        context.sendBroadcast(new Intent(AppController.INTENT_TRACK_CHANGED));
        context.sendBroadcast(new Intent(AppController.INTENT_TRACK_CHANGED_HOME));

        Intent trackEditIntent = new Intent(AppController.INTENT_TRACK_EDITED);
        trackEditIntent.putExtra("tag", MainActivity.class.toString());
        trackEditIntent.putExtra("index", -1);
        trackEditIntent.putExtra("item", queueItem);
        context.sendBroadcast(trackEditIntent);


        ArrayList<QueueItem> queueItems = new ArrayList<>();
        queueItems.addAll(getTracksForAlbum(queueItem.title));
        String tracks = GsonManager.Instance().getGson().toJson(queueItems);
        AppController.Instance().serviceUpdateCache(tracks);

    }

    public void loadMissingAlbumArt(ArrayList<QueueItem> queueItems) {
        Target loadtarget = null;

        for (QueueItem queueItem : queueItems) {
            if (loadtarget == null) loadtarget = new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    // do something with the Bitmap
//					handleLoadedBitmap(bitmap);
                }

                @Override
                public void onBitmapFailed(Drawable errorDrawable) {

                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {

                }

            };
        }
    }

    public interface ArtworkTaskListener {

        void onSuccess(String url);

        void onSuccess();

        void onError();
    }

    public interface ACRArtworkTaskListener {

        void onCoverArtDownloadSuccess(TrackModel trackModel);

        void onCoverArtDownloadError(TrackModel trackModel);
    }
}
