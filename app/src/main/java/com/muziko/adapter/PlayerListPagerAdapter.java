package com.muziko.adapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.muziko.MyApplication;
import com.muziko.fragments.Listening.AlbumsFragment;
import com.muziko.fragments.Listening.TracksFragment;

public class PlayerListPagerAdapter extends FragmentPagerAdapter {

    private final String[] tabTitles = new String[]{"Tracks", "Albums"};

	private final int playType;
	private final String playName;
	private final String playData;
	private final int count;

	public PlayerListPagerAdapter(FragmentManager fragmentManager, int playType, String playName, String playData, int count) {
		super(fragmentManager);
		this.playType = playType;
		this.playName = playName;
		this.playData = playData;
		this.count = count;
	}

	@Override
	public Fragment getItem(int position) {
		Bundle bundle = new Bundle();
		bundle.putInt(MyApplication.ARG_TYPE, playType);
		bundle.putString(MyApplication.ARG_NAME, playName);
		bundle.putString(MyApplication.ARG_DATA, playData);

        TracksFragment fragTracksFragment = null;
        if (count == 1) {
            fragTracksFragment = new TracksFragment();
            fragTracksFragment.setArguments(bundle);
            return fragTracksFragment;
        } else {
            switch (position) {
				case 0:
                    fragTracksFragment = new TracksFragment();
                    fragTracksFragment.setArguments(bundle);
                    return fragTracksFragment;


				case 1:
                    AlbumsFragment fragAlbum = new AlbumsFragment();
                    fragAlbum.setArguments(bundle);
                    return fragAlbum;

			}
		}
		return null;
	}

	@Override
	public int getCount() {
		return count;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		return tabTitles[position];
	}

}
