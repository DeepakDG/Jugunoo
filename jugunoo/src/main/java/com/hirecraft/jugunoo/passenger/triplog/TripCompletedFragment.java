package com.hirecraft.jugunoo.passenger.triplog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.hirecraft.jugunoo.passenger.R;
import com.hirecraft.jugunoo.passenger.adapter.CustomTripLogAdapter;
import com.hirecraft.jugunoo.passenger.adapter.TripLogModel;
import com.hirecraft.jugunoo.passenger.common.Constant;
import com.hirecraft.jugunoo.passenger.common.ConstantMessages;
import com.hirecraft.jugunoo.passenger.common.Function;
import com.hirecraft.jugunoo.passenger.interfaces.TripLogOnClickInterface;
import com.hirecraft.jugunoo.passenger.networkhandler.NetworkHandler;
import com.hirecraft.jugunoo.passenger.utility.SharedPreferencesManager;

public class TripCompletedFragment extends Fragment implements TripLogOnClickInterface
{
	private String TAG = TripCompletedFragment.class.getSimpleName();

	private int currentPage = 0;

	ListView listView;

	private boolean isgetTlStatus = true;

	private CustomTripLogAdapter adapter;

	private ArrayList<TripLogModel> tripLogsArray = new ArrayList<TripLogModel>();

	Gson gson = new Gson();
	View view;

	private RelativeLayout loading;

	private ProgressBar progressBar;

	private boolean isServiceCallFinished = true;

	private SharedPreferencesManager mgr;

	// private LinearLayout llDialogPopUp;

	private Dialog dialog = null;

	private Dialog overlayInfo = null;

	int hourOfDay;
	private TextView textView;
	private LinearLayout Netstate_layout;
	private Button retryBtn;
	private int listIndexPosition = 0;

