package com.muziko.api.LastFM.Utils.enums;

/**
 * A convenient way to send the sort order of a query as a parameter to the
 * method.
 *
 * @author tgwizard
 */
public enum SortOrder {
	ASCENDING(
			"asc"),
	DESCENDING(
			"desc");

	private final String sql;

	SortOrder(String sql) {
		this.sql = sql;
	}

	public String getSql() {
		return sql;
	}
}