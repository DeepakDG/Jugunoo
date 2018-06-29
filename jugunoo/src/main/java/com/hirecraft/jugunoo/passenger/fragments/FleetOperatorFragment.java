package com.hirecraft.jugunoo.passenger.fragments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.hirecraft.jugunoo.passenger.R;
import com.hirecraft.jugunoo.passenger.common.Constant;
import com.hirecraft.jugunoo.passenger.common.ConstantMessages;
import com.hirecraft.jugunoo.passenger.common.Function;
import com.hirecraft.jugunoo.passenger.common.Global;
import com.hirecraft.jugunoo.passenger.networkhandler.NetworkHandler;
import com.hirecraft.jugunoo.passenger.services.VolleyErrorHelper;
import com.hirecraft.jugunoo.passenger.utility.SharedPreferencesManager;

import de.keyboardsurfer.android.widget.crouton.Crouton;

public class FleetOperatorFragment extends Fragment
{

	private SharedPreferencesManager mgr;
	private static final String TAG = FleetOperatorFragment.class
			.getSimpleName();
	private ListView fleetList;
	private ProgressBar progressBar;
	private ArrayList<HashMap<String, String>> fleets = null;
	private String grName, mgrName, count, status, rid, mobile, usertype;
	private static String updateStatus = "";
	private Runnable pdRunnable;
	private Handler pdHandler;
	private Spinner fleetOperatorSpinner;
	private TextView fleetOpsStatus;
	private ListAdapter adapter;
	ArrayAdapter<String> dataAdapter;
	private LinearLayout Netstate_layout;
	private Button retryBtn;
	private RelativeLayout fleet_operatorlayout;
	private static boolean fleetfrag_visible;
	private static String fleetspinner_pos;

