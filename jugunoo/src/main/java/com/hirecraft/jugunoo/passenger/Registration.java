//package com.hirecraft.jugunoo.passenger;
//
//import java.util.HashMap;
//import java.util.Map;
//import java.util.regex.Matcher;
//
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import android.annotation.SuppressLint;
//import android.app.ActionBar;
//import android.app.ActionBar.LayoutParams;
//import android.app.Activity;
//import android.app.Dialog;
//import android.app.Notification;
//import android.app.NotificationManager;
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.graphics.Color;
//import android.graphics.Typeface;
//import android.os.AsyncTask;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.Message;
//import android.provider.Settings;
//import android.support.v4.app.NotificationCompat;
//import android.telephony.SmsManager;
//import android.telephony.SmsMessage;
//import android.text.Editable;
//import android.text.Spannable;
//import android.text.SpannableString;
//import android.text.TextUtils;
//import android.text.TextWatcher;
//import android.text.style.ForegroundColorSpan;
//import android.util.Log;
//import android.view.Gravity;
//import android.view.KeyEvent;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.view.View.OnFocusChangeListener;
//import android.view.Window;
//import android.view.inputmethod.EditorInfo;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.ProgressBar;
//import android.widget.RelativeLayout;
//import android.widget.TextView;
//import android.widget.TextView.OnEditorActionListener;
//
//import com.google.android.gms.common.ConnectionResult;
//import com.google.android.gms.common.GooglePlayServicesUtil;
//import com.hirecraft.jugunoo.passenger.common.Constant;
//import com.hirecraft.jugunoo.passenger.common.ConstantMessages;
//import com.hirecraft.jugunoo.passenger.common.Function;
//import com.hirecraft.jugunoo.passenger.common.Global;
//import com.hirecraft.jugunoo.passenger.common.TransparentProgressDialog;
//import com.hirecraft.jugunoo.passenger.common.Validation;
//import com.hirecraft.jugunoo.passenger.gcm.GCMRegistrationHelper;
//import com.hirecraft.jugunoo.passenger.networkhandler.NetworkHandler;
//import com.hirecraft.jugunoo.passenger.services.VolleyErrorHelper;
//import com.hirecraft.jugunoo.passenger.utility.JugunooUtil;
//import com.hirecraft.jugunoo.passenger.utility.SharedPreferencesManager;
//import com.loopj.android.http.AsyncHttpClient;
//import com.loopj.android.http.AsyncHttpResponseHandler;
//import com.loopj.android.http.RequestParams;
//
//public class Registration extends Activity implements OnClickListener,
//		OnEditorActionListener
//{
//
//	private static final String TAG = Registration.class.getSimpleName();
//
//	private EditText etUsername, etPassword, etCpassword, etEmail, etMobile,
//			etPin;
//	private String usernameStr = "", passwordStr = "", cpasswordStr,
//			emailStr = "", mobileStr = "", deviceIDStr = "", gcmIDStr = "",
//			countryCodeStr = "";
//
//	private TextView usernameValidate, emailValidate, mobileValidate,
//			resendMsg, passwordValidate, cpasswordValidate, title;
//
//	private LinearLayout verificationLayout, resendLayout;
//	private Button verify;
//	// private Button proceed, resendOtp;
//	private ImageView getCountry;
//	private ProgressBar emailProgress, mobileProgress;
//	private static TransparentProgressDialog pd;
//	private static Handler pdHandler;
//	private static Runnable pdRunnable;
//	private GCMRegistrationHelper gcmRegistrationHelper;
//	private SharedPreferencesManager mgr;
//
//	boolean isUsername = false, isPasswrd = false, isCpasswrd = false,
//			isEmail = false;
//	Typeface light, bold, semibold;
//
//	private final int JUGUNOO_EMAIL_CHECK = 11;
//	private final int JUGUNOO_MOBILE_CHECK = 12;
//	private final int JUGUNOO_DELAY_IN_MILLIS = 1000;
//
//	AsyncHttpClient reqisterClient = null;
//
//	private boolean isEmailExist, isMobileExist;
//
//	private boolean isSmsRegistered;
//
//	// timer
//	// private Timer timer = new Timer();
//
//	private Handler smsHandler = new Handler();
//	// time in millis 1 min
//	private int SMS_TIME_OUT_VAL = 30000;
//
//	// sms dialogue
//	private Dialog smsDialog;
//	private TextView tvDiaMsg;
//	private ProgressBar pbDialogue;
//	private Button btnTryAgain;
//	private String otpValue;
//	private String availabilityField;
//
//	private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
//
//	@SuppressLint("InflateParams")
//	@Override
//	protected void onCreate(Bundle savedInstanceState)
//	{
//		super.onCreate(savedInstanceState);
//
//		SetActionBar();
//
//		// ActionBar actionBar = getActionBar();
//		// actionBar.setDisplayUseLogoEnabled(true);
//		// actionBar.setDisplayShowCustomEnabled(true);
//		//
//		// LayoutParams jugunooActionbar = new LayoutParams(
//		// LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
//		// jugunooActionbar.setMargins(0, 0, 0, 0);
//		//
//		// LayoutInflater inflater = (LayoutInflater)
//		// getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//		// RelativeLayout lay = new RelativeLayout(this);
//		// View jugunooTitleBar = inflater
//		// .inflate(R.layout.jugunoo_actionbar, lay);
//		// ImageView img = (ImageView)
//		// jugunooTitleBar.findViewById(R.id.followme);
//		// img.setVisibility(View.GONE);
//		// actionBar.setCustomView(jugunooTitleBar, jugunooActionbar);
//
//		setContentView(R.layout.activity_passenger_registration);
//
//		light = Typeface.createFromAsset(getAssets(),
//				"fonts/opensans-regular-webfont.ttf");
//		bold = Typeface.createFromAsset(getAssets(),
//				"fonts/opensans-bold-webfont.ttf");
//		semibold = Typeface.createFromAsset(getAssets(),
//				"fonts/opensans-semibold-webfont.ttf");
//
//		verificationLayout = (LinearLayout) findViewById(R.id.verifyLayout);
//		resendLayout = (LinearLayout) findViewById(R.id.resendLayout);
//		resendLayout.setVisibility(View.VISIBLE);
//		gcmRegistrationHelper = new GCMRegistrationHelper(Registration.this);
//		mgr = new SharedPreferencesManager(Registration.this);
//
//		etUsername = (EditText) findViewById(R.id.regUserName);
//		etPassword = (EditText) findViewById(R.id.regPassword);
//		etCpassword = (EditText) findViewById(R.id.regCPassword);
//
//		etEmail = (EditText) findViewById(R.id.regEmail);
//		etMobile = (EditText) findViewById(R.id.regMobile);
//		etPin = (EditText) findViewById(R.id.pin);
//
//		verify = (Button) findViewById(R.id.verifyAndRegister);
//
//		// resendOtp = (Button) findViewById(R.id.resendCode);
//		// proceed = (Button) findViewById(R.id.proceed);
//
//		verificationLayout = (LinearLayout) findViewById(R.id.verifyLayout);
//		usernameValidate = (TextView) findViewById(R.id.isUsernameValid);
//		title = (TextView) findViewById(R.id.textView1);
//		mobileValidate = (TextView) findViewById(R.id.isMobileValid);
//		emailValidate = (TextView) findViewById(R.id.isEmailValid);
//		emailValidate.setTextColor(Color.rgb(231, 39, 2));
//		resendMsg = (TextView) findViewById(R.id.resendMsg);
//		passwordValidate = (TextView) findViewById(R.id.isPasswordValid);
//		cpasswordValidate = (TextView) findViewById(R.id.isCPasswordValid);
//
//		emailProgress = (ProgressBar) findViewById(R.id.emailProgress);
//		mobileProgress = (ProgressBar) findViewById(R.id.mobileProgress);
//
//		getCountry = (ImageView) findViewById(R.id.getCountry);
//		// mobile.setOnEditorActionListener(this);
//		// getCountry.setOnClickListener(this);
//		verify.setOnClickListener(this);
//		// resendOtp.setOnClickListener(this);
//		// proceed.setOnClickListener(this);
//
//		etUsername.addTextChangedListener(usernameWatcher);
//		etPassword.addTextChangedListener(passwordWatcher);
//		etCpassword.addTextChangedListener(cpasswordWatcher);
//		etEmail.addTextChangedListener(emailWatcher);
//		etMobile.addTextChangedListener(mobileWatcher);
//		// etMobile.setOnClickListener(mobileClickListener);
//
//		etMobile.setOnFocusChangeListener(new OnFocusChangeListener()
//		{
//			@Override
//			public void onFocusChange(View v, boolean hasFocus)
//			{
//				// etMobile.setError("Enter the current device mobile number");
//			}
//		});
//
//		// pin.addTextChangedListener(pinWatcher);
//
//		Init();
//
//		// sms register
//		// registerSmsReceiver();
//
//		if (checkPlayServices())
//		{
//			registerInBackground();
//		}
//
//	}
//
//	@Override
//	protected void onResume()
//	{
//		super.onResume();
//		checkPlayServices();
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
//		RelativeLayout l = new RelativeLayout(this);
//		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//		View bar = inflater.inflate(R.layout.custom_title_actionbar, l);
//
//		LinearLayout back = (LinearLayout) bar.findViewById(R.id.llCTitle);
//		TextView title = (TextView) bar.findViewById(R.id.tvTATitle);
//		title.setText("Sign Up");
//
//		back.setOnClickListener(new OnClickListener()
//		{
//			@Override
//			public void onClick(View v)
//			{
//				onBackPressed();
//			}
//		});
//
//		actionBar.setCustomView(bar, layoutParams);
//		actionBar.setDisplayHomeAsUpEnabled(true);
//		actionBar.setDisplayShowCustomEnabled(true);
//		actionBar.setHomeButtonEnabled(false);
//	}
//
//	private boolean checkPlayServices()
//	{
//		int resultCode = GooglePlayServicesUtil
//				.isGooglePlayServicesAvailable(this);
//		if (resultCode != ConnectionResult.SUCCESS)
//		{
//			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode))
//			{
//				GooglePlayServicesUtil.getErrorDialog(resultCode, this,
//						PLAY_SERVICES_RESOLUTION_REQUEST).show();
//			}
//			else
//			{
//				Log.i(TAG, TAG + " This device is not supported.");
//				finish();
//			}
//			return false;
//		}
//		return true;
//	}
//
//	private void registerSmsReceiver()
//	{
//
//		Log.i(TAG, TAG + " registerSmsReceiver");
//
//		// sms register
//
//		IntentFilter mIntentFilter = new IntentFilter();
//		mIntentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
//
//		registerReceiver(smsReceiver, mIntentFilter);
//
//		isSmsRegistered = true;
//	}
//
//	private void Init()
//	{
//
//		etUsername.setTypeface(light);
//		etPassword.setTypeface(light);
//		etCpassword.setTypeface(light);
//		etEmail.setTypeface(light);
//		etMobile.setTypeface(light);
//
//		etPin.setTypeface(light);
//
//		usernameValidate.setTypeface(light);
//		mobileValidate.setTypeface(light);
//		emailValidate.setTypeface(light);
//		resendMsg.setTypeface(light);
//		passwordValidate.setTypeface(light);
//		cpasswordValidate.setTypeface(light);
//
//		usernameValidate.setVisibility(View.GONE);
//		mobileValidate.setVisibility(View.GONE);
//		emailValidate.setVisibility(View.GONE);
//		resendMsg.setVisibility(View.GONE);
//		passwordValidate.setVisibility(View.GONE);
//		cpasswordValidate.setVisibility(View.GONE);
//
//		String RegisterStatus = mgr.GetValueFromSharedPrefs("RegisterStatus");
//		if (RegisterStatus.equalsIgnoreCase("WaitForOtp"))
//		{
//			String usern = mgr.GetValueFromSharedPrefs("username");
//			String passwo = mgr.GetValueFromSharedPrefs("password");
//			String emai = mgr.GetValueFromSharedPrefs("email");
//			String mobil = mgr.GetValueFromSharedPrefs("mobile");
//
//			etUsername.setText(usern);
//			etPassword.setText(passwo);
//			etCpassword.setText(passwo);
//			etEmail.setText(emai);
//			etMobile.setText(mobil);
//			verificationLayout.setVisibility(View.GONE);
//		}
//		else
//		{
//			verificationLayout.setVisibility(View.GONE);
//		}
//
//	}
//
//	private void clearFocus()
//	{
//		etUsername.clearFocus();
//		etPassword.clearFocus();
//		etCpassword.clearFocus();
//		etPin.clearFocus();
//		etEmail.clearFocus();
//		etMobile.clearFocus();
//	}
//
//	@Override
//	public void onBackPressed()
//	{
//		super.onBackPressed();
//		Registration.this.finish();
//		startActivity(new Intent().setClass(Registration.this,
//				SplashScreen.class));
//	}
//
//	private void showLoadingDilog()
//	{
//		// Showing loading dialog
//
//		pdHandler = new Handler();
//		pd = new TransparentProgressDialog(Registration.this,
//				R.drawable.loading_image);
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
//	}
//
//	private void cancelLoadingDialog()
//	{
//		// Cancel loading dialog
//
//		pdHandler.removeCallbacks(pdRunnable);
//
//		if (pd.isShowing())
//		{
//			pd.dismiss();
//		}
//	}
//
//	Handler handler = new Handler()
//	{
//		@Override
//		public void handleMessage(Message msg)
//		{
//
//			if (msg.what == JUGUNOO_EMAIL_CHECK)
//			{
//
//				if (JugunooUtil.isConnectedToInternet(Registration.this))
//				{
//
//					// isEmailExist = true;
//
//					String emailStr = etEmail.getText().toString();
//
//					if (!TextUtils.isEmpty(emailStr)
//							&& Validation.isValidE(etEmail,
//									Validation.EMAIL_REGEX,
//									"Enter a valid Email ID."))
//					{
//
//						String url = Global.JUGUNOO_WS
//								+ "Passenger/ValidateParameter?CheckValue="
//								+ emailStr.toString() + "&Parameter=Email";
//
//						Log.d("JUGUNOO_EMAIL_CHECK", "JUGUNOO_EMAIL_CHECK "
//								+ url);
//
//						// CheckAvailability(url, "Email");
//						makeAvailabilityRequest(url, "Email");
//
//					}
//					else if (!TextUtils.isEmpty(emailStr))
//					{
//
//						if (!Validation.isValidE(etEmail,
//								Validation.EMAIL_REGEX,
//								"Enter a valid Email ID."))
//						{
//							isEmailExist = true;
//						}
//						else
//						{
//							isEmailExist = false;
//						}
//
//					}
//					else if (TextUtils.isEmpty(emailStr))
//					{
//						isEmailExist = false;
//					}
//
//				}
//				else
//				{
//					Function.showToast(Registration.this,
//							Global.networkErrorMsg);
//				}
//
//			}
//
//			if (msg.what == JUGUNOO_MOBILE_CHECK)
//			{
//
//				if (JugunooUtil.isConnectedToInternet(Registration.this))
//				{
//
//					isMobileExist = false;
//
//					String mobileStr = etMobile.getText().toString();
//
//					Log.i("reg", "mob len=" + mobileStr.length());
//
//					if (mobileStr.length() == 10 && checkValidation())
//					{
//						String url = Global.JUGUNOO_WS
//								+ "Passenger/ValidateParameter?CheckValue="
//								+ mobileStr.toString() + "&Parameter=Mobile";
//
//						Log.d("JUGUNOO_MOBILE_CHECK", "JUGUNOO_MOBILE_CHECK "
//								+ url);
//
//						// CheckAvailability(url, "Mobile");
//						makeAvailabilityRequest(url, "Mobile");
//					}
//				}
//				else
//				{
//					VolleyErrorHelper.getMessage(msg.obj, Registration.this,
//							true);
//				}
//
//			}
//
//		};
//	};
//
//	@Override
//	protected void onStart()
//	{
//		super.onStart();
//
//		etMobile.setOnEditorActionListener(new EditText.OnEditorActionListener()
//		{
//
//			@Override
//			public boolean onEditorAction(TextView v, int actionId,
//					KeyEvent event)
//			{
//
//				Log.i("mobile", "mobile edit");
//
//				if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER))
//						|| (actionId == EditorInfo.IME_ACTION_DONE))
//				{
//					String mobileStr = etMobile.getText().toString();
//
//					if (mobileStr.length() != 10)
//					{
//						setTextColor(ConstantMessages.MSG3, "Mobile");
//					}
//
//					else
//					{
//
//						if (JugunooUtil
//								.isConnectedToInternet(Registration.this))
//						{
//							register();
//						}
//						else
//						{
//							Function.showToast(Registration.this,
//									ConstantMessages.MSG6);
//						}
//
//						// if (mobileStr.length() == 10 && checkValidation()) {
//						// String url = Global.JUGUNOO_WS
//						// + "Passenger/ValidateParameter?CheckValue="
//						// + mobileStr.toString()
//						// + "&Parameter=Mobile";
//						// CheckAvailability(url, "Mobile");
//						// }
//					}
//				}
//				return false;
//			}
//		});
//
//		// pin.setOnEditorActionListener(new EditText.OnEditorActionListener() {
//		//
//		// @Override
//		// public boolean onEditorAction(TextView v, int actionId,
//		// KeyEvent event) {
//		//
//		// if (actionId == EditorInfo.IME_ACTION_DONE) {
//		// verifyParams("");
//		// }
//		//
//		// return false;
//		// }
//		// });
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
//	private TextWatcher usernameWatcher = new TextWatcher()
//	{
//
//		@Override
//		public void onTextChanged(CharSequence s, int start, int before,
//				int count)
//		{
//			// Log.i("user", "user onTextChanged");
//			// usernameValidate.setVisibility(View.GONE);
//			// username.setError(null);
//
//		}
//
//		@Override
//		public void beforeTextChanged(CharSequence s, int start, int count,
//				int after)
//		{
//
//		}
//
//		@Override
//		public void afterTextChanged(Editable s)
//		{
//			Log.i("user", "user afterTextChanged");
//			clearvalidationTexts();
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
//			passwordValidate.setVisibility(View.GONE);
//			etPassword.setError(null);
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
//			if (isUsername == true)
//			{
//				etCpassword.setEnabled(true);
//
//			}
//			clearvalidationTexts();
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
//			cpasswordValidate.setVisibility(View.GONE);
//			etCpassword.setError(null);
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
//			clearvalidationTexts();
//		}
//	};
//
//	private TextWatcher mobileWatcher = new TextWatcher()
//	{
//
//		@Override
//		public void onTextChanged(CharSequence s, int start, int before,
//				int count)
//		{
//			// mobileValidate.setVisibility(View.GONE);
//			// mobile.setError(null);
//		}
//
//		@Override
//		public void beforeTextChanged(CharSequence s, int start, int count,
//				int after)
//		{
//
//		}
//
//		@Override
//		public void afterTextChanged(Editable s)
//		{
//			// handler.removeMessages(JUGUNOO_MOBILE_CHECK);
//			// handler.sendEmptyMessageDelayed(JUGUNOO_MOBILE_CHECK,
//			// JUGUNOO_DELAY_IN_MILLIS);
//			clearvalidationTexts();
//
//			if (s.length() == 3)
//			{
//				etMobile.setError(ConstantMessages.MSG8);
//			}
//
//		}
//	};
//
//	private TextWatcher emailWatcher = new TextWatcher()
//	{
//
//		@Override
//		public void onTextChanged(CharSequence s, int start, int before,
//				int count)
//		{
//			emailValidate.setVisibility(View.GONE);
//			etEmail.setError(null);
//
//		}
//
//		@Override
//		public void beforeTextChanged(CharSequence s, int start, int count,
//				int after)
//		{
//
//		}
//
//		@Override
//		public void afterTextChanged(Editable s)
//		{
//
//			emailStr = etEmail.getText().toString().trim();
//			clearvalidationTexts();
//			if (!emailStr.isEmpty())
//			{
//				handler.removeMessages(JUGUNOO_EMAIL_CHECK);
//				handler.sendEmptyMessageDelayed(JUGUNOO_EMAIL_CHECK,
//						JUGUNOO_DELAY_IN_MILLIS);
//			}
//			else
//			{
//				isEmailExist = false;
//			}
//
//		}
//	};
//
//	// private TextWatcher pinWatcher = new TextWatcher() {
//	//
//	// @Override
//	// public void onTextChanged(CharSequence s, int start, int before,
//	// int count) {
//	//
//	// }
//	//
//	// @Override
//	// public void beforeTextChanged(CharSequence s, int start, int count,
//	// int after) {
//	//
//	// }
//	//
//	// @Override
//	// public void afterTextChanged(Editable s) {
//	//
//	// Validation.hasText(pin);
//	// }
//	// };
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
//	private void CheckAvailability(String URL, final String field)
//	{
//		if (field.equalsIgnoreCase("Email"))
//			emailProgress.setVisibility(View.VISIBLE);
//		else if (field.equalsIgnoreCase("Mobile"))
//			mobileProgress.setVisibility(View.VISIBLE);
//
//		AsyncHttpClient client = new AsyncHttpClient();
//
//		client.get(URL, new AsyncHttpResponseHandler()
//		{
//
//			@Override
//			@Deprecated
//			public void onFailure(int statusCode, Throwable error,
//					String content)
//			{
//
//				JugunooInteractiveDialog("NETWORK",
//						getString(R.string.network_error_msg));
//
//				super.onFailure(statusCode, error, content);
//				if (field.equalsIgnoreCase("Email"))
//					emailProgress.setVisibility(View.GONE);
//				else if (field.equalsIgnoreCase("Mobile"))
//				{
//					mobileProgress.setVisibility(View.GONE);
//				}
//			}
//
//			@Override
//			@Deprecated
//			public void onSuccess(int statusCode, String content)
//			{
//				if (statusCode == 200)
//				{
//					try
//					{
//
//						Log.i("email valid", content);
//
//						JSONObject obj = new JSONObject(content);
//						String result = obj.getString("Result");
//						if (result.equalsIgnoreCase("Pass"))
//						{
//
//							if (field.equalsIgnoreCase("Email"))
//							{
//								isEmail = true;
//								isEmailExist = false;
//								setTextColor("Available", field);
//								emailValidate.setVisibility(View.GONE);
//							}
//							else if (field.equalsIgnoreCase("Mobile"))
//							{
//								setTextColor("Available", field);
//								isMobileExist = false;
//								mobileValidate.setVisibility(View.GONE);
//							}
//
//						}
//						else
//						{
//							if (field.equalsIgnoreCase("Email"))
//							{
//								setTextColor("Already Exists", field);
//								isEmailExist = true;
//							}
//							else if (field.equalsIgnoreCase("Mobile"))
//							{
//								setTextColor("Already Exists", field);
//								isMobileExist = true;
//							}
//
//						}
//					}
//					catch (JSONException e)
//					{
//
//						e.printStackTrace();
//					}
//
//				}
//				else
//				{
//					inVisibleSegmentOne();
//
//				}
//
//				super.onSuccess(statusCode, content);
//
//				if (field.equalsIgnoreCase("Email"))
//				{
//
//					emailProgress.setVisibility(View.GONE);
//
//				}
//				else if (field.equalsIgnoreCase("Mobile"))
//				{
//					mobileProgress.setVisibility(View.GONE);
//
//				}
//			}
//		});
//
//	}
//
//	private void makeAvailabilityRequest(String url, String field)
//	{
//		if (field.equalsIgnoreCase("Email"))
//			emailProgress.setVisibility(View.VISIBLE);
//		else if (field.equalsIgnoreCase("Mobile"))
//			mobileProgress.setVisibility(View.VISIBLE);
//
//		availabilityField = field;
//		Map<String, String> params = new HashMap<String, String>();
//		NetworkHandler.mailAvailabilityRequest(TAG, handlerAvailability,
//				params, url);
//	}
//
//	Handler handlerAvailability = new Handler()
//	{
//		public void handleMessage(Message msg)
//		{
//
//			switch (msg.arg1)
//			{
//				case Constant.MessageState.AVAILABILITY_MAIL_NUMBER_SUCCESS:
//
//					parserAvailability((JSONObject) msg.obj);
//
//					if (availabilityField.equalsIgnoreCase("Email"))
//					{
//						emailProgress.setVisibility(View.GONE);
//					}
//					else if (availabilityField.equalsIgnoreCase("Mobile"))
//					{
//						mobileProgress.setVisibility(View.GONE);
//					}
//					break;
//
//				case Constant.MessageState.FAIL:
//					VolleyErrorHelper.getMessage(msg.obj, Registration.this,
//							true);
//
//					if (availabilityField.equalsIgnoreCase("Email"))
//					{
//						emailProgress.setVisibility(View.GONE);
//					}
//					else if (availabilityField.equalsIgnoreCase("Mobile"))
//					{
//						mobileProgress.setVisibility(View.GONE);
//					}
//					break;
//
//				default:
//					break;
//			}
//
//		};
//	};
//
//	private void parserAvailability(JSONObject obj)
//	{
//		try
//		{
//			String result = obj.getString("Result");
//			if (result.equalsIgnoreCase("Pass"))
//			{
//				if (availabilityField.equalsIgnoreCase("Email"))
//				{
//					isEmail = true;
//					isEmailExist = false;
//					setTextColor("Available", availabilityField);
//					emailValidate.setVisibility(View.GONE);
//				}
//				else if (availabilityField.equalsIgnoreCase("Mobile"))
//				{
//					setTextColor("Available", availabilityField);
//					isMobileExist = false;
//					mobileValidate.setVisibility(View.GONE);
//				}
//
//			}
//			else
//			{
//				if (availabilityField.equalsIgnoreCase("Email"))
//				{
//					// setTextColor("Already Exists", availabilityField);
//					emailValidate.setText(obj.getString("Message"));
//					isEmailExist = true;
//					emailValidate.setVisibility(View.VISIBLE);
//				}
//				else if (availabilityField.equalsIgnoreCase("Mobile"))
//				{
//					// setTextColor("Already Exists", availabilityField);
//					mobileValidate.setText(obj.getString("Message"));
//					isMobileExist = true;
//					mobileValidate.setVisibility(View.VISIBLE);
//
//				}
//
//			}
//		}
//		catch (Exception e)
//		{
//			e.printStackTrace();
//		}
//
//	}
//
//	private void inVisibleSegmentOne()
//	{
//		getCountry.setVisibility(View.GONE);
//	}
//
//	private void setTextColor(String text, String field)
//	{
//
//		if (!text.equalsIgnoreCase(""))
//		{
//			Spannable strength = new SpannableString(text);
//			if (text.equalsIgnoreCase(ConstantMessages.MSG9)
//					|| text.equalsIgnoreCase(ConstantMessages.MSG10)
//					|| text.equalsIgnoreCase(ConstantMessages.MSG11)
//					|| text.equalsIgnoreCase(ConstantMessages.MSG12)
//					|| text.equalsIgnoreCase(ConstantMessages.MSG13)
//					|| text.equalsIgnoreCase(ConstantMessages.MSG14)
//					|| text.equalsIgnoreCase(ConstantMessages.MSG15)
//					|| text.equalsIgnoreCase(ConstantMessages.MSG16)
//					|| text.equalsIgnoreCase(ConstantMessages.MSG17)
//					|| text.equalsIgnoreCase(ConstantMessages.MSG18)
//					|| text.equalsIgnoreCase(ConstantMessages.MSG19)
//					|| text.equals(ConstantMessages.MSG20)
//					|| text.equalsIgnoreCase(ConstantMessages.MSG21))
//				strength.setSpan(
//						new ForegroundColorSpan(Color.rgb(231, 39, 2)), 0,
//						text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//			else
//				strength.setSpan(
//						new ForegroundColorSpan(Color.rgb(40, 183, 9)), 0,
//						text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//
//			if (field.equalsIgnoreCase("UserName"))
//			{
//				usernameValidate.setVisibility(View.VISIBLE);
//				usernameValidate.setText(strength);
//			}
//			else if (field.equalsIgnoreCase("Email"))
//			{
//				emailValidate.setVisibility(View.VISIBLE);
//				emailValidate.setText(strength);
//			}
//			else if (field.equalsIgnoreCase("Mobile"))
//			{
//				mobileValidate.setVisibility(View.VISIBLE);
//				mobileValidate.setText(strength);
//			}
//			else if (field.equalsIgnoreCase("Password"))
//			{
//				passwordValidate.setVisibility(View.VISIBLE);
//				passwordValidate.setText(strength);
//			}
//			else if (field.equalsIgnoreCase("cPassword"))
//			{
//				cpasswordValidate.setVisibility(View.VISIBLE);
//				cpasswordValidate.setText(strength);
//			}
//		}
//
//	}
//
//	@Override
//	public void onClick(View v)
//	{
//
//		switch (v.getId())
//		{
//
//			case R.id.verifyAndRegister:
//
//				clearFocus();
//				clearvalidationTexts();
//				if (JugunooUtil.isConnectedToInternet(Registration.this))
//				{
//					register();
//				}
//				else
//				{
//					Function.showToast(Registration.this, ConstantMessages.MSG6);
//				}
//
//				break;
//
//			case R.id.proceed:
//
//				clearFocus();
//				clearvalidationTexts();
//
//				if (JugunooUtil.isConnectedToInternet(Registration.this))
//				{
//					verifyParams("");
//				}
//				else
//				{
//					Function.showToast(Registration.this, ConstantMessages.MSG6);
//				}
//
//				break;
//
//			case R.id.resendCode:
//
//				clearFocus();
//
//				if (JugunooUtil.isConnectedToInternet(Registration.this))
//				{
//					requestKeyParams();
//				}
//				else
//				{
//					Function.showToast(Registration.this, ConstantMessages.MSG6);
//				}
//
//				break;
//
//		}
//
//	}
//
//	private void clearvalidationTexts()
//	{
//		usernameValidate.setVisibility(View.GONE);
//		emailValidate.setVisibility(View.GONE);
//		mobileValidate.setVisibility(View.GONE);
//		passwordValidate.setVisibility(View.GONE);
//		cpasswordValidate.setVisibility(View.GONE);
//
//	}
//
//	private void register()
//	{
//
//		// String emailvalidation = Validation.EMAIL_REGEX;
//		// if (email.getText().toString().matches(emailvalidation))
//
//		registerSmsReceiver();
//
//		JugunooUtil util = new JugunooUtil(Registration.this);
//
//		String deviceID = util.getUniqueDeviceID(getApplicationContext());
//
//		gcmIDStr = mgr.GetValueFromSharedPrefs("GCM_ID");
//		boolean isSuccessValidation = checkValidation();
//		boolean isSuccessGcm = IsGcmSuccess();
//
//		Log.i(TAG, TAG + " reg==" + isSuccessValidation + " " + isSuccessGcm
//				+ " " + usernameStr + " " + passwordStr + " " + mobileStr + " "
//				+ emailStr + " " + deviceID + " " + gcmIDStr);
//
//		if (isSuccessValidation)
//		{
//			if (isSuccessGcm)
//			{
//
//				countryCodeStr = "+91";
//
//				mobileProgress.setVisibility(View.GONE);
//				mgr.SaveValueToSharedPrefs("UserName", usernameStr);
//				mgr.SaveValueToSharedPrefs("Mobile", mobileStr);
//				mgr.SaveValueToSharedPrefs("Email", emailStr);
//
//				HashMap<String, String> params = new HashMap<String, String>();
//				// RequestParams params = new RequestParams();
//
//				params.put("FirstName", usernameStr);
//				params.put("Password", passwordStr);
//				params.put("Mobile", mobileStr);
//				params.put("Country", countryCodeStr);
//				params.put("Email", emailStr);
//				params.put("DeviceId", deviceID);
//				params.put("gcmRegistrationID", gcmIDStr);
//
//				// Register(params);
//				makeRegistrationReq(params);
//
//			}
//			else
//			{
//				Log.i(Global.APPTAG, "GCM ID is null.");
//				Function.showToast(Registration.this, ConstantMessages.MSG22);
//
//			}
//		}
//		else
//		{
//
//		}
//	}
//
//	private boolean checkValidation()
//	{
//		boolean flag = true, cancel = false;
//
//		mobileStr = etMobile.getText().toString().trim();
//
//		if (!Validation.hasText(etMobile))
//		{
//			flag = false;
//			cancel = true;
//		}
//		else if (mobileStr.length() != 10)
//		{
//			flag = false;
//			cancel = true;
//			setTextColor(ConstantMessages.MSG3, "Mobile");
//		}
//		else
//		{
//			flag = true;
//		}
//
//		usernameStr = etUsername.getText().toString().trim();
//
//		if (!Validation.hasText(etUsername))
//		{
//			flag = false;
//			cancel = true;
//		}
//		else if (usernameStr.length() < 3)
//		{
//			flag = false;
//			cancel = true;
//			isUsername = false;
//			setTextColor(ConstantMessages.MSG10, "UserName");
//		}
//		else
//		{
//			flag = true;
//			isUsername = true;
//			usernameStr = etUsername.getText().toString();
//		}
//		Log.i("usernameStr", flag + "");
//
//		passwordStr = etPassword.getText().toString();
//
//		if (!Validation.hasText(etPassword))
//		{
//			flag = false;
//			cancel = true;
//		}
//		else if (passwordStr.length() < 3)
//		{
//			isPasswrd = false;
//			flag = false;
//			cancel = true;
//			setTextColor(ConstantMessages.MSG20, "Password");
//		}
//		else
//		{
//			flag = true;
//			isPasswrd = true;
//			passwordStr = etPassword.getText().toString();
//		}
//
//		Log.i("passwordStr", flag + "");
//		cpasswordStr = etCpassword.getText().toString();
//
//		if (!Validation.hasText(etCpassword))
//		{
//			flag = false;
//			cancel = true;
//		}
//		else if (!cpasswordStr.equals(passwordStr))
//		{
//			flag = false;
//			cancel = true;
//			isCpasswrd = false;
//			setTextColor(ConstantMessages.MSG12, "cPassword");
//		}
//		else
//		{
//			flag = true;
//			isCpasswrd = true;
//			cpasswordStr = etCpassword.getText().toString();
//		}
//
//		Log.i("Confirm Password Match", flag + "");
//
//		String emailstr = etEmail.getText().toString();
//		String emailvalidation = Validation.EMAIL_REGEX;
//
//		if (!emailstr.isEmpty() && !emailstr.matches(emailvalidation))
//		{
//			flag = false;
//			cancel = true;
//			// email.setError("Enter a valid Email ID.");
//			setTextColor(ConstantMessages.MSG14, "Email");
//		}
//
//		if (emailstr.matches(emailvalidation) && isEmailExist)
//		{
//			Log.i("isEmailExist", TAG + " isEmailExist " + isEmailExist);
//			setTextColor(ConstantMessages.MSG15, "Email");
//			cancel = true;
//		}
//		else
//		{
//			flag = true;
//			isEmail = true;
//			emailStr = etEmail.getText().toString();
//		}
//
//		if (isMobileExist)
//		{
//			Log.i("isMobileExist", "isMobileExist " + isMobileExist);
//			setTextColor(ConstantMessages.MSG23, "Mobile");
//			cancel = true;
//		}
//
//		if (cancel)
//		{
//			Log.i("cv", "valid=" + false);
//			return false;
//		}
//		else
//		{
//			Log.i("cv", "valid=" + true);
//			return true;
//		}
//
//	}
//
//	private boolean IsGcmSuccess()
//	{
//
//		gcmIDStr = mgr.GetValueFromSharedPrefs("GCM_ID");
//		if (gcmIDStr.equalsIgnoreCase(""))
//		{
//			return false;
//		}
//		else
//		{
//			return true;
//		}
//	}
//
//	private void registerInBackground()
//	{
//
//		JugunooUtil util = new JugunooUtil(Registration.this);
//		deviceIDStr = util.getUniqueDeviceID(getApplicationContext());
//
//		Global.DEVICE_ID = deviceIDStr;
//
//		new AsyncTask<Void, Void, String>()
//		{
//			@Override
//			protected String doInBackground(Void... params)
//			{
//				String msg = "";
//				try
//				{
//					String gcmIDStr = "";
//					String diviceID = Global.DEVICE_ID;
//					String userID = mgr.GetValueFromSharedPrefs("UserID");
//
//					SharedPreferencesManager manager;
//					manager = new SharedPreferencesManager(
//							getApplicationContext());
//					gcmIDStr = manager.GetValueFromSharedPrefs("GCM_ID");
//
//					Log.e("gcm", "gcm=" + gcmIDStr);
//
//					if (gcmIDStr.equalsIgnoreCase(""))
//					{
//						String gcmRegID = gcmRegistrationHelper.Register(
//								diviceID, userID);
//						manager.SaveValueToSharedPrefs("GCM_ID", gcmRegID);
//						Global.GCMID = gcmRegID;
//						Log.i(Global.APPTAG, "GCM:" + gcmRegID);
//					}
//					else
//					{
//						Global.GCMID = gcmIDStr;
//						Log.i(Global.APPTAG, "GCM:" + Global.GCMID);
//					}
//
//				}
//				catch (Exception bug)
//				{
//					bug.printStackTrace();
//					for (StackTraceElement gcmErrElement : bug.getStackTrace())
//					{
//						// sendNotification("GCM Error:"
//						// + gcmErrElement.getClassName());
//					}
//				}
//				return msg;
//			}
//
//			@Override
//			protected void onPostExecute(String msg)
//			{
//
//			}
//		}.execute(null, null, null);
//	}
//
//	private void Register(RequestParams params)
//	{
//
//		Log.i("reg", "Register");
//
//		if (reqisterClient == null)
//		{
//
//			mgr.SaveValueToSharedPrefs("RegisterStatus", "AttemptToRegister");
//
//			pdHandler = new Handler();
//			pd = new TransparentProgressDialog(Registration.this,
//					R.drawable.loading_image);
//
//			pdRunnable = new Runnable()
//			{
//				@Override
//				public void run()
//				{
//					if (pd != null)
//					{
//						if (pd.isShowing())
//						{
//							pd.dismiss();
//						}
//					}
//				}
//			};
//			pd.show();
//
//			reqisterClient = new AsyncHttpClient();
//			reqisterClient.setTimeout(30000);
//			reqisterClient.post(Global.JUGUNOO_WS + "Passenger/Registration?",
//					params, new AsyncHttpResponseHandler()
//					{
//
//						@Override
//						@Deprecated
//						public void onFailure(int statusCode, Throwable error,
//								String content)
//						{
//							try
//							{
//								Function.showToast(Registration.this,
//										Global.networkErrorMsg);
//							}
//							catch (Exception bug)
//							{
//								bug.printStackTrace();
//							}
//							finally
//							{
//
//								pdHandler.removeCallbacks(pdRunnable);
//								if (pd.isShowing())
//								{
//									pd.dismiss();
//								}
//
//								reqisterClient = null;
//							}
//							super.onFailure(statusCode, error, content);
//
//						}
//
//						@Override
//						@Deprecated
//						public void onSuccess(int statusCode, String content)
//						{
//							try
//							{
//								if (statusCode == 200)
//								{
//
//									Log.i("reg", "reee=" + content);
//
//									JSONObject regObj = new JSONObject(content);
//									String result = regObj
//											.getString(Constant.RESULT);
//
//									if (result
//											.equalsIgnoreCase(Constant.Result_STATE.PASS))
//									{
//
//										String userID = regObj
//												.getString("UserId");
//
//										if (regObj.has("otp"))
//										{
//											otpValue = regObj.getString("otp");
//										}
//
//										// showDialog("Verification code sent to your Mobile Number.");
//										verificationLayout
//												.setVisibility(View.GONE);
//										// isWaitForOTP();
//										mgr.SaveValueToSharedPrefs("UserID",
//												userID);
//										mgr.SaveValueToSharedPrefs("Verify",
//												"Pass");
//
//										setCustomDialogue();
//
//										// mobileValidate
//										// .setText("Verification Time expired. Tap on <VERIFY> button to try again.");
//
//									}
//									else
//									{
//										Function.showToast(Registration.this,
//												"Mobile number already exist");
//									}
//								}
//								else
//								{
//									Function.showToast(Registration.this,
//											"Registration failed. Try again");
//								}
//
//							}
//							catch (Exception bug)
//							{
//								bug.printStackTrace();
//							}
//							finally
//							{
//
//								pdHandler.removeCallbacks(pdRunnable);
//								if (pd.isShowing())
//								{
//									pd.dismiss();
//								}
//
//								reqisterClient = null;
//							}
//							super.onSuccess(statusCode, content);
//
//						}
//
//					});
//
//		}
//	}
//
//	private void makeRegistrationReq(HashMap<String, String> params)
//	{
//		mgr.SaveValueToSharedPrefs("RegisterStatus", "AttemptToRegister");
//		showLoadingDilog();
//		NetworkHandler.registrationRequest(TAG, handlerRegister, params);
//	}
//
//	Handler handlerRegister = new Handler()
//	{
//		public void handleMessage(Message msg)
//		{
//
//			switch (msg.arg1)
//			{
//				case Constant.MessageState.REGISTRATION_SUCCESS:
//					cancelLoadingDialog();
//					parserRegistration((JSONObject) msg.obj);
//					break;
//
//				case Constant.MessageState.FAIL:
//					cancelLoadingDialog();
//					VolleyErrorHelper.getMessage(msg.obj, Registration.this,
//							true);
//				default:
//					break;
//			}
//		};
//	};
//
//	private void parserRegistration(JSONObject regObj)
//	{
//
//		try
//		{
//			String result = regObj.getString(Constant.RESULT);
//			if (result.equalsIgnoreCase(Constant.Result_STATE.PASS))
//			{
//				String userID = regObj.getString("UserId");
//
//				if (regObj.has("otp"))
//				{
//					otpValue = regObj.getString("otp");
//				}
//
//				// showDialog("Verification code sent to your Mobile Number.");
//				verificationLayout.setVisibility(View.GONE);
//				// isWaitForOTP();
//				mgr.SaveValueToSharedPrefs("UserID", userID);
//				mgr.SaveValueToSharedPrefs("Verify", "Pass");
//
//				setCustomDialogue();
//			}
//			else
//			{
//				Function.showToast(Registration.this, ConstantMessages.MSG23);
//			}
//		}
//		catch (Exception e)
//		{
//			e.printStackTrace();
//		}
//
//	}
//
//	private void isWaitForOTP()
//	{
//		mgr.SaveValueToSharedPrefs("username", etUsername.getText().toString());
//		mgr.SaveValueToSharedPrefs("password", etPassword.getText().toString());
//		mgr.SaveValueToSharedPrefs("email", etEmail.getText().toString());
//		mgr.SaveValueToSharedPrefs("mobile", etMobile.getText().toString());
//		mgr.SaveValueToSharedPrefs("RegisterStatus", "WaitForOtp");
//	}
//
//	public static final int NOTIFICATION_ID = 1;
//
//	private NotificationManager mNotificationManager;
//	NotificationCompat.Builder builder;
//
//	@SuppressWarnings("deprecation")
//	private void sendNotification(String msg)
//	{
//		mNotificationManager = (NotificationManager) this
//				.getSystemService(Context.NOTIFICATION_SERVICE);
//
//		builder = new NotificationCompat.Builder(this)
//				.setSmallIcon(R.drawable.ic_launcher)
//				.setContentTitle("GCM ERROR! ")
//				.setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
//				.setLights(Color.BLUE, 500, 1000).setAutoCancel(true)
//				.setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
//				.setContentText(msg);
//
//		builder.getNotification().flags |= Notification.FLAG_AUTO_CANCEL;
//		mNotificationManager.notify(NOTIFICATION_ID, builder.build());
//	}
//
//	@Override
//	public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
//	{
//		if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER))
//				|| (actionId == EditorInfo.IME_ACTION_DONE))
//		{
//			if (JugunooUtil.isConnectedToInternet(Registration.this))
//			{
//				register();
//			}
//			else
//			{
//				Function.showToast(Registration.this, Global.networkErrorMsg);
//			}
//		}
//
//		return false;
//	}
//
//	private void verifyParams(String otpVal)
//	{
//
//		String key = otpVal;
//		// String key = pin.getText().toString();
//		// mgr.SaveValueToSharedPrefs("OTP", key);
//
//		// String mobile = mgr.GetValueFromSharedPrefs("Mobile");
//		String mobile = this.etMobile.getText().toString().trim();
//
//		if (TextUtils.isEmpty(key))
//		{
//			Function.showToast(Registration.this, ConstantMessages.MSG26);
//		}
//
//		if (TextUtils.isEmpty(mobile))
//		{
//			Function.showToast(Registration.this, ConstantMessages.MSG24);
//		}
//
//		if (TextUtils.isEmpty(key) && TextUtils.isEmpty(mobile))
//		{
//			Function.showToast(Registration.this, ConstantMessages.MSG25);
//		}
//
//		if (!TextUtils.isEmpty(key) && !TextUtils.isEmpty(mobile))
//		{
//			// RequestParams params = new RequestParams();
//			HashMap<String, String> params = new HashMap<String, String>();
//
//			params.put("Mobile", mobile);
//			params.put("OTP", key);
//
//			// String url = Global.JUGUNOO_WS + "Passenger/OTPUpdate";
//			// VerifyNumber(params, url);
//			makeVerifyNumberRequest(params);
//		}
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
//				startActivity(new Intent(Registration.this, SplashScreen.class));
//				Registration.this.finish();
//				return true;
//			}
//
//		}
//		return super.onKeyDown(keyCode, event);
//	}
//
//	private void requestKeyParams()
//	{
//
//		String userName = mgr.GetValueFromSharedPrefs("UserName");
//		// String mobile = mgr.GetValueFromSharedPrefs("Mobile");
//		String mobile = this.etMobile.getText().toString().trim();
//
//		Log.e(TAG, TAG + " requestKeyParams=" + userName + " " + mobile + " "
//				+ otpValue);
//
//		if (!TextUtils.isEmpty(mobile) && mobile.length() == 10)
//		{
//			// RequestParams params = new RequestParams();
//			// params.put("FirstName", userName);
//			// params.put("Mobile", mobile);
//			// String url = Global.JUGUNOO_WS + "Passenger/OTPReset";
//
//			// RequestOTP(params, url);
//			// makeOtpRequest(params);
//
//			sendSms(mobile, otpValue);
//
//		}
//		else
//		{
//			Log.i(TAG, TAG + " Parameters are null");
//		}
//
//	}
//
//	private void VerifyNumber(RequestParams params, String url)
//	{
//
//		AsyncHttpClient client = new AsyncHttpClient();
//		client.setTimeout(45000);
//		client.post(url, params, new AsyncHttpResponseHandler()
//		{
//
//			@Override
//			@Deprecated
//			public void onFailure(int statusCode, Throwable error,
//					String content)
//			{
//
//				tvDiaMsg.setText("Please try again");
//				pbDialogue.setVisibility(View.GONE);
//				btnTryAgain.setVisibility(View.VISIBLE);
//
//				mgr.SaveValueToSharedPrefs("Verify", "Fail");
//
//				super.onFailure(statusCode, error, content);
//			}
//
//			@Override
//			@Deprecated
//			public void onSuccess(int statusCode, String content)
//			{
//				if (statusCode == 200)
//				{
//					JSONObject obj;
//					try
//					{
//
//						Log.e("ver", "VerifyNumber=" + content);
//
//						obj = new JSONObject(content);
//						String result = obj.getString("Result");
//
//						if (result.equalsIgnoreCase("Pass"))
//						{
//
//							mgr.SaveValueToSharedPrefs("Verify", "Success");
//							mgr.SaveValueToSharedPrefs("RegisterStatus",
//									"Verified");
//
//							mgr.SaveValueToSharedPrefs("UserName", "");
//							mgr.SaveValueToSharedPrefs("Mobile", "");
//							mgr.SaveValueToSharedPrefs("Email", "");
//							mgr.SaveValueToSharedPrefs(Constant.USER_TYPE,
//									"Normal");
//							mgr.savePreferenceIndex(Constant.PREFERENCE_INDEX,
//									0);
//							// custom dia cancel
//							if (smsDialog != null)
//							{
//								smsDialog.cancel();
//							}
//
//							startActivity(new Intent(Registration.this,
//									SplashScreen.class));
//							Registration.this.finish();
//						}
//						else
//						{
//							// JugunooInteractiveDialog("OTP", "Invalid OTP");
//							tvDiaMsg.setText("Invalid OTP");
//							pbDialogue.setVisibility(View.GONE);
//							btnTryAgain.setVisibility(View.VISIBLE);
//
//							mgr.SaveValueToSharedPrefs("RegisterStatus",
//									"WaitForOtp");
//							mgr.SaveValueToSharedPrefs("Verify", "Fail");
//						}
//					}
//					catch (JSONException e)
//					{
//
//						e.printStackTrace();
//					}
//				}
//				else
//				{
//
//					tvDiaMsg.setText("Please try again");
//					pbDialogue.setVisibility(View.GONE);
//					btnTryAgain.setVisibility(View.VISIBLE);
//
//					mgr.SaveValueToSharedPrefs("RegisterStatus", "WaitForOtp");
//
//					mgr.SaveValueToSharedPrefs("Verify", "Fail");
//				}
//
//				super.onSuccess(statusCode, content);
//			}
//
//		});
//	}
//
//	private void makeVerifyNumberRequest(HashMap<String, String> params)
//	{
//		showLoadingDilog();
//		NetworkHandler.verifyNumberRequest(TAG, handlerVerifyNumber, params);
//	}
//
//	Handler handlerVerifyNumber = new Handler()
//	{
//		public void handleMessage(Message msg)
//		{
//
//			switch (msg.arg1)
//			{
//				case Constant.MessageState.VERIFY_NUMBER_SUCCESS:
//					cancelLoadingDialog();
//					parserVerifyNumber((JSONObject) msg.obj);
//					break;
//
//				case Constant.MessageState.FAIL:
//					cancelLoadingDialog();
//
//					VolleyErrorHelper.getMessage(msg.obj, Registration.this,
//							true);
//
//					tvDiaMsg.setText(ConstantMessages.MSG30);
//					pbDialogue.setVisibility(View.GONE);
//					btnTryAgain.setVisibility(View.VISIBLE);
//					// showDialog("Verification Time expired. Tap on <VERIFY> button to try again.");
//					mgr.SaveValueToSharedPrefs("Verify", "Fail");
//					break;
//
//				default:
//					break;
//			}
//
//		};
//
//	};
//
//	private void parserVerifyNumber(JSONObject obj)
//	{
//		try
//		{
//			String result = obj.getString("Result");
//
//			if (result.equalsIgnoreCase("Pass"))
//			{
//				mgr.SaveValueToSharedPrefs("Verify", "Success");
//				mgr.SaveValueToSharedPrefs("RegisterStatus", "Verified");
//
//				mgr.SaveValueToSharedPrefs("UserName", "");
//				mgr.SaveValueToSharedPrefs("Mobile", "");
//				mgr.SaveValueToSharedPrefs("Email", "");
//				mgr.SaveValueToSharedPrefs(Constant.USER_TYPE, "Normal");
//				mgr.savePreferenceIndex(Constant.PREFERENCE_INDEX, 0);
//
//				if (smsDialog != null)
//				{
//					smsDialog.cancel();
//				}
//
//				startActivity(new Intent(Registration.this, SplashScreen.class));
//				Registration.this.finish();
//			}
//			else
//			{
//				// JugunooInteractiveDialog("OTP", "Invalid OTP");
//				tvDiaMsg.setText("Invalid OTP");
//				pbDialogue.setVisibility(View.GONE);
//				btnTryAgain.setVisibility(View.VISIBLE);
//
//				mgr.SaveValueToSharedPrefs("RegisterStatus", "WaitForOtp");
//				mgr.SaveValueToSharedPrefs("Verify", "Fail");
//			}
//		}
//		catch (JSONException e)
//		{
//			e.printStackTrace();
//		}
//
//	}
//
//	private void RequestOTP(RequestParams params, String url)
//	{
//
//		// pdHandler = new Handler();
//		// pd = new TransparentProgressDialog(Registration.this,
//		// R.drawable.loading_image);
//		//
//		// pdRunnable = new Runnable() {
//		// @Override
//		// public void run() {
//		// if (pd != null) {
//		// if (pd.isShowing()) {
//		// pd.dismiss();
//		// }
//		// }
//		// }
//		// };
//		// pd.show();
//
//		AsyncHttpClient client = new AsyncHttpClient();
//
//		client.post(url, params, new AsyncHttpResponseHandler()
//		{
//
//			@Override
//			@Deprecated
//			public void onFailure(int statusCode, Throwable error,
//					String content)
//			{
//
//				Function.showToast(Registration.this, Global.networkErrorMsg);
//
//				tvDiaMsg.setText("Please try again");
//				pbDialogue.setVisibility(View.GONE);
//				btnTryAgain.setVisibility(View.VISIBLE);
//
//				super.onFailure(statusCode, error, content);
//				// pdHandler.removeCallbacks(pdRunnable);
//				// if (pd.isShowing()) {
//				// pd.dismiss();
//				// }
//			}
//
//			@Override
//			@Deprecated
//			public void onSuccess(int statusCode, String content)
//			{
//
//				Log.e(TAG, TAG + " otp=" + content);
//
//				if (statusCode == 200)
//				{
//					JSONObject obj;
//					try
//					{
//						obj = new JSONObject(content);
//						String result = obj.getString("Result");
//						if (result.equalsIgnoreCase("Pass"))
//						{
//							// JugunooInteractiveDialog("OTP",
//							// "Verification code resend successfully.");
//							setSmsReceiveTimer();
//						}
//						else
//						{
//
//							tvDiaMsg.setText("Please try again");
//							pbDialogue.setVisibility(View.GONE);
//							btnTryAgain.setVisibility(View.VISIBLE);
//
//							// JugunooInteractiveDialog("OTP",
//							// "Do You Want to Request OTP Again.");
//						}
//					}
//					catch (JSONException e)
//					{
//						e.printStackTrace();
//					}
//					finally
//					{
//						// pdHandler.removeCallbacks(pdRunnable);
//						// if (pd.isShowing()) {
//						// pd.dismiss();
//						// }
//					}
//
//				}
//				else
//				{
//
//					tvDiaMsg.setText("Please try again");
//					pbDialogue.setVisibility(View.GONE);
//					btnTryAgain.setVisibility(View.VISIBLE);
//
//					// JugunooInteractiveDialog("OTP",
//					// "Do You Want to Request OTP Again!");
//					// pdHandler.removeCallbacks(pdRunnable);
//					// if (pd.isShowing()) {
//					// pd.dismiss();
//					// }
//				}
//				super.onSuccess(statusCode, content);
//
//			}
//
//		});
//	}
//
//	private void makeOtpRequest(HashMap<String, String> params)
//	{
//		showLoadingDilog();
//		NetworkHandler.verifyOtpRequest(TAG, handlerOtp, params);
//	}
//
//	Handler handlerOtp = new Handler()
//	{
//		public void handleMessage(Message msg)
//		{
//
//			switch (msg.arg1)
//			{
//				case Constant.MessageState.VERIFY_NUMBER_SUCCESS:
//					cancelLoadingDialog();
//					parserOtp((JSONObject) msg.obj);
//					break;
//
//				case Constant.MessageState.FAIL:
//
//					cancelLoadingDialog();
//					VolleyErrorHelper.getMessage(msg.obj, Registration.this,
//							true);
//
//					tvDiaMsg.setText(ConstantMessages.MSG30);
//					pbDialogue.setVisibility(View.GONE);
//					btnTryAgain.setVisibility(View.VISIBLE);
//
//				default:
//					break;
//			}
//
//		};
//
//	};
//
//	private void parserOtp(JSONObject obj)
//	{
//		try
//		{
//			String result = obj.getString("Result");
//			if (result.equalsIgnoreCase("Pass"))
//			{
//				// JugunooInteractiveDialog("OTP",
//				// "Verification code resend successfully.");
//				setSmsReceiveTimer();
//			}
//			else
//			{
//				tvDiaMsg.setText(ConstantMessages.MSG30);
//				pbDialogue.setVisibility(View.GONE);
//				btnTryAgain.setVisibility(View.VISIBLE);
//
//				// JugunooInteractiveDialog("OTP",
//				// "Do You Want to Request OTP Again.");
//			}
//		}
//		catch (JSONException e)
//		{
//			e.printStackTrace();
//		}
//	}
//
//	private void JugunooInteractiveDialog(String title, String message)
//	{
//		final Dialog dialog = new Dialog(Registration.this);
//		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//		Window window = dialog.getWindow();
//		window.setBackgroundDrawableResource(android.R.color.transparent);
//		LinearLayout.LayoutParams dialogParams = new LinearLayout.LayoutParams(
//				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
//
//		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//		RelativeLayout lay = new RelativeLayout(Registration.this);
//		View dislogView = inflater.inflate(R.layout.jugunoo_interactive_dialog,
//				lay);
//		dialog.setContentView(dislogView, dialogParams);
//
//		TextView textView = (TextView) dialog.findViewById(R.id.messageText);
//		Button centerBtn = (Button) dialog.findViewById(R.id.centerBtn);
//		Button leftBtn = (Button) dialog.findViewById(R.id.leftBtn);
//		Button rightBtn = (Button) dialog.findViewById(R.id.rightBtn);
//
//		if (title.equalsIgnoreCase("REGISTER"))
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
//
//				}
//			});
//		}
//		else if (title.equalsIgnoreCase("OTP"))
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
//		else
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
//	// sms receiver
//	BroadcastReceiver smsReceiver = new BroadcastReceiver()
//	{
//
//		@Override
//		public void onReceive(Context context, Intent intent)
//		{
//			Object[] pdus = (Object[]) intent.getExtras().get("pdus");
//			SmsMessage shortMessage = SmsMessage
//					.createFromPdu((byte[]) pdus[0]);
//
//			Log.i("SMSReceiver",
//					"SMS message sender: "
//							+ shortMessage.getOriginatingAddress());
//			Log.i("SMSReceiver",
//					"SMS message text: " + shortMessage.getDisplayMessageBody());
//
//			String otpVal = shortMessage.getDisplayMessageBody().replaceAll(
//					"[^0-9]", "");
//
//			if (otpValue.equals(otpVal))
//			{
//
//				if (JugunooUtil.isConnectedToInternet(Registration.this))
//				{
//					// timer.cancel();
//					smsHandler.removeCallbacks(waitFrSms);
//
//					tvDiaMsg.setText(ConstantMessages.MSG27);
//
//					verifyParams(otpVal);
//				}
//				else
//				{
//					Function.showToast(Registration.this,
//							Global.networkErrorMsg);
//				}
//			}
//		}
//	};
//
//	// set custom dialogue
//	private void setCustomDialogue()
//	{
//		requestKeyParams();
//		smsDialog = new Dialog(Registration.this);
//		smsDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//		smsDialog.setContentView(R.layout.activity_sms_custom_dialog);
//		smsDialog.setCanceledOnTouchOutside(false);
//
//		tvDiaMsg = (TextView) smsDialog.findViewById(R.id.tvDiaMsg);
//		tvDiaMsg.setText("waiting for sms to receive");
//		pbDialogue = (ProgressBar) smsDialog.findViewById(R.id.pbDia);
//		pbDialogue.setVisibility(View.VISIBLE);
//
//		btnTryAgain = (Button) smsDialog.findViewById(R.id.btn_try_again);
//		btnTryAgain.setVisibility(View.GONE);
//		btnTryAgain.setOnClickListener(new OnClickListener()
//		{
//
//			@Override
//			public void onClick(View v)
//			{
//
//				if (JugunooUtil.isConnectedToInternet(Registration.this))
//				{
//
//					registerSmsReceiver();
//
//					tvDiaMsg.setText(ConstantMessages.MSG29);
//					pbDialogue.setVisibility(View.VISIBLE);
//					btnTryAgain.setVisibility(View.GONE);
//
//					requestKeyParams();
//					setSmsReceiveTimer();
//				}
//				else
//				{
//					Function.showToast(Registration.this, ConstantMessages.MSG6);
//				}
//
//			}
//		});
//
//		smsDialog.show();
//
//		setSmsReceiveTimer();
//
//	}
//
//	private void setSmsReceiveTimer()
//	{
//
//		smsHandler.postDelayed(waitFrSms, SMS_TIME_OUT_VAL);
//
//		// timer.schedule(new TimerTask() {
//		//
//		// @Override
//		// public void run() {
//		//
//		// btnTryAgain.setVisibility(View.VISIBLE);
//		// tvDiaMsg.setText("Resend to verify");
//		// pbDialogue.setVisibility(View.GONE);
//		//
//		// if (isSmsRegistered) {
//		// isSmsRegistered = false;
//		//
//		// unregisterReceiver(smsReceiver);
//		// }
//		// }
//		// }, SMS_TIME_OUT_VAL);
//
//	}
//
//	Runnable waitFrSms = new Runnable()
//	{
//
//		@Override
//		public void run()
//		{
//			btnTryAgain.setVisibility(View.VISIBLE);
//			tvDiaMsg.setText(ConstantMessages.MSG28);
//			pbDialogue.setVisibility(View.GONE);
//
//			if (isSmsRegistered)
//			{
//				isSmsRegistered = false;
//				unregisterReceiver(smsReceiver);
//			}
//		}
//	};
//
//	private void sendSms(String phnNo, String otp)
//	{
//		try
//		{
//			SmsManager sms = SmsManager.getDefault();
//			sms.sendTextMessage(phnNo, null, otp, null, null);
//		}
//		catch (Exception e)
//		{
//			e.printStackTrace();
//			Function.showToast(Registration.this, ConstantMessages.MSG31);
//		}
//
//	}
//
//	@Override
//	protected void onStop()
//	{
//		super.onStop();
//		if (isSmsRegistered)
//		{
//			isSmsRegistered = false;
//			unregisterReceiver(smsReceiver);
//		}
//	}
//
//	@Override
//	protected void onDestroy()
//	{
//		super.onDestroy();
//		if (isSmsRegistered)
//		{
//			isSmsRegistered = false;
//			unregisterReceiver(smsReceiver);
//		}
//	}
//
//}
