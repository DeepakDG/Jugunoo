package com.hirecraft.jugunoo.passenger.utility;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.hirecraft.jugunoo.passenger.common.Global;
import com.hirecraft.jugunoo.passenger.listeners.GetMyAddressListener;

public class GetMyAddress extends AsyncTask<String, String, String> {

	Context ctx;
	double latitude, longitude;
	String addresses = "";
	GetMyAddressListener listener;
	List<Address> address;

	public GetMyAddress(Context context, double latitude, double longitude,
			GetMyAddressListener listener) {
		this.ctx = context;
		Log.e("get address", "latt=" + latitude + " " + longitude);

		this.latitude = latitude;
		this.longitude = longitude;
		this.listener = listener;
	}

	@Override
	protected String doInBackground(String... params) {
		Geocoder geocoder = new Geocoder(ctx, Locale.getDefault());
		try {
			address = GetMyAddress.getFromLocation(latitude, longitude, 1);
			if (address.size() > 0) {
				addresses = address.get(0).getAddressLine(0);
//						+ ","
//						+ address.get(0).getLocality();
			}

		} 
		
//		catch (IOException bug) {
//			bug.printStackTrace();
//			addresses = "";
//		} 
		catch (Exception bug) {
			bug.printStackTrace();
			addresses = "";
		}

		return addresses;
	}

	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);

		if (listener != null) {
			
			Log.e("My ADD", "address="+result);
			
			listener.MyAddress(result);
		}
	}

	public JSONObject getLocationFormGoogle(String placesName) {

		HttpGet httpGet = new HttpGet(
				"http://maps.google.com/maps/api/geocode/json?address="
						+ placesName + "&ka&sensor=false");
		HttpClient client = new DefaultHttpClient();
		HttpResponse response;
		StringBuilder stringBuilder = new StringBuilder();

		try {
			response = client.execute(httpGet);
			HttpEntity entity = response.getEntity();
			InputStream stream = entity.getContent();
			int b;
			while ((b = stream.read()) != -1) {
				stringBuilder.append((char) b);
			}
		} catch (ClientProtocolException e) {
		} catch (IOException e) {
		}

		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject = new JSONObject(stringBuilder.toString());
		} catch (JSONException e) {

			e.printStackTrace();
		}

		return jsonObject;
	}

	public LatLng getLatLng(JSONObject jsonObject) {

		Double lon = Double.valueOf(0);
		Double lat = Double.valueOf(0);

		try {

			lon = ((JSONArray) jsonObject.get("results")).getJSONObject(0)
					.getJSONObject("geometry").getJSONObject("location")
					.getDouble("lng");

			lat = ((JSONArray) jsonObject.get("results")).getJSONObject(0)
					.getJSONObject("geometry").getJSONObject("location")
					.getDouble("lat");

		} catch (JSONException e) {
			e.printStackTrace();
		}

		return new LatLng(lat, lon);

	}

	public static List<Address> getFromLocation(double lat, double lng,
			int maxResult) {

		String address = String
				.format(Locale.ENGLISH,
						"http://maps.googleapis.com/maps/api/geocode/json?latlng=%1$f,%2$f&sensor=true&language="
								+ Locale.getDefault().getCountry(), lat, lng);
		HttpGet httpGet = new HttpGet(address);
		HttpClient client = new DefaultHttpClient();
		HttpResponse response;
		StringBuilder stringBuilder = new StringBuilder();

		List<Address> retList = null;

		try {
			response = client.execute(httpGet);
			HttpEntity entity = response.getEntity();
			InputStream stream = entity.getContent();
			int b;
			while ((b = stream.read()) != -1) {
				stringBuilder.append((char) b);
			}

			JSONObject jsonObject = new JSONObject();
			jsonObject = new JSONObject(stringBuilder.toString());

			retList = new ArrayList<Address>();

			if ("OK".equalsIgnoreCase(jsonObject.getString("status"))) {
				JSONArray results = jsonObject.getJSONArray("results");
				for (int i = 0; i < results.length(); i++) {
					JSONObject result = results.getJSONObject(i);
					String indiStr = result.getString("formatted_address");
					Address addr = new Address(Locale.getDefault());
					addr.setAddressLine(0, indiStr);
					retList.add(addr);
				}
			}

		} catch (ClientProtocolException e) {
			Log.e(Global.APPTAG, "Error calling Google geocode webservice.", e);
		} catch (IOException e) {
			Log.e(Global.APPTAG, "Error calling Google geocode webservice.", e);
		} catch (JSONException e) {
			Log.e(Global.APPTAG,
					"Error parsing Google geocode webservice response.", e);
		}

		return retList;
	}
}
