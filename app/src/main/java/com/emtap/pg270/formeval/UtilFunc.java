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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import android.content.Context;
import android.widget.Toast;

import com.emtap.pg576.formeval.R;

public final class UtilFunc {
	private static final  String TAG = "UtilFunc";
	private static LogWrapper lw = new LogWrapper();
	private static UtilFunc oUtilFunc = new UtilFunc();
	private UtilFunc() {}
	public static UtilFunc getInstance( ) {
	      return oUtilFunc;
	}
	
	public static String readFileFromRaw(Context oCtx, int nRawResource) {	
	    InputStream inputStream = oCtx.getResources().openRawResource(nRawResource);
	
	    InputStreamReader inputreader = new InputStreamReader(inputStream);
	    BufferedReader bufferedreader = new BufferedReader(inputreader);
	    String line;
	    StringBuilder stringBuilder = new StringBuilder();
	    try 
	    {
	        while (( line = bufferedreader.readLine()) != null) 
	        {
	            stringBuilder.append(line);
	            stringBuilder.append('\n');
	        }
	    } 
	    catch (IOException e) 
	    {
	        return null;
	    }
	    return (stringBuilder.toString());
	}
	
	public static String readFile(Context oCtx, String sFName) {
		boolean bShowMsg = true;
		String sFileData  = "";
		

		try {
			File oTmpFile =  oCtx.getFileStreamPath(sFName);
			lw.dlog(TAG, "oCtx.getApplicationInfo().dataDir: ".concat(oCtx.getApplicationInfo().dataDir));
			lw.dlog(TAG, "oTmpFile: ".concat(oTmpFile.toString()));
			lw.dlog(TAG, "oTmpFile: ".concat(oTmpFile.getAbsolutePath()));
			if (!oTmpFile.exists()) { // first start return wo msg
				lw.dlog(TAG, "File not yet created, no error");
				bShowMsg = false;								
			} 
			FileInputStream oFIS = oCtx.openFileInput(sFName);
	        if (oFIS != null ) {
	            InputStreamReader objInputStreamReader = new InputStreamReader(oFIS);
	            BufferedReader bufferedReader = new BufferedReader(objInputStreamReader);
	            String sReceiveString = "";
	            StringBuilder objStringBuilder = new StringBuilder();

	            while ( (sReceiveString = bufferedReader.readLine()) != null ) {
	            	objStringBuilder.append(sReceiveString);
	            }
	            oFIS.close();
	            sFileData = objStringBuilder.toString();
	        }
		} catch (FileNotFoundException e) { // no errormsg
			lw.dlog(TAG, "FileNotFoundException");
			bShowMsg = false;
		} catch (Exception e) {
			lw.dlog(TAG, "Generic Exception");
			e.printStackTrace();
		} finally {
			lw.dlog(TAG, "readFromMemory() finally");
			if (sFileData.length()== 0) {
				if (bShowMsg) {			
					Toast oToast = Toast.makeText(oCtx, (String) oCtx.getResources().getText(R.string.strFEJSONErrorMsgFileRead), Toast.LENGTH_LONG);
					oToast.show();
				}
				return null;
			}			
		}
		return sFileData;
	}
	public static void writeFile(Context oCtx, String sJSONFileName, String sFileData) {
		try {
			FileOutputStream oFOS = oCtx.openFileOutput(sJSONFileName, Context.MODE_PRIVATE);
			OutputStreamWriter objOutputStreamWriter = new OutputStreamWriter(oFOS);
			objOutputStreamWriter.write(sFileData);
			objOutputStreamWriter.close();			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Toast oToast = Toast.makeText(oCtx, (String) oCtx.getText(R.string.strFEJSONErrorMsgFileSave), Toast.LENGTH_LONG);
			oToast.show();
			e.printStackTrace();
		}
	}


}
