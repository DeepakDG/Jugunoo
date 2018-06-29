package com.hirecraft.jugunoo.passenger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.hirecraft.jugunoo.passenger.adapter.PendingRequestCustomAdapter;
import com.hirecraft.jugunoo.passenger.adapter.PendingRequestModel;
import com.hirecraft.jugunoo.passenger.common.Constant;
import com.hirecraft.jugunoo.passenger.common.Function;
import com.hirecraft.jugunoo.passenger.common.TransparentProgressDialog;
import com.hirecraft.jugunoo.passenger.interfaces.PendingOnClickInterface;
import com.hirecraft.jugunoo.passenger.networkhandler.NetworkHandler;
import com.hirecraft.jugunoo.passenger.utility.SharedPreferencesManager;

public class PendingRequest extends FragmentActivity implements
		PendingOnClickInterface
{

	private ListView PendingItemsList;
	PendingRequestCustomAdapter PendingItemsAdapter;
	ArrayList<PendingRequestModel> PendingItemsArray;
	private SharedPreferencesManager mgr;
	private String TAG = PendingRequest.class.getSimpleName();
	private TransparentProgressDialog pd;
	private Handler pdHandler;
	private Runnable pdRunnable;
	private LinearLayout Netstate_layout, Pendingmain_layout;
	private Button retryBtn;
	private TextView Emptyview;
	Map<String, String> params;
	Gson gsonpending = new Gson();
	private static int selected_statuspos;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		SetActionBar();
		setContentView(R.layout.pendingrequest);
		Init();

	}

	private void Init()
	{
		mgr = new SharedPreferencesManager(getApplicationContext());
		pd = new TransparentProgressDialog(getApplicationContext(),
				R.drawable.loading_image);

		params = new HashMap<String, String>();
		PendingItemsArray = new ArrayList<PendingRequestModel>();
		Netstate_layout = (LinearLayout) findViewById(R.id.pending_ErrorState);
		Pendingmain_layout = (LinearLayout) findViewById(R.id.pending_mainll);
		retryBtn = (Button) findViewById(R.id.pending_retrybtn);
		Emptyview = (TextView) findViewById(R.id.empty_list);
		pendingServiceCall();

		retryBtn.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				pendingServiceCall();
			}
		});
	}

	private void pendingServiceCall()
	{
		showLoadingDilog();
		String userID = mgr.GetValueFromSharedPrefs("UserID");
		NetworkHandler.GetUserGroupStatus(TAG, handler, userID);
	}

	Handler handler = new Handler()
	{
		public void handleMessage(android.os.Message msg)
		{
			switch (msg.arg1)
			{
				case Constant.MessageState.USERGROUP_STATUS_SUCCESS:
					try
					{
						cancelLoadingDialog();
						manageView(true);
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
					prepareGroupUserStatus((JSONObject) msg.obj);
					break;

				case Constant.MessageState.PENDINGUSERS_STATUS_SUCCESS:
					try
					{
						cancelLoadingDialog();
						parseSelectedStatusItem((JSONObject) msg.obj);
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
					break;

				case Constant.MessageState.FAIL:
					try
					{
						cancelLoadingDialog();
						manageView(false);
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
					// Function.showToast(getActivity(),
					// Constant.network_err_msg);
					Log.e(TAG, "Null Fleets");
					break;
			}
		}

		private void prepareGroupUserStatus(JSONObject obj)
		{
			try
			{
				if (obj.getString("Result").equalsIgnoreCase("pass"))
				{
					Emptyview.setVisibility(View.GONE);
					JSONArray array = obj.getJSONArray("Details");
					int len = array.length();
					if (len != 0)
					{
						for (int f = 0; f < len; f++)
						{

							JSONObject jsonobj = array.getJSONObject(f);
							PendingRequestModel pendingModel = gsonpending
									.fromJson(jsonobj.toString(),
											PendingRequestModel.class);

							PendingItemsArray.add(pendingModel);
							PendingItemsAdapter = new PendingRequestCustomAdapter(
									getApplicationContext(),
									R.layout.pending_request_row_item,
									PendingItemsArray, PendingRequest.this);
							PendingItemsList = (ListView) findViewById(R.id.listView);
							PendingItemsList.setItemsCanFocus(false);
							PendingItemsList.setAdapter(PendingItemsAdapter);
						}

					}
//					else
//					{
//						Emptyview.setVisibility(View.VISIBLE);
//					}
				}
				else
				{
					Function.showToast(PendingRequest.this, obj.getString("Message"));
					Emptyview.setVisibility(View.VISIBLE);
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

		};
	};

	private void manageView(boolean Netstatus)
	{
		if (Netstatus)
		{
			Pendingmain_layout.setVisibility(View.VISIBLE);
			Netstate_layout.setVisibility(View.GONE);
		}
		else
		{
			Pendingmain_layout.setVisibility(View.GONE);
			Netstate_layout.setVisibility(View.VISIBLE);
		}
	}

	protected void parseSelectedStatusItem(JSONObject obj)
	{
		try
		{
			if (obj.getString("Result").equalsIgnoreCase("Pass"))
			{	
					PendingItemsAdapter
							.remove(PendingItemsArray.get(selected_statuspos));
					PendingItemsAdapter.notifyDataSetChanged();
					Function.showToast(PendingRequest.this, obj.getString("Message"));
				if (PendingItemsArray.isEmpty())
				{
					Emptyview.setVisibility(View.VISIBLE);
				}
			}
			else
			{
				Function.showToast(PendingRequest.this, obj.getString("Message"));
			}
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
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
		title.setText("Pending Requests");

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

	private void makeUserPendingStatus(Map<String, String> params2)
	{
		showLoadingDilog();
		NetworkHandler.UserStatusPendingRequest(TAG, handler, params2);
	}

	private void showLoadingDilog()
	{
		// Showing loading dialog

		pdHandler = new Handler();
		pd = new TransparentProgressDialog(PendingRequest.this,
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
	public void onBackPressed()
	{
		startActivity(new Intent(PendingRequest.this, LandingPage.class));
		PendingRequest.this.finish();
	}

	@Override
	public void statusAcceptClick(int position)
	{
		userGroupStatusAccepted(position);
		selected_statuspos = position;
	}

	@Override
	public void statusRejectClick(int position)
	{
		userGroupStatusRejected(position);
		selected_statuspos = position;
	}

	private void userGroupStatusRejected(int position)
	{
		String rejectedrid;
		PendingRequestModel reqposition = PendingItemsArray.get(position);
		rejectedrid = reqposition.getRID();
		params.put("PRID", rejectedrid);
		params.put("status", "0");
		Log.d(TAG, "userGroupStatusRejected params=" + params);
		makeUserPendingStatus(params);
	}

	private void userGroupStatusAccepted(int position)
	{
		String acceptedrid;
		PendingRequestModel reqposition = PendingItemsArray.get(position);
		acceptedrid = reqposition.getRID();
		params.put("PRID", acceptedrid);
		params.put("status", "1");
		Log.d(TAG, "userGroupStatusAccepted params=" + params);
		makeUserPendingStatus(params);
	}

}
