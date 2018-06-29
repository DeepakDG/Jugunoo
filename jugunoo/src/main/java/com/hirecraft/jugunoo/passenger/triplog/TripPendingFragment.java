package com.hirecraft.jugunoo.passenger.triplog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
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
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.hirecraft.jugunoo.passenger.R;
import com.hirecraft.jugunoo.passenger.UpdateBookingNewActivity;
import com.hirecraft.jugunoo.passenger.adapter.CustomTripLogAdapter;
import com.hirecraft.jugunoo.passenger.adapter.TripLogModel;
import com.hirecraft.jugunoo.passenger.common.Constant;
import com.hirecraft.jugunoo.passenger.common.ConstantMessages;
import com.hirecraft.jugunoo.passenger.common.Function;
import com.hirecraft.jugunoo.passenger.common.Global;
import com.hirecraft.jugunoo.passenger.common.TransparentProgressDialog;
import com.hirecraft.jugunoo.passenger.interfaces.TripLogOnClickInterface;
import com.hirecraft.jugunoo.passenger.networkhandler.NetworkHandler;
import com.hirecraft.jugunoo.passenger.utility.SharedPreferencesManager;

@SuppressLint("SimpleDateFormat")
public class TripPendingFragment extends Fragment implements TripLogOnClickInterface
{
	private String TAG = TripPendingFragment.class.getSimpleName();

	private int pageNumber = 0;

	private ListView listView;

	private boolean isgetTlStatus = true;

	private CustomTripLogAdapter adapter;

	private ArrayList<TripLogModel> tripLogsArray;

	private HashMap<String, String> listSelectItem;

	private View view;

	private RelativeLayout loading;

	private ProgressBar progressBar;

	private boolean isServiceCallFinished = true;

	private Handler pdHandler;

	private TransparentProgressDialog pDialog;

	private Runnable pdRunnable;

	private int tripLogsArrayIndex;

	private SharedPreferencesManager mgr;

	private boolean popUpStatus = false;

	private LinearLayout llDialogPopUp;
	private Gson gson = new Gson();

	private Dialog dialog = null;

	private int listIndexPosition = 0;;

	private Dialog overlayInfo = null;

	private TextView textView;

	// private EditText etEndDate;
	// private EditText etStartDate;
	// private EditText etTime;
	// private EditText etDaysNo;
	private LinearLayout Netstate_layout;
	private Button retryBtn;
	int hourOfDay;

