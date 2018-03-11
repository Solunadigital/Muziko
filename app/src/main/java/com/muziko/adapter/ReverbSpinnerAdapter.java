package com.muziko.adapter;

import android.app.Activity;
import android.media.audiofx.PresetReverb;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.muziko.R;
import com.muziko.common.models.ReverbItem;

import java.util.ArrayList;
import java.util.List;

public class ReverbSpinnerAdapter extends BaseAdapter {
	private final Activity ctx;
	private List<ReverbItem> mItems = new ArrayList<>();

	public ReverbSpinnerAdapter(Activity ctx, List<ReverbItem> items) {
		this.mItems = items;
		this.ctx = ctx;
	}

	public void clear() {
		mItems.clear();
	}

	@Override
	public int getCount() {
		return mItems.size();
	}

	@Override
	public Object getItem(int position) {
		if (position >= 0 && position < getCount())
			return mItems.get(position);
		else
			return null;
	}

	@Override
	public long getItemId(int position) {
		if (position >= 0 && position < getCount())
			return mItems.get(position).id;
		else
			return PresetReverb.PRESET_NONE;
	}

	@Override
    public View getView(int position, View view, ViewGroup parent) {
        if (view == null || !view.getTag().toString().equals("NON_DROPDOWN")) {
            view = this.ctx.getLayoutInflater().inflate(R.layout.spinner_item_actionbar, parent, false);
            view.setTag("NON_DROPDOWN");
        }
        TextView textView = view.findViewById(android.R.id.text1);
        textView.setText(getTitle(position));
		return view;
	}

	@Override
    public View getDropDownView(int position, View view, ViewGroup parent) {
        if (view == null || !view.getTag().toString().equals("DROPDOWN")) {
            view = this.ctx.getLayoutInflater().inflate(R.layout.spinner_item_dropdown, parent, false);
            view.setTag("DROPDOWN");
        }

        TextView textView = view.findViewById(android.R.id.text1);
        textView.setText(getTitle(position));

        return view;
	}

	private String getTitle(int position) {
		ReverbItem item = (ReverbItem) getItem(position);
		if (item == null)
			return "";
		else
			return item.title;
	}
}