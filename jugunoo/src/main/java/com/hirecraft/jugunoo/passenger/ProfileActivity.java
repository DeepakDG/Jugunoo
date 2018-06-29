package com.hirecraft.jugunoo.passenger;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.hirecraft.jugunoo.passenger.common.Constant;
import com.hirecraft.jugunoo.passenger.common.TransparentProgressDialog;
import com.hirecraft.jugunoo.passenger.fragments.ChangePasswordActivity;
import com.hirecraft.jugunoo.passenger.networkhandler.NetworkHandler;
import com.hirecraft.jugunoo.passenger.services.VolleyErrorHelper;
import com.hirecraft.jugunoo.passenger.utility.SharedPreferencesManager;

public class ProfileActivity extends Activity implements OnClickListener
{
	private static final String TAG = ProfileActivity.class.getSimpleName();
	private SharedPreferencesManager mgr;
	private TransparentProgressDialog pd;
	private Handler pdHandler;
	private Runnable pdRunnable;
	private Typeface light, semibold;
	private ProgressBar progressBar;
	private TextView mobile, title, exporttxt, txtptrip, txtpdistance,
			txtpamount, txtctrip;

	public static String gName = "";
	public static String gMobile = "";
	public static String gEmail = "";
	public static String gptrip = "";
	public static String gpdistance = "";
	public static String gpamount = "";
	private ActionBar actionBar;
	private LinearLayout titleL;
	static ImageView changePwds, back;
	private LinearLayout llNetworkErr;
	private ScrollView rlProfileMain;
	private Button retryBtn, emaileditBtn, addcorporate, button_fav_driver;
	private LinearLayout corporatedetails, Accountheadingll, Normalemailll,
			corporatedetailsll;
	private View mobileview, Accountview;
	private TableRow corporate_row;
	private EditText email, name;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.profile);

		SetActionBar();

		try
		{
			Init();
			Log.d(TAG, "user_id " + mgr.getPreferenceIndex("UserID"));

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void Init()
	{
		mgr = new SharedPreferencesManager(getApplicationContext());
		pd = new TransparentProgressDialog(getApplicationContext(),
				R.drawable.loading_image);
		pdHandler = new Handler();

		light = Typeface.createFromAsset(getApplicationContext().getAssets(),
				"fonts/opensans-regular-webfont.ttf");

		semibold = Typeface.createFromAsset(
				getApplicationContext().getAssets(),
				"fonts/opensans-semibold-webfont.ttf");

		mobile = (TextView) findViewById(R.id.mobilep);
		name = (EditText) findViewById(R.id.namep);
		email = (EditText) findViewById(R.id.normal_emailp);

		txtptrip = (TextView) findViewById(R.id.txt_ptrips);
		txtpdistance = (TextView) findViewById(R.id.txt_pkms);
		txtpamount = (TextView) findViewById(R.id.txt_pamount);

		progressBar = (ProgressBar) findViewById(R.id.profileProgress);
		progressBar.setVisibility(View.GONE);

		rlProfileMain = (ScrollView) findViewById(R.id.main_container);
		llNetworkErr = (LinearLayout) findViewById(R.id.profile_ErrorState);
		retryBtn = (Button) findViewById(R.id.profile_retrybtn);
		corporatedetails = (LinearLayout) findViewById(R.id.pcorporate_heading);
		corporatedetailsll = (LinearLayout) findViewById(R.id.corporate_detailsll);
		mobileview = findViewById(R.id.mobile_view);
		corporate_row = (TableRow) findViewById(R.id.corporate_row);
		Accountheadingll = (LinearLayout) findViewById(R.id.normal_pheading);
		Accountview = findViewById(R.id.normal_pview);
		Normalemailll = (LinearLayout) findViewById(R.id.normal_emailpll);
		emaileditBtn = (Button) findViewById(R.id.but_emailedit);
		button_fav_driver = (Button) findViewById(R.id.favorite_but);

		addcorporate = (Button) findViewById(R.id.paddcorp);
		mobileview.setVisibility(View.VISIBLE);
		corporatedetails.setVisibility(View.VISIBLE);
		corporate_row.setVisibility(View.GONE);
		Accountheadingll.setVisibility(View.VISIBLE);
		Accountview.setVisibility(View.VISIBLE);
		Normalemailll.setVisibility(View.VISIBLE);
		corporatedetailsll.setVisibility(View.GONE);
		emaileditBtn.setBackgroundResource(R.drawable.ic_action_edit);
		addcorporate.setBackgroundResource(R.drawable.ic_action_new_black);
		emaileditBtn.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				if (emaileditBtn.getBackground().getConstantState() == getResources()
						.getDrawable(R.drawable.ic_action_edit)
						.getConstantState())
				{
					name.setFocusableInTouchMode(true);
					name.setEnabled(true);
					email.setFocusableInTouchMode(true);
					email.setEnabled(true);
					emaileditBtn
							.setBackgroundResource(R.drawable.ic_action_accept);
				}
				else
				{
					makePersonalProfileUpdate();
				}
			}
		});

		retryBtn.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				makeGetProfileRequest();
			}
		});

		button_fav_driver.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				startActivity(new Intent(getApplicationContext(),
						FavoriteDriversListActivity.class));
			}
		});

		name.setTypeface(light);
		mobile.setTypeface(light);
		// title.setTypeface(semibold);
		email.setTypeface(light);

		// String usertype = mgr.GetValueFromSharedPrefs("FleetUserType");
		// Log.d(TAG, "usertype=" + usertype);
		//
		// if (usertype.equalsIgnoreCase("")) {
		// makeGetProfileRequest();
		// } else if (usertype.equalsIgnoreCase("Normal")) {
		makeGetProfileRequest();
		// }
		addcorporate.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				Intent intent = new Intent(ProfileActivity.this, AddCorporateInfoActivity.class);
				intent.putExtra("FirstName", gName);
				startActivity(intent);
			}
		});
	}

	private void showLoadingDilog()
	{
		// Showing loading dialog

		pdHandler = new Handler();
		pd = new TransparentProgressDialog(ProfileActivity.this,
				R.drawable.loading_image);

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
	}

	private void cancelLoadingDialog()
	{
		// Cancel loading dialog

		pdHandler.removeCallbacks(pdRunnable);

		if (pd.isShowing())
		{
			pd.dismiss();
		}
	}

	private void SetActionBar()
	{
		actionBar = getActionBar();
		ActionBar.LayoutParams layoutParams = new ActionBar.LayoutParams(
				ActionBar.LayoutParams.MATCH_PARENT,
				ActionBar.LayoutParams.MATCH_PARENT);
		layoutParams.gravity = Gravity.CENTER_HORIZONTAL
				| Gravity.CENTER_VERTICAL;

		RelativeLayout l = new RelativeLayout(ProfileActivity.this);
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

	private void manageView(boolean Netstatus)
	{
		if (Netstatus)
		{
			rlProfileMain.setVisibility(View.VISIBLE);
			llNetworkErr.setVisibility(View.GONE);
		}
		else
		{
			rlProfileMain.setVisibility(View.GONE);
			llNetworkErr.setVisibility(View.VISIBLE);
		}
	}

	private Handler handler = new Handler(new Handler.Callback()
	{
		@Override
		public boolean handleMessage(Message msg)
		{
			switch (msg.arg1)
			{
				case Constant.MessageState.USER_PROFILE_SUCCESS:
					pdHandler.removeCallbacks(pdRunnable);
					cancelLoadingDialog();

					try
					{
						manageView(true);
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}

					JSONObject fleets = (JSONObject) msg.obj;
					PrepareProfile(fleets);
					break;

				case Constant.MessageState.USER_PROFILE_FAIL:
					pdHandler.removeCallbacks(pdRunnable);
					cancelLoadingDialog();

					try
					{
						manageView(false);
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
					Log.e(TAG, "Null Groups");
					break;

				case Constant.MessageState.UPDATEPROFILE_STATUS_SUCCESS:
					pdHandler.removeCallbacks(pdRunnable);
					cancelLoadingDialog();

					try
					{
						manageView(true);
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}

					JSONObject updatedobj = (JSONObject) msg.obj;
					PreparePersonalProfile(updatedobj);

				case Constant.MessageState.UPDATEPROFILE_STATUS_FAIL:
					pdHandler.removeCallbacks(pdRunnable);
					cancelLoadingDialog();

					try
					{
						manageView(false);
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
					Log.e(TAG, "Null Groups");
					break;

			}
			return false;
		}

	});

	private void makeGetProfileRequest()
	{
		showLoadingDilog();
		String userID = mgr.GetValueFromSharedPrefs("UserID");
		Log.d(TAG, "userID=" + userID);
		NetworkHandler.GetProfile(TAG, handler, userID);
	}

	private void makePersonalProfileUpdate()
	{
		showLoadingDilog();
		String userID = mgr.GetValueFromSharedPrefs("UserID");
		String newname = name.getText().toString();
		String newemail = email.getText().toString();
		Log.d(TAG, "userID=" + userID);
		Map<String, String> personalParams = new HashMap<String, String>();
		personalParams.put("Status", "P");
		personalParams.put("UserId", userID);
		personalParams.put("FirstName", newname);
		personalParams.put("Email", newemail);
		NetworkHandler.updateUserProfile(TAG, handler, personalParams);
	}

	private void PreparePersonalProfile(JSONObject obj)
	{
		try
		{
			String resultStr = obj.getString("Result");
			if (resultStr.equalsIgnoreCase("Pass"))
			{
				makeGetProfileRequest();
			}
			else
			{
				Log.i("Fleet Listener", "Profile null");
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	private void PrepareProfile(JSONObject object)
	{
		try
		{
			emaileditBtn.setBackgroundResource(R.drawable.ic_action_edit);
			email.setEnabled(false);
			name.setEnabled(false);
			String resultStr = object.getString("Result");
			// String type = object.getString("UserType");
			// mgr.SaveValueToSharedPrefs("FleetUserType", type);

			if (resultStr.equalsIgnoreCase("Pass"))
			{
				String userDetail = object.getString("UserDetails");
				JSONObject objd = new JSONObject(userDetail);
				String name = objd.getString("FirstName");
				String mobile = objd.getString("Mobile");
				String email = objd.getString("Email");

				JSONArray triparray = object.getJSONArray("TripDetails");

				for (int i = 0; i < triparray.length(); i++)
				{
					JSONObject c = triparray.getJSONObject(i);
					gptrip = c.getString("PersonalTrip");
					gpdistance = c.getString("PersonalDistance");
					gpamount = c.getString("PersonalAmount");

				}
				gName = name;
				gMobile = mobile;
				gEmail = email;
				txtptrip.setText(gptrip);
				txtpdistance.setText(gpdistance);
				txtpamount.setText(gpamount);
				UpdateProfile(name, mobile, email);
			}
			else
			{
				Log.i("Fleet Listener", "Profile null");
			}

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void UpdateProfile(String nam, String mobil, String emai)
	{
		if (!nam.equalsIgnoreCase(""))
		{
			name.setText(nam);
		}
		else
			name.setVisibility(View.GONE);
		if (!mobil.equalsIgnoreCase(""))
		{
			mobile.setText(mobil);
		}
		else
			mobile.setVisibility(View.GONE);
		if (!emai.equalsIgnoreCase(""))
		{
			email.setText(emai);
		}
		else
			email.setVisibility(View.GONE);
	}

	@Override
	public void onStart()
	{
		super.onStart();
	}

	@Override
	public void onResume()
	{
		super.onResume();
	}

	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
			case R.id.nav_iconL:
				startActivity(new Intent(ProfileActivity.this,
						LandingPage.class));
				ProfileActivity.this.finish();
				break;

			case R.id.barChangePwd:
				startActivity(new Intent().setClass(ProfileActivity.this,
						ChangePasswordActivity.class));
				break;
		}

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
