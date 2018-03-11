package com.muziko.objects;

import com.muziko.MyApplication;
import com.muziko.R;

public class MenuObject {
    public static final int ADD_TO_QUEUE = 1;
    public static final int ADD_TO_PLAYLIST = 2;
    public static final int PLAY_NEXT = 3;
    public static final int FAV = 4;
    public static final int GO_TO_ARTIST = 5;
    public static final int GO_TO_ALBUM = 6;
    public static final int CUT = 7;
    public static final int EDIT_TAGS = 8;
    public static final int DETAILS = 9;
    public static final int SHARE_ITEM = 10;
    public static final int MOVE_TO_IGNORE = 11;
    public static final int DELETE_ITEM = 12;
    public static final int RESET_MOST_PLAYED = 13;
    public static final int PLAY_X_TIMES = 14;
    public static final int SET_RINGTONE = 15;
    public static final int PREVIEW_SONG = 16;
    public static final int RESET_RECENT_PLAYED = 17;
    public static final int SEND = 18;
    public static final int SEND_WIFI = 19;
    public static final int MANAGE_ARTWORK = 20;
    public static final int SET_AS_START_DIRECTORY = 21;
    public static final int SCAN = 22;
    public static final int DELETE_FROM_PLAYLIST = 23;
    public static final int DELETE_FROM_QUEUE = 24;
    public static final int SEND_CONTACTS = 25;
    public static final int SET_START_TIME = 26;
    public static final int SEND_AUDIO_CLIP = 27;
    public static final int RENAME_CLOUD = 28;
    public static final int REMOVE_CLOUD = 29;
    public static final int DOWNLOAD = 30;
    public static final int UPLOAD = 31;
    public static final int ADD_TO_LIBRARY = 32;
    public static final int ADD_TO_HOME = 33;
    public static final int REMOVE_FROM_HOME = 34;
    public static final int HIDE_FOLDER = 35;
    public static final int SHOW_FOLDER = 36;
    public static final int EXCLUDE_FOLDER = 37;
    public static final int INCLUDE_FOLDER = 38;
    public static final int SYNC_FAV_OR_PLAYLIST = 39;
    public static final int DONT_SYNC_FAV_OR_PLAYLIST = 40;

    public final int id;
    public final String title;
    public int icon;

    public MenuObject(int id, String title) {
        this.id = id;
        this.title = title;
    }

    public MenuObject(int id) {
        this.id = id;
        this.title = getTitle();
        this.icon = getIcon();
    }

    public int getIcon() {
        switch (id) {
            case ADD_TO_QUEUE:
                return R.drawable.menu_queue;

            case ADD_TO_PLAYLIST:
                return R.drawable.menu_playlist;

            case FAV:
                return R.drawable.menu_fav;

            case PLAY_NEXT:
                return R.drawable.menu_playnext;

            case GO_TO_ARTIST:
                return R.drawable.menu_artist;

            case GO_TO_ALBUM:
                return R.drawable.menu_album;

            case CUT:
                return R.drawable.menu_cut;

            case EDIT_TAGS:
                return R.drawable.menu_tags;

            case DETAILS:
                return R.drawable.menu_details;

            case SHARE_ITEM:
                return R.drawable.menu_share;

            case MOVE_TO_IGNORE:
                return R.drawable.menu_ignore;

            case DELETE_ITEM:
                return R.drawable.menu_delete;

            case PLAY_X_TIMES:
                return R.drawable.menu_removeafter;

            case SET_RINGTONE:
                return R.drawable.menu_ringtone;

            case PREVIEW_SONG:
                return R.drawable.menu_songpreview;

            case RESET_MOST_PLAYED:
                return R.drawable.menu_resetmostplayed;

            case RESET_RECENT_PLAYED:
                return R.drawable.menu_resetmostplayed;

            case SEND:
                return R.drawable.menu_send;

            case SEND_WIFI:
                return R.drawable.menu_wifi;

            case MANAGE_ARTWORK:
                return R.drawable.menu_manage_artwork;

            case SET_AS_START_DIRECTORY:
                return R.drawable.menu_defaultfolder;

            case SCAN:
                return R.drawable.menu_scanmedia;

            case DELETE_FROM_PLAYLIST:
                return R.drawable.menu_delete;

            case DELETE_FROM_QUEUE:
                return R.drawable.menu_delete;

            case SEND_CONTACTS:
                return R.drawable.menu_contact;

            case SET_START_TIME:
                return R.drawable.menu_play_from;

            case SEND_AUDIO_CLIP:
                return R.drawable.send_au_clip;

            case RENAME_CLOUD:
                return R.drawable.ic_rename;

            case REMOVE_CLOUD:
                return R.drawable.ic_minus_circle;

            case DOWNLOAD:
                return R.drawable.ic_download;

            case UPLOAD:
                return R.drawable.ic_upload;

            case ADD_TO_LIBRARY:
                return R.drawable.ic_add_to_library;

            case ADD_TO_HOME:
                return R.drawable.menu_add_to_home;

            case REMOVE_FROM_HOME:
                return R.drawable.menu_remove_home;

            case HIDE_FOLDER:
                return R.drawable.menu_folder_hide;

            case SHOW_FOLDER:
                return R.drawable.menu_folder_show;

            case EXCLUDE_FOLDER:
                return R.drawable.menu_exclude_folder;

            case INCLUDE_FOLDER:
                return R.drawable.menu_include_folder;

            case SYNC_FAV_OR_PLAYLIST:
                return R.drawable.menu_sync;

            case DONT_SYNC_FAV_OR_PLAYLIST:
                return R.drawable.menu_notsync;
        }
        return 0;
    }

    @Override
    public String toString() {
        return title;
    }

    private String getTitle() {
        switch (id) {
            case ADD_TO_QUEUE:
                return MyApplication.getInstance().getApplicationContext().getString(R.string.add_to_queue);

            case ADD_TO_PLAYLIST:
                return MyApplication.getInstance().getApplicationContext().getString(R.string.add_to_playlist);

            case PLAY_NEXT:
                return MyApplication.getInstance().getApplicationContext().getString(R.string.play_next);

            case GO_TO_ARTIST:
                return MyApplication.getInstance().getApplicationContext().getString(R.string.go_to_artist);

            case GO_TO_ALBUM:
                return MyApplication.getInstance().getApplicationContext().getString(R.string.go_to_album);

            case CUT:
                return MyApplication.getInstance().getApplicationContext().getString(R.string.cut);

            case EDIT_TAGS:
                return MyApplication.getInstance().getApplicationContext().getString(R.string.tag_edit);

            case DETAILS:
                return MyApplication.getInstance().getApplicationContext().getString(R.string.details);

            case SHARE_ITEM:
                return MyApplication.getInstance().getApplicationContext().getString(R.string.share);

            case MOVE_TO_IGNORE:
                return MyApplication.getInstance().getApplicationContext().getString(R.string.move_to_ignore);

            case DELETE_ITEM:
                return MyApplication.getInstance().getApplicationContext().getString(R.string.delete);

            case PLAY_X_TIMES:
                return MyApplication.getInstance().getApplicationContext().getString(R.string.play_x_times);

            case SET_RINGTONE:
                return MyApplication.getInstance().getApplicationContext().getString(R.string.set_as_ringtone);

            case PREVIEW_SONG:
                return MyApplication.getInstance().getApplicationContext().getString(R.string.preview_track);

            case RESET_MOST_PLAYED:
                return MyApplication.getInstance().getApplicationContext().getString(R.string.reset_play_count);

            case RESET_RECENT_PLAYED:
                return MyApplication.getInstance().getApplicationContext().getString(R.string.remove);

            case SEND:
                return MyApplication.getInstance().getApplicationContext().getString(R.string.send);

            case SEND_WIFI:
                return MyApplication.getInstance().getApplicationContext().getString(R.string.send_wifi);

            case MANAGE_ARTWORK:
                return MyApplication.getInstance().getApplicationContext().getString(R.string.manage_artwork);

            case SET_AS_START_DIRECTORY:
                return MyApplication.getInstance().getApplicationContext().getString(R.string.set_as_start_directory);

            case SCAN:
                return MyApplication.getInstance().getApplicationContext().getString(R.string.scan);

            case DELETE_FROM_PLAYLIST:
                return MyApplication.getInstance().getApplicationContext().getString(R.string.delete_from_playlist);

            case DELETE_FROM_QUEUE:
                return MyApplication.getInstance().getApplicationContext().getString(R.string.delete_from_queue);

            case SEND_CONTACTS:
                return MyApplication.getInstance().getApplicationContext().getString(R.string.contacts);

            case SET_START_TIME:
                return MyApplication.getInstance().getApplicationContext().getString(R.string.set_start_time);

            case SEND_AUDIO_CLIP:
                return MyApplication.getInstance().getApplicationContext().getString(R.string.send_au_clip);

            case RENAME_CLOUD:
                return MyApplication.getInstance().getApplicationContext().getString(R.string.rename);

            case REMOVE_CLOUD:
                return MyApplication.getInstance().getApplicationContext().getString(R.string.remove);

            case DOWNLOAD:
                return MyApplication.getInstance().getApplicationContext().getString(R.string.download_cloud_track);

            case UPLOAD:
                return MyApplication.getInstance().getApplicationContext().getString(R.string.upload);

            case ADD_TO_LIBRARY:
                return MyApplication.getInstance().getApplicationContext().getString(R.string.add_to_library);

            case ADD_TO_HOME:
                return MyApplication.getInstance().getApplicationContext().getString(R.string.add_to_home);

            case REMOVE_FROM_HOME:
                return MyApplication.getInstance().getApplicationContext().getString(R.string.remove_from_home);

            case HIDE_FOLDER:
                return MyApplication.getInstance().getApplicationContext().getString(R.string.hide_folder);

            case SHOW_FOLDER:
                return MyApplication.getInstance().getApplicationContext().getString(R.string.show_folder);

            case EXCLUDE_FOLDER:
                return MyApplication.getInstance().getApplicationContext().getString(R.string.exclude_folder);

            case INCLUDE_FOLDER:
                return MyApplication.getInstance().getApplicationContext().getString(R.string.include_folder);

            case SYNC_FAV_OR_PLAYLIST:
                return MyApplication.getInstance().getApplicationContext().getString(R.string.sync);

            case DONT_SYNC_FAV_OR_PLAYLIST:
                return MyApplication.getInstance().getApplicationContext().getString(R.string.dont_sync);
        }
        return "";
    }

}
