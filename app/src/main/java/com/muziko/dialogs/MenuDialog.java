package com.muziko.dialogs;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.view.LayoutInflater;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.google.firebase.auth.FirebaseAuth;
import com.muziko.R;
import com.muziko.activities.register.RegisterActivity;
import com.muziko.adapter.MaterialMenuAdapter;
import com.muziko.common.models.QueueItem;
import com.muziko.controls.FastScroller.FastScrollRecyclerView;
import com.muziko.controls.LayoutManagers.NpaLinearLayoutManager;
import com.muziko.database.TrackRealmHelper;
import com.muziko.helpers.Utils;
import com.muziko.interfaces.BasicRecyclerItemListener;
import com.muziko.manager.AppController;
import com.muziko.manager.MuzikoConstants;
import com.muziko.mediaplayer.PlayerConstants;
import com.muziko.objects.MenuObject;
import com.muziko.tasks.FavoriteEdit;
import com.muziko.tasks.FolderDelete;
import com.muziko.tasks.TrackDelete;

import java.util.ArrayList;

import static com.muziko.MyApplication.networkState;
import static com.muziko.manager.MuzikoConstants.CODE_WRITE_SETTINGS_PERMISSION;
import static com.muziko.objects.MenuObject.ADD_TO_PLAYLIST;
import static com.muziko.objects.MenuObject.ADD_TO_QUEUE;
import static com.muziko.objects.MenuObject.CUT;
import static com.muziko.objects.MenuObject.DELETE_ITEM;
import static com.muziko.objects.MenuObject.DETAILS;
import static com.muziko.objects.MenuObject.EDIT_TAGS;
import static com.muziko.objects.MenuObject.FAV;
import static com.muziko.objects.MenuObject.GO_TO_ALBUM;
import static com.muziko.objects.MenuObject.GO_TO_ARTIST;
import static com.muziko.objects.MenuObject.MANAGE_ARTWORK;
import static com.muziko.objects.MenuObject.MOVE_TO_IGNORE;
import static com.muziko.objects.MenuObject.PLAY_NEXT;
import static com.muziko.objects.MenuObject.PLAY_X_TIMES;
import static com.muziko.objects.MenuObject.PREVIEW_SONG;
import static com.muziko.objects.MenuObject.RESET_MOST_PLAYED;
import static com.muziko.objects.MenuObject.RESET_RECENT_PLAYED;
import static com.muziko.objects.MenuObject.SEND;
import static com.muziko.objects.MenuObject.SEND_CONTACTS;
import static com.muziko.objects.MenuObject.SEND_WIFI;
import static com.muziko.objects.MenuObject.SET_RINGTONE;
import static com.muziko.objects.MenuObject.SHARE_ITEM;

/**
 * Created by Bradley on 31/01/2017.
 */

public class MenuDialog implements BasicRecyclerItemListener, MaterialMenuAdapter.Callback {

    private final String TAG = MenuDialog.class.getName();
    private FastScrollRecyclerView mRecyclerView;
    private MaterialMenuAdapter mAdapter;
    private ArrayList<MenuObject> menuItems = new ArrayList<>();
    private QueueItem queueItem;
    private int mPosition;
    private Activity mcontext;
    private MaterialDialog menuDialog = null;
    private int mode;
    private onMenuDialogCompleted mListener;
    private ArrayList<QueueItem> queueItems = new ArrayList<>();


    public MenuDialog(final Activity context, final QueueItem queueItem, int position, final ArrayList<MenuObject> menuItems, onMenuDialogCompleted mListener) {
        mcontext = context;
        this.queueItem = queueItem;
        this.menuItems = menuItems;
        this.mPosition = position;
        this.mListener = mListener;
    }

