package com.hirecraft.jugunoo.passenger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hirecraft.jugunoo.passenger.adapter.CustomCompanyListAdapter;
import com.hirecraft.jugunoo.passenger.common.Constant;
import com.hirecraft.jugunoo.passenger.networkhandler.NetworkHandler;

public class CompanyListActivity extends Activity
{

	private ActionBar actionBar;
	private RelativeLayout titleL;
	static ImageView changePwds, back;
	private static final String TAG = CompanyListActivity.class.getSimpleName();
	// private EditText txt_item;

	private String arrayCompany[];
	private String arrayGroup[];

	private ProgressBar progressBar;
	ListView listview;
	private EditText etSeacrch;
	TextView emptyview;
	private Button retrynames;
	CustomCompanyListAdapter arrayAdapter;

	private List<String> listSortedCompany;
	private List<String> listSortedGroups;

	int textlength = 0;
	private LinearLayout companylist_netstate;

	private static int REQUEST_CODE_COMPANY = 1;
	private static int REQUEST_CODE_GROUP = 2;

	private String searchHint, searchURL;
	private int successMsg;
	private HashMap<String, String> searchParams;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		SetActionBar();

		setContentView(R.layout.dialog);

		listSortedCompany = new ArrayList<String>();
		listSortedGroups = new ArrayList<String>();

		etSeacrch = (EditText) findViewById(R.id.search_edittext);

		Bundle bundle = getIntent().getExtras();
		if (bundle != null)
		{
			searchHint = getIntent().getStringExtra("searchHint");
			searchURL = getIntent().getStringExtra("URL");
			successMsg = getIntent().getIntExtra("successMsg", 0);

			etSeacrch.setHint(searchHint);
		}

		retrynames = (Button) findViewById(R.id.retry_companynames);
		listview = (ListView) findViewById(R.id.companylist);
		emptyview = (TextView) findViewById(R.id.empty);
		progressBar = (ProgressBar) findViewById(R.id.progressbar_loading);
		progressBar.getIndeterminateDrawable().setColorFilter(
				Color.rgb(155, 219, 70),
				android.graphics.PorterDuff.Mode.MULTIPLY);

		companylist_netstate = (LinearLayout) findViewById(R.id.companylist_netstate);

