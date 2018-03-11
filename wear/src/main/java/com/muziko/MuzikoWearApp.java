package com.muziko;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;

import com.crashlytics.android.Crashlytics;
import com.muziko.common.controls.MuzikoArrayList;
import com.muziko.services.MuzikoWearService;
import com.squareup.picasso.LruCache;
import com.squareup.picasso.Picasso;

import io.fabric.sdk.android.BuildConfig;
import io.fabric.sdk.android.Fabric;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Created by Bradley on 9/03/2017.
 */

public class MuzikoWearApp extends Application {

    public static RealmConfiguration realmConfiguration;
    public static String versionName;
    public static String wearAction = "";
    public static int wearPosition = 0;
    private static MuzikoWearApp instance;

    public static MuzikoWearApp getInstance() {
        return instance;
    }

    public static void serviceUpdateCache(Context context, String data) {
//        Intent intent = new Intent(context, SongService.class);
//        intent.setAction(MyApplication.ACTION_UPDATE_CACHE);
//        intent.putExtra(SongService.ARG_DATA, data);
//        context.startService(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;


        PlayerConstants.QUEUE_LIST = new MuzikoArrayList<>();

//		if (BuildConfig.DEBUG) {
//			StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
//					.detectAll()
//					.penaltyLog()
//					.build());
//			StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
//					.detectLeakedSqlLiteObjects()
//					.detectLeakedClosableObjects()
//					.penaltyLog()
////					.penaltyDeath()
//					.build());
//		}

//		if (LeakCanary.isInAnalyzerProcess(this)) {
//			// This process is dedicated to LeakCanary for heap analysis.
//			// You should not init your app in this process.
//			return;
//		}
//		LeakCanary.install(this);

        Realm.init(this);
        realmConfiguration = new RealmConfiguration.Builder()
                .name(Realm.DEFAULT_REALM_NAME)
                .schemaVersion(0)
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(realmConfiguration);

        versionName = BuildConfig.VERSION_NAME;

        Picasso.Builder picassoBuilder = new Picasso.Builder(this).memoryCache(new LruCache(15 * 1024 * 1024)).defaultBitmapConfig(Bitmap.Config.RGB_565).indicatorsEnabled(false);
        Picasso picasso = picassoBuilder.build();
        try {
            Picasso.setSingletonInstance(picasso);
        } catch (IllegalStateException e) {
            Crashlytics.logException(e);
        }

        Fabric.with(this, new Crashlytics());

        startService(new Intent(getBaseContext(), MuzikoWearService.class));

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Roboto-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

    }

}
