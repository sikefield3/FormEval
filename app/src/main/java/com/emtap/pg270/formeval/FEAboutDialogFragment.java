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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.emtap.pg576.formeval.R;

public class FEAboutDialogFragment extends android.support.v4.app.DialogFragment {
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	    // Get the layout inflater
	    LayoutInflater inflater = getActivity().getLayoutInflater();
	    View oView = inflater.inflate(R.layout.about, null);
	    // Inflate and set the layout for the dialog
	    // Pass null as the parent view because its going in the dialog layout
	    TextView oTVVersion = (TextView)oView.findViewById(R.id.textViewVersion);
	    oTVVersion.setText( ((String) getResources().getText(R.string.str_AboutVersion)).concat(GlobalSettings.getVersionName()));
	    builder.setView(oView);
	    builder.setTitle(R.string.app_name);
	    
	    TextView oTVAck = (TextView)oView.findViewById(R.id.textViewAck);
	    oTVAck.setMovementMethod(LinkMovementMethod.getInstance());
	    

	    String sFileText = UtilFunc.readFileFromRaw(getActivity(), R.raw.apachelicense2_0);
        TextView oTVApacheLicense = (TextView)oView.findViewById(R.id.textViewApacheLicense);
        oTVApacheLicense.setMovementMethod(ScrollingMovementMethod.getInstance());
        oTVApacheLicense.setText(sFileText);

	    
        builder.setNegativeButton(R.string.str_AboutClose, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       // User cancelled the dialog
                   }
               });
        // Create the AlertDialog object and return it
        return builder.create();
    }


}
