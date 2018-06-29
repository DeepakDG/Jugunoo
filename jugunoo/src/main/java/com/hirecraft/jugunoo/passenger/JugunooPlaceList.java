package com.hirecraft.jugunoo.passenger;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.hirecraft.jugunoo.passenger.common.Constant;
import com.hirecraft.jugunoo.passenger.common.Function;
import com.hirecraft.jugunoo.passenger.common.Global;
import com.hirecraft.jugunoo.passenger.common.TransparentProgressDialog;
import com.hirecraft.jugunoo.passenger.db.TripHistoryDB;
import com.hirecraft.jugunoo.passenger.networkhandler.NetworkHandler;
import com.hirecraft.jugunoo.passenger.services.PassengerTripDirection;
import com.hirecraft.jugunoo.passenger.services.VolleyErrorHelper;
import com.hirecraft.jugunoo.passenger.utility.JugunooUtil;
import com.hirecraft.jugunoo.passenger.utility.SharedPreferencesManager;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class JugunooPlaceList extends Activity implements OnClickListener
{

	private static final String TAG = JugunooPlaceList.class.getSimpleName();
	PlacesTask placesTask;
	GetLatLng getLatLng;

	AutoCompleteTextView dropPoint;
	Dialog interactiveDialog = null;
	Button btn_request, cancel;
	LinearLayout reqBtnLayout;

	ListView listViewPlaces, listViewRecentPlaces, listViewFavoritePlaces;
	private TransparentProgressDialog pd;
	private Handler pdHandler;
	private Runnable pdRunnable;
	private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place/autocomplete/";
	private static final String SEARCH_API = "https://maps.googleapis.com/maps/api/place/textsearch/";
	private static final String OUT_JSON = "json?";
	String browserKey = Global.BROWSER_KEY, dropStr = "";
	ArrayList<HashMap<String, String>> addresses;
	ArrayList<String> isExist;
	SharedPreferencesManager mgr;
	Typeface light, bold, semibold;
	TripHistoryDB tripDb;
	String dropLati = "", dropLongi = "";
	TextView recentPlaceLbl, favPlaceLbl;

	// View recentPlaceV, favoritePlaceV;

	String pickLL, dropLL, latLongForLandingPage;
	Intent returnIntent;
	ImageView clearAddress;
	private ProgressBar pbAuto;
	boolean isFromList;
	boolean isFavorite;
	boolean isRecentPlaceEmpty, isFavoritePlaceEmpty;
	boolean isValidAddr;
	private boolean isPickAddressFirstTime;

	private String pickPointStr, pickPointLatLng, type, formType, passengerLoc,
			dropPointStr, dLatiLongi, userId, formCat = "No";

	private String formIdentity = "";

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		ActionBar actionBar = getActionBar();
		actionBar.hide();

		setContentView(R.layout.activity_jugunoo_place_list);

		Intent intent = getIntent();

		pickPointStr = intent.getStringExtra("pickPointStr");
		pickPointLatLng = intent.getStringExtra("pLatiLongi");

		if (intent.hasExtra("passengerLoc"))
		{
			passengerLoc = intent.getStringExtra("passengerLoc");
		}

		Log.e("jp", "pickPointLatLng=" + pickPointLatLng);

		type = intent.getStringExtra("type");

		formType = intent.getStringExtra("form");

		if (formType.equalsIgnoreCase("1"))
		{
			if (intent.hasExtra("address"))
			{
				pickPointStr = intent.getStringExtra("address");
			}
			else if (intent.hasExtra("formCat"))
			{
				formCat = intent.getStringExtra("formCat");
			}
		}

		if (intent.hasExtra("formIdentity"))
		{
			formIdentity = intent.getStringExtra("formIdentity");
		}

		light = Typeface.createFromAsset(getAssets(),
				"fonts/opensans-regular-webfont.ttf");
		bold = Typeface.createFromAsset(getAssets(),
				"fonts/opensans-bold-webfont.ttf");
		semibold = Typeface.createFromAsset(getAssets(),
				"fonts/opensans-semibold-webfont.ttf");

		dropPoint = (AutoCompleteTextView) findViewById(R.id.dropPoint);

		try
		{
			if (intent.getStringExtra("formLp").equalsIgnoreCase("2"))
			{
				dropPoint.setHint("Pick Location");
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		clearAddress = (ImageView) findViewById(R.id.clrBtn);
		pbAuto = (ProgressBar) findViewById(R.id.pbAutoComp);
		clearAddress.setVisibility(View.GONE);
		btn_request = (Button) findViewById(R.id.requestBtn);
		cancel = (Button) findViewById(R.id.cancelBtn);
		btn_request.setOnClickListener(this);
		cancel.setOnClickListener(this);
		clearAddress.setOnClickListener(this);
		dropPoint.setTypeface(light);
		btn_request.setTypeface(bold);
		btn_request.setVisibility(View.GONE);
		cancel.setTypeface(bold);

		listViewPlaces = (ListView) findViewById(R.id.listViewPlaces);
		listViewRecentPlaces = (ListView) findViewById(R.id.recentPlaces);
		listViewFavoritePlaces = (ListView) findViewById(R.id.favoritePlaces);

		recentPlaceLbl = (TextView) findViewById(R.id.rmsgText);
		favPlaceLbl = (TextView) findViewById(R.id.fmsgText);

		// recentPlaceV = (View) findViewById(R.id.line);
		// favoritePlaceV = (View) findViewById(R.id.line_);

		mgr = new SharedPreferencesManager(getApplicationContext());
		pdHandler = new Handler();
		pd = new TransparentProgressDialog(JugunooPlaceList.this,
				R.drawable.loading_image);
		tripDb = new TripHistoryDB(JugunooPlaceList.this);
		placesTask = new PlacesTask();
		dropPoint.addTextChangedListener(droppointWatcher);

		GetMyRecentTrips();
		GetMyFavoriteTrips();

		openAnimation();

		if (formType.equalsIgnoreCase("1"))
		{

			isPickAddressFirstTime = true;
			btn_request.setVisibility(View.GONE);
			cancel.setVisibility(View.GONE);

			if (formCat.equalsIgnoreCase("pick"))
			{
				dropPoint.setHint("Enter Pick location");
			}
			else if (formCat.equalsIgnoreCase("drop"))
			{
				dropPoint.setHint("Drop Location");
			}
			mgr.SaveValueToSharedPrefs("Form", "1");

			dropPoint.setText(pickPointStr);

		}
		else
		{
			isPickAddressFirstTime = false;
			btn_request.setVisibility(View.VISIBLE);
			dropPoint.setHint("Drop Location");

		}

		Function.showSoftKeyBoard(JugunooPlaceList.this);

	}

	public void changeVisibility()
	{

		if (isRecentPlaceEmpty)
		{
			recentPlaceLbl.setVisibility(View.GONE);
			listViewRecentPlaces.setVisibility(View.GONE);
		}
		else
		{
			recentPlaceLbl.setVisibility(View.VISIBLE);
			listViewRecentPlaces.setVisibility(View.VISIBLE);
		}

		if (isFavoritePlaceEmpty)
		{
			favPlaceLbl.setVisibility(View.GONE);
			listViewFavoritePlaces.setVisibility(View.GONE);
		}
		else
		{
			favPlaceLbl.setVisibility(View.VISIBLE);
			listViewFavoritePlaces.setVisibility(View.VISIBLE);
		}
	}

	private TextWatcher droppointWatcher = new TextWatcher()
	{

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count)
		{

			dropStr = dropPoint.getText().toString();

			//
			// if (start % 3 == 0) {
			// if (!isFromList) {
			// listViewPlaces.setVisibility(View.VISIBLE);
			// placesTask = new PlacesTask();
			// placesTask.execute(dropStr);
			// }
			// }

			if (dropStr.length() == 0)
			{

				clearAddress.setVisibility(View.GONE);
				listViewPlaces.setVisibility(View.GONE);
				changeVisibility();

				isFromList = false;

			}
			else
			{
				/*
				 * recentPlaceLbl.setVisibility(View.GONE);
				 * recentPlaces.setVisibility(View.GONE);
				 * 
				 * favPlaceLbl.setVisibility(View.GONE);
				 * favoritePlaces.setVisibility(View.GONE);
				 */

				clearAddress.setVisibility(View.VISIBLE);
			}
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after)
		{
		}

		@Override
		public void afterTextChanged(Editable s)
		{
			btn_request.requestFocus();

			handler.removeMessages(JUGUNOO_SEARCH_LOC);
			handler.sendEmptyMessageDelayed(JUGUNOO_SEARCH_LOC,
					JUGUNOO_DELAY_IN_MILLIS);

		}
	};

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{

		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0)
		{
			// Intent landingPageIntent = new Intent(JugunooPlaceList.this,
			// LandingPage.class);
			// landingPageIntent.putExtra("from", "placeList");
			// startActivity(landingPageIntent);

			if (formIdentity.equalsIgnoreCase("LandingPage"))
			{
				onBackPressed();
			}
			else
			{
				JugunooPlaceList.this.finish();
			}

			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onBackPressed()
	{
		super.onBackPressed();

		Intent landingPageIntent = new Intent(JugunooPlaceList.this,
				LandingPage.class);
		startActivity(landingPageIntent);
	}

	private class PlacesTask extends AsyncTask<String, Void, String>
	{

		@Override
		protected void onPreExecute()
		{
			super.onPreExecute();
			pbAuto.setVisibility(View.VISIBLE);
			clearAddress.setVisibility(View.GONE);
		}

		@Override
		protected String doInBackground(String... place)
		{

			String data = "";
			try
			{

				StringBuilder sb = new StringBuilder(PLACES_API_BASE + OUT_JSON);
				sb.append("input=" + URLEncoder.encode(place[0], "utf8"));
				sb.append("&key=" + browserKey);
				sb.append("&radius=500");
				sb.append("&location=" + pickPointLatLng);

				String url = sb.toString();

				Log.e("tag", "autoc url=" + url);

				try
				{
					JugunooUtil util = new JugunooUtil(JugunooPlaceList.this);
					data = util.downloadUrl(url);
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}

			}
			catch (UnsupportedEncodingException e)
			{
				e.printStackTrace();
			}
			return data;
		}

		@Override
		protected void onPostExecute(String result)
		{
			super.onPostExecute(result);
			try
			{
				parseAndBindDataToListview(result);

				recentPlaceLbl.setVisibility(View.GONE);
				// recentPlaceV.setVisibility(View.GONE);
				listViewRecentPlaces.setVisibility(View.GONE);

				favPlaceLbl.setVisibility(View.GONE);
				// favoritePlaceV.setVisibility(View.GONE);
				listViewFavoritePlaces.setVisibility(View.GONE);

			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			finally
			{

				pbAuto.setVisibility(View.GONE);
				clearAddress.setVisibility(View.VISIBLE);

			}
		}
	}

	private void parseAndBindDataToListview(String result)
	{
		try
		{

			Log.e("places data", "place data==" + result);

			JSONObject jsonObject = new JSONObject(result);
			JSONArray array = jsonObject.getJSONArray("predictions");
			if (addresses != null)
			{
				addresses.clear();
			}
			addresses = new ArrayList<HashMap<String, String>>();
			for (int i = 0; i < array.length(); i++)
			{
				JSONObject c = array.getJSONObject(i);
				String addess = c.getString("description");

				HashMap<String, String> fetchData = new HashMap<String, String>();
				fetchData.put("description", addess);

				addresses.add(fetchData);
			}

			ListAdapter adapter = new SimpleAdapter(JugunooPlaceList.this,
					addresses, R.layout.jugunoo_addresses_row, new String[]
					{ "description", "", "" }, new int[]
					{ R.id.txtPlace, R.id.latitude, R.id.longitude, })
			{

				@Override
				public View getView(int position, View convertView,
						ViewGroup parent)
				{
					final View view = super.getView(position, convertView,
							parent);

					LinearLayout row = (LinearLayout) view
							.findViewById(R.id.addressLayout);

					// LinearLayout linearLayoutFavorite = (LinearLayout) view
					// .findViewById(R.id.linearLayoutFavorite);

					final ImageView img = (ImageView) row
							.findViewById(R.id.favTrips);
					ImageView img_ = (ImageView) row
							.findViewById(R.id.favTrips_);

					img_.setVisibility(View.GONE);
					img.setVisibility(View.VISIBLE);

					if (formType.equalsIgnoreCase("1"))
					{
						img.setVisibility(View.GONE);
					}

					// else {

					img.setOnClickListener(new OnClickListener()
					{

						@Override
						public void onClick(View v)
						{

							String address = ((TextView) view
									.findViewById(R.id.txtPlace)).getText()
									.toString();

							isValidAddr = true;
							Log.e("Favorite", "OnClk");

							// String latitude = ((TextView) view
							// .findViewById(R.id.latitude)).getText()
							// .toString();
							//
							// String longitude = ((TextView) view
							// .findViewById(R.id.longitude)).getText()
							// .toString();
							//
							// dropLati = latitude;
							// dropLongi = longitude;

							// String userId = mgr
							// .GetValueFromSharedPrefs("UserID");

							Log.e("jugu place", "drop lati=" + dropLati + ","
									+ dropLongi);

							isFromList = true;
							isFavorite = true;

							// SaveFavTrips("Favorite", pickPointStr,
							// address,
							// pickPointLatLng,
							// dropLati + "," + dropLongi, userId);

							dropPoint.setText(address);
							getLatLongFromGivenAddress(address);

							listViewPlaces.setVisibility(View.GONE);

							changeVisibility();

							InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
							imm.hideSoftInputFromWindow(
									dropPoint.getWindowToken(), 0);
						}
					});

					// }

					return view;

				}
			};
			listViewPlaces.setAdapter(adapter);

			listViewPlaces.setOnItemClickListener(new OnItemClickListener()
			{

				@Override
				public void onItemClick(AdapterView<?> parent, final View view,
						int position, long id)
				{
					showLoadingDilog();

					getLatLng = new GetLatLng();

					isValidAddr = true;

					String address = ((TextView) view
							.findViewById(R.id.txtPlace)).getText().toString();

					String latitude = ((TextView) view
							.findViewById(R.id.latitude)).getText().toString();

					String longitude = ((TextView) view
							.findViewById(R.id.longitude)).getText().toString();

					dropLati = latitude;
					dropLongi = longitude;

					Global.isCorrect = false;

					isFromList = true;

					dropPoint.setText(address);

					getLatLng.execute(address);
					getLatLongFromGivenAddress(address);

					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(
							listViewPlaces.getWindowToken(), 0);

					listViewPlaces.setVisibility(View.GONE);

					changeVisibility();

				}
			});

		}
		catch (Exception e)
		{
			e.toString();
		}
	}

	private void LetsRequest()
	{

		dropPointStr = dropPoint.getText().toString();
		userId = mgr.GetValueFromSharedPrefs("UserID");
		String deviceId = GetDeviceID();
		dLatiLongi = "";

		Log.e("jsp", dropLati + "," + dropLongi);

		if (!TextUtils.isEmpty(dropLati) && !TextUtils.isEmpty(dropLongi))
		{
			dLatiLongi = dropLati + "," + dropLongi;
		}

		if (!TextUtils.isEmpty(type) && !TextUtils.isEmpty(pickPointStr)
				&& !TextUtils.isEmpty(dropPointStr)
				&& !TextUtils.isEmpty(pickPointLatLng)
				&& !TextUtils.isEmpty(dLatiLongi) && !TextUtils.isEmpty(userId)
				&& !TextUtils.isEmpty(deviceId))
		{
			// RequestParams params = new RequestParams();
			HashMap<String, String> params = new HashMap<String, String>();

			params.put("CabType", type);
			params.put("PickPoint", pickPointStr);
			params.put("DropPoint", dropPointStr);
			params.put("Picklatlng", pickPointLatLng);
			params.put("PassengerLoc", passengerLoc);
			params.put("Droplatlng", dLatiLongi);
			params.put("UserId", userId);
			params.put("UserDeviceId", deviceId);
			params.put("Client", "Jugunoo");

			int prefIndex = mgr.getPreferenceIndex(Constant.PREFERENCE_INDEX);

			if (prefIndex == 1)
			{
				params.put("Pref", "C");
			}
			else
			{
				params.put("Pref", "P");
			}

			String url = Global.JUGUNOO_WS + "Passenger/BookingNew";

			// RequestForCab(url, params, pickPointStr,
			// dropPointStr,pickPointLatLng, dLatiLongi, userId);

			makeCabRequest(params, pickPointStr, dropPointStr, pickPointLatLng,
					dLatiLongi, userId);

		}
		else
		{
			Log.i(Global.APPTAG, "getting null when request");
		}

	}

	private void SaveTrips(String tripFor, String addressFrom,
			String addressTo, String addressFromLatLng, String addressToLatLng,
			String UserID)
	{
		try
		{
			HashMap<String, String> cab = new HashMap<String, String>();
			cab.put("TripFor", tripFor);
			cab.put("AddressFrom", addressFrom);
			cab.put("AddressTo", addressTo);
			cab.put("AddressFromLatLng", addressFromLatLng);
			cab.put("AddressToLatLng", addressToLatLng);
			cab.put("UserId", UserID);
			tripDb.insertBookingHistory(cab);
			tripDb.close();

		}
		catch (Exception bug)
		{
			bug.printStackTrace();

		}
	}

	private void SaveFavTrips(String tripFor, String addressFrom,
			String addressTo, String addressFromLatLng, String addressToLatLng,
			String UserID)
	{
		try
		{
			HashMap<String, String> cab = new HashMap<String, String>();
			cab.put("TripFor", tripFor);
			cab.put("AddressFrom", addressFrom);
			cab.put("AddressTo", addressTo);
			cab.put("AddressFromLatLng", addressFromLatLng);
			cab.put("AddressToLatLng", addressToLatLng);
			cab.put("UserId", UserID);
			tripDb.insertBookingHistory(cab);
			tripDb.close();
		}
		catch (Exception bug)
		{
			bug.printStackTrace();
		}
	}

	private void RequestForCab(String url, RequestParams params,
			final String pickPointStr, final String dropPointStr,
			final String pickPointLatLng, final String dLatiLongi,
			final String userId)
	{

		Log.e("JuPlacelist", "book now req=" + params);

		pdHandler.removeCallbacks(pdRunnable);
		if (pd.isShowing())
		{
			pd.dismiss();
		}

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

		if (!pd.isShowing())
		{
			pd.show();
		}

		pickLL = pickPointLatLng;
		dropLL = dLatiLongi;

		AsyncHttpClient requestClient = new AsyncHttpClient();
		requestClient.post(url, params, new AsyncHttpResponseHandler()
		{

			@Override
			@Deprecated
			public void onFailure(int statusCode, Throwable error,
					String content)
			{
				if (statusCode == 200)
				{
					showDialog("Connection time out");
				}
				else
				{
					showDialog("Server not responding");
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

				if (statusCode == 200)
				{
					try
					{
						JSONObject obj = new JSONObject(content);
						String result = obj.getString("Result");
						if (result.equalsIgnoreCase("Pass"))
						{
							String bookId = obj.getString("BookingId");
							mgr.SaveValueToSharedPrefs("EngID", bookId);
							// showDialog("Request in progress… Please wait.");

							mgr.SaveValueToSharedPrefs("IsNotify", "");
							mgr.SaveValueToSharedPrefs("PickPoint",
									pickPointLatLng);
							mgr.SaveValueToSharedPrefs("TripStatus", "");
							mgr.SaveValueToSharedPrefs("routeVal", "");

							mgr.SaveValueToSharedPrefs("DropPoint", dLatiLongi);

							if (isFavorite)
							{

								Log.e("fav", "inside fav");

								isFavorite = false;
								SaveFavTrips("Favorite", pickPointStr,
										dropPointStr, pickPointLatLng,
										dLatiLongi, userId);
							}
							else
							{

								Log.e("fav", "inside fav");

								SaveTrips("Recent", pickPointStr, dropPointStr,
										pickPointLatLng, dLatiLongi, userId);
							}
							Intent i = new Intent(JugunooPlaceList.this,
									PassengerTripDirection.class);
							i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
									| Intent.FLAG_ACTIVITY_NEW_TASK
									| Intent.FLAG_ACTIVITY_TASK_ON_HOME);
							i.putExtra("pickPointLatLng", pickLL);
							i.putExtra("dLatiLongi", dropLL);
							startActivity(i);
							finish();

						}
						else if (result.equalsIgnoreCase("Fail"))
						{

							// mgr.SaveValueToSharedPrefs("EngID", "");

							showDialog("Selected Taxi/Vehicle  not available at this time.");
						}
						else
						{
							showDialog("Booking Request not accepted. Try again later.");
						}

					}
					catch (JSONException e)
					{
						e.printStackTrace();
					}
				}
				else
				{
					showDialog("Booking Request not accepted. Try again later.");
				}
				super.onSuccess(statusCode, content);
				pdHandler.removeCallbacks(pdRunnable);
				if (pd.isShowing())
				{
					pd.dismiss();
				}
			}
		});
	}

	private void makeCabRequest(HashMap<String, String> params,
			final String pickPointStr, final String dropPointStr,
			final String pickPointLatLng, final String dLatiLongi,
			final String userId)
	{
		showLoadingDilog();
		pickLL = pickPointLatLng;
		dropLL = dLatiLongi;
		NetworkHandler.getPlaceListCabs(TAG, handlerCabReq, params);
	}

	private Handler handlerCabReq = new Handler()
	{

		public void handleMessage(Message msg)
		{

			switch (msg.arg1)
			{
				case Constant.MessageState.PLACE_LIST_GET_CAB_SUCCESS:
					cancelLoadingDialog();
					parseCabDetails((JSONObject) msg.obj);
					break;
				case Constant.MessageState.FAIL:
					cancelLoadingDialog();
					VolleyErrorHelper.getMessage(msg.obj,
							JugunooPlaceList.this, true);

					break;
				default:
					break;
			}

		};

	};

	private void parseCabDetails(JSONObject obj)
	{

		try
		{
			String result = obj.getString("Result");
			if (result.equalsIgnoreCase("Pass"))
			{
				String bookId = obj.getString("BookingId");
				mgr.SaveValueToSharedPrefs("EngID", bookId);
				// showDialog("Request in progress… Please wait.");

				mgr.SaveValueToSharedPrefs("IsNotify", "");
				mgr.SaveValueToSharedPrefs("PickPoint", pickPointLatLng);
				mgr.SaveValueToSharedPrefs("TripStatus", "");
				mgr.SaveValueToSharedPrefs("routeVal", "");

				mgr.SaveValueToSharedPrefs("DropPoint", dLatiLongi);

				if (isFavorite)
				{

					Log.e("fav", "inside fav");

					isFavorite = false;
					SaveFavTrips("Favorite", pickPointStr, dropPointStr,
							pickPointLatLng, dLatiLongi, userId);
				}
				else
				{

					Log.e("fav", "inside fav");

					SaveTrips("Recent", pickPointStr, dropPointStr,
							pickPointLatLng, dLatiLongi, userId);
				}
				Intent i = new Intent(JugunooPlaceList.this,
						PassengerTripDirection.class);
				i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
						| Intent.FLAG_ACTIVITY_NEW_TASK
						| Intent.FLAG_ACTIVITY_TASK_ON_HOME);
				i.putExtra("pickPointLatLng", pickLL);
				i.putExtra("dLatiLongi", dropLL);
				startActivity(i);
				finish();

			}
			else if (result.equalsIgnoreCase("Fail"))
			{

				// mgr.SaveValueToSharedPrefs("EngID", "");

				showDialog("Selected Taxi/Vehicle  not available at this time.");
			}
			else
			{
				showDialog("Booking Request not accepted. Try again later.");
			}

		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}

	}

	private String GetDeviceID()
	{
		JugunooUtil util = new JugunooUtil(JugunooPlaceList.this);
		return util.getUniqueDeviceID(getApplicationContext());
	}

	@Override
	public void finish()
	{
		if (pd != null)
		{
			if (pd.isShowing())
			{
				pd.dismiss();
			}
		}
		if (interactiveDialog != null)
		{
			interactiveDialog.dismiss();
		}
		super.finish();
	}

	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
			case R.id.requestBtn:

				Log.e("tets", "pickPointStr=" + pickPointStr + " " + dropStr
						+ " " + pickPointLatLng + " " + dropLati + ","
						+ dropLongi);

				clearNotification();

				if (pickPointStr.equalsIgnoreCase(dropStr)
						|| pickPointLatLng.equalsIgnoreCase(dropLati + ","
								+ dropLongi))
				{
					showDialog("Your Pickup and Drop locations must be different.");
				}
				else
				{
					GetRequest();
				}
				break;

			case R.id.cancelBtn:

				pdHandler.removeCallbacks(pdRunnable);

				if (pd.isShowing())
				{
					pd.dismiss();
				}

				Function.hideSoftKeyBoard(JugunooPlaceList.this);

				btn_request.setEnabled(false);

				if (formIdentity.equalsIgnoreCase("LandingPage"))
				{
					onBackPressed();
				}
				else
				{
					JugunooPlaceList.this.finish();
				}
				break;

			case R.id.clrBtn:

				Function.showSoftKeyBoard(JugunooPlaceList.this);

				dropStr = "";
				dropPoint.setText("");
				listViewPlaces.setVisibility(View.GONE);

				changeVisibility();

				break;

		}
	}

	private void GetMyRecentTrips()
	{
		try
		{

			// tripDb.deleteFavRecentBookingHistory();

			String addess = "";
			addresses = tripDb.getMyBookingHistory("Recent");
			ArrayList<HashMap<String, String>> favAddresses = tripDb
					.getMyBookingHistory("Favorite");
			addresses.addAll(favAddresses);
			tripDb.close();
			int count = 0;
			isExist = new ArrayList<String>();

			ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();

			if (addresses.size() != 0)
			{
				for (HashMap<String, String> map : addresses)
				{
					addess = map.get("AddressTo");
					if (isExist.contains(addess) && count <= 4)
					{
						Log.i(Global.APPTAG, "Address is contain in the list.");
					}
					else
					{

						String locationTo = map.get("AddressToLatLng");
						StringTokenizer token = new StringTokenizer(locationTo,
								",");
						String lon = token.nextToken();
						String lat = token.nextToken();
						HashMap<String, String> fetchData = new HashMap<String, String>();
						fetchData.put("formatted_address", addess);
						fetchData.put("lat", lat);
						fetchData.put("lng", lon);
						isExist.add(addess);
						count++;
						list.add(fetchData);

					}

					SimpleAdapter adapter = new SimpleAdapter(
							JugunooPlaceList.this, list,
							R.layout.jugunoo_addresses_row, new String[]
							{ "formatted_address", "lat", "lng" }, new int[]
							{ R.id.txtPlace, R.id.latitude, R.id.longitude, })
					{
						@Override
						public View getView(int position, View convertView,
								ViewGroup parent)
						{
							View view = super.getView(position, convertView,
									parent);

							LinearLayout row = (LinearLayout) view
									.findViewById(R.id.addressLayout);
							ImageView img = (ImageView) row
									.findViewById(R.id.favTrips);
							ImageView img_ = (ImageView) row
									.findViewById(R.id.favTrips_);

							img.setVisibility(View.GONE);
							img_.setVisibility(View.GONE);

							return view;
						}

					};
					listViewRecentPlaces.setAdapter(adapter);

					listViewRecentPlaces
							.setOnItemClickListener(new OnItemClickListener()
							{

								@Override
								public void onItemClick(AdapterView<?> parent,
										View view, int position, long id)
								{

									isFromList = true;
									isValidAddr = true;

									String address = ((TextView) view
											.findViewById(R.id.txtPlace))
											.getText().toString();

									String latitude = ((TextView) view
											.findViewById(R.id.latitude))
											.getText().toString();

									String longitude = ((TextView) view
											.findViewById(R.id.longitude))
											.getText().toString();
									dropLati = longitude;
									dropLongi = latitude;

									Global.isCorrect = true;

									if (formType.equalsIgnoreCase("1"))
									{
										Intent returnIntent = new Intent();
										returnIntent.putExtra("address",
												address);
										returnIntent.putExtra("latLong",
												dropLati + "," + dropLongi);
										setResult(RESULT_OK, returnIntent);
										finish();
									}
									else
									{

										if (latitude != null
												&& longitude != null)
										{
											if (isFromList)
											{
												btn_request
														.setVisibility(View.VISIBLE);
											}
										}
										else
										{
											// getLatLng.execute(address);
											getLatLongFromGivenAddress(address);
										}

									}

									Log.e("places", "dd=" + dropLati + ","
											+ dropLongi + " add " + address);

									// getLatLongFromGivenAddress(address);

									dropPoint.setText(address);
									InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
									imm.hideSoftInputFromWindow(
											listViewRecentPlaces
													.getWindowToken(), 0);
								}
							});

					SimpleAdapter autoAdapter = new SimpleAdapter(
							JugunooPlaceList.this, list,
							R.layout.jugunoo_addresses_row, new String[]
							{ "formatted_address", "lat", "lng" }, new int[]
							{ R.id.txtPlace, R.id.latitude, R.id.longitude, })
					{
						@Override
						public View getView(int position, View convertView,
								ViewGroup parent)
						{
							View view = super.getView(position, convertView,
									parent);

							LinearLayout row = (LinearLayout) view
									.findViewById(R.id.addressLayout);
							ImageView img = (ImageView) row
									.findViewById(R.id.favTrips);
							ImageView img_ = (ImageView) row
									.findViewById(R.id.favTrips_);
							img.setVisibility(View.GONE);
							img_.setVisibility(View.GONE);
							return view;
						}

					};

					dropPoint.setAdapter(autoAdapter);

					dropPoint.setOnItemClickListener(new OnItemClickListener()
					{
						@Override
						public void onItemClick(AdapterView<?> p, View view,
								int pos, long id)
						{
							HashMap<String, String> map = (HashMap<String, String>) p
									.getItemAtPosition(pos);
							String itemName = map.get("formatted_address");
							dropPoint.setText(itemName);

							isFromList = true;

							String address = ((TextView) view
									.findViewById(R.id.txtPlace)).getText()
									.toString();

							String latitude = ((TextView) view
									.findViewById(R.id.latitude)).getText()
									.toString();

							String longitude = ((TextView) view
									.findViewById(R.id.longitude)).getText()
									.toString();
							dropLati = longitude;
							dropLongi = latitude;

							Global.isCorrect = true;

							if (formType.equalsIgnoreCase("1"))
							{
								Intent returnIntent = new Intent();
								returnIntent.putExtra("address", address);
								returnIntent.putExtra("latLong", dropLati + ","
										+ dropLongi);
								setResult(RESULT_OK, returnIntent);
								finish();
							}
							else
							{

								if (latitude != null && longitude != null)
								{
									if (isFromList)
									{
										btn_request.setVisibility(View.VISIBLE);
									}
								}
								else
								{
									// getLatLng.execute(address);
									getLatLongFromGivenAddress(address);
								}

							}

							InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
							imm.hideSoftInputFromWindow(
									listViewRecentPlaces.getWindowToken(), 0);

						}
					});

				}
			}
			else
			{
				recentPlaceLbl.setVisibility(View.GONE);
				isRecentPlaceEmpty = true;
			}
		}
		catch (Exception bug)
		{
			bug.printStackTrace();
		}
	}

	private void GetMyFavoriteTrips()
	{
		try
		{

			String addess = "";
			addresses = tripDb.getMyBookingHistory("Favorite");
			tripDb.close();
			int count = 0;
			isExist = new ArrayList<String>();
			ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
			if (addresses.size() != 0)
			{
				for (HashMap<String, String> map : addresses)
				{
					addess = map.get("AddressTo");
					if (isExist.contains(addess) && count <= 4)
					{
						Log.i(Global.APPTAG, "Address is contain in the list.");
					}
					else
					{
						String locationTo = map.get("AddressToLatLng");
						StringTokenizer token = new StringTokenizer(locationTo,
								",");
						String lon = token.nextToken();
						String lat = token.nextToken();
						HashMap<String, String> fetchData = new HashMap<String, String>();
						fetchData.put("formatted_address", addess);
						fetchData.put("lat", lat);
						fetchData.put("lng", lon);
						isExist.add(addess);
						count++;
						list.add(fetchData);

					}
					ListAdapter adapter = new SimpleAdapter(
							JugunooPlaceList.this, list,
							R.layout.jugunoo_addresses_row, new String[]
							{ "formatted_address", "lat", "lng" }, new int[]
							{ R.id.txtPlace, R.id.latitude, R.id.longitude, })
					{

						@Override
						public View getView(int position, View convertView,
								ViewGroup parent)
						{
							View view = super.getView(position, convertView,
									parent);

							LinearLayout row = (LinearLayout) view
									.findViewById(R.id.addressLayout);
							ImageView img = (ImageView) row
									.findViewById(R.id.favTrips);
							img.setVisibility(View.GONE);
							return view;
						}

					};
					listViewFavoritePlaces.setAdapter(adapter);
					listViewFavoritePlaces
							.setOnItemClickListener(new OnItemClickListener()
							{

								@Override
								public void onItemClick(AdapterView<?> parent,
										View view, int position, long id)
								{

									isFromList = true;
									isValidAddr = true;

									String address = ((TextView) view
											.findViewById(R.id.txtPlace))
											.getText().toString();

									String latitude = ((TextView) view
											.findViewById(R.id.latitude))
											.getText().toString();

									String longitude = ((TextView) view
											.findViewById(R.id.longitude))
											.getText().toString();

									dropLati = longitude;
									dropLongi = latitude;

									dropPoint.setText(address);

									Log.e("places", "dd fav=" + dropLati + ","
											+ dropLongi);

									if (formType.equalsIgnoreCase("1"))
									{
										Intent returnIntent = new Intent();
										returnIntent.putExtra("address",
												address);
										returnIntent.putExtra("latLong",
												dropLati + "," + dropLongi);
										setResult(RESULT_OK, returnIntent);
										finish();
									}
									else
									{

										if (latitude != null
												&& longitude != null)
										{
											if (isFromList)
											{
												btn_request
														.setVisibility(View.VISIBLE);
											}
										}
										else
										{
											// getLatLng.execute(address);
											getLatLongFromGivenAddress(address);
										}

									}

									// getLatLongFromGivenAddress(address);

									InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
									imm.hideSoftInputFromWindow(
											listViewFavoritePlaces
													.getWindowToken(), 0);
								}
							});
				}

			}
			else
			{
				favPlaceLbl.setVisibility(View.GONE);
				isFavoritePlaceEmpty = true;
			}

		}
		catch (Exception bug)
		{
			bug.printStackTrace();
		}
	}

	private void GetRequest()
	{
		String dropStr = dropPoint.getText().toString();

		if (TextUtils.isEmpty(dropStr))
		{
			showDialog("Enter your Drop location.");
		}
		else if (!isValidAddr)
		{
			showDialog("Enter valid Drop location.");
		}
		else
		{
			LetsRequest();
		}
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
		if (isExist.size() != 0)
		{
			isExist.clear();
		}
		closeAnimation();
	}

	public void showDialog(String message)
	{
		Function.showToast(JugunooPlaceList.this, message);
	}

	public class GetLatLng extends AsyncTask<String, Void, String>
	{
		@Override
		protected String doInBackground(String... params)
		{

			String data = "";
			try
			{
				StringBuilder sb = new StringBuilder(SEARCH_API + OUT_JSON);
				sb.append("query=" + URLEncoder.encode(params[0], "utf8"));
				sb.append("&key=" + browserKey);
				sb.append("&radius=50000");
				sb.append("&location=" + pickPointLatLng);
				String url = sb.toString();
				JugunooUtil util = new JugunooUtil(JugunooPlaceList.this);
				data = util.downloadUrl(url);

			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

			Geocoder coder = new Geocoder(JugunooPlaceList.this);
			try
			{
				ArrayList<Address> adresses = (ArrayList<Address>) coder
						.getFromLocationName(params[0], 10);
				for (Address add : adresses)
				{
					double longitude = add.getLongitude();
					double latitude = add.getLatitude();

					Log.e("add", "address=" + adresses + " " + latitude + ","
							+ longitude);

				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			catch (IllegalArgumentException e)
			{
				e.printStackTrace();
			}

			return data;
		}

		@Override
		protected void onPostExecute(String result)
		{
			super.onPostExecute(result);

			cancelLoadingDialog();
			String pickUpLat, pickUpLong;

			try
			{
				JSONObject jObj = new JSONObject(result);
				JSONArray jArray = jObj.getJSONArray("results");
				for (int i = 0; i < 1; i++)
				{
					if (formType.equalsIgnoreCase("1"))
					{
						pickUpLat = jArray.getJSONObject(i)
								.getJSONObject("geometry")
								.getJSONObject("location").getString("lat");
						pickUpLong = jArray.getJSONObject(i)
								.getJSONObject("geometry")
								.getJSONObject("location").getString("lng");
						latLongForLandingPage = pickUpLat + "," + pickUpLong;
					}
					else
					{
						dropLati = jArray.getJSONObject(i)
								.getJSONObject("geometry")
								.getJSONObject("location").getString("lat");
						dropLongi = jArray.getJSONObject(i)
								.getJSONObject("geometry")
								.getJSONObject("location").getString("lng");

						btn_request.setVisibility(View.VISIBLE);

					}

				}

				if (formType.equalsIgnoreCase("1"))
				{
					Intent returnIntent = new Intent();
					returnIntent.putExtra("address", dropStr);
					returnIntent.putExtra("latLong", latLongForLandingPage);
					setResult(RESULT_OK, returnIntent);
					finish();
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	private final int JUGUNOO_SEARCH_LOC = 12;
	private final int JUGUNOO_DELAY_IN_MILLIS = 1000;

	Handler handler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{

			if (msg.what == JUGUNOO_SEARCH_LOC)
			{

				if (JugunooUtil.isConnectedToInternet(JugunooPlaceList.this))
				{

					if (!isPickAddressFirstTime)
					{

						dropStr = dropPoint.getText().toString();

						if (dropStr.trim().length() > 3)
						{
							if (!isFromList)
							{

								isFromList = false;

								if (!dropPoint.isPopupShowing())
								{
									listViewPlaces.setVisibility(View.VISIBLE);
									placesTask = new PlacesTask();
									placesTask.execute(dropStr);
								}

							}
						}

					}
					else
					{
						isPickAddressFirstTime = false;
					}

				}
				else
				{
					VolleyErrorHelper.getMessage(msg.obj,
							JugunooPlaceList.this, true);
				}

			}

		};
	};

	public void getLatLongFromGivenAddress(String address)
	{
		double lat = 0.0, lng = 0.0;

		Geocoder geoCoder = new Geocoder(this, Locale.getDefault());
		try
		{
			List<Address> addresses = geoCoder.getFromLocationName(address, 1);

			while (addresses.size() == 0)
			{
				addresses = geoCoder.getFromLocationName(address, 1);
			}

			if (addresses.size() > 0)
			{
				// GeoPoint p = new GeoPoint(
				// (int) (addresses.get(0).getLatitude() * 1E6),
				// (int) (addresses.get(0).getLongitude() * 1E6));

				LatLng latLng = new LatLng((addresses.get(0).getLatitude()),
						(addresses.get(0).getLongitude()));

				lat = latLng.latitude;
				lng = latLng.longitude;

				Log.e("Latitude", "latt" + lat);
				Log.e("Longitude", "lngg" + lng);

				if (formType.equalsIgnoreCase("1"))
				{

					latLongForLandingPage = lat + "," + lng;

					dropLati = lat + "";
					dropLongi = lng + "";

				}
				else
				{
					dropLati = lat + "";
					dropLongi = lng + "";

					btn_request.setVisibility(View.VISIBLE);

				}

				if (formType.equalsIgnoreCase("1"))
				{
					Intent returnIntent = new Intent();
					returnIntent.putExtra("address", dropStr);
					returnIntent.putExtra("latLong", latLongForLandingPage);
					setResult(RESULT_OK, returnIntent);
					finish();
				}

			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void clearNotification()
	{
		NotificationManager notificationManager = (NotificationManager) getApplicationContext()
				.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancelAll();

	}

	private void showLoadingDilog()
	{
		// Showing loading dialog

		pdHandler = new Handler();
		pd = new TransparentProgressDialog(JugunooPlaceList.this,
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

	@Override
	protected void onStart()
	{
		super.onStart();
	}

	@Override
	protected void onStop()
	{
		super.onStop();
		Function.hideSoftKeyBoard(JugunooPlaceList.this);
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		Function.hideSoftKeyBoard(JugunooPlaceList.this);
	}

}
