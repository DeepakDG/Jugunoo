package com.hirecraft.jugunoo.passenger;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hirecraft.jugunoo.passenger.common.Constant;
import com.hirecraft.jugunoo.passenger.common.ConstantMessages;
import com.hirecraft.jugunoo.passenger.common.Function;
import com.hirecraft.jugunoo.passenger.common.Global;
import com.hirecraft.jugunoo.passenger.networkhandler.NetworkHandler;
import com.hirecraft.jugunoo.passenger.services.PassengerTripDirection;
import com.hirecraft.jugunoo.passenger.services.VolleyErrorHelper;
import com.hirecraft.jugunoo.passenger.utility.JugunooUtil;
import com.hirecraft.jugunoo.passenger.utility.SharedPreferencesManager;

public class SplashScreen extends Activity implements
		android.view.View.OnClickListener
{

	// private LocationService mLocationService;
	// Location location;
	private String TAG = SplashScreen.class.getSimpleName();
	private Typeface bold, light, semibold;
	private static final int TIME = 3 * 1000;
	private SharedPreferencesManager mgr;
	private Button signIn, signUp;
	// Address mLastKnownAddress;
	// AddressResultListener mAddressResultListener;
	String addressStr = "", rate = "";
	ArrayList<HashMap<String, String>> feedHash = null;
	Dialog feedDialog;
	TextView tvMsg;
	int feedCount = 0, count = 0;

	private Handler pdHandler;
	private Runnable pdRunnable;

	// private TransparentProgressDialog pd;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash_screen);

		// mAddressResultListener = (AddressResultListener) this;
		// mAddressResultListener.onAddressAvailable(mLastKnownAddress);
		pdHandler = new Handler();

		InitFontScale();
		signIn = (Button) findViewById(R.id.signInsplash);
		signUp = (Button) findViewById(R.id.signUpSplash);
		tvMsg = (TextView) findViewById(R.id.msgForVerify);
		signIn.setOnClickListener(this);
		signUp.setOnClickListener(this);
		mgr = new SharedPreferencesManager(getApplicationContext());

		// mLocationService = new LocationService();
		// mLocationService.getLocation(getApplicationContext(), this);

		LoadSplash();
	}

	@Override
	protected void onRestart()
	{
		super.onRestart();
		LoadSplash();
	}

	@Override
	public void finish()
	{

		if (feedDialog != null)
			feedDialog.dismiss();
		super.finish();
	}

	private void InitFontScale()
	{
		light = Typeface.createFromAsset(getAssets(),
				"fonts/opensans-regular-webfont.ttf");
		bold = Typeface.createFromAsset(getAssets(),
				"fonts/opensans-bold-webfont.ttf");
		semibold = Typeface.createFromAsset(getAssets(),
				"fonts/opensans-semibold-webfont.ttf");
	}

	private void LoadSplash()
	{

		mgr.SaveValueToSharedPrefs("Lati", "");
		mgr.SaveValueToSharedPrefs("Longi", "");
		final String userID = mgr.GetValueFromSharedPrefs("UserID");

		Log.e(TAG, "spl userid=" + userID);

		if (!TextUtils.isEmpty(userID))
		{
			signIn.setVisibility(View.GONE);
			signUp.setVisibility(View.GONE);

			if (JugunooUtil.isConnectedToInternet(SplashScreen.this))
			{
				tvMsg.setVisibility(View.VISIBLE);
				tvMsg.setText("Verifying...");

				new Handler().postDelayed(new Runnable()
				{
					@Override
					public void run()
					{
						String url = Global.JUGUNOO_WS
								+ "Passenger/ValidateUser?UserId=" + userID;

						// CheckUser(url);
						makeUserCheckRequest(url);
						pdHandler.removeCallbacks(pdRunnable);

					}
				}, TIME);

			}
			else
			{
				Function.showToast(SplashScreen.this, ConstantMessages.MSG1);
			}

		}
		else
		{
			signIn.setVisibility(View.VISIBLE);
			signUp.setVisibility(View.VISIBLE);
		}

	}

	public static String Epoch2DateString(long epochSeconds, String formatString)
	{
		Date updatedate = new Date(epochSeconds * 1000);
		SimpleDateFormat format = new SimpleDateFormat(formatString,
				Locale.getDefault());
		return format.format(updatedate);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{

		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0)
		{
			if (feedDialog != null)
				feedDialog.dismiss();
			SplashScreen.this.finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
			case R.id.signInsplash:
				if (JugunooUtil.isConnectedToInternet(getApplicationContext()))
				{
					startActivity(new Intent(SplashScreen.this, Login.class));
					SplashScreen.this.finish();
				}
				else
				{
					Function.showToast(SplashScreen.this, ConstantMessages.MSG1);
				}
				break;

			case R.id.signUpSplash:
				if (JugunooUtil.isConnectedToInternet(getApplicationContext()))
				{
					startActivity(new Intent(SplashScreen.this,
							SignupTabActivity.class));
					SplashScreen.this.finish();
				}
				else
				{
					Function.showToast(SplashScreen.this, ConstantMessages.MSG1);
				}
				break;
		}

	}

	public static void showErrorMessage(final Context context,
			final RelativeLayout layout, final TextView textView, String message)
	{
		// Animation slideDown = AnimationUtils.loadAnimation(context,
		// R.anim.slide_up);

		layout.setVisibility(View.VISIBLE);
		// layout.setAnimation(slideDown);
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
						// Animation slideUp = AnimationUtils.loadAnimation(
						// context, R.anim.slide_down);
						//
						// layout.setAnimation(slideUp);
						layout.setVisibility(View.GONE);
					}
				});
			}
		});

		// Starting thread
		thread.start();
	}

	// @Override
	// public void onLocationResultAvailable(final Location location) {
	// this.location = location;
	// SplashScreen.this.runOnUiThread(new Runnable() {
	//
	// @Override
	// public void run() {
	// if (location == null) {
	// Log.i(Global.APPTAG, "Location is null: ");
	//
	// } else {
	// double lati = location.getLatitude();
	// double longi = location.getLongitude();
	// String lati_ = String.valueOf(lati);
	// String longi_ = String.valueOf(longi);
	// getAddress(lati, longi);
	//
	// mgr.SaveValueToSharedPrefs("Lati", lati_);
	// mgr.SaveValueToSharedPrefs("Longi", longi_);
	// }
	// }
	// });
	//
	// }

	// private void getAddress(double latitude, double longitude) {
	// GetMyAddress address = new GetMyAddress(getApplicationContext(),
	// latitude, longitude, this);
	// address.execute();
	// }
	//
	// @Override
	// public void MyAddress(String address) {
	// if (!address.equalsIgnoreCase("")) {
	// addressStr = address;
	// }
	// }

	// private void setAddressDetails(Address address) {
	// if (address.getMaxAddressLineIndex() > 0) {
	//
	// addressStr = address.getAddressLine(0);
	//
	// }
	// }

	// @Override
	// public void onAddressAvailable(Address address) {
	// if (address != null) {
	// mLastKnownAddress = address;
	// setAddressDetails(address);
	// }
	// }

	@Override
	public void onStop()
	{
		super.onStop();
		// if (mLocationService != null)
		// mLocationService.stop();
	}

	@SuppressLint("SimpleDateFormat")
	private String getReadableDate(String epoch)
	{
		Date date = new Date(Long.parseLong(epoch) * 1000L);
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss a");
		sdf.setTimeZone(TimeZone.getDefault());
		String formatted = sdf.format(date);
		return formatted;
	}

	private void JugunooInteractiveDialog(String title, String message)
	{
		final Dialog dialog = new Dialog(SplashScreen.this);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		Window window = dialog.getWindow();
		window.setBackgroundDrawableResource(android.R.color.transparent);
		LinearLayout.LayoutParams dialogParams = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		RelativeLayout lay = new RelativeLayout(SplashScreen.this);
		View dislogView = inflater.inflate(R.layout.jugunoo_interactive_dialog,
				lay);
		dialog.setContentView(dislogView, dialogParams);

		TextView textView = (TextView) dialog.findViewById(R.id.messageText);
		Button centerBtn = (Button) dialog.findViewById(R.id.centerBtn);
		Button leftBtn = (Button) dialog.findViewById(R.id.leftBtn);
		Button rightBtn = (Button) dialog.findViewById(R.id.rightBtn);

		centerBtn.setVisibility(View.GONE);
		String settings = getResources().getString(R.string.settings);
		String cancel = getResources().getString(R.string.cancel);
		textView.setText(message);
		leftBtn.setText(cancel);
		rightBtn.setText(settings);
		if (title.equalsIgnoreCase("NET"))
		{
			leftBtn.setOnClickListener(new OnClickListener()
			{

				@Override
				public void onClick(View v)
				{
					SplashScreen.this.finish();
					dialog.dismiss();
				}
			});
			rightBtn.setOnClickListener(new OnClickListener()
			{

				@Override
				public void onClick(View v)
				{
					dialog.dismiss();
					Intent settings = new Intent(
							Settings.ACTION_WIRELESS_SETTINGS);
					startActivityForResult(settings, 200);

				}
			});
		}
		else if (title.equalsIgnoreCase("GPS"))
		{

			centerBtn.setVisibility(View.GONE);

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
					SplashScreen.this
							.startActivityForResult(settingIntent, 100);
					dialog.dismiss();
				}
			});
		}
		dialog.show();
	}

	// private void CheckUser(String url)
	// {
	//
	// tvMsg.setVisibility(View.VISIBLE);
	// tvMsg.setText("Verifying...");
	//
	// AsyncHttpClient client = new AsyncHttpClient();
	// client.setTimeout(30000);
	//
	// if (JugunooUtil.isConnectedToInternet(getApplicationContext()))
	// {
	//
	// client.get(url, new AsyncHttpResponseHandler()
	// {
	//
	// @Override
	// @Deprecated
	// public void onFailure(int statusCode, Throwable error,
	// String content)
	// {
	// tvMsg.setVisibility(View.GONE);
	// // signIn.setVisibility(View.VISIBLE);
	// // signUp.setVisibility(View.VISIBLE);
	// try
	// {
	// if (statusCode == 0)
	// {
	// showDialog("Login failed due to  connection time expired. Try again.");
	//
	// }
	// else
	// {
	//
	// showDialog(getString(R.string.network_error_msg));
	//
	// }
	//
	// }
	// catch (Exception bug)
	// {
	// bug.printStackTrace();
	// }
	//
	// super.onFailure(statusCode, error, content);
	//
	// pdHandler.removeCallbacks(pdRunnable);
	//
	// }
	//
	// @Override
	// @Deprecated
	// public void onSuccess(int statusCode, String content)
	// {
	//
	// Log.i("sp", "loaduser=" + content);
	//
	// try
	// {
	// if (statusCode == 200)
	// {
	//
	// try
	// {
	// String result = "", status = "";
	// JSONObject obj = new JSONObject(content);
	//
	// Log.i("Splash", "check user=" + content);
	//
	// result = obj.getString("Result");
	// if (result.equalsIgnoreCase("Pass"))
	// {
	// status = obj.getString("Status");
	//
	// Log.e("Status", status);
	//
	// mgr.SaveValueToSharedPrefs("UserStatus",
	// status);
	//
	// boolean con = JugunooUtil
	// .isConnectedToInternet(getApplicationContext());
	// if (con)
	// {
	// if (status.equalsIgnoreCase("A"))
	// {
	//
	// mgr.SaveValueToSharedPrefs(
	// "TripStatus", "");
	// mgr.SaveValueToSharedPrefs(
	// "routeVal", "");
	// mgr.SaveValueToSharedPrefs("EngID",
	// "");
	// if (obj.has("UserType"))
	// {
	// mgr.SaveValueToSharedPrefs(
	// Constant.USER_TYPE,
	// obj.getString("UserType"));
	// }
	//
	// Intent landIntent = new Intent(
	// SplashScreen.this,
	// LandingPage.class);
	// landIntent.putExtra("from",
	// "splash");
	//
	// startActivity(landIntent);
	// SplashScreen.this.finish();
	// //
	//
	// }
	// else if (status.equalsIgnoreCase("H"))
	// {
	// // mgr.SaveValueToSharedPrefs(
	// // "TripStatus", "ENGAGED");
	// startActivity(new Intent(
	// SplashScreen.this,
	// PassengerTripDirection.class));
	// SplashScreen.this.finish();
	// }
	// else if (status.equalsIgnoreCase("S"))
	// {
	// // mgr.SaveValueToSharedPrefs(
	// // "TripStatus", "START");
	// startActivity(new Intent(
	// SplashScreen.this,
	// PassengerTripDirection.class));
	// SplashScreen.this.finish();
	// }
	// else if (status.equalsIgnoreCase("E"))
	// {
	// mgr.SaveValueToSharedPrefs(
	// "TripStatus", "");
	// startActivity(new Intent(
	// SplashScreen.this,
	// PassengerTripDirection.class));
	// SplashScreen.this.finish();
	// }
	// else if (status.equalsIgnoreCase("C"))
	// {
	// // mgr.SaveValueToSharedPrefs(
	// // "TripStatus", "CANCEL");
	// mgr.SaveValueToSharedPrefs(
	// "routeVal", "");
	// startActivity(new Intent(
	// SplashScreen.this,
	// PassengerTripDirection.class));
	// SplashScreen.this.finish();
	// }
	// else
	// {
	// tvMsg.setVisibility(View.GONE);
	// // signIn.setVisibility(View.VISIBLE);
	// // signUp.setVisibility(View.VISIBLE);
	// }
	//
	// }
	// else
	// {
	// JugunooInteractiveDialog("NET",
	// "You don't have Internet connection");
	// }
	//
	// }
	// else
	// {
	// signIn.setVisibility(View.VISIBLE);
	// signUp.setVisibility(View.VISIBLE);
	// tvMsg.setVisibility(View.INVISIBLE);
	// }
	//
	// }
	// catch (Exception bug)
	// {
	// bug.printStackTrace();
	// }
	//
	// }
	// else
	// {
	// tvMsg.setVisibility(View.GONE);
	// signIn.setVisibility(View.VISIBLE);
	// signUp.setVisibility(View.VISIBLE);
	// }
	//
	// }
	// catch (Exception bug)
	// {
	// bug.printStackTrace();
	// }
	//
	// super.onSuccess(statusCode, content);
	//
	// pdHandler.removeCallbacks(pdRunnable);
	//
	// }
	// });
	// }
	// else
	// {
	// JugunooInteractiveDialog("NET",
	// "You dont have an active internet connection!");
	// }
	// }

	private void makeUserCheckRequest(String url)
	{
		HashMap<String, String> params = new HashMap<String, String>();
		NetworkHandler.checkUserRequest(TAG, handlerCheckUser, params, url);
	}

	Handler handlerCheckUser = new Handler()
	{
		public void handleMessage(android.os.Message msg)
		{
			switch (msg.arg1)
			{
				case Constant.MessageState.CHECK_USER_SUCCESS:
					parseUserCheck((JSONObject) msg.obj);
					break;

				case Constant.MessageState.FAIL:
					tvMsg.setVisibility(View.GONE);
					pdHandler.removeCallbacks(pdRunnable);

					VolleyErrorHelper.getMessage(msg.obj, SplashScreen.this,
							true);
					tvMsg.setVisibility(View.GONE);
					signIn.setVisibility(View.VISIBLE);
					signUp.setVisibility(View.VISIBLE);

					break;
				default:
					break;
			}
		};
	};

	private void parseUserCheck(JSONObject obj)
	{
		try
		{
			String result = "", status = "";
			result = obj.getString("Result");

			if (result.equalsIgnoreCase("Pass"))
			{
				status = obj.getString("Status");
				mgr.SaveValueToSharedPrefs("UserStatus", status);

				boolean con = JugunooUtil
						.isConnectedToInternet(getApplicationContext());
				if (con)
				{
					if (status.equalsIgnoreCase("A"))
					{

						mgr.SaveValueToSharedPrefs("TripStatus", "");
						mgr.SaveValueToSharedPrefs("routeVal", "");
						mgr.SaveValueToSharedPrefs("EngID", "");
						if (obj.has("UserType"))
						{
							mgr.SaveValueToSharedPrefs(Constant.USER_TYPE,
									obj.getString("UserType"));
						}

						Intent landIntent = new Intent(SplashScreen.this,
								LandingPage.class);

						try
						{
							if (getIntent().hasExtra("isAC")
									&& getIntent().getStringExtra("isAC")
											.equalsIgnoreCase("true"))
							{
								landIntent.putExtra("isAC", "true");
							}
							else if (getIntent().hasExtra("isAB")
									&& getIntent().getStringExtra("isAB")
											.equalsIgnoreCase("true"))
							{
								landIntent.putExtra("isAB", "true");
							}
						}
						catch (Exception e)
						{
							e.printStackTrace();
						}

						startActivity(landIntent);
						SplashScreen.this.finish();
					}
					else if (status.equalsIgnoreCase("H"))
					{
						// mgr.SaveValueToSharedPrefs(
						// "TripStatus", "ENGAGED");
						startActivity(new Intent(SplashScreen.this,
								PassengerTripDirection.class));
						SplashScreen.this.finish();
					}
					else if (status.equalsIgnoreCase("S"))
					{
						// mgr.SaveValueToSharedPrefs(
						// "TripStatus", "START");
						startActivity(new Intent(SplashScreen.this,
								PassengerTripDirection.class));
						SplashScreen.this.finish();
					}
					else if (status.equalsIgnoreCase("E"))
					{
						mgr.SaveValueToSharedPrefs("TripStatus", "");
						startActivity(new Intent(SplashScreen.this,
								PassengerTripDirection.class));
						SplashScreen.this.finish();
					}
					else if (status.equalsIgnoreCase("C"))
					{
						// mgr.SaveValueToSharedPrefs(
						// "TripStatus", "CANCEL");
						mgr.SaveValueToSharedPrefs("routeVal", "");
						startActivity(new Intent(SplashScreen.this,
								PassengerTripDirection.class));
						SplashScreen.this.finish();
					}
					else
					{
						tvMsg.setVisibility(View.GONE);
						// signIn.setVisibility(View.VISIBLE);
						// signUp.setVisibility(View.VISIBLE);
					}

				}
				else
				{
					JugunooInteractiveDialog("NET",
							"You don't have Internet connection");
				}

			}
			else
			{
				signIn.setVisibility(View.VISIBLE);
				signUp.setVisibility(View.VISIBLE);
				tvMsg.setVisibility(View.INVISIBLE);
			}
		}
		catch (Exception bug)
		{
			bug.printStackTrace();
		}

	}

	// private void CheckUserFeedback() {
	//
	// msg.setVisibility(View.VISIBLE);
	// msg.setText("Verifying...");
	//
	// String userID = mgr.GetValueFromSharedPrefs("UserID");
	// String url = Global.JUGUNOO_WS
	// + "Passenger/GetNotFeedBackedTrip?UserId=" + userID;
	//
	// AsyncHttpClient client = new AsyncHttpClient();
	// client.setTimeout(70000);
	// client.get(url, new AsyncHttpResponseHandler() {
	//
	// @Override
	// @Deprecated
	// public void onFailure(int statusCode, Throwable error,
	// String content) {
	// msg.setVisibility(View.GONE);
	// super.onFailure(statusCode, error, content);
	// pdHandler.removeCallbacks(pdRunnable);
	//
	// }
	//
	// @Override
	// @Deprecated
	// public void onSuccess(int statusCode, String content) {
	//
	// try {
	// if (statusCode == 200) {
	//
	// try {
	//
	// JSONObject obj = new JSONObject(content);
	// String result = obj.getString("Result");
	// if (result.equalsIgnoreCase("Pass")) {
	// JSONArray ar = obj
	// .getJSONArray("FeedBackArray");
	// count = ar.length();
	// if (count != 0) {
	// feedHash = new ArrayList<HashMap<String, String>>();
	// JSONObject child = ar.getJSONObject(0);
	// String startPoint = child
	// .getString("PickPoint");
	// String endPoint = child
	// .getString("DropPoint");
	// String engID = child.getString("RID");
	// String driverId = child
	// .getString("OtherUserId");
	// String engTime = child
	// .getString("EngageTime");
	// GetDriverFeed(startPoint, endPoint, engID,
	// driverId, engTime);
	// }
	// } else {
	// msg.setVisibility(View.GONE);
	//
	// Intent intent = new Intent(SplashScreen.this,
	// LandingPage.class);
	// intent.putExtra("Address", addressStr);
	// intent.putExtra("from", "splash");
	// startActivity(intent);
	// SplashScreen.this.finish();
	// }
	//
	// } catch (Exception bug) {
	// bug.printStackTrace();
	// }
	// } else {
	// msg.setVisibility(View.GONE);
	// }
	//
	// } catch (Exception bug) {
	// bug.printStackTrace();
	// }
	//
	// super.onSuccess(statusCode, content);
	// pdHandler.removeCallbacks(pdRunnable);
	//
	// }
	// });
	// }

	@Override
	protected void onDestroy()
	{
		if (feedDialog != null)
			feedDialog.dismiss();
		super.onDestroy();
	}

	@Override
	protected void onPause()
	{
		super.onPause();

	}

	@SuppressLint("SimpleDateFormat")
	private void GetDriverFeed(String startPoint, String endPoint,
			String engID, String driverId, String engTime)
	{

		feedDialog = new Dialog(SplashScreen.this);
		feedDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		Window window = feedDialog.getWindow();
		window.setBackgroundDrawableResource(android.R.color.transparent);
		LinearLayout.LayoutParams dialogParams = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View dislogView = inflater.inflate(
				R.layout.activity_passenger_feedback_page, new LinearLayout(
						SplashScreen.this));
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
		driverNameF.setTypeface(semibold);
		startLoc.setTypeface(semibold);
		endLoc.setTypeface(semibold);
		driverNameF.setText("" + engTime_);
		startLoc.setText(startPoint);
		endLoc.setText(endPoint);

		// jugunooRating
		// .setOnRatingBarChangeListener(new OnRatingBarChangeListener() {
		//
		// @Override
		// public void onRatingChanged(RatingBar ratingBar,
		// float rating, boolean fromUser) {
		// rate = String.valueOf(rating);
		// }
		// });
		//
		// rateText.setOnEditorActionListener(new
		// EditText.OnEditorActionListener() {
		//
		// @Override
		// public boolean onEditorAction(TextView v, int actionId,
		// KeyEvent event) {
		// if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER))
		// || (actionId == EditorInfo.IME_ACTION_DONE)) {
		// String info = rateText.getText().toString();
		// validation(info);
		// }
		// return false;
		// }
		// });
		//
		// submit.setOnClickListener(new View.OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// String info = rateText.getText().toString();
		// validation(info);
		// feedDialog.show();
		// }
		// });
		// feedDialog.show();
	}

	// private void validation(String info) {
	// if (rate.equalsIgnoreCase("")) {
	// showDialog("Rate the trip!");
	// } else if (info.equalsIgnoreCase("")) {
	// showDialog("Enter your valid feedback!");
	// } else {
	// String userId = mgr.GetValueFromSharedPrefs("UserID");
	// String engageID = mgr.GetValueFromSharedPrefs("EngID");
	// String driverID = mgr.GetValueFromSharedPrefs("DriverID");
	// if (!info.equalsIgnoreCase("") && !userId.equalsIgnoreCase("")
	// && !engageID.equalsIgnoreCase("")) {
	// RequestParams params = new RequestParams();
	// params.put("EngId", engageID);
	// params.put("UserId", userId);
	// params.put("FeedBack", info);
	// params.put("Rating", rate);
	// params.put("DriverId", driverID);
	// String feedbackUrl = Global.JUGUNOO_WS + "Passenger/Feedback";
	//
	// SendFeed(feedbackUrl, params);
	//
	// } else {
	// showDialog("UserId or tripId is null!");
	// }
	//
	// }
	// }

	// private void SendFeed(String url, RequestParams params) {
	//
	// AsyncHttpClient client = new AsyncHttpClient();
	// client.setTimeout(30000);
	// client.post(url, params, new AsyncHttpResponseHandler() {
	//
	// @Override
	// @Deprecated
	// public void onFailure(int statusCode, Throwable error,
	// String content) {
	// if (statusCode == 0) {
	// showDialog("Connection timed out");
	// } else
	// showDialog("Unable to submit");
	//
	// super.onFailure(statusCode, error, content);
	// pdHandler.removeCallbacks(pdRunnable);
	//
	// }
	//
	// @Override
	// @Deprecated
	// public void onSuccess(int statusCode, String content) {
	// try {
	// if (statusCode == 200) {
	//
	// JSONObject obj = new JSONObject(content);
	// String result = obj.getString("Result");
	// if (result.equalsIgnoreCase("Pass")) {
	// showDialog("Feedback submitted successfully!");
	// feedDialog.dismiss();
	// mgr.SaveValueToSharedPrefs("TripStatus", "");
	// mgr.SaveValueToSharedPrefs("EngID", "");
	// mgr.SaveValueToSharedPrefs("DriverID", "");
	// CheckUserFeedback();
	// } else
	// showDialog("Unable to submit");
	// } else
	// showDialog("Unable to submit");
	//
	// } catch (Exception bug) {
	// bug.printStackTrace();
	// }
	// super.onSuccess(statusCode, content);
	// pdHandler.removeCallbacks(pdRunnable);
	//
	// }
	//
	// });
	//
	// }

	public void showDialog(String message)
	{

		LayoutInflater inflater = getLayoutInflater();
		View layoutToast = inflater.inflate(R.layout.jugunoo_toast_layout,
				(ViewGroup) findViewById(R.id.cabchainToast));
		((TextView) layoutToast.findViewById(R.id.textToast)).setText(message);
		final Toast myToast = new Toast(this);
		myToast.setView(layoutToast);
		myToast.setDuration(10000);
		myToast.setGravity(Gravity.BOTTOM, 0, 45);
		myToast.show();

		Handler handler = new Handler();
		handler.postDelayed(new Runnable()
		{

			@Override
			public void run()
			{
				myToast.cancel();
			}
		}, 10000);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{

		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 200)
		{
			LoadSplash();
		}
	}

}
