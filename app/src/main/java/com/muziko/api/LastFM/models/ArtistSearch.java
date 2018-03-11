
package com.muziko.api.LastFM.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
public class ArtistSearch {

	@SerializedName("results")
	@Expose
	private Results results;

	/**
	 * @return The results
	 */
	public Results getResults() {
		return results;
	}

	/**
	 * @param results The results
	 */
	public void setResults(Results results) {
		this.results = results;
	}

}
