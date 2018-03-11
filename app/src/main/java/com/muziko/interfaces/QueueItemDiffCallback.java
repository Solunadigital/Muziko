package com.muziko.interfaces;

import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;

import com.muziko.common.models.QueueItem;

import java.util.ArrayList;

/**
 * Created by Bradley on 25/03/2017.
 */
public class QueueItemDiffCallback extends DiffUtil.Callback {

    private final ArrayList<QueueItem> oldList;
    private final ArrayList<QueueItem> newList;

    public QueueItemDiffCallback(ArrayList<QueueItem> oldList, ArrayList<QueueItem> newList) {
        this.oldList = oldList;
        this.newList = newList;
    }

    @Override
    public int getOldListSize() {
        return oldList.size();
    }

    @Override
    public int getNewListSize() {
        return newList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return oldList.get(oldItemPosition).getData().equalsIgnoreCase(newList.get(newItemPosition).getData());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        final QueueItem oldItem = oldList.get(oldItemPosition);
        final QueueItem newItem = newList.get(newItemPosition);

        return oldItem.equals(newItem);
    }

    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        // Implement method if you're going to use ItemAnimator
        return super.getChangePayload(oldItemPosition, newItemPosition);
    }
}
