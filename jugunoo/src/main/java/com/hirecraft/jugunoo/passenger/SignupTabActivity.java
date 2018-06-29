package com.hirecraft.jugunoo.passenger;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.internal.nu;
import com.hirecraft.jugunoo.passenger.adapter.SignupTabAdapter;
import com.hirecraft.jugunoo.passenger.common.Global;
import com.hirecraft.jugunoo.passenger.fragments.CorporateSignupFragment;

public class SignupTabActivity extends FragmentActivity implements
		ActionBar.TabListener, OnClickListener
{

	private ViewPager viewPager;
	private SignupTabAdapter mAdapter;
	private ActionBar actionBar;
	private String[] tabs =
	{ "Corporate", "Personal" };
	private int tabPosition;

	private RelativeLayout titleL;
	static ImageView imgSignupNext, back;

	@Override
	protected void onCreate(Bundle bundle)
	{
		super.onCreate(bundle);
		setContentView(R.layout.signup_tabs_fragment);
		SetActionBar();
		init();
	}

	private void SetActionBar()
	{
		ActionBar actionBar = getActionBar();
		ActionBar.LayoutParams layoutParams = new ActionBar.LayoutParams(
				ActionBar.LayoutParams.MATCH_PARENT,
				ActionBar.LayoutParams.MATCH_PARENT);
		layoutParams.gravity = Gravity.CENTER_HORIZONTAL
				| Gravity.CENTER_VERTICAL;

		RelativeLayout l = new RelativeLayout(this);
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View bar = inflater.inflate(R.layout.custom_signup_action_bar, l);

		RelativeLayout back = (RelativeLayout) bar
				.findViewById(R.id.rl_nav_signup);
		TextView title = (TextView) bar.findViewById(R.id.tvScreenTitleSignUp);
		ImageView imageView = (ImageView) bar.findViewById(R.id.imgSignupNext);
		title.setText("Sign Up");

		back.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				onBackPressed();
			}
		});

		imageView.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{

			}
		});

		actionBar.setCustomView(bar, layoutParams);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowCustomEnabled(true);
		actionBar.setHomeButtonEnabled(false);
	}

	@Override
	public void onBackPressed()
	{
		super.onBackPressed();
		SignupTabActivity.this.finish();
		startActivity(new Intent().setClass(SignupTabActivity.this,
				SplashScreen.class));
	}

	private void init()
	{
		actionBar = getActionBar();
		viewPager = (ViewPager) findViewById(R.id.pager_signup);
		mAdapter = new SignupTabAdapter(getSupportFragmentManager());
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

				}
				else
				{

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
	public void onClick(View v)
	{
		switch (v.getId())
		{
			case R.id.nav_iconL:
				SignupTabActivity.this.finish();
				startActivity(new Intent(SignupTabActivity.this,
						SplashScreen.class));
				break;
		}
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
	public void onStop()
	{
		super.onStop();
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
	}

}
