package com.hirecraft.jugunoo.passenger.utility;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class SharedPreferencesManager {

	Context ctx;
	SharedPreferences sharedpreferences;

	public SharedPreferencesManager(Context context) {
		ctx = context;
		sharedpreferences = ctx.getSharedPreferences("Jugunoo",
				Context.MODE_PRIVATE);
	}

	public SharedPreferences getGcmPreferences() {

		return sharedpreferences;
	}

	public String GetValueFromSharedPrefs(String KeyName) {
		return sharedpreferences.getString(KeyName, "");
	}
	
	public String GetNotiValueFromSharedPrefs(String KeyName) {
		return sharedpreferences.getString(KeyName, "XXX");
	}

	public boolean SaveValueToSharedPrefs(String KeyName, String value) {
		Editor editor = sharedpreferences.edit();
		editor.putString(KeyName, value);
		editor.commit();
		return true;
	}
	
	public boolean SaveValueToSharedPrefs(String KeyName, Boolean value) {
		Editor editor = sharedpreferences.edit();
		editor.putBoolean(KeyName, value);
		editor.commit();
		return true;
	}

	public int GetIntFromSharedPrefs(String KeyName) {
		return sharedpreferences.getInt(KeyName, 1);
	}

	public boolean GetBoolFromSharedPrefs(String KeyName) {
		return sharedpreferences.getBoolean(KeyName, false);
	}
	
	
	public void savePreferenceIndex(String keyName, int index){
		Editor editor = sharedpreferences.edit();
		editor.putInt(keyName, index);
		editor.commit();
	}
	
	public int getPreferenceIndex(String keyName){
		return sharedpreferences.getInt(keyName, 0);
	}
	

}
