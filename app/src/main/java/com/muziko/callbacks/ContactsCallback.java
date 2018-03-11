package com.muziko.callbacks;

/**
 * Created by divyanshunegi on 12/10/15.
 */
public interface ContactsCallback {
	void onFilterValue(int value, boolean reverse);

	void onSearchQuery(String chars);

	void onReload();

	void onLayoutChanged(Float bottomMargin);

}
