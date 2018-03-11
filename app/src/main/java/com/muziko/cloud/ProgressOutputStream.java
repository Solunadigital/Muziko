package com.muziko.cloud;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Bradley on 27/04/2017.
 */

public class ProgressOutputStream extends OutputStream {
    private OutputStream underlying;
    private long totalSize;
    private Listener listener;
    private long completed;

    public ProgressOutputStream(long totalSize, OutputStream underlying, Listener listener) {
        this.underlying = underlying;
        this.listener = listener;
        this.completed = 0;
        this.totalSize = totalSize;
    }

    @Override
    public void write(int c) throws IOException {
        this.underlying.write(c);
        track(1);
    }

    @Override
    public void write(byte[] data) throws IOException {
        this.underlying.write(data);
        track(data.length);
    }

    @Override
    public void write(byte[] data, int off, int len) throws IOException {
        this.underlying.write(data, off, len);
        track(len);
    }

    private void track(int len) {
        this.completed += len;
        this.listener.progress(this.completed, this.totalSize);
    }

    public interface Listener {
        void progress(long completed, long totalSize);
    }
}