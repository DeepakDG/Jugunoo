package com.hirecraft.jugunoo.passenger.fragments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.hirecraft.jugunoo.passenger.R;
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

import de.keyboardsurfer.android.widget.crouton.Crouton;

public class AddFleetUserActivity_new extends Activity implements OnUserName,
		OnClickListener
{
	private static final String TAG = AddFleetUserActivity_new.class
			.getSimpleName();
	private TransparentProgressDialog pd;
	private Handler pdHandler;
	private Runnable pdRunnable;
	ListView listView, nameList;
	SharedPreferencesManager mgr;

	RelativeLayout back;
	EditText etUsername;
	TextView title, bar, hrText;
	Button btnSave;
	ArrayList<HashMap<String, String>> names;
	String usernameStr = "", currentStatus = "", userRid = "", rid = "",
			managingUserId = "", groupId = "", mobileNo = "";
	ProgressBar mobProgress;
	UserName user;
	String name;
	private static String user_name;

	int id;
	RelativeLayout groupListL;
	boolean isGetName = false, isUpdate;

	private String selectedGroupId;
	private static int REQUEST_CODE_ADD_USER = 1;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		SetActionBar();
		setContentView(R.layout.fragment_add_users_new);
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

		RelativeLayout l = new RelativeLayout(AddFleetUserActivity_new.this);
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.action_bar, l);
		back = (RelativeLayout) view.findViewById(R.id.actionBackL);
		title = (TextView) view.findViewById(R.id.screenTitles);
		bar = (TextView) view.findViewById(R.id.bar2);
		btnSave = (Button) view.findViewById(R.id.actionSave);
		// back.setOnClickListener(this);
		btnSave.setOnClickListener(this);
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
		mgr = new SharedPreferencesManager(AddFleetUserActivity_new.this);
		pd = new TransparentProgressDialog(AddFleetUserActivity_new.this,
				R.drawable.loading_image);
		pdHandler = new Handler();

		etUsername = (EditText) findViewById(R.id.etAdUserName);
		etUsername.addTextChangedListener(userIDWatcher);

		nameList = (ListView) findViewById(R.id.nameList);

		Bundle bundle = getIntent().getExtras();
		if (bundle != null)
		{
			selectedGroupId = bundle.getString("groupId");
		}
		groupListL = (RelativeLayout) findViewById(R.id.listLayout);
		mobProgress = (ProgressBar) findViewById(R.id.usernameProgress);

		listView = (ListView) findViewById(R.id.profDetailNList);
		hrText = (TextView) findViewById(R.id.txtHeader);
		mobProgress.getIndeterminateDrawable().setColorFilter(
				Color.rgb(155, 219, 70),
				android.graphics.PorterDuff.Mode.MULTIPLY);
	}

	@Override
	public void onBackPressed()
	{
		super.onBackPressed();
		finish();
		Function.hideSoftKeyBoard(AddFleetUserActivity_new.this, etUsername);
	}

	@Override
	public void onResume()
	{
		super.onResume();
	}

	private TextWatcher userIDWatcher = new TextWatcher()
	{
		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count)
		{
			usernameStr = etUsername.getText().toString();
			String userId = mgr.GetValueFromSharedPrefs("UserID");
			if (count % 2 == 1)
			{
				String url = Global.JUGUNOO_WS
						+ "Passenger/GetNameByMobile?Mobile=" + usernameStr
						+ "&UserId=" + userId;
				makeReqToGetUserName(url);
			}
			// if (usernameStr.length() < 1)
			// {
			// Function.showToast(AddFleetUserActivity_new.this,
			// "Enter a valid username or mobile number.");
			// }
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after)
		{

		}

		@Override
		public void afterTextChanged(Editable s)
		{
			if (usernameStr.length() == 0)
			{
				nameList.setVisibility(View.GONE);
			}
		}
	};

	private void makeReqToGetUserName(String url)
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

	@Override
	protected void onPause()
	{
		super.onPause();
		closeAnimation();
		Crouton.cancelAllCroutons();
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
				AddFleetUserActivity_new.this.finish();
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
							AddFleetUserActivity_new.this, list,
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
							etUsername.setText(grName);
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
		if (saveUserValidation() != false)
		{
			showLoadingDilog();
			Map<String, String> params = new HashMap<String, String>();
			params.put("RID", "0");
			params.put("UserId", managingUserId);
			params.put("Status", "A");
			params.put("GroupId", selectedGroupId);
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
		user_name = etUsername.getText().toString();

		// int action = PassengerProfileActivity.IS_GROUP_ACTION;
		if (!(JugunooUtil.isConnectedToInternet(getApplicationContext())))
		{
			Function.showToast(AddFleetUserActivity_new.this,
					"No internet connection");
		}
		else if (user_name.equalsIgnoreCase(""))
		{
			Function.showToast(AddFleetUserActivity_new.this,
					"Enter a mobile number for user.");
		}
		else if (userRid == "0" && managingUserId.equalsIgnoreCase(""))
		{
			Function.showToast(AddFleetUserActivity_new.this,
					"Entered mobile number doesnot exists.");
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

			case R.id.actionBackL:
				// startActivity(new Intent(AddFleetUsers.this,
				// PassengerProfile.class));
				AddFleetUserActivity_new.this.finish();
				onBackPressed();
				break;

		}
	}

	private Handler handler = new Handler(new Handler.Callback()
	{
		@Override
		public boolean handleMessage(Message msg)
		{
			switch (msg.arg1)
			{
				case Constant.MessageState.USERNAMES_SUCCESS:
					mobProgress.setVisibility(View.GONE);
					parserUserList((JSONObject) msg.obj);
					break;

				case Constant.MessageState.USERNAMES_FAIL:
					mobProgress.setVisibility(View.GONE);
					nameList.setVisibility(View.GONE);
					break;

				case Constant.MessageState.ADD_FLEET_USER_SUCCESS:
					cancelLoadingDialog();
					JSONObject result = (JSONObject) msg.obj;
					parseAddUser(result);
					break;

				case Constant.MessageState.ADD_FLEET_USER_FAIL:
					cancelLoadingDialog();
					VolleyErrorHelper.getMessage(msg.obj,
							AddFleetUserActivity_new.this, true);
					Log.e(TAG, "Unable to add");
					break;
			}
			return false;
		}
	});

	private void parserUserList(JSONObject result)
	{
		if (result != null)
		{
			names = new ArrayList<HashMap<String, String>>();
			try
			{
				if (result.getString("Result").equalsIgnoreCase("Pass"))
				{
					JSONArray jsonArray = result.getJSONArray("UserArray");
					Log.d("array", "array " + jsonArray.toString());

					int len = jsonArray.length();
					if (len != 0)
					{
						for (int f = 0; f < len; f++)
						{
							JSONObject obj = jsonArray.getJSONObject(f);
							String grName = obj.getString("FirstName");
							String rid = obj.getString("RID");

							HashMap<String, String> fetchData = new HashMap<String, String>();
							fetchData.put("FirstName", grName);
							fetchData.put("RID", rid);
							names.add(fetchData);
							nameList.setVisibility(View.VISIBLE);

							ListAdapter adapter = new SimpleAdapter(
									AddFleetUserActivity_new.this, names,
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

									etUsername.setText(grName);
									managingUserId = rid;
									Log.i("tag", "grName: " + grName + ", rid"
											+ rid);
									nameList.setVisibility(View.GONE);
									InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
									imm.hideSoftInputFromWindow(
											etUsername.getWindowToken(), 0);
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

	private void parseAddUser(JSONObject object)
	{
		try
		{
			String message = object.getString("Message");

			if (object.getString("Result").equalsIgnoreCase("Pass"))
			{
				Function.showToast(AddFleetUserActivity_new.this, message);
				// NavToParent();

				new Handler().postDelayed(new Runnable()
				{
					@Override
					public void run()
					{
						Intent intent = new Intent();
						intent.putExtra("refresh", "Yes");
						setResult(REQUEST_CODE_ADD_USER, intent);

						AddFleetUserActivity_new.this.finish();
					}
				}, 3000);
			}
			else
			{
				Function.showToast(AddFleetUserActivity_new.this, message);
			}

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void NavToParent()
	{
		new Handler().postDelayed(new Runnable()
		{
			@Override
			public void run()
			{
				AddFleetUserActivity_new.this.finish();

				Intent intent = new Intent();
				intent.putExtra("refresh", "Yes");
				setResult(REQUEST_CODE_ADD_USER, intent);
			}
		}, 3000);

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