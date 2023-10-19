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

import java.util.Locale;
import java.util.regex.Pattern;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TableLayout;

import com.emtap.pg576.formeval.R;

// TODO has to be fixed android:screenOrientation="portrait"

/**
 * @author berzem
 *
 */
final public class FEMainActivity extends android.support.v7.app.ActionBarActivity implements ActionBar.TabListener, FEAbstractTabFragment.OnUpdateStatusbarListener {
	ViewPager mViewPager;
	SectionsPagerAdapter mSectionsPagerAdapter;
	
	// TODO   1. The fragments for the tabs are stored in private variables in the activity.
	// This might lead to unforeseen problems, but the correct way didn't work so far.
//	FragmentFormulaTab oTestFormFragment;
//	FragmentListTab oTestListFragment, oTestSampleFragment;
	
	
	final int nTabCnt = 3;
	final int nFormTabPos = 0;
	final int nListTabPos = 1;
	final int nSampleTabPos = 2;


	//	private TabHost tabHost; 
	private MenuItem oMenuItemAdd, oMenuItemDelete, oMenuItemCompute, oMenuItemSelect, oMenuItemDeleteForm; 
	private MenuItem oMenuItemFortranStyle, oMenuItemAngle, oMenuItemAngleDegree, oMenuItemAngleRadiant;
	private EditText editFieldFormInput;
	private LogWrapper lw;
	// private View oCurrentRowView; // to remember the current row in the formula list to manually unhighlight it 


	final int nExponentSize = 2; // vars < 10^100 
	final int nExponentFieldLenght = nExponentSize + 1; // including sign ! 
	final String  sTabTagFormula = "tagFormula";
	final String  sTabTagList = "tagList";
	final String TAG = "FEMainActivity";
	final String sPatternETVarnameForb = "[[^a-z]&&[^A-Z]&&[^0-9]]";
	final Pattern oPatternETVarnameForb = Pattern.compile(sPatternETVarnameForb);
	final Pattern oPatternETVarnameFirst = Pattern.compile("[[a-z][A-Z]]");
	final String sPatternETFormulaForb = "[[^a-z]&&[^A-Z]&&[^0-9]]";
	final Pattern oPatternETFormulaForb = Pattern.compile(sPatternETFormulaForb);
	
	String sClipLabel; 
	TextWatcher oTextWatcherETFormula, oTextWatcherETVarname, oTextWatcherETExp;
	static String sCurrentTabTag = "";	



    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	GlobalSettings.configure(this);
    	
        
        lw = new LogWrapper();
        lw.dlog(TAG, "oncreate  before super.onCreate");
        super.onCreate(savedInstanceState);
        lw.dlog(TAG, "oncreate  after super.onCreate");
        
        setContentView(R.layout.fe_activity_main);
        // Set up the action bar. The navigation mode is set to NAVIGATION_MODE_TABS, which will
        // cause the ActionBar to render a set of tabs. Note that these tabs are *not* rendered
        // by the ViewPager; additional logic is lower in this file to synchronize the ViewPager
        // state with the tab state. (See mViewPager.setOnPageChangeListener() and onTabSelected().)
        // BEGIN_INCLUDE (set_navigation_mode)       
        
        
        final ActionBar actionBar = getSupportActionBar();        
//        final ActionBar actionBar = getActionBar();
//        actionBar.setTitle(((String) getResources().getText(R.string.app_name_short)));
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        
        
        
        // END_INCLUDE (set_navigation_mode)

        // BEGIN_INCLUDE (setup_view_pager)
        // Create the adapter that will return a fragment for each of the three primary sections
        // of the app.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(2);
        // END_INCLUDE (setup_view_pager)

