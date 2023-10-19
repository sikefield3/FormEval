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

/**
 * @author berzem
 * This class is used to populate a listview in reverse order and should not be used for anything else !!!
 * Only the "get" method returns elements in reverse order (if the constructor parameter is set to false), 
 * other methods are not implemented 
 * @param <E>
 */
class FEArrayList<E> extends ArrayList<E> {


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private boolean bOrderForItemGetterTopDown = true;
	public FEArrayList(boolean bOrderForItemGetterTopDown) {
		super();
		this.bOrderForItemGetterTopDown = bOrderForItemGetterTopDown;
	}
	
	public E get(int index){
		if (bOrderForItemGetterTopDown){
			return (super.get(index));
		} else {
			return (super.get(this.size()-1-index));
		}
	}
}
