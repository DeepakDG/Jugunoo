package com.hirecraft.jugunoo.passenger.triplog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Dialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.fourmob.datetimepicker.date.DatePickerDialog.OnDateSetListener;
import com.hirecraft.jugunoo.passenger.LandingPage;
import com.hirecraft.jugunoo.passenger.R;
import com.hirecraft.jugunoo.passenger.R.color;
import com.hirecraft.jugunoo.passenger.R.drawable;
import com.hirecraft.jugunoo.passenger.R.id;
import com.hirecraft.jugunoo.passenger.R.layout;
import com.hirecraft.jugunoo.passenger.R.string;
import com.hirecraft.jugunoo.passenger.adapter.TripLogAdapter;
import com.hirecraft.jugunoo.passenger.common.Constant;
import com.hirecraft.jugunoo.passenger.common.Function;
import com.hirecraft.jugunoo.passenger.common.TransparentProgressDialog;
import com.hirecraft.jugunoo.passenger.common.Validation;
import com.hirecraft.jugunoo.passenger.networkhandler.NetworkHandler;
import com.hirecraft.jugunoo.passenger.utility.JugunooUtil;
import com.hirecraft.jugunoo.passenger.utility.SharedPreferencesManager;

import de.keyboardsurfer.android.widget.crouton.Crouton;