        // When swiping between different sections, select the corresponding tab. We can also use
        // ActionBar.Tab#select() to do this if we have a reference to the Tab.
        // BEGIN_INCLUDE (page_change_listener)
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
            	Log.d(TAG, "setOnPageChangeListener,  onPageSelected: ".concat(Integer.toString(position)));
                actionBar.setSelectedNavigationItem(position);
            }
        });
        // END_INCLUDE (page_change_listener)       

        // BEGIN_INCLUDE (add_tabs)
        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by the adapter. Also
            // specify this Activity object, which implements the TabListener interface, as the
            // callback (listener) for when this tab is selected.
        	lw.dlog(TAG, "oncreate before actionBar.addTab: ".concat((Integer.toString(i))));
            actionBar.addTab(actionBar.newTab().setText(mSectionsPagerAdapter.getPageTitle(i)).setTabListener(this));
        }
        // END_INCLUDE (add_tabs)        
        lw.dlog(TAG, "AndroidDevice.Id ".concat(Secure.getString(getContentResolver(),Secure.ANDROID_ID)));
        try {
//			int v = getPackageManager().getPackageInfo("com.google.android.gms", 0 ).versionCode;
			String s = getPackageManager().getPackageInfo("com.google.android.gms", 0 ).versionName;			
			lw.dlog(TAG, "com.google.android.gms: versionName: ".concat(s));
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        if (GlobalSettings.isFirstStart()) {
        	showAbout();        	
        }
        GlobalSettings.setFirstStart(this, false);
    }
	private void showAbout() {
		FEAboutDialogFragment oDlg = new FEAboutDialogFragment();
		oDlg.show(getSupportFragmentManager(), ((String) getResources().getText(R.string.str_AboutDlgTag)));
	}

    protected void onResume() {
    	super.onResume();
    	lw.dlog(TAG, "onResume");
    	
    	FEAbstractTabFragment oFragment = getCurFEFragment();
    	if (oFragment != null) {
    		oFragment.updateStatusBar();
    	}
    	lw.dlog(TAG, "onResume: end");
    }
    protected void onStop() {
    	super.onStop();
    	lw.dlog(TAG, "onStop");
    }
    protected void onDestroy() {
    	super.onDestroy();
    	lw.dlog(TAG, "ondestroy");    	
    }
    protected void onStart() {
    	super.onStart();
    	lw.dlog(TAG, "onStart");
    }
    protected void onPause() {
    	super.onPause();
    	lw.dlog(TAG, "onPause");
    }
    protected void onSaveInstanceState (Bundle outState) {
    	super.onSaveInstanceState (outState);
    	lw.dlog(TAG, "onSaveInstanceState()");
    }

    
    protected void onRestoreInstanceState (Bundle savedInstanceState) {
    	super.onRestoreInstanceState(savedInstanceState);
    	lw.dlog(TAG, "onRestoreInstanceState()");
    }
    protected   void onRestart () {
    	super.onRestart();
    	lw.dlog(TAG, "onRestart()");
    }
    @Override
    public void onBackPressed() { // TODO implement
    	if (!getFEFormFragment().canClose()) {
    		gotoFragmentFormulaTab();
        	AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.str_AcAlertCancClose)
                   .setPositiveButton(R.string.str_AcAlertYes, new DialogInterface.OnClickListener() {
                       public void onClick(DialogInterface dialog, int id) { // Yes
                    	   getFEListFragment().add();
                    	   finish();
                       }
                   })
                   .setNeutralButton(R.string.str_AcAlertNo, new DialogInterface.OnClickListener() {
                       public void onClick(DialogInterface dialog, int id) {
                           // User cancelled the dialog
                    	   finish();
                       }
                   })
                   .setNegativeButton(R.string.str_AcAlertCancel, new DialogInterface.OnClickListener() {
                           public void onClick(DialogInterface dialog, int id) {
                               // User cancelled the dialog                        	   
                           }});
            // Create the AlertDialog object and return it
            builder.create();
            builder.show();		
    		
    	} else {
    		finish();
    	}
    }
    
    private FEAbstractTabFragment getCurFEFragment(){
    	return (getFEFragment(mViewPager.getCurrentItem()));
    }
    
    /**
     * TODO We shouldn't store the fragments into variables, but make
     * mSectionsPagerAdapter.getItem(nPos)) or findFragmentById work
     * 2. getFEFormFragment should call getFEFragment !! 
     * @return
     */
    private FEAbstractTabFragment getFEFragment(int pos){
    	String name = makeFragmentName(mViewPager.getId(), pos);
        return ((FEAbstractTabFragment)getSupportFragmentManager().findFragmentByTag(name));
    	
    	
//    	return ((FEAbstractTabFragment)mSectionsPagerAdapter.getItem(pos));
//    	return ((FEAbstractTabFragment)mSectionsPagerAdapter.fetchItem(pos));
//    	switch(pos){
//    	case nFormTabPos:
//    		return oTestFormFragment;
//    	case nListTabPos:
//    		return oTestListFragment;
//    	case nSampleTabPos:
//    		return oTestSampleFragment;
//    	}
//    	return null;
    }
    private static String makeFragmentName(int viewId, int position) {
        return "android:switcher:" + viewId + ":" + position;
    }
    public FragmentFormulaTab getFEFormFragment(){    	
    	return (FragmentFormulaTab)getFEFragment(nFormTabPos);
    }
    public FragmentListTab getFEListFragment(){
    	return (FragmentListTab)getFEFragment(nListTabPos);
    }
    private FragmentListTab getFESampleFragment(){
    	return (FragmentListTab)getFEFragment(nSampleTabPos);
    }    
    private void gotoFragmentFormulaTab(){
    	gotoTab(nFormTabPos);
    }
    private void gotoTab(int nPos){
//    	mViewPager.setCurrentItem(nPos);
//    	mViewPager.getAdapter().notifyDataSetChanged ();
//    	mViewPager.setCurrentItem(nPos, true);
    	getSupportActionBar().setSelectedNavigationItem(nPos);
    	
    }
    
    public ListFragmentType getFragmentType(final FEAbstractTabFragment oFragment){
    	if (oFragment == getFEFormFragment()){
    		return (ListFragmentType.enFormula);
    	}
    	if (oFragment == getFEListFragment()){
    		return (ListFragmentType.enList);
    	}
    	if (oFragment == getFESampleFragment()){
    		return (ListFragmentType.enSample);
    	}    	
    	throw new IllegalStateException();    	
    }
