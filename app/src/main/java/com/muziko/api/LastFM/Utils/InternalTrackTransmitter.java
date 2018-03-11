package com.muziko.api.LastFM.Utils;

import java.util.LinkedList;

/**
 * Internal class that transmits tracks from the scrobbling API listeners to the
 * {@link ScrobblingService}.
 *
 * @see AbstractPlayStatusReceiver
 */
public class InternalTrackTransmitter {
	private static LinkedList<Track> tracks = new LinkedList<>();

	/**
	 * Appends {@code track} to the queue of tracks that
	 * {@link ScrobblingService} will pickup.
	 * <p>
	 * The method is thread-safe.
	 *
	 * @param track the track to be appended
	 * @see #popTrack()
	 */
	public static synchronized void appendTrack(Track track) {
		tracks.addLast(track);
	}

	/**
	 * Pops a {@code Track} from the queue of tracks in FIFO order.
	 * <p>
	 * The method is thread-safe.
	 *
	 * @return the track at the front of the list
	 * @see #appendTrack(Track)
	 */
	public synchronized static Track popTrack() {
		if (tracks.isEmpty())
			return null;
		return tracks.removeFirst();
	}
}
