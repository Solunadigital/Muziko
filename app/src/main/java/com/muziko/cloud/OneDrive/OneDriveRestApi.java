package com.muziko.cloud.OneDrive;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.QueryMap;

/**
 * Created by Bradley on 21/08/2017.
 */
public interface OneDriveRestApi {

    @GET("me")
    Call<UserData> getUserData(@QueryMap Map<String, String> options);
}
