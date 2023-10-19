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

import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TextView;

import com.emtap.pg576.formeval.R;

public class FragmentFormulaTab extends FEAbstractTabFragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
	FE_EvalWrapper oFE_EvalWrapper;
//	FE_FormulaContainer oFE_FormulaContainer;
	// 
	EditText oETFormInput;
	TextView oTextViewResult;
	
	final int nFormulaLength = 100;
	final int nVarNameLength = 8;
	final int nExponentSize = 2; // vars < 10^100 
	final int nExponentFieldLenght = nExponentSize + 1; // including sign ! 
	

	private LogWrapper lw;
    final String TAG = "FragmentFormulaTab";
    int nMaxVars; // better: check it on Start!
    String sClipLabel;

    TextWatcher oTextWatcherETFormula, oTextWatcherETVarname, oTextWatcherETExp;
	final String sPatternETVarnameForb = "[[^a-z]&&[^A-Z]&&[^0-9]]";
	final Pattern oPatternETVarnameForb = Pattern.compile(sPatternETVarnameForb);
	final Pattern oPatternETVarnameFirst = Pattern.compile("[[a-z][A-Z]]");
	final String sPatternETFormulaForb = "[[^a-z]&&[^A-Z]&&[^0-9]]";
	final Pattern oPatternETFormulaForb = Pattern.compile(sPatternETFormulaForb);
	String sAcceptedSymb;
	
	private boolean bResultError;
    public FragmentFormulaTab() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	View oRootView = onCreateView(inflater, container,savedInstanceState,R.layout.fe_formulafragment);
    	lw = new LogWrapper();
    	lw.dlog(TAG, "onCreateView: ");
    	
        nMaxVars = ((ViewGroup)getFEChildView(R.id.tableLayoutVars)).getChildCount();
        lw.dlog(TAG, "onCreateView: ".concat(Integer.toString(nMaxVars)));
        sClipLabel = (String) getResources().getText(R.string.app_name);
        sClipLabel.concat((String) getResources().getText(R.string.clip_label));
        oETFormInput = (EditText)  getFEChildView(R.id.editTextFormEditFormInput);
        oETFormInput.addTextChangedListener(new TextWatcher(){
            public void afterTextChanged(Editable s) {
            	updateStatusBar();
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after){}
            public void onTextChanged(CharSequence s, int start, int before, int count){}
        });
        oTextViewResult = (TextView) getFEChildView(R.id.textViewFormEditResult);
        
        oFE_EvalWrapper = new FE_EvalWrapper();
        oFE_EvalWrapper.setJuxtaPosition(false);
        oFE_EvalWrapper.setbSignWorkaround(true);
        oFE_EvalWrapper.useMathConstants(true);
        oFE_EvalWrapper.setMathConstNameE((String) getResources().getText(R.string.str_MathConstNameE));
        oFE_EvalWrapper.setMathConstNamePi((String) getResources().getText(R.string.str_MathConstNamePi));
        oFE_EvalWrapper.setFortranExp((String) getResources().getText(R.string.str_FORTRAN_ExpSymb));
        oFE_EvalWrapper.setCommonExp((String) getResources().getText(R.string.str_CommonExpSymb));
        oFE_EvalWrapper.setTrigConvFunctionName((String) getResources().getText(R.string.str_Exp4jTrigConvFunc));
        oFE_EvalWrapper.setLog10Name((String) getResources().getText(R.string.str_Exp4jLog10Func));
        oFE_EvalWrapper.confInternalFuncs(); // !!!!!! This has to be called after the settings
