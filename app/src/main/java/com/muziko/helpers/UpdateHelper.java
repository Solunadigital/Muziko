package com.muziko.helpers;

import android.app.Activity;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.Uri;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.muziko.BuildConfig;
import com.muziko.MyApplication;
import com.muziko.R;
import com.muziko.api.Updater.UpdaterAPI;
import com.muziko.common.models.UpdateModel;
import com.muziko.manager.OkHttpManager;
import com.muziko.manager.PrefsManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.muziko.MyApplication.networkState;

/**
 * Created by Bradley on 12/05/2017.
 */

public class UpdateHelper {

    public void checkForUpdate(Activity context) {

        if (!PrefsManager.Instance().getDontShowUpdates() && networkState == NetworkInfo.State.CONNECTED) {

            Gson gson = new GsonBuilder()
                    .setLenient()
                    .create();

            Retrofit baseRetrofit = new Retrofit.Builder()
                    .baseUrl(BuildConfig.updaterAPI)
                    .client(OkHttpManager.Instance().getOkHttpClient())
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();

            UpdaterAPI updaterAPI = baseRetrofit.create(UpdaterAPI.class);

            Call<UpdateModel> updateModelCall = updaterAPI.getVersion();

            updateModelCall.enqueue(new Callback<UpdateModel>() {
                @Override
                public void onResponse(Call<UpdateModel> call, retrofit2.Response<UpdateModel> rawResponse) {
                    try {

                        if (rawResponse.isSuccessful()) {

                            String currentVersion = MyApplication.versionName;
                            String newVersion = rawResponse.body().getVersion();

                            if (!MyApplication.versionName.equals(rawResponse.body().getVersion())) {

                                MaterialDialog updateDialog = new MaterialDialog.Builder(context).theme(Theme.LIGHT).titleColorRes(R.color.normal_blue).title("Update Available")
                                        .customView(R.layout.dialog_update, false)
                                        .positiveText("Update Now").onPositive((dialog, which) -> {

                                            final String appPackageName = context.getPackageName();
                                            try {
                                                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                                            } catch (android.content.ActivityNotFoundException anfe) {
                                                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + appPackageName)));
                                            }
                                        }).neutralText("Don't show again").onNeutral((dialog, which) -> {
                                            PrefsManager.Instance().setDontShowUpdates(true);
                                        }).negativeText("Maybe Later").build();

                                View dialogView = updateDialog.getView();
                                TextView versionText = dialogView.findViewById(R.id.versionText);
                                TextView dateText = dialogView.findViewById(R.id.dateText);
                                TextView notesText = dialogView.findViewById(R.id.notesText);
                                versionText.setText(rawResponse.body().getVersion());
                                dateText.setText(rawResponse.body().getDate());
                                notesText.setText(rawResponse.body().getNotes());
                                updateDialog.show();

                            }
                        }

                    } catch (Exception e) {
                        Crashlytics.logException(e);

                    }
                }

                @Override
                public void onFailure(Call<UpdateModel> call, Throwable throwable) {
                    Crashlytics.logException(throwable);
                }
            });

        }
    }
}
