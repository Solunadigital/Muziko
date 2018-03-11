package com.muziko.helpers;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.muziko.common.models.QueueItem;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by dev on 27/07/2016.
 */
public class MediaStoreHelper {

	private final static String[] genreNameColumns = {MediaStore.Audio.Genres._ID, MediaStore.Audio.Genres.NAME};
	private final static String[] albumNameColumns = {MediaStore.Audio.Albums.ALBUM, MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ARTIST};
	private static final String TAG = MediaStoreHelper.class.getSimpleName();
	private static final Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");

	// copied from MediaProvider
	private static boolean ensureFileExists(String path) {
		File file = new File(path);
		if (file.exists()) {
			return true;
		} else {
			// we will not attempt to create the first directory in the path
			// (for example, do not create /sdcard if the SD card is not mounted)
			int secondSlash = path.indexOf('/', 1);
			if (secondSlash < 1) return false;
			String directoryPath = path.substring(0, secondSlash);
			File directory = new File(directoryPath);
			if (!directory.exists())
				return false;
			file.getParentFile().mkdirs();
			try {
				return file.createNewFile();
			} catch (IOException ioe) {
				Log.e(TAG, "File creation failed", ioe);
			}
			return false;
		}
	}

	public static void removeCoverArt(Context context, long album_id) {

		try {
			ContentResolver res = context.getContentResolver();

			Uri uri = ContentUris.withAppendedId(sArtworkUri, album_id);
			context.getContentResolver().delete(uri, null, null);

			String file = Environment.getExternalStorageDirectory()
					+ "/Muziko/" + String.valueOf(System.currentTimeMillis());

			ContentValues values = new ContentValues();
			values.put("album_id", album_id);
			values.put("_data", file);
			Uri newuri = res.insert(sArtworkUri, values);
			if (newuri == null) {

//				Utils.toast(context, "There was a problem removing the cover art");

			}
			ImagePipeline imagePipeline = Fresco.getImagePipeline();
			imagePipeline.evictFromCache(Uri.parse("content://media/external/audio/albumart/" + album_id));
			Picasso.with(context).invalidate("content://media/external/audio/albumart/" + album_id);
		} catch (Exception ex) {
			Crashlytics.logException(ex);
		}
	}

	public static void updateCoverArt(Context context, byte[] bitmapdata, QueueItem queueItem) {

		boolean success = false;

		ContentResolver res = context.getContentResolver();

		Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapdata, 0, bitmapdata.length);

		String file = Environment.getExternalStorageDirectory()
				+ "/Muziko/" + String.valueOf(System.currentTimeMillis());
		if (ensureFileExists(file)) {
			try {
				OutputStream outstream = new FileOutputStream(file);
				if (bitmap.getConfig() == null) {
					bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, false);
					if (bitmap == null) {
						return;
					}
				}
				success = bitmap.compress(Bitmap.CompressFormat.PNG, 100, outstream);
				outstream.close();
				if (success) {
					Uri uri = ContentUris.withAppendedId(sArtworkUri, queueItem.album);
					context.getContentResolver().delete(uri, null, null);

					ContentValues values = new ContentValues();
					values.put("album_id", queueItem.album);
					values.put("_data", file);
					Uri newuri = res.insert(sArtworkUri, values);

					if (newuri == null) {

//						Utils.toast(context, "There was a problem saving the cover art");
						// Failed to insert in to the database. The most likely
						// cause of this is that the item already existed in the
						// database, and the most likely cause of that is that
						// the album was scanned before, but the user deleted the
						// album art from the sd card.
						// We can ignore that case here, since the media provider
						// will regenerate the album art for those entries when
						// it detects this.
						success = false;
					}
				}
//				if (!success) {
//					File f = new File(file);
//					f.delete();
//				}
				ImagePipeline imagePipeline = Fresco.getImagePipeline();
				imagePipeline.evictFromCache(Uri.parse("content://media/external/audio/albumart/" + queueItem.album));

				Picasso.with(context).invalidate("content://media/external/audio/albumart/" + queueItem.album);

			} catch (IOException e) {
				Log.e(TAG, "error creating file", e);
			}
		}

	}

	public static void updateSongTags(Context context, QueueItem queueItem) {
		ContentResolver res = context.getContentResolver();
		long genreId = 0;
		String genreName = null;

		Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
				queueItem.id);

		long current = System.currentTimeMillis();

		ContentValues values = new ContentValues();
