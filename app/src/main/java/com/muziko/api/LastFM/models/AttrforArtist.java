
package com.muziko.api.LastFM.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
public class AttrforArtist {

	@SerializedName("for")
	@Expose
	private String _for;

	/**
	 * @return The _for
	 */
	public String getFor() {
		return _for;
	}

	/**
	 * @param _for The for
	 */
	public void setFor(String _for) {
		this._for = _for;
	}

}
