
package com.muziko.api.LastFM.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
public class Artistmatches {

	@SerializedName("artist")
	@Expose
	private List<ArtistforArtist> artist = new ArrayList<ArtistforArtist>();

	/**
	 * @return The artist
	 */
	public List<ArtistforArtist> getArtist() {
		return artist;
	}

	/**
	 * @param artist The artist
	 */
	public void setArtist(List<ArtistforArtist> artist) {
		this.artist = artist;
	}

}
