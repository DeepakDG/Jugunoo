package com.hirecraft.jugunoo.passenger.fragments;

import java.util.HashMap;
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
import com.hirecraft.jugunoo.passenger.CompanyListActivity;
import com.hirecraft.jugunoo.passenger.CompanyListActivity_new;
import com.hirecraft.jugunoo.passenger.LandingPage;
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

import de.keyboardsurfer.android.widget.crouton.Crouton;

public class CorporateSignupFragment extends Fragment implements
		OnClickListener, OnEditorActionListener
{
	private static final String TAG = CorporateSignupFragment.class
			.getSimpleName();

	private EditText etUsername, etPassword, etCpassword, etEmail;

	public EditText etCompany;
	public EditText etGroupName;

	private EditText etMobile;
	private String usernameStr = "", passwordStr = "", cpasswordStr,
			companyStr = "", groupStr = "", emailStr = "", mobileStr = "",
			deviceIDStr = "", gcmIDStr = "", countryCodeStr = "";

	private Button btVerify;
	private ProgressBar progressEmail;
	private SharedPreferencesManager mgr;

	boolean isUsername = false, isPasswrd = false, isCpasswrd = false,
			isEmail = false;
	private Typeface light, bold, semibold;

	private final int JUGUNOO_EMAIL_CHECK = 11;
	private final int JUGUNOO_MOBILE_CHECK = 12;
	private final int JUGUNOO_DELAY_IN_MILLIS = 1000;

	private boolean isEmailExist, isMobileExist;
	private String availabilityField;
	private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

	private static TransparentProgressDialog pd;
	private static Handler pdHandler;
	private static Runnable pdRunnable;
	private static int REQUEST_CODE_COMPANY = 1;
	private static int REQUEST_CODE_GROUP = 2;

	private HashMap<String, String> params;

	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.fragment_corporate_signup, container,
				false);
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);

		// creating the device Id
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
				R.id.etNameCorporateSignup);
		etPassword = (EditText) getActivity().findViewById(
				R.id.etPwCorporateSignup);
		etCpassword = (EditText) getActivity().findViewById(
				R.id.etCpwCorporateSignup);

		etCompany = (EditText) getActivity().findViewById(
				R.id.etCompanyCorporateSignup);

		etGroupName = (EditText) getActivity().findViewById(
				R.id.etGroupnameCorporateSignup);

		etEmail = (EditText) getActivity().findViewById(
				R.id.etEmailCorporateSignup);
		etMobile = (EditText) getActivity().findViewById(
				R.id.etMobileCorporateSignup);

		btVerify = (Button) getActivity().findViewById(
				R.id.btVerifyCorporateSignup);

		progressEmail = (ProgressBar) getActivity().findViewById(
				R.id.progMailCorporateSignup);

		btVerify.setOnClickListener(this);
		etCompany.setOnClickListener(this);
		etGroupName.setOnClickListener(this);
		etEmail.addTextChangedListener(emailWatcher);

		Init();

	}

	@Override
	public void onResume()
	{
		super.onResume();
		checkPlayServices();
	}

	@Override
	public void onPause()
	{
		super.onPause();
		Crouton.cancelAllCroutons();
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

		etCompany.setTypeface(light);
		etGroupName.setTypeface(light);

		etEmail.setTypeface(light);
		etMobile.setTypeface(light);
	}

	private void clearFocus()
	{
		etUsername.clearFocus();
		etPassword.clearFocus();
		etCpassword.clearFocus();
		etCompany.clearFocus();
		etGroupName.clearFocus();
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
					emailStr = etEmail.getText().toString();

					if (!TextUtils.isEmpty(emailStr)
							&& emailStr.matches(Validation.EMAIL_REGEX))
					{
						String url = Global.JUGUNOO_WS
								+ "Passenger/ValidateParameter?CheckValue="
								+ emailStr.toString() + "&Parameter=Email";

						Log.d("JUGUNOO_EMAIL_CHECK", "JUGUNOO_EMAIL_CHECK "
								+ url);
						makeMailIdAvailabilityRequest(url, "Email");

					}
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
					isMobileExist = false;

					mobileStr = etMobile.getText().toString();

					Log.i("reg", "mob len=" + mobileStr.length());

					if (mobileStr.length() == 10 && validateForm())
					{
						String url = Global.JUGUNOO_WS
								+ "Passenger/ValidateParameter?CheckValue="
								+ mobileStr.toString() + "&Parameter=Mobile";

						Log.d("JUGUNOO_MOBILE_CHECK", "JUGUNOO_MOBILE_CHECK "
								+ url);
						makeMailIdAvailabilityRequest(url, "Mobile");
					}
				}
				else
				{
					Function.showToast(getActivity(), Global.networkErrorMsg);
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
				if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER))
						|| (actionId == EditorInfo.IME_ACTION_DONE))
				{
					String mobileStr = etMobile.getText().toString();

					if (mobileStr.length() != 10)
					{
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

	private void makeMailIdAvailabilityRequest(String url, String field)
	{
		if (field.equalsIgnoreCase("Email"))
		{
			progressEmail.setVisibility(View.VISIBLE);
		}

		availabilityField = field;
		params = new HashMap<String, String>();
		NetworkHandler.mailAvailabilityRequest(TAG, handlerMaildIdAvailability,
				params, url);
	}

	Handler handlerMaildIdAvailability = new Handler()
	{
		public void handleMessage(Message msg)
		{
			switch (msg.arg1)
			{
				case Constant.MessageState.AVAILABILITY_MAIL_NUMBER_SUCCESS:

					parserMaildIdAvailability((JSONObject) msg.obj);

					if (availabilityField.equalsIgnoreCase("Email"))
					{
						progressEmail.setVisibility(View.GONE);
					}
					break;

				case Constant.MessageState.FAIL:
					VolleyErrorHelper.getMessage(msg.obj, getActivity(), true);

					if (availabilityField.equalsIgnoreCase("Email"))
					{
						progressEmail.setVisibility(View.GONE);
					}
					break;

				default:
					break;
			}

		};
	};

	private void parserMaildIdAvailability(JSONObject obj)
	{
		try
		{
			String result = obj.getString("Result");
			if (result.equalsIgnoreCase("Pass"))
			{
				if (availabilityField.equalsIgnoreCase("Email"))
				{
					isEmail = true;
					isEmailExist = false;
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
			case R.id.btVerifyCorporateSignup:

				clearFocus();
				if (JugunooUtil.isConnectedToInternet(getActivity()))
				{
					register();
				}
				else
				{
					Function.showToast(getActivity(), ConstantMessages.MSG6);
				}
				break;

			case R.id.etCompanyCorporateSignup:

				Function.hideSoftKeyBoard(getActivity(), etCompany);

				String urlCompany = Global.JUGUNOO_WS
						+ "Passenger/GetCompanyName";

				Intent intent = new Intent(this.getActivity(),
						CompanyListActivity_new.class);
				intent.putExtra("searchHint", "Search Company");
				intent.putExtra("URL", urlCompany);
				intent.putExtra("successMsg",
						Constant.MessageState.COMPANY_NAMES_SUCCESS);

				this.startActivityForResult(intent, REQUEST_CODE_COMPANY);

				break;

			case R.id.etGroupnameCorporateSignup:

				if (TextUtils.isEmpty(etCompany.getText().toString().trim()))
				{
					showTooltip(ConstantMessages.MSG109);
				}
				else
				{
					String urlGroup = Global.JUGUNOO_WS
							+ "Passenger/GetGroupName?CompanyName="
							+ etCompany.getText().toString().trim();

					Intent intentGroup = new Intent(this.getActivity(),
							CompanyListActivity_new.class);

					// Intent intentGroup = new Intent(this.getActivity(),
					// GroupListActivity.class);

					intentGroup.putExtra("searchHint", "Search Groups");
					intentGroup.putExtra("URL", urlGroup);
					intentGroup.putExtra("successMsg",
							Constant.MessageState.GROUP_NAMES_SUCCESS);
					this.startActivityForResult(intentGroup, REQUEST_CODE_GROUP);
				}

				break;
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);

		if (data != null)
		{
			if (requestCode == REQUEST_CODE_COMPANY)
			{
				String companyName = data.getStringExtra("message");
				etCompany.setText(companyName);
			}
			else if (requestCode == REQUEST_CODE_GROUP)
			{
				String groupName = data.getStringExtra("message");
				etGroupName.setText(groupName);
			}
		}
	}

	private void register()
	{
		boolean isSuccessValidation = validateForm();

		Log.i(TAG, TAG + " reg==" + isSuccessValidation + usernameStr + " "
				+ passwordStr + " " + mobileStr + " " + emailStr + " "
				+ deviceIDStr + " " + gcmIDStr);

		if (isSuccessValidation)
		{
			countryCodeStr = "+91";

			mgr.SaveValueToSharedPrefs("UserName", usernameStr);
			mgr.SaveValueToSharedPrefs("Mobile", mobileStr);
			mgr.SaveValueToSharedPrefs("Email", emailStr);

			params = new HashMap<String, String>();
			params.put("FirstName", usernameStr);
			params.put("Mobile", mobileStr);
			params.put("Email", emailStr);
			params.put("DeviceId", deviceIDStr);
			params.put("Password", passwordStr);
			params.put("GcmRegistrationID", "");
			params.put("AccountType", "Corporate");
			params.put("Country", countryCodeStr);
			params.put("CompanyName", companyStr);
			params.put("GroupName", groupStr);

			makeRegistrationReq(params);
		}
	}

	private boolean validateForm()
	{

		boolean flag = false;

		usernameStr = etUsername.getText().toString().trim();
		passwordStr = etPassword.getText().toString().trim();
		cpasswordStr = etCpassword.getText().toString().trim();
		mobileStr = etMobile.getText().toString().trim();
		companyStr = etCompany.getText().toString().trim();
		groupStr = etGroupName.getText().toString().trim();

		emailStr = etEmail.getText().toString();
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
		else if (!Validation.hasText(etCompany))
		{
			showTooltip(ConstantMessages.MSG110);
		}
		else if (!Validation.hasText(etGroupName))
		{
			showTooltip(ConstantMessages.MSG111);
		}
		else if (!Validation.hasText(etEmail))
		{
			showTooltip(ConstantMessages.MSG107);
		}
		else if (!emailStr.isEmpty() && !emailStr.matches(emailvalidation))
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
		else if (emailStr.matches(emailvalidation) && isEmailExist)
		{
			Log.i("isEmailExist", TAG + " isEmailExist " + isEmailExist);
			showTooltip(ConstantMessages.MSG15);
		}
		else
		{
			flag = true;
		}
		return flag;
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
				mgr.SaveValueToSharedPrefs("UserID", userID);
				mgr.SaveValueToSharedPrefs("Verify", "Pass");

				startActivity(new Intent().setClass(getActivity(),
						LandingPage.class));
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
