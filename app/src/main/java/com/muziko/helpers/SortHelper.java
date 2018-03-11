package com.muziko.helpers;

import com.crashlytics.android.Crashlytics;
import com.muziko.R;
import com.muziko.common.models.QueueItem;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by dev on 27/09/2016.
 */

public class SortHelper {

	// star strings (we use the Unicode BLACK STAR and WHITE STAR characters -- lazy graphics!)
	private final String[] STAR_STRINGS = {
			"\u2606\u2606\u2606\u2606\u2606", // 0 stars
			"\u2605\u2606\u2606\u2606\u2606", // 1 star
			"\u2605\u2605\u2606\u2606\u2606", // 2 stars
			"\u2605\u2605\u2605\u2606\u2606", // 3 stars
			"\u2605\u2605\u2605\u2605\u2606", // 4 stars
			"\u2605\u2605\u2605\u2605\u2605", // 5 stars
	};
	private ArrayList<QueueItem> items = new ArrayList<>();
	private String lastAlphaSectionName = "A";
	private String lastTimeSectionName = "0:00";
	private String lastDateSectionName = "1/1/2016";

	private boolean hasNonAlpha(String s) {
		return s.matches("^.*[^a-zA-Z0-9 ].*$");
	}

	public String getSectionName(int sort, QueueItem queueItem) {
		String result = "";
		try {


			switch (sort) {
				case R.id.player_sort_tracks:
					result = String.valueOf(queueItem.track);
					return result;
				case R.id.player_sort_tracktitle:

					result = String.valueOf(queueItem.title);
					if (result == null) {
						return lastAlphaSectionName;
					}
					if (result.length() == 0) {
						return lastAlphaSectionName;
					}

					if (hasNonAlpha(result.substring(0, 1))) {
						return lastAlphaSectionName;
					} else {
						lastAlphaSectionName = result.substring(0, 1).toUpperCase();
						return result.substring(0, 1).toUpperCase();
					}
				case R.id.player_sort_title:

					result = String.valueOf(queueItem.title);
					if (result == null) {
						return lastAlphaSectionName;
					}
					if (result.length() == 0) {
						return lastAlphaSectionName;
					}

					if (hasNonAlpha(result.substring(0, 1))) {
						return lastAlphaSectionName;
					} else {
						lastAlphaSectionName = result.substring(0, 1).toUpperCase();
						return result.substring(0, 1).toUpperCase();
					}
				case R.id.player_sort_filename:
					result = String.valueOf(queueItem.title);
					if (result == null) {
						return lastAlphaSectionName;
					}
					if (result.length() == 0) {
						return lastAlphaSectionName;
					}

					if (hasNonAlpha(result.substring(0, 1))) {
						return lastAlphaSectionName;
					} else {
						lastAlphaSectionName = result.substring(0, 1).toUpperCase();
						return result.substring(0, 1).toUpperCase();
					}
				case R.id.player_sort_album:
					result = String.valueOf(queueItem.album_name);
					if (result == null) {
						return lastAlphaSectionName;
					}
					if (result.length() == 0) {
						return lastAlphaSectionName;
					}

					if (hasNonAlpha(result.substring(0, 1))) {
						return lastAlphaSectionName;
					} else {
						lastAlphaSectionName = result.substring(0, 1).toUpperCase();
						return result.substring(0, 1).toUpperCase();
					}
				case R.id.player_sort_artist:
					result = String.valueOf(queueItem.artist_name);
					if (result == null) {
						return lastAlphaSectionName;
					}
					if (result.length() == 0) {
						return lastAlphaSectionName;
					}

					if (hasNonAlpha(result)) {
						return lastAlphaSectionName;
					} else {
						lastAlphaSectionName = result.substring(0, 1).toUpperCase();
						return result.substring(0, 1).toUpperCase();
					}
				case R.id.player_sort_trackduration:

					result = Utils.getDuration(Integer.valueOf(queueItem.duration));
					if (result == null) {
						return lastTimeSectionName;
					}
					if (result.length() == 0) {
						return lastTimeSectionName;
					}

					lastTimeSectionName = result;
					return result;
				case R.id.player_sort_duration:

					result = Utils.getDuration(Integer.valueOf(queueItem.duration));
					if (result == null) {
						return lastTimeSectionName;
					}
					if (result.length() == 0) {
						return lastTimeSectionName;
					}

					lastTimeSectionName = result;
					return result;

				case R.id.player_sort_year:
					result = String.valueOf(queueItem.year);
					return result;
				case R.id.player_sort_trackdate:
					result = FriendlyTimeFormat.format(Long.valueOf(queueItem.date));
					if (result == null) {
						return lastDateSectionName;
					}
					if (result.length() == 0) {
						return lastDateSectionName;
					}

					if (hasNonAlpha(result)) {
						return lastDateSectionName;
					} else {
						lastDateSectionName = result;
						return result;
					}
				case R.id.player_sort_date:
					result = FriendlyTimeFormat.format(Long.valueOf(queueItem.date));
					if (result == null) {
						return lastDateSectionName;
					}
					if (result.length() == 0) {
						return lastDateSectionName;
					}

					if (hasNonAlpha(result)) {
						return lastDateSectionName;
					} else {
						lastDateSectionName = result;
						return result;
					}
				case R.id.player_sort_songs:
					result = String.valueOf(queueItem.songs);
					return result;
				case R.id.player_sort_rating:

					switch (queueItem.rating) {
						case 0:
							return STAR_STRINGS[0];
						case 1:
							return STAR_STRINGS[1];
						case 2:
							return STAR_STRINGS[2];
						case 3:
							return STAR_STRINGS[3];
						case 4:
							return STAR_STRINGS[4];
						case 5:
							return STAR_STRINGS[5];
					}
					return result;
			}
		} catch (Exception ex) {
			Crashlytics.logException(ex);
		}
		return result;
	}


