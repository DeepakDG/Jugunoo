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
import com.hirecraft.jugunoo.passenger.common.FleetGroup;
import com.hirecraft.jugunoo.passenger.common.Function;
import com.hirecraft.jugunoo.passenger.common.Global;
import com.hirecraft.jugunoo.passenger.common.TransparentProgressDialog;
import com.hirecraft.jugunoo.passenger.networkhandler.NetworkHandler;
import com.hirecraft.jugunoo.passenger.utility.SharedPreferencesManager;
import com.sleepbot.datetimepicker.time.RadialPickerLayout;
import com.sleepbot.datetimepicker.time.TimePickerDialog;

import de.keyboardsurfer.android.widget.crouton.Crouton;

public class BookingFormActivity extends FragmentActivity implements
		OnDateSetListener, TimePickerDialog.OnTimeSetListener, OnClickListener,
		OnCheckedChangeListener
{
	private Spinner spSelectCity;
	private Spinner spBillTo;
	private Spinner spBookingType;
	private Spinner spCabType;

	// private EditText etPackageDetail;
	private EditText etPickupDate;
	private EditText etDropDate;
	private EditText etDaysNo;
	private EditText etPickupTime;
	private EditText etDropTime;
	private EditText etPickupPoint;
	private EditText etDropPoint;
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
	private int pref;
	// private long timeInMillis;

	private TransparentProgressDialog pd;
	private Handler pdHandler;
	private Runnable pdRunnable;
	private SharedPreferencesManager mgr;

	private String startDateTime, endDateTime, pickLatLng, dropLatLng;
	private String cabType = "";

	private ProgressBar pbCity, pbGroup, pbBookType, pbCabType;

	private CheckBox cbOnBehalf;
	private LinearLayout llOnBehalf, llChkBox;
	private ListView nameList;
	private boolean isGetName;
	private boolean isValidPickupDate, isValidDropDate;
	private String usernameStr, behalfValue = "0";

	private ArrayList<HashMap<String, String>> names;
	private static final String TAG = BookingFormActivity.class.getSimpleName();

	private Date now;

	private RelativeLayout rlBookingForm;
	private LinearLayout llRetry;
	private Button btRetry;
	private Dialog dialog;

	private LinearLayout llPickTime, llDaysno, llDropDate, llDropTime, llCabs;
	private RelativeLayout rlPickPoint, rlDropPoint;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		SetActionBar();

		setContentView(R.layout.activity_booking_form);
		dialog = new Dialog(BookingFormActivity.this);

		// hidekeys
		etDaysNo = (EditText) findViewById(R.id.etDaysNo);

		calendar = Calendar.getInstance();

		// datePickerDialog = DatePickerDialog.newInstance(this,
		// calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
		// calendar.get(Calendar.DAY_OF_MONTH), false);

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

	public void hidekeys()
	{
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(etDaysNo.getWindowToken(), 0);
	}

	@Override
	public void onBackPressed()
	{
		super.onBackPressed();
		BookingFormActivity.this.finish();
		hidekeys();
		startActivity(new Intent().setClass(BookingFormActivity.this,
				LandingPage.class));
	}

	private void initUiElement()
	{

		rlBookingForm = (RelativeLayout) findViewById(R.id.rlBookingForm);
		llRetry = (LinearLayout) findViewById(R.id.llRetry);
		btRetry = (Button) findViewById(R.id.booking_retrybtn);

		pbCity = (ProgressBar) findViewById(R.id.pbCity);
		pbGroup = (ProgressBar) findViewById(R.id.pbGroup);
		pbBookType = (ProgressBar) findViewById(R.id.pbBookingType);
		pbCabType = (ProgressBar) findViewById(R.id.pbCabType);
		llOnBehalf = (LinearLayout) findViewById(R.id.llOnBehalf);
		llChkBox = (LinearLayout) findViewById(R.id.llChkBox);

		nameList = (ListView) findViewById(R.id.nameList);

		cbOnBehalf = (CheckBox) findViewById(R.id.cbOnBehalf);
		cbOnBehalf.setOnCheckedChangeListener(this);

		llPickTime = (LinearLayout) findViewById(R.id.llPickTime);
		llDaysno = (LinearLayout) findViewById(R.id.llDaysNo);
		llDropDate = (LinearLayout) findViewById(R.id.llDropDate);
		llDropTime = (LinearLayout) findViewById(R.id.llDropTime);
		llCabs = (LinearLayout) findViewById(R.id.llCabs);
		rlPickPoint = (RelativeLayout) findViewById(R.id.rlPickPoint);
		rlDropPoint = (RelativeLayout) findViewById(R.id.rlDropPoint);

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
		pd = new TransparentProgressDialog(BookingFormActivity.this,
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
							getCalDateCount(count);
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
					getCalDateCount(count);
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
		etPickupDate = (EditText) findViewById(R.id.etStartDate);
		etPickupTime = (EditText) findViewById(R.id.etTime);

		etDropDate = (EditText) findViewById(R.id.etDropDate);

		etPickupPoint = (EditText) findViewById(R.id.etPickupPoint);
		etPickupPoint.setOnClickListener(this);
		etDropPoint = (EditText) findViewById(R.id.etDropPoint);
		etDropTime = (EditText) findViewById(R.id.etEndTime);

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

		etPickupDate.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				// hidekeys added3
				// datePickerDialog.setFirstDayOfWeek(calendar.get(Calendar.DAY_OF_WEEK));

				if (year == 0)
				{
					datePickerDialog = DatePickerDialog.newInstance(
							BookingFormActivity.this,
							calendar.get(Calendar.YEAR),
							calendar.get(Calendar.MONTH),
							calendar.get(Calendar.DAY_OF_MONTH), false);
				}
				else
				{
					datePickerDialog = DatePickerDialog.newInstance(
							BookingFormActivity.this, year, month, day, false);
				}

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
				// hidekeys added3

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
						TIMEPICKER_TAG);

			}
		});

		etDropTime.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				// hidekeys added4

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

		etPickupDate.addTextChangedListener(dateWatcher);
		etPickupTime.addTextChangedListener(timeWatcher);

		etDaysNo.addTextChangedListener(watcher);

		username = (AutoCompleteTextView) findViewById(R.id.acAdUserName);
		username.addTextChangedListener(userIDWatcher);

	}

	// EditTextWacther Implementation

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

	private final TextWatcher watcher = new TextWatcher()
	{

		public void beforeTextChanged(CharSequence s, int start, int count,
				int after)
		{
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
				daysCount = Integer.valueOf(s.toString());

				if (s.length() == 2)
				{
					Function.hideSoftKeyBoard(BookingFormActivity.this);
				}

				if (daysCount > 60)
				{
					Function.showToast(BookingFormActivity.this,
							ConstantMessages.MSG38);
				}

				if (!TextUtils
						.isEmpty(etPickupDate.getText().toString().trim())
						&& !TextUtils.isEmpty(etPickupTime.getText().toString()
								.trim()))
				{
					getCalDateCount(daysCount);
				}
			}
			else
			{
				String noCabs[] =
				{ "Cab Type not available" };
				spCabType.setAdapter(populateSpinnerGeneric(noCabs));
				etDropDate.setText("");

				llDropTime.setBackgroundColor(getResources().getColor(
						R.color.light_gray));
				etDropTime.setEnabled(false);

				hideCabView();

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
				llPickTime.setBackgroundColor(getResources().getColor(
						R.color.light_blue));
				etPickupTime.setEnabled(true);

				String countVal = etDaysNo.getText().toString();
				if (!TextUtils.isEmpty(countVal)
						&& !TextUtils
								.isEmpty(etPickupTime.getText().toString()))
				{
					int count = Integer.valueOf(countVal);
					getCalDateCount(count);
				}
			}
			else
			{
				etDropDate.setText("");
			}
		}
	};

	// time watcher
	private final TextWatcher timeWatcher = new TextWatcher()
	{

		public void beforeTextChanged(CharSequence s, int start, int count,
				int after)
		{
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
				// now = Calendar.getInstance().getTime();

				String countVal = etDaysNo.getText().toString();
				if (!TextUtils.isEmpty(countVal))
				{
					int count = Integer.valueOf(countVal);

					if (!TextUtils.isEmpty(etPickupDate.getText().toString()
							.trim()))
					{
						getCalDateCount(count);
					}
				}
				else
				{
					getCalDateCount(-1);
				}
			}
			else
			{
				etDropDate.setText("");
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

		String dayName = endDateCal.getDisplayName(Calendar.DAY_OF_WEEK,
				Calendar.SHORT, Locale.getDefault());

		if (count != -1)
		{
			etDropDate.setText(convertedDate + " (" + dayName + ")");
		}

		Integer groupId = -1;
		if (!spBillTo.getAdapter().isEmpty())
		{
			// hidekeys
			groupId = ((FleetGroup) spBillTo.getSelectedItem()).getRID();

			Log.i("TAG", "groupId -->" + groupId);
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

		if (pref == 1)
		{
			params.put("Pref", "C");
		}
		else
		{
			params.put("Pref", "P");
		}

		// if (validateTime())
		// {
		// if (TextUtils.isEmpty(etPickupTime.getText().toString())
		// && TextUtils.isEmpty(etDropTime.getText().toString()))
		// Function.showToast(BookingFormActivity.this,
		// "Enter Pickup & Drop time");
		// else
		// Function.showToast(BookingFormActivity.this,
		// "Drop time should be greater than Pickup time");
		//
		// String noCabs[] =
		// { "Cab Type not available" };
		// spCabType.setAdapter(populateSpinnerGeneric(noCabs));
		// }

		if (!Function.checkForHourDiff(
				dateFormatter1.format(startDateCal.getTime()),
				dateFormatter1.format(now), 60))
		{
			Function.showToast(BookingFormActivity.this, ConstantMessages.MSG90);

			String noCabs[] =
			{ "Cab Type not available" };
			spCabType.setAdapter(populateSpinnerGeneric(noCabs));
			isValidPickupDate = false;

		}
		else if (!TextUtils.isEmpty(etDaysNo.getText().toString().trim())
				&& !TextUtils.isEmpty(etDropTime.getText().toString().trim())
				&& (!Function.checkForHourDiff(
						dateFormatter1.format(endDateCal.getTime()),
						dateFormatter1.format(startDateCal.getTime()), 30)))
		{
			Function.showToast(BookingFormActivity.this, ConstantMessages.MSG40);
			String noCabs[] =
			{ "Cab Type not available" };
			spCabType.setAdapter(populateSpinnerGeneric(noCabs));
			isValidDropDate = false;
		}
		else
		{
			isValidPickupDate = true;
			isValidDropDate = true;
			if (validateInputs())
			{
				getCabTypeRequest(params);
			}
		}

		if (!Function.checkForHourDiff(
				dateFormatter1.format(startDateCal.getTime()),
				dateFormatter1.format(now), 60))
		{
			manageView2();
		}
		else
		{
			llDaysno.setBackgroundColor(getResources().getColor(
					R.color.light_blue));
			etDaysNo.setEnabled(true);

			if (!TextUtils.isEmpty(etDaysNo.getText().toString().trim()))
			{
				llDropTime.setBackgroundColor(getResources().getColor(
						R.color.light_blue));
				llDropTime.setEnabled(true);
			}
		}

		if (!TextUtils.isEmpty(etDaysNo.getText().toString().trim())
				&& Function.checkForHourDiff(
						dateFormatter1.format(startDateCal.getTime()),
						dateFormatter1.format(now), 60))
		{

			if (daysCount <= 60)
			{
				llDropTime.setBackgroundColor(getResources().getColor(
						R.color.light_blue));
				etDropTime.setEnabled(true);
			}
			else
			{
				llDropTime.setBackgroundColor(getResources().getColor(
						R.color.light_gray));
				etDropTime.setEnabled(false);
			}

		}

		if (!TextUtils.isEmpty(etDropTime.getText().toString().trim())
				&& (!Function.checkForHourDiff(
						dateFormatter1.format(endDateCal.getTime()),
						dateFormatter1.format(startDateCal.getTime()), 30)))
		{
			hideCabView();
		}

		cabType = spCabType.getSelectedItem().toString();
		Log.d(TAG, "cabType " + cabType);
	}

	private <A> ArrayAdapter<A> populateSpinnerGeneric(A[] list)
	{
		ArrayAdapter<A> dataAdapter = new ArrayAdapter<A>(this,
				android.R.layout.simple_spinner_item, list);
		dataAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		return dataAdapter;
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
			etDropTime.setText(Function.timeFormater24Hrs(hourOfDay, minute));

			if (!TextUtils.isEmpty(etDaysNo.getText().toString())
					&& !TextUtils.isEmpty(etPickupDate.getText().toString())
					&& !TextUtils.isEmpty(etPickupTime.getText().toString()))
			{
				String countVal = etDaysNo.getText().toString();
				getCalDateCount(Integer.valueOf(countVal));
			}
			// if (!TextUtils.isEmpty(countVal))
			// {
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
			//
			// // new code in this method
			//
			// Integer groupId = -1;
			// if (!spBillTo.getAdapter().isEmpty())
			// {
			// groupId = ((FleetGroup) spBillTo.getSelectedItem())
			// .getRID();
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
			// int prefIndex = mgr
			// .getPreferenceIndex(Constant.PREFERENCE_INDEX);
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
			// Function.showToast(BookingFormActivity.this,
			// "Drop time should be greater than Pickup time..");
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
		etPickupDate.setText(getCalDate(day, month, year));

		if (!TextUtils.isEmpty(etPickupDate.getText().toString())
				&& !TextUtils.isEmpty(etPickupTime.getText().toString()))
		{
			if (TextUtils.isEmpty(etDaysNo.getText().toString()))
			{
				getCalDateCount(-1);
			}
			else
			{
				getCalDateCount(Integer.valueOf(etDaysNo.getText().toString()
						.trim()));
			}
		}
	}

	private String getCalDate(int day, int month, int year)
	{
		// Method to get Calendar date

		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_MONTH, day);
		cal.set(Calendar.MONTH, month);
		cal.set(Calendar.YEAR, year);

		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM");
		String convertedDate = dateFormat.format(cal.getTime());
		String dayName = cal.getDisplayName(Calendar.DAY_OF_WEEK,
				Calendar.SHORT, Locale.getDefault());

		// trimString
		return convertedDate + " (" + dayName + ")";
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
					pbBookType.setVisibility(View.GONE);

					JSONObject jsonObj = (JSONObject) msg.obj;
					// cabsJsonParser(jsonArr);
					Log.e(TAG, jsonObj.toString());
					parseBookingInit(jsonObj);

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
					pbBookType.setVisibility(View.GONE);

					break;

				case Constant.MessageState.FLEET_BOOK_SUCCESS:

					Log.e("FLEET_BOOK_SUCCESS", "SUCCESS");

					cancelLoadingDialog();

					pbCabType.setVisibility(View.GONE);

					JSONObject jsonFleetObj = (JSONObject) msg.obj;

					Log.e(TAG, "Success= " + jsonFleetObj.toString());

					parseFleetBooking(jsonFleetObj);

					break;

				case Constant.MessageState.FLEET_BOOK_FAIL:

					Log.e("FLEET_BOOK_FAIL", "FAIL:");

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

					Log.e(TAG, "cabty=fail");
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

	private void bookingInit()
	{
		try
		{
			showLoadingDilog();
			Log.e(TAG, "user id" + mgr.GetValueFromSharedPrefs("UserID"));
			String userId = mgr.GetValueFromSharedPrefs("UserID");
			// String userType = mgr.GetValueFromSharedPrefs("UserType");
			int prefIndex = mgr.getPreferenceIndex(Constant.PREFERENCE_INDEX);

			NetworkHandler.getBookingInit(TAG, handler, userId, prefIndex);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	private void getCabTypeRequest(Map<String, String> params)
	{
		pbCabType.setVisibility(View.VISIBLE);

		Log.e(TAG, "user id" + mgr.GetValueFromSharedPrefs("UserID"));

		NetworkHandler.getCabTypeRequest(TAG, handler, params);

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

					if (jsonObj.has("BookingType"))
					{

						BookingTypeDetail[] bookingTypGrps = gson.fromJson(
								jsonObj.getString("BookingType"),
								BookingTypeDetail[].class);

						spBookingType
								.setAdapter(populateSpinnerGeneric(bookingTypGrps));

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

							llCabs.setBackgroundColor(getResources().getColor(
									R.color.light_blue));
							spCabType.setEnabled(true);

							rlPickPoint.setBackgroundColor(getResources()
									.getColor(R.color.light_blue));
							etPickupPoint.setEnabled(true);

							if (pref == 0
									&& !TextUtils.isEmpty(etPickupPoint
											.getText().toString()))
							{
								rlDropPoint.setBackgroundColor(getResources()
										.getColor(R.color.light_blue));
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
					Function.showToast(BookingFormActivity.this,
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
		if (!TextUtils.isEmpty(etPickupDate.getText().toString())
				&& !TextUtils.isEmpty(etDropDate.getText().toString())
				&& !TextUtils.isEmpty(etPickupTime.getText().toString())
				&& !TextUtils.isEmpty(etDropTime.getText().toString())
				&& daysCount <= 60)
			return true;
		else
			return false;
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
					Function.showToast(BookingFormActivity.this,
							jsonObj.getString("Message"));
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

		if (!spCabType.getAdapter().isEmpty())
		{
			cabType = spCabType.getSelectedItem().toString();
		}

		String startDateVal = etPickupDate.getText().toString();
		// String endDateVal = etEndDate.getText().toString();
		String pickTimeVal = etPickupTime.getText().toString();
		String dropTimeVal = etDropTime.getText().toString();
		String noOfDays = etDaysNo.getText().toString();
		String pickPointVal = etPickupPoint.getText().toString();
		String userOnBehalf = username.getText().toString();

		String dropPointVal;
		if (pref == 0)
			dropPointVal = etDropPoint.getText().toString();
		else
			dropPointVal = "";

		// if (TextUtils.isDigitsOnly(userOnBehalf))
		// {
		// behalfValue = userOnBehalf;
		// }

		if (cityId == -1)
		{
			Function.showToast(BookingFormActivity.this, ConstantMessages.MSG41);
		}
		else if (groupId == -1)
		{
			Function.showToast(BookingFormActivity.this, ConstantMessages.MSG42);
		}
		// else if (bookId == -1)
		// {
		//
		// showErrorMessage(getApplicationContext(), rlErrorMessage,
		// tvErrorMessage, "Please select booking type");
		//
		// }

		else if (TextUtils.isEmpty(noOfDays))
		{
			Function.showToast(BookingFormActivity.this, ConstantMessages.MSG44);
		}
		else if (TextUtils.isEmpty(startDateVal))
		{
			Function.showToast(BookingFormActivity.this, ConstantMessages.MSG34);
		}
		else if (TextUtils.isEmpty(pickTimeVal))
		{
			Function.showToast(BookingFormActivity.this, ConstantMessages.MSG97);
		}

		else if (daysCount > 60)
		{
			Function.showToast(BookingFormActivity.this, ConstantMessages.MSG38);
		}
		else if (TextUtils.isEmpty(dropTimeVal))
		{
			Function.showToast(BookingFormActivity.this, ConstantMessages.MSG45);
		}
		else if (TextUtils.isEmpty(cabType))
		{
			Function.showToast(BookingFormActivity.this, ConstantMessages.MSG43);
		}
		else if (TextUtils.isEmpty(cabType)
				|| cabType.equalsIgnoreCase("Cab Type not available"))
		{
			Function.showToast(BookingFormActivity.this, ConstantMessages.MSG43);
		}
		else if (llOnBehalf.getVisibility() == View.VISIBLE
				&& TextUtils.isEmpty(userOnBehalf))
		{
			Function.showToast(BookingFormActivity.this, ConstantMessages.MSG46);
		}
		else if (Function.isInteger(userOnBehalf)
				&& (userOnBehalf.length() != 10))
		{
			Function.showToast(BookingFormActivity.this, ConstantMessages.MSG46);
		}
		else if (TextUtils.isEmpty(pickPointVal))
		{
			Function.showToast(BookingFormActivity.this, ConstantMessages.MSG47);
		}
		else if (pref == 0 && TextUtils.isEmpty(dropPointVal))
		{
			Function.showToast(BookingFormActivity.this, ConstantMessages.MSG48);
		}
		else
		{

			Log.e("else ", "params ");
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

			if (cbOnBehalf.isChecked())
			{
				if (Function.isInteger(username.getText().toString().trim()))
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
				Log.e(TAG, "user id" + mgr.GetValueFromSharedPrefs("UserID")
						+ " params = " + params);

				NetworkHandler.fleetBookingRequest(TAG, handler, params);
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

				if (pref == 1)
				{
					btProceed.setBackgroundResource(R.drawable.selector_button);
					btProceed.setEnabled(true);
				}
				else
				{
					rlDropPoint.setBackgroundColor(getResources().getColor(
							R.color.light_blue));
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

		// dialog.setContentView(R.layout.jugunoo_signout_confirmation_popup);
		// TextView popupMsg = (TextView) dialog.findViewById(R.id.msg);
		// popupMsg.setText(message);
		// Button popupOk = (Button) dialog.findViewById(R.id.signoutOk);

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
				Intent homeIntent = new Intent(BookingFormActivity.this,
						LandingPage.class);
				homeIntent.addFlags(Intent.FLAG_ACTIVITY_TASK_ON_HOME);
				startActivity(homeIntent);
				finish();

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
									BookingFormActivity.this, names,
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

									Function.hideSoftKeyBoard(BookingFormActivity.this);

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
				Function.showToast(BookingFormActivity.this,
						"Enter a valid Mobile Number");
			}
			if (usernameStr.length() == 10)
			{
				Function.hideSoftKeyBoard(BookingFormActivity.this);
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

	private void manageView()
	{
		llPickTime.setBackgroundColor(getResources().getColor(
				R.color.light_gray));
		llDaysno.setBackgroundColor(getResources().getColor(R.color.light_gray));
		llDropDate.setBackgroundColor(getResources().getColor(
				R.color.light_gray));
		llDropTime.setBackgroundColor(getResources().getColor(
				R.color.light_gray));
		llCabs.setBackgroundColor(getResources().getColor(R.color.light_gray));
		rlPickPoint.setBackgroundColor(getResources().getColor(
				R.color.light_gray));
		rlDropPoint.setBackgroundColor(getResources().getColor(
				R.color.light_gray));
		btProceed.setBackgroundColor(getResources().getColor(
				R.color.light_green));

		etPickupTime.setEnabled(false);
		etDaysNo.setEnabled(false);
		etDropTime.setEnabled(false);
		spCabType.setEnabled(false);
		etPickupPoint.setEnabled(false);
		etDropPoint.setEnabled(false);
		cbOnBehalf.setEnabled(false);
		btProceed.setEnabled(false);
	}

	private void manageView2()
	{
		llDaysno.setBackgroundColor(getResources().getColor(R.color.light_gray));
		llDropTime.setBackgroundColor(getResources().getColor(
				R.color.light_gray));
		llCabs.setBackgroundColor(getResources().getColor(R.color.light_gray));
		rlPickPoint.setBackgroundColor(getResources().getColor(
				R.color.light_gray));
		rlDropPoint.setBackgroundColor(getResources().getColor(
				R.color.light_gray));
		etDaysNo.setEnabled(false);
		etDropTime.setEnabled(false);
		spCabType.setEnabled(false);
		etPickupPoint.setEnabled(false);
		etDropPoint.setEnabled(false);
	}

	private void hideCabView()
	{
		llCabs.setBackgroundColor(getResources().getColor(R.color.light_gray));
		spCabType.setEnabled(false);

		rlPickPoint.setBackgroundColor(getResources().getColor(
				R.color.light_gray));
		etPickupPoint.setEnabled(false);

		rlDropPoint.setBackgroundColor(getResources().getColor(
				R.color.light_gray));
		etDropPoint.setEnabled(false);
	}

	@Override
	public void onStop()
	{
		super.onStop();
		hidekeys();
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		hidekeys();
	}
}
