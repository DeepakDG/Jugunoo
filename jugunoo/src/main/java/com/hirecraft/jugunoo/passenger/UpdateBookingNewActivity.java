package com.hirecraft.jugunoo.passenger;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.fourmob.datetimepicker.date.DatePickerDialog.OnDateSetListener;
import com.google.gson.Gson;
import com.hirecraft.jugunoo.passenger.common.City;
import com.hirecraft.jugunoo.passenger.common.Constant;
import com.hirecraft.jugunoo.passenger.common.ConstantMessages;
import com.hirecraft.jugunoo.passenger.common.EngDetails;
import com.hirecraft.jugunoo.passenger.common.FleetGroup;
import com.hirecraft.jugunoo.passenger.common.Function;
import com.hirecraft.jugunoo.passenger.common.Global;
import com.hirecraft.jugunoo.passenger.common.TransparentProgressDialog;
import com.hirecraft.jugunoo.passenger.networkhandler.NetworkHandler;
import com.hirecraft.jugunoo.passenger.services.VolleyErrorHelper;
import com.hirecraft.jugunoo.passenger.triplog.TripLogActivity;
import com.hirecraft.jugunoo.passenger.utility.SharedPreferencesManager;
import com.sleepbot.datetimepicker.time.RadialPickerLayout;
import com.sleepbot.datetimepicker.time.TimePickerDialog;

import de.keyboardsurfer.android.widget.crouton.Crouton;

