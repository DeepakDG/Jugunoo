package com.hirecraft.jugunoo.passenger.common;

public class Constant
{

	/**
	 * Webservice Result state Result = PASS/FAIL
	 */

	public static final String RESULT = "Result";
	public static final String MESSAGE = "Message";

	public static final String network_err_msg = "Not able to connect, please try again.";

	public static final class Result_STATE
	{
		public static final String PASS = "Pass";
		public static final String FAIL = "Fail";
	}

	public static final class MessageState
	{

		public static final int CAB_SUCCESS = 1;
		public static final int CAB_FAIL = 2;

		// logout
		public static final int LOGOUT_SUCCESS = 3;
		public static final int LOGOUT_FAIL = 4;

		// cancel tip
		public static final int TRIP_CANCEL_SUCCESS = 5;
		public static final int TRIP_CANCEL_FAIL = 6;

		// trip direction
		public static final int TRIP_DIRECTION_SUCCESS = 7;
		public static final int TRIP_DIRECTION_FAIL = 8;

		// driver photo
		public static final int DRIVER_PHOTO_SUCCESS = 9;
		public static final int DRIVER_PHOTO_FAIL = 10;

		// map pick and drop route plot
		public static final int MAP_PLOT_SUCCESS = 11;
		public static final int MAP_PLOT_FAIL = 12;

		// fetch my address
		public static final int MY_ADDRESS_SUCCESS = 13;
		public static final int MY_ADDRESS_FAIL = 14;

		public static final int FLEET_OPERATOR_SUCCESS = 15;
		public static final int FLEET_OPERATOR_FAIL = 16;

		public static final int FLEET_GROUP_SUCCESS = 17;
		public static final int FLEET_GROUP_FAIL = 18;

		public static final int FLEET_USERS_SUCCESS = 19;
		public static final int FLEET_USERS_FAIL = 20;

		public static final int ADD_FLEET_GROUP_SUCCESS = 21;
		public static final int ADD_FLEET_GROUP_FAIL = 22;

		public static final int ADD_FLEET_USER_SUCCESS = 23;
		public static final int ADD_FLEET_USER_FAIL = 24;

		public static final int USER_PROFILE_SUCCESS = 25;
		public static final int USER_PROFILE_FAIL = 26;

		public static final int CHANGE_PWD_SUCCESS = 27;
		public static final int CHANGE_PWD_FAIL = 28;

		public static final int UPDATE_FLEET_SUCCESS = 29;
		public static final int UPDATE_FLEET_FAIL = 30;

		// Constants of TripLogs
		public static final int TRIP_LOGS_AVAILABLE = 33;
		public static final int TRIP_LOGS_UNAVAILABLE = 34;
		public static final int TRIPLOG_CANCEL_SUCCESS = 35;
		public static final int TRIPLOG_CANCEL_FAILED = 36;

		public static final int USERNAMES_SUCCESS = 37;
		public static final int USERNAMES_FAIL = 38;

		// get booking load details
		public static final int BOOKING_LOAD_SUCCESS = 39;
		public static final int BOOKING_LOAD_FAIL = 40;

		// fleet book
		public static final int FLEET_BOOK_SUCCESS = 41;
		public static final int FLEET_BOOK_FAIL = 42;

		// get cab type
		public static final int CAB_TYPE_SUCCESS = 43;
		public static final int CAB_TYPE_FAIL = 44;

		// get booking details
		public static final int GET_BOOKING_DETAILS_SUCCESS = 45;
		public static final int GET_BOOKING_DETAILS_FAIL = 46;

		// update fleet book
		public static final int FLEET_BOOK_UPDATE_SUCCESS = 47;
		public static final int FLEET_BOOK_UPDATE_FAIL = 48;

		// get cab type update
		public static final int CAB_TYPE_UPDATE_SUCCESS = 49;
		public static final int CAB_TYPE_UPDATE_FAIL = 50;

		public static final int FAIL = 0;

		// Login
		public static final int LOGIN_SUCCESS = 51;

		// DEVICE_REGISTRATION
		public static final int DEVICE_REGISTRATION_SUCCESS = 52;

		// Passenger REGISTRATION
		public static final int REGISTRATION_SUCCESS = 53;

		// Verify Mobile Number
		public static final int VERIFY_NUMBER_SUCCESS = 54;

		// Verify OTP
		public static final int OTP_SUCCESS = 55;

		// Check AVAILABILITY of MAIL & NUMBER
		public static final int AVAILABILITY_MAIL_NUMBER_SUCCESS = 56;

		// check user details available in shared preference
		public static final int CHECK_USER_SUCCESS = 57;

		// Feedback success
		public static final int FORGOT_PASSWORD_SUCCESS = 58;

		// Forgot password success
		public static final int FEEDBACK_SUCCESS = 59;

		// Trip log export
		public static final int EXPORT_TRIPLOG_SUCCESS = 60;

		// To get Active Fleet Operators by Groups
		public static final int GROUP_FLEET_SUCCESS = 61;

		public static final int PLACE_LIST_GET_CAB_SUCCESS = 62;

		// To get Company Names while registration
		public static final int COMPANY_NAMES_SUCCESS = 63;

		// To get Group Names while registration
		public static final int GROUP_NAMES_SUCCESS = 64;

		// To get pending Group Users
		public static final int USERGROUP_STATUS_SUCCESS = 65;

		// To get favorite drivers
		public static final int FAVORITEDRIVERS_STATUS_SUCCESS = 66;

		// pending User Group status
		public static final int PENDINGUSERS_STATUS_SUCCESS = 67;

		// Pending request count
		public static final int PENDINGREQUESTCOUNT_STATUS_SUCCESS = 68;

		// Get cab basic tariff details
		public static final int BASIC_TARIFF_SUCCESS = 69;
		public static final int BASIC_TARIFF_FAIL = 70;
		
		//Update profiles
		public static final int UPDATEPROFILE_STATUS_SUCCESS=71;
		public static final int UPDATEPROFILE_STATUS_FAIL=72;
	}

	// notification
	public static final class NotificationState
	{

		public static String NOTIFY_STATUS;

	}

	// sharedpref keys
	public static final String USER_TYPE = "FleetUserType";

	public static final String ADMIN = "Admin";
	public static final String GROUP_USER = "GroupUser";
	public static final String MANAGER = "Manager";

	public static final String PREFERENCE_INDEX = "prefIndex";

}
