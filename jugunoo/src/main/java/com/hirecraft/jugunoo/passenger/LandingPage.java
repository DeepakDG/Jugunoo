package com.hirecraft.jugunoo.passenger;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.CancelableCallback;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CameraPosition.Builder;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.hirecraft.jugunoo.passenger.adapter.MenuAdapter;
import com.hirecraft.jugunoo.passenger.adapter.MenuItems;
import com.hirecraft.jugunoo.passenger.common.Constant;
import com.hirecraft.jugunoo.passenger.common.ConstantMessages;
import com.hirecraft.jugunoo.passenger.common.Global;
import com.hirecraft.jugunoo.passenger.common.TransparentProgressDialog;
import com.hirecraft.jugunoo.passenger.fragments.ProfileFragment;
import com.hirecraft.jugunoo.passenger.listeners.AddressResultListener;
import com.hirecraft.jugunoo.passenger.listeners.GcmIdListener;
import com.hirecraft.jugunoo.passenger.listeners.GetDurationAndDistance;
import com.hirecraft.jugunoo.passenger.listeners.GetMyAddressListener;
import com.hirecraft.jugunoo.passenger.listeners.LocationResultListener;
import com.hirecraft.jugunoo.passenger.locationmanager.LocationService;
import com.hirecraft.jugunoo.passenger.networkhandler.NetworkHandler;
import com.hirecraft.jugunoo.passenger.services.PassengerTripDirection;
import com.hirecraft.jugunoo.passenger.services.VolleyErrorHelper;
import com.hirecraft.jugunoo.passenger.triplog.TripLogActivity;
import com.hirecraft.jugunoo.passenger.utility.DistanceAndDuration;
import com.hirecraft.jugunoo.passenger.utility.GetMyAddress;
import com.hirecraft.jugunoo.passenger.utility.JugunooUtil;
import com.hirecraft.jugunoo.passenger.utility.SharedPreferencesManager;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

