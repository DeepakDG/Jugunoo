package com.hirecraft.jugunoo.passenger.fragments;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Intent;
import android.database.DataSetObserver;
import android.graphics.Color;
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

public class UsersFragment extends Fragment {

	private SharedPreferencesManager mgr;
	private String TAG = UsersFragment.class.getSimpleName();
	private ListView userList;
	private ProgressBar progressBar;
	private ArrayList<HashMap<String, String>> users = null;
	private Runnable pdRunnable;
	private TransparentProgressDialog pd;
	private Handler pdHandler;
	private TextView userState;
	private ImageView addUsers;
	private Spinner userSpinner;
	ListAdapter adapter;
	private LinearLayout Netstate_layout;
	private Button retryBtn;
	public static int IS_User_ACTION = 0;
	private ImageView call;
	private LinearLayout userSpinnerll;
	private String grName, grId, grStatus, grMobileNo, grCount, userName,
			userId, grOwn, usertype;
	ArrayAdapter<String> dataAdapter;
	private RelativeLayout Usermain_layout;
	private static boolean userfrag_visible;
	private static String userspinner_pos;

	private SwipeRefreshLayout mSwipeRefreshLayout;;
	private Handler handlerswipe = new Handler();

	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState)

	{
		return inflater.inflate(R.layout.fragment_users, container, false);
	}

	@Override
	public void onPause() {
		super.onPause();
		Crouton.cancelAllCroutons();

	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mgr = new SharedPreferencesManager(getActivity());
		Usermain_layout = (RelativeLayout) getActivity().findViewById(
				R.id.rl_usermain);
		Netstate_layout = (LinearLayout) getActivity().findViewById(
				R.id.user_ErrorState);
		userSpinnerll = (LinearLayout) getActivity().findViewById(
				R.id.userSpinnerLayout);
		retryBtn = (Button) getActivity().findViewById(R.id.users_retrybtn);
		userList = (ListView) getActivity().findViewById(R.id.users_list);
		progressBar = (ProgressBar) getActivity().findViewById(
				R.id.userProgress);
		progressBar.getIndeterminateDrawable().setColorFilter(
				Color.rgb(155, 219, 70),
				android.graphics.PorterDuff.Mode.MULTIPLY);
		pdHandler = new Handler();
		pd = new TransparentProgressDialog(getActivity(),
				R.drawable.loading_image);
		userState = (TextView) getActivity().findViewById(R.id.userStatus);
		addUsers = (ImageView) getActivity().findViewById(R.id.addUsers);
		userSpinner = (Spinner) getActivity().findViewById(R.id.userSpinner);

		mSwipeRefreshLayout = (SwipeRefreshLayout) getActivity().findViewById(
				R.id.user_refresh_layout);
		mSwipeRefreshLayout.setColorSchemeResources(R.color.refresh_yellow,
				R.color.green, R.color.refresh_yellow, R.color.green);

		serviceCall("B");

		// userSpinner.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// FragmentManager manager = getFragmentManager();
		// PopUp_listview dialog = new PopUp_listview();
		//
		// Bundle bundle = new Bundle();
		// bundle.putStringArrayList(PopUp_listview.DATA, getItems()); //
		// Require
		// // ArrayList
		// bundle.putInt(PopUp_listview.SELECTED, 0);
		// dialog.setArguments(bundle);
		// dialog.show(manager, "Dialog");
		// }
		//
		// private ArrayList<String> getItems() {
		// ArrayList<String> ret_val = new ArrayList<String>();
		//
		// ret_val.add("Implementation");
		// ret_val.add("developer");
		// ret_val.add("Android");
		// ret_val.add("Service");
		// ret_val.add("Production");
		// return ret_val;
		// }
		// });
		View headerView = getActivity().getLayoutInflater().inflate(
				R.layout.profile_header_list, null);
		userList.addHeaderView(headerView);

		retryBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				serviceCall("B");
			}
		});

		mSwipeRefreshLayout
				.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
					@Override
					public void onRefresh() {
						initiateRefresh();
					}
				});

		usertype = mgr.GetValueFromSharedPrefs("FleetUserType");
		try {
			if (usertype.equalsIgnoreCase("Admin")) {
				addUsers.setVisibility(View.VISIBLE);
			} else if (usertype.equalsIgnoreCase("Manager")) {
				addUsers.setVisibility(View.VISIBLE);
			} else {
				addUsers.setVisibility(View.INVISIBLE);
			}
			addUsers.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (JugunooUtil.isConnectedToInternet(getActivity())) {
						IS_User_ACTION = 0;
						startActivity(new Intent(getActivity(),
								AddFleetUserActivity.class));
					} else {
						Function.showToast(getActivity(),
								"No internet connection");
					}
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		userfrag_visible = isVisibleToUser;
		if (userfrag_visible) {
			try {
				userList.setSelection(0);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			// fragment is no longer visible
		}
	}

	private void initiateRefresh() {
		Log.i("LOG", "initiateRefresh");
		try {
			handlerswipe.postDelayed(new Runnable() {
				public void run() {
					try {
						// userspinner_pos = userSpinner.getSelectedItem()
						// .toString();
						// ServiceCallSpinner(userspinner_pos);
					} catch (Exception e) {
						e.printStackTrace();
					}
					mSwipeRefreshLayout.setRefreshing(false);
				}

			}, 2000);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		if (Global.IsUserstatUpdated) {

			serviceCall("B");
			Global.IsUserstatUpdated = false;
		} else {
			// Not Updated anything
		}
	}

	private void manageView(boolean Netstatus) {
		if (Netstatus) {
			Usermain_layout.setVisibility(View.VISIBLE);
			Netstate_layout.setVisibility(View.GONE);
		} else {
			Usermain_layout.setVisibility(View.GONE);
			Netstate_layout.setVisibility(View.VISIBLE);
		}
	}

	private void ServiceCallSpinner(String userspinner_pos) {
		if (userspinner_pos.equalsIgnoreCase("All")) {
			serviceCall("B");
		} else if (userspinner_pos.equalsIgnoreCase("Active")) {
			serviceCall("A");
		} else if (userspinner_pos.equalsIgnoreCase("Inactive")) {
			serviceCall("N");
		} else if (userspinner_pos.equalsIgnoreCase("Pending")) {
			serviceCall("P");
		}
	}

	private void serviceCall(String status) {
		String userID = mgr.GetValueFromSharedPrefs("UserID");
		String usertype = mgr.GetValueFromSharedPrefs("FleetUserType");
		NetworkHandler.GetUsers(TAG, handler, userID, status, usertype);
	}

	Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.arg1) {
				case Constant.MessageState.FLEET_USERS_SUCCESS :
					try {
						manageView(true);
					} catch (Exception e) {
						e.printStackTrace();
					}
					progressBar.setVisibility(View.INVISIBLE);
					PrepareUserList((JSONObject) msg.obj);
					break;

				case Constant.MessageState.FLEET_USERS_FAIL :
					try {
						progressBar.setVisibility(View.INVISIBLE);
						manageView(false);
					} catch (Exception e) {
						e.printStackTrace();
					}
					// Function.showToast(getActivity(),
					// Constant.network_err_msg);
					Log.e(TAG, "Null Fleets");
					break;
			}
		};
	};

	private void PrepareUserList(JSONObject obj) {
		try {
			if (obj.getString("Result").equalsIgnoreCase("Pass")) {
				Log.i(TAG,
						"userType -->"
								+ mgr.GetValueFromSharedPrefs(Constant.USER_TYPE));

				try {
					// Condition to check userType
					if (!mgr.GetValueFromSharedPrefs("UserType")
							.equalsIgnoreCase("UserType")) {
						mgr.SaveValueToSharedPrefs(Constant.USER_TYPE,
								obj.getString("UserType"));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				userState.setVisibility(View.INVISIBLE);
				userList.setVisibility(View.VISIBLE);

				JSONArray array = obj.getJSONArray("GroupArray");
				Global.fleetUserArray = array;
				users = new ArrayList<HashMap<String, String>>();
				users.clear();
				int len = array.length();
				if (len != 0) {
					for (int f = 0; f < len; f++) {
						JSONObject jObj = array.getJSONObject(f);
						grId = jObj.getString("GroupId");
						grName = jObj.getString("GroupName");
						userName = jObj.getString("UserName");
						userId = jObj.getString("UserId");
						grStatus = jObj.getString("Status");
						grMobileNo = jObj.getString("MobileNo");
						grCount = jObj.getString("Count");
						usertype = mgr.GetValueFromSharedPrefs("FleetUserType");
						if (usertype.equalsIgnoreCase("Manager")) {
							grOwn = jObj.getString("Own");
						}

						if (grStatus.equalsIgnoreCase("A"))
							grStatus = "Active";
						else if (grStatus.equalsIgnoreCase("N"))
							grStatus = "Inactive";
						HashMap<String, String> fetchData = new HashMap<String, String>();
						fetchData.put("GroupId", grId);
						fetchData.put("GroupName", grName);
						fetchData.put("UserName", userName);
						fetchData.put("UserId", userId);
						fetchData.put("Status", grStatus);

						fetchData.put("MobileNo", grMobileNo);
						fetchData.put("Count", grCount);
						if (usertype.equalsIgnoreCase("Manager")) {
							fetchData.put("Own", grOwn);
						}
						users.add(fetchData);
					}
				} else
					Log.i("Fleet Listener", "Fleet null");
			} else {
				userState.setVisibility(View.VISIBLE);
				userState.setText(obj.getString("Message"));
				users.clear();
				userList.setAdapter(null);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			adapter = new SimpleAdapter(getActivity(), users,
					R.layout.fleet_list_row, new String[]{"GroupId",
							"GroupName", "UserName", "UserId", "Status",
							"MobileNo", "Count", "Own"}, new int[]{R.id.rid,
							R.id.txtGroupName, R.id.txtMgrName, R.id.userid,
							R.id.isEnable, R.id.mobile, R.id.txtCounter,
							R.id.userOwn}) {
				@Override
				public View getView(int position, View convertView,
						ViewGroup parent) {
					View view = super.getView(position, convertView, parent);
					// call = (ImageView) view.findViewById(R.id.imgIcon);
					//
					// Log.d(TAG,
					// ((TextView) view.findViewById(R.id.userid))
					// .getText().toString()
					// + " "
					// + ((TextView) view
					// .findViewById(R.id.txtMgrName))
					// .getText().toString());
					//
					// String USERID = mgr.GetValueFromSharedPrefs("UserID");
					//
					// String userId = ((TextView)
					// view.findViewById(R.id.userid))
					// .getText().toString();
					//
					// final String mobile = ((TextView) view
					// .findViewById(R.id.mobile)).getText().toString();
					//
					// if (USERID.equalsIgnoreCase(userId))
					// {
					//
					// call.setVisibility(View.GONE);
					// }
					// else
					// {
					// call.setVisibility(View.VISIBLE);
					// }
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
			if (adapter.isEmpty()) {
				userState.setVisibility(View.VISIBLE);
				userState.setText(obj.getString("Message"));
			} else {
				userList.setAdapter(adapter);
				adapter.registerDataSetObserver(new DataSetObserver() {

					@Override
					public void onChanged() {
						super.onChanged();
					}
				});
				userList.setSelection(0);

				userList.smoothScrollToPosition(Global.SELECTED_ITEM_U);

				userList.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {

						Global.SELECTED_ITEM_U = -1;
						Global.SELECTED_ITEM_U = position;
						IS_User_ACTION = 1;
						String mgrName = ((TextView) view
								.findViewById(R.id.txtMgrName)).getText()
								.toString();
						String userId = ((TextView) view
								.findViewById(R.id.userid)).getText()
								.toString();
						String mobile = ((TextView) view
								.findViewById(R.id.mobile)).getText()
								.toString();
						String rid = ((TextView) view.findViewById(R.id.rid))
								.getText().toString();
						String status = ((TextView) view
								.findViewById(R.id.isEnable)).getText()
								.toString();
						usertype = mgr.GetValueFromSharedPrefs("FleetUserType");

						Log.e("Usertype", usertype);
						if (usertype.equalsIgnoreCase("Admin")) {
							Intent i = new Intent(getActivity(),
									AddFleetUserActivity.class);
							i.putExtra("GroupId", rid);
							i.putExtra("UserName", mgrName);
							i.putExtra("UserId", userId);
							i.putExtra("MobileNo", mobile);
							i.putExtra("Status", status);
							i.putExtra("isFromList", true);
							startActivity(i);
						} else if (usertype.equalsIgnoreCase("Manager")) {
							String own = ((TextView) view
									.findViewById(R.id.userOwn)).getText()
									.toString();
							if (own.equalsIgnoreCase("Y")) {
								Intent i = new Intent(getActivity(),
										AddFleetUserActivity.class);
								i.putExtra("GroupId", rid);
								i.putExtra("UserName", mgrName);
								i.putExtra("UserId", userId);
								i.putExtra("MobileNo", mobile);
								i.putExtra("Status", status);
								i.putExtra("isFromList", true);
								startActivity(i);
							}
						}
					}

				});
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// private void PhoneCall(String mobile)
	// {
	// try
	// {
	// Intent phoneCallIntent = new Intent(Intent.ACTION_DIAL);
	// phoneCallIntent.setData(Uri.parse("tel:" + mobile));
	// startActivity(phoneCallIntent);
	// }
	// catch (Exception bug)
	// {
	// Log.i(TAG, "Call failed!");
	// bug.printStackTrace();
	// }
	// }

	// private void spinnerAdapter2()
	// {
	// // Loading data to spinner
	// List<String> list = new ArrayList<String>();
	// list.add("All");
	// list.add("Active");
	// list.add("Inactive");
	// list.add("Pending");
	//
	// dataAdapter = new ArrayAdapter<String>(getActivity(),
	// android.R.layout.simple_spinner_item, list);
	// dataAdapter
	// .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	//
	// try
	// {
	// userSpinner.setAdapter(dataAdapter);
	// dataAdapter.notifyDataSetChanged();
	// userSpinner.setOnItemSelectedListener(new OnItemSelectedListener()
	// {
	//
	// @Override
	// public void onItemSelected(AdapterView<?> arg0, View arg1,
	// int arg2, long arg3)
	// {
	// switch (arg2)
	// {
	// case 0:
	// progressBar.setVisibility(View.VISIBLE);
	// userList.setVisibility(View.GONE);
	// userState.setVisibility(View.INVISIBLE);
	// serviceCall("B");
	// break;
	//
	// case 1:
	// progressBar.setVisibility(View.VISIBLE);
	// userList.setVisibility(View.GONE);
	// userState.setVisibility(View.INVISIBLE);
	// serviceCall("A");
	// break;
	//
	// case 2:
	// progressBar.setVisibility(View.VISIBLE);
	// userList.setVisibility(View.GONE);
	// userState.setVisibility(View.INVISIBLE);
	// serviceCall("N");
	// break;
	//
	// case 3:
	// progressBar.setVisibility(View.VISIBLE);
	// userList.setVisibility(View.GONE);
	// userState.setVisibility(View.INVISIBLE);
	// serviceCall("P");
	// break;
	//
	// default:
	// break;
	// }
	// }
	//
	// @Override
	// public void onNothingSelected(AdapterView<?> arg0)
	// {
	//
	// }
	//
	// });
	// }
	// catch (Exception e)
	// {
	// e.printStackTrace();
	// }
	// }

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}
}
