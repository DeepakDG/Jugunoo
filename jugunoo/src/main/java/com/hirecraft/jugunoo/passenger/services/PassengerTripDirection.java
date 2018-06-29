package com.hirecraft.jugunoo.passenger.services;

import java.util.ArrayList;
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
import org.w3c.dom.Document;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CameraPosition.Builder;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.api.client.util.Key;
import com.google.maps.android.SphericalUtil;
import com.hirecraft.jugunoo.passenger.GenerateGcmId;
import com.hirecraft.jugunoo.passenger.LandingPage;
import com.hirecraft.jugunoo.passenger.R;
import com.hirecraft.jugunoo.passenger.SplashScreen;
import com.hirecraft.jugunoo.passenger.common.Constant;
import com.hirecraft.jugunoo.passenger.common.ConstantMessages;
import com.hirecraft.jugunoo.passenger.common.Function;
import com.hirecraft.jugunoo.passenger.common.Global;
import com.hirecraft.jugunoo.passenger.common.TransparentProgressDialog;
import com.hirecraft.jugunoo.passenger.gcm.GCMNotificationHandler;
import com.hirecraft.jugunoo.passenger.googlemap.util.GoogleMapUtis;
import com.hirecraft.jugunoo.passenger.listeners.GcmIdListener;
import com.hirecraft.jugunoo.passenger.listeners.LocationResultListener;
import com.hirecraft.jugunoo.passenger.locationmanager.LocationService;
import com.hirecraft.jugunoo.passenger.networkhandler.NetworkHandler;
import com.hirecraft.jugunoo.passenger.utility.JugunooUtil;
import com.hirecraft.jugunoo.passenger.utility.PathJSONParser;
import com.hirecraft.jugunoo.passenger.utility.SharedPreferencesManager;
import com.hirecraft.jugunoo.passenger.utility.WakeLocker;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;

