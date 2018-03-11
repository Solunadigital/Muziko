package com.muziko.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.muziko.R;
import com.muziko.common.controls.MuzikoArrayList;
import com.muziko.common.models.QueueItem;
import com.muziko.controls.FastScroller.FastScrollRecyclerView;
import com.muziko.helpers.Utils;
import com.muziko.interfaces.RecyclerItemListener;
import com.muziko.mediaplayer.PlayerConstants;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.muziko.MyApplication.networkState;


public class QueueAdapter extends SelectableAdapter<QueueAdapter.AdapterQueueHolder> implements FastScrollRecyclerView.SectionedAdapter {
    private final Context mContext;
    private final ArrayList<Integer> pendingDismissList = new ArrayList<>();
    private final RecyclerItemListener listener;
    private String lastSectionName = "A";
    private int gridtype = 0;
    private MuzikoArrayList<QueueItem> items;
    private boolean pendingDimiss = false;

    public QueueAdapter(Context context, MuzikoArrayList<QueueItem> listData, RecyclerItemListener listener) {
        super();
        this.mContext = context;
        this.items = listData;
        this.listener = listener;
        setHasStableIds(true);
    }


    public void setPendingDimiss(boolean pendingdismiss, int position) {
        if (pendingdismiss) {
            pendingDismissList.add(position);
        } else {
            for (int i = 0; i < pendingDismissList.size(); i++) {
                if (pendingDismissList.get(i) == position) {
                    pendingDismissList.remove(i);
                }

            }

        }
    }

    public void updateProgress(int position, int progress, int duration) {

        for (int i = 0; i < pendingDismissList.size(); i++) {
            if (pendingDismissList.get(i) == position) {
                return;
            }
        }
        notifyItemChanged(position, new Integer(progress));
    }