		makeRequestToGetList();
		retrynames.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				companylist_netstate.setVisibility(View.GONE);
				progressBar.setVisibility(View.VISIBLE);
				listview.setEmptyView(emptyview);
				makeRequestToGetList();
			}
		});

		etSeacrch.setOnKeyListener(new OnKeyListener()
		{
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event)
			{
				if (event.getAction() == KeyEvent.ACTION_DOWN)
				{
					Intent intent = new Intent();
					intent.putExtra("message", "");
					setResult(REQUEST_CODE_COMPANY, intent);
					CompanyListActivity.this.finish();
					return true;
				}
				return false;
			}
		});
	}

	@Override
	public void onBackPressed()
	{
		super.onBackPressed();
	}

	private void SetActionBar()
	{
		actionBar = getActionBar();
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

	}

	private void makeRequestToGetList()
	{
		// TAG, handler
		// String URL = Global.JUGUNOO_WS + "Passenger/GetCompanyName";

		NetworkHandler.CompanyNamesRequest(TAG, handlerToGetList, searchURL,
				successMsg);
	}

	Handler handlerToGetList = new Handler()
	{
		public void handleMessage(android.os.Message msg)
		{
			switch (msg.arg1)
			{
				case Constant.MessageState.COMPANY_NAMES_SUCCESS:
					try
					{
						Log.d("COMPANY_NAMES", "COMPANY_NAMES pass");
						parsecompanynames((JSONObject) msg.obj);
						progressBar.setVisibility(View.GONE);
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
					break;

				case Constant.MessageState.GROUP_NAMES_SUCCESS:
					try
					{
						Log.d("GROUP_NAMES", "GROUP_NAMES pass");
						parseGroupnames((JSONObject) msg.obj);
						progressBar.setVisibility(View.GONE);
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
					break;

				case Constant.MessageState.FAIL:
					try
					{
						Log.d("FAIL", "FAIL fail");
						progressBar.setVisibility(View.GONE);
						enableRetryButton();
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
					break;

			}
		}

		private void enableRetryButton()
		{

			companylist_netstate.setVisibility(View.VISIBLE);

			// listview.setVisibility(View.VISIBLE);
			// editText.setVisibility(View.VISIBLE);
			// retrynames.setVisibility(View.VISIBLE);
			// retrynames.setOnClickListener(new OnClickListener()
			// {
			//
			// @Override
			// public void onClick(View v)
			// {
			// GetCompanyList();
			// }
			// });

		}

		private void parsecompanynames(JSONObject obj)
		{
			try
			{
				if (obj.getString("Result").equalsIgnoreCase("Pass"))
				{
					JSONArray array = obj.getJSONArray("CompanyName");
					for (int i = 0; i < array.length(); i++)
					{
						listSortedCompany.add(array.get(i).toString());

					}

					arrayCompany = new String[listSortedCompany.size()];
					for (int i = 0; i < listSortedCompany.size(); i++)
					{
						arrayCompany[i] = listSortedCompany.get(i);
					}
					arrayAdapter = new CustomCompanyListAdapter(
							CompanyListActivity.this, listSortedCompany);
					listview.setAdapter(arrayAdapter);
					listview.setOnItemClickListener(new OnItemClickListener()
					{

						@Override
						public void onItemClick(AdapterView<?> arg0, View arg1,
								int position, long arg3)
						{
							String strName = listSortedCompany.get(position);

							Intent intentCompanyName = new Intent();
							intentCompanyName.putExtra("message", strName);
							setResult(REQUEST_CODE_COMPANY, intentCompanyName);
							CompanyListActivity.this.finish();
						}
					});
					try
					{
						etSeacrch.addTextChangedListener(new TextWatcher()
						{
							public void afterTextChanged(Editable s)
							{
								etSeacrch
										.setCompoundDrawablesWithIntrinsicBounds(
												0, 0, 0, 0);
								textlength = etSeacrch.getText().length();
								listSortedCompany.clear();
								for (int i = 0; i < arrayCompany.length; i++)
								{
									// if (textlength <= TitleName[i].length())
									// {

									if (arrayCompany[i].toLowerCase().contains(
											etSeacrch.getText().toString()
													.toLowerCase().trim()))
									{
										listSortedCompany.add(arrayCompany[i]);
									}

									// }
									else
									{
										// array_sort.add("No Result Found");
										emptyview.setVisibility(View.VISIBLE);
										listview.setEmptyView(emptyview);
									}
								}
								listview.setAdapter(new CustomCompanyListAdapter(
										CompanyListActivity.this,
										listSortedCompany));
							}

							public void beforeTextChanged(CharSequence s,
									int start, int count, int after)
							{

							}

							public void onTextChanged(CharSequence s,
									int start, int before, int count)
							{

							}
						});

					}
					catch (Exception e)
					{
						e.printStackTrace();
					}

				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		private void parseGroupnames(JSONObject obj)
		{

			try
			{
				if (obj.getString("Result").equalsIgnoreCase("Pass"))
				{
					JSONArray array = obj.getJSONArray("GroupName");
					for (int i = 0; i < array.length(); i++)
					{
						listSortedGroups.add(array.get(i).toString());

					}

					// array_sort = new
					// ArrayList<String>(Arrays.asList(TitleName));
					arrayGroup = new String[listSortedGroups.size()];
					for (int i = 0; i < listSortedGroups.size(); i++)
					{
						arrayGroup[i] = listSortedGroups.get(i);
					}
					arrayAdapter = new CustomCompanyListAdapter(
							CompanyListActivity.this, listSortedGroups);
					listview.setAdapter(arrayAdapter);
					listview.setOnItemClickListener(new OnItemClickListener()
					{

						@Override
						public void onItemClick(AdapterView<?> arg0, View arg1,
								int position, long arg3)
						{
							String strName = listSortedGroups.get(position);

							Intent intent = new Intent();
							intent.putExtra("message", strName);
							setResult(REQUEST_CODE_GROUP, intent);
							CompanyListActivity.this.finish();
						}
					});
					try
					{
						etSeacrch.addTextChangedListener(new TextWatcher()
						{
							public void afterTextChanged(Editable s)
							{
								etSeacrch
										.setCompoundDrawablesWithIntrinsicBounds(
												0, 0, 0, 0);
								textlength = etSeacrch.getText().length();
								listSortedGroups.clear();
								for (int i = 0; i < arrayGroup.length; i++)
								{
									// if (textlength <= TitleName[i].length())
									// {

									if (arrayGroup[i].toLowerCase().contains(
											etSeacrch.getText().toString()
													.toLowerCase().trim()))
									{
										listSortedGroups.add(arrayGroup[i]);
									}

									// }
									else
									{
										// array_sort.add("No Result Found");
										emptyview.setVisibility(View.VISIBLE);
										listview.setEmptyView(emptyview);
									}
								}
								listview.setAdapter(new CustomCompanyListAdapter(
										CompanyListActivity.this,
										listSortedGroups));
							}

							public void beforeTextChanged(CharSequence s,
									int start, int count, int after)
							{

							}

							public void onTextChanged(CharSequence s,
									int start, int before, int count)
							{

							}
						});

					}
					catch (Exception e)
					{
						e.printStackTrace();
					}

				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

		}

	};

}
