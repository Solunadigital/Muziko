package com.muziko.receivers;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.support.v4.app.NotificationCompat;

import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.muziko.BuildConfig;
import com.muziko.MyApplication;
import com.muziko.R;
import com.muziko.api.Updater.UpdaterAPI;
import com.muziko.common.models.UpdateModel;
import com.muziko.manager.NotificationController;
import com.muziko.manager.OkHttpManager;
import com.muziko.manager.PrefsManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.muziko.MyApplication.networkState;
import static com.muziko.service.SongService.UPDATER_NOTIFY_DISMISS;
import static com.muziko.service.SongService.UPDATER_NOTIFY_DISMISS_ALWAYS;
import static com.muziko.service.SongService.UPDATER_NOTIFY_UPDATE;

/**
 * Created by dev on 7/07/2016.
 */
public class MuzikoUpdateReceiver extends BroadcastReceiver {

    private static final int nID = 401;
    private NotificationManager mNotifyManager;
    private NotificationCompat.Builder mBuilder;

    // Triggered by the Alarm periodically (starts the service to run task)
    @Override
    public void onReceive(Context context, Intent intent) {

        checkForUpdate(context);
    }

    private void checkForUpdate(Context context) {

        if (networkState == NetworkInfo.State.CONNECTED && !PrefsManager.Instance().getDontShowUpdates()) {

            Gson gson = new GsonBuilder().setLenient().create();

            Retrofit baseRetrofit =
                    new Retrofit.Builder()
                            .baseUrl(BuildConfig.updaterAPI)
                            .client(OkHttpManager.Instance().getOkHttpClient())
                            .addConverterFactory(GsonConverterFactory.create(gson))
                            .build();

            UpdaterAPI updaterAPI = baseRetrofit.create(UpdaterAPI.class);

            Call<UpdateModel> updateModelCall = updaterAPI.getVersion();

            updateModelCall.enqueue(
                    new Callback<UpdateModel>() {
                        @Override
                        public void onResponse(
                                Call<UpdateModel> call,
                                retrofit2.Response<UpdateModel> rawResponse) {
                            try {

                                if (rawResponse.isSuccessful()) {

                                    String currentVersion = MyApplication.versionName;
                                    String newVersion = rawResponse.body().getVersion();

                                    if (!MyApplication.versionName.equals(
                                            rawResponse.body().getVersion())) {

                                        mNotifyManager =
                                                (NotificationManager)
                                                        context.getSystemService(
                                                                Context.NOTIFICATION_SERVICE);
                                        mBuilder = new NotificationCompat.Builder(context, "Muziko");
                                        mBuilder.setContentTitle(
                                                context.getString(
                                                        R.string.update_available))
                                                .setContentText(
                                                        context.getString(R.string.new_version)
                                                                + newVersion
                                                                + " - "
                                                                + rawResponse.body().getDate())
                                                .setSmallIcon(
                                                        NotificationController.Instance().getUpdateNotificationIcon())
                                                .setDefaults(Notification.DEFAULT_LIGHTS)
                                                .setStyle(
                                                        new NotificationCompat.BigTextStyle()
                                                                .bigText(
                                                                        rawResponse
                                                                                .body()
                                                                                .getNotes()))
                                                .setContentIntent(getUpdateIntent(context, nID))
                                                .setAutoCancel(true)
                                                .addAction(
                                                        R.drawable.ic_cancel_black_24dp,
                                                        context.getString(R.string.dont_show_again),
                                                        getDismissAlwaysIntent(context, nID))
                                                .addAction(
                                                        R.drawable.ic_close_black_24dp,
                                                        context.getString(R.string.maybe_later),
                                                        getDismissIntent(context, nID))
                                                .addAction(
                                                        R.drawable.ic_close_black_24dp,
                                                        context.getString(R.string.update_now),
                                                        getUpdateIntent(context, nID));
                                        mNotifyManager.notify(nID, mBuilder.build());
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

    private PendingIntent getDismissIntent(Context context, int nID) {
        Intent intent = new Intent(context, NotificationBroadcast.class);
        intent.setAction(UPDATER_NOTIFY_DISMISS);
        intent.putExtra("id", nID);
        return PendingIntent.getBroadcast(context, 0, intent, nID);
    }

    private PendingIntent getDismissAlwaysIntent(Context context, int nID) {
        Intent intent = new Intent(context, NotificationBroadcast.class);
        intent.setAction(UPDATER_NOTIFY_DISMISS_ALWAYS);
        intent.putExtra("id", nID);
        return PendingIntent.getBroadcast(context, 0, intent, nID);
    }

    private PendingIntent getUpdateIntent(Context context, int nID) {
        Intent intent = new Intent(context, NotificationBroadcast.class);
        intent.setAction(UPDATER_NOTIFY_UPDATE);
        intent.putExtra("id", nID);
        return PendingIntent.getBroadcast(context, 0, intent, nID);
    }
}
