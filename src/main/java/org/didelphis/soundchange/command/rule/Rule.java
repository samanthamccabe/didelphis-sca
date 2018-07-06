/******************************************************************************
 * Copyright (c) 2016 Samantha Fiona McCabe                                   *
 *                                                                            *
 * This software is not licensed for any purpose                              *
 ******************************************************************************/

package org.didelphis.soundchange.command.rule;

import org.didelphis.language.phonetic.sequences.Sequence;

import java.util.function.UnaryOperator;

/**
 * @author Samantha Fiona McCabe
 * @date 2016-10-23
 * @since 0.1.0
 */
@FunctionalInterface
public interface Rule<T> extends UnaryOperator<Sequence<T>>, Runnable {

	/**
	 * Applies the rule to the given input <i>in place</i>, that is, the object
	 * passed in will be modified
	 *
	 * @param sequence the {@link Sequence} to which this rule will be applied
	 * @param index    the index at which to apply this rule
	 *
	 * @return the index of the next position relative to the modified {@link
	 * Sequence}, <i>i.e.</i> the position of the cursor after this method has
	 * been called
	 */
	int applyAtIndex(Sequence<T> sequence, int index);

	@Override
	default Sequence<T> apply(Sequence<T> sequence) {
		// Step through the word to see if the rule might apply, i.e. if the
		// source pattern can be found
		for (int index = 0; index < sequence.size(); ) {
			index = applyAtIndex(sequence, index);
		}
		return sequence;
	}

	@Override
	default void run() {
		// Does nothing
	}
}