	private boolean isVisibleToUser;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState)
	{

		view = inflater.inflate(R.layout.fragment_trip_completed, container,
				false);
		return view;
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);

		Init();
		// clearListView();
	}

	@Override
	public void onResume()
	{
		super.onResume();

	}

	private void Init()
	{
		Netstate_layout = (LinearLayout) getActivity().findViewById(
				R.id.comp_netstate);
		retryBtn = (Button) getActivity().findViewById(R.id.comp_retrybtn);
		listView = (ListView) view.findViewById(R.id.tripList);
		progressBar = (ProgressBar) view.findViewById(R.id.pbTProgressBar);
		progressBar.getIndeterminateDrawable().setColorFilter(
				Color.rgb(155, 219, 70),
				android.graphics.PorterDuff.Mode.MULTIPLY);
		loading = (RelativeLayout) view.findViewById(R.id.rlLoading);
		textView = (TextView) view.findViewById(R.id.tripemptymsg);
		// llDialogPopUp = (LinearLayout) view.findViewById(R.id.llDialogPopUp);

		mgr = new SharedPreferencesManager(getActivity()
				.getApplicationContext());

		listView.setOnScrollListener(new OnScrollListener()
		{
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState)
			{
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount)
			{
				int position = firstVisibleItem + visibleItemCount;
				int limit = totalItemCount;

				Log.i(TAG, "TAG-- firstVisibleItem -->" + firstVisibleItem);
				Log.i(TAG, "TAG-- visibleItemCount -->" + visibleItemCount);
				Log.i(TAG, "TAG-- add both -->" + position);
				Log.i(TAG, "TAG-- totalItemCount -->" + totalItemCount);

				// Check if bottom has been reached
				if (limit != 0)
				{
					if (position == limit)
					{
						if (isServiceCallFinished)
						{
							if (isgetTlStatus)
							{
								loading.setVisibility(View.VISIBLE);
								isServiceCallFinished = false;
								listIndexPosition = totalItemCount - 1;
								getTripLogServiceCall(++currentPage, "PF");
							}
						}
					}
				}
			}
		});

	}

	private void listAdapter(ArrayList<TripLogModel> tripLogsArray)
	{
		// ListView adapter to populate data to listView

		adapter = new CustomTripLogAdapter(getActivity(),
				R.layout.passenger_trip_log_row_item, tripLogsArray, this);

		listView.setAdapter(adapter);
		if (listView.getAdapter().getCount() > 0)
		{
			textView.setVisibility(View.GONE);
		}
	}

	private void clearListView()
	{
		listIndexPosition = 0;
		currentPage = 0; // Clearing page no
		isgetTlStatus = true; // Clearing getTripLog Status
		tripLogsArray.clear(); // Clearing listView
		listAdapter(tripLogsArray);

		getTripLogServiceCall(++currentPage, "PF");
	}

	public void finish()
	{
		if (dialog != null)
		{
			dialog.dismiss();
		}
		if (overlayInfo != null)
		{
			overlayInfo.dismiss();
		}
		getActivity().finish();
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser)
	{
		super.setUserVisibleHint(isVisibleToUser);
		this.isVisibleToUser = isVisibleToUser;

		if (isVisibleToUser)
		{
			try
			{
				// listView.setSelection(0);

				listIndexPosition = 0;
				currentPage = 0; // Clearing page no
				isgetTlStatus = true; // Clearing getTripLog Status

				showLoadingDilog();
				getTripLogServiceCall(++currentPage, "PF");
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			// fragment is no longer visible
		}
	}

	private void showLoadingDilog()
	{
		// Showing loading dialog
		progressBar.setVisibility(View.VISIBLE);
	}

	private void cancelLoadingDialog()
	{
		// Cancel loading dialog
		progressBar.setVisibility(View.GONE);
	}

	Handler handler = new Handler()
	{

		public void handleMessage(Message msg)
		{

			switch (msg.arg1)
			{

				case Constant.MessageState.TRIP_LOGS_AVAILABLE:
					cancelLoadingDialog();
					loading.setVisibility(View.GONE);

					isServiceCallFinished = true;

					parseTripLogsResponse((JSONObject) msg.obj);
					break;

				case Constant.MessageState.TRIP_LOGS_UNAVAILABLE:
					cancelLoadingDialog();
					loading.setVisibility(View.GONE);
					--currentPage;
					isServiceCallFinished = true;
					
					tripLogsArray.clear(); // clearing the arrayList
					listAdapter(tripLogsArray);
					listView.setSelection(listIndexPosition);

					Netstate_layout.setVisibility(View.VISIBLE);
					retryBtn.setOnClickListener(new OnClickListener()
					{
						@Override
						public void onClick(View v)
						{
							Netstate_layout.setVisibility(View.GONE);
							progressBar.setVisibility(View.VISIBLE);
							getTripLogServiceCall(++currentPage, "PF");
						}
					});
					break;
			}
		}
	};

	private void getTripLogServiceCall(int currentPage, String status)
	{
		// Tag used to cancel the reque

		if (isgetTlStatus)
		{
			Log.i(TAG, "CURRENT PAGE -->" + currentPage);

			if (tripLogsArray.isEmpty())
			{
				showLoadingDilog();
			}

			mgr = new SharedPreferencesManager(getActivity()
					.getApplicationContext());
			String userId = mgr.GetValueFromSharedPrefs("UserID");

			Map<String, String> params = new HashMap<String, String>();
			NetworkHandler.GetTripLog(TAG, handler, params, getActivity(),
					userId, currentPage, status);
		}
	}

	private void parseTripLogsResponse(JSONObject response)
	{
		Log.e(TAG, "TripLog response -->" + response.toString());

		try
		{
			if (response.has("Trip"))
			{
				if (isVisibleToUser)
				{
					tripLogsArray.clear();
					isVisibleToUser = false;
				}

				JSONArray array = response.getJSONArray("Trip");

				if (array.length() != 0)
				{
					for (int i = 0; i < array.length(); i++)
					{
						JSONObject c = array.getJSONObject(i);

						TripLogModel logModel = gson.fromJson(c.toString(),
								TripLogModel.class);

						tripLogsArray.add(logModel);

						Log.i("passenger log",
								"addr size=" + tripLogsArray.size());
						
						listAdapter(tripLogsArray);
						listView.setSelection(listIndexPosition);
					}
				}
				else
				{
					Function.showToast(getActivity(), ConstantMessages.MSG88);
				}
			}
			else
			{
				if (response.getString("Result").equalsIgnoreCase("Fail"))
				{
					isgetTlStatus = false;
					if (tripLogsArray.size() <= 0)
					{
						textView.setVisibility(View.VISIBLE);
						textView.setText(response.getString("Message"));
					}
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void editTextClicked(int position)
	{

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
