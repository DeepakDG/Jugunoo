package com.hirecraft.jugunoo.passenger;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hirecraft.jugunoo.passenger.common.ConstantMessages;
import com.hirecraft.jugunoo.passenger.common.Global;
import com.hirecraft.jugunoo.passenger.common.TransparentProgressDialog;
import com.hirecraft.jugunoo.passenger.utility.SharedPreferencesManager;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class AboutJugunoo extends Activity implements OnMenuItemClickListener
{

	SharedPreferencesManager mgr;
	private TransparentProgressDialog pd;
	private Handler pdHandler;
	private Runnable pdRunnable;
	Typeface light, bold, semibold;
	TextView content;
	// HireCraftSLideLayout mainLayout;
	// private ListView lvMenu;
	Dialog dialog = null;
	String[] member_names;
	TypedArray profile_pics;

	@SuppressLint(
	{ "Recycle", "InflateParams" })
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		SetActionBar();

		// mainLayout = (HireCraftSLideLayout) this.getLayoutInflater().inflate(
		// R.layout.activity_aboutus_page, null);
		setContentView(R.layout.activity_aboutus_page);

		// List<MenuItems> rowItems = new ArrayList<MenuItems>();
		// member_names =
		// getResources().getStringArray(R.array.nav_drawer_items);
		// profile_pics = getResources()
		// .obtainTypedArray(R.array.nav_drawer_icons);

		/*
		 * for (int i = 0; i < member_names.length; i++) {
		 * 
		 * MenuItems item = new MenuItems(member_names[i],
		 * profile_pics.getResourceId(i, -1)); rowItems.add(item); }
		 * 
		 * lvMenu = (ListView) findViewById(R.id.activity_main_menu_listview);
		 * MenuAdapter madapter = new MenuAdapter(AboutJugunoo.this, rowItems);
		 * lvMenu.setAdapter(madapter); lvMenu.setOnItemClickListener(new
		 * OnItemClickListener() {
		 * 
		 * @Override public void onItemClick(AdapterView<?> parent, View view,
		 * int position, long id) { onMenuItemClick(parent, view, position, id);
		 * }
		 * 
		 * });
		 */

		mgr = new SharedPreferencesManager(AboutJugunoo.this);
		pdHandler = new Handler();
		pd = new TransparentProgressDialog(AboutJugunoo.this,
				R.drawable.loading_image);
		Init();

		openAnimation();
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
		View bar = inflater.inflate(R.layout.custom_title_actionbar, l);

		LinearLayout back = (LinearLayout) bar.findViewById(R.id.llCTitle);
		TextView title = (TextView) bar.findViewById(R.id.tvTATitle);
		title.setText("About us");

		back.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				onBackPressed();
			}
		});

		actionBar.setCustomView(bar, layoutParams);

		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowCustomEnabled(true);
		actionBar.setHomeButtonEnabled(false);
	}

	/*
	 * public void toggleMenu(View v) { mainLayout.toggleMenu(); }
	 */

	@Override
	protected void onPause()
	{
		super.onPause();
		closeAnimation();
	}

	// @Override
	// public void finish()
	// {
	// if (pd != null)
	// {
	// if (pd.isShowing())
	// {
	// pd.dismiss();
	// }
	// }
	// if (dialog != null)
	// dialog.dismiss();
	//
	// super.finish();
	// }

	/*
	 * private void onMenuItemClick(AdapterView<?> parent, View view, int
	 * position, long id) {
	 * 
	 * switch (position) {
	 * 
	 * 
	 * case 0: startActivity(new Intent(AboutJugunoo.this,
	 * PassengerProfile.class)); AboutJugunoo.this.finish();
	 * 
	 * break;
	 * 
	 * case 1: startActivity(new Intent(AboutJugunoo.this,
	 * PassengerTripLog.class)); AboutJugunoo.this.finish(); break;
	 * 
	 * case 2: if (!mainLayout.isActivated()) { mainLayout.toggleMenu(); }
	 * break; case 3: JugunooInteractiveDialog("LOGOUT",
	 * "Do you really want to signout!"); break;
	 * 
	 * default: break; } // mainLayout.toggleMenu(); }
	 */

	@Override
	public void onBackPressed()
	{

		if (pd != null)
		{
			if (pd.isShowing())
			{
				pd.dismiss();
			}
		}
		if (dialog != null)
			dialog.dismiss();

		startActivity(new Intent(AboutJugunoo.this, LandingPage.class));
		super.finish();
		/*
		 * if (mainLayout.isMenuShown()) { mainLayout.toggleMenu(); } else {
		 * 
		 * }
		 */
	}

	private void openAnimation()
	{
		overridePendingTransition(R.anim.activity_open_translate,
				R.anim.activity_close_scale);
	}

	private void closeAnimation()
	{
		overridePendingTransition(R.anim.activity_open_scale,
				R.anim.activity_close_translate);
	}

	private void Logout(RequestParams params)
	{

		String userID = mgr.GetValueFromSharedPrefs("UserID");
		String Passenger_logout_url = Global.JUGUNOO_WS
				+ "Passenger/PassengerLogout?PassengerId=" + userID;
		AsyncHttpClient client = new AsyncHttpClient();

		pdRunnable = new Runnable()
		{
			@Override
			public void run()
			{
				if (pd != null)
				{
					if (pd.isShowing())
					{
						pd.dismiss();
					}
				}
			}
		};
		pd.show();
		client.post(Passenger_logout_url, params,
				new AsyncHttpResponseHandler()
				{

					@Override
					@Deprecated
					public void onFailure(int statusCode, Throwable error,
							String content)
					{

						try
						{
							if (statusCode == 200)
							{
								JugunooInteractiveDialog("NETWORK",
										ConstantMessages.MSG32);
							}
							else
							{
								JugunooInteractiveDialog("NETWORK",
										"Network Error..." + error.getMessage()
												+ "Try later");
							}
						}
						catch (Exception bug)
						{
							bug.printStackTrace();
						}

						super.onFailure(statusCode, error, content);
						pdHandler.removeCallbacks(pdRunnable);
						if (pd.isShowing())
						{
							pd.dismiss();
						}
					}

					@Override
					@Deprecated
					public void onSuccess(int statusCode, String content)
					{
						try
						{
							if (statusCode == 200)
							{
								JugunooInteractiveDialog("CANCEL",
										getString(R.string.logout_success_msg));
								mgr.SaveValueToSharedPrefs("UserID", "");
								mgr.SaveValueToSharedPrefs("UserRole", "");
								Intent logoutIntent = new Intent(
										AboutJugunoo.this, SplashScreen.class);
								logoutIntent
										.addFlags(Intent.FLAG_ACTIVITY_TASK_ON_HOME);
								startActivity(logoutIntent);
								AboutJugunoo.this.finish();

							}
							else
							{
								showDialog(ConstantMessages.MSG50);
							}

						}
						catch (Exception bug)
						{
							bug.printStackTrace();
						}

						pdHandler.removeCallbacks(pdRunnable);
						if (pd.isShowing())
						{
							pd.dismiss();
						}
					}

				});
	}

	private void JugunooInteractiveDialog(String title, String message)
	{
		dialog = new Dialog(AboutJugunoo.this);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		Window window = dialog.getWindow();
		window.setBackgroundDrawableResource(android.R.color.transparent);
		LinearLayout.LayoutParams dialogParams = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View dislogView = inflater.inflate(R.layout.jugunoo_interactive_dialog,
				new LinearLayout(AboutJugunoo.this));
		dialog.setContentView(dislogView, dialogParams);

		TextView textView = (TextView) dialog.findViewById(R.id.messageText);
		Button centerBtn = (Button) dialog.findViewById(R.id.centerBtn);
		Button leftBtn = (Button) dialog.findViewById(R.id.leftBtn);
		Button rightBtn = (Button) dialog.findViewById(R.id.rightBtn);

		textView.setTypeface(light);
		centerBtn.setTypeface(bold);
		leftBtn.setTypeface(bold);
		rightBtn.setTypeface(bold);

		if (title.equalsIgnoreCase("LOGOUT"))
		{

			centerBtn.setVisibility(View.GONE);
			String signOut = getResources().getString(R.string.signout);
			String notnow = getResources().getString(R.string.notnow);
			textView.setText(message);
			leftBtn.setText(notnow);
			rightBtn.setText(signOut);

			leftBtn.setOnClickListener(new OnClickListener()
			{

				@Override
				public void onClick(View v)
				{
					dialog.dismiss();
				}
			});
			rightBtn.setOnClickListener(new OnClickListener()
			{

				@Override
				public void onClick(View v)
				{
					String userId = mgr.GetValueFromSharedPrefs("UserID");
					RequestParams params = new RequestParams();
					params.put("PassengerId", userId);
					Logout(params);
					dialog.dismiss();
				}
			});
		}
		else if (title.equalsIgnoreCase("BOOK"))
		{

			leftBtn.setVisibility(View.GONE);
			rightBtn.setVisibility(View.GONE);
			String ok = getResources().getString(R.string.ok);
			textView.setText(message);
			centerBtn.setText(ok);
			centerBtn.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					dialog.dismiss();
				}
			});
		}
		else if (title.equalsIgnoreCase("NETWORK"))
		{

			leftBtn.setVisibility(View.GONE);
			rightBtn.setVisibility(View.GONE);
			String ok = getResources().getString(R.string.ok);
			textView.setText(message);
			centerBtn.setText(ok);
			centerBtn.setOnClickListener(new OnClickListener()
			{

				@Override
				public void onClick(View v)
				{
					dialog.dismiss();
				}
			});
		}
		else if (title.equalsIgnoreCase("CANCEL"))
		{

			leftBtn.setVisibility(View.GONE);
			rightBtn.setVisibility(View.GONE);
			String ok = getResources().getString(R.string.ok);
			textView.setText(message);
			centerBtn.setText(ok);
			centerBtn.setOnClickListener(new OnClickListener()
			{

				@Override
				public void onClick(View v)
				{
					dialog.dismiss();
				}
			});
		}
		else if (title.equalsIgnoreCase("LOCATION"))
		{

			leftBtn.setVisibility(View.GONE);
			rightBtn.setVisibility(View.GONE);
			String ok = getResources().getString(R.string.ok);
			textView.setText("Unable to get your current location. Please make sure getting the Google location servics!");
			centerBtn.setText(ok);
			centerBtn.setOnClickListener(new OnClickListener()
			{

				@Override
				public void onClick(View v)
				{
					dialog.dismiss();
				}
			});
		}
		else if (title.equalsIgnoreCase("GPS"))
		{

			centerBtn.setVisibility(View.GONE);
			String settings = getResources().getString(R.string.settings);
			String cancel = getResources().getString(R.string.cancel);
			textView.setText(message);
			leftBtn.setText(cancel);
			rightBtn.setText(settings);

			leftBtn.setOnClickListener(new OnClickListener()
			{

				@Override
				public void onClick(View v)
				{
					dialog.dismiss();
				}
			});
			rightBtn.setOnClickListener(new OnClickListener()
			{

				@Override
				public void onClick(View v)
				{
					Global.LOCATIONSERVICE_CALLBACK = "1";
					Intent settingIntent = new Intent(
							Settings.ACTION_LOCATION_SOURCE_SETTINGS);
					AboutJugunoo.this
							.startActivityForResult(settingIntent, 100);
					dialog.dismiss();
				}
			});
		}
		dialog.show();
	}

	private void Init()
	{
		light = Typeface.createFromAsset(getAssets(),
				"fonts/opensans-regular-webfont.ttf");
		bold = Typeface.createFromAsset(getAssets(),
				"fonts/opensans-bold-webfont.ttf");
		semibold = Typeface.createFromAsset(getAssets(),
				"fonts/opensans-semibold-webfont.ttf");
		content = (TextView) findViewById(R.id.contentAb);

		content.setTypeface(light);

		TextView terms = (TextView) findViewById(R.id.tvTerms);
		terms.setText(Html.fromHtml(getString(R.string.terms_text)));
	}

	public void onTerms(View v)
	{
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse(getString(R.string.terms_url)));
		startActivity(intent);
	}

	public void showDialog(String message)
	{
		// Toast.makeText(this, message, Toast.LENGTH_LONG).show();
	}

	@Override
	public boolean onMenuItemClick(MenuItem item)
	{
		return false;
	}

}
