package com.muziko.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.crashlytics.android.Crashlytics;
import com.muziko.MyApplication;
import com.muziko.common.models.TabModel;
import com.muziko.database.TabRealmHelper;
import com.muziko.fragments.Listening.AlbumsFragment;
import com.muziko.fragments.Listening.ArtistsFragment;
import com.muziko.fragments.Listening.GenresFragment;
import com.muziko.fragments.Listening.HomeFragment;
import com.muziko.fragments.Listening.TracksFragment;
import com.muziko.helpers.TabsHelper;

import static com.muziko.MyApplication.tabModels;


public class PlayerPagerAdapter extends FragmentPagerAdapter {

    private HomeFragment homeFragment = null;
    private TracksFragment tracksFragment = null;
    private ArtistsFragment artistsFragment = null;
    private AlbumsFragment albumsFragment = null;
    private GenresFragment genresFragment = null;
    private int currentItem = 0;

    public PlayerPagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
        tabModels = TabRealmHelper.getTabs();
        if (tabModels.size() == 0) {
            TabsHelper tabsHelper = new TabsHelper();
            tabsHelper.saveInitalTabLayout(MyApplication.getInstance().getApplicationContext());
            tabModels = TabRealmHelper.getAllTabs();
        }
    }

    public String getCurrentTab() {

        try {
            if (tabModels.size() == 0) {
                TabsHelper tabsHelper = new TabsHelper();
                tabsHelper.saveInitalTabLayout(MyApplication.getInstance().getApplicationContext());
                tabModels = TabRealmHelper.getAllTabs();
            }

            if (tabModels.size() == 0) {
                return MyApplication.TRACKS;
            } else {
                return tabModels.get(currentItem).title;
            }
        } catch (Exception e) {
            Crashlytics.logException(e);
            return MyApplication.TRACKS;
        }
    }

    public int getCurrentItem() {
        return currentItem;
    }

    public void setCurrentItem(int currentItem) {
        this.currentItem = currentItem;
    }

    @Override
    public Fragment getItem(int position) {

        TabModel tabModel = tabModels.get(position);

        switch (tabModel.title) {

            case MyApplication.HOME:
                if (homeFragment == null) {
                    homeFragment = HomeFragment.newInstance();
                }
                return homeFragment;

            case MyApplication.TRACKS:
                if (tracksFragment == null) {
                    tracksFragment = TracksFragment.newInstance();
                }
                return tracksFragment;

            case MyApplication.ARTISTS:
                if (artistsFragment == null) {
                    artistsFragment = ArtistsFragment.newInstance();
                }
                return artistsFragment;

            case MyApplication.ALBUMS:
                if (albumsFragment == null) {
                    albumsFragment = AlbumsFragment.newInstance();
                }
                return albumsFragment;

            case MyApplication.GENRES:
                if (genresFragment == null) {
                    genresFragment = GenresFragment.newInstance();
                }
                return genresFragment;
        }
        return null;
    }

    @Override
    public int getCount() {
        return tabModels.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {

        String title = tabModels.get(position).title;
        if (title == null) {
            title = "";
        }
        return title;
    }

    public int getPagePosition(String title) {

        for (TabModel tabModel : tabModels) {
            if (tabModel.title.equals(title)) {
                return tabModel.order;
            }
        }

        return 0;
    }

}