public class UpdateBookingNewActivity extends FragmentActivity implements
		OnDateSetListener, TimePickerDialog.OnTimeSetListener, OnClickListener
{
	private Spinner spSelectCity;
	private Spinner spBillTo;
	private Spinner spCabType;

	private EditText etStartDate;
	private EditText etEndDate;
	private EditText etPickupPoint;
	private EditText etDropPoint;
	private EditText etStartTime;
	private EditText etEndTime;

	private Button btProceed;
	private Button btCancel;

	private Calendar calendar;
	private DatePickerDialog datePickerDialog;
	private TimePickerDialog timePickerDialog;

	private int startDay;
	private int startMonth;
	private int startYear;

	private int endDay;
	private int endMonth;
	private int endYear;

	private int startHourOfDay, endHourOfDay;
	private int startMinute, endMinute;

	private TransparentProgressDialog pd;
	private Handler pdHandler;
	private Runnable pdRunnable;
	private SharedPreferencesManager mgr;

	private String pickLatLng, dropLatLng;

	private ProgressBar pbCity, pbGroup, pbCabType;

	private String engId;
	private String prefType;
	private EngDetails engDetailsObj;

	private RelativeLayout rlUpdateBookingForm;
	private LinearLayout llRetry;
	private Button btRetry;
	private RelativeLayout rlDropPoint;

	private static final String TAG = UpdateBookingNewActivity.class
			.getSimpleName();

	private Dialog dialog;

	private String startDate = "";
	private String endDate = "";

	public static final String START_DATE_PICKERTAG = "startDatePicker";
	public static final String DROP_DATE_PICKERTAG = "endDatePicker";
	public static final String START_TIME_PICKERTAG = "startTimepicker";
	public static final String DROP_TIME_PICKER = "dropTimepicker";

	private Date now;
	private boolean isFirstTime = true;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		SetActionBar();
		setContentView(R.layout.activity_update_booking_new);

		dialog = new Dialog(UpdateBookingNewActivity.this);
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

	private void makeBookingInitReq()
	{
		try
		{
			showLoadingDilog();
			NetworkHandler.getBookingDetails(TAG, handler, engId,
					mgr.GetValueFromSharedPrefs("UserID"));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
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
		llRetry = (LinearLayout) findViewById(R.id.llRetry);
		btRetry = (Button) findViewById(R.id.updateBooking_retrybtn);

		rlDropPoint = (RelativeLayout) findViewById(R.id.rlDropPoint);

		pbCity = (ProgressBar) findViewById(R.id.pbCity);
		pbGroup = (ProgressBar) findViewById(R.id.pbGroup);
		pbCabType = (ProgressBar) findViewById(R.id.pbCabType);

		pdHandler = new Handler();
		pd = new TransparentProgressDialog(UpdateBookingNewActivity.this,
				R.drawable.loading_image);

		spSelectCity = (Spinner) findViewById(R.id.spCity);

		spSelectCity
				.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
				{
					@Override
					public void onItemSelected(AdapterView<?> arg0, View arg1,
							int arg2, long arg3)
					{
						if (!isFirstTime)
						{
							makeGetCabRequest(false);
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
				if (!isFirstTime)
				{
					makeGetCabRequest(false);
				}
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
		etDropPoint.setOnClickListener(this);

		btProceed = (Button) findViewById(R.id.btProceed);
		btProceed.setOnClickListener(this);

		btCancel = (Button) findViewById(R.id.btCancel);
		btCancel.setOnClickListener(this);

		etStartDate.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				datePickerDialog = DatePickerDialog.newInstance(
						UpdateBookingNewActivity.this, startYear, startMonth,
						startDay, false);
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
				datePickerDialog = DatePickerDialog.newInstance(
						UpdateBookingNewActivity.this, endYear, endMonth,
						endDay, false);
				datePickerDialog.setYearRange(calendar.get(Calendar.YEAR),
						calendar.get(Calendar.YEAR) + 1);
				// datePickerDialog
				// .setCloseOnSingleTapDay(isCloseOnSingleTapDay());
				datePickerDialog.show(getSupportFragmentManager(),
						DROP_DATE_PICKERTAG);
			}
		});

		etStartTime.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				// timePickerDialog
				// .setCloseOnSingleTapMinute(isCloseOnSingleTapMinute());

				timePickerDialog.setStartTime(startHourOfDay, startMinute);
				timePickerDialog.show(getSupportFragmentManager(),
						START_TIME_PICKERTAG);
			}
		});

		etEndTime.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				timePickerDialog.setStartTime(endHourOfDay, endMinute);
				timePickerDialog.show(getSupportFragmentManager(),
						DROP_TIME_PICKER);
			}
		});
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

	@Override
	public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute)
	{
		Log.e(TAG, "hourOfDay=" + hourOfDay + " " + minute);

		String tag = timePickerDialog.getTag();
		if (tag.equals(START_TIME_PICKERTAG))
		{
			this.startHourOfDay = hourOfDay;
			this.startMinute = minute;
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
				makeGetCabRequest(false);
			}
			else
			{
				Function.showToast(UpdateBookingNewActivity.this,
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
				makeGetCabRequest(false);
			}
			else
			{
				Function.showToast(UpdateBookingNewActivity.this,
						ConstantMessages.MSG40);

				makeGetCabRequest(true);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
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
				Function.showToast(UpdateBookingNewActivity.this,
						ConstantMessages.MSG103);

				makeGetCabRequest(true);
			}
			else
			{
				makeGetCabRequest(false);
			}
		}
		else
		{
			endDate = dateFormat2.format(cal.getTime());

			if (Function.isDateLessThanCurrentDate(endDate))
			{
				Function.showToast(UpdateBookingNewActivity.this,
						ConstantMessages.MSG103);

				makeGetCabRequest(true);
			}
			else
			{
				makeGetCabRequest(false);
			}
		}
		return convertedDate;
	}

	private void makeGetCabRequest(boolean isSetNoCabs)
	{
		now = Calendar.getInstance().getTime();

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

			String endDateTime = endDate + " " + etEndTime.getText().toString()
					+ ":" + "00";

			Log.i("TAG", "date test" + startDateTime);
			Log.i("TAG", "date test" + endDateTime);

			if (!Function.checkForHourDiff(startDateTime,
					dateFormatter1.format(now), 60))
			{
				Function.showToast(UpdateBookingNewActivity.this,
						ConstantMessages.MSG90);
			}
			else if (!Function.checkForHourDiff(endDateTime, startDateTime, 30))
			{
				Function.showToast(UpdateBookingNewActivity.this,
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

	private static void showErrorMessage(final Context context, String message)
	{
		Function.showToast(context, message);
	}

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

					parseBookingDetails((JSONObject) msg.obj);

					prefType = engDetailsObj.getPref();
					if (prefType.equalsIgnoreCase("P"))
					{
						rlDropPoint.setVisibility(View.VISIBLE);
					}
					else
					{
						rlDropPoint.setVisibility(View.GONE);
					}
					break;

				case Constant.MessageState.GET_BOOKING_DETAILS_FAIL:
					VolleyErrorHelper.getMessage(msg.obj,
							UpdateBookingNewActivity.this, true);

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
					break;

				case Constant.MessageState.FLEET_BOOK_UPDATE_SUCCESS:
					cancelLoadingDialog();
					pbCabType.setVisibility(View.GONE);
					parseUpdateBooking((JSONObject) msg.obj);
					break;

				case Constant.MessageState.FLEET_BOOK_UPDATE_FAIL:
					VolleyErrorHelper.getMessage(msg.obj,
							UpdateBookingNewActivity.this, true);
					cancelLoadingDialog();
					pbCabType.setVisibility(View.GONE);
					break;

				case Constant.MessageState.CAB_TYPE_UPDATE_SUCCESS:
					pbCabType.setVisibility(View.GONE);
					parseReqCabType((JSONObject) msg.obj);

					break;
				case Constant.MessageState.CAB_TYPE_UPDATE_FAIL:
					Log.e(TAG, "cabty=fail");
					pbCabType.setVisibility(View.GONE);
					break;

				default:
					break;
			}
		}
	};

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
		params.put("Pref", engDetailsObj.getPref());
		params.put("EngId", engId);

		pbCabType.setVisibility(View.VISIBLE);
		Log.e(TAG, "user id" + mgr.GetValueFromSharedPrefs("UserID"));

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

	private void parseBookingDetails(JSONObject jsonObj)
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

						this.startDay = Integer.valueOf(Function
								.getDateTimeFromUTC(engDetailsObj
										.getStartDate().toString(), "dd"));

						this.startMonth = Integer.valueOf(Function
								.getDateTimeFromUTC(engDetailsObj
										.getStartDate().toString(), "MM")) - 1;

						this.startYear = Integer.valueOf(Function
								.getDateTimeFromUTC(engDetailsObj
										.getStartDate().toString(), "yyyy"));

						etStartDate.setText(Function.getDateTimeFromUTC(
								engDetailsObj.getStartDate().toString(),
								"dd-MMM (EEE)"));

						this.startHourOfDay = Integer.valueOf(Function
								.getDateTimeFromUTC(engDetailsObj
										.getStartDate().toString(), "HH"));
						this.startMinute = Integer.valueOf(Function
								.getDateTimeFromUTC(engDetailsObj
										.getStartDate().toString(), "mm"));

						etStartTime.setText(Function.getDateTimeFromUTC(
								engDetailsObj.getStartDate().toString(),
								"HH:mm"));

						// Calendar endCal = Calendar.getInstance();
						// endCal.setTimeInMillis(engDetailsObj.getEndDate());

						this.endHourOfDay = Integer.valueOf(Function
								.getDateTimeFromUTC(engDetailsObj.getEndDate()
										.toString(), "HH"));
						this.endMinute = Integer.valueOf(Function
								.getDateTimeFromUTC(engDetailsObj.getEndDate()
										.toString(), "mm"));

						etEndDate.setText(Function.getDateTimeFromUTC(
								engDetailsObj.getEndDate().toString(),
								"dd-MMM (EEE)"));

						etEndTime
								.setText(Function.getDateTimeFromUTC(
										engDetailsObj.getEndDate().toString(),
										"HH:mm"));

						this.endDay = Integer.valueOf(Function
								.getDateTimeFromUTC(engDetailsObj.getEndDate()
										.toString(), "dd"));

						this.endMonth = Integer.valueOf(Function
								.getDateTimeFromUTC(engDetailsObj.getEndDate()
										.toString(), "MM")) - 1;

						this.endYear = Integer.valueOf(Function
								.getDateTimeFromUTC(engDetailsObj.getEndDate()
										.toString(), "yyyy"));

						startDate = Function.getDateTimeFromUTC(engDetailsObj
								.getStartDate().toString(), "MM/dd/yyyy");
						endDate = Function.getDateTimeFromUTC(engDetailsObj
								.getEndDate().toString(), "MM/dd/yyyy");

						// Loading cabs
						String startDateTime = startDate + " "
								+ etStartTime.getText().toString() + ":" + "00";

						String endDateTime = endDate + " "
								+ etEndTime.getText().toString() + ":" + "00";

						getCabTypeRequest(startDateTime, endDateTime);
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
					// if (isFirstTime)
					// {
					// String noCabs[] =
					// { engDetailsObj.getCabType() };
					// spCabType.setAdapter(populateSpinnerGeneric(noCabs));
					// isFirstTime = false;
					// }
					// else
					// {
					Function.showToast(UpdateBookingNewActivity.this,
							jsonObj.getString("Message"));

					String noCabs[] =
					{ "Cab Type not available" };
					spCabType.setAdapter(populateSpinnerGeneric(noCabs));
					// }
				}
			}
		}
		catch (Exception bug)
		{
			bug.printStackTrace();
		}
	}

	private void parseUpdateBooking(JSONObject jsonObj)
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
					Function.showToast(UpdateBookingNewActivity.this,
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

				Function.hideSoftKeyBoard(UpdateBookingNewActivity.this);

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
		UpdateBookingNewActivity.this.finish();
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

			String cabType = "";
			if (!spCabType.getAdapter().isEmpty())
			{
				cabType = spCabType.getSelectedItem().toString();
			}

			String pickPointVal = etPickupPoint.getText().toString();
			String dropPointVal;
			if (prefType.equalsIgnoreCase("P"))
				dropPointVal = etDropPoint.getText().toString();
			else
				dropPointVal = "";

			now = Calendar.getInstance().getTime();
			SimpleDateFormat dateFormatter1 = new SimpleDateFormat(
					"MM/dd/yyyy HH:mm:ss");

			String startDateTime = startDate + " "
					+ etStartTime.getText().toString() + ":" + "00";

			String endDateTime = endDate + " " + etEndTime.getText().toString()
					+ ":" + "00";

			if (cityId == -1)
			{
				showErrorMessage(UpdateBookingNewActivity.this,
						ConstantMessages.MSG41);
			}
			else if (groupId == -1)
			{
				showErrorMessage(UpdateBookingNewActivity.this,
						ConstantMessages.MSG42);
			}
			else if (!Function.checkForHourDiff(startDateTime,
					dateFormatter1.format(now), 60))
			{
				Function.showToast(UpdateBookingNewActivity.this,
						ConstantMessages.MSG90);
			}
			else if (!Function.checkForHourDiff(endDateTime, startDateTime, 30))
			{
				Function.showToast(UpdateBookingNewActivity.this,
						ConstantMessages.MSG40);
			}
			else if (TextUtils.isEmpty(cabType)
					|| cabType.equalsIgnoreCase("Cab Type not available"))
			{
				showErrorMessage(UpdateBookingNewActivity.this,
						ConstantMessages.MSG43);
			}
			else if (TextUtils.isEmpty(pickPointVal))
			{
				showErrorMessage(UpdateBookingNewActivity.this,
						ConstantMessages.MSG47);
			}
			else if (prefType.equalsIgnoreCase("P")
					&& TextUtils.isEmpty(dropPointVal))
			{
				Function.showToast(UpdateBookingNewActivity.this,
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
				params.put("CabType", cabType);
				params.put("PickPoint", pickPointVal);
				params.put("Days", "0");
				params.put("DropPoint", dropPointVal);
				params.put("Picklatlng", pickLatLng);
				params.put("Droplatlng", dropLatLng);
				params.put("EngId", engId);
				params.put("Operation", "U");
				params.put("Pref", engDetailsObj.getPref());

				try
				{
					showLoadingDilog();
					Log.e(TAG,
							"user id" + mgr.GetValueFromSharedPrefs("UserID")
									+ " p=" + params);

					NetworkHandler.fleetBookingUpdateRequest(TAG, handler,
							params);
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
				UpdateBookingNewActivity.this.finish();

				Intent homeIntent = new Intent(UpdateBookingNewActivity.this,
						TripLogActivity.class);
				homeIntent.addFlags(Intent.FLAG_ACTIVITY_TASK_ON_HOME);
				startActivity(homeIntent);

			}
		});

		dialog.show();

	}

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
