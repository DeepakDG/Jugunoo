package com.hirecraft.jugunoo.passenger.gcm;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.hirecraft.jugunoo.passenger.R;
import com.hirecraft.jugunoo.passenger.SplashScreen;
import com.hirecraft.jugunoo.passenger.common.Function;
import com.hirecraft.jugunoo.passenger.common.Global;
import com.hirecraft.jugunoo.passenger.services.PassengerFeedback;
import com.hirecraft.jugunoo.passenger.utility.SharedPreferencesManager;
import com.hirecraft.jugunoo.passenger.utility.WakeLocker;

public class GCMNotificationHandler extends IntentService
{
	public static final int NOTIFICATION_ID = 54;
	public static final int NOTIFICATION_START_ID = 55;
	public static final int NOTIFICATION_HIRED_ID = 56;
	private NotificationCompat.Builder builder;

	private NotificationManager mNotificationManager;

	private SharedPreferencesManager mgr;

	public GCMNotificationHandler()
	{
		super("Jugunoo GCM Notification Handler");
	}

	@Override
	protected void onHandleIntent(Intent intent)
	{
		Bundle extras = intent.getExtras();

		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);

		String messageType = gcm.getMessageType(intent);

		if (!extras.isEmpty())
		{
			if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType))
			{

				Log.e("gcm", "gcm recieved");

				mgr = new SharedPreferencesManager(getApplicationContext());

				String title = "";

				try
				{
					if (extras.containsKey("Title"))
					{
						title = extras.getString("Title");
					}

					String headerMsg = extras.getString("Header");

					String status = "";
					if (extras.containsKey("TripStatus"))
					{
						status = extras.getString("TripStatus");
					}

					Log.e("TAG", "TripStatus" + status);

					// if (status.equalsIgnoreCase("E"))
					// {
					// String tripId = extras.get("TripId").toString();
					//
					// mgr.SaveValueToSharedPrefs("EngID", tripId);
					//
					// // sendNotification(title, title, NOTIFICATION_ID);
					// // broadcastServiceStatus(title, tripId);
					// }

					if (status.equalsIgnoreCase("H"))
					{
						// String headerMsg = extras.getString("Header");
						// String tripId = extras.get("TripId").toString();
						// String tripStatus = extras.getString("TripStatus")
						// .toString();
						// String name =
						// extras.getString("FirstName").toString();
						// String mobile =
						// extras.getString("Mobile").toString();
						// String cabNo = extras.get("CabNo").toString();
						// String cabID = extras.get("CabId").toString();
						// String driverID = extras.get("DriverId").toString();

						// mgr.SaveValueToSharedPrefs("TripStatus", "ENGAGED");
						// mgr.SaveValueToSharedPrefs("EngID", tripId);
						// mgr.SaveValueToSharedPrefs("DriverID", driverID);
						// mgr.SaveValueToSharedPrefs("CabId", cabID);
						// mgr.SaveValueToSharedPrefs("CabNo", cabNo);
						// mgr.SaveValueToSharedPrefs("DriverMobile", mobile);
						// mgr.SaveValueToSharedPrefs("message", message);

						// broadcastServiceStatus(title, message, tripId,
						// tripStatus, name, mobile, cabNo, driverID,
						// cabID);

						sendNotification(headerMsg, title,
								NOTIFICATION_HIRED_ID, false, false);
					}
					else if (status.equalsIgnoreCase("S"))
					{
						// String headerMsg = extras.getString("Header");
						// String driverId = extras.get("DriverId").toString();
						// String result = extras.get("Result").toString();
						// String tripId = extras.get("TripId").toString();
						// String tripStatus = extras.getString("TripStatus")
						// .toString();

						// mgr.SaveValueToSharedPrefs("TripID", tripId);

						// broadcastServiceStatus(title, tripId, result,
						// tripStatus, driverId);

						sendNotification(headerMsg, title,
								NOTIFICATION_HIRED_ID, false, false);
					}
					else if (status.equalsIgnoreCase("AC"))
					{
						Function.clearNotification(getBaseContext());

						sendNotification(headerMsg, title,
								NOTIFICATION_HIRED_ID, false, true);
					}
					else if (status.equalsIgnoreCase("F"))
					{
						if (WakeLocker.wakeLock == null)
						{
							String tripId = extras.get("TripId").toString();
							String tripStatus = extras.getString("TripStatus")
									.toString();
							String pick = extras.getString("PickPoint")
									.toString();
							String time = extras.getString("EngageTime")
									.toString();
							String drop = extras.getString("DropPoint")
									.toString();
							String name = extras.getString("FirstName")
									.toString();
							String mobile = extras.getString("Mobile")
									.toString();
							String driverID = extras.get("DriverId").toString();

							// mgr.SaveValueToSharedPrefs("TripStatus",
							// "FINISH");
							mgr.SaveValueToSharedPrefs("EngID", tripId);
							mgr.SaveValueToSharedPrefs("EngageTime", time);
							mgr.SaveValueToSharedPrefs("DriverID", driverID);
							mgr.SaveValueToSharedPrefs("fPick", pick);
							mgr.SaveValueToSharedPrefs("fDrop", drop);

							// broadcastServiceStatusF(title, tripId,
							// tripStatus,
							// name, mobile, driverID, pick, drop, time);

							Function.clearNotification(getBaseContext());

							sendNotificationFinish(headerMsg, title,
									NOTIFICATION_ID);
						}
					}
					else if (status.equalsIgnoreCase("AB"))
					{
						// String tripId = extras.getString("TripId");

						// mgr.SaveValueToSharedPrefs("TripStatus", "AC");
						// broadcastServiceStatus(title, tripId, stat);

						Function.clearNotification(getBaseContext());

						sendNotification(headerMsg, title,
								NOTIFICATION_HIRED_ID, true, false);
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
					Log.d(Global.APPTAG,
							"Ignore message as no handler found. Message Owner is ("
									+ title + ")");
				}

				Log.i("Notification Received", "Received: " + extras.toString());
			}
		}
		// Release the wake lock provided by the WakefulBroadcastReceiver.
		GCMReceiver.completeWakefulIntent(intent);
	}

	// private void broadcastServiceStatus(String title, String message,
	// String tripId, String tripStatus, String name, String mobile,
	// String cabNo, String driverID, String cabID)
	// {
	//
	// Intent i = new Intent();
	// i.setAction(GCMResponseReceiver.PROCESS_RESPONSE);
	// i.addCategory(Intent.CATEGORY_DEFAULT);
	// i.putExtra("Title", title);
	// i.putExtra("message", message);
	// i.putExtra("TripId", tripId);
	// i.putExtra("TripStatus", tripStatus);
	// i.putExtra("DriverName", name);
	// i.putExtra("DriverMobile", mobile);
	// i.putExtra("CabNo", cabNo);
	// i.putExtra("CabId", cabID);
	// i.putExtra("DriverId", driverID);
	// sendBroadcast(i);
	//
	// }
	//
	// private void broadcastServiceStatusF(String title, String tripId,
	// String tripStatus, String name, String mobile, String driverphoto,
	// String pick, String drop, String time)
	// {
	//
	// Intent i = new Intent();
	// i.setAction(GCMResponseReceiver.PROCESS_RESPONSE);
	// i.addCategory(Intent.CATEGORY_DEFAULT);
	// i.putExtra("Title", title);
	// i.putExtra("TripId", tripId);
	// i.putExtra("TripStatus", tripStatus);
	// i.putExtra("DriverName", name);
	// i.putExtra("DriverMobile", mobile);
	// i.putExtra("DriverId", driverphoto);
	// i.putExtra("PickPoint", pick);
	// i.putExtra("DropPoint", drop);
	// i.putExtra("EngageTime", time);
	//
	// sendBroadcast(i);
	// }
	//
	// private void broadcastServiceStatus(String title, String tripId)
	// {
	//
	// Intent i = new Intent();
	// i.setAction(GCMResponseReceiver.PROCESS_RESPONSE);
	// i.addCategory(Intent.CATEGORY_DEFAULT);
	// i.putExtra("Title", title);
	// i.putExtra("TripID", tripId);
	//
	// sendBroadcast(i);
	//
	// }
	//
	// private void broadcastServiceStatus(String title, String tripId,
	// String result, String status, String driverId)
	// {
	// Intent i = new Intent();
	// i.setAction(GCMResponseReceiver.PROCESS_RESPONSE);
	// i.addCategory(Intent.CATEGORY_DEFAULT);
	// i.putExtra("Title", title);
	// i.putExtra("DriverId", driverId);
	// i.putExtra("TripId", tripId);
	// i.putExtra("Result", result);
	// i.putExtra("TripStatus", status);
	//
	// sendBroadcast(i);
	// }
	//
	// private void broadcastServiceStatus(String title, String tripId,
	// String status)
	// {
	// Intent i = new Intent();
	// i.setAction(GCMResponseReceiver.PROCESS_RESPONSE);
	// i.addCategory(Intent.CATEGORY_DEFAULT);
	// i.putExtra("TripStatus", status);
	// i.putExtra("Title", title);
	// sendBroadcast(i);
	// }

	private void sendNotification(String header, String title, int id,
			boolean isAB, boolean isAC)
	{
		Log.e("gcm", "notification=" + header);

		if (WakeLocker.wakeLock == null)
		{
			Intent intent = new Intent(getApplicationContext(),
					SplashScreen.class);
			if (isAB)
			{
				intent.putExtra("isAB", "true");
			}
			else if (isAC)
			{
				intent.putExtra("isAC", "true");
			}

			mNotificationManager = (NotificationManager) this
					.getSystemService(Context.NOTIFICATION_SERVICE);

			PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
					intent, PendingIntent.FLAG_UPDATE_CURRENT);

			Notification notify = null;

			builder = new NotificationCompat.Builder(this)
					.setSmallIcon(R.drawable.ic_launcher)
					.setContentTitle(header)
					.setStyle(
							new NotificationCompat.BigTextStyle()
									.bigText(title))
					.setLights(Color.BLUE, 500, 1000).setAutoCancel(true)
					.setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
					.setWhen(System.currentTimeMillis()).setContentText(title);
			builder.setContentIntent(contentIntent);

			notify = builder.build();

			mNotificationManager.notify(id, notify);
		}
	}

	private void sendNotificationFinish(String header, String title, int id)
	{
		Log.e("gcm", "notification=" + header);
		Intent intent = new Intent(getApplicationContext(),
				PassengerFeedback.class);

		mNotificationManager = (NotificationManager) this
				.getSystemService(Context.NOTIFICATION_SERVICE);

		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);

		Notification notify = null;

		builder = new NotificationCompat.Builder(this)
				.setSmallIcon(R.drawable.ic_launcher).setContentTitle(header)
				.setStyle(new NotificationCompat.BigTextStyle().bigText(title))
				.setLights(Color.BLUE, 500, 1000).setAutoCancel(true)
				.setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
				.setWhen(System.currentTimeMillis()).setContentText(title);
		builder.setContentIntent(contentIntent);

		notify = builder.build();

		mNotificationManager.notify(id, notify);
	}
}
