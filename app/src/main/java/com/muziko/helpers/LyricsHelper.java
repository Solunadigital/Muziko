package com.muziko.helpers;

import com.muziko.common.models.Lyrics;
import com.muziko.database.TrackRealmHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by dev on 19/11/2016.
 */

public class LyricsHelper {

	private static TreeMap<Long, String> dictionnary;
	private static List<Long> mTimes;
	private static String uploader;
	private static Lyrics lyrics;

	public static String getStaticLyrics(String data) {

		lyrics = TrackRealmHelper.getLyricsforTrack(data);

		List<String> texts = new ArrayList<>();
		mTimes = new ArrayList<>();

		BufferedReader reader = new BufferedReader(new StringReader(lyrics.getText()));

		String line;
		String[] arr;
		try {
			while (null != (line = reader.readLine())) {
				arr = parseLine(line);
				if (null == arr) {
					continue;
				}

				if (1 == arr.length) {
					String last = texts.remove(texts.size() - 1);
					texts.add(last + arr[0]);
					continue;
				}
				for (int i = 0; i < arr.length - 1; i++) {
					mTimes.add(Long.parseLong(arr[i]));
					texts.add(arr[arr.length - 1]);
				}
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// Collections.sort(mTimes);
		dictionnary = new TreeMap<>();
		for (int i = 0; i < mTimes.size(); i++) {
			if (!(texts.get(i).startsWith("Album:") || texts.get(i).startsWith("Title:")
					|| texts.get(i).startsWith("Artist:"))
					&& (i > 2 || (!texts.get(i).contains(lyrics.getArtist()) &&
					!texts.get(i).contains(lyrics.getTrack()) &&
					(uploader == null || !texts.get(i).contains(uploader)))))
				if (!(dictionnary.isEmpty() && texts.get(i).replaceAll("\\s", "").isEmpty()))
					dictionnary.put(mTimes.get(i), texts.get(i));
		}
		Collections.sort(mTimes);


		StringBuilder text = new StringBuilder();
		Iterator<String> iterator = dictionnary.values().iterator();
		while (iterator.hasNext()) {
			String next = iterator.next();
			if (text.length() == 0 && next.replaceAll("\\s", "").isEmpty())
				continue;
			text.append(next);
			if (iterator.hasNext())
				text.append("<br/>\n");
		}

		return text.toString();
	}

	private static Long parseTime(String time) {
		String[] min = time.split(":");
		String[] sec;
		if (!min[1].contains("."))
			min[1] += ".00";
		sec = min[1].split("\\.");

		long minInt = Long.parseLong(min[0].replaceAll("\\D+", "")
				.replaceAll("\r", "").replaceAll("\n", "").trim());
		long secInt = Long.parseLong(sec[0].replaceAll("\\D+", "")
				.replaceAll("\r", "").replaceAll("\n", "").trim());
		long milInt = Long.parseLong(sec[1].replaceAll("\\D+", "")
				.replaceAll("\r", "").replaceAll("\n", "").trim());

		return minInt * 60 * 1000 + secInt * 1000 + milInt * 10;
	}

	private static String[] parseLine(String line) {
		Matcher matcher = Pattern.compile("\\[.+\\].+").matcher(line);
		if (!matcher.matches() || line.contains("By:")) {
			if (line.contains("[by:") && line.length() > 6)
				uploader = line.substring(5, line.length() - 1);
			return null;
		}

		if (line.endsWith("]"))
			line += " ";
		line = line.replaceAll("\\[", "");
		String[] result = line.split("\\]");
		try {
			for (int i = 0; i < result.length - 1; ++i)
				result[i] = String.valueOf(parseTime(result[i]));
		} catch (NumberFormatException | ArrayIndexOutOfBoundsException ignored) {
			return null;
		}

		return result;
	}
}
