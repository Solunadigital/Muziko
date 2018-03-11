package com.muziko.cloud;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Bradley on 27/04/2017.
 */


public class ProgressInputStream extends InputStream {
    private InputStream wrappedInputStream;
    private long size;
    private Object tag;
    private long counter;
    private long lastPercent;
    private OnProgressListener listener;

    public ProgressInputStream(InputStream in, long size, Object tag, OnProgressListener listener) {
        wrappedInputStream = in;
        this.listener = listener;
        this.size = size;
        this.tag = tag;
    }


    public void setOnProgressListener(OnProgressListener listener) {
        this.listener = listener;
    }

    public Object getTag() {
        return tag;
    }

    @Override
    public int read() throws IOException {
        counter += 1;
        check();
        return wrappedInputStream.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        int retVal = wrappedInputStream.read(b);
        counter += retVal;
        check();
        return retVal;
    }

    @Override
    public int read(byte[] b, int offset, int length) throws IOException {
        int retVal = wrappedInputStream.read(b, offset, length);
        counter += retVal;
        check();
        return retVal;
    }

    @Override
    public long skip(long n) throws IOException {
        return wrappedInputStream.skip(n);
    }

    @Override
    public int available() throws IOException {
        return wrappedInputStream.available();
    }

    @Override
    public void close() throws IOException {
        wrappedInputStream.close();
    }

    @Override
    public void mark(int readlimit) {
        wrappedInputStream.mark(readlimit);
    }

    @Override
    public synchronized void reset() throws IOException {
        wrappedInputStream.reset();
    }

    @Override
    public boolean markSupported() {
        return wrappedInputStream.markSupported();
    }

    private void check() {
        int percent = (int) (counter * 100 / size);
        if (percent - lastPercent >= 10) {
            lastPercent = percent;
            this.listener.onProgress(percent, tag);
        }
    }

    /**
     * Interface for classes that want to monitor this input stream
     */
    public interface OnProgressListener {
        void onProgress(int percentage, Object tag);
    }
}

