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

import android.app.Application;
import android.content.Context;
import android.util.Log;

/**
 * @author berzem
 * Purpose of this class is to ensure that logging is turned off in release version.
 */
public class LogWrapper extends Application {

	final boolean bDebuggable;
	Context ctx;	
	
	LogWrapper()
	{
		bDebuggable = GlobalSettings.isDebuggable();
	}
	

	
	public int dlog (String sTag, String sMsg) {
		if (bDebuggable) {
			return (Log.d(sTag, sMsg));
		} else {
			return (0);
		}
	}
	

}
