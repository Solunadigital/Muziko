package com.muziko.api.LastFM;

import com.muziko.BuildConfig;
import com.muziko.api.LastFM.models.AlbumSearch;
import com.muziko.api.LastFM.models.ArtistSearch;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by dev on 7/07/2016.
 */

public interface lastFMApi {

    @GET("/2.0/?method=artist.search&api_key=" + BuildConfig.lastFmApiKey + "&format=json")
    Call<ArtistSearch> SearchArtistImage(@Query("artist") String artist);

    @GET("/2.0/?method=album.getinfo&api_key=" + BuildConfig.lastFmApiKey + "&format=json")
    Call<AlbumSearch> SearchCoverArt(@Query("artist") String artist,
	                                 @Query("album") String album);

}