public class TripLogActivity extends FragmentActivity implements
		ActionBar.TabListener, OnDateSetListener
{
	private static final String TAG = TripLogActivity.class.getSimpleName();
	private String startDate;
	private String endDate;
	private String selectedStatus;
	private String selectedPref;

	private double startDateMills;
	private double endDateMills;

	private ActionBar actionBar;

	private TextView save;

	private Handler h;

	private TransparentProgressDialog pd;

	private SharedPreferencesManager mgr;

	private Runnable r;

	private boolean flag;

	private Dialog dialog;

	private int year = 0;
	private int month = 0;
	private int day = 0;

	private int endyear = 0;
	private int endmonth = 0;
	private int endday = 0;

	private Calendar calendar;

	public static final String DATEPICKER_TAG = "datepicker";

	private Calendar endcalendar;
	// Declaring our tabs and the corresponding fragments.
	private DatePickerDialog datePickerDialog;

	private DatePickerDialog enddatePickerDialog;

	private EditText etTriplogStartDate;
	private EditText etTriplogEndDate;
	private EditText tripLogEmail;

	private ViewPager viewPager;
	private TripLogAdapter mAdapter;
	// Tab titles
	private String[] tabs =
	{ "Pending", "On-Going", "Completed" };

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_triplogs);

		SetActionBar();
		calendar = Calendar.getInstance();
		init();
		mgr = new SharedPreferencesManager(getApplicationContext());
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		Crouton.cancelAllCroutons();
	}

	private void init()
	{
		// Initilization
		// viewPager = (ViewPager) findViewById(R.id.pager);
		actionBar = getActionBar();

		// viewPager.setAdapter(mAdapter);
		actionBar.setHomeButtonEnabled(false);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		viewPager = (ViewPager) findViewById(R.id.trip_pager);
		mAdapter = new TripLogAdapter(getSupportFragmentManager());
		viewPager.setAdapter(mAdapter);
		viewPager.setPageMargin(10);
		viewPager.setPageMarginDrawable(R.color.black);
		actionBar.setHomeButtonEnabled(false);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// Adding tabs
		for (String tab_name : tabs)
		{
			actionBar.addTab(actionBar.newTab().setText(tab_name)
					.setTabListener(TripLogActivity.this));
		}

		viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener()
		{

			@Override
			public void onPageSelected(int position)
			{
				actionBar.setSelectedNavigationItem(position);
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2)
			{

			}

			@Override
			public void onPageScrollStateChanged(int arg0)
			{

			}
		});

	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft)
	{
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft)
	{
		// on tab selected
		// show respected fragment view
		viewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft)
	{
	}

	private String selectedTripsSpinnerItem(int position)
	{
		String item = "All";

		if (position == 0)
		{
			return "All";
		}
		else if (position == 1)
		{
			return "P";
		}
		else if (position == 2)
		{
			return "PC";
		}
		else if (position == 3)
		{
			return "PF";
		}

		return item;
	}

	private String selectedPrefTripsSpinnerItem(int position)
	{
		String item = "All";

		if (position == 0)
		{
			return "All";
		}
		else if (position == 1)
		{
			return "P";
		}
		else if (position == 2)
		{
			return "C";
		}

		return item;
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
		View bar = inflater.inflate(R.layout.custom_actionbar_triplog, l);

		LinearLayout back = (LinearLayout) bar.findViewById(R.id.llCTitle);
		TextView title = (TextView) bar.findViewById(R.id.tvTATitle);
		title.setText("Trip Log");
		TextView export = (TextView) bar.findViewById(R.id.tvExport);
		export.setText("Export");

		export.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				JugunooExportAlertDialog();
			}
		});

		back.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Intent intent = new Intent(TripLogActivity.this,
						LandingPage.class);
				startActivity(intent);
				finish();
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
		if (viewPager.getCurrentItem() == 0)
		{
			// Back button. This calls finish() on this activity and pops the
			Intent intent = new Intent(this, LandingPage.class);
			startActivity(intent);
			finish();
		}
		else
		{
			// Otherwise, select the previous step.
			viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
		}

	}

	private String getEndCalDate(int endday, int endmonth, int endyear,
			String format)
	{
		// Method to get Calendar date

		Calendar endCal = Calendar.getInstance();
		endCal.set(Calendar.DAY_OF_MONTH, endday);
		endCal.set(Calendar.MONTH, endmonth);
		endCal.set(Calendar.YEAR, endyear);

		SimpleDateFormat dateFormat = new SimpleDateFormat(format);
		String convertedDate = dateFormat.format(endCal.getTime());

		return convertedDate;
	}

	private String getStartCalDate(int day, int month, int year, String format)
	{
		// Method to get Calendar date

		Calendar cal = Calendar.getInstance();

		cal.set(Calendar.DAY_OF_MONTH, day);
		cal.set(Calendar.MONTH, month);
		cal.set(Calendar.YEAR, year);

		SimpleDateFormat dateFormat = new SimpleDateFormat(format);
		String convertedDate = dateFormat.format(cal.getTime());

		return convertedDate;
	}

	private void showLoadingDilogTr()
	{
		// Showing loading dialog

		h = new Handler();
		pd = new TransparentProgressDialog(TripLogActivity.this,
				R.drawable.loading_image);

		r = new Runnable()
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

	private void makeExportRequest()
	{
		String userId = mgr.GetValueFromSharedPrefs("UserID");

		String emailstr = tripLogEmail.getText().toString();

		// boolean isSuccessValidation = checkValidation();
		String tag_json_obj = "Export TRip log ------>";

		if (checkValidation())
		{

			showLoadingDilogTr();

			Map<String, String> params = new HashMap<String, String>();
			params.put("UserId", userId);
			params.put("Status", selectedStatus);
			params.put("StartDate", startDate);
			params.put("EndDate", endDate);
			params.put("ToAddr", emailstr);
			params.put("Pref", selectedPref);

			NetworkHandler.ExportTripLog(tag_json_obj, handlerExportTrips,
					params);

		}
	}

	private void cancelLoadingDialogTr()
	{
		// Cancel loading dialog

		h.removeCallbacks(r);

		if (pd.isShowing())
		{
			pd.dismiss();

		}
	}

	Handler handlerExportTrips = new Handler()
	{

		public void handleMessage(Message msg)
		{

			switch (msg.arg1)
			{

				case Constant.MessageState.EXPORT_TRIPLOG_SUCCESS:
					Log.d("handlerExportTrips",
							"handlerExportTrips EXPORT_TRIPLOG_SUCCESS");
					cancelLoadingDialogTr();
					dialog.dismiss();

					ExportTripLogsResponse((JSONObject) msg.obj);
					break;

				case Constant.MessageState.FAIL:
					Log.d("handlerExportTrips",
							"handlerExportTrips EXPORT_TRIPLOG_FAIL");
					cancelLoadingDialogTr();
					dialog.dismiss();
					Function.showToast(TripLogActivity.this,
							"Failed to export triplogs. Please try later");
					break;
			}
		}
	};

	private void ExportTripLogsResponse(JSONObject response)
	{

		try
		{
			Function.showToast(TripLogActivity.this,
					response.getString("Message"));
		}
		catch (JSONException e)
		{

			e.printStackTrace();
		}

	}

	/* Export triplog dialogbox */
	public void JugunooExportAlertDialog()
	{

		dialog = new Dialog(TripLogActivity.this);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.activity_export_triplogdialog);
		dialog.setCancelable(false);
		tripLogEmail = (EditText) dialog.findViewById(R.id.tripLogEmail);

		etTriplogStartDate = (EditText) dialog
				.findViewById(R.id.etTriplogStartDate);
		etTriplogEndDate = (EditText) dialog
				.findViewById(R.id.etTriplogEndDate);

		Button btnTriplogSubmit = (Button) dialog
				.findViewById(R.id.btnTriplogSubmit);
		Button btnTriplogCancel = (Button) dialog
				.findViewById(R.id.btnTriplogCancel);

		/* Trips log spinner start point */

		Spinner extportTripsSpinner = (Spinner) dialog
				.findViewById(R.id.extportTripsSpOView);

		List<String> tripslogspinnerList = new ArrayList<String>();
		tripslogspinnerList.add("All");
		tripslogspinnerList.add("Pending Trips");
		tripslogspinnerList.add("Cancelled Trips");
		tripslogspinnerList.add("Completed Trips");

		ArrayAdapter<String> tripsdataAdapter = new ArrayAdapter<String>(
				TripLogActivity.this, android.R.layout.simple_spinner_item,
				tripslogspinnerList);
		tripsdataAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		extportTripsSpinner.setAdapter(tripsdataAdapter);

		extportTripsSpinner
				.setOnItemSelectedListener(new OnItemSelectedListener()
				{

					@Override
					public void onItemSelected(AdapterView<?> parent,
							View view, int position, long id)
					{
						selectedStatus = selectedTripsSpinnerItem(position);

					}

					@Override
					public void onNothingSelected(AdapterView<?> parent)
					{

					}
				});
		/* Trips log spinner end point */

		/* Pref Trips log spinner Start point */
		Spinner prefTripsSpinner = (Spinner) dialog
				.findViewById(R.id.prefTripsSpOView);

		List<String> preflogspinnerList = new ArrayList<String>();
		preflogspinnerList.add("Both");
		preflogspinnerList.add("Personal Bookings");
		preflogspinnerList.add("Corporate Bookings");

		ArrayAdapter<String> prefdataAdapter = new ArrayAdapter<String>(
				TripLogActivity.this, android.R.layout.simple_spinner_item,
				preflogspinnerList);
		prefdataAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		prefTripsSpinner.setAdapter(prefdataAdapter);

		prefTripsSpinner.setOnItemSelectedListener(new OnItemSelectedListener()
		{

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id)
			{
				selectedPref = selectedPrefTripsSpinnerItem(position);

			}

			@Override
			public void onNothingSelected(AdapterView<?> parent)
			{

			}
		});

		/* Pref Trips log spinner end point */

		/*
		 * buttons on click functionality
		 */
		etTriplogStartDate.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				Function.hideSoftKeyBoard(TripLogActivity.this,
						etTriplogStartDate);
				flag = false;
				if (year == 0)
				{
					datePickerDialog = DatePickerDialog.newInstance(
							TripLogActivity.this, calendar.get(Calendar.YEAR),
							calendar.get(Calendar.MONTH),
							calendar.get(Calendar.DAY_OF_MONTH), false);

				}
				else
				{

					enddatePickerDialog = DatePickerDialog.newInstance(
							TripLogActivity.this, year, month, day, false);
				}

				datePickerDialog.setYearRange(calendar.get(Calendar.YEAR) - 1,
						calendar.get(Calendar.YEAR) + 1);

				datePickerDialog.show(getSupportFragmentManager(),
						DATEPICKER_TAG);

			}
		});

		etTriplogEndDate.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				Function.hideSoftKeyBoard(TripLogActivity.this,
						etTriplogEndDate);
				flag = true;

				endcalendar = Calendar.getInstance();

				if (endyear == 0)
				{
					enddatePickerDialog = DatePickerDialog.newInstance(
							TripLogActivity.this,
							endcalendar.get(Calendar.YEAR),
							endcalendar.get(Calendar.MONTH),
							endcalendar.get(Calendar.DAY_OF_MONTH), false);
				}

				enddatePickerDialog.setYearRange(
						endcalendar.get(Calendar.YEAR) - 1,
						endcalendar.get(Calendar.YEAR) + 1);

				enddatePickerDialog.show(getSupportFragmentManager(),
						DATEPICKER_TAG);

			}
		});

		btnTriplogSubmit.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{

				if (JugunooUtil.isConnectedToInternet(getApplicationContext()))
				{
					makeExportRequest();
				}
				else
				{
					Function.showToast(TripLogActivity.this, getResources()
							.getString(R.string.connection_error));
				}

			}
		});

		btnTriplogCancel.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				year = 0;
				endyear = 0;
				dialog.dismiss();
			}
		});

		dialog.show();
	}

	@Override
	public void onDateSet(DatePickerDialog datePickerDialog, int year,
			int month, int day)
	{
		if (flag)
		{
			endday = day;
			endmonth = month;
			endyear = year;
			endDate = getEndCalDate(endday, endmonth, endyear, "MM-dd-yyyy");
			endDateMills = Long.parseLong(Function.getMillsTs(endDate,
					"MM-dd-yyyy"));
			etTriplogEndDate.setText(getEndCalDate(endday, endmonth, endyear,
					"dd-MMM (EEE)"));

			// flag = false;
		}
		else
		{
			this.day = day;
			this.month = month;
			this.year = year;

			startDate = getStartCalDate(day, month, year, "MM-dd-yyyy");
			startDateMills = Long.parseLong(Function.getMillsTs(startDate,
					"MM-dd-yyyy"));
			etTriplogStartDate.setText(getStartCalDate(day, month, year,
					"dd-MMM (EEE)"));

		}

	}

	private boolean checkValidation()
	{

		String tripLogEmailstr = tripLogEmail.getText().toString();
		String etTriplogStartDateStr = etTriplogStartDate.getText().toString();
		String etTriplogEndDateStr = etTriplogEndDate.getText().toString();

		if (TextUtils.isEmpty(tripLogEmailstr))
		{

			Function.hideSoftKeyBoard(TripLogActivity.this, tripLogEmail);
			Toast.makeText(TripLogActivity.this, "Email cannot be empty",
					Toast.LENGTH_SHORT).show();
		}

		else if (!Validation.isValidE(tripLogEmail, Validation.EMAIL_REGEX,
				null))
		{
			Toast.makeText(TripLogActivity.this, "Enter valid email",
					Toast.LENGTH_SHORT).show();
		}

		else if (TextUtils.isEmpty(etTriplogStartDateStr))
		{
			Function.hideSoftKeyBoard(TripLogActivity.this, etTriplogStartDate);
			Toast.makeText(TripLogActivity.this, "Enter Start Date",
					Toast.LENGTH_SHORT).show();

		}
		else if (TextUtils.isEmpty(etTriplogEndDateStr))
		{
			Function.hideSoftKeyBoard(TripLogActivity.this, etTriplogEndDate);
			Toast.makeText(TripLogActivity.this, "Enter End Date",
					Toast.LENGTH_SHORT).show();
		}
		else if (startDateMills > endDateMills)
		{

			Toast.makeText(TripLogActivity.this,
					"End date should be greater than start date",
					Toast.LENGTH_SHORT).show();
		}
		else
		{
			return true;
		}
		return false;
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
