
package com.muziko.api.LastFM.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
public class AlbumSearch {

	@SerializedName("album")
	@Expose
	private Album album;

	/**
	 * @return The album
	 */
	public Album getAlbum() {
		return album;
	}

	/**
	 * @param album The album
	 */
	public void setAlbum(Album album) {
		this.album = album;
	}

}
