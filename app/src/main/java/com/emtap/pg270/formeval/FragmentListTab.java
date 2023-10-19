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

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.emtap.pg270.formeval.FEAbstractTabFragment.OnUpdateStatusbarListener.ListFragmentType;
import com.emtap.pg576.formeval.R;

final public class FragmentListTab extends FEAbstractTabFragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
	final String TAG = "FragmentListTab";	
	private LogWrapper lw;
	ListFragmentType m_en_Type;
	private boolean bReadOnly;
	String sContainerFileName;
	private int nRawResourceFile;
	FE_FormulaContainer oFE_FormulaContainer;
	final int nMaxFormulae = 20; // better: check it on Start!
	boolean bAdsEnabled = GlobalSettings.isAdsEnabled();
	int nAdViewLayout, nRelLayout;
	
	ListView oListViewFormulae;

	// keys for instance save
	final String sKLVFormTag = "oListViewFormulae_TAG";
	
    public FragmentListTab() {
    }

    
    public boolean isReadOnly() {
		return bReadOnly;
	}

	public int getRawResourceFileID() {
		return nRawResourceFile;
	}


	public String getContainerFileName() {
		return sContainerFileName;
	}

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		int nLayout, nListLayout;
		m_en_Type = getListener().getFragmentType(this);
		switch (m_en_Type){
		case enFormula:
			throw new IllegalStateException("FragmentListTab onCreateView");
		case enList:
			nLayout = R.layout.fe_listfragment;
			nListLayout = R.id.listViewFormulae;
			nRelLayout = R.id.RelativeLayoutListFragment;
//			bAdsEnabled = bAdsEnabled;
			sContainerFileName = (String) getActivity().getText(R.string.strFEJSONFilename);
			nRawResourceFile = -1; // unused in this case
			bReadOnly = false;
			break;
		case enSample:
			nLayout = R.layout.fe_samplefragment;
			nListLayout = R.id.listViewFormulaeSample;
			nRelLayout = R.id.RelativeLayoutSampleFragment;
//			bAdsEnabled = false;
			sContainerFileName = "";
			nRawResourceFile = R.raw.fesample; // unused in this case
			bReadOnly = true;
			break;
		default:
			throw new IllegalStateException("FragmentListTab onCreateView");
		}
    	View oRootView = onCreateView(inflater, container,savedInstanceState,nLayout);
    	lw = new LogWrapper();
        lw.dlog(TAG, "ads enabled: ".concat(String.valueOf(bAdsEnabled)));
        
        oListViewFormulae = (ListView) getFEChildView(nListLayout);

        oFE_FormulaContainer = new FE_FormulaContainer(this);        
        
        // listViewFormulae.setAdapter(new ArrayAdapter<String> (FEMainActivity.this, android.R.layout.simple_list_item_1, aStrFormulae));
        
        oListViewFormulae.setAdapter(new ArrayAdapter<String> (getActivity(), android.R.layout.simple_list_item_checked, oFE_FormulaContainer.getArrayList()));
        if (savedInstanceState == null) {
        	lw.dlog(TAG, "oncreateview: savedInstanceState == null");
        	oListViewFormulae.setTag(-1);        	
        }
      	        	
        
        oListViewFormulae.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                int position, long id) {
            	parent.setTag(position);
            	updateStatusBar();         	
            }
          });
    	int nListViewPos = getFormulaeListViewPosition();
    	if (nListViewPos < 0 ){ // no item checked -> scoll to bottom    		
    		if (oListViewFormulae.getCount() > 0) {
    			oListViewFormulae.setSelection(oListViewFormulae.getCount() - 1);
    		}
    	}
    	updateStatusBar();
        return oRootView;
    }

    public void onResume() {
    	super.onResume();
    	lw.dlog(TAG, "onResume");

//        if (bAdsEnabled) {    	
//        	startAd();
//        }
    	
//    	updateStatusBar();
    	
    	
    	lw.dlog(TAG, "onResume: end");
    }    
    public void onPause() {
//    	if (bAdsEnabled){
//    		destroyAd();
//    	}
    			
    	super.onPause();
    	lw.dlog(TAG, "onPause");
//    	onSaveInstanceState(getArguments());
    }
    @Override
    public void onSaveInstanceState (Bundle outState) {
    	super.onSaveInstanceState (outState);
    	lw.dlog(TAG, "onSaveInstanceState");    	
//    	outState.putInt(sKLVFormTag, getFormulaeListViewPosition());    	
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
        lw.dlog(TAG, "onDestroy");

    	super.onDestroy();
    	
    }
    public void onDetach() {
    	super.onDetach();
    	lw.dlog(TAG, "onDetach");
    }
    public void refreshTab(){
    	try {
    		InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
    		imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);

    	} catch(NullPointerException e) {} // imm or getCurrentFocus() might be zero
    	try {
    		updateStatusBar();
        	if (getFormulaeListViewPosition()<0){ 
        		getFormListView().setSelection(0);    	
        	}
    	} catch (NullPointerException e) { // to fix crash 
    		LogWrapper lw = new LogWrapper();
    		lw.dlog(TAG,  "refreshTab() : NullPointerException funny crash");
//			Toast oToast = Toast.makeText(getActivity(), TAG.concat("refreshTab() : NullPointerException funny crash"), Toast.LENGTH_LONG);
//			oToast.show();
    		e.printStackTrace();
    	}

    }
    /**
     * this is also a severe design weakness !
     * @return
     */
    FragmentFormulaTab getFEFormFragment(){
    	return (oListener.getFEFormFragment());
    }
	protected int getMaxFormulae() {
		return nMaxFormulae;
	}    
	public ListView getFormListView(){		
		return (oListViewFormulae);
	}
	public int getFormulaeListViewPosition(){
		int nResult = -1;
		try { // dont know if this is so goods
			ListView listViewFormulae = getFormListView();
//			nResult = Integer.parseInt(listViewFormulae.getTag().toString());
//			lw.dlog(TAG, "getFormulaeListViewPosition(): ".concat (listViewFormulae.getTag().toString()));
			
			nResult = listViewFormulae.getCheckedItemPosition();
			if (nResult == AdapterView.INVALID_POSITION) {
				nResult = -1;
			}			
			lw.dlog(TAG, "getFormulaeListViewPosition(): getCheckedItemPosition(): ".concat (Integer.toString(nResult)));
		} catch (Exception e) {
			// TODO: handle exception
		}
		return (nResult);
	}
	public boolean IsFull(){
		return(oFE_FormulaContainer.count() >= getMaxFormulae());
	}
	public void add(){
		oFE_FormulaContainer.addFromEditFields();
	}
	public boolean select(){
    	int nListViewPos = getFormulaeListViewPosition();
    	if (nListViewPos >= 0){
    		oFE_FormulaContainer.select(getFormulaeListViewPosition());
    		return true;
    	}
    	return false;
	}
	public boolean delete(){
    	int nListViewPos = getFormulaeListViewPosition();
    	if (nListViewPos >= 0){
    		oFE_FormulaContainer.askDelete(getFormulaeListViewPosition());
    		return true;
    	}
    	return false;
	}
	public boolean canClose(){
		return (getFormulaeListViewPosition() < 0);
	}
	protected void updateStatusBar(){
		try { // in case we are too  early
			getListener().updateStatusBar(this, !canClose(), isReadOnly());
			lw.dlog(TAG, "updateStatusBar(): ".concat(Integer.toString(getFormulaeListViewPosition())));
		} catch (NullPointerException e) {
			lw.dlog(TAG, "updateStatusBar(): NullPointerException");
		};
	}
	/**
	 * Workaround for Layout problem with Adview
	 *  
	 * TODO should be fixed in XML file
	 */
	private void configLayoutForAd(int nRelLayout){
//		int nAdHeight = adView.getAdSize().getHeightInPixels(getActivity());
//		ListView oLv = getFormListView();
//		oLv.setPadding(oLv.getPaddingLeft(), oLv.getPaddingTop(), oLv.getPaddingRight(), nAdHeight);
//		RelativeLayout oRelLayout = (RelativeLayout) getFEChildView(nRelLayout);
//		int nLayoutHeight = oRelLayout.getHeight();		
//		android.view.ViewGroup.LayoutParams oLP =  getFormListView().getLayoutParams();
//		oLP.height -= nAdHeight;
//		lw.dlog(TAG, "configLayoutForAd: nAdHeight = ".concat(Integer.toString(nAdHeight)));
//		lw.dlog(TAG, "configLayoutForAd: nLayoutHeight = ".concat(Integer.toString(nLayoutHeight)));
//		lw.dlog(TAG, "configLayoutForAd: oLP.height = ".concat(Integer.toString(oLP.height)));
//		getFormListView().setLayoutParams(oLP);
	}
}


