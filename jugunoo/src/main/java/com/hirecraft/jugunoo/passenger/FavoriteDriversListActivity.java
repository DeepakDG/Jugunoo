package com.hirecraft.jugunoo.passenger;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import android.R.color;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.hirecraft.jugunoo.passenger.adapter.CustomFavoriteDriversAdapter;
import com.hirecraft.jugunoo.passenger.adapter.FavoriteDriverModel;
import com.hirecraft.jugunoo.passenger.common.Constant;
import com.hirecraft.jugunoo.passenger.common.ConstantMessages;
import com.hirecraft.jugunoo.passenger.common.Function;
import com.hirecraft.jugunoo.passenger.common.TransparentProgressDialog;
import com.hirecraft.jugunoo.passenger.interfaces.FavoriteDriverOnClickInterface;
import com.hirecraft.jugunoo.passenger.networkhandler.NetworkHandler;
import com.hirecraft.jugunoo.passenger.utility.SharedPreferencesManager;

public class FavoriteDriversListActivity extends Activity implements FavoriteDriverOnClickInterface
{

	static ImageView changePwds, back;
	private static final String TAG = FavoriteDriversListActivity.class
			.getSimpleName();
	private SharedPreferencesManager mgr;
	private Handler pdHandler;
	private Runnable pdRunnable;
	private ProgressBar progressBar;
	private ListView fav_listview;
	private Button retrynames;
	CustomFavoriteDriversAdapter favdriver_Adapter;
	private ArrayList<FavoriteDriverModel> driver_array;
	int textlength = 0;
	private LinearLayout favDrivers_netstate;
	private String drivername;
	private static int REQUEST_CODE = 1;
	Gson gsondrivers = new Gson();
	private static String selected_pos;
	private TransparentProgressDialog pd;
	private TextView Emptyview;
	private static int favdriver_selectedpos;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		SetActionBar();
		setContentView(R.layout.favoritedrivers);
		pd = new TransparentProgressDialog(getApplicationContext(),
				R.drawable.loading_image);
		retrynames = (Button) findViewById(R.id.retry_favdrivers);
		fav_listview = (ListView) findViewById(R.id.favdrivers_list);
		fav_listview.setSelector(new ColorDrawable(color.transparent));
		progressBar = (ProgressBar) findViewById(R.id.progressbar_loading);
		progressBar.getIndeterminateDrawable().setColorFilter(
				Color.rgb(155, 219, 70),
				android.graphics.PorterDuff.Mode.MULTIPLY);
		driver_array = new ArrayList<FavoriteDriverModel>();
		mgr = new SharedPreferencesManager(getApplicationContext());
		Emptyview = (TextView) findViewById(R.id.fav_emptyview);
		favDrivers_netstate = (LinearLayout) findViewById(R.id.favdrivers_netstate);

		getFavoriteDriversList();
		retrynames.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				favDrivers_netstate.setVisibility(View.GONE);
				progressBar.setVisibility(View.VISIBLE);
				getFavoriteDriversList();
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
		title.setText("Favorite Drivers");

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

	private void getFavoriteDriversList()
	{
		String userID = mgr.GetValueFromSharedPrefs("UserID");
		NetworkHandler.GetFavDriverlist(TAG, handler, userID);
	}

