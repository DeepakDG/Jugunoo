package com.hirecraft.jugunoo.passenger.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.hirecraft.jugunoo.passenger.fragments.GroupFragment_New;
import com.hirecraft.jugunoo.passenger.fragments.ProfileFragment;
import com.hirecraft.jugunoo.passenger.fragments.UsersFragment;

public class TabsManagingUserAdapter extends FragmentPagerAdapter {
	public TabsManagingUserAdapter(FragmentManager frag) {
		super(frag);
	}

	@Override
	public Fragment getItem(int index) {

		switch (index) {
		case 0:
			return new ProfileFragment();
		case 1:
			return new GroupFragment_New();
		}

		return null;
	}

	@Override
	public int getCount() {
		// get item count - equal to number of tabs
		return 2;
	}
}
