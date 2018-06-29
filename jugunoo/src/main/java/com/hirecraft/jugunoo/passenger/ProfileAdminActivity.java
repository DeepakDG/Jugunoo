package com.hirecraft.jugunoo.passenger;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hirecraft.jugunoo.passenger.adapter.TabsAdminPagerAdapter;
import com.hirecraft.jugunoo.passenger.fragments.ChangePasswordActivity;

public class ProfileAdminActivity extends FragmentActivity implements
		ActionBar.TabListener, OnClickListener
{

	// private TabsPagerAdapter mAdapter;
	// private String[] tabs ={ "Profile", "Fleet", "Groups", "Users" };
	// RelativeLayout ErrorMessagerelative;
	private static final String TAG = ProfileAdminActivity.class
			.getSimpleName();
	LinearLayout titleL;
	static ImageView changePwds, back;
	private int tabPosition;
	// private Fragment fragment;
	TextView ErrorMessagetext;

	private ViewPager viewPager;
	private TabsAdminPagerAdapter mAdapter;
	private ActionBar actionBar;
	// Tab titles
	private String[] tabs =
	{ "Profile", "Fleet", "Groups" };

	@Override
	protected void onCreate(Bundle arg0)
	{
		super.onCreate(arg0);
		setContentView(R.layout.profile_tabs_fragment);
		SetActionBar();
		init();
	}

	private void SetActionBar()
	{
		actionBar = getActionBar();
		ActionBar.LayoutParams layoutParams = new ActionBar.LayoutParams(
				ActionBar.LayoutParams.MATCH_PARENT,
				ActionBar.LayoutParams.MATCH_PARENT);
		layoutParams.gravity = Gravity.CENTER_HORIZONTAL
				| Gravity.CENTER_VERTICAL;

		RelativeLayout l = new RelativeLayout(ProfileAdminActivity.this);
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View bar = inflater.inflate(R.layout.custom_action_bar, l);
		titleL = (LinearLayout) bar.findViewById(R.id.nav_iconL);
		TextView title = (TextView) bar.findViewById(R.id.screenTitle);
		changePwds = (ImageView) bar.findViewById(R.id.barChangePwd);
		// refreshBar = (ImageView) bar.findViewById(R.id.refreshBar);
		// back = (ImageView) bar.findViewById(R.id.nav_icon);
		// back.setOnClickListener(this);
		titleL.setOnClickListener(this);
		changePwds.setOnClickListener(this);
		// refreshBar.setOnClickListener(this);

		title.setText("Profile");
		actionBar.setCustomView(bar, layoutParams);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowCustomEnabled(true);

		// viewPager = (ViewPager) findViewById(R.id.pager);
		// mAdapter = new TabsPagerAdapter(getSupportFragmentManager());
		//
	}

	private void init()
	{
		actionBar = getActionBar();
		viewPager = (ViewPager) findViewById(R.id.pager);
		mAdapter = new TabsAdminPagerAdapter(getSupportFragmentManager());
		viewPager.setAdapter(mAdapter);
		actionBar.setHomeButtonEnabled(false);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// Adding tabs
		for (String tab_name : tabs)
		{
			actionBar.addTab(actionBar.newTab().setText(tab_name)
					.setTabListener(this));
		}

		viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener()
		{

			@Override
			public void onPageSelected(int position)
			{
				actionBar.setSelectedNavigationItem(position);
				if (position == 0)
				{
					changePwds.setVisibility(View.VISIBLE);
				}
				else
				{
					changePwds.setVisibility(View.GONE);
				}

			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2)
			{

			}

			@Override
			public void onPageScrollStateChanged(int arg0)
			{

			}
		});

	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft)
	{
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft)
	{
		// on tab selected
		// show respected fragment view
		viewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft)
	{
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState)
	{
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putInt("mMyCurrentPosition", tabPosition);
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState)
	{
		super.onRestoreInstanceState(savedInstanceState);
		tabPosition = savedInstanceState.getInt("mMyCurrentPosition");
	}

	@Override
	protected void onResume()
	{
		super.onResume();
	}

	@Override
	public void onBackPressed()
	{
		if (viewPager.getCurrentItem() == 0)
		{
			// Back button. This calls finish() on this activity and pops the
			// back stack.
			super.onBackPressed();
		}
		else
		{
			// Otherwise, select the previous step.
			viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
		}
	}

	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
			case R.id.nav_iconL:
				startActivity(new Intent(ProfileAdminActivity.this,
						LandingPage.class));
				ProfileAdminActivity.this.finish();
				break;

			case R.id.barChangePwd:

				// changPwdPopup();

				startActivity(new Intent().setClass(ProfileAdminActivity.this,
						ChangePasswordActivity.class));
				break;
		}
	}

	private void changPwdPopup()
	{
		LinearLayout viewGroup = (LinearLayout) findViewById(R.id.menu);
		LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = layoutInflater.inflate(R.layout.custom_menu_item_row,
				viewGroup);

		/**
		 * Creating the PopupWindow
		 */
		final PopupWindow popup = new PopupWindow(layout,
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		popup.setContentView(layout);
		popup.setFocusable(true);

		/**
		 * Clear the default translucent background
		 */
		popup.setBackgroundDrawable(new BitmapDrawable());
		popup.showAsDropDown(changePwds);

		/**
		 * Getting a reference to Close button, and close the popup when
		 * clicked.
		 */

		Button btn = (Button) layout.findViewById(R.id.menu_item);
		btn.setText("Change Password");
		btn.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				popup.dismiss();
				startActivity(new Intent().setClass(ProfileAdminActivity.this,
						ChangePasswordActivity.class));
			}
		});
	}

	@Override
	public void onStop()
	{
		super.onStop();
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
	}

	// public void showDialog(String message)
	// {
	//
	// // showErrorMessage(getApplicationContext(), ErrorMessagerelative,
	// // ErrorMessagetext, message);
	//
	// Function.showToast(getApplicationContext(), message);
	// }

}
