
package com.muziko.api.LastFM.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
public class Results {

	@SerializedName("opensearch:Query")
	@Expose
	private OpensearchQuery opensearchQuery;
	@SerializedName("opensearch:totalResults")
	@Expose
	private String opensearchTotalResults;
	@SerializedName("opensearch:startIndex")
	@Expose
	private String opensearchStartIndex;
	@SerializedName("opensearch:itemsPerPage")
	@Expose
	private String opensearchItemsPerPage;
	@SerializedName("artistmatches")
	@Expose
	private Artistmatches artistmatches;
	@SerializedName("@attr")
	@Expose
	private AttrforArtist attr;

	/**
	 * @return The opensearchQuery
	 */
	public OpensearchQuery getOpensearchQuery() {
		return opensearchQuery;
	}

	/**
	 * @param opensearchQuery The opensearch:Query
	 */
	public void setOpensearchQuery(OpensearchQuery opensearchQuery) {
		this.opensearchQuery = opensearchQuery;
	}

	/**
	 * @return The opensearchTotalResults
	 */
	public String getOpensearchTotalResults() {
		return opensearchTotalResults;
	}

	/**
	 * @param opensearchTotalResults The opensearch:totalResults
	 */
	public void setOpensearchTotalResults(String opensearchTotalResults) {
		this.opensearchTotalResults = opensearchTotalResults;
	}

	/**
	 * @return The opensearchStartIndex
	 */
	public String getOpensearchStartIndex() {
		return opensearchStartIndex;
	}

	/**
	 * @param opensearchStartIndex The opensearch:startIndex
	 */
	public void setOpensearchStartIndex(String opensearchStartIndex) {
		this.opensearchStartIndex = opensearchStartIndex;
	}

	/**
	 * @return The opensearchItemsPerPage
	 */
	public String getOpensearchItemsPerPage() {
		return opensearchItemsPerPage;
	}

	/**
	 * @param opensearchItemsPerPage The opensearch:itemsPerPage
	 */
	public void setOpensearchItemsPerPage(String opensearchItemsPerPage) {
		this.opensearchItemsPerPage = opensearchItemsPerPage;
	}

	/**
	 * @return The artistmatches
	 */
	public Artistmatches getArtistmatches() {
		return artistmatches;
	}

	/**
	 * @param artistmatches The artistmatches
	 */
	public void setArtistmatches(Artistmatches artistmatches) {
		this.artistmatches = artistmatches;
	}

	/**
	 * @return The attr
	 */
	public AttrforArtist getAttr() {
		return attr;
	}

	/**
	 * @param attr The @attr
	 */
	public void setAttr(AttrforArtist attr) {
		this.attr = attr;
	}

}