//		values.put(MediaStore.Audio.Media._ID, queueItem.id);
//		values.put(MediaStore.Audio.Media.DATA, queueItem.data);
//		values.put(MediaStore.Audio.Media.ALBUM_ID, queueItem.album);
//		values.put(MediaStore.Audio.Media.ARTIST_ID, queueItem.artist);
		values.put(MediaStore.Audio.Media.TITLE, queueItem.title);
		values.put(MediaStore.Audio.Media.ARTIST, queueItem.artist_name);
		values.put(MediaStore.Audio.Media.ALBUM, queueItem.album_name);
		values.put(MediaStore.Audio.Media.DATE_ADDED, (int) (current / 1000));
		values.put(MediaStore.Audio.Media.DATE_MODIFIED, (int) (current / 1000));
		values.put(MediaStore.Audio.Media.TRACK, queueItem.track);

		if (queueItem.year > 0) {
			String year = String.valueOf(queueItem.year);
			values.put(MediaStore.Audio.Media.YEAR, year);
		}
		// Insert it into the database
		res.update(uri, values, null, null);

//		if (newUri == null) {
//
//			Utils.toast(context, "There was a problem updating tags");
//
//		}
	}

	public static long checkGenre(Context context, QueueItem queueItem) {
		ContentResolver res = context.getContentResolver();
		long genreId = 0;
		String genreName = null;

		Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
				queueItem.id);

		long current = System.currentTimeMillis();


		Uri audioGenresUri = MediaStore.Audio.Genres.getContentUriForAudioId("external", (int) queueItem.id);
		Cursor genresCursor = res.query(audioGenresUri,
				genreNameColumns, null, null, null);
		int genre_column_index = genresCursor.getColumnIndexOrThrow(MediaStore.Audio.Genres.NAME);


		if (genresCursor.moveToFirst()) {
			genreName = genresCursor.getString(genre_column_index);
		}

		HashMap<Long, String> genreList = getGenres(context);
		for (Map.Entry<Long, String> e : genreList.entrySet()) {
			long key = e.getKey();
			String value = e.getValue();

			if (value.equals(genreName)) {
				genreId = key;
			}
		}

		if (genreName != null) {
			if (!genreName.equals(queueItem.genre_name)) {

				// delete existing genre record
				res.delete(MediaStore.Audio.Genres.Members.getContentUri(
						"external", genreId), MediaStore.Audio.Genres.Members.AUDIO_ID + " = " + queueItem.id, null);

			}

		}


		// check if correct genre exists
		if (genreList.containsValue(queueItem.genre_name)) {
			for (Map.Entry<Long, String> e : genreList.entrySet()) {
				long key = e.getKey();
				String value = e.getValue();

				if (value.equals(queueItem.genre_name)) {
					genreId = key;
				}
			}
		} else {
			// create new genre
			ContentValues genreValues = new ContentValues();
			genreValues.put(MediaStore.Audio.Genres.NAME, queueItem.genre_name);
			Uri genreUri = res.insert(MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI, genreValues);
			if (genreUri != null) {
				genreId = ContentUris.parseId(genreUri);
			}

		}

		// add track to new genre

		ContentValues genreValues = new ContentValues();
		genreValues.put(MediaStore.Audio.Genres.Members.AUDIO_ID, queueItem.id);
		Uri membersUri = MediaStore.Audio.Genres.Members.getContentUri("external", genreId);
		res.insert(membersUri, genreValues);

		genresCursor.close();
		return genreId;
	}

	public static long checkAlbum(Context context, QueueItem queueItem) {


		ContentResolver res = context.getContentResolver();
		long albumId = 0;
		String albumName = null;

		Cursor albumsCursor = res.query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
				albumNameColumns, null, null, null);
		int genre_column_index = albumsCursor.getColumnIndexOrThrow(MediaStore.Audio.Genres.NAME);


		if (albumsCursor.moveToFirst()) {
			albumName = albumsCursor.getString(genre_column_index);
		}

		HashMap<Long, String> albumList = getAlbums(context);
		for (Map.Entry<Long, String> e : albumList.entrySet()) {
			long key = e.getKey();
			String value = e.getValue();

			if (value.equals(albumName)) {
				albumId = key;
			}
		}

		if (albumName != null) {
			if (!albumName.equals(queueItem.album_name)) {

				// delete existing genre record
				res.delete(MediaStore.Audio.Genres.Members.getContentUri(
						"external", albumId), MediaStore.Audio.Genres.Members.AUDIO_ID + " = " + queueItem.id, null);

			}

		}


		// check if correct genre exists
		if (albumList.containsValue(queueItem.album_name)) {
			for (Map.Entry<Long, String> e : albumList.entrySet()) {
				long key = e.getKey();
				String value = e.getValue();

				if (value.equals(queueItem.album_name)) {
					albumId = key;
				}
			}
		} else {
			// create new genre
			ContentValues genreValues = new ContentValues();
			genreValues.put(MediaStore.Audio.Genres.NAME, queueItem.album_name);
			Uri genreUri = res.insert(MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI, genreValues);
			if (genreUri != null) {
				albumId = ContentUris.parseId(genreUri);
			}

		}

		// add track to new genre

		ContentValues genreValues = new ContentValues();
		genreValues.put(MediaStore.Audio.Genres.Members.AUDIO_ID, queueItem.id);
		Uri membersUri = MediaStore.Audio.Genres.Members.getContentUri("external", albumId);
		res.insert(membersUri, genreValues);

		albumsCursor.close();
		return albumId;
	}

	private static HashMap<Long, String> getAlbums(Context context) {
		ContentResolver res = context.getContentResolver();

		String[] projection = new String[]{MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM, MediaStore.Audio.Albums.ARTIST, MediaStore.Audio.Albums.ALBUM_ART, MediaStore.Audio.Albums.NUMBER_OF_SONGS};
		Cursor cursor = res.query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, projection, null, null, null);

		HashMap<Long, String> albumNameMap = new HashMap<>();

		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
			albumNameMap.put(cursor.getLong(0), cursor.getString(1));
		}
		cursor.close();

		return albumNameMap;
	}


	private static HashMap<Long, String> getGenres(Context context) {
		ContentResolver res = context.getContentResolver();

		HashMap<Long, String> genreNameMap = new HashMap<>();

		final String[] genreNameColumns = {MediaStore.Audio.Genres._ID, MediaStore.Audio.Genres.NAME};
		Cursor cursor = res.query(MediaStore.Audio.Genres.getContentUri("external"), genreNameColumns, null, null, null);

		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
			genreNameMap.put(cursor.getLong(0), cursor.getString(1));
		}
		cursor.close();

		return genreNameMap;
	}


