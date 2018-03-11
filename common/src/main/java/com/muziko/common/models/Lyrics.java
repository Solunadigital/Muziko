package com.muziko.common.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Keep;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;

@Keep
public class Lyrics implements Serializable, Parcelable {

    public static final int NO_RESULT = -2;
    public static final int NEGATIVE_RESULT = -1;
    public static final int POSITIVE_RESULT = 1;
    public static final int ERROR = -3;
    public static final int SEARCH_ITEM = 2;
    public static final Creator<Lyrics> CREATOR = new Creator<Lyrics>() {
        @Override
        public Lyrics createFromParcel(Parcel in) {
            return new Lyrics(in);
        }

        @Override
        public Lyrics[] newArray(int size) {
            return new Lyrics[size];
        }
    };
    private final int mFlag;
    private String mTitle;
    private String mArtist;
    private String mOriginalTitle;
    private String mOriginalArtist;
    private String mSourceUrl;
    private String mCoverURL;
    private String mLyrics;
    private String mSource;
    private boolean mLRC = false;

    public Lyrics(int flag) {
        this.mFlag = flag;
    }

    private Lyrics(Parcel in) {
        mTitle = in.readString();
        mArtist = in.readString();
        mOriginalTitle = in.readString();
        mOriginalArtist = in.readString();
        mSourceUrl = in.readString();
        mCoverURL = in.readString();
        mLyrics = in.readString();
        mSource = in.readString();
        mLRC = in.readByte() != 0;
        mFlag = in.readInt();
    }

    public static Lyrics fromBytes(byte[] data) throws IOException, ClassNotFoundException {
        if (data == null)
            return null;
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ObjectInputStream is = new ObjectInputStream(in);
        return (Lyrics) is.readObject();
    }

    public String getTrack() {
        return mTitle;
    }

    public void setTitle(String title) {
        this.mTitle = title;
    }

    public String getOriginalTrack() {
        if (mOriginalTitle != null)
            return mOriginalTitle;
        else
            return mTitle;
    }

    public void setOriginalTitle(String originalTitle) {
        this.mOriginalTitle = originalTitle;
    }

    public String getArtist() {
        return mArtist;
    }

    public void setArtist(String artist) {
        this.mArtist = artist;
    }

    public String getOriginalArtist() {
        if (mOriginalArtist != null)
            return mOriginalArtist;
        else
            return mArtist;
    }

    public void setOriginalArtist(String originalArtist) {
        this.mOriginalArtist = originalArtist;
    }

    public String getURL() {
        return mSourceUrl;
    }

    public void setURL(String uRL) {
        this.mSourceUrl = uRL;
    }

    public String getCoverURL() {
        return mCoverURL;
    }

    public void setCoverURL(String coverURL) {
        this.mCoverURL = coverURL;
    }

    public String getText() {
        return mLyrics;
    }

    public void setText(String lyrics) {
        this.mLyrics = lyrics;
    }

    public String getSource() {
        return mSource;
    }

    public void setSource(String mSource) {
        this.mSource = mSource;
    }

    public int getFlag() {
        return mFlag;
    }

    public boolean isLRC() {
        return this.mLRC;
    }

    public void setLRC(boolean LRC) {
        this.mLRC = LRC;
    }

    public byte[] toBytes() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ObjectOutput out = new ObjectOutputStream(bos);
            out.writeObject(this);
            out.close();
        } finally {
            bos.close();
        }
        return bos.toByteArray();
    }

    @Override
    public int hashCode() {
        // Potential issue with the Birthday Paradox when we hash over 50k lyrics
        return this.getURL() != null ? this.getURL().hashCode() :
                ("" + this.getOriginalArtist() + this.getOriginalTrack() + this.getSource()).hashCode();
    }

    @Override
    public boolean equals(Object object) {
        boolean isLyrics = object instanceof Lyrics;
        if (isLyrics && (this.getURL() != null) && ((Lyrics) object).getURL() != null)
            return this.getURL().equals(((Lyrics) object).getURL());
        else if (isLyrics) {
            Lyrics other = (Lyrics) object;
            boolean result = this.getText().equals(other.getText());
            result &= this.getFlag() == other.getFlag();
            result &= this.getSource().equals(other.getSource());
            result &= this.getArtist().equals(other.getArtist());
            result &= this.getTrack().equals(other.getTrack());
            return result;
        } else
            return false;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mTitle);
        dest.writeString(mArtist);
        dest.writeString(mOriginalTitle);
        dest.writeString(mOriginalArtist);
        dest.writeString(mSourceUrl);
        dest.writeString(mCoverURL);
        dest.writeString(mLyrics);
        dest.writeString(mSource);
        dest.writeByte((byte) (mLRC ? 1 : 0));
        dest.writeInt(mFlag);
    }

    public interface Callback {
        void onLyricsDownloaded(Lyrics lyrics);
    }
}
