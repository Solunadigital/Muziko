
package com.muziko.api.LastFM.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
class Toptags {

	@SerializedName("tag")
	@Expose
	private List<Tag> tag = new ArrayList<Tag>();

	/**
	 * @return The tag
	 */
	public List<Tag> getTag() {
		return tag;
	}

	/**
	 * @param tag The tag
	 */
	public void setTag(List<Tag> tag) {
		this.tag = tag;
	}

}
