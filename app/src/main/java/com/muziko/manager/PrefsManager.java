package com.muziko.manager;

import android.content.Context;
import android.content.SharedPreferences;

import com.muziko.MyApplication;
import com.muziko.R;
import com.muziko.activities.MainActivity;
import com.muziko.mediaplayer.PlayerConstants;

import static com.muziko.manager.MuzikoConstants.LANGUAGE_ENGLISH;
import static com.muziko.manager.MuzikoConstants.STORAGE_SUBSCRIPTION_LEVEL_FREE;


public class PrefsManager {

    private static PrefsManager instance;
    private Context mContext;
    private SharedPreferences prefs;

    //no outer class can initialize this class's object
    private PrefsManager() {
    }

    public static PrefsManager Instance() {
        //if no instance is initialized yet then create new instance
        //else return stored instance
        if (instance == null) {
            instance = new PrefsManager();
        }
        return instance;
    }

    public boolean getSleepTimeLastSong() {
        return prefs.getBoolean("sleep_time_last_song", false);
    }

    public void setSleepTimeLastSong(boolean listing) {
        prefs.edit().putBoolean("sleep_time_last_song", listing).apply();
    }

    public boolean getQueueWidgetChange() {
        return prefs.getBoolean("queue_widget_change", false);
    }

    public void setQueueWidgetChange(boolean listing) {
        prefs.edit().putBoolean("queue_widget_change", listing).apply();
    }

    public void setStoragePermsURI(String uri) {
        prefs.edit().putString("storageperms", uri).apply();
    }

    public String getStoragePermsURi() {
        return prefs.getString("storageperms", "");
    }

    public String getLastActivity() {
        return prefs.getString("last_activity", MainActivity.class.getName());
    }

    public void setLastActivity(String lastActivity) {
        prefs.edit().putString("last_activity", lastActivity).apply();
    }

    public String getLastMainActivityTab() {
        return prefs.getString("last_main_activity_tab", MyApplication.TRACKS);
    }

    public void setLastMainActivityTab(String lastTab) {
        prefs.edit().putString("last_main_activity_tab", lastTab).apply();
    }

    public int getLastRecentActivityTab() {
        return prefs.getInt("last_recent_activity_tab", 0);
    }

    public void setLastRecentActivityTab(int lastTab) {
        prefs.edit().putInt("last_recent_activity_tab", lastTab).apply();
    }

    public long getLastPlaylist() {
        return prefs.getLong("last_playlist", 0);
    }

    public void setLastPlaylist(long playlist) {
        prefs.edit().putLong("last_playlist", playlist).apply();
    }

    public String getLastPlaylistTitle() {
        return prefs.getString("last_playlist_title", null);
    }

    public void setLastPlaylistTitle(String playlist) {
        prefs.edit().putString("last_playlist_title", playlist).apply();
    }

    public long getLastPlayerListArt() {
        return prefs.getLong("last_playerlist_art", 0);
    }

    // PlayerList
    public void setLastPlayerListArt(long playlist) {
        prefs.edit().putLong("last_playerlist_art", playlist).apply();
    }

    public int getLastPlayerListType() {
        return prefs.getInt("last_playerlist_type", 0);
    }

    public void setLastPlayerListType(int lastTab) {
        prefs.edit().putInt("last_playerlist_type", lastTab).apply();
    }

    public String getLastPlayerListName() {
        return prefs.getString("last_playerlist_name", null);
    }

    public void setLastPlayerListName(String playlist) {
        prefs.edit().putString("last_playerlist_name", playlist).apply();
    }

    public String getLastPlayerListData() {
        return prefs.getString("last_playerlist_data", null);
    }

    public void setLastPlayerListData(String playlist) {
        prefs.edit().putString("last_playerlist_data", playlist).apply();
    }

    public void setLastPlayerListDuration(int playlist) {
        prefs.edit().putInt("last_playerlist_duration", playlist).apply();
    }

    public int getLastPlayerListtDuration() {
        return prefs.getInt("last_playerlist_duration", 0);
    }

    public int getLastPlayerListSongs() {
        return prefs.getInt("last_playerlist_songs", 0);
    }

