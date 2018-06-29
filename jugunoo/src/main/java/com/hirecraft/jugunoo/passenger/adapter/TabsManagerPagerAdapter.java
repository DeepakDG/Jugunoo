package com.hirecraft.jugunoo.passenger.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.hirecraft.jugunoo.passenger.fragments.GroupFragment_New;
import com.hirecraft.jugunoo.passenger.fragments.ProfileFragment;

public class TabsManagerPagerAdapter extends FragmentPagerAdapter
{
	public TabsManagerPagerAdapter(FragmentManager fm)
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
				return new GroupFragment_New();
		}

		return null;
	}

	@Override
	public int getCount()
	{
		// get item count - equal to number of tabs
		return 2;
	}
}
