package com.muziko.controls;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.SearchView;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.muziko.R;
import com.muziko.helpers.Utils;

/**
 * Created by Bradley on 14/02/2017.
 */

public class AdvancedSearchButton {

	private int padding = 4;

	public ImageButton addButton(Context context, Resources resources, SearchView searchView) {
		ImageButton advancedSearch;
		LinearLayout linearLayoutOfSearchView = (LinearLayout) searchView.getChildAt(0);
		advancedSearch = new ImageButton(context);
		advancedSearch.setImageResource(R.drawable.ic_youtube_searched_for_white_24dp);
		int pixelPadding = Utils.toPixels(resources, padding);
		advancedSearch.setPadding(pixelPadding, pixelPadding, pixelPadding, pixelPadding);
		int[] attrs = new int[]{R.attr.selectableItemBackgroundBorderless};
		TypedArray typedArray = context.obtainStyledAttributes(attrs);
		int backgroundResource = typedArray.getResourceId(0, 0);
		advancedSearch.setBackgroundResource(backgroundResource);
		typedArray.recycle();
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayoutCompat.LayoutParams.WRAP_CONTENT, LinearLayoutCompat.LayoutParams.WRAP_CONTENT);
		params.gravity = Gravity.CENTER_VERTICAL;
		advancedSearch.setLayoutParams(params);
		linearLayoutOfSearchView.addView(advancedSearch, 0);
		advancedSearch.setVisibility(View.GONE);

		return advancedSearch;
	}
}
