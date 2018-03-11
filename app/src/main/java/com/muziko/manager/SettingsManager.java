package com.muziko.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;


import com.muziko.MyApplication;
import com.muziko.R;
import com.muziko.common.models.SettingModel;
import com.muziko.common.models.TabModel;
import com.muziko.database.TabRealmHelper;

import java.util.ArrayList;

/**
 * Created by dev on 27/08/2016.
 */
public class SettingsManager {

    public static final String prefShowArtwork = "prefShowArtwork";
    public static final String prefHideSongDuration = "prefHideSongDuration";
    public static final String prefHideUnknown = "prefHideUnknown";
    public static final String prefStoragePerms = "prefStoragePerms";
    public static final String prefArtworkDownload = "prefArtworkDownload";
    public static final String prefArtworkDownloadWifi = "prefArtworkDownloadWifi";
    public static final String prefLyricsDownload = "prefLyricsDownload";
    public static final String prefShake = "prefShake";
    public static final String prefShakeThreshold = "prefShakeThreshold";
    public static final String prefGapless = "prefGapless";
    public static final String prefScrobbling = "prefScrobbling";
    public static final String prefHeadset = "prefHeadset";
    public static final String prefBluetooth = "prefBluetooth";
    public static final String prefLockScreen = "prefLockScreen";
    public static final String prefLanguage = "prefLanguage";
    public static final String prefmanagetabs = "prefmanagetabs";
    public static final String prefStartScreen = "prefStartScreen";
    public static final String prefArtworkLock = "prefArtworkLock";
    public static final String prefFaceDown = "prefFaceDown";
    public static final String prefFaceUp = "prefFaceUp";
    public static final String prefDownloadCloudWhenStreaming = "prefDownloadCloudWhenStreaming";
    public static final String prefShowStreamDataWarning = "prefShowStreamDataWarning";
    public static final String prefAutoSyncLibrary = "prefAutoSyncLibrary";
    public static final String prefUpdateLibraryOnlyWifi = "prefUpdateLibraryOnlyWifi";
    public static final String prefSyncPlaylist = "prefSyncPlaylist";
    public static final String prefSyncFavourites = "prefSyncFavourites";
    public static final String prefSyncLocation = "prefSyncLocation";
    public static final String preflastfmusercred = "preflastfmusercred";
    public static final String preflastfmusercredclear = "preflastfmusercredclear";
    public static final String preflastfmstatus = "preflastfmstatus";
    public static final String preflastfmsignup = "preflastfmsignup";
    private static SettingsManager instance;
    private Context mContext;
    private SharedPreferences prefs;

    //no outer class can initialize this class's object
    private SettingsManager() {
    }

    public static SettingsManager Instance() {
        //if no instance is initialized yet then create new instance
        //else return stored instance
        if (instance == null) {
            instance = new SettingsManager();
        }
        return instance;
    }

    public boolean getPrefShowArtWork() {
        return prefs.getBoolean(prefShowArtwork, false);
    }

    public boolean getPrefAutoSyncLibrary() {
        return prefs.getBoolean(prefAutoSyncLibrary, false);
    }

    public boolean getPrefUpdateLibraryOnlyWifi() {
        return prefs.getBoolean(prefUpdateLibraryOnlyWifi, false);
    }

    public void setPrefUpdateLibraryOnlyWifi(boolean value) {
        prefs.edit().putBoolean(prefUpdateLibraryOnlyWifi, value).apply();
    }

    public boolean getPrefSyncPlaylist() {
        return prefs.getBoolean(prefSyncPlaylist, false);
    }

    public boolean getPrefSyncFavourites() {
        return prefs.getBoolean(prefSyncFavourites, false);
    }

    public boolean getPrefStoragePerms() {
        return prefs.getBoolean(prefStoragePerms, false);
    }

    public boolean getPrefShowStreamDataWarning() {
        return prefs.getBoolean(prefShowStreamDataWarning, false);
    }

    public void setPrefShowStreamDataWarning(boolean value) {
        prefs.edit().putBoolean(prefShowStreamDataWarning, value).apply();
    }

