package com.hirecraft.jugunoo.passenger.db;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class TripHistoryDB extends SQLiteOpenHelper {
	private static final String LOGCAT = null;

	public TripHistoryDB(Context applicationcontext) {
		super(applicationcontext, "trip.db", null, 1);
		Log.d(LOGCAT, "Created");
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		String query;
		query = "CREATE TABLE trip_history ( tripRowId INTEGER PRIMARY KEY,TripFor TEXT, AddressFrom TEXT, AddressTo TEXT, AddressFromLatLng TEXT,  AddressToLatLng TEXT, UserId TEXT)";
		database.execSQL(query);
		Log.d(LOGCAT, "trip_history table Created");
	}

	@Override
	public void onUpgrade(SQLiteDatabase database, int version_old,
			int current_version) {
		String query;
		query = "DROP TABLE IF EXISTS trip_history";
		database.execSQL(query);
		onCreate(database);
	}

	public void insertBookingHistory(HashMap<String, String> queryValues) {
		SQLiteDatabase database = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("TripFor", queryValues.get("TripFor"));
		values.put("AddressFrom", queryValues.get("AddressFrom"));
		values.put("AddressTo", queryValues.get("AddressTo"));
		values.put("AddressFromLatLng", queryValues.get("AddressFromLatLng"));
		values.put("AddressToLatLng", queryValues.get("AddressToLatLng"));
		values.put("UserId", queryValues.get("UserId"));
		database.insert("trip_history", null, values);
		database.close();
	}

	public void insertFavoriteHistory(HashMap<String, String> queryValues) {
		SQLiteDatabase database = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("TripFor", queryValues.get("TripFor"));
		values.put("AddressFrom", queryValues.get("AddressFrom"));
		values.put("AddressTo", queryValues.get("AddressTo"));
		values.put("AddressFromLatLng", queryValues.get("AddressFromLatLng"));
		values.put("AddressToLatLng", queryValues.get("AddressToLatLng"));
		values.put("UserId", queryValues.get("UserId"));
		database.insert("trip_history", null, values);
		database.close();
	}

	public int updateBookingHistory(HashMap<String, String> queryValues) {
		SQLiteDatabase database = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("TripFor", queryValues.get("TripFor"));
		values.put("AddressFrom", queryValues.get("AddressFrom"));
		values.put("AddressTo", queryValues.get("AddressTo"));
		values.put("AddressFromLatLng", queryValues.get("AddressFromLatLng"));
		values.put("AddressToLatLng", queryValues.get("AddressToLatLng"));
		values.put("UserId", queryValues.get("UserId"));
		return database.update("trip_history", values, "tripRowId" + " = ?",
				new String[] { queryValues.get("tripRowId") });
	}

	public void deleteBookingHistory(String id) {
		Log.d(LOGCAT, "delete");
		SQLiteDatabase database = this.getWritableDatabase();
		String deleteQuery = "DELETE FROM  trip_history where tripRowId='" + id
				+ "'";
		Log.d("query", deleteQuery);
		database.execSQL(deleteQuery);
	}

	public void deleteFavRecentBookingHistory() {

		Log.e(LOGCAT, "deleteFavRecentBookingHistory");
		SQLiteDatabase database = this.getWritableDatabase();
		// String deleteQuery = "DELETE FROM  trip_history where tripRowId='" +
		// id
		// + "'";

	//	String deleteFavQuery = "DELETE FROM trip_history WHERE ROWID IN (SELECT ROWID FROM trip_history WHERE TripFor='Favourite' ORDER BY ROWID ASC LIMIT 10)";
		String deleteHistoryQuery = "DELETE FROM trip_history WHERE ROWID IN (SELECT ROWID FROM trip_history WHERE TripFor='Recent' ORDER BY ROWID ASC LIMIT 2)";

		//Log.e("query", deleteFavQuery);
		Log.e("query", deleteHistoryQuery);
		//database.execSQL(deleteFavQuery);
		database.execSQL(deleteHistoryQuery);

	}

	public ArrayList<HashMap<String, String>> getAllBookingHistory() {
		ArrayList<HashMap<String, String>> bookHisList;
		bookHisList = new ArrayList<HashMap<String, String>>();
		String selectQuery = "SELECT  * FROM trip_history LIMIT 5";
		SQLiteDatabase database = this.getWritableDatabase();
		Cursor cursor = database.rawQuery(selectQuery, null);
		if (cursor.moveToFirst()) {
			do {
				HashMap<String, String> map = new HashMap<String, String>();
				map.put("tripRowId", cursor.getString(0));
				map.put("TripFor", cursor.getString(1));
				map.put("AddressFrom", cursor.getString(2));
				map.put("AddressTo", cursor.getString(3));
				map.put("AddressFromLatLng", cursor.getString(4));
				map.put("AddressToLatLng", cursor.getString(5));
				map.put("UserId", cursor.getString(6));

				bookHisList.add(map);
			} while (cursor.moveToNext());
		}
		cursor.close();

		return bookHisList;
	}

	// tripRowId INTEGER PRIMARY KEY,TripFor TEXT, AddressFrom TEXT, AddressTo
	// TEXT, AddressFromLatLng TEXT, AddressToLatLng TEXT, UserId TEXT

	public ArrayList<HashMap<String, String>> getAllFavoritesHistory() {
		ArrayList<HashMap<String, String>> bookHisList;
		bookHisList = new ArrayList<HashMap<String, String>>();
		String selectQuery = "SELECT  * FROM trip_history LIMIT 5";
		SQLiteDatabase database = this.getWritableDatabase();
		Cursor cursor = database.rawQuery(selectQuery, null);
		if (cursor.moveToFirst()) {
			do {
				HashMap<String, String> map = new HashMap<String, String>();
				map.put("tripRowId", cursor.getString(0));
				map.put("TripFor", cursor.getString(1));
				map.put("AddressFrom", cursor.getString(2));
				map.put("AddressTo", cursor.getString(3));
				map.put("AddressFromLatLng", cursor.getString(4));
				map.put("AddressToLatLng", cursor.getString(5));
				map.put("UserId", cursor.getString(6));

				bookHisList.add(map);
			} while (cursor.moveToNext());
		}
		cursor.close();

		return bookHisList;
	}

	// tripRowId INTEGER PRIMARY KEY,TripFor TEXT, AddressFrom TEXT, AddressTo
	// TEXT, AddressFromLatLng TEXT, AddressToLatLng TEXT, UserId TEXT

	public ArrayList<HashMap<String, String>> getMyBookingHistory(String flag) {
		ArrayList<HashMap<String, String>> bookHisList;
		bookHisList = new ArrayList<HashMap<String, String>>();
		String selectQuery = "SELECT * FROM trip_history where TripFor='"
				+ flag + "'ORDER BY tripRowId DESC LIMIT 5";
		SQLiteDatabase database = this.getWritableDatabase();
		Cursor cursor = database.rawQuery(selectQuery, null);
		if (cursor.moveToFirst()) {
			do {
				HashMap<String, String> map = new HashMap<String, String>();
				map.put("TripFor", cursor.getString(1));
				map.put("AddressFrom", cursor.getString(2));
				map.put("AddressTo", cursor.getString(3));
				map.put("AddressFromLatLng", cursor.getString(4));
				map.put("AddressToLatLng", cursor.getString(5));
				map.put("UserId", cursor.getString(6));
				bookHisList.add(map);
			} while (cursor.moveToNext());
		}
		cursor.close();
		return bookHisList;
	}

	// tripRowId INTEGER PRIMARY KEY,TripFor TEXT, AddressFrom TEXT, AddressTo
	// TEXT, AddressFromLatLng TEXT, AddressToLatLng TEXT, UserId TEXT

	public HashMap<String, String> getBookingHistoryInfo(String id) {
		HashMap<String, String> map = new HashMap<String, String>();
		SQLiteDatabase database = this.getReadableDatabase();
		String selectQuery = "SELECT * FROM trip_history where tripRowId='"
				+ id + "'";
		Cursor cursor = database.rawQuery(selectQuery, null);
		if (cursor.moveToFirst()) {
			do {
				map.put("tripRowId", cursor.getString(0));
				map.put("AddressFrom", cursor.getString(1));
				map.put("AddressTo", cursor.getString(2));
				map.put("AddressFromLatLng", cursor.getString(3));
				map.put("AddressToLatLng", cursor.getString(4));
				map.put("UserId", cursor.getString(5));
			} while (cursor.moveToNext());
		}
		return map;
	}

	public int getBookingHistoryLength(String id) {

		int jobsLength = 0;
		SQLiteDatabase database = this.getReadableDatabase();

		id = "%" + id + "%";
		String selectQuery = "SELECT  * FROM trip_history where UserId like '"
				+ id + "'";
		Cursor cursor = database.rawQuery(selectQuery, null);
		if (cursor.moveToFirst()) {
			do {
				jobsLength = cursor.getCount();
			} while (cursor.moveToNext());
		}

		cursor.close();
		return jobsLength;
	}

	public String isExistBookingHistory(String id) {

		String existingJoId = "";
		SQLiteDatabase database = this.getReadableDatabase();
		String selectQuery = "SELECT * FROM trip_history where UserId='" + id
				+ "'";
		Cursor cursor = database.rawQuery(selectQuery, null);
		if (cursor.moveToFirst()) {
			do {
				existingJoId = cursor.getString(9);
			} while (cursor.moveToNext());
		}

		return existingJoId;
	}
}
