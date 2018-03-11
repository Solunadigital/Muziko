package com.muziko.common.controls;

import com.muziko.common.models.QueueItem;
import com.muziko.common.models.SongModel;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Bradley on 3/03/2017.
 */

@SuppressWarnings("unchecked")
public class MuzikoArrayList<Q extends SongModel> extends ArrayList<QueueItem> {

    private onListChangedListener changeListener;

    public void forceRefresh() {
        if (changeListener != null) {
            changeListener.onQueueListChanged();
        }
    }

    public onListChangedListener getChangeListener() {
        return changeListener;
    }

    public void setChangeListener(onListChangedListener changeListener) {
        this.changeListener = changeListener;
    }

    @Override
    public QueueItem set(int index, QueueItem queueItem) {
        if (changeListener != null) {
            changeListener.onAdd(index, queueItem);
        }
        return super.set(index, queueItem);
    }

    @Override
    public boolean add(QueueItem queueItem) {
        if (changeListener != null) {
            changeListener.onAdd(this.size(), queueItem);
        }
        return super.add(queueItem);
    }

    @Override
    public void add(int index, QueueItem queueItem) {
        if (changeListener != null) {
            changeListener.onAdd(index, queueItem);
        }
        super.add(index, queueItem);
    }

    @Override
    public QueueItem remove(int index) {
        if (changeListener != null) {
            changeListener.onRemove(index);
        }
        return super.remove(index);
    }

    @Override
    public void clear() {
        if (changeListener != null) {
            changeListener.onClear();
        }
        super.clear();
    }

    @Override
    public boolean addAll(Collection<? extends QueueItem> c) {
        if (changeListener != null) {
            changeListener.onAddAll(this.size(), (ArrayList<QueueItem>) c);
        }
        return super.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends QueueItem> c) {
        if (changeListener != null) {
            changeListener.onAddAll(index, (ArrayList<QueueItem>) c);
        }
        return super.addAll(index, c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        if (changeListener != null) {
            changeListener.onClear();
        }
        return super.removeAll(c);
    }

    public QueueItem setNotification(int index, QueueItem queueItem) {

        return super.set(index, queueItem);
    }

    public interface onListChangedListener {

        void onQueueListChanged();

        void onRemove(int index);

        void onAdd(int index, QueueItem queueItem);

        void onAddAll(int index, ArrayList<QueueItem> queueItems);

        void onClear();
    }
}
