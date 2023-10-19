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

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

abstract class FEAbstractTabFragment extends Fragment{
	final String TAG = "FEAbstractTabFragment";
	LogWrapper lw = new LogWrapper();;
	OnUpdateStatusbarListener oListener;
	View oRootView;
	protected OnUpdateStatusbarListener getListener() {
		return oListener;
	}
	interface OnUpdateStatusbarListener {
		
		/**
		 * @return the fragment can get its type (important to discern List/Sample fragments)
		 */
		public enum ListFragmentType {enFormula, enList, enSample};
		public ListFragmentType getFragmentType(FEAbstractTabFragment oFragment);
	    FragmentFormulaTab getFEFormFragment();
	    FragmentListTab getFEListFragment();
		/**
		 * @param oFragment
		 * @param nParam: can have different semantics depending on the subclass
		 * 			see implementation there 
		 */
	    public void updateStatusBar(FEAbstractTabFragment oFragment, boolean bParam);
		public void updateStatusBar(FEAbstractTabFragment oFragment, boolean bParam, boolean bParam2);
	    }
	abstract protected void updateStatusBar();
	abstract public boolean canClose();
	/**
	 * Should be called in onTabSelected
	 */
	abstract public void refreshTab();
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // retain this fragment
        setRetainInstance(true);
    }
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState, int nLayoutID) {
//		super.onCreateView(inflater, container,savedInstanceState);
		lw.dlog(TAG, "onCreateView: ");
		oRootView = inflater.inflate(nLayoutID, container, false);
		if (oRootView == null) {
			lw.dlog(TAG, "onCreateView: (oRootView == null)");
		}		
		return (oRootView);
	}
	public void onAttach(Activity activity) {
	      super.onAttach(activity);
	      if (activity instanceof OnUpdateStatusbarListener) {
	    	  oListener = (OnUpdateStatusbarListener) activity;
	      } else {
	        throw new ClassCastException(activity.toString()
	            + " must implement FEAbstractTabFragment.OnUpdateStatusbarListene");
	      }
	    }
	@Override
	public void onDetach() {
		super.onDetach();
		oListener = null;
	}
	public View getFEChildView(int nLayoutID){
		if (oRootView == null) {
			lw.dlog(TAG, "getFEChildView: (oRootView == null)");
		}
		return oRootView.findViewById(nLayoutID); 
	}
}
