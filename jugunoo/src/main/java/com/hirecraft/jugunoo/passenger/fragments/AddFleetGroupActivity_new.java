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
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
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
import android.view.Window;
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
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.hirecraft.jugunoo.passenger.R;
import com.hirecraft.jugunoo.passenger.adapter.FleetGroup;
import com.hirecraft.jugunoo.passenger.common.Constant;
import com.hirecraft.jugunoo.passenger.common.ConstantMessages;
import com.hirecraft.jugunoo.passenger.common.Function;
import com.hirecraft.jugunoo.passenger.common.Global;
import com.hirecraft.jugunoo.passenger.common.TransparentProgressDialog;
import com.hirecraft.jugunoo.passenger.common.Validation;
import com.hirecraft.jugunoo.passenger.listeners.OnUserName;
import com.hirecraft.jugunoo.passenger.networkhandler.NetworkHandler;
import com.hirecraft.jugunoo.passenger.services.VolleyErrorHelper;
import com.hirecraft.jugunoo.passenger.utility.JugunooUtil;
import com.hirecraft.jugunoo.passenger.utility.SharedPreferencesManager;
import com.hirecraft.jugunoo.passenger.utility.UserName;

public class AddFleetGroupActivity_new extends Activity implements OnUserName,
		OnClickListener
{

	private static final String TAG = AddFleetGroupActivity_new.class
			.getSimpleName();
	private TransparentProgressDialog pd;
	private Handler pdHandler;
	private Runnable pdRunnable;
	ListView listView, nameList;
	MyCustomAdapter dataAdapter = null;
	private static CheckBox enable;
	ArrayList<FleetGroup> fleetGroupList;
	EditText grname, mgrname;

	TextView title;

	ImageView imgDeleteGroup;

	TextView tvBarAddGroup;

	// TextView errorT;
	ImageView save;
	JSONArray selectedIds;
	SharedPreferencesManager mgr;
	ArrayList<HashMap<String, String>> names;
	String mgrStr = "", grStr = "", grnames = "", mgrnames = "", mgrid = "",
			currentStatus = "", groupId = "", managingUserId = "";

	// String mobileNo = "", status = "";

	ProgressBar mobProgress;
	UserName user;
	RelativeLayout errorL;
	LinearLayout llBackAddGroup;
	boolean isGetName = false;
	public JSONArray fleetArrayactive;
	private ArrayList<String> groupfleetActive = new ArrayList<String>();
	FleetGroup groupList;
	private static String managername;
	private static String fleetarraystatus = "B";
	private static int CURRENT_TAB = 0;
	private static int UPDATE_USER;
	JSONArray fleetarray;
	private LinearLayout Netstate_layout, Addfleet_Mainlay;
	private Button retryBtn;
	private static boolean isFirstTime;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		SetActionBar();
		setContentView(R.layout.fragment_add_groups_new);
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

		RelativeLayout l = new RelativeLayout(AddFleetGroupActivity_new.this);
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		View view = inflater.inflate(R.layout.action_bar_new, l);

		title = (TextView) view.findViewById(R.id.tvScreenTitleAddGroup);

		tvBarAddGroup = (TextView) view.findViewById(R.id.tvBarAddGroup);
		imgDeleteGroup = (ImageView) view.findViewById(R.id.imgDeleteGroup);
		imgDeleteGroup.setOnClickListener(this);

		save = (ImageView) view.findViewById(R.id.imgSaveGroup);
		llBackAddGroup = (LinearLayout) view.findViewById(R.id.llBackAddGroup);

		save.setOnClickListener(this);
		title.setText("Groups");
		actionBar.setCustomView(view, layoutParams);
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setDisplayShowCustomEnabled(true);
		actionBar.setDisplayUseLogoEnabled(false);
		actionBar.setDisplayShowHomeEnabled(false);

		llBackAddGroup.setOnClickListener(new OnClickListener()
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
		mgr = new SharedPreferencesManager(AddFleetGroupActivity_new.this);
		pd = new TransparentProgressDialog(AddFleetGroupActivity_new.this,
				R.drawable.loading_image);
		pdHandler = new Handler();
		grname = (EditText) findViewById(R.id.etGroupName);
		mgrname = (EditText) findViewById(R.id.etMgrUserName);
		mgrname.addTextChangedListener(userIDWatcher);
		nameList = (ListView) findViewById(R.id.nameList);
		listView = (ListView) findViewById(R.id.groupDList);
		enable = (CheckBox) findViewById(R.id.headerCheck);
		Netstate_layout = (LinearLayout) findViewById(R.id.addfleet_Errorstate);
		Addfleet_Mainlay = (LinearLayout) findViewById(R.id.llAddGroupMain);
		retryBtn = (Button) findViewById(R.id.addfleet_retrybtn);
		TextView hrText = (TextView) findViewById(R.id.txtHeader);
		errorL = (RelativeLayout) findViewById(R.id.rlErrorMessage);
		mobProgress = (ProgressBar) findViewById(R.id.usernameProgress);
		mobProgress.getIndeterminateDrawable().setColorFilter(
				Color.rgb(155, 219, 70),
				android.graphics.PorterDuff.Mode.MULTIPLY);

		Intent i = getIntent();
		if (i.getExtras() != null)
		{
			tvBarAddGroup.setVisibility(View.VISIBLE);
			imgDeleteGroup.setVisibility(View.VISIBLE);

			groupId = i.getStringExtra("GroupId");
			grnames = i.getStringExtra("GrName");

			mgrid = i.getStringExtra("ManagingUserId");
			mgrnames = i.getStringExtra("MgrName");

			grname.setText(grnames);
			mgrname.setText(mgrnames);
			managingUserId = mgrid;
			isFirstTime = true;
			GetActiveFleetsArrays();
		}
		else
		{
			tvBarAddGroup.setVisibility(View.GONE);
			imgDeleteGroup.setVisibility(View.GONE);
			showLoadingDilog();
			GetFleetArrays();
			groupId = "0";
		}

		hrText.setText("Fleet Operators");
		// enable.setChecked(true);
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

		retryBtn.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				GetActiveFleetsArrays();
				GetFleetArrays();
			}
		});
	}

	private void GetActiveFleetsArrays()
	{
		showLoadingDilog();
		NetworkHandler.GetGroupById(TAG, handler, groupId);
	}

	private void GetFleetArrays()
	{
		String userID = mgr.GetValueFromSharedPrefs("UserID");
		String usertype = mgr.GetValueFromSharedPrefs("FleetUserType");
		// Jugunoo getting the fleet array by url
		NetworkHandler.GetUserFleet(TAG, handler, userID, fleetarraystatus,
				usertype);
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

	private void parserGroupfleet(JSONObject result)
	{
		if (result != null)
		{
			try
			{
				String resultStr = result.getString("Result");
				if (resultStr.equalsIgnoreCase("Pass"))
				{
					GetFleetArrays();

					JSONObject groupObj = result.getJSONObject("GroupArray");
					fleetArrayactive = groupObj.getJSONArray("FleetUserId");
					for (int i = 0; i < fleetArrayactive.length(); i++)
					{
						groupfleetActive
								.add(fleetArrayactive.get(i).toString());
						Log.d("Fleet ", "Fleet size="
								+ fleetArrayactive.get(i).toString());
					}
				}
				else
				{
					pdHandler.removeCallbacks(pdRunnable);
					if (pd.isShowing())
					{
						pd.dismiss();
					}
					Function.showToast(AddFleetGroupActivity_new.this,
							result.getString("Message"));
					Log.i("JSONArray fleetArrayactive",
							"No fleetArrayactive available");
				}
			}
			catch (Exception bug)
			{
				bug.printStackTrace();
			}

		}
		else
		{
			Log.d("Fleet", "Fleet size=" + "No ACtiveFleetArrays");
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

	private boolean checkMGRValidation()
	{
		boolean flag = true;
		if (!Validation.hasText(mgrname))
		{
			flag = false;
		}
		else
		{
			flag = true;
			mgrStr = mgrname.getText().toString();
		}
		return flag;
	}

	private class MyCustomAdapter extends ArrayAdapter<FleetGroup>
	{
		private ArrayList<FleetGroup> fleetGroupList1;

		public MyCustomAdapter(Context context, int textViewResourceId,
				ArrayList<FleetGroup> fleetGroupList)
		{
			super(context, textViewResourceId, fleetGroupList);
			this.fleetGroupList1 = new ArrayList<FleetGroup>();
			this.fleetGroupList1.addAll(fleetGroupList);
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
				LinearLayout l = new LinearLayout(
						AddFleetGroupActivity_new.this);
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
						if (EnableCheck(fleetGroupList1) == true)
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

			final FleetGroup fleetGroup = fleetGroupList1.get(position);
			// *jugunoo viewgroup checking the checkbox from view
			holder.checkBox.setText(fleetGroup.getName());
			holder.checkBox.setTag(fleetGroup);
			holder.checkBox.setChecked(fleetGroup.isChecked);
			return convertView;
		}
	}

	private void manageView(boolean Netstatus)
	{
		if (Netstatus)
		{
			Addfleet_Mainlay.setVisibility(View.VISIBLE);
			Netstate_layout.setVisibility(View.GONE);
		}
		else
		{
			Addfleet_Mainlay.setVisibility(View.GONE);
			Netstate_layout.setVisibility(View.VISIBLE);
		}
	}

	private void SellectAllList()
	{
		dataAdapter = new MyCustomAdapter(AddFleetGroupActivity_new.this,
				R.layout.edit_fleet_status_row, fleetGroupList)
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
		dataAdapter = new MyCustomAdapter(AddFleetGroupActivity_new.this,
				R.layout.edit_fleet_status_row, fleetGroupList)
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
		Global.getInstance().getRequestQueue().cancelAll(TAG);
	}

	private TextWatcher userIDWatcher = new TextWatcher()
	{

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count)
		{

			mgrStr = mgrname.getText().toString();
			String userId = mgr.GetValueFromSharedPrefs("UserID");
			if (count % 2 == 1)
			{
				String url = Global.JUGUNOO_WS
						+ "Passenger/GetNameByMobile?Mobile="
						+ mgrStr.toString() + "&UserId=" + userId;
				getUserNameReq(url);
			}
			if (mgrStr.length() < 1)
			{
				Function.showToast(AddFleetGroupActivity_new.this,
						"Enter a mobile number for managing user.");
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
			// if (mgrStr.length() == 0)
			// isGetName = false;
			// else if (checkMGRValidation() == false)
			// JugunooUtil.showErrorMessage(AddFleetGroups.this, errorL,
			// errorT, "Enter a valid User ID.");
			if (mgrStr.length() == 0)
				isGetName = false;
			else if (checkMGRValidation() == false)
				Function.showToast(AddFleetGroupActivity_new.this,
						"Entered mobile number doesnot exists.");
			// added for clearing listview when edittext cleared totally
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

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0)
		{

			if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0)
			{
				/*
				 * startActivity(new Intent(AddFleetGroups.this,
				 * PassengerProfile.class));
				 */
				AddFleetGroupActivity_new.this.finish();
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
							AddFleetGroupActivity_new.this, list,
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

							mgrname.setText(grName);
							managingUserId = rid;
							Log.i("tag", "grName: " + grName + ", rid" + rid);
							nameList.setVisibility(View.GONE);
							isGetName = true;
							InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
							imm.hideSoftInputFromWindow(
									mgrname.getWindowToken(), 0);
						}
					});
				}
			}
		}
	}

	private void saveGroup()
	{
		selectedIds = new JSONArray();
		for (int i = 0; i < fleetGroupList.size(); i++)
		{
			if (fleetGroupList.get(i).isChecked())
			{
				selectedIds.put(fleetGroupList.get(i).getId());
			}
		}

		grnames = grname.getText().toString();
		managername = mgrname.getText().toString();

		if (saveGroupValidation(grnames) != false
				&& !(managername.equalsIgnoreCase("")))
		{
			showLoadingDilog();
			String groupIds = selectedIds.toString();
			groupIds = groupIds.replaceAll("\\[", "").replaceAll("\\]", "");

			Map<String, String> params = new HashMap<String, String>();
			params.put("RID", groupId);
			params.put("CreatedUserId", mgr.GetValueFromSharedPrefs("UserID"));
			params.put("ManagingUserId", managingUserId);
			params.put("Status", "A");
			params.put("FleetUserId", groupIds);
			params.put("Name", grnames);

			Log.e(TAG, "save params=" + params);
			NetworkHandler.SaveGroup(TAG, handler, params);
		}
		else
		{
			Log.i(Global.APPTAG, "Parameters are invalid!");
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
					Thread.sleep(2500);
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
						 * startActivity(new Intent(AddFleetGroups.this,
						 * PassengerProfile.class));
						 */
						AddFleetGroupActivity_new.this.finish();
					}
				});
			}
		});
		thread.start();
	}

	private boolean saveGroupValidation(String grnames)
	{
		boolean flag = false;
		if (JugunooUtil.isConnectedToInternet(getApplicationContext()))
		{
			if (grnames.equalsIgnoreCase(""))
				Function.showToast(AddFleetGroupActivity_new.this,
						"Enter a group name.");

			else if (managername.equalsIgnoreCase(""))
				Function.showToast(AddFleetGroupActivity_new.this,
						"Enter a mobile number for managing user.");

			else if (mgr.GetValueFromSharedPrefs("UserID").equalsIgnoreCase(""))
				Function.showToast(AddFleetGroupActivity_new.this,
						"Group details not available.");

			else if (managingUserId.equalsIgnoreCase(""))
				Function.showToast(AddFleetGroupActivity_new.this,
						"Entered mobile number doesnot exists.");

			// else if (currentStatus.equalsIgnoreCase(""))
			// Function.showToast(AddFleetGroupActivity_new.this,
			// "Group status not updated.");

			else if (selectedIds.toString().equalsIgnoreCase("[]"))
				Function.showToast(AddFleetGroupActivity_new.this,
						"Select at least one fleet operator.");

			else
				flag = true;
		}
		else
		{
			Function.showToast(AddFleetGroupActivity_new.this,
					"No internet connection");
		}
		return flag;
	}

	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
			case R.id.imgSaveGroup:
				try
				{
					saveGroup();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				break;

			case R.id.llBackAddGroup:
				/*
				 * startActivity(new Intent(AddFleetGroups.this,
				 * PassengerProfile.class));
				 */
				AddFleetGroupActivity_new.this.finish();
				break;

			case R.id.imgDeleteGroup:

				alertDeleteGroup();
				break;

		}
	}

	private void alertDeleteGroup()
	{

		final Dialog dialog = new Dialog(AddFleetGroupActivity_new.this);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.dialog_common_alert);

		TextView popupHeader = (TextView) dialog
				.findViewById(R.id.tvAlertHeader);
		TextView popupMsg = (TextView) dialog.findViewById(R.id.tvAlertMsg);

		Button btOk = (Button) dialog.findViewById(R.id.btAlertOk);
		Button btCancel = (Button) dialog.findViewById(R.id.btAlertCancel);

		popupMsg.setText("Are you sure want to delete this group ?");
		popupHeader.setText(ConstantMessages.MSG91);

		btOk.setText(ConstantMessages.MSG93);
		btCancel.setText(ConstantMessages.MSG96);
		btCancel.setVisibility(View.VISIBLE);

		btOk.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				// makeDeleteGroupReq(selectedGroupId);
				dialog.cancel();
				// startActivity(new
				// Intent().setClass(AddFleetGroupActivity_new.this, ));

				AddFleetGroupActivity_new.this.finish();
				onBackPressed();
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

	private Handler handler = new Handler()
	{

		@Override
		public void handleMessage(Message msg)
		{
			JSONObject result = null;
			JSONObject fleets = null;

			switch (msg.arg1)
			{
				case Constant.MessageState.ADD_FLEET_GROUP_SUCCESS:
					try
					{
						manageView(true);
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
					cancelLoadingDialog();
					result = (JSONObject) msg.obj;
					PrepareResult(result);
					break;

				case Constant.MessageState.ADD_FLEET_GROUP_FAIL:
					try
					{
						manageView(false);
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
					cancelLoadingDialog();
					VolleyErrorHelper.getMessage(msg.obj,
							AddFleetGroupActivity_new.this, true);
					Log.e(TAG, "Unable to add");
					break;

				case Constant.MessageState.FLEET_OPERATOR_SUCCESS:
					try
					{
						manageView(true);
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
					cancelLoadingDialog();
					fleets = (JSONObject) msg.obj;
					PreparefleetList(fleets);
					break;

				case Constant.MessageState.FLEET_OPERATOR_FAIL:
					try
					{
						manageView(false);
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
					cancelLoadingDialog();
					VolleyErrorHelper.getMessage(msg.obj,
							AddFleetGroupActivity_new.this, true);
					break;

				case Constant.MessageState.USERNAMES_SUCCESS:
					try
					{
						manageView(true);
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
					mobProgress.setVisibility(View.GONE);
					result = (JSONObject) msg.obj;
					PrepareUserList(result);
					break;

				case Constant.MessageState.USERNAMES_FAIL:
					mobProgress.setVisibility(View.GONE);
					nameList.setVisibility(View.GONE);
					break;

				case Constant.MessageState.GROUP_FLEET_SUCCESS:
					try
					{
						manageView(true);
						mobProgress.setVisibility(View.GONE);
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
					parserGroupfleet((JSONObject) msg.obj);
					break;

				case Constant.MessageState.FAIL:
					try
					{
						manageView(false);
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
					cancelLoadingDialog();
					VolleyErrorHelper.getMessage(msg.obj,
							AddFleetGroupActivity_new.this, true);
					break;
			}
		}
	};

	private void PreparefleetList(JSONObject object)
	{
		try
		{
			String resultStr = object.getString("Result");
			String message = object.getString("Message");
			if (!resultStr.equalsIgnoreCase("Fail"))
			{
				fleetarray = object.getJSONArray("FleetArray");
				fleetGroupList = new ArrayList<FleetGroup>();
				Log.d("Fleet ", "Fleet size=" + groupfleetActive.size());
				if (!fleetarray.isNull(0))
				{
					for (int i = 0; i < fleetarray.length(); i++)
					{
						try
						{
							JSONObject objectfleet = fleetarray
									.getJSONObject(i);
							String name = objectfleet.getString("GroupName");
							String status = objectfleet.getString("Status");
							int id = objectfleet.getInt("FleetId");
							boolean flag = groupfleetActive.contains(String
									.valueOf(id));
							// jugunoo Getting only fleet operator list
							groupList = new FleetGroup(name, status, id, flag);
							fleetGroupList.add(groupList);
						}
						catch (JSONException e)
						{
							e.printStackTrace();
						}
					}
				}
				// create an ArrayAdaptar from the String Array
				MyCustomAdapter dataAdapter = new MyCustomAdapter(
						getApplicationContext(),
						R.layout.edit_fleet_status_row, fleetGroupList);
				if (EnableCheck(fleetGroupList) == true)
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
			else
			{
				Log.d("AddfleetsGroups", "Null Fleets");
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
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
									AddFleetGroupActivity_new.this, names,
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

									mgrname.setText(grName);
									managingUserId = rid;
									Log.i("tag", "grName: " + grName + ", rid"
											+ rid);
									nameList.setVisibility(View.GONE);
									isGetName = true;
									InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
									imm.hideSoftInputFromWindow(
											mgrname.getWindowToken(), 0);
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
					if (isFirstTime)
					{
						isFirstTime = false;
					}
					else
					{
						managingUserId = "";
						isFirstTime = true;
					}
				}

			}
			catch (Exception bug)
			{
				bug.printStackTrace();
			}
		}
	}

	// Parse Group Fleet for checkbox Method
	private void PrepareResult(JSONObject object)
	{
		Log.e(TAG, "result up=" + object);
		try
		{
			String resultStr = object.getString("Result");
			String message = object.getString("Message");
			if (!resultStr.equalsIgnoreCase("Fail"))
			{
				Global.IsGroupstatUpdated = true;
				Function.showToast(AddFleetGroupActivity_new.this, message);
				NavToParent();
			}
			else
			{
				Function.showToast(AddFleetGroupActivity_new.this, message);
			}

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
	protected void onDestroy()
	{
		super.onDestroy();
	}

	@Override
	public void onResume()
	{
		super.onResume();
	}

}
