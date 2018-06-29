package com.hirecraft.jugunoo.passenger;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.hirecraft.jugunoo.passenger.common.Global;
import com.hirecraft.jugunoo.passenger.gcm.GCMRegistrationHelper;
import com.hirecraft.jugunoo.passenger.listeners.GcmIdListener;
import com.hirecraft.jugunoo.passenger.utility.JugunooUtil;
import com.hirecraft.jugunoo.passenger.utility.SharedPreferencesManager;

public class GenerateGcmId extends AsyncTask<String, String, String>
{
	private SharedPreferencesManager mgr;
	private GCMRegistrationHelper gcmRegistrationHelper;
	private Context context;

	private String deviceId;
	private String userId;
	private GcmIdListener listener1;
	private GcmIdListener listener2;

	public GenerateGcmId(Context context, GcmIdListener listener1,
			GcmIdListener listener2)
	{
		this.context = context;
		this.listener1 = listener1;
		this.listener2 = listener2;
	}

	@Override
	protected void onPreExecute()
	{
		super.onPreExecute();
		try
		{
			mgr = new SharedPreferencesManager(context);
			gcmRegistrationHelper = new GCMRegistrationHelper(context);

			// Generating deviceId
			JugunooUtil util = new JugunooUtil(context);
			deviceId = util.getUniqueDeviceID(context);

			userId = mgr.GetValueFromSharedPrefs("UserID");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	protected String doInBackground(String... params)
	{
		String gcmRegID = "";
		try
		{
			gcmRegID = gcmRegistrationHelper.Register(deviceId, userId);
			mgr.SaveValueToSharedPrefs("GCM_ID", gcmRegID);

			Log.i(Global.APPTAG, "GCM: id -->" + gcmRegID);
		}
		catch (Exception bug)
		{
			bug.printStackTrace();
		}
		return gcmRegID;
	}

	@Override
	protected void onPostExecute(String gcmId)
	{
		super.onPostExecute(gcmId);

		if (!TextUtils.isEmpty(gcmId))
		{
			if (listener1 != null)
			{
				listener1.gcmIdListener(userId, deviceId, gcmId);
			}
			else if (listener2 != null)
			{
				listener2.gcmIdListener(userId, deviceId, gcmId);
			}
		}
	}
}
