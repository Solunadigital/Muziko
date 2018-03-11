
package com.muziko.api.LastFM.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
public class OpensearchQuery {

	@SerializedName("#text")
	@Expose
	private String text;
	@SerializedName("role")
	@Expose
	private String role;
	@SerializedName("searchTerms")
	@Expose
	private String searchTerms;
	@SerializedName("startPage")
	@Expose
	private String startPage;

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
	 * @return The role
	 */
	public String getRole() {
		return role;
	}

	/**
	 * @param role The role
	 */
	public void setRole(String role) {
		this.role = role;
	}

	/**
	 * @return The searchTerms
	 */
	public String getSearchTerms() {
		return searchTerms;
	}

	/**
	 * @param searchTerms The searchTerms
	 */
	public void setSearchTerms(String searchTerms) {
		this.searchTerms = searchTerms;
	}

	/**
	 * @return The startPage
	 */
	public String getStartPage() {
		return startPage;
	}

	/**
	 * @param startPage The startPage
	 */
	public void setStartPage(String startPage) {
		this.startPage = startPage;
	}

}
