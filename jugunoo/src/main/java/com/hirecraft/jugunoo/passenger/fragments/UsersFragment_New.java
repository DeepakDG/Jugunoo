package com.hirecraft.jugunoo.passenger.fragments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.hirecraft.jugunoo.passenger.R;
import com.hirecraft.jugunoo.passenger.adapter.CustomGroupUserAdapter;
import com.hirecraft.jugunoo.passenger.adapter.GroupUserModel;
import com.hirecraft.jugunoo.passenger.common.Constant;
import com.hirecraft.jugunoo.passenger.common.ConstantMessages;
import com.hirecraft.jugunoo.passenger.common.Function;
import com.hirecraft.jugunoo.passenger.floatingactionbar.FloatingActionButton;
import com.hirecraft.jugunoo.passenger.networkhandler.NetworkHandler;
import com.hirecraft.jugunoo.passenger.services.VolleyErrorHelper;
import com.hirecraft.jugunoo.passenger.utility.SharedPreferencesManager;

import de.keyboardsurfer.android.widget.crouton.Crouton;

public class UsersFragment_New extends Activity
{
	private SharedPreferencesManager mgr;
	private String TAG = UsersFragment_New.class.getSimpleName();

	private ListView lvGroupUsers;
	private ProgressBar progressBar;

	// private Runnable pdRunnable;
	// private TransparentProgressDialog pd;
	// private Handler pdHandler;

	private TextView tvUserStatusGroupUser;
	ListAdapter adapter;
	private CustomGroupUserAdapter customAdapter;

	private LinearLayout llErrorState_GroupUser;
	private Button btRetry_GroupUser;

	public static int IS_User_ACTION = 0;

	private String usertype;

	ArrayAdapter<String> dataAdapter;
	private RelativeLayout rlMain_GroupUser;

	private SwipeRefreshLayout mSwipeRefreshLayout;

	private ArrayList<GroupUserModel> listOfGroupUsers;
	private ArrayList<GroupUserModel> userArrayFilter;

	private Gson gson = new Gson();

	private String selectedUserId, selectedGroupId;

	private FloatingActionButton fab;

	private int positionToBeRemoved;

