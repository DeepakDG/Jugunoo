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

public class CustomGroupListAdapter extends ArrayAdapter<GroupListModel>
{
	private Context context;
	private int layoutResourceId;
	private List<GroupListModel> data;
	private SharedPreferencesManager mgr;
	private GroupListModel model;
	private GroupName holder = null;

	public CustomGroupListAdapter(Context context, int resource,
			List<GroupListModel> data)
	{
		super(context, resource, data);

		Log.d("CustomGroupListAdapter", "CustomGroupListAdapter called");

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
		// model = data.get(position);

		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

		if (convertView == null)
		{
			try
			{
				convertView = inflater.inflate(layoutResourceId, null);
				holder = new GroupName();

				holder.tvGroupId = (TextView) convertView
						.findViewById(R.id.tvGroupIdTemplate);

				holder.tvGroupName = (TextView) convertView
						.findViewById(R.id.tvGroupNameTemplate);

				holder.tvManagerName = (TextView) convertView
						.findViewById(R.id.tvGroupManagerNameTemplate);

				holder.tvGroupCount = (TextView) convertView
						.findViewById(R.id.tvGroupCountTemplate);

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

		holder.tvGroupId.setText(model.getGroupId());
		holder.tvGroupName.setText(model.getGroupName());
		holder.tvManagerName.setText(model.getManagerName());
		holder.tvGroupCount.setText(model.getCount());

		return convertView;
	}

	private class GroupName
	{
		TextView tvGroupId;
		TextView tvGroupName;
		TextView tvManagerName;
		TextView tvGroupCount;
	}

}
