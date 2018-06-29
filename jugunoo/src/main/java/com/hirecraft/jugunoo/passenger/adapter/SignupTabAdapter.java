package com.hirecraft.jugunoo.passenger.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.hirecraft.jugunoo.passenger.fragments.CorporateSignupFragment;
import com.hirecraft.jugunoo.passenger.fragments.PersonalSignupFragment;

public class SignupTabAdapter extends FragmentPagerAdapter
{

	public SignupTabAdapter(FragmentManager fm)
	{
		super(fm);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Fragment getItem(int index)
	{

		switch (index)
		{
			case 0:
				return new CorporateSignupFragment();
			case 1:
				return new PersonalSignupFragment();
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
