package com.muziko.api.YouTube;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

/**
 * Created by Bradley on 22/03/2017.
 */

public interface YouTubeAPI {

    @GET()
    Call<YouTubeDetails> getVideoDetails(@Url String url);
}
