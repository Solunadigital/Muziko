package com.muziko.fragments.Contacts;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DefaultItemAnimator;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.badoo.mobile.util.WeakHandler;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;
import com.muziko.MyApplication;
import com.muziko.R;
import com.muziko.activities.ContactsActivity;
import com.muziko.adapter.ContactsAdapter;
import com.muziko.adapter.SelectableAdapter;
import com.muziko.callbacks.ContactsCallback;
import com.muziko.common.models.firebase.Contact;
import com.muziko.common.models.firebase.Person;
import com.muziko.controls.FastScroller.FastScrollRecyclerView;
import com.muziko.controls.FastScroller.OnFastScrollStateChangeListener;
import com.muziko.controls.LayoutManagers.NpaLinearLayoutManager;
import com.muziko.fragments.BaseFragment;
import com.muziko.helpers.Utils;
import com.muziko.interfaces.PicassoScrollListener;
import com.muziko.interfaces.ShareListener;
import com.muziko.manager.FirebaseManager;

import java.util.ArrayList;
import java.util.List;

import static br.com.zbra.androidlinq.Linq.stream;

/**
 * Created by dev on 2/11/2016.
 */

public class BlockedContactsFragment extends BaseFragment implements ContactsCallback, ShareListener, ActionMode.Callback, OnFastScrollStateChangeListener {
    private final String TAG = BlockedContactsFragment.class.getName();
    private FastScrollRecyclerView recyclerView;
    private ActionMode actionMode;
    private ContactsAdapter mAdapter;
    private List<Person> contacts = new ArrayList<>();
    private WeakHandler handler = new WeakHandler();

    public BlockedContactsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_activity_child, container, false);

        contacts = stream(MyApplication.userList.values())
                .where(c -> c.isBlocked())
                .toList();

        mAdapter = new ContactsAdapter(getActivity(), contacts, TAG, 1, this);
        mAdapter.sortOnlineHighest();

        NpaLinearLayoutManager layoutList = new NpaLinearLayoutManager(getActivity());

        recyclerView = rootView.findViewById(R.id.itemList);
        recyclerView.setVisibility(View.VISIBLE);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setHasFixedSize(true);
        recyclerView.setStateChangeListener(this);
        recyclerView.addOnScrollListener(new PicassoScrollListener(getActivity(), TAG));
        recyclerView.setLayoutManager(layoutList);
        recyclerView.setAdapter(mAdapter);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroyView() {

        super.onDestroyView();
    }

    @Override
    public void onAttach(Context context) {


        ((ContactsActivity) getActivity()).callbackBlocked = this;


        super.onAttach(context);
    }

    @Override
    public void onResume() {
        super.onResume();

        reload();
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();

        ((ContactsActivity) getActivity()).callbackBlocked = null;

    }

    @Override
    public void onListingChanged() {

    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        getActivity().getMenuInflater().inflate(R.menu.contact_blocked_menu_context, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        actionMode = mode;
        ((ContactsActivity) getActivity()).enableTabs(false);

        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        final ArrayList<Person> persons = mAdapter.getSelectedItems();
        if (persons.size() > 0) {
            // Handle presses on the action bar items
            switch (item.getItemId()) {

                case R.id.contact_unblock:

                    new MaterialDialog.Builder(getActivity()).theme(Theme.LIGHT).titleColorRes(R.color.normal_blue).negativeColorRes(R.color.dialog_negetive_button).positiveColorRes(R.color.normal_blue).title("Unblock Contacts").content("Are you want to unblock these contacts?").positiveText("Unblock").onPositive((dialog, which) -> {
                        for (Person person : persons) {
                            blockUser(person);
                        }
                        mAdapter.notifyDataSetChanged();

                    }).negativeText("Cancel").show();

                    break;


                default:
                    return false;
            }
        }

        mode.finish();
        actionMode = null;
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        ((ContactsActivity) getActivity()).enableTabs(true);
        handler.post(() -> {
            if (!recyclerView.isComputingLayout()) {
                ((SelectableAdapter) recyclerView.getAdapter()).setMultiSelect(false);
                actionMode = null;
            } else {
                onDestroyActionMode(this.actionMode);
            }
        });
    }

    @Override
    public void onItemClicked(final int position) {
        if (mAdapter.isMultiSelect()) {
            toggleSelection(position);
        } else {

            final Person person = mAdapter.getItem(position);
            new MaterialDialog.Builder(getActivity()).theme(Theme.LIGHT).titleColorRes(R.color.normal_blue).negativeColorRes(R.color.dialog_negetive_button).positiveColorRes(R.color.normal_blue).title("Unblock Contact").content("Are you want to unblock this contact?").positiveText("Unblock").onPositive((dialog, which) -> {
                blockUser(person);
                mAdapter.notifyItemRemoved(position);

            }).negativeText("Cancel").show();

        }
    }

    @Override
    public void onMenuClicked(Context context, int position) {

    }

    @Override
    public void onBlockClicked(int position) {

    }

    @Override
    public boolean onItemLongClicked(int position) {
        if (!this.mAdapter.isMultiSelect()) {
            ((AppCompatActivity) getActivity()).startSupportActionMode(this);

            this.mAdapter.setMultiSelect(true);
            toggleSelection(position);
            return true;
        }
        return false;
    }

    private void blockUser(Person person) {
        DatabaseReference userref = FirebaseManager.Instance().getContactsRef();

        Contact contact = new Contact(person.getUid(), false, ServerValue.TIMESTAMP);

        userref.child(person.getUid()).setValue(contact, (error, firebase) -> {
            if (error != null) {

            }
        });
    }

    @Override
    public void onFilterValue(int value, boolean reverse) {

        switch (value) {
            case R.id.contact_sort_title:
                if (!reverse) {
                    mAdapter.sortTitleLowest();
                } else {
                    mAdapter.sortTitleHighest();
                }
                break;
            case R.id.contact_sort_online:
                if (!reverse) {
                    mAdapter.sortOnlineLowest();
                } else {
                    mAdapter.sortOnlineHighest();
                }
                break;
        }
    }

    @Override
    public void onSearchQuery(String chars) {
        mAdapter.search(chars);
    }

    @Override
    public void onReload() {
        reload();
    }

    @Override
    public void onLayoutChanged(Float bottomMargin) {
        Resources resources = getActivity().getResources();
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) recyclerView.getLayoutParams();
        params.setMargins(0, 0, 0, Utils.toPixels(resources, bottomMargin));
        recyclerView.requestLayout();
    }

    private void reload() {

        contacts.clear();
        contacts.addAll(stream(MyApplication.userList.values())
                .where(c -> c.isBlocked())
                .toList());
        mAdapter.notifyDataSetChanged();
    }

    private void toggleSelection(int position) {
        ((SelectableAdapter) recyclerView.getAdapter()).toggleSelection(position);

        int count = ((SelectableAdapter) recyclerView.getAdapter()).getSelectedItemCount();

        if (actionMode != null) {
            if (count == 0)
                actionMode.setTitle("");
            else
                actionMode.setTitle(String.format("%d contact%s", count, count != 1 ? "s" : ""));
        }
    }

    @Override
    public void onFastScrollStart() {
        ((ContactsActivity) getActivity()).fastScrolling(true);
    }

    @Override
    public void onFastScrollStop() {

    }
}
