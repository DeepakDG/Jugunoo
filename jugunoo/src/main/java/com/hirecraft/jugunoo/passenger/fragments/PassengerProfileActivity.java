//package com.hirecraft.jugunoo.passenger.fragments;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.regex.Matcher;
//
//import org.json.JSONArray;
//import org.json.JSONObject;
//
//import android.app.ActionBar;
//import android.app.Activity;
//import android.app.Dialog;
//import android.content.Context;
//import android.content.Intent;
//import android.graphics.Color;
//import android.graphics.Typeface;
//import android.graphics.drawable.BitmapDrawable;
//import android.net.Uri;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.Message;
//import android.support.v4.app.FragmentActivity;
//import android.text.Editable;
//import android.text.TextWatcher;
//import android.util.Log;
//import android.view.Gravity;
//import android.view.KeyEvent;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.view.ViewGroup;
//import android.view.ViewGroup.LayoutParams;
//import android.view.Window;
//import android.view.WindowManager;
//import android.view.inputmethod.EditorInfo;
//import android.view.inputmethod.InputMethodManager;
//import android.widget.AdapterView;
//import android.widget.AdapterView.OnItemClickListener;
//import android.widget.Button;
//import android.widget.CheckBox;
//import android.widget.EditText;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.ListAdapter;
//import android.widget.ListView;
//import android.widget.PopupWindow;
//import android.widget.ProgressBar;
//import android.widget.RadioButton;
//import android.widget.RadioGroup;
//import android.widget.RadioGroup.OnCheckedChangeListener;
//import android.widget.RelativeLayout;
//import android.widget.SimpleAdapter;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.hirecraft.jugunoo.passenger.LandingPage;
//import com.hirecraft.jugunoo.passenger.Login;
//import com.hirecraft.jugunoo.passenger.R;
//import com.hirecraft.jugunoo.passenger.common.Constant;
//import com.hirecraft.jugunoo.passenger.common.Global;
//import com.hirecraft.jugunoo.passenger.common.TransparentProgressDialog;
//import com.hirecraft.jugunoo.passenger.common.Validation;
//import com.hirecraft.jugunoo.passenger.networkhandler.NetworkHandler;
//import com.hirecraft.jugunoo.passenger.utility.JugunooUtil;
//import com.hirecraft.jugunoo.passenger.utility.SharedPreferencesManager;
//
//public class PassengerProfileActivity extends FragmentActivity implements
//		OnClickListener
//{
//
//	private static final String TAG = PassengerProfileActivity.class
//			.getSimpleName();
//	public static String viewState = "";
//	public static int IS_GROUP_ACTION = 0;
//	public static int CURRENT_TAB = 0;
//	public static int UPDATE_USER = 0;
//	public static boolean isGroupSuccess = false;
//	public static boolean isOpSuccess = false;
//
//	ActionBar actionbar;
//	LinearLayout tabLayout;
//	private static int addUser = 0;
//	Activity activity;
//	private Button fleet, group, user;
//	ProgressBar fleetProgressBar;
//	TextView fleetState;
//	private ListView fleetList;
//	private ArrayList<HashMap<String, String>> fleets = null;
//	private String grName, mgrName, count, status, rid, mobile, grId,
//			grMgrName, grMgrID, grStatus, grMobileNo, grCount, userName,
//			userId, grOwn, usertype;
//
//	private static SharedPreferencesManager mgr;
//	private TransparentProgressDialog pd;
//	private Handler pdHandler;
//	private Runnable pdRunnable;
//	private Typeface light, bold;
//	private EditText oldPwd, newPwd, cnfPwd;
//	private CheckBox enable;
//	static ImageView add, changePwds, back, refreshBar;
//
//	RelativeLayout titleL;
//	private RadioGroup userStatus;
//	private static String currentStatus = "", updateStatus = "";
//
//	@Override
//	protected void onCreate(Bundle savedInstanceState)
//	{
//		super.onCreate(savedInstanceState);
//		SetActionBar();
//		setContentView(R.layout.activity_profile);
//		Init();
//		openAnimation();
//	}
//
//	private void SetActionBar()
//	{
//		ActionBar actionBar = getActionBar();
//		ActionBar.LayoutParams layoutParams = new ActionBar.LayoutParams(
//				ActionBar.LayoutParams.MATCH_PARENT,
//				ActionBar.LayoutParams.MATCH_PARENT);
//		layoutParams.gravity = Gravity.CENTER_HORIZONTAL
//				| Gravity.CENTER_VERTICAL;
//
//		RelativeLayout l = new RelativeLayout(PassengerProfileActivity.this);
//		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//		View bar = inflater.inflate(R.layout.custom_action_bar, l);
//		titleL = (RelativeLayout) bar.findViewById(R.id.nav_iconL);
//		TextView title = (TextView) bar.findViewById(R.id.screenTitle);
//		changePwds = (ImageView) bar.findViewById(R.id.barChangePwd);
//		refreshBar = (ImageView) bar.findViewById(R.id.refreshBar);
//		// back = (ImageView) bar.findViewById(R.id.nav_icon);
//		// back.setOnClickListener(this);
//		titleL.setOnClickListener(this);
//		changePwds.setOnClickListener(this);
//		refreshBar.setOnClickListener(this);
//
//		title.setText("Profile");
//		actionBar.setCustomView(bar, layoutParams);
//		actionBar.setDisplayShowTitleEnabled(false);
//		actionBar.setDisplayShowCustomEnabled(true);
//		actionBar.setDisplayUseLogoEnabled(false);
//		actionBar.setDisplayShowHomeEnabled(false);
//	}
//
//	public void onOpenDialog(View view)
//	{
//		IS_GROUP_ACTION = 0;
//		if (addUser == 1)
//		{
//			startActivity(new Intent(PassengerProfileActivity.this,
//					AddFleetGroupActivity.class));
//		}
//		else if (addUser == 2)
//		{
//			startActivity(new Intent(PassengerProfileActivity.this,
//					AddFleetUserActivity.class));
//		}
//		PassengerProfileActivity.this.finish();
//	}
//
//	private void Init()
//	{
//		mgr = new SharedPreferencesManager(PassengerProfileActivity.this);
//		pd = new TransparentProgressDialog(PassengerProfileActivity.this,
//				R.drawable.loading_image);
//		pdHandler = new Handler();
//		light = Typeface.createFromAsset(getAssets(),
//				"fonts/opensans-regular-webfont.ttf");
//		bold = Typeface.createFromAsset(getAssets(),
//				"fonts/opensans-bold-webfont.ttf");
//		tabLayout = (LinearLayout) findViewById(R.id.tabLayout);
//		fleets = new ArrayList<HashMap<String, String>>();
//		enable = (CheckBox) findViewById(R.id.chkEnable);
//		add = (ImageView) findViewById(R.id.addUser);
//
//		fleet = (Button) findViewById(R.id.fleetOptBtn);
//		group = (Button) findViewById(R.id.fleetGroupBtn);
//		user = (Button) findViewById(R.id.fleetUserBtn);
//		fleetProgressBar = (ProgressBar) findViewById(R.id.progressfleet);
//		fleetState = (TextView) findViewById(R.id.fleetStatus);
//		fleetList = (ListView) findViewById(R.id.fleetList);
//		userStatus = (RadioGroup) findViewById(R.id.psStatusGroup);
//		fleetProgressBar.getIndeterminateDrawable().setColorFilter(
//				Color.rgb(155, 219, 70),
//				android.graphics.PorterDuff.Mode.MULTIPLY);
//
//		SetUserState();
//		SetTabContent(CURRENT_TAB);
//		SetEvent();
//	}
//
//	private void SetEvent()
//	{
//		fleet.setOnClickListener(this);
//		group.setOnClickListener(this);
//		user.setOnClickListener(this);
//		enable.setChecked(true);
//		enable.setOnClickListener(this);
//		// add.setVisibility(View.GONE);
//
//		OnClickListener listener = new OnClickListener()
//		{
//			@Override
//			public void onClick(View v)
//			{
//				RadioButton rb = (RadioButton) v;
//				Log.i("Tag", "my user status2 :" + rb.getText());
//				viewState = rb.getText().toString();
//				GetState(rb.getText().toString(), CURRENT_TAB);
//			}
//		};
//
//		RadioButton rb1 = (RadioButton) findViewById(R.id.drActive);
//		rb1.setOnClickListener(listener);
//
//		RadioButton rb2 = (RadioButton) findViewById(R.id.drInactive);
//		rb2.setOnClickListener(listener);
//
//		RadioButton rb3 = (RadioButton) findViewById(R.id.all);
//		rb3.setOnClickListener(listener);
//
//	}
//
//	private void SetUserState()
//	{
//		if (UPDATE_USER == 1)
//		{
//			SetTabContent(CURRENT_TAB);
//		}
//		if (userStatus.getCheckedRadioButtonId() != -1)
//		{
//			int id = userStatus.getCheckedRadioButtonId();
//			View radioButton = userStatus.findViewById(id);
//			int radioId = userStatus.indexOfChild(radioButton);
//			RadioButton btn = (RadioButton) userStatus.getChildAt(radioId);
//			String selection = (String) btn.getText();
//			GetState(selection, CURRENT_TAB);
//		}
//		EnableAddOption();
//	}
//
//	private void SetTabContent(int pos)
//	{
//		switch (pos)
//		{
//			case 0:
//				fleet.setSelected(true);
//				group.setSelected(false);
//				user.setSelected(false);
//				fleet.setTextColor(Color.WHITE);
//				group.setTextColor(Color.rgb(97, 144, 32));
//				user.setTextColor(Color.rgb(97, 144, 32));
//				SetCustomSelected(fleet, user, group);
//				GetFleetOperators();
//				break;
//
//			case 1:
//				fleet.setSelected(false);
//				group.setSelected(true);
//				user.setSelected(false);
//				group.setTextColor(Color.WHITE);
//				fleet.setTextColor(Color.rgb(97, 144, 32));
//				user.setTextColor(Color.rgb(97, 144, 32));
//				SetCustomSelected(group, user, fleet);
//				GetFleetGroups();
//				break;
//
//			case 2:
//				fleet.setSelected(false);
//				group.setSelected(false);
//				user.setSelected(true);
//				user.setTextColor(Color.WHITE);
//				fleet.setTextColor(Color.rgb(97, 144, 32));
//				group.setTextColor(Color.rgb(97, 144, 32));
//				SetCustomSelected(user, group, fleet);
//				GetFleetUsers();
//				break;
//		}
//
//	}
//
//	private void GetState(String state, int pos)
//	{
//
//		if (state.equalsIgnoreCase("All"))
//			currentStatus = "B";
//		else if (state.equalsIgnoreCase("Active"))
//			currentStatus = "A";
//		else if (state.equalsIgnoreCase("Inactive"))
//			currentStatus = "N";
//		else
//			currentStatus = "";
//
//		switch (pos)
//		{
//			case 0:
//				GetFleetOperators();
//				break;
//			case 1:
//				GetFleetGroups();
//				break;
//			case 2:
//				GetFleetUsers();
//				break;
//
//			default:
//				break;
//		}
//	}
//
//	@Override
//	protected void onPause()
//	{
//		super.onPause();
//		closeAnimation();
//	}
//
//	private void openAnimation()
//	{
//		overridePendingTransition(R.anim.activity_open_translate,
//				R.anim.activity_close_scale);
//	}
//
//	private void closeAnimation()
//	{
//		/**
//		 * Closing transition animations.
//		 */
//		overridePendingTransition(R.anim.activity_open_scale,
//				R.anim.activity_close_translate);
//	}
//
//	@Override
//	public boolean onKeyDown(int keyCode, KeyEvent event)
//	{
//
//		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0)
//		{
//
//			if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0)
//			{
//				startActivity(new Intent(PassengerProfileActivity.this,
//						LandingPage.class));
//				PassengerProfileActivity.this.finish();
//				return true;
//			}
//		}
//		return super.onKeyDown(keyCode, event);
//	}
//
//	private void enableView()
//	{
//
//		if (enable.isChecked())
//			tabLayout.setVisibility(View.VISIBLE);
//		else
//			tabLayout.setVisibility(View.INVISIBLE);
//	}
//
//	@Override
//	public void onClick(View v)
//	{
//
//		switch (v.getId())
//		{
//
//			case R.id.chkEnable:
//				enableView();
//				break;
//
//			case R.id.barChangePwd:
//				changPwdPopup();
//				break;
//
//			case R.id.refreshBar:
//				SetTabContent(CURRENT_TAB);
//				break;
//
//			case R.id.nav_iconL:
//				startActivity(new Intent(PassengerProfileActivity.this,
//						LandingPage.class));
//				PassengerProfileActivity.this.finish();
//				break;
//
//			case R.id.fleetOptBtn:
//				CURRENT_TAB = 0;
//
//				if (fleet.isSelected() == false)
//				{
//					SetCustomSelected(fleet, group, user);
//					add.setVisibility(View.GONE);
//					GetFleetOperators();
//					Log.i("tag", "fleet selected" + fleet.isSelected());
//				}
//				break;
//
//			case R.id.fleetGroupBtn:
//				CURRENT_TAB = 1;
//				if (group.isSelected() == false)
//				{
//					SetCustomSelected(group, user, fleet);
//					EnableAddOption();
//					addUser = 1;
//					GetFleetGroups();
//				}
//
//				break;
//
//			case R.id.fleetUserBtn:
//				CURRENT_TAB = 2;
//				if (user.isSelected() == false)
//				{
//					SetCustomSelected(user, group, fleet);
//					EnableAddOption();
//					addUser = 2;
//					GetFleetUsers();
//				}
//				break;
//		}
//	}
//
//	@Override
//	protected void onResume()
//	{
//		EnableAddOption();
//		super.onResume();
//	}
//
//	private void EnableAddOption()
//	{
//
//		String usertype = mgr.GetValueFromSharedPrefs("FleetUserType");
//		if (usertype.equalsIgnoreCase("Manager"))
//		{
//
//			if (group.isSelected() == true)
//			{
//				add.setVisibility(View.INVISIBLE);
//			}
//			else if (user.isSelected() == true && isGroupSuccess == true)
//			{
//				add.setVisibility(View.VISIBLE);
//			}
//			else
//			{
//				add.setVisibility(View.INVISIBLE);
//			}
//
//		}
//		else if (usertype.equalsIgnoreCase("Admin"))
//		{
//
//			if (group.isSelected() == true && isOpSuccess == true)
//			{
//				add.setVisibility(View.VISIBLE);
//			}
//			else if (user.isSelected() == true && isGroupSuccess == true)
//			{
//				add.setVisibility(View.VISIBLE);
//			}
//			else
//			{
//				add.setVisibility(View.INVISIBLE);
//			}
//
//		}
//		else
//		{
//			add.setVisibility(View.INVISIBLE);
//		}
//	}
//
//	private void SetCustomSelected(Button btn1, Button btn2, Button btn3)
//	{
//
//		btn1.setSelected(true);
//		btn2.setSelected(false);
//		btn3.setSelected(false);
//		btn1.setTextColor(Color.WHITE);
//		btn2.setTextColor(Color.rgb(97, 144, 32));
//		btn3.setTextColor(Color.rgb(97, 144, 32));
//		btn1.setBackgroundResource(R.drawable.jugunoo_tab_selector_press);
//		btn2.setBackgroundResource(R.drawable.jugunoo_tab_selector_normal);
//		btn3.setBackgroundResource(R.drawable.jugunoo_tab_selector_normal);
//
//	}
//
//	private void JugunooChangePwdDialog()
//	{
//		final Dialog dialog = new Dialog(PassengerProfileActivity.this);
//		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//		Window window = dialog.getWindow();
//
//		InputMethodManager input = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//		input.hideSoftInputFromWindow(window.getDecorView().getWindowToken(), 0);
//
//		window.setBackgroundDrawableResource(android.R.color.transparent);
//		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//		View dislogView = inflater.inflate(R.layout.jugunoo_changepwd_dialog,
//				new LinearLayout(PassengerProfileActivity.this));
//		dialog.setContentView(dislogView);
//		window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
//				ViewGroup.LayoutParams.MATCH_PARENT);
//
//		oldPwd = (EditText) dialog.findViewById(R.id.oldPwd);
//		newPwd = (EditText) dialog.findViewById(R.id.newPwd);
//		cnfPwd = (EditText) dialog.findViewById(R.id.cnfPwd);
//		Button leftBtn = (Button) dialog.findViewById(R.id.cancelChBtn);
//		Button rightBtn = (Button) dialog.findViewById(R.id.submitChBtn);
//		oldPwd.addTextChangedListener(oldPwdWatcher);
//		newPwd.addTextChangedListener(passwordWatcher);
//		cnfPwd.addTextChangedListener(cpasswordWatcher);
//
//		oldPwd.setOnFocusChangeListener(new View.OnFocusChangeListener()
//		{
//			@Override
//			public void onFocusChange(View v, boolean hasFocus)
//			{
//				if (hasFocus)
//				{
//					dialog.getWindow()
//							.setSoftInputMode(
//									WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
//				}
//			}
//		});
//
//		cnfPwd.setOnEditorActionListener(new EditText.OnEditorActionListener()
//		{
//
//			@Override
//			public boolean onEditorAction(TextView v, int actionId,
//					KeyEvent event)
//			{
//				if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER))
//						|| (actionId == EditorInfo.IME_ACTION_NEXT))
//				{
//					String cnfPwdStr = cnfPwd.getText().toString();
//					String passwordStr = newPwd.getText().toString();
//					int i = lastIndexOfUCL(cnfPwdStr);
//					if (!cnfPwdStr.equalsIgnoreCase(passwordStr))
//					{
//						cnfPwd.setError("Password and Confirm Password Do Not Match.");
//					}
//					else if (i == -1)
//					{
//						cnfPwd.setError("Required.");
//					}
//				}
//				return false;
//			}
//		});
//
//		oldPwd.setTypeface(light);
//		newPwd.setTypeface(light);
//		cnfPwd.setTypeface(light);
//		leftBtn.setTypeface(bold);
//		rightBtn.setTypeface(bold);
//		leftBtn.setOnClickListener(new OnClickListener()
//		{
//
//			@Override
//			public void onClick(View v)
//			{
//				dialog.dismiss();
//			}
//		});
//		rightBtn.setOnClickListener(new OnClickListener()
//		{
//
//			@Override
//			public void onClick(View v)
//			{
//
//				String oldPwdStr = oldPwd.getText().toString();
//				String newPwdStr = newPwd.getText().toString();
//				String confPwdStr = cnfPwd.getText().toString();
//
//				if (Validation.hasText(oldPwd) && Validation.hasText(newPwd)
//						&& Validation.hasText(cnfPwd))
//				{
//
//					if (!oldPwdStr.equalsIgnoreCase("")
//							&& !newPwdStr.equalsIgnoreCase(""))
//					{
//
//						if (oldPwdStr.equals(newPwdStr))
//						{
//							showDialog(getResources().getString(
//									R.string.newOldSame));
//
//						}
//						else
//						{
//							if (confPwdStr.equals(newPwdStr))
//							{
//								String userId = mgr
//										.GetValueFromSharedPrefs("UserID");
//								ChangePassword(userId, oldPwdStr, newPwdStr);
//
//								dialog.dismiss();
//							}
//							else
//							{
//								showDialog(getResources().getString(
//										R.string.pwdNotMatch));
//							}
//						}
//					}
//					else
//					{
//						showDialog(getResources().getString(
//								R.string.enterRequired));
//						Log.i(Global.APPTAG, "Invalid input");
//					}
//
//				}
//				else
//				{
//					showDialog(getResources().getString(R.string.enterRequired));
//					Log.i(Global.APPTAG, "Invalid input");
//				}
//
//			}
//		});
//
//		dialog.show();
//	}
//
//	public int lastIndexOfUCL(String str)
//	{
//		for (int i = str.length() - 1; i >= 0; i--)
//		{
//			if (Character.isUpperCase(str.charAt(i)))
//			{
//				return i;
//			}
//		}
//		return -1;
//	}
//
//	public void showDialog(String message)
//	{
//
//		Toast.makeText(PassengerProfileActivity.this, message,
//				Toast.LENGTH_LONG).show();
//	}
//
//	public static boolean validatePassword(String password)
//	{
//
//		Matcher mtch = JugunooUtil.pswNamePtrn.matcher(password);
//		if (mtch.matches())
//		{
//			return true;
//		}
//		return false;
//	}
//
//	private TextWatcher oldPwdWatcher = new TextWatcher()
//	{
//
//		@Override
//		public void onTextChanged(CharSequence s, int start, int before,
//				int count)
//		{
//
//		}
//
//		@Override
//		public void beforeTextChanged(CharSequence s, int start, int count,
//				int after)
//		{
//		}
//
//		@Override
//		public void afterTextChanged(Editable s)
//		{
//			Validation.hasText(oldPwd);
//		}
//	};
//
//	private TextWatcher passwordWatcher = new TextWatcher()
//	{
//
//		@Override
//		public void onTextChanged(CharSequence s, int start, int before,
//				int count)
//		{
//
//		}
//
//		@Override
//		public void beforeTextChanged(CharSequence s, int start, int count,
//				int after)
//		{
//		}
//
//		@Override
//		public void afterTextChanged(Editable s)
//		{
//			Validation.hasText(newPwd);
//		}
//	};
//
//	private TextWatcher cpasswordWatcher = new TextWatcher()
//	{
//
//		@Override
//		public void onTextChanged(CharSequence s, int start, int before,
//				int count)
//		{
//
//		}
//
//		@Override
//		public void beforeTextChanged(CharSequence s, int start, int count,
//				int after)
//		{
//		}
//
//		@Override
//		public void afterTextChanged(Editable s)
//		{
//			Validation.hasText(cnfPwd);
//		}
//	};
//
//	private void JugunooInteractiveDialog(String title, String message)
//	{
//		final Dialog dialog = new Dialog(PassengerProfileActivity.this);
//		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//		Window window = dialog.getWindow();
//		window.setBackgroundDrawableResource(android.R.color.transparent);
//		LinearLayout.LayoutParams dialogParams = new LinearLayout.LayoutParams(
//				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
//		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//		View dislogView = inflater.inflate(R.layout.jugunoo_interactive_dialog,
//				new LinearLayout(PassengerProfileActivity.this));
//		dialog.setContentView(dislogView, dialogParams);
//
//		TextView textView = (TextView) dialog.findViewById(R.id.messageText);
//		Button centerBtn = (Button) dialog.findViewById(R.id.centerBtn);
//		Button leftBtn = (Button) dialog.findViewById(R.id.leftBtn);
//		Button rightBtn = (Button) dialog.findViewById(R.id.rightBtn);
//
//		textView.setTypeface(light);
//		centerBtn.setTypeface(bold);
//		leftBtn.setTypeface(bold);
//		rightBtn.setTypeface(bold);
//
//		if (title.equalsIgnoreCase("PASSWORD"))
//		{
//
//			leftBtn.setVisibility(View.GONE);
//			rightBtn.setVisibility(View.GONE);
//			String ok = getResources().getString(R.string.ok);
//			textView.setText(message);
//			centerBtn.setText(ok);
//			centerBtn.setOnClickListener(new OnClickListener()
//			{
//
//				@Override
//				public void onClick(View v)
//				{
//					dialog.dismiss();
//					startActivity(new Intent(PassengerProfileActivity.this,
//							Login.class));
//					PassengerProfileActivity.this.finish();
//				}
//			});
//		}
//		else if (title.equalsIgnoreCase("NETWORK"))
//		{
//
//			leftBtn.setVisibility(View.GONE);
//			rightBtn.setVisibility(View.GONE);
//			String ok = getResources().getString(R.string.ok);
//			textView.setText(message);
//			centerBtn.setText(ok);
//			centerBtn.setOnClickListener(new OnClickListener()
//			{
//
//				@Override
//				public void onClick(View v)
//				{
//					dialog.dismiss();
//				}
//			});
//		}
//		else if (title.equalsIgnoreCase("CANCEL"))
//		{
//
//			leftBtn.setVisibility(View.GONE);
//			rightBtn.setVisibility(View.GONE);
//			String ok = getResources().getString(R.string.ok);
//			textView.setText(message);
//			centerBtn.setText(ok);
//			centerBtn.setOnClickListener(new OnClickListener()
//			{
//
//				@Override
//				public void onClick(View v)
//				{
//					dialog.dismiss();
//				}
//			});
//		}
//		dialog.show();
//	}
//
//	@Override
//	public void onStop()
//	{
//		super.onStop();
//		Global.getInstance().getRequestQueue().cancelAll(TAG);
//	}
//
//	private void changPwdPopup()
//	{
//
//		LinearLayout viewGroup = (LinearLayout) findViewById(R.id.menu);
//		LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//		View layout = layoutInflater.inflate(R.layout.custom_menu_item_row,
//				viewGroup);
//
//		/**
//		 * Creating the PopupWindow
//		 */
//		final PopupWindow popup = new PopupWindow(layout,
//				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
//		popup.setContentView(layout);
//		popup.setFocusable(true);
//
//		/**
//		 * Clear the default translucent background
//		 */
//		popup.setBackgroundDrawable(new BitmapDrawable());
//		popup.showAsDropDown(changePwds);
//
//		/**
//		 * Getting a reference to Close button, and close the popup when
//		 * clicked.
//		 */
//
//		Button btn = (Button) layout.findViewById(R.id.menu_item);
//		btn.setText("Change Password");
//		btn.setOnClickListener(new OnClickListener()
//		{
//
//			@Override
//			public void onClick(View v)
//			{
//				popup.dismiss();
//				JugunooChangePwdDialog();
//			}
//		});
//
//	}
//
//	private void ChangePassword(String userId, String oldPwd, String newPwd)
//	{
//
//		pdRunnable = new Runnable()
//		{
//			@Override
//			public void run()
//			{
//				if (pd != null)
//				{
//					if (pd.isShowing())
//					{
//						pd.dismiss();
//					}
//				}
//			}
//		};
//		pd.show();
//
//		Map<String, String> params = new HashMap<String, String>();
//		params.put("UserId", userId);
//		params.put("OldPassword", oldPwd);
//		params.put("NewPassword", newPwd);
//
//		NetworkHandler.ChangePwd(TAG, handler, params);
//	}
//
//	private void ParseChangePwd(JSONObject object)
//	{
//
//		try
//		{
//			String resultStr = object.getString("Result");
//			if (!resultStr.equalsIgnoreCase("Fail"))
//			{
//
//				JugunooInteractiveDialog("PASSWORD",
//						getResources().getString(R.string.loginAgain));
//
//			}
//			else
//			{
//				showDialog(getResources().getString(R.string.OldPwdNotMatch));
//			}
//
//		}
//		catch (Exception e)
//		{
//			e.printStackTrace();
//		}
//	}
//
//	private void GetFleetOperators()
//	{
//		if (fleets.size() != 0)
//		{
//			fleets.clear();
//		}
//		fleetState.setVisibility(View.VISIBLE);
//		fleetState.setText("");
//		fleetProgressBar.setVisibility(View.VISIBLE);
//		fleetList.setVisibility(View.GONE);
//		String userID = mgr.GetValueFromSharedPrefs("UserID");
//		String usertype = mgr.GetValueFromSharedPrefs("FleetUserType");
//
//		// Jugunoo getting the fleet array by url
//		NetworkHandler.GetUserFleet(TAG, handler, userID, currentStatus,
//				usertype);
//	}
//
//	private Handler handler = new Handler(new Handler.Callback()
//	{
//
//		@Override
//		public boolean handleMessage(Message msg)
//		{
//
//			JSONObject fleets = null;
//
//			switch (msg.arg1)
//			{
//
//				case Constant.MessageState.CHANGE_PWD_SUCCESS:
//					pdHandler.removeCallbacks(pdRunnable);
//					if (pd.isShowing())
//					{
//						pd.dismiss();
//					}
//					JSONObject fleet = (JSONObject) msg.obj;
//					ParseChangePwd(fleet);
//					break;
//
//				case Constant.MessageState.CHANGE_PWD_FAIL:
//					pdHandler.removeCallbacks(pdRunnable);
//					if (pd.isShowing())
//					{
//						pd.dismiss();
//					}
//					showDialog("Unable to change the current password.");
//					Log.e(TAG, "Unable to change the current password.");
//					break;
//
//				case Constant.MessageState.FLEET_OPERATOR_SUCCESS:
//					fleetProgressBar.setVisibility(View.GONE);
//					fleetState.setVisibility(View.GONE);
//					fleets = (JSONObject) msg.obj;
//					PrepareList(fleets);
//					break;
//
//				case Constant.MessageState.FLEET_OPERATOR_FAIL:
//					fleetProgressBar.setVisibility(View.GONE);
//					fleetState.setVisibility(View.VISIBLE);
//					isOpSuccess = false;
//					fleetState.setText("Null Fleets");
//					Log.e(TAG, "Null Fleets");
//					break;
//
//				case Constant.MessageState.UPDATE_FLEET_SUCCESS:
//					pdHandler.removeCallbacks(pdRunnable);
//					if (pd.isShowing())
//					{
//						pd.dismiss();
//					}
//					fleet = (JSONObject) msg.obj;
//					SetFleetUpdateStatus(fleet);
//					break;
//
//				case Constant.MessageState.UPDATE_FLEET_FAIL:
//					pdHandler.removeCallbacks(pdRunnable);
//					if (pd.isShowing())
//					{
//						pd.dismiss();
//					}
//					Log.e(TAG, "Updated failed!");
//					break;
//
//				case Constant.MessageState.FLEET_GROUP_SUCCESS:
//					fleetProgressBar.setVisibility(View.GONE);
//					fleetState.setVisibility(View.GONE);
//					fleets = (JSONObject) msg.obj;
//					PrepareGroupList(fleets);
//					break;
//
//				case Constant.MessageState.FLEET_GROUP_FAIL:
//					fleetProgressBar.setVisibility(View.GONE);
//					isGroupSuccess = false;
//					fleetState.setVisibility(View.VISIBLE);
//					fleetState.setText("Null Fleets");
//					Log.e(TAG, "Null Groups");
//					break;
//
//				case Constant.MessageState.FLEET_USERS_SUCCESS:
//					fleetProgressBar.setVisibility(View.GONE);
//					fleets = (JSONObject) msg.obj;
//					PrepareUserList(fleets);
//					break;
//
//				case Constant.MessageState.FLEET_USERS_FAIL:
//					fleetProgressBar.setVisibility(View.GONE);
//					fleetState.setVisibility(View.VISIBLE);
//					fleetState.setText("Null Users");
//					Log.e(TAG, "Null Fleets");
//					break;
//			}
//			return false;
//		}
//	});
//
//	private void PrepareList(JSONObject object)
//	{
//		try
//		{
//			String resultStr = object.getString("Result");
//			String message = object.getString("Message");
//			if (!resultStr.equalsIgnoreCase("Fail"))
//			{
//				fleetList.setVisibility(View.VISIBLE);
//				JSONArray array = object.getJSONArray("FleetArray");
//				fleets = new ArrayList<HashMap<String, String>>();
//				int len = array.length();
//				if (len != 0)
//				{
//					isOpSuccess = true;
//					for (int f = 0; f < len; f++)
//					{
//						JSONObject obj = array.getJSONObject(f);
//						grName = obj.getString("GroupName");
//						mgrName = obj.getString("CreaterName");
//						count = obj.getString("Count");
//						mobile = obj.getString("Mobile");
//						rid = obj.getString("FleetId");
//						status = obj.getString("Status");
//						if (status.equalsIgnoreCase("A"))
//							status = "Active";
//						else if (status.equalsIgnoreCase("N"))
//							status = "Inactive";
//						HashMap<String, String> fetchData = new HashMap<String, String>();
//						fetchData.put("GroupName", grName);
//						fetchData.put("CreaterName", mgrName);
//						fetchData.put("Count", count);
//						fetchData.put("Mobile", mobile);
//						fetchData.put("FleetId", rid);
//						fetchData.put("Status", status);
//						fleets.add(fetchData);
//						ListAdapter adapter = new SimpleAdapter(
//								PassengerProfileActivity.this, fleets,
//								R.layout.fleet_list_row, new String[]
//								{ "GroupName", "CreaterName", "Count",
//										"Mobile", "FleetId", "Status" },
//								new int[]
//								{ R.id.txtGroupName, R.id.txtMgrName,
//										R.id.txtCounter, R.id.mobile, R.id.rid,
//										R.id.isEnable })
//						{
//
//							@Override
//							public View getView(int position, View convertView,
//									ViewGroup parent)
//							{
//								View view = super.getView(position,
//										convertView, parent);
//								ImageView call = (ImageView) view
//										.findViewById(R.id.imgIcon);
//								final String mobile = ((TextView) view
//										.findViewById(R.id.mobile)).getText()
//										.toString();
//								call.setOnClickListener(new OnClickListener()
//								{
//
//									@Override
//									public void onClick(View v)
//									{
//										if (!mobile.equalsIgnoreCase(""))
//										{
//											PhoneCall(mobile);
//										}
//									}
//								});
//
//								return view;
//							}
//
//						};
//						fleetList.setAdapter(adapter);
//						fleetList.setSelection(Global.SELECTED_ITEM_FO);
//						fleetList
//								.smoothScrollToPosition(Global.SELECTED_ITEM_FO);
//						fleetList
//								.setOnItemClickListener(new OnItemClickListener()
//								{
//
//									@Override
//									public void onItemClick(
//											AdapterView<?> parent, View view,
//											int position, long id)
//									{
//										usertype = mgr
//												.GetValueFromSharedPrefs("FleetUserType");
//										if (usertype.equalsIgnoreCase("Admin"))
//										{
//											Global.SELECTED_ITEM_FO = -1;
//											Global.SELECTED_ITEM_FO = position;
//											String grName = ((TextView) view
//													.findViewById(R.id.txtGroupName))
//													.getText().toString();
//											String mgrName = ((TextView) view
//													.findViewById(R.id.txtMgrName))
//													.getText().toString();
//											String status = ((TextView) view
//													.findViewById(R.id.isEnable))
//													.getText().toString();
//											String mobile = ((TextView) view
//													.findViewById(R.id.mobile))
//													.getText().toString();
//											String fleetId = ((TextView) view
//													.findViewById(R.id.rid))
//													.getText().toString();
//											FleetUpdateDialog(grName, mgrName,
//													status, mobile, fleetId);
//										}
//									}
//								});
//					}
//
//				}
//				else
//					Log.i("Fleet Listener", "Fleet null");
//
//			}
//			else
//			{
//				isOpSuccess = false;
//				fleetState.setVisibility(View.VISIBLE);
//				fleetState.setText(message);
//			}
//		}
//		catch (Exception e)
//		{
//			fleetState.setVisibility(View.VISIBLE);
//			fleetState.setText("Null list");
//			e.printStackTrace();
//		}
//	}
//
//	private void PhoneCall(String mobile)
//	{
//		try
//		{
//
//			Intent phoneCallIntent = new Intent(Intent.ACTION_DIAL);
//			phoneCallIntent.setData(Uri.parse("tel:" + mobile));
//			startActivity(phoneCallIntent);
//
//		}
//		catch (Exception bug)
//		{
//			Log.i(TAG, "Call failed!");
//			bug.printStackTrace();
//		}
//	}
//
//	private void FleetUpdateDialog(String groupNam, String mgrNam,
//			final String status, final String mobile, final String fleetId)
//	{
//
//		final Dialog dialog = new Dialog(PassengerProfileActivity.this);
//		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//		Window window = dialog.getWindow();
//		window.setBackgroundDrawableResource(android.R.color.transparent);
//		LinearLayout.LayoutParams dialogParams = new LinearLayout.LayoutParams(
//				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
//
//		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//		View dislogView = inflater.inflate(R.layout.jugunoo_profile_dialog,
//				new LinearLayout(PassengerProfileActivity.this));
//		dialog.setContentView(dislogView, dialogParams);
//
//		EditText groupName = (EditText) dialog.findViewById(R.id.groupNameD);
//		EditText mgrName = (EditText) dialog.findViewById(R.id.mgrNameD);
//		RadioGroup radioStatusGroup = (RadioGroup) dialog
//				.findViewById(R.id.etchdrStatusGroup);
//		RadioButton act = (RadioButton) dialog.findViewById(R.id.etchdrActive);
//		RadioButton inact = (RadioButton) dialog
//				.findViewById(R.id.etchdrInactive);
//
//		Button update = (Button) dialog.findViewById(R.id.updateBtn);
//		Button cancel = (Button) dialog.findViewById(R.id.cancelBtn);
//		ImageView msg = (ImageView) dialog.findViewById(R.id.msgBtn);
//		ImageView call = (ImageView) dialog.findViewById(R.id.callBtn);
//		groupName.setText(groupNam);
//		mgrName.setText(mgrNam);
//		if (status.equalsIgnoreCase("Active"))
//		{
//			act.setChecked(true);
//		}
//		else
//		{
//			inact.setChecked(true);
//		}
//
//		radioStatusGroup
//				.setOnCheckedChangeListener(new OnCheckedChangeListener()
//				{
//
//					@Override
//					public void onCheckedChanged(RadioGroup group, int checkedId)
//					{
//						RadioButton fleetStatus = (RadioButton) dialog
//								.findViewById(group.getCheckedRadioButtonId());
//						String option = fleetStatus.getText().toString();
//						if (option.equalsIgnoreCase("Active"))
//						{
//							updateStatus = "A";
//						}
//						else
//						{
//							updateStatus = "N";
//						}
//					}
//				});
//
//		call.setOnClickListener(new OnClickListener()
//		{
//
//			@Override
//			public void onClick(View v)
//			{
//				dialog.dismiss();
//				try
//				{
//
//					Intent phoneCallIntent = new Intent(Intent.ACTION_DIAL);
//					phoneCallIntent.setData(Uri.parse("tel:" + mobile));
//					startActivity(phoneCallIntent);
//
//				}
//				catch (Exception bug)
//				{
//					Log.i(TAG, "Call failed!");
//					bug.printStackTrace();
//				}
//			}
//		});
//		msg.setOnClickListener(new OnClickListener()
//		{
//
//			@Override
//			public void onClick(View v)
//			{
//				dialog.dismiss();
//				try
//				{
//
//					Intent smsIntent = new Intent(Intent.ACTION_VIEW);
//					smsIntent.setType("vnd.android-dir/mms-sms");
//					smsIntent.putExtra("address", mobile);
//					smsIntent.putExtra("sms_body", "Hi....");
//					startActivity(smsIntent);
//
//				}
//				catch (Exception bug)
//				{
//					Log.i(TAG, "Send failed!");
//					bug.printStackTrace();
//				}
//			}
//		});
//		update.setOnClickListener(new OnClickListener()
//		{
//
//			@Override
//			public void onClick(View v)
//			{
//				dialog.dismiss();
//				saveGroup(fleetId, updateStatus);
//			}
//		});
//		cancel.setOnClickListener(new OnClickListener()
//		{
//
//			@Override
//			public void onClick(View v)
//			{
//				dialog.dismiss();
//			}
//		});
//
//		dialog.show();
//	}
//
//	private void saveGroup(String fleetId, String status)
//	{
//
//		pdRunnable = new Runnable()
//		{
//			@Override
//			public void run()
//			{
//				if (pd != null)
//				{
//					if (pd.isShowing())
//					{
//						pd.dismiss();
//					}
//				}
//			}
//		};
//		pd.show();
//
//		if (!fleetId.equalsIgnoreCase(""))
//		{
//
//			Map<String, String> params = new HashMap<String, String>();
//			params.put("UserId", mgr.GetValueFromSharedPrefs("UserID"));
//			params.put("FleetUserId", fleetId);
//			params.put("Status", status);
//			params.put("RID", "1");
//
//			NetworkHandler.SaveFleetOperator(TAG, handler, params);
//		}
//		else
//		{
//			Log.i(Global.APPTAG, "Parameter are invalid!");
//		}
//	}
//
//	private void SetFleetUpdateStatus(JSONObject object)
//	{
//
//		try
//		{
//			String resultStr = object.getString("Result");
//			String message = object.getString("Message");
//			if (!resultStr.equalsIgnoreCase("Fail"))
//			{
//				Toast.makeText(PassengerProfileActivity.this, message,
//						Toast.LENGTH_SHORT).show();
//				SetTabContent(CURRENT_TAB);
//			}
//			else
//			{
//				Toast.makeText(PassengerProfileActivity.this, message,
//						Toast.LENGTH_SHORT).show();
//			}
//		}
//		catch (Exception e)
//		{
//			e.printStackTrace();
//		}
//	}
//
//	private void GetFleetGroups()
//	{
//		if (fleets.size() != 0)
//		{
//			fleets.clear();
//		}
//		fleetState.setVisibility(View.VISIBLE);
//		fleetState.setText("");
//		fleetProgressBar.setVisibility(View.VISIBLE);
//		fleetList.setVisibility(View.GONE);
//		String userID = mgr.GetValueFromSharedPrefs("UserID");
//		String usertype = mgr.GetValueFromSharedPrefs("FleetUserType");
//		NetworkHandler.GetGroups(TAG, handler, userID, currentStatus, usertype);
//	}
//
//	private void PrepareGroupList(JSONObject object)
//	{
//
//		try
//		{
//			String resultStr = object.getString("Result");
//			String message = object.getString("Message");
//			if (!resultStr.equalsIgnoreCase("Fail"))
//			{
//				fleetList.setVisibility(View.VISIBLE);
//				JSONArray array = object.getJSONArray("GroupArray");
//				Global.fleetGroupArray = array;
//				fleets = new ArrayList<HashMap<String, String>>();
//				int len = array.length();
//				if (len != 0)
//				{
//					isGroupSuccess = true;
//					for (int f = 0; f < len; f++)
//					{
//						JSONObject obj = array.getJSONObject(f);
//						grId = obj.getString("GroupId");
//						grName = obj.getString("GroupName");
//						grMgrName = obj.getString("ManagerName");
//						grMgrID = obj.getString("ManagingUserId");
//						grStatus = obj.getString("Status");
//						grMobileNo = obj.getString("MobileNo");
//						grCount = obj.getString("Count");
//						if (grStatus.equalsIgnoreCase("A"))
//							grStatus = "Active";
//						else if (grStatus.equalsIgnoreCase("N"))
//							grStatus = "Inactive";
//						HashMap<String, String> fetchData = new HashMap<String, String>();
//						fetchData.put("GroupId", grId);
//						fetchData.put("GroupName", grName);
//						fetchData.put("ManagerName", grMgrName);
//						fetchData.put("Status", grStatus);
//						fetchData.put("MobileNo", grMobileNo);
//						fetchData.put("Count", grCount);
//						fetchData.put("ManagingUserId", grMgrID);
//						fleets.add(fetchData);
//						ListAdapter adapter = new SimpleAdapter(
//								PassengerProfileActivity.this, fleets,
//								R.layout.fleet_list_row, new String[]
//								{ "GroupId", "GroupName", "ManagerName",
//										"Status", "MobileNo", "Count",
//										"ManagingUserId" }, new int[]
//								{ R.id.rid, R.id.txtGroupName, R.id.txtMgrName,
//										R.id.isEnable, R.id.mobile,
//										R.id.txtCounter, R.id.managingUid })
//						{
//							@Override
//							public View getView(int position, View convertView,
//									ViewGroup parent)
//							{
//								View view = super.getView(position,
//										convertView, parent);
//								ImageView call = (ImageView) view
//										.findViewById(R.id.imgIcon);
//								final String mobile = ((TextView) view
//										.findViewById(R.id.mobile)).getText()
//										.toString();
//								call.setOnClickListener(new OnClickListener()
//								{
//
//									@Override
//									public void onClick(View v)
//									{
//										if (!mobile.equalsIgnoreCase(""))
//										{
//											PhoneCall(mobile);
//										}
//									}
//								});
//
//								return view;
//							}
//						};
//						fleetList.setAdapter(adapter);
//						fleetList.setSelection(Global.SELECTED_ITEM_G);
//						fleetList
//								.smoothScrollToPosition(Global.SELECTED_ITEM_G);
//						fleetList
//								.setOnItemClickListener(new OnItemClickListener()
//								{
//
//									@Override
//									public void onItemClick(
//											AdapterView<?> parent, View view,
//											int position, long id)
//									{
//
//										usertype = mgr
//												.GetValueFromSharedPrefs("FleetUserType");
//										if (usertype.equalsIgnoreCase("Admin"))
//										{
//											Global.SELECTED_ITEM_G = -1;
//											Global.SELECTED_ITEM_G = position;
//											IS_GROUP_ACTION = 1;
//											String grName = ((TextView) view
//													.findViewById(R.id.txtGroupName))
//													.getText().toString();
//											String mgrName = ((TextView) view
//													.findViewById(R.id.txtMgrName))
//													.getText().toString();
//											String status = ((TextView) view
//													.findViewById(R.id.isEnable))
//													.getText().toString();
//
//											String rid = ((TextView) view
//													.findViewById(R.id.rid))
//													.getText().toString();
//											String mobile = ((TextView) view
//													.findViewById(R.id.mobile))
//													.getText().toString();
//
//											String mgrID = ((TextView) view
//													.findViewById(R.id.managingUid))
//													.getText().toString();
//
//											Intent i = new Intent(
//													PassengerProfileActivity.this,
//													AddFleetGroupActivity.class);
//											i.putExtra("GrName", grName);
//											i.putExtra("MgrName", mgrName);
//											i.putExtra("Status", status);
//											i.putExtra("GroupId", rid);
//											i.putExtra("MobileNo", mobile);
//											i.putExtra("ManagingUserId", mgrID);
//											startActivity(i);
//											PassengerProfileActivity.this
//													.finish();
//										}
//									}
//								});
//					}
//
//				}
//				else
//				{
//					Log.i("Fleet Listener", "Fleet null");
//				}
//
//			}
//			else
//			{
//				isGroupSuccess = false;
//				fleetState.setVisibility(View.VISIBLE);
//				fleetState.setText(message);
//			}
//		}
//		catch (Exception e)
//		{
//			fleetState.setVisibility(View.VISIBLE);
//			fleetState.setText("Null list");
//			e.printStackTrace();
//		}
//	}
//
//	private void GetFleetUsers()
//	{
//		if (fleets.size() != 0)
//		{
//			fleets.clear();
//		}
//		fleetState.setVisibility(View.VISIBLE);
//		fleetState.setText("");
//		fleetProgressBar.setVisibility(View.VISIBLE);
//		fleetList.setVisibility(View.GONE);
//		String userID = mgr.GetValueFromSharedPrefs("UserID");
//		String usertype = mgr.GetValueFromSharedPrefs("FleetUserType");
//		NetworkHandler.GetUsers(TAG, handler, userID, currentStatus, usertype);
//	}
//
//	private void PrepareUserList(JSONObject object)
//	{
//
//		try
//		{
//			String resultStr = object.getString("Result");
//			String message = object.getString("Message");
//			if (!resultStr.equalsIgnoreCase("Fail"))
//			{
//				fleetList.setVisibility(View.VISIBLE);
//				JSONArray array = object.getJSONArray("GroupArray");
//				Global.fleetUserArray = array;
//				fleets = new ArrayList<HashMap<String, String>>();
//				int len = array.length();
//				if (len != 0)
//				{
//					for (int f = 0; f < len; f++)
//					{
//						JSONObject obj = array.getJSONObject(f);
//						grId = obj.getString("GroupId");
//						grName = obj.getString("GroupName");
//						userName = obj.getString("UserName");
//						userId = obj.getString("UserId");
//						grStatus = obj.getString("Status");
//						grMobileNo = obj.getString("MobileNo");
//						grCount = obj.getString("Count");
//						usertype = mgr.GetValueFromSharedPrefs("FleetUserType");
//						if (usertype.equalsIgnoreCase("Manager"))
//						{
//							grOwn = obj.getString("Own");
//						}
//
//						if (grStatus.equalsIgnoreCase("A"))
//							grStatus = "Active";
//						else if (grStatus.equalsIgnoreCase("N"))
//							grStatus = "Inactive";
//						HashMap<String, String> fetchData = new HashMap<String, String>();
//						fetchData.put("GroupId", grId);
//						fetchData.put("GroupName", grName);
//						fetchData.put("UserName", userName);
//						fetchData.put("UserId", userId);
//						fetchData.put("Status", grStatus);
//						fetchData.put("MobileNo", grMobileNo);
//						fetchData.put("Count", grCount);
//						if (usertype.equalsIgnoreCase("Manager"))
//						{
//							fetchData.put("Own", grOwn);
//						}
//
//						fleets.add(fetchData);
//						ListAdapter adapter = new SimpleAdapter(
//								PassengerProfileActivity.this, fleets,
//								R.layout.fleet_list_row, new String[]
//								{ "GroupId", "GroupName", "UserName", "UserId",
//										"Status", "MobileNo", "Count", "Own" },
//								new int[]
//								{ R.id.rid, R.id.txtGroupName, R.id.txtMgrName,
//										R.id.userid, R.id.isEnable,
//										R.id.mobile, R.id.txtCounter,
//										R.id.userOwn })
//						{
//							@Override
//							public View getView(int position, View convertView,
//									ViewGroup parent)
//							{
//								View view = super.getView(position,
//										convertView, parent);
//								ImageView call = (ImageView) view
//										.findViewById(R.id.imgIcon);
//								final String mobile = ((TextView) view
//										.findViewById(R.id.mobile)).getText()
//										.toString();
//								call.setOnClickListener(new OnClickListener()
//								{
//
//									@Override
//									public void onClick(View v)
//									{
//										if (!mobile.equalsIgnoreCase(""))
//										{
//											PhoneCall(mobile);
//										}
//									}
//								});
//
//								return view;
//							}
//						};
//
//						fleetList.setAdapter(adapter);
//						fleetList.setSelection(Global.SELECTED_ITEM_U);
//						fleetList
//								.smoothScrollToPosition(Global.SELECTED_ITEM_U);
//
//						fleetList
//								.setOnItemClickListener(new OnItemClickListener()
//								{
//
//									@Override
//									public void onItemClick(
//											AdapterView<?> parent, View view,
//											int position, long id)
//									{
//
//										Global.SELECTED_ITEM_U = -1;
//										Global.SELECTED_ITEM_U = position;
//										IS_GROUP_ACTION = 1;
//
//										String mgrName = ((TextView) view
//												.findViewById(R.id.txtMgrName))
//												.getText().toString();
//										String userId = ((TextView) view
//												.findViewById(R.id.userid))
//												.getText().toString();
//										String mobile = ((TextView) view
//												.findViewById(R.id.mobile))
//												.getText().toString();
//										String rid = ((TextView) view
//												.findViewById(R.id.rid))
//												.getText().toString();
//										String status = ((TextView) view
//												.findViewById(R.id.isEnable))
//												.getText().toString();
//										usertype = mgr
//												.GetValueFromSharedPrefs("FleetUserType");
//										if (usertype.equalsIgnoreCase("Admin"))
//										{
//											Intent i = new Intent(
//													PassengerProfileActivity.this,
//													AddFleetUserActivity.class);
//											i.putExtra("GroupId", rid);
//											i.putExtra("UserName", mgrName);
//											i.putExtra("UserId", userId);
//											i.putExtra("MobileNo", mobile);
//											i.putExtra("Status", status);
//											startActivity(i);
//											PassengerProfileActivity.this
//													.finish();
//										}
//										else if (usertype
//												.equalsIgnoreCase("Manager"))
//										{
//											String own = ((TextView) view
//													.findViewById(R.id.userOwn))
//													.getText().toString();
//											if (own.equalsIgnoreCase("Y"))
//											{
//												Intent i = new Intent(
//														PassengerProfileActivity.this,
//														AddFleetUserActivity.class);
//												i.putExtra("GroupId", rid);
//												i.putExtra("UserName", mgrName);
//												i.putExtra("UserId", userId);
//												i.putExtra("MobileNo", mobile);
//												i.putExtra("Status", status);
//												startActivity(i);
//												PassengerProfileActivity.this
//														.finish();
//											}
//										}
//									}
//
//								});
//					}
//
//				}
//				else
//					Log.i("Fleet Listener", "Fleet null");
//
//			}
//			else
//			{
//				fleetState.setVisibility(View.VISIBLE);
//				fleetState.setText(message);
//			}
//		}
//		catch (Exception e)
//		{
//			fleetState.setVisibility(View.VISIBLE);
//			fleetState.setText("Null list");
//			e.printStackTrace();
//		}
//	}
//
//}
