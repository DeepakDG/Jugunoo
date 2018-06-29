package com.hirecraft.jugunoo.passenger.fragments;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

import org.json.JSONObject;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.hirecraft.jugunoo.passenger.OtpVarificationActivity;
import com.hirecraft.jugunoo.passenger.R;
import com.hirecraft.jugunoo.passenger.common.Constant;
import com.hirecraft.jugunoo.passenger.common.ConstantMessages;
import com.hirecraft.jugunoo.passenger.common.Function;
import com.hirecraft.jugunoo.passenger.common.Global;
import com.hirecraft.jugunoo.passenger.common.TransparentProgressDialog;
import com.hirecraft.jugunoo.passenger.common.Validation;
import com.hirecraft.jugunoo.passenger.networkhandler.NetworkHandler;
import com.hirecraft.jugunoo.passenger.services.VolleyErrorHelper;
import com.hirecraft.jugunoo.passenger.utility.JugunooUtil;
import com.hirecraft.jugunoo.passenger.utility.SharedPreferencesManager;
import com.loopj.android.http.AsyncHttpClient;

public class PersonalSignupFragment extends Fragment implements
		OnClickListener, OnEditorActionListener
{

	private static final String TAG = PersonalSignupFragment.class
			.getSimpleName();

	private EditText etUsername, etPassword, etCpassword, etEmail, etMobile;
	private String usernameStr = "", passwordStr = "", cpasswordStr,
			emailStr = "", mobileStr = "", deviceIDStr = "",
			countryCodeStr = "";

	private String gcmIDStr = "";

	private Button btVerify;
	private ProgressBar emailProgress;
	private SharedPreferencesManager mgr;

	boolean isUsername = false, isPasswrd = false, isCpasswrd = false,
			isEmail = false;
	Typeface light, bold, semibold;

	private final int JUGUNOO_EMAIL_CHECK = 11;
	private final int JUGUNOO_MOBILE_CHECK = 12;
	private final int JUGUNOO_DELAY_IN_MILLIS = 1000;

	AsyncHttpClient reqisterClient = null;

	private boolean isEmailExist;
	private String availabilityField;

	private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

	private static TransparentProgressDialog pd;
	private static Handler pdHandler;
	private static Runnable pdRunnable;

	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.fragment_personal_signup, container,
				false);
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);

		JugunooUtil util = new JugunooUtil(getActivity());
		deviceIDStr = util.getUniqueDeviceID(getActivity());

		light = Typeface.createFromAsset(getActivity().getAssets(),
				"fonts/opensans-regular-webfont.ttf");
		bold = Typeface.createFromAsset(getActivity().getAssets(),
				"fonts/opensans-bold-webfont.ttf");
		semibold = Typeface.createFromAsset(getActivity().getAssets(),
				"fonts/opensans-semibold-webfont.ttf");

		mgr = new SharedPreferencesManager(getActivity());

		etUsername = (EditText) getActivity().findViewById(
				R.id.etNamePersonalSignup);
		etPassword = (EditText) getActivity().findViewById(
				R.id.etPwPersonalSignup);
		etCpassword = (EditText) getActivity().findViewById(
				R.id.etCpwPersonalSignup);

		etEmail = (EditText) getActivity().findViewById(
				R.id.etEmailPersonalSignup);
		etMobile = (EditText) getActivity().findViewById(
				R.id.etMobilePersonalSignup);

		btVerify = (Button) getActivity().findViewById(
				R.id.btVerifyPersonalSignup);

		emailProgress = (ProgressBar) getActivity().findViewById(
				R.id.progMailPersonalSignup);
		btVerify.setOnClickListener(this);

		etUsername.addTextChangedListener(usernameWatcher);
		etPassword.addTextChangedListener(passwordWatcher);
		etCpassword.addTextChangedListener(cpasswordWatcher);
		etEmail.addTextChangedListener(emailWatcher);

		Init();

		if (checkPlayServices())
		{
			// registerInBackground();
		}
	}

	@Override
	public void onResume()
	{
		super.onResume();
		checkPlayServices();
	}

	private boolean checkPlayServices()
	{
		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(getActivity());
		if (resultCode != ConnectionResult.SUCCESS)
		{
			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode))
			{
				GooglePlayServicesUtil.getErrorDialog(resultCode,
						getActivity(), PLAY_SERVICES_RESOLUTION_REQUEST).show();
			}
			else
			{
				Log.i(TAG, TAG + " This device is not supported.");
				getActivity().finish();
			}
			return false;
		}
		return true;
	}

	private void Init()
	{

		etUsername.setTypeface(light);
		etPassword.setTypeface(light);
		etCpassword.setTypeface(light);
		etEmail.setTypeface(light);
		etMobile.setTypeface(light);

		String RegisterStatus = mgr.GetValueFromSharedPrefs("RegisterStatus");
		if (RegisterStatus.equalsIgnoreCase("WaitForOtp"))
		{
			String usern = mgr.GetValueFromSharedPrefs("username");
			String passwo = mgr.GetValueFromSharedPrefs("password");
			String emai = mgr.GetValueFromSharedPrefs("email");
			String mobil = mgr.GetValueFromSharedPrefs("mobile");

			etUsername.setText(usern);
			etPassword.setText(passwo);
			etCpassword.setText(passwo);
			etEmail.setText(emai);
			etMobile.setText(mobil);
		}
		else
		{
		}

	}

	private void clearFocus()
	{
		etUsername.clearFocus();
		etPassword.clearFocus();
		etCpassword.clearFocus();
		etEmail.clearFocus();
		etMobile.clearFocus();
	}

	Handler handler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{

			if (msg.what == JUGUNOO_EMAIL_CHECK)
			{

				if (JugunooUtil.isConnectedToInternet(getActivity()))
				{
					// isEmailExist = true;

					String emailStr = etEmail.getText().toString();

					if (!TextUtils.isEmpty(emailStr)
							&& emailStr.matches(Validation.EMAIL_REGEX))
					{
						String url = Global.JUGUNOO_WS
								+ "Passenger/ValidateParameter?CheckValue="
								+ emailStr.toString() + "&Parameter=Email";

						Log.d("JUGUNOO_EMAIL_CHECK", "JUGUNOO_EMAIL_CHECK "
								+ url);
						makeAvailabilityRequest(url, "Email");

					}
					// else if (!TextUtils.isEmpty(emailStr))
					// {
					//
					// if (!Validation.isValidE(etEmail,
					// Validation.EMAIL_REGEX,
					// "Enter a valid Email ID."))
					// {
					// isEmailExist = true;
					// }
					// else
					// {
					// isEmailExist = false;
					// }
					//
					// }
					// else if (TextUtils.isEmpty(emailStr))
					// {
					// isEmailExist = false;
					// }

				}
				else
				{
					Function.showToast(getActivity(), Global.networkErrorMsg);
				}

			}

			if (msg.what == JUGUNOO_MOBILE_CHECK)
			{

				if (JugunooUtil.isConnectedToInternet(getActivity()))
				{
					String mobileStr = etMobile.getText().toString();

					Log.i("reg", "mob len=" + mobileStr.length());

					if (mobileStr.length() == 10 && makeValidation())
					{
						String url = Global.JUGUNOO_WS
								+ "Passenger/ValidateParameter?CheckValue="
								+ mobileStr.toString() + "&Parameter=Mobile";

						Log.d("JUGUNOO_MOBILE_CHECK", "JUGUNOO_MOBILE_CHECK "
								+ url);

						// CheckAvailability(url, "Mobile");
						makeAvailabilityRequest(url, "Mobile");
					}
				}
				else
				{
					VolleyErrorHelper.getMessage(msg.obj, getActivity(), true);
				}

			}

		};
	};

	@Override
	public void onStart()
	{
		super.onStart();

		etMobile.setOnEditorActionListener(new EditText.OnEditorActionListener()
		{

			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event)
			{

				Log.i("mobile", "mobile edit");

				if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER))
						|| (actionId == EditorInfo.IME_ACTION_DONE))
				{
					String mobileStr = etMobile.getText().toString();

					if (mobileStr.length() != 10)
					{
						// setTextColor(ConstantMessages.MSG16, "Mobile");
						showTooltip(ConstantMessages.MSG16);
					}

					else
					{

						if (JugunooUtil.isConnectedToInternet(getActivity()))
						{
							register();
						}
						else
						{
							Function.showToast(getActivity(),
									ConstantMessages.MSG6);
						}
					}
				}
				return false;
			}
		});
	}

	public int lastIndexOfUCL(String str)
	{
		for (int i = str.length() - 1; i >= 0; i--)
		{
			if (Character.isUpperCase(str.charAt(i)))
			{
				return i;
			}
		}
		return -1;
	}

	private TextWatcher usernameWatcher = new TextWatcher()
	{

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count)
		{
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after)
		{
		}

		@Override
		public void afterTextChanged(Editable s)
		{
		}
	};

	private TextWatcher passwordWatcher = new TextWatcher()
	{

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count)
		{
			etPassword.setError(null);
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after)
		{
		}

		@Override
		public void afterTextChanged(Editable s)
		{
			if (isUsername == true)
			{
				etCpassword.setEnabled(true);
			}
		}
	};

	private TextWatcher cpasswordWatcher = new TextWatcher()
	{

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count)
		{
			etCpassword.setError(null);
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after)
		{
		}

		@Override
		public void afterTextChanged(Editable s)
		{
		}
	};

	private TextWatcher emailWatcher = new TextWatcher()
	{

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count)
		{
			etEmail.setError(null);
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after)
		{
		}

		@Override
		public void afterTextChanged(Editable s)
		{

			emailStr = etEmail.getText().toString().trim();
			if (!emailStr.isEmpty())
			{
				handler.removeMessages(JUGUNOO_EMAIL_CHECK);
				handler.sendEmptyMessageDelayed(JUGUNOO_EMAIL_CHECK,
						JUGUNOO_DELAY_IN_MILLIS);
			}
			else
			{
				isEmailExist = false;
			}
		}
	};

	public static boolean validatePassword(String password)
	{

		Matcher mtch = JugunooUtil.pswNamePtrn.matcher(password);
		if (mtch.matches())
		{
			return true;
		}
		return false;
	}

	private void makeAvailabilityRequest(String url, String field)
	{
		if (field.equalsIgnoreCase("Email"))
		{
			emailProgress.setVisibility(View.VISIBLE);
		}
		availabilityField = field;
		Map<String, String> params = new HashMap<String, String>();
		NetworkHandler.mailAvailabilityRequest(TAG, handlerAvailability,
				params, url);
	}

	Handler handlerAvailability = new Handler()
	{
		public void handleMessage(Message msg)
		{

			switch (msg.arg1)
			{
				case Constant.MessageState.AVAILABILITY_MAIL_NUMBER_SUCCESS:

					parserAvailability((JSONObject) msg.obj);

					if (availabilityField.equalsIgnoreCase("Email"))
					{
						emailProgress.setVisibility(View.GONE);
					}
					break;

				case Constant.MessageState.FAIL:
					VolleyErrorHelper.getMessage(msg.obj, getActivity(), true);

					if (availabilityField.equalsIgnoreCase("Email"))
					{
						emailProgress.setVisibility(View.GONE);
					}
					break;

				default:
					break;
			}

		};
	};

	private void parserAvailability(JSONObject obj)
	{
		try
		{
			Log.d("parserAvailability", "parserAvailability " + obj.toString());
			String result = obj.getString("Result");
			if (result.equalsIgnoreCase("Pass"))
			{
				if (availabilityField.equalsIgnoreCase("Email"))
				{
					isEmail = true;
					isEmailExist = false;
					// setTextColor("Available", availabilityField);
				}
			}
			else
			{
				if (availabilityField.equalsIgnoreCase("Email"))
				{
					isEmailExist = true;
					showTooltip(obj.getString("Message"));
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
			case R.id.btVerifyPersonalSignup:

				clearFocus();
				if (JugunooUtil.isConnectedToInternet(getActivity()))
				{
					// checkValidation();
					// makeValidation();

					register();
				}
				else
				{
					Function.showToast(getActivity(), ConstantMessages.MSG6);
				}
				break;
		}

	}

	private void register()
	{
		JugunooUtil util = new JugunooUtil(getActivity());
		String deviceID = util.getUniqueDeviceID(getActivity());

		// gcmIDStr = mgr.GetValueFromSharedPrefs("GCM_ID");

		// boolean isSuccessValidation = checkValidation();
		boolean isSuccessValidation = makeValidation();
		// boolean isSuccessGcm = IsGcmSuccess();

		Log.i(TAG, TAG + " reg==" + isSuccessValidation + " gcm=" + " "
				+ usernameStr + " " + passwordStr + " " + mobileStr + " "
				+ emailStr + " " + deviceID);

		if (isSuccessValidation)
		{
			// if (isSuccessGcm)
			// {
			countryCodeStr = "+91";

			mgr.SaveValueToSharedPrefs("UserName", usernameStr);
			mgr.SaveValueToSharedPrefs("Mobile", mobileStr);
			mgr.SaveValueToSharedPrefs("Email", emailStr);

			HashMap<String, String> params = new HashMap<String, String>();
			params.put("FirstName", usernameStr);
			params.put("Mobile", mobileStr);
			params.put("Email", emailStr);
			params.put("DeviceId", deviceIDStr);
			params.put("Password", passwordStr);
			params.put("GcmRegistrationID", gcmIDStr);
			params.put("AccountType", "Personal");
			params.put("Country", countryCodeStr);

			// makeRegistrationReq(params);

			Intent intent = new Intent();
			intent.putExtra("params", params);
			intent.setClass(getActivity(), OtpVarificationActivity.class);
			startActivity(intent);
		}
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
					VolleyErrorHelper.getMessage(msg.obj, getActivity(), true);
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

				mgr.SaveValueToSharedPrefs("GCM_ID", "");
				mgr.SaveValueToSharedPrefs("isGcmSent", "");

				mgr.SaveValueToSharedPrefs("UserID", userID);
				mgr.SaveValueToSharedPrefs("Verify", "Pass");

				Intent intent = new Intent();
				intent.setClass(getActivity(), OtpVarificationActivity.class);
				startActivity(intent);
			}
			else
			{
				Function.showToast(getActivity(), ConstantMessages.MSG23);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private boolean makeValidation()
	{

		boolean flag = false;

		usernameStr = etUsername.getText().toString().trim();
		passwordStr = etPassword.getText().toString().trim();
		cpasswordStr = etCpassword.getText().toString().trim();
		mobileStr = etMobile.getText().toString().trim();

		String emailstr = etEmail.getText().toString();
		String emailvalidation = Validation.EMAIL_REGEX;

		if (!Validation.hasText(etUsername))
		{
			showTooltip(ConstantMessages.MSG104);
		}
		else if (usernameStr.length() < 3)
		{
			showTooltip(ConstantMessages.MSG10);
		}
		else if (!Validation.hasText(etPassword))
		{
			showTooltip(ConstantMessages.MSG105);
		}
		else if (passwordStr.length() < 3)
		{
			showTooltip(ConstantMessages.MSG20);
		}
		else if (!Validation.hasText(etCpassword))
		{
			showTooltip(ConstantMessages.MSG106);
		}
		else if (!cpasswordStr.equals(passwordStr))
		{
			showTooltip(ConstantMessages.MSG12);
		}
		else if (!Validation.hasText(etEmail))
		{
			showTooltip(ConstantMessages.MSG107);
		}
		else if (!emailstr.isEmpty() && !emailstr.matches(emailvalidation))
		{
			showTooltip(ConstantMessages.MSG14);
		}
		else if (!TextUtils.isEmpty(emailStr) && isEmailExist)
		{
			showTooltip(ConstantMessages.MSG15);
		}
		else if (!Validation.hasText(etMobile))
		{
			showTooltip(ConstantMessages.MSG108);
		}
		else if (mobileStr.length() != 10)
		{
			showTooltip(ConstantMessages.MSG16);
		}
		else if (emailstr.matches(emailvalidation) && isEmailExist)
		{
			Log.i("isEmailExist", TAG + " isEmailExist " + isEmailExist);
			showTooltip(ConstantMessages.MSG15);
		}
		else
		{
			flag = true;
		}

		return flag;

		// if (isMobileExist)
		// {
		// showTooltip(ConstantMessages.MSG23);
		// }

	}

	private void showLoadingDilog()
	{
		pdHandler = new Handler();
		pd = new TransparentProgressDialog(getActivity(),
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
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
	{
		if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER))
				|| (actionId == EditorInfo.IME_ACTION_DONE))
		{
			if (JugunooUtil.isConnectedToInternet(getActivity()))
			{
				register();
			}
			else
			{
				showTooltip(Global.networkErrorMsg);
			}
		}

		return false;
	}

	private void showTooltip(String message)
	{
		Function.showToast(getActivity(), message);
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
