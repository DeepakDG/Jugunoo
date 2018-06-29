package com.hirecraft.jugunoo.passenger;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.hirecraft.jugunoo.passenger.adapter.ComapnyNameModel;
import com.hirecraft.jugunoo.passenger.adapter.CustomCompanyNameAdapter;
import com.hirecraft.jugunoo.passenger.adapter.CustomGroupNameAdapter;
import com.hirecraft.jugunoo.passenger.adapter.GroupNameModel;
import com.hirecraft.jugunoo.passenger.common.Constant;
import com.hirecraft.jugunoo.passenger.common.Function;
import com.hirecraft.jugunoo.passenger.networkhandler.NetworkHandler;

public class CompanyListActivity_new extends Activity
{
	private static final String TAG = CompanyListActivity.class.getSimpleName();

	private ProgressBar progressBar;
	ListView listview;
	private EditText etSeacrch;
	TextView emptyview;
	private Button retrynames;
	CustomCompanyNameAdapter companyNameAdapter;
	CustomGroupNameAdapter customGroupNameAdapter;

	private List<ComapnyNameModel> listCompanyObjects;
	private List<GroupNameModel> listGroupObjects;

	private List<ComapnyNameModel> listCompanySearchObjects;
	private List<GroupNameModel> listGroupSearchObjects;
	// private List<String> listSortedGroups;

	int textlength = 0;
	private LinearLayout companylist_netstate;

	private static int REQUEST_CODE_COMPANY = 1;
	private static int REQUEST_CODE_GROUP = 2;

	private String searchHint, searchURL;
	private int successMsg;

	private Gson gson = new Gson();

