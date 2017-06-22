/******************************************************************************
 * Copyright (c) 2016 Samantha Fiona McCabe                                   *
 *                                                                            *
 * This software is not licensed for any purpose                              *
 ******************************************************************************/

package org.didelphis.soundchange.command.rule;

import org.didelphis.language.phonetic.sequences.Sequence;

/**
 * Created by samantha on 10/23/16.
 */
public interface Rule<T> extends Runnable {

	/**
	 * Applies the rule to the provided <code>Sequence</code>. It is not
	 * required that the changes be made in-place, <i>i.e.</i> to the input
	 * parameter
	 *
	 * @param sequence the <code>Sequence</code> to which to apply the rule; cannot
	 * be null
	 * @return the modified <code>Sequence</code>. This can be
	 */
	Sequence<T> apply(Sequence<T> sequence);

	/**
	 * Applies the rule to the given input <i>in place</i>, that is, the object
	 * passed in will be modified
	 *
	 * @param sequence the <code>Sequence</code> to which this rule will be applied
	 * @param index the index at which to apply this rule
	 * @return the index of the next position relative to the modified
	 * <code>Sequence</code>, <i>i.e.</i> the position of the cursor after this
	 * method has been called
	 */
	int applyAtIndex(Sequence<T> sequence, int index);
}
