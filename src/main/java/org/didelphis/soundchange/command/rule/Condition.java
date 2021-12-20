package org.didelphis.soundchange.command.rule;

import org.didelphis.language.phonetic.sequences.Sequence;

public interface Condition {

	/**
	 * Checks if this condition is applicable to the Sequence at the provided
	 * index
	 *
	 * @param word  the Sequence to check
	 * @param index the index of the targeted Sequence; cannot be negative; the
	 *              target will automatically end at index + 1
	 *
	 * @return Returns true if the condition isMatch
	 */
	boolean isMatch(Sequence word, int index);

	/**
	 * Checks if this condition is applicable to the Sequence at the provided
	 * index
	 *
	 * @param word       the Sequence to check
	 * @param startIndex the first index of the targeted Sequence; cannot be
	 *                   negative
	 * @param endIndex   the last index of the targeted Sequence (exclusive);
	 *                   cannot be negative
	 *
	 * @return Returns true if the condition isMatch
	 */
	boolean isMatch(Sequence word, int startIndex, int endIndex);
}
