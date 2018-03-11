
package com.muziko.api.LastFM.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
public class Attr {

	@SerializedName("rank")
	@Expose
	private String rank;

	/**
	 * @return The rank
	 */
	public String getRank() {
		return rank;
	}

	/**
	 * @param rank The rank
	 */
	public void setRank(String rank) {
		this.rank = rank;
	}

}