    public boolean getPrefDownloadCloudWhenStreaming() {
        return prefs.getBoolean(prefDownloadCloudWhenStreaming, false);
    }

    public void setPrefDownloadCloudWhenStreaming(boolean value) {
        prefs.edit().putBoolean(prefDownloadCloudWhenStreaming, value).apply();
    }

    public int getPrefSyncLocation() {
        return prefs.getInt(prefSyncLocation, 0);
    }

    public void setPrefSyncLocation(int value) {
        prefs.edit().putInt(prefSyncLocation, value).apply();
    }

    public SharedPreferences getPrefs() {
        return prefs;
    }

    public void init(Context context) {
        mContext = context;
        prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
    }

    public void createDefaultSettings() {

        // General
        prefs.edit().putInt(prefHideSongDuration, 0).apply();
        prefs.edit().putBoolean(prefHideUnknown, false).apply();
        prefs.edit().putBoolean(prefStoragePerms, false).apply();

        // Art Work
        prefs.edit().putBoolean(prefShowArtwork, true).apply();
        prefs.edit().putBoolean(prefArtworkDownload, false).apply();
        prefs.edit().putBoolean(prefArtworkDownloadWifi, false).apply();
        prefs.edit().putBoolean(prefArtworkLock, true).apply();

        //Lyrics
        prefs.edit().putBoolean(prefLyricsDownload, false).apply();

        // playback
        prefs.edit().putBoolean(prefShake, false).apply();
        prefs.edit().putInt(prefShakeThreshold, 1).apply();
        prefs.edit().putBoolean(prefGapless, false).apply();
        prefs.edit().putBoolean(prefScrobbling, false).apply();
        prefs.edit().putBoolean(prefHeadset, false).apply();
        prefs.edit().putBoolean(prefBluetooth, false).apply();
        prefs.edit().putBoolean(prefLockScreen, true).apply();

        // cloud
        prefs.edit().putBoolean(prefAutoSyncLibrary, true).apply();
        prefs.edit().putBoolean(prefSyncFavourites, true).apply();
        prefs.edit().putBoolean(prefSyncPlaylist, true).apply();
    }

