package com.muziko.api.LastFM;

import android.content.Context;
import android.net.NetworkInfo;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.muziko.api.LastFM.models.Album;
import com.muziko.api.LastFM.models.AlbumSearch;
import com.muziko.api.LastFM.models.ArtistSearch;
import com.muziko.api.LastFM.models.ArtistforArtist;
import com.muziko.api.LastFM.models.Image;
import com.muziko.api.LastFM.models.ImageforArtist;
import com.muziko.common.models.QueueItem;
import com.muziko.database.ArtistImageRealm;
import com.muziko.manager.MuzikoConstants;

import io.realm.Realm;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.muziko.MyApplication.networkState;

/**
 * Created by dev on 9/07/2016.
 */
class LastFMHelper {

	private static String TAG = LastFMHelper.class.getSimpleName();
	private static String artistName;
	private static String albumName;

	public static String SearchArtist(final Context context, final QueueItem queueItem) {


		try
		{

            if (networkState != NetworkInfo.State.CONNECTED) {

				return null;
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

			if (queueItem.data == null || queueItem.data.isEmpty()) {
				if (queueItem.title != null && !queueItem.title.isEmpty()) {

					Realm myRealm = Realm.getDefaultInstance();
					ArtistImageRealm artistImageRealmResult = myRealm.where(ArtistImageRealm.class)
							.equalTo("artistName", queueItem.title)
							.findFirst();

					if (artistImageRealmResult == null) {
						artistName = queueItem.title;
						artistName = artistName.replaceAll("\\(.*?\\) ?", "");
						Call<ArtistSearch> call = lastFMApi.SearchArtistImage(artistName);
						call.enqueue(new Callback<ArtistSearch>() {
							@Override
							public void onResponse(Call<ArtistSearch> call, Response<ArtistSearch> response) {
								Log.v(TAG, "Got response from Last.FM for " + artistName);


								int statusCode = response.code();
								ArtistSearch artistSearch = response.body();
								if (artistSearch != null) {
									if (artistSearch.getResults().getArtistmatches().getArtist().size() > 0) {
										ArtistforArtist artist = artistSearch.getResults().getArtistmatches().getArtist().get(0);
										if (artist != null) {
											for (ImageforArtist image : artist.getImage()) {
												if (image.getSize().equals("medium")) {

													if (image.getText() != null && !image.getText().isEmpty()) {
														queueItem.data = image.getText();

//														MyApplication.artists.put(queueItem.title, queueItem);
														Realm myRealm = Realm.getDefaultInstance();
														myRealm.beginTransaction();
														ArtistImageRealm artistImageRealm = new ArtistImageRealm();
														artistImageRealm.setArtistName(queueItem.title);
														artistImageRealm.setUrl(queueItem.data);
														artistImageRealm.setUpdated(System.currentTimeMillis());
														myRealm.copyToRealmOrUpdate(artistImageRealm);
														myRealm.commitTransaction();


														myRealm.close();
													}
												}
											}
										}
									}
								}

							}

							@Override
							public void onFailure(Call<ArtistSearch> call, Throwable t) {
								// Log error here since request failed
								Log.v(TAG, "No response from Last.FM for " + artistName);
							}
						});
					} else {
						queueItem.data = artistImageRealmResult.getUrl();

//						MyApplication.artists.put(queueItem.title, queueItem);
					}
				}
			}
			return queueItem.data;
		} catch (Exception ex) {
			Crashlytics.logException(ex);
			return null;
		}
	}

	public static String SearchCoverArt(Context context, final QueueItem queueItem) {

		final String[] result = {null};

		try
		{

            if (networkState != NetworkInfo.State.CONNECTED) {

				return null;
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

				artistName = queueItem.artist_name;
				albumName = queueItem.album_name;

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
								if (image.getSize().equals("medium")) {

									result[0] = image.getText();

								}
							}
						}
					}

					@Override
					public void onFailure(Call<AlbumSearch> call, Throwable t) {
						// Log error here since request failed
						Log.v(TAG, "No response from Last.FM for " + artistName + " & " + albumName);
					}
				});

			}
			return result[0];

		} catch (Exception ex) {
			Crashlytics.logException(ex);
			return null;
		}
	}
}
