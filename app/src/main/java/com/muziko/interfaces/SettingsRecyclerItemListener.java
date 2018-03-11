package com.muziko.interfaces;

import android.content.Context;

import com.muziko.common.models.SettingModel;

/**
 * Created by dev on 27/08/2016.
 */
public interface SettingsRecyclerItemListener {

	void onItemClicked(Context context, SettingModel settingModel);

	void onItemChecked(SettingModel settingModel);
}