    @Override
    public AdapterQueueHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        view = LayoutInflater.from(mContext).inflate(R.layout.adapter_queue_item, parent, false);
        return new AdapterQueueHolder(mContext, view, listener);
    }

    @Override
    public void onBindViewHolder(final AdapterQueueHolder holder, int position) {

        pendingDimiss = false;

        final QueueItem item = this.getItem(position);
        if (item != null) {

            for (int i = 0; i < pendingDismissList.size(); i++) {
                if (pendingDismissList.get(i) == position) {
                    pendingDimiss = true;
                }
            }

            if (pendingDimiss) {
                holder.layoutSwipe.setVisibility(View.VISIBLE);
                holder.layoutMain.setVisibility(View.GONE);
            } else {
                holder.layoutSwipe.setVisibility(View.GONE);
                holder.layoutMain.setVisibility(View.VISIBLE);

                if (position != PlayerConstants.QUEUE_INDEX) {
                    holder.textTitle.setTypeface(null, Typeface.NORMAL);
                    holder.textDuration.setTypeface(null, Typeface.NORMAL);
                    holder.textDuration.setText(Utils.getDuration(Integer.valueOf(item.duration)));
                    holder.playingOverlay.setVisibility(View.GONE);
                } else {
                    holder.textTitle.setTypeface(null, Typeface.BOLD);
                    holder.textDuration.setTypeface(null, Typeface.BOLD);
                    holder.textDuration.setText(Utils.getDuration(Integer.valueOf(item.duration)));
                    holder.playingOverlay.setVisibility(View.VISIBLE);
                }

                if (item.removeafter != 0) {
                    holder.removeAfter.setVisibility(View.VISIBLE);
                    int left = item.removeafter - item.played;
                    String remaining = String.valueOf(left);
                    holder.removeAfter.setText(remaining);
                } else {
                    holder.removeAfter.setVisibility(View.GONE);
                }
                holder.textTitle.setText(item.title);
                holder.offlineOverlay.setVisibility(View.GONE);
                holder.viewOverlay.setVisibility(isSelected(position) ? View.VISIBLE : View.GONE);
                holder.layoutMenu.setVisibility(isMultiSelect() ? View.GONE : View.VISIBLE);
                holder.imageGrabber.setVisibility(isMultiSelect() ? View.GONE : View.VISIBLE);

                if (item.storage != 0 && item.storage != 1 && item.storage != 2 && networkState != NetworkInfo.State.CONNECTED) {
                    holder.offlineOverlay.setVisibility(View.VISIBLE);
                }
            }

        }
    }

    @Override
    public void onBindViewHolder(final AdapterQueueHolder holder, int position, List<Object> payloads) {
        if (!payloads.isEmpty()) {
            final QueueItem item = this.getItem(position);
            if (position != PlayerConstants.QUEUE_INDEX) {
                holder.textTitle.setTypeface(null, Typeface.NORMAL);
                holder.textDuration.setTypeface(null, Typeface.NORMAL);
                holder.textDuration.setText(Utils.getDuration(Integer.valueOf(item.duration)));
                holder.playingOverlay.setVisibility(View.GONE);
            } else {
                holder.textTitle.setTypeface(null, Typeface.BOLD);
                holder.textDuration.setTypeface(null, Typeface.BOLD);
                holder.textDuration.setText(Utils.getDuration(Integer.valueOf(item.duration)));
                holder.playingOverlay.setVisibility(View.VISIBLE);
            }
        }
        super.onBindViewHolder(holder, position, payloads);
    }

    @Override
    public int getItemViewType(int position) {

        return 0;
    }

    @Override
    public long getItemId(int position) {
//		return items.get(position).id;
        return items.get(position).hashCode();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void notifyRemoveEach() {
        for (int i = 0; i < items.size(); i++) {
            notifyItemRemoved(i);
        }
    }

    public void notifyAddEach() {
        for (int i = 0; i < items.size(); i++) {
            notifyItemInserted(i);
        }
    }

    public QueueItem getItem(int position) {
        if (position >= 0 && position < items.size())
            return items.get(position);
        else
            return null;
    }

    public int getItemPosition(QueueItem queueItem) {
        return items.indexOf(queueItem);
    }

    public void clearSelectedItems() {

        clearSelection();
    }

    public ArrayList<QueueItem> getSelectedItems() {
        ArrayList<QueueItem> selection = new ArrayList<>();

        List<Integer> indexes = getSelectedIndexes();
        for (Integer intr : indexes) {
            selection.add(getItem(intr));
        }
        indexes.clear();

        return selection;
    }


    public boolean moveTo(int from, int to) {
        boolean ret = false;
        try {
            if (!items.isEmpty()) {
                Collections.swap(items, from, to);
                notifyItemMoved(from, to);
                ret = true;
            }
        } catch (Exception ex) {
            Crashlytics.logException(ex);
        }

        return ret;
    }

    public void removeIndex(int position) {
        if (position >= 0 && position < items.size()) {
            items.remove(position);
            notifyItemRemoved(position);
        }

    }

    public MuzikoArrayList<QueueItem> getList() {
        return items;
    }

    public void add(Collection<QueueItem> list) {
        items.clear();
        items.addAll(list);
    }

    public void removeAll(ArrayList<QueueItem> del) {
        items.removeAll(del);
        notifyDataSetChanged();
    }

    public void reset() {
        items.clear();
        notifyDataSetChanged();
    }

    public void set(QueueItem item) {
        if (item != null) {
            for (int i = 0; i < getItemCount(); i++) {
                if (items.get(i) != null && items.get(i).data != null && items.get(i).data.equals(item.data)) {
                    items.set(i, item);
                    notifyItemChanged(i);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public String getSectionName(int position) {

        String s = items.get(position).title;
        if (s == null) {
            return lastSectionName;
        }
        if (s.length() == 0) {
            return lastSectionName;
        }
        boolean hasNonAlpha = s.matches("^.*[^a-zA-Z0-9 ].*$");

        if (hasNonAlpha) {
            return lastSectionName;
        } else {
            lastSectionName = s.substring(0, 1).toUpperCase();
            return s.substring(0, 1).toUpperCase();
        }
    }

    public static class AdapterQueueHolder extends RecyclerView.ViewHolder implements View.OnTouchListener {
        final RelativeLayout layoutMain;
        final LinearLayout layoutSwipe;
        final LinearLayout layoutMenu;
        final View viewOverlay;
        final View playingOverlay;
        final View offlineOverlay;
        final TextView textTitle;
        final TextView textDuration;
        final ImageView imageGrabber;
        final ImageView imageMenu;
        final TextView removeAfter;
        final RecyclerItemListener listener;
        private final Context context;

        public AdapterQueueHolder(Context context, final View view, final RecyclerItemListener listener) {
            super(view);

            this.context = context;
            this.listener = listener;

            layoutMain = view.findViewById(R.id.layoutMain);
            layoutSwipe = view.findViewById(R.id.layoutSwipe);
            layoutMenu = view.findViewById(R.id.layoutMenu);
            viewOverlay = view.findViewById(R.id.viewOverlay);
            playingOverlay = view.findViewById(R.id.playingOverlay);
            offlineOverlay = view.findViewById(R.id.offlineOverlay);

            textTitle = view.findViewById(R.id.textTitle);
            textDuration = view.findViewById(R.id.textDuration);
            removeAfter = view.findViewById(R.id.removeAfter);

            imageGrabber = view.findViewById(R.id.imageGrabber);
            imageMenu = view.findViewById(R.id.imageMenu);
            if (imageGrabber != null) {
                imageGrabber.setOnTouchListener(this);
            }


        }

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                if (listener != null) {
                    listener.onDragTouched(this);
                }
            }
            return false;
        }

    }

}
