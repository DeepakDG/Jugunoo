package com.hirecraft.jugunoo.passenger.utility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.hirecraft.jugunoo.passenger.listeners.GetDurationAndDistance;

public class DistanceAndDuration extends AsyncTask<String, Void, String> {

	Context context;
	private String distanceStr = "", durationStr = "";
	private GetDurationAndDistance listener;

	public DistanceAndDuration(Context context, GetDurationAndDistance listener) {
		this.context = context;
		this.listener = listener;
	}

	@Override
	protected String doInBackground(String... params) {
	
		String data = "";

		try {
			
			JugunooUtil util = new JugunooUtil(context);
			data = util.downloadUrl(params[0]);
		} catch (Exception e) {
			Log.d("Background Task", e.toString());
		}
		return data;
	}

	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);

		ParserTask parserTask = new ParserTask();
		parserTask.execute(result);
	}

	/** A class to parse the Google Places in JSON format */
	private class ParserTask extends
			AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

		
		@Override
		protected List<List<HashMap<String, String>>> doInBackground(
				String... jsonData) {

			JSONObject jObject;
			List<List<HashMap<String, String>>> routes = null;

			try {
				jObject = new JSONObject(jsonData[0]);
				DirectionsJSONParser parser = new DirectionsJSONParser();
				routes = parser.parse(jObject);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return routes;
		}

		/**
		 * Executes in UI thread, after the parsing process.
		 */
		@Override
		protected void onPostExecute(List<List<HashMap<String, String>>> result) {
			ArrayList<LatLng> points = null;
		

			if (result.size() < 1) {
				return;
			}

			
			for (int i = 0; i < result.size(); i++) {
				points = new ArrayList<LatLng>();
			
				List<HashMap<String, String>> path = result.get(i);

			
				for (int j = 0; j < path.size(); j++) {
					HashMap<String, String> point = path.get(j);

					if (j == 0) { 
						distanceStr = (String) point.get("distance");
						if (listener != null) {
							listener.GetDistance(distanceStr);

						}
						continue;
					} else if (j == 1) {
						durationStr = (String) point.get("duration");

						String duration = durationStr.substring(0, 2);
						duration = durationStr.replaceAll(" ", "");

						if (listener != null) {
							listener.GetDuration(duration);
						}
						
						continue;
					}

					double lat = Double.parseDouble(point.get("lat"));
					double lng = Double.parseDouble(point.get("lng"));
					LatLng position = new LatLng(lat, lng);

					points.add(position);
				}

			}

		}
	}

}
