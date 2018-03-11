
package com.muziko.api.LastFM.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
public class Album {

	@SerializedName("name")
	@Expose
	private String name;
	@SerializedName("artist")
	@Expose
	private String artist;
	@SerializedName("mbid")
	@Expose
	private String mbid;
	@SerializedName("url")
	@Expose
	private String url;
	@SerializedName("image")
	@Expose
	private List<Image> image = new ArrayList<Image>();
	@SerializedName("listeners")
	@Expose
	private String listeners;
	@SerializedName("playcount")
	@Expose
	private String playcount;
	@SerializedName("tracks")
	@Expose
	private Tracks tracks;
	@SerializedName("tags")
	@Expose
	private Tags tags;
	@SerializedName("wiki")
	@Expose
	private Wiki wiki;

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
	 * @return The artist
	 */
	public String getArtist() {
		return artist;
	}

	/**
	 * @param artist The artist
	 */
	public void setArtist(String artist) {
		this.artist = artist;
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
	 * @return The image
	 */
	public List<Image> getImage() {
		return image;
	}

	/**
	 * @param image The image
	 */
	public void setImage(List<Image> image) {
		this.image = image;
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
	 * @return The playcount
	 */
	public String getPlaycount() {
		return playcount;
	}

	/**
	 * @param playcount The playcount
	 */
	public void setPlaycount(String playcount) {
		this.playcount = playcount;
	}

	/**
	 * @return The tracks
	 */
	public Tracks getTracks() {
		return tracks;
	}

	/**
	 * @param tracks The tracks
	 */
	public void setTracks(Tracks tracks) {
		this.tracks = tracks;
	}

	/**
	 * @return The tags
	 */
	public Tags getTags() {
		return tags;
	}

	/**
	 * @param tags The tags
	 */
	public void setTags(Tags tags) {
		this.tags = tags;
	}

	/**
	 * @return The wiki
	 */
	public Wiki getWiki() {
		return wiki;
	}

	/**
	 * @param wiki The wiki
	 */
	public void setWiki(Wiki wiki) {
		this.wiki = wiki;
	}

}
