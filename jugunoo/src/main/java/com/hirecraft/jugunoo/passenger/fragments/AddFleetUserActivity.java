package com.hirecraft.jugunoo.passenger.fragments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
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
import android.widget.TextView;

import com.hirecraft.jugunoo.passenger.R;
import com.hirecraft.jugunoo.passenger.adapter.FleetGroup;
import com.hirecraft.jugunoo.passenger.common.Constant;
import com.hirecraft.jugunoo.passenger.common.Function;
import com.hirecraft.jugunoo.passenger.common.Global;
import com.hirecraft.jugunoo.passenger.common.TransparentProgressDialog;
import com.hirecraft.jugunoo.passenger.listeners.OnUserName;
import com.hirecraft.jugunoo.passenger.networkhandler.NetworkHandler;
import com.hirecraft.jugunoo.passenger.services.VolleyErrorHelper;
import com.hirecraft.jugunoo.passenger.utility.JugunooUtil;
import com.hirecraft.jugunoo.passenger.utility.SharedPreferencesManager;
import com.hirecraft.jugunoo.passenger.utility.UserName;

public class AddFleetUserActivity extends Activity implements OnUserName,
		OnClickListener
{

	private static final String TAG = AddFleetUserActivity.class
			.getSimpleName();
	private TransparentProgressDialog pd;
	private Handler pdHandler;
	private Runnable pdRunnable;
	ListView listView, nameList;
	SharedPreferencesManager mgr;
	ArrayList<FleetGroup> fleetUserList;
	MyCustomAdapter dataAdapter = null;
	private static CheckBox enable;
	ImageView call, msg;
	RelativeLayout back;
	EditText username;
	RadioGroup userStatus;
	TextView title, bar, hrText;
	Button save;
	JSONArray selectedIds;
	private static String usergroupsstatus = "B";
	ArrayList<HashMap<String, String>> names;
	String usernameStr = "", currentStatus = "", userRid = "", rid = "",
			managingUserId = "", groupId = "", mobileNo = "";
	ProgressBar mobProgress;
	UserName user;
	String name, status = "";
	private static String user_name;

	int id;
	RelativeLayout errorL, groupListL;
	RadioButton rb1, rb2;
	boolean isGetName = false, isUpdate;
	private static int UPDATE_USER;
	private LinearLayout Netstate_layout, AddUser_Mainlay;
	private Button retryBtn;

	// private static boolean isFirstTime;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		SetActionBar();
		setContentView(R.layout.fragment_add_users);
		Init();
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

		RelativeLayout l = new RelativeLayout(AddFleetUserActivity.this);
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.action_bar, l);
		back = (RelativeLayout) view.findViewById(R.id.actionBackL);
		title = (TextView) view.findViewById(R.id.screenTitles);
		bar = (TextView) view.findViewById(R.id.bar2);
		save = (Button) view.findViewById(R.id.actionSave);
		call = (ImageView) view.findViewById(R.id.actionCall);
		msg = (ImageView) view.findViewById(R.id.actionMsg);
		// back.setOnClickListener(this);
		call.setOnClickListener(this);
		msg.setOnClickListener(this);
		save.setOnClickListener(this);
		title.setText("Users");
		actionBar.setCustomView(view, layoutParams);
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setDisplayShowCustomEnabled(true);
		actionBar.setDisplayUseLogoEnabled(false);
		actionBar.setDisplayShowHomeEnabled(false);

		back.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				onBackPressed();
			}
		});
	}

	private void Init()
	{
		mgr = new SharedPreferencesManager(AddFleetUserActivity.this);
		pd = new TransparentProgressDialog(AddFleetUserActivity.this,
				R.drawable.loading_image);
		pdHandler = new Handler();
		username = (EditText) findViewById(R.id.etAdUserName);
		nameList = (ListView) findViewById(R.id.nameList);

		// errorT = (TextView) findViewById(R.id.tvErrorMessage);
		Netstate_layout = (LinearLayout) findViewById(R.id.adduser_Errorstate);
		AddUser_Mainlay = (LinearLayout) findViewById(R.id.llAddUserMain);
		retryBtn = (Button) findViewById(R.id.adduser_retrybtn);
		errorL = (RelativeLayout) findViewById(R.id.rlErrorMessage);
		groupListL = (RelativeLayout) findViewById(R.id.listLayout);
		mobProgress = (ProgressBar) findViewById(R.id.usernameProgress);
		username.addTextChangedListener(userIDWatcher);
		userStatus = (RadioGroup) findViewById(R.id.addUserStatus);
		rb1 = (RadioButton) findViewById(R.id.drActive);
		rb2 = (RadioButton) findViewById(R.id.drInactive);
		listView = (ListView) findViewById(R.id.profDetailNList);
		enable = (CheckBox) findViewById(R.id.headerCheck);
		// enable.setChecked(true);
		hrText = (TextView) findViewById(R.id.txtHeader);
		mobProgress.getIndeterminateDrawable().setColorFilter(
				Color.rgb(155, 219, 70),
				android.graphics.PorterDuff.Mode.MULTIPLY);
		GetParcellableData();
		SetEvent();

		if (status.length() != 0)
		{
			if (status.equalsIgnoreCase("Active"))
			{
				rb1.setChecked(true);
				currentStatus = "A";
			}
			else
			{
				rb2.setChecked(true);
				currentStatus = "N";
			}
		}
		// prepareList();
		retryBtn.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				GetUserGroups();
			}
		});
	}

	private void SetEvent()
	{
		hrText.setText("Groups");
		enable.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (enable.isChecked())
					SellectAllList();
				else
					UnSelectAll();
			}
		});
		if (userStatus.getCheckedRadioButtonId() != -1)
		{
			int id = userStatus.getCheckedRadioButtonId();
			View radioButton = userStatus.findViewById(id);
			int radioId = userStatus.indexOfChild(radioButton);
			RadioButton btn = (RadioButton) userStatus.getChildAt(radioId);
			String selection = (String) btn.getText();
			if (selection.equalsIgnoreCase("Active"))
			{
				currentStatus = "A";
			}
			else
			{
				currentStatus = "N";
			}
		}
		userStatus.setOnCheckedChangeListener(new OnCheckedChangeListener()
		{
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId)
			{
				RadioButton fleetStatus = (RadioButton) findViewById(group
						.getCheckedRadioButtonId());
				String option = fleetStatus.getText().toString();
				if (option.equalsIgnoreCase("Active"))
				{
					currentStatus = "A";
				}
				else
				{
					currentStatus = "N";
				}
			}
		});

	}

	private void manageView(boolean Netstatus)
	{
		if (Netstatus)
		{
			AddUser_Mainlay.setVisibility(View.VISIBLE);
			Netstate_layout.setVisibility(View.GONE);
		}
		else
		{
			AddUser_Mainlay.setVisibility(View.GONE);
			Netstate_layout.setVisibility(View.VISIBLE);
		}
	}

	private void GetUserGroups()
	{
		showLoadingDilog();
		String userID = mgr.GetValueFromSharedPrefs("UserID");
		Log.e("User id", userID);
		String usertype = mgr.GetValueFromSharedPrefs("FleetUserType");
		NetworkHandler.GetGroups(TAG, handler, userID, usergroupsstatus);
	}

	private void GetParcellableData()
	{
		Intent i = getIntent();
		if (i.getExtras() != null)
		{
			String name = i.getStringExtra("UserName");
			rid = i.getStringExtra("UserId");
			groupId = i.getStringExtra("GroupId");
			mobileNo = i.getStringExtra("MobileNo");
			status = i.getStringExtra("Status");
			isGetName = true;
			if (i.hasExtra("isFromList"))
			{
				isUpdate = true;
				groupListL.setVisibility(View.GONE);
				username.setText(name);
				// isFirstTime = true;
			}
			selectedIds = new JSONArray();
			selectedIds.put(groupId);
			username.setText(name);
			managingUserId = rid;
		}
		else
		{
			GetUserGroups();
		}
		int action = UsersFragment.IS_User_ACTION;
		if (action == 0)
		{
			currentStatus = "A";
			userRid = "0";
			username.setEnabled(true);
			call.setVisibility(View.GONE);
			msg.setVisibility(View.GONE);
			bar.setVisibility(View.GONE);
		}
		else
		{
			username.setEnabled(false);
			groupListL.setVisibility(View.GONE);
			userRid = "1";
		}
	}

	@Override
	public void onResume()
	{
		super.onResume();
		// prepareList();
		// GetUserGroups();
	}

	private TextWatcher userIDWatcher = new TextWatcher()
	{
		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count)
		{
			usernameStr = username.getText().toString();
			String userId = mgr.GetValueFromSharedPrefs("UserID");
			if (count % 2 == 1)
			{
				String url = Global.JUGUNOO_WS
						+ "Passenger/GetNameByMobile?Mobile=" + usernameStr
						+ "&UserId=" + userId;
				getUserNameReq(url);
			}
			if (usernameStr.length() < 1)
			{
				Function.showToast(AddFleetUserActivity.this,
						"Enter a valid username or mobile number.");
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
			if (usernameStr.length() == 0)
				nameList.setVisibility(View.GONE);
		}
	};

	private void getUserNameReq(String url)
	{
		if (isGetName == false)
		{
			mobProgress.setVisibility(View.VISIBLE);
			NetworkHandler.GetUserNames(TAG, handler, url);
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

	private class MyCustomAdapter extends ArrayAdapter<FleetGroup>
	{
		private ArrayList<FleetGroup> fleetUserList1;

		public MyCustomAdapter(Context context, int textViewResourceId,
				ArrayList<FleetGroup> fleetGroupList)
		{
			super(context, textViewResourceId, fleetGroupList);
			this.fleetUserList1 = new ArrayList<FleetGroup>();
			this.fleetUserList1.addAll(fleetGroupList);
		}

		public class ViewHolder
		{
			TextView name;
			TextView status;
			CheckBox checkBox;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			ViewHolder holder = null;
			Log.v("ConvertView", String.valueOf(position));

			if (convertView == null)
			{
				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				LinearLayout l = new LinearLayout(AddFleetUserActivity.this);
				convertView = vi.inflate(R.layout.edit_fleet_status_row, l);
				holder = new ViewHolder();
				holder.name = (TextView) convertView
						.findViewById(R.id.grNameTOSelect);
				holder.status = (TextView) convertView
						.findViewById(R.id.grStatus);
				holder.checkBox = (CheckBox) convertView
						.findViewById(R.id.isSelect);
				convertView.setTag(holder);
				holder.checkBox.setOnClickListener(new View.OnClickListener()
				{
					public void onClick(View v)
					{
						boolean state;
						CheckBox cb = (CheckBox) v;
						FleetGroup model = (FleetGroup) cb.getTag();
						state = cb.isChecked();
						model.setChecked(state);
						if (EnableCheck(fleetUserList1) == true)
						{
							enable.setChecked(true);
						}
						else
						{
							enable.setChecked(false);
						}
					}
				});

			}
			else
			{
				holder = (ViewHolder) convertView.getTag();
			}

			final FleetGroup fleetGroup = fleetUserList1.get(position);
			holder.checkBox.setChecked(fleetGroup.isChecked());
			holder.checkBox.setText(fleetGroup.getName());
			holder.checkBox.setTag(fleetGroup);
			return convertView;
		}
	}

	private boolean EnableCheck(ArrayList<FleetGroup> fleetUserList)
	{
		boolean state = false;
		for (int i = 0; i < fleetUserList.size(); i++)
		{
			if (fleetUserList.get(i).isChecked())
			{
				state = true;
			}
			else
			{
				state = false;
				break;
			}
		}
		return state;
	}

	private void SellectAllList()
	{
		dataAdapter = new MyCustomAdapter(AddFleetUserActivity.this,
				R.layout.edit_fleet_status_row, fleetUserList)
		{
			@Override
			public View getView(int position, View convertView, ViewGroup parent)
			{
				View view = super.getView(position, convertView, parent);
				CheckBox check = (CheckBox) view.findViewById(R.id.isSelect);
				FleetGroup model = (FleetGroup) check.getTag();
				model.setChecked(true);
				return view;
			}
		};
		dataAdapter.notifyDataSetChanged();
		// Assign adapter to ListView
		listView.setAdapter(dataAdapter);
	}

	private void UnSelectAll()
	{
		dataAdapter = new MyCustomAdapter(AddFleetUserActivity.this,
				R.layout.edit_fleet_status_row, fleetUserList)
		{
			@Override
			public View getView(int position, View convertView, ViewGroup parent)
			{
				View view = super.getView(position, convertView, parent);
				CheckBox check = (CheckBox) view.findViewById(R.id.isSelect);
				FleetGroup model = (FleetGroup) check.getTag();
				model.setChecked(false);
				return view;
			}

		};
		dataAdapter.notifyDataSetChanged();
		// Assign adapter to ListView
		listView.setAdapter(dataAdapter);
	}

	private void openAnimation()
	{
		overridePendingTransition(R.anim.activity_open_translate,
				R.anim.activity_close_scale);
	}

	private void closeAnimation()
	{
		/**
		 * Closing transition animations.
		 */
		overridePendingTransition(R.anim.activity_open_scale,
				R.anim.activity_close_translate);
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		closeAnimation();
		// Global.getInstance().getRequestQueue().cancelAll(TAG);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{

		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0)
		{

			if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0)
			{
				/*
				 * startActivity(new Intent(AddFleetUsers.this,
				 * PassengerProfile.class));
				 */
				AddFleetUserActivity.this.finish();
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void OnUserName(ArrayList<HashMap<String, String>> list)
	{

		HashMap<String, String> ma;

		if (list == null)
		{
			mobProgress.setVisibility(View.GONE);
			// JugunooUtil.showErrorMessage(AddFleetUsers.this, errorL, errorT,
			// "Enter a valid User ID.");
			nameList.setVisibility(View.GONE);
		}
		else
		{
			mobProgress.setVisibility(View.GONE);
			names = new ArrayList<HashMap<String, String>>();
			for (HashMap<String, String> map : list)
			{
				for (Entry<String, String> mapEntry : map.entrySet())
				{
					String name = mapEntry.getValue();
					String id = mapEntry.getValue();
					ma = new HashMap<String, String>();
					ma.put("FirstName", name);
					ma.put("RID", id);
					names.add(ma);
					nameList.setVisibility(View.VISIBLE);

					ListAdapter adapter = new SimpleAdapter(
							AddFleetUserActivity.this, list,
							R.layout.username_row, new String[]
							{ "FirstName", "RID" }, new int[]
							{ R.id.name, R.id.rid });
					nameList.setAdapter(adapter);
					nameList.setOnItemClickListener(new OnItemClickListener()
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
							managingUserId = rid;
							Log.i("tag", "grName: " + grName + ", rid" + rid);
							nameList.setVisibility(View.GONE);
						}
					});
				}
			}

		}

	}

	private void saveUser()
	{
		if (userRid == "0")
		{
			if (!isUpdate)
			{
				selectedIds = new JSONArray();
				for (int i = 0; i < fleetUserList.size(); i++)
				{
					if (fleetUserList.get(i).isChecked())
					{
						selectedIds.put(fleetUserList.get(i).getId());
					}
				}
			}
		}

		if (saveUserValidation() != false)
		{

			showLoadingDilog();
			String groupIds = selectedIds.toString();
			groupIds = groupIds.replaceAll("\\[", "").replaceAll("\\]", "")
					.replaceAll("\"", "");

			Map<String, String> params = new HashMap<String, String>();
			params.put("RID", userRid);
			params.put("UserId", managingUserId);
			params.put("Status", currentStatus);
			params.put("GroupId", groupIds);
			NetworkHandler.SaveUser(TAG, handler, params);

		}
		else
		{
			Log.i(Global.APPTAG, "Parameters are invalid!");
		}
	}

	private boolean saveUserValidation()
	{
		boolean flag = false;
		user_name = username.getText().toString();

		// int action = PassengerProfileActivity.IS_GROUP_ACTION;
		if (!(JugunooUtil.isConnectedToInternet(getApplicationContext())))
		{
			Function.showToast(AddFleetUserActivity.this,
					"No internet connection");
		}
		else if (user_name.equalsIgnoreCase(""))
		{
			Function.showToast(AddFleetUserActivity.this,
					"Enter a mobile number for user.");
		}
		else if (userRid == "0" && managingUserId.equalsIgnoreCase(""))
		{
			Function.showToast(AddFleetUserActivity.this,
					"Entered mobile number doesnot exists.");
		}
		else if (currentStatus.equalsIgnoreCase(""))
		{
			Function.showToast(AddFleetUserActivity.this,
					"User status does not match.");
		}
		else if (selectedIds.toString().equalsIgnoreCase("[]"))
		{
			Function.showToast(AddFleetUserActivity.this,
					"Select at least one group.");
		}
		else
			flag = true;

		return flag;

	}

	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
			case R.id.actionSave:
				saveUser();
				break;

			case R.id.actionCall:
				Call();
				break;

			case R.id.actionMsg:
				Msg();
				break;

			case R.id.actionBackL:
				// startActivity(new Intent(AddFleetUsers.this,
				// PassengerProfile.class));
				AddFleetUserActivity.this.finish();
				break;

		}
	}

	private void Call()
	{
		if (!mobileNo.equalsIgnoreCase(""))
		{
			try
			{
				Intent phoneCallIntent = new Intent(Intent.ACTION_DIAL);
				phoneCallIntent.setData(Uri.parse("tel:" + mobileNo));
				startActivity(phoneCallIntent);
			}
			catch (Exception bug)
			{
				Function.showToast(AddFleetUserActivity.this, "Call failed!");
				bug.printStackTrace();
			}
		}
	}

	private void Msg()
	{
		if (!mobileNo.equalsIgnoreCase(""))
		{
			try
			{
				Intent smsIntent = new Intent(Intent.ACTION_VIEW);
				smsIntent.setType("vnd.android-dir/mms-sms");
				smsIntent.putExtra("address", mobileNo);
				smsIntent.putExtra("sms_body", "Hi....");
				startActivity(smsIntent);
			}
			catch (Exception bug)
			{
				Function.showToast(AddFleetUserActivity.this, "Send failed!");
				bug.printStackTrace();
			}
		}
	}

	private Handler handler = new Handler(new Handler.Callback()
	{
		@Override
		public boolean handleMessage(Message msg)
		{
			switch (msg.arg1)
			{
				case Constant.MessageState.ADD_FLEET_USER_SUCCESS:
					cancelLoadingDialog();
					JSONObject result = (JSONObject) msg.obj;
					PrepareResult(result);
					break;

				case Constant.MessageState.ADD_FLEET_USER_FAIL:
					cancelLoadingDialog();
					VolleyErrorHelper.getMessage(msg.obj,
							AddFleetUserActivity.this, true);
					Log.e(TAG, "Unable to add");
					break;

				case Constant.MessageState.FLEET_GROUP_SUCCESS:
					try
					{
						manageView(true);
						cancelLoadingDialog();
						PrepareUserGroupList((JSONObject) msg.obj);
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
					break;

				case Constant.MessageState.FLEET_GROUP_FAIL:
					try
					{
						manageView(false);
						cancelLoadingDialog();
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
					// Function.showToast(getActivity(),
					// Constant.network_err_msg);
					break;

				case Constant.MessageState.USERNAMES_SUCCESS:
					mobProgress.setVisibility(View.GONE);
					result = (JSONObject) msg.obj;
					PrepareUserList(result);
					break;

				case Constant.MessageState.USERNAMES_FAIL:
					mobProgress.setVisibility(View.GONE);
					nameList.setVisibility(View.GONE);
					break;
			}
			return false;
		}
	});

	private void PrepareUserGroupList(JSONObject obj)
	{
		try
		{
			String Result = obj.getString("Result");
			if (Result.equalsIgnoreCase("Pass"))
			{
				Log.i(TAG,
						"userType -->"
								+ mgr.GetValueFromSharedPrefs(Constant.USER_TYPE));

				// Condition to check userType
				if (!mgr.GetValueFromSharedPrefs("UserType").equalsIgnoreCase(
						"UserType"))
				{
					mgr.SaveValueToSharedPrefs(Constant.USER_TYPE,
							obj.getString("UserType"));
				}
			}
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}

		try
		{
			JSONArray array = obj.getJSONArray("GroupArray");
			fleetUserList = new ArrayList<FleetGroup>();
			if (!array.isNull(0))
			{
				for (int i = 0; i < array.length(); i++)
				{
					JSONObject object = array.getJSONObject(i);
					String usertype = mgr
							.GetValueFromSharedPrefs("FleetUserType");
					if (usertype.equalsIgnoreCase("Manager"))
					{
						String grIsOwn = object.getString("Own");
						if (grIsOwn.equalsIgnoreCase("Y"))
						{
							name = object.getString("GroupName");
							status = object.getString("Status");
							id = object.getInt("GroupId");
							FleetGroup groupList = new FleetGroup(name, status,
									id, false);
							fleetUserList.add(groupList);
						}
					}
					else
					{
						name = object.getString("GroupName");
						status = object.getString("Status");
						id = object.getInt("GroupId");
						FleetGroup groupList = new FleetGroup(name, status, id,
								false);
						fleetUserList.add(groupList);
					}

					if (array.length() == 1)
					{
						SellectAllList();
					}
				}
			}
			else
			{
				Log.i(TAG, "Groups Null");
			}
		}
		catch (JSONException e1)
		{
			e1.printStackTrace();
		}

		// create an ArrayAdaptar from the String Array
		MyCustomAdapter dataAdapter = new MyCustomAdapter(
				getApplicationContext(), R.layout.edit_fleet_status_row,
				fleetUserList);

		if (EnableCheck(fleetUserList) == true)
		{
			enable.setChecked(true);
		}
		else
		{
			enable.setChecked(false);
		}
		// Assign adapter to ListView
		listView.setAdapter(dataAdapter);
	}

	private void PrepareUserList(JSONObject result)
	{
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
							String rid = obj.getString("RID");

							HashMap<String, String> fetchData = new HashMap<String, String>();
							fetchData.put("FirstName", grName);
							fetchData.put("RID", rid);
							names.add(fetchData);
							nameList.setVisibility(View.VISIBLE);

							ListAdapter adapter = new SimpleAdapter(
									AddFleetUserActivity.this, names,
									R.layout.username_row, new String[]
									{ "FirstName", "RID" }, new int[]
									{ R.id.name, R.id.rid });
							nameList.setAdapter(adapter);
							nameList.setOnItemClickListener(new OnItemClickListener()
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
									managingUserId = rid;
									Log.i("tag", "grName: " + grName + ", rid"
											+ rid);
									nameList.setVisibility(View.GONE);
									InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
									imm.hideSoftInputFromWindow(
											username.getWindowToken(), 0);
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

	private void PrepareResult(JSONObject object)
	{
		try
		{
			String resultStr = object.getString("Result");
			String message = object.getString("Message");

			if (!resultStr.equalsIgnoreCase("Fail"))
			{
				Global.IsUserstatUpdated = true;
				Function.showToast(AddFleetUserActivity.this, message);
				NavToParent();
			}
			else
			{
				Function.showToast(AddFleetUserActivity.this, message);
			}

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void NavToParent()
	{
		final Handler handler = new Handler();
		Thread thread = new Thread(new Runnable()
		{

			@Override
			public void run()
			{
				try
				{
					Thread.sleep(2000);
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}

				handler.post(new Runnable()
				{

					@Override
					public void run()
					{
						UPDATE_USER = 1;
						/*
						 * startActivity(new Intent(AddFleetUsers.this,
						 * PassengerProfile.class));
						 */
						AddFleetUserActivity.this.finish();
					}
				});
			}
		});
		thread.start();
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