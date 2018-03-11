package com.muziko.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.muziko.R;
import com.muziko.models.EqualizerItem;

import java.util.ArrayList;
import java.util.List;

public class EqualizerSpinnerAdapter extends BaseAdapter {
	private final Activity ctx;
	private List<EqualizerItem> mItems = new ArrayList<>();

	public EqualizerSpinnerAdapter(Activity ctx, List<EqualizerItem> mItems) {
		this.mItems = mItems;
		this.ctx = ctx;
	}

	public void clear() {
		mItems.clear();
	}

	public void addItem(EqualizerItem yourObject) {
		mItems.add(yourObject);
	}

	public void addItems(List<EqualizerItem> yourObjectList) {
		mItems.addAll(yourObjectList);
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
		return position;
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
		EqualizerItem item = (EqualizerItem) getItem(position);
		if (item == null)
			return "";
		else
			return item.title;
	}
}