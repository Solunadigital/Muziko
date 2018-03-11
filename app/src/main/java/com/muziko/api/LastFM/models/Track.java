
package com.muziko.api.LastFM.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
public class Track {

	@SerializedName("name")
	@Expose
	private String name;
	@SerializedName("url")
	@Expose
	private String url;
	@SerializedName("duration")
	@Expose
	private String duration;
	@SerializedName("@attr")
	@Expose
	private Attr attr;
	@SerializedName("streamable")
	@Expose
	private Streamable streamable;
	@SerializedName("artist")
	@Expose
	private Artist artist;

	/**
	 * @return The name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name The name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return The url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @param url The url
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * @return The duration
	 */
	public String getDuration() {
		return duration;
	}

	/**
	 * @param duration The duration
	 */
	public void setDuration(String duration) {
		this.duration = duration;
	}

	/**
	 * @return The attr
	 */
	public Attr getAttr() {
		return attr;
	}

	/**
	 * @param attr The @attr
	 */
	public void setAttr(Attr attr) {
		this.attr = attr;
	}

	/**
	 * @return The streamable
	 */
	public Streamable getStreamable() {
		return streamable;
	}

	/**
	 * @param streamable The streamable
	 */
	public void setStreamable(Streamable streamable) {
		this.streamable = streamable;
	}

	/**
	 * @return The artist
	 */
	public Artist getArtist() {
		return artist;
	}

	/**
	 * @param artist The artist
	 */
	public void setArtist(Artist artist) {
		this.artist = artist;
	}

}