@SuppressLint("ClickableViewAccessibility")
public class LandingPage extends Activity implements LocationResultListener,
		AddressResultListener, OnCameraChangeListener, OnClickListener,
		GetMyAddressListener, GetDurationAndDistance, OnTouchListener,
		LocationListener, GcmIdListener
{

	private static final String TAG = LandingPage.class.getSimpleName();

	private LocationService mLocationService;
	private GoogleMap googleMap;

	private TransparentProgressDialog pd;
	private Handler pdHandler;
	private Runnable pdRunnable;

	private ProgressBar pickupProgress;
	// private boolean gpsStatus;
	// private boolean isPickUpStatus;

	String driverDeviceID = "", driverID, driverUserID = "", cabID = "";
	private ArrayList<HashMap<String, String>> mapList;
	private ArrayList<String> cabNoList = null;
	ImageView autoImg, miniImg, preImg, luxImg, sedanImg, ambulance, police,
			school, locationImg;
	Location location = null;
	LocationManager lm;
	SharedPreferencesManager mgr;
	TextView cabDuration, cabDisText, cabMessage, counterText, cabNoText,
			driverNameText, markerOvText, autoMin, miniMin, sedanMin, preMin,
			luxMin;
	Marker myMarker, cabMarker;
	Dialog trackerDialog = null, interactiveDialog = null;
	JugunooUtil util;
	DistanceAndDuration distanceAndDuration;

	List<double[]> autuDur, miniDur, sedanDur, preDur, luxDur;
	AddressResultListener mAddressResultListener;
	Geocoder geocoder;
	List<Address> destAddress, startAddress;
	String addressStr = "", distanceStr, durationStr = "";

	Address mLastKnownAddress;
	// private LinearLayout autoLayout, miniLayout, sedanLayout, premiumLayout,
	// luxLayout;
	AutoCompleteTextView pickPoint;
	LatLng pick;
	String dropAddress = "", dropLati = "", dropLongi = "";
	String pickStr = "";
	Timer timer;
	// HCTimer countDownTimer;

	Boolean isReceiverRegistered = false;
	ListView listViewPlaces;
	boolean isDrop = false;
	ArrayList<HashMap<String, String>> addresses;
	Typeface light, bold, semibold;

	ArrayList<LatLng> points;

	// private final long startTime = 10 * 1000;
	// private final long interval = 1000;
	private ListView lvMenu;
	ImageView btMenu;
	String[] member_names;
	TypedArray profile_pics;
	boolean searchStatus;

	private SlidingMenu menu;

	ArrayList<HashMap<String, String>> feedHash = null;
	int feedCount = 0, count = 0;
	Dialog feedDialog;
	String rate = "";
	String browserKey = Global.BROWSER_KEY;

	String cameraLatitude, cameraLongitude;

	private GetMyAddress address;

	private String G_PickUpLat, G_PickUpLong;

	private Boolean isNetworkRegistered;

	private CameraPosition centerPosVal;

	// tariff view
	private int clickCounter;
	private LinearLayout llCenter;
	private TextView tvNow, tvLater;
	// private LinearLayout llMain;
	// private TextView tvTariff;
	// private Button btnDest;
	private String cabType = "all";

	RelativeLayout Errorlandingrelative;
	TextView Errorlandingtext;

	private Dialog dialogCheckData = null;
	private Dialog gpsDialog = null;

	private LocationManager locationManager;
	private final long MIN_TIME = 400;
	private final float MIN_DISTANCE = 1000;

	private String GcmId = "";
	private String userId = "";
	private String deviceId = "";

	private Dialog dialog;
	private static String pendingrequestcount;
	List<MenuItems> rowItems;
	private MenuAdapter madapter;
	private MenuItems item;

	@SuppressLint(
	{ "InflateParams", "Recycle" })
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_landing_page);

		mgr = new SharedPreferencesManager(LandingPage.this);

		final String userType = mgr
				.GetNotiValueFromSharedPrefs(Constant.USER_TYPE);

		settingScreen();
		getPendingRequestCount();
		rowItems = new ArrayList<MenuItems>();

		if (!TextUtils.isEmpty(userType)
				&& (userType.equalsIgnoreCase(Constant.ADMIN) || userType
						.equalsIgnoreCase(Constant.MANAGER)))
		{

			member_names = getResources().getStringArray(
					R.array.nav_drawer_items_super);
			profile_pics = getResources().obtainTypedArray(
					R.array.nav_drawer_icons_super);

		}
		else if (!TextUtils.isEmpty(userType)
				&& userType.equalsIgnoreCase(Constant.GROUP_USER))
		{

			member_names = getResources().getStringArray(
					R.array.nav_drawer_items_Groupuser);
			profile_pics = getResources().obtainTypedArray(
					R.array.nav_drawer_icons_groupuser);
		}
		else
		{
			member_names = getResources().getStringArray(
					R.array.nav_drawer_items);
			profile_pics = getResources().obtainTypedArray(
					R.array.nav_drawer_icons);
		}

		for (int i = 0; i < member_names.length; i++)
		{

			item = new MenuItems(member_names[i], profile_pics.getResourceId(i,
					-1), pendingrequestcount);
			rowItems.add(item);
		}

		Errorlandingrelative = (RelativeLayout) findViewById(R.id.Errorlandingrelative);
		Errorlandingtext = (TextView) findViewById(R.id.Errorlandingtext);

		lvMenu = (ListView) findViewById(R.id.activity_main_menu_listview);
		madapter = new MenuAdapter(LandingPage.this, rowItems);
		lvMenu.setAdapter(madapter);
		lvMenu.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id)
			{

				if (!TextUtils.isEmpty(userType)
						&& (userType.equalsIgnoreCase(Constant.ADMIN) || userType
								.equalsIgnoreCase(Constant.MANAGER)))
				{
					onMenuItemClickSuper(parent, view, position, id);
				}
				else if (!TextUtils.isEmpty(userType)
						&& userType.equalsIgnoreCase(Constant.GROUP_USER))
				{
					onMenuItemClickGroupUser(parent, view, position, id);
				}
				else
				{
					onMenuItemNormal(parent, view, position, id);
				}
			}

		});

		btMenu = (ImageView) findViewById(R.id.action_menu_icon);
		btMenu.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{

				toggleMenu(v);
			}
		});

		mapList = new ArrayList<HashMap<String, String>>();

		InitScreen();

		mLocationService = new LocationService();
		mLocationService.getLocation(getApplicationContext(), this);

		mAddressResultListener = (AddressResultListener) this;
		mAddressResultListener.onAddressAvailable(mLastKnownAddress);

		geocoder = new Geocoder(getBaseContext(), Locale.getDefault());

		googleMap = ((MapFragment) getFragmentManager().findFragmentById(
				R.id.landingMap)).getMap();
		googleMap.getUiSettings().setZoomControlsEnabled(false);
		googleMap.getUiSettings().setCompassEnabled(false);
		googleMap.getUiSettings().setMyLocationButtonEnabled(false);
		googleMap.setMyLocationEnabled(true);
		googleMap.getUiSettings().setRotateGesturesEnabled(false);

		googleMap.setOnCameraChangeListener(this);

		// if (savedInstanceState != null) {
		// double lati_ = savedInstanceState.getDouble("Latitude");
		// double longi_ = savedInstanceState.getDouble("Longitude");
		// LandingPage.this.UpdateMyLocation(lati_, longi_);
		// }

		util = new JugunooUtil(getApplicationContext());

		cabNoList = new ArrayList<String>();
		autuDur = new ArrayList<double[]>();
		miniDur = new ArrayList<double[]>();
		sedanDur = new ArrayList<double[]>();
		preDur = new ArrayList<double[]>();
		luxDur = new ArrayList<double[]>();

		// String lati = mgr.GetValueFromSharedPrefs("Lati");
		// String longi = mgr.GetValueFromSharedPrefs("Longi");
		// if (!lati.equalsIgnoreCase("") && !longi.equalsIgnoreCase("")) {
		// LandingPage.this.UpdateMyLocation(Double.parseDouble(lati),
		// Double.parseDouble(longi));
		// }

		// network change
		isNetworkRegistered = true;
		registerReceiver(networkStateReceiver, new IntentFilter(
				ConnectivityManager.CONNECTIVITY_ACTION));

		openAnimation();

		// method to check trip AC or AB happens
		checkTripACorAB();
	}

	private void checkGcmID()
	{
		// Checking Device registered or not

		// Generating deviceId
		JugunooUtil util = new JugunooUtil(getApplicationContext());
		deviceId = util.getUniqueDeviceID(getApplicationContext());
		userId = mgr.GetValueFromSharedPrefs("UserID");

		String gcmId = mgr.GetValueFromSharedPrefs("GCM_ID");

		Log.i(TAG, "gcmId --> " + gcmId);
		Log.i(TAG,
				"gcmId status --> " + mgr.GetBoolFromSharedPrefs("isGcmSent"));

		if (!mgr.GetBoolFromSharedPrefs("isGcmSent"))
		{
			if (TextUtils.isEmpty(gcmId))
			{
				GenerateGcmId generateGcmId = new GenerateGcmId(
						getApplicationContext(), this, null);
				generateGcmId.execute();
			}
			else
			{
				// Generating deviceId
				HashMap<String, String> params = new HashMap<String, String>();
				params.put("UserId", userId);
				params.put("GcmId", gcmId);
				params.put("DeviceId", deviceId);

				makeRegistrationReq(params);
			}
		}
	}

	private void checkTripACorAB()
	{
		// method to check trip AC or AB happens
		try
		{
			if (getIntent().hasExtra("isAC")
					&& getIntent().getStringExtra("isAC").equalsIgnoreCase(
							"true"))
			{
				jugunooDialog(ConstantMessages.MSG91, ConstantMessages.MSG100);
			}
			else if (getIntent().hasExtra("isAB")
					&& getIntent().getStringExtra("isAB").equalsIgnoreCase(
							"true"))
			{
				jugunooDialog(ConstantMessages.MSG91, ConstantMessages.MSG101);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	// ** **//
	// Viewbadger code
	public void getPendingRequestCount()
	{
		String userid = mgr.GetValueFromSharedPrefs("UserID");
		NetworkHandler.GetPendingRequestCount(TAG, handler, userid);

	}

	private void parsePendingRequestCount(JSONObject obj)
	{
		try
		{
			String pendingCount = obj.getString("Count");
			String pendingResult = obj.getString("Result");
			if (pendingResult.equalsIgnoreCase("pass"))
			{
				pendingrequestcount = pendingCount;
//				Toast.makeText(getApplicationContext(),
//						"pendingrequestcount " + pendingrequestcount,
//						Toast.LENGTH_LONG).show();
//				madapter.notifyDataSetChanged();
				Log.d(TAG, "pending request" + pendingrequestcount);
			}
			else
			{
				Log.d(TAG, "null" + pendingrequestcount);
			}

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	private void jugunooDialog(String title, String message)
	{
		dialog = new Dialog(LandingPage.this);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.dialog_common_alert);
		dialog.setCancelable(false);

		TextView tvTitle = (TextView) dialog.findViewById(R.id.tvAlertHeader);
		TextView tvMsg = (TextView) dialog.findViewById(R.id.tvAlertMsg);
		Button btOk = (Button) dialog.findViewById(R.id.btAlertOk);

		tvTitle.setText(title);
		tvMsg.setText(message);
		btOk.setText("Ok");

		btOk.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				dialog.dismiss();
			}
		});

		dialog.show();
	}

	private void settingScreen()
	{
		menu = new SlidingMenu(this);
		menu.setMode(SlidingMenu.LEFT);
		menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
		menu.setShadowWidthRes(R.dimen.shadow_width);
		menu.setShadowDrawable(R.drawable.shadow);
		menu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		menu.setBackgroundColor(getResources().getColor(
				R.color.trans_white_shade));
		menu.setFadeEnabled(true);
		menu.setFadeDegree(0.45f);
		menu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
		menu.setMenu(R.layout.setting_list);
	}

	public void toggleMenu(View v)
	{
		if (!menu.isActivated())
		{
			menu.toggle();
		}
	}

	private void onMenuItemClickGroupUser(AdapterView<?> parent, View view,
			int position, long id)
	{

		switch (position)
		{

			case 0:
				// startActivity(new Intent(LandingPage.this,
				// PassengerProfile.class));

				Log.d("UserType",
						"UserType: "
								+ mgr.GetValueFromSharedPrefs(Constant.USER_TYPE));
				startActivity(new Intent(LandingPage.this,
						ProfileManagerUserActivity.class));
				break;

			case 1:
				startActivity(new Intent(LandingPage.this,
						TripLogActivity.class));
				break;
			case 2:
				final CharSequence[] prefTypes =
				{ "Personal", "Corporate" };
				AlertDialog.Builder alt_bld = new AlertDialog.Builder(this);
				alt_bld.setIcon(R.drawable.ic_launcher);
				alt_bld.setTitle("Select Preference");
				alt_bld.setSingleChoiceItems(prefTypes,
						mgr.getPreferenceIndex(Constant.PREFERENCE_INDEX),
						new DialogInterface.OnClickListener()
						{
							public void onClick(DialogInterface dialog, int item)
							{
								// Toast.makeText(getApplicationContext(),
								// "Prefrence Model = " + prefTypes[item],
								// Toast.LENGTH_SHORT).show();
								mgr.savePreferenceIndex(
										Constant.PREFERENCE_INDEX, item);
								resetView();

								preMin.setText("-NA-");
								miniMin.setText("-NA-");
								sedanMin.setText("-NA-");
								preMin.setText("-NA-");
								luxMin.setText("-NA-");
								autuDur.clear();
								miniDur.clear();
								sedanDur.clear();
								preDur.clear();
								luxDur.clear();
								mapList.clear();
								ApplyMins(mapList);
								GetAllCabs(mapList);
								googleMap.clear();

								dialog.dismiss();
							}
						});
				AlertDialog alert = alt_bld.create();
				alert.show();
				break;

			case 3:
				startActivity(new Intent(LandingPage.this, AboutJugunoo.class));
				break;

			case 4:
				JugunooInteractiveDialog("LOGOUT", ConstantMessages.MSG51);
				break;
			default:
				break;
		}
		if (!menu.isActivated())
		{
			menu.toggle();
		}
	}

	private void onMenuItemClickSuper(AdapterView<?> parent, View view,
			int position, long id)
	{

		switch (position)
		{

			case 0:
				// startActivity(new Intent(LandingPage.this,
				// PassengerProfile.class));
				if (mgr.GetValueFromSharedPrefs(Constant.USER_TYPE)
						.equalsIgnoreCase("Manager"))
				{
					startActivity(new Intent(LandingPage.this,
							ProfileManagerUserActivity.class));

				}
				else
				{
					startActivity(new Intent(LandingPage.this,
							ProfileAdminActivity.class));
				}
				break;

			case 1:
				startActivity(new Intent(LandingPage.this,
						TripLogActivity.class));

				break;

			case 2:

				final CharSequence[] prefTypes =
				{ "Personal", "Corporate" };
				AlertDialog.Builder alt_bld = new AlertDialog.Builder(this);
				alt_bld.setIcon(R.drawable.ic_launcher);
				alt_bld.setTitle("Select Preference");
				alt_bld.setSingleChoiceItems(prefTypes,
						mgr.getPreferenceIndex(Constant.PREFERENCE_INDEX),
						new DialogInterface.OnClickListener()
						{
							public void onClick(DialogInterface dialog, int item)
							{
								// Toast.makeText(getApplicationContext(),
								// "Prefrence Model = " + prefTypes[item],
								// Toast.LENGTH_SHORT).show();
								mgr.savePreferenceIndex(
										Constant.PREFERENCE_INDEX, item);
								resetView();

								preMin.setText("-NA-");
								miniMin.setText("-NA-");
								sedanMin.setText("-NA-");
								preMin.setText("-NA-");
								luxMin.setText("-NA-");
								autuDur.clear();
								miniDur.clear();
								sedanDur.clear();
								preDur.clear();
								luxDur.clear();
								mapList.clear();
								ApplyMins(mapList);
								GetAllCabs(mapList);
								googleMap.clear();

								dialog.dismiss();
							}
						});
				AlertDialog alert = alt_bld.create();
				alert.show();
				break;

			case 3:
				startActivity(new Intent(LandingPage.this, PendingRequest.class));
				break;

			case 4:

				startActivity(new Intent(LandingPage.this, AboutJugunoo.class));

				break;

			case 5:

				JugunooInteractiveDialog("LOGOUT", ConstantMessages.MSG51);

				break;

			default:
				break;
		}
		if (!menu.isActivated())
		{
			menu.toggle();
		}
	}

	private void onMenuItemNormal(AdapterView<?> parent, View view,
			int position, long id)
	{
		switch (position)
		{

			case 0:
				// startActivity(new Intent(LandingPage.this,
				// PassengerProfile.class));
				startActivity(new Intent(LandingPage.this,
						ProfileActivity.class));
				break;

			case 1:
				startActivity(new Intent(LandingPage.this,
						TripLogActivity.class));
				break;
			case 2:
				startActivity(new Intent(LandingPage.this, AboutJugunoo.class));
				break;

			case 3:
				JugunooInteractiveDialog("LOGOUT",
						"Do you really want to signout!");
				break;

			default:
				break;
		}
		if (!menu.isActivated())
		{
			menu.toggle();
		}
	}

	@Override
	public void onBackPressed()
	{

		if (menu.isMenuShowing())
		{

			menu.toggle();

		}
		else
		{
			super.onBackPressed();
		}

	}

	// private TextWatcher pickpointWatcher = new TextWatcher() {
	//
	// @Override
	// public void onTextChanged(CharSequence s, int start, int before,
	// int count) {
	//
	// }
	//
	// @Override
	// public void beforeTextChanged(CharSequence s, int start, int count,
	// int after) {
	// }
	//
	// @Override
	// public void afterTextChanged(Editable s) {
	//
	// pickStr = pickPoint.getText().toString();
	// if (count % 3 == 1 && pickStr.length() < 10) {
	// listViewPlaces.setVisibility(View.VISIBLE);
	// PlacesTask placesTask = new PlacesTask();
	// placesTask.execute(pickStr);
	// }
	//
	// }
	// };

	@Override
	public void onClick(View view)
	{
		switch (view.getId())
		{

			case R.id.followme:
				JugunooInteractiveDialog("SLIDE", "Share Ride");
				break;

			case R.id.location:
				getMyLocation();
				break;

			case R.id.jugunoo_auto:
				if (autuDur.size() != 0)
				{
					// GetDropLocation("auto");
				}
				break;

			case R.id.jugunoo_mini:
				// if (miniDur.size() != 0) {
				// GetDropLocation("mini");
				onCabSelect("mini", view);

				// }
				break;

			case R.id.jugunoo_premium:
				// if (preDur.size() != 0) {
				// GetDropLocation("premium");
				onCabSelect("premium", view);
				// }
				break;

			case R.id.jugunoo_luxury:
				// if (luxDur.size() != 0) {
				// GetDropLocation("luxy");
				onCabSelect("luxy", view);
				// }
				break;

			case R.id.jugunoo_sedan:
				// if (sedanDur.size() != 0) {
				// GetDropLocation("sedan");
				onCabSelect("sedan", view);
				// }
				break;

			case R.id.autoAction:
				if (autuDur.size() != 0)
				{
					getDropLocation("auto");
				}
				break;

			case R.id.miniAction:
				if (miniDur.size() != 0)
				{
					getDropLocation("mini");
				}
				break;

			case R.id.premiumAction:
				if (preDur.size() != 0)
				{
					getDropLocation("premium");
				}
				break;

			case R.id.sedanAction:
				if (sedanDur.size() != 0)
				{
					getDropLocation("sedan");
				}
				break;

			case R.id.ambulance:
				JugunooInteractiveDialog("SLIDE", "Ambulance");
				break;
			case R.id.police:
				JugunooInteractiveDialog("SLIDE", "Police");
				break;
			case R.id.school:
				JugunooInteractiveDialog("SLIDE", "School");
				break;

			case R.id.locationPickup:
				Intent getAddressIntent = new Intent(LandingPage.this,
						JugunooPlaceList.class);
				getAddressIntent.putExtra("formLp", "2");
				getAddressIntent.putExtra("form", "1");
				getAddressIntent.putExtra("address", pickPoint.getText()
						.toString());
				getAddressIntent.putExtra("formIdentity", "LandingPage");
				startActivityForResult(getAddressIntent, 200);
				break;

			case R.id.tvNow:
				// Toast.makeText(getApplicationContext(), "NOW " + cabType,
				// Toast.LENGTH_SHORT).show();
				if (miniDur.size() == 0 && sedanDur.size() == 0
						&& preDur.size() == 0 && luxDur.size() == 0)
				{
					// tvNow.setEnabled(false);

					// Toast.makeText(this, "No Cabs are available",
					// Toast.LENGTH_SHORT).show();

					// Function.showToast(LandingPage.this,
					// ConstantMessages.MSG52);

					showErrorMessage(getApplicationContext(),
							Errorlandingrelative, Errorlandingtext,
							ConstantMessages.MSG54);

				}
				else
				{
					// tvNow.setEnabled(true);
					// getDropLocation(cabType);

					if (cabType.equalsIgnoreCase("mini"))
					{
						if (miniDur.size() == 0)
						{
							// tvNow.setEnabled(false);
							// Toast.makeText(this,
							// "Mini cabs are not available",
							// Toast.LENGTH_SHORT).show();
							showErrorMessage(getApplicationContext(),
									Errorlandingrelative, Errorlandingtext,
									ConstantMessages.MSG55);

						}
						else
						{
							// tvNow.setEnabled(true);
							getDropLocation(cabType);
						}

					}

					if (cabType.equalsIgnoreCase("sedan"))
					{

						if (sedanDur.size() == 0)
						{
							// tvNow.setEnabled(false);
							// Toast.makeText(this,
							// "Sedan cabs are not available",
							// Toast.LENGTH_SHORT).show();
							showErrorMessage(getApplicationContext(),
									Errorlandingrelative, Errorlandingtext,
									ConstantMessages.MSG56);
						}
						else
						{
							// tvNow.setEnabled(true);
							getDropLocation(cabType);
						}

					}

					if (cabType.equalsIgnoreCase("premium"))
					{

						if (preDur.size() == 0)
						{
							// tvNow.setEnabled(false);
							// Toast.makeText(this,
							// "Premium cabs are not available",
							// Toast.LENGTH_SHORT).show();
							showErrorMessage(getApplicationContext(),
									Errorlandingrelative, Errorlandingtext,
									ConstantMessages.MSG57);

						}
						else
						{
							// tvNow.setEnabled(true);
							getDropLocation(cabType);
						}

					}

					if (cabType.equalsIgnoreCase("luxy"))
					{

						if (luxDur.size() == 0)
						{
							// tvNow.setEnabled(false);
							// Toast.makeText(this,
							// "Luxury cabs are not available",
							// Toast.LENGTH_SHORT).show();
							showErrorMessage(getApplicationContext(),
									Errorlandingrelative, Errorlandingtext,
									ConstantMessages.MSG58);
						}
						else
						{
							// tvNow.setEnabled(true);
							getDropLocation(cabType);
						}

					}

				}
				break;
			case R.id.tvLater:
				// Toast.makeText(getApplicationContext(), "LATER",
				// Toast.LENGTH_SHORT)
				// .show();

				String pickVal = pickPoint.getText().toString();
				Log.i(TAG, "pick val=" + pickVal);

				// if (!TextUtils.isEmpty(pickVal)) {

				Intent bookLaterIntent = new Intent(LandingPage.this,
						BookingFormNewActivity.class);
				bookLaterIntent.putExtra("addr", pickVal);
				bookLaterIntent.putExtra("pickLatLng", G_PickUpLat + ","
						+ G_PickUpLong);
				startActivity(bookLaterIntent);

				// }
				break;

		}

	}

	public static void showErrorMessage(final Context context,
			final RelativeLayout layout, final TextView textView, String message)
	{
		Animation slideDown = AnimationUtils.loadAnimation(context,
				R.anim.slide_up);

		layout.setVisibility(View.VISIBLE);
		layout.setAnimation(slideDown);
		textView.setText(message);

		// Creating handler
		final Handler handler = new Handler();

		// Creating thread
		Thread thread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					Thread.sleep(3000);
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}

				handler.post(new Runnable()
				{
					// This thread runs in the UI
					@Override
					public void run()
					{
						// Update the UI
						Animation slideUp = AnimationUtils.loadAnimation(
								context, R.anim.slide_down);

						layout.setAnimation(slideUp);
						layout.setVisibility(View.GONE);
					}
				});
			}
		});

		// Starting thread
		thread.start();
	}

	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		/**
		 * save the last location before the config changes.
		 */
		if (location != null)
		{

			outState.putDouble("Latitude", location.getLatitude());
			outState.putDouble("Longitude", location.getLongitude());
		}
	}

	// private void UpdateMyLocation(double lati, double longi) {
	//
	// try {
	//
	// LatLng myLatLng = new LatLng(lati, longi);
	// // myMarker = googleMap.addMarker(new MarkerOptions().position(
	// // myLatLng).icon(
	// // BitmapDescriptorFactory
	// // .fromResource(R.drawable.transparent)));
	//
	// CameraUpdate center = CameraUpdateFactory.newLatLng(myLatLng);
	// CameraUpdate zoom = CameraUpdateFactory.zoomTo(15);
	// googleMap.moveCamera(center);
	// googleMap.animateCamera(zoom);
	//
	// String lati_ = String.valueOf(lati);
	// String longi_ = String.valueOf(longi);
	// mgr.SaveValueToSharedPrefs("Lati", lati_);
	// mgr.SaveValueToSharedPrefs("Longi", longi_);
	//
	// } catch (Exception bug) {
	// bug.printStackTrace();
	// }
	//
	// }

	@Override
	public void finish()
	{
		if (interactiveDialog != null)
		{
			interactiveDialog.dismiss();
		}
		try
		{
			// if (countDownTimer != null) {
			// countDownTimer.cancel();
			// countDownTimer = null;
			// }
			if (timer != null)
			{
				timer.cancel();
				timer.purge();
				timer = null;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		super.finish();
	}

	private void openAnimation()
	{
		overridePendingTransition(R.anim.activity_open_translate,
				R.anim.activity_close_scale);
	}

	private void closeAnimation()
	{
		/**
		 * Closing transition animations.
		 */
		overridePendingTransition(R.anim.activity_open_scale,
				R.anim.activity_close_translate);
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		closeAnimation();

		try
		{
			if (dialogCheckData != null)
			{
				dialogCheckData.dismiss();
				dialogCheckData = null;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		try
		{
			if (gpsDialog != null)
			{
				gpsDialog.dismiss();
				gpsDialog = null;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void restoreActionBar()
	{
		ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setDisplayShowTitleEnabled(false);
	}

	private boolean checkGpsStatus()
	{
		/* Check GPS is On or off */
		boolean gpsStatus;
		locationManager = (LocationManager) this
				.getSystemService(LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				MIN_TIME, MIN_DISTANCE, this);
		gpsStatus = locationManager
				.isProviderEnabled(LocationManager.GPS_PROVIDER);
		return gpsStatus;
	}

	// private boolean checkGpsStatus()
	// {
	// boolean gpsStatus;
	//
	// LocationManager locationManager = (LocationManager) this
	// .getSystemService(LOCATION_SERVICE);
	//
	// locationManager.requestLocationUpdates(
	// LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE,
	// new LocationListener()
	// {
	//
	// @Override
	// public void onStatusChanged(String provider, int status,
	// Bundle extras)
	// {
	//
	// }
	//
	// @Override
	// public void onProviderEnabled(String provider)
	// {
	// // called when the GPS provider is turned off (user
	// // turning off the GPS
	// // on the phone)
	//
	// try
	// {
	// if (gpsDialog != null)
	// {
	// gpsDialog.dismiss();
	// gpsDialog = null;
	// }
	// }
	// catch (Exception e)
	// {
	// e.printStackTrace();
	// }
	// }
	//
	// @Override
	// public void onProviderDisabled(String provider)
	// {
	// // called when the GPS provider is turned on (user
	// // turning on the GPS on
	// // the phone)
	//
	// try
	// {
	// // if (gpsDialog != null)
	// // {
	// // gpsDialog.dismiss();
	// // gpsDialog = null;
	// // }
	//
	// if (gpsDialog == null)
	// {
	// AlertForEnableGPS();
	// }
	// else
	// {
	// gpsDialog.show();
	// }
	// }
	// catch (Exception e)
	// {
	// e.printStackTrace();
	// }
	// }
	//
	// @Override
	// public void onLocationChanged(Location location)
	// {
	//
	// }
	// });
	// gpsStatus = locationManager
	// .isProviderEnabled(LocationManager.GPS_PROVIDER);
	// return gpsStatus;
	// }

	public void showDialog(String message)
	{

		// Toast.makeText(LandingPage.this, message, Toast.LENGTH_LONG).show();
		showErrorMessage(getApplicationContext(), Errorlandingrelative,
				Errorlandingtext, message);

	}

	private void GetCabs(String lati, String longi, String cabType, int pref,
			String userId)
	{
		NetworkHandler.getCabsRequest(TAG, handler, lati, longi, cabType, pref,
				userId);
	}

	private double getDuration(String distance)
	{
		double dis = Double.parseDouble(distance);

		double dist = (double) Math.ceil(dis * 6);
		return dist;
	}

	private void addMarker(LatLng lnt, HashMap<String, String> map,
			int markerImages, String cabNo, String cabType)
	{
		try
		{
			MarkerOptions marker = new MarkerOptions();
			marker.position(lnt).icon(
					BitmapDescriptorFactory.fromResource(markerImages));
			Marker carMarker = googleMap.addMarker(new MarkerOptions()
					.position(lnt).anchor(0.5f, 0.5f)
					.icon(BitmapDescriptorFactory.fromResource(markerImages)));
			if (cabNoList.contains(cabNo))
			{
				AnimateMarker(carMarker, lnt, false);
			}
			else
			{
				cabNoList.add(cabNo);
				googleMap.addMarker(marker);
				carMarker.remove();
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	private void getMyLocation()
	{

		Log.e(TAG, "getMyLocation()");
		try
		{
			Location location = null;

			location = googleMap.getMyLocation();

			if (location == null)
			{
				location = this.location;
			}

			gpsHandler.removeCallbacks(gpsRunnable);

			if (location != null)
			{

				double lati = location.getLatitude();
				double longi = location.getLongitude();

				mgr.SaveValueToSharedPrefs("Lati", String.valueOf(lati));
				mgr.SaveValueToSharedPrefs("Longi", String.valueOf(longi));

				G_PickUpLat = lati + "";
				G_PickUpLong = longi + "";

				String userId = mgr.GetValueFromSharedPrefs("UserID");
				int prefIndex = mgr
						.getPreferenceIndex(Constant.PREFERENCE_INDEX);

				GetCabs(String.valueOf(lati), String.valueOf(longi), cabType,
						prefIndex, userId);

				LatLng target = new LatLng(location.getLatitude(),
						location.getLongitude());
				if (address == null)
				{
					address = new GetMyAddress(getApplicationContext(),
							location.getLatitude(), location.getLongitude(),
							this);
					address.execute();
				}

				pick = target;

				Builder builder = new CameraPosition.Builder();
				builder.zoom(15);
				builder.target(target);

				this.googleMap.animateCamera(CameraUpdateFactory
						.newCameraPosition(builder.build()));
			}
			else
			{
				gpsHandler.postDelayed(gpsRunnable, 1000);
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	private void Logout(Map<String, String> params)
	{

		try
		{
			showLoadingDilog();
			NetworkHandler.logoutRequest(TAG, handler, params);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}

	}

	@Override
	public void onLocationResultAvailable(final Location location)
	{
		// this.location = location;

		Log.e(TAG, "on loc avai");
		try
		{
			// LandingPage.this.runOnUiThread(new Runnable() {
			//
			// @Override
			// public void run() {
			if (location == null)
			{

				gpsHandler.post(gpsRunnable);

				Log.e(Global.APPTAG, "Location is null: ");
				// locationImg = (ImageView) findViewById(R.id.location);

				// locationImg.setVisibility(View.INVISIBLE);
				// countDownTimer = new HCTimer(startTime, interval);
				// countDownTimer.start();
			}
			else
			{
				Log.e(Global.APPTAG, "Location is not null: " + searchStatus);
				// locationImg = (ImageView) findViewById(R.id.location);

				this.location = location;

				// locationImg.setVisibility(View.VISIBLE);
				double lati = location.getLatitude();
				double longi = location.getLongitude();
				String lati_ = String.valueOf(lati);
				String longi_ = String.valueOf(longi);

				if (!searchStatus)
				{
					mgr.SaveValueToSharedPrefs("Lati", lati_);
					mgr.SaveValueToSharedPrefs("Longi", longi_);

					G_PickUpLat = lati_ + "";
					G_PickUpLong = longi_ + "";

					// gpsHandler.post(gpsRunnable);

					// UpdateMyLocation(lati, longi);
					// searchStatus = true;

					runOnUiThread(new Runnable()
					{

						@Override
						public void run()
						{
							SetPickpoint(G_PickUpLat, G_PickUpLong);
						}
					});

				}

			}

		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		// }
		// });
	}

	@Override
	public void onStop()
	{
		super.onStop();
		if (mLocationService != null)
		{
			mLocationService.stop();
		}

		try
		{
			if (dialogCheckData != null)
			{
				dialogCheckData.dismiss();
				dialogCheckData = null;
			}

			unregisterReceiver(mConnReceiver);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		try
		{
			if (gpsDialog != null)
			{
				gpsDialog.dismiss();
				gpsDialog = null;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		if (isNetworkRegistered)
		{
			unregisterReceiver(networkStateReceiver);
		}

		try
		{
			if (dialogCheckData != null)
			{
				dialogCheckData.dismiss();
				dialogCheckData = null;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		try
		{
			if (gpsDialog != null)
			{
				gpsDialog.dismiss();
				gpsDialog = null;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void setAddressDetails(Address address)
	{
		if (address.getMaxAddressLineIndex() > 0)
		{
			addressStr = address.getAddressLine(0);
		}
	}

	@Override
	public void onAddressAvailable(Address address)
	{
		if (address == null)
		{
			Log.i(Global.APPTAG, "Address is null");
		}
		else
		{
			mLastKnownAddress = address;
			setAddressDetails(address);
		}
	}

	@Override
	public void onCameraChange(CameraPosition position)
	{

		Log.e(TAG, "ON CAMERA CHANGE  " + searchStatus);

		if (location != null)
		{

			handler.removeCallbacksAndMessages(null);

			Message msg = new Message();
			msg.arg1 = CAMERA_CAHNGE_CALLBACK;
			msg.obj = position;

			handler.sendMessageDelayed(msg, CAMERA_DELAY);

		}

	}

	private final int CAMERA_CAHNGE_CALLBACK = 420;
	private final int CAMERA_DELAY = 1000;

	private void setOnCamChanged(CameraPosition position)
	{
		if (position != null)
		{
			try
			{
				Log.e(TAG, "set on cam chag");

				LatLng loc = position.target;
				double lati = loc.latitude;
				double longi = loc.longitude;

				pickPoint.setText("");
				pickPoint.setHint("Please wait...");
				pickupProgress.setVisibility(View.VISIBLE);

				if (address == null)
				{
					address = new GetMyAddress(getApplicationContext(), lati,
							longi, this);
					address.execute();
				}
				mgr.SaveValueToSharedPrefs("Lati", String.valueOf(lati));
				mgr.SaveValueToSharedPrefs("Longi", String.valueOf(longi));

				String userId = mgr.GetValueFromSharedPrefs("UserID");
				int prefIndex = mgr
						.getPreferenceIndex(Constant.PREFERENCE_INDEX);

				GetCabs(String.valueOf(lati), String.valueOf(longi), cabType,
						prefIndex, userId);

				G_PickUpLat = String.valueOf(loc.latitude);
				G_PickUpLong = String.valueOf(loc.longitude);

			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
	}

	@Override
	protected void onStart()
	{
		super.onStart();
		// gpsStatus = checkGpsStatus();
		// if (!gpsStatus)
		// {
		// // Log.i(Global.APPTAG, "GPS Exist.");
		// // } else {
		// mgr.SaveValueToSharedPrefs("Lati", "");
		// mgr.SaveValueToSharedPrefs("Longi", "");
		//
		// if (interactiveDialog == null)
		// {
		// JugunooInteractiveDialog("GPS", ConstantMessages.MSG59);
		// }
		// }

		// String pickup = pickPoint.getText().toString();
		// if (pickup.equalsIgnoreCase("")) {
		// getAddressSplash();
		// }
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		getPendingRequestCount();
		checkGcmID();

		Handler handler = new Handler();
		handler.postDelayed(new Runnable()
		{
			@Override
			public void run()
			{
				registerReceiver(mConnReceiver, new IntentFilter(
						ConnectivityManager.CONNECTIVITY_ACTION));
			}
		}, 5000);

		try
		{
			if (!checkGpsStatus())
			{
				AlertForEnableGPS();
			}

			getMyLocation();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		resetView();
	}

	@Override
	public void MyAddress(String address)
	{

		Log.e(TAG, "my addr=" + address);

		if (!TextUtils.isEmpty(address))
		{
			pickPoint.setText(address + ".");

		}
		else
		{

			handler.removeCallbacksAndMessages(null);

			Message msg = new Message();
			msg.arg1 = CAMERA_CAHNGE_CALLBACK;
			msg.obj = centerPosVal;

			handler.sendMessageDelayed(msg, CAMERA_DELAY);

			pickPoint.setHint(ConstantMessages.MSG60);
		}

		pickupProgress.setVisibility(View.INVISIBLE);
		this.address = null;

	}

	@Override
	public void GetDuration(String duration)
	{

		duration = durationStr;
	}

	@Override
	public void GetDistance(String distance)
	{

		cabDisText.setText(distance);

	}

	private void JugunooInteractiveDialog(String title, String message)
	{
		try
		{
			/* New Interactive alert Dialog */
			interactiveDialog = new Dialog(LandingPage.this);
			interactiveDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			interactiveDialog.setContentView(R.layout.dialog_common_alert);

			TextView tvTitle = (TextView) interactiveDialog
					.findViewById(R.id.tvAlertHeader);
			TextView tvMsg = (TextView) interactiveDialog
					.findViewById(R.id.tvAlertMsg);
			tvMsg.setTextColor(getResources().getColor(android.R.color.black));

			Button btSignOut = (Button) interactiveDialog
					.findViewById(R.id.btAlertOk);
			Button btNotNow = (Button) interactiveDialog
					.findViewById(R.id.btAlertCancel);
			btNotNow.setVisibility(View.VISIBLE);

			tvMsg.setText(message);
			tvTitle.setText(title);

			btSignOut.setText(getResources().getString(R.string.signout));
			btNotNow.setText(getResources().getString(R.string.notnow));

			if (title.equalsIgnoreCase("LOGOUT"))
			{
				btNotNow.setOnClickListener(new OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						interactiveDialog.dismiss();
					}
				});
				btSignOut.setOnClickListener(new OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						String userId = mgr.GetValueFromSharedPrefs("UserID");

						Map<String, String> params = new HashMap<String, String>();
						params.put("PassengerId", userId);
						Logout(params);
						interactiveDialog.dismiss();
					}
				});
			}
			interactiveDialog.show();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	private void InitScreen()
	{

		light = Typeface.createFromAsset(getAssets(),
				"fonts/opensans-regular-webfont.ttf");
		bold = Typeface.createFromAsset(getAssets(),
				"fonts/opensans-bold-webfont.ttf");
		semibold = Typeface.createFromAsset(getAssets(),
				"fonts/opensans-semibold-webfont.ttf");
		pickPoint = (AutoCompleteTextView) findViewById(R.id.locationPickup);
		// autoLayout = (LinearLayout) findViewById(R.id.autoAction);
		// miniLayout = (LinearLayout) findViewById(R.id.miniAction);
		// sedanLayout = (LinearLayout) findViewById(R.id.sedanAction);
		// premiumLayout = (LinearLayout) findViewById(R.id.premiumAction);
		// luxLayout = (LinearLayout) findViewById(R.id.luxuryAction);
		listViewPlaces = (ListView) findViewById(R.id.pickupPlaces);
		cabMessage = (TextView) findViewById(R.id.messageText);
		counterText = (TextView) findViewById(R.id.counterText);
		autoMin = (TextView) findViewById(R.id.cabMinsauto);
		miniMin = (TextView) findViewById(R.id.cabMinsmini);
		sedanMin = (TextView) findViewById(R.id.cabMinssedan);
		preMin = (TextView) findViewById(R.id.cabMinsPre);
		luxMin = (TextView) findViewById(R.id.cabMinsLuxury);
		ambulance = (ImageView) findViewById(R.id.ambulance);
		police = (ImageView) findViewById(R.id.police);
		school = (ImageView) findViewById(R.id.school);
		locationImg = (ImageView) findViewById(R.id.location);

		autoImg = (ImageView) findViewById(R.id.jugunoo_auto);
		miniImg = (ImageView) findViewById(R.id.jugunoo_mini);
		preImg = (ImageView) findViewById(R.id.jugunoo_premium);
		luxImg = (ImageView) findViewById(R.id.jugunoo_luxury);
		sedanImg = (ImageView) findViewById(R.id.jugunoo_sedan);

		pickupProgress = (ProgressBar) findViewById(R.id.pickupProgress);
		pickPoint.setTypeface(light);
		pickPoint.setFocusable(false);

		cabMessage.setTypeface(light);
		counterText.setTypeface(light);
		autoMin.setTypeface(light);
		miniMin.setTypeface(light);
		preMin.setTypeface(light);
		counterText.setTypeface(light);
		ambulance.setOnClickListener(this);
		police.setOnClickListener(this);
		school.setOnClickListener(this);
		locationImg.setOnClickListener(this);
		autoImg.setOnClickListener(this);
		miniImg.setOnClickListener(this);
		preImg.setOnClickListener(this);
		luxImg.setOnClickListener(this);
		sedanImg.setOnClickListener(this);

		// autoLayout.setOnClickListener(this);
		// miniLayout.setOnClickListener(this);
		// sedanLayout.setOnClickListener(this);
		// premiumLayout.setOnClickListener(this);
		// luxLayout.setOnClickListener(this);
		pickPoint.setOnClickListener(this);

		// tariff
//		llCenter = (LinearLayout) findViewById(R.id.llCenter);
//		llCenter.setOnTouchListener(this);
		// llMain = (LinearLayout) findViewById(R.id.llMain);

		tvNow = (TextView) findViewById(R.id.tvNow);
		tvNow.setOnClickListener(this);

		tvLater = (TextView) findViewById(R.id.tvLater);
		tvLater.setOnClickListener(this);

		// tvTariff = (TextView) findViewById(R.id.tvTariff);

		miniImg.setImageDrawable(getResources().getDrawable(
				R.drawable.select_mini));

		preImg.setImageDrawable(getResources().getDrawable(
				R.drawable.select_premium));

		sedanImg.setImageDrawable(getResources().getDrawable(
				R.drawable.select_sedan));

		luxImg.setImageDrawable(getResources().getDrawable(
				R.drawable.select_luxury));

		timer = new Timer();
		TimerTask updateProfile = new CustomTimerTask(LandingPage.this);
		timer.scheduleAtFixedRate(updateProfile, 10, 3000);

	}

	private void SetPickpoint(String lati, String longi)
	{
		try
		{
			LatLng target = new LatLng(Double.parseDouble(lati),
					Double.parseDouble(longi));
			Builder builder = new CameraPosition.Builder();
			builder.zoom(15);
			builder.target(target);

			// googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(builder
			// .build()));

			googleMap.animateCamera(
					CameraUpdateFactory.newCameraPosition(builder.build()),
					new CancelableCallback()
					{

						@Override
						public void onFinish()
						{

							Log.e(TAG, "camera finish");

							new Handler().postDelayed(new Runnable()
							{

								@Override
								public void run()
								{
									googleMap
											.setOnCameraChangeListener(LandingPage.this);
								}
							}, 300);

							// searchStatus = false;
						}

						@Override
						public void onCancel()
						{

							Log.e(TAG, "camera cancel");

							new Handler().postDelayed(new Runnable()
							{

								@Override
								public void run()
								{
									googleMap
											.setOnCameraChangeListener(LandingPage.this);
								}
							}, 300);

							// googleMap.setOnCameraChangeListener(LandingPage.this);
							// searchStatus = false;
						}
					});

		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	class CustomTimerTask extends TimerTask
	{

		Context context;
		Handler mHandler = new Handler();

		public CustomTimerTask(Context con)
		{
			this.context = con;
		}

		@Override
		public void run()
		{

			mHandler.post(new Runnable()
			{
				@Override
				public void run()
				{

					// String url = "";
					String lati = mgr.GetValueFromSharedPrefs("Lati");
					String longi = mgr.GetValueFromSharedPrefs("Longi");
					if (!TextUtils.isEmpty(lati) && !TextUtils.isEmpty(longi))
					{

						// url = Global.JUGUNOO_WS_LOCATION +
						// "location?latitude="
						// + lati + "&longitude=" + longi
						// + "&Client=Jugunoo";

						// if (!TextUtils.isEmpty(url)) {

						String userId = mgr.GetValueFromSharedPrefs("UserID");
						int prefIndex = mgr
								.getPreferenceIndex(Constant.PREFERENCE_INDEX);
						GetCabs(lati, longi, cabType, prefIndex, userId);
						// } else {
						// Log.i(Global.APPTAG, "Invalid Url.");
						// }
					}
				}

			});

		}
	}

	private void GetAllCabs(ArrayList<HashMap<String, String>> mapList)
	{
		try
		{
			int markerImages = 0;
			if (mapList.size() != 0)
			{

				for (HashMap<String, String> map : mapList)
				{
					String cabType = map.get("CabType");
					String loc = map.get("coordinates");
					loc = loc.replaceAll("\\[", "").replaceAll("\\]", "");
					StringTokenizer token = new StringTokenizer(loc, ",");
					String lon = token.nextToken();
					String lat = token.nextToken();

					double latitude = Double.parseDouble(lat);
					double longitude = Double.parseDouble(lon);

					LatLng latLngPoint = new LatLng(latitude, longitude);
					if (cabType.equalsIgnoreCase("auto"))
					{
						markerImages = R.drawable.auto_marker;
					}
					else if (cabType.equalsIgnoreCase("mini"))
					{
						markerImages = R.drawable.mini_marker;
					}
					else if (cabType.equalsIgnoreCase("sedan"))
					{
						markerImages = R.drawable.sedan_marker;
					}
					else if (cabType.equalsIgnoreCase("premium"))
					{
						markerImages = R.drawable.premium_marker;
					}
					else if (cabType.equalsIgnoreCase("luxy"))
					{
						markerImages = R.drawable.luxury_marker;
					}
					String cabNo = map.get("CabNo");
					if (this.cabType.equalsIgnoreCase(cabType))
					{
						addMarker(latLngPoint, map, markerImages, cabNo,
								cabType);
					}
					else if (this.cabType.equalsIgnoreCase("all"))
					{
						addMarker(latLngPoint, map, markerImages, cabNo,
								cabType);
					}
				}
				mapList.clear();
			}

		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	private void getDropLocation(String type)
	{
		try
		{

			String myLoc = "";
			Location myLocation = googleMap.getMyLocation();

			if (myLocation == null)
			{
				myLocation = this.location;
			}

			if (myLocation != null)
			{

				myLoc = myLocation.getLatitude() + ","
						+ myLocation.getLongitude();
			}

			String status = mgr.GetValueFromSharedPrefs("IsNotify");
			if (!status.equalsIgnoreCase("passengerGetIn")
					|| !status.equalsIgnoreCase("true"))
			{
				String pickPointStr = pickPoint.getText().toString();

				String pLatiLongi = G_PickUpLat + "," + G_PickUpLong;

				Log.e(TAG, "pickPointStr btn=" + pickPointStr + " "
						+ pLatiLongi);

				System.out.println("GET DROP LOCN---" + pLatiLongi);

				if (!TextUtils.isEmpty(pickPointStr)
						&& !TextUtils.isEmpty(pLatiLongi))
				{
					Intent i = new Intent(LandingPage.this,
							JugunooPlaceList.class);
					i.putExtra("type", type);
					i.putExtra("pLatiLongi", pLatiLongi);
					i.putExtra("pickPointStr", pickPointStr);
					i.putExtra("passengerLoc", myLoc);
					i.putExtra("form", "2");
					i.putExtra("formIdentity", "LandingPage");
					startActivity(i);

					finish();
				}
				else
				{
					Log.i(Global.APPTAG, "Null data");
				}
			}
			else
			{
				startActivity(new Intent(LandingPage.this,
						PassengerTripDirection.class));
				finish();
			}

		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	private void ApplyMins(ArrayList<HashMap<String, String>> mapList)
	{
		try
		{

			if (autuDur.size() != 0)
			{
				long i = getMinDuration(autuDur);
				autoMin.setText(i + " Min");

			}
			else
			{
				autoMin.setText("-NA-");
			}
			if (miniDur.size() != 0)
			{
				long i = getMinDuration(miniDur);
				miniMin.setText(i + " Min");
			}
			else
			{
				miniMin.setText("-NA-");
			}
			if (sedanDur.size() != 0)
			{
				long i = getMinDuration(sedanDur);
				sedanMin.setText(i + " Min");
			}
			else
			{
				sedanMin.setText("-NA-");
			}
			if (preDur.size() != 0)
			{
				long i = getMinDuration(preDur);
				preMin.setText(i + " Min");
			}
			else
			{
				preMin.setText("-NA-");
			}

			if (luxDur.size() != 0)
			{
				long i = getMinDuration(luxDur);
				luxMin.setText(i + " Min");
			}
			else
			{
				luxMin.setText("-NA-");
			}

		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	private long getMinDuration(List<double[]> list)
	{

		double min = Double.MAX_VALUE, max = Double.MIN_VALUE;
		for (double[] ds : list)
		{
			for (double d : ds)
			{
				if (d > max)
					max = d;
				if (d < min)
					min = d;
			}
		}
		long i = Math.round(min);
		if (i == 0)
		{
			i = 1;
		}
		return i;

	}

	public void AnimateMarker(final Marker marker, final LatLng toPosition,
			final boolean hideMarker)
	{
		final Handler handler = new Handler();
		final long start = SystemClock.uptimeMillis();
		Projection proj = googleMap.getProjection();
		Point startPoint = proj.toScreenLocation(marker.getPosition());
		final LatLng startLatLng = proj.fromScreenLocation(startPoint);
		final long duration = 500;

		final Interpolator interpolator = new LinearInterpolator();

		handler.post(new Runnable()
		{
			@Override
			public void run()
			{
				long elapsed = SystemClock.uptimeMillis() - start;
				float t = interpolator.getInterpolation((float) elapsed
						/ duration);
				double lng = t * toPosition.longitude + (1 - t)
						* startLatLng.longitude;
				double lat = t * toPosition.latitude + (1 - t)
						* startLatLng.latitude;
				marker.setPosition(new LatLng(lat, lng));

				if (t < 1.0)
				{

					handler.postDelayed(this, 10);
				}
				else
				{
					if (hideMarker)
					{
						marker.setVisible(false);
					}
					else
					{
						marker.setVisible(true);
					}
				}
			}
		});
	}

	// following code to be uncommented if mandatory feedback is required for
	// every trip
	/*
	 * private void CheckUserFeedback() {
	 * 
	 * mgr = new SharedPreferencesManager(LandingPage.this);
	 * 
	 * String userID = mgr.GetValueFromSharedPrefs("UserID"); String url =
	 * Global.JUGUNOO_WS + "Passenger/GetNotFeedBackedTrip?UserId=" + userID;
	 * 
	 * AsyncHttpClient client = new AsyncHttpClient(); client.setTimeout(70000);
	 * client.get(url, new AsyncHttpResponseHandler() {
	 * 
	 * @Override
	 * 
	 * @Deprecated public void onFailure(int statusCode, Throwable error, String
	 * content) { super.onFailure(statusCode, error, content);
	 * pdHandler.removeCallbacks(pdRunnable);
	 * 
	 * }
	 * 
	 * @Override
	 * 
	 * @Deprecated public void onSuccess(int statusCode, String content) {
	 * 
	 * try { if (statusCode == 200) {
	 * 
	 * try { JSONObject obj = new JSONObject(content); String result =
	 * obj.getString("Result"); if (result.equalsIgnoreCase("Pass")) { JSONArray
	 * ar = obj .getJSONArray("FeedBackArray");
	 * 
	 * count = ar.length(); if (count != 0) { feedHash = new
	 * ArrayList<HashMap<String, String>>(); JSONObject child =
	 * ar.getJSONObject(0); String startPoint = child .getString("PickPoint");
	 * String endPoint = child .getString("DropPoint"); String engID =
	 * child.getString("RID"); String driverId = child
	 * .getString("OtherUserId"); String engTime = child
	 * .getString("EngageTime");
	 * 
	 * GetDriverFeed(startPoint, endPoint, engID, driverId, engTime); } } }
	 * catch (Exception bug) { bug.printStackTrace(); } }
	 * 
	 * } catch (Exception bug) { bug.printStackTrace(); }
	 * 
	 * super.onSuccess(statusCode, content);
	 * pdHandler.removeCallbacks(pdRunnable);
	 * 
	 * } }); }
	 */

	private void GetDriverFeed(String startPoint, String endPoint,
			String engID, String driverId, String engTime)
	{

		feedDialog = new Dialog(LandingPage.this);
		feedDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		Window window = feedDialog.getWindow();
		window.setBackgroundDrawableResource(android.R.color.transparent);
		LinearLayout.LayoutParams dialogParams = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View dislogView = inflater.inflate(
				R.layout.activity_passenger_feedback_page, new LinearLayout(
						LandingPage.this));
		feedDialog.setContentView(dislogView, dialogParams);

		long timestamp = Long.parseLong(engTime) / 1000;
		String engTime_ = Epoch2DateString(timestamp, "dd-MM-yyyy HH:mm:ss aa");

		mgr.SaveValueToSharedPrefs("EngID", engID);
		mgr.SaveValueToSharedPrefs("DriverID", driverId);
		RatingBar jugunooRating = (RatingBar) feedDialog
				.findViewById(R.id.jugunooRating);
		final EditText rateText = (EditText) feedDialog
				.findViewById(R.id.feedbackText);
		TextView driverNameF = (TextView) feedDialog
				.findViewById(R.id.fareText);
		TextView driverNamelbl = (TextView) feedDialog
				.findViewById(R.id.driverLabel);
		TextView startLoc = (TextView) feedDialog
				.findViewById(R.id.startPonitf);
		TextView startLoclbl = (TextView) feedDialog
				.findViewById(R.id.startPonitfLabel);
		TextView endLoc = (TextView) feedDialog.findViewById(R.id.endPonitf);
		TextView endLoclbl = (TextView) feedDialog
				.findViewById(R.id.endPonitfLabel);
		TextView title = (TextView) feedDialog.findViewById(R.id.titleF);
		TextView ratetitle = (TextView) feedDialog.findViewById(R.id.rateLable);
		Button submit = (Button) feedDialog.findViewById(R.id.feedbackBtn);
		jugunooRating.setRating(3);
		rate = "3";
		rateText.setTypeface(light);
		driverNamelbl.setTypeface(light);
		startLoclbl.setTypeface(light);
		endLoclbl.setTypeface(light);
		ratetitle.setTypeface(semibold);
		submit.setTypeface(bold);
		title.setTypeface(bold);
		title.setText(getResources().getString(R.string.feedbackPopupTitle));
		driverNameF.setTypeface(semibold);
		startLoc.setTypeface(semibold);
		endLoc.setTypeface(semibold);
		driverNameF.setText("" + engTime_);
		startLoc.setText(startPoint);
		endLoc.setText(endPoint);

		jugunooRating
				.setOnRatingBarChangeListener(new OnRatingBarChangeListener()
				{

					@Override
					public void onRatingChanged(RatingBar ratingBar,
							float rating, boolean fromUser)
					{
						rate = String.valueOf(rating);
					}
				});

		rateText.setOnEditorActionListener(new EditText.OnEditorActionListener()
		{

			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event)
			{
				if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER))
						|| (actionId == EditorInfo.IME_ACTION_DONE))
				{
					String info = rateText.getText().toString();
					validation(info);
				}
				return false;
			}
		});

		submit.setOnClickListener(new View.OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				String info = rateText.getText().toString();
				validation(info);
				feedDialog.show();
			}
		});
		feedDialog.show();
	}

	private void validation(String info)
	{
		if (TextUtils.isEmpty(rate))
		{
			showDialog("Rate the trip!");
		}
		else if (info.equalsIgnoreCase(""))
		{
			showDialog(ConstantMessages.MSG62);
		}
		else
		{
			String userId = mgr.GetValueFromSharedPrefs("UserID");
			String engageID = mgr.GetValueFromSharedPrefs("EngID");
			String driverID = mgr.GetValueFromSharedPrefs("DriverID");
			if (!info.equalsIgnoreCase("") && !userId.equalsIgnoreCase("")
					&& !engageID.equalsIgnoreCase(""))
			{
				RequestParams params = new RequestParams();
				params.put("EngId", engageID);
				params.put("UserId", userId);
				params.put("FeedBack", info);
				params.put("Rating", rate);
				params.put("DriverId", driverID);
				String feedbackUrl = Global.JUGUNOO_WS + "Passenger/Feedback";

				SendFeed(feedbackUrl, params);

			}
			// else {
			// showDialog("UserId or tripId is null!");
			// }

		}
	}

	private void SendFeed(String url, RequestParams params)
	{

		AsyncHttpClient client = new AsyncHttpClient();
		client.setTimeout(30000);
		client.post(url, params, new AsyncHttpResponseHandler()
		{

			@Override
			@Deprecated
			public void onFailure(int statusCode, Throwable error,
					String content)
			{
				if (statusCode == 0)
				{
					showDialog("Connection timed out");
				}
				else
				{
					showDialog("Unable to submit");
				}
				super.onFailure(statusCode, error, content);
				pdHandler.removeCallbacks(pdRunnable);

			}

			@Override
			@Deprecated
			public void onSuccess(int statusCode, String content)
			{
				try
				{
					if (statusCode == 200)
					{

						JSONObject obj = new JSONObject(content);
						String result = obj.getString("Result");
						if (result.equalsIgnoreCase("Pass"))
						{
							// showDialog("Feedback submitted successfully");
							feedDialog.dismiss();
							mgr.SaveValueToSharedPrefs("TripStatus", "");
							mgr.SaveValueToSharedPrefs("EngID", "");
							mgr.SaveValueToSharedPrefs("DriverID", "");

						}
						else
						{
							showDialog("Unable to submit");
						}
					}
					else
					{
						showDialog("Unable to submit");
					}
				}
				catch (Exception bug)
				{
					bug.printStackTrace();
				}
				super.onSuccess(statusCode, content);
				pdHandler.removeCallbacks(pdRunnable);

			}

		});
	}

	public static String Epoch2DateString(long epochSeconds, String formatString)
	{
		Date updatedate = new Date(epochSeconds * 1000);
		SimpleDateFormat format = new SimpleDateFormat(formatString,
				Locale.getDefault());
		return format.format(updatedate);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (requestCode == 200)
		{

			Log.i(TAG, "on act result");

			if (resultCode == RESULT_OK)
			{

				googleMap.setOnCameraChangeListener(null);

				// searchStatus = true;
				pickPoint.setText(data.getStringExtra("address"));

				String pickupLatLong = data.getStringExtra("latLong");
				final String latAndLong[] = pickupLatLong.split(",");
				mgr.SaveValueToSharedPrefs("PickPoint", latAndLong[0] + ","
						+ latAndLong[1]);

				G_PickUpLat = latAndLong[0];
				G_PickUpLong = latAndLong[1];

				mgr.SaveValueToSharedPrefs("Lati", G_PickUpLat);
				mgr.SaveValueToSharedPrefs("Longi", G_PickUpLong);

				// isPickUpStatus = true;

				runOnUiThread(new Runnable()
				{

					@Override
					public void run()
					{
						SetPickpoint(latAndLong[0], latAndLong[1]);
					}
				});
				// SetPickpoint(latAndLong[0], latAndLong[1]);
			}
		}
		else if (requestCode == 100)
		{
			Log.e(TAG, "on gps result");
			gpsHandler.post(gpsRunnable);

		}
	}

	Handler handler = new Handler()
	{

		@Override
		public void handleMessage(Message msg)
		{
			Log.i(TAG, "Handler()=" + msg.arg1);
			switch (msg.arg1)
			{
				case Constant.MessageState.CAB_SUCCESS:

					JSONArray jsonArr = (JSONArray) msg.obj;

					cabsJsonParser(jsonArr);
					// Log.e(TAG, jsonArr.toString());

					break;

				case Constant.MessageState.CAB_FAIL:

					JSONArray jsonEmpArr = new JSONArray();

					cabsJsonParser(jsonEmpArr);

					break;

				case Constant.MessageState.LOGOUT_SUCCESS:

					cancelLoadingDialog();

					// showDialog(getString(R.string.logout_success_msg));
					mgr.SaveValueToSharedPrefs("UserID", "");
					mgr.SaveValueToSharedPrefs("UserRole", "");
					mgr.SaveValueToSharedPrefs("FleetUserType", "");
					mgr.savePreferenceIndex(Constant.PREFERENCE_INDEX, 0);
					ProfileFragment.gName = "";
					ProfileFragment.gMobile = "";
					ProfileFragment.gEmail = "";

					Intent logoutIntent = new Intent(LandingPage.this,
							SplashScreen.class);
					logoutIntent.addFlags(Intent.FLAG_ACTIVITY_TASK_ON_HOME);
					startActivity(logoutIntent);
					LandingPage.this.finish();

					break;

				case Constant.MessageState.LOGOUT_FAIL:

					cancelLoadingDialog();

					VolleyErrorHelper.getMessage(msg.obj, LandingPage.this,
							false);
					break;

				case Constant.MessageState.FAIL:

					Log.e(TAG, "volley http err failed");

					break;

				case CAMERA_CAHNGE_CALLBACK:

					CameraPosition pos = (CameraPosition) msg.obj;
					centerPosVal = pos;
					setOnCamChanged(pos);

					break;

				case Constant.MessageState.DEVICE_REGISTRATION_SUCCESS:
					cancelLoadingDialog();
					parseDeviceRegResp((JSONObject) msg.obj);
					break;

				case Constant.MessageState.PENDINGREQUESTCOUNT_STATUS_SUCCESS:
					cancelLoadingDialog();
					parsePendingRequestCount((JSONObject) msg.obj);
					break;

				default:
					break;
			}
		}
	};

	private void cabsJsonParser(JSONArray mapArray)
	{
		try
		{
			cabNoList.clear();

			if (mapArray != null)
			{
				int mapArrayLen = mapArray.length();
				if (mapArrayLen != 0)
				{
					autuDur.clear();
					miniDur.clear();
					sedanDur.clear();
					preDur.clear();
					luxDur.clear();
					mapList.clear();
					googleMap.clear();
					for (int map = 0; map < mapArrayLen; map++)
					{
						JSONObject mapObjectDistance = mapArray
								.getJSONObject(map);
						String distance = mapObjectDistance.getString("dis");
						JSONObject obj = mapObjectDistance.getJSONObject("obj");
						String id = obj.getString("_id");
						JSONObject mapObject = obj.getJSONObject("loc");
						String location = mapObject.getString("coordinates");
						String type = mapObject.getString("type");
						String locationTime = obj.getString("LocationTime");
						String userid = obj.getString("UserId");
						JSONObject carObj = mapObjectDistance
								.getJSONObject("CabDetails");
						String carType = carObj.getString("CabType");
						String cabNo = carObj.getString("CabNo");
						String driverID = carObj.getString("UserId");

						if (carType.equalsIgnoreCase("auto"))
						{
							autuDur.add(new double[]
							{ getDuration(distance) });
						}
						if (carType.equalsIgnoreCase("mini"))
						{
							miniDur.add(new double[]
							{ getDuration(distance) });
						}
						if (carType.equalsIgnoreCase("sedan"))
						{
							sedanDur.add(new double[]
							{ getDuration(distance) });
						}
						if (carType.equalsIgnoreCase("premium"))
						{
							preDur.add(new double[]
							{ getDuration(distance) });
						}

						if (carType.equalsIgnoreCase("luxy"))
						{
							luxDur.add(new double[]
							{ getDuration(distance) });
						}

						HashMap<String, String> mapData = new HashMap<String, String>();
						mapData.put("_id", id);
						mapData.put("dis", distance);
						mapData.put("coordinates", location);
						mapData.put("type", type);
						mapData.put("LocationTime", locationTime);
						mapData.put("AssetId", userid);
						mapData.put("CabType", carType);
						mapData.put("CabNo", cabNo);
						mapData.put("UserId", driverID);
						mapList.add(mapData);
						ApplyMins(mapList);
						GetAllCabs(mapList);
					}
				}
				else
				{
					Log.i(Global.APPTAG, "Josn Data is null");

					preMin.setText("-NA-");
					miniMin.setText("-NA-");
					sedanMin.setText("-NA-");
					preMin.setText("-NA-");
					luxMin.setText("-NA-");
					autuDur.clear();
					miniDur.clear();
					sedanDur.clear();
					preDur.clear();
					luxDur.clear();
					mapList.clear();
					ApplyMins(mapList);
					GetAllCabs(mapList);
					googleMap.clear();

				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	BroadcastReceiver networkStateReceiver = new BroadcastReceiver()
	{

		@Override
		public void onReceive(Context context, Intent intent)
		{

			if (JugunooUtil.isConnectedToInternet(getApplicationContext()))
			{

				// Toast.makeText(LandingPage.this, "online",
				// Toast.LENGTH_SHORT)
				// .show();
			}
			else
			{
				// Toast.makeText(LandingPage.this, "offline",
				// Toast.LENGTH_SHORT)
				// .show();
			}

		}
	};

	// gps handler
	Handler gpsHandler = new Handler();

	Runnable gpsRunnable = new Runnable()
	{

		@Override
		public void run()
		{
			Log.e(TAG, "gps run");
			getMyLocation();
		}
	};

	@Override
	public boolean onTouch(View v, MotionEvent event)
	{
		llCenter.setVisibility(View.GONE);
		// llMain.setBackgroundColor(Color.parseColor("#E6000000"));
		Log.i(TAG, "on touch");
		return false;
	}

	private void showTariffView(String type)
	{

		if (type.equalsIgnoreCase("mini"))
		{

			sedanImg.setSelected(false);
			preImg.setSelected(false);
			luxImg.setSelected(false);

		}
		else if (type.equalsIgnoreCase("sedan"))
		{

			miniImg.setSelected(false);
			preImg.setSelected(false);
			luxImg.setSelected(false);

		}
		else if (type.equalsIgnoreCase("premium"))
		{

			miniImg.setSelected(false);
			sedanImg.setSelected(false);
			luxImg.setSelected(false);

		}
		else if (type.equalsIgnoreCase("luxy"))
		{

			miniImg.setSelected(false);
			sedanImg.setSelected(false);
			preImg.setSelected(false);

		}

		if (tvNow.getVisibility() == View.GONE)
		{

			tvNow.setVisibility(View.VISIBLE);
			tvLater.setVisibility(View.VISIBLE);

			Animation now_Anim = AnimationUtils.loadAnimation(this,
					R.anim.slide_left_in);

			tvNow.startAnimation(now_Anim);

			Animation later_Anim = AnimationUtils.loadAnimation(this,
					R.anim.slide_right_in);

			tvLater.startAnimation(later_Anim);
		}
	}

	private void resetView()
	{

		miniImg.setSelected(false);
		sedanImg.setSelected(false);
		preImg.setSelected(false);
		luxImg.setSelected(false);

		if (tvNow.getVisibility() == View.VISIBLE)
		{
			llCenter.setVisibility(View.GONE);
			cabType = "all";
			llCenter.setBackgroundColor(Color.parseColor("#E6000000"));

			Animation now_Anim = AnimationUtils.loadAnimation(this,
					R.anim.slide_left_out);
			now_Anim.setAnimationListener(new AnimationListener()
			{

				@Override
				public void onAnimationStart(Animation animation)
				{

				}

				@Override
				public void onAnimationRepeat(Animation animation)
				{

				}

				@Override
				public void onAnimationEnd(Animation animation)
				{
					tvNow.setVisibility(View.GONE);
				}
			});
			tvNow.startAnimation(now_Anim);

			Animation later_Anim = AnimationUtils.loadAnimation(this,
					R.anim.slide_right_out);
			later_Anim.setAnimationListener(new AnimationListener()
			{

				@Override
				public void onAnimationStart(Animation animation)
				{

				}

				@Override
				public void onAnimationRepeat(Animation animation)
				{

				}

				@Override
				public void onAnimationEnd(Animation animation)
				{
					tvLater.setVisibility(View.GONE);
				}
			});
			tvLater.startAnimation(later_Anim);

			// tvNow.setVisibility(View.GONE);
			// tvLater.setVisibility(View.GONE);
			clickCounter = 0;
		}

	}

	private void onCabSelect(String type, View button)
	{

		// tvTariff.setText("CAB 1");
		cabType = type;

		if (button.isSelected())
		{
			// Handle selected state change
			Log.i("dp", "selected");
			if (clickCounter == 1)
			{

				++clickCounter;
				// AlertForEnableGPS();

				if (llCenter.isShown())
				{

					llCenter.setVisibility(View.GONE);
					llCenter.setBackgroundColor(Color.parseColor("#E6000000"));
				}
				else
				{
					llCenter.setVisibility(View.VISIBLE);
					llCenter.setBackgroundColor(Color.parseColor("#99000000"));
				}

			}
			else
			{
				llCenter.setVisibility(View.GONE);
				cabType = "all";
				llCenter.setBackgroundColor(Color.parseColor("#E6000000"));
				button.setSelected(false);

				Animation now_Anim = AnimationUtils.loadAnimation(this,
						R.anim.slide_left_out);
				now_Anim.setAnimationListener(new AnimationListener()
				{

					@Override
					public void onAnimationStart(Animation animation)
					{

					}

					@Override
					public void onAnimationRepeat(Animation animation)
					{

					}

					@Override
					public void onAnimationEnd(Animation animation)
					{
						tvNow.setVisibility(View.GONE);
					}
				});
				tvNow.startAnimation(now_Anim);

				Animation later_Anim = AnimationUtils.loadAnimation(this,
						R.anim.slide_right_out);
				later_Anim.setAnimationListener(new AnimationListener()
				{

					@Override
					public void onAnimationStart(Animation animation)
					{

					}

					@Override
					public void onAnimationRepeat(Animation animation)
					{

					}

					@Override
					public void onAnimationEnd(Animation animation)
					{

						tvLater.setVisibility(View.GONE);
					}
				});
				tvLater.startAnimation(later_Anim);

				// tvNow.setVisibility(View.GONE);
				// tvLater.setVisibility(View.GONE);
				clickCounter = 0;
			}

		}
		else
		{
			// Handle de-select state change
			Log.i("dp", "not selected");
			button.setSelected(true);
			clickCounter = 0;
			++clickCounter;

			showTariffView(type);

		}
	}

	private BroadcastReceiver mConnReceiver = new BroadcastReceiver()
	{
		// Method to check Device connect to internet or not
		public void onReceive(Context context, Intent intent)
		{
			checkDataEnable();
		}
	};

	private void checkDataEnable()
	{
		// Method to check Device connect to internet or not

		try
		{
			ConnectivityManager conMngr = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
			android.net.NetworkInfo wifi = conMngr
					.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			android.net.NetworkInfo mobile = conMngr
					.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

			if (!wifi.isConnected() && !mobile.isConnected())
			{
				if (dialogCheckData == null)
				{
					dialogCheckData = new Dialog(LandingPage.this);
					dialogCheckData
							.requestWindowFeature(Window.FEATURE_NO_TITLE);
					dialogCheckData
							.setContentView(R.layout.dialog_common_alert);
					dialogCheckData.setCancelable(false);

					TextView header = (TextView) dialogCheckData
							.findViewById(R.id.tvAlertHeader);
					TextView msg = (TextView) dialogCheckData
							.findViewById(R.id.tvAlertMsg);
					Button button = (Button) dialogCheckData
							.findViewById(R.id.btAlertOk);

					header.setText(ConstantMessages.MSG91);
					msg.setText(ConstantMessages.MSG92);
					button.setText(ConstantMessages.MSG93);

					button.setOnClickListener(new OnClickListener()
					{
						@Override
						public void onClick(View v)
						{
							dialogCheckData.dismiss();
						}
					});
				}
				dialogCheckData.show();
			}
			else
			{
				if (dialogCheckData != null)
				{
					dialogCheckData.dismiss();
					dialogCheckData = null;
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void AlertForEnableGPS()
	{
		/* Show alert if gps is disabled */

		if (gpsDialog == null)
		{
			gpsDialog = new Dialog(LandingPage.this);
			gpsDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			gpsDialog.setContentView(R.layout.dialog_common_alert);
			gpsDialog.setCancelable(false);

			TextView header = (TextView) gpsDialog
					.findViewById(R.id.tvAlertHeader);
			TextView msg = (TextView) gpsDialog.findViewById(R.id.tvAlertMsg);
			Button btOk = (Button) gpsDialog.findViewById(R.id.btAlertOk);
			Button btCancel = (Button) gpsDialog
					.findViewById(R.id.btAlertCancel);
			btCancel.setVisibility(View.VISIBLE);

			header.setText(ConstantMessages.MSG91);
			msg.setText(ConstantMessages.MSG94);
			btOk.setText(ConstantMessages.MSG95);
			btCancel.setText(ConstantMessages.MSG96);

			btOk.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					Global.LOCATIONSERVICE_CALLBACK = "1";

					Intent settingIntent = new Intent(
							Settings.ACTION_LOCATION_SOURCE_SETTINGS);
					LandingPage.this.startActivityForResult(settingIntent, 100);
				}
			});

			btCancel.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					try
					{
						if (gpsDialog != null)
						{
							gpsDialog.dismiss();
							gpsDialog = null;
						}
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			});
		}

		gpsDialog.show();
	}

	@Override
	public void onLocationChanged(Location location)
	{

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras)
	{

	}

	@Override
	public void onProviderEnabled(String provider)
	{
		// called when the GPS provider is turned off (user turning off the GPS
		// on the phone)

		try
		{
			if (gpsDialog != null)
			{
				gpsDialog.dismiss();
				gpsDialog = null;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void onProviderDisabled(String provider)
	{
		// called when the GPS provider is turned on (user turning on the GPS on
		// the phone)

		try
		{
			if (gpsDialog == null)
			{
				AlertForEnableGPS();
			}
			else
			{
				gpsDialog.show();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void gcmIdListener(String userId, String deviceId, String gcmId)
	{
		Log.i(Global.APPTAG, "GCM: id --> onListener -- " + gcmId);
		GcmId = gcmId;

		HashMap<String, String> params = new HashMap<String, String>();
		params.put("UserId", userId);
		params.put("GcmId", gcmId);
		params.put("DeviceId", deviceId);

		Log.i(TAG, "test params --> " + params);

		makeRegistrationReq(params);
	}

	private void makeRegistrationReq(HashMap<String, String> params)
	{
		showLoadingDilog();
		NetworkHandler.deviceRegistration(TAG, handler, params);
	}

	private void parseDeviceRegResp(JSONObject obj)
	{
		try
		{
			Log.i(TAG, "test params --> " + obj);

			if (obj.getString("Result").equalsIgnoreCase("Pass"))
			{
				mgr.SaveValueToSharedPrefs("GCM_ID", GcmId);
				mgr.SaveValueToSharedPrefs("isGcmSent", true);
			}
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
	}

	private void showLoadingDilog()
	{
		// Showing loading dialog

		try
		{
			pdHandler = new Handler();
			pd = new TransparentProgressDialog(LandingPage.this,
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
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void cancelLoadingDialog()
	{
		// Cancel loading dialog
		try
		{
			pdHandler.removeCallbacks(pdRunnable);

			if (pd.isShowing())
			{
				pd.dismiss();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

}