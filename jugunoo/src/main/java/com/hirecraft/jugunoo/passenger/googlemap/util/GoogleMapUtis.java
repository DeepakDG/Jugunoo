package com.hirecraft.jugunoo.passenger.googlemap.util;

import java.util.ArrayList;
import java.util.List;

import android.location.Location;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;

public class GoogleMapUtis {

	public static void toggleStyle(GoogleMap googleMap) {
		if (GoogleMap.MAP_TYPE_NORMAL == googleMap.getMapType()) {
			googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
		} else {
			googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
		}
	}

	public static Location convertLatLngToLocation(LatLng latLng) {
		Location loc = new Location("someLoc");
		loc.setLatitude(latLng.latitude);
		loc.setLongitude(latLng.longitude);
		return loc;
	}

	public static float bearingBetweenLatLngs(LatLng begin, LatLng end) {
		Location beginL = convertLatLngToLocation(begin);
		Location endL = convertLatLngToLocation(end);
		return beginL.bearingTo(endL);
	}

	public static String encodeMarkerForDirection(Marker marker) {
		return marker.getPosition().latitude + ","
				+ marker.getPosition().longitude;
	}

	public static void fixZoomForLatLngs(GoogleMap googleMap,
			List<LatLng> latLngs) {
		if (latLngs != null && latLngs.size() > 0) {
			LatLngBounds.Builder bc = new LatLngBounds.Builder();

			for (LatLng latLng : latLngs) {
				bc.include(latLng);
			}
			CameraUpdate center = CameraUpdateFactory.newLatLngBounds(
					bc.build(), 50);
			googleMap.moveCamera(center);

		}
	}

	public static void fixZoomForMarkers(GoogleMap googleMap,
			List<Marker> markers) {
		if (markers != null && markers.size() > 0) {
			LatLngBounds.Builder bc = new LatLngBounds.Builder();

			for (Marker marker : markers) {
				bc.include(marker.getPosition());
			}
			CameraUpdate center = CameraUpdateFactory.newLatLngBounds(
					bc.build(), 50);
			googleMap.moveCamera(center);
		}
	}

}
