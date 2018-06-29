package com.hirecraft.jugunoo.passenger.services;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.hirecraft.jugunoo.passenger.common.Function;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class VolleyErrorHelper
{
	/**
	 * Returns appropriate message which is to be displayed to the user against
	 * the specified error object.
	 * 
	 */

	private static String connectionTimeOut = "Connection timeout. Please try again";
	private static String networkError = "No internet connection";
	private static String serverError = "Unable to connect server. Please try later";
	private static String defError = "Something went wrong. Please try later";

	public static void getMessage(Object error, Context context,
			boolean showToolTipToast)
	{
		try
		{
			String whatError = "";

			if (error instanceof TimeoutError)
			{
				Log.e("Error msg", "result msg-->" + "time out");
				whatError = connectionTimeOut;
			}
			else if (error instanceof ServerError
					|| error instanceof AuthFailureError)
			{
				Log.e("Error msg", "result msg-->" + "ServerError");
				whatError = serverError;
			}
			else if (error instanceof NetworkError
					|| error instanceof NoConnectionError)
			{
				Log.e("Error msg", "result msg-->" + "NetworkError");
				whatError = networkError;
			}
			else
			{
				whatError = defError;
			}

			// Showing error message to user
			if (showToolTipToast)
			{
				Function.showToast(context, whatError);
			}
			else
			{
				Toast.makeText(context, whatError, Toast.LENGTH_SHORT).show();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
