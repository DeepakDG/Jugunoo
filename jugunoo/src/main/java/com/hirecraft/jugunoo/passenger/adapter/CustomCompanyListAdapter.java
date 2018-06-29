package com.hirecraft.jugunoo.passenger.adapter;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.hirecraft.jugunoo.passenger.R;

public class CustomCompanyListAdapter extends BaseAdapter
{

	Context ctx = null;
	List<String> listarray = null;
	private LayoutInflater mInflater = null;

	public CustomCompanyListAdapter(Activity activty, List<String> list)
	{
		this.ctx = activty;
		mInflater = activty.getLayoutInflater();
		this.listarray = list;
	}

	@Override
	public int getCount()
	{

		return listarray.size();
	}

	@Override
	public Object getItem(int arg0)
	{
		return null;
	}

	@Override
	public long getItemId(int arg0)
	{
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup arg2)
	{
		final ViewHolder holder;
		if (convertView == null)
		{
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.alertlistrow, null);

			holder.titlename = (TextView) convertView
					.findViewById(R.id.textView_titllename);
			convertView.setTag(holder);
		}
		else
		{
			holder = (ViewHolder) convertView.getTag();
		}

		String datavalue = listarray.get(position);
		holder.titlename.setText(datavalue);

		return convertView;
	}

	private static class ViewHolder
	{
		TextView titlename;
	}
}
