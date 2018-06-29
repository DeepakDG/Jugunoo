package com.hirecraft.jugunoo.passenger.adapter;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hirecraft.jugunoo.passenger.R;
import com.hirecraft.jugunoo.passenger.common.Function;
import com.hirecraft.jugunoo.passenger.interfaces.TripLogOnClickInterface;
import com.hirecraft.jugunoo.passenger.utility.SharedPreferencesManager;

public class CustomTripLogAdapter extends ArrayAdapter<TripLogModel> {
	private Context context;
	private int layoutResourceId;
	private List<TripLogModel> data;
	private SharedPreferencesManager mgr;
	private TripLogModel model;
	private TripHolder holder = null;
	private TripLogOnClickInterface listener;

	public CustomTripLogAdapter(Context context, int resource,
			List<TripLogModel> data, TripLogOnClickInterface listener) {
		super(context, resource, data);
		this.layoutResourceId = resource;
		this.context = context;
		this.data = data;
		this.listener = listener;
		mgr = new SharedPreferencesManager(getContext());

	}

	public View getView(final int position, View convertView, ViewGroup parent) {

		model = getItem(position);
		// model = data.get(position);

		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

		if (convertView == null) {
			try {
				convertView = inflater.inflate(layoutResourceId, null);
				holder = new TripHolder();
				holder.llenddate = (LinearLayout) convertView
						.findViewById(R.id.llenddate);
				holder.llCompany = (LinearLayout) convertView
						.findViewById(R.id.llCompany);
				holder.txtBookingId = (TextView) convertView
						.findViewById(R.id.tvBookingId);
				holder.txtDate = (TextView) convertView
						.findViewById(R.id.tvDate);
				holder.txtCompanyName = (TextView) convertView
						.findViewById(R.id.tvCompany);
				holder.txtPickPoint = (EditText) convertView
						.findViewById(R.id.startPonit);
				holder.txtDropPoint = (EditText) convertView
						.findViewById(R.id.endPonit);
				holder.txtCabNo = (TextView) convertView
						.findViewById(R.id.tvVehicleNo);
				holder.txtName = (EditText) convertView
						.findViewById(R.id.tvName);
				holder.tvBookname = (TextView) convertView
						.findViewById(R.id.tvbookname);
				holder.enddate = (TextView) convertView
						.findViewById(R.id.enddate);
				holder.tvbooktext = (TextView) convertView
						.findViewById(R.id.tvbooktext);
				holder.llBook = (LinearLayout) convertView
						.findViewById(R.id.llBook);
				holder.llEndPoint = (LinearLayout) convertView
						.findViewById(R.id.llEndPoint);
				holder.txtTripPref = (TextView) convertView
						.findViewById(R.id.tripPref);
				// holder.llBook.setVisibility(View.GONE);
				convertView.setTag(holder);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			holder = (TripHolder) convertView.getTag();
		}

		holder.txtBookingId.setText(model.getBookingId());
		try {
			if (!TextUtils.isEmpty(model.getDate())) {
				holder.txtDate.setText(Function.getDateTimeFromUTC(
						model.getDate(), "dd-MMM-yyyy HH:mm"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			if (!TextUtils.isEmpty(model.getDate())) {

				holder.txtDate.setText(Function.getDateTimeFromUTC(
						model.getDate(), "dd-MMM-yyyy HH:mm"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			if (!TextUtils.isEmpty(model.getEndDate())) {
				holder.enddate.setText(Function.getDateTimeFromUTC(
						model.getEndDate(), "dd-MMM-yyyy HH:mm"));
			} else {
				holder.llenddate.setVisibility(View.GONE);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		holder.txtCompanyName.setText(model.getCompanyName());
		holder.txtPickPoint.setText(model.getStartPoint());
		holder.txtDropPoint.setText(model.getEndPoint());
		holder.txtCabNo.setText(model.getCabNo());
		holder.txtName.setText(model.getName());
		holder.txtTripPref.setText(model.getPref());

		handelView();

		// holder.tvBookname.setText(model.getbookedName());
		// if(model.getbookedNumber().length() == 0 &&
		// model.getbookedName().length() >0){
		// holder.tvBookname.setText(model.getbookedName());
		// }else{
		// holder.tvBookname.setText(model.getbookedNumber());
		//
		// }

		holder.txtPickPoint.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				listener.editTextClicked(position);
			}
		});

		holder.txtDropPoint.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				listener.editTextClicked(position);
			}
		});

		holder.txtName.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				listener.editTextClicked(position);
			}
		});

		return convertView;
	}

	private void handelView() {
		try {
			String usertype = mgr.GetValueFromSharedPrefs("FleetUserType");
			if (model.getCompanyName().length() <= 0
					|| (usertype.equalsIgnoreCase("Normal") && model.getOwn()
							.equalsIgnoreCase("Y"))) {
				holder.llCompany.setVisibility(View.GONE);
			} else {
				holder.llCompany.setVisibility(View.VISIBLE);
			}
			if ((model.getbookedName().length() > 0 && model.getbookedNumber()
					.length() > 0) || model.getbookedNumber().length() > 0) {
				holder.tvbooktext.setText("Booked By :");
				holder.tvBookname.setText(model.getbookedNumber());
			} else {
				holder.tvbooktext.setText("Booked For :");
				holder.tvBookname.setText(model.getbookedName());
			}
			if (TextUtils.isEmpty(holder.tvBookname.getText())
					|| holder.tvBookname.getText().equals("0")) {
				holder.llBook.setVisibility(View.GONE);
			} else {
				holder.llBook.setVisibility(View.VISIBLE);
			}
			if (TextUtils.isEmpty(holder.txtDropPoint.getText())
					|| model.getEndPoint().length() <= 0) {
				holder.llEndPoint.setVisibility(View.GONE);
			} else {
				holder.llEndPoint.setVisibility(View.VISIBLE);
			}
			if (TextUtils.isEmpty(holder.txtCabNo.getText())
					|| holder.txtCabNo.getText().length() <= 0) {
				holder.txtCabNo.setVisibility(View.GONE);
			} else {
				holder.txtCabNo.setVisibility(View.VISIBLE);
			}
			if (model.getName().length() <= 0) {
				holder.txtName.setVisibility(View.GONE);
			} else {
				holder.txtName.setVisibility(View.VISIBLE);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private class TripHolder {
		TextView txtBookingId;
		TextView txtDate;
		TextView txtCompanyName;
		TextView txtPickPoint;
		TextView txtDropPoint;
		TextView txtCabNo;
		TextView txtName;
		LinearLayout llCompany;
		TextView tvBookname;
		TextView tvbooktext;
		LinearLayout llBook;
		LinearLayout llenddate;
		TextView enddate;
		TextView txtTripPref;
		LinearLayout llEndPoint;
	}
}
