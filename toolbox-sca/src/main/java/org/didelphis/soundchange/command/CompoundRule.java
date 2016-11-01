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

import org.didelphis.phonetic.Lexicon;
import org.didelphis.phonetic.LexiconMap;
import org.didelphis.phonetic.Sequence;

import java.util.List;

/**
 * Created by samantha on 10/23/16.
 */
public class CompoundRule implements Rule {

	private final Iterable<BaseRule> rules;
	private final LexiconMap lexicons;

	public CompoundRule(Iterable<BaseRule> rules, LexiconMap lexicons) {
		this.rules = rules;
		this.lexicons = lexicons;
	}

	@Override
	public void run() {
		for (Lexicon lexicon : lexicons.values()) {
			for (List<Sequence> row : lexicon) {
				for (int i = 0; i < row.size(); i++) {
					Sequence word = apply(row.get(i));
					row.set(i, word);
				}
			}
		}
	}

	@Override
	public Sequence apply(Sequence input) {
		Sequence output = new Sequence(input);
		for (int index = 0; index < output.size(); index++) {
			for (BaseRule rule : rules) {
				rule.applyAtIndex(output, index);
			}
		}
		return output;
	}

	@Override
	public int applyAtIndex(Sequence input, int index) {
		return 0;
	}
}
