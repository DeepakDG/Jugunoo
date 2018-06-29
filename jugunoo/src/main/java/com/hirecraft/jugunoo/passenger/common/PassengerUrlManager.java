package com.hirecraft.jugunoo.passenger.common;

public final class PassengerUrlManager
{

	private final static boolean isDebugMode = true;

	// location api
	private static final String LOCATION_PRODUCTION_URL = "http://location.jugunoo.com/";
	private static final String LOCATION_DEBUG_URL = "http://location.jugunoo.com/uat/";

	// user api
	private static final String USER_PRODUCTION_URL = "http://api.jugunoo.com/";
	private static final String USER_DEBUG_URL = "http://api.jugunoo.com/uat/";

	public static String getLocationUrl()
	{

		return isDebugMode ? LOCATION_DEBUG_URL : LOCATION_PRODUCTION_URL;

	}

	public static String getUserApiUrl()
	{

		return isDebugMode ? USER_DEBUG_URL : USER_PRODUCTION_URL;
	}

}
