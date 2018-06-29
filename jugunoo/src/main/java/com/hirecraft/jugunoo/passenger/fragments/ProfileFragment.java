package com.hirecraft.jugunoo.passenger.fragments;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.fourmob.datetimepicker.date.DatePickerDialog.OnDateSetListener;
import com.hirecraft.jugunoo.passenger.AddCorporateInfoActivity;
import com.hirecraft.jugunoo.passenger.FavoriteDriversListActivity;
import com.hirecraft.jugunoo.passenger.ProfileActivity;
import com.hirecraft.jugunoo.passenger.R;
import com.hirecraft.jugunoo.passenger.common.Constant;
import com.hirecraft.jugunoo.passenger.common.ConstantMessages;
import com.hirecraft.jugunoo.passenger.common.Validation;
import com.hirecraft.jugunoo.passenger.networkhandler.NetworkHandler;
import com.hirecraft.jugunoo.passenger.utility.SharedPreferencesManager;

public class ProfileFragment extends Fragment implements OnDateSetListener
{

	private static final String TAG = ProfileFragment.class.getSimpleName();
	private SharedPreferencesManager mgr;
	private Handler pdHandler;
	private Runnable pdRunnable;
	private Typeface light, semibold;
	private TextView mobile, title, email, exporttxt, txtptrip, txtpdistance,
			txtpamount, txtctrip, txtcdistance, txtcamount, txtcompanyname;

	public static String gName = "";
	public static String gMobile = "";
	public static String gEmail = "";
	public static String gptrip = "";
	public static String gpdistance = "";
	public static String gpamount = "";
	public static String gctrip = "";
	public static String gcdistance = "";
	public static String gcamount = "";
	public static String gccompany = "";

	private LinearLayout Netstate_layout;
	private Button retryBtn, favorite_drivers, removeBtn;
	private LinearLayout contentll;
	private ScrollView Profilemain_layout;
	private ProgressBar progressBar;

	private String startDate;
	private String endDate;
	private String selectedStatus;
	private String selectedPref;

