package com.hirecraft.jugunoo.passenger.fragments;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
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
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hirecraft.jugunoo.passenger.Login;
import com.hirecraft.jugunoo.passenger.ProfileAdminActivity;
import com.hirecraft.jugunoo.passenger.ProfileManagerActivity;
import com.hirecraft.jugunoo.passenger.R;
import com.hirecraft.jugunoo.passenger.common.Constant;
import com.hirecraft.jugunoo.passenger.common.ConstantMessages;
import com.hirecraft.jugunoo.passenger.common.Function;
import com.hirecraft.jugunoo.passenger.common.Global;
import com.hirecraft.jugunoo.passenger.common.TransparentProgressDialog;
import com.hirecraft.jugunoo.passenger.common.Validation;
import com.hirecraft.jugunoo.passenger.networkhandler.NetworkHandler;
import com.hirecraft.jugunoo.passenger.utility.SharedPreferencesManager;

import de.keyboardsurfer.android.widget.crouton.Crouton;

public class ChangePasswordActivity extends Activity
{
	private static final String TAG = ChangePasswordActivity.class
			.getSimpleName();

	private EditText oldPwd, newPwd, cnfPwd;
	Button leftBtn;
	Button rightBtn;
	private Typeface light, bold;
	private Runnable pdRunnable;
	private SharedPreferencesManager mgr;
	private TransparentProgressDialog pd;
	private Handler pdHandler;
	private String userId;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.jugunoo_changepwd_dialog);
		SetActionBar();
		mgr = new SharedPreferencesManager(getApplicationContext());
		init();
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		Crouton.cancelAllCroutons();
	}

	private void init()
	{
		pd = new TransparentProgressDialog(ChangePasswordActivity.this,
				R.drawable.loading_image);

		light = Typeface.createFromAsset(getAssets(),
				"fonts/opensans-regular-webfont.ttf");
		bold = Typeface.createFromAsset(getAssets(),
				"fonts/opensans-bold-webfont.ttf");

		pdHandler = new Handler();

		oldPwd = (EditText) findViewById(R.id.oldPwd);
		newPwd = (EditText) findViewById(R.id.newPwd);
		cnfPwd = (EditText) findViewById(R.id.cnfPwd);
		leftBtn = (Button) findViewById(R.id.cancelChBtn);
		rightBtn = (Button) findViewById(R.id.submitChBtn);

		oldPwd.addTextChangedListener(oldPwdWatcher);
		newPwd.addTextChangedListener(passwordWatcher);
		cnfPwd.addTextChangedListener(cpasswordWatcher);

		oldPwd.setOnFocusChangeListener(new View.OnFocusChangeListener()
		{
			@Override
			public void onFocusChange(View v, boolean hasFocus)
			{
				if (hasFocus)
				{
					getWindow()
							.setSoftInputMode(
									WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
				}
			}
		});

		cnfPwd.setOnEditorActionListener(new EditText.OnEditorActionListener()
		{

			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event)
			{
				if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER))
						|| (actionId == EditorInfo.IME_ACTION_NEXT))
				{
					String cnfPwdStr = cnfPwd.getText().toString();
					String passwordStr = newPwd.getText().toString();
					int i = lastIndexOfUCL(cnfPwdStr);
					if (!cnfPwdStr.equalsIgnoreCase(passwordStr))
					{
						cnfPwd.setError(ConstantMessages.MSG12);
					}
					else if (i == -1)
					{
						cnfPwd.setError(ConstantMessages.MSG71);
					}
				}
				return false;
			}
		});

		oldPwd.setTypeface(light);
		newPwd.setTypeface(light);
		cnfPwd.setTypeface(light);
		leftBtn.setTypeface(bold);
		rightBtn.setTypeface(bold);

		leftBtn.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
