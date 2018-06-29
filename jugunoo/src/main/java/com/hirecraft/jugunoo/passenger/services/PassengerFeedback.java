package com.hirecraft.jugunoo.passenger.services;

import java.util.HashMap;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hirecraft.jugunoo.passenger.LandingPage;
import com.hirecraft.jugunoo.passenger.R;
import com.hirecraft.jugunoo.passenger.common.Constant;
import com.hirecraft.jugunoo.passenger.common.Function;
import com.hirecraft.jugunoo.passenger.common.Global;
import com.hirecraft.jugunoo.passenger.common.TransparentProgressDialog;
import com.hirecraft.jugunoo.passenger.networkhandler.NetworkHandler;
import com.hirecraft.jugunoo.passenger.utility.SharedPreferencesManager;

public class PassengerFeedback extends Activity
{

	private String TAG = PassengerFeedback.class.getSimpleName();
	private String rate = "";
	private Button submit;
	private EditText rateText;
	private TextView driverNameF, title, ratetitle, driverNamelbl, startLoc,
			startLoclbl, endLoc, endLoclbl;
	private static TransparentProgressDialog pd;
	private static Handler pdHandler;
	private static Runnable pdRunnable;
	private SharedPreferencesManager mgr;
	private Typeface light, bold, semibold;

	private boolean isFavouriteDriver;
	private CheckBox imgFavouriteDriver;

	@SuppressLint("SimpleDateFormat")
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		SetActionBar();

		// ActionBar actionBar = getActionBar();
		// actionBar.setDisplayUseLogoEnabled(true);
		// actionBar.setDisplayShowCustomEnabled(true);
		// LayoutParams jugunooActionbar = new LayoutParams(
		// LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		// jugunooActionbar.setMargins(0, 0, 0, 0);
		//
		// LayoutInflater inflater = (LayoutInflater)
		// getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		// RelativeLayout lay = new RelativeLayout(PassengerFeedback.this);
		// View jugunooTitleBar = inflater
		// .inflate(R.layout.jugunoo_actionbar, lay);
		// actionBar.setCustomView(jugunooTitleBar, jugunooActionbar);

		setContentView(R.layout.activity_passenger_feedback_page_new);
		pdHandler = new Handler();
		pd = new TransparentProgressDialog(PassengerFeedback.this,
				R.drawable.loading_image);
		Init();
		RatingBar jugunooRating = (RatingBar) findViewById(R.id.jugunooRating);
		rateText = (EditText) findViewById(R.id.feedbackText);
		driverNameF = (TextView) findViewById(R.id.fareText);
		driverNamelbl = (TextView) findViewById(R.id.driverLabel);
		startLoc = (TextView) findViewById(R.id.startPonitf);
		startLoclbl = (TextView) findViewById(R.id.startPonitfLabel);
		endLoc = (TextView) findViewById(R.id.endPonitf);
		endLoclbl = (TextView) findViewById(R.id.endPonitfLabel);
		title = (TextView) findViewById(R.id.titleF);
		ratetitle = (TextView) findViewById(R.id.rateLable);
		submit = (Button) findViewById(R.id.feedbackBtn);

		imgFavouriteDriver = (CheckBox) findViewById(R.id.imgFavouriteDriver);
		// jugunooRating.setRating(1);
		// jugunooRating.setStepSize(1);
		rate = "0.5";
		rateText.setTypeface(light);
		driverNamelbl.setTypeface(light);
		startLoclbl.setTypeface(light);
		endLoclbl.setTypeface(light);
		ratetitle.setTypeface(semibold);
		submit.setTypeface(bold);
		title.setTypeface(bold);
		driverNameF.setTypeface(semibold);
		startLoc.setTypeface(semibold);
		endLoc.setTypeface(semibold);

		mgr = new SharedPreferencesManager(getApplicationContext());
		String time = mgr.GetValueFromSharedPrefs("EngageTime");
		String pick = mgr.GetValueFromSharedPrefs("fPick");
		String drop = mgr.GetValueFromSharedPrefs("fDrop");
		// long timestamp = Long.parseLong(time) / 1000;
		// String engTime = Epoch2DateString(timestamp,
		// "dd-MM-yyyy HH:mm:ss aa");
		driverNameF.setText(Function.getDateTimeFromUTC(time,
				"dd-MM-yyyy HH:mm"));
		startLoc.setText(pick);
		endLoc.setText(drop);

