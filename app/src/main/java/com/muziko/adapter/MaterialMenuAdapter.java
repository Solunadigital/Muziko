package com.muziko.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.internal.MDAdapter;
import com.muziko.R;
import com.muziko.objects.MenuObject;

import java.util.ArrayList;

/**
 * Created by Bradley on 3/02/2017.
 */


public class MaterialMenuAdapter extends RecyclerView.Adapter<MaterialMenuAdapter.SimpleListVH> implements MDAdapter {

	private MaterialDialog dialog;
	private ArrayList<MenuObject> items = new ArrayList<>();
	private Callback callback;

	public MaterialMenuAdapter(ArrayList<MenuObject> items, Callback callback) {
		this.items = items;
		this.callback = callback;
	}

	public void add(MenuObject item) {
		items.add(item);
		notifyItemInserted(items.size() - 1);
	}

	public void clear() {
		items.clear();
		notifyDataSetChanged();
	}

	public MenuObject getItem(int index) {
		return items.get(index);
	}

	@Override
	public void setDialog(MaterialDialog dialog) {
		this.dialog = dialog;
	}

	@Override
	public SimpleListVH onCreateViewHolder(ViewGroup parent, int viewType) {
		final View view = LayoutInflater.from(parent.getContext())
				.inflate(R.layout.adapter_dialog_menu, parent, false);
		return new SimpleListVH(view, this);
	}

	@Override
	public void onBindViewHolder(SimpleListVH holder, int position) {
		if (dialog != null) {
			final MenuObject item = items.get(position);
			if (item.getIcon() != 0) {
				holder.icon.setImageResource(item.getIcon());
			} else {
				holder.icon.setVisibility(View.GONE);
			}
			holder.title.setTextColor(dialog.getBuilder().getItemColor());
			holder.title.setText(item.title);
			dialog.setTypeface(holder.title, dialog.getBuilder().getRegularFont());
		}
	}

	@Override
	public int getItemCount() {
		return items.size();
	}

	public interface Callback {
		void onMenuObjectItemSelected(MaterialDialog dialog, int index, MenuObject item);
	}

	static class SimpleListVH extends RecyclerView.ViewHolder implements View.OnClickListener {

		final ImageView icon;
		final TextView title;
		final MaterialMenuAdapter adapter;

		SimpleListVH(View itemView, MaterialMenuAdapter adapter) {
			super(itemView);
            icon = itemView.findViewById(R.id.icon);
            title = itemView.findViewById(R.id.label);
            this.adapter = adapter;
			itemView.setOnClickListener(this);
		}

		@Override
		public void onClick(View view) {
			if (adapter.callback != null)
				adapter.callback.onMenuObjectItemSelected(adapter.dialog, getAdapterPosition(), adapter.getItem(getAdapterPosition()));
		}
	}
}