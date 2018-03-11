package com.muziko.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.muziko.MyApplication;
import com.muziko.fragments.Contacts.AllowedContactsFragment;
import com.muziko.fragments.Contacts.BlockedContactsFragment;
import com.muziko.fragments.Contacts.ContactsFragment;

public class ContactsPagerAdapter extends FragmentPagerAdapter {

	// Tab Titles
//	private String tabTitles[] = new String[]{"Recently Played", "Most Played", "Recently Added"};

	private final String[] tabTitles = new String[]{"Contacts", "Allowed", "Blocked"};

	private ContactsFragment fragContacts = null;
	private AllowedContactsFragment fragAllowed = null;
	private BlockedContactsFragment fragBlocked = null;

	public ContactsPagerAdapter(FragmentManager fragmentManager) {
		super(fragmentManager);
	}

	@Override
	public Fragment getItem(int position) {
		switch (position) {
			case MyApplication.ACTIVITY_PAGE_PLAYED:
				if (fragContacts == null)
					fragContacts = new ContactsFragment();
				return fragContacts;

			case MyApplication.ACTIVITY_PAGE_MOST:
				if (fragAllowed == null)
					fragAllowed = new AllowedContactsFragment();
				return fragAllowed;

			case MyApplication.ACTIVITY_PAGE_ADDED:
				if (fragBlocked == null)
					fragBlocked = new BlockedContactsFragment();
				return fragBlocked;

		}
		return null;
	}

	@Override
	public int getCount() {
		return tabTitles.length;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		return null;
//		return tabTitles[position];
	}

}
