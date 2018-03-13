package com.muziko.tageditor.metadata;

import java.util.Vector;

public interface IMusicMetadata {

	String getSongTitle();

	void setSongTitle(String s);

	String getArtist();

	void setArtist(String s);

	String getAlbum();

	void setAlbum(String s);

	String getYear();

	void setYear(String s);

	String getComment();

	void setComment(String s);

	Number getTrackNumber();

	void setTrackNumber(Number s);

	String getGenre();

	void setGenre(String s);

	String getDurationSeconds();

	void setDurationSeconds(String s);

	String getComposer();

	void setComposer(String s);

	String getProducerArtist();

	void setProducerArtist(String s);

	String getComposer2();

	void setComposer2(String s);

	String getCompilation();

	void setCompilation(String s);

	void clearSongTitle();

	void clearArtist();

	void clearAlbum();

	void clearYear();

	void clearComment();

	void clearTrackNumber();

	void clearGenre();

	void clearDurationSeconds();

	void clearComposer();

	void clearProducerArtist();

	void clearComposer2();

	void clearCompilation();

	void clearFeaturingList();

	Vector getFeaturingList();

	void setFeaturingList(Vector v);

	String getProducer();

	void setProducer(String s);

	void clearProducer();

}