//        oFE_EvalWrapper.testtrg();
//        oFE_EvalWrapper.testtrg1Param();
//        oFE_EvalWrapper.testCustFunc();
        // TODO currently this is not used, regexp has to be adjusted
        // we use filter in confedittext
        oTextWatcherETFormula = new TextWatcher() {

            public void afterTextChanged(Editable s) {
                Matcher matcher = oPatternETFormulaForb.matcher(s);
                if (matcher.find())
                {
                    s.replace(0, s.length(), s.toString().replaceAll(sPatternETFormulaForb, ""));
                }
            }

            public void beforeTextChanged(CharSequence s, int start,
                    int before, int count) {
                // TODO Auto-generated method stub
            }

            public void onTextChanged(CharSequence s, int start, int before,
                    int count) {
                // TODO Auto-generated method stub
            }

        };
        
        oTextWatcherETVarname = new TextWatcher() {

            public void afterTextChanged(Editable s) {
                Matcher matcher = oPatternETVarnameForb.matcher(s);
                if (matcher.find())
                {
                    s.replace(0, s.length(), s.toString().replaceAll(sPatternETVarnameForb, ""));
                }
            	if (s.length() <= 0) {
            		return;
            	}                
                char cFirstChar = s.charAt(0);
                if (cFirstChar >= '0' && cFirstChar <= '9') {
                	s.delete(0, 1);
                }
                
            }

            public void beforeTextChanged(CharSequence s, int start,
                    int before, int count) {
                // TODO Auto-generated method stub
            }

            public void onTextChanged(CharSequence s, int start, int before,
                    int count) {
                // TODO Auto-generated method stub
            }

        };
        oTextWatcherETExp  = new TextWatcher() {
            public void afterTextChanged(Editable s) {
            	final char cMS = new DecimalFormatSymbols().getMinusSign();
            	if (s.length() <= 0 || s.charAt(0) == cMS) {
            		return;
            	}
            	if (s.length() > nExponentSize) {
            		s.delete(s.length()-1, s.length());
            	}
            }

            public void beforeTextChanged(CharSequence s, int start,
                    int before, int count) {
                // TODO Auto-generated method stub
            }

            public void onTextChanged(CharSequence s, int start, int before,
                    int count) {
                // TODO Auto-generated method stub
            }
        };
        confEditTexts();


        updateStatusBar();
//        oFE_EvalWrapper.testExp4j();
        return oRootView;
    } 
    public void onPause() {
    	super.onPause();
    	lw.dlog(TAG, "onPause");
    }
    public void onResume() {
    	super.onResume();
    	lw.dlog(TAG, "onResume");
    	refreshResult();
    }
    public void onStop() {
    	super.onStop();
    	lw.dlog(TAG, "onStop");
    }
    public void onDestroyView() {
    	super.onDestroyView();
    	lw.dlog(TAG, "onDestroyView");
    }
    public void onDestroy() {
    	super.onDestroy();
    	lw.dlog(TAG, "onDestroy");
    }
    public void onDetach() {
    	super.onDetach();
    	lw.dlog(TAG, "onDetach");
    }
    public void refreshTab(){
    	try {
	    	getVarNameEditText(0).requestFocus(); // cheat to scroll to top
	    	getETFormInput().requestFocus();
			updateStatusBar();
    	} catch (NullPointerException e) { // to fix 
    		LogWrapper lw = new LogWrapper();
    		lw.dlog(TAG,  "refreshTab() : NullPointerException funny crash");
//			Toast oToast = Toast.makeText(getActivity(), TAG.concat("refreshTab() : NullPointerException funny crash"), Toast.LENGTH_LONG);
//			oToast.show();    		
    		e.printStackTrace();
    	}
    }    
	public int getMaxVars() {
		return nMaxVars;
	}
	EditText getETFormInput (){
		return (oETFormInput);		
	}
	TextView getTextViewResult(){
		return (oTextViewResult);
	}
    EditText getVarNameEditText (int nIndex) {
    	Object objTag = ((String) getResources().getText(R.string.str_TagVarName)).concat (Integer.toString(nIndex+100));
    	TableLayout oTableLayoutVars = (TableLayout) getFEChildView(R.id.tableLayoutVars);    	
    	return ((EditText)  oTableLayoutVars.findViewWithTag(objTag));
    }
    EditText getVarValEditText (int nIndex) {
    	Object objTag = ((String) getResources().getText(R.string.str_TagVarVAL)).concat(Integer.toString(nIndex+100));
    	TableLayout oTableLayoutVars = (TableLayout) getFEChildView(R.id.tableLayoutVars);    	
    	return ((EditText)  oTableLayoutVars.findViewWithTag(objTag));
    }
    EditText getVarExpEditText (int nIndex) {
    	Object objTag = ((String) getResources().getText(R.string.str_TagExpo)).concat(Integer.toString(nIndex+100));
    	TableLayout oTableLayoutVars = (TableLayout) getFEChildView(R.id.tableLayoutVars);    	
    	return ((EditText)  oTableLayoutVars.findViewWithTag(objTag));
    }
    public double getNumber(int nIndex) {
    	String sMant = getVarValEditText (nIndex).getText().toString();
    	String sExp  = getVarExpEditText (nIndex).getText().toString();
    	if (sExp.length() <= 0) {
    		sExp = "0";
    	}
    	String sNumber = sMant.concat(getExponentSeparator ()).concat(sExp);
    	return (Double.parseDouble(sNumber));
    }
    public void writeDoubletoEditFields (int nIndex, double dValue) {
    	String[] s = Double.toString(dValue).split(getExponentSeparator());
    	if (s.length  <= 0) {
    		getVarValEditText(nIndex).setText("");
    	} else {
    		getVarValEditText(nIndex).setText(s[0]);
    	}
    	if (s.length  <= 1) {
    		getVarExpEditText(nIndex).setText("");
    	} else {
    		getVarExpEditText(nIndex).setText(s[1]);
    	}
    }
    
    private String getExponentSeparator () {
    	return (String) getResources().getText(R.string.str_ExponentSeparator);
    }

	private void confEditTexts(){
		lw.dlog(TAG, "confEditTexts");
    	EditText oETFormInput, oEditTextVarNames, oEditTextValVals, oEditTextExp;
    	oETFormInput = getETFormInput();
    	oETFormInput.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
    	InputFilter[] filters = new InputFilter[2];
    	sAcceptedSymb = (String) getResources().getText(R.string.str_AccSymbolsInFormula);
    	filters[0] = new InputFilter.LengthFilter(nFormulaLength);
    	filters[1] = new InputFilter(){
    	    @Override
    	    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
    	        if (end > start) {

    	            for (int index = start; index < end; index++) {    	            	
    	            	if (Character.isLetterOrDigit(source.charAt(index)) || source.charAt(index) == ' ') {
    	            		continue;
    	            	}
    	                if (!sAcceptedSymb.contains(String.valueOf(source.charAt(index)))) { 
    	                    return ""; 
    	                }               
    	            }
    	        }
    	        return null;
    	    }

    	};
    	oETFormInput.setFilters(filters);

