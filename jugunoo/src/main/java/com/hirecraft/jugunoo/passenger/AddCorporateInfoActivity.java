package com.hirecraft.jugunoo.passenger;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hirecraft.jugunoo.passenger.common.Constant;
import com.hirecraft.jugunoo.passenger.common.ConstantMessages;
import com.hirecraft.jugunoo.passenger.common.Function;
import com.hirecraft.jugunoo.passenger.common.Global;
import com.hirecraft.jugunoo.passenger.common.TransparentProgressDialog;
import com.hirecraft.jugunoo.passenger.common.Validation;
import com.hirecraft.jugunoo.passenger.fragments.CorporateSignupFragment;
import com.hirecraft.jugunoo.passenger.fragments.ProfileFragment;
import com.hirecraft.jugunoo.passenger.networkhandler.NetworkHandler;
import com.hirecraft.jugunoo.passenger.services.VolleyErrorHelper;
import com.hirecraft.jugunoo.passenger.utility.JugunooUtil;
import com.hirecraft.jugunoo.passenger.utility.SharedPreferencesManager;

import de.keyboardsurfer.android.widget.crouton.Crouton;

public class AddCorporateInfoActivity extends Activity implements
		OnClickListener
{
	private static final String TAG = AddCorporateInfoActivity.class
			.getSimpleName();

	public EditText etCompany;
	public EditText etGroupName;
	private EditText etEmail;
	private Button btProceed;

	private ProgressBar progressBarEmail;
	private SharedPreferencesManager mgr;

	private static TransparentProgressDialog pd;
	private static Handler pdHandler;
	private static Runnable pdRunnable;
	private static int REQUEST_CODE_COMPANY = 1;
	private static int REQUEST_CODE_GROUP = 2;

	boolean isEmail = false;
	private Typeface light;

	private final int JUGUNOO_EMAIL_CHECK = 11;
	private final int JUGUNOO_DELAY_IN_MILLIS = 1000;

	boolean flag = false;
	private boolean isEmailExist;
	private String companyStr = "", groupStr = "", emailStr = "";
	private HashMap<String, String> params;
	private static String firstname;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_corporate_details);
		SetActionBar();
		init();
		GetParcellableData();
	}

	@Override
	public void onBackPressed()
	{
		super.onBackPressed();
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		Crouton.cancelAllCroutons();
		closeAnimation();
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
		View bar = inflater.inflate(R.layout.custom_signup_action_bar, l);

		RelativeLayout back = (RelativeLayout) bar
				.findViewById(R.id.rl_nav_signup);
		TextView title = (TextView) bar.findViewById(R.id.tvScreenTitleSignUp);
		ImageView imageView = (ImageView) bar.findViewById(R.id.imgSignupNext);
		title.setText("Corporate Details");

		back.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				onBackPressed();
			}
		});

		imageView.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{

			}
		});

		actionBar.setCustomView(bar, layoutParams);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowCustomEnabled(true);
		actionBar.setHomeButtonEnabled(false);
	}

	private void init()
	{

		mgr = new SharedPreferencesManager(AddCorporateInfoActivity.this);

		light = Typeface.createFromAsset(
				AddCorporateInfoActivity.this.getAssets(),
				"fonts/opensans-regular-webfont.ttf");

		etCompany = (EditText) findViewById(R.id.etCompanyAddCorporateInfo);
		etGroupName = (EditText) findViewById(R.id.etGroupnameAddCorporateInfo);
		etEmail = (EditText) findViewById(R.id.etEmailAddCorporateInfo);
		progressBarEmail = (ProgressBar) findViewById(R.id.progMailAddCorporateInfo);
		btProceed = (Button) findViewById(R.id.btProceedAddCorporateInfo);

		etCompany.setTypeface(light);
		etGroupName.setTypeface(light);
		etEmail.setTypeface(light);

		etEmail.clearFocus();

		etCompany.setOnClickListener(this);
		etGroupName.setOnClickListener(this);
		btProceed.setOnClickListener(this);
		etEmail.addTextChangedListener(emailWatcher);
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

	private void GetParcellableData()
	{
		Intent i = getIntent();
		if (i.getExtras() != null)
		{
			firstname = i.getStringExtra("FirstName");
		}
		else
		{

		}
	}

	private TextWatcher emailWatcher = new TextWatcher()
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
			emailStr = etEmail.getText().toString().trim();

			if (!TextUtils.isEmpty(emailStr)
					&& emailStr.matches(Validation.EMAIL_REGEX))
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

	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
			case R.id.etCompanyAddCorporateInfo:

				Function.hideSoftKeyBoard(AddCorporateInfoActivity.this,
						etCompany);

				String urlCompany = Global.JUGUNOO_WS
						+ "Passenger/GetCompanyName";

				Intent intent = new Intent(AddCorporateInfoActivity.this,
						CompanyListActivity_new.class);
				intent.putExtra("searchHint", "Search Company");
				intent.putExtra("URL", urlCompany);
				intent.putExtra("successMsg",
						Constant.MessageState.COMPANY_NAMES_SUCCESS);

				startActivityForResult(intent, REQUEST_CODE_COMPANY);

				break;

			case R.id.etGroupnameAddCorporateInfo:

				Function.hideSoftKeyBoard(AddCorporateInfoActivity.this,
						etGroupName);
				if (TextUtils.isEmpty(etCompany.getText().toString().trim()))
				{
					showTooltip(ConstantMessages.MSG109);
				}
				else
				{
					String urlGroup = Global.JUGUNOO_WS
							+ "Passenger/GetGroupName?CompanyName="
							+ etCompany.getText().toString().trim();

					Intent intentGroup = new Intent(
							AddCorporateInfoActivity.this,
							CompanyListActivity_new.class);

					// Intent intentGroup = new
					// Intent(this.AddCorporateInfoActivity.this,
					// GroupListActivity.class);

					intentGroup.putExtra("searchHint", "Search Groups");
					intentGroup.putExtra("URL", urlGroup);
					intentGroup.putExtra("successMsg",
							Constant.MessageState.GROUP_NAMES_SUCCESS);
					startActivityForResult(intentGroup, REQUEST_CODE_GROUP);
				}
				break;
			case R.id.btProceedAddCorporateInfo:

				etEmail.clearFocus();
				if (JugunooUtil
						.isConnectedToInternet(AddCorporateInfoActivity.this))
				{
					saveCorporateDetails();
				}
				else
				{
					Function.showToast(AddCorporateInfoActivity.this,
							ConstantMessages.MSG6);
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
				etGroupName.setText("");
			}
			else if (requestCode == REQUEST_CODE_GROUP)
			{
				String groupName = data.getStringExtra("message");
				etGroupName.setText(groupName);
			}
		}
	}

	private void saveCorporateDetails()
	{
		if (validateForm())
		{
			mgr.SaveValueToSharedPrefs("CompanyName", companyStr);
			mgr.SaveValueToSharedPrefs("GroupName", groupStr);
			mgr.SaveValueToSharedPrefs("Email", emailStr);

			params = new HashMap<String, String>();
			params.put("UserId", mgr.GetValueFromSharedPrefs("UserId"));
			params.put("FirstName", firstname);
			params.put("Email", emailStr);
			params.put("CompanyId", companyStr);
			params.put("GroupId", groupStr);

			// params.put("AccountType", "Corporate");
			makeCorporateRegistrationReq(params);
		}
	}

	private void makeCorporateRegistrationReq(HashMap<String, String> params)
	{
		showLoadingDilog();
		NetworkHandler.updateUserProfile(TAG, handlerRegister, params);
	}

	Handler handlerRegister = new Handler()
	{
		public void handleMessage(Message msg)
		{
			switch (msg.arg1)
			{
				case Constant.MessageState.UPDATEPROFILE_STATUS_SUCCESS:
					cancelLoadingDialog();
					parserCorporateRegistration((JSONObject) msg.obj);
					break;

				case Constant.MessageState.UPDATEPROFILE_STATUS_FAIL:
					cancelLoadingDialog();
					VolleyErrorHelper.getMessage(msg.obj,
							AddCorporateInfoActivity.this, false);
				default:
					break;
			}
		};
	};

	private void parserCorporateRegistration(JSONObject obj)
	{
		try
		{
			Log.d(TAG, "parse  " + obj.toString());
			String result = obj.getString(Constant.RESULT);
			String message = obj.getString(Constant.MESSAGE);

			if (result.equalsIgnoreCase(Constant.Result_STATE.PASS))
			{

				// ------------read response from the server----------------
				String userID = obj.getString("UserId");
				mgr.SaveValueToSharedPrefs("UserID", userID);
				mgr.SaveValueToSharedPrefs("Verify", "Pass");

				// showTooltip(message);

				new Handler().postDelayed(new Runnable()
				{
					@Override
					public void run()
					{
						// AddCorporateInfoActivity.this.finish();
						// onBackPressed();
						startActivity(new Intent(AddCorporateInfoActivity.this,
								ProfileFragment.class));
						AddCorporateInfoActivity.this.finish();
					}
				}, 3000);

			}
			else
			{
				Function.showToast(AddCorporateInfoActivity.this,
						ConstantMessages.MSG23);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private boolean validateForm()
	{
		companyStr = etCompany.getText().toString().trim();
		groupStr = etGroupName.getText().toString().trim();
		emailStr = etEmail.getText().toString();
		String emailvalidation = Validation.EMAIL_REGEX;

		if (!Validation.hasText(etCompany))
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

	Handler handler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			if (msg.what == JUGUNOO_EMAIL_CHECK)
			{
				if (JugunooUtil
						.isConnectedToInternet(AddCorporateInfoActivity.this))
				{
					emailStr = etEmail.getText().toString().trim();

					String url = Global.JUGUNOO_WS
							+ "Passenger/ValidateParameter?CheckValue="
							+ emailStr + "&Parameter=Email";

					Log.d("JUGUNOO_EMAIL_CHECK", "JUGUNOO_EMAIL_CHECK " + url);
					makeMailIdAvailabilityRequest(url, "Email");
				}
				else
				{
					Function.showToast(AddCorporateInfoActivity.this,
							Global.networkErrorMsg);
				}
			}

		};
	};

	private void makeMailIdAvailabilityRequest(String url, String field)
	{
		progressBarEmail.setVisibility(View.VISIBLE);
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

					progressBarEmail.setVisibility(View.GONE);
					parserMaildIdAvailability((JSONObject) msg.obj);

					break;

				case Constant.MessageState.FAIL:

					VolleyErrorHelper.getMessage(msg.obj,
							AddCorporateInfoActivity.this, true);
					progressBarEmail.setVisibility(View.GONE);
					break;
			}
		};
	};

	private void parserMaildIdAvailability(JSONObject obj)
	{
		try
		{
			String result = obj.getString(Constant.RESULT);
			String message = obj.getString(Constant.MESSAGE);

			if (result.equalsIgnoreCase(Constant.Result_STATE.PASS))
			{
				isEmail = true;
				isEmailExist = false;
			}
			else
			{
				isEmailExist = true;
				showTooltip(message);
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
		pd = new TransparentProgressDialog(AddCorporateInfoActivity.this,
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
		Function.showToast(AddCorporateInfoActivity.this, message);
	}

}
