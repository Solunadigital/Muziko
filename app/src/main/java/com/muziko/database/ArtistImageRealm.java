package com.muziko.database;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by dev on 7/07/2016.
 */
public class ArtistImageRealm extends RealmObject {

	@PrimaryKey
	private String artistName;

	private String url;
	private long Updated;

	public String getArtistName() {
		return artistName;
	}

	public void setArtistName(String artistName) {
		this.artistName = artistName;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public long getUpdated() {
		return Updated;
	}

	public void setUpdated(long updated) {
		Updated = updated;
	}

}