package com.hirecraft.jugunoo.passenger.adapter;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hirecraft.jugunoo.passenger.R;

public class MenuAdapter extends BaseAdapter
{

	private ViewHolder holder = null;
	private MenuItems row_pos;
	String usertype;
	Context context;
	List<MenuItems> rowItems;
	private static final int droidred = Color.parseColor("#FF4040");

	public MenuAdapter(Context context, List<MenuItems> rowItems)
	{
		this.context = context;
		this.rowItems = rowItems;
	}

	@Override
	public int getCount()
	{
		return rowItems.size();
	}

	@Override
	public Object getItem(int position)
	{
		return rowItems.get(position);
	}

	@Override
	public long getItemId(int position)
	{
		return rowItems.indexOf(getItem(position));
	}

	private class ViewHolder
	{
		ImageView profile_pic;
		TextView member_name;
		TextView text;
		BadgeView badge;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{

		LayoutInflater mInflater = (LayoutInflater) context
				.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		if (convertView == null)
		{
			convertView = mInflater.inflate(R.layout.drawer_list_item, null);
			holder = new ViewHolder();

			holder.text = (TextView) convertView.findViewById(R.id.badge);
			holder.badge = new BadgeView(context, holder.text);
			holder.badge.setBadgeBackgroundColor(droidred);
			holder.badge.setTextColor(Color.WHITE);
			holder.badge.setBadgePosition(BadgeView.POSITION_CENTER);

			holder.member_name = (TextView) convertView
					.findViewById(R.id.title);
			holder.profile_pic = (ImageView) convertView
					.findViewById(R.id.icon);

			row_pos = rowItems.get(position);

			holder.profile_pic.setImageResource(row_pos.getProfile_pic_id());
			holder.member_name.setText(row_pos.getMember_name());
			handlebadge();
			convertView.setTag(holder);
		}
		else
		{
			holder = (ViewHolder) convertView.getTag();
		}

		return convertView;
	}

	private void handlebadge()
	{
		String cnt = row_pos.getCount();
		if (row_pos.getMember_name().equalsIgnoreCase("Requests") && cnt != null)
		{	
			holder.badge.setText(String.valueOf(cnt));
			holder.badge.show();
		}else if(cnt == null){
			holder.badge.hide();
		}
	}
}
