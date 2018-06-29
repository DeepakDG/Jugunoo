package com.hirecraft.jugunoo.passenger.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hirecraft.jugunoo.passenger.R;

public class ProfileAdapter extends ArrayAdapter<ProfileModel> {

	Context context;
	int layoutResourceId;
	ProfileModel data[] = null;

	public ProfileAdapter(Context context, int layoutResourceId,
			ProfileModel[] data) {
		super(context, layoutResourceId, data);
		this.layoutResourceId = layoutResourceId;
		this.context = context;
		this.data = data;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		ProfileModelHolder holder = null;

		if (row == null) {
			LayoutInflater inflater = ((Activity) context).getLayoutInflater();
			row = inflater.inflate(layoutResourceId, parent, false);

			holder = new ProfileModelHolder();
//			holder.imgIcon = (ImageView) row.findViewById(R.id.imgIcon);
			holder.txtGroupName = (TextView) row
					.findViewById(R.id.txtGroupName);
			holder.txtMgrName = (TextView) row.findViewById(R.id.txtMgrName);
			holder.status = (TextView) row.findViewById(R.id.isEnable);
			holder.txtCount = (TextView) row.findViewById(R.id.txtCounter);
			holder.imgIcon.setFocusable(false);
			row.setTag(holder);
		} else {
			holder = (ProfileModelHolder) row.getTag();
		}

		final ProfileModel ProfileModel = data[position];
		holder.txtGroupName.setText(ProfileModel.groupName);
		holder.txtMgrName.setText(ProfileModel.mgrName);
		holder.status.setText(ProfileModel.status);
		holder.imgIcon.setImageResource(ProfileModel.icon);
		holder.txtCount.setText(ProfileModel.count);

//		if (Global.SELECTED_ITEM_FO == position)
//			row.setBackgroundColor(Color.rgb(180, 215, 122));
//		else if (Global.SELECTED_ITEM_G == position)
//			row.setBackgroundColor(Color.rgb(180, 215, 122));
//		else if (Global.SELECTED_ITEM_U == position)
//			row.setBackgroundColor(Color.rgb(180, 215, 122));

		return row;
	}

	static class ProfileModelHolder {
		ImageView imgIcon;
		TextView txtGroupName;
		TextView txtMgrName;
		TextView txtCount;
		TextView status;
	}
}
