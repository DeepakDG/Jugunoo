package com.hirecraft.jugunoo.passenger;

import java.util.HashMap;

import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
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
import android.widget.TextView.OnEditorActionListener;

import com.hirecraft.jugunoo.passenger.common.Constant;
import com.hirecraft.jugunoo.passenger.common.ConstantMessages;
import com.hirecraft.jugunoo.passenger.common.Function;
import com.hirecraft.jugunoo.passenger.common.TransparentProgressDialog;
import com.hirecraft.jugunoo.passenger.networkhandler.NetworkHandler;
import com.hirecraft.jugunoo.passenger.services.VolleyErrorHelper;

import de.keyboardsurfer.android.widget.crouton.Crouton;

public class ForgotPasswordActivity extends Activity implements OnClickListener
{
	private String TAG = ForgotPasswordActivity.class.getSimpleName();
	private EditText etMobileForgotPw;
	private Button btCancelForgotPw, btSubmitForgotPw;

	private Handler pdHandler;
	private Runnable pdRunnable;
	private TransparentProgressDialog pd;
	private String mobileStr;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		SetActionBar();
		setContentView(R.layout.activity_forgot_password);

		etMobileForgotPw = (EditText) findViewById(R.id.etMobileForgotPw);
		etMobileForgotPw.requestFocus();
		etMobileForgotPw
				.setOnEditorActionListener(new DoneOnEditorActionListener());

		btCancelForgotPw = (Button) findViewById(R.id.btCancelForgotPw);
		btSubmitForgotPw = (Button) findViewById(R.id.btSubmitForgotPw);

		btCancelForgotPw.setOnClickListener(this);
		btSubmitForgotPw.setOnClickListener(this);
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

		RelativeLayout l = new RelativeLayout(this);
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View bar = inflater.inflate(R.layout.custom_title_actionbar, l);

		LinearLayout back = (LinearLayout) bar.findViewById(R.id.llCTitle);
		TextView title = (TextView) bar.findViewById(R.id.tvTATitle);
		title.setText("Forgot Password");

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
		startActivity(new Intent().setClass(ForgotPasswordActivity.this,
				Login.class));
		ForgotPasswordActivity.this.finish();
	}

	private void showLoadingDilog()
	{
		// Showing loading dialog
		pdHandler = new Handler();
		pd = new TransparentProgressDialog(ForgotPasswordActivity.this,
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
		// Cancel loading dialog
		pdHandler.removeCallbacks(pdRunnable);

		if (pd.isShowing())
		{
			pd.dismiss();
		}
	}

	class DoneOnEditorActionListener implements OnEditorActionListener
	{
		@Override
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
		{
			if (actionId == EditorInfo.IME_ACTION_DONE)
			{
				Function.hideSoftKeyBoard(ForgotPasswordActivity.this);

				String fpwdStr = etMobileForgotPw.getText().toString();
				if (TextUtils.isEmpty(fpwdStr))
				{
					Function.showToast(ForgotPasswordActivity.this,
							ConstantMessages.MSG2);
				}
				else if (fpwdStr.length() != 10)
				{
					Function.showToast(ForgotPasswordActivity.this,
							ConstantMessages.MSG3);
				}
				else
				{
					fpwdStr = etMobileForgotPw.getText().toString();
					HashMap<String, String> params = new HashMap<String, String>();
					params.put("Mobile", fpwdStr);
					makeForgotPasswordReq(params);
				}
				return true;
			}
			return false;
		}
	}

	private void makeForgotPasswordReq(HashMap<String, String> params)
	{
		showLoadingDilog();
		NetworkHandler.forgotPasswordRequest(TAG, handlerForgotPw, params);
	}

	Handler handlerForgotPw = new Handler()
	{
		public void handleMessage(android.os.Message msg)
		{
			switch (msg.arg1)
			{
				case Constant.MessageState.FORGOT_PASSWORD_SUCCESS:
					cancelLoadingDialog();
					parserForgotPassword((JSONObject) msg.obj);
					break;

				case Constant.MessageState.FAIL:
					cancelLoadingDialog();
					VolleyErrorHelper.getMessage(msg.obj,
							ForgotPasswordActivity.this, true);
					break;
			}
		};
	};

	private void parserForgotPassword(JSONObject obj)
	{
		try
		{
			String result = obj.getString(Constant.RESULT);
			String message = obj.getString(Constant.MESSAGE);

			if (result.equalsIgnoreCase("Pass"))
			{
				Function.showToast(ForgotPasswordActivity.this, message);
				// "Password sent to your Mobile Number"

				new Handler().postDelayed(new Runnable()
				{
					@Override
					public void run()
					{
						ForgotPasswordActivity.this.finish();
						startActivity(new Intent().setClass(
								ForgotPasswordActivity.this, Login.class));
					}
				}, 3000);

			}
			else
			{
				Function.showToast(ForgotPasswordActivity.this, message);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0)
		{
			if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0)
			{
				startActivity(new Intent(ForgotPasswordActivity.this,
						Login.class));
				ForgotPasswordActivity.this.finish();
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
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

	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
			case R.id.btCancelForgotPw:
				startActivity(new Intent().setClass(
						ForgotPasswordActivity.this, Login.class));
				break;

			case R.id.btSubmitForgotPw:

				mobileStr = etMobileForgotPw.getText().toString();
				if (TextUtils.isEmpty(mobileStr))
				{
					Function.showToast(ForgotPasswordActivity.this,
							ConstantMessages.MSG2);
				}
				else if (mobileStr.length() != 10)
				{
					Function.showToast(ForgotPasswordActivity.this,
							ConstantMessages.MSG3);
				}
				else
				{
					mobileStr = etMobileForgotPw.getText().toString();
					HashMap<String, String> params = new HashMap<String, String>();
					params.put("Mobile", mobileStr);
					makeForgotPasswordReq(params);
				}
				break;
		}
	}
}
