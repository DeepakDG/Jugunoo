package com.hirecraft.jugunoo.passenger;

import java.util.HashMap;

import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.hirecraft.jugunoo.passenger.common.Constant;
import com.hirecraft.jugunoo.passenger.common.ConstantMessages;
import com.hirecraft.jugunoo.passenger.common.Function;
import com.hirecraft.jugunoo.passenger.common.Global;
import com.hirecraft.jugunoo.passenger.common.TransparentProgressDialog;
import com.hirecraft.jugunoo.passenger.networkhandler.NetworkHandler;
import com.hirecraft.jugunoo.passenger.services.VolleyErrorHelper;
import com.hirecraft.jugunoo.passenger.utility.JugunooUtil;
import com.hirecraft.jugunoo.passenger.utility.SharedPreferencesManager;

import de.keyboardsurfer.android.widget.crouton.Crouton;

public class Login extends Activity implements OnClickListener
{
	private String TAG = Login.class.getSimpleName();
	private EditText etMobileLogin, etPwLogin;
	private String deviceID;
	private TransparentProgressDialog pd;

	private Handler pdHandler;
	private TransparentProgressDialog pDialog;
	private Runnable pdRunnable;

	private SharedPreferencesManager mgr;
	private Dialog dialog;
	private Button btLogin;
	private TextView tvForgotPw;
	// private boolean flag;
	private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		SetActionBar();

		setContentView(R.layout.activity_login_screen);

		etMobileLogin = (EditText) findViewById(R.id.etMobileLogin);
		etPwLogin = (EditText) findViewById(R.id.etPwLogin);

		btLogin = (Button) findViewById(R.id.btLogin);
		tvForgotPw = (TextView) findViewById(R.id.tvForgotPw);

		btLogin.setOnClickListener(this);
		tvForgotPw.setOnClickListener(this);

		mgr = new SharedPreferencesManager(getApplicationContext());