    public void setLastPlayerListSongs(int lastTab) {
        prefs.edit().putInt("last_playerlist_songs", lastTab).apply();
    }

    public int getTrackSort() {
        return prefs.getInt("track_sort", R.id.player_sort_tracktitle);
    }

    // Sorting
    public void setTrackSort(int sort) {
        prefs.edit().putInt("track_sort", sort).apply();
    }

    public boolean getTrackSortReverse() {
        return prefs.getBoolean("track_sort_reverse", false);
    }

    public void setTrackSortReverse(boolean state) {
        prefs.edit().putBoolean("track_sort_reverse", state).apply();
    }

    public int getArtistSort() {
        return prefs.getInt("artist_sort", R.id.player_sort_title);
    }

    public void setArtistSort(int sort) {
        prefs.edit().putInt("artist_sort", sort).apply();
    }

    public boolean getArtistSortReverse() {
        return prefs.getBoolean("artist_sort_reverse", false);
    }

    public void setArtistSortReverse(boolean state) {
        prefs.edit().putBoolean("artist_sort_reverse", state).apply();
    }

    public int getAlbumSort() {
        return prefs.getInt("album_sort", R.id.player_sort_title);
    }

    public void setAlbumSort(int sort) {
        prefs.edit().putInt("album_sort", sort).apply();
    }

    public boolean getAlbumSortReverse() {
        return prefs.getBoolean("album_sort_reverse", false);
    }

    public void setAlbumSortReverse(boolean state) {
        prefs.edit().putBoolean("album_sort_reverse", state).apply();
    }

    public int getGenreSort() {
        return prefs.getInt("genre_sort", R.id.player_sort_title);
    }

    public void setGenreSort(int sort) {
        prefs.edit().putInt("genre_sort", sort).apply();
    }

    public boolean getGenreSortReverse() {
        return prefs.getBoolean("genre_sort_reverse", false);
    }

    public void setGenreSortReverse(boolean state) {
        prefs.edit().putBoolean("genre_sort_reverse", state).apply();
    }

    public int getPlayerListTrackSort() {
        return prefs.getInt("PLAYERLIST_track_sort", R.id.player_sort_tracktitle);
    }

    public void setPlayerListTrackSort(int sort) {
        prefs.edit().putInt("PLAYERLIST_track_sort", sort).apply();
    }

    public boolean getPlayerListTrackSortReverse() {
        return prefs.getBoolean("PLAYERLIST_track_sort_reverse", false);
    }

    public void setPlayerListTrackSortReverse(boolean state) {
        prefs.edit().putBoolean("PLAYERLIST_track_sort_reverse", state).apply();
    }

    public int getPlayerListAlbumSort() {
        return prefs.getInt("PLAYERLIST_album_sort", R.id.player_sort_title);
    }

    public void setPlayerListAlbumSort(int sort) {
        prefs.edit().putInt("PLAYERLIST_album_sort", sort).apply();
    }

    public boolean getPlayerListAlbumSortReverse() {
        return prefs.getBoolean("PLAYERLIST_album_sort_reverse", false);
    }

    public void setPlayerListAlbumSortReverse(boolean state) {
        prefs.edit().putBoolean("PLAYERLIST_album_sort_reverse", state).apply();
    }

    public int getFolderTrackSort() {
        return prefs.getInt("folder_track_sort", R.id.player_sort_tracktitle);
    }

    public void setFolderTrackSort(int sort) {
        prefs.edit().putInt("folder_track_sort", sort).apply();
    }

    public boolean getFolderTrackSortReverse() {
        return prefs.getBoolean("folder_track_sort_reverse", false);
    }

    public void setFolderTrackSortReverse(boolean state) {
        prefs.edit().putBoolean("folder_track_sort_reverse", state).apply();
    }

    public int getPlaylistSort() {
        return prefs.getInt("playlist_sort", 0);
    }

    public void setPlaylistSort(int sort) {
        prefs.edit().putInt("playlist_sort", sort).apply();
    }

    public boolean getPlaylistSortReverse() {
        return prefs.getBoolean("playlist_sort_reverse", false);
    }

