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

public class CustomGroupNameAdapter extends ArrayAdapter<GroupNameModel>
{
	private Context context;
	private int layoutResourceId;
	private List<GroupNameModel> data;
	private SharedPreferencesManager mgr;
	private GroupNameModel model;
	private GroupName holder = null;

	public CustomGroupNameAdapter(Context context, int resource,
			List<GroupNameModel> data)
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
				holder = new GroupName();

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
			holder = (GroupName) convertView.getTag();
		}

		holder.tvName.setText(model.getGroupName());
		return convertView;
	}

	private class GroupName
	{
		TextView tvName;
	}

}
