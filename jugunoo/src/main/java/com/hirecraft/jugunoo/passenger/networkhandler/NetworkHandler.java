package com.hirecraft.jugunoo.passenger.networkhandler;

import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.location.Location;
import android.os.Handler;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.maps.model.LatLng;
import com.hirecraft.jugunoo.passenger.common.Constant;
import com.hirecraft.jugunoo.passenger.common.Global;

public class NetworkHandler
{
	private final static int timeOut = 5 * 1000;

	private static final String TAG = NetworkHandler.class.getSimpleName();

	// login
	public static void loginRequest(String tag, Handler handler,
			final Map<String, String> params)
	{
		String URL = Global.JUGUNOO_WS + "Passenger/Login";
		Log.i(TAG, "url --> " + URL);

		JsonObjectRequest jsObjRequest = new JsonObjectRequest(
				Request.Method.POST, URL, new JSONObject(params),
				ResponseListener.<JSONObject> createGenericReqSuccessListener(
						handler, Constant.MessageState.LOGIN_SUCCESS),
				ResponseListener.createErrorListener(handler,
						Constant.MessageState.FAIL));

		jsObjRequest.setShouldCache(false);
		jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(timeOut, 3, 1.0f));
		Global.getInstance().addToRequestQueue(jsObjRequest, tag);
	}

	// device Registration
	public static void deviceRegistration(String tag, Handler handler,
			final Map<String, String> params)
	{

		String URL = Global.JUGUNOO_WS + "Passenger/DeviceRegistration";
		Log.i(TAG, "url --> " + URL);

		JsonObjectRequest jsObjRequest = new JsonObjectRequest(
				Request.Method.POST, URL, new JSONObject(params),
				ResponseListener.<JSONObject> createGenericReqSuccessListener(
						handler,
						Constant.MessageState.DEVICE_REGISTRATION_SUCCESS),
				ResponseListener.createErrorListener(handler,
						Constant.MessageState.FAIL));

		jsObjRequest.setShouldCache(false);
		jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(timeOut, 3, 1.0f));
		Global.getInstance().addToRequestQueue(jsObjRequest, tag);

	}

	// Passenger registration
	public static void registrationRequest(String tag, Handler handler,
			final Map<String, String> params)
	{
		String URL = Global.JUGUNOO_WS + "Passenger/Registration?";
		Log.i(TAG, "url --> " + URL);

		JsonObjectRequest jsObjRequest = new JsonObjectRequest(
				Request.Method.POST, URL, new JSONObject(params),
				ResponseListener.<JSONObject> createGenericReqSuccessListener(
						handler, Constant.MessageState.REGISTRATION_SUCCESS),
				ResponseListener.createErrorListener(handler,
						Constant.MessageState.FAIL));

		jsObjRequest.setShouldCache(false);
		jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(timeOut, 3, 1.0f));
		Global.getInstance().addToRequestQueue(jsObjRequest, tag);

	}

	// verify number
	public static void verifyNumberRequest(String tag, Handler handler,
			final Map<String, String> params)
	{
		String URL = Global.JUGUNOO_WS + "Passenger/OTPUpdate";
		Log.i(TAG, "url --> " + URL);

		JsonObjectRequest jsObjRequest = new JsonObjectRequest(
				Request.Method.POST, URL, new JSONObject(params),
				ResponseListener.<JSONObject> createGenericReqSuccessListener(
						handler, Constant.MessageState.VERIFY_NUMBER_SUCCESS),
				ResponseListener.createErrorListener(handler,
						Constant.MessageState.FAIL));

		jsObjRequest.setShouldCache(false);
		jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(timeOut, 3, 1.0f));
		Global.getInstance().addToRequestQueue(jsObjRequest, tag);

	}

	// verify OTP
	public static void verifyOtpRequest(String tag, Handler handler,
			final Map<String, String> params)
	{
		String URL = Global.JUGUNOO_WS + "Passenger/OTPReset";
		Log.i(TAG, "url --> " + URL);

		JsonObjectRequest jsObjRequest = new JsonObjectRequest(
				Request.Method.POST, URL, new JSONObject(params),
				ResponseListener.<JSONObject> createGenericReqSuccessListener(
						handler, Constant.MessageState.OTP_SUCCESS),
				ResponseListener.createErrorListener(handler,
						Constant.MessageState.FAIL));

		jsObjRequest.setShouldCache(false);
		jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(timeOut, 3, 1.0f));
		Global.getInstance().addToRequestQueue(jsObjRequest, tag);

	}

	// verify mail & number available or not
	public static void mailAvailabilityRequest(String tag, Handler handler,
			final Map<String, String> params, String url)
	{
		String URL = url;
		Log.i(TAG, "url --> " + URL);

		JsonObjectRequest jsObjRequest = new JsonObjectRequest(
				Request.Method.GET,
				URL,
				new JSONObject(params),
				ResponseListener.<JSONObject> createGenericReqSuccessListener(
						handler,
						Constant.MessageState.AVAILABILITY_MAIL_NUMBER_SUCCESS),
				ResponseListener.createErrorListener(handler,
						Constant.MessageState.FAIL));

		jsObjRequest.setShouldCache(false);
		jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(timeOut, 3, 1.0f));
		Global.getInstance().addToRequestQueue(jsObjRequest, tag);

	}

	// registration-Company names
	public static void CompanyNamesRequest(String tag, Handler handler,
			String URL, int SUCCESS_MESSAGE)
	{

		Log.d(TAG, "url --> " + URL);

		// String URL = Global.JUGUNOO_WS + "Passenger/GetCompanyName";

		// JsonObjectRequest jsObjRequest = new JsonObjectRequest(URL, null,
		// ResponseListener.<JSONObject> createGenericReqSuccessListener(
		// handler, Constant.MessageState.COMPANY_NAMES_SUCCESS),
		// ResponseListener.createErrorListener(handler,
		// Constant.MessageState.FAIL));
		// Log.i(TAG, "url :" + URL);
		// jsObjRequest.setShouldCache(false);
		// jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(timeOut, 3,
		// 1.0f));
		// Global.getInstance().addToRequestQueue(jsObjRequest, tag);

		// int method, String url, JSONObject jsonRequest, Listener<JSONObject>
		// listener, ErrorListener errorListener

		JsonObjectRequest jsObjRequest = new JsonObjectRequest(
				Request.Method.GET, URL, null,
				ResponseListener.<JSONObject> createGenericReqSuccessListener(
						handler, SUCCESS_MESSAGE),
				ResponseListener.createErrorListener(handler,
						Constant.MessageState.FAIL));

		jsObjRequest.setShouldCache(false);
		jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(timeOut, 3, 1.0f));
		Global.getInstance().addToRequestQueue(jsObjRequest, tag);

	}

	// forgot Password
	public static void forgotPasswordRequest(String tag, Handler handler,
			final Map<String, String> params)
	{
		String URL = Global.JUGUNOO_WS + "Passenger/ForgetPassword";
		Log.i(TAG, "url --> " + URL);

		JsonObjectRequest jsObjRequest = new JsonObjectRequest(
				Request.Method.POST,
				URL,
				new JSONObject(params),
				ResponseListener.<JSONObject> createGenericReqSuccessListener(
						handler, Constant.MessageState.FORGOT_PASSWORD_SUCCESS),
				ResponseListener.createErrorListener(handler,
						Constant.MessageState.FAIL));

		jsObjRequest.setShouldCache(false);
		jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(timeOut, 3, 1.0f));
		Global.getInstance().addToRequestQueue(jsObjRequest, tag);
	}

	public static void checkUserRequest(String tag, Handler handler,
			final Map<String, String> params, String url)
	{
		String URL = url;
		Log.i(TAG, "url --> " + URL);

		JsonObjectRequest jsObjRequest = new JsonObjectRequest(
				Request.Method.GET, URL, new JSONObject(params),
				ResponseListener.<JSONObject> createGenericReqSuccessListener(
						handler, Constant.MessageState.CHECK_USER_SUCCESS),
				ResponseListener.createErrorListener(handler,
						Constant.MessageState.FAIL));

		jsObjRequest.setShouldCache(false);
		jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(timeOut, 3, 1.0f));
		Global.getInstance().addToRequestQueue(jsObjRequest, tag);

	}

	// get all cabs nearby
	public static void getCabsRequest(String tag, Handler handler, String lat,
			String lng, String cabType, int pref, String userId)
	{
		String url = "";

		// if (TextUtils.isEmpty(cabType) || cabType.equalsIgnoreCase("all")) {
		// url = Global.JUGUNOO_WS_LOCATION + "location?latitude=" + lat
		// + "&longitude=" + lng + "&Client=Jugunoo";
		// } else {
		// url = Global.JUGUNOO_WS_LOCATION + "Location/SearchbyCab?latitude="
		// + lat + "&longitude=" + lng + "&Client=Jugunoo&CabType="
		// + cabType;
		// }

		/**
		 * personal = 0 corporate = 1
		 */
		if (pref == 1)
		{

			url = Global.JUGUNOO_WS_LOCATION
					+ "Location/SearchCorpSpot?UserId=" + userId
					+ "&CabType=All&latitude=" + lat + "&longitude=" + lng
					+ "&Client=Jugunoo";

		}
		else
		{

			url = Global.JUGUNOO_WS_LOCATION + "location?latitude=" + lat
					+ "&longitude=" + lng + "&Client=Jugunoo";

		}
		// Log.i(TAG, "url --> " + url);

		JsonArrayRequest jsonArrRequest = new JsonArrayRequest(url,
				ResponseListener.<JSONArray> createGenericReqSuccessListener(
						handler, Constant.MessageState.CAB_SUCCESS),
				ResponseListener.createErrorListener(handler,
						Constant.MessageState.CAB_FAIL));

		jsonArrRequest.setShouldCache(false);
		jsonArrRequest.setRetryPolicy(new DefaultRetryPolicy(timeOut, 3, 1.0f));
		Global.getInstance().addToRequestQueue(jsonArrRequest, tag);

	}

	public static void getPlaceListCabs(String tag, Handler handler,
			final Map<String, String> params)
	{
		String URL = Global.JUGUNOO_WS + "Passenger/BookingNew";
		Log.i(TAG, "url --> " + URL);

		JsonObjectRequest jsObjRequest = new JsonObjectRequest(
				Request.Method.POST, URL, new JSONObject(params),
				ResponseListener.<JSONObject> createGenericReqSuccessListener(
						handler,
						Constant.MessageState.PLACE_LIST_GET_CAB_SUCCESS),
				ResponseListener.createErrorListener(handler,
						Constant.MessageState.FAIL));

		jsObjRequest.setShouldCache(false);
		jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(timeOut, 3, 1.0f));
		Global.getInstance().addToRequestQueue(jsObjRequest, tag);

	}

	// logout
	public static void logoutRequest(String tag, Handler handler,
			final Map<String, String> params)
	{
		String url = Global.JUGUNOO_WS + "Passenger/PassengerLogout";
		Log.i(TAG, "url --> " + url);

		JsonObjectRequest jsObjRequest = new JsonObjectRequest(
				Request.Method.POST, url, new JSONObject(params),
				ResponseListener.<JSONObject> createGenericReqSuccessListener(
						handler, Constant.MessageState.LOGOUT_SUCCESS),
				ResponseListener.createErrorListener(handler,
						Constant.MessageState.LOGOUT_FAIL));

		jsObjRequest.setShouldCache(false);
		jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(timeOut, 3, 1.0f));
		Global.getInstance().addToRequestQueue(jsObjRequest, tag);

	}

	// cancel trip
	public static void cancelTripRequest(String tag, Handler handler,
			final Map<String, String> params)
	{

		String url = Global.JUGUNOO_WS + "Passenger/PassengerCancel";
		Log.i(TAG, "url --> " + url);

		JsonObjectRequest jsObjRequest = new JsonObjectRequest(
				Request.Method.POST, url, new JSONObject(params),
				ResponseListener.<JSONObject> createGenericReqSuccessListener(
						handler, Constant.MessageState.TRIP_CANCEL_SUCCESS),
				ResponseListener.createErrorListener(handler,
						Constant.MessageState.TRIP_CANCEL_FAIL));

		jsObjRequest.setShouldCache(false);
		jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(timeOut, 3, 1.0f));
		Global.getInstance().addToRequestQueue(jsObjRequest, tag);

	}

	// direction fetcher
	public static void tripDirectionRequest(String tag, Handler handler,
			String status, String engId, String driverId, String userId,
			Location myPostion)
	{
		String url = "";

		if (status.equalsIgnoreCase("START"))
		{
			url = Global.JUGUNOO_WS_LOCATION
					+ "Location/TripPathDriver?DriverId=" + driverId
					+ "&EngId=" + engId;

		}
		else if (status.equalsIgnoreCase("ENGAGED"))
		{

			if (myPostion != null)
			{

				url = Global.JUGUNOO_WS_LOCATION
						+ "Location/TripPathPassenger?UserId=" + userId
						+ "&EngId=" + engId + "&lat=" + myPostion.getLatitude()
						+ "&lng=" + myPostion.getLongitude();

			}
			else
			{

				url = Global.JUGUNOO_WS_LOCATION
						+ "Location/TripPathPassenger?UserId=" + userId
						+ "&EngId=" + engId;
			}
		}
		else
		{
			if (myPostion != null)
			{

				url = Global.JUGUNOO_WS_LOCATION
						+ "Location/TripPathPassenger?UserId=" + userId
						+ "&EngId=" + engId + "&lat=" + myPostion.getLatitude()
						+ "&lng=" + myPostion.getLongitude();

			}
			else
			{

				url = Global.JUGUNOO_WS_LOCATION
						+ "Location/TripPathPassenger?UserId=" + userId
						+ "&EngId=" + engId;
			}
		}
		Log.i(TAG, "url --> " + url);

		JsonObjectRequest jsObjRequest = new JsonObjectRequest(
				Request.Method.GET, url, null,
				ResponseListener.<JSONObject> createGenericReqSuccessListener(
						handler, Constant.MessageState.TRIP_DIRECTION_SUCCESS),
				ResponseListener.createErrorListener(handler,
						Constant.MessageState.TRIP_DIRECTION_FAIL));

		jsObjRequest.setShouldCache(false);
		jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(timeOut, 5, 1.0f));
		Global.getInstance().addToRequestQueue(jsObjRequest, tag);

	}

	// get driver photo
	public static void driverPhotoRequest(String tag, Handler handler,
			String cabId)
	{

		String url = Global.JUGUNOO_WS + "Passenger/DriverInfo?CabId=" + cabId;
		Log.i(TAG, "url --> " + url);

		JsonObjectRequest jsObjRequest = new JsonObjectRequest(
				Request.Method.GET, url, null,
				ResponseListener.<JSONObject> createGenericReqSuccessListener(
						handler, Constant.MessageState.DRIVER_PHOTO_SUCCESS),
				ResponseListener.createErrorListener(handler,
						Constant.MessageState.DRIVER_PHOTO_FAIL));

		jsObjRequest.setShouldCache(false);
		jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(timeOut, 5, 1.0f));
		Global.getInstance().addToRequestQueue(jsObjRequest, tag);

	}

	// map fetch route between pick and drop
	// get driver photo
	public static void routeRequest(String tag, Handler handler,
			LatLng pickPoint, LatLng dropPoint)
	{
		// String waypoints = "waypoints=optimize:true|" + pickPoint.latitude
		// + "," + pickPoint.longitude + "|" + "|" + dropPoint.latitude
		// + "," + dropPoint.longitude;

		String waypoints = "origin=" + pickPoint.latitude + ","
				+ pickPoint.longitude + "&destination=" + dropPoint.latitude
				+ "," + dropPoint.longitude;

		String sensor = "sensor=false";
		String params = waypoints + "&" + sensor;
		String output = "json";
		String url = "https://maps.googleapis.com/maps/api/directions/"
				+ output + "?" + params;

		Log.i(TAG, "url --> " + url);

		StringRequest stringRequest = new StringRequest(Request.Method.GET,
				url, ResponseListener.<String> createGenericReqSuccessListener(
						handler, Constant.MessageState.MAP_PLOT_SUCCESS),
				ResponseListener.createErrorListener(handler,
						Constant.MessageState.MAP_PLOT_FAIL));
		stringRequest.setShouldCache(false);
		stringRequest.setRetryPolicy(new DefaultRetryPolicy(timeOut, 5, 1.0f));
		Global.getInstance().addToRequestQueue(stringRequest, tag);
	}

	// get booking init
	public static void getBookingInit(String tag, Handler handler,
			String userId, int prefIndex)
	{
		/**
		 * personal = 0 corporate = 1
		 */
		String url = "";
		if (prefIndex == 1)
		{
			url = Global.JUGUNOO_WS
					+ "Passenger/GetBookingLoadDetails?Pref=C&UserId=" + userId;
		}
		else
		{
			url = Global.JUGUNOO_WS
					+ "Passenger/GetBookingLoadDetails?Pref=P&UserId=" + userId;
		}
		Log.i(TAG, "url --> " + url);

		JsonObjectRequest jsObjRequest = new JsonObjectRequest(
				Request.Method.GET, url, null,
				ResponseListener.<JSONObject> createGenericReqSuccessListener(
						handler, Constant.MessageState.BOOKING_LOAD_SUCCESS),
				ResponseListener.createErrorListener(handler,
						Constant.MessageState.BOOKING_LOAD_FAIL));

		jsObjRequest.setShouldCache(false);
		jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(timeOut, 5, 1.0f));
		Global.getInstance().addToRequestQueue(jsObjRequest, tag);

	}

	// Fleet Booking
	public static void fleetBookingRequest(String tag, Handler handler,
			final Map<String, String> params)
	{
		String url = Global.JUGUNOO_WS + "Passenger/FleetBook";
		Log.i("url", "url -->" + url);

		JsonObjectRequest jsObjRequest = new JsonObjectRequest(
				Request.Method.POST, url, new JSONObject(params),
				ResponseListener.<JSONObject> createGenericReqSuccessListener(
						handler, Constant.MessageState.FLEET_BOOK_SUCCESS),
				ResponseListener.createErrorListener(handler,
						Constant.MessageState.FLEET_BOOK_FAIL));

		jsObjRequest.setShouldCache(false);
		jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(timeOut, 3, 1.0f));
		Global.getInstance().addToRequestQueue(jsObjRequest, tag);
	}

	// get cab type
	public static void getCabTypeRequest(String tag, Handler handler,
			final Map<String, String> params)
	{
		String url = Global.JUGUNOO_WS + "Passenger/GetCabTypeByDate";
		Log.i(TAG, "url --> " + url);

		JsonObjectRequest jsObjRequest = new JsonObjectRequest(
				Request.Method.POST, url, new JSONObject(params),
				ResponseListener.<JSONObject> createGenericReqSuccessListener(
						handler, Constant.MessageState.CAB_TYPE_SUCCESS),
				ResponseListener.createErrorListener(handler,
						Constant.MessageState.CAB_TYPE_FAIL));

		jsObjRequest.setShouldCache(false);
		jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(timeOut, 3, 1.0f));
		Global.getInstance().addToRequestQueue(jsObjRequest, tag);

	}

	// get cab type
	public static void getCabTypeUpdateRequest(String tag, Handler handler,
			final Map<String, String> params)
	{
		String url = Global.JUGUNOO_WS + "Passenger/GetCabTypeUpdate";
		Log.i(TAG, "url --> " + url);

		JsonObjectRequest jsObjRequest = new JsonObjectRequest(
				Request.Method.POST,
				url,
				new JSONObject(params),
				ResponseListener.<JSONObject> createGenericReqSuccessListener(
						handler, Constant.MessageState.CAB_TYPE_UPDATE_SUCCESS),
				ResponseListener.createErrorListener(handler,
						Constant.MessageState.CAB_TYPE_UPDATE_FAIL));

		jsObjRequest.setShouldCache(false);
		jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(timeOut, 3, 1.0f));
		Global.getInstance().addToRequestQueue(jsObjRequest, tag);

	}

	public static void GetTripLog(String tag, Handler handler,
			final Map<String, String> params, Context ctx, String userId,
			int currentPage, String status)
	{
		String url = Global.JUGUNOO_WS + "Passenger/GetFleetLog?UserId="
				+ userId + "&page=" + currentPage + "&Status=" + status;
		Log.i(TAG, "url --> " + url);

		JsonObjectRequest jsObjRequest = new JsonObjectRequest(
				Request.Method.GET, url, new JSONObject(params),
				ResponseListener.<JSONObject> createGenericReqSuccessListener(
						handler, Constant.MessageState.TRIP_LOGS_AVAILABLE),
				ResponseListener.createErrorListener(handler,
						Constant.MessageState.TRIP_LOGS_UNAVAILABLE));

		jsObjRequest.setShouldCache(false);
		jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(timeOut, 3, 1.0f));
		Global.getInstance().addToRequestQueue(jsObjRequest, tag);
	}

	public static void updateCancelTripLog(String tag, Handler handler,
			final Map<String, String> params)
	{
		String url = Global.JUGUNOO_WS + "FleetUpdate";
		Log.i(TAG, "url --> " + url);

		JsonObjectRequest jsObjRequest = new JsonObjectRequest(
				Request.Method.POST, url, new JSONObject(params),
				ResponseListener.<JSONObject> createGenericReqSuccessListener(
						handler, Constant.MessageState.TRIPLOG_CANCEL_SUCCESS),
				ResponseListener.createErrorListener(handler,
						Constant.MessageState.TRIPLOG_CANCEL_FAILED));

		jsObjRequest.setShouldCache(false);
		jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(timeOut, 3, 1.0f));
		Global.getInstance().addToRequestQueue(jsObjRequest, tag);
	}

	public static void SaveFleetOperator(String tag, Handler handler,
			final Map<String, String> params)
	{
		String url = Global.JUGUNOO_WS + "Driver/CreateUserFleet";
		Log.i(TAG, "url --> " + url);

		JsonObjectRequest jsObjRequest = new JsonObjectRequest(
				Request.Method.POST, url, new JSONObject(params),
				ResponseListener.<JSONObject> createGenericReqSuccessListener(
						handler, Constant.MessageState.UPDATE_FLEET_SUCCESS),
				ResponseListener.createErrorListener(handler,
						Constant.MessageState.UPDATE_FLEET_FAIL));

		jsObjRequest.setShouldCache(false);
		jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(timeOut, 3, 1.0f));
		Global.getInstance().addToRequestQueue(jsObjRequest, tag);

	}

	public static void SaveGroup(String tag, Handler handler,
			final Map<String, String> params)
	{
		String url = Global.JUGUNOO_WS + "Passenger/CreateGroup";
		Log.i(TAG, "url --> " + url);

		JsonObjectRequest jsObjRequest = new JsonObjectRequest(
				Request.Method.POST,
				url,
				new JSONObject(params),
				ResponseListener.<JSONObject> createGenericReqSuccessListener(
						handler, Constant.MessageState.ADD_FLEET_GROUP_SUCCESS),
				ResponseListener.createErrorListener(handler,
						Constant.MessageState.ADD_FLEET_GROUP_FAIL));

		jsObjRequest.setShouldCache(false);
		jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(timeOut, 3, 1.0f));
		Global.getInstance().addToRequestQueue(jsObjRequest, tag);

	}

	public static void SaveUser(String tag, Handler handler,
			final Map<String, String> params)
	{
		String url = Global.JUGUNOO_WS + "Passenger/CreateGroupUser";
		Log.i(TAG, "url --> " + url);

		JsonObjectRequest jsObjRequest = new JsonObjectRequest(
				Request.Method.POST, url, new JSONObject(params),
				ResponseListener.<JSONObject> createGenericReqSuccessListener(
						handler, Constant.MessageState.ADD_FLEET_USER_SUCCESS),
				ResponseListener.createErrorListener(handler,
						Constant.MessageState.ADD_FLEET_USER_FAIL));

		jsObjRequest.setShouldCache(false);
		jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(timeOut, 3, 1.0f));
		Global.getInstance().addToRequestQueue(jsObjRequest, tag);

	}

	public static void GetProfile(String tag, Handler handler, String UserId)
	{
		String url = Global.JUGUNOO_WS + "Passenger/GetUserProfile?UserId="
				+ UserId;
		Log.i(TAG, "url --> " + url);

		JsonObjectRequest jsObjRequest = new JsonObjectRequest(url, null,
				ResponseListener.<JSONObject> createGenericReqSuccessListener(
						handler, Constant.MessageState.USER_PROFILE_SUCCESS),
				ResponseListener.createErrorListener(handler,
						Constant.MessageState.USER_PROFILE_FAIL));
		jsObjRequest.setShouldCache(false);
		jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(timeOut, 3, 1.0f));
		Global.getInstance().addToRequestQueue(jsObjRequest, tag);

	}

	public static void ChangePwd(String tag, Handler handler,
			final Map<String, String> params)
	{
		String url = Global.JUGUNOO_WS + "Passenger/ChangePassword";
		Log.i(TAG, "url --> " + url);

		JsonObjectRequest jsObjRequest = new JsonObjectRequest(
				Request.Method.POST, url, new JSONObject(params),
				ResponseListener.<JSONObject> createGenericReqSuccessListener(
						handler, Constant.MessageState.CHANGE_PWD_SUCCESS),
				ResponseListener.createErrorListener(handler,
						Constant.MessageState.CHANGE_PWD_FAIL));
		jsObjRequest.setShouldCache(false);
		jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(timeOut, 3, 1.0f));
		Global.getInstance().addToRequestQueue(jsObjRequest, tag);
	}

	public static void GetUserFleet(String tag, Handler handler, String UserId,
			String Status, String UserType)
	{
		String url = Global.JUGUNOO_WS + "Passenger/GetUserFleet?UserId="
				+ UserId + "&Status=" + Status;
		Log.i(TAG, "url --> " + url);

		JsonObjectRequest jsObjRequest = new JsonObjectRequest(url, null,
				ResponseListener.<JSONObject> createGenericReqSuccessListener(
						handler, Constant.MessageState.FLEET_OPERATOR_SUCCESS),
				ResponseListener.createErrorListener(handler,
						Constant.MessageState.FLEET_OPERATOR_FAIL));
		jsObjRequest.setShouldCache(false);
		jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(timeOut, 3, 1.0f));
		Global.getInstance().addToRequestQueue(jsObjRequest, tag);

	}

	public static void GetGroups(String tag, Handler handler, String UserId,
			String Status)
	{
		String url = Global.JUGUNOO_WS + "Passenger/GetGroups?UserId=" + UserId
				+ "&Status=" + Status;
		Log.i(TAG, "url --> " + url);

		JsonObjectRequest jsObjRequest = new JsonObjectRequest(url, null,
				ResponseListener.<JSONObject> createGenericReqSuccessListener(
						handler, Constant.MessageState.FLEET_GROUP_SUCCESS),
				ResponseListener.createErrorListener(handler,
						Constant.MessageState.FLEET_GROUP_FAIL));
		jsObjRequest.setShouldCache(false);
		jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(timeOut, 3, 1.0f));
		Global.getInstance().addToRequestQueue(jsObjRequest, tag);

	}

	public static void GetUsers(String tag, Handler handler, String UserId,
			String GroupId, String Status)
	{
		String url = Global.JUGUNOO_WS + "Passenger/GetUsers?UserId=" + UserId
				+ "&GroupId=" + GroupId + "&Status=" + Status;
		Log.d(TAG, "url --> " + url);

		JsonObjectRequest jsObjRequest = new JsonObjectRequest(url, null,
				ResponseListener.<JSONObject> createGenericReqSuccessListener(
						handler, Constant.MessageState.FLEET_USERS_SUCCESS),
				ResponseListener.createErrorListener(handler,
						Constant.MessageState.FLEET_USERS_FAIL));
		jsObjRequest.setShouldCache(false);
		jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(timeOut, 3, 1.0f));
		Global.getInstance().addToRequestQueue(jsObjRequest, tag);
	}

	public static void GetUserNames(String tag, Handler handler, String url)
	{
		Log.d(TAG, "url --> " + url);

		JsonObjectRequest jsObjRequest = new JsonObjectRequest(url, null,
				ResponseListener.<JSONObject> createGenericReqSuccessListener(
						handler, Constant.MessageState.USERNAMES_SUCCESS),
				ResponseListener.createErrorListener(handler,
						Constant.MessageState.USERNAMES_FAIL));
		jsObjRequest.setShouldCache(false);
		jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(timeOut, 3, 1.0f));
		Global.getInstance().addToRequestQueue(jsObjRequest, tag);
	}

	public static void cancelTripLog(String tag, Handler handler,
			final Map<String, String> params)
	{
		// method to cancel reserved trip

		String url = Global.JUGUNOO_WS + "Passenger/FleetUpdate";
		Log.i(TAG, "url --> " + url);

		JsonObjectRequest jsObjRequest = new JsonObjectRequest(
				Request.Method.POST, url, new JSONObject(params),
				ResponseListener.<JSONObject> createGenericReqSuccessListener(
						handler, Constant.MessageState.TRIPLOG_CANCEL_SUCCESS),
				ResponseListener.createErrorListener(handler,
						Constant.MessageState.TRIPLOG_CANCEL_FAILED));

		jsObjRequest.setShouldCache(false);
		jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(timeOut, 3, 1.0f));
		Global.getInstance().addToRequestQueue(jsObjRequest, tag);
	}

	public static void getBookingDetails(String tag, Handler handler,
			String engId, String userId)
	{
		// get booking details for change

		String url = Global.JUGUNOO_WS + "Passenger/GetTrip?EngId=" + engId
				+ "&UserId=" + userId;
		Log.i(TAG, "url --> " + url);

		JsonObjectRequest jsObjRequest = new JsonObjectRequest(url, null,
				ResponseListener.<JSONObject> createGenericReqSuccessListener(
						handler,
						Constant.MessageState.GET_BOOKING_DETAILS_SUCCESS),
				ResponseListener.createErrorListener(handler,
						Constant.MessageState.GET_BOOKING_DETAILS_FAIL));
		jsObjRequest.setShouldCache(false);
		jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(timeOut, 3, 1.0f));
		Global.getInstance().addToRequestQueue(jsObjRequest, tag);

	}

	// Fleet Booking
	public static void fleetBookingUpdateRequest(String tag, Handler handler,
			final Map<String, String> params)
	{
		String url = Global.JUGUNOO_WS + "Passenger/FleetUpdate";
		Log.i(TAG, "url --> " + url);

		JsonObjectRequest jsObjRequest = new JsonObjectRequest(
				Request.Method.POST, url, new JSONObject(params),
				ResponseListener.<JSONObject> createGenericReqSuccessListener(
						handler,
						Constant.MessageState.FLEET_BOOK_UPDATE_SUCCESS),
				ResponseListener.createErrorListener(handler,
						Constant.MessageState.FLEET_BOOK_UPDATE_FAIL));

		jsObjRequest.setShouldCache(false);
		jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(timeOut, 3, 1.0f));
		Global.getInstance().addToRequestQueue(jsObjRequest, tag);
	}

	public static void ExportTripLog(String tag, Handler handler,
			final Map<String, String> params)
	{
		String url = Global.JUGUNOO_WS + "Passenger/GetTripReport";
		Log.i(TAG, "url --> " + url);

		JsonObjectRequest jsObjRequest = new JsonObjectRequest(
				Request.Method.POST, url, new JSONObject(params),
				ResponseListener.<JSONObject> createGenericReqSuccessListener(
						handler, Constant.MessageState.EXPORT_TRIPLOG_SUCCESS),
				ResponseListener.createErrorListener(handler,
						Constant.MessageState.FAIL));

		jsObjRequest.setShouldCache(false);
		jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(timeOut, 3, 1.0f));
		Global.getInstance().addToRequestQueue(jsObjRequest, tag);
	}

	// login
	public static void feedBackRequest(String tag, Handler handler,
			final Map<String, String> params)
	{
		String URL = Global.JUGUNOO_WS + "Passenger/Feedback";
		Log.i(TAG, "url --> " + URL);

		JsonObjectRequest jsObjRequest = new JsonObjectRequest(
				Request.Method.POST, URL, new JSONObject(params),
				ResponseListener.<JSONObject> createGenericReqSuccessListener(
						handler, Constant.MessageState.FEEDBACK_SUCCESS),
				ResponseListener.createErrorListener(handler,
						Constant.MessageState.FAIL));

		jsObjRequest.setShouldCache(false);
		jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(timeOut, 3, 1.0f));
		Global.getInstance().addToRequestQueue(jsObjRequest, tag);
	}

	public static void GetGroupById(String tag, Handler handler, String GroupId)
	{
		String url = Global.JUGUNOO_WS + "Passenger/GetGroupById?GroupId="
				+ GroupId;
		Log.i(TAG, "url --> " + url);

		JsonObjectRequest jsObjRequest = new JsonObjectRequest(url, null,
				ResponseListener.<JSONObject> createGenericReqSuccessListener(
						handler, Constant.MessageState.GROUP_FLEET_SUCCESS),
				ResponseListener.createErrorListener(handler,
						Constant.MessageState.FAIL));
		jsObjRequest.setShouldCache(false);
		jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(timeOut, 3, 1.0f));
		Global.getInstance().addToRequestQueue(jsObjRequest, tag);
	}

	// get basic cab tariff
	public static void getBasicTariff(String tag, Handler handler,
			String cabType)
	{
		String URL = Global.JUGUNOO_WS
				+ "Passenger/GetCabDetailsByCabType?CabType=" + cabType;
		Log.d(TAG, "url --> " + URL);

		JsonObjectRequest jsObjRequest = new JsonObjectRequest(
				Request.Method.GET, URL, null,
				ResponseListener.<JSONObject> createGenericReqSuccessListener(
						handler, Constant.MessageState.BASIC_TARIFF_SUCCESS),
				ResponseListener.createErrorListener(handler,
						Constant.MessageState.BASIC_TARIFF_FAIL));

		jsObjRequest.setShouldCache(false);
		jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(timeOut, 3, 1.0f));
		Global.getInstance().addToRequestQueue(jsObjRequest, tag);
	}
	
	// GetUserGroupStatus-Pending Request
	public static void GetUserGroupStatus(String tag, Handler handler,
			String userID)
	{
		String URL = Global.JUGUNOO_WS + "Passenger/GetUserGroupStatus?UserId="
				+ userID;
		Log.i(TAG, "url --> " + URL);

		JsonObjectRequest jsObjRequest = new JsonObjectRequest(
				URL,
				null,
				ResponseListener
						.<JSONObject> createGenericReqSuccessListener(handler,
								Constant.MessageState.USERGROUP_STATUS_SUCCESS),
				ResponseListener.createErrorListener(handler,
						Constant.MessageState.FAIL));
		Log.i(TAG, "url :" + URL);
		jsObjRequest.setShouldCache(false);
		jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(timeOut, 3, 1.0f));
		Global.getInstance().addToRequestQueue(jsObjRequest, tag);
	}

	// Get Favorite Drivers
	public static void GetFavDriverlist(String tag, Handler handler,
			String userID)
	{
		String URL = Global.JUGUNOO_WS + "Passenger/GetFavDriverlist?UserId="
				+ userID;
		Log.i(TAG, "url --> " + URL);

		JsonObjectRequest jsObjRequest = new JsonObjectRequest(URL, null,
				ResponseListener.<JSONObject> createGenericReqSuccessListener(
						handler,
						Constant.MessageState.FAVORITEDRIVERS_STATUS_SUCCESS),
				ResponseListener.createErrorListener(handler,
						Constant.MessageState.FAIL));
		Log.i(TAG, "url :" + URL);
		jsObjRequest.setShouldCache(false);
		jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(timeOut, 3, 1.0f));
		Global.getInstance().addToRequestQueue(jsObjRequest, tag);
	}

	// Accept and Reject Pending Request
	public static void UserStatusPendingRequest(String tag, Handler handler,
			final Map<String, String> params)
	{
		String URL = Global.JUGUNOO_WS + "Passenger/UserGroupStatus";
		Log.i(TAG, "url --> " + URL);

		JsonObjectRequest jsObjRequest = new JsonObjectRequest(
				Request.Method.POST, URL, new JSONObject(params),
				ResponseListener.<JSONObject> createGenericReqSuccessListener(
						handler,
						Constant.MessageState.PENDINGUSERS_STATUS_SUCCESS),
				ResponseListener.createErrorListener(handler,
						Constant.MessageState.FAIL));

		jsObjRequest.setShouldCache(false);
		jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(timeOut, 3, 1.0f));
		Global.getInstance().addToRequestQueue(jsObjRequest, tag);
	}

	// Get Favorite Drivers
		public static void GetPendingRequestCount(String tag, Handler handler,
				String userID)
		{
			String URL = Global.JUGUNOO_WS + "Passenger/GetPendingRequestCount?UserId="
					+ userID;
			Log.i(TAG, "url --> " + URL);

			JsonObjectRequest jsObjRequest = new JsonObjectRequest(URL, null,
					ResponseListener.<JSONObject> createGenericReqSuccessListener(
							handler,
							Constant.MessageState.PENDINGREQUESTCOUNT_STATUS_SUCCESS),
					ResponseListener.createErrorListener(handler,
							Constant.MessageState.FAIL));
			Log.i(TAG, "url :" + URL);
			jsObjRequest.setShouldCache(false);
			jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(timeOut, 3, 1.0f));
			Global.getInstance().addToRequestQueue(jsObjRequest, tag);
		}

		// Update User Profile corp to personal & vice versa
		public static void updateUserProfile(String tag, Handler handler,
				final Map<String, String> params)
		{
			String URL = Global.JUGUNOO_WS + "Passenger/UpdateUserProfile";
			Log.i(TAG, "url --> " + URL);

			JsonObjectRequest jsObjRequest = new JsonObjectRequest(
					Request.Method.POST,
					URL,
					new JSONObject(params),
					ResponseListener.<JSONObject> createGenericReqSuccessListener(
							handler, Constant.MessageState.UPDATEPROFILE_STATUS_SUCCESS),
					ResponseListener.createErrorListener(handler,
							Constant.MessageState.UPDATEPROFILE_STATUS_FAIL));

			jsObjRequest.setShouldCache(false);
			jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(timeOut, 3, 1.0f));
			Global.getInstance().addToRequestQueue(jsObjRequest, tag);
		}
	
	
}