    public void open() {

        View view = LayoutInflater.from(mcontext).inflate(R.layout.dialog_menu_options, null, false);
        mRecyclerView = view.findViewById(R.id.itemList);

        NpaLinearLayoutManager layoutList = new NpaLinearLayoutManager(mcontext);

        mAdapter = new MaterialMenuAdapter(menuItems, this);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(layoutList);
        mRecyclerView.setAdapter(mAdapter);
        menuDialog = new MaterialDialog.Builder(mcontext).theme(Theme.LIGHT).titleColorRes(R.color.normal_blue).neutralColorRes(R.color.normal_blue).positiveColorRes(R.color.normal_blue).customView(view, false).show();


    }

    @Override
    public void onMenuObjectItemSelected(MaterialDialog dialog, int index, MenuObject item) {

    }

    @Override
    public void onItemClicked(int position) {
        final MenuObject item = mAdapter.getItem(position);

        switch (item.id) {
            case ADD_TO_QUEUE:     //add to q
                PlayerConstants.QUEUE_TYPE = 0;
                AppController.Instance().addToQueue(mcontext, queueItem, false);
                break;

            case ADD_TO_PLAYLIST:     //add to p
                AppController.Instance().addToPlaylist(mcontext, queueItem);
                break;

            case PLAY_NEXT:     //play next
                AppController.Instance().addToQueue(mcontext, queueItem, true);
                break;

            case FAV:     //add to f`

                FavoriteEdit fe = new FavoriteEdit(mcontext, PlayerConstants.QUEUE_TYPE_TRACKS, s -> {

                    if (mListener != null) {
                        mListener.onUpdateItem(mPosition);
                    }
                });
                fe.execute(queueItem);

                break;


            case GO_TO_ARTIST:     //goto ar
                AppController.Instance().gotoArtist(mcontext, queueItem, null);
                break;

            case GO_TO_ALBUM:     //goto al
                AppController.Instance().gotoAlbum(mcontext, queueItem, null);
                break;

            case CUT:     //CUT
                AppController.Instance().cutSong(queueItem);
                break;

            case EDIT_TAGS:     //EDIT_TAGS
                AppController.Instance().editSong(mcontext, TAG, mPosition, queueItem);
                break;

            case DETAILS:     //DETAILS
                AppController.Instance().details(mcontext, queueItem);
                break;

            case SHARE_ITEM:     //SHARE_ITEM
                AppController.Instance().shareSong(mcontext, queueItem);
                break;

            case MOVE_TO_IGNORE:     //MOVE_TO_IGNORE
                Utils.askDelete(mcontext, "Move to Ignore Folder", "You can later restore it from ignore folder, are you sure you want to ignore it?", () -> {

                    TrackRealmHelper.movetoNegative(queueItem);
                    if (mListener != null) {
                        mListener.onRemoveItem(mPosition);
                    }
                });
                break;

            case DELETE_ITEM:     //DELETE_ITEM
                if (queueItem.folder) {
                    Utils.askDelete(mcontext, "Delete Folder", "This will permanently delete all songs in the folder, do you want to proceed ?", () -> {
                        FolderDelete fl = new FolderDelete(mcontext, () -> {
                            if (mListener != null) {
                                mListener.onRemoveItem(mPosition);
                            }
                        });
                        fl.execute(queueItem.data);
                    });
                } else {
                    Utils.askDelete(mcontext, "Delete Song", "This will delete song permanently from this device, do you want to proceed ?", () -> {
                        TrackDelete tr = new TrackDelete(mcontext, PlayerConstants.QUEUE_TYPE_TRACKS, () -> {
                            if (mListener != null) {
                                mListener.onRemoveItem(mPosition);
                            }
                        });
                        tr.execute(queueItem);
                    });
                }
                break;

            case RESET_MOST_PLAYED:     //RESET_MOST_PLAYED
                if (TrackRealmHelper.resetMostPlayedCount(queueItem)) {
                    if (mListener != null) {
                        mListener.onReload();
                    }
                } else {

                    AppController.toast(mcontext, "Unable to reset track play count!");
                }
                break;

            case PLAY_X_TIMES:     //PLAY_X_TIMES
                AppController.Instance().removeAfter(mcontext, queueItem);

                break;

            case SET_RINGTONE:     //SET_RINGTONE

                boolean permission;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    permission = Settings.System.canWrite(mcontext);
                } else {
                    permission = ContextCompat.checkSelfPermission(mcontext, Manifest.permission.WRITE_SETTINGS) == PackageManager.PERMISSION_GRANTED;
                }
                if (!permission) {

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                        if (mListener != null) {
                            mListener.onSetRingtone(queueItem);
                        }

                        new MaterialDialog.Builder(mcontext).theme(Theme.LIGHT).titleColorRes(R.color.normal_blue).negativeColorRes(R.color.dialog_negetive_button).positiveColorRes(R.color.normal_blue).title("Permission required").content("Muziko requires the write settings permission to set your default ringtone. You need to enable it on the settings screen.").positiveText("Ok").onPositive((mdialog, mwhich) -> {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                                intent.setData(Uri.parse("package:" + mcontext.getPackageName()));
                                mcontext.startActivityForResult(intent, CODE_WRITE_SETTINGS_PERMISSION);
                            } else {
                                ActivityCompat.requestPermissions(mcontext, new String[]{Manifest.permission.WRITE_SETTINGS}, CODE_WRITE_SETTINGS_PERMISSION);
                            }
                        }).negativeText("Cancel").show();
                    } else {
                        SetRingtone createRingtone = new SetRingtone();
                        createRingtone.open(mcontext, queueItem);
                    }
                } else {
                    SetRingtone createRingtone = new SetRingtone();
                    createRingtone.open(mcontext, queueItem);
                }

                break;

            case PREVIEW_SONG:     //PREVIEW_SONG

                PreviewSong previewSong = new PreviewSong();
                previewSong.open(mcontext, queueItem);

                break;

            case RESET_RECENT_PLAYED:     //RESET_RECENT_PLAYED

                if (TrackRealmHelper.resetRecentlyPlayedCount(queueItem)) {
                    if (mListener != null) {
                        mListener.onRemoveItem(mPosition);
                    }
                } else {

                    AppController.toast(mcontext, "Unable to reset track play count!");
                }

                break;

            case SEND_CONTACTS:     //SEND_CONTACTS


                break;

            case SEND:     //SEND

                queueItems.add(queueItem);

                if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                    if (FirebaseAuth.getInstance().getCurrentUser().isAnonymous()) {

                        if (mListener != null) {
                            mListener.onSendTracks(queueItem);
                        }
                        if (networkState == NetworkInfo.State.CONNECTED) {
                            Intent registerIntent = new Intent(mcontext, RegisterActivity.class);
                            mcontext.startActivityForResult(registerIntent, MuzikoConstants.REQUEST_REGISTER_USER_TRACKS);
                        } else {
                            AppController.toast(mcontext, mcontext.getString(R.string.no_internet_for_register));
                        }

                    } else {
//						AppController.Instance().sendTracks(mcontext,  queueItems, );
                    }
                } else {
                    if (networkState == NetworkInfo.State.CONNECTED) {
                        Intent registerIntent = new Intent(mcontext, RegisterActivity.class);
                        mcontext.startActivityForResult(registerIntent, MuzikoConstants.REQUEST_REGISTER_USER_TRACKS);
                    } else {
                        AppController.toast(mcontext, mcontext.getString(R.string.no_internet_for_register));
                    }
                }


                break;

            case SEND_WIFI:     //SEND_WIFI

                queueItems.add(queueItem);

                AppController.Instance().sendTracksWifi(mcontext, queueItems);


                break;

            case MANAGE_ARTWORK:     //MANAGE_ARTWORK


                break;

        }
        menuDialog.dismiss();
    }

    public interface onMenuDialogCompleted {

        void onReload();

        void onSendTracks(QueueItem queueItem);

        void onSetRingtone(QueueItem queueItem);

        void onUpdateItem(int position);

        void onRemoveItem(int position);
    }
}