	private static boolean trippend_visible;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState)
	{
		view = inflater.inflate(R.layout.fragment_trip_pending, container,
				false);
		return view;
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		Init();

		if (!Global.isTripLogUpdated)
		{
			clearListView();
		}
	}

	@Override
	public void onResume()
	{
		super.onResume();

		Log.d("Pending", "onResume=" + Global.isTripLogUpdated);
		if (Global.isTripLogUpdated)
		{
			clearListView();
			Global.isTripLogUpdated = false;
		}
	}

	private void Init()
	{
		Netstate_layout = (LinearLayout) getActivity().findViewById(
				R.id.pending_netstate);
		retryBtn = (Button) getActivity().findViewById(R.id.pending_retrybtn);
		listView = (ListView) view.findViewById(R.id.tripList);
		progressBar = (ProgressBar) view.findViewById(R.id.pbTProgressBar);
		progressBar.getIndeterminateDrawable().setColorFilter(
				Color.rgb(155, 219, 70),
				android.graphics.PorterDuff.Mode.MULTIPLY);
		loading = (RelativeLayout) view.findViewById(R.id.rlLoading);
		llDialogPopUp = (LinearLayout) view.findViewById(R.id.llDialogPopUp);
		textView = (TextView) view.findViewById(R.id.tripemptymsg);

		tripLogsArray = new ArrayList<TripLogModel>();
		listSelectItem = new HashMap<String, String>();
		mgr = new SharedPreferencesManager(getActivity());

		listView.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id)
			{
				listItemOnClicked(position);
			}
		});

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
				hideBottomDialog();

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

								getTripLogServiceCall(++pageNumber, "P");
							}
						}
					}
				}
			}
		});

	}

	// private void alertDialog()
	// {
	// /* Confirmation dialog */
	//
	// final Dialog dialog = new Dialog(getActivity());
	// dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
	// dialog.setContentView(R.layout.custom_alert_dialog);
	//
	// TextView popupMsg = (TextView) dialog
	// .findViewById(R.id.tvAlertTitleDesc);
	// Button btNegative = (Button) dialog.findViewById(R.id.btNegative);
	// Button btPositive = (Button) dialog.findViewById(R.id.btPositive);
	//
	// popupMsg.setText(R.string.alert_desc);
	// btNegative.setText(R.string.alert_cancel);
	// btPositive.setText(R.string.alert_proceed);
	//
	// btNegative.setOnClickListener(new OnClickListener()
	// {
	// @Override
	// public void onClick(View v)
	// {
	// dialog.cancel();
	// }
	// });
	//
	// btPositive.setOnClickListener(new OnClickListener()
	// {
	// @Override
	// public void onClick(View v)
	// {
	// Intent intent = new Intent(getActivity(),
	// TransferBookingActivity.class);
	// intent.putExtra("bookingId", listSelectItem.get("BookingId"));
	// startActivity(intent);
	//
	// dialog.cancel();
	// }
	// });
	//
	// dialog.show();
	// }

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser)
	{
		super.setUserVisibleHint(isVisibleToUser);
		trippend_visible = isVisibleToUser;
		if (trippend_visible)
		{
			try
			{
				listView.setSelection(0);
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

	private void showBottomDialog()
	{
		if (!popUpStatus)
		{
			popUpStatus = true;

			Animation slideDown = AnimationUtils.loadAnimation(getActivity(),
					R.anim.slide_up_dialog);
			llDialogPopUp.setVisibility(View.VISIBLE);
			llDialogPopUp.setAnimation(slideDown);

			LinearLayout btUpdate = (LinearLayout) view
					.findViewById(R.id.llTChange);
			LinearLayout btCancel = (LinearLayout) view
					.findViewById(R.id.llTCancel);

			btUpdate.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					hideBottomDialog();
					updateDialog();
				}
			});

			btCancel.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					hideBottomDialog();
					confirmationAlert();
				}
			});
		}
	}

	private void hideBottomDialog()
	{
		if (popUpStatus)
		{
			popUpStatus = false;

			Animation slideDown = AnimationUtils.loadAnimation(getActivity(),
					R.anim.slide_down_dialog);
			llDialogPopUp.setAnimation(slideDown);
			llDialogPopUp.setVisibility(View.GONE);
		}
	}

	private void confirmationAlert()
	{

		final Dialog dialog = new Dialog(getActivity());
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.dialog_common_alert);
		TextView popupHeader = (TextView) dialog
				.findViewById(R.id.tvAlertHeader);
		TextView popupMsg = (TextView) dialog.findViewById(R.id.tvAlertMsg);
		Button btOk = (Button) dialog.findViewById(R.id.btAlertOk);
		Button btCancel = (Button) dialog.findViewById(R.id.btAlertCancel);
		popupMsg.setText(ConstantMessages.MSG89);
		popupHeader.setText(ConstantMessages.MSG91);
		btOk.setText(ConstantMessages.MSG93);
		btCancel.setText(ConstantMessages.MSG96);
		btCancel.setVisibility(View.VISIBLE);

		btOk.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				dialog.cancel();
				updateCancelTripLogServiceCall();
			}
		});

		btCancel.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				dialog.cancel();
			}
		});

		dialog.show();
	}

	private void updateCancelTripLogServiceCall()
	{
		String tag = "tripCancel";

		Map<String, String> params = new HashMap<String, String>();
		params.put("EngId", listSelectItem.get("BookingId"));
		params.put("Operation", "C");
		params.put("UserId", mgr.GetValueFromSharedPrefs("UserID"));
		params.put("Status", "PC");

		Log.i("Cancel info -->", "Cancel info -->" + params);

		NetworkHandler.cancelTripLog(TAG, handler, params);
		showLoadingDilogTr();
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
		pageNumber = 0; // Clearing page no
		isgetTlStatus = true; // Clearing getTripLog Status
		tripLogsArray.clear(); // Clearing listView
		listAdapter(tripLogsArray);

		getTripLogServiceCall(++pageNumber, "P");
	}

	private void updateDialog()
	{
		Intent updateBookingIntent = new Intent(getActivity(),
				UpdateBookingNewActivity.class);

		updateBookingIntent.putExtra("bookingId",
				listSelectItem.get("BookingId"));
		startActivity(updateBookingIntent);
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

	private void showLoadingDilogTr()
	{
		// Showing loading dialog

		pdHandler = new Handler();
		pDialog = new TransparentProgressDialog(getActivity(),
				R.drawable.loading_image);

		pdRunnable = new Runnable()
		{
			@Override
			public void run()
			{
				if (pDialog != null)
				{
					if (pDialog.isShowing())
					{
						pDialog.dismiss();
					}
				}
			}
		};
		pDialog.show();
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

	private void cancelLoadingDialogTr()
	{
		// Cancel loading dialog

		pdHandler.removeCallbacks(pdRunnable);

		if (pDialog.isShowing())
		{
			pDialog.dismiss();
		}
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

					--pageNumber;

					isServiceCallFinished = true;

					// Function.showToast(TripLogActivity.this,
					// Global.networkErrorMsg);
					Netstate_layout.setVisibility(View.VISIBLE);
					retryBtn.setOnClickListener(new OnClickListener()
					{
						@Override
						public void onClick(View v)
						{
							Netstate_layout.setVisibility(View.GONE);
							progressBar.setVisibility(View.VISIBLE);
							getTripLogServiceCall(++pageNumber, "P");
						}
					});
					break;

				case Constant.MessageState.TRIPLOG_CANCEL_SUCCESS:
					cancelLoadingDialogTr();
					parseCancelTripResponse((JSONObject) msg.obj);
					break;

				case Constant.MessageState.TRIPLOG_CANCEL_FAILED:
					cancelLoadingDialogTr();
					parseCancelTripResponse((JSONObject) msg.obj);
					break;
			}
		}
	};

	private void parseCancelTripResponse(JSONObject resObject)
	{
		Log.i(TAG, "Cancel trip response -->" + resObject);

		try
		{
			if (resObject != null)
			{
				if (resObject.has("Message"))
				{
					try
					{
						Function.showToast(getActivity(),
								resObject.getString("Message"));

						if (resObject.getString("Result").equalsIgnoreCase(
								"Pass"))
						{
							tripLogsArray.remove(tripLogsArrayIndex);
							listAdapter(tripLogsArray);
						}
					}
					catch (JSONException e)
					{
						e.printStackTrace();
					}
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void getTripLogServiceCall(int currentPage, String status)
	{
		// Tag used to cancel the reque

		if (isgetTlStatus)
		{
			String tag_json_obj = "get_trip_logs";

			Log.i(TAG, "CURRENT PAGE -->" + currentPage);

			if (tripLogsArray.isEmpty())
			{
				showLoadingDilog();
			}

			mgr = new SharedPreferencesManager(getActivity());
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
					}

					listAdapter(tripLogsArray);
					listView.setSelection(listIndexPosition);
				}
				else
				{
					Function.showToast(getActivity(),
							"You are yet to Ride on JUGUNOO");
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

	public void listItemOnClicked(int position)
	{
		TripLogModel model = tripLogsArray.get(position);

		if (model.getEditable().equalsIgnoreCase("Y"))
		{
			showBottomDialog();
		}

		for (int i = 0; i < tripLogsArray.size(); i++)
		{
			if (i == position)
			{
				listSelectItem = new HashMap<String, String>();

				TripLogModel modell = tripLogsArray.get(i);
				listSelectItem.put("BookingId", modell.getBookingId());
				// listSelectItem.put("PrefType", modell.getPref());

				tripLogsArrayIndex = i;

				Log.i("List selected item", "Selected item -->"
						+ listSelectItem);
				break;
			}
		}
	}

	@Override
	public void editTextClicked(int position)
	{
		listItemOnClicked(position);
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
