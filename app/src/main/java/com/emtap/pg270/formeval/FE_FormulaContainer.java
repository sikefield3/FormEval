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

import java.util.ArrayList;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.format.Time;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.emtap.pg576.formeval.R;


class FE_FormulaContainer {
//	ArrayList<String> aLStrFormulaHeader; // only the formula, used by setAdapter for ListView (has to be in sync w aLStrFormulaAll)
	FEArrayList<String> aLStrFormulaHeader; // only the formula, used by setAdapter for ListView (has to be in sync w aLStrFormulaAll)
	// ArrayList<String> aLStrFormulaAll;  // formula + all vars
	JSONArray aJSONFormulaAll; // formula + all vars
	final int nJSONFEListVersion = 1; // if the structur of the JSON obj ever gets changed this number has to be increased
	final int nMaxEntries;
	int nPrivDeletePosition; // needed to transfer delete position from askDelete to delete 
	final FragmentListTab oParent;
	LogWrapper lw;
	private static final String TAG = "FE_FormulaContainer";
	Time oCurTime = new Time();
	String sJSONFileName;
	boolean bReadOnly;
	private int nRawResourceFile;
	private boolean bTopBottom = GlobalSettings.isListTopBottom(); 
	String sJSONNameVersionName, sJSONNameVersionCode, sJSONNameFEListVersion, sJSONNameTimestamp, sJSONNameFormula, sJSONNameLeftVar;
	final int nTagOffset = 100;
//	final int nTagSize = 4; // to parse varnames in JSON field and put the vars in appropriate edit fields 
	
