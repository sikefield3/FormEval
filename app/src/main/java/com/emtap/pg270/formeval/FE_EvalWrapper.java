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

import java.util.Arrays;

import de.congrace.exp4j.Calculable;
import de.congrace.exp4j.CustomFunction;
import de.congrace.exp4j.ExpressionBuilder;
import de.congrace.exp4j.InvalidCustomFunctionException;
import de.congrace.exp4j.UnknownFunctionException;
import de.congrace.exp4j.UnparsableExpressionException;

public class FE_EvalWrapper {
	LogWrapper lw;
	final String TAG = "FE_EvalWrapper";
	final String sLeftBracket = "(";
	private boolean exp4j_bJuxtaPosition = true;
	private boolean exp4j_bSignWorkaround = true;
	private boolean exp4j_bMathConsts = true; // include e and pi 
	
	String exp4j_sMathConstNameE, exp4j_sMathConstNamePi;
	String exp4j_sCommonExp, exp4j_sFortranExp;
	String exp4j_sLog10Name;
	
	final String[] asTrigFunc = {"sin", "cos", "tan"};
	final String[] asInvTrigFunc = {"asin", "acos", "atan"};
	final String sPiExp = "2*asin(1)"; 
	final String sCnvLog = "1/log(10)"; // to have log to base 10
	double dCnstPi, dCnvLog;
	double dTrigConvFactor;// TODO fix this
	double dInvTrigConvFactor; // TODO fix this
	TrigConv oTrigConv; 
	cfLog10 oCfLog10;
	String sTrigConvFunctionName;
	