	private double startDateMills;
	private double endDateMills;
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
	private EditText personalEmailID;
	String validEmainId;
	private Button btnprofileEdit;
	private EditText name;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.profile, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);

		calendar = Calendar.getInstance();
		mgr = new SharedPreferencesManager(getActivity());
		pdHandler = new Handler();
		progressBar = (ProgressBar) getActivity().findViewById(
				R.id.profileProgress);
		progressBar.getIndeterminateDrawable().setColorFilter(
				Color.rgb(155, 219, 70),
				android.graphics.PorterDuff.Mode.MULTIPLY);
		Profilemain_layout = (ScrollView) getActivity().findViewById(
				R.id.main_container);
		// contentll = (LinearLayout) getActivity().findViewById(R.id.contentL);
		Netstate_layout = (LinearLayout) getActivity().findViewById(
				R.id.profile_ErrorState);
		retryBtn = (Button) getActivity().findViewById(R.id.profile_retrybtn);
		removeBtn = (Button) getActivity().findViewById(R.id.but_remove);
		btnprofileEdit = (Button) getActivity()
				.findViewById(R.id.but_emailedit);
		exporttxt = (TextView) getActivity().findViewById(R.id.txt_export);
		favorite_drivers = (Button) getActivity().findViewById(
				R.id.favorite_but);

		removeBtn.setBackgroundResource(R.drawable.ic_action_remove);
		removeBtn.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				if (removeBtn.getBackground().getConstantState() == getResources()
						.getDrawable(R.drawable.ic_action_remove)
						.getConstantState())
				{
					Confirmationdialog();
				}
				else
				{
					startActivity(new Intent(getActivity(),
							AddCorporateInfoActivity.class));
				}
			}
		});
		btnprofileEdit.setBackgroundResource(R.drawable.ic_action_edit);
		btnprofileEdit.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				if (btnprofileEdit.getBackground().getConstantState() == getResources()
						.getDrawable(R.drawable.ic_action_edit)
						.getConstantState())
				{
					name.setFocusableInTouchMode(true);
					name.setEnabled(true);
					btnprofileEdit
							.setBackgroundResource(R.drawable.ic_action_accept);
				}
				else
				{
					btnprofileEdit
							.setBackgroundResource(R.drawable.ic_action_edit);
					name.setEnabled(false);

				}
			}
		});

		try
		{
			Init();
			getProfile();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void Init()
	{

		mgr = new SharedPreferencesManager(getActivity());
		pdHandler = new Handler();
		light = Typeface.createFromAsset(getActivity().getAssets(),
				"fonts/opensans-regular-webfont.ttf");

		semibold = Typeface.createFromAsset(getActivity().getAssets(),
				"fonts/opensans-semibold-webfont.ttf");

		name = (EditText) getActivity().findViewById(R.id.namep);
		mobile = (TextView) getActivity().findViewById(R.id.mobilep);
		// title = (TextView) getActivity().findViewById(R.id.titlep);
		email = (TextView) getActivity().findViewById(R.id.emailp);
		txtptrip = (TextView) getActivity().findViewById(R.id.txt_ptrips);
		txtpdistance = (TextView) getActivity().findViewById(R.id.txt_pkms);
		txtpamount = (TextView) getActivity().findViewById(R.id.txt_pamount);
		txtctrip = (TextView) getActivity().findViewById(R.id.txt_ctrips);
		txtcdistance = (TextView) getActivity().findViewById(R.id.txt_ckms);
		txtcamount = (TextView) getActivity().findViewById(R.id.txt_camount);
		txtcompanyname = (TextView) getActivity().findViewById(
				R.id.txt_Compname);
		name.setTypeface(light);
		mobile.setTypeface(light);
		// title.setTypeface(semibold);
		email.setTypeface(light);

		retryBtn.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				progressBar.setVisibility(View.INVISIBLE);
				getProfile();
			}
		});

		exporttxt.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				JugunooExportWeeklystatus();
			}
		});

		favorite_drivers.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				startActivity(new Intent(getActivity(),
						FavoriteDriversListActivity.class));
			}
		});
	}

	private void manageView(boolean Netstatus)
	{
		if (Netstatus)
		{
			Profilemain_layout.setVisibility(View.VISIBLE);
			Netstate_layout.setVisibility(View.GONE);
		}
		else
		{
			Profilemain_layout.setVisibility(View.GONE);
			Netstate_layout.setVisibility(View.VISIBLE);
		}
	}

	private Handler handler = new Handler(new Handler.Callback()
	{

		@Override
		public boolean handleMessage(Message msg)
		{
			switch (msg.arg1)
			{
				case Constant.MessageState.USER_PROFILE_SUCCESS:
					pdHandler.removeCallbacks(pdRunnable);
					try
					{
						manageView(true);
						dialog.dismiss();
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
					progressBar.setVisibility(View.INVISIBLE);
					JSONObject fleets = (JSONObject) msg.obj;
					prepareProfile(fleets);
					break;

				case Constant.MessageState.USER_PROFILE_FAIL:
					pdHandler.removeCallbacks(pdRunnable);
					try
					{
						manageView(false);
						progressBar.setVisibility(View.INVISIBLE);
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
					// Function.showToast(getActivity(),
					// Constant.network_err_msg);
					Log.e(TAG, "Null Groups");
					break;
					
				case Constant.MessageState.UPDATEPROFILE_STATUS_SUCCESS:
					pdHandler.removeCallbacks(pdRunnable);
					try
					{
						manageView(false);
						progressBar.setVisibility(View.INVISIBLE);
						JSONObject updateobj = (JSONObject) msg.obj;
						parseUpdatedUserProfile(updateobj);
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
					// Function.showToast(getActivity(),
					// Constant.network_err_msg);
					Log.e(TAG, "Null Groups");
					break;

			}
			return false;
		}
	});

	private void parseUpdatedUserProfile(JSONObject updateobj)
	{
		
	}
	
	private void getProfile()
	{
		pdRunnable = new Runnable()
		{
			@Override
			public void run()
			{
				progressBar.setVisibility(View.INVISIBLE);
			}
		};
		String userID = mgr.GetValueFromSharedPrefs("UserID");
		NetworkHandler.GetProfile(TAG, handler, userID);
	}

	private void prepareProfile(JSONObject object)
	{

		try
		{
			String resultStr = object.getString("Result");
			// String type = object.getString("UserType");
			// mgr.SaveValueToSharedPrefs("FleetUserType", type);
			if (!resultStr.equalsIgnoreCase("Fail"))
			{
				String userDetail = object.getString("UserDetails");
				JSONObject objd = new JSONObject(userDetail);
				String name = objd.getString("FirstName");
				String mobile = objd.getString("Mobile");
				String email = objd.getString("Email");

				JSONArray triparray = object.getJSONArray("TripDetails");

				for (int i = 0; i < triparray.length(); i++)
				{
					JSONObject c = triparray.getJSONObject(i);
					gptrip = c.getString("PersonalTrip");
					gpdistance = c.getString("PersonalDistance");
					gpamount = c.getString("PersonalAmount");
					gctrip = c.getString("CorporateTrip");
					gcdistance = c.getString("CorporateDistance");
					gcamount = c.getString("CorporateAmount");

				}
				gName = name;
				gMobile = mobile;
				gEmail = email;
				txtptrip.setText(gptrip);
				txtpdistance.setText(gpdistance);
				txtpamount.setText(gpamount);
				txtctrip.setText(gctrip);
				txtcdistance.setText(gcdistance);
				txtcamount.setText(gcamount);
				UpdateProfile(name, mobile, email);
				Log.e("tag", "Result: " + object.toString());
			}
			else
			{
				Log.i("Fleet Listener", "Profile null");
			}

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void Confirmationdialog()
	{
		final Dialog dialog = new Dialog(getActivity());
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.delete_corporate_dialog);
		TextView popupHeader = (TextView) dialog
				.findViewById(R.id.tvAlertHeader);
		TextView popupMsg = (TextView) dialog.findViewById(R.id.tvAlertMsg);
		Button btOk = (Button) dialog.findViewById(R.id.btAlertOk);
		Button btCancel = (Button) dialog.findViewById(R.id.btAlertCancel);
		personalEmailID = (EditText) dialog.findViewById(R.id.personal_emailid);
		validEmainId = personalEmailID.getText().toString();

		popupMsg.setText("Do you want to delete Corporate details,if you delete You cannot make Corporate Booking?");
		popupHeader.setText(ConstantMessages.MSG91);
		btOk.setText(ConstantMessages.MSG93);
		btCancel.setText(ConstantMessages.MSG96);
		btCancel.setVisibility(View.VISIBLE);

		btOk.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (Validation.isEmailAddress(personalEmailID, true)
						&& validEmainId != null)
				{
					dialog.cancel();
					RequestRemovingCorp();
				}
				else
				{
					Toast.makeText(getActivity(), "Enter a Valid Email Id",
							Toast.LENGTH_SHORT).show();
				}
			}
		});

		btCancel.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				dialog.cancel();
			}
		});

		dialog.show();

	}

	private void JugunooExportWeeklystatus()
	{

		dialog = new Dialog(getActivity());
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

		LinearLayout spinner1ll = (LinearLayout) dialog
				.findViewById(R.id.spinner1ll);
		LinearLayout spinner2ll = (LinearLayout) dialog
				.findViewById(R.id.spinner2ll);
		spinner1ll.setVisibility(View.GONE);
		spinner2ll.setVisibility(View.GONE);

		etTriplogStartDate.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				// Function.hideSoftKeyBoard(ProfileFragment.this,
				// etTriplogStartDate);
				flag = false;
				if (year == 0)
				{
					datePickerDialog = DatePickerDialog.newInstance(
							ProfileFragment.this, calendar.get(Calendar.YEAR),
							calendar.get(Calendar.MONTH),
							calendar.get(Calendar.DAY_OF_MONTH), false);

				}
				else
				{

					enddatePickerDialog = DatePickerDialog.newInstance(
							ProfileFragment.this, year, month, day, false);
				}

				datePickerDialog.setYearRange(calendar.get(Calendar.YEAR) - 1,
						calendar.get(Calendar.YEAR) + 1);

				datePickerDialog.show(getFragmentManager(), DATEPICKER_TAG);

			}
		});

		etTriplogEndDate.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				// Function.hideSoftKeyBoard(ProfileFragment.this,
				// etTriplogEndDate);
				flag = true;

				endcalendar = Calendar.getInstance();

				if (endyear == 0)
				{
					enddatePickerDialog = DatePickerDialog.newInstance(
							ProfileFragment.this,
							endcalendar.get(Calendar.YEAR),
							endcalendar.get(Calendar.MONTH),
							endcalendar.get(Calendar.DAY_OF_MONTH), false);
				}

				enddatePickerDialog.setYearRange(
						endcalendar.get(Calendar.YEAR) - 1,
						endcalendar.get(Calendar.YEAR) + 1);

				enddatePickerDialog.show(getFragmentManager(), DATEPICKER_TAG);

			}
		});

		btnTriplogSubmit.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{

				// if (JugunooUtil.isConnectedToInternet())
				// {
				makeExportRequest();
				// }
				// else
				// {
				// // Function.showToast(ProfileFragment.this, getResources()
				// // .getString(R.string.connection_error));
				// }

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

	private void RequestRemovingCorp()
	{
		String userID = mgr.GetValueFromSharedPrefs("UserID");
		Map<String, String> userParams = new HashMap<String, String>();
		userParams.put("Status", "CP");
		userParams.put("UserId", userID);
		userParams.put("FirstName", gName);
		userParams.put("Email", validEmainId);
		NetworkHandler.updateUserProfile(TAG, handler, userParams);
		 startActivity(new Intent(getActivity(),ProfileActivity.class));
	}

	private void makeExportRequest()
	{
		String userId = mgr.GetValueFromSharedPrefs("UserID");
		String emailstr = tripLogEmail.getText().toString();

		// boolean isSuccessValidation = checkValidation();
		String tag_json_obj = "Export TRip log ------>";

		Map<String, String> params = new HashMap<String, String>();
		params.put("UserId", userId);
		params.put("Status", selectedStatus);
		params.put("StartDate", startDate);
		params.put("EndDate", endDate);
		params.put("ToAddr", emailstr);
		params.put("Pref", selectedPref);

		// NetworkHandler.ExportTripLog(tag_json_obj, handlerExportTrips,
		// params);

	}

	private void UpdateProfile(String nam, String mobil, String emai)
	{
		if (!nam.equalsIgnoreCase(""))
		{
			name.setText(nam);

		}
		else
			name.setVisibility(View.GONE);
		if (!mobil.equalsIgnoreCase(""))
		{

			mobile.setText(mobil);
		}
		else
			mobile.setVisibility(View.GONE);
		if (!emai.equalsIgnoreCase(""))
		{

			email.setText(emai);
		}
		else
			email.setVisibility(View.GONE);
	}

	@Override
	public void onStart()
	{
		super.onStart();
	}

	@Override
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);
	}

	@Override
	public void onResume()
	{
		super.onResume();

	}

	@Override
	public void onStop()
	{
		super.onStop();
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
	}

	@Override
	public void onDateSet(DatePickerDialog datePickerDialog, int year,
			int month, int day)
	{
		// if (flag)
		// {
		// endday = day;
		// endmonth = month;
		// endyear = year;
		// endDate = getEndCalDate(endday, endmonth, endyear, "MM-dd-yyyy");
		// endDateMills = Long.parseLong(Function.getMillsTs(endDate,
		// "MM-dd-yyyy"));
		// etTriplogEndDate.setText(getEndCalDate(endday, endmonth, endyear,
		// "dd-MMM (EEE)"));
		//
		// // flag = false;
		// }
		// else
		// {
		// this.day = day;
		// this.month = month;
		// this.year = year;
		//
		// startDate = getStartCalDate(day, month, year, "MM-dd-yyyy");
		// startDateMills = Long.parseLong(Function.getMillsTs(startDate,
		// "MM-dd-yyyy"));
		// etTriplogStartDate.setText(getStartCalDate(day, month, year,
		// "dd-MMM (EEE)"));
		//
		// }

	}
}