	public ArrayList<QueueItem> sort(int sort, boolean reverse, ArrayList<QueueItem> list) {

		items = list;
		switch (sort) {
			case R.id.player_sort_tracks:
				if (!reverse) {
					sortTrackLowest();
				} else {
					sortTrackHighest();
				}
				break;
			case R.id.player_sort_title:
				if (!reverse) {
					sortTitleLowest();
				} else {
					sortTitleHighest();
				}
				break;
			case R.id.player_sort_filename:
				if (!reverse) {
					sortFilenameLowest();
				} else {
					sortFilenameHighest();
				}
				break;
			case R.id.player_sort_album:
				if (!reverse) {
					sortAlbumLowest();
				} else {
					sortAlbumHighest();
				}
				break;
			case R.id.player_sort_artist:
				if (!reverse) {
					sortArtistLowest();
				} else {
					sortAlbumHighest();
				}
				break;
			case R.id.player_sort_duration:
				if (!reverse) {
					sortDurationSmallest();
				} else {
					sortDurationLargest();
				}
				break;
			case R.id.player_sort_year:
				if (!reverse) {
					sortYearEarliest();
				} else {
					sortYearLatest();
				}
				break;
			case R.id.player_sort_date:
				if (!reverse) {
					sortDateEarliest();
				} else {
					sortDateLatest();
				}
				break;
		}

		return items;
	}

	private void sortTrackLowest() {
		Collections.sort(items, (s1, s2) -> s1.track - s2.track);

	}

	private void sortTrackHighest() {
		Collections.sort(items, (s1, s2) -> s2.track - s1.track);
	}

	private void sortTitleLowest() {

		Collections.sort(items, (s1, s2) -> s1.title.compareToIgnoreCase(s2.title));
	}

	private void sortTitleHighest() {
		Collections.sort(items, (s1, s2) -> s2.title.compareToIgnoreCase(s1.title));
	}

	private void sortFilenameLowest() {
		Collections.sort(items, (s1, s2) -> {

			String filename1 = new File(s1.data).getName();
			String filename2 = new File(s2.data).getName();
			return filename1.compareTo(filename2);
		});
	}

	private void sortFilenameHighest() {
		Collections.sort(items, (s1, s2) -> {

			String filename1 = new File(s1.data).getName();
			String filename2 = new File(s2.data).getName();
			return filename2.compareTo(filename1);
		});

	}

	private void sortAlbumLowest() {
		Collections.sort(items, (s1, s2) -> s1.album_name.compareTo(s2.album_name));

	}

	private void sortAlbumHighest() {
		Collections.sort(items, (s1, s2) -> s2.album_name.compareTo(s1.album_name));
	}

	private void sortArtistLowest() {
		Collections.sort(items, (s1, s2) -> s1.artist_name.compareTo(s2.artist_name));
	}

	public void sortArtistHighest() {
		Collections.sort(items, (s1, s2) -> s2.artist_name.compareTo(s1.artist_name));

	}

	private void sortDurationSmallest() {
		Collections.sort(items, (s1, s2) -> Integer.valueOf(s1.duration).compareTo(Integer.valueOf(s2.duration)));
	}

	private void sortDurationLargest() {
		Collections.sort(items, (s1, s2) -> Integer.valueOf(s2.duration).compareTo(Integer.valueOf(s1.duration)));
	}


	private void sortYearEarliest() {
		Collections.sort(items, (s1, s2) -> s1.year - s2.year);
	}

	private void sortYearLatest() {
		Collections.sort(items, (s1, s2) -> s2.year - s1.year);
	}

	private void sortDateEarliest() {
		Collections.sort(items, (s1, s2) -> s2.date.compareTo(s1.date));
	}

	private void sortDateLatest() {
		Collections.sort(items, (s1, s2) -> s1.date.compareTo(s2.date));
	}

	public void sortSongsLowest() {
		Collections.sort(items, (s1, s2) -> s1.songs - s2.songs);

	}

	public void sortSongsHighest() {
		Collections.sort(items, (s1, s2) -> s2.songs - s1.songs);
	}

}
