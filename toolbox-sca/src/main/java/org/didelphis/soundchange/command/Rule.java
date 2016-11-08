/******************************************************************************
 * Copyright (c) 2016 Samantha Fiona McCabe                                   *
 *                                                                            *
 * This program is free software: you can redistribute it and/or modify       *
 * it under the terms of the GNU General Public License as published by       *
 * the Free Software Foundation, either version 3 of the License, or          *
 * (at your option) any later version.                                        *
 *                                                                            *
 * This program is distributed in the hope that it will be useful,            *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the              *
 * GNU General Public License for more details.                               *
 *                                                                            *
 * You should have received a copy of the GNU General Public License          *
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.      *
 ******************************************************************************/

package org.didelphis.soundchange.command;

import org.didelphis.phonetic.Sequence;

/**
 * Created by samantha on 10/23/16.
 */
public interface Rule extends Runnable {

	/**
	 * Applies the rule to the provided <code>Sequence</code>. It is not
	 * required that the changes be made in-place, <i>i.e.</i> to the input
	 * parameter
	 *
	 * @param input the <code>Sequence</code> to which to apply the rule; cannot
	 * be null
	 * @return the modified <code>Sequence</code>. This can be
	 */
	public Sequence apply(Sequence input);

	/**
	 * Applies the rule to the given input <i>in place</i>, that is, the object
	 * passed in will be modified
	 *
	 * @param input the <code>Sequence</code> to which this rule will be applied
	 * @param index the index at which to apply this rule
	 * @return the index of the next position relative to the modified
	 * <code>Sequence</code>, <i>i.e.</i> the position of the cursor after this
	 * method has been called
	 */
	public int applyAtIndex(Sequence input, int index);
}
