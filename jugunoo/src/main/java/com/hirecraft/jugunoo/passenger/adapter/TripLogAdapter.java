package com.hirecraft.jugunoo.passenger.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.hirecraft.jugunoo.passenger.triplog.TripOngoingFragment;
import com.hirecraft.jugunoo.passenger.triplog.TripCompletedFragment;
import com.hirecraft.jugunoo.passenger.triplog.TripPendingFragment;

public class TripLogAdapter extends FragmentPagerAdapter  {
	public TripLogAdapter(FragmentManager fgm) {
        super(fgm);
    }
 
    @Override
    public Fragment getItem(int index) {
 
        switch (index) {
        case 0:
        	return new TripPendingFragment();
        case 1:
        	return new TripOngoingFragment();
        case 2:
        	return new TripCompletedFragment();
        }
 
        return null;
    }
 
    @Override
    public int getCount() {
        // get item count - equal to number of tabs
        return 3;
    }
}
