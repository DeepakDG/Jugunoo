package com.hirecraft.jugunoo.passenger;

import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.telephony.SmsMessage;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hirecraft.jugunoo.passenger.common.Constant;
import com.hirecraft.jugunoo.passenger.common.ConstantMessages;
import com.hirecraft.jugunoo.passenger.common.Function;
import com.hirecraft.jugunoo.passenger.common.Global;
import com.hirecraft.jugunoo.passenger.common.TransparentProgressDialog;
import com.hirecraft.jugunoo.passenger.networkhandler.NetworkHandler;
import com.hirecraft.jugunoo.passenger.services.VolleyErrorHelper;
import com.hirecraft.jugunoo.passenger.utility.JugunooUtil;
import com.hirecraft.jugunoo.passenger.utility.SharedPreferencesManager;

public class OtpVarificationActivity extends Activity implements
		android.view.View.OnClickListener
{

	private String TAG = OtpVarificationActivity.class.getSimpleName();

	private EditText etOtp;
	private Button btResend, btProceed;
	private ProgressBar progBarOtp;
	private TextView tvcountDown, tvRemaining;

	private static TransparentProgressDialog pd;
	private static Handler pdHandler;
	private static Runnable pdRunnable;

	private boolean isSmsRegistered;

	private Handler smsHandler = new Handler();
	// time in millis 1 min
	private int SMS_TIME_OUT_VAL = 30000;

	// sms dialogue
	private Dialog smsDialog;
	private TextView tvDiaMsg;
	private ProgressBar pbDialogue;
	private Button btnTryAgain;
	private SharedPreferencesManager mgr;

	private HashMap<String, String> params;
	private static final String SENDER_NUMBER = "DZ-JUGUNOO";

	private CountDownTimer countDownTimer;
	private boolean timerHasStarted = false;
	private final long startTime = 30 * 1000;
	private final long interval = 1 * 1000;

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		SetActionBar();
		setContentView(R.layout.activity_otp);
		mgr = new SharedPreferencesManager(OtpVarificationActivity.this);

		etOtp = (EditText) findViewById(R.id.etOtp);
		btProceed = (Button) findViewById(R.id.btProceed);
		btResend = (Button) findViewById(R.id.btResend);

		tvcountDown = (TextView) findViewById(R.id.tvCountDown);
		tvRemaining = (TextView) findViewById(R.id.tvRemaining);
		progBarOtp = (ProgressBar) findViewById(R.id.progBarOtp);

		hideResendBtn();

		btResend.setOnClickListener(this);
		btProceed.setOnClickListener(this);

		countDownTimer = new MyCountDownTimer(startTime, interval);
		tvcountDown.setText(String.valueOf(startTime / 1000) + " secs");

		Intent intent = getIntent();
		params = (HashMap<String, String>) intent
				.getSerializableExtra("params");
		Log.d("param", "param " + params);

		if (params != null)
		{
			makeRegistrationReq(params);
		}
		else
		{
			Function.showToast(OtpVarificationActivity.this,
					"No Registration details found");
		}

		//
		// new CountDownTimer(30000, 1000)
		// {
		//
		// @Override
		// public void onTick(long millisUntilFinished)
		// {
		// tvcountDown.setText("seconds remaining: " + millisUntilFinished
		// / 1000);
		// }
		//
		// @Override
		// public void onFinish()
		// {
		// showResendBtn();
		// }
		// };

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
		title.setText("Otp Verification");

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

	public class MyCountDownTimer extends CountDownTimer
	{
		public MyCountDownTimer(long startTime, long interval)
		{
			super(startTime, interval);
		}

		@Override
		public void onFinish()
		{
			tvcountDown.setText("Time's up!");
		}

		@Override
		public void onTick(long millisUntilFinished)
		{
			tvcountDown.setText("" + millisUntilFinished / 1000 + " sec's");
		}
	}

	@Override
	public void onBackPressed()
	{
		super.onBackPressed();
		startActivity(new Intent().setClass(OtpVarificationActivity.this,
				SignupTabActivity.class));
		OtpVarificationActivity.this.finish();

		// finish();
	}

	@Override
	public void onClick(View v)
	{

		switch (v.getId())
		{
			case R.id.btResend:

				startCountDown();
				requestKeyParams();

				break;

			case R.id.btProceed:

				if (JugunooUtil
						.isConnectedToInternet(OtpVarificationActivity.this))
				{

					// verifyParams("");

					String etOtpval = etOtp.getText().toString().trim();

					if (TextUtils.isEmpty(etOtpval) || etOtpval.length() <= 0)
					{
						Function.showToast(OtpVarificationActivity.this,
								ConstantMessages.MSG26);
					}
					else
					{
						verifyParams(etOtpval);
					}

				}
				else
				{
					Function.showToast(OtpVarificationActivity.this,
							ConstantMessages.MSG6);
				}
				break;

			default:
				break;
		}

	}

	private void showLoadingDilog()
	{
		pdHandler = new Handler();
		pd = new TransparentProgressDialog(OtpVarificationActivity.this,
				R.drawable.loading_image);

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

	private void cancelLoadingDialog()
	{
		pdHandler.removeCallbacks(pdRunnable);

		if (pd.isShowing())
		{
			pd.dismiss();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{

		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0)
		{

			if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0)
			{
				startActivity(new Intent(OtpVarificationActivity.this,
						SplashScreen.class));
				OtpVarificationActivity.this.finish();
				return true;
			}

		}
		return super.onKeyDown(keyCode, event);
	}

	private void makeRegistrationReq(HashMap<String, String> params)
	{
		mgr.SaveValueToSharedPrefs("RegisterStatus", "AttemptToRegister");
		showLoadingDilog();
		NetworkHandler.registrationRequest(TAG, handlerRegister, params);
	}

	Handler handlerRegister = new Handler()
	{
		public void handleMessage(Message msg)
		{
			switch (msg.arg1)
			{
				case Constant.MessageState.REGISTRATION_SUCCESS:
					cancelLoadingDialog();
					parserRegistration((JSONObject) msg.obj);
					break;

				case Constant.MessageState.FAIL:
					cancelLoadingDialog();
					VolleyErrorHelper.getMessage(msg.obj,
							OtpVarificationActivity.this, true);
				default:
					break;
			}
		};
	};

	private void parserRegistration(JSONObject regObj)
	{

		try
		{
			Log.d("parserRegistration",
					"parserRegistration " + regObj.toString());
			String result = regObj.getString(Constant.RESULT);
			if (result.equalsIgnoreCase(Constant.Result_STATE.PASS))
			{
				String userID = regObj.getString("UserId");
				mgr.SaveValueToSharedPrefs("UserID", userID);
				mgr.SaveValueToSharedPrefs("Verify", "Pass");

				registerSmsReceiver();
				setSmsReceiveTimer();
				startCountDown();

				// new CountDownTimer(30000, 1000)
				// {
				//
				// @Override
				// public void onTick(long millisUntilFinished)
				// {
				// tvcountDown.setText("seconds remaining: "
				// + millisUntilFinished / 1000);
				// }
				//
				// @Override
				// public void onFinish()
				// {
				// showResendBtn();
				// }
				// };

			}
			else
			{
				Function.showToast(OtpVarificationActivity.this,
						ConstantMessages.MSG23);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	private void requestKeyParams()
	{
		String mobile = mgr.GetValueFromSharedPrefs("Mobile");
		// Log.d("requestKeyParams", "requestKeyParams=" + userName + " "
		// +mobile+ " " + otpValue);

		if (!TextUtils.isEmpty(mobile) && mobile.length() == 10)
		{
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("Mobile", mobile);
			makeOtpRequest(params);
		}
		else
		{
			Log.i(TAG, TAG + "Register Parameters are null");
		}

	}

	private void makeOtpRequest(HashMap<String, String> params)
	{
		// showLoadingDilog();
		NetworkHandler.verifyOtpRequest(TAG, handlerOtp, params);
	}

	Handler handlerOtp = new Handler()
	{
		public void handleMessage(Message msg)
		{

			switch (msg.arg1)
			{
				case Constant.MessageState.OTP_SUCCESS:
					// cancelLoadingDialog();
					parserOtp((JSONObject) msg.obj);
					break;

				case Constant.MessageState.FAIL:
					// cancelLoadingDialog();
					VolleyErrorHelper.getMessage(msg.obj,
							OtpVarificationActivity.this, true);
				default:
					break;
			}

		};

	};

	private void parserOtp(JSONObject obj)
	{
		try
		{
			Log.d("parseOtp", "parseOtp " + obj.toString());
			String result = obj.getString("Result");
			if (result.equalsIgnoreCase("Pass"))
			{
				setSmsReceiveTimer();
			}
			else
			{
				showResendBtn();
			}
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
	}

	private void makeVerifyNumberRequest(HashMap<String, String> params)
	{
		showLoadingDilog();
		NetworkHandler.verifyNumberRequest(TAG, handlerVerifyNumber, params);
	}

	Handler handlerVerifyNumber = new Handler()
	{
		public void handleMessage(Message msg)
		{

			switch (msg.arg1)
			{
				case Constant.MessageState.VERIFY_NUMBER_SUCCESS:

					cancelLoadingDialog();
					parserVerifyNumber((JSONObject) msg.obj);
					break;

				case Constant.MessageState.FAIL:

					cancelLoadingDialog();
					VolleyErrorHelper.getMessage(msg.obj,
							OtpVarificationActivity.this, true);

					tvDiaMsg.setText(ConstantMessages.MSG30);
					pbDialogue.setVisibility(View.GONE);
					btnTryAgain.setVisibility(View.VISIBLE);
					// showDialog("Verification Time expired. Tap on <VERIFY> button to try again.");
					mgr.SaveValueToSharedPrefs("Verify", "Fail");
					break;

				default:
					break;
			}

		};

	};

	private void parserVerifyNumber(JSONObject obj)
	{
		try
		{
			String result = obj.getString("Result");
			Log.d("parserVerifyNumber", "parserVerifyNumber " + obj.toString());

			if (result.equalsIgnoreCase("Pass"))
			{
				mgr.SaveValueToSharedPrefs("Verify", "Success");
				mgr.SaveValueToSharedPrefs("RegisterStatus", "Verified");

				mgr.SaveValueToSharedPrefs("UserName", "");
				mgr.SaveValueToSharedPrefs("Mobile", "");
				mgr.SaveValueToSharedPrefs("Email", "");
				mgr.SaveValueToSharedPrefs(Constant.USER_TYPE, "Normal");
				mgr.savePreferenceIndex(Constant.PREFERENCE_INDEX, 0);

				stopCountDown();

				startActivity(new Intent(OtpVarificationActivity.this,
						SplashScreen.class));
				OtpVarificationActivity.this.finish();
			}
			else
			{
				Function.showToast(OtpVarificationActivity.this, "Invalid OTP");
				showResendBtn();
				mgr.SaveValueToSharedPrefs("RegisterStatus", "WaitForOtp");
				mgr.SaveValueToSharedPrefs("Verify", "Fail");
			}
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}

	}

	Runnable waitFrSms = new Runnable()
	{
		@Override
		public void run()
		{
			if (isSmsRegistered)
			{
				isSmsRegistered = false;
				unregisterReceiver(smsReceiver);
			}

			stopCountDown();

		}
	};

	private void setSmsReceiveTimer()
	{
		smsHandler.postDelayed(waitFrSms, SMS_TIME_OUT_VAL);
	}

	// sms receiver
	BroadcastReceiver smsReceiver = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			Object[] pdus = (Object[]) intent.getExtras().get("pdus");
			SmsMessage shortMessage = SmsMessage
					.createFromPdu((byte[]) pdus[0]);

			Log.d("SMSReceiver",
					"SMS message sender: "
							+ shortMessage.getOriginatingAddress());
			Log.d("SMSReceiver",
					"SMS message text: " + shortMessage.getDisplayMessageBody());

			String otpVal = shortMessage.getDisplayMessageBody().replaceAll(
					"[^0-9]", "");

			Log.d("otpVal", "otpVal=" + otpVal);

			if (shortMessage.getOriginatingAddress().equalsIgnoreCase(
					SENDER_NUMBER))
			{
				etOtp.setText(otpVal);
				stopCountDown();

				if (JugunooUtil
						.isConnectedToInternet(OtpVarificationActivity.this))
				{
					smsHandler.removeCallbacks(waitFrSms);
					verifyParams(otpVal);
				}
				else
				{
					Function.showToast(OtpVarificationActivity.this,
							Global.networkErrorMsg);
				}
			}
			else
			{
				Log.d("otpValue", "otpValue match failed");
			}

			showResendBtn();
		}
	};

	private void verifyParams(String otpVal)
	{

		String mobile = mgr.GetValueFromSharedPrefs("Mobile");

		if (TextUtils.isEmpty(otpVal)
				|| TextUtils.isEmpty(etOtp.getText().toString()))
		{
			Function.showToast(OtpVarificationActivity.this,
					ConstantMessages.MSG26);
		}

		if (!TextUtils.isEmpty(otpVal))
		{
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("Mobile", mobile);
			params.put("OTP", otpVal);
			makeVerifyNumberRequest(params);
		}
	}

	private void registerSmsReceiver()
	{

		Log.i(TAG, TAG + " registerSmsReceiver");
		// sms register
		IntentFilter mIntentFilter = new IntentFilter();
		mIntentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");

		registerReceiver(smsReceiver, mIntentFilter);

		isSmsRegistered = true;
	}

	private void showResendBtn()
	{

		btResend.setBackgroundResource(R.drawable.selector_button);
		btResend.setEnabled(true);

		tvcountDown.setVisibility(View.GONE);
		tvRemaining.setVisibility(View.GONE);
		progBarOtp.setVisibility(View.GONE);
	}

	private void hideResendBtn()
	{

		btResend.setBackgroundColor(getResources()
				.getColor(R.color.light_green));
		btResend.setEnabled(false);

		tvcountDown.setVisibility(View.VISIBLE);
		tvRemaining.setVisibility(View.VISIBLE);
		progBarOtp.setVisibility(View.VISIBLE);
	}

	private void startCountDown()
	{
		if (!timerHasStarted)
		{
			countDownTimer.start();
			timerHasStarted = true;
			hideResendBtn();
		}
	}

	private void stopCountDown()
	{
		if (timerHasStarted)
		{
			countDownTimer.cancel();
			timerHasStarted = false;
			showResendBtn();
		}
	}

	@Override
	protected void onStop()
	{
		super.onStop();
		if (isSmsRegistered)
		{
			isSmsRegistered = false;
			unregisterReceiver(smsReceiver);
		}
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		if (isSmsRegistered)
		{
			isSmsRegistered = false;
			unregisterReceiver(smsReceiver);
		}
	}

}
