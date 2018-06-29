package com.hirecraft.jugunoo.passenger.networkhandler;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;

public class ResponseListener<T>
{

	private static final String TAG = ResponseListener.class.getSimpleName();

	public static <T> Response.Listener<T> createGenericReqSuccessListener(
			final Handler handler, final int messageId)
	{
		return new Response.Listener<T>()
		{
			@Override
			public void onResponse(T response)
			{
				Log.v(TAG, "volley=" + response.toString());

				Message msg = new Message();
				msg.arg1 = messageId;
				msg.obj = response;
				handler.sendMessage(msg);
			}
		};
	}

	public static Response.ErrorListener createErrorListener(
			final Handler handler, final int messageId)
	{
		return new Response.ErrorListener()
		{
			@Override
			public void onErrorResponse(VolleyError error)
			{
				Message msg = new Message();
				msg.arg1 = messageId;
				msg.obj = error;
				handler.sendMessage(msg);
			}
		};

	}

}
