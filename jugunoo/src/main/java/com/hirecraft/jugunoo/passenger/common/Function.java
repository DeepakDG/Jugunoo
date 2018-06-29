package com.hirecraft.jugunoo.passenger.common;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;
import android.view.Gravity;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class Function
{
	public static void showToast(Context context, String message)
	{
		// Method shows toast below the action bar

		Style style = new Style.Builder()
				.setBackgroundColorValue(Color.parseColor("#EC6C6B"))
				.setGravity(Gravity.CENTER_HORIZONTAL)
				.setTextColorValue(Color.parseColor("#323a2c")).build();

		Crouton.showText((Activity) context, message, style);
	}

	public static void hideSoftKeyBoard(Context context, EditText editext)
	{
		// Hide keys Method Function
		InputMethodManager imm = (InputMethodManager) context
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(editext.getWindowToken(), 0);

	}

	public static void hideSoftKeyBoard(Context context)
	{
		// Hide keys Method Function
		InputMethodManager inputMethodManager = (InputMethodManager) context
				.getSystemService(context.INPUT_METHOD_SERVICE);
		inputMethodManager.hideSoftInputFromWindow(((Activity) context)
				.getCurrentFocus().getWindowToken(), 0);

	}

	public static void showSoftKeyBoard(Context context)
	{
		// Hide keys Method Function
		InputMethodManager imm = (InputMethodManager) context
				.getSystemService(context.INPUT_METHOD_SERVICE);
		if (imm != null)
		{
			imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
		}

	}

	public static boolean isInteger(String s)
	{
		// Method to check whether Integer or String

		String regex = "[0-9]+";
		return s.matches(regex);
	}

	public static String Epoch2DateString(String date, String dateFormater)
	{
		// Method to returns String Date from constant timeStamp

		long epochSeconds = 0;
		try
		{
			epochSeconds = Long.parseLong(date) / 1000;
		}
		catch (NumberFormatException e)
		{
			e.printStackTrace();
		}

		Date updatedate = new Date(epochSeconds * 1000);
		SimpleDateFormat format = new SimpleDateFormat(dateFormater,
				Locale.getDefault());
		return format.format(updatedate);
	}

	public static String getDateTimeFromUTC(String dateString, String format)
	{
		// Method to returns String Date from UTC

		long date = Long.parseLong(dateString);

		SimpleDateFormat formatter = new SimpleDateFormat(
				"yyyy-MM-dd'T'HH:mm:ss");
		formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
		Date value = null;
		try
		{
			value = new Date(date);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		SimpleDateFormat dateFormatter = new SimpleDateFormat(format);

		TimeZone tz = TimeZone.getDefault();
		int currentOffsetFromUTC = tz.getRawOffset()
				+ (tz.inDaylightTime(value) ? tz.getDSTSavings() : 0);
		String resultString = dateFormatter.format(value.getTime()
				+ currentOffsetFromUTC);

		return resultString;
	}

	public static String timeFormater24Hrs(int hourOfDay, int minute)
	{
		// Method to change time format to 24 hours

		String minStr = String.valueOf(minute);
		String hourStr = String.valueOf(hourOfDay);

		if (minStr.length() == 1)
		{
			minStr = "0" + minStr;
		}

		if (hourStr.length() == 1)
		{
			hourStr = "0" + hourStr;
		}

		return hourStr + ":" + minStr;
	}

	public static String getMillsTs(String dateString, String format)
	{
		// Method to returns Long mills from stringDate

		SimpleDateFormat f = new SimpleDateFormat(format);

		Date date = null;
		try
		{
			date = f.parse(dateString);
		}
		catch (ParseException e)
		{
			e.printStackTrace();
		}

		Log.v("TAG", "time --> " + date.getTime());

		return String.valueOf(date.getTime());
	}

	public static boolean checkForHourDiff(String dateTimeStr1,
			String dateTimeStr2, int hourDif)
	{
		/*
		 * Method will returns true if selected dateTime is greater than 1 hour
		 * of current dateTime
		 */

		// Date now = Calendar.getInstance().getTime(); // Get time now

		long MAX_DURATION = TimeUnit.MILLISECONDS.convert(hourDif,
				TimeUnit.MINUTES);

		// long duration = Long.parseLong(getMillsTs(dateTimeStr1,
		// "MM/dd/yyyy HH:mm:ss")) - now.getTime();

		long duration = Long.parseLong(getMillsTs(dateTimeStr1,
				"MM/dd/yyyy HH:mm:ss"))
				- Long.parseLong(getMillsTs(dateTimeStr2, "MM/dd/yyyy HH:mm:ss"));

		if (duration >= MAX_DURATION)
		{
			return true;
		}
		return false;
	}

	public static boolean isDateLessThanCurrentDate(String date)
	{
		/*
		 * Method will returns true if selected dateTime is lesser than current
		 * date
		 */
		try
		{
			DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
			
			Calendar cal = Calendar.getInstance();
			cal.setTime(df.parse(date));

			String nowStr = df.format(Calendar.getInstance().getTime());
			Calendar cal2 = Calendar.getInstance();
			cal2.setTime(df.parse(nowStr));

			if (cal.before(cal2))
			{
				return true;
			}
		}
		catch (ParseException e)
		{
			e.printStackTrace();
		}

		return false;
	}

	public static boolean isProcessRunning(Context context)
	{
		// Check the app is running in background or not

		ActivityManager activityManager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> procInfos = activityManager
				.getRunningAppProcesses();

		for (int i = 0; i < procInfos.size(); i++)
		{
			if (procInfos.get(i).processName
					.equals("com.hirecraft.jugunoo.passenger"))
			{
				return true;
			}
		}

		return false;
	}

	public static void clearNotification(Context context)
	{
		// Clear notification

		NotificationManager notificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancelAll();
	}

	public static void playNotificationSound(Context context)
	{
		// Method to play default notification sound
		try
		{
			Uri notification = RingtoneManager
					.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
			Ringtone r = RingtoneManager.getRingtone(context, notification);
			r.play();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

}