	private SwipeRefreshLayout mSwipeRefreshLayout;;
	private Handler handlerswipe = new Handler();

	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.fleet_operator_fragment, container,
				false);

	}

	@Override
	public void onPause()
	{
		super.onPause();
		Crouton.cancelAllCroutons();
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		mgr = new SharedPreferencesManager(getActivity());
		fleet_operatorlayout = (RelativeLayout) getActivity().findViewById(
				R.id.rlFleetOperatormain);
		Netstate_layout = (LinearLayout) getActivity().findViewById(
				R.id.fleet_ErrorState);
		retryBtn = (Button) getActivity().findViewById(R.id.fleet_retrybtn);
		fleetList = (ListView) getActivity().findViewById(R.id.fleetlist);
		progressBar = (ProgressBar) getActivity().findViewById(
				R.id.progressfleet);
		progressBar.getIndeterminateDrawable().setColorFilter(
				Color.rgb(155, 219, 70),
				android.graphics.PorterDuff.Mode.MULTIPLY);
		pdHandler = new Handler();
		fleetOperatorSpinner = (Spinner) getActivity().findViewById(
				R.id.fleetOperatorSpinner);
		fleetOpsStatus = (TextView) getActivity()
				.findViewById(R.id.fleetStatus);

		mSwipeRefreshLayout = (SwipeRefreshLayout) getActivity().findViewById(
				R.id.fleet_refresh_layout);
		mSwipeRefreshLayout.setColorSchemeResources(R.color.refresh_yellow,
				R.color.green, R.color.refresh_yellow, R.color.green);

		progressBar.setVisibility(View.VISIBLE);
		spinnerAdapter2();
		serviceCall("B");

		retryBtn.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				spinnerAdapter2();
				serviceCall("B");
			}
		});

		mSwipeRefreshLayout
				.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
				{
					@Override
					public void onRefresh()
					{
						initiateRefresh();
					}
				});
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser)
	{
		super.setUserVisibleHint(isVisibleToUser);
		fleetfrag_visible = isVisibleToUser;
		if (fleetfrag_visible)
		{
			try
			{
				fleetList.setSelection(0);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			// fragment is no longer visible
		}
	}

	@Override
	public void onResume()
	{
		super.onResume();
	}

	private void initiateRefresh()
	{
		Log.i("LOG", "initiateRefresh");
		try
		{
			handlerswipe.postDelayed(new Runnable()
			{
				public void run()
				{
					try
					{
						fleetspinner_pos = fleetOperatorSpinner
								.getSelectedItem().toString();
						ServiceCallSpinner(fleetspinner_pos);
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
					mSwipeRefreshLayout.setRefreshing(false);
				}
			}, 2000);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void manageView(boolean Netstatus)
	{
		if (Netstatus)
		{
			fleet_operatorlayout.setVisibility(View.VISIBLE);
			Netstate_layout.setVisibility(View.GONE);
		}
		else
		{
			fleet_operatorlayout.setVisibility(View.GONE);
			Netstate_layout.setVisibility(View.VISIBLE);
		}
	}

	private void ServiceCallSpinner(String fleetspinner_pos)
	{
		if (fleetspinner_pos.equalsIgnoreCase("All"))
		{
			serviceCall("B");
		}
		else if (fleetspinner_pos.equalsIgnoreCase("Active"))
		{
			serviceCall("A");
		}
		else if (fleetspinner_pos.equalsIgnoreCase("Inactive"))
		{
			serviceCall("N");
		}
		else if (fleetspinner_pos.equalsIgnoreCase("Pending"))
		{
			serviceCall("P");
		}
	}

	private void serviceCall(String state)
	{
		String userID = mgr.GetValueFromSharedPrefs("UserID");
		String usertype = mgr.GetValueFromSharedPrefs("FleetUserType");
		NetworkHandler.GetUserFleet(TAG, handler, userID, state, usertype);
	}

	Handler handler = new Handler()
	{
		public void handleMessage(android.os.Message msg)
		{
			switch (msg.arg1)
			{
				case Constant.MessageState.FLEET_OPERATOR_SUCCESS:
					try
					{
						manageView(true);
						progressBar.setVisibility(View.INVISIBLE);
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
					parseFleetOperators((JSONObject) msg.obj);
					break;

				case Constant.MessageState.FLEET_OPERATOR_FAIL:
					try
					{
						progressBar.setVisibility(View.INVISIBLE);
						manageView(false);
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
					break;

				case Constant.MessageState.UPDATE_FLEET_SUCCESS:
					manageView(true);
					pdHandler.removeCallbacks(pdRunnable);
					SetFleetUpdateStatus((JSONObject) msg.obj);
					// Function.showToast(getActivity(),
					// getResources().getString(R.string.fleetUpdated));
					break;

				case Constant.MessageState.UPDATE_FLEET_FAIL:
					// manageView(false);
					pdHandler.removeCallbacks(pdRunnable);
					// Function.showToast(getActivity(),
					// Constant.network_err_msg);
					VolleyErrorHelper.getMessage(msg.obj, getActivity(), true);
					break;
			}
		};
	};

	public void parseFleetOperators(JSONObject obj)
	{
		try
		{
			if (obj.has("Result"))
			{
				Log.e("Response", "parseFleetOperators -->" + obj);
				if (obj.getString("Result").equalsIgnoreCase("Pass"))
				{
					Log.i(TAG,
							"userType -->"
									+ mgr.GetValueFromSharedPrefs(Constant.USER_TYPE));
					try
					{
						// Condition to check userType
						if (!mgr.GetValueFromSharedPrefs("UserType")
								.equalsIgnoreCase("UserType"))
						{
							mgr.SaveValueToSharedPrefs(Constant.USER_TYPE,
									obj.getString("UserType"));
						}
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
					fleetOpsStatus.setVisibility(View.INVISIBLE);
					JSONArray array = obj.getJSONArray("FleetArray");
					// Global.fleetOperaatorArray = array;
					fleets = new ArrayList<HashMap<String, String>>();
					int len = array.length();
					if (len != 0)
					{
						for (int f = 0; f < len; f++)
						{
							JSONObject jObj = array.getJSONObject(f);
							grName = jObj.getString("GroupName");
							mgrName = jObj.getString("CreaterName");
							count = jObj.getString("Count");
							mobile = jObj.getString("Mobile");
							rid = jObj.getString("FleetId");
							status = jObj.getString("Status");
							if (status.equalsIgnoreCase("A"))
								status = "Active";
							else if (status.equalsIgnoreCase("N"))
								status = "Inactive";
							else if (status.equalsIgnoreCase("P"))
								status = "Pending";
							HashMap<String, String> fetchData = new HashMap<String, String>();
							// fetchData.put("GroupName", grName);
							// fetchData.put("CreaterName", mgrName);
							// fetchData.put("Count", count);
							fetchData.put("Mobile", R.drawable.ic_action_call
									+ "");
							fetchData.put("GroupName", grName);
							fetchData.put("FleetId", rid);
							fetchData.put("Mobilee", mobile);
							fetchData.put("CreaterName", mgrName);
							fetchData.put("Count", count);
							// fetchData.put("Status", status);
							fleets.add(fetchData);
							adapter = new SimpleAdapter(getActivity(), fleets,
									R.layout.fleetoperator_list_row_new,
									new String[]
									{ "Mobile", "GroupName", "FleetId",
											"Mobile" }, new int[]
									{ R.id.imgIcon, R.id.txtOperatorName,
											R.id.rid })
							// { R.id.txtGroupName, R.id.txtMgrName,
							// R.id.txtCounter, R.id.mobile,
							// R.id.rid, R.id.isEnable })
							// { "GroupName", "CreaterName", "Count",
							// "Mobile", "FleetId", "Status" },
							{

								@Override
								public View getView(final int position,
										View convertView, ViewGroup parent)
								{
									View view = super.getView(position,
											convertView, parent);
									ImageView call = (ImageView) view
											.findViewById(R.id.imgIcon);

									TextView textViewName = (TextView) view
											.findViewById(R.id.txtOperatorName);

									ImageView imgOperatorDelete = (ImageView) view
											.findViewById(R.id.imgOperatorDelete);

									call.setOnClickListener(new OnClickListener()
									{

										@Override
										public void onClick(View v)
										{
											if (!mobile.equalsIgnoreCase(""))
											{
												HashMap<String, String> hashMap = fleets
														.get(position);
												PhoneCall(hashMap
														.get("Mobilee"));
											}
										}
									});

									textViewName
											.setOnClickListener(new OnClickListener()
											{

												@Override
												public void onClick(View arg0)
												{
													HashMap<String, String> hashMap = fleets
															.get(position);
													alertOperatorDialog(
															"Details",
															hashMap.get("CreaterName"),
															hashMap.get("Count"),
															"", "", "");
												}
											});

									imgOperatorDelete
											.setOnClickListener(new OnClickListener()
											{

												@Override
												public void onClick(View v)
												{
													alertDeleteOperator(
															"Are you sure want to Delete this Operator?",
															position);
												}
											});

									return view;
								}
							};
							fleetList.setVisibility(View.VISIBLE);
							fleetList.setAdapter(adapter);
							adapter.registerDataSetObserver(new DataSetObserver()
							{

								@Override
								public void onChanged()
								{
									super.onChanged();
								}
							});
							fleetList.setSelection(0);
							fleetList
									.smoothScrollToPosition(Global.SELECTED_ITEM_FO);

							fleetList
									.setOnItemClickListener(new OnItemClickListener()
									{

										@Override
										public void onItemClick(
												AdapterView<?> parent,
												View view, int position, long id)
										{
											usertype = mgr
													.GetValueFromSharedPrefs("FleetUserType");
											// if (usertype
											// .equalsIgnoreCase("Admin"))
											// {
											// Global.SELECTED_ITEM_FO = -1;
											// Global.SELECTED_ITEM_FO =
											// position;
											// String grName = ((TextView) view
											// .findViewById(R.id.txtGroupName))
											// .getText().toString();
											// String mgrName = ((TextView) view
											// .findViewById(R.id.txtMgrName))
											// .getText().toString();
											// String status = ((TextView) view
											// .findViewById(R.id.isEnable))
											// .getText().toString();
											// String mobile = ((TextView) view
											// .findViewById(R.id.mobile))
											// .getText().toString();
											// String fleetId = ((TextView) view
											// .findViewById(R.id.rid))
											// .getText().toString();
											// FleetUpdateDialog(grName,
											// mgrName, status,
											// mobile, fleetId);
											//
											// }
										}
									});
						}

					}
					else
						Log.e("Fleet Listener", "Fleet null");
				}
				else
				{
					fleetOpsStatus.setVisibility(View.VISIBLE);
					fleetOpsStatus.setText(obj.getString("Message"));
					fleets.clear();
					fleetList.setAdapter(null);
				}
			}
		}
		catch (Exception e)
		{

		}
	}

	private void alertOperatorDialog(String title, String Mname, String cabs,
			String bookings, String invoice, String email)
	{
		/* Confirmation dialog */
		final Dialog dialog = new Dialog(getActivity());
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.custom_operator_alertdialogbox);

		TextView textTitle = (TextView) dialog.findViewById(R.id.tvDriverTitle);
		textTitle.setText(title);

		TextView tvOperatorMngname = (TextView) dialog
				.findViewById(R.id.tvOperatorMngname);
		tvOperatorMngname.setText(Mname);

		TextView tvOPCabsCount = (TextView) dialog
				.findViewById(R.id.tvOPCabsCount);
		tvOPCabsCount.setText(cabs);

		TextView tvOPBookings = (TextView) dialog
				.findViewById(R.id.tvOPBookings);
		tvOPBookings.setText(bookings);

		TextView tvOPInvoice = (TextView) dialog.findViewById(R.id.tvOPInvoice);
		tvOPInvoice.setText(invoice);
		TextView tvOPEmail = (TextView) dialog.findViewById(R.id.tvOPEmail);
		tvOPEmail.setText(email);
		dialog.show();
	}

	private void alertDeleteOperator(String msg, int position)
	{
		/* Show Alert dialog when back key pressed */

		HashMap<String, String> hashMap = fleets.get(position);
		final Dialog alertBack = new Dialog(getActivity());
		alertBack.requestWindowFeature(Window.FEATURE_NO_TITLE);

		LayoutInflater inflater = (LayoutInflater) getActivity()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View dislogView = inflater.inflate(R.layout.dialog_common_alert, null);
		alertBack.setContentView(dislogView);

		TextView popupText = (TextView) alertBack
				.findViewById(R.id.tvAlertHeader);
		popupText.setText(ConstantMessages.MSG91);
		TextView popupMsg = (TextView) alertBack.findViewById(R.id.tvAlertMsg);
		Button popupOk = (Button) alertBack.findViewById(R.id.btAlertOk);
		Button cancel = (Button) alertBack.findViewById(R.id.btAlertCancel);
		cancel.setVisibility(View.VISIBLE);
		popupMsg.setText(msg);
		popupOk.setText(ConstantMessages.MSG93);
		cancel.setText(ConstantMessages.MSG96);
		alertBack.show();

		popupOk.setOnClickListener(new View.OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				alertBack.cancel();
			}
		});

		cancel.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				alertBack.cancel();
			}
		});
	}

	private void FleetUpdateDialog(String groupNam, String mgrNam,
			final String status, final String mobile, final String fleetId)
	{

		final Dialog dialog = new Dialog(getActivity());
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		Window window = dialog.getWindow();
		window.setBackgroundDrawableResource(android.R.color.transparent);
		LinearLayout.LayoutParams dialogParams = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

		LayoutInflater inflater = (LayoutInflater) getActivity()
				.getBaseContext().getSystemService(
						Context.LAYOUT_INFLATER_SERVICE);
		View dislogView = inflater.inflate(R.layout.jugunoo_profile_dialog,
				new LinearLayout(getActivity()));
		dialog.setContentView(dislogView, dialogParams);

		EditText groupName = (EditText) dialog.findViewById(R.id.groupNameD);
		EditText mgrName = (EditText) dialog.findViewById(R.id.mgrNameD);
		RadioGroup radioStatusGroup = (RadioGroup) dialog
				.findViewById(R.id.etchdrStatusGroup);
		RadioButton act = (RadioButton) dialog.findViewById(R.id.etchdrActive);
		RadioButton inact = (RadioButton) dialog
				.findViewById(R.id.etchdrInactive);

		Button update = (Button) dialog.findViewById(R.id.updateBtn);
		Button cancel = (Button) dialog.findViewById(R.id.cancelBtn);
		groupName.setText(groupNam);
		mgrName.setText(mgrNam);
		if (status.equalsIgnoreCase("Active"))
		{
			updateStatus = "A";
			act.setChecked(true);
		}
		else
		{
			updateStatus = "N";
			inact.setChecked(true);
		}

		radioStatusGroup
				.setOnCheckedChangeListener(new OnCheckedChangeListener()
				{

					@Override
					public void onCheckedChanged(RadioGroup group, int checkedId)
					{
						RadioButton fleetStatus = (RadioButton) dialog
								.findViewById(group.getCheckedRadioButtonId());
						String option = fleetStatus.getText().toString();
						if (option.equalsIgnoreCase("Active"))
						{
							updateStatus = "A";
						}
						else
						{
							updateStatus = "N";
						}
					}
				});

		update.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				dialog.dismiss();
				saveGroup(fleetId, updateStatus);
				// spinnerAdapter2();
			}
		});
		cancel.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				dialog.dismiss();
			}
		});

		dialog.show();
	}

	private void saveGroup(String fleetId, String status)
	{

		pdRunnable = new Runnable()
		{
			@Override
			public void run()
			{
				progressBar.setVisibility(View.INVISIBLE);
			}
		};
		if (!fleetId.equalsIgnoreCase(""))
		{

			Map<String, String> params = new HashMap<String, String>();
			params.put("UserId", mgr.GetValueFromSharedPrefs("UserID"));
			params.put("FleetUserId", fleetId);
			params.put("Status", status);
			params.put("RID", "1");

			NetworkHandler.SaveFleetOperator(TAG, handler, params);
		}
		else
		{
			Log.i(Global.APPTAG, "Parameter are invalid!");
		}
	}

	private void SetFleetUpdateStatus(JSONObject object)
	{

		try
		{
			String resultStr = object.getString("Result");
			String message = object.getString("Message");
			if (!resultStr.equalsIgnoreCase("Fail"))
			{
				fleetspinner_pos = fleetOperatorSpinner.getSelectedItem()
						.toString();
				ServiceCallSpinner(fleetspinner_pos);
			}

			Function.showToast(getActivity(), message);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void PhoneCall(String mobile)
	{
		try
		{
			Intent phoneCallIntent = new Intent(Intent.ACTION_DIAL);
			phoneCallIntent.setData(Uri.parse("tel:" + mobile));
			startActivity(phoneCallIntent);
		}
		catch (Exception bug)
		{
			Log.i(TAG, "Call failed!");
			bug.printStackTrace();
		}
	}

	private void spinnerAdapter2()
	{
		// Loading data to spinner
		List<String> list = new ArrayList<String>();
		list.add("All");
		list.add("Active");
		list.add("Inactive");
		list.add("Pending");

		dataAdapter = new ArrayAdapter<String>(getActivity(),
				android.R.layout.simple_spinner_item, list);
		dataAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		try
		{
			fleetOperatorSpinner.setAdapter(dataAdapter);
			dataAdapter.notifyDataSetChanged();
			fleetOperatorSpinner
					.setOnItemSelectedListener(new OnItemSelectedListener()
					{

						@Override
						public void onItemSelected(AdapterView<?> arg0,
								View arg1, int arg2, long arg3)
						{
							switch (arg2)
							{
								case 0:
									fleetList.setVisibility(View.GONE);
									fleetOpsStatus
											.setVisibility(View.INVISIBLE);
									progressBar.setVisibility(View.VISIBLE);
									serviceCall("B");
									break;
								case 1:
									fleetList.setVisibility(View.GONE);
									fleetOpsStatus
											.setVisibility(View.INVISIBLE);
									progressBar.setVisibility(View.VISIBLE);
									serviceCall("A");
									break;

								case 2:
									fleetList.setVisibility(View.GONE);
									fleetOpsStatus
											.setVisibility(View.INVISIBLE);
									progressBar.setVisibility(View.VISIBLE);
									serviceCall("N");
									break;

								case 3:
									fleetList.setVisibility(View.GONE);
									fleetOpsStatus
											.setVisibility(View.INVISIBLE);
									progressBar.setVisibility(View.VISIBLE);
									serviceCall("P");
									break;

							}
						}

						@Override
						public void onNothingSelected(AdapterView<?> arg0)
						{

						}
					});
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
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
}
