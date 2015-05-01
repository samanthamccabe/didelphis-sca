/******************************************************************************
 * Copyright (c) 2015. Samantha Fiona McCabe                                  *
 *                                                                            *
 * Licensed under the Apache License, Version 2.0 (the "License");            *
 * you may not use this file except in compliance with the License.           *
 * You may obtain a copy of the License at                                    *
 *     http://www.apache.org/licenses/LICENSE-2.0                             *
 * Unless required by applicable law or agreed to in writing, software        *
 * distributed under the License is distributed on an "AS IS" BASIS,          *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   *
 * See the License for the specific language governing permissions and        *
 * limitations under the License.                                             *
 ******************************************************************************/

package org.haedus.soundchange;

import org.haedus.enums.FormatterMode;
import org.haedus.exceptions.ParseException;
import org.haedus.phonetic.FeatureModel;
import org.haedus.phonetic.Lexicon;
import org.haedus.phonetic.Sequence;
import org.haedus.phonetic.SequenceFactory;
import org.haedus.phonetic.VariableStore;
import org.haedus.soundchange.command.Rule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 * Samantha Fiona Morrigan McCabe
 * Created: 1/14/2015
 */
public class BasicScript extends AbstractScript {

	public static final String DEFAULT_LEXICON = "DEFAULT";

	private final HashSet<String> reservedSymbols;

	private final FormatterMode formatterMode;
	private final FeatureModel     featureModel;

	public BasicScript(String[] rulesParam, String[] lexiconParam, FormatterMode modeParam) {
		reservedSymbols = new HashSet<String>();
		featureModel = FeatureModel.EMPTY_MODEL;
		formatterMode = modeParam;

		parse(rulesParam);
		populateLexicon(lexiconParam);
	}

	public BasicScript(CharSequence rulesParam, CharSequence lexiconParam, FormatterMode modeParam) {
		reservedSymbols = new HashSet<String>();
		featureModel = FeatureModel.EMPTY_MODEL;
		formatterMode = modeParam;


		parse(WHITESPACE_PATTERN.split(rulesParam));
		populateLexicon(WHITESPACE_PATTERN.split(lexiconParam));
	}

	private void populateLexicon(String[] lexiconParam) {

		SequenceFactory factory = new SequenceFactory(featureModel, new VariableStore(), new HashSet<String>(reservedSymbols), formatterMode);
		Lexicon lexicon = new Lexicon();
		for (String line : lexiconParam) {
			List<Sequence> row = new ArrayList<Sequence>();
			for (String cell : line.split("\\t")) {
				row.add(factory.getSequence(cell));
			}
			lexicon.add(row);
		}
		lexicons.addLexicon(DEFAULT_LEXICON, lexicon);
	}

	private void parse(String[] strings) {
		VariableStore variables = new VariableStore();

		for (String string : strings) {
			if (!string.startsWith(COMMENT_STRING) && !string.isEmpty()) {
				String command = COMMENT_PATTERN.matcher(string).replaceAll("");

				if (command.contains("=")) {
					variables.add(command);
				} else if (command.contains(">")) {
					SequenceFactory factory = new SequenceFactory(
							FeatureModel.EMPTY_MODEL,
							new VariableStore(variables),
							new HashSet<String>(reservedSymbols),
							formatterMode
					);

					commands.add(new Rule(command, lexicons, factory));
				} else if (command.startsWith(RESERVE_STRING)) {
					String reserve = RESERVE_PATTERN.matcher(command).replaceAll("");
					Collections.addAll(reservedSymbols, WHITESPACE_PATTERN.split(reserve));
				} else if (command.startsWith("BREAK")) {
					// Stop parsing commands
					break;
				} else {
					throw new ParseException("Unrecognized or unsupported command:\n\t\"" + string +
							"\"\n\nIf you wish to import scripts, or read and write lexicons in the script file, " +
							"you will need to run the SCA in standard mode, where these capabilities are enabled.");
				}
			}
		}
	}
}
