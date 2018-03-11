package com.muziko.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.muziko.R;
import com.muziko.common.models.SettingModel;
import com.muziko.controls.MuzikoSwitch;
import com.muziko.helpers.StorageUtils;
import com.muziko.interfaces.SettingsRecyclerItemListener;
import com.muziko.manager.SettingsManager;

import java.util.ArrayList;
import java.util.List;

import static android.view.View.GONE;

/**
 * Created by dev on 27/08/2016.
 */

public class SettingsAdapter extends SelectableAdapter<SettingsAdapter.AdapterQueueHolder> {

    private final SettingsRecyclerItemListener listener;
    private final Context mContext;
    private final ArrayList<SettingModel> items;
    private SharedPreferences prefs;

    public SettingsAdapter(Context context, ArrayList<SettingModel> listData, SettingsRecyclerItemListener listener) {
        super();
        this.mContext = context;
        this.items = listData;
        this.listener = listener;
        prefs = SettingsManager.Instance().getPrefs();
    }


    @Override
    public AdapterQueueHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        view = LayoutInflater.from(mContext).inflate(R.layout.adapter_settings_item, parent, false);
        return new AdapterQueueHolder(mContext, view, listener);
    }

    @Override
    public void onBindViewHolder(final AdapterQueueHolder holder, int position) {

        boolean prefHideUnknown = prefs.getBoolean(SettingsManager.prefHideUnknown, false);
        boolean prefStoragePerms = prefs.getBoolean(SettingsManager.prefStoragePerms, false);
        boolean prefShowArtwork = prefs.getBoolean(SettingsManager.prefShowArtwork, false);
        boolean prefArtworkDownload = prefs.getBoolean(SettingsManager.prefArtworkDownload, false);
        boolean prefArtworkDownloadWifi = prefs.getBoolean(SettingsManager.prefArtworkDownloadWifi, false);
        boolean prefShake = prefs.getBoolean(SettingsManager.prefShake, false);
        boolean prefArtworkLock = prefs.getBoolean(SettingsManager.prefArtworkLock, false);
        boolean prefGapless = prefs.getBoolean(SettingsManager.prefGapless, false);
        boolean prefScrobbling = prefs.getBoolean(SettingsManager.prefScrobbling, false);
        boolean prefHeadset = prefs.getBoolean(SettingsManager.prefHeadset, false);
        boolean prefBluetooth = prefs.getBoolean(SettingsManager.prefBluetooth, false);
        boolean prefLockScreen = prefs.getBoolean(SettingsManager.prefLockScreen, false);
        boolean prefLyricsDownload = prefs.getBoolean(SettingsManager.prefLyricsDownload, false);

        boolean prefDownloadCloudWhenStreaming = prefs.getBoolean(SettingsManager.prefDownloadCloudWhenStreaming, false);
        boolean prefShowStreamDataWarning = prefs.getBoolean(SettingsManager.prefShowStreamDataWarning, false);
        boolean preAutoSyncLibrary = prefs.getBoolean(SettingsManager.prefAutoSyncLibrary, false);
        boolean preUpdateLibraryOnlyWifi = prefs.getBoolean(SettingsManager.prefUpdateLibraryOnlyWifi, false);
        boolean preSyncPlaylist = prefs.getBoolean(SettingsManager.prefSyncPlaylist, false);
        boolean preSyncFavourites = prefs.getBoolean(SettingsManager.prefSyncFavourites, false);

//		boolean prefSmartPause = prefs.getBoolean("prefSmartPause", false);
//		boolean prefSmartResume = prefs.getBoolean("prefSmartResume", false);
//		int prefShakeThreshold = prefs.getInt("prefShakeThreshold", 1);

        final SettingModel item = this.getItem(position);
        if (item != null) {

            holder.hiddenOverlay.setVisibility(View.GONE);
            holder.settinglayout.setClickable(true);
            holder.settingModel = item;
            holder.settingtitle.setText(item.title);
            holder.settingdesc.setText(item.description);


            if (item.type != 1) {
                holder.settingswitch.setVisibility(GONE);
            } else {
                holder.settingswitch.setVisibility(View.VISIBLE);
            }

            switch (item.key) {
                case SettingsManager.prefHideUnknown:
                    if (prefHideUnknown) {
                        holder.settingswitch.setCheckedSilent(true);
                    } else {
                        holder.settingswitch.setCheckedSilent(false);
                    }
                    break;
                case SettingsManager.prefLyricsDownload:
                    if (prefLyricsDownload) {
                        holder.settingswitch.setCheckedSilent(true);
                    } else {
                        holder.settingswitch.setCheckedSilent(false);
                    }
                    break;
                case SettingsManager.prefStoragePerms:
                    if (prefStoragePerms) {
                        holder.settingswitch.setCheckedSilent(true);
                    } else {
                        holder.settingswitch.setCheckedSilent(false);
                    }
                    break;
                case SettingsManager.prefShowArtwork:
                    if (prefShowArtwork) {
                        holder.settingswitch.setCheckedSilent(true);
                    } else {
                        holder.settingswitch.setCheckedSilent(false);
                    }
                    break;
                case SettingsManager.prefArtworkDownload:
                    if (prefArtworkDownload) {
                        holder.settingswitch.setCheckedSilent(true);
                    } else {
                        holder.settingswitch.setCheckedSilent(false);
                    }
                    break;
                case SettingsManager.prefArtworkDownloadWifi:
                    if (prefArtworkDownloadWifi) {
                        holder.settingswitch.setCheckedSilent(true);
                    } else {
                        holder.settingswitch.setCheckedSilent(false);
                    }
                    break;
                case SettingsManager.prefArtworkLock:
                    if (prefArtworkLock) {
                        holder.settingswitch.setCheckedSilent(true);
                    } else {
                        holder.settingswitch.setCheckedSilent(false);
                    }
                    break;
                case SettingsManager.prefShake:
                    if (prefShake) {
                        holder.settingswitch.setCheckedSilent(true);
                    } else {
                        holder.settingswitch.setCheckedSilent(false);
                    }
                    break;
                case SettingsManager.prefGapless:
                    if (prefGapless) {
                        holder.settingswitch.setCheckedSilent(true);
                    } else {
                        holder.settingswitch.setCheckedSilent(false);
                    }
                    break;

                case SettingsManager.prefScrobbling:
                    if (prefScrobbling) {
                        holder.settingswitch.setCheckedSilent(true);
                    } else {
                        holder.settingswitch.setCheckedSilent(false);
                    }
                    break;

                case SettingsManager.prefHeadset:
                    if (prefHeadset) {
                        holder.settingswitch.setCheckedSilent(true);
                    } else {
                        holder.settingswitch.setCheckedSilent(false);
                    }
                    break;

                case SettingsManager.prefBluetooth:
                    if (prefBluetooth) {
                        holder.settingswitch.setCheckedSilent(true);
                    } else {
                        holder.settingswitch.setCheckedSilent(false);
                    }
                    break;

                case SettingsManager.prefLockScreen:
                    if (prefLockScreen) {
                        holder.settingswitch.setCheckedSilent(true);
                    } else {
                        holder.settingswitch.setCheckedSilent(false);
                    }
                    break;

                case SettingsManager.prefDownloadCloudWhenStreaming:
                    if (prefDownloadCloudWhenStreaming) {
                        holder.settingswitch.setCheckedSilent(true);
                    } else {
                        holder.settingswitch.setCheckedSilent(false);
                    }
                    break;

                case SettingsManager.prefShowStreamDataWarning:
                    if (prefShowStreamDataWarning) {
                        holder.settingswitch.setCheckedSilent(true);
                    } else {
                        holder.settingswitch.setCheckedSilent(false);
                    }
                    break;

                case SettingsManager.prefAutoSyncLibrary:
                    if (preAutoSyncLibrary) {
                        holder.settingswitch.setCheckedSilent(true);
                    } else {
                        holder.settingswitch.setCheckedSilent(false);
                    }
                    break;

                case SettingsManager.prefUpdateLibraryOnlyWifi:
                    if (preUpdateLibraryOnlyWifi) {
                        holder.settingswitch.setCheckedSilent(true);
                    } else {
                        holder.settingswitch.setCheckedSilent(false);
                    }
                    break;
                case SettingsManager.prefSyncPlaylist:
                    if (preSyncPlaylist) {
                        holder.settingswitch.setCheckedSilent(true);
                    } else {
                        holder.settingswitch.setCheckedSilent(false);
                    }
                    break;

                case SettingsManager.prefSyncFavourites:
                    if (preSyncFavourites) {
                        holder.settingswitch.setCheckedSilent(true);
                    } else {
                        holder.settingswitch.setCheckedSilent(false);
                    }
                    break;

//				case "prefSmartPause":
//					if (prefSmartPause) {
//						holder.settingswitch.setCheckedSilent(true);
//					} else {
//						holder.settingswitch.setCheckedSilent(false);
//					}
//					break;
//
//				case "prefSmartResume":
//					if (prefSmartResume) {
//						holder.settingswitch.setCheckedSilent(true);
//					} else {
//						holder.settingswitch.setCheckedSilent(false);
//					}
//					break;

                case SettingsManager.prefShakeThreshold:
                    if (prefShake) {
                        holder.settinglayout.setClickable(true);
                    } else {
                        holder.settinglayout.setClickable(false);
                        holder.settingdesc.setText(R.string.disabled);
                    }
                    break;

                case SettingsManager.prefSyncLocation:
                    List<StorageUtils.StorageInfo> storageInfoList = StorageUtils.getStorageList();
                    holder.hiddenOverlay.setVisibility(storageInfoList.size() == 1 ? View.VISIBLE : GONE);
                    break;

            }

        }
    }

    @Override
    public int getItemViewType(int position) {

        return 0;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private SettingModel getItem(int position) {
        if (position >= 0 && position < items.size())
            return items.get(position);
        else
            return null;
    }

    public void removeIndex(int position) {
        if (position >= 0 && position < items.size()) {
            items.remove(position);
            notifyItemRemoved(position);
        }

    }


    public static class AdapterQueueHolder extends RecyclerView.ViewHolder implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
        final FrameLayout settinglayout;
        final TextView settingtitle;
        final TextView settingdesc;
        final MuzikoSwitch settingswitch;
        final View hiddenOverlay;
        final SettingsRecyclerItemListener listener;
        private final Context context;
        SettingModel settingModel;

        public AdapterQueueHolder(Context context, final View view, final SettingsRecyclerItemListener listener) {
            super(view);

            this.context = context;
            this.listener = listener;

            settinglayout = view.findViewById(R.id.settinglayout);
            settingtitle = view.findViewById(R.id.settingtitle);
            settingdesc = view.findViewById(R.id.settingdesc);
            settingswitch = view.findViewById(R.id.settingswitch);
            hiddenOverlay = view.findViewById(R.id.hiddenOverlay);

            settingswitch.setOnCheckedChangeListener(this);
            settinglayout.setOnClickListener(this);

        }

        @Override
        public void onClick(View view) {
            if (listener != null) {

                listener.onItemClicked(context, settingModel);
            }
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (listener != null) {


                listener.onItemChecked(settingModel);
            }
        }
    }
}