    public ArrayList<SettingModel> getSettings() {
        ArrayList<SettingModel> items = new ArrayList<>();

        SettingModel settingModel = new SettingModel();
        settingModel.title = mContext.getString(R.string.change_language);
        settingModel.description = mContext.getString(R.string.change_language_desc);
        settingModel.key = prefLanguage;
        settingModel.type = 3;
        items.add(settingModel);

        settingModel = new SettingModel();
        settingModel.title = mContext.getString(R.string.manage_tabs);
        settingModel.description = mContext.getString(R.string.manage_tabs_desc);
        settingModel.key = prefmanagetabs;
        settingModel.type = 3;
        items.add(settingModel);

        settingModel = new SettingModel();
        settingModel.title = mContext.getString(R.string.choose_start_screen);
        settingModel.description = mContext.getString(R.string.choose_start_screen_desc);
        settingModel.key = prefStartScreen;
        settingModel.type = 3;
        items.add(settingModel);

        settingModel = new SettingModel();
        settingModel.title = mContext.getString(R.string.hide_songs_less);
        settingModel.description = mContext.getString(R.string.hide_songs_less_desc);
        settingModel.key = prefHideSongDuration;
        settingModel.type = 3;
        items.add(settingModel);

        settingModel = new SettingModel();
        settingModel.title = mContext.getString(R.string.hide_unknown);
        settingModel.description = mContext.getString(R.string.hide_unknown_desc);
        settingModel.key = prefHideUnknown;
        settingModel.type = 1;
        items.add(settingModel);

        settingModel = new SettingModel();
        settingModel.title = mContext.getString(R.string.download_missing_lyrics);
        settingModel.description = mContext.getString(R.string.download_missing_lyrics_desc);
        settingModel.key = prefLyricsDownload;
        settingModel.type = 1;
        items.add(settingModel);

//		settingModel = new SettingModel();
//		settingModel.title = Grant SD card write permission;
//		settingModel.description = Grant access to SD card for updating ID3 tags;
//		settingModel.key = prefStoragePerms;
//		settingModel.type = 1;
//		items.add(settingModel);

        settingModel = new SettingModel();
        settingModel.title = mContext.getString(R.string.display_artwork);
        settingModel.description = mContext.getString(R.string.display_artwork_desc);
        settingModel.key = prefShowArtwork;
        settingModel.type = 1;
        items.add(settingModel);

        settingModel = new SettingModel();
        settingModel.title = mContext.getString(R.string.download_artwork);
        settingModel.description = mContext.getString(R.string.down_artwork_desc);
        settingModel.key = prefArtworkDownload;
        settingModel.type = 1;
        items.add(settingModel);

        settingModel = new SettingModel();
        settingModel.title = mContext.getString(R.string.download_artwork_wifi);
        settingModel.description = mContext.getString(R.string.download_artwork_wifi_desc);
        settingModel.key = prefArtworkDownloadWifi;
        settingModel.type = 1;
        items.add(settingModel);

        settingModel = new SettingModel();
        settingModel.title = mContext.getString(R.string.display_album_cover);
        settingModel.description = mContext.getString(R.string.display_album_cover_desc);
        settingModel.key = prefArtworkLock;
        settingModel.type = 1;
        items.add(settingModel);

//		settingModel = new SettingModel();
//		settingModel.title = Shake action enabled;
//		settingModel.description = Enable shake action;
//		settingModel.key = prefShake;
//		settingModel.type = 1;
//		items.add(settingModel);
//
//		settingModel = new SettingModel();
//		settingModel.title = Shake sensitivity;
//		settingModel.description = Sensitivity of shake control;
//		settingModel.key = prefShakeThreshold;
//		settingModel.type = 2;
//		items.add(settingModel);

//		settingModel = new SettingModel();
//		settingModel.title = Gapless Playback;
//		settingModel.description = Play sequential tracks seamlessly;
//		settingModel.key = prefGapless;
//		settingModel.type = 1;
//		items.add(settingModel);

        settingModel = new SettingModel();
        settingModel.title = mContext.getString(R.string.enable_scrobbling);
        settingModel.description = mContext.getString(R.string.enable_scrobbling_desc);
        settingModel.key = prefScrobbling;
        settingModel.type = 1;
        items.add(settingModel);

        settingModel = new SettingModel();
        settingModel.title = mContext.getString(R.string.auto_resume);
        settingModel.description = mContext.getString(R.string.auto_resume_desc);
        settingModel.key = prefHeadset;
        settingModel.type = 1;
        items.add(settingModel);

        settingModel = new SettingModel();
        settingModel.title = mContext.getString(R.string.auto_resume_bluetooth);
        settingModel.description = mContext.getString(R.string.auto_resume_bluetooth_desc);
        settingModel.key = prefBluetooth;
        settingModel.type = 1;
        items.add(settingModel);

        settingModel = new SettingModel();
        settingModel.title = mContext.getString(R.string.enable_lock_screen);
        settingModel.description = mContext.getString(R.string.enable_lockscreen_desc);
        settingModel.key = prefLockScreen;
        settingModel.type = 1;
        items.add(settingModel);

        settingModel = new SettingModel();
        settingModel.title = mContext.getString(R.string.phone_face_down);
        settingModel.description = mContext.getString(R.string.phone_face_down_desc);
        settingModel.key = prefFaceDown;
        settingModel.type = 3;
        items.add(settingModel);

        settingModel = new SettingModel();
        settingModel.title = mContext.getString(R.string.phone_face_up);
        settingModel.description = mContext.getString(R.string.phone_face_up_desc);
        settingModel.key = prefFaceUp;
        settingModel.type = 3;
        items.add(settingModel);

        settingModel = new SettingModel();
        settingModel.title = mContext.getString(R.string.auto_download_streaming);
        settingModel.description = mContext.getString(R.string.auto_download_streaming_desc);
        settingModel.key = prefDownloadCloudWhenStreaming;
        settingModel.type = 1;
        items.add(settingModel);

        settingModel = new SettingModel();
        settingModel.title = mContext.getString(R.string.warning_stream_data);
        settingModel.description = mContext.getString(R.string.warning_stream_data_desc);
        settingModel.key = prefShowStreamDataWarning;
        settingModel.type = 1;
        items.add(settingModel);

        settingModel = new SettingModel();
        settingModel.title = mContext.getString(R.string.auto_download_library);
        settingModel.description = mContext.getString(R.string.auto_download_library_desc);
        settingModel.key = prefAutoSyncLibrary;
        settingModel.type = 1;
        items.add(settingModel);

//        settingModel = new SettingModel();
//        settingModel.title = mContext.getString(R.string.sync_playlists);
//        settingModel.description = mContext.getString(R.string.sync_playlists_desc);
//        settingModel.key = prefSyncPlaylist;
//        settingModel.type = 1;
//        items.add(settingModel);

        settingModel = new SettingModel();
        settingModel.title = mContext.getString(R.string.sync_favs);
        settingModel.description = mContext.getString(R.string.sync_favs_desc);
        settingModel.key = prefSyncFavourites;
        settingModel.type = 1;
        items.add(settingModel);

        settingModel = new SettingModel();
        settingModel.title = mContext.getString(R.string.update_library_wifi);
        settingModel.description = mContext.getString(R.string.update_library_wifi_desc);
        settingModel.key = prefUpdateLibraryOnlyWifi;
        settingModel.type = 1;
        items.add(settingModel);

        settingModel = new SettingModel();
        settingModel.title = mContext.getString(R.string.sync_location);
        settingModel.description = SettingsManager.Instance().getPrefSyncLocation() == 0 ? mContext.getString(R.string.internal_storage) : mContext.getString(R.string.sd_card);
        settingModel.key = prefSyncLocation;
        settingModel.type = 3;
        items.add(settingModel);
        return items;
    }