	private static int REQUEST_CODE_ADD_USER = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_users_new);

		SetActionBar();

		Bundle bundle = getIntent().getExtras();
		if (bundle != null)
		{
			selectedGroupId = bundle.getString("groupId");
		}

		listOfGroupUsers = new ArrayList<GroupUserModel>();
		userArrayFilter = new ArrayList<GroupUserModel>();

		mgr = new SharedPreferencesManager(UsersFragment_New.this);

		rlMain_GroupUser = (RelativeLayout) UsersFragment_New.this
				.findViewById(R.id.rlMain_GroupUser);

		llErrorState_GroupUser = (LinearLayout) UsersFragment_New.this
				.findViewById(R.id.llErrorState_GroupUser);

		btRetry_GroupUser = (Button) UsersFragment_New.this
				.findViewById(R.id.btRetry_GroupUser);

		lvGroupUsers = (ListView) UsersFragment_New.this
				.findViewById(R.id.lvGroupUsers);

		tvUserStatusGroupUser = (TextView) UsersFragment_New.this
				.findViewById(R.id.tvUserStatusGroupUser);

		progressBar = (ProgressBar) UsersFragment_New.this
				.findViewById(R.id.progGroupUsers);

		progressBar.getIndeterminateDrawable().setColorFilter(
				Color.rgb(155, 219, 70),
				android.graphics.PorterDuff.Mode.MULTIPLY);

		mSwipeRefreshLayout = (SwipeRefreshLayout) UsersFragment_New.this
				.findViewById(R.id.user_refresh_layout_groupUser);

		mSwipeRefreshLayout.setColorSchemeResources(R.color.refresh_yellow,
				R.color.green, R.color.refresh_yellow, R.color.green);

		makeRequestToGetGroupUsersList("A", true);

		btRetry_GroupUser.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				makeRequestToGetGroupUsersList("A", true);
			}
		});

		mSwipeRefreshLayout
				.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
				{
					@Override
					public void onRefresh()
					{
						makeRequestToGetGroupUsersList("A", false);
					}
				});

		usertype = mgr.GetValueFromSharedPrefs("FleetUserType");

		// -------------To Add User-----------------

		// try
		// {
		// if (usertype.equalsIgnoreCase("Admin"))
		// {
		// addUsers.setVisibility(View.VISIBLE);
		// }
		// else if (usertype.equalsIgnoreCase("Manager"))
		// {
		// addUsers.setVisibility(View.VISIBLE);
		// }
		// else
		// {
		// addUsers.setVisibility(View.INVISIBLE);
		// }
		// addUsers.setOnClickListener(new OnClickListener()
		// {
		//
		// @Override
		// public void onClick(View v)
		// {
		// if (JugunooUtil.isConnectedToInternet(UsersFragment_New.this))
		// {
		// IS_User_ACTION = 0;
		// startActivity(new Intent(UsersFragment_New.this,
		// AddFleetUserActivity.class));
		// }
		// else
		// {
		// Function.showToast(UsersFragment_New.this,
		// "No internet connection");
		// }
		// }
		// });
		// }
		// catch (Exception e)
		// {
		// e.printStackTrace();
		// }

		// -------------To Add User-----------------

		lvGroupUsers.setOnItemLongClickListener(new OnItemLongClickListener()
		{
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int position, long arg3)
			{
				listItemOnClicked(position);

				if (usertype.equalsIgnoreCase("Admin"))
				{
					alertDeleteUser();
				}

				positionToBeRemoved = position;

				return true;
			}
		});

		fab = (FloatingActionButton) findViewById(R.id.fab);

		if (!usertype.equalsIgnoreCase("Admin"))
		{
			fab.setVisibility(View.GONE);
		}
		fab.attachToListView(lvGroupUsers);

		fab.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View arg0)
			{
				Intent intent = new Intent();
				intent.putExtra("groupId", selectedGroupId);
				intent.setClass(UsersFragment_New.this,
						AddFleetUserActivity_new.class);
				startActivityForResult(intent, REQUEST_CODE_ADD_USER);
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == REQUEST_CODE_ADD_USER && data != null)
		{
			String msg = data.getStringExtra("refresh");
			if (msg.equalsIgnoreCase("Yes"))
			{
				makeRequestToGetGroupUsersList("A", true);
			}
		}
		else
		{
			Log.d(TAG, "toast inside else");
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
		View bar = inflater.inflate(R.layout.custom_search_actionbar, l);

		ImageView imgBack = (ImageView) bar
				.findViewById(R.id.imgBackIconGroupUser);

		imgBack.setOnClickListener(new OnClickListener()
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

		final EditText etSearchUser = (EditText) bar
				.findViewById(R.id.etSearchUser);
		final ImageView ivCancel = (ImageView) bar.findViewById(R.id.ivCancle);

		etSearchUser.addTextChangedListener(new TextWatcher()
		{
			@Override
			public void onTextChanged(CharSequence charSeq, int start,
					int befor, int count)
			{
				if (charSeq.length() == 0)
				{
					ivCancel.setVisibility(View.GONE);

					// userArraySecondary.clear();
					// userArraySecondary = userArrayMain;
					listAdapter(listOfGroupUsers);
				}
				else
				{
					ivCancel.setVisibility(View.VISIBLE);

					userArrayFilter.clear();

					for (int i = 0; i < listOfGroupUsers.size(); i++)
					{
						if (listOfGroupUsers
								.get(i)
								.getUserName()
								.toLowerCase()
								.contains(
										charSeq.toString().toLowerCase().trim()))
						{
							userArrayFilter.add(listOfGroupUsers.get(i));
						}
					}
					listAdapter(userArrayFilter);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence charSeq, int start,
					int count, int after)
			{

			}

			@Override
			public void afterTextChanged(Editable s)
			{

			}
		});

		ivCancel.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View arg0)
			{
				etSearchUser.setText("");

				// userArraySecondary.clear();
				// userArraySecondary = userArrayMain;
				listAdapter(listOfGroupUsers);
			}
		});
	}

	@Override
	public void onPause()
	{
		super.onPause();
		Crouton.cancelAllCroutons();
	}

	private void alertDeleteUser()
	{
		final Dialog dialogManageGroup = new Dialog(UsersFragment_New.this);
		dialogManageGroup.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialogManageGroup
				.setContentView(R.layout.dialog_manage_group_alertdialog);

		TextView tvChoiceTitle = (TextView) dialogManageGroup
				.findViewById(R.id.tvChoiceTitle);
		tvChoiceTitle.setText("Delete User");

		tvChoiceTitle.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				dialogManageGroup.cancel();
				alertDeleteUserFromGroup();
			}
		});

		dialogManageGroup.show();
	}

	private void showLoadingDialog()
	{
		progressBar.setVisibility(View.VISIBLE);
	}

	private void cancelLoadingDialog()
	{
		progressBar.setVisibility(View.GONE);
	}

	// private void initiateRefresh()
	// {
	// Log.i("LOG", "initiateRefresh");
	// try
	// {
	// handlerswipe.postDelayed(new Runnable()
	// {
	// public void run()
	// {
	// try
	// {
	// // userspinner_pos = userSpinner.getSelectedItem()
	// // .toString();
	// // ServiceCallSpinner(userspinner_pos);
	// }
	// catch (Exception e)
	// {
	// e.printStackTrace();
	// }
	// mSwipeRefreshLayout.setRefreshing(false);
	// }
	//
	// }, 2000);
	// }
	// catch (Exception e)
	// {
	// e.printStackTrace();
	// }
	// }

	public void listItemOnClicked(int position)
	{
		for (int i = 0; i < listOfGroupUsers.size(); i++)
		{
			if (i == position)
			{
				GroupUserModel model = listOfGroupUsers.get(i);
				selectedUserId = model.getUserId();
				Log.d("selectedUserId", "selectedUserId " + selectedUserId);
				break;
			}
		}
	}

	private void alertDeleteUserFromGroup()
	{
		final Dialog dialog = new Dialog(UsersFragment_New.this);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.dialog_common_alert);

		TextView popupHeader = (TextView) dialog
				.findViewById(R.id.tvAlertHeader);
		TextView popupMsg = (TextView) dialog.findViewById(R.id.tvAlertMsg);

		Button btOk = (Button) dialog.findViewById(R.id.btAlertOk);
		Button btCancel = (Button) dialog.findViewById(R.id.btAlertCancel);

		popupMsg.setText("Are you sure want to delete this user from the group ?");
		popupHeader.setText(ConstantMessages.MSG91);

		btOk.setText(ConstantMessages.MSG93);
		btCancel.setText(ConstantMessages.MSG96);
		btCancel.setVisibility(View.VISIBLE);

		btOk.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				makeRequestToDeleteUserFromGroup(selectedUserId);
				dialog.cancel();
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

	@Override
	public void onResume()
	{
		super.onResume();
	}

	private void makeRequestToDeleteUserFromGroup(String userId)
	{
		showLoadingDialog();
		Map<String, String> params = new HashMap<String, String>();
		params.put("RID", "1");
		params.put("UserId", userId);
		params.put("Status", "N");
		params.put("GroupId", selectedGroupId);
		NetworkHandler.SaveUser(TAG, handler, params);
	}

	private void manageView(boolean Netstatus)
	{
		if (Netstatus)
		{
			rlMain_GroupUser.setVisibility(View.VISIBLE);
			llErrorState_GroupUser.setVisibility(View.GONE);
		}
		else
		{
			rlMain_GroupUser.setVisibility(View.GONE);
			llErrorState_GroupUser.setVisibility(View.VISIBLE);
		}
	}

	private void makeRequestToGetGroupUsersList(String status,
			boolean isShowRefresh)
	{
		if (isShowRefresh)
		{
			showLoadingDialog();
			llErrorState_GroupUser.setVisibility(View.GONE);
		}

		String userID = mgr.GetValueFromSharedPrefs("UserID");
		NetworkHandler.GetUsers(TAG, handler, userID, selectedGroupId, status);
	}

	Handler handler = new Handler()
	{
		public void handleMessage(android.os.Message msg)
		{
			switch (msg.arg1)
			{
				case Constant.MessageState.FLEET_USERS_SUCCESS:
					cancelLoadingDialog();
					try
					{
						manageView(true);
						mSwipeRefreshLayout.setRefreshing(false);
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
					parseGetGroupUsersList((JSONObject) msg.obj);
					break;

				case Constant.MessageState.FLEET_USERS_FAIL:
					cancelLoadingDialog();
					try
					{
						manageView(false);
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
					break;

				case Constant.MessageState.ADD_FLEET_USER_SUCCESS:
					cancelLoadingDialog();
					JSONObject result = (JSONObject) msg.obj;
					parseDeleteUser(result);
					break;

				case Constant.MessageState.ADD_FLEET_USER_FAIL:
					cancelLoadingDialog();
					VolleyErrorHelper.getMessage(msg.obj,
							UsersFragment_New.this, true);
					Log.e(TAG, "Unable to add");
					break;
			}
		};
	};

	private void parseGetGroupUsersList(JSONObject obj)
	{
		try
		{
			Log.d("TAG", "parse obj" + obj.toString());

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

				// tvUserStatusGroupUser.setVisibility(View.INVISIBLE);
				// lvGroupUsers.setVisibility(View.VISIBLE);

				JSONArray array = obj.getJSONArray("GroupArray");

				listOfGroupUsers.clear();

				if (array.length() != 0)
				{
					for (int i = 0; i < array.length(); i++)
					{
						JSONObject jsonObject = array.getJSONObject(i);

						Log.d("parseGetGroupUsersList",
								"parseGetGroupUsersList "
										+ jsonObject.toString());
						GroupUserModel logModel = gson.fromJson(
								jsonObject.toString(), GroupUserModel.class);

						listOfGroupUsers.add(logModel);
					}
					listAdapter(listOfGroupUsers);
				}
				else
				{
					Log.d("parseGetGroupUsersList",
							"parseGetGroupUsersList else");
					Function.showToast(UsersFragment_New.this,
							"You are yet to Ride on JUGUNOO");
				}
			}
			else
			{
				tvUserStatusGroupUser.setVisibility(View.VISIBLE);
				tvUserStatusGroupUser.setText(obj.getString("Message"));
				lvGroupUsers.setAdapter(null);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void parseDeleteUser(JSONObject object)
	{
		try
		{
			String message = object.getString("Message");

			if (object.getString("Result").equalsIgnoreCase("Pass"))
			{
				Function.showToast(UsersFragment_New.this,
						"User Deleted successfully");

				listOfGroupUsers.remove(positionToBeRemoved);
				listAdapter(listOfGroupUsers);
			}
			else
			{
				Function.showToast(UsersFragment_New.this, message);
			}

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void listAdapter(ArrayList<GroupUserModel> userArray)
	{
		Log.d("TAG", "listAdapter " + userArray.toString());

		customAdapter = new CustomGroupUserAdapter(UsersFragment_New.this,
				R.layout.fleet_list_row_new1, userArray);

		lvGroupUsers.setAdapter(customAdapter);
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
