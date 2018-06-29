package com.hirecraft.jugunoo.passenger.adapter;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hirecraft.jugunoo.passenger.R;
import com.hirecraft.jugunoo.passenger.interfaces.FavoriteDriverOnClickInterface;

public class CustomFavoriteDriversAdapter extends
		ArrayAdapter<FavoriteDriverModel>
{

	private Context context;
	private int layoutResourceId;
	ArrayList<FavoriteDriverModel> data;
	FavoriteDriverOnClickInterface fav_listener;

	public CustomFavoriteDriversAdapter(Context context, int layoutResourceId,
			ArrayList<FavoriteDriverModel> data,
			FavoriteDriverOnClickInterface fav_listener)
	{

		super(context, layoutResourceId, data);

		this.layoutResourceId = layoutResourceId;
		this.context = context;
		this.data = data;
		this.fav_listener = fav_listener;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent)
	{
		View row = convertView;
		DriverItemsHolder holder = null;

		if (row == null)
		{
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
			row = inflater.inflate(layoutResourceId, parent, false);
			holder = new DriverItemsHolder();
			holder.textdriverName = (TextView) row.findViewById(R.id.txt_driverename);
			holder.driver_rid=(TextView) row.findViewById(R.id.txt_driverrid);
			holder.favDriverDelete=(ImageView) row.findViewById(R.id.favDriverDelete_but);
			row.setTag(holder);
		}
		else
		{
			holder = (DriverItemsHolder) row.getTag();
		}
		FavoriteDriverModel favdrivers_Items = data.get(position);
		holder.textdriverName.setText(favdrivers_Items.getDriverName());
		holder.favDriverDelete.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				fav_listener.favoriteDriverDeleteClicked(position);
			}
		});
		return row;

	}

	static class DriverItemsHolder
	{
		TextView textdriverName;
		TextView driver_rid;
		ImageView favDriverDelete;
	}
}
