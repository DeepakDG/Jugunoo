package com.hirecraft.jugunoo.passenger.adapter;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.hirecraft.jugunoo.passenger.R;
import com.hirecraft.jugunoo.passenger.interfaces.PendingOnClickInterface;
import com.indris.material.RippleView;

public class PendingRequestCustomAdapter extends ArrayAdapter<PendingRequestModel> {
	private Context context;
	private int layoutResourceId;
	ArrayList<PendingRequestModel> data ;
	PendingOnClickInterface listener;

	public PendingRequestCustomAdapter(Context context, int layoutResourceId,ArrayList<PendingRequestModel> data, PendingOnClickInterface listener) {
		
		super(context, layoutResourceId, data);
		
		this.layoutResourceId = layoutResourceId;
		this.context = context;
		this.data = data;
		this.listener = listener;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		View row = convertView;
		PendingItemsHolder holder = null;

		if (row == null) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
			row = inflater.inflate(layoutResourceId, parent, false);
			holder = new PendingItemsHolder();
			holder.textgroupName = (TextView) row
					.findViewById(R.id.group_name_item);
			holder.textName = (TextView) row.findViewById(R.id.name_item);
			holder.textEmail = (TextView) row.findViewById(R.id.email_item);
			holder.btnReject = (RippleView) row
					.findViewById(R.id.pending_reject_but);
			holder.btnAccept = (RippleView) row
					.findViewById(R.id.pending_Accept_but);
			holder.textrid=(TextView) row.findViewById(R.id.pendinglist_rid);
			row.setTag(holder);
		} else {
			holder = (PendingItemsHolder) row.getTag();
		}
		PendingRequestModel PendingItems = data.get(position);
		holder.textgroupName.setText(PendingItems.getGroupName());
		holder.textName.setText(PendingItems.getUserName());
		holder.textEmail.setText(PendingItems.getEmailId());
		holder.btnReject.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				listener.statusRejectClick(position);
			}
		});
		holder.btnAccept.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				listener.statusAcceptClick(position);
			}
		});
		return row;

	}

	static class PendingItemsHolder {
		TextView textgroupName;
		TextView textName;
		TextView textEmail;
		TextView textrid;
		RippleView btnReject;
		RippleView btnAccept;
	}
}
