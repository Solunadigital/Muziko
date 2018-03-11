package com.muziko.dialogs;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.muziko.R;
import com.muziko.adapter.AddPlaylistAdapter;
import com.muziko.common.models.PlaylistItem;
import com.muziko.common.models.QueueItem;
import com.muziko.database.PlaylistRealmHelper;
import com.muziko.helpers.Utils;
import com.muziko.interfaces.BasicRecyclerItemListener;
import com.muziko.manager.AppController;
import com.muziko.mediaplayer.PlayerConstants;
import com.muziko.tasks.PlaylistAdder;

import java.util.ArrayList;

public class PlaylistAdd implements View.OnClickListener, CompoundButton.OnCheckedChangeListener, BasicRecyclerItemListener {
	private Context mContext;
	private ArrayList<PlaylistItem> playList;
	private AddPlaylistAdapter adapter;
	private RelativeLayout createNewButton;
	private boolean isBusy = false;
	private boolean isAdding = false;
	private boolean override = false;
	private boolean showOverride = false;
	private AlertDialog playlistDialog;

	public PlaylistAdd(boolean showOverride) {
		this.showOverride = showOverride;
	}

	public void open(Context context) {
		mContext = context;

		playList = new ArrayList<>();
        playList.addAll(PlaylistRealmHelper.loadAll());
        adapter = new AddPlaylistAdapter(mContext, playList, this);

		View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_add_to_playlist, null, false);
        CheckBox overridecheckbox = view.findViewById(R.id.overridecheckbox);
        overridecheckbox.setOnCheckedChangeListener(this);
		if (showOverride) {
			overridecheckbox.setVisibility(View.VISIBLE);
		} else {
			overridecheckbox.setVisibility(View.GONE);
		}
        RecyclerView recyclerView = view.findViewById(R.id.itemList);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
		recyclerView.setAdapter(adapter);

        createNewButton = view.findViewById(R.id.createNewButton);
        createNewButton.setOnClickListener(this);

		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mContext);
		dialogBuilder.setView(view);
		playlistDialog = dialogBuilder.create();
		playlistDialog.show();

//		playlistDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

		reload();
	}

	private void close() {

		if (playlistDialog != null) {
			playlistDialog.dismiss();
		}
	}

	@Override
	public void onClick(View v) {
		if (v == createNewButton) {
			create();
		}
	}

	private void create() {
		MaterialDialog createDialog = new MaterialDialog.Builder(mContext)
				.theme(Theme.LIGHT).titleColorRes(R.color.black)
				.negativeColorRes(R.color.dialog_negetive_button)
				.title(R.string.create_playlist)
				.positiveText(R.string.create)
				.negativeText(R.string.cancel_caps)
				.inputType(InputType.TYPE_CLASS_TEXT).input(mContext.getString(R.string.playlist_name), "", (dialog, input) -> {
					if (input.toString().length() > 0) {
						PlaylistItem item = new PlaylistItem();
                        item.id = Utils.randLong();
                        item.title = input.toString();
                        item.hash = Utils.generateSha1Hash(item.title + System.currentTimeMillis());
                        long id = PlaylistRealmHelper.insert(item);
                        if (id > 0) {

							Intent intent = new Intent(AppController.INTENT_PLAYLIST_CHANGED);
							intent.putExtra("id", id);
							mContext.sendBroadcast(intent);

							reload();
						} else {
                            AppController.toast(mContext, mContext.getString(R.string.unable_to_create_playlist));
                        }
					} else {
                        AppController.toast(mContext, mContext.getString(R.string.enter_name_playlist));
                    }
				}).build();

		createDialog.show();
	}

	private void reload() {

		playList.clear();
        playList.addAll(PlaylistRealmHelper.loadAll());
        adapter.notifyDataSetChanged();

	}

	private void addTo(Context context, PlaylistItem playlist) {
		ArrayList<QueueItem> items = new ArrayList<>(PlayerConstants.PLAYLIST_QUEUE);
		PlayerConstants.PLAYLIST_QUEUE.clear();

		PlaylistAdder taskAdd = new PlaylistAdder(context, playlist, items, override);
		taskAdd.execute();

//		Utils.toast(context, String.format("%d song%s saved in playlist", items.size(), items.size() != 1 ? "s" : ""));

		Intent intent = new Intent(AppController.INTENT_PLAYLIST_CHANGED);
		intent.putExtra("id", playlist.id);
		context.sendBroadcast(intent);

		close();
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

		override = isChecked;

	}

	@Override
	public void onItemClicked(int position) {

		PlaylistItem playlist = playList.get(position);
		if (playlist == null) return;

		addTo(mContext, playlist);
	}
}
