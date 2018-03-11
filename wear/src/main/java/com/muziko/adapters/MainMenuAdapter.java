package com.muziko.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.wearable.view.WearableRecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.muziko.R;
import com.muziko.models.MainMenuModel;

import java.util.ArrayList;

/**
 * Created by Bradley on 9/03/2017.
 */

public class MainMenuAdapter extends WearableRecyclerView.Adapter<MainMenuAdapter.ViewHolder> {

    private ArrayList<MainMenuModel> data;
    private Context context;
    private ItemSelectedListener itemSelectedListener;

    public MainMenuAdapter(Context context, ArrayList<MainMenuModel> data) {
        this.context = context;
        this.data = data;
    }

    public void setListener(ItemSelectedListener itemSelectedListener) {
        this.itemSelectedListener = itemSelectedListener;
    }

    @Override
    public MainMenuAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_mainmenu_item, parent, false));
    }

    @Override
    public void onBindViewHolder(MainMenuAdapter.ViewHolder holder, final int position) {
        if (data != null && !data.isEmpty()) {
            holder.textView.setText(data.get(position).getTitle());
            holder.imageView.setImageResource(data.get(position).getImage());
            holder.bind(position, itemSelectedListener);
        }
    }

    @Override
    public int getItemCount() {
        if (data != null && !data.isEmpty()) {
            return data.size();
        }
        return 0;
    }

    public interface ItemSelectedListener {
        void onItemSelected(int position);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView textView;
        private ImageView imageView;

        ViewHolder(View view) {
            super(view);
            textView = (TextView) view.findViewById(R.id.text_item);
            imageView = (ImageView) view.findViewById(R.id.item_image);
        }

        void bind(final int position, final ItemSelectedListener listener) {

            itemView.setOnClickListener(view -> {
                if (listener != null) {
                    listener.onItemSelected(position);
                }
            });
        }
    }
}
