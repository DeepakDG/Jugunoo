package com.hirecraft.jugunoo.passenger.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.acra.sender.HttpSender;
import org.json.JSONArray;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.Volley;
import com.hirecraft.jugunoo.passenger.R;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

//@ReportsCrashes(formKey = "", // will not be used
//mailTo = "deepak@hirecraft.in", mode = ReportingInteractionMode.TOAST, resToastText = R.string.crash_toast_text)

// @ReportsCrashes(formKey = "",
// reportType = HttpSender.Type.JSON,
// httpMethod = HttpSender.Method.POST,
// formUri = "http://api.jugunoo.com/uat/Passenger/Acra",
// mode = ReportingInteractionMode.DIALOG,
// resToastText = R.string.crash_toast_text)

@ReportsCrashes(formKey = "", mode = ReportingInteractionMode.DIALOG, formUri = "http://api.jugunoo.com/uat/Passenger/Acra", reportType = HttpSender.Type.JSON, httpMethod = HttpSender.Method.POST, resDialogText = R.string.crash_dialog_text, resDialogIcon = android.R.drawable.ic_dialog_info, // optional.
resDialogTitle = R.string.crash_dialog_title)
public class Global extends Application
{
	/**
	 * Log or request TAG
	 */

	public static String APPTAG = "JugunooP";
	public static String networkErrorMsg = "Not able to connect, please try again";

	// User Variables
	public static String UserID = "";
	public static String GCMID = "";
	public static String CabID = "";

	// Roles
	public static Boolean IsDriver = false;
	public static Boolean IsPassenger = false;
	public static Boolean IsTracker = false;

	public static Boolean isCorrect = false;

	public static int SELECTED_ITEM_FO = -1;
	public static int SELECTED_ITEM_U = -1;
	public static int SELECTED_ITEM_G = -1;
	public static ArrayList<HashMap<String, String>> fleets = null;

	public static String DEVICE_ID;

	// status to check whether trip log is updated or not
	public static boolean isTripLogUpdated = false;

	/**
	 * Web services
	 */
	public static String JUGUNOO_WS_LOCATION = PassengerUrlManager
			.getLocationUrl();
	public static String JUGUNOO_WS = PassengerUrlManager.getUserApiUrl();

	public static String LOCATIONSERVICE_CALLBACK = "0";
	public static String SENDER_ID = "1075779076760";
	public static final String PROPERTY_REG_ID = "GCM_ID";
	public static final String PROPERTY_APP_VERSION = "appVersion";
	public static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
	public static int REQUEST_CODE_RECOVER_PLAY_SERVICES = 1001;
	public static int REQUEST_LOCATIONS_UPDATES = 10 * 60 * 1000;
	public static int REQUEST_LOCATIONS_UPDATES_ISBOOKED = 5 * 60 * 1000;
	public static boolean swipeOverlap = true;
	public static String driverDeviceID = "";
	public static boolean ISCAB_HIRED = false;

	public static String BROWSER_KEY = "AIzaSyCdpuFhgl44hjkGK4ACSscPrHaKJQ4N3Bo";
	public static String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place/autocomplete/";
	public static String OUT_JSON = "json?";

	// Active/Inactive status update in Groups and User Fragment
	public static boolean IsGroupstatUpdated = false;
	public static boolean IsUserstatUpdated = false;

	public static final String FORCE_LOCAL = "force_local";

	public static JSONArray fleetOperaatorArray = new JSONArray();
	public static JSONArray fleetGroupArray = new JSONArray();
	public static JSONArray fleetUserArray = new JSONArray();

	public static void updateLanguage(Context ctx, String lang)
	{
		Configuration cfg = new Configuration();
		SharedPreferences force_pref = PreferenceManager
				.getDefaultSharedPreferences(ctx);
		String language = force_pref.getString(FORCE_LOCAL, "");

		if (TextUtils.isEmpty(language) && lang == null)
		{
			cfg.locale = Locale.getDefault();

			SharedPreferences.Editor edit = force_pref.edit();
			String tmp = "";
			tmp = Locale.getDefault().toString().substring(0, 2);

			edit.putString(FORCE_LOCAL, tmp);
			edit.commit();

		}
		else if (lang != null)
		{
			cfg.locale = new Locale(lang);
			SharedPreferences.Editor edit = force_pref.edit();
			edit.putString(FORCE_LOCAL, lang);
			edit.commit();

		}
		else if (!TextUtils.isEmpty(language))
		{
			cfg.locale = new Locale(language);
		}

		ctx.getResources().updateConfiguration(cfg, null);
	}

	public boolean getSwipeOverlap()
	{
		return swipeOverlap;
	}

	public void setSwipeOverlap(boolean swipe)
	{
		swipeOverlap = swipe;
	}

	private RequestQueue mRequestQueue;

	private static Global sInstance;

	@Override
	public void onCreate()
	{
		super.onCreate();
		// Crash reporting tool
		ACRA.init(this);

		sInstance = this;

		DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
				.cacheInMemory(true).cacheOnDisk(true).build();

		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
				getApplicationContext()).memoryCacheExtraOptions(480, 800)
				.denyCacheImageMultipleSizesInMemory()
				.memoryCache(new LruMemoryCache(2 * 1024 * 1024))
				.memoryCacheSize(90 * 1024 * 1024)
				.diskCacheSize(90 * 1024 * 1024).diskCacheFileCount(100)
				.defaultDisplayImageOptions(defaultOptions).writeDebugLogs()
				.build();

		ImageLoader.getInstance().init(config);

	}

	public static synchronized Global getInstance()
	{
		return sInstance;
	}

	public RequestQueue getRequestQueue()
	{
		if (mRequestQueue == null)
		{
			mRequestQueue = Volley.newRequestQueue(getApplicationContext());
		}

		return mRequestQueue;
	}

	public <T> void addToRequestQueue(Request<T> req, String tag)
	{

		req.setTag(TextUtils.isEmpty(tag) ? APPTAG : tag);

		VolleyLog.d("Adding request to queue: %s", req.getUrl());

		getRequestQueue().add(req);
	}

	public <T> void addToRequestQueue(Request<T> req)
	{

		req.setTag(APPTAG);

		getRequestQueue().add(req);
	}

	/**
	 * Cancels all pending requests by the specified TAG, it is important to
	 * specify a TAG so that the pending/ongoing requests can be cancelled.
	 * 
	 * @param tag
	 */
	public void cancelPendingRequests(Object tag)
	{
		if (mRequestQueue != null)
		{
			mRequestQueue.cancelAll(tag);

		}
	}

	/* Functions to check whether activity is visible to user or not */

	private static boolean activityVisible;

	public static boolean isActivityVisible()
	{
		return activityVisible;
	}

	public static void activityResumed()
	{
		activityVisible = true;
	}

	public static void activityPaused()
	{
		activityVisible = false;
	}

	/* End of checking activity is visible or not */
}