package com.hirecraft.jugunoo.passenger.utility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.location.Address;
import android.os.AsyncTask;
import android.util.Log;

import com.hirecraft.jugunoo.passenger.listeners.OnUserName;

public class UserName extends AsyncTask<String, String, String> {

	Context ctx;
	String url = "";
	String setServerString = "";
	OnUserName listener;
	List<Address> address;
	private ArrayList<HashMap<String, String>> names = null;
	private String grName, rid;

	public UserName(Context context, String url, OnUserName listener) {
		this.ctx = context;
		this.url = url;
		this.listener = listener;
	}

	@Override
	protected String doInBackground(String... params) {
		HttpClient httpClient = new DefaultHttpClient();
		try {
			HttpGet httpGet = new HttpGet(url);
			ResponseHandler<String> response = new BasicResponseHandler();
			setServerString = httpClient.execute(httpGet, response);
		} catch (Exception bug) {
			bug.printStackTrace();
			setServerString = "";
		}
		return setServerString;
	}

	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);

		if (!result.equalsIgnoreCase("")) {
			names = new ArrayList<HashMap<String, String>>();
			try {
				JSONObject jsonObject = new JSONObject(result);
				String resultStr = jsonObject.getString("Result");
				if (!resultStr.equalsIgnoreCase("Fail")) {
					JSONArray array = jsonObject.getJSONArray("UserArray");
					int len = array.length();
					if (len != 0) {
						for (int f = 0; f < len; f++) {
							JSONObject obj = array.getJSONObject(f);
							grName = obj.getString("FirstName");
							rid = obj.getString("RID");

							HashMap<String, String> fetchData = new HashMap<String, String>();
							fetchData.put("FirstName", grName);
							fetchData.put("RID", rid);
							names.add(fetchData);
						}
						if (listener != null) {
							listener.OnUserName(names);
						}
					} else {
						Log.i("UserName Listener", "No user available");
						names = null;
						if (listener != null) {
							listener.OnUserName(names);
						}
					}
				} else {
					Log.i("UserName Listener", "No user available");
					names = null;
					if (listener != null) {
						listener.OnUserName(names);
					}
				}

			} catch (Exception bug) {
				bug.printStackTrace();
			}

		}
	}
}
