package com.muziko.helpers;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.crashlytics.android.Crashlytics;

import java.util.HashMap;

class SongMetadataReader {
	private Uri GENRES_URI = MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI;
	private Activity mActivity = null;
	private String mFilename = "";
	private String mTitle = "";
	private String mGenre = "";

	SongMetadataReader(Activity activity, String filename) {
		mActivity = activity;
		mFilename = filename;
		mTitle = getBasename(filename);
		try {
			ReadMetadata();
		} catch (Exception ex) {
			Crashlytics.logException(ex);
		}
	}

	private void ReadMetadata() {
		String GENRE_ID = MediaStore.Audio.Genres._ID;
		String GENRE_NAME = MediaStore.Audio.Genres.NAME;

		// Get a map from genre ids to names
		HashMap<String, String> genreIdMap = new HashMap<>();
		Cursor c = mActivity.getContentResolver().query(
				GENRES_URI,
				new String[]{GENRE_ID, GENRE_NAME},
				null, null, null);
		for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
			genreIdMap.put(c.getString(0), c.getString(1));
		}
		mGenre = "";
		for (String genreId : genreIdMap.keySet()) {
			c = mActivity.getContentResolver().query(
					makeGenreUri(genreId),
					new String[]{MediaStore.Audio.Media.DATA},
					MediaStore.Audio.Media.DATA + " LIKE \"" + mFilename + "\"",
					null, null);
			if (c.getCount() != 0) {
				mGenre = genreIdMap.get(genreId);
				break;
			}
			c = null;
		}
		c.close();
	}

	private Uri makeGenreUri(String genreId) {
		String CONTENTDIR = MediaStore.Audio.Genres.Members.CONTENT_DIRECTORY;
		return Uri.parse(
				GENRES_URI.toString() +
						"/" +
						genreId +
						"/" +
						CONTENTDIR);
	}

	private String getBasename(String filename) {
		return filename.substring(filename.lastIndexOf('/') + 1,
				filename.lastIndexOf('.'));
	}
}