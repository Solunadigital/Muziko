package com.muziko.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.muziko.R;
import com.muziko.common.models.QueueItem;
import com.muziko.common.models.SongModel;
import com.muziko.helpers.Utils;
import com.muziko.manager.ImageManager;

import java.util.ArrayList;

public class SearchListAdapter extends BaseAdapter implements Filterable {
    private final Context mContext;
    private final ArrayList<QueueItem> filteredList;
    private final ArrayList<QueueItem> selectedList;
    private ArrayList<QueueItem> originalList;
    private CustomFilter filter;

    public SearchListAdapter(Context context, ArrayList<QueueItem> listData, ArrayList<QueueItem> selected) {
        super();
        this.mContext = context;
        this.originalList = listData;
        this.filteredList = listData;

        this.selectedList = selected;
        getFilter();

    }

    @Override
    public Filter getFilter() {
        if (filter == null) {
            filter = new CustomFilter();
        }
        return filter;
    }

    @Override
    public int getCount() {
        return originalList.size();
    }

    @Override
    public Object getItem(int position) {
        return originalList.get(position);
    }

    @Override
    public long getItemId(int position) {
        //Unimplemented, because we aren't using Sqlite.
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final SongModel mDataList = (SongModel) this.getItem(position);

        final ViewHolder holder;

        holder = new ViewHolder();
        convertView = LayoutInflater.from(mContext).inflate(R.layout.element_search_song_list, parent, false);

        holder.songIcon = convertView.findViewById(R.id.songIcon);
        holder.songName = convertView.findViewById(R.id.songName);
        holder.artistName = convertView.findViewById(R.id.artistName);
        holder.dropDownButton = convertView.findViewById(R.id.menuDropLayout);
        holder.upLayout = convertView.findViewById(R.id.upLayout);
        holder.dropDownImage = convertView.findViewById(R.id.menuDrop);

        convertView.setTag(holder);


        if (mDataList.selected) {

            holder.dropDownImage.setImageResource(R.mipmap.ic_check_blue);

        } else {

            holder.dropDownImage.setImageResource(R.mipmap.ic_check_white);

        }

        String titleText = mDataList.title;
        String artistText = mDataList.artist_name;
        String url = mDataList.url;

        if (titleText.length() > 20) {
            titleText = titleText.substring(0, 20) + "...";
        }
        if (artistText.length() > 20) {
            artistText = artistText.substring(0, 20) + "...";
        }

        holder.songName.setText(titleText);
        holder.artistName.setText(artistText);

        ImageManager.Instance().loadImage(mDataList, holder.songIcon);

        convertView.setOnClickListener(v -> {

            selectedList.remove(originalList.get(position));

            if (originalList.get(position).selected) {
                originalList.get(position).selected = (false);
            } else {
                originalList.get(position).selected = (true);

                selectedList.add(originalList.get(position));
            }

            notifyDataSetChanged();
        });

        return convertView;
    }

    private static class ViewHolder {

        ImageView songIcon;
        TextView songName;
        TextView artistName;
        LinearLayout dropDownButton;
        ImageView dropDownImage;
        RelativeLayout upLayout;

    }


    private class CustomFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            constraint = constraint.toString().toLowerCase();
            FilterResults result = new FilterResults();
            if (constraint != null && constraint.toString().length() > 0) {
                ArrayList<SongModel> filterList = new ArrayList<>();

                String needle = constraint.toString().toLowerCase();

                // starts with
                ArrayList<Integer> already = new ArrayList<>();
                for (int i = 0; i < filteredList.size(); i++) {

                    if (filteredList.get(i).title.length() > 0 && Utils.searchStringStartsWith(needle, filteredList.get(i).title)) {
                        filterList.add(filteredList.get(i));
                        already.add(i);
                    } else if (filteredList.get(i).artist_name.length() > 0 && Utils.searchStringStartsWith(needle, filteredList.get(i).artist_name)) {
                        filterList.add(filteredList.get(i));
                        already.add(i);
                    }
                }
                // contains
                for (int i = 0; i < filteredList.size(); i++) {
                    for (int has : already) {
                        if (i == has) {
                        }
                    }
                    if (filteredList.get(i).title.length() > 0 && Utils.searchStringContains(needle, filteredList.get(i).title)) {
                        filterList.add(filteredList.get(i));
                    } else if (filteredList.get(i).artist_name.length() > 0 && Utils.searchStringContains(needle, filteredList.get(i).artist_name)) {
                        filterList.add(filteredList.get(i));
                    }
                }
                result.count = filterList.size();

                result.values = filterList;

            } else {
                result.count = filteredList.size();

                result.values = filteredList;
            }
            return result;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {

            originalList = (ArrayList<QueueItem>) results.values;
            notifyDataSetChanged();

        }
    }


}