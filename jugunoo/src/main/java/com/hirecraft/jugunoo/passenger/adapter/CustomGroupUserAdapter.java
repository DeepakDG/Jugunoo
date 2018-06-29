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

public class CustomGroupUserAdapter extends ArrayAdapter<GroupUserModel>
{
	private Context context;
	private int layoutResourceId;
	private List<GroupUserModel> data;
	private SharedPreferencesManager mgr;
	private GroupUserModel model;
	private GroupUser holder = null;

	public CustomGroupUserAdapter(Context context, int resource,
			List<GroupUserModel> data)
	{
		super(context, resource, data);
		Log.d("CustomGroupUserAdapter", "CustomGroupUserAdapter called");

		this.context = context;
		this.layoutResourceId = resource;
		this.data = data;
		mgr = new SharedPreferencesManager(getContext());

	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent)
	{
		Log.d("CustomGroupUserAdapter getView",
				"CustomGroupUserAdapter getView called");

		model = getItem(position);
		// model = data.get(position);

		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

		if (convertView == null)
		{
			try
			{
				convertView = inflater.inflate(layoutResourceId, null);
				holder = new GroupUser();

				// holder.llMain_GroupUsers = (LinearLayout) convertView
				// .findViewById(R.id.llMain_GroupUsers);

				holder.tvGroupUserId = (TextView) convertView
						.findViewById(R.id.tvUserId_GroupUser);

				holder.tvGroupUserName = (TextView) convertView
						.findViewById(R.id.tvUserName_GroupUsers);

				holder.tvGroupUserNoOfTrips = (TextView) convertView
						.findViewById(R.id.tvNoOfTrips_GroupUsers);

				holder.tvGroupUserKms = (TextView) convertView
						.findViewById(R.id.tvKms_GroupUsers);

				holder.tvGroupUserAmount = (TextView) convertView
						.findViewById(R.id.tvAmount_GroupUsers);

				convertView.setTag(holder);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			holder = (GroupUser) convertView.getTag();
		}

		holder.tvGroupUserId.setText(model.getUserId());
		holder.tvGroupUserName.setText(model.getUserName());
		holder.tvGroupUserNoOfTrips.setText(model.getCount());
		holder.tvGroupUserKms.setText(model.getKMS());
		holder.tvGroupUserAmount.setText(model.getAmount());

		return convertView;
	}

	private class GroupUser
	{

		TextView tvGroupUserId;
		TextView tvGroupUserName;
		TextView tvGroupUserNoOfTrips;
		TextView tvGroupUserKms;
		TextView tvGroupUserAmount;
	}

}