		etPwLogin
				.setOnEditorActionListener(new EditText.OnEditorActionListener()
				{

					@Override
					public boolean onEditorAction(TextView v, int actionId,
							KeyEvent event)
					{
						if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER))
								|| (actionId == EditorInfo.IME_ACTION_DONE))
						{

							if (JugunooUtil.isConnectedToInternet(Login.this))
							{

								String usernameStr = etMobileLogin.getText()
										.toString();
								String passwordStr = etPwLogin.getText()
										.toString();
								if (TextUtils.isEmpty(usernameStr))
								{
									setTextColor(ConstantMessages.MSG2,
											"Mobile");
								}
								else if (usernameStr.length() != 10)
								{
									setTextColor(ConstantMessages.MSG3,
											"Mobile");

								}
								else
								{
									LoginValidation(usernameStr, passwordStr);
								}
							}
							else
							{
								Function.showToast(Login.this,
										Global.networkErrorMsg);
							}
						}
						return false;
					}
				});

		// String gcmid = mgr.GetValueFromSharedPrefs("GCM_ID");
		// Global.GCMID = gcmid;
		// if (TextUtils.isEmpty(gcmid))
		// {
		// isGCMnull = true;
		//
		// if (checkPlayServices())
		// {
		// registerInBackground();
		// }
		//
		// }

		openAnimation();
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		checkPlayServices();
		Function.showSoftKeyBoard(Login.this);
	}

	private boolean checkPlayServices()
	{
		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(this);
		if (resultCode != ConnectionResult.SUCCESS)
		{
			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode))
			{
				GooglePlayServicesUtil.getErrorDialog(resultCode, this,
						PLAY_SERVICES_RESOLUTION_REQUEST).show();
			}
			else
			{
				Log.i("Login", "This device is not supported.");
				finish();
			}
			return false;
		}
		return true;
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
		View bar = inflater.inflate(R.layout.custom_title_actionbar, l);

		LinearLayout back = (LinearLayout) bar.findViewById(R.id.llCTitle);
		TextView title = (TextView) bar.findViewById(R.id.tvTATitle);
		title.setText("Login");

		back.setOnClickListener(new OnClickListener()
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
	}

	@Override
	public void onBackPressed()
	{
		super.onBackPressed();
		Login.this.finish();
		startActivity(new Intent().setClass(Login.this, SplashScreen.class));
	}

	private void setTextColor(String text, String field)
	{

		if (!text.equalsIgnoreCase(""))
		{
			Spannable strength = new SpannableString(text);
			if (text.equalsIgnoreCase(ConstantMessages.MSG2)
					|| text.equalsIgnoreCase(ConstantMessages.MSG3))
				strength.setSpan(
						new ForegroundColorSpan(Color.rgb(231, 39, 2)), 0,
						text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			else
				strength.setSpan(
						new ForegroundColorSpan(Color.rgb(40, 183, 9)), 0,
						text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{

		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0)
		{

			if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0)
			{
				startActivity(new Intent(Login.this, SplashScreen.class));
				Login.this.finish();
				return true;
			}

		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onStart()
	{
		super.onStart();
		JugunooUtil util = new JugunooUtil(Login.this);
		deviceID = util.getUniqueDeviceID(getApplicationContext());

		mgr.SaveValueToSharedPrefs("DEVICE_ID", deviceID);
	}

	@Override
	public void onClick(View v)
	{

		switch (v.getId())
		{

			case R.id.btLogin:

				if (JugunooUtil.isConnectedToInternet(Login.this))
				{

					String usernameStr = etMobileLogin.getText().toString();
					String passwordStr = etPwLogin.getText().toString();

					if (TextUtils.isEmpty(usernameStr))
					{
						Function.showToast(Login.this, ConstantMessages.MSG2);
						// setTextColor("Mobile Number is mandatory.",
						// "Mobile");
					}
					else if (usernameStr.length() != 10)
					{
						// setTextColor("Enter a valid Mobile Number.",
						// "Mobile");
						Function.showToast(Login.this, ConstantMessages.MSG3);
					}
					else if (TextUtils.isEmpty(passwordStr))
					{
						Function.showToast(Login.this, ConstantMessages.MSG4);
					}
					else
					{
						LoginValidation(usernameStr, passwordStr);
					}
				}
				else
				{
					Function.showToast(Login.this, ConstantMessages.MSG6);
				}

				break;

			case R.id.tvForgotPw:

				startActivity(new Intent().setClass(Login.this,
						ForgotPasswordActivity.class));
				// startActivity(new Intent().setClass(Login.this,
				// InvoiceActivity.class));

				break;
		}
	}

	private void LoginValidation(String userNameStr, String passwordStr)
	{

		if (!TextUtils.isEmpty(userNameStr) && !TextUtils.isEmpty(passwordStr))
		{
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("username", userNameStr);
			params.put("password", passwordStr);
			params.put("DeviceId", deviceID);
			params.put("GcmId", "");

			// params.put("GcmId", Global.GCMID);
			// LoginResuest(params);
			makeLoginRequest(params);
			Log.i(Global.APPTAG, "LOGIN:" + params.toString());
		}
		else
		{
			Function.showToast(Login.this, ConstantMessages.MSG5);
		}
	}

	private void makeLoginRequest(HashMap<String, String> params)
	{
		showLoadingDilog();
		NetworkHandler.loginRequest(TAG, handlerLoginReq, params);
	}

	Handler handlerLoginReq = new Handler()
	{
		public void handleMessage(android.os.Message msg)
		{
			switch (msg.arg1)
			{
				case Constant.MessageState.LOGIN_SUCCESS:
					cancelLoadingDialog();
					parseLoginRes((JSONObject) msg.obj);
					break;

				case Constant.MessageState.FAIL:
					cancelLoadingDialog();
					VolleyErrorHelper.getMessage(msg.obj, Login.this, true);
					break;

				default:
					break;
			}
		}
	};

	private void parseLoginRes(JSONObject obj)
	{
		try
		{
			String result = obj.getString("Result");
			String message = obj.getString("Message");
			if (result.equalsIgnoreCase("Pass"))
			{
				String userID = obj.getString("UserId");
				// String isDriver = obj.getString("IsDriver");
				// String isPassenger = obj.getString("IsPassenger");
				// String isTracker = obj.getString("IsTracker");
				// String deviceStatus = obj.getString("DeviceStatus");

				mgr.SaveValueToSharedPrefs("UserID", userID);
				mgr.savePreferenceIndex(Constant.PREFERENCE_INDEX, 0);

				if (obj.has("UserType"))
				{
					mgr.SaveValueToSharedPrefs(Constant.USER_TYPE,
							obj.getString("UserType"));
				}

				mgr.SaveValueToSharedPrefs("GCM_ID", "");
				mgr.SaveValueToSharedPrefs("isGcmSent", false);

				mgr.SaveValueToSharedPrefs("UserRole", "Passenger");
				startActivity(new Intent(Login.this, LandingPage.class));
				Login.this.finish();

				// String gcmid = mgr.GetValueFromSharedPrefs("GCM_ID");
				// Log.i("login", "gcm=" + gcmid);
				// if (TextUtils.isEmpty(gcmid))
				// {
				// isGCMnull = true;
				// GetGCMID(isDriver, isPassenger, isTracker, deviceStatus);
				//
				// }
				// else
				// {
				// loginStatus(isDriver, isPassenger, isTracker, deviceStatus);
				// }

				// loginStatus(isDriver, isPassenger, isTracker, deviceStatus);
			}
			else
			{
				// JugunooInteractiveDialog("LOGIN", message);
				Function.showToast(Login.this, message);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	};

	// private void makeDeviceRegistrationRequest(HashMap<String, String>
	// params)
	// {
	// showLoadingDilog();
	// NetworkHandler.deviceRegistration(TAG, handlerDevReg, params);
	// }

	// Handler handlerDevReg = new Handler()
	// {
	//
	// public void handleMessage(android.os.Message msg)
	// {
	// switch (msg.arg1)
	// {
	// case Constant.MessageState.DEVICE_REGISTRATION_SUCCESS:
	//
	// cancelLoadingDialog();
	// parseDevRegResponse((JSONObject) msg.obj);
	// flag = true;
	//
	// // loginStatus(isDriver, isPassenger, isTracker,
	// // deviceStatus);
	//
	// break;
	//
	// case Constant.MessageState.FAIL:
	// cancelLoadingDialog();
	// flag = false;
	// VolleyErrorHelper.getMessage(msg.obj, Login.this, true);
	//
	// break;
	//
	// default:
	// break;
	// }
	//
	// };
	//
	// };
	//
	// private void parseDevRegResponse(JSONObject obj)
	// {
	// try
	// {
	// String result = obj.getString("Result");
	// if (result.equalsIgnoreCase("Pass"))
	// {
	// Intent landingPageIntent = new Intent(Login.this,
	// LandingPage.class);
	// landingPageIntent.putExtra("from", "login");
	// startActivity(landingPageIntent);
	// Login.this.finish();
	// }
	// else
	// {
	// Function.showToast(Login.this, obj.getString("Message"));
	// }
	// }
	// catch (Exception e)
	// {
	// e.printStackTrace();
	// }
	// }

	@Override
	protected void onPause()
	{
		super.onPause();
		if (dialog != null)
		{
			dialog.dismiss();
		}
		if (pd != null)
		{
			if (pd.isShowing())
			{
				pd.dismiss();
			}
		}
		closeAnimation();

		Crouton.cancelAllCroutons();
	}

	@Override
	protected void onStop()
	{
		super.onStop();
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
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

	private void showLoadingDilog()
	{
		// Showing loading dialog

		pdHandler = new Handler();
		pDialog = new TransparentProgressDialog(Login.this,
				R.drawable.loading_image);

		pdRunnable = new Runnable()
		{
			@Override
			public void run()
			{
				if (pDialog != null)
				{
					if (pDialog.isShowing())
					{
						pDialog.dismiss();
					}
				}
			}
		};
		pDialog.show();
	}

	private void cancelLoadingDialog()
	{
		// Cancel loading dialog

		pdHandler.removeCallbacks(pdRunnable);

		if (pDialog.isShowing())
		{
			pDialog.dismiss();
		}
	}

	// private void DeviceRegistrationTask(RequestParams params,
	// final String isDriver, final String isPassenger,
	// final String isTracker, final String deviceStatus)
	// {
	//
	// AsyncHttpClient client = new AsyncHttpClient();
	// client.setTimeout(30000);
	// client.post(Global.JUGUNOO_WS + "DeviceRegistration", params,
	// new AsyncHttpResponseHandler()
	// {
	//
	// @Override
	// @Deprecated
	// public void onFailure(int statusCode, Throwable error,
	// String content)
	// {
	//
	// try
	// {
	// if (statusCode == 0)
	// {
	// Function.showToast(Login.this,
	// "Login failed due to  connection time expired. Try again");
	// }
	// else
	// {
	// Function.showToast(Login.this,
	// Global.networkErrorMsg);
	// }
	//
	// }
	// catch (Exception bug)
	// {
	// bug.printStackTrace();
	// }
	// super.onFailure(statusCode, error, content);
	//
	// }
	//
	// @Override
	// @Deprecated
	// public void onSuccess(int statusCode, String content)
	// {
	// try
	// {
	//
	// if (statusCode == 200)
	// {
	// JSONObject obj = new JSONObject(content);
	// String result = obj.getString("Result");
	// if (result.equalsIgnoreCase("Pass"))
	// {
	// Intent landingPageIntent = new Intent(
	// Login.this, LandingPage.class);
	// landingPageIntent.putExtra("from", "login");
	// startActivity(landingPageIntent);
	// Login.this.finish();
	// }
	// loginStatus(isDriver, isPassenger, isTracker,
	// deviceStatus);
	//
	// }
	// else
	// {
	// JugunooInteractiveDialog("LOGIN",
	// "Device Registration failed.");
	//
	// startActivity(new Intent(Login.this,
	// SplashScreen.class));
	// Login.this.finish();
	// }
	//
	// }
	// catch (Exception e)
	// {
	// e.printStackTrace();
	// }
	// super.onSuccess(content);
	//
	// }
	//
	// });
	// }

	// private boolean IsGcmSuccess(String isDriver, String isPassenger,
	// String isTracker, String deviceStatus)
	// {
	//
	// String gcmID = mgr.GetValueFromSharedPrefs("GCM_ID");
	// if (gcmID.equalsIgnoreCase(""))
	// {
	// return false;
	// }
	// else
	// {
	// return true;
	// }
	// }

	// private void GetGCMID(final String isDriver, final String isPassenger,
	// final String isTracker, final String deviceStatus)
	// {
	//
	// new AsyncTask<Void, Void, Void>()
	// {
	// @Override
	// protected Void doInBackground(Void... params)
	// {
	//
	// try
	// {
	// String gcmReg = "";
	// String diviceID = Global.DEVICE_ID;
	//
	// SharedPreferencesManager manager;
	// manager = new SharedPreferencesManager(
	// getApplicationContext());
	// gcmReg = manager.GetValueFromSharedPrefs("GCM_ID");
	// if (gcmReg.equalsIgnoreCase(""))
	// {
	// String gcmRegID = gcmRegistrationHelper
	// .Register(diviceID);
	//
	// manager.SaveValueToSharedPrefs("GCM_ID", gcmRegID);
	// Global.GCMID = gcmRegID;
	// Log.i(Global.APPTAG, "GCM:" + gcmRegID);
	//
	// DeviceRegisteration(isDriver, isPassenger, isTracker,
	// deviceStatus);
	//
	// }
	// else
	// {
	// Global.GCMID = gcmReg;
	// Log.i(Global.APPTAG, "GCM:" + Global.GCMID);
	// }
	//
	// }
	// catch (Exception bug)
	// {
	// bug.printStackTrace();
	// for (StackTraceElement gcmErrElement : bug.getStackTrace())
	// {
	// sendNotification("GCM Error:Please sync your Google account!"
	// + gcmErrElement.getClassName());
	// }
	// }
	// return null;
	//
	// }
	//
	// }.execute(null, null, null);
	//
	// }

	// public static final int NOTIFICATION_ID = 1;
	// private NotificationManager mNotificationManager;
	// NotificationCompat.Builder builder;
	//
	// @SuppressWarnings("deprecation")
	// private void sendNotification(String msg)
	// {
	// mNotificationManager = (NotificationManager) this
	// .getSystemService(Context.NOTIFICATION_SERVICE);
	//
	// builder = new NotificationCompat.Builder(this)
	// .setSmallIcon(R.drawable.ic_launcher)
	// .setContentTitle("GCM ERROR! ")
	// .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
	// .setLights(Color.BLUE, 500, 1000).setAutoCancel(true)
	// .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
	// .setContentText(msg);
	//
	// builder.getNotification().flags |= Notification.FLAG_AUTO_CANCEL;
	// mNotificationManager.notify(NOTIFICATION_ID, builder.build());
	// }
	//
	// private void DeviceRegisteration(final String isDriver,
	// final String isPassenger, final String isTracker,
	// final String deviceStatus)
	// {
	//
	// Login.this.runOnUiThread(new Runnable()
	// {
	//
	// @Override
	// public void run()
	// {
	//
	// String userId = mgr.GetValueFromSharedPrefs("UserID");
	// String deviceId = mgr.GetValueFromSharedPrefs("DEVICE_ID");
	// String gcmID = mgr.GetValueFromSharedPrefs("GCM_ID");
	// HashMap<String, String> params = new HashMap<String, String>();
	// params.put("UserId", userId);
	// params.put("GcmId", gcmID);
	// params.put("DeviceId", deviceId);
	// pdHandler = new Handler();
	// pd = new TransparentProgressDialog(Login.this,
	// R.drawable.loading_image);
	//
	// pdRunnable = new Runnable()
	// {
	// @Override
	// public void run()
	// {
	// if (pd != null)
	// {
	// if (pd.isShowing())
	// {
	// pd.dismiss();
	// }
	// }
	// }
	// };
	// pd.show();
	//
	// // DeviceRegistrationTask(params, isDriver, isPassenger,
	// // isTracker, deviceStatus);
	//
	// makeDeviceRegistrationRequest(params);
	//
	// if (flag)
	// {
	// loginStatus(isDriver, isPassenger, isTracker, deviceStatus);
	// }
	//
	// pdHandler.removeCallbacks(pdRunnable);
	// if (pd.isShowing())
	// {
	// pd.dismiss();
	// }
	// }
	// });
	//
	// }

	// private void loginStatus(String isDriver, String isPassenger,
	// String isTracker, String deviceStatus)
	// {
	//
	// String userId = mgr.GetValueFromSharedPrefs("UserID");
	// // gcmid = mgr.GetValueFromSharedPrefs("GCM_ID");
	//
	// if (isPassenger.equalsIgnoreCase("1"))
	// {
	// mgr.SaveValueToSharedPrefs("UserRole", "Passenger");
	// startActivity(new Intent(Login.this, SplashScreen.class));
	// Login.this.finish();
	// }
	// else
	// {
	// mgr.SaveValueToSharedPrefs("UserID", "");
	// JugunooInteractiveDialog("LOGIN", ConstantMessages.MSG7);
	// }
	//
	// if (isPassenger.equalsIgnoreCase("1"))
	// {
	// Global.IsPassenger = true;
	// mgr.SaveValueToSharedPrefs("UserRole", "Passenger");
	//
	// if (deviceStatus.equalsIgnoreCase("N"))
	// {
	// // isSuccessGcm = IsGcmSuccess(isDriver, isPassenger, isTracker,
	// // deviceStatus);
	//
	// // if (isSuccessGcm == true)
	// // {
	//
	// if (!userId.equalsIgnoreCase("")
	// && !Global.DEVICE_ID.equalsIgnoreCase(""))
	// {
	// HashMap<String, String> params = new HashMap<String, String>();
	// params.put("UserId", userId);
	// params.put("GcmId", gcmid);
	// params.put("DeviceId", Global.DEVICE_ID);
	//
	// // DeviceRegistrationTask(params, isDriver, isPassenger,
	// // isTracker, deviceStatus);
	//
	// makeDeviceRegistrationRequest(params);
	//
	// if (flag)
	// {
	// loginStatus(isDriver, isPassenger, isTracker,
	// deviceStatus);
	// }
	// }
	// else
	// {
	// Log.i(Global.APPTAG, "GCM ID is null!");
	// }
	//
	// // }
	//
	// }
	// else
	// {
	//
	// // showDialog("Login successful.");
	// Intent landingPageIntent = new Intent(Login.this,
	// LandingPage.class);
	// landingPageIntent.putExtra("from", "login");
	// startActivity(landingPageIntent);
	// Login.this.finish();
	// }
	// }
	//
	// // if (isDriver.equalsIgnoreCase("0") &&
	// // isPassenger.equalsIgnoreCase("0")
	// // && isTracker.equalsIgnoreCase("1")) {
	// //
	// // Global.IsTracker = true;
	// // mgr.SaveValueToSharedPrefs("UserRole", "Tracker");
	// //
	// // if (deviceStatus.equalsIgnoreCase("N")) {
	// // isSuccessGcm = IsGcmSuccess(isDriver, isPassenger, isTracker,
	// // deviceStatus);
	// // if (isSuccessGcm == true) {
	// // if (!userId.equalsIgnoreCase("")
	// // && !deviceID.equalsIgnoreCase("")) {
	// // RequestParams params = new RequestParams();
	// // params.put("UserId", userId);
	// // params.put("GcmId", Global.GCMID);
	// // params.put("DeviceId", deviceID);
	// // DeviceRegistrationTask(params, isDriver, isPassenger,
	// // isTracker, deviceStatus);
	// // }
	// // } else {
	// // Log.i(Global.APPTAG, "GCM ID is null");
	// // }
	// //
	// // } else {
	// //
	// // // showDialog("Login successful.");
	// //
	// // Intent landingPageIntent = new Intent(Login.this,
	// // LandingPage.class);
	// // landingPageIntent.putExtra("from", "login");
	// // startActivity(landingPageIntent);
	// // Login.this.finish();
	// //
	// // }
	// // }
	// }

	// private void JugunooInteractiveDialog(String title, String message)
	// {
	//
	// dialog = new Dialog(Login.this);
	// dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
	// Window window = dialog.getWindow();
	// window.setBackgroundDrawableResource(android.R.color.transparent);
	// LinearLayout.LayoutParams dialogParams = new LinearLayout.LayoutParams(
	// LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
	//
	// LayoutInflater inflater = (LayoutInflater)
	// getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	// RelativeLayout lay = new RelativeLayout(Login.this);
	// View dislogView = inflater.inflate(R.layout.jugunoo_interactive_dialog,
	// lay);
	// dialog.setContentView(dislogView, dialogParams);
	//
	// TextView textView = (TextView) dialog.findViewById(R.id.messageText);
	// Button centerBtn = (Button) dialog.findViewById(R.id.centerBtn);
	// Button leftBtn = (Button) dialog.findViewById(R.id.leftBtn);
	// Button rightBtn = (Button) dialog.findViewById(R.id.rightBtn);
	// textView.setTypeface(light);
	// centerBtn.setTypeface(bold);
	// leftBtn.setTypeface(bold);
	// rightBtn.setTypeface(bold);
	// if (title.equalsIgnoreCase("LOGIN"))
	// {
	//
	// leftBtn.setVisibility(View.GONE);
	// rightBtn.setVisibility(View.GONE);
	// String ok = getResources().getString(R.string.ok);
	// textView.setText(message);
	// centerBtn.setText(ok);
	// centerBtn.setOnClickListener(new OnClickListener()
	// {
	//
	// @Override
	// public void onClick(View v)
	// {
	// dialog.dismiss();
	// }
	// });
	// }
	// else if (title.equalsIgnoreCase("NETWORK"))
	// {
	//
	// leftBtn.setVisibility(View.GONE);
	// rightBtn.setVisibility(View.GONE);
	// String ok = getResources().getString(R.string.ok);
	// textView.setText(message);
	// centerBtn.setText(ok);
	// centerBtn.setOnClickListener(new OnClickListener()
	// {
	//
	// @Override
	// public void onClick(View v)
	// {
	// dialog.dismiss();
	// }
	// });
	// }
	// else if (title.equalsIgnoreCase("FPWD"))
	// {
	//
	// leftBtn.setVisibility(View.GONE);
	// rightBtn.setVisibility(View.GONE);
	// String ok = getResources().getString(R.string.ok);
	// textView.setText(message);
	// centerBtn.setText(ok);
	// centerBtn.setOnClickListener(new OnClickListener()
	// {
	//
	// @Override
	// public void onClick(View v)
	// {
	// dialog.dismiss();
	// }
	// });
	// }
	// dialog.show();
	// }
	//
	// private void changeLocale(final String languageCode)
	// {
	// new AsyncTask<Void, Void, Void>()
	// {
	//
	// @Override
	// protected Void doInBackground(Void... params)
	// {
	// String languageToLoad = languageCode;
	// Locale locale = new Locale(languageToLoad);
	// Locale.setDefault(locale);
	// Configuration config = new Configuration();
	// config.locale = locale;
	// getResources().updateConfiguration(config,
	// getResources().getDisplayMetrics());
	//
	// Bidi b = new Bidi(languageToLoad,
	// Bidi.DIRECTION_DEFAULT_LEFT_TO_RIGHT);
	// b.isRightToLeft();
	// mgr.SaveValueToSharedPrefs("", languageToLoad);
	// Intent i = getIntent();
	// finish();
	// startActivity(i);
	// return null;
	// }
	//
	// }.execute();
	// }
	//
	// private void registerInBackground()
	// {
	//
	// new AsyncTask<Void, Void, String>()
	// {
	// @Override
	// protected String doInBackground(Void... params)
	// {
	// String msg = "";
	// try
	// {
	// String gcmIDStr = "";
	// String diviceID = Global.DEVICE_ID;
	// String userID = mgr.GetValueFromSharedPrefs("UserID");
	//
	// SharedPreferencesManager manager;
	// manager = new SharedPreferencesManager(
	// getApplicationContext());
	// gcmIDStr = manager.GetValueFromSharedPrefs("GCM_ID");
	//
	// Log.e("gcm", "gcm=" + gcmIDStr);
	//
	// if (TextUtils.isEmpty(gcmIDStr))
	// {
	// String gcmRegID = gcmRegistrationHelper.Register(
	// diviceID, userID);
	// manager.SaveValueToSharedPrefs("GCM_ID", gcmRegID);
	// Global.GCMID = gcmRegID;
	// Log.i(Global.APPTAG, "GCM:" + gcmRegID);
	//
	// }
	// else
	// {
	// Global.GCMID = gcmIDStr;
	// Log.i(Global.APPTAG, "GCM:" + Global.GCMID);
	// }
	//
	// }
	// catch (Exception bug)
	// {
	// bug.printStackTrace();
	// for (StackTraceElement gcmErrElement : bug.getStackTrace())
	// {
	// sendNotification("GCM Error:"
	// + gcmErrElement.getClassName());
	// }
	// }
	// return msg;
	// }
	//
	// @Override
	// protected void onPostExecute(String msg)
	// {
	//
	// }
	// }.execute(null, null, null);
	// }
}