    public ArrayList<SettingModel> getLastFMSettings(boolean auth, String status) {
        ArrayList<SettingModel> items = new ArrayList<>();
        SettingModel settingModel = new SettingModel();
        if (auth) {
            settingModel = new SettingModel();
            settingModel.title = mContext.getString(R.string.clear_credentials);
            settingModel.description = mContext.getString(R.string.clear_credentials_desc);
            settingModel.key = preflastfmusercredclear;
            settingModel.type = 3;
            items.add(settingModel);

            settingModel = new SettingModel();
            settingModel.title = mContext.getString(R.string.scrobble_status);
            settingModel.description = mContext.getString(R.string.scrobble_status_desc);
            settingModel.key = preflastfmstatus;
            settingModel.type = 3;
            items.add(settingModel);
        } else {
            settingModel.title = mContext.getString(R.string.user_credentials);
            settingModel.description = mContext.getString(R.string.user_credentials_desc);
            settingModel.key = preflastfmusercred;
            settingModel.type = 3;
            items.add(settingModel);

            settingModel = new SettingModel();
            settingModel.title = mContext.getString(R.string.sign_up);
            settingModel.description = mContext.getString(R.string.sign_up_desc);
            settingModel.key = preflastfmsignup;
            settingModel.type = 3;
            items.add(settingModel);
        }

        return items;
    }

    public ArrayList<String> getStartScreens() {


        ArrayList<String> strings = new ArrayList<>();
        strings.add(0, MyApplication.LAST_OPENED);
        strings.add(1, MyApplication.TRACKS);
        strings.add(2, MyApplication.ALBUMS);
        strings.add(3, MyApplication.ARTISTS);
        strings.add(4, MyApplication.GENRES);
        strings.add(5, MyApplication.FOLDERS);

        ArrayList<TabModel> tabModels = TabRealmHelper.getAllTabs();
        for (TabModel tabModel : tabModels) {
            if (!tabModel.show) {
                strings.remove(tabModel.title);
            }
        }

        return strings;
    }
}
