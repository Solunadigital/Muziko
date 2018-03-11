package com.muziko.helpers;

import android.content.Context;
import android.content.SharedPreferences;

import com.muziko.PlayerConstants;
import com.muziko.activities.MainActivity;


public class Prefs {

    public static void setSleepTimeLastSong(Context ctx, boolean listing) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        prefs.edit().putBoolean("sleep_time_last_song", listing).apply();
    }

    public static boolean getSleepTimeLastSong(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        return prefs.getBoolean("sleep_time_last_song", false);
    }

    public static void setQueueWidgetChange(Context ctx, boolean listing) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        prefs.edit().putBoolean("queue_widget_change", listing).apply();
    }

    public static boolean getQueueWidgetChange(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        return prefs.getBoolean("queue_widget_change", false);
    }

    public static void setStoragePermsURI(Context ctx, String uri) {

        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        prefs.edit().putString("storageperms", uri).apply();
    }

    public static String getStoragePermsURi(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        return prefs.getString("storageperms", "");
    }

    public static void setLastActivity(Context ctx, String lastActivity) {

        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        prefs.edit().putString("last_activity", lastActivity).apply();
    }

    public static String getLastActivity(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        return prefs.getString("last_activity", MainActivity.class.getName());
    }


    public static void setLastRecentActivityTab(Context ctx, int lastTab) {

        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        prefs.edit().putInt("last_recent_activity_tab", lastTab).apply();
    }

    public static int getLastRecentActivityTab(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        return prefs.getInt("last_recent_activity_tab", 0);
    }

    public static void setLastPlaylist(Context ctx, long playlist) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        prefs.edit().putLong("last_playlist", playlist).apply();
    }

    public static long getLastPlaylist(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        return prefs.getLong("last_playlist", 0);
    }

    public static void setLastPlaylistTitle(Context ctx, String playlist) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        prefs.edit().putString("last_playlist_title", playlist).apply();
    }

    public static String getLastPlaylistTitle(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        return prefs.getString("last_playlist_title", null);
    }

    // PlayerList
    public static void setLastPlayerListArt(Context ctx, long playlist) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        prefs.edit().putLong("last_playerlist_art", playlist).apply();
    }

    public static long getLastPlayerListArt(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        return prefs.getLong("last_playerlist_art", 0);
    }

    public static void setLastPlayerListType(Context ctx, int lastTab) {

        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        prefs.edit().putInt("last_playerlist_type", lastTab).apply();
    }

    public static int getLastPlayerListType(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        return prefs.getInt("last_playerlist_type", 0);
    }

    public static void setLastPlayerListName(Context ctx, String playlist) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        prefs.edit().putString("last_playerlist_name", playlist).apply();
    }

    public static String getLastPlayerListName(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        return prefs.getString("last_playerlist_name", null);
    }

    public static void setLastPlayerListData(Context ctx, String playlist) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        prefs.edit().putString("last_playerlist_data", playlist).apply();
    }

    public static String getLastPlayerListData(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        return prefs.getString("last_playerlist_data", null);
    }

    public static void setLastPlayerListDuration(Context ctx, int playlist) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        prefs.edit().putInt("last_playerlist_duration", playlist).apply();
    }

    public static int getLastPlayerListtDuration(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        return prefs.getInt("last_playerlist_duration", 0);
    }

    public static void setLastPlayerListSongs(Context ctx, int lastTab) {

        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        prefs.edit().putInt("last_playerlist_songs", lastTab).apply();
    }

    public static int getLastPlayerListSongs(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        return prefs.getInt("last_playerlist_songs", 0);
    }

    // Sorting

    public static void setTrackSortReverse(Context ctx, boolean state) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        prefs.edit().putBoolean("track_sort_reverse", state).apply();
    }

    public static boolean getTrackSortReverse(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        return prefs.getBoolean("track_sort_reverse", false);
    }

    public static void setArtistSort(Context ctx, int sort) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        prefs.edit().putInt("artist_sort", sort).apply();
    }

    public static boolean getArtistSortReverse(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        return prefs.getBoolean("artist_sort_reverse", false);
    }

    public static void setAlbumSortReverse(Context ctx, boolean state) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        prefs.edit().putBoolean("album_sort_reverse", state).apply();
    }

    public static boolean getAlbumSortReverse(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        return prefs.getBoolean("album_sort_reverse", false);
    }

    public static void setGenreSortReverse(Context ctx, boolean state) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        prefs.edit().putBoolean("genre_sort_reverse", state).apply();
    }

    public static boolean getGenreSortReverse(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        return prefs.getBoolean("genre_sort_reverse", false);
    }

    public static void setPlayerListTrackSortReverse(Context ctx, boolean state) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        prefs.edit().putBoolean("PLAYERLIST_track_sort_reverse", state).apply();
    }

    public static boolean getPlayerListTrackSortReverse(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        return prefs.getBoolean("PLAYERLIST_track_sort_reverse", false);
    }

    public static void setPlayerListAlbumSortReverse(Context ctx, boolean state) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        prefs.edit().putBoolean("PLAYERLIST_album_sort_reverse", state).apply();
    }

    public static boolean getPlayerListAlbumSortReverse(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        return prefs.getBoolean("PLAYERLIST_album_sort_reverse", false);
    }

    public static void setFolderTrackSortReverse(Context ctx, boolean state) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        prefs.edit().putBoolean("folder_track_sort_reverse", state).apply();
    }

    public static boolean getFolderTrackSortReverse(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        return prefs.getBoolean("folder_track_sort_reverse", false);
    }

    public static void setPlaylistSort(Context ctx, int sort) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        prefs.edit().putInt("playlist_sort", sort).apply();
    }

    public static int getPlaylistSort(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        return prefs.getInt("playlist_sort", 0);
    }

    public static void setPlaylistSortReverse(Context ctx, boolean state) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        prefs.edit().putBoolean("playlist_sort_reverse", state).apply();
    }

    public static boolean getPlaylistSortReverse(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        return prefs.getBoolean("playlist_sort_reverse", false);
    }

    public static void setTrashSortReverse(Context ctx, boolean state) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        prefs.edit().putBoolean("trash_sort_reverse", state).apply();
    }

    public static boolean getTrashSortReverse(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        return prefs.getBoolean("trash_sort_reverse", false);
    }

    public static void setArtistDB(Context ctx, String artist) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        prefs.edit().putString("artist", artist).apply();
    }

    public static String getArtistDB(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        return prefs.getString("artist", null);
    }

    public static void setAlbumDB(Context ctx, String album) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        prefs.edit().putString("album", album).apply();
    }

    public static String getAlbumDB(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        return prefs.getString("album", null);
    }

    public static void setFolderDB(Context ctx, String folder) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        prefs.edit().putString("folder", folder).apply();
    }

    public static String getFolderDB(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        return prefs.getString("folder", null);
    }

    public static void setTracksDB(Context ctx, String tracks) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        prefs.edit().putString("tracks", tracks).apply();
    }

    public static String getTracksDB(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        return prefs.getString("tracks", null);
    }

    public static void setGenreDB(Context ctx, String genre) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        prefs.edit().putString("genre", genre).apply();
    }

    public static String getGenreDB(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        return prefs.getString("genre", null);
    }

    public static void setDatabaseReady(Context ctx, boolean state) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        prefs.edit().putBoolean("DB", state).apply();
    }

    public static boolean getDatabaseReady(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        return prefs.getBoolean("DB", false);
    }

    //	public static void setHasSD(Context ctx, Boolean hasSD) {
    //		SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(), Context.MODE_PRIVATE);
    //		prefs.edit().putBoolean("hasSD", hasSD).apply();
    //	}
    //
    //	public static boolean getHasSD(Context ctx) {
    //		SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(), Context.MODE_PRIVATE);
    //		return prefs.getBoolean("hasSD", false);
    //	}

    public static void setEqualizer(Context ctx, boolean state) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        prefs.edit().putBoolean("EQ", state).apply();
    }

    public static boolean getEqualizer(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        return prefs.getBoolean("EQ", false);
    }

    public static void setEqualizerPreset(Context ctx, int order) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        prefs.edit().putInt("EQPRESET", order).apply();
    }

    public static int getEqualizerPreset(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        return prefs.getInt("EQPRESET", 0);
    }

    public static void setQueueLevel(Context ctx, long order) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        prefs.edit().putLong("QUEUE", order).apply();
    }

    public static long getQueueLevel(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        return prefs.getLong("QUEUE", 0);
    }

    public static void setPlayPosition(Context ctx, int musicTime) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        prefs.edit().putInt("MUSIC_TIME", musicTime).apply();
    }

    public static int getPlayPosition(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        return prefs.getInt("MUSIC_TIME", 0);
    }

    public static int getPlayRepeat(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        return prefs.getInt("REPEAT", PlayerConstants.REPEAT_ALL);
    }

    public static void setPlayRepeat(Context ctx, int loopCount) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        prefs.edit().putInt("REPEAT", loopCount).apply();
        //		MyApplication.setNextMediaPlayer(ctx);
    }

    public static void setPlayShuffle(Context ctx, boolean enableShuffle) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        prefs.edit().putBoolean("SHUFFLE", enableShuffle).apply();
        //		MyApplication.setNextMediaPlayer(ctx);
    }

    public static boolean getPlayShuffle(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        return prefs.getBoolean("SHUFFLE", false);
    }

    public static void setRateShowNumber(Context ctx, int rateCount) {

        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        prefs.edit().putInt("RATE_COUNT", rateCount).apply();
    }

    public static int getRateShowNumber(Context ctx) {

        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);

        return prefs.getInt("RATE_COUNT", 0);
    }

    public static void setRateShowDone(Context ctx, boolean isRated) {

        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        prefs.edit().putBoolean("RATE_DONE", isRated).apply();
    }

    public static boolean getRateShowDone(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        return prefs.getBoolean("RATE_DONE", false);
    }

    public static void setFirstLogin(Context ctx, int loginCount) {

        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        prefs.edit().putInt("LOGIN_COUNT", loginCount).apply();
    }

    public static int getLoginCount(Context ctx) {

        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);

        return prefs.getInt("LOGIN_COUNT", 0);
    }

    public static void setTracksViewType(Context ctx, int viewtype) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        prefs.edit().putInt("VIEW_TYPE_TRACKS", viewtype).apply();
    }

    public static int getTracksViewType(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        return prefs.getInt("VIEW_TYPE_TRACKS", 0);
    }

    public static void setArtistsViewType(Context ctx, int viewtype) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        prefs.edit().putInt("VIEW_TYPE_ARTISTS", viewtype).apply();
    }

    public static int getArtistsViewType(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        return prefs.getInt("VIEW_TYPE_ARTISTS", 0);
    }

    public static void setAlbumsViewType(Context ctx, int viewtype) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        prefs.edit().putInt("VIEW_TYPE_ALBUMS", viewtype).apply();
    }

    public static int getAlbumsViewType(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        return prefs.getInt("VIEW_TYPE_ALBUMS", 0);
    }

    public static void setGenresViewType(Context ctx, int viewtype) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        prefs.edit().putInt("VIEW_TYPE_GENRES", viewtype).apply();
    }

    public static int getGenresViewType(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        return prefs.getInt("VIEW_TYPE_GENRES", 0);
    }

    public static void setPlayerListTracksViewType(Context ctx, int viewtype) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        prefs.edit().putInt("VIEW_TYPE_PLAYERLIST_TRACKS", viewtype).apply();
    }

    public static int getPlayerListTracksViewType(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        return prefs.getInt("VIEW_TYPE_PLAYERLIST_TRACKS", 0);
    }

    public static void setPlayerListAlbumsViewType(Context ctx, int viewtype) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        prefs.edit().putInt("VIEW_TYPE_PLAYERLIST_ALBUMS", viewtype).apply();
    }

    public static int getPlayerListAlbumsViewType(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        return prefs.getInt("VIEW_TYPE_PLAYERLIST_ALBUMS", 0);
    }

    public static void setFavouriteViewType(Context ctx, int viewtype) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        prefs.edit().putInt("VIEW_TYPE_FAVOURITE", viewtype).apply();
    }

    public static int getFavouriteViewType(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        return prefs.getInt("VIEW_TYPE_FAVOURITE", 0);
    }

    public static void setStorageViewType(Context ctx, int storage) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        prefs.edit().putInt("STORAGE_ACT", storage).apply();
    }

    public static int getStorageViewType(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        return prefs.getInt("STORAGE_ACT", 0);
    }

    public static void setNeedsUpdate(Context ctx, boolean update) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        prefs.edit().putBoolean("UPDATE_NEEDED", update).apply();
    }

    public static boolean getNeedsUpdate(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        return prefs.getBoolean("UPDATE_NEEDED", false);
    }

    public static void setPremium(Context ctx, boolean listing) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        prefs.edit().putBoolean("PREMIUM", listing).apply();
    }

    public static boolean getPremium(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        return prefs.getBoolean("PREMIUM", false);
    }

    public static void setEarnedPremium(Context ctx, boolean listing) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        prefs.edit().putBoolean("PREMIUM_EARNED", listing).apply();
    }

    public static boolean getEarnedPremium(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        return prefs.getBoolean("PREMIUM_EARNED", false);
    }

    public static void setNegativeWarning(Context ctx, boolean listing) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        prefs.edit().putBoolean("NEGATIVE_WARNING", listing).apply();
    }

    public static boolean getNegativeWarning(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        return prefs.getBoolean("NEGATIVE_WARNING", false);
    }

    public static void setRegisterReferrer(Context ctx, String referrer) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        prefs.edit().putString("REGISTER_REFERRER", referrer).apply();
    }

    public static String getRegisterReferrer(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        return prefs.getString("REGISTER_REFERRER", null);
    }

    public static void setRegisterTryAgain(Context ctx, boolean state) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        prefs.edit().putBoolean("REGISTER_TRY_AGAIN", state).apply();
    }

    public static boolean getRegisterTryAgain(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        return prefs.getBoolean("REGISTER_TRY_AGAIN", false);
    }

    public static void setRegisterTime(Context ctx, long time) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        prefs.edit().putLong("REGISTER_TIME", time).apply();
    }

    public static long getRegisterTime(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        return prefs.getLong("REGISTER_TIME", 0);
    }

    public static void setRegisterNotifyTime(Context ctx, long time) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        prefs.edit().putLong("REGISTER_NOTIFY_TIME", time).apply();
    }

    public static long getRegisterNotifyTime(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        return prefs.getLong("REGISTER_NOTIFY_TIME", 0);
    }

    public static void setInviteCount(Context ctx, int count) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        prefs.edit().putInt("INVITE_COUNT", count).apply();
    }

    public static int getInviteCount(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        return prefs.getInt("INVITE_COUNT", 0);
    }

    public static void setStartFolderPath(Context ctx, String path) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        prefs.edit().putString("START_FOLDER_PATH", path).apply();
    }

    public static String getStartFolderPath(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        return prefs.getString("START_FOLDER_PATH", null);
    }

    public static void setAdLastShown(Context ctx, int count) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        prefs.edit().putInt("AD_SHOWN", count).apply();
    }

    public static int getAdLastShown(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        return prefs.getInt("AD_SHOWN", 0);
    }

    public static void setShowLaunchAd(Context ctx, boolean state) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        prefs.edit().putBoolean("SHOW_LAUNCH_AD", state).apply();
    }

    public static boolean getShowLaunchAd(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        return prefs.getBoolean("SHOW_LAUNCH_AD", false);
    }

    public static void setShowAd(Context ctx, boolean state) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        prefs.edit().putBoolean("SHOW_AD", state).apply();
    }

    public static boolean getShowAd(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        return prefs.getBoolean("SHOW_AD", false);
    }

    public static void setLastContactSync(Context ctx, long time) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        prefs.edit().putLong("CONTACTS_SYNC", time).apply();
    }

    public static long getLastContactSync(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        return prefs.getLong("CONTACTS_SYNC", 0);
    }

    public static void setProfileUrl(Context ctx, String path) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        prefs.edit().putString("PROFILE_URL", path).apply();
    }

    public static String getProfileUrl(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        return prefs.getString("PROFILE_URL", "");
    }

    public static void setDontShowUpdates(Context ctx, boolean show) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        prefs.edit().putBoolean("DONT_SHOW_UPDATES", show).apply();
    }

    public static boolean getDontShowUpdates(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        return prefs.getBoolean("DONT_SHOW_UPDATES", false);
    }

    public static void setQueueList(Context ctx, String path) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        prefs.edit().putString("QUEUE_LIST", path).apply();
    }

    public static String getQueueList(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        return prefs.getString("QUEUE_LIST", "");
    }

    public static void setDontShowNowPlayingTutorial(Context ctx, boolean show) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        prefs.edit().putBoolean("DONT_SHOW_NOW_PLAYING_TUTORIAL", show).apply();
    }

    public static boolean getDontShowNowPlayingTutorial(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        return prefs.getBoolean("DONT_SHOW_NOW_PLAYING_TUTORIAL", false);
    }

    public static void setExternalFile(Context ctx, String path) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        prefs.edit().putString("EXTERNAL_FILE", path).apply();
    }

    public static String getExternalFile(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE);
        return prefs.getString("EXTERNAL_FILE", "");
    }
}