//    	editFieldFormInput.addTextChangedListener(oTextWatcherETFormula);

    	for (int j = 0;j < nMaxVars;j++) {
    		oEditTextVarNames = getVarNameEditText(j);
    		if (j==0) {
    			getETFormInput().setNextFocusDownId(oEditTextVarNames.getId());
    		}
    		oEditTextVarNames.setFilters(new InputFilter[] {new InputFilter.LengthFilter(nVarNameLength)});
    		// oEditTextVarNames.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
    		// oEditTextVarNames.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
    		// oEditTextVarNames.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS & InputType.TYPE_TEXT_VARIATION_URI);
    		// oEditTextVarNames.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
    		// oEditTextVarNames.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_FLAG_CAP_WORDS );
    		oEditTextVarNames.addTextChangedListener(oTextWatcherETVarname);
    		oEditTextValVals = getVarValEditText(j);
    		// oEditTextVarNames.setNextFocusForwardId(oEditTextValVals.getId());
    		oEditTextVarNames.setNextFocusDownId(oEditTextValVals.getId());
    		oEditTextExp = getVarExpEditText(j);
    		oEditTextExp.setFilters(new InputFilter[] {new InputFilter.LengthFilter(nExponentFieldLenght)});
    		oEditTextExp.addTextChangedListener(oTextWatcherETExp);
    		oEditTextValVals.setNextFocusDownId(oEditTextExp.getId());
    		if (j < nMaxVars - 1) {
    			// oEditTextValVals.setNextFocusForwardId(getVarNameEditText(j+1).getId());
    			oEditTextExp.setNextFocusDownId(getVarNameEditText(j+1).getId());
    		}
    	}
    }
    /**
     * 
     */
    public void computeClicked() {
    	String sInput = getETFormInput().getText().toString();
    	String sOutput = "";
    	String[] aStrVarName;
    	
    	String sTemp;
    	double[] aDVarValue;
    	double fTemp;
    	int cntVars = 0;
    	boolean bError = true;  // controls the color of the output, is essentially boolean
//    	int nCorrectCol = getResources().getColor(R.color.colFEMy_clover_green);
//    	int nRedCol = R.color.colFEMy_holo_blue_light;
//    	int nGreenCol = R.color.colFE_back;
    	
    	aStrVarName = new String [nMaxVars];
    	aDVarValue = new double [nMaxVars];
    	// get the varnames and values from the edit fields
		try{
	    	for (int j = 0;j < nMaxVars; j++){
	    		sTemp = getVarNameEditText (j).getText().toString();
	    		if (sTemp.length()!=0) {
	    			// check for duplicate varname
	    			if (Arrays.asList(aStrVarName).contains(sTemp)) {
	    				throw new FE_DuplicateVarNameException("", sTemp);
	    			}
	    			aStrVarName[cntVars] = sTemp;
//	    			aDVarValue[cntVars]  = Double.parseDouble(getVarValEditText (j).getText().toString());
	    			aDVarValue[cntVars]  = getNumber(j);
	    			cntVars++;
	    		}
	    	}
			// fTemp = oFE_EvalWrapper.evaluate(sInput);
			fTemp = oFE_EvalWrapper.evaluate(sInput, cntVars, aStrVarName, aDVarValue);
			if (Double.isNaN(fTemp)) {
				sOutput = (String) getResources().getText(R.string.strErrorFormArithmetic);
			} else if (Double.isInfinite(fTemp)) {
				sOutput = (String) getResources().getText(R.string.strErrorFormInfinite);
			} else {
				sOutput = Double.toString (fTemp);
				bError = false;
				// ClipData.newPlainText(sClipLabel, sOutput);
				// android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
			    // clipboard.setText(shareViaSMSBody);
				try {
					CompatWrapper.writeClipBoard(sClipLabel, sOutput, getActivity().getSystemService(Context.CLIPBOARD_SERVICE));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (NumberFormatException e) {
			sOutput = (String) getResources().getText(R.string.strErrorFormVarNotValid);
		} catch (FE_UnparsableExprException e) {
			sOutput = (String) getResources().getText(R.string.strErrorFormNotValid);
		} catch (FE_UnknownFuncException  e) { // same as 1st exception , might be changed
			sOutput = (String) getResources().getText(R.string.strErrorFormNotValid);
		} catch (FE_DuplicateVarNameException  e) { // same as 1st exception , might be changed
			sOutput = ((String) getResources().getText(R.string.strErrorFormDuplicateVarName)).concat(e.getVarname());
		} catch (java.lang.ArithmeticException  e) { // same as 1st exception , might be changed
			sOutput = (String) getResources().getText(R.string.strErrorFormArithmetic);
		} catch (Exception  e) { 
			sOutput = (String) getResources().getText(R.string.strErrorFormGeneric);
			e.printStackTrace();
		} finally {
			if (bError == false){
				sOutput = sOutput.replace(getExponentSeparator(), " ".concat(getExponentSeparator()).concat(" "));
			}
			setResult(sOutput, bError);
		}
    }
    private void refreshResult(){
    	TextView textViewOutputFormula = getTextViewResult();
    	String sText = (String) textViewOutputFormula.getText();
    	setResult(sText, bResultError);
    }
    private void setResult(String sOutput, boolean bError){
    	TextView textViewOutputFormula = getTextViewResult();
    	final int nErrorCol = getResources().getColor(R.color.colFEMy_candy_apple_red);
    	final int nCorrectCol = getResources().getColor(R.color.colFEMy_CeruleanBlue);
    	bResultError = bError;
    	final int nError = (bError) ? 1 : 0;

		textViewOutputFormula.setTextColor(nError * nErrorCol + (1 - nError) * nCorrectCol);
		textViewOutputFormula.setText(sOutput);    	
    }    

	protected void updateStatusBar(){
		;

		try { // in case we are too  early
			getListener().updateStatusBar(this, !canClose(), getListener().getFEListFragment().IsFull());
			lw.dlog(TAG, "updateStatusBar(): ".concat(Integer.toString(getETFormInput().length())));
		} catch (NullPointerException e) {
			lw.dlog(TAG, "updateStatusBar(): NullPointerException");
		};
	}
	public boolean canClose(){
		return (getETFormInput().length() <= 0);
	}

	/**
	 * Delete the content of oETFormInput and all EditText for varname, value etc   
	 */
	public void clearControls() {
		getETFormInput().setText("");
		setResult("",false);
		for (int j=0;j<getMaxVars();j++){
			getVarNameEditText(j).setText("");
			getVarValEditText(j).setText("");
			getVarExpEditText(j).setText("");
		}
	}
}