	FE_EvalWrapper() {
		 lw = new LogWrapper();
	}
	public void confInternalFuncs(){
		 try {
			dCnstPi = evaluate(sPiExp); // TODO this is a workaround, since sin(Math.Pi) gives not exactly 0
			dTrigConvFactor = dCnstPi / 180.0; // TODO fix this
			dInvTrigConvFactor = 1.0 / dTrigConvFactor; // TODO fix this			
			dCnvLog = evaluate(sCnvLog);
		} catch (FE_UnknownFuncException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (FE_UnparsableExprException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
//			oTrigConv = new TrigConv();
			lw.dlog(TAG, "confInternalFuncs: ".concat(sTrigConvFunctionName));
			oTrigConv = new TrigConv(sTrigConvFunctionName);			
			lw.dlog(TAG, "confInternalFuncs: ".concat(exp4j_sLog10Name));
			oCfLog10 = new cfLog10(exp4j_sLog10Name);
//			oCfLog10 = new cfLog10();
		} catch (InvalidCustomFunctionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	public void setMathConstNameE(String exp4j_sMathConstNameE) {
		this.exp4j_sMathConstNameE = exp4j_sMathConstNameE;
	}
	public void setMathConstNamePi(String exp4j_sMathConstNamePi) {
		this.exp4j_sMathConstNamePi = exp4j_sMathConstNamePi;
	}
	public void setFortranExp(String exp4j_sFortranExp) {
		this.exp4j_sFortranExp = exp4j_sFortranExp;
	}
	public void setCommonExp(String exp4j_sCommonExp) {
		this.exp4j_sCommonExp = exp4j_sCommonExp;
	}

	/**
	 * if true: 'e' and 'pi' get their usual values 
	 */
	public void useMathConstants(boolean bMathConsts) {
		this.exp4j_bMathConsts = bMathConsts;
	}	
	/**
	 *  Tweek for exp4j library, on construction set to true
	 *  Set to false too avoid following behaviour of exp4j:
	 *  an expression "j(4+1)" would give the result 5, if 
	 *  j is a valid variable (otherwise we get an error)
	 *  After setJuxtaPosition(false) this gives always an error.
	 * @param exp4j_bJuxtaPosition the bJuxtaPosition to set
	 */
	public void setJuxtaPosition(boolean bJuxtaPosition) {
		this.exp4j_bJuxtaPosition = bJuxtaPosition;
	}
	/**
	 * Tweek for exp4j library, set true to avoid the quirky operator precedence of "-"
	 * in unary usage: -5^2 would give +25 
	 * @param exp4j_bSignWorkaround
	 */
	public void setbSignWorkaround(boolean exp4j_bSignWorkaround) {
		this.exp4j_bSignWorkaround = exp4j_bSignWorkaround;
	}
	/**
	 * @param sTrigConvFunctionName: This internally used function name should be longer than any variable names.
	 */
	public void setTrigConvFunctionName(String sTrigConvFunctionName) {
		this.sTrigConvFunctionName = sTrigConvFunctionName;
	}

	public void setLog10Name(String exp4j_sLog10Name) {
		this.exp4j_sLog10Name = exp4j_sLog10Name;
	}

	double evaluate (String sInput) throws FE_UnknownFuncException, FE_UnparsableExprException {
		Calculable calc;
		
		try{
			calc = new ExpressionBuilder(sInput).build();			
		} catch (UnknownFunctionException e) {
			throw new FE_UnknownFuncException();
		} catch (UnparsableExpressionException e) {
			throw new FE_UnparsableExprException();
		}
		return (calc.calculate());
	}
	double evaluate (String sInput, int cntVars, String[] aStrVarNames, double[] aDVarValues) throws FE_UnknownFuncException, FE_UnparsableExprException {
		Calculable calc;
		sInput = sInput.replace(" ", "");
		// juxtaposition check
		if (exp4j_bJuxtaPosition == false) {			
			String sTemp;
			for (int j = 0;j < aStrVarNames.length;j++) {
				sTemp = aStrVarNames[j];
				if (sTemp == null || sTemp.length() <= 0) {
					continue;
				}
				if (sInput.indexOf(sTemp.concat(sLeftBracket)) >= 0) {
					throw new FE_UnparsableExprException();
				}
			}
		}
		if (exp4j_bSignWorkaround) { // to avoid error in operator priority
			String sOperatorSymbols = GlobalSettings.getOperatorSymbols();
			String sWorkAroundSymb = "0"; // we set "-" to "0-" to avoid error
			for (int j = 1; j < sInput.length();j++){ // (nb: start with 1!) here we check if have sth like "*-" or "+-" etc, which we disallow 
				if (sInput.charAt(j) == '-' && sOperatorSymbols.indexOf(sInput.charAt(j-1)) > -1) {
					throw new FE_UnparsableExprException();
				}
			}
			// now check if input begins with "-" and set to "0-"
			if (sInput.charAt(0) == '-'){
				sInput = sWorkAroundSymb.concat(sInput);
			}
			// now replace "(-" with "(0-"
			sInput = sInput.replace("(-", "(".concat(sWorkAroundSymb).concat("-"));
			
		}
		if (GlobalSettings.getFortranStyle()) { // use FORTRAN Exponent
			sInput = sInput.replace(exp4j_sFortranExp, exp4j_sCommonExp);
		}
		// degree /  radian conversion
		// TODO error if one of the trig. function names is part of a variable !
		boolean bIsDegree = GlobalSettings.getAngleDegree();
		final String sTrgFuncBugWorakaround = ",0+";
		final String sTrgFunc = sTrigConvFunctionName.concat("(");
		if (bIsDegree) {
			for (int j = 0;j < asInvTrigFunc.length;j++) {
				sInput = sInput.replace(asInvTrigFunc[j].concat("("), sTrgFunc.concat(Integer.toString(-(j+1))).concat(sTrgFuncBugWorakaround));
			}
			for (int j = 0;j < asTrigFunc.length;j++) {
				sInput = sInput.replace(asTrigFunc[j].concat("("), sTrgFunc.concat(Integer.toString(j+1)).concat(sTrgFuncBugWorakaround)); 
			}
			lw.dlog(TAG, "evaluate: degree: ".concat(sInput));
		}		
		ExpressionBuilder oExpressionBuilder = new ExpressionBuilder(sInput);
		oExpressionBuilder = oExpressionBuilder.withCustomFunction(oCfLog10);
		if (bIsDegree) {
			oExpressionBuilder = oExpressionBuilder.withCustomFunction(oTrigConv);
		}
		
		if (exp4j_bMathConsts) {
			if (Arrays.asList(aStrVarNames).contains(exp4j_sMathConstNameE) || Arrays.asList(aStrVarNames).contains(exp4j_sMathConstNamePi)) {
				throw new NumberFormatException();
			}
			oExpressionBuilder = oExpressionBuilder.withVariable(exp4j_sMathConstNameE,Math.E);
			oExpressionBuilder = oExpressionBuilder.withVariable(exp4j_sMathConstNamePi,dCnstPi);
		}
		
		for (int j = 0;j < cntVars; j++) {
			oExpressionBuilder = oExpressionBuilder.withVariable(aStrVarNames[j], aDVarValues[j]);
		}
		try{
			calc = oExpressionBuilder.build();
		} catch (UnknownFunctionException e) {
			throw new FE_UnknownFuncException();
		} catch (UnparsableExpressionException e) {
			throw new FE_UnparsableExprException();
		}
		return (calc.calculate());
	}
	void testCustFunc(){
		lw.dlog(TAG, "testCustFunc()");
		CustomFunction oTestTrgBugCust;
		try {	
			oTestTrgBugCust = new CustomFunction("TestTrgBugCust",2) {
			public double applyFunction(double...values) {
			                return values[1];
			}};
		} catch (InvalidCustomFunctionException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
			return;
		}

//		testtrg1Param(); // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		
		
//		String sInput = "TestTrgBugCust(0,-1)";
//		String sInput = "TestTrgBugCust(0,1)";
//		String sInput = "TestTrgBugCust(0,0)";
//		String sInput = "TestTrgBugCust(0,0+-1)";
//		String sInput = "TestTrgBugCust(0,0++1)";
		String sInput = "TestTrgBugCust(0,++1)";
//		String sInput = "TestTrgBugCust(0,+-1)";

		try {
			ExpressionBuilder oExpressionBuilder = new ExpressionBuilder(sInput);
			oExpressionBuilder = oExpressionBuilder.withCustomFunction(oTestTrgBugCust);
			Calculable calc = oExpressionBuilder.build();
			double dResult = calc.calculate();
			lw.dlog(TAG, "TestTrgBugCust Result: ".concat(Double.toString(dResult)));
		} catch (UnknownFunctionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnparsableExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		
	}
	
	void testtrg(){

//		testtrg1Param(); // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		
		TestTrgBug oTestTrgBug;
//		String sInput = "TestTrgBug(0,-1)";
//		String sInput = "TestTrgBug(0,1)";
//		String sInput = "TestTrgBug(0,0)";
//		String sInput = "TestTrgBug(0,0+-1)";
		String sInput = "TestTrgBug(0,0++1)";

		try {
			oTestTrgBug = new TestTrgBug();
			ExpressionBuilder oExpressionBuilder = new ExpressionBuilder(sInput);
			oExpressionBuilder = oExpressionBuilder.withCustomFunction(oTestTrgBug);
			Calculable calc = oExpressionBuilder.build();
			double dResult = calc.calculate();
			lw.dlog(TAG, "testtrg Result: ".concat(Double.toString(dResult)));
		} catch (InvalidCustomFunctionException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (UnknownFunctionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnparsableExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		
	}
	void testtrg1Param(){
		TestTrgBug1Param oTestTrgBug1Param;
		String sInput = "TestTrgBug1Param(0,-180)";
		

		try {
			oTestTrgBug1Param = new TestTrgBug1Param();
			ExpressionBuilder oExpressionBuilder = new ExpressionBuilder(sInput);
			oExpressionBuilder = oExpressionBuilder.withCustomFunction(oTestTrgBug1Param);
			Calculable calc = oExpressionBuilder.build();
			double dResult = calc.calculate();
			lw.dlog(TAG, "TestTrgBug1Param Result: ".concat(Double.toString(dResult)));
		} catch (InvalidCustomFunctionException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (UnknownFunctionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnparsableExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		
	}

	/**
	 * Test of exp4j:
	 * 1. long input: 1+2+3...+N
	 * 	  tested with N=512 ok, time (from logcat: 0.33 sec on real device)
	 * 2. great number of vars : x1+x2+x3...+xN
	 * 	  tested with N=256 ok, time (from logcat: 0.46 sec on real device)
	 * 
	 */
	public void testExp4j()
	{
		// Test 1: input length
		String[] aStrVarName = new String[5];
		double[] aDVarValue = new double [5];
		double dResult, dControlResult;
		// 1. test length of input
		int N = 4;
		String sInput = "1";
		for (int j = 2;j <= N; j++) {
			sInput = sInput.concat("+");
			sInput = sInput.concat(Integer.toString(j));		
		}
		lw.dlog(TAG, "Test 1");
		lw.dlog(TAG, "N: ".concat(Integer.toString(N)));
		lw.dlog(TAG, "sInput: ".concat(sInput));
		lw.dlog(TAG, "sInput.length(): ".concat(Integer.toString(sInput.length())));
		dControlResult = 0.5*N*(N+1);

		try {
			dResult = evaluate(sInput, 0, aStrVarName, aDVarValue);
			lw.dlog(TAG, "Result: ".concat(Double.toString(dResult)));
			lw.dlog(TAG, "Control: ".concat(Double.toString(dControlResult)));
		} catch (FE_UnknownFuncException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FE_UnparsableExprException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Test 2: number of vars
		N=256;
		aStrVarName = new String[N];
		aDVarValue = new double [N];
		for (int j=0;j<N;j++) {
			aStrVarName[j] = "x".concat(Integer.toString(j+1));
			aDVarValue[j] = (double)(j+1);			
		}
		sInput = "x1";
		for (int j = 1;j < N; j++) {
			sInput = sInput.concat("+");
			sInput = sInput.concat(aStrVarName[j]);
		}
		lw.dlog(TAG, "Test 2");		
		lw.dlog(TAG, "N: ".concat(Integer.toString(N)));
		lw.dlog(TAG, "sInput: ".concat(sInput));
		lw.dlog(TAG, "sInput.length(): ".concat(Integer.toString(sInput.length())));
		dControlResult = 0.5*N*(N+1);

		try {
			dResult = evaluate(sInput, N, aStrVarName, aDVarValue);
			lw.dlog(TAG, "Result: ".concat(Double.toString(dResult)));
			lw.dlog(TAG, "Control: ".concat(Double.toString(dControlResult)));
		} catch (FE_UnknownFuncException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FE_UnparsableExprException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	class TrigConv extends CustomFunction{
//		TrigConv() throws InvalidCustomFunctionException {super("trigconv",2);}; // TODO set string in string.xml
		TrigConv(String sName) throws InvalidCustomFunctionException {super(sName,2);}; // TODO set string in string.xml		
	    public double applyFunction(double...values) { // TODO create asin, acos, atan
	    	int nFuncIndex = (int)values[0];
	    	lw.dlog(TAG, "CustomFunction: applyFunction: dTrigConvFactor: ".concat(Double.toString(dTrigConvFactor)));
	    	lw.dlog(TAG, "CustomFunction: applyFunction: ".concat(Double.toString(values[1])));

	    	switch (nFuncIndex){
	    	case 1:
	    		return (Math.sin(values[1] * dTrigConvFactor));
	    	case 2:
	    		return (Math.cos(values[1] * dTrigConvFactor));
	    	case 3:
	    		return (Math.tan(values[1] * dTrigConvFactor));
	    	case -1:
	    		return (dInvTrigConvFactor * Math.asin(values[1]));
	    	case -2:
	    		return (dInvTrigConvFactor * Math.acos(values[1]));
	    	case -3:
	    		return (dInvTrigConvFactor * Math.atan(values[1]));	    		
	    	default: // error case 
	    		throw new IllegalArgumentException();
	    	}    	
	    }
	}
	class TestTrgBug extends CustomFunction{
		TestTrgBug() throws InvalidCustomFunctionException {super("TestTrgBug",2);}; // TODO set string in string.xml
//		TrigConv(String sName) throws InvalidCustomFunctionException {super(sName,2);}; // TODO set string in string.xml		
	    public double applyFunction(double...values) { // TODO create asin, acos, atan
//	    	return(0.0);
	    	return(values[1]);
	    }
	}
	class TestTrgBug1Param extends CustomFunction{
		TestTrgBug1Param() throws InvalidCustomFunctionException {super("TestTrgBug1Param");}; // TODO set string in string.xml
//		TrigConv(String sName) throws InvalidCustomFunctionException {super(sName,2);}; // TODO set string in string.xml		
	    public double applyFunction(double...value) { // TODO create asin, acos, atan
//	    	return(0.0);
	    	return(value[1]);
	    }	    
//	    public double applyFunction(double[] value) { 
////	    	return(0.0);
//	    	return(value[1]);
//	    }	    
	}	
	class cfLog10 extends CustomFunction{
		cfLog10(String sName) throws InvalidCustomFunctionException {super(sName,1);};
//		cfLog10() throws InvalidCustomFunctionException {super("log10",1);};
		public double applyFunction(double...values) { 
			return (Math.log10(values[0]));
		}
	}
}