    public void setPlaylistSortReverse(boolean state) {
        prefs.edit().putBoolean("playlist_sort_reverse", state).apply();
    }

    public int getTrashSort() {
        return prefs.getInt("trash_sort", R.id.player_sort_title);
    }

    public void setTrashSort(int sort) {
        prefs.edit().putInt("trash_sort", sort).apply();
    }

    public boolean getTrashSortReverse() {
        return prefs.getBoolean("trash_sort_reverse", false);
    }

    public void setTrashSortReverse(boolean state) {
        prefs.edit().putBoolean("trash_sort_reverse", state).apply();
    }

    public String getArtistDB() {
        return prefs.getString("artist", null);
    }

    public void setArtistDB(String artist) {
        prefs.edit().putString("artist", artist).apply();
    }

    public String getAlbumDB() {
        return prefs.getString("album", null);
    }

    public void setAlbumDB(String album) {
        prefs.edit().putString("album", album).apply();
    }

    public String getFolderDB() {
        return prefs.getString("folder", null);
    }

    public void setFolderDB(String folder) {
        prefs.edit().putString("folder", folder).apply();
    }

    public String getTracksDB() {
        return prefs.getString("tracks", null);
    }

    public void setTracksDB(String tracks) {
        prefs.edit().putString("tracks", tracks).apply();
    }

    public String getGenreDB() {
        return prefs.getString("genre", null);
    }

    public void setGenreDB(String genre) {
        prefs.edit().putString("genre", genre).apply();
    }

    public boolean getDatabaseReady() {
        return prefs.getBoolean("DB", false);
    }

    public void setDatabaseReady(boolean state) {
        prefs.edit().putBoolean("DB", state).apply();
    }

    public boolean getEqualizer() {
        return prefs.getBoolean("EQ", false);
    }

    public void setEqualizer(boolean state) {
        prefs.edit().putBoolean("EQ", state).apply();
    }

    public int getEqualizerPreset() {
        return prefs.getInt("EQPRESET", 0);
    }

    public void setEqualizerPreset(int order) {
        prefs.edit().putInt("EQPRESET", order).apply();
    }

    public long getQueueLevel() {
        return prefs.getLong("QUEUE", 0);
    }

    public void setQueueLevel(long order) {
        prefs.edit().putLong("QUEUE", order).apply();
    }

    public int getPlayPosition() {
        return prefs.getInt("MUSIC_TIME", 0);
    }

    public void setPlayPosition(int musicTime) {
        prefs.edit().putInt("MUSIC_TIME", musicTime).apply();
    }

    public int getPlayRepeat() {
        return prefs.getInt("REPEAT", PlayerConstants.REPEAT_ALL);
    }

    public void setPlayRepeat(int loopCount) {
        prefs.edit().putInt("REPEAT", loopCount).apply();
    }

    public void setPlayShuffle(boolean enableShuffle) {
        prefs.edit().putBoolean("SHUFFLE", enableShuffle).apply();
    }

    public boolean getPlayShuffle(Context ctx) {
        return prefs.getBoolean("SHUFFLE", false);
    }

    public int getRateShowNumber() {
        return prefs.getInt("RATE_COUNT", 0);
    }

    public void setRateShowNumber(int rateCount) {
        prefs.edit().putInt("RATE_COUNT", rateCount).apply();
    }

    public boolean getRateShowDone() {
        return prefs.getBoolean("RATE_DONE", false);
    }

    public void setRateShowDone(boolean isRated) {
        prefs.edit().putBoolean("RATE_DONE", isRated).apply();
    }

    public void setFirstLogin(int loginCount) {

        prefs.edit().putInt("LOGIN_COUNT", loginCount).apply();
    }

    public int getLoginCount() {
        return prefs.getInt("LOGIN_COUNT", 0);
    }

    public int getHomeViewType() {
        return prefs.getInt("VIEW_TYPE_HOME", 0);
    }

    public void setHomeViewType(int viewtype) {
        prefs.edit().putInt("VIEW_TYPE_HOME", viewtype).apply();
    }

