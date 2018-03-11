package com.muziko.manager;

import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.muziko.cloud.OneDrive.OneDriveRestApi;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.muziko.manager.MuzikoConstants.oneDriveApiUrl;

/**
 * Created by Bradley on 16/05/2017.
 */

public class OkHttpManager {

    private static OkHttpManager instance;
    private OkHttpClient okHttpClient;
    private OneDriveRestApi oneDriveRestApi;

    //no outer class can initialize this class's object
    private OkHttpManager() {
    }

    public static OkHttpManager Instance() {
        //if no instance is initialized yet then create new instance
        //else return stored instance
        if (instance == null) {
            instance = new OkHttpManager();
        }
        return instance;
    }

    public OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }

    public OneDriveRestApi getOneDriveRestApi() {
        return oneDriveRestApi;
    }

    public void init() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        okHttpClient = new OkHttpClient();
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.networkInterceptors().add(new StethoInterceptor());
        builder.connectTimeout(10, TimeUnit.SECONDS);
        builder.readTimeout(10, TimeUnit.SECONDS);
        builder.writeTimeout(10, TimeUnit.SECONDS);
        okHttpClient = builder.build();

        Retrofit awanRetrofit = new Retrofit.Builder()
                .baseUrl(oneDriveApiUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        oneDriveRestApi = awanRetrofit.create(OneDriveRestApi.class);
    }
}
