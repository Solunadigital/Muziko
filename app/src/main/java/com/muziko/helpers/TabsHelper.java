package com.muziko.helpers;

import android.content.Context;

import com.muziko.R;
import com.muziko.common.models.TabModel;
import com.muziko.database.TabRealmHelper;

import java.util.ArrayList;

/**
 * Created by dev on 27/08/2016.
 */
public class TabsHelper {

    public void saveInitalTabLayout(Context context) {
        ArrayList<TabModel> tabModels = getTabs(context);
        TabRealmHelper.saveTabs(tabModels);

	}

    public ArrayList<TabModel> getTabs(Context context) {
        ArrayList<TabModel> items = new ArrayList<>();

		TabModel tabModel = new TabModel();
		tabModel.title = context.getString(R.string.home);
		tabModel.show = true;
		tabModel.order = 0;
		items.add(tabModel);

		tabModel = new TabModel();
		tabModel.title = context.getString(R.string.tracks);
		tabModel.show = true;
		tabModel.order = 1;
		items.add(tabModel);

		tabModel = new TabModel();
        tabModel.title = context.getString(R.string.artists);
        tabModel.show = true;
		tabModel.order = 2;
		items.add(tabModel);


		tabModel = new TabModel();
        tabModel.title = context.getString(R.string.albums);
        tabModel.show = true;
		tabModel.order = 3;
		items.add(tabModel);


		tabModel = new TabModel();
        tabModel.title = context.getString(R.string.genres);
        tabModel.show = true;
		tabModel.order = 4;
		items.add(tabModel);


		return items;
	}
}