//				startActivity(new Intent().setClass(
//						ChangePasswordActivity.this, ProfileManagerActivity.class));
//				ChangePasswordActivity.this.finish();
				onBackPressed();
			}
		});

		rightBtn.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				String oldPwdStr = oldPwd.getText().toString();
				String newPwdStr = newPwd.getText().toString();
				String confPwdStr = cnfPwd.getText().toString();

				// if(oldPwdStr.equalsIgnoreCase(string))
				if (Validation.hasText(oldPwd) && Validation.hasText(newPwd)
						&& Validation.hasText(cnfPwd))
				{

					if (!oldPwdStr.equalsIgnoreCase("")
							&& !newPwdStr.equalsIgnoreCase(""))
					{

						if (oldPwdStr.equals(newPwdStr))
						{
							showDialog(getResources().getString(
									R.string.newOldSame));
						}
						else
						{
							if (confPwdStr.equals(newPwdStr))
							{

								// Function.hideSoftKeyBoard(ChangePasswordActivity.this);

								String userId = mgr
										.GetValueFromSharedPrefs("UserID");
								ChangePassword(userId, oldPwdStr, newPwdStr);
							}
							else
							{
								showDialog(getResources().getString(
										R.string.pwdNotMatch));
							}
						}
					}
					else
					{
						showDialog(getResources().getString(
								R.string.enterRequired));
						Log.i(Global.APPTAG, "Invalid input");
					}

				}
				else
				{
					showDialog(getResources().getString(R.string.enterRequired));
					Log.i(Global.APPTAG, "Invalid input");
				}

			}
		});

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
		title.setText(ConstantMessages.MSG73);

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

	public void showDialog(String message)
	{
		Function.showToast(ChangePasswordActivity.this, message);
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

	private void ParseChangePwd(JSONObject object)
	{
		try
		{
			String resultStr = object.getString("Result").trim();
			if (resultStr.equalsIgnoreCase("Pass"))
			{
				userId = mgr.GetValueFromSharedPrefs("UserID");

				// Function.showToast(ChangePasswordActivity.this,
				// "Password Updated Successfully...");

				Function.hideSoftKeyBoard(ChangePasswordActivity.this);

				Map<String, String> params = new HashMap<String, String>();
				params.put("PassengerId", userId);
				NetworkHandler.logoutRequest(TAG, handlerLogout, params);

			}
			else if (resultStr.equalsIgnoreCase("Fail"))
			{
				showDialog(getResources().getString(R.string.OldPwdNotMatch));
				if (pd.isShowing())
				{
					pd.dismiss();
				}
			}

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void JugunooInteractiveDialog(String title, String message)
	{
		final Dialog dialog = new Dialog(ChangePasswordActivity.this);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		Window window = dialog.getWindow();
		window.setBackgroundDrawableResource(android.R.color.transparent);
		LinearLayout.LayoutParams dialogParams = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View dislogView = inflater.inflate(R.layout.jugunoo_interactive_dialog,
				new LinearLayout(ChangePasswordActivity.this));
		dialog.setContentView(dislogView, dialogParams);

		TextView textView = (TextView) dialog.findViewById(R.id.messageText);
		Button centerBtn = (Button) dialog.findViewById(R.id.centerBtn);
		Button leftBtn = (Button) dialog.findViewById(R.id.leftBtn);
		Button rightBtn = (Button) dialog.findViewById(R.id.rightBtn);

		textView.setTypeface(light);
		centerBtn.setTypeface(bold);
		leftBtn.setTypeface(bold);
		rightBtn.setTypeface(bold);

		if (title.equalsIgnoreCase("PASSWORD"))
		{
			leftBtn.setVisibility(View.GONE);
			rightBtn.setVisibility(View.GONE);
			String ok = getResources().getString(R.string.ok);
			textView.setText(message);
			centerBtn.setText(ok);
			centerBtn.setOnClickListener(new OnClickListener()
			{

				@Override
				public void onClick(View v)
				{
					dialog.dismiss();

					userId = mgr.GetValueFromSharedPrefs("UserID");

					Intent logoutIntent = new Intent(
							ChangePasswordActivity.this, Login.class);
					logoutIntent.addFlags(Intent.FLAG_ACTIVITY_TASK_ON_HOME);
					startActivity(logoutIntent);
					ChangePasswordActivity.this.finish();

					// Map<String, String> params = new HashMap<String,
					// String>();
					// params.put("PassengerId", userId);
					// NetworkHandler.logoutRequest(TAG, handlerLogout, params);

				}
			});
		}
		else if (title.equalsIgnoreCase("NETWORK"))
		{

			leftBtn.setVisibility(View.GONE);
			rightBtn.setVisibility(View.GONE);
			String ok = getResources().getString(R.string.ok);
			textView.setText(message);
			centerBtn.setText(ok);
			centerBtn.setOnClickListener(new OnClickListener()
			{

				@Override
				public void onClick(View v)
				{
					dialog.dismiss();
				}
			});
		}
		else if (title.equalsIgnoreCase("CANCEL"))
		{

			leftBtn.setVisibility(View.GONE);
			rightBtn.setVisibility(View.GONE);
			String ok = getResources().getString(R.string.ok);
			textView.setText(message);
			centerBtn.setText(ok);
			centerBtn.setOnClickListener(new OnClickListener()
			{

				@Override
				public void onClick(View v)
				{
					dialog.dismiss();
				}
			});
		}
		dialog.show();
	}

	private TextWatcher oldPwdWatcher = new TextWatcher()
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
			Validation.hasText(oldPwd);
		}
	};

	private TextWatcher passwordWatcher = new TextWatcher()
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
			Validation.hasText(newPwd);
		}
	};

	private TextWatcher cpasswordWatcher = new TextWatcher()
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
			Validation.hasText(cnfPwd);
		}
	};

	private Handler handler = new Handler(new Handler.Callback()
	{

		@Override
		public boolean handleMessage(Message msg)
		{
			switch (msg.arg1)
			{
				case Constant.MessageState.CHANGE_PWD_SUCCESS:
					pdHandler.removeCallbacks(pdRunnable);

					// if (pd.isShowing())
					// {
					// pd.dismiss();
					// }

					JSONObject fleet = (JSONObject) msg.obj;
					ParseChangePwd(fleet);
					break;

				case Constant.MessageState.CHANGE_PWD_FAIL:
					pdHandler.removeCallbacks(pdRunnable);
					if (pd.isShowing())
					{
						pd.dismiss();
					}
					showDialog(ConstantMessages.MSG72);
					Log.e(TAG, ConstantMessages.MSG72);
					break;

			}

			return false;
		}
	});

	private void ChangePassword(String userId, String oldPwd, String newPwd)
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

		Map<String, String> params = new HashMap<String, String>();
		params.put("UserId", userId);
		params.put("OldPassword", oldPwd);
		params.put("NewPassword", newPwd);

		NetworkHandler.ChangePwd(TAG, handler, params);

	}

	Handler handlerLogout = new Handler()
	{
		public void handleMessage(Message msg)
		{

			switch (msg.arg1)
			{
				case Constant.MessageState.LOGOUT_SUCCESS:

					mgr.SaveValueToSharedPrefs("UserID", "");
					mgr.SaveValueToSharedPrefs("UserRole", "");
					mgr.SaveValueToSharedPrefs("FleetUserType", "");
					mgr.savePreferenceIndex(Constant.PREFERENCE_INDEX, 0);
					ProfileFragment.gName = "";
					ProfileFragment.gMobile = "";
					ProfileFragment.gEmail = "";

					if (pd.isShowing())
					{
						pd.dismiss();
					}

					JugunooInteractiveDialog("PASSWORD", getResources()
							.getString(R.string.loginAgain));

					// new Handler().postDelayed(new Runnable()
					// {
					// @Override
					// public void run()
					// {
					//
					// Intent logoutIntent = new Intent(
					// ChangePasswordActivity.this, Login.class);
					// logoutIntent
					// .addFlags(Intent.FLAG_ACTIVITY_TASK_ON_HOME);
					// startActivity(logoutIntent);
					// ChangePasswordActivity.this.finish();
					// }
					// }, 300);

					break;

				case Constant.MessageState.LOGOUT_FAIL:
					if (pd.isShowing())
					{
						pd.dismiss();
					}

					showDialog(getString(R.string.unable_to_logout));
					break;
			}
		};

	};

	@Override
	public void onStop()
	{
		super.onStop();
		Function.hideSoftKeyBoard(ChangePasswordActivity.this);
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		Function.hideSoftKeyBoard(ChangePasswordActivity.this);
	}

}
