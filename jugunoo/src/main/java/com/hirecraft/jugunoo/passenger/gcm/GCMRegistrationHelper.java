package com.hirecraft.jugunoo.passenger.gcm;

import android.content.Context;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.hirecraft.jugunoo.passenger.common.Global;
import com.hirecraft.jugunoo.passenger.utility.SharedPreferencesManager;

public final class GCMRegistrationHelper
{

	static GoogleCloudMessaging gcm = null;

	static Context ctx = null;

	public GCMRegistrationHelper(Context context)
	{
		ctx = context;
	}

	public String Register(String deviceID, String userID) throws Exception
	{
		String regid = "";

		if (!checkPlayServices())
			throw new Exception(
					"Google Play Services not supported. Please install and configure Google Play Store.");

		if (gcm == null)
		{
			gcm = GoogleCloudMessaging.getInstance(ctx);
		}
		regid = gcm.register(Global.SENDER_ID);

		return regid;
	}

	public String Register(String deviceID) throws Exception
	{
		String regid = "";

		if (!checkPlayServices())
			throw new Exception(
					"Google Play Services not supported. Please install and configure Google Play Store.");

		if (gcm == null)
		{
			gcm = GoogleCloudMessaging.getInstance(ctx);
		}
		regid = gcm.register(Global.SENDER_ID);

		return regid;
	}

	public boolean SaveIDToSharedPrefs(String GCMid)
	{
		SharedPreferencesManager mgr = new SharedPreferencesManager(ctx);
		return mgr.SaveValueToSharedPrefs("GCM_ID", GCMid);
	}

	private static boolean checkPlayServices()
	{
		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(ctx);
		if (resultCode != ConnectionResult.SUCCESS)
		{
			return false;
		}
		return true;
	}
}