    public int getTracksViewType() {
        return prefs.getInt("VIEW_TYPE_TRACKS", 0);
    }

    public void setTracksViewType(int viewtype) {
        prefs.edit().putInt("VIEW_TYPE_TRACKS", viewtype).apply();
    }

    public int getArtistsViewType() {
        return prefs.getInt("VIEW_TYPE_ARTISTS", 0);
    }

    public void setArtistsViewType(int viewtype) {
        prefs.edit().putInt("VIEW_TYPE_ARTISTS", viewtype).apply();
    }

    public int getAlbumsViewType() {
        return prefs.getInt("VIEW_TYPE_ALBUMS", 0);
    }

    public void setAlbumsViewType(int viewtype) {
        prefs.edit().putInt("VIEW_TYPE_ALBUMS", viewtype).apply();
    }

    public int getGenresViewType() {
        return prefs.getInt("VIEW_TYPE_GENRES", 0);
    }

    public void setGenresViewType(int viewtype) {
        prefs.edit().putInt("VIEW_TYPE_GENRES", viewtype).apply();
    }

    public int getPlayerListTracksViewType() {
        return prefs.getInt("VIEW_TYPE_PLAYERLIST_TRACKS", 0);
    }

    public void setPlayerListTracksViewType(int viewtype) {
        prefs.edit().putInt("VIEW_TYPE_PLAYERLIST_TRACKS", viewtype).apply();
    }

    public int getPlayerListAlbumsViewType() {
        return prefs.getInt("VIEW_TYPE_PLAYERLIST_ALBUMS", 0);
    }

    public void setPlayerListAlbumsViewType(int viewtype) {
        prefs.edit().putInt("VIEW_TYPE_PLAYERLIST_ALBUMS", viewtype).apply();
    }

    public int getFavouriteViewType() {
        return prefs.getInt("VIEW_TYPE_FAVOURITE", 0);
    }

    public void setFavouriteViewType(int viewtype) {
        prefs.edit().putInt("VIEW_TYPE_FAVOURITE", viewtype).apply();
    }

    public int getStorageViewType() {
        return prefs.getInt("STORAGE_ACT", 0);
    }

    public void setStorageViewType(int storage) {
        prefs.edit().putInt("STORAGE_ACT", storage).apply();
    }

    public boolean getNeedsUpdate() {
        return prefs.getBoolean("UPDATE_NEEDED", false);
    }

    public void setNeedsUpdate(boolean update) {
        prefs.edit().putBoolean("UPDATE_NEEDED", update).apply();
    }

    public boolean getPremium() {
        return prefs.getBoolean("PREMIUM", false);
    }

    public void setPremium(boolean listing) {
        prefs.edit().putBoolean("PREMIUM", listing).apply();
    }

    public boolean getEarnedPremium() {
        return prefs.getBoolean("PREMIUM_EARNED", false);
    }

    public void setEarnedPremium(boolean listing) {
        prefs.edit().putBoolean("PREMIUM_EARNED", listing).apply();
    }

    public boolean getNegativeWarning() {
        return prefs.getBoolean("NEGATIVE_WARNING", false);
    }

    public void setNegativeWarning(boolean listing) {
        prefs.edit().putBoolean("NEGATIVE_WARNING", listing).apply();
    }

    public String getRegisterReferrer() {
        return prefs.getString("REGISTER_REFERRER", null);
    }

    public void setRegisterReferrer(String referrer) {
        prefs.edit().putString("REGISTER_REFERRER", referrer).apply();
    }

    public void setRegisterTryAgain(boolean state) {
        prefs.edit().putBoolean("REGISTER_TRY_AGAIN", state).apply();
    }

    public boolean getRegisterTryAgain(Context ctx) {
        return prefs.getBoolean("REGISTER_TRY_AGAIN", false);
    }

    public long getRegisterTime() {
        return prefs.getLong("REGISTER_TIME", 0);
    }

    public void setRegisterTime(long time) {
        prefs.edit().putLong("REGISTER_TIME", time).apply();
    }

    public long getRegisterNotifyTime() {
        return prefs.getLong("REGISTER_NOTIFY_TIME", 0);
    }

    public void setRegisterNotifyTime(long time) {
        prefs.edit().putLong("REGISTER_NOTIFY_TIME", time).apply();
    }

    public int getInviteCount() {
        return prefs.getInt("INVITE_COUNT", 0);
    }

    public void setInviteCount(int count) {
        prefs.edit().putInt("INVITE_COUNT", count).apply();
    }

    public String getStartFolderPath() {
        return prefs.getString("START_FOLDER_PATH", null);
    }

    public void setStartFolderPath(String path) {
        prefs.edit().putString("START_FOLDER_PATH", path).apply();
    }

    public int getAdLastShown() {
        return prefs.getInt("AD_SHOWN", 0);
    }

    public void setAdLastShown(int count) {
        prefs.edit().putInt("AD_SHOWN", count).apply();
    }

    public boolean getShowLaunchAd() {
        return prefs.getBoolean("SHOW_LAUNCH_AD", false);
    }

    public void setShowLaunchAd(boolean state) {
        prefs.edit().putBoolean("SHOW_LAUNCH_AD", state).apply();
    }

    public boolean getShowAd() {
        return prefs.getBoolean("SHOW_AD", false);
    }

    public void setShowAd(boolean state) {
        prefs.edit().putBoolean("SHOW_AD", state).apply();
    }

    public long getLastContactSync() {
        return prefs.getLong("CONTACTS_SYNC", 0);
    }

    public void setLastContactSync(long time) {
        prefs.edit().putLong("CONTACTS_SYNC", time).apply();
    }

    public String getProfileUrl() {
        return prefs.getString("PROFILE_URL", "");
    }

    public void setProfileUrl(String path) {
        prefs.edit().putString("PROFILE_URL", path).apply();
    }

    public boolean getDontShowUpdates() {
        return prefs.getBoolean("DONT_SHOW_UPDATES", false);
    }

    public void setDontShowUpdates(boolean show) {
        prefs.edit().putBoolean("DONT_SHOW_UPDATES", show).apply();
    }

    public String getQueueList() {
        return prefs.getString("QUEUE_LIST", "");
    }

    public void setQueueList(String path) {
        prefs.edit().putString("QUEUE_LIST", path).apply();
    }

    public String getClipHistory() {
        return prefs.getString("CLIP_HISTORY", "");
    }

    public void setClipHistory(String path) {
        prefs.edit().putString("CLIP_HISTORY", path).apply();
    }

    public boolean getDontShowNowPlayingTutorial() {
        return prefs.getBoolean("DONT_SHOW_NOW_PLAYING_TUTORIAL", false);
    }

    public void setDontShowNowPlayingTutorial(boolean show) {
        prefs.edit().putBoolean("DONT_SHOW_NOW_PLAYING_TUTORIAL", show).apply();
    }

    public String getExternalFile() {
        return prefs.getString("EXTERNAL_FILE", "");
    }

    public void setExternalFile(String path) {
        prefs.edit().putString("EXTERNAL_FILE", path).apply();
    }

    public String getLanguage() {
        return prefs.getString("language", LANGUAGE_ENGLISH);
    }

    public void setLanguage(String language) {
        prefs.edit().putString("language", language).apply();
    }

    public String getSubscription() {
        String current = prefs.getString("subscription", STORAGE_SUBSCRIPTION_LEVEL_FREE);
        if (current == null) {
            return STORAGE_SUBSCRIPTION_LEVEL_FREE;
        } else {
            return current;
        }
    }

    public void setSubscription(String subscription) {
        prefs.edit().putString("subscription", subscription).apply();
    }

    public long getLastFirebaseOverlimitWarning() {
        return prefs.getLong("FIREBASE_OVERLIMIT", 0);
    }

    public void setLastFirebaseOverlimitWarning(long time) {
        prefs.edit().putLong("FIREBASE_OVERLIMIT", time).apply();
    }


    public void init(Context context) {
        mContext = context;
        prefs = mContext.getSharedPreferences(mContext.getPackageName(), Context.MODE_PRIVATE);
    }

}
