package com.muziko.manager;

import com.muziko.BuildConfig;

/**
 * Created by dev on 7/07/2016.
 */
public class MuzikoConstants {

    public static final String muzikoCloud = "https://muziko-48de4.appspot.com/";
    public static final String googlecustomSearch = "https://www.googleapis.com/customsearch/v1";
    public static final String youtubeAPI = "https://www.googleapis.com/youtube/v3/videos";
    public static final String lastfm_url = "http://ws.audioscrobbler.com/";
    public static final String googlePlayURL = "https://play.google.com/store/apps/details?id=com.muziko&referrer=";
    public static final String oneDriveApiUrl = "https://apis.live.net/v5.0/";
    public static final int invitesRequired = 50;
    public static final int RC_SIGN_IN = 9001;
    public static final int REQUEST_REGISTER_USER = 665;
    public static final int REQUEST_REGISTER_USER_TRACKS = 666;
    public static final int INSERT_CONTACT_REQUEST = 2;
    public final static int REQUEST_CODE_STORAGE_ACCESS = 890;
    public final static int REQUEST_RESOLVE_ERROR = 1111;
    public final static int CODE_WRITE_SETTINGS_PERMISSION = 9001;
    public final static int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124;
    public final static int ARTWORK_PICK_FROM_INTERNET = 7100;
    public final static int ARTWORK_PICK_FROM_GALLERY = 7200;
    public final static int ARTWORK_PICK_FROM_ALBUM_FOLDER = 7300;
    public final static int ARTWORK_PICK_FROM_ID3_TAGS = 7400;
    public final static String ERROR_CODE_KEY = "errorCode";
    public static final int FIREBASE_READY_INTERVAL = BuildConfig.DEBUG ? 60000 : 4000;
    public static final int SCAN_DOWNLOADS_INTERVAL = BuildConfig.DEBUG ? 60000 : 30000;
    public static final int REFRESH_CLOUD_DRIVES_INTERVAL = BuildConfig.DEBUG ? 60000 : 30000;
    public static final int PROGRESS_UPDATE_INTERVAL = 350;
    public static final int UPLOAD_SETTLE_DELAY = 3000;
    public static final String LANGUAGE_ENGLISH = "LANGUAGE_ENGLISH";
    public static final String LANGUAGE_ARABIC = "LANGUAGE_ARABIC";
    public static final String LANGUAGE_THAI = "LANGUAGE_THAI";
    public static final String LANGUAGE_TURKISH = "LANGUAGE_TURKISH";
    public static final String STORAGE_SUBSCRIPTION_LEVEL_FREE = "storage_subscription_level_free";
    public static final String STORAGE_SUBSCRIPTION_LEVEL_ONE = "storage_subscription_level_1";
    public static final String STORAGE_SUBSCRIPTION_LEVEL_TWO = "storage_subscription_level_2";
    public static final String STORAGE_SUBSCRIPTION_LEVEL_THREE = "storage_subscription_level_3";
    public static final String STORAGE_SUBSCRIPTION_LEVEL_UNLIMITED = "storage_subscription_level_unlimited";
    public static final String PRODUCT_ID = "muziko.ads";
    public static final String UNKNOWN_TITLE = "Unknown";
    public static long addCloudDelay = 3000;
    public static long boxCachePercent = 20;
    public static long boxCacheBytes = 2000000;
    public static long weekMilliseconds = 604800000;
    public static long dayMilliseconds = 86399999;
    public static long halfDayMilliseconds = 43200000;
    public static long halfHourMilliseconds = 1800000;
    public static String[] extensions = {
            "mp3", "midi", "wav", "aac", "amr"
    };
    public static String[] audioContentTypes = {
            "audio/mp3", "audio/mpeg", "audio/x-mpegurl"
    };

    public enum FallNotesState {START, PAUSE, RUNNING, STOP}

    public enum CloudFileActions {UPLOAD, DOWNLOAD, DELETE, RETRY, CONTINUE}

    public enum FirebaseFileMode {
        LIBRARY,
        FAVS,
        PLAYLISTS
    }
}
