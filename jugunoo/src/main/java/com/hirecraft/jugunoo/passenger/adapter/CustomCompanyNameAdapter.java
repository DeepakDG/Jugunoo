package com.hirecraft.jugunoo.passenger.adapter;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.hirecraft.jugunoo.passenger.R;
import com.hirecraft.jugunoo.passenger.utility.SharedPreferencesManager;

public class CustomCompanyNameAdapter extends ArrayAdapter<ComapnyNameModel>
{
	private Context context;
	private int layoutResourceId;
	private List<ComapnyNameModel> data;
	private SharedPreferencesManager mgr;
	private ComapnyNameModel model;
	private CompanyName holder = null;

	public CustomCompanyNameAdapter(Context context, int resource,
			List<ComapnyNameModel> data)
	{
		super(context, resource, data);

		Log.d("CustomCompanyNameAdapter", "CustomCompanyNameAdapter called");

		this.context = context;
		this.layoutResourceId = resource;
		this.data = data;

		mgr = new SharedPreferencesManager(getContext());

	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent)
	{
		Log.d("CustomGroupListAdapter getView",
				"CustomGroupListAdapter getView called");

		model = getItem(position);

		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

		if (convertView == null)
		{
			try
			{
				convertView = inflater.inflate(layoutResourceId, null);
				holder = new CompanyName();

				holder.tvName = (TextView) convertView
						.findViewById(R.id.textView_titllename);

				convertView.setTag(holder);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			holder = (CompanyName) convertView.getTag();
		}

		holder.tvName.setText(model.getCompanyName());

		return convertView;
	}

	private class CompanyName
	{
		TextView tvName;
	}

}
