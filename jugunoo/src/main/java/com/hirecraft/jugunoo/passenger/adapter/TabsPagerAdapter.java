package com.hirecraft.jugunoo.passenger.adapter;

import com.hirecraft.jugunoo.passenger.fragments.FleetOperatorFragment;
import com.hirecraft.jugunoo.passenger.fragments.GroupFragment_New;
import com.hirecraft.jugunoo.passenger.fragments.GroupsFragment;
import com.hirecraft.jugunoo.passenger.fragments.ProfileFragment;
import com.hirecraft.jugunoo.passenger.fragments.UsersFragment;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class TabsPagerAdapter extends FragmentPagerAdapter
{
	public TabsPagerAdapter(FragmentManager fm)
	{
		super(fm);
	}

	@Override
	public Fragment getItem(int index)
	{

		switch (index)
		{
			case 0:
				return new ProfileFragment();
			case 1:
				return new FleetOperatorFragment();
			case 2:
				return new GroupFragment_New();
				// case 3:
				// return new UsersFragment();
		}

		return null;
	}

	@Override
	public int getCount()
	{
		// get item count - equal to number of tabs
		return 3;
	}
}