//	switch (mViewPager.getCurrentItem()){
//    	case nFormTabPos:
//    		return (ListFragmentType.enFormula);
//    	case nListTabPos:
//    		return (ListFragmentType.enList);
//    	case nSampleTabPos:
//    		return (ListFragmentType.enSample);  		
//    	}
//    	throw new IllegalStateException();
//    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	// Inflate the menu; this adds items to the action bar if it is present.    	
    	getMenuInflater().inflate(R.menu.femain, menu);
        
    	// TODO this is troublesome , it was just a workaround, since I couldnt find another method
    	// to get the "Menu" object, but it caused trouble, when onRestoreInstanceState calls updateStatusbar
    	//
    	lw.dlog(TAG, "onCreateOptionsMenu ");
        if (oMenuItemAdd == null) {
        	lw.dlog(TAG, "onCreateOptionsMenu oMenuItemAdd");
        	oMenuItemAdd = menu.findItem(R.id.action_add);
        	oMenuItemDelete = menu.findItem(R.id.action_delete);
        	oMenuItemCompute = menu.findItem(R.id.action_compute);
        	oMenuItemSelect = menu.findItem(R.id.action_select);
        	oMenuItemDeleteForm = menu.findItem(R.id.action_deleteForm);
        	oMenuItemFortranStyle = menu.findItem(R.id.menu_Fortran_exp);
        	oMenuItemAngle = menu.findItem(R.id.menu_degree_rad_selection);
        	oMenuItemAngleDegree = menu.findItem(R.id.menu_angle_degree);
        	oMenuItemAngleRadiant = menu.findItem(R.id.menu_angle_radiant);
        	// get the values to set the menu option
        	oMenuItemFortranStyle.setChecked(GlobalSettings.getFortranStyle());
        	setAngleMenuDegree(GlobalSettings.getAngleDegree());
        }
//    	int nListViewPos = getFormulaeListViewPosition();
//    	if (nListViewPos < 0 ){ // no item checked -> scroll to bottom
//    		ListView listViewFormulae = (ListView) findViewById(R.id.listViewFormulae);
//    		if (listViewFormulae.getCount() > 0) {
//    			listViewFormulae.setSelection(listViewFormulae.getCount() - 1);
//    		}
//    	}
		FEAbstractTabFragment oFragment = getFEFragment(mViewPager.getCurrentItem());
		if (oFragment != null) {
			oFragment.updateStatusBar();
		}
        return true;
    }