		rateText.setOnTouchListener(new OnTouchListener()
		{

			@Override
			public boolean onTouch(View v, MotionEvent event)
			{
				PassengerFeedback.this
						.getWindow()
						.setSoftInputMode(
								WindowManager.LayoutParams.SOFT_INPUT_ADJUST_UNSPECIFIED);
				return false;
			}
		});

		jugunooRating
				.setOnRatingBarChangeListener(new OnRatingBarChangeListener()
				{

					@Override
					public void onRatingChanged(RatingBar ratingBar,
							float rating, boolean fromUser)
					{

						rate = String.valueOf(rating);
					}
				});

		rateText.setOnEditorActionListener(new EditText.OnEditorActionListener()
		{

			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event)
			{
				if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER))
						|| (actionId == EditorInfo.IME_ACTION_DONE))
				{
					String info = rateText.getText().toString();
					validation(info);
				}
				return false;
			}
		});

		submit.setOnClickListener(new View.OnClickListener()
		{

			@Override
			public void onClick(View v)
			{

				String info = rateText.getText().toString();
				validation(info);
			}
		});

		imgFavouriteDriver
				.setOnCheckedChangeListener(new OnCheckedChangeListener()
				{

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked)
					{

						if (isChecked)
						{
							isFavouriteDriver = true;
						}
						else
						{
							isFavouriteDriver = false;
						}

						Function.showToast(PassengerFeedback.this,
								"Favourite..? " + isFavouriteDriver);

					}
				});

	}

	private void Init()
	{
		light = Typeface.createFromAsset(getAssets(),
				"fonts/opensans-regular-webfont.ttf");
		bold = Typeface.createFromAsset(getAssets(),
				"fonts/opensans-bold-webfont.ttf");
		semibold = Typeface.createFromAsset(getAssets(),
				"fonts/opensans-semibold-webfont.ttf");

		clearNotification();

	}

	private void clearNotification()
	{
		NotificationManager notificationManager = (NotificationManager) getApplicationContext()
				.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancelAll();

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
		title.setText("Send Feedback");

		back.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Global.ISCAB_HIRED = false;
				mgr.SaveValueToSharedPrefs("TripStatus", "");
				Intent intent = new Intent(PassengerFeedback.this,
						LandingPage.class);
				intent.putExtra("from", "feedback");
				startActivity(intent);
				PassengerFeedback.this.finish();
			}
		});

		actionBar.setCustomView(bar, layoutParams);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowCustomEnabled(true);
		actionBar.setHomeButtonEnabled(false);
	}

	private void showLoadingDilog()
	{
		// Showing loading dialog

		pdHandler = new Handler();
		pd = new TransparentProgressDialog(PassengerFeedback.this,
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

	@Override
	public void finish()
	{
		if (pd != null)
		{
			if (pd.isShowing())
			{
				pd.dismiss();
			}
		}
		super.finish();
	}

	private void validation(String info)
	{
		if (TextUtils.isEmpty(rate))
		{
			showDialog("Rate your Trip Experience.");
		}
		else
		{
			String userId = mgr.GetValueFromSharedPrefs("UserID");
			String engageID = mgr.GetValueFromSharedPrefs("EngID");
			String driverID = mgr.GetValueFromSharedPrefs("DriverID");

			Log.d("Feedback", "Feedback userId=" + userId);
			Log.d("Feedback", "Feedback engageID=" + engageID);

			if (!TextUtils.isEmpty(userId) && !TextUtils.isEmpty(engageID))
			{
				// RequestParams params = new RequestParams();
				HashMap<String, String> params = new HashMap<String, String>();

				params.put("EngId", engageID);
				params.put("UserId", userId);
				params.put("FeedBack", info);
				params.put("Rating", rate);
				params.put("DriverId", driverID);

				params.put("RID", "0");

				if (isFavouriteDriver)
				{
					params.put("FavouriteStatus", "1");
				}
				else
				{
					params.put("FavouriteStatus", "0");
				}

				// String feedbackUrl = Global.JUGUNOO_WS +
				// "Passenger/Feedback";
				// SendFeed(feedbackUrl, params);
				makeFeedbackReq(params);

			}
			else
			{
				showDialog("Invalid User ID or Trip ID.");
			}
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{

		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0)
		{
			// Intent i = new Intent(PassengerFeedback.this, LandingPage.class);
			// Global.ISCAB_HIRED = false;
			// i.putExtra("from", "feedback");
			// startActivity(i);
			// PassengerFeedback.this.finish();

			Global.ISCAB_HIRED = false;
			mgr.SaveValueToSharedPrefs("TripStatus", "");
			Intent intent = new Intent(PassengerFeedback.this,
					LandingPage.class);
			intent.putExtra("from", "feedback");
			startActivity(intent);
			PassengerFeedback.this.finish();

			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	// private void SendFeed(String url, RequestParams params)
	// {
	//
	// AsyncHttpClient client = new AsyncHttpClient();
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
	// client.setTimeout(30000);
	// client.post(url, params, new AsyncHttpResponseHandler()
	// {
	//
	// @Override
	// @Deprecated
	// public void onFailure(int statusCode, Throwable error,
	// String content)
	// {
	// if (statusCode == 0)
	// {
	// showDialog(getString(R.string.network_error_msg));
	// }
	// else
	// showDialog("Unable to Submit.");
	//
	// super.onFailure(statusCode, error, content);
	// pdHandler.removeCallbacks(pdRunnable);
	// if (pd.isShowing())
	// {
	// pd.dismiss();
	// }
	// }
	//
	// @Override
	// @Deprecated
	// public void onSuccess(int statusCode, String content)
	// {
	// try
	// {
	// if (statusCode == 200)
	// {
	//
	// JSONObject obj = new JSONObject(content);
	// String result = obj.getString("Result");
	// if (result.equalsIgnoreCase("Pass"))
	// {
	// // showDialog("Feedback submitted.");
	// Global.ISCAB_HIRED = false;
	// mgr.SaveValueToSharedPrefs("TripStatus", "");
	// Intent intent = new Intent(PassengerFeedback.this,
	// LandingPage.class);
	// intent.putExtra("from", "feedback");
	// startActivity(intent);
	// PassengerFeedback.this.finish();
	// }
	// else
	// showDialog("Unable to Submit.");
	// }
	// else
	// {
	// showDialog("Unable to Submit.");
	// }
	//
	// }
	// catch (Exception bug)
	// {
	// bug.printStackTrace();
	// }
	//
	// pdHandler.removeCallbacks(pdRunnable);
	// if (pd.isShowing())
	// {
	// pd.dismiss();
	// }
	// }
	//
	// });
	//
	// }

	private void makeFeedbackReq(HashMap<String, String> params)
	{
		showLoadingDilog();
		NetworkHandler.feedBackRequest(TAG, handlerSendFeedback, params);
	}

	Handler handlerSendFeedback = new Handler()
	{
		public void handleMessage(android.os.Message msg)
		{

			switch (msg.arg1)
			{
				case Constant.MessageState.FEEDBACK_SUCCESS:
					cancelLoadingDialog();
					parseFeedback((JSONObject) msg.obj);
					break;
				case Constant.MessageState.FAIL:
					cancelLoadingDialog();
					VolleyErrorHelper.getMessage(msg.obj,
							PassengerFeedback.this, true);
					break;

				default:
					break;
			}

		};

	};

	private void parseFeedback(JSONObject obj)
	{
		try
		{
			String result = obj.getString("Result");
			if (result.equalsIgnoreCase("Pass"))
			{
				// showDialog("Feedback submitted.");
				Global.ISCAB_HIRED = false;
				mgr.SaveValueToSharedPrefs("TripStatus", "");
				Intent intent = new Intent(PassengerFeedback.this,
						LandingPage.class);
				intent.putExtra("from", "feedback");
				startActivity(intent);
				PassengerFeedback.this.finish();
			}
			else
			{
				showDialog("Unable to Submit.");
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	public void showDialog(String message)
	{
		Function.showToast(PassengerFeedback.this, message);
	}

}
