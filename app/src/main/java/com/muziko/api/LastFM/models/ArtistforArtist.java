
package com.muziko.api.LastFM.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
public class ArtistforArtist {

	@SerializedName("name")
	@Expose
	private String name;
	@SerializedName("listeners")
	@Expose
	private String listeners;
	@SerializedName("mbid")
	@Expose
	private String mbid;
	@SerializedName("url")
	@Expose
	private String url;
	@SerializedName("streamable")
	@Expose
	private String streamable;
	@SerializedName("image")
	@Expose
	private List<ImageforArtist> image = new ArrayList<ImageforArtist>();

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
	 * @return The listeners
	 */
	public String getListeners() {
		return listeners;
	}

	/**
	 * @param listeners The listeners
	 */
	public void setListeners(String listeners) {
		this.listeners = listeners;
	}

	/**
	 * @return The mbid
	 */
	public String getMbid() {
		return mbid;
	}

	/**
	 * @param mbid The mbid
	 */
	public void setMbid(String mbid) {
		this.mbid = mbid;
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
	 * @return The streamable
	 */
	public String getStreamable() {
		return streamable;
	}

	/**
	 * @param streamable The streamable
	 */
	public void setStreamable(String streamable) {
		this.streamable = streamable;
	}

	/**
	 * @return The image
	 */
	public List<ImageforArtist> getImage() {
		return image;
	}

	/**
	 * @param image The image
	 */
	public void setImage(List<ImageforArtist> image) {
		this.image = image;
	}

}
