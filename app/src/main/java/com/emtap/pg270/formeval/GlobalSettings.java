/*
 * FormEval - Simple calculator tool for Android 
 * Copyright (C) 2013-19 Bernd Zemann - emtap@arcor.de
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.emtap.pg270.formeval;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.text.format.Time;

import com.emtap.pg576.formeval.R;

final class GlobalSettings {
	private static GlobalSettings oGlobalSettings = new GlobalSettings();
	private GlobalSettings() {}
	private static boolean bConfigured = false;
	private static boolean bDebuggable = true;
	private static boolean bAdsEnabled = false;
	private static boolean bListTopBottom = false;
	private static int nVersionCode = -1;
	private static String sVersionName;
	private static String sOperatorSymols; 
	
	// Preference Values
	private static String sPrefFileName, sJSONNameVersionName, sJSONNameVersionCode, sJSONNameTimestamp, strJSONNamePrefValFirstStart,sJSONNamePrefValFortran, sJSONNameAngleDegree;
	
	private static boolean bPrefValFirstStart, bPrefValFortran, bPrefValAngleConv;
	
	public static GlobalSettings getInstance( ) {
	      return oGlobalSettings;
	}
	public static void configure (Context oCtx) {
		GlobalSettings.bDebuggable = false;
		 
	    PackageManager pm = oCtx.getPackageManager();
	    try
	    {
	        ApplicationInfo appinfo = pm.getApplicationInfo(oCtx.getPackageName(), 0);
	        GlobalSettings.bDebuggable = (0 != (appinfo.flags &= ApplicationInfo.FLAG_DEBUGGABLE));
	    }
	    catch(NameNotFoundException e)
	    {
	        /*debuggable variable will remain false*/
	    }
		try {
			PackageInfo pinfo = pm.getPackageInfo(oCtx.getPackageName(), 0);
			nVersionCode = pinfo.versionCode;
			sVersionName = pinfo.versionName;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// get preferences
		// check if we can read form file
		setJSONNames(oCtx);
		sOperatorSymols = (String) oCtx.getResources().getText(R.string.str_Exp4jOperatorSymbols);
		bConfigured = true;
		// only now we can call readPreferences(oCtx) 
		readPreferences(oCtx);
	}
	public static void readPreferences(Context oCtx) {
		checkConfigured();
		String sPrefFileData = UtilFunc.readFile(oCtx, sPrefFileName);
		if (sPrefFileData == null) { // get default values from xml
			getDefaultPreferences(oCtx);
			// so we have a preference file next time
			writePreferences(oCtx);
			return;
		}
		try {
			JSONObject oJSONPreferences = new JSONObject (sPrefFileData);
			@SuppressWarnings("unused")
			String sVersionName = oJSONPreferences.getString(sJSONNameVersionName);
			@SuppressWarnings("unused")
			String sVersionCode = oJSONPreferences.getString(sJSONNameVersionCode);
			@SuppressWarnings("unused")
			String sTimeStamp = oJSONPreferences.getString(sJSONNameTimestamp);
			bPrefValFirstStart = oJSONPreferences.getBoolean(strJSONNamePrefValFirstStart);			
			bPrefValFortran = oJSONPreferences.getBoolean(sJSONNamePrefValFortran);
			bPrefValAngleConv = oJSONPreferences.getBoolean(sJSONNameAngleDegree);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			getDefaultPreferences(oCtx);
			return;			
		}		
	}
	public static void writePreferences(Context oCtx) {		
		checkConfigured();
		Time oCurTime = new Time();
		JSONObject oJSONPreferences = new JSONObject();
		try {
			oJSONPreferences.put(sJSONNameVersionName, sVersionName);
			oJSONPreferences.put(sJSONNameVersionCode, nVersionCode);
			oCurTime.setToNow();
			oJSONPreferences.put(sJSONNameTimestamp, oCurTime.format3339(false));
			
			oJSONPreferences.put(strJSONNamePrefValFirstStart, bPrefValFirstStart);
			oJSONPreferences.put(sJSONNamePrefValFortran, bPrefValFortran);
			oJSONPreferences.put(sJSONNameAngleDegree, bPrefValAngleConv);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		UtilFunc.writeFile(oCtx, sPrefFileName, oJSONPreferences.toString());
	}
	private static void setJSONNames(Context oCtx) {
		sPrefFileName = (String) oCtx.getResources().getText(R.string.str_pref_file);
		
		sJSONNameVersionName = (String) oCtx.getResources().getText(R.string.strFEJSONNameVersionName);
		sJSONNameVersionCode = (String) oCtx.getResources().getText(R.string.strFEJSONNameVersionCode);
		sJSONNameTimestamp = (String) oCtx.getResources().getText(R.string.strFEJSONNameTimestamp);
		
		strJSONNamePrefValFirstStart = (String) oCtx.getResources().getText(R.string.strFEJSONNamePrefValFirstStart);
		sJSONNamePrefValFortran = (String) oCtx.getResources().getText(R.string.strFEJSONNamePrefValFortran);
		sJSONNameAngleDegree = (String) oCtx.getResources().getText(R.string.strFEJSONNamePrefValAngleDegree);
	}	
	private static void getDefaultPreferences(Context oCtx) {
		bPrefValFirstStart = oCtx.getResources().getBoolean(R.bool.bPrefValFirstStart);
		bPrefValFortran = oCtx.getResources().getBoolean(R.bool.bPrefValFortran);
		bPrefValAngleConv = oCtx.getResources().getBoolean(R.bool.bPrefValAngleDegree);
	}	
	public static boolean isConfigured() {
		return bConfigured;
	}
	private static void checkConfigured(){
		if (!isConfigured()) {
			throw new IllegalStateException();
		}
	}
	public static boolean isDebuggable() {
		checkConfigured();
		return GlobalSettings.bDebuggable;
	}
	public static boolean isAdsEnabled() {
		checkConfigured();
		return bAdsEnabled;
	}
	public static boolean isListTopBottom() {
		checkConfigured();
		return bListTopBottom;
	}	
	public static int getVersionCode() {
		checkConfigured();
		return nVersionCode;
	}
	public static String getVersionName() {
		checkConfigured();
		return sVersionName;
	}
	public static boolean isFirstStart() {
		checkConfigured();
		return bPrefValFirstStart;
	}
	public static void setFirstStart(Context oCtx, boolean bPrefValFirstStart) {
		GlobalSettings.bPrefValFirstStart = bPrefValFirstStart;
		writePreferences(oCtx);
	}
	
	/**
	 * @return true: use '**' instead of '^' (useful for included softkeyboards)
	 */
	public static boolean getFortranStyle() {
		checkConfigured();
		return bPrefValFortran;
	}
	public static boolean getAngleDegree() {
		checkConfigured();
		return bPrefValAngleConv;
	}
	public static void setPrefValFortran(Context oCtx, boolean bPrefValFortran) {
		GlobalSettings.bPrefValFortran = bPrefValFortran;
		writePreferences(oCtx);
	}
	public static void setPrefValAngleConv(Context oCtx, boolean bPrefValAngleConv) {
		GlobalSettings.bPrefValAngleConv = bPrefValAngleConv;
		writePreferences(oCtx);
	}
	public static String getOperatorSymbols(){
		checkConfigured();
		return (sOperatorSymols);
	}
}