public class PassengerTripDirection extends Activity implements
		LocationResultListener, OnCameraChangeListener, OnClickListener,
		GcmIdListener
{
	private static final String TAG = PassengerTripDirection.class
			.getSimpleName();

	private LocationService mLocationService;
	private GoogleMap googleMap;

	private TransparentProgressDialog pd;
	private Handler pdHandler;
	private Runnable pdRunnable;

	private boolean isDriverLoc = false;
	ImageView locationImg, callToDriver;
	Location location;
	SharedPreferencesManager mgr;
	TextView driverNameText, driverRateText, driverMobileText;
	private GCMResponseReceiver receiver;
	JugunooUtil util;
	LinearLayout driverUpdateLayout;
	Geocoder geocoder;
	String addressStr = "", distanceStr, durationStr = "", pickupLati = "",
			pickupLongi = "", driverID = "";
	String driverMobileNo, trackerMasterUrl = "";
	LatLng pick;
	String dropAddress = "", dropLati = "", dropLongi = "";
	String pickStr = "";
	LinearLayout driverDisInfoLayout;
	Boolean isReceiverRegistered = false;
	ListView listViewPlaces;
	boolean isDrop = false;
	ArrayList<HashMap<String, String>> addresses;
	// Typeface light, bold, semibold;

	Document mDoc;
	Button cancelTrip;
	ArrayList<LatLng> points;
	String type = "All";
	TextView acText;
	Timer timer;

	Dialog dialog;
	TextView readingAzimuth, readingPitch, readingRoll, tripMessages;
	
	// private Polyline polyLine;
	// private PolylineOptions rectOptions = new PolylineOptions();
	// private static final HttpTransport HTTP_TRANSPORT = AndroidHttp
	// .newCompatibleTransport();
	// private static final JsonFactory JSON_FACTORY = new JacksonFactory();
	private List<Marker> markers = new ArrayList<Marker>();
	boolean flag, firstTime = true, directionsFetched = false;
	private String driverpoint = "", cabType = "";

	String gMessage, gDriverName, gDriverMobile, gTripId, gTripStatus, gCabNo,
			gDriverImage, gCabId, gPhoto, gRate;

	private Boolean isNetworkRegistered;

	// notification
	private NotificationCompat.Builder builder;
	private NotificationManager mNotificationManager;

	private DisplayImageOptions options;

	private Dialog dialogCheckData = null;

	private String GcmId = "";
	private String userId = "";
	private String deviceId = "";

	private ProgressBar pbLoader;

	@SuppressLint(
	{ "InflateParams", "Recycle" })
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_passenger_trip_direction);

		mgr = new SharedPreferencesManager(PassengerTripDirection.this);

		mLocationService = new LocationService();
		mLocationService.getLocation(getApplicationContext(), this);

		pdHandler = new Handler();
		pd = new TransparentProgressDialog(PassengerTripDirection.this,
				R.drawable.loading_image);

		geocoder = new Geocoder(getBaseContext(), Locale.getDefault());

		googleMap = ((MapFragment) getFragmentManager().findFragmentById(
				R.id.landingMap)).getMap();
		googleMap.getUiSettings().setZoomControlsEnabled(false);
		googleMap.getUiSettings().setCompassEnabled(false);
		googleMap.setBuildingsEnabled(true);
		googleMap.setTrafficEnabled(false);
		googleMap.getUiSettings().setMyLocationButtonEnabled(false);
		googleMap.setMyLocationEnabled(true);
		googleMap.setOnCameraChangeListener(this);
		googleMap.getUiSettings().setRotateGesturesEnabled(false);

		DrawTripRoute();
		InitScreen();

		if (savedInstanceState != null)
		{
			double lati_ = savedInstanceState.getDouble("Latitude");
			double longi_ = savedInstanceState.getDouble("Longitude");

			PassengerTripDirection.this.UpdateMyLocation(lati_, longi_);
		}
		util = new JugunooUtil(getApplicationContext());

		String lati = mgr.GetValueFromSharedPrefs("Lati");
		String longi = mgr.GetValueFromSharedPrefs("Longi");
		if (!TextUtils.isEmpty(lati) && !TextUtils.isEmpty(longi))
		{
			PassengerTripDirection.this.UpdateMyLocation(
					Double.parseDouble(lati), Double.parseDouble(longi));
		}

		String tripStat = mgr.GetValueFromSharedPrefs("TripStatus");

		Log.e(TAG, "trip stat=" + tripStat);

		if (!TextUtils.isEmpty(tripStat)
				&& (tripStat.equalsIgnoreCase("ENGAGED") || tripStat
						.equalsIgnoreCase("START")))
		{

			Log.e(TAG, "inside..............@@@@@@@@");

			String cabId = mgr.GetValueFromSharedPrefs("CabId");
			String cabNo = mgr.GetValueFromSharedPrefs("CabNo");
			String driverName = mgr.GetValueFromSharedPrefs("DriverName");
			String mobile = mgr.GetValueFromSharedPrefs("Mobile");
			driverMobileNo = mobile;
			String tripId = mgr.GetValueFromSharedPrefs("EngID");
			String tripStatus = mgr.GetValueFromSharedPrefs("TripStatus");
			String message = mgr.GetValueFromSharedPrefs("message");
			String driverImage = mgr.GetValueFromSharedPrefs("DriverImage");

			if (tripStat.equalsIgnoreCase("START"))
			{
				tripMessages.setText("Journey in progress");
				callToDriver.setVisibility(View.INVISIBLE);
			}
			else if (tripStat.equalsIgnoreCase("ENGAGED"))
			{
				tripMessages.setText("Arriving...");
			}

			driverUpdateLayout.setVisibility(View.VISIBLE);

			driverNameText.setText(mgr.GetValueFromSharedPrefs("DriverName"));

			driverMobileText.setText(mgr.GetValueFromSharedPrefs("CabNo"));
			driverDisInfoLayout.setVisibility(View.VISIBLE);

			// if (!cabNo.equalsIgnoreCase("")) {

			// String DriverId = mgr.GetValueFromSharedPrefs("DriverID");

			// if (DriverId != null) {

			DriverDetailPopup(message, driverName, mobile, tripId, tripStatus,
					cabNo, driverImage, cabId);

			// }

			// }

		}

		// else if (!TextUtils.isEmpty(tripStat)
		// && tripStat.equalsIgnoreCase("FINISH")) {
		//
		// String tripId = mgr.GetValueFromSharedPrefs("EngID");
		// String driverID = mgr.GetValueFromSharedPrefs("DriverID");
		// String time = mgr.GetValueFromSharedPrefs("EngageTime");
		// String pick = mgr.GetValueFromSharedPrefs("fPick");
		// String drop = mgr.GetValueFromSharedPrefs("fDrop");
		//
		// tripMessages.setText("Arrived at destination");
		//
		// TripFeedback(tripId, "", driverID, pick, drop, time);
		//
		// }

		// network change
		isNetworkRegistered = true;
		registerReceiver(networkStateReceiver, new IntentFilter(
				ConnectivityManager.CONNECTIVITY_ACTION));
		openAnimation();
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

	public void addPolylineToMap(List<LatLng> latLngs)
	{
		PolylineOptions options = new PolylineOptions();
		for (LatLng latLng : latLngs)
		{
			options.add(latLng);
		}
		googleMap.addPolyline(options);
	}

	public void clearMarkers()
	{
		googleMap.clear();
		markers.clear();
	}

	public void onCallClicked()
	{

		Log.e(TAG, "driver mob call click=" + driverMobileNo);

		try
		{
			if (!TextUtils.isEmpty(driverMobileNo))
			{
				String number = "tel:" + driverMobileNo;
				Intent callIntent = new Intent(Intent.ACTION_CALL,
						Uri.parse(number));
				startActivity(callIntent);
			}
		}
		catch (android.content.ActivityNotFoundException ex)
		{
			Toast.makeText(PassengerTripDirection.this,
					"Call failed, please try again later.", Toast.LENGTH_SHORT)
					.show();
		}

	}

	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{

			case R.id.callToDriver:
				onCallClicked();
				break;

			case R.id.location:
				getMyLocation();
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
			case R.id.cancelTripBtn:
				JugunooInteractiveDialog(ConstantMessages.MSG91,
						"Are you sure to cancel your trip?");
				break;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{

		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0)
		{
			PassengerTripDirection.this.finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onResume()
	{
		super.onResume();

		checkGcmID();

		// Setting screen visibility status
		Global.activityResumed();

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
			WakeLocker.acquire(getApplicationContext());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);

		if (location != null)
		{

			outState.putDouble("Latitude", location.getLatitude());
			outState.putDouble("Longitude", location.getLongitude());
		}
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState)
	{
		super.onRestoreInstanceState(savedInstanceState);

		double lati_ = savedInstanceState.getDouble("Latitude");
		double longi_ = savedInstanceState.getDouble("Longitude");

		PassengerTripDirection.this.UpdateMyLocation(lati_, longi_);
	}

	private void UpdateMyLocation(double lati, double longi)
	{

		try
		{
			LatLng myLatLng = new LatLng(lati, longi);

			googleMap.addMarker(new MarkerOptions().position(myLatLng).icon(
					BitmapDescriptorFactory
							.fromResource(R.drawable.transparent)));

			String lati_ = String.valueOf(lati);
			String longi_ = String.valueOf(longi);
			mgr.SaveValueToSharedPrefs("Lati", lati_);
			mgr.SaveValueToSharedPrefs("Longi", longi_);

		}
		catch (Exception bug)
		{
			bug.printStackTrace();
		}

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

	@Override
	protected void onPause()
	{
		super.onPause();

		// Setting screen visibility status
		Global.activityPaused();

		closeAnimation();

		try
		{
			WakeLocker.release();
		}
		catch (Exception e)
		{
			e.printStackTrace();
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
	}

	// private void restoreActionBar()
	// {
	// ActionBar actionBar = getActionBar();
	// actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
	// actionBar.setDisplayShowTitleEnabled(false);
	// }

	// private boolean ceckGpsStatus() {
	// boolean gpsStatus;
	// ContentResolver contentResolver = getBaseContext().getContentResolver();
	// gpsStatus = Settings.Secure.isLocationProviderEnabled(contentResolver,
	// LocationManager.GPS_PROVIDER);
	// if (gpsStatus == false) {
	// gpsStatus = Settings.Secure.isLocationProviderEnabled(
	// contentResolver, LocationManager.NETWORK_PROVIDER);
	// }
	// return gpsStatus;
	// }

	public void showDialog(String message)
	{

		Toast.makeText(PassengerTripDirection.this, message, Toast.LENGTH_LONG)
				.show();

	}

	private void DrawTripRoute()
	{
		try
		{
			String pLatiLongi = mgr.GetValueFromSharedPrefs("PickPoint");
			String dLatiLongi = mgr.GetValueFromSharedPrefs("DropPoint");

			Log.e("ptrip", "pick=" + pLatiLongi + " drp=" + dLatiLongi);

			if (!TextUtils.isEmpty(pLatiLongi)
					&& !TextUtils.isEmpty(dLatiLongi))
			{
				String pickLatLng[] = pLatiLongi.split(",");
				String pLati = pickLatLng[0];
				String pLongi = pickLatLng[1];

				String dropLatLng[] = dLatiLongi.split(",");
				String dLati = dropLatLng[0];
				String dLongi = dropLatLng[1];

				LatLng pickPoint = new LatLng(Double.parseDouble(pLati),
						Double.parseDouble(pLongi));

				LatLng dropPoint = new LatLng(Double.parseDouble(dLati),
						Double.parseDouble(dLongi));

				googleMap.addMarker(new MarkerOptions().position(pickPoint)
						.icon(BitmapDescriptorFactory
								.fromResource(R.drawable.pick_point_marker)));

				googleMap.addMarker(new MarkerOptions().position(dropPoint)
						.icon(BitmapDescriptorFactory
								.fromResource(R.drawable.drop_pont_marker)));

				String routeVal = mgr.GetValueFromSharedPrefs("routeVal");

				if (TextUtils.isEmpty(routeVal))
				{
					Log.i(TAG, "route emp");

					NetworkHandler.routeRequest(TAG, handler, pickPoint,
							dropPoint);
				}
				else
				{
					Log.i(TAG, "route not emp");
					Message msg = new Message();
					msg.arg1 = Constant.MessageState.MAP_PLOT_SUCCESS;
					msg.obj = routeVal;
					handler.sendMessage(msg);
				}
			}
		}
		catch (NumberFormatException e)
		{
			e.printStackTrace();
		}
	}

	private void showLoadingDilog()
	{
		// Showing loading dialog

		// pdHandler = new Handler();
		// pd = new TransparentProgressDialog(PassengerTripDirection.this,
		// R.drawable.loading_image);

		try
		{
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

	private class ParserTask extends
			AsyncTask<String, Integer, List<List<HashMap<String, String>>>>
	{

		@Override
		protected List<List<HashMap<String, String>>> doInBackground(
				String... jsonData)
		{

			JSONObject jObject;
			List<List<HashMap<String, String>>> routes = null;

			try
			{
				jObject = new JSONObject(jsonData[0]);
				PathJSONParser parser = new PathJSONParser();
				routes = parser.parse(jObject);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			return routes;
		}

		@Override
		protected void onPostExecute(List<List<HashMap<String, String>>> routes)
		{

			ArrayList<LatLng> points = null;
			PolylineOptions polyLineOptions = null;

			for (int i = 0; i < routes.size(); i++)
			{
				points = new ArrayList<LatLng>();
				polyLineOptions = new PolylineOptions();
				List<HashMap<String, String>> path = routes.get(i);

				for (int j = 0; j < path.size(); j++)
				{
					HashMap<String, String> point = path.get(j);

					double lat = Double.parseDouble(point.get("lat"));
					double lng = Double.parseDouble(point.get("lng"));
					LatLng position = new LatLng(lat, lng);
					points.add(position);
				}

				GoogleMapUtis.fixZoomForLatLngs(googleMap, points);
				polyLineOptions.addAll(points);
				polyLineOptions.width(13);
				polyLineOptions.color(Color.rgb(209, 21, 21));
			}

			if (polyLineOptions != null)
			{
				googleMap.addPolyline(polyLineOptions);
			}
		}
	}

	private void getMyLocation()
	{

		try
		{
			Location location = googleMap.getMyLocation();

			if (location != null)
			{

				gpsHandler.removeCallbacks(gpsRunnable);

				LatLng target = new LatLng(location.getLatitude(),
						location.getLongitude());

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
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	private void Logout(Map<String, String> params)
	{

		try
		{
			// pdRunnable = new Runnable()
			// {
			// @Override
			// public void run()
			// {
			// if (pd != null)
			// {
			// if (pd.isShowing())
			// {
			// pd.dismiss();
			// }
			// }
			// }
			// };
			//
			// pd.show();

			showLoadingDilog();
			NetworkHandler.logoutRequest(TAG, handler, params);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void onLocationResultAvailable(final Location location)
	{
		this.location = location;
		PassengerTripDirection.this.runOnUiThread(new Runnable()
		{

			@Override
			public void run()
			{
				if (location == null)
				{

					gpsHandler.post(gpsRunnable);

					// JugunooInteractiveDialog("LOCATION", "");
				}
				else
				{
					double lati = location.getLatitude();
					double longi = location.getLongitude();
					String lati_ = String.valueOf(lati);
					String longi_ = String.valueOf(longi);

					mgr.SaveValueToSharedPrefs("Lati", lati_);
					mgr.SaveValueToSharedPrefs("Longi", longi_);

					getMyLocation();

				}

			}
		});
	}

	@Override
	public void onStop()
	{
		super.onStop();
		if (mLocationService != null)
		{
			mLocationService.stop();
		}

		mHandler.removeCallbacks(animator);

		try
		{
			WakeLocker.release();
		}
		catch (Exception e)
		{
			e.printStackTrace();
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
	}

	@Override
	public void onCameraChange(CameraPosition position)
	{
		Location location = this.location;
		if (location != null)
		{
			googleMap.addMarker(new MarkerOptions()
					.position(
							new LatLng(location.getLatitude(), location
									.getLongitude())).icon(
							BitmapDescriptorFactory
									.fromResource(R.drawable.transparent)));
		}
	}

	@Override
	protected void onStart()
	{
		super.onStart();
		// if (!checkGpsStatus())
		// {
		// if (dialog == null)
		// {
		// JugunooInteractiveDialog("GPS",
		// "Location Service is Not Enabled! Enable Service in the Settings Menu?");
		// }
		// }
	}

	// private boolean checkGpsStatus()
	// {
	// /* Check GPS is On or off */
	// boolean gpsStatus;
	//
	// long MIN_TIME = 400;
	// float MIN_DISTANCE = 1000;
	//
	// locationManager = (LocationManager) this
	// .getSystemService(LOCATION_SERVICE);
	// locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
	// MIN_TIME, MIN_DISTANCE, this);
	// gpsStatus = locationManager
	// .isProviderEnabled(LocationManager.GPS_PROVIDER);
	// return gpsStatus;
	// }

	private void JugunooInteractiveDialog(String title, String message)
	{
		try
		{
			// dialog = new Dialog(PassengerTripDirection.this);
			// dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			// Window window = dialog.getWindow();
			// window.setBackgroundDrawableResource(android.R.color.transparent);
			// LinearLayout.LayoutParams dialogParams = new
			// LinearLayout.LayoutParams(
			// LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			//
			// LayoutInflater inflater = (LayoutInflater)
			// getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			// View dislogView = inflater.inflate(
			// R.layout.jugunoo_interactive_dialog, new LinearLayout(
			// PassengerTripDirection.this));
			// dialog.setContentView(dislogView, dialogParams);
			//
			// TextView textView = (TextView) dialog
			// .findViewById(R.id.messageText);
			// Button centerBtn = (Button) dialog.findViewById(R.id.centerBtn);
			// Button leftBtn = (Button) dialog.findViewById(R.id.leftBtn);
			// Button rightBtn = (Button) dialog.findViewById(R.id.rightBtn);
			//
			// textView.setTypeface(light);
			// centerBtn.setTypeface(bold);
			// leftBtn.setTypeface(bold);
			// rightBtn.setTypeface(bold);

			dialog = new Dialog(PassengerTripDirection.this);
			dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			dialog.setContentView(R.layout.dialog_common_alert);

			TextView tvTitle = (TextView) dialog
					.findViewById(R.id.tvAlertHeader);
			TextView tvMsg = (TextView) dialog.findViewById(R.id.tvAlertMsg);
			tvMsg.setTextColor(getResources().getColor(android.R.color.black));
			Button rightBtn = (Button) dialog.findViewById(R.id.btAlertOk);
			Button leftBtn = (Button) dialog.findViewById(R.id.btAlertCancel);
			leftBtn.setVisibility(View.VISIBLE);

			tvMsg.setText(message);
			tvTitle.setText(title);
			rightBtn.setText(ConstantMessages.MSG93);
			leftBtn.setText(ConstantMessages.MSG96);

			if (title.equalsIgnoreCase("LOGOUT"))
			{

				// centerBtn.setVisibility(View.GONE);
				// textView.setText(message);

				String signOut = getResources().getString(R.string.signout);
				String notnow = getResources().getString(R.string.notnow);

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
						Map<String, String> params = new HashMap<String, String>();
						params.put("PassengerId", userId);
						Logout(params);
						dialog.dismiss();
					}
				});
			}
			// else if (title.equalsIgnoreCase("SLIDE"))
			// {
			//
			// leftBtn.setVisibility(View.GONE);
			// rightBtn.setVisibility(View.GONE);
			// textView.setText(message);
			// centerBtn.setOnClickListener(new OnClickListener()
			// {
			//
			// @Override
			// public void onClick(View v)
			// {
			//
			// dialog.dismiss();
			// }
			// });
			//
			// }
			// else if (title.equalsIgnoreCase("RIDESHARE"))
			// {
			//
			// leftBtn.setVisibility(View.GONE);
			// rightBtn.setVisibility(View.GONE);
			// textView.setText(message);
			// centerBtn.setOnClickListener(new OnClickListener()
			// {
			//
			// @Override
			// public void onClick(View v)
			// {
			// dialog.dismiss();
			// }
			// });
			//
			// }
			// else if (title.equalsIgnoreCase("BOOK"))
			// {
			//
			// leftBtn.setVisibility(View.GONE);
			// rightBtn.setVisibility(View.GONE);
			// String ok = getResources().getString(R.string.ok);
			// textView.setText(message);
			// centerBtn.setText(ok);
			// centerBtn.setOnClickListener(new OnClickListener()
			// {
			//
			// @Override
			// public void onClick(View v)
			// {
			// dialog.dismiss();
			// }
			// });
			// }

			// else if (title.equalsIgnoreCase("CANCEL FAIL"))
			// {
			//
			// leftBtn.setVisibility(View.GONE);
			// rightBtn.setVisibility(View.GONE);
			// String ok = getResources().getString(R.string.ok);
			// textView.setText(message);
			// centerBtn.setText(ok);
			// centerBtn.setOnClickListener(new OnClickListener()
			// {
			//
			// @Override
			// public void onClick(View v)
			// {
			// dialog.dismiss();
			// }
			// });
			// }

			// else if (title.equalsIgnoreCase("CANCEL SUCCESS"))
			// {
			//
			// Log.i(TAG, "CANCEL CANCEL SUCCESS");
			//
			// leftBtn.setVisibility(View.GONE);
			// rightBtn.setVisibility(View.GONE);
			// String ok = getResources().getString(R.string.ok);
			// textView.setText(message);
			// centerBtn.setText(ok);
			// centerBtn.setOnClickListener(new OnClickListener()
			// {
			//
			// @Override
			// public void onClick(View v)
			// {
			// dialog.dismiss();
			// Intent landingPageIntent = new Intent(
			// PassengerTripDirection.this, LandingPage.class);
			// landingPageIntent.putExtra("from", "passengerTrip");
			// startActivity(landingPageIntent);
			// PassengerTripDirection.this.finish();
			// }
			// });
			// }

			// else if (title.equalsIgnoreCase("NETWORK"))
			// {
			//
			// leftBtn.setVisibility(View.GONE);
			// rightBtn.setVisibility(View.GONE);
			// String ok = getResources().getString(R.string.ok);
			// textView.setText(message);
			// centerBtn.setText(ok);
			// centerBtn.setOnClickListener(new OnClickListener()
			// {
			//
			// @Override
			// public void onClick(View v)
			// {
			// dialog.dismiss();
			// startActivity(new Intent(PassengerTripDirection.this,
			// LandingPage.class));
			// PassengerTripDirection.this.finish();
			// }
			// });
			// }

			// else if (title.equalsIgnoreCase("CANCEL"))
			// {
			//
			// leftBtn.setVisibility(View.GONE);
			// rightBtn.setVisibility(View.GONE);
			// String ok = getResources().getString(R.string.ok);
			// textView.setText(message);
			// centerBtn.setText(ok);
			// centerBtn.setOnClickListener(new OnClickListener()
			// {
			//
			// @Override
			// public void onClick(View v)
			// {
			// dialog.dismiss();
			// }
			// });
			// }
			// else if (title.equalsIgnoreCase("AUTO CANCEL"))
			// {
			//
			// leftBtn.setVisibility(View.GONE);
			// rightBtn.setVisibility(View.GONE);
			// String ok = getResources().getString(R.string.ok);
			// textView.setText(message);
			// centerBtn.setText(ok);
			// centerBtn.setOnClickListener(new OnClickListener()
			// {
			//
			// @Override
			// public void onClick(View v)
			// {
			// dialog.dismiss();
			// Intent landingPageIntent = new Intent(
			// PassengerTripDirection.this, LandingPage.class);
			// landingPageIntent.putExtra("from", "passengerTrip");
			// startActivity(landingPageIntent);
			// PassengerTripDirection.this.finish();
			// }
			// });
			// }

			// else if (title.equalsIgnoreCase("LOCATION"))
			// {
			//
			// leftBtn.setVisibility(View.GONE);
			// rightBtn.setVisibility(View.GONE);
			// String ok = getResources().getString(R.string.ok);
			// textView.setText("Unable to get your current location. Please make sure getting the Google location servics!");
			// centerBtn.setText(ok);
			// centerBtn.setOnClickListener(new OnClickListener()
			// {
			//
			// @Override
			// public void onClick(View v)
			// {
			// dialog.dismiss();
			// }
			// });
			// }

			else if (title.equalsIgnoreCase(ConstantMessages.MSG91))
			{

				Log.i(TAG, "CANCEL CONFIRM");

				// centerBtn.setVisibility(View.GONE);
				// textView.setText(message);
				leftBtn.setText(ConstantMessages.MSG96);
				rightBtn.setText(ConstantMessages.MSG93);

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
						dialog.dismiss();
						cancelMyTrip();
					}
				});
			}

			// else if (title.equalsIgnoreCase("GPS"))
			// {
			//
			// // centerBtn.setVisibility(View.GONE);
			// // textView.setText(message);
			//
			// String settings = getResources().getString(R.string.settings);
			// String cancel = getResources().getString(R.string.cancel);
			//
			// leftBtn.setText(cancel);
			// rightBtn.setText(settings);
			// leftBtn.setVisibility(View.GONE);
			//
			// leftBtn.setOnClickListener(new OnClickListener()
			// {
			//
			// @Override
			// public void onClick(View v)
			// {
			// dialog.dismiss();
			// }
			// });
			// rightBtn.setOnClickListener(new OnClickListener()
			// {
			//
			// @Override
			// public void onClick(View v)
			// {
			// Global.LOCATIONSERVICE_CALLBACK = "1";
			//
			// dialog.dismiss();
			// dialog = null;
			//
			// Intent settingIntent = new Intent(
			// Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			// PassengerTripDirection.this.startActivityForResult(
			// settingIntent, 100);
			//
			// }
			// });
			// }
			dialog.show();
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
		if (dialog != null)
		{
			dialog.dismiss();
		}
		this.unregisterReceiver(receiver);
		Log.i(Global.APPTAG, "destroy");

		try
		{
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

		if (isNetworkRegistered)
		{
			unregisterReceiver(networkStateReceiver);
		}

		try
		{
			WakeLocker.release();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void InitScreen()
	{
		options = new DisplayImageOptions.Builder()
				.showImageForEmptyUri(R.drawable.ic_launcher)
				.showImageOnFail(R.drawable.ic_launcher).cacheOnDisk(true)
				.cacheInMemory(true).showImageOnLoading(R.drawable.ic_launcher)
				.considerExifParams(true).cacheInMemory(true)
				.displayer(new SimpleBitmapDisplayer()).build();

		IntentFilter filter = new IntentFilter(
				GCMResponseReceiver.PROCESS_RESPONSE);
		filter.addCategory(Intent.CATEGORY_DEFAULT);
		receiver = new GCMResponseReceiver();
		registerReceiver(receiver, filter);

		pbLoader = (ProgressBar) findViewById(R.id.pbLoader);
		driverUpdateLayout = (LinearLayout) findViewById(R.id.driverInfoLayout);
		driverDisInfoLayout = (LinearLayout) findViewById(R.id.driverDisInfoLayout);
		driverNameText = (TextView) findViewById(R.id.driverNameText);
		driverMobileText = (TextView) findViewById(R.id.driverMobile);
		driverRateText = (TextView) findViewById(R.id.rateText);
		tripMessages = (TextView) findViewById(R.id.tripMessages);
		locationImg = (ImageView) findViewById(R.id.location);
		callToDriver = (ImageView) findViewById(R.id.callToDriver);
		cancelTrip = (Button) findViewById(R.id.cancelTripBtn);
		callToDriver.setOnClickListener(this);
		locationImg.setOnClickListener(this);
		cancelTrip.setOnClickListener(this);

		locationImg.setVisibility(View.VISIBLE);

		tripMessages.setText("Waiting for driver to accept...");

		timer = new Timer();

		TimerTask updateProfile = new CustomTimerTask(
				PassengerTripDirection.this);

		timer.scheduleAtFixedRate(updateProfile, 10, 5000);
	}

	public class GCMResponseReceiver extends BroadcastReceiver
	{

		public static final String PROCESS_RESPONSE = "com.hirecraft.intent.action.PROCESS_RESPONSE";

		@Override
		public void onReceive(Context context, Intent notificationIntent)
		{
			Log.i(TAG, "gcm on receive");

			pbLoader.setVisibility(View.GONE);

			// WakeLocker.acquire(getApplicationContext());

			String title = notificationIntent.getStringExtra("Title");
			String stat = notificationIntent.getStringExtra("TripStatus");
			if (title.equalsIgnoreCase("Jugunoo Booking"))
			{
				String tripId = notificationIntent.getStringExtra("TripID");
				mgr.SaveValueToSharedPrefs("EngID", tripId);
			}
			else if (title.equalsIgnoreCase("Trip Hired"))
			{
				mgr.SaveValueToSharedPrefs("TripStatus", "ENGAGED");
				gMessage = notificationIntent.getStringExtra("message");
				gDriverName = notificationIntent.getStringExtra("DriverName");
				gDriverMobile = notificationIntent
						.getStringExtra("DriverMobile");
				gTripId = notificationIntent.getStringExtra("TripId");
				gTripStatus = notificationIntent.getStringExtra("TripStatus");
				gCabNo = notificationIntent.getStringExtra("CabNo");
				gDriverImage = notificationIntent.getStringExtra("DriverPhoto");
				gCabId = notificationIntent.getStringExtra("CabId");
				driverID = notificationIntent.getStringExtra("DriverId");
				mgr.SaveValueToSharedPrefs("EngID", gTripId);

				mgr.SaveValueToSharedPrefs("DriverID", driverID);
				mgr.SaveValueToSharedPrefs("CabId", gCabId);
				mgr.SaveValueToSharedPrefs("CabNo", gCabNo);
				mgr.SaveValueToSharedPrefs("DriverMobile", gDriverMobile);
				mgr.SaveValueToSharedPrefs("message", gMessage);
				mgr.SaveValueToSharedPrefs("DriverImage", gDriverImage);

				Global.ISCAB_HIRED = true;

				if (driverID != null)
				{
					DriverDetailPopup(gMessage, gDriverName, gDriverMobile,
							gTripId, gTripStatus, gCabNo, gDriverImage, gCabId);

					UpdateDriverInfo();
				}

			}
			else if (stat.equalsIgnoreCase("F"))
			{
				Log.i(TAG, "gcm onFinish");

				mgr.SaveValueToSharedPrefs("TripStatus", "FINISH");
				final String tripId = notificationIntent
						.getStringExtra("TripId");
				// String driverName = notificationIntent
				// .getStringExtra("DriverName");
				final String driverID = notificationIntent
						.getStringExtra("DriverId");
				final String time = notificationIntent
						.getStringExtra("EngageTime");
				final String pick = notificationIntent
						.getStringExtra("PickPoint");
				final String drop = notificationIntent
						.getStringExtra("DropPoint");
				mgr.SaveValueToSharedPrefs("DriverID", driverID);

				tripMessages.setText("Arrived at destination");

				mgr.SaveValueToSharedPrefs("CabId", "");
				mgr.SaveValueToSharedPrefs("CabNo", "");
				mgr.SaveValueToSharedPrefs("DriverName", "");
				mgr.SaveValueToSharedPrefs("Mobile", "");
				mgr.SaveValueToSharedPrefs("TripStatus", "");
				mgr.SaveValueToSharedPrefs("message", "");
				mgr.SaveValueToSharedPrefs("DriverImage", "");

				new Handler().postDelayed(new Runnable()
				{
					@Override
					public void run()
					{
						TripFeedback(tripId, "", driverID, pick, drop, time);
					}
				}, 2000);

			}
			else if (title.equalsIgnoreCase("Trip started"))
			{

				driverID = notificationIntent.getStringExtra("DriverId");
				mgr.SaveValueToSharedPrefs("DriverID", driverID);
				String tripId = notificationIntent.getStringExtra("TripId");
				String result = notificationIntent.getStringExtra("Result");
				String status = notificationIntent.getStringExtra("TripStatus");
				tripMessages.setText("Journey Started");
				callToDriver.setVisibility(View.INVISIBLE);

				mgr.SaveValueToSharedPrefs("TripID", tripId);

				UpdateLayout(result, tripId, status, driverID);

			}
			else if (stat.equalsIgnoreCase("AC"))
			{
				mgr.SaveValueToSharedPrefs("TripStatus", "AC");

			}

			// WakeLocker.release();
		}
	}

	private void UpdateLayout(String result, String tripId, String status,
			String driverId)
	{
		mgr.SaveValueToSharedPrefs("TripStatus", "START");
	}

	private void DriverDetailPopup(String message, String driverName,
			String mobile, String tripId, String tripStatus, String cabNo,
			String driverImage, String cabId)
	{

		driverMobileNo = mobile;
		mgr.SaveValueToSharedPrefs("IsNotify", "true");
		mgr.SaveValueToSharedPrefs("EngID", tripId);
		mgr.SaveValueToSharedPrefs("DriverName", driverName);
		mgr.SaveValueToSharedPrefs("Mobile", mobile);

		NetworkHandler.driverPhotoRequest(TAG, handler, cabId);

	}

	private void driverPhotoParser(JSONObject obj)
	{

		try
		{

			String result = obj.getString("Result");

			if (!result.equalsIgnoreCase("Fail"))
			{
				JSONObject driverObj = obj.getJSONObject("DriverDetails");
				String photo = driverObj.getString("PhotoFileId");
				String rateObj = obj.getString("RateDetails");
				if (!TextUtils.isEmpty(rateObj))
				{
					driverUpdateLayout.setVisibility(View.VISIBLE);
					ImageView driverImage = (ImageView) findViewById(R.id.deriverPhotoP);
					// JugunooUtil util = new
					// JugunooUtil(getApplicationContext());

					// if (!TextUtils.isEmpty(photo)) {

					// photo = photo.substring(22, photo.length());
					gPhoto = photo;
					Log.i(TAG, "driver image url=" + photo);
					// Bitmap b = util.driverImage(photo);
					// image.setImageBitmap(b);
					ImageLoader.getInstance().displayImage(photo, driverImage,
							options);

					mgr.SaveValueToSharedPrefs("PhotoFileId", photo);

					// }
					gRate = rateObj;
					mgr.SaveValueToSharedPrefs("Rate", rateObj);

					driverNameText.setText(mgr
							.GetValueFromSharedPrefs("DriverName"));
					driverRateText.setText(rateObj);
					driverMobileText.setText(mgr
							.GetValueFromSharedPrefs("CabNo"));
					driverDisInfoLayout.setVisibility(View.VISIBLE);

					String tripStat = mgr.GetValueFromSharedPrefs("TripStatus");
					if (tripStat.equalsIgnoreCase("START"))
					{
						tripMessages.setText("Journey in progress");
						callToDriver.setVisibility(View.INVISIBLE);
					}
					else if (tripStat.equalsIgnoreCase("ENGAGED"))
					{
						tripMessages.setText("Arriving...");
					}

				}
			}

		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}

	}

	private void TripFeedback(String tripId, String driverName,
			String driverId, String pick, String drop, String time)
	{

		mgr.SaveValueToSharedPrefs("EngID", tripId);
		mgr.SaveValueToSharedPrefs("EngageTime", time);
		// mgr.SaveValueToSharedPrefs("DriverName", driverName);
		mgr.SaveValueToSharedPrefs("DriverID", driverId);
		mgr.SaveValueToSharedPrefs("fPick", pick);
		mgr.SaveValueToSharedPrefs("fDrop", drop);

		// mgr.SaveValueToSharedPrefs("CabId", "");
		// mgr.SaveValueToSharedPrefs("CabNo", "");
		// mgr.SaveValueToSharedPrefs("DriverName", "");
		// mgr.SaveValueToSharedPrefs("Mobile", "");
		// mgr.SaveValueToSharedPrefs("TripStatus", "");
		// mgr.SaveValueToSharedPrefs("message", "");
		// mgr.SaveValueToSharedPrefs("DriverImage", "");

		Intent i = new Intent(PassengerTripDirection.this,
				PassengerFeedback.class);
		startActivity(i);

		finish();

	}

	private Marker trackingMarker;
	// private List<LatLng> latLngsMain = new ArrayList<LatLng>();
	private List<String> latLngsMain = new ArrayList<String>();

	private void GetDriverPoint(String driverpoint, String cabType)
	{
		try
		{
			if (!isDriverLoc)
			{
				StringTokenizer token_ = new StringTokenizer(driverpoint, ",");
				String dLongi = token_.nextToken();
				String dLati = token_.nextToken();

				final LatLng driverLoc = new LatLng(Double.parseDouble(dLati),
						Double.parseDouble(dLongi));

				int img = R.drawable.car_premium_move;
				if (cabType.equalsIgnoreCase("auto"))
				{
					img = R.drawable.auto_move;
				}
				if (cabType.equalsIgnoreCase("mini"))
				{
					img = R.drawable.car_mini_move;
				}
				if (cabType.equalsIgnoreCase("sedan"))
				{
					img = R.drawable.car_sedan_move;
				}

				if (cabType.equalsIgnoreCase("premium"))
				{
					img = R.drawable.car_premium_move;
				}

				if (cabType.equalsIgnoreCase("luxy"))
				{
					img = R.drawable.car_luxury_move;
				}

				if (trackingMarker == null)
				{
					trackingMarker = googleMap.addMarker(new MarkerOptions()
							.position(driverLoc).flat(true).anchor(0.5f, 0.5f)
							.icon(BitmapDescriptorFactory.fromResource(img)));

					LatLngBounds.Builder builder = new LatLngBounds.Builder();
					builder.include(driverLoc);

					LatLngBounds bounds = builder.build();

					// CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(
					// bounds, 1);
					Log.i(TAG, "max=" + googleMap.getMaxZoomLevel() + " "
							+ googleMap.getMinZoomLevel());
					CameraUpdate cu = CameraUpdateFactory
							.newLatLngZoom(
									bounds.getCenter(),
									googleMap.getCameraPosition().zoom >= 18 ? googleMap
											.getCameraPosition().zoom : 18);

					googleMap.moveCamera(cu);

				}
				else
				{

					trackingMarker.setPosition(driverLoc);
				}

			}

			isDriverLoc = true;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	private void GetDistance(final String dis)
	{

		PassengerTripDirection.this.runOnUiThread(new Runnable()
		{

			@Override
			public void run()
			{
				pbLoader.setVisibility(View.GONE);

				Log.e(TAG, "dis b " + dis);
				int time = (int) getDuration(dis, 6);
				int distance = (int) getDistanceInMeters(dis); // in meters
				Log.e(TAG, "dis a " + time + " " + distance);

				driverDisInfoLayout.setVisibility(View.VISIBLE);

				String status = mgr.GetValueFromSharedPrefs("TripStatus");

				if (status.equalsIgnoreCase("START"))
				{
					tripMessages.setText("Journey in progress");
					callToDriver.setVisibility(View.INVISIBLE);
				}
				else if (status.equalsIgnoreCase("FINISH"))
				{

					tripMessages.setText("Arrived at destination.");
				}
				else
				{

					if (distance == 0)
					{
						mgr.SaveValueToSharedPrefs("IsNotify", "passengerGetIn");
						tripMessages.setText("Reached");
					}
					else if (distance <= 50)
					{
						// tripMessages.setText("Arriving in " + distance
						// + " Min.");
						tripMessages.setText("Reached");
					}
					else
					{
						tripMessages.setText("Arriving in " + time + " Min.");
					}
				}
			}
		});

	}

	public static class DirectionsResult
	{

		@Key("routes")
		public List<Route> routes;

	}

	public static class Route
	{
		@Key("overview_polyline")
		public OverviewPolyLine overviewPolyLine;

	}

	public static class OverviewPolyLine
	{
		@Key("points")
		public String points;

	}

	private Animator animator = new Animator();
	private final Handler mHandler = new Handler();

	public class Animator implements Runnable
	{

		// private static final int ANIMATE_SPEEED = 1500;
		// private static final int ANIMATE_SPEEED_TURN = 0;

		private final Interpolator interpolator = new LinearInterpolator();

		private boolean animating = false;

		private List<String> latLngs = new ArrayList<String>();

		int currentIndex = 0;

		float tilt = 90;
		float zoom = 15.5f;
		boolean upward = true;

		long start = SystemClock.uptimeMillis();

		LatLng endLatLng = null;
		LatLng beginLatLng = null;

		long beginLocTime;
		long endLocTime;

		boolean showPolyline = false;

		public void reset()
		{

			try
			{
				start = SystemClock.uptimeMillis();
				currentIndex = 0;

				if (latLngs.size() > 1)
				{

					setLatLngTime();
					// endLatLng = getEndLatLng();
					// beginLatLng = getBeginLatLng();
				}
			}
			catch (Exception ex)
			{
				animator.stopAnimation();
				this.animating = false;
			}
			Log.e("tag", "reset2=" + currentIndex);

		}

		private void setLatLngTime()
		{

			// end lat lng
			StringTokenizer EndLatLngTimetoken = new StringTokenizer(
					getEndLatLng(), ",");

			String endLat = EndLatLngTimetoken.nextToken();
			String endLong = EndLatLngTimetoken.nextToken();
			String endTime = EndLatLngTimetoken.nextToken();

			LatLng endTmpLatLng = new LatLng(Double.parseDouble(endLat),
					Double.parseDouble(endLong));
			endLatLng = endTmpLatLng;
			endLocTime = Long.parseLong(endTime);

			// begin lat lng

			StringTokenizer beginLatLngTimetoken = new StringTokenizer(
					getBeginLatLng(), ",");

			String beginLat = beginLatLngTimetoken.nextToken();
			String beginLong = beginLatLngTimetoken.nextToken();
			String beginTime = beginLatLngTimetoken.nextToken();

			LatLng beginTmpLatLng = new LatLng(Double.parseDouble(beginLat),
					Double.parseDouble(beginLong));
			beginLatLng = beginTmpLatLng;
			beginLocTime = Long.parseLong(beginTime);

			// Double heading = SphericalUtil.computeHeading(beginLatLng,
			// endLatLng);
			//
			// trackingMarker.setRotation(heading.floatValue());

		}

		public void stop()
		{
			this.animating = false;

			mHandler.removeCallbacks(animator);

		}

		public void stopAnimation()
		{

			// if (latLngsMain.size() != 1)
			latLngsMain.removeAll(this.latLngs);
			Log.e("stopAnimation()", "stopAnimation()" + latLngsMain.toString());

			animator.stop();

		}

		public void initialize(boolean showPolyLine)
		{
			reset();

			LatLng markerPos = beginLatLng;
			LatLng secondPos = endLatLng;

			// LatLng markerPos = latLngs.get(0);
			// LatLng secondPos = latLngs.get(1);

			setInitialCameraPosition(markerPos, secondPos);

		}

		private void setInitialCameraPosition(LatLng markerPos, LatLng secondPos)
		{

			// float bearing = GoogleMapUtis.bearingBetweenLatLngs(markerPos,
			// secondPos);

			Double bearing = SphericalUtil.computeHeading(markerPos, secondPos);

			int img = R.drawable.car_premium_move;
			if (cabType.equalsIgnoreCase("auto"))
			{
				img = R.drawable.auto_move;
			}
			if (cabType.equalsIgnoreCase("mini"))
			{
				img = R.drawable.car_mini_move;
			}
			if (cabType.equalsIgnoreCase("sedan"))
			{
				img = R.drawable.car_sedan_move;
			}

			if (cabType.equalsIgnoreCase("premium"))
			{
				img = R.drawable.car_premium_move;
			}

			if (cabType.equalsIgnoreCase("luxy"))
			{
				img = R.drawable.car_luxury_move;
			}

			if (trackingMarker == null)
			{
				trackingMarker = googleMap.addMarker(new MarkerOptions()
						.position(markerPos).flat(true).anchor(0.5f, 0.5f)
						.icon(BitmapDescriptorFactory.fromResource(img)));

				// CameraPosition cameraPosition = new CameraPosition.Builder()
				// .target(markerPos)
				// .zoom(googleMap.getCameraPosition().zoom >= 16 ? googleMap
				// .getCameraPosition().zoom : 16).build();
				//
				// googleMap.animateCamera(
				// CameraUpdateFactory.newCameraPosition(cameraPosition),
				// ANIMATE_SPEEED_TURN, new CancelableCallback() {
				//
				// @Override
				// public void onFinish() {
				// animator.reset();
				//
				// }
				//
				// @Override
				// public void onCancel() {
				//
				// }
				// });

				LatLngBounds.Builder builder = new LatLngBounds.Builder();
				builder.include(secondPos);

				LatLngBounds bounds = builder.build();

				// CameraUpdate cu = CameraUpdateFactory
				// .newLatLngBounds(bounds, 1);

				CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(
						bounds.getCenter(),
						googleMap.getCameraPosition().zoom >= 18 ? googleMap
								.getCameraPosition().zoom : 18);

				googleMap.moveCamera(cu);

			}
			else
			{
				if (secondPos != null)
				{
					trackingMarker.setPosition(secondPos);
					trackingMarker.setRotation(bearing.floatValue());
				}
			}

			Handler handler = new Handler();
			handler.post(animator);

		}

		public void startAnimation(boolean showPolyLine, List<String> latLngs,
				Animator anim)
		{

			this.animating = true;

			latLngsMain.removeAll(this.latLngs);
			this.latLngs.clear();
			this.latLngs.addAll(latLngs);

			Log.e(TAG, "startAnimation " + this.latLngs);

			if (latLngs.size() > 1)
			{
				initialize(showPolyLine);
			}
			else
			{
				animator.stopAnimation();
			}

		}

		public boolean isAnimating()
		{
			return this.animating;
		}

		@Override
		public void run()
		{

			try
			{

				long elapsed = SystemClock.uptimeMillis() - start;

				// double t = interpolator.getInterpolation((float) elapsed
				// / ANIMATE_SPEEED);
				double distanceBetween = SphericalUtil.computeDistanceBetween(
						beginLatLng, endLatLng);

				long animTime = (endLocTime - beginLocTime);

				// Log.i(TAG, "distanceBetween=" + distanceBetween + " "
				// + animTime);

				double t = interpolator.getInterpolation((float) elapsed
						/ animTime);

				double lat = t * endLatLng.latitude + (1 - t)
						* beginLatLng.latitude;
				double lng = t * endLatLng.longitude + (1 - t)
						* beginLatLng.longitude;
				LatLng newPosition = new LatLng(lat, lng);
				// LatLng newPosition = SphericalUtil.interpolate(beginLatLng,
				// endLatLng, t);

				trackingMarker.setPosition(newPosition);

				// if (distanceBetween > 1) {
				if (t < 1)
				{

					Log.i(TAG, "distanceBetween=t < 1  " + distanceBetween
							+ " " + animTime);
					if (distanceBetween <= 1)
					{
						currentIndex++;

						setLatLngTime();
					}
					else if (distanceBetween >= 500)
					{

						stopAnimation();
					}

					if (!(currentIndex < latLngs.size() - 2))
					{
						stopAnimation();
					}

					mHandler.postDelayed(this, 16);

				}
				else
				{

					if (currentIndex < latLngs.size() - 2)
					{

						currentIndex++;

						setLatLngTime();
						// endLatLng = getEndLatLng();
						// beginLatLng = getBeginLatLng();

						// start = SystemClock.uptimeMillis();

						// Double heading = SphericalUtil.computeHeading(
						// beginLatLng, endLatLng);
						//
						// trackingMarker.setRotation(heading.floatValue());

						start = SystemClock.uptimeMillis();
						mHandler.postDelayed(this, 16);

						Log.i(TAG,
								"distanceBetween=currentIndex < latLngs.size()"
										+ distanceBetween + " " + animTime);

					}
					else
					{
						currentIndex++;

						Log.i(TAG,
								"distanceBetween=currentIndex > latLngs.size()"
										+ distanceBetween + " " + animTime);

						stopAnimation();

					}

				}
				// } else {
				// if (currentIndex < latLngs.size() - 2) {
				//
				// currentIndex++;
				//
				// setLatLngTime();
				//
				// }
				// else {
				// currentIndex++;
				//
				// stopAnimation();
				//
				// }
				// }

			}
			catch (Exception ex)
			{
				stopAnimation();
				this.animating = false;
			}
		}

		private String getEndLatLng()
		{
			return latLngs.get(currentIndex + 1);
		}

		private String getBeginLatLng()
		{
			return latLngs.get(currentIndex);
		}

		// public void updateLatlng(List<String> latLngs) {
		// this.latLngs.addAll(latLngs);
		// }

	};

	private double getDuration(String distance, int factor)
	{

		double dis = Double.parseDouble(distance);

		double dist = (double) Math.ceil(dis * factor);

		return dist;
	}

	private double getDistanceInMeters(String distance)
	{

		double dis = Double.parseDouble(distance);

		return dis * 1000;

	}

	class CustomTimerTask extends TimerTask
	{
		Context context;
		String isDriver = "";
		private Handler mHandler = new Handler();

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
					final Handler handler = new Handler();
					handler.post(new Runnable()
					{
						@Override
						public void run()
						{
							RunUiOnFetcher();
						}
					});
				}
			});

		}
	}

	private void RunUiOnFetcher()
	{
		String driverId = "", engId = "", userId = "";
		String status = mgr.GetValueFromSharedPrefs("TripStatus");
		driverId = mgr.GetValueFromSharedPrefs("DriverID");

		if (status.equalsIgnoreCase("START") && !TextUtils.isEmpty(driverId))
		{

			Log.e(TAG, "start");
			engId = mgr.GetValueFromSharedPrefs("EngID");
			NetworkHandler.tripDirectionRequest(TAG, handler, status, engId,
					driverId, "", null);

			driverDisInfoLayout.setVisibility(View.VISIBLE);

		}
		else if (status.equalsIgnoreCase("ENGAGED"))
		{

			Log.e(TAG, "engaged");

			engId = mgr.GetValueFromSharedPrefs("EngID");
			userId = mgr.GetValueFromSharedPrefs("UserID");

			NetworkHandler.tripDirectionRequest(TAG, handler, status, engId,
					"", userId, googleMap.getMyLocation());

		}
		else if (status.equalsIgnoreCase("FINISH"))
		{

			Log.e(TAG, "finish");

			mgr.SaveValueToSharedPrefs("TripStatus", "");
			mgr.SaveValueToSharedPrefs("routeVal", "");

			if (timer != null)
			{
				timer.cancel();
				timer.purge();
				timer = null;
			}
		}

		else if (status.equalsIgnoreCase("AC"))
		{

			Log.e(TAG, "auto cancel");

			mgr.SaveValueToSharedPrefs("TripStatus", "");
			mgr.SaveValueToSharedPrefs("routeVal", "");

			if (timer != null)
			{
				timer.cancel();
				timer.purge();
				timer = null;
			}
		}
		else
		{
			engId = mgr.GetValueFromSharedPrefs("EngID");
			userId = mgr.GetValueFromSharedPrefs("UserID");

			Log.e(TAG, "default " + engId + " " + userId);

			NetworkHandler.tripDirectionRequest(TAG, handler, status, engId,
					"", userId, googleMap.getMyLocation());
		}
	}

	private void UpdateDriverInfo()
	{
		String status = mgr.GetValueFromSharedPrefs("TripStatus");

		driverMobileNo = mgr.GetValueFromSharedPrefs("Mobile");

		if (status.equalsIgnoreCase("ENGAGED"))
		{
			driverUpdateLayout.setVisibility(View.VISIBLE);
			ImageView driverImage = (ImageView) findViewById(R.id.deriverPhotoP);
			// JugunooUtil util = new JugunooUtil(PassengerTripDirection.this);
			// Bitmap b = util.driverImage(gPhoto);
			// image.setImageBitmap(b);
			ImageLoader.getInstance()
					.displayImage(gPhoto, driverImage, options);

			driverNameText.setText(gDriverName);
			driverRateText.setText(gRate);
			driverMobileText.setText(gCabNo);
			driverDisInfoLayout.setVisibility(View.VISIBLE);
			tripMessages.setText("Arriving...");
		}
	}

	private void cancelMyTrip()
	{

		try
		{
			// pdRunnable = new Runnable()
			// {
			// @Override
			// public void run()
			// {
			// if (pd != null)
			// {
			// if (pd.isShowing())
			// {
			// pd.dismiss();
			// }
			// }
			// }
			// };
			// pd.show();

			showLoadingDilog();

			Map<String, String> params = new HashMap<String, String>();
			params.put("UserId", mgr.GetValueFromSharedPrefs("UserID"));
			params.put("EngId", mgr.GetValueFromSharedPrefs("EngID"));
			params.put("StatusId", "PC");

			Log.e(TAG, "user id" + mgr.GetValueFromSharedPrefs("UserID") + " "
					+ mgr.GetValueFromSharedPrefs("EngID"));

			NetworkHandler.cancelTripRequest(TAG, handler, params);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	@Override
	public void finish()
	{
		try
		{
			if (pd != null)
			{
				if (pd.isShowing())
				{
					pd.dismiss();
				}
			}
			if (dialog != null)
			{
				dialog.dismiss();
			}

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

	BroadcastReceiver networkStateReceiver = new BroadcastReceiver()
	{

		@Override
		public void onReceive(Context context, Intent intent)
		{

			if (JugunooUtil.isConnectedToInternet(getApplicationContext()))
			{

				// Toast.makeText(PassengerTripDirection.this, "online",
				// Toast.LENGTH_SHORT).show();
			}
			else
			{
				// Toast.makeText(PassengerTripDirection.this, "offline",
				// Toast.LENGTH_SHORT).show();
			}

		}
	};

	Handler handler = new Handler()
	{

		@Override
		public void handleMessage(Message msg)
		{
			Log.i(TAG, "Handler()=" + msg.arg1);
			switch (msg.arg1)
			{
				case Constant.MessageState.LOGOUT_SUCCESS:
					cancelLoadingDialog();
					mgr.SaveValueToSharedPrefs("UserID", "");
					mgr.SaveValueToSharedPrefs("UserRole", "");

					Intent logoutIntent = new Intent(
							PassengerTripDirection.this, SplashScreen.class);
					logoutIntent.addFlags(Intent.FLAG_ACTIVITY_TASK_ON_HOME);
					startActivity(logoutIntent);
					PassengerTripDirection.this.finish();
					break;

				case Constant.MessageState.LOGOUT_FAIL:
					cancelLoadingDialog();
					VolleyErrorHelper.getMessage(msg.obj,
							PassengerTripDirection.this, false);
					break;

				case Constant.MessageState.TRIP_CANCEL_SUCCESS:
					cancelLoadingDialog();
					JSONObject jsonobjCancelTrip = (JSONObject) msg.obj;
					parseCancelTripResponse(jsonobjCancelTrip);

					// showDialog("Booking is Cancelled.");
					// Intent landingPageIntent = new Intent(
					// PassengerTripDirection.this, LandingPage.class);
					// landingPageIntent.putExtra("from", "passengerTrip");
					// startActivity(landingPageIntent);
					// PassengerTripDirection.this.finish();
					//
					// mgr.SaveValueToSharedPrefs("CabId", "");
					// mgr.SaveValueToSharedPrefs("CabNo", "");
					// mgr.SaveValueToSharedPrefs("DriverName", "");
					// mgr.SaveValueToSharedPrefs("Mobile", "");
					break;

				case Constant.MessageState.TRIP_CANCEL_FAIL:
					cancelLoadingDialog();
					VolleyErrorHelper.getMessage(msg.obj,
							PassengerTripDirection.this, false);
					break;

				case Constant.MessageState.TRIP_DIRECTION_SUCCESS:
					JSONObject jsonObj = (JSONObject) msg.obj;
					parseTripDirectionRespose(jsonObj);
					break;

				case Constant.MessageState.DRIVER_PHOTO_SUCCESS:
					JSONObject jsonobjDriver = (JSONObject) msg.obj;
					driverPhotoParser(jsonobjDriver);
					break;

				case Constant.MessageState.MAP_PLOT_SUCCESS:

					String routeVal = (String) msg.obj;
					mgr.SaveValueToSharedPrefs("routeVal", routeVal);
					Log.i(TAG, "routeVal --> " + routeVal);
					new ParserTask().execute(routeVal);
					break;

				case Constant.MessageState.FAIL:
					Log.e(TAG, "volley http err failed");
					VolleyErrorHelper.getMessage(msg.obj,
							PassengerTripDirection.this, false);
					break;

				case Constant.MessageState.DEVICE_REGISTRATION_SUCCESS:
					parseDeviceRegResp((JSONObject) msg.obj);
					break;

				default:
					break;
			}
		}
	};

	private void parseCancelTripResponse(JSONObject jsonOb)
	{
		Log.e(TAG, "cancel trip=" + jsonOb);

		try
		{
			if (jsonOb.has(Constant.RESULT))
			{
				String result = jsonOb.getString(Constant.RESULT);
				if (result.equalsIgnoreCase(Constant.Result_STATE.PASS))
				{

					clearNotification();
					// showDialog("Booking is Cancelled.");
					Intent landingPageIntent = new Intent(
							PassengerTripDirection.this, LandingPage.class);
					landingPageIntent.putExtra("from", "passengerTrip");
					startActivity(landingPageIntent);
					PassengerTripDirection.this.finish();

					mgr.SaveValueToSharedPrefs("CabId", "");
					mgr.SaveValueToSharedPrefs("CabNo", "");
					mgr.SaveValueToSharedPrefs("DriverName", "");
					mgr.SaveValueToSharedPrefs("Mobile", "");
					mgr.SaveValueToSharedPrefs("TripStatus", "");
					mgr.SaveValueToSharedPrefs("routeVal", "");

				}
				else
				{

					showDialog("Unable to Cancel. Try again.");

				}
			}

		}
		catch (Exception bug)
		{
			bug.printStackTrace();
		}

	}

	private void parseTripDirectionRespose(JSONObject jsonOb)
	{
		try
		{
			Log.e(TAG, "parseTripDirectionRespose" + " " + jsonOb.toString());

			if (jsonOb.has(Constant.RESULT))
			{
				String result = jsonOb.getString(Constant.RESULT);
				if (result.equalsIgnoreCase(Constant.Result_STATE.PASS))
				{

					if (jsonOb.has("Notification"))
					{

						JSONObject jsonNotiObj = jsonOb
								.getJSONObject("Notification");
						String status = jsonNotiObj.getString("TripStatus");

						onReceiveNotification(status, jsonNotiObj);
					}

					if (jsonOb.has("LocArray"))
					{
						if (jsonOb.has("DriverPoint"))
						{
							driverpoint = jsonOb.getString("DriverPoint");
						}
						Log.e(TAG, "driveP  " + driverpoint + " ");

						cabType = jsonOb.getString("CabType");

						JSONArray jsonArr = jsonOb.getJSONArray("LocArray");
						String distanceStr = jsonOb.getString("dis");
						GetDistance(distanceStr);

						// final List<LatLng> path = new ArrayList<LatLng>();
						final List<String> path = new ArrayList<String>();

						for (int i = 0; i < jsonArr.length(); i++)
						{
							JSONObject obj = jsonArr.getJSONObject(i);
							JSONArray arr = obj.getJSONArray("loc");
							double lng = arr.getDouble(0);
							double lat = arr.getDouble(1);
							driverpoint = String.valueOf(lng) + ","
									+ String.valueOf(lat);

							long locTime = obj.getLong("LocationTime");

							String latLngAndTime = String.valueOf(lat) + ","
									+ String.valueOf(lng) + ","
									+ String.valueOf(locTime);

							// LatLng updateLng = new LatLng(lat, lng);
							// path.add(updateLng);
							path.add(latLngAndTime);

							Log.i(TAG, "lat=" + lat + " lng=" + lng + " time="
									+ locTime + "   latLngAndTime="
									+ latLngAndTime);
						}

						if (!path.isEmpty())
						{
							latLngsMain.addAll(path);
						}

						// when to start anim

						if (firstTime)
						{
							Log.i("tag", "firstTime=");

							if (latLngsMain.isEmpty())
							{
								if (!TextUtils.isEmpty(driverpoint))
								{
									GetDriverPoint(driverpoint, cabType);
								}
							}
							else
							{
								firstTime = false;
								// if (!animator.isAnimating()) {
								animator.startAnimation(false, latLngsMain,
										null);
								// }
							}

						}
						else
						{
							if (latLngsMain.isEmpty())
							{
								if (!TextUtils.isEmpty(driverpoint))
								{
									GetDriverPoint(driverpoint, cabType);
								}
							}
							else
							{
								// if (!animator.isAnimating()) {

								animator.startAnimation(false, latLngsMain,
										null);
								// }

							}

						}
					}

				}
			}
		}
		catch (Exception bug)
		{
			bug.printStackTrace();
		}

	}

	private void onReceiveNotification(String status, JSONObject jsonNotiObj)
	{

		Log.i(TAG, "onReceiveNotification=" + status);
		try
		{
			// WakeLocker.acquire(getApplicationContext());

			String header = jsonNotiObj.getString("Header");
			String title = jsonNotiObj.getString("Title");

			if (status.equalsIgnoreCase("H"))
			{

				String tripState = mgr
						.GetNotiValueFromSharedPrefs("TripStatus");

				if (!tripState.equalsIgnoreCase("ENGAGED"))
				{
					jugunooAlert("Dear user, your booking request is accepted by the driver");

					mgr.SaveValueToSharedPrefs("TripStatus", "ENGAGED");

					gMessage = jsonNotiObj.getString("message");
					gDriverName = jsonNotiObj.getString("FirstName");
					gDriverMobile = jsonNotiObj.getString("Mobile");
					gTripId = jsonNotiObj.getString("TripId");
					gCabNo = jsonNotiObj.getString("CabNo");
					gCabId = jsonNotiObj.getString("CabId");
					driverID = jsonNotiObj.getString("DriverId");
					// String headerMsg = jsonNotiObj.getString("Header");

					mgr.SaveValueToSharedPrefs("EngID", gTripId);
					mgr.SaveValueToSharedPrefs("DriverID", driverID);
					mgr.SaveValueToSharedPrefs("CabId", gCabId);
					mgr.SaveValueToSharedPrefs("CabNo", gCabNo);
					mgr.SaveValueToSharedPrefs("DriverMobile", gDriverMobile);
					mgr.SaveValueToSharedPrefs("message", gMessage);
					mgr.SaveValueToSharedPrefs("DriverImage", gDriverImage);

					if (driverID != null)
					{
						DriverDetailPopup(gMessage, gDriverName, gDriverMobile,
								gTripId, gTripStatus, gCabNo, gDriverImage,
								gCabId);

						UpdateDriverInfo();
					}

					Function.playNotificationSound(getApplicationContext());

					// sendNotification(header, title,
					// GCMNotificationHandler.NOTIFICATION_HIRED_ID);
				}

			}
			else if (status.equalsIgnoreCase("S"))
			{
				jugunooAlert("Dear user, your journey has started");

				String tripState = mgr
						.GetNotiValueFromSharedPrefs("TripStatus");

				if (!tripState.equalsIgnoreCase("START"))
				{
					// String headerMsg = jsonNotiObj.getString("Header");
					// String message = jsonNotiObj.getString("message");

					driverID = jsonNotiObj.getString("DriverId");

					String tripId = jsonNotiObj.getString("TripId");

					tripMessages.setText("Journey Started");
					callToDriver.setVisibility(View.INVISIBLE);

					mgr.SaveValueToSharedPrefs("TripID", tripId);
					mgr.SaveValueToSharedPrefs("DriverID", driverID);
					mgr.SaveValueToSharedPrefs("TripStatus", "START");
					// UpdateLayout(result, tripId, status, driverID);

					Function.playNotificationSound(getApplicationContext());

					// if (!Global.isActivityVisible())
					// {
					// sendNotification(header, title,
					// GCMNotificationHandler.NOTIFICATION_START_ID);
					// }
				}

			}
			else if (status.equalsIgnoreCase("F"))
			{
				String tripState = mgr
						.GetNotiValueFromSharedPrefs("TripStatus");

				if (!tripState.equalsIgnoreCase("FINISH"))
				{

					mgr.SaveValueToSharedPrefs("TripStatus", "FINISH");
					mgr.SaveValueToSharedPrefs("routeVal", "");
					clearNotification();

					// String headerMsg = jsonNotiObj.getString("Header");
					// String message = jsonNotiObj.getString("message");

					final String tripId = jsonNotiObj.getString("TripId");

					final String driverID = jsonNotiObj.getString("DriverId");
					final String time = jsonNotiObj.getString("EngageTime");
					final String pick = jsonNotiObj.getString("PickPoint");
					final String drop = jsonNotiObj.getString("DropPoint");
					mgr.SaveValueToSharedPrefs("DriverID", driverID);

					tripMessages.setText("Arrived at destination");

					mgr.SaveValueToSharedPrefs("CabId", "");
					mgr.SaveValueToSharedPrefs("CabNo", "");
					mgr.SaveValueToSharedPrefs("DriverName", "");
					mgr.SaveValueToSharedPrefs("Mobile", "");
					// mgr.SaveValueToSharedPrefs("TripStatus", "");
					mgr.SaveValueToSharedPrefs("message", "");
					mgr.SaveValueToSharedPrefs("DriverImage", "");

					// if (!Global.isActivityVisible())
					// {
					// sendNotification(header, title,
					// GCMNotificationHandler.NOTIFICATION_ID);
					// }

					Function.playNotificationSound(getApplicationContext());

					TripFeedback(tripId, "", driverID, pick, drop, time);
					mgr.SaveValueToSharedPrefs("TripStatus", "");

					// new Handler().postDelayed(new Runnable()
					// {
					// @Override
					// public void run()
					// {
					// TripFeedback(tripId, "", driverID, pick, drop, time);
					// mgr.SaveValueToSharedPrefs("TripStatus", "");
					// }
					// }, 500);
				}

			}
			else if (status.equalsIgnoreCase("AB"))
			{
				mgr.SaveValueToSharedPrefs("TripStatus", "");
				mgr.SaveValueToSharedPrefs("routeVal", "");

				sendNotification(header, title,
						GCMNotificationHandler.NOTIFICATION_HIRED_ID);

				if (timer != null)
				{
					timer.cancel();
					timer.purge();
					timer = null;
				}

				// showDialog("Sorry, Your trip is Aborted.");
				Intent landingPageIntent = new Intent(
						PassengerTripDirection.this, LandingPage.class);
				landingPageIntent.putExtra("isAB", "true");
				startActivity(landingPageIntent);
				PassengerTripDirection.this.finish();

			}
			else if (status.equalsIgnoreCase("AC"))
			{
				mgr.SaveValueToSharedPrefs("TripStatus", "AC");
				mgr.SaveValueToSharedPrefs("routeVal", "");

				sendNotification(header, title,
						GCMNotificationHandler.NOTIFICATION_HIRED_ID);

				Intent landingPageIntent = new Intent(
						PassengerTripDirection.this, LandingPage.class);
				landingPageIntent.putExtra("isAC", "true");
				startActivity(landingPageIntent);
				PassengerTripDirection.this.finish();
			}

			// WakeLocker.release();

		}
		catch (Exception bug)
		{
			bug.printStackTrace();
		}

	}

	private void sendNotification(String title, String msg, int id)
	{
		try
		{
			Log.e("gcm", "notification title - " + title + "msg - " + msg);

			Intent intent = new Intent(getApplicationContext(),
					LandingPage.class);

			mNotificationManager = (NotificationManager) this
					.getSystemService(Context.NOTIFICATION_SERVICE);

			PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
					intent, PendingIntent.FLAG_UPDATE_CURRENT);

			Notification notification = null;

			builder = new NotificationCompat.Builder(this)
					.setSmallIcon(R.drawable.ic_launcher)
					.setContentTitle(title)
					.setStyle(
							new NotificationCompat.BigTextStyle().bigText(msg))
					.setLights(Color.BLUE, 500, 1000).setAutoCancel(true)
					.setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
					.setWhen(System.currentTimeMillis()).setContentText(msg);

			builder.setContentIntent(contentIntent);

			notification = builder.build();

			mNotificationManager.notify(id, notification);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

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

	private void clearNotification()
	{
		NotificationManager notificationManager = (NotificationManager) getApplicationContext()
				.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancelAll();
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
					dialogCheckData = new Dialog(PassengerTripDirection.this);
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

	@Override
	public void gcmIdListener(String userId, String deviceId, String gcmId)
	{
		Log.i(Global.APPTAG, "GCM: id --> onListener -- " + gcmId);
		GcmId = gcmId;

		HashMap<String, String> params = new HashMap<String, String>();
		params.put("UserId", userId);
		params.put("GcmId", gcmId);
		params.put("DeviceId", deviceId);

		Log.i(TAG, "test params2 --> " + params);

		makeRegistrationReq(params);
	}

	private void makeRegistrationReq(HashMap<String, String> params)
	{
		NetworkHandler.deviceRegistration(TAG, handler, params);
	}

	private void parseDeviceRegResp(JSONObject obj)
	{
		try
		{
			Log.i(TAG, "test params2 --> " + obj);

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

	private void jugunooAlert(String msg)
	{
		final Dialog dialog = new Dialog(PassengerTripDirection.this);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.dialog_common_alert);
		dialog.setCancelable(true);

		TextView tvTitle = (TextView) dialog.findViewById(R.id.tvAlertHeader);
		TextView tvMsg = (TextView) dialog.findViewById(R.id.tvAlertMsg);
		Button rightBtn = (Button) dialog.findViewById(R.id.btAlertOk);
		Button leftBtn = (Button) dialog.findViewById(R.id.btAlertCancel);
		leftBtn.setVisibility(View.GONE);

		tvTitle.setText(ConstantMessages.MSG91);
		tvMsg.setText(msg);
		rightBtn.setText(ConstantMessages.MSG93);

		dialog.show();

		rightBtn.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View arg0)
			{
				dialog.cancel();
			}
		});
	}
}