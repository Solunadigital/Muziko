package com.muziko.helpers;


import com.muziko.manager.OkHttpManager;

import java.io.IOException;
import java.net.URL;

import okhttp3.Request;
import okhttp3.Response;

public class NetworkHelper {

    public static String USER_AGENT =
            "Mozilla/5.0 (Linux; U; Android 6.0.1; ko-kr; Build/IML74K) AppleWebkit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30";

    public static String getUrlAsString(String paramURL) throws IOException {
        return getUrlAsString(new URL(paramURL));
    }

    public static String getUrlAsString(URL paramURL) throws IOException {
        Request request = new Request.Builder().header("User-Agent", USER_AGENT).url(paramURL).build();
        Response response = OkHttpManager.Instance().getOkHttpClient().newCall(request).execute();

        return response.body().string();
    }
}