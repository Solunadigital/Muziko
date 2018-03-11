
package com.muziko.api.LastFM.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
class TrackSearch {

	@SerializedName("track")
	@Expose
	private Track track;

	/**
	 * @return The track
	 */
	public Track getTrack() {
		return track;
	}

	/**
	 * @param track The track
	 */
	public void setTrack(Track track) {
		this.track = track;
	}

}