	FE_FormulaContainer(FragmentListTab oParParent){
		oParent = oParParent;
		lw = new LogWrapper();
		nMaxEntries = oParParent.getMaxFormulae();
		sJSONFileName = oParent.getContainerFileName();
		bReadOnly = oParent.isReadOnly();
		nRawResourceFile = oParent.getRawResourceFileID(); 
		aJSONFormulaAll = new JSONArray();
//		aLStrFormulaHeader = new ArrayList<String>();
		aLStrFormulaHeader = new FEArrayList<String>(bTopBottom);
		lw.dlog(TAG, oParent.getActivity().getFilesDir().getAbsolutePath().toString());
		sJSONNameVersionName = (String) oParent.getResources().getText(R.string.strFEJSONNameVersionName);
		sJSONNameVersionCode = (String) oParent.getResources().getText(R.string.strFEJSONNameVersionCode);
		sJSONNameFEListVersion = (String) oParent.getResources().getText(R.string.strFEJSONNameFEListVersion);
		sJSONNameTimestamp = (String) oParent.getResources().getText(R.string.strFEJSONNameTimestamp);
		sJSONNameFormula  = (String) oParent.getResources().getText(R.string.strFEJSONNameFormula);
		sJSONNameLeftVar = (String) oParent.getResources().getText(R.string.strFEJSONNameLeftVar);
		readFromMemory();
	}
	public void readFromMemory(){
		JSONObject oJSONHeader;
		String sFileData;
		if (nRawResourceFile >= 0) {
			sFileData = UtilFunc.readFileFromRaw(oParent.getActivity(), nRawResourceFile);
		} else {
			sFileData = UtilFunc.readFile(oParent.getActivity(), sJSONFileName);
		}
		if (sFileData == null) {
			return;
		}
		
		// now look, if we can parse JSON header
		JSONArray aJSONToRead = new JSONArray();
		try {
			aJSONToRead = new JSONArray (sFileData);
			oJSONHeader = aJSONToRead.getJSONObject(0);
			String sVersionName = oJSONHeader.getString(sJSONNameVersionName);
			String sVersionCode = oJSONHeader.getString(sJSONNameVersionCode);
			String sFEListVersion = oJSONHeader.getString(sJSONNameFEListVersion);
			String sTimeStamp = oJSONHeader.getString(sJSONNameTimestamp);
			lw.dlog(TAG, sVersionName);
			lw.dlog(TAG, sVersionCode);
			lw.dlog(TAG, sFEListVersion);
			lw.dlog(TAG, sTimeStamp);			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// now copy all formulae to aJSONFormulaAll
		clear();
		fill(aJSONToRead, aJSONFormulaAll, 1, true);
		updateListView();
	}
	public void writeToMemory(){
		if (bReadOnly) {
			throw new IllegalStateException();
		}
		JSONArray aJSONToWrite = new JSONArray();
		JSONObject oJSONHeader = new JSONObject();
		// create header
		int nVersionCode = GlobalSettings.getVersionCode();
		String sVersionName = GlobalSettings.getVersionName();
		try {
			oJSONHeader.put(sJSONNameVersionName, sVersionName);
			oJSONHeader.put(sJSONNameVersionCode, nVersionCode);
			oJSONHeader.put(sJSONNameFEListVersion, nJSONFEListVersion);
			oCurTime.setToNow();
			oJSONHeader.put(sJSONNameTimestamp, oCurTime.format3339(false));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		aJSONToWrite.put(oJSONHeader);
		// copy all list entries to aJSONToWrite
		fill(aJSONFormulaAll, aJSONToWrite, 0, false);
		// write to memory
		String sFileData = aJSONToWrite.toString();
		UtilFunc.writeFile(oParent.getActivity(), sJSONFileName, sFileData);
	}
	private void clear(){
		aJSONFormulaAll = new JSONArray();
		aLStrFormulaHeader.clear();		
	}
	/**
	 * copy elements from oJSONArraySrc to aJSONFormulaAll and update aLStrFormulaHeader
	 * @param aJSONArraySrc source JSON Array 
	 * @param aJSONArrayDest source JSON Array 
	 * @param nStart start index in oJSONArraySrc 
	 * @param bUpdFrmHeader if true: adapter array for listview gets updated too, use iff aJSONArrayDest == aJSONFormulaAll    
	 */
	private void fill(JSONArray aJSONArraySrc, JSONArray aJSONArrayDest, int nStart, boolean bUpdFrmHeader){
		JSONObject oJSONObj;
		String sFormula;
		try {
			for (int j = nStart;j < aJSONArraySrc.length();j++){
				oJSONObj = aJSONArraySrc.getJSONObject(j);
				aJSONArrayDest.put(oJSONObj);
				if (bUpdFrmHeader) {
					sFormula = oJSONObj.getString(sJSONNameFormula);				
					aLStrFormulaHeader.add(sFormula);
				}
			}} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	public void addFromEditFields(){
		JSONObject oJSONObj = new JSONObject();
		EditText objEditText;
		String sFormula, sTemp;
//		EditText editFieldFormula = oParent..getEditFieldFormInput();
		EditText editFieldFormula = oParent.getFEFormFragment().getETFormInput();
		sFormula = editFieldFormula.getText().toString();
		if (sFormula.length() == 0){
			return;
		}
		// check if we dont excess the maximal number of formulae
		if (aJSONFormulaAll.length() >= oParent.getMaxFormulae()) {
			Toast oToast = Toast.makeText(oParent.getActivity(), (String) oParent.getResources().getText(R.string.strErrorFormTooManyFormulae), Toast.LENGTH_LONG);
			oToast.show();
			return;
		}
		try {
			Toast oToast = Toast.makeText(oParent.getActivity(), (String) oParent.getActivity().getResources().getText(R.string.str_AcToastAddItem), Toast.LENGTH_SHORT);
			oToast.show();
			oJSONObj.put (sJSONNameFormula, sFormula);
			oJSONObj.put (sJSONNameLeftVar, (String) oParent.getResources().getText(R.string.strFEJSONValueLeftVarDummy));
	    	for (int j = 0;j < oParent.getFEFormFragment().getMaxVars(); j++){
	    		objEditText = oParent.getFEFormFragment().getVarNameEditText (j); 
	    		sTemp = objEditText.getText().toString();
	    		if (sTemp.length()!=0) {
	    			sTemp = objEditText.getTag().toString().concat(sTemp);
	    			try {
//	    				oJSONObj.put (sTemp, Double.parseDouble(oParent.getVarValEditText (j).getText().toString()));
	    				oJSONObj.put (sTemp, oParent.getFEFormFragment().getNumber(j));
	    			} catch (NumberFormatException e) { // no number for a var in edit field (Double.parseDouble)  
	    				oJSONObj.put (sTemp, "0.0"); // maybe not so good!
	    			}
	    		}
	    	}
	    	aJSONFormulaAll.put(oJSONObj);
	    	aLStrFormulaHeader.add(sFormula);
	    	lw.dlog(TAG, oJSONObj.toString());
	    	lw.dlog(TAG, aLStrFormulaHeader.toString());
	    	updateListView();
	    	writeToMemory();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	@SuppressWarnings("unchecked")
	private void updateListView() {
		ListView listViewFormulae = (ListView) oParent.getFormListView();
		ArrayAdapter<String> oAdapter = (ArrayAdapter<String>) listViewFormulae.getAdapter();
		if (oAdapter != null) { // to avoid call on creation
			oAdapter.notifyDataSetChanged();
		}
	}
	public FEArrayList<String> getArrayList(){
		return (aLStrFormulaHeader);
	}
	
//	public ArrayList<String> getArrayList(){
//		if (GlobalSettings.isListTopBottom()){
//			return (aLStrFormulaHeader);
//		} else {
//			ArrayList<String> alStrtemp = aLStrFormulaHeader;
//			Collections.reverse(alStrtemp);
//			return (alStrtemp);
//		}
//	}
	private int getArrayPosition(int nPosition) {
		return ((bTopBottom) ? nPosition : aLStrFormulaHeader.size()-1-nPosition);
	}
	public void askDelete(int nPosition){
		// show deleposition for later
		nPrivDeletePosition = getArrayPosition(nPosition);
    	// show alert
    	AlertDialog.Builder builder = new AlertDialog.Builder(oParent.getActivity());
        builder.setMessage(R.string.str_AcAlertDeleteItem)
               .setPositiveButton(R.string.str_AcAlertDeleteItemPos, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       delete();
                   }
               })
               .setNegativeButton(R.string.str_AcAlertDeleteItemNeg, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       // User cancelled the dialog
                   }
               });
        // Create the AlertDialog object and return it
        builder.create();
        builder.show();		
	}
	public int count(){
		return (aJSONFormulaAll.length());
	}
	private void delete(){
		delete (nPrivDeletePosition);
	}
	
	private void delete (int nPosition){
		JSONArray oTmpJSONArray = new JSONArray();
		// get the position
    	lw.dlog(TAG, Integer.toString(nPosition));
		// since we have to be backward compatible, we cannot use 'remove'
		// so we have to copy 
		// aJSONFormulaAll.remove(nPosition);

    	
    	
    	try {
			Toast oToast = Toast.makeText(oParent.getActivity(), (String) oParent.getActivity().getResources().getText(R.string.str_AcToastDeleteItem), Toast.LENGTH_LONG);
			oToast.show();
    		for (int j=0;j < aJSONFormulaAll.length();j++){
    			if (j == nPosition){
    				aLStrFormulaHeader.remove(nPosition);
    			} else {
    				oTmpJSONArray.put(aJSONFormulaAll.getJSONObject(j));
    			}
    		}
    	} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    		
		aJSONFormulaAll = oTmpJSONArray;
		lw.dlog(TAG, aJSONFormulaAll.toString());
		writeToMemory();
		updateListView();
	}
	public boolean isEmpty() {
		return (aJSONFormulaAll.length() <= 0);
	}
	
	public void select (int nPosition){
		String sJSONName, sFormula, sVarName;
		String[] sVarValSplit;
		final String sExpSep = (String) oParent.getResources().getText(R.string.str_ExponentSeparator);
		// to parse varnames in JSON field and put the vars in appropriate edit fields
		final int nTagNumberLen = Integer.toString(nTagOffset).length();
		final int nTagSize = ((String) oParent.getResources().getText(R.string.str_TagVarName)).length() 
				+ nTagNumberLen;

		int nEditFieldIndex;
		nPosition = getArrayPosition(nPosition);
		ArrayList<String> aVarNames = new ArrayList<String>();
		ArrayList<Double> aVarVals = new ArrayList<Double>();
//		EditText editFieldFormula = oParent.getFEFormFragment().getEditFieldFormInput();
		EditText editFieldFormula = oParent.getFEFormFragment().getETFormInput();
		sFormula = "";
		try {
			JSONObject oJSONObj =  aJSONFormulaAll.getJSONObject(nPosition);
			// collect the Name/Value pairs
			Iterator<?> itJSONobj = oJSONObj.keys();
			while (itJSONobj.hasNext()) {
				sJSONName = itJSONobj.next().toString();
				if (sJSONName.equals(sJSONNameFormula)) {
					sFormula = oJSONObj.getString(sJSONName); 
				} else if (!sJSONName.equals(sJSONNameLeftVar)){ // unused so far
					// now we have only vars
					aVarNames.add(sJSONName);
					aVarVals.add(oJSONObj.getDouble(sJSONName));
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// first clear the editfields (might get an extra method !)
		oParent.getFEFormFragment().clearControls();
		
		// now fill the editfields
		if (sFormula.length() == 0) {
			Toast oToast = Toast.makeText(oParent.getActivity(), (String) oParent.getResources().getText(R.string.strErrorFormNoFormula), Toast.LENGTH_LONG);
			oToast.show();
			return;
		}
		editFieldFormula.setText(sFormula);
		for (int j = 0;j < aVarNames.size();j++) {
			sVarName = aVarNames.get(j);
//			nEditFieldIndex = Integer.parseInt (sVarName.substring(1,nTagSize)) - nTagOffset;
			nEditFieldIndex = Integer.parseInt (sVarName.substring(nTagSize - nTagNumberLen,nTagSize)) - nTagOffset;
			sVarName = sVarName.substring(nTagSize);
			oParent.getFEFormFragment().getVarNameEditText(nEditFieldIndex).setText(sVarName);
			
//			oParent.getFEFormFragment().getVarValEditText(nEditFieldIndex).setText(Double.toString(aVarVals.get(j)));
			// split matissa and exponent
			sVarValSplit =  Double.toString(aVarVals.get(j)).split(sExpSep);
			oParent.getFEFormFragment().getVarValEditText(nEditFieldIndex).setText(sVarValSplit[0]);			
			if (sVarValSplit.length >= 2){
				oParent.getFEFormFragment().getVarExpEditText(nEditFieldIndex).setText(sVarValSplit[1]);
			}
		}
//		oParent.gotoFormulaTab();
	}


		

}

