package com.hirecraft.jugunoo.passenger;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
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
import com.hirecraft.jugunoo.passenger.common.BookingTypeDetail;
import com.hirecraft.jugunoo.passenger.common.City;
import com.hirecraft.jugunoo.passenger.common.Constant;
import com.hirecraft.jugunoo.passenger.common.ConstantMessages;
import com.hirecraft.jugunoo.passenger.common.EngDetails;
import com.hirecraft.jugunoo.passenger.common.FleetGroup;
import com.hirecraft.jugunoo.passenger.common.Function;
import com.hirecraft.jugunoo.passenger.common.Global;
import com.hirecraft.jugunoo.passenger.common.TransparentProgressDialog;
import com.hirecraft.jugunoo.passenger.networkhandler.NetworkHandler;
import com.hirecraft.jugunoo.passenger.triplog.TripLogActivity;
import com.hirecraft.jugunoo.passenger.utility.SharedPreferencesManager;
import com.sleepbot.datetimepicker.time.RadialPickerLayout;
import com.sleepbot.datetimepicker.time.TimePickerDialog;

import de.keyboardsurfer.android.widget.crouton.Crouton;

public class UpdateBookingActivity extends FragmentActivity implements
		OnDateSetListener, TimePickerDialog.OnTimeSetListener, OnClickListener,
		OnCheckedChangeListener
{
	private Spinner spSelectCity;
	private Spinner spBillTo;
	private Spinner spBookingType;
	private Spinner spCabType;

	// private EditText etPackageDetail;
	private EditText etStartDate;
	private EditText etEndDate;
	private EditText etDaysNo;
	private EditText etPickupPoint;
	private EditText etDropPoint;

	private EditText etPickupTime;
	private EditText etDropTime;
	private AutoCompleteTextView username;

	private Button btProceed;
	private Button btCancel;
	// private Button btTariff;

	public static final String DATEPICKER_TAG = "datepicker";
	public static final String TIMEPICKER_TAG = "timepicker";
	public static final String DROP_TIME_PICKER = "drop_time";

	private Calendar calendar;
	private DatePickerDialog datePickerDialog;
	private TimePickerDialog timePickerDialog;

	private int day;
	private int month;
	private int year;
	private int hourOfDay, endHourOfDay;
	private int minute, endMinute;
	private int daysCount;
	// private int pref;
	// private long timeInMillis;

	private TransparentProgressDialog pd;
	private Handler pdHandler;
	private Runnable pdRunnable;
	private SharedPreferencesManager mgr;

	private String startDateTime, endDateTime, pickLatLng, dropLatLng;

	private ProgressBar pbCity, pbGroup, pbBookType, pbCabType;

	private CheckBox cbOnBehalf;
	private LinearLayout llOnBehalf, llChkBox;
	private ListView nameList;
	private boolean isGetName;
	private String usernameStr;
	private String behalfValue = "0";

	private String engId;
	private String prefType;
	private EngDetails engDetailsObj;
	private boolean isFirstTime = true;

	private Date now;

	private RelativeLayout rlUpdateBookingForm;
	private LinearLayout llRetry;
	private Button btRetry;
	private RelativeLayout rlUpdateBookingDropPoint;

	private static final String TAG = UpdateBookingActivity.class
			.getSimpleName();

	private Dialog dialog;

	private boolean isFirst = true;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		SetActionBar();

		setContentView(R.layout.activity_update_booking);

		dialog = new Dialog(UpdateBookingActivity.this);

		try
		{
			Intent updateIntent = getIntent();
			engId = updateIntent.getStringExtra("bookingId");
			Log.d(TAG, "engid=" + engId);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		calendar = Calendar.getInstance();

		// datePickerDialog = DatePickerDialog.newInstance(this,
		// calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
		// calendar.get(Calendar.DAY_OF_MONTH), false);

		timePickerDialog = TimePickerDialog.newInstance(this,
				calendar.get(Calendar.HOUR_OF_DAY),
				calendar.get(Calendar.MINUTE), true, false);

		mgr = new SharedPreferencesManager(this);

		initUiElement();
		makeBookingInitReq();
		openAnimation();
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
		title.setText("Update Trip");

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

	private void initUiElement()
	{

		rlUpdateBookingForm = (RelativeLayout) findViewById(R.id.rlUpdateBookingForm);
		llRetry = (LinearLayout) findViewById(R.id.llUpdateBookingRetry);
		btRetry = (Button) findViewById(R.id.updateBooking_retrybtn);

		rlUpdateBookingDropPoint = (RelativeLayout) findViewById(R.id.rlUpdateBookingDropPoint);

		pbCity = (ProgressBar) findViewById(R.id.pbCity);
		pbGroup = (ProgressBar) findViewById(R.id.pbGroup);
		pbBookType = (ProgressBar) findViewById(R.id.pbBookingType);
		pbCabType = (ProgressBar) findViewById(R.id.pbCabType);
		llOnBehalf = (LinearLayout) findViewById(R.id.llOnBehalf);
		llChkBox = (LinearLayout) findViewById(R.id.llChkBox);

		nameList = (ListView) findViewById(R.id.nameList);

		cbOnBehalf = (CheckBox) findViewById(R.id.cbOnBehalf);
		cbOnBehalf.setOnCheckedChangeListener(this);

		final String userType = mgr
				.GetNotiValueFromSharedPrefs(Constant.USER_TYPE);

		if (!TextUtils.isEmpty(userType)
				&& (userType.equalsIgnoreCase(Constant.ADMIN) || userType
						.equalsIgnoreCase(Constant.MANAGER)))
		{
			llChkBox.setVisibility(View.GONE);
		}
		else
		{
			llChkBox.setVisibility(View.GONE);
		}

		pdHandler = new Handler();
		pd = new TransparentProgressDialog(UpdateBookingActivity.this,
				R.drawable.loading_image);

		spSelectCity = (Spinner) findViewById(R.id.spCity);

		spSelectCity
				.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
				{

					@Override
					public void onItemSelected(AdapterView<?> arg0, View arg1,
							int arg2, long arg3)
					{
						String countVal = etDaysNo.getText().toString();

						if (!TextUtils.isEmpty(countVal))
						{
							int count = Integer.valueOf(countVal);

							if (!isFirstTime)
							{
								getCalDateCount(count);
							}
						}
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
				String countVal = etDaysNo.getText().toString();
				if (!TextUtils.isEmpty(countVal))
				{
					int count = Integer.valueOf(countVal);

					if (!isFirstTime)
					{
						getCalDateCount(count);
					}
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent)
			{
			}
		});
		spBillTo.setAdapter(populateSpinnerGeneric(new FleetGroup[0]));
		spBookingType = (Spinner) findViewById(R.id.spBookingType);
		spBookingType
				.setAdapter(populateSpinnerGeneric(new BookingTypeDetail[0]));
		spCabType = (Spinner) findViewById(R.id.spCabType);

		String noCabs[] =
		{ "Cab Type not available" };
		spCabType.setAdapter(populateSpinnerGeneric(noCabs));

		etStartDate = (EditText) findViewById(R.id.etStartDate);
		etPickupTime = (EditText) findViewById(R.id.etTime);
		etDaysNo = (EditText) findViewById(R.id.etDaysNo);
		etEndDate = (EditText) findViewById(R.id.etEndDate);

		etPickupPoint = (EditText) findViewById(R.id.etPickupPoint);
		etPickupPoint.setOnClickListener(this);
		etDropPoint = (EditText) findViewById(R.id.etDropPoint);
		etDropPoint.setOnClickListener(this);
		etDropTime = (EditText) findViewById(R.id.etEndTime);

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
				datePickerDialog = DatePickerDialog.newInstance(
						UpdateBookingActivity.this, year, month, day, false);
				// datePickerDialog.setFirstDayOfWeek(calendar.get(Calendar.DAY_OF_WEEK));
				datePickerDialog.setYearRange(calendar.get(Calendar.YEAR),
						calendar.get(Calendar.YEAR) + 1);
				// datePickerDialog
				// .setCloseOnSingleTapDay(isCloseOnSingleTapDay());
				datePickerDialog.show(getSupportFragmentManager(),
						DATEPICKER_TAG);
			}
		});

		etPickupTime.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				// timePickerDialog
				// .setCloseOnSingleTapMinute(isCloseOnSingleTapMinute());

				timePickerDialog.setStartTime(hourOfDay, minute);
				timePickerDialog.show(getSupportFragmentManager(),
						TIMEPICKER_TAG);
			}
		});

		etDropTime.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				timePickerDialog.setStartTime(endHourOfDay, endMinute);
				timePickerDialog.show(getSupportFragmentManager(),
						DROP_TIME_PICKER);
			}
		});

		etStartDate.addTextChangedListener(dateWatcher);
		etPickupTime.addTextChangedListener(timeWatcher);

		etDaysNo.addTextChangedListener(watcher);

		username = (AutoCompleteTextView) findViewById(R.id.acAdUserName);
		username.addTextChangedListener(userIDWatcher);

	}

	private void manageView(boolean status)
	{
		if (status)
		{
			rlUpdateBookingForm.setVisibility(View.VISIBLE);
			llRetry.setVisibility(View.GONE);
		}
		else
		{
			rlUpdateBookingForm.setVisibility(View.GONE);
			llRetry.setVisibility(View.VISIBLE);
		}
	}

	public void hidekeys()
	{
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(etDaysNo.getWindowToken(), 0);
	}

	private void openAnimation()
	{

		overridePendingTransition(R.anim.activity_open_translate,
				R.anim.activity_close_scale);
	}

	private void closeAnimation()
	{

		overridePendingTransition(R.anim.activity_open_scale,
				R.anim.activity_close_translate);
	}

	// EditTextWacther Implementation

	private final TextWatcher watcher = new TextWatcher()
	{

		public void beforeTextChanged(CharSequence s, int start, int count,
				int after)
		{
			// When No Number Entered
		}

		public void onTextChanged(CharSequence s, int start, int before,
				int count)
		{

		}

		@Override
		public void afterTextChanged(Editable s)
		{

			if (s.length() == 0)
			{
				String noCabs[] =
				{ "Cab Type not available" };
				spCabType.setAdapter(populateSpinnerGeneric(noCabs));
			}

			if (s.length() > 0)
			{
				daysCount = Integer.valueOf(s.toString());

				// if (!isFirstTime)
				// getCalDateCount(count);
				if (s.length() == 2)
				{
					// Function.hideSoftKeyBoard(UpdateBookingActivity.this);
					hidekeys();
				}
				if (daysCount > 60)
				{
					Function.showToast(UpdateBookingActivity.this,
							ConstantMessages.MSG38);
				}

				if (!TextUtils.isEmpty(etStartDate.getText().toString().trim())
						&& !isFirstTime)
				{
					getCalDateCount(daysCount);
				}

			}
			else
			{
				etEndDate.setText("");
			}
		}
	};

	// date watcher
	private final TextWatcher dateWatcher = new TextWatcher()
	{

		public void beforeTextChanged(CharSequence s, int start, int count,
				int after)
		{
			// When No Number Entered
		}

		public void onTextChanged(CharSequence s, int start, int before,
				int count)
		{

		}

		@Override
		public void afterTextChanged(Editable s)
		{
			if (s.length() > 0)
			{

				now = Calendar.getInstance().getTime();

				String countVal = etDaysNo.getText().toString();
				if (!TextUtils.isEmpty(countVal))
				{
					int count = Integer.valueOf(countVal);

					if (!isFirstTime)
					{
						getCalDateCount(count);
					}
				}
			}
			else
			{
				etEndDate.setText("");
			}
		}
	};

	// time watcher
	private final TextWatcher timeWatcher = new TextWatcher()
	{

		public void beforeTextChanged(CharSequence s, int start, int count,
				int after)
		{
			// When No Number Entered
		}

		public void onTextChanged(CharSequence s, int start, int before,
				int count)
		{

		}

		@Override
		public void afterTextChanged(Editable s)
		{
			if (s.length() > 0)
			{
				String countVal = etDaysNo.getText().toString();
				if (!TextUtils.isEmpty(countVal))
				{

					int count = Integer.valueOf(countVal);

					if (!isFirstTime)
					{
						getCalDateCount(count);
					}
				}
			}
			else
			{
				etEndDate.setText("");
			}
		}
	};

	private void getCalDateCount(int count)
	{

		Calendar startDateCal = Calendar.getInstance();
		startDateCal.set(Calendar.DAY_OF_MONTH, day);
		startDateCal.set(Calendar.MONTH, month);
		startDateCal.set(Calendar.YEAR, year);
		startDateCal.set(Calendar.HOUR_OF_DAY, hourOfDay);
		startDateCal.set(Calendar.MINUTE, minute);

		Calendar endDateCal = Calendar.getInstance();
		endDateCal.set(Calendar.DAY_OF_MONTH, day);
		endDateCal.set(Calendar.MONTH, month);
		endDateCal.set(Calendar.YEAR, year);
		endDateCal.set(Calendar.HOUR_OF_DAY, endHourOfDay);
		endDateCal.set(Calendar.MINUTE, endMinute);
		endDateCal.add(Calendar.DATE, count);

		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM");
		String convertedDate = dateFormat.format(endDateCal.getTime());

		SimpleDateFormat dateFormatter = new SimpleDateFormat(
				"MM/dd/yyyy HH:mm:ss");
		// SimpleDateFormat dateFormatter1 = new
		// SimpleDateFormat("HH:mm:ss");

		// System.out.println(dateFormatter.format(cal.getTime()));
		// System.out.println(dateFormatter1.format(cal.getTime()));

		String dayName = endDateCal.getDisplayName(Calendar.DAY_OF_WEEK,
				Calendar.SHORT, Locale.getDefault());

		etEndDate.setText(convertedDate + " (" + dayName + ")");

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

		startDateTime = dateFormatter.format(startDateCal.getTime());
		endDateTime = dateFormatter.format(endDateCal.getTime());

		SimpleDateFormat dateFormatter1 = new SimpleDateFormat(
				"MM/dd/yyyy HH:mm:ss");

		Map<String, String> params = new HashMap<String, String>();
		params.put("UserId", mgr.GetValueFromSharedPrefs("UserID"));
		params.put("GroupId", groupId.toString());
		params.put("StartDate", startDateTime);
		params.put("EndDate", endDateTime);
		params.put("City", city);
		params.put("Pref", engDetailsObj.getPref());
		params.put("EngId", engId);

		if (isFirst)
		{
			makeGetCabTypeRequest(params);
			isFirst = false;
		}
		else
		{
			if (!Function.checkForHourDiff(
					dateFormatter1.format(startDateCal.getTime()),
					dateFormatter1.format(now), 15))
			{
				Function.showToast(UpdateBookingActivity.this,
						ConstantMessages.MSG39);

				String noCabs[] =
				{ "Cab Type not available" };
				spCabType.setAdapter(populateSpinnerGeneric(noCabs));
			}
			else if (!Function.checkForHourDiff(
					dateFormatter1.format(endDateCal.getTime()),
					dateFormatter1.format(startDateCal.getTime()), 30))
			{
				Function.showToast(UpdateBookingActivity.this,
						ConstantMessages.MSG40);

				String noCabs[] =
				{ "Cab Type not available" };
				spCabType.setAdapter(populateSpinnerGeneric(noCabs));
			}
			else if (validateInputs())
			{
				makeGetCabTypeRequest(params);
			}
		}

	}

	private boolean validateTime()
	{
		int noDay = Integer.parseInt(etDaysNo.getText().toString().trim());
		String startTime = etPickupTime.getText().toString().trim();
		String endTime = etDropTime.getText().toString().trim();

		if (noDay == 0)
		{
			if (startTime.equalsIgnoreCase(endTime))
			{
				return true;
			}
		}
		return false;
	}

	private boolean validateInputs()
	{

		boolean flag = false;

		if (!TextUtils.isEmpty(etStartDate.getText().toString())
				&& !TextUtils.isEmpty(etEndDate.getText().toString())
				&& !TextUtils.isEmpty(etPickupTime.getText().toString())
				&& !TextUtils.isEmpty(etDropTime.getText().toString())
				&& !TextUtils.isEmpty(etDaysNo.getText().toString()))
		{
			if (Integer.valueOf(etDaysNo.getText().toString()) <= 60)
			{
				flag = true;
			}
		}
		Log.d("validateInputs", "validateInputs=" + flag);

		return flag;
	}

	private <A> ArrayAdapter<A> populateSpinnerGeneric(A[] list)
	{
		ArrayAdapter<A> dataAdapter = new ArrayAdapter<A>(this,
				android.R.layout.simple_spinner_item, list);
		dataAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		return dataAdapter;
	}

	private <A> int getIndex(Spinner spinner, A value)
	{

		int index = 0;

		for (int i = 0; i < spinner.getCount(); i++)
		{
			Log.e(TAG, "ind=" + spinner.getItemAtPosition(i) + " " + value
					+ " " + spinner.getItemAtPosition(i).equals(value));

			if (spinner.getItemAtPosition(i).equals(value))
			{
				index = i;
			}
		}
		return index;
	}

	// private void spinnerAdapter4() {
	// List<String> list = new ArrayList<String>();
	// list.add("Cab type");
	// list.add("Mini");
	// list.add("Sedan");
	// list.add("Premium");
	// list.add("Luxy");
	//
	// ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
	// android.R.layout.simple_spinner_item, list);
	// dataAdapter
	// .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	//
	// spCabType.setAdapter(dataAdapter);
	// }

	@Override
	public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute)
	{

		Log.e(TAG, "hourOfDay=" + hourOfDay + " " + minute);

		String tag = timePickerDialog.getTag();
		if (tag.equals(DROP_TIME_PICKER))
		{

			this.endHourOfDay = hourOfDay;
			this.endMinute = minute;
			// timeFormater(int hourOfDay, int minute)
			etDropTime.setText(Function.timeFormater24Hrs(hourOfDay, minute));

			if (!TextUtils.isEmpty(etDaysNo.getText().toString())
					&& !TextUtils.isEmpty(etStartDate.getText().toString()))
			{
				String countVal = etDaysNo.getText().toString();
				getCalDateCount(Integer.valueOf(countVal));
			}

			// String countVal = etDaysNo.getText().toString();
			// if (!TextUtils.isEmpty(countVal))
			// {
			//
			// int count = Integer.valueOf(countVal);
			// Log.e("Test number", "" + count);
			//
			// Calendar endDateCal = Calendar.getInstance();
			// endDateCal.set(Calendar.DAY_OF_MONTH, day);
			// endDateCal.set(Calendar.MONTH, month);
			// endDateCal.set(Calendar.YEAR, year);
			// endDateCal.set(Calendar.HOUR_OF_DAY, endHourOfDay);
			// endDateCal.set(Calendar.MINUTE, endMinute);
			// endDateCal.add(Calendar.DATE, count);
			//
			// SimpleDateFormat dateFormatter = new SimpleDateFormat(
			// "MM/dd/yyyy HH:mm:ss");
			//
			// endDateTime = dateFormatter.format(endDateCal.getTime());
			// }
			//
			// // new code in this method
			//
			// Integer groupId = -1;
			// if (!spBillTo.getAdapter().isEmpty())
			// {
			// groupId = ((FleetGroup) spBillTo.getSelectedItem()).getRID();
			//
			// Log.i("TAG", "groupId -->" + groupId);
			// }
			//
			// String city = "";
			//
			// if (!spSelectCity.getAdapter().isEmpty())
			// {
			// city = ((City) spSelectCity.getSelectedItem()).getCity();
			// }
			//
			// Map<String, String> params = new HashMap<String, String>();
			// params.put("UserId", mgr.GetValueFromSharedPrefs("UserID"));
			// params.put("GroupId", groupId.toString());
			// params.put("StartDate", startDateTime);
			// params.put("EndDate", endDateTime);
			// params.put("City", city);
			//
			// int prefIndex =
			// mgr.getPreferenceIndex(Constant.PREFERENCE_INDEX);
			//
			// if (prefIndex == 1)
			// {
			// params.put("Pref", "C");
			// }
			// else
			// {
			// params.put("Pref", "P");
			// }
			//
			// if (validateTime())
			// {
			// Function.showToast(UpdateBookingActivity.this,
			// "End time should be greater than Start time");
			//
			// String noCabs[] =
			// { "Cab Type not available" };
			// spCabType.setAdapter(populateSpinnerGeneric(noCabs));
			// }
			// else
			// {
			// if (validateInputs())
			// {
			// getCabTypeRequest(params);
			// }
			// }
			//
			// }

		}
		else
		{
			this.hourOfDay = hourOfDay;
			this.minute = minute;
			etPickupTime.setText(Function.timeFormater24Hrs(hourOfDay, minute));
		}
	}

	@Override
	public void onDateSet(DatePickerDialog datePickerDialog, int year,
			int month, int day)
	{
		this.day = day;
		this.month = month;
		this.year = year;
		etStartDate.setText(getCalDate(day, month, year));
	}

	private String getCalDate(int day, int month, int year)
	{
		// Method to get Calendar date

		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_MONTH, day);
		cal.set(Calendar.MONTH, month);
		cal.set(Calendar.YEAR, year);

		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM (EEE)");
		String convertedDate = dateFormat.format(cal.getTime());

		// return convertedDate + " (" + dayName + ")";
		return convertedDate;
	}

	private static void showErrorMessage(final Context context, String message)
	{
		Function.showToast(context, message);
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
				case Constant.MessageState.GET_BOOKING_DETAILS_SUCCESS:

					manageView(true);

					cancelLoadingDialog();

					pbCity.setVisibility(View.GONE);
					pbGroup.setVisibility(View.GONE);
					pbBookType.setVisibility(View.GONE);

					JSONObject jsonObj = (JSONObject) msg.obj;

					// cabsJsonParser(jsonArr);
					Log.e(TAG, jsonObj.toString());
					parseBookingInit(jsonObj);

					prefType = engDetailsObj.getPref();
					if (prefType.equalsIgnoreCase("P"))
					{
						rlUpdateBookingDropPoint.setVisibility(View.VISIBLE);
					}
					else
					{
						rlUpdateBookingDropPoint.setVisibility(View.GONE);
					}

					break;

				case Constant.MessageState.GET_BOOKING_DETAILS_FAIL:

					Log.e(TAG, "failed");

					manageView(false);

					btRetry.setOnClickListener(new View.OnClickListener()
					{
						@Override
						public void onClick(View v)
						{
							makeBookingInitReq();
						}
					});

					cancelLoadingDialog();

					pbCity.setVisibility(View.GONE);
					pbGroup.setVisibility(View.GONE);
					pbBookType.setVisibility(View.GONE);

					break;

				case Constant.MessageState.FLEET_BOOK_UPDATE_SUCCESS:

					cancelLoadingDialog();

					pbCabType.setVisibility(View.GONE);

					JSONObject jsonFleetObj = (JSONObject) msg.obj;

					Log.e(TAG, jsonFleetObj.toString());

					parseFleetBooking(jsonFleetObj);

					break;

				case Constant.MessageState.FLEET_BOOK_UPDATE_FAIL:

					Log.e(TAG, "failed");

					Function.showToast(UpdateBookingActivity.this,
							ConstantMessages.MSG6);

					cancelLoadingDialog();

					pbCabType.setVisibility(View.GONE);
					break;

				case Constant.MessageState.CAB_TYPE_UPDATE_SUCCESS:

					pbCabType.setVisibility(View.GONE);

					JSONObject jsoncabTypeObj = (JSONObject) msg.obj;

					Log.e(TAG, "cabty=" + jsoncabTypeObj.toString());

					parseReqCabType(jsoncabTypeObj);

					break;
				case Constant.MessageState.CAB_TYPE_UPDATE_FAIL:

					Log.e(TAG, "cabty=fail");
					pbCabType.setVisibility(View.GONE);
					break;

				case Constant.MessageState.USERNAMES_SUCCESS:

					JSONObject result = (JSONObject) msg.obj;
					parseUserList(result);
					break;

				case Constant.MessageState.USERNAMES_FAIL:

					nameList.setVisibility(View.GONE);
					break;

				default:
					break;
			}

			if (msg.what == JUGUNOO_SEARCH_USER)
			{

				String userId = mgr.GetValueFromSharedPrefs("UserID");

				String url = Global.JUGUNOO_WS
						+ "Passenger/GetNameByMobile?Mobile=" + usernameStr
						+ "&UserId=" + userId;
				setUrl(url);

			}

		}

	};
	private ArrayList<HashMap<String, String>> names;

	private void makeBookingInitReq()
	{
		try
		{
			showLoadingDilog();

			// Log.e(TAG, "user id" + mgr.GetValueFromSharedPrefs("UserID"));
			// String userId = mgr.GetValueFromSharedPrefs("UserID");
			// String userType = mgr.GetValueFromSharedPrefs("UserType");
			// int prefIndex =
			// mgr.getPreferenceIndex(Constant.PREFERENCE_INDEX);

			NetworkHandler.getBookingDetails(TAG, handler, engId,
					mgr.GetValueFromSharedPrefs("UserID"));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	private void makeGetCabTypeRequest(Map<String, String> params)
	{
		pbCabType.setVisibility(View.VISIBLE);
		NetworkHandler.getCabTypeUpdateRequest(TAG, handler, params);
	}

	@Override
	public void finish()
	{
		if (pd != null)
		{
			if (pd.isShowing())
			{
				pd.dismiss();
			}
		}

		super.finish();
	}

	private void parseBookingInit(JSONObject jsonObj)
	{
		try
		{
			Log.d(TAG, "parseBookingInit" + " " + jsonObj.toString());

			if (jsonObj.has(Constant.RESULT))
			{
				String result = jsonObj.getString(Constant.RESULT);
				if (result.equalsIgnoreCase(Constant.Result_STATE.PASS))
				{
					Gson gson = new Gson();
					if (jsonObj.has("City"))
					{

						City[] citys = gson.fromJson(jsonObj.getString("City"),
								City[].class);

						spSelectCity.setAdapter(populateSpinnerGeneric(citys));
					}

					if (jsonObj.has("Groups"))
					{

						FleetGroup[] fleetGrps = gson
								.fromJson(jsonObj.getString("Groups"),
										FleetGroup[].class);

						spBillTo.setAdapter(populateSpinnerGeneric(fleetGrps));

					}

					if (jsonObj.has("EngDetails"))
					{
						engDetailsObj = gson.fromJson(
								jsonObj.getString("EngDetails"),
								EngDetails.class);

						City city = new City();
						city.setCity(engDetailsObj.getCity());
						spSelectCity.setSelection(getIndex(spSelectCity, city));

						FleetGroup fleetGrp = new FleetGroup();
						fleetGrp.setRID(engDetailsObj.getGroupX());
						spBillTo.setSelection(getIndex(spBillTo, fleetGrp));

						etPickupPoint.setText(engDetailsObj.getPickPoint());
						pickLatLng = engDetailsObj.getPicklatlng();

						etDropPoint.setText(engDetailsObj.getDropPoint());

						// start date

						// Calendar startCal = Calendar.getInstance();
						// startCal.setTimeInMillis(engDetailsObj.getStartDate());
						//
						// int year = startCal.get(Calendar.YEAR);
						// int month = startCal.get(Calendar.MONTH);
						// int day = startCal.get(Calendar.DAY_OF_MONTH);
						// int hour = startCal.get(Calendar.HOUR_OF_DAY);
						// int min = startCal.get(Calendar.MINUTE);

						this.day = Integer.valueOf(Function.getDateTimeFromUTC(
								engDetailsObj.getStartDate().toString(), "dd"));

						this.month = Integer.valueOf(Function
								.getDateTimeFromUTC(engDetailsObj
										.getStartDate().toString(), "MM")) - 1;

						this.year = Integer.valueOf(Function
								.getDateTimeFromUTC(engDetailsObj
										.getStartDate().toString(), "yyyy"));

						// this.day = day;
						// this.month = month;
						// this.year = year;

						// getCalDate(day, month, year);

						// String date = day + "-" + (month + 1) + "-" + year;
						// etStartDate.setText(getCalDate(day, month, year));

						etStartDate.setText(Function.getDateTimeFromUTC(
								engDetailsObj.getStartDate().toString(),
								"dd-MMM (EEE)"));

						this.hourOfDay = Integer.valueOf(Function
								.getDateTimeFromUTC(engDetailsObj
										.getStartDate().toString(), "HH"));
						this.minute = Integer.valueOf(Function
								.getDateTimeFromUTC(engDetailsObj
										.getStartDate().toString(), "mm"));

						// etTime.setText(timeFormater(hourOfDay, minute));

						etPickupTime.setText(Function.getDateTimeFromUTC(
								engDetailsObj.getStartDate().toString(),
								"HH:mm"));

						Calendar endCal = Calendar.getInstance();
						endCal.setTimeInMillis(engDetailsObj.getEndDate());

						// int endYear = endCal.get(Calendar.YEAR);
						// int endMonth = endCal.get(Calendar.MONTH);
						// int endDay = endCal.get(Calendar.DAY_OF_MONTH);
						// int endHour = endCal.get(Calendar.HOUR_OF_DAY);
						// int endMin = endCal.get(Calendar.MINUTE);

						this.endHourOfDay = Integer.valueOf(Function
								.getDateTimeFromUTC(engDetailsObj.getEndDate()
										.toString(), "HH"));
						this.endMinute = Integer.valueOf(Function
								.getDateTimeFromUTC(engDetailsObj.getEndDate()
										.toString(), "mm"));

						// etDropTime
						// .setText(timeFormater(endHourOfDay, endMinute));

						etDropTime
								.setText(Function.getDateTimeFromUTC(
										engDetailsObj.getEndDate().toString(),
										"HH:mm"));

						int count = Integer.valueOf(engDetailsObj.getDays());
						Log.e("Test number", "" + count);
						etDaysNo.setText(engDetailsObj.getDays());
						getCalDateCount(count);

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
			Log.e(TAG, "parseReqCabType" + " " + jsonObj.toString());

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

							spCabType.setSelection(getIndex(spCabType,
									engDetailsObj.getCabType()));
						}

						if (isFirstTime)
						{
							spCabType.setSelection(getIndex(spCabType,
									engDetailsObj.getCabType()));

							isFirstTime = false;
						}
					}
				}
				else
				{

					if (isFirstTime)
					{
						String noCabs[] =
						{ engDetailsObj.getCabType() };
						spCabType.setAdapter(populateSpinnerGeneric(noCabs));
						isFirstTime = false;

					}
					else
					{
						Function.showToast(UpdateBookingActivity.this,
								jsonObj.getString("Message"));

						String noCabs[] =
						{ "Cab Type not available" };
						spCabType.setAdapter(populateSpinnerGeneric(noCabs));
					}
				}
			}
		}
		catch (Exception bug)
		{
			bug.printStackTrace();
		}

	}

	private void parseFleetBooking(JSONObject jsonObj)
	{
		try
		{
			if (jsonObj.has(Constant.RESULT))
			{
				String result = jsonObj.getString(Constant.RESULT);
				if (result.equalsIgnoreCase(Constant.Result_STATE.PASS))
				{
					Global.isTripLogUpdated = true;
					JugunooInteractiveDialog(ConstantMessages.MSG91,
							ConstantMessages.MSG53);

				}
				else
				{
					Function.showToast(UpdateBookingActivity.this,
							jsonObj.getString("Message"));

					new Handler().postDelayed(new Runnable()
					{
						@Override
						public void run()
						{
							startActivity(new Intent().setClass(
									UpdateBookingActivity.this,
									TripLogActivity.class));
						}
					}, 3000);

				}
			}
		}
		catch (Exception bug)
		{
			bug.printStackTrace();
		}

	}

	@Override
	protected void onPause()
	{
		super.onPause();
		Crouton.cancelAllCroutons();
		closeAnimation();
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

				// InputMethodManager imm = (InputMethodManager)
				// view.getContext()
				// .getSystemService(Context.INPUT_METHOD_SERVICE);
				// imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

				Function.hideSoftKeyBoard(UpdateBookingActivity.this);

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

	@Override
	public void onBackPressed()
	{
		super.onBackPressed();
		UpdateBookingActivity.this.finish();
	}

	private void attemptBooking()
	{

		String city = "";
		Integer cityId = -1;
		if (!spSelectCity.getAdapter().isEmpty())
		{
			city = ((City) spSelectCity.getSelectedItem()).getCity();
			cityId = ((City) spSelectCity.getSelectedItem()).getRID();
		}

		// String group = "";
		Integer groupId = -1;
		if (!spBillTo.getAdapter().isEmpty())
		{
			// group = ((FleetGroup) spBillTo.getSelectedItem()).getGroupName();
			groupId = ((FleetGroup) spBillTo.getSelectedItem()).getRID();
		}

		String bookName = "";
		Integer bookId = -1;

		if (!spBookingType.getAdapter().isEmpty())
		{
			bookName = ((BookingTypeDetail) spBookingType.getSelectedItem())
					.getBookingType();
			bookId = ((BookingTypeDetail) spBookingType.getSelectedItem())
					.getRID();
		}

		String cabType = "";

		if (!spCabType.getAdapter().isEmpty())
		{
			cabType = spCabType.getSelectedItem().toString();
		}

		String startDateVal = etStartDate.getText().toString();
		// String endDateVal = etEndDate.getText().toString();
		String pickTimeVal = etPickupTime.getText().toString();
		String dropTimeVal = etDropTime.getText().toString();
		String noOfDays = etDaysNo.getText().toString();
		String pickPointVal = etPickupPoint.getText().toString();
		String userOnBehalf = username.getText().toString();
		String dropPointVal;

		if (prefType.equalsIgnoreCase("P"))
			dropPointVal = etDropPoint.getText().toString();
		else
			dropPointVal = "";

		if (TextUtils.isDigitsOnly(userOnBehalf))
		{
			behalfValue = userOnBehalf;
		}

		if (cityId == -1)
		{
			showErrorMessage(UpdateBookingActivity.this, ConstantMessages.MSG41);

		}
		else if (groupId == -1)
		{
			showErrorMessage(UpdateBookingActivity.this, ConstantMessages.MSG42);

		}
		// else if (bookId == -1)
		// {
		//
		// showErrorMessage(getApplicationContext(), rlErrorMessage,
		// tvErrorMessage, "Please select booking type");
		//
		// }
		else if (TextUtils.isEmpty(cabType))
		{
			showErrorMessage(UpdateBookingActivity.this, ConstantMessages.MSG43);
		}
		else if (TextUtils.isEmpty(startDateVal))
		{
			showErrorMessage(UpdateBookingActivity.this, ConstantMessages.MSG34);
		}
		else if (TextUtils.isEmpty(pickTimeVal))
		{
			showErrorMessage(UpdateBookingActivity.this, ConstantMessages.MSG35);
		}

		else if (TextUtils.isEmpty(noOfDays))
		{
			showErrorMessage(UpdateBookingActivity.this, ConstantMessages.MSG44);
		}
		else if (daysCount > 60)
		{
			Function.showToast(UpdateBookingActivity.this,
					ConstantMessages.MSG38);
		}
		else if (TextUtils.isEmpty(dropTimeVal))
		{
			showErrorMessage(UpdateBookingActivity.this, ConstantMessages.MSG45);
		}
		else if (TextUtils.isEmpty(cabType)
				|| cabType.equalsIgnoreCase("Cab Type not available"))
		{
			showErrorMessage(UpdateBookingActivity.this, ConstantMessages.MSG43);
		}
		else if (llOnBehalf.getVisibility() == View.VISIBLE
				&& TextUtils.isEmpty(userOnBehalf))
		{
			showErrorMessage(UpdateBookingActivity.this, ConstantMessages.MSG46);
		}
		else if (TextUtils.isEmpty(pickPointVal))
		{
			showErrorMessage(UpdateBookingActivity.this, ConstantMessages.MSG47);
		}
		else if (prefType.equalsIgnoreCase("P")
				&& TextUtils.isEmpty(dropPointVal))
		{
			Function.showToast(UpdateBookingActivity.this,
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
			params.put("BookingType", bookName);
			params.put("CabType", cabType);
			params.put("PickPoint", pickPointVal);
			params.put("Days", noOfDays);
			params.put("DropPoint", dropPointVal);
			params.put("Picklatlng", pickLatLng);
			params.put("Droplatlng", dropLatLng);
			// params.put("Behalf", engDetailsObj.getBehalf().toString());
			params.put("EngId", engId);
			params.put("Operation", "U");
			params.put("Pref", engDetailsObj.getPref());

			try
			{
				showLoadingDilog();
				Log.e(TAG, "user id" + mgr.GetValueFromSharedPrefs("UserID")
						+ " p=" + params);

				NetworkHandler.fleetBookingUpdateRequest(TAG, handler, params);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
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
			}

		}
		else if (requestCode == 202)
		{

			Log.i(TAG, "on act result");

			if (resultCode == RESULT_OK)
			{

				etDropPoint.setText(data.getStringExtra("address"));

				dropLatLng = data.getStringExtra("latLong");

			}

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

	private void JugunooInteractiveDialog(String title, String message)
	{

		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.dialog_common_alert);
		dialog.setCancelable(false);

		TextView tvTitle = (TextView) dialog.findViewById(R.id.tvAlertHeader);
		tvTitle.setText(title);
		TextView tvMsg = (TextView) dialog.findViewById(R.id.tvAlertMsg);
		tvMsg.setText(message);

		Button btOk = (Button) dialog.findViewById(R.id.btAlertOk);
		btOk.setText(ConstantMessages.MSG93);

		Button btCancel = (Button) dialog.findViewById(R.id.btAlertCancel);
		btCancel.setText(ConstantMessages.MSG96);

		btOk.setOnClickListener(new View.OnClickListener()
		{

			@Override
			public void onClick(View v)
			{

				dialog.dismiss();
				UpdateBookingActivity.this.finish();

				Intent homeIntent = new Intent(UpdateBookingActivity.this,
						TripLogActivity.class);
				homeIntent.addFlags(Intent.FLAG_ACTIVITY_TASK_ON_HOME);
				startActivity(homeIntent);

			}
		});

		dialog.show();

	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
	{
		Log.e(TAG, "chk =" + isChecked);
		if (isChecked)
		{
			llOnBehalf.setVisibility(View.VISIBLE);
		}
		else
		{
			llOnBehalf.setVisibility(View.GONE);
		}

	}

	private void parseUserList(JSONObject result)
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
							HashMap<String, String> fetchData = new HashMap<String, String>();
							fetchData.put("FirstName", grName);
							fetchData.put("RID", rid);
							names.add(fetchData);
							// nameList.setVisibility(View.VISIBLE);

							SimpleAdapter adapter = new SimpleAdapter(
									UpdateBookingActivity.this, names,
									R.layout.username_row, new String[]
									{ "FirstName", "RID" }, new int[]
									{ R.id.name, R.id.rid });
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

									username.setText(grName);
									behalfValue = rid;
									// managingUserId = rid;
									Log.i("tag", "grName: " + grName + ", rid"
											+ rid);
									nameList.setVisibility(View.GONE);

									// InputMethodManager imm =
									// (InputMethodManager)
									// getSystemService(Context.INPUT_METHOD_SERVICE);
									// imm.hideSoftInputFromWindow(username.getWindowToken(),
									// 0);
									Function.hideSoftKeyBoard(UpdateBookingActivity.this);
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

	private void setUrl(String url)
	{
		if (isGetName == false)
		{
			// user = new UserName(AddFleetGroups.this, url, this);
			// user.execute();
			handler.removeCallbacksAndMessages(null);
			NetworkHandler.GetUserNames(TAG, handler, url);
		}

	}

	private TextWatcher userIDWatcher = new TextWatcher()
	{

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count)
		{
			usernameStr = username.getText().toString();
			// String userId = mgr.GetValueFromSharedPrefs("UserID");
			if (count % 2 == 1)
			{

				handler.removeMessages(JUGUNOO_SEARCH_USER);
				handler.sendEmptyMessageDelayed(JUGUNOO_SEARCH_USER,
						JUGUNOO_DELAY_IN_MILLIS);

				// String url = Global.JUGUNOO_WS
				// + "Passenger/GetNameByMobile?Mobile=" + usernameStr
				// + "&UserId=" + userId;
				// setUrl(url);
			}
			if (usernameStr.length() < 1)
			{
				showErrorMessage(UpdateBookingActivity.this,
						ConstantMessages.MSG49);
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
			// usernameStr = username.getText().toString();
			// if (usernameStr.length() == 0)
			// isGetName = false;
			// else
			// JugunooUtil.showErrorMessage(AddFleetUsers.this, errorL,
			// errorT, "Enter a valid User ID.");
		}
	};

	@Override
	public void onStop()
	{
		super.onStop();
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
	}

}