	Handler handler = new Handler()
	{
		public void handleMessage(android.os.Message msg)
		{
			switch (msg.arg1)
			{
				case Constant.MessageState.FAVORITEDRIVERS_STATUS_SUCCESS:
					try
					{
						progressBar.setVisibility(View.GONE);
						parseFavoriteDrivers((JSONObject) msg.obj);
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
					break;

				case Constant.MessageState.FAIL:
					try
					{
						progressBar.setVisibility(View.GONE);
						cancelLoadingDialog();
						retryFavoriteDrivers();
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
					break;

				case Constant.MessageState.FEEDBACK_SUCCESS:
					try
					{
						cancelLoadingDialog();
						parseDeleteFavDrivers((JSONObject) msg.obj);

					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
			}
		}
	};

	private void retryFavoriteDrivers()
	{
		favDrivers_netstate.setVisibility(View.VISIBLE);
	}

	private void parseDeleteFavDrivers(JSONObject obj)
	{
		try
		{
			if (obj.getString("Result").equalsIgnoreCase("Pass"))
			{
				driver_array.remove(favdriver_selectedpos);
				favdriver_Adapter.notifyDataSetChanged();
				favdriver_Adapter.notifyDataSetInvalidated();
				Function.showToast(FavoriteDriversListActivity.this,
						obj.getString("Message"));
				if (driver_array.isEmpty())
				{
					Emptyview.setVisibility(View.VISIBLE);
				}
			}
			else
			{
				Function.showToast(FavoriteDriversListActivity.this,
						obj.getString("Message"));
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}
	private void alertDeleteOperator(String msg, int position)
	{
		/* Show Alert dialog when back key pressed */

//		HashMap<String, String> hashMap = fleets.get(position);
		final Dialog alertBack = new Dialog(FavoriteDriversListActivity.this);
		alertBack.requestWindowFeature(Window.FEATURE_NO_TITLE);

		LayoutInflater inflater = (LayoutInflater) getApplicationContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View dislogView = inflater.inflate(R.layout.dialog_common_alert, null);
		alertBack.setContentView(dislogView);

		TextView popupText = (TextView) alertBack
				.findViewById(R.id.tvAlertHeader);
		popupText.setText(ConstantMessages.MSG91);
		TextView popupMsg = (TextView) alertBack.findViewById(R.id.tvAlertMsg);
		Button popupOk = (Button) alertBack.findViewById(R.id.btAlertOk);
		Button cancel = (Button) alertBack.findViewById(R.id.btAlertCancel);
		cancel.setVisibility(View.VISIBLE);
		popupMsg.setText(msg);
		popupOk.setText(ConstantMessages.MSG93);
		cancel.setText(ConstantMessages.MSG96);
		alertBack.show();

		popupOk.setOnClickListener(new View.OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				FavoriteDriverModel selected_driverpos = driver_array
						.get(favdriver_selectedpos);
				selected_pos = selected_driverpos
						.getRID();
				HashMap<String, String> params = new HashMap<String, String>();
				params.put("FavouriteStatus", "0");
				params.put("RID", selected_pos);
				makeDeleteFavoriteDriver(params);
				alertBack.cancel();
			}
		});

		cancel.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				alertBack.cancel();
			}
		});
	}

	private void showLoadingDilog()
	{
		// Showing loading dialog
		pdHandler = new Handler();
		pd = new TransparentProgressDialog(FavoriteDriversListActivity.this,
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

	private void parseFavoriteDrivers(JSONObject obj)
	{
		try
		{
			if (obj.getString("Result").equalsIgnoreCase("Pass"))
			{
				JSONArray array = obj.getJSONArray("Details");
				int len = array.length();
				if (len != 0)
				{
					for (int f = 0; f < len; f++)
					{
						Emptyview.setVisibility(View.GONE);
						JSONObject jsonobj = array.getJSONObject(f);
						FavoriteDriverModel FavoriteModel = gsondrivers
								.fromJson(jsonobj.toString(),
										FavoriteDriverModel.class);
						driver_array.add(FavoriteModel);
					}
				}
				favdriver_Adapter = new CustomFavoriteDriversAdapter(
						getApplicationContext(), R.layout.favorite_row_items,
						driver_array,FavoriteDriversListActivity.this);
				fav_listview.setAdapter(favdriver_Adapter);
//				fav_listview
//						.setOnItemLongClickListener(new OnItemLongClickListener()
//						{
//
//							@Override
//							public boolean onItemLongClick(
//									AdapterView<?> parent, View view,
//									final int position, long id)
//							{
//								AlertDialog.Builder builder = new AlertDialog.Builder(
//										FavoriteDriversListActivity.this);
//								builder.setItems(R.array.delete_driver_item,
//
//								new DialogInterface.OnClickListener()
//								{
//
//									@Override
//									public void onClick(DialogInterface dialog,
//											int which)
//									{
//										FavoriteDriverModel selected_driverpos = driver_array
//												.get(position);
//										finalposfav=position;
//										selected_pos = selected_driverpos
//												.getRID();
//										HashMap<String, String> params = new HashMap<String, String>();
//										params.put("FavouriteStatus", "0");
//										params.put("RID", selected_pos);
//										makeDeleteFavoriteDriver(params);
//									}
//
//								});
//								AlertDialog alert = builder.create();
//								alert.show();
//								return false;
//							}
//
//						});

			}
			else
			{
				Emptyview.setVisibility(View.VISIBLE);
				Function.showToast(FavoriteDriversListActivity.this,
						obj.getString("Message"));
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	};

	private void makeDeleteFavoriteDriver(HashMap<String, String> params)
	{
		showLoadingDilog();
		NetworkHandler.feedBackRequest(TAG, handler, params);

	}

	@Override
	public void favoriteDriverDeleteClicked(int position)
	{
		
		favdriver_selectedpos=position;
		alertDeleteOperator(
				"Are you sure want to Delete Your Favorite Driver?",
				favdriver_selectedpos);
	}
}
