package com.muziko.events;

/** Created by Bradley on 16/03/2017. */
public class AudioEvent {
    private boolean musicPlaying;

    public boolean isMusicPlaying() {
        return musicPlaying;
    }

    public void setMusicPlaying(boolean musicPlaying) {
        this.musicPlaying = musicPlaying;
    }

    public AudioEvent(boolean musicPlaying) {
        this.musicPlaying = musicPlaying;
    }
}
