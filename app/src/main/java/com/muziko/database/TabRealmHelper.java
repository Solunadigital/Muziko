package com.muziko.database;

import com.muziko.MyApplication;
import com.muziko.common.models.TabModel;
import com.muziko.fragments.Listening.AlbumsFragment;
import com.muziko.fragments.Listening.ArtistsFragment;
import com.muziko.fragments.Listening.GenresFragment;
import com.muziko.fragments.Listening.HomeFragment;
import com.muziko.fragments.Listening.TracksFragment;
import com.muziko.models.FragmentModel;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by dev on 27/08/2016.
 */
public class TabRealmHelper {

	public static void saveTabs(ArrayList<TabModel> tabModels) {

		Realm myRealm = Realm.getDefaultInstance();

		if (!myRealm.isInTransaction()) {
			myRealm.beginTransaction();
		}

		for (TabModel tabModel : tabModels) {
			TabRealm tabRealm = new TabRealm();
			tabRealm.setTitle(tabModel.title);
			tabRealm.setShow(tabModel.show);
			tabRealm.setOrder(tabModel.order);
			myRealm.insertOrUpdate(tabRealm);
		}

		myRealm.commitTransaction();
		myRealm.close();
	}


	public static ArrayList<TabModel> getAllTabs() {

		ArrayList<TabModel> list = new ArrayList<>();

		Realm myRealm = Realm.getDefaultInstance();

		RealmResults<TabRealm> realmResults = myRealm.where(TabRealm.class).findAllSorted("order", Sort.ASCENDING);

		for (int i = 0; i < realmResults.size(); i++) {

			TabModel tabModel = new TabModel();
			tabModel.title = realmResults.get(i).getTitle();
			tabModel.show = realmResults.get(i).isShow();
			tabModel.order = realmResults.get(i).getOrder();
			list.add(tabModel);
		}


		myRealm.close();
		return list;
	}

	public static ArrayList<TabModel> getTabs() {

		ArrayList<TabModel> list = new ArrayList<>();

		Realm myRealm = Realm.getDefaultInstance();

		RealmResults<TabRealm> realmResults = myRealm.where(TabRealm.class).equalTo("show", true).findAllSorted("order", Sort.ASCENDING);

		for (int i = 0; i < realmResults.size(); i++) {

			TabModel tabModel = new TabModel();
			tabModel.title = realmResults.get(i).getTitle();
			tabModel.show = realmResults.get(i).isShow();
			tabModel.order = realmResults.get(i).getOrder();
			list.add(tabModel);
		}


		myRealm.close();
		return list;
	}

	public static TabModel getTab(int position) {

		Realm myRealm = Realm.getDefaultInstance();

		TabRealm tabRealm = myRealm.where(TabRealm.class).equalTo("order", position).findFirst();

		TabModel tabModel = new TabModel();
		if (tabRealm != null) {
			tabModel.title = tabRealm.getTitle();
			tabModel.show = tabRealm.isShow();
			tabModel.order = tabRealm.getOrder();
		}

		myRealm.close();
		return tabModel;
	}

	public static TabModel getTab(String title) {

		Realm myRealm = Realm.getDefaultInstance();

		TabRealm tabRealm = myRealm.where(TabRealm.class).equalTo("title", title).findFirst();

		TabModel tabModel = new TabModel();
		if (tabRealm != null) {
			tabModel.title = tabRealm.getTitle();
			tabModel.show = tabRealm.isShow();
			tabModel.order = tabRealm.getOrder();
		}

		myRealm.close();
		return tabModel;
	}

	public static int getCount() {

		Realm myRealm = Realm.getDefaultInstance();
		int count = 0;
		RealmResults<TabRealm> realmResults = myRealm.where(TabRealm.class).equalTo("show", true).findAll();

		if (realmResults != null) {
			count = realmResults.size();
		}
		myRealm.close();
		return count;
	}

	public static ArrayList<FragmentModel> getFragments() {
		ArrayList<FragmentModel> fragmentArrayList = new ArrayList<>();
		ArrayList<TabModel> tabs = TabRealmHelper.getAllTabs();
		FragmentModel fragmentModel = new FragmentModel();
		for (TabModel tabModel : tabs) {
			fragmentModel = new FragmentModel();
			switch (tabModel.title) {

				case MyApplication.HOME:
					HomeFragment homeFragment = HomeFragment.newInstance();
					fragmentModel.setTitle(tabModel.title);
					fragmentModel.setPosition(tabModel.order);
					fragmentModel.setFragment(homeFragment);
					fragmentArrayList.add(fragmentModel);
					break;

				case MyApplication.TRACKS:
					TracksFragment fragTracksFragment = TracksFragment.newInstance();
					fragmentModel.setTitle(tabModel.title);
					fragmentModel.setPosition(tabModel.order);
					fragmentModel.setFragment(fragTracksFragment);
					fragmentArrayList.add(fragmentModel);
					break;

				case MyApplication.ARTISTS:
					ArtistsFragment fragArtistsFragment = ArtistsFragment.newInstance();
					fragmentModel.setTitle(tabModel.title);
					fragmentModel.setPosition(tabModel.order);
					fragmentModel.setFragment(fragArtistsFragment);
					fragmentArrayList.add(fragmentModel);
					break;

				case MyApplication.ALBUMS:
					AlbumsFragment fragAlbumsFragment = AlbumsFragment.newInstance();
					fragmentModel.setTitle(tabModel.title);
					fragmentModel.setPosition(tabModel.order);
					fragmentModel.setFragment(fragAlbumsFragment);
					fragmentArrayList.add(fragmentModel);
					break;

				case MyApplication.GENRES:
					GenresFragment fragGenresFragment = GenresFragment.newInstance();
					fragmentModel.setTitle(tabModel.title);
					fragmentModel.setPosition(tabModel.order);
					fragmentModel.setFragment(fragGenresFragment);
					fragmentArrayList.add(fragmentModel);
					break;
			}
		}

		return fragmentArrayList;
	}
}
