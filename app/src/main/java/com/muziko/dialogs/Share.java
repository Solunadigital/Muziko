package com.muziko.dialogs;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.badoo.mobile.util.WeakHandler;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;
import com.muziko.MyApplication;
import com.muziko.R;
import com.muziko.adapter.UserShareAdapter;
import com.muziko.common.events.FirebaseRefreshEvent;
import com.muziko.common.models.QueueItem;
import com.muziko.common.models.firebase.Contact;
import com.muziko.common.models.firebase.Person;
import com.muziko.controls.FastScroller.FastScrollRecyclerView;
import com.muziko.controls.LayoutManagers.NpaLinearLayoutManager;
import com.muziko.interfaces.PicassoScrollListener;
import com.muziko.interfaces.ShareListener;
import com.muziko.manager.AppController;
import com.muziko.manager.FirebaseManager;
import com.muziko.manager.GsonManager;
import com.muziko.service.MuzikoFirebaseService;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;

import static com.muziko.helpers.Utils.findChildrenByClass;

/**
 * Created by Bradley on 31/01/2017.
 */

public class Share implements ShareListener, SearchView.OnQueryTextListener, View.OnClickListener {

    private final String TAG = Share.class.getName();
    private final WeakHandler handler = new WeakHandler();
    private final LinkedHashMap<String, Person> selectList = new LinkedHashMap<>();
    private Collection<Person> selected;
    private FastScrollRecyclerView mRecyclerView;
    private RelativeLayout emptyLayout;
    private ArrayList<Person> contacts = new ArrayList<>();
    private UserShareAdapter mAdapter;
    private String data;
    private int sortId;
    private ArrayList<QueueItem> queueItems = new ArrayList<>();
    private SearchView searchView;
    private Context mcontext;


    public void open(final Context context, final ArrayList<QueueItem> queueItems) {
        mcontext = context;
        this.queueItems = queueItems;

        View view = LayoutInflater.from(mcontext).inflate(R.layout.dialog_share, null, false);
        emptyLayout = view.findViewById(R.id.emptyLayout);
        mRecyclerView = view.findViewById(R.id.itemList);
        searchView = view.findViewById(R.id.searchView);

        NpaLinearLayoutManager layoutList = new NpaLinearLayoutManager(mcontext);

        emptyLayout.setVisibility(View.GONE);

        selected = selectList.values();

        contacts = new ArrayList<>();
        mAdapter = new UserShareAdapter(mcontext, contacts, selected, TAG, this);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addOnScrollListener(new PicassoScrollListener(mcontext, TAG));
        mRecyclerView.setLayoutManager(layoutList);
        mRecyclerView.setAdapter(mAdapter);

        for (TextView textView : findChildrenByClass(searchView, TextView.class)) {
            textView.setTextColor(Color.WHITE);
        }
        searchView.setIconifiedByDefault(false);
        searchView.setQueryHint("Search...");
        searchView.setOnQueryTextListener(this);
        searchView.setOnSearchClickListener(this);

        EventBus.getDefault().register(this);

        reload();

        new MaterialDialog.Builder(context).theme(Theme.LIGHT).titleColorRes(R.color.normal_blue).neutralColorRes(R.color.normal_blue).positiveColorRes(R.color.normal_blue).customView(view, false).autoDismiss(true).positiveText("Send").onPositive((dialog, which) -> {

            if (selectList.size() == 0) {
                AppController.toast(mcontext, "You must select at least one contact");
            } else {
                shareFile(selectList.values());
                dialog.dismiss();
                mRecyclerView.setAdapter(null);
            }
        }).neutralText("Cancel").onNeutral((dialog, which) -> dialog.dismiss()).dismissListener(dialog -> {
            dialog.dismiss();
            EventBus.getDefault().unregister(this);

        }).autoDismiss(false).show();

    }

    private void shareFile(Collection<Person> personList) {

        for (QueueItem queueItem : queueItems) {
            String persons = GsonManager.Instance().getGson().toJson(personList);

            Intent intent = new Intent(mcontext, MuzikoFirebaseService.class);
            intent.setAction(AppController.ACTION_UPLOAD);
            intent.putExtra(AppController.ARG_ITEM, queueItem);
            intent.putExtra(AppController.ARG_PEOPLE, persons);
            mcontext.startService(intent);
        }


    }

    // This method will be called when a MessageEvent is posted (in the UI thread for Toast)
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFirebaseEvent(FirebaseRefreshEvent event) {

        handler.postDelayed(this::reload, event.delay);


    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        mAdapter.getFilter().filter(newText);

        return false;
    }

    @Override
    public void onClick(View v) {
//		if (v == createButton) {
//			PlayerConstants.QUEUE_TYPE = 0;
//
//			Intent in = new Intent(this, SearchSongsActivity.class);
//			in.putExtra(MyApplication.ARG_FAV, true);
//			startActivity(in);
//		} else {
//			//super.onClick(v);
//		}
    }


    @Override
    public void onItemClicked(int position) {

        Person person = mAdapter.getItem(position);
        if (selectList.get(person.getUid()) != null) {
            selectList.remove(person.getUid());
        } else {
            selectList.put(person.getUid(), person);
        }
        reload();

    }

    @Override
    public void onMenuClicked(Context context, int position) {

    }

    @Override
    public void onBlockClicked(final int position) {
        final Person person = mAdapter.getItem(position);
        new MaterialDialog.Builder(mcontext).theme(Theme.LIGHT).titleColorRes(R.color.normal_blue).negativeColorRes(R.color.dialog_negetive_button).positiveColorRes(R.color.normal_blue).title("Block Contact").content("Are you want to block this contact?").positiveText("Block").onPositive((dialog, which) -> {

            blockUser(person);
            mAdapter.notifyItemRemoved(position);

        }).negativeText("Cancel").show();

    }

    @Override
    public boolean onItemLongClicked(int position) {
//		if (!this.mAdapter.isMultiSelect()) {
//			ShareActivity.this.startSupportActionMode(this);
//			this.mAdapter.setMultiSelect(true);
//			toggleSelection(position);
//			return true;
//		}
        return false;
    }

    private void blockUser(Person person) {
        DatabaseReference userref = FirebaseManager.Instance().getContactsRef();

        Contact contact = new Contact(person.getUid(), true, ServerValue.TIMESTAMP);

        userref.child(person.getUid()).setValue(contact, (error, firebase) -> {
            if (error != null) {

            }
        });
    }

    private void reload() {

        selected = selectList.values();

        contacts.clear();
        for (Person person : MyApplication.userList.values()) {
            if (!person.isBlocked() && person.isFriend()) {
                contacts.add(person);
            }
        }
        mAdapter.notifyDataSetChanged();

        emptied();
    }


    private void emptied() {
        if (mAdapter.getItemCount() > 0) {
            mRecyclerView.setVisibility(View.VISIBLE);
            emptyLayout.setVisibility(View.GONE);
        } else {
            mRecyclerView.setVisibility(View.GONE);
            emptyLayout.setVisibility(View.VISIBLE);
        }

    }
}
