package com.muziko.dialogs;

import android.content.Context;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.muziko.R;
import com.muziko.adapter.PlaylistMergeAdapter;
import com.muziko.common.models.PlaylistItem;
import com.muziko.common.models.QueueItem;
import com.muziko.database.PlaylistRealmHelper;
import com.muziko.database.PlaylistSongRealmHelper;
import com.muziko.helpers.Utils;
import com.muziko.interfaces.RecyclerItemListener;
import com.muziko.manager.AppController;
import com.muziko.tasks.PlaylistAdder;
import com.muziko.tasks.PlaylistDelete;

import java.util.ArrayList;

/**
 * Created by dev on 22/08/2016.
 */
public class PlaylistMerge implements View.OnClickListener, CompoundButton.OnCheckedChangeListener, RecyclerItemListener {
    private final MergePlaylistItemTouchHelper touchCallback = new MergePlaylistItemTouchHelper();
    private final ItemTouchHelper touchHelper = new ItemTouchHelper(touchCallback);
    private Context context;
    private ArrayList<PlaylistItem> playList = new ArrayList<>();
    private PlaylistMergeAdapter adapter;
    private RelativeLayout createNewButton;
    private MaterialDialog createDialog = null;
    private boolean dragged;
    private boolean isBusy = false;
    private boolean isAdding = false;
    private boolean removeExisting = false;

    public PlaylistMerge(ArrayList<PlaylistItem> playlistItems) {
        playList = playlistItems;
    }

    public void open(Context context) {
        this.context = context;

        adapter = new PlaylistMergeAdapter(context, playList, this);

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_merge_playlist, null, false);
        ProgressBar progressBar = view.findViewById(R.id.progressBar);
        CheckBox removecheckbox = view.findViewById(R.id.removecheckbox);
        removecheckbox.setOnCheckedChangeListener(this);

        LinearLayoutManager layoutList = new LinearLayoutManager(context);
        RecyclerView recyclerView = view.findViewById(R.id.itemList);
        recyclerView.setLayoutManager(layoutList);
        recyclerView.setAdapter(adapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setHasFixedSize(true);
        touchHelper.attachToRecyclerView(recyclerView);

        createNewButton = view.findViewById(R.id.createNewButton);
        createNewButton.setOnClickListener(this);

        createDialog = new MaterialDialog.Builder(context)
                .theme(Theme.LIGHT).titleColorRes(R.color.normal_blue)
                .negativeColorRes(R.color.dialog_negetive_button)
                .title("Merge Playlist")
                .customView(view, false)
                .build();

        createDialog.show();
    }

    public void close() {

        if (createDialog != null) {
            createDialog.dismiss();
        }
    }


    @Override
    public void onClick(View v) {
        if (v == createNewButton) {
            create();
        }
    }

    private void create() {
        MaterialDialog nameDialog = new MaterialDialog.Builder(context)
                .theme(Theme.LIGHT).titleColorRes(R.color.normal_blue)
                .negativeColorRes(R.color.dialog_negetive_button)
                .title("Merge Playlist")
                .positiveText("CREATE")
                .negativeText("CANCEL")
                .inputType(InputType.TYPE_CLASS_TEXT).input("Playlist Name", "", (dialog, input) -> {
                    if (input.toString().length() > 0) {
                        createDialog.dismiss();
                        PlaylistItem item = new PlaylistItem();
                        item.id = Utils.randLong();
                        item.title = input.toString();
                        item.hash = Utils.generateSha1Hash(item.title + System.currentTimeMillis());
                        long id = PlaylistRealmHelper.insert(item);
                        if (id > 0) {
                            ArrayList<QueueItem> queueItems = new ArrayList<>();
                            ArrayList<PlaylistItem> playlistItems = adapter.getList();
                            for (PlaylistItem playlistItem : playlistItems) {
                                queueItems.addAll(PlaylistSongRealmHelper.loadAllByPlaylist(0, playlistItem.id));
                            }


                            if (removeExisting) {
                                for (PlaylistItem playlistItem : playList) {
                                    PlaylistDelete taskdelete = new PlaylistDelete(context, playlistItem.id);
                                    taskdelete.execute();
                                }
                            }

                            PlaylistItem newplaylist = PlaylistRealmHelper.getPlaylist(id);
                            PlaylistAdder taskAdd = new PlaylistAdder(context, newplaylist, queueItems, false);
                            taskAdd.execute();


                        } else {
                            AppController.toast(context, "Unable to createActivityListener playlist!");
                        }
                    } else {
                        AppController.toast(context, "Enter name playlist!");
                    }
                }).build();

        nameDialog.show();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        removeExisting = isChecked;

    }

    @Override
    public void onDragTouched(RecyclerView.ViewHolder viewHolder) {
        if (touchHelper != null) touchHelper.startDrag(viewHolder);
    }

    @Override
    public void onMenuClicked(int position) {

    }

    @Override
    public void onItemClicked(int position) {

    }

    @Override
    public boolean onItemLongClicked(int position) {
        if (dragged) {
            dragged = false;
            return false;
        }
        return false;
    }

    private class MergePlaylistItemTouchHelper extends ItemTouchHelper.Callback {
        //defines the enabled move directions in each state (idle, swiping, dragging).
        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            return makeFlag(ItemTouchHelper.ACTION_STATE_DRAG, ItemTouchHelper.DOWN | ItemTouchHelper.UP | ItemTouchHelper.START | ItemTouchHelper.END);
        }

        //and in your imlpementaion of
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            int from = viewHolder.getAdapterPosition();
            int to = target.getAdapterPosition();
            if (adapter.moveTo(from, to)) {
                AppController.Instance().serviceDirty();
                AppController.Instance().updateQueueIndex();
                dragged = true;
            }
            return true;
        }

        @Override
        public boolean isLongPressDragEnabled() {
            return false;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

        }
    }

}
