package com.hirecraft.jugunoo.passenger.locationmanager;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.hirecraft.jugunoo.passenger.listeners.LocationResultListener;

public class LocationService
{

	private final LocationListener mGpsLocationListener;
	private final LocationListener mNetworkLocationListener;
	private LocationResultListener mLocationResultListener;
	private LocationManager mLocationManager;
	private Timer mTimer;
	private boolean mGpsEnabled;
	private boolean mNetworkEnabled;

	public LocationService()
	{

		mGpsLocationListener = new LocationListener()
		{
			@Override
			public void onLocationChanged(Location location)
			{
				stop();

				mLocationResultListener.onLocationResultAvailable(location);
			}

			@Override
			public void onStatusChanged(String s, int i, Bundle bundle)
			{
			}

			@Override
			public void onProviderEnabled(String s)
			{
			}

			@Override
			public void onProviderDisabled(String s)
			{
			}
		};

		mNetworkLocationListener = new

		LocationListener()
		{
			@Override
			public void onLocationChanged(Location location)
			{
				stop();

				mLocationResultListener.onLocationResultAvailable(location);
			}

			@Override
			public void onStatusChanged(String s, int i, Bundle bundle)
			{
			}

			@Override
			public void onProviderEnabled(String s)
			{
			}

			@Override
			public void onProviderDisabled(String s)
			{
			}
		};
	}

	public boolean getLocation(Context context,
			LocationResultListener locationListener)
	{
		mLocationResultListener = locationListener;

		if (mLocationManager == null)
			mLocationManager = (LocationManager) context
					.getSystemService(Context.LOCATION_SERVICE);

		try
		{
			mGpsEnabled = mLocationManager
					.isProviderEnabled(LocationManager.GPS_PROVIDER);
		}
		catch (Exception ex)
		{
		}

		try
		{
			mNetworkEnabled = mLocationManager
					.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		}
		catch (Exception ex)
		{
		}

		Criteria criteriaFine = new Criteria();
		criteriaFine.setAltitudeRequired(false);
		criteriaFine.setBearingRequired(false);
		criteriaFine.setCostAllowed(false);
		criteriaFine.setPowerRequirement(Criteria.POWER_LOW);
		criteriaFine.setSpeedRequired(true);
		criteriaFine.setAccuracy(Criteria.ACCURACY_FINE);
		criteriaFine.setSpeedAccuracy(Criteria.ACCURACY_FINE);
		String providerFine = mLocationManager.getBestProvider(criteriaFine,
				true);

		Criteria criteriaCoarse = new Criteria();
		criteriaCoarse.setAltitudeRequired(false);
		criteriaCoarse.setBearingRequired(false);
		criteriaCoarse.setCostAllowed(false);
		criteriaCoarse.setPowerRequirement(Criteria.POWER_LOW);
		criteriaCoarse.setSpeedRequired(true);
		criteriaCoarse.setAccuracy(Criteria.ACCURACY_COARSE);
		criteriaCoarse.setSpeedAccuracy(Criteria.ACCURACY_COARSE);
		String providerCoarse = mLocationManager.getBestProvider(
				criteriaCoarse, true);

		/**
		 * if none are available, there is no way to get the location
		 */
		if (!mGpsEnabled && !mNetworkEnabled)
			return false;

		if (mGpsEnabled && providerFine != null)
			mLocationManager.requestLocationUpdates(providerFine, 0, 0,
					mGpsLocationListener);

		if (mNetworkEnabled && providerCoarse != null)
			mLocationManager.requestLocationUpdates(providerCoarse, 0, 0,
					mNetworkLocationListener);

		/**
		 * set a timer that will fire in 20 seconds but only if we can't get the
		 * current location. Otherwise it will be cancelled.
		 */
		mTimer = new Timer();
		mTimer.schedule(new LastLocationFetcher(), 1000);
		return true;
	}

	public void stop()
	{
		if (mTimer != null)
			mTimer.cancel();

		mLocationManager.removeUpdates(mGpsLocationListener);
		mLocationManager.removeUpdates(mNetworkLocationListener);
	}

	private class LastLocationFetcher extends TimerTask
	{

		@Override
		public void run()
		{

			mLocationManager.removeUpdates(mGpsLocationListener);
			mLocationManager.removeUpdates(mNetworkLocationListener);

			Location gpsLoc = null, netLoc = null;
			if (mGpsEnabled)
				gpsLoc = mLocationManager
						.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			if (mNetworkEnabled)
				netLoc = mLocationManager
						.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

			if (gpsLoc != null && netLoc != null)
			{
				if (gpsLoc.getTime() > netLoc.getTime())
					mLocationResultListener.onLocationResultAvailable(gpsLoc);
				else
					mLocationResultListener.onLocationResultAvailable(netLoc);
				return;
			}

			if (gpsLoc != null)
			{
				mLocationResultListener.onLocationResultAvailable(gpsLoc);
				return;
			}

			if (netLoc != null)
			{
				mLocationResultListener.onLocationResultAvailable(netLoc);
				return;
			}

			mLocationResultListener.onLocationResultAvailable(null);
		}
	}

}
