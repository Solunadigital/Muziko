
package com.muziko.api.LastFM.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
public class Streamable {

	@SerializedName("#text")
	@Expose
	private String text;
	@SerializedName("fulltrack")
	@Expose
	private String fulltrack;

	/**
	 * @return The text
	 */
	public String getText() {
		return text;
	}

	/**
	 * @param text The #text
	 */
	public void setText(String text) {
		this.text = text;
	}

	/**
	 * @return The fulltrack
	 */
	public String getFulltrack() {
		return fulltrack;
	}

	/**
	 * @param fulltrack The fulltrack
	 */
	public void setFulltrack(String fulltrack) {
		this.fulltrack = fulltrack;
	}

}