//	public void testGetContentUriForAudioId() {
//		// Insert an audio file into the content provider.
//		ContentValues values = Audio1.getInstance().getContentValues(true);
//		Uri audioUri = mContentResolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values);
//		assertNotNull(audioUri);
//		long audioId = ContentUris.parseId(audioUri);
//		assertTrue(audioId != -1);
//		// Insert a genre into the content provider.
//		values.clear();
//		values.put(MediaStore.Audio.GenresFragment.NAME, "Soda Pop");
//		Uri genreUri = mContentResolver.insert(MediaStore.Audio.GenresFragment.EXTERNAL_CONTENT_URI, values);
//		assertNotNull(genreUri);
//		long genreId = ContentUris.parseId(genreUri);
//		assertTrue(genreId != -1);
//		Cursor cursor = null;
//		try {
//			String volumeName = MediaStoreAudioTestHelper.EXTERNAL_VOLUME_NAME;
//			// Check that the audio file has no genres yet.
//			Uri audioGenresUri = MediaStore.Audio.GenresFragment.getContentUriForAudioId(volumeName, (int) audioId);
//			cursor = mContentResolver.query(audioGenresUri, null, null, null, null);
//			assertFalse(cursor.moveToNext());
//			// Link the audio file to the genre.
//			values.clear();
//			values.put(MediaStore.Audio.GenresFragment.Members.AUDIO_ID, audioId);
//			Uri membersUri = MediaStore.Audio.GenresFragment.Members.getContentUri(volumeName, genreId);
//			assertNotNull(mContentResolver.insert(membersUri, values));
//			// Check that the audio file has the genre it was linked to.
//			cursor = mContentResolver.query(audioGenresUri, null, null, null, null);
//			assertTrue(cursor.moveToNext());
//			assertEquals(genreId, cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.GenresFragment._ID)));
//		} finally {
//			if (cursor != null) {
//				cursor.close();
//			}
//			assertEquals(1, mContentResolver.delete(audioUri, null, null));
//			assertEquals(1, mContentResolver.delete(genreUri, null, null));
//		}
//	}

}