//    @Override
//    public void onTabChanged(String tabId) {
//        /* Your code to handle tab changes */
//    	sCurrentTabTag = tabHost.getCurrentTabTag();
//    	if (sCurrentTabTag.equals(sTabTagList)) {
//    		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//    		if(imm != null) {
//    			imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
//    		}
//    	}
//    	updateStatusBar();    
//    }

    
    /* (non-Javadoc)
     * @see com.pg576.formeval.FEAbstractTabFragment.OnUpdateStatusbarListener#updateStatusBar(com.pg576.formeval.FEAbstractTabFragment, int)
     * nParam has different semantics according to fragment type
     */
    
    public void updateStatusBar(FEAbstractTabFragment oFragment, boolean bParam) {
    	updateStatusBar(oFragment, bParam, false);
    }
    
    public void updateStatusBar(FEAbstractTabFragment oFragment, boolean bParam, boolean bParam2) {   		
    	// TODO (s.onCreateOptionsMenu) we have to check this , since in onRestoreInstanceState this might not be created
    	boolean bHasInput, bItemSelected, bReadOnly, bIsFull;
    	if (oMenuItemAdd == null) {
    		lw.dlog(TAG, "updateStatusBar oMenuItemAdd");
    		return;
    	}
    	if (oFragment instanceof FragmentFormulaTab) {
    		bHasInput = bParam;
    		bIsFull = bParam2;
   	    	oMenuItemAdd.setVisible(bHasInput && !bIsFull);
   	    	oMenuItemDeleteForm.setVisible(bHasInput);
   	    	oMenuItemDelete.setVisible(false);	
   	    	oMenuItemCompute.setVisible(bHasInput);
   	    	oMenuItemSelect.setVisible(false);
   	    	return;
		}
    	if (oFragment instanceof FragmentListTab) {
    		bItemSelected = bParam;
    		bReadOnly =  bParam2;
   	    	oMenuItemAdd.setVisible(false);
   	    	oMenuItemDeleteForm.setVisible(false);
   	    	oMenuItemDelete.setVisible(bItemSelected && !bReadOnly);
   	    	oMenuItemCompute.setVisible(false);
   	    	oMenuItemSelect.setVisible(bItemSelected);
   	    	return;
		}
    	throw new IllegalArgumentException();
    }
    
    public boolean onOptionsItemSelected(MenuItem item) {
    	boolean bValToSet;
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_compute:
            	getFEFormFragment().computeClicked();
                return true;
            case R.id.action_add:
//            	oFE_FormulaContainer.addFromEditFields();
            	getFEListFragment().add();
                return true;
            case R.id.action_deleteForm:
            	return askClearControls();
            	
            case R.id.action_delete:
            	return getFEListFragment().delete();
//            	nListViewPos = getFormulaeListViewPosition();
//            	if (nListViewPos >= 0){
//            		oFE_FormulaContainer.askDelete(getFormulaeListViewPosition());
//            		return true;
//            	}
            case R.id.action_select:
            	return askSelect(); 
//            	nListViewPos = getFormulaeListViewPosition();
//            	if (nListViewPos >= 0){
//            		oFE_FormulaContainer.select(getFormulaeListViewPosition());
//            		return true;
//            	}
            case R.id.menu_Fortran_exp:
            	bValToSet = !item.isChecked();
            	GlobalSettings.setPrefValFortran(this, bValToSet);
            	item.setChecked(bValToSet);  
            	return true;
            case R.id.menu_angle_degree:
            	GlobalSettings.setPrefValAngleConv(this, true);
            	setAngleMenuDegree(true);
            	return true;
            case R.id.menu_angle_radiant:
            	GlobalSettings.setPrefValAngleConv(this, false);
            	setAngleMenuDegree(false);
            	return true;
            case R.id.menu_About:
            	showAbout();
            	return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    private void setAngleMenuDegree (boolean bVal) {
    	oMenuItemAngleDegree.setChecked(bVal);
    	oMenuItemAngleRadiant.setChecked(!bVal);
    	String sText = (String) getResources().getText(R.string.str_pref_angleunit_sel_title);
    	int nbVal = (bVal) ? 1 : 0;
    	int nStrId = nbVal * R.string.str_pref_angleunit_degree + (1 - nbVal) * R.string.str_pref_angleunit_radiant;
    	sText = sText.concat((String) getResources().getText(nStrId));
    	oMenuItemAngle.setTitle(sText);
    }
	private boolean askClearControls() {
    	// show alert
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.str_AcAlertAskClCtls)
        .setPositiveButton(R.string.str_AcAlertYes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) { // Yes
         	   getFEListFragment().add();
         	   getFEFormFragment().clearControls();
            }
        })
        .setNeutralButton(R.string.str_AcAlertNo, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            	getFEFormFragment().clearControls();
            }
        })
        .setNegativeButton(R.string.str_AcAlertCancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User cancelled the dialog                        	   
                }});

        // Create the AlertDialog object and return it
        builder.create();
        builder.show();		
		
		return true;
	}
	private FragmentListTab oFrListTab; // only used in askselect
	private boolean askSelect() {
		oFrListTab = (FragmentListTab)getCurFEFragment();
		if (getFEFormFragment().canClose()) {
			oFrListTab.select();
			gotoFragmentFormulaTab();
		} else {
	    	// show alert
	    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
	        builder.setMessage(R.string.str_AcAlertAskSelect)
	        .setPositiveButton(R.string.str_AcAlertYes, new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int id) { // Yes
	            	oFrListTab.select();
	         	   	gotoFragmentFormulaTab();
	            }
	        })
	        .setNegativeButton(R.string.str_AcAlertNo, new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int id) {
	                    // User cancelled the dialog                        	   
	                }});
	
	        // Create the AlertDialog object and return it
	        builder.create();
	        builder.show();
		}		
		return true;
	}

	
    public EditText getVarNameEditText (int nIndex) {
    	Object objTag = ((String) getResources().getText(R.string.str_TagVarName)).concat (Integer.toString(nIndex+100));
    	TableLayout oTableLayoutVars = (TableLayout) findViewById(R.id.tableLayoutVars);    	
    	return ((EditText)  oTableLayoutVars.findViewWithTag(objTag));
    }
    public EditText getVarValEditText (int nIndex) {
    	Object objTag = ((String) getResources().getText(R.string.str_TagVarVAL)).concat(Integer.toString(nIndex+100));
    	TableLayout oTableLayoutVars = (TableLayout) findViewById(R.id.tableLayoutVars);    	
    	return ((EditText)  oTableLayoutVars.findViewWithTag(objTag));
    }
    public EditText getVarExpEditText (int nIndex) {
    	Object objTag = ((String) getResources().getText(R.string.str_TagExpo)).concat(Integer.toString(nIndex+100));
    	TableLayout oTableLayoutVars = (TableLayout) findViewById(R.id.tableLayoutVars);    	
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
//    	String sNumber = sMant.concat(new DecimalFormatSymbols().getExponentSeparator()).concat(sExp);
    	return (String) getResources().getText(R.string.str_ExponentSeparator);
    }
    
    
    public EditText getEditFieldFormInput() {
		return editFieldFormInput;
	}
	@Override
	public void onTabReselected(Tab oTab,
			android.support.v4.app.FragmentTransaction arg1) {
		// TODO Auto-generated method stub
		int nPos = oTab.getPosition();
		lw.dlog(TAG,  "onTabSelected: ".concat(Integer.toString(nPos)));
	}

	@Override
	public void onTabSelected(Tab oTab,
			android.support.v4.app.FragmentTransaction oFt) {
		// TODO Auto-generated method stub
		int nPos = oTab.getPosition();
		mViewPager.setCurrentItem(nPos);
		lw.dlog(TAG,  "onTabSelected: ".concat(Integer.toString(nPos)));
		
		
		FEAbstractTabFragment oFragment = getFEFragment(nPos);
		if (oFragment != null) {
			oFragment.refreshTab();
		}
		
//		if (nPos == nFormTabPos && oTestFormFragment != null){
//			getFEFormFragment().updateStatusBar();
//		}
//		if (nPos == nListTabPos && oTestListFragment != null){
//			getFEListFragment().updateStatusBar();
//		}

		
//		if (nPos == nFormTabPos && oTestFormFragment != null){
//			((FragmentFormulaTab)getSupportFragmentManager().findFragmentById(R.layout.fe_formulafragment)).updateStatusBar();
//		}
//		if (nPos == nListTabPos && oTestListFragment != null){
//			((FragmentListTab)getSupportFragmentManager().findFragmentById(R.layout.fe_listfragment)).updateStatusBar();
//		}
		
		
		
//		if (nPos == nFormTabPos && oTestFormFragment != null){
//			oTestFormFragment.updateStatusBar();
//		}
//		if (nPos == nListTabPos && oTestListFragment != null){
//			oTestListFragment.updateStatusBar();
//		}
		
		// this is quite messy !!!!
//		try {
////			((FEAbstractTabFragment) mSectionsPagerAdapter.fetchFragment(nPos)).updateStatusBar();
//			((FEAbstractTabFragment) mSectionsPagerAdapter.getItem(nPos)).updateStatusBar();
//		} catch (Exception e) {
//			lw.dlog(TAG,  "onTabSelected: Exception: ".concat(e.toString()));
//			e.printStackTrace();
//		};
//		((FEAbstractTabFragment) mSectionsPagerAdapter.getItem(nPos)).updateStatusBar();
//		Fragment oSPA = mSectionsPagerAdapter.getItem(nPos);
//		
//		oFt.attach(oSPA);
		
		// oFt.add(oTab.getPosition(), mSectionsPagerAdapter.getItem(oTab.getPosition()));
	}

	@Override
	public void onTabUnselected(Tab oTab,
			android.support.v4.app.FragmentTransaction oFt) {
		// TODO Auto-generated method stub
//		Fragment oFragment = mSectionsPagerAdapter.fetchFragment(oTab.getPosition());
//		Fragment oFragment = mSectionsPagerAdapter.getItem(oTab.getPosition());
		int nPos = oTab.getPosition();
		lw.dlog(TAG,  "onTabUnselected: ".concat(Integer.toString(nPos)));
//		oFragment.getView().setVisibility(false);
//		oFt.detach(oFragment);
//		oFt.detach(mSectionsPagerAdapter.getItem(oTab.getPosition()));
		
	}
	
	public class SectionsPagerAdapter extends FragmentPagerAdapter {
	    // END_INCLUDE (fragment_pager_adapter)
	    final String TAG = "MainActivity";
        final String[] asTags = {(String) getResources().getText(R.string.strFEFragmentTagFormula),
        		(String) getResources().getText(R.string.strFEFragmentTagList)}; 
	    
	    public SectionsPagerAdapter(FragmentManager fm) {
	        super(fm);
	    }

	    
	    /**
	     * get the fragment wo instantiating ! 
	     * @param position
	     * @return
	     */
	    public Fragment fetchFragment(int position) {	    
	    	lw.dlog(TAG, "SectionsPagerAdapter,  fetchFragment: ".concat(Integer.toString(position)));
	    	String name = asTags[position];
	    	Fragment oFragment = getSupportFragmentManager().findFragmentByTag(name);
	    	return (oFragment);
	    }
	    // BEGIN_INCLUDE (fragment_pager_adapter_getitem)
	    /**
	     * Get fragment corresponding to a specific position. This will be used to populate the
	     * contents of the {@link ViewPager}.
	     *
	     * @param position Position to fetch fragment for.
	     * @return Fragment for specified position.
	     */
	    @Override
	    public Fragment getItem(int position) {
	        // getItem is called to instantiate the fragment for the given page.
	        // Return a DummySectionFragment (defined as a static inner class
	        // below) with the page number as its lone argument.
	    	Fragment oFragment = null;
	    	lw.dlog(TAG, "SectionsPagerAdapter,  getItem: ".concat(Integer.toString(position)));
	    		switch (position) {
	    		case nFormTabPos:
	    			oFragment = new FragmentFormulaTab();
//	    			oTestFormFragment = (FragmentFormulaTab)oFragment; 
	    			break;
	    		case nListTabPos:
	    			oFragment = new FragmentListTab();
//	    			oTestListFragment = (FragmentListTab)oFragment;
//	    			oTestListFragment.configure(sContainerFileName, bReadOnly)
	    			break;
	    		case nSampleTabPos:
	    			oFragment = new FragmentListTab();
//	    			oTestSampleFragment = (FragmentListTab)oFragment;
	    			break;	    			
	    		}	        
//	        Bundle args = new Bundle();
//	    	Log.d(TAG, "SectionsPagerAdapter,  getItem: ".concat(Integer.toString(position)));
//	        args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, position + 1);
//	        oFragment.setArguments(args);
	        return oFragment;
	    }
	    // END_INCLUDE (fragment_pager_adapter_getitem)

	    // BEGIN_INCLUDE (fragment_pager_adapter_getcount)
	    /**
	     * Get number of pages the {@link ViewPager} should render.
	     *
	     * @return Number of fragments to be rendered as pages.
	     */
	    @Override
	    public int getCount() {
	        // Show 3 total pages.
	        return nTabCnt;
	    }
	    // END_INCLUDE (fragment_pager_adapter_getcount)

	    // BEGIN_INCLUDE (fragment_pager_adapter_getpagetitle)
	    /**
	     * Get title for each of the pages. This will be displayed on each of the tabs.
	     *
	     * @param position Page to fetch title for.
	     * @return Title for specified page.
	     */
	    @Override
	    public CharSequence getPageTitle(int position) {
	        Locale l = Locale.getDefault();
	        switch (position) {
	            case nFormTabPos:
	            	return getString(R.string.str_TabFormulaTitle).toUpperCase(l);
	            case nListTabPos:
	                return getString(R.string.str_TabListTitle).toUpperCase(l);
	            case nSampleTabPos:
	                return getString(R.string.str_TabSampleTitle).toUpperCase(l);	                
	        }
	        return null;
	    }
	    // END_INCLUDE (fragment_pager_adapter_getpagetitle)

	}
}
