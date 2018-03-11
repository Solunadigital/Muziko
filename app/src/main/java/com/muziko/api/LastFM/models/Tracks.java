
package com.muziko.api.LastFM.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
public class Tracks {

	@SerializedName("track")
	@Expose
	private List<Track> track = new ArrayList<Track>();

	/**
	 * @return The track
	 */
	public List<Track> getTrack() {
		return track;
	}

	/**
	 * @param track The track
	 */
	public void setTrack(List<Track> track) {
		this.track = track;
	}

}
