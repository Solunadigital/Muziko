package com.muziko.database;

import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

/**
 * Created by dev on 8/07/2016.
 */
public class PlaylistSongRealm extends RealmObject {

	@PrimaryKey
	private int key;
	private String data;
	@Index
	private long playlist;

	public int getKey() {
		return key;
	}

	public void setKey(int key) {
		this.key = key;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public long getPlaylist() {
		return playlist;
	}

	public void setPlaylist(long playlist) {
		this.playlist = playlist;
	}
}
