package com.muziko.tageditor.metadata;

import com.muziko.tageditor.fs.FSParser;
import com.muziko.tageditor.myid3.ID3Tag;
import com.muziko.tageditor.myid3.TagFormat;

import java.util.Map;
import java.util.Vector;

public class MusicMetadataSet {
	public static final String newline = System.getProperty("line.separator");
	private static final TagFormat utils = new TagFormat();
	public final ID3Tag id3v1Raw;
	public final ID3Tag.V2 id3v2Raw;
	public final MusicMetadata id3v1Clean;
	public final MusicMetadata id3v2Clean;
	public final MusicMetadata filename;
	public final MusicMetadata merged;

	private MusicMetadataSet(ID3Tag id3_v1_raw, ID3Tag.V2 id3_v2_raw,
                             MusicMetadata id3_v1_clean, MusicMetadata id3_v2_clean,
                             String file_name, String folder_name) {
		this.id3v1Raw = id3_v1_raw;
		this.id3v2Raw = id3_v2_raw;
		this.id3v1Clean = id3_v1_clean;
		this.id3v2Clean = id3_v2_clean;
		this.filename = FSParser.parseFilename(file_name, folder_name);
		this.merged = new MusicMetadata("merged");

		merge();
	}

	public static MusicMetadataSet factoryMethod(ID3Tag id3_v1_raw,
                                                 ID3Tag.V2 id3_v2_raw, String filename, String folder_name) {
		MusicMetadata id3_v1_clean = id3_v1_raw == null ? null : utils
				.process(id3_v1_raw.values);
		MusicMetadata id3_v2_clean = id3_v2_raw == null ? null : utils
				.process(id3_v2_raw.values);

		return new MusicMetadataSet(id3_v1_raw, id3_v2_raw, id3_v1_clean,
				id3_v2_clean, filename, folder_name);
	}

	public IMusicMetadata getSimplified() {
		return new MusicMetadata(merged);
	}

	public String toString() {
		String result = "{ID3TagSet. " +
				newline +
				"v1_raw: " + id3v1Raw +
				newline +
				"v2_raw: " + id3v2Raw +
				newline +
				"v1: " + id3v1Clean +
				newline +
				"v2: " + id3v2Clean +
				newline +
				"filename: " + filename +
				newline +
				"merged: " + merged +
				newline +
				" }";

		return result;
	}

    private void merge(Map src) {
        if (src == null)
			return;

		Vector keys = new Vector(src.keySet());
		for (int i = 0; i < keys.size(); i++) {
			Object key = keys.get(i);
			if (null != merged.get(key))
				continue;
			Object value = src.get(key);
			merged.put(key, value);
		}
	}

    private void merge() {
        if (id3v2Clean != null)
			merged.putAll(id3v2Clean);

		merge(id3v1Clean);
		merge(filename);
	}

}