	private String selectedCompanyId, selectedCompanyName;
	private String selectedGroupId, selectedGroupName;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog);

		listCompanyObjects = new ArrayList<ComapnyNameModel>();
		listGroupObjects = new ArrayList<GroupNameModel>();

		listCompanySearchObjects = new ArrayList<ComapnyNameModel>();
		listGroupSearchObjects = new ArrayList<GroupNameModel>();

		etSeacrch = (EditText) findViewById(R.id.search_edittext);

		Bundle bundle = getIntent().getExtras();
		if (bundle != null)
		{
			searchHint = getIntent().getStringExtra("searchHint");
			searchURL = getIntent().getStringExtra("URL");
			successMsg = getIntent().getIntExtra("successMsg", 0);

			etSeacrch.setHint(searchHint);
		}

		etSeacrch.addTextChangedListener(watcher);
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
					CompanyListActivity_new.this.finish();
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

	private void makeRequestToGetList()
	{
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
						parseCompanyNames((JSONObject) msg.obj);
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
						parseGroupNames((JSONObject) msg.obj);
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
	};

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

	private void parseCompanyNames(JSONObject obj)
	{
		try
		{
			if (obj.getString("Result").equalsIgnoreCase("Pass"))
			{
				JSONArray array = obj.getJSONArray("CompanyName");

				if (array.length() != 0)
				{
					for (int i = 0; i < array.length(); i++)
					{
						JSONObject c = array.getJSONObject(i);
						ComapnyNameModel logModel = gson.fromJson(c.toString(),
								ComapnyNameModel.class);

						listCompanyObjects.add(logModel);

						Log.d("passenger log", "addr size="
								+ listCompanyObjects.size());
					}
					listAdapterCompany(listCompanyObjects);
				}
				else
				{
					Log.d("parseGroupList", "parseGroupList if");
					Function.showToast(CompanyListActivity_new.this,
							"You are yet to Ride on JUGUNOO");
				}

				companyNameAdapter = new CustomCompanyNameAdapter(
						CompanyListActivity_new.this, R.layout.alertlistrow,
						listCompanyObjects);
				listview.setAdapter(companyNameAdapter);

				listview.setOnItemClickListener(new OnItemClickListener()
				{

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int position, long arg3)
					{

						listItemOnClicked(position);

						Intent intent = new Intent();
						intent.putExtra("message", selectedCompanyName);
						setResult(REQUEST_CODE_COMPANY, intent);

						CompanyListActivity_new.this.finish();
					}
				});

			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void parseGroupNames(JSONObject obj)
	{
		try
		{
			if (obj.getString("Result").equalsIgnoreCase("Pass"))
			{
				JSONArray array = obj.getJSONArray("GroupName");

				if (array.length() != 0)
				{
					for (int i = 0; i < array.length(); i++)
					{
						JSONObject c = array.getJSONObject(i);
						GroupNameModel logModel = gson.fromJson(c.toString(),
								GroupNameModel.class);

						listGroupObjects.add(logModel);

						Log.d("passenger log",
								"addr size=" + listGroupObjects.size());
					}
					listAdapterGroup(listGroupObjects);
				}
				else
				{
					Log.d("parseGroupList", "parseGroupList if");
					Function.showToast(CompanyListActivity_new.this,
							"You are yet to Ride on JUGUNOO");
				}

				customGroupNameAdapter = new CustomGroupNameAdapter(
						CompanyListActivity_new.this, R.layout.alertlistrow,
						listGroupObjects);
				listview.setAdapter(customGroupNameAdapter);

				listview.setOnItemClickListener(new OnItemClickListener()
				{

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int position, long arg3)
					{

						listItemOnClicked(position);

						Intent intent = new Intent();
						intent.putExtra("message", selectedGroupName);
						setResult(REQUEST_CODE_GROUP, intent);
						CompanyListActivity_new.this.finish();
					}
				});

			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void listAdapterCompany(List<ComapnyNameModel> companyListArray)
	{
		Log.d("listAdapter", "listAdapter company called");

		companyNameAdapter = new CustomCompanyNameAdapter(
				CompanyListActivity_new.this, R.layout.template_groups,
				companyListArray);

		listview.setAdapter(companyNameAdapter);
	}

	private void listAdapterGroup(List<GroupNameModel> groupListArray)
	{
		Log.d("listAdapter", "listAdapter group called");

		customGroupNameAdapter = new CustomGroupNameAdapter(
				CompanyListActivity_new.this, R.layout.template_groups,
				groupListArray);

		listview.setAdapter(customGroupNameAdapter);
	}

	private TextWatcher watcher = new TextWatcher()
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
			etSeacrch.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
			String searchText = etSeacrch.getText().toString().toLowerCase()
					.trim();

			if (searchHint.equalsIgnoreCase("Search Company"))
			{
				ComapnyNameModel model;
				listCompanySearchObjects.clear();

				for (int i = 0; i < listCompanyObjects.size(); i++)
				{
					model = listCompanyObjects.get(i);
					selectedCompanyId = model.getCompanyId();
					selectedCompanyName = model.getCompanyName();

					if (selectedCompanyName.toLowerCase().contains(searchText))
					{
						listCompanySearchObjects.add(listCompanyObjects.get(i));
					}
					else
					{
						emptyview.setVisibility(View.VISIBLE);
						listview.setEmptyView(emptyview);
					}

				}
				companyNameAdapter = new CustomCompanyNameAdapter(
						CompanyListActivity_new.this, R.layout.alertlistrow,
						listCompanySearchObjects);
				listview.setAdapter(companyNameAdapter);

			}
			else if (searchHint.equalsIgnoreCase("Search Groups"))
			{
				GroupNameModel model;
				listGroupSearchObjects.clear();

				for (int i = 0; i < listGroupObjects.size(); i++)
				{
					model = listGroupObjects.get(i);
					selectedGroupId = model.getGroupId();
					selectedGroupName = model.getGroupName();

					if (selectedGroupName.toLowerCase().contains(searchText))
					{
						listGroupSearchObjects.add(listGroupObjects.get(i));
					}
					else
					{
						emptyview.setVisibility(View.VISIBLE);
						listview.setEmptyView(emptyview);
					}

				}
				customGroupNameAdapter = new CustomGroupNameAdapter(
						CompanyListActivity_new.this, R.layout.alertlistrow,
						listGroupSearchObjects);
				listview.setAdapter(customGroupNameAdapter);
			}
		}
	};

	public void listItemOnClicked(int position)
	{
		if (searchHint.equalsIgnoreCase("Search Company"))
		{
			for (int i = 0; i < listCompanyObjects.size(); i++)
			{
				if (i == position)
				{
					ComapnyNameModel model = listCompanyObjects.get(position);
					selectedCompanyId = model.getCompanyId();
					selectedCompanyName = model.getCompanyName();

					Log.i("List selected item", "Selected item -->"
							+ selectedCompanyId);
					break;
				}
			}
		}
		else if (searchHint.equalsIgnoreCase("Search Groups"))
		{
			for (int i = 0; i < listGroupObjects.size(); i++)
			{
				if (i == position)
				{
					GroupNameModel model = listGroupObjects.get(position);
					selectedGroupId = model.getGroupId();
					selectedGroupName = model.getGroupName();

					Log.i("List selected item", "Selected item -->"
							+ selectedGroupId);
					break;
				}
			}
		}
	}

}
