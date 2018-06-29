package com.hirecraft.jugunoo.passenger.utility;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Pattern;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.hirecraft.jugunoo.passenger.R;

public class JugunooUtil
{

	private static final float RADIUS_FACTOR = 8.0f;
	private static final int TRIANGLE_WIDTH = 120;
	private static final int TRIANGLE_HEIGHT = 100;
	private static final int TRIANGLE_OFFSET = 300;

	Context context;
	public static Pattern pswNamePtrn = Pattern
			.compile("((?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%]).{6,15})");

	public JugunooUtil(Context context)
	{
		this.context = context;
	}

	public String getDirectionsUrl(LatLng origin, LatLng dest)
	{

		// Origin of route
		String str_origin = "origin=" + origin.latitude + ","
				+ origin.longitude;

		// Destination of route
		String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

		// Sensor enabled
		String sensor = "sensor=false";

		// Building the parameters to the web service
		String parameters = str_origin + "&" + str_dest + "&" + sensor;

		// Output format
		String output = "json";

		// Building the url to the web service
		String url = "https://maps.googleapis.com/maps/api/directions/"
				+ output + "?" + parameters;

		return url;
	}

	/** A method to download json data from url */
	public String downloadUrl(String strUrl) throws IOException
	{
		String data = "";
		InputStream iStream = null;
		HttpURLConnection urlConnection = null;
		try
		{
			URL url = new URL(strUrl);

			// Creating an http connection to communicate with url
			urlConnection = (HttpURLConnection) url.openConnection();

			// Connecting to url
			urlConnection.connect();

			// Reading data from url
			iStream = urlConnection.getInputStream();

			BufferedReader br = new BufferedReader(new InputStreamReader(
					iStream));

			StringBuffer sb = new StringBuffer();

			String line = "";
			while ((line = br.readLine()) != null)
			{
				sb.append(line);
			}

			data = sb.toString();

			br.close();

		}
		catch (Exception e)
		{
			Log.d("Exception while downloading url", e.toString());
		}
		finally
		{
			try
			{
				if (iStream != null)
				{
					iStream.close();
				}

				if (urlConnection != null)
				{
					urlConnection.disconnect();
				}
			}
			catch (Exception e2)
			{
				e2.printStackTrace();
			}

		}
		return data;
	}

	public void animateMarker(GoogleMap googleMap, final Marker marker,
			final LatLng toPosition, final boolean hideMarker)
	{
		final Handler handler = new Handler();
		final long start = SystemClock.uptimeMillis();
		Projection proj = googleMap.getProjection();
		Point startPoint = proj.toScreenLocation(marker.getPosition());
		final LatLng startLatLng = proj.fromScreenLocation(startPoint);
		final long duration = 500;

		final Interpolator interpolator = new LinearInterpolator();

		handler.post(new Runnable()
		{
			@Override
			public void run()
			{
				long elapsed = SystemClock.uptimeMillis() - start;
				float t = interpolator.getInterpolation((float) elapsed
						/ duration);
				double lng = t * toPosition.longitude + (1 - t)
						* startLatLng.longitude;
				double lat = t * toPosition.latitude + (1 - t)
						* startLatLng.latitude;
				marker.setPosition(new LatLng(lat, lng));

				if (t < 1.0)
				{
					// Post again 16ms later.
					handler.postDelayed(this, 16);
				}
				else
				{
					if (hideMarker)
					{
						marker.setVisible(false);
					}
					else
					{
						marker.setVisible(true);
					}
				}
			}
		});
	}

	public int CabDistance(LatLng ln, double mLati, double mLongi)
	{

		double cabLati = ln.latitude;
		double cabLongi = ln.longitude;

		float mLatif = (float) mLati;
		float mLongif = (float) mLongi;
		float cabLatif = (float) cabLati;
		float cabLongif = (float) cabLongi;
		double distance = distFrom(mLatif, mLongif, cabLatif, cabLongif);
		float distancef = (float) distance;
		int dis = (int) distancef;
		return dis;
	}

	public double distFrom(float lat1, float lng1, float lat2, float lng2)
	{
		double earthRadius = 3958.75;
		double dLat = Math.toRadians(lat2 - lat1);
		double dLng = Math.toRadians(lng2 - lng1);
		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
				+ Math.cos(Math.toRadians(lat1))
				* Math.cos(Math.toRadians(lat2)) * Math.sin(dLng / 2)
				* Math.sin(dLng / 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		double dist = earthRadius * c;

		int meterConversion = 1609;
		return dist * meterConversion;
	}

	public Bitmap driverImage(String blobs)
	{
		try
		{
			byte[] decodedString = Base64.decode(blobs, Base64.DEFAULT);
			Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0,
					decodedString.length);
			return bitmap;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

	public String getUniqueDeviceID(Context context)
	{

		return Settings.Secure.getString(context.getContentResolver(),
				Settings.Secure.ANDROID_ID);

	}

	public static String driveImage;

	public double angleBteweenCoordinate(double lat1, double long1,
			double lat2, double long2)
	{

		double dLon = (long2 - long1);

		double y = Math.sin(dLon) * Math.cos(lat2);
		double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1)
				* Math.cos(lat2) * Math.cos(dLon);

		double brng = Math.atan2(y, x);

		brng = Math.toDegrees(brng);
		brng = (brng + 360) % 360;
		brng = 360 - brng;

		return brng;
	}

	public static boolean isConnectedToInternet(final Context scontext)
	{
		try
		{
			ConnectivityManager connectivity = (ConnectivityManager) scontext
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			if (connectivity != null)
			{
				NetworkInfo[] info = connectivity.getAllNetworkInfo();
				if (info != null)
					for (int i = 0; i < info.length; i++)
						if (info[i].getState() == NetworkInfo.State.CONNECTED)
						{
							return true;
						}

			}
			return false;
		}
		catch (Exception e)
		{

			return false;
		}
	}

	public static void showErrorMessage(final Context context,
			final RelativeLayout layout, final TextView textView, String message)
	{

		Animation slideDown = AnimationUtils.loadAnimation(context,
				R.anim.slide_down);

		layout.setAnimation(slideDown);
		layout.setVisibility(View.VISIBLE);
		textView.setText(message);

		final Handler handler = new Handler();

		Thread thread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					Thread.sleep(2000);
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
						Animation slideUp = AnimationUtils.loadAnimation(
								context, R.anim.slide_up);

						layout.setAnimation(slideUp);
						layout.setVisibility(View.INVISIBLE);
					}
				});
			}
		});

		// Starting thread
		thread.start();
	}

	public Bitmap processImage(Bitmap bitmap)
	{
		Bitmap bmp;

		bmp = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(),
				Bitmap.Config.ARGB_8888);
		BitmapShader shader = new BitmapShader(bitmap,
				BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);

		float radius = Math.min(bitmap.getWidth(), bitmap.getHeight())
				/ RADIUS_FACTOR;
		Canvas canvas = new Canvas(bmp);
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setShader(shader);

		RectF rect = new RectF(TRIANGLE_WIDTH, 0, bitmap.getWidth(),
				bitmap.getHeight());
		canvas.drawRoundRect(rect, radius, radius, paint);

		Path triangle = new Path();
		triangle.moveTo(0, TRIANGLE_OFFSET);
		triangle.lineTo(TRIANGLE_WIDTH, TRIANGLE_OFFSET - (TRIANGLE_HEIGHT / 2));
		triangle.lineTo(TRIANGLE_WIDTH, TRIANGLE_OFFSET + (TRIANGLE_HEIGHT / 2));
		triangle.close();
		canvas.drawPath(triangle, paint);

		return bmp;
	}

}
