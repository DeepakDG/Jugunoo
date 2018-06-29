package com.hirecraft.jugunoo.passenger;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.fourmob.datetimepicker.date.DatePickerDialog.OnDateSetListener;
import com.google.gson.Gson;
import com.hirecraft.jugunoo.passenger.common.City;
import com.hirecraft.jugunoo.passenger.common.Constant;
import com.hirecraft.jugunoo.passenger.common.ConstantMessages;
import com.hirecraft.jugunoo.passenger.common.FleetGroup;
import com.hirecraft.jugunoo.passenger.common.Function;
import com.hirecraft.jugunoo.passenger.common.Global;
import com.hirecraft.jugunoo.passenger.common.TransparentProgressDialog;
import com.hirecraft.jugunoo.passenger.networkhandler.NetworkHandler;
import com.hirecraft.jugunoo.passenger.utility.SharedPreferencesManager;
import com.sleepbot.datetimepicker.time.RadialPickerLayout;
import com.sleepbot.datetimepicker.time.TimePickerDialog;

public class BookingFormNewActivity extends FragmentActivity implements
		OnDateSetListener, TimePickerDialog.OnTimeSetListener, OnClickListener,
		OnCheckedChangeListener
{
	private Spinner spSelectCity;
	private Spinner spBillTo;
	private Spinner spCabType;

	private EditText etStartDate;
	private EditText etEndDate;
	private EditText etStartTime;
	private EditText etEndTime;
	private EditText etPickupPoint;
	private EditText etDropPoint;
	private AutoCompleteTextView username;

	private Button btProceed;
	private Button btCancel;

	public static final String START_DATE_PICKERTAG = "startDatePicker";
	public static final String END_DATE_PICKERTAG = "endDatePicker";
	public static final String START_TIME_PICKERTAG = "startTimepicker";
	public static final String DROP_TIME_PICKER = "dropTimepicker";

	private Calendar calendar;
	private DatePickerDialog datePickerDialog;
	private TimePickerDialog timePickerDialog;

	private int startDay;
	private int startMonth;
	private int startYear;

	private int endDay;
	private int endMonth;
	private int endYear;

	private int hourOfDay, endHourOfDay;
	private int minute, endMinute;
	private int pref;
	// private long timeInMillis;

	private TransparentProgressDialog pd;
	private Handler pdHandler;
	private Runnable pdRunnable;
	private SharedPreferencesManager mgr;

	private String pickLatLng, dropLatLng;
	private String cabType = "";

	private ProgressBar pbCity, pbGroup, pbCabType;

	private CheckBox cbOnBehalf;
	private LinearLayout llOnBehalf, llChkBox;
	private ListView nameList;
	private boolean isGetName;
	private String usernameStr, behalfValue = "0";

	private ArrayList<HashMap<String, String>> names;
	private static final String TAG = BookingFormNewActivity.class
			.getSimpleName();

	private Date now;

	private RelativeLayout rlBookingForm;
	private LinearLayout llRetry;
	private Button btRetry;
	private Dialog dialog;

	private LinearLayout llCabs;
	private RelativeLayout rlPickPoint, rlDropPoint;

	private String startDate = "";
	private String endDate = "";

	// private boolean validateStartDateTimeStatus = false;
	// private boolean validateEndDateTimeStatus = false;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		SetActionBar();

		setContentView(R.layout.activity_booking_form_new);
		dialog = new Dialog(BookingFormNewActivity.this);

		calendar = Calendar.getInstance();

		timePickerDialog = TimePickerDialog.newInstance(this,
				calendar.get(Calendar.HOUR_OF_DAY),
				calendar.get(Calendar.MINUTE), true, false);

		mgr = new SharedPreferencesManager(this);

		initUiElement();

		manageView();

		bookingInit();

		Intent intent = getIntent();
		etPickupPoint.setText(intent.getStringExtra("addr"));
		pickLatLng = intent.getStringExtra("pickLatLng");
	}

	private void bookingInit()
	{
		try
		{
			showLoadingDilog();
			Log.e(TAG, "user id" + mgr.GetValueFromSharedPrefs("UserID"));
			String userId = mgr.GetValueFromSharedPrefs("UserID");
			int prefIndex = mgr.getPreferenceIndex(Constant.PREFERENCE_INDEX);

			NetworkHandler.getBookingInit(TAG, handler, userId, prefIndex);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	private void manageView()
	{
		// llPickTime.setBackgroundColor(getResources().getColor(
		// R.color.light_gray));
		// llDropDate.setBackgroundColor(getResources().getColor(
		// R.color.light_gray));
		// llDropTime.setBackgroundColor(getResources().getColor(
		// R.color.light_gray));
		// llCabs.setBackgroundColor(getResources().getColor(R.color.light_gray));
		// rlPickPoint.setBackgroundColor(getResources().getColor(
		// R.color.light_gray));
		// rlDropPoint.setBackgroundColor(getResources().getColor(
		// R.color.light_gray));
		btProceed.setBackgroundColor(getResources().getColor(
				R.color.light_green));

		etStartTime.setEnabled(false);
		etEndDate.setEnabled(false);
		etEndTime.setEnabled(false);
		spCabType.setEnabled(false);
		etPickupPoint.setEnabled(false);
		etDropPoint.setEnabled(false);
		cbOnBehalf.setEnabled(false);
		btProceed.setEnabled(false);

		llCabs.setEnabled(false);
		rlPickPoint.setEnabled(false);
		rlDropPoint.setEnabled(false);
	}

	private void SetActionBar()
	{
		ActionBar actionBar = getActionBar();
		ActionBar.LayoutParams layoutParams = new ActionBar.LayoutParams(
				ActionBar.LayoutParams.MATCH_PARENT,
				ActionBar.LayoutParams.MATCH_PARENT);
		layoutParams.gravity = Gravity.CENTER_HORIZONTAL
				| Gravity.CENTER_VERTICAL;

		RelativeLayout l = new RelativeLayout(this);
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View bar = inflater.inflate(R.layout.custom_title_actionbar, l);

		LinearLayout back = (LinearLayout) bar.findViewById(R.id.llCTitle);
		TextView title = (TextView) bar.findViewById(R.id.tvTATitle);
		title.setText("Booking Form");

		back.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				onBackPressed();
			}
		});

		actionBar.setCustomView(bar, layoutParams);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowCustomEnabled(true);
		actionBar.setHomeButtonEnabled(false);
	}

	@Override
	public void onBackPressed()
	{
		super.onBackPressed();
		BookingFormNewActivity.this.finish();
		startActivity(new Intent().setClass(BookingFormNewActivity.this,
				LandingPage.class));
	}

	private void initUiElement()
	{
		rlBookingForm = (RelativeLayout) findViewById(R.id.rlBookingForm);
		llRetry = (LinearLayout) findViewById(R.id.llRetry);
		btRetry = (Button) findViewById(R.id.booking_retrybtn);

		pbCity = (ProgressBar) findViewById(R.id.pbCity);
		pbGroup = (ProgressBar) findViewById(R.id.pbGroup);
		pbCabType = (ProgressBar) findViewById(R.id.pbCabType);
		llOnBehalf = (LinearLayout) findViewById(R.id.llOnBehalf);
		llChkBox = (LinearLayout) findViewById(R.id.llChkBox);

		nameList = (ListView) findViewById(R.id.nameList);

		cbOnBehalf = (CheckBox) findViewById(R.id.cbOnBehalf);
		cbOnBehalf.setOnCheckedChangeListener(this);

		// llPickTime = (LinearLayout) findViewById(R.id.llPickTime);
		// llDropTime = (LinearLayout) findViewById(R.id.llDropTime);
		llCabs = (LinearLayout) findViewById(R.id.llCabs);
		rlPickPoint = (RelativeLayout) findViewById(R.id.rlPickPoint);
		rlDropPoint = (RelativeLayout) findViewById(R.id.rlDropPoint);
		// llDropDate = (LinearLayout) findViewById(R.id.llDropDate);

		final String userType = mgr
				.GetNotiValueFromSharedPrefs(Constant.USER_TYPE);

		if (!TextUtils.isEmpty(userType)
				&& (userType.equalsIgnoreCase(Constant.ADMIN) || userType
						.equalsIgnoreCase(Constant.MANAGER)))
		{
			llChkBox.setVisibility(View.VISIBLE);
		}
		else
		{
			llChkBox.setVisibility(View.GONE);
		}

		pdHandler = new Handler();
		pd = new TransparentProgressDialog(BookingFormNewActivity.this,
				R.drawable.loading_image);

		spSelectCity = (Spinner) findViewById(R.id.spCity);

		spSelectCity
				.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
				{
					@Override
					public void onItemSelected(AdapterView<?> arg0, View arg1,
							int arg2, long arg3)
					{
						makeGetCabRequest(false);
					}

					@Override
					public void onNothingSelected(AdapterView<?> parent)
					{
					}
				});

		spSelectCity.setAdapter(populateSpinnerGeneric(new City[0]));

		spBillTo = (Spinner) findViewById(R.id.spBillTo);
		spBillTo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
		{
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3)
			{
				makeGetCabRequest(false);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent)
			{
			}
		});

		spBillTo.setAdapter(populateSpinnerGeneric(new FleetGroup[0]));

		spCabType = (Spinner) findViewById(R.id.spCabType);

		String noCabs[] =
		{ "Cab Type not available" };
		spCabType.setAdapter(populateSpinnerGeneric(noCabs));
		etStartDate = (EditText) findViewById(R.id.etStartDate);
		etStartTime = (EditText) findViewById(R.id.etStartTime);
		etEndDate = (EditText) findViewById(R.id.etEndDate);
		etEndTime = (EditText) findViewById(R.id.etEndTime);

		etPickupPoint = (EditText) findViewById(R.id.etPickupPoint);
		etPickupPoint.setOnClickListener(this);
		etDropPoint = (EditText) findViewById(R.id.etDropPoint);

		username = (AutoCompleteTextView) findViewById(R.id.acAdUserName);
		username.addTextChangedListener(userIDWatcher);

		pref = mgr.getPreferenceIndex(Constant.PREFERENCE_INDEX);

		etDropPoint.setOnClickListener(this);

		if (pref == 0)
		{
			rlDropPoint.setVisibility(View.VISIBLE);
		}
		else
		{
			rlDropPoint.setVisibility(View.GONE);
		}

		btProceed = (Button) findViewById(R.id.btProceed);
		btProceed.setOnClickListener(this);

		btCancel = (Button) findViewById(R.id.btCancel);
		btCancel.setOnClickListener(this);

		// btProceed.setOnClickListener(new OnClickListener() {
		// @Override
		// public void onClick(View v) {
		// if (etStartDate.getText().toString().length() != 0) {
		// if (etTime.getText().toString().length() != 0) {
		//
		// } else {
		// showErrorMessage(getApplicationContext(),
		// rlErrorMessage, tvErrorMessage,
		// "Please enter time");
		// }
		// } else {
		// showErrorMessage(getApplicationContext(), rlErrorMessage,
		// tvErrorMessage, "Please enter start date");
		// }
		// }
		// });

		etStartDate.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				// hidekeys added3
				// datePickerDialog.setFirstDayOfWeek(calendar.get(Calendar.DAY_OF_WEEK));

				if (startYear == 0)
				{
					datePickerDialog = DatePickerDialog.newInstance(
							BookingFormNewActivity.this,
							calendar.get(Calendar.YEAR),
							calendar.get(Calendar.MONTH),
							calendar.get(Calendar.DAY_OF_MONTH), false);
				}
				else
				{
					datePickerDialog = DatePickerDialog.newInstance(
							BookingFormNewActivity.this, startYear, startMonth,
							startDay, false);
				}

				datePickerDialog.setYearRange(calendar.get(Calendar.YEAR),
						calendar.get(Calendar.YEAR) + 1);
				// datePickerDialog
				// .setCloseOnSingleTapDay(isCloseOnSingleTapDay());
				datePickerDialog.show(getSupportFragmentManager(),
						START_DATE_PICKERTAG);
			}
		});

		etEndDate.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				// hidekeys added3
				// datePickerDialog.setFirstDayOfWeek(calendar.get(Calendar.DAY_OF_WEEK));

				if (endYear == 0)
				{
					datePickerDialog = DatePickerDialog.newInstance(
							BookingFormNewActivity.this,
							calendar.get(Calendar.YEAR),
							calendar.get(Calendar.MONTH),
							calendar.get(Calendar.DAY_OF_MONTH), false);
				}
				else
				{
					datePickerDialog = DatePickerDialog.newInstance(
							BookingFormNewActivity.this, endYear, endMonth,
							endDay, false);
				}

				datePickerDialog.setYearRange(calendar.get(Calendar.YEAR),
						calendar.get(Calendar.YEAR) + 1);
				// datePickerDialog
				// .setCloseOnSingleTapDay(isCloseOnSingleTapDay());
				datePickerDialog.show(getSupportFragmentManager(),
						END_DATE_PICKERTAG);
			}
		});

		etStartTime.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				// timePickerDialog
				// .setCloseOnSingleTapMinute(isCloseOnSingleTapMinute());

				if (hourOfDay == 0)
				{
					timePickerDialog.setStartTime(
							calendar.get(Calendar.HOUR_OF_DAY),
							calendar.get(Calendar.MINUTE));
				}
				else
				{
					timePickerDialog.setStartTime(hourOfDay, minute);
				}

				timePickerDialog.show(getSupportFragmentManager(),
						START_TIME_PICKERTAG);

			}
		});

		etEndTime.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (endHourOfDay == 0)
				{
					timePickerDialog.setStartTime(
							calendar.get(Calendar.HOUR_OF_DAY),
							calendar.get(Calendar.MINUTE));
				}
				else
				{
					timePickerDialog.setStartTime(endHourOfDay, endMinute);
				}

				timePickerDialog.show(getSupportFragmentManager(),
						DROP_TIME_PICKER);
			}
		});
	}

	private TextWatcher userIDWatcher = new TextWatcher()
	{
		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count)
		{
			usernameStr = username.getText().toString();

			if (count % 2 == 1)
			{
				handler.removeMessages(JUGUNOO_SEARCH_USER);
				handler.sendEmptyMessageDelayed(JUGUNOO_SEARCH_USER,
						JUGUNOO_DELAY_IN_MILLIS);
			}
			if (usernameStr.length() < 1)
			{
				Function.showToast(BookingFormNewActivity.this,
						"Enter a valid Mobile Number");
			}
			if (usernameStr.length() == 10)
			{
				Function.hideSoftKeyBoard(BookingFormNewActivity.this);
			}
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after)
		{
		}

		@Override
		public void afterTextChanged(Editable s)
		{
		}
	};

	private <A> ArrayAdapter<A> populateSpinnerGeneric(A[] list)
	{
		ArrayAdapter<A> dataAdapter = new ArrayAdapter<A>(this,
				android.R.layout.simple_spinner_item, list);
		dataAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		return dataAdapter;
	}

	private final int JUGUNOO_SEARCH_USER = 12;
	private final int JUGUNOO_DELAY_IN_MILLIS = 1000;

	Handler handler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			Log.i(TAG, "Handler()=" + msg.arg1);
			switch (msg.arg1)
			{
				case Constant.MessageState.BOOKING_LOAD_SUCCESS:

					manageView(true);
					cancelLoadingDialog();
					pbCity.setVisibility(View.GONE);
					pbGroup.setVisibility(View.GONE);

					JSONObject jsonObj = (JSONObject) msg.obj;
					parseBookingDetails(jsonObj);
					break;

				case Constant.MessageState.BOOKING_LOAD_FAIL:

					manageView(false);

					btRetry.setOnClickListener(new View.OnClickListener()
					{
						@Override
						public void onClick(View v)
						{
							bookingInit();
						}
					});

					cancelLoadingDialog();
					pbCity.setVisibility(View.GONE);
					pbGroup.setVisibility(View.GONE);
					break;

				case Constant.MessageState.FLEET_BOOK_SUCCESS:
					cancelLoadingDialog();
					pbCabType.setVisibility(View.GONE);
					JSONObject jsonFleetObj = (JSONObject) msg.obj;
					parseFleetBooking(jsonFleetObj);
					break;

				case Constant.MessageState.FLEET_BOOK_FAIL:
					cancelLoadingDialog();
					pbCabType.setVisibility(View.GONE);
					break;

				case Constant.MessageState.CAB_TYPE_SUCCESS:
					pbCabType.setVisibility(View.GONE);
					JSONObject jsoncabTypeObj = (JSONObject) msg.obj;
					Log.e(TAG, "cabty=" + jsoncabTypeObj.toString());
					parseReqCabType(jsoncabTypeObj);
					break;

				case Constant.MessageState.CAB_TYPE_FAIL:
					pbCabType.setVisibility(View.GONE);
					break;

				case Constant.MessageState.USERNAMES_SUCCESS:
					JSONObject result = (JSONObject) msg.obj;
					parseUserListFromMobile(result);
					break;

				case Constant.MessageState.USERNAMES_FAIL:
					nameList.setVisibility(View.GONE);
					break;

				default:
					break;
			}

			if (msg.what == JUGUNOO_SEARCH_USER)
			{
				int groupId = 0;
				if (!spBillTo.getAdapter().isEmpty())
				{
					// group = ((FleetGroup)
					// spBillTo.getSelectedItem()).getGroupName();
					groupId = ((FleetGroup) spBillTo.getSelectedItem())
							.getRID();
				}

				String userId = mgr.GetValueFromSharedPrefs("UserID");

				String url = Global.JUGUNOO_WS
						+ "Passenger/GetNameGroup?Mobile=" + usernameStr
						+ "&UserId=" + userId + "&GroupId=" + groupId;
				setUrl(url);

				Log.i("TAG", "GetNameForGroup -->" + url);
			}
		}
	};

	private void setUrl(String url)
	{
		if (isGetName == false)
		{
			handler.removeCallbacksAndMessages(null);
			NetworkHandler.GetUserNames(TAG, handler, url);
		}
	}

	private void manageView(boolean status)
	{
		if (status)
		{
			rlBookingForm.setVisibility(View.VISIBLE);
			llRetry.setVisibility(View.GONE);
		}
		else
		{
			rlBookingForm.setVisibility(View.GONE);
			llRetry.setVisibility(View.VISIBLE);
		}
	}

	private void showLoadingDilog()
	{
		// Showing loading dialog
		try
		{
			pdRunnable = new Runnable()
			{
				@Override
				public void run()
				{
					if (pd != null)
					{
						if (pd.isShowing())
						{
							pd.dismiss();
						}
					}
				}
			};
			pd.show();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void cancelLoadingDialog()
	{
		// Cancel loading dialog
		try
		{
			pdHandler.removeCallbacks(pdRunnable);
			if (pd.isShowing())
			{
				pd.dismiss();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void parseFleetBooking(JSONObject jsonObj)
	{
		try
		{
			Log.e(TAG, "parseFleetBooking" + " " + jsonObj.toString());

			if (jsonObj.has(Constant.RESULT))
			{
				String result = jsonObj.getString(Constant.RESULT);
				if (result.equalsIgnoreCase(Constant.Result_STATE.PASS))
				{
					JugunooInteractiveDialog(ConstantMessages.MSG91,
							ConstantMessages.MSG52);
				}
				else
				{
					Function.showToast(BookingFormNewActivity.this,
							jsonObj.getString("Message"));
				}
			}
		}
		catch (Exception bug)
		{
			bug.printStackTrace();
		}
	}

	private void parseBookingDetails(JSONObject jsonObj)
	{
		try
		{
			Log.e(TAG, "parseBookingInit" + " " + jsonObj.toString());

			if (jsonObj.has(Constant.RESULT))
			{
				String result = jsonObj.getString(Constant.RESULT);
				if (result.equalsIgnoreCase(Constant.Result_STATE.PASS))
				{
					Gson gson = new Gson();
					if (jsonObj.has("Citys"))
					{
						City[] citys = gson.fromJson(
								jsonObj.getString("Citys"), City[].class);

						spSelectCity.setAdapter(populateSpinnerGeneric(citys));
					}

					if (jsonObj.has("Groups"))
					{
						FleetGroup[] fleetGrps = gson
								.fromJson(jsonObj.getString("Groups"),
										FleetGroup[].class);

						spBillTo.setAdapter(populateSpinnerGeneric(fleetGrps));
					}
				}
			}
		}
		catch (Exception bug)
		{
			bug.printStackTrace();
		}
	}

	private void parseReqCabType(JSONObject jsonObj)
	{
		try
		{
			Log.e(TAG, "parseBookingInit" + " " + jsonObj.toString());

			if (jsonObj.has(Constant.RESULT))
			{
				String result = jsonObj.getString(Constant.RESULT);
				if (result.equalsIgnoreCase(Constant.Result_STATE.PASS))
				{

					Gson gson = new Gson();
					if (jsonObj.has("CabType"))
					{

						String[] cabType = gson.fromJson(
								jsonObj.getString("CabType"), String[].class);

						if (cabType.length != 0)
						{
							spCabType
									.setAdapter(populateSpinnerGeneric(cabType));

							cbOnBehalf.setEnabled(true);

							// llCabs.setBackgroundColor(getResources().getColor(
							// R.color.light_blue));
							llCabs.setEnabled(true);
							spCabType.setEnabled(true);

							// rlPickPoint.setBackgroundColor(getResources()
							// .getColor(R.color.light_blue));

							rlPickPoint.setEnabled(true);
							etPickupPoint.setEnabled(true);

							if (pref == 0
									&& !TextUtils.isEmpty(etPickupPoint
											.getText().toString()))
							{
								// rlDropPoint.setBackgroundColor(getResources()
								// .getColor(R.color.light_blue));
								rlDropPoint.setEnabled(true);
								etDropPoint.setEnabled(true);
							}
							else
							{
								if (!TextUtils.isEmpty(etPickupPoint.getText()
										.toString()))
								{
									btProceed
											.setBackgroundResource(R.drawable.selector_button);
									btProceed.setEnabled(true);
								}
							}
						}
					}

				}
				else
				{
					Function.showToast(BookingFormNewActivity.this,
							jsonObj.getString("Message"));

					String noCabs[] =
					{ "Cab Type not available" };
					spCabType.setAdapter(populateSpinnerGeneric(noCabs));

					hideCabView();
				}
			}
		}
		catch (Exception bug)
		{
			bug.printStackTrace();
		}
	}

	private void parseUserListFromMobile(JSONObject result)
	{
		Log.e(TAG, "user list = " + result);

		if (result != null)
		{
			names = new ArrayList<HashMap<String, String>>();
			try
			{
				String resultStr = result.getString("Result");
				if (!resultStr.equalsIgnoreCase("Fail"))
				{
					JSONArray array = result.getJSONArray("UserArray");
					int len = array.length();
					if (len != 0)
					{
						for (int f = 0; f < len; f++)
						{
							JSONObject obj = array.getJSONObject(f);
							String grName = obj.getString("FirstName");
							// String rid = obj.getString("RID");
							String rid = obj.getString("Mobile");
							String status = obj.getString("Status");

							if (!TextUtils.isEmpty(grName)
									&& !TextUtils.isEmpty(rid))
							{
								HashMap<String, String> fetchData = new HashMap<String, String>();
								fetchData.put("FirstName", grName);
								fetchData.put("RID", rid);
								fetchData.put("Status", status);
								names.add(fetchData);
							}
							// nameList.setVisibility(View.VISIBLE);

							SimpleAdapter adapter = new SimpleAdapter(
									BookingFormNewActivity.this, names,
									R.layout.username_row, new String[]
									{ "FirstName", "RID", "Status" }, new int[]
									{ R.id.name, R.id.rid, R.id.userStatus });
							username.setAdapter(adapter);

							username.showDropDown();

							username.setOnItemClickListener(new OnItemClickListener()
							{
								@Override
								public void onItemClick(AdapterView<?> parent,
										View view, int position, long id)
								{
									String grName = ((TextView) view
											.findViewById(R.id.name)).getText()
											.toString();
									String rid = ((TextView) view
											.findViewById(R.id.rid)).getText()
											.toString();
									String status = ((TextView) view
											.findViewById(R.id.userStatus))
											.getText().toString();

									if (status.equalsIgnoreCase("N"))
									{
										alertDialog();
									}

									username.setText(grName);
									behalfValue = rid;
									// managingUserId = rid;
									Log.i("tag", "grName: " + grName + ", rid"
											+ rid);
									nameList.setVisibility(View.GONE);

									Function.hideSoftKeyBoard(BookingFormNewActivity.this);
								}
							});
						}
					}
					else
					{
						Log.i("UserName Listener", "No user available");
					}
				}
				else
				{
					Log.i("UserName Listener", "No user available");
					names = null;
				}
			}
			catch (Exception bug)
			{
				bug.printStackTrace();
			}
		}
	}

	private void JugunooInteractiveDialog(String title, String message)
	{
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.dialog_common_alert);
		dialog.setCancelable(false);

		TextView tvTitle = (TextView) dialog.findViewById(R.id.tvAlertHeader);
		TextView tvMsg = (TextView) dialog.findViewById(R.id.tvAlertMsg);
		Button btOk = (Button) dialog.findViewById(R.id.btAlertOk);

		tvMsg.setText(message);
		tvTitle.setText(title);
		btOk.setText("Ok");

		btOk.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				dialog.dismiss();
				Intent homeIntent = new Intent(BookingFormNewActivity.this,
						LandingPage.class);
				homeIntent.addFlags(Intent.FLAG_ACTIVITY_TASK_ON_HOME);
				startActivity(homeIntent);
				finish();
			}
		});
		dialog.show();
	}

	private void alertDialog()
	{
		/* Confirmation dialog */

		final Dialog dialog = new Dialog(this);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.custom_alert_dialog);

		TextView popupMsg = (TextView) dialog
				.findViewById(R.id.tvAlertTitleDesc);
		Button btNegative = (Button) dialog.findViewById(R.id.btNegative);
		Button btPositive = (Button) dialog.findViewById(R.id.btPositive);

		popupMsg.setText(R.string.bf_alert_title);
		btNegative.setText(R.string.bf_cancel);
		btPositive.setText(R.string.bf_proceed);

		btNegative.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				dialog.cancel();
				finish();
			}
		});

		btPositive.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				dialog.cancel();
			}
		});

		dialog.show();
	}

	private void hideCabView()
	{
		// llCabs.setBackgroundColor(getResources().getColor(R.color.light_gray));
		llCabs.setEnabled(true);
		spCabType.setEnabled(false);

		// rlPickPoint.setBackgroundColor(getResources().getColor(
		// R.color.light_gray));
		etPickupPoint.setEnabled(false);

		// rlDropPoint.setBackgroundColor(getResources().getColor(
		// R.color.light_gray));
		etDropPoint.setEnabled(false);
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
	{
		if (isChecked)
		{
			llOnBehalf.setVisibility(View.VISIBLE);
		}
		else
		{
			llOnBehalf.setVisibility(View.GONE);
		}
	}

	@Override
	public void onClick(View view)
	{
		switch (view.getId())
		{
			case R.id.btProceed:
				attemptBooking();
				break;

			case R.id.btCancel:

				InputMethodManager imm = (InputMethodManager) view.getContext()
						.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

				Intent homeIntent = new Intent(this, LandingPage.class);
				homeIntent.addFlags(Intent.FLAG_ACTIVITY_TASK_ON_HOME);
				startActivity(homeIntent);
				finish();
				break;

			case R.id.nav_icon:
				Intent home = new Intent(this, LandingPage.class);
				home.addFlags(Intent.FLAG_ACTIVITY_TASK_ON_HOME);
				startActivity(home);
				finish();
				break;

			case R.id.etPickupPoint:

				Intent getAddressIntentPick = new Intent(this,
						JugunooPlaceList.class);
				getAddressIntentPick.putExtra("form", "1");
				getAddressIntentPick.putExtra("formCat", "pick");
				startActivityForResult(getAddressIntentPick, 200);

				break;
			case R.id.etDropPoint:

				Intent getAddressIntentDrop = new Intent(this,
						JugunooPlaceList.class);
				getAddressIntentDrop.putExtra("form", "1");
				getAddressIntentDrop.putExtra("formCat", "drop");
				startActivityForResult(getAddressIntentDrop, 202);

				break;

			default:
				break;
		}
	}

	private void attemptBooking()
	{
		try
		{
			String city = "";
			Integer cityId = -1;
			if (!spSelectCity.getAdapter().isEmpty())
			{
				city = ((City) spSelectCity.getSelectedItem()).getCity();
				cityId = ((City) spSelectCity.getSelectedItem()).getRID();
			}

			Integer groupId = -1;
			if (!spBillTo.getAdapter().isEmpty())
			{
				groupId = ((FleetGroup) spBillTo.getSelectedItem()).getRID();
			}

			String pickPointVal = etPickupPoint.getText().toString();
			String userOnBehalf = username.getText().toString();
			String dropPointVal;
			if (pref == 0)
			{
				dropPointVal = etDropPoint.getText().toString();
			}
			else
			{
				dropPointVal = "";
			}
			
			if (!spCabType.getAdapter().isEmpty())
			{
				cabType = spCabType.getSelectedItem().toString();
			}

			// now = Calendar.getInstance().getTime();
			SimpleDateFormat dateFormatter1 = new SimpleDateFormat(
					"MM/dd/yyyy HH:mm:ss");

			String startDateTime = startDate + " "
					+ etStartTime.getText().toString() + ":" + "00";

			String endDateTime = endDate + " " + etEndTime.getText().toString()
					+ ":" + "00";

			if (cityId == -1)
			{
				Function.showToast(BookingFormNewActivity.this,
						ConstantMessages.MSG41);
			}
			else if (groupId == -1)
			{
				Function.showToast(BookingFormNewActivity.this,
						ConstantMessages.MSG42);
			}
			else if (!Function.checkForHourDiff(startDateTime,
					dateFormatter1.format(now), 60))
			{
				Function.showToast(BookingFormNewActivity.this,
						ConstantMessages.MSG90);
			}
			else if (!Function.checkForHourDiff(endDateTime, startDateTime, 30))
			{
				Function.showToast(BookingFormNewActivity.this,
						ConstantMessages.MSG40);
			}
			else if (TextUtils.isEmpty(cabType))
			{
				Function.showToast(BookingFormNewActivity.this,
						ConstantMessages.MSG43);
			}
			else if (TextUtils.isEmpty(cabType)
					|| cabType.equalsIgnoreCase("Cab Type not available"))
			{
				Function.showToast(BookingFormNewActivity.this,
						ConstantMessages.MSG43);
			}
			else if (llOnBehalf.getVisibility() == View.VISIBLE
					&& TextUtils.isEmpty(userOnBehalf))
			{
				Function.showToast(BookingFormNewActivity.this,
						ConstantMessages.MSG46);
			}
			else if (Function.isInteger(userOnBehalf)
					&& (userOnBehalf.length() != 10))
			{
				Function.showToast(BookingFormNewActivity.this,
						ConstantMessages.MSG46);
			}
			else if (TextUtils.isEmpty(pickPointVal))
			{
				Function.showToast(BookingFormNewActivity.this,
						ConstantMessages.MSG47);
			}
			else if (pref == 0 && TextUtils.isEmpty(dropPointVal))
			{
				Function.showToast(BookingFormNewActivity.this,
						ConstantMessages.MSG48);
			}
			else
			{
				Map<String, String> params = new HashMap<String, String>();
				params.put("UserId", mgr.GetValueFromSharedPrefs("UserID"));
				params.put("GroupId", groupId.toString());
				params.put("StartDate", startDateTime);
				params.put("EndDate", endDateTime);
				params.put("City", city);
				params.put("BookingType", "Fleet Booking");
				params.put("CabType", cabType);
				params.put("PickPoint", pickPointVal);
				params.put("Days", "0");
				params.put("DropPoint", dropPointVal);
				params.put("Picklatlng", pickLatLng);
				params.put("Droplatlng", dropLatLng);

				if (cbOnBehalf.isChecked())
				{
					if (Function
							.isInteger(username.getText().toString().trim()))
					{
						params.put("Behalf", userOnBehalf);
						params.put("BehalfMobile", userOnBehalf);
					}
					else
					{
						params.put("Behalf", userOnBehalf);
						params.put("BehalfMobile", behalfValue);
					}
				}
				else
				{
					params.put("Behalf", "0");
					params.put("BehalfMobile", "0");
				}

				if (pref == 1)
				{
					params.put("Pref", "C");
				}
				else
				{
					params.put("Pref", "P");
				}

				Log.d(TAG, "params " + params.toString());
				try
				{
					showLoadingDilog();
					Log.e(TAG,
							"user id" + mgr.GetValueFromSharedPrefs("UserID")
									+ " params = " + params);

					NetworkHandler.fleetBookingRequest(TAG, handler, params);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute)
	{
		Log.e(TAG, "hourOfDay=" + hourOfDay + " " + minute);

		String tag = timePickerDialog.getTag();
		if (tag.equals(START_TIME_PICKERTAG))
		{
			this.hourOfDay = hourOfDay;
			this.minute = minute;
			etStartTime.setText(Function.timeFormater24Hrs(hourOfDay, minute));

			validateStartDateTime();
		}
		else
		{
			this.endHourOfDay = hourOfDay;
			this.endMinute = minute;
			etEndTime.setText(Function.timeFormater24Hrs(hourOfDay, minute));

			validateEndDateTime();
		}
	}

	@Override
	public void onDateSet(DatePickerDialog datePickerDialog, int year,
			int month, int day)
	{
		String tag = datePickerDialog.getTag();
		if (tag.equals(START_DATE_PICKERTAG))
		{
			this.startDay = day;
			this.startMonth = month;
			this.startYear = year;

			etStartDate.setText(getCalDate(day, month, year, true));
		}
		else
		{
			this.endDay = day;
			this.endMonth = month;
			this.endYear = year;

			etEndDate.setText(getCalDate(day, month, year, false));
		}
	}

	private String getCalDate(int day, int month, int year, boolean isStartDate)
	{
		// Method to get Calendar date

		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_MONTH, day);
		cal.set(Calendar.MONTH, month);
		cal.set(Calendar.YEAR, year);

		String convertedDate = "";
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
		SimpleDateFormat dateFormat2 = new SimpleDateFormat("MM/dd/yyyy");

		convertedDate = dateFormat.format(cal.getTime());

		if (isStartDate)
		{
			startDate = dateFormat2.format(cal.getTime());

			if (Function.isDateLessThanCurrentDate(startDate))
			{
				Function.showToast(BookingFormNewActivity.this,
						ConstantMessages.MSG103);

				makeGetCabRequest(true);
			}
			else
			{
				// llPickTime.setBackgroundColor(getResources().getColor(
				// R.color.light_blue));
				etStartTime.setEnabled(true);

				makeGetCabRequest(false);
			}

			if (!TextUtils.isEmpty(etStartTime.getText().toString()))
			{
				validateStartDateTime();
			}
		}
		else
		{
			endDate = dateFormat2.format(cal.getTime());

			if (Function.isDateLessThanCurrentDate(endDate))
			{
				Function.showToast(BookingFormNewActivity.this,
						ConstantMessages.MSG103);

				makeGetCabRequest(true);
			}
			else
			{
				// llDropTime.setBackgroundColor(getResources().getColor(
				// R.color.light_blue));
				etEndTime.setEnabled(true);

				makeGetCabRequest(false);
			}

			if (!TextUtils.isEmpty(etEndTime.getText().toString()))
			{
				validateEndDateTime();
			}
		}

		return convertedDate;
	}

	private void validateStartDateTime()
	{
		try
		{
			now = Calendar.getInstance().getTime();
			SimpleDateFormat dateFormatter1 = new SimpleDateFormat(
					"MM/dd/yyyy HH:mm:ss");

			String startDateTime = startDate + " "
					+ etStartTime.getText().toString() + ":" + "00";

			Log.i(TAG, "DateTime now --> " + dateFormatter1.format(now));
			Log.i(TAG, "DateTime start --> " + startDateTime);

			if (Function.checkForHourDiff(startDateTime,
					dateFormatter1.format(now), 60))
			{
				// llDropDate.setBackgroundColor(getResources().getColor(
				// R.color.light_blue));
				etEndDate.setEnabled(true);

				makeGetCabRequest(false);
			}
			else
			{
				Function.showToast(BookingFormNewActivity.this,
						ConstantMessages.MSG90);

				makeGetCabRequest(true);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void validateEndDateTime()
	{
		try
		{
			SimpleDateFormat dateFormatter1 = new SimpleDateFormat(
					"MM/dd/yyyy HH:mm:ss");

			String startDateTime = startDate + " "
					+ etStartTime.getText().toString() + ":" + "00";

			String endDateTime = endDate + " " + etEndTime.getText().toString()
					+ ":" + "00";

			Log.i(TAG, "DateTime start --> " + startDateTime);
			Log.i(TAG, "DateTime end --> " + endDateTime);

			if (Function.checkForHourDiff(endDateTime, startDateTime, 30))
			{
				// llCabs.setBackgroundColor(getResources().getColor(
				// R.color.light_blue));
				llCabs.setEnabled(true);
				spCabType.setEnabled(true);

				makeGetCabRequest(false);
			}
			else
			{
				Function.showToast(BookingFormNewActivity.this,
						ConstantMessages.MSG40);

				makeGetCabRequest(true);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void makeGetCabRequest(boolean isSetNoCabs)
	{
		if (isAllDateTimeFieldFilled())
		{
			if (isSetNoCabs)
			{
				String noCabs[] =
				{ ConstantMessages.MSG102 };
				spCabType.setAdapter(populateSpinnerGeneric(noCabs));
			}
			else
			{
				SimpleDateFormat dateFormatter1 = new SimpleDateFormat(
						"MM/dd/yyyy HH:mm:ss");

				String startDateTime = startDate + " "
						+ etStartTime.getText().toString() + ":" + "00";

				String endDateTime = endDate + " "
						+ etEndTime.getText().toString() + ":" + "00";

				if (!Function.checkForHourDiff(startDateTime,
						dateFormatter1.format(now), 60))
				{
					Function.showToast(BookingFormNewActivity.this,
							ConstantMessages.MSG90);
				}
				else if (!Function.checkForHourDiff(endDateTime, startDateTime,
						30))
				{
					Function.showToast(BookingFormNewActivity.this,
							ConstantMessages.MSG40);

					String noCabs[] =
					{ ConstantMessages.MSG102 };
					spCabType.setAdapter(populateSpinnerGeneric(noCabs));
				}
				else
				{
					getCabTypeRequest(startDateTime, endDateTime);
				}
			}
		}
	}

	private boolean isAllDateTimeFieldFilled()
	{
		/*
		 * Method to check all date time fields are filled or not. If its filled
		 * it will return true
		 */

		if (TextUtils.isEmpty(etStartDate.getText().toString())
				|| TextUtils.isEmpty(etStartTime.getText().toString())
				|| TextUtils.isEmpty(etEndDate.getText().toString())
				|| TextUtils.isEmpty(etEndTime.getText().toString()))
		{
			return false;
		}
		return true;
	}

	private void getCabTypeRequest(String startDateTime, String endDateTime)
	{
		Integer groupId = -1;
		if (!spBillTo.getAdapter().isEmpty())
		{
			groupId = ((FleetGroup) spBillTo.getSelectedItem()).getRID();
		}

		String city = "";
		if (!spSelectCity.getAdapter().isEmpty())
		{
			city = ((City) spSelectCity.getSelectedItem()).getCity();
		}

		Map<String, String> params = new HashMap<String, String>();
		params.put("UserId", mgr.GetValueFromSharedPrefs("UserID"));
		params.put("GroupId", groupId.toString());
		params.put("StartDate", startDateTime);
		params.put("EndDate", endDateTime);
		params.put("City", city);

		if (pref == 1)
		{
			params.put("Pref", "C");
		}
		else
		{
			params.put("Pref", "P");
		}

		pbCabType.setVisibility(View.VISIBLE);
		Log.e(TAG, "user id" + mgr.GetValueFromSharedPrefs("UserID"));

		NetworkHandler.getCabTypeRequest(TAG, handler, params);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (requestCode == 200)
		{
			Log.i(TAG, "on act result");
			if (resultCode == RESULT_OK)
			{
				etPickupPoint.setText(data.getStringExtra("address"));
				pickLatLng = data.getStringExtra("latLong");

				if (pref == 1)
				{
					btProceed.setBackgroundResource(R.drawable.selector_button);
					btProceed.setEnabled(true);
				}
				else
				{
					// rlDropPoint.setBackgroundColor(getResources().getColor(
					// R.color.light_blue));
					rlDropPoint.setEnabled(true);
					etDropPoint.setEnabled(true);
				}
			}
		}
		else if (requestCode == 202)
		{
			Log.i(TAG, "on act result");
			if (resultCode == RESULT_OK)
			{
				etDropPoint.setText(data.getStringExtra("address"));
				dropLatLng = data.getStringExtra("latLong");

				btProceed.setBackgroundResource(R.drawable.selector_button);
				btProceed.setEnabled(true);
			}
		}
	}

}
