package com.muziko.helpers;

import io.realm.Realm;

/**
 * Created by Bradley on 16/02/2017.
 */

public class RealmHelper {

	public static long getUniqueId(Realm realm, Class className) {
		Number number = realm.where(className).max("id");
		if (number == null) return 1;
		else return (long) number + 1;
	}
}
