package com.muziko.mediaplayer;

import com.muziko.common.controls.MuzikoArrayList;
import com.muziko.common.models.QueueItem;

import java.util.ArrayList;

public class PlayerConstants {

	public static final int REPEAT_OFF = 0;
	public static final int REPEAT_ONE = 1;
	public static final int REPEAT_ALL = 2;
	public static final int REPEAT_TOTAL = 3;
	public static final int QUEUE_STATE_PLAYING = 1;
	public static final int QUEUE_STATE_PAUSED = 2;
	public static final int QUEUE_STATE_STOPPED = 3;
    public static final int QUEUE_STATE_BUFFERING = 4;
    public static final int QUEUE_TYPE_NONE = 0;
	public static final int QUEUE_TYPE_QUEUE = 1;
	public static final int QUEUE_TYPE_RECENT = 2;
	public static final int QUEUE_TYPE_LATEST = 3;
	public static final int QUEUE_TYPE_TRACKS = 4;
	public static final int QUEUE_TYPE_ALBUMS = 5;
	public static final int QUEUE_TYPE_ARTISTS = 6;
	public static final int QUEUE_TYPE_GENRES = 7;
	public static final int QUEUE_TYPE_FOLDERS = 8;
	public static final int QUEUE_TYPE_FAVORITES = 9;
	public static final int QUEUE_TYPE_ACTIVITY = 10;
	public static final int QUEUE_ACTION_DELETE = 0;
	public static final int QUEUE_ACTION_QUEUE = 1;
	public static final int QUEUE_ACTION_NEXT = 2;
	public static final int QUEUE_ACTION_SAVE = 3;
	public static final int QUEUE_ACTION_SHARE = 4;
	public static final int QUEUE_TYPE_PLAYLIST = 11;
	public static final int QUEUE_TYPE_PLAYLIST_SONGS = 12;
	public static final int REQUEST_NOW = 90;
	public static final int REQUEST_QUEUE = 91;
	public static final int RESULT_EQUALIZER = 90;
	public static final int RESULT_GOTO_ARTIST = 91;
	public static final int RESULT_GOTO_ALBUM = 92;
	public static final int UPDATE_TIME = 150;
	private static final int QUEUE_STATE_READY = 0;
	//song number which is playing right now from SONGS_LIST
	public static long SLEEP_TIMER = 0;
	public static long SAVE_TIMER = 0;
	public static long SAVE_INTERVAL = 5 * 60 * 1000;
	//song is playing or paused
	public static int QUEUE_STATE = QUEUE_STATE_READY;
	public static int QUEUE_TIME = 0;
	public static int QUEUE_DURATION = 0;
	public static long QUEUE_DIRTY = 0;
	public static boolean QUEUE_SAVING = false;
	public static long QUEUE_TYPE = 0;
	public static int QUEUE_INDEX = 0;
	public static QueueItem QUEUE_SONG = new QueueItem();
	public static MuzikoArrayList<QueueItem> QUEUE_LIST = new MuzikoArrayList<>();
	public static ArrayList<QueueItem> PLAYLIST_QUEUE = new ArrayList<>();

    public enum MiniPlayerState {PLAYER_MINI, PLAYER_QUEUE, PLAYER_NOW, PLAYER_CLOSED}
}
