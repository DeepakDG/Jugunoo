package com.hirecraft.jugunoo.passenger.fragments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.hirecraft.jugunoo.passenger.R;
import com.hirecraft.jugunoo.passenger.common.Constant;
import com.hirecraft.jugunoo.passenger.common.Function;
import com.hirecraft.jugunoo.passenger.common.Global;
import com.hirecraft.jugunoo.passenger.common.TransparentProgressDialog;
import com.hirecraft.jugunoo.passenger.networkhandler.NetworkHandler;
import com.hirecraft.jugunoo.passenger.utility.JugunooUtil;
import com.hirecraft.jugunoo.passenger.utility.SharedPreferencesManager;

import de.keyboardsurfer.android.widget.crouton.Crouton;

public class GroupsFragment extends Fragment
{

	private SharedPreferencesManager mgr;
	private String TAG = GroupsFragment.class.getSimpleName();
	private ListView groupList;
	private ProgressBar progressBar;
	private ArrayList<HashMap<String, String>> groups = null;
	private Runnable pdRunnable;
	private TransparentProgressDialog pd;
	private Handler pdHandler;
	private String grName, grId, grMgrName, grMgrID, grStatus, grMobileNo,
			grCount, usertype;
	private TextView groupState;
	private ImageView addGroups;
	private Spinner groupsFilterSpinner;
	public static int IS_GROUP_ACTION = 0;
	public static boolean isGroupSuccess = false;
	private ListAdapter adapter;
	private TextView tvMangerId;
	ArrayAdapter<String> dataAdapter;
	private LinearLayout Netstate_layout;
	private Button retryBtn;
	private LinearLayout groupSpinnerll;
	private RelativeLayout Groupmain_layout;
	private static boolean groupfrag_visible;
	private static String groupspinner_pos;

	private SwipeRefreshLayout mSwipeRefreshLayout;;
	private Handler handlerswipe = new Handler();

	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.fragment_group, container, false);
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
		Groupmain_layout = (RelativeLayout) getActivity().findViewById(
				R.id.rlGroupmain);
		groupSpinnerll = (LinearLayout) getActivity().findViewById(
				R.id.groupSpinnerLayout);
		Netstate_layout = (LinearLayout) getActivity().findViewById(
				R.id.group_ErrorState);
		retryBtn = (Button) getActivity().findViewById(R.id.group_retrybtn);
		groupList = (ListView) getActivity().findViewById(R.id.group_list);
		progressBar = (ProgressBar) getActivity().findViewById(
				R.id.groupProgress);
		progressBar.getIndeterminateDrawable().setColorFilter(
				Color.rgb(155, 219, 70),
				android.graphics.PorterDuff.Mode.MULTIPLY);
		pdHandler = new Handler();
		pd = new TransparentProgressDialog(getActivity(),
				R.drawable.loading_image);
		groupState = (TextView) getActivity().findViewById(R.id.groupStatus);
		addGroups = (ImageView) getActivity().findViewById(R.id.addGroup);
		groupsFilterSpinner = (Spinner) getActivity().findViewById(
				R.id.groupSpinner);

		mSwipeRefreshLayout = (SwipeRefreshLayout) getActivity().findViewById(
				R.id.group_refresh_layout);
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
		usertype = mgr.GetValueFromSharedPrefs("FleetUserType");
		try
		{
			if (usertype.equalsIgnoreCase("Admin"))
			{
				addGroups.setVisibility(View.VISIBLE);
			}
			else
			{
				addGroups.setVisibility(View.INVISIBLE);
			}
			addGroups.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View arg0)
				{
					if (JugunooUtil.isConnectedToInternet(getActivity()))
					{
						startActivity(new Intent(getActivity(),
								AddFleetGroupActivity.class));
					}
					else
					{
						Function.showToast(getActivity(),
								"No internet connection");
					}
				}
			});
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void onResume()
	{
		super.onResume();
		if (Global.IsGroupstatUpdated)
		{
			spinnerAdapter2();
			serviceCall("B");
			Global.IsGroupstatUpdated = false;
		}
		else
		{
			// Not Updated anything
		}
	}

	private void initiateRefresh()
	{
		Log.i("LOG_TAG", "initiateRefresh");
		try
		{
			handlerswipe.postDelayed(new Runnable()
			{
				public void run()
				{
					try
					{
						groupspinner_pos = groupsFilterSpinner
								.getSelectedItem().toString();
						ServiceCallSpinner(groupspinner_pos);
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

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser)
	{
		super.setUserVisibleHint(isVisibleToUser);
		groupfrag_visible = isVisibleToUser;
		if (groupfrag_visible)
		{
			try
			{
				groupList.setSelection(0);
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

	private void manageView(boolean Netstatus)
	{
		if (Netstatus)
		{
			Groupmain_layout.setVisibility(View.VISIBLE);
			Netstate_layout.setVisibility(View.GONE);
		}
		else
		{
			Groupmain_layout.setVisibility(View.GONE);
			Netstate_layout.setVisibility(View.VISIBLE);
		}
	}

	private void ServiceCallSpinner(String groupspinner_pos)
	{
		if (groupspinner_pos.equalsIgnoreCase("All"))
		{
			serviceCall("B");
		}
		else if (groupspinner_pos.equalsIgnoreCase("Active"))
		{
			serviceCall("A");
		}
		else if (groupspinner_pos.equalsIgnoreCase("Inactive"))
		{
			serviceCall("N");
		}
		else if (groupspinner_pos.equalsIgnoreCase("Pending"))
		{
			serviceCall("P");
		}
	}

	private void serviceCall(String status)
	{
		String userID = mgr.GetValueFromSharedPrefs("UserID");
		Log.e("User id", userID);
		String usertype = mgr.GetValueFromSharedPrefs("FleetUserType");
		NetworkHandler.GetGroups(TAG, handler, userID, status);
	}

	Handler handler = new Handler()
	{
		public void handleMessage(android.os.Message msg)
		{
			switch (msg.arg1)
			{
				case Constant.MessageState.FLEET_GROUP_SUCCESS:
					try
					{
						manageView(true);
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
					progressBar.setVisibility(View.GONE);
					PrepareGroupList((JSONObject) msg.obj);
					break;

				case Constant.MessageState.FLEET_GROUP_FAIL:
					try
					{
						manageView(false);
						progressBar.setVisibility(View.GONE);
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
					// Function.showToast(getActivity(),
					// Constant.network_err_msg);
					break;

			}
		}
	};

	private void PrepareGroupList(JSONObject obj)
	{
		Log.e(TAG, "grp frag=" + obj);
		try
		{
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
				groupState.setVisibility(View.INVISIBLE);
				groupList.setVisibility(View.VISIBLE);
				JSONArray array = obj.getJSONArray("GroupArray");
				// Global.fleetGroupArray = array;
				groups = new ArrayList<HashMap<String, String>>();
				int len = array.length();
				if (len != 0)
				{
					isGroupSuccess = true;
					for (int f = 0; f < len; f++)
					{
						JSONObject jObj = array.getJSONObject(f);
						grId = jObj.getString("GroupId");
						grName = jObj.getString("GroupName");
						grMgrName = jObj.getString("ManagerName");
						grMgrID = jObj.getString("ManagingUserId");
						grStatus = jObj.getString("Status");
						grMobileNo = jObj.getString("MobileNo");
						grCount = jObj.getString("Count");
						if (grStatus.equalsIgnoreCase("A"))
							grStatus = "Active";
						else if (grStatus.equalsIgnoreCase("N"))
							grStatus = "Inactive";

						HashMap<String, String> fetchData = new HashMap<String, String>();
						fetchData.put("GroupId", grId);
						fetchData.put("GroupName", grName);
						fetchData.put("ManagerName", grMgrName);
						fetchData.put("Status", grStatus);
						fetchData.put("MobileNo", grMobileNo);
						fetchData.put("Count", grCount);
						fetchData.put("ManagingUserId", grMgrID);

						groups.add(fetchData);
						Log.d("GroupId", grMgrID.toString());

					}

				}
				else
				{
					Log.i("Fleet Listener", "Fleet null");

				}

			}
			else
			{
				isGroupSuccess = false;
				groupState.setVisibility(View.VISIBLE);
				groupState.setText(obj.getString("Message"));
				groups.clear();
				groupList.setAdapter(null);
			}

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		try
		{
			adapter = new SimpleAdapter(getActivity(), groups,
					R.layout.fleet_list_row, new String[]
					{ "GroupId", "GroupName", "ManagerName", "Status",
							"MobileNo", "Count", "ManagingUserId" }, new int[]
					{ R.id.rid, R.id.txtGroupName, R.id.txtMgrName,
							R.id.isEnable, R.id.mobile, R.id.txtCounter,
							R.id.managingUid })
			{
				@Override
				public View getView(int position, View convertView,
						ViewGroup parent)
				{
					View view = super.getView(position, convertView, parent);
					// ImageView call = (ImageView) view
					// .findViewById(R.id.imgIcon);
					//
					// tvMangerId = (TextView)
					// view.findViewById(R.id.managingUid);
					//
					// String USERID = mgr.GetValueFromSharedPrefs("UserID");
					//
					// if (tvMangerId.getText().toString()
					// .equalsIgnoreCase(USERID))
					// {
					// call.setVisibility(View.INVISIBLE);
					//
					// Log.d("tvMangerId", "tvMangerId"
					// + tvMangerId.getText().toString());
					// }
					// else
					// {
					// call.setVisibility(View.VISIBLE);
					// Log.d("Else", "tvMangerId"
					// + tvMangerId.getText().toString());
					//
					// }
					//
					// final String mobile = ((TextView) view
					// .findViewById(R.id.mobile)).getText().toString();
					//
					// call.setOnClickListener(new OnClickListener()
					// {
					//
					// @Override
					// public void onClick(View v)
					// {
					// if (!mobile.equalsIgnoreCase(""))
					// {
					// PhoneCall(mobile);
					// }
					// }
					// });

					return view;
				}
			};

			if (adapter.isEmpty())
			{
				groupState.setVisibility(View.VISIBLE);
				groupState.setText(obj.getString("Message"));
			}
			else
			{
				groupList.setAdapter(adapter);
				adapter.registerDataSetObserver(new DataSetObserver()
				{

					@Override
					public void onChanged()
					{
						super.onChanged();
					}
				});
				groupList.setSelection(0);

				groupList.smoothScrollToPosition(Global.SELECTED_ITEM_G);
				groupList.setOnItemClickListener(new OnItemClickListener()
				{

					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id)
					{

						usertype = mgr.GetValueFromSharedPrefs("FleetUserType");
						if (usertype.equalsIgnoreCase("Admin"))
						{
							Global.SELECTED_ITEM_G = -1;
							Global.SELECTED_ITEM_G = position;
							IS_GROUP_ACTION = 1;
							String grName = ((TextView) view
									.findViewById(R.id.txtGroupName)).getText()
									.toString();
							String mgrName = ((TextView) view
									.findViewById(R.id.txtMgrName)).getText()
									.toString();
							String status = ((TextView) view
									.findViewById(R.id.isEnable)).getText()
									.toString();

							String rid = ((TextView) view
									.findViewById(R.id.rid)).getText()
									.toString();
							String mobile = ((TextView) view
									.findViewById(R.id.mobile)).getText()
									.toString();

							String mgrID = ((TextView) view
									.findViewById(R.id.managingUid)).getText()
									.toString();

							if (JugunooUtil
									.isConnectedToInternet(getActivity()))
							{
								Intent i = new Intent(getActivity(),
										AddFleetGroupActivity.class);
								i.putExtra("GrName", grName);
								i.putExtra("MgrName", mgrName);
								i.putExtra("Status", status);
								i.putExtra("GroupId", rid);
								i.putExtra("MobileNo", mobile);
								i.putExtra("ManagingUserId", mgrID);
								startActivity(i);

								Log.d("Groupsadapterlist", "Groupsadapterlist"
										+ groups.toString());
							}
							else
							{
								Function.showToast(getActivity(),
										"No internet connection");
							}
						}
					}

				});
			}
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
			groupsFilterSpinner.setAdapter(dataAdapter);
			dataAdapter.notifyDataSetChanged();
			groupsFilterSpinner
					.setOnItemSelectedListener(new OnItemSelectedListener()
					{

						@Override
						public void onItemSelected(AdapterView<?> arg0,
								View arg1, int arg2, long arg3)
						{
							switch (arg2)
							{
								case 0:
									groupList.setVisibility(View.GONE);
									groupState.setVisibility(View.INVISIBLE);
									progressBar.setVisibility(View.VISIBLE);
									serviceCall("B");
									break;

								case 1:
									groupList.setVisibility(View.GONE);
									groupState.setVisibility(View.INVISIBLE);
									progressBar.setVisibility(View.VISIBLE);
									serviceCall("A");
									break;

								case 2:
									groupList.setVisibility(View.GONE);
									groupState.setVisibility(View.INVISIBLE);
									progressBar.setVisibility(View.VISIBLE);
									serviceCall("N");
									break;

								case 3:
									groupList.setVisibility(View.GONE);
									groupState.setVisibility(View.INVISIBLE);
									progressBar.setVisibility(View.VISIBLE);
									serviceCall("P");
									break;

								default:
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
