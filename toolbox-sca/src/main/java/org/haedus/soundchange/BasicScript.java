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

import org.haedus.datatypes.FormatterMode;
import org.haedus.datatypes.NormalizerMode;
import org.haedus.datatypes.SegmentationMode;
import org.haedus.datatypes.Segmenter;
import org.haedus.datatypes.phonetic.FeatureModel;
import org.haedus.datatypes.phonetic.Sequence;
import org.haedus.datatypes.phonetic.SequenceFactory;
import org.haedus.datatypes.phonetic.VariableStore;
import org.haedus.exceptions.ParseException;
import org.haedus.soundchange.command.Rule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Samantha Fiona Morrigan McCabe
 * Created: 1/14/2015
 */
public class BasicScript extends AbstractScript {

	public static final String DEFAULT_LEXICON = "DEFAULT";

//	private final FormatterMode formatterMode;

	private final SegmentationMode segmentationMode;
	private final NormalizerMode  normalizerMode;

	public BasicScript(CharSequence rulesParam, CharSequence lexiconParam, FormatterMode modeParam) {

		if (modeParam == FormatterMode.INTELLIGENT) {
			segmentationMode = SegmentationMode.DEFAULT;
			normalizerMode   = NormalizerMode.NFD;
		} else if (modeParam == FormatterMode.DECOMPOSITION) {
			segmentationMode = SegmentationMode.NAIVE;
			normalizerMode   = NormalizerMode.NFD;
		} else if (modeParam == FormatterMode.COMPOSITION) {
			segmentationMode = SegmentationMode.NAIVE;
			normalizerMode = NormalizerMode.NFC;
		}else if (modeParam == FormatterMode.NONE) {
			segmentationMode = SegmentationMode.NAIVE;
			normalizerMode   = NormalizerMode.NONE;
		} else {
			throw new UnsupportedOperationException("Invalid formatter mode provided: " + modeParam);
		}

		populateLexicon(lexiconParam);
		parse(rulesParam);
	}

	private void populateLexicon(CharSequence lexiconParam) {
		List<List<Sequence>> lexicon = new ArrayList<List<Sequence>>();
		for (String line : WHITESPACE_PATTERN.split(lexiconParam)) {
			List<Sequence> row = new ArrayList<Sequence>();
			for (String cell : line.split("\\t")) {
				row.add(Segmenter.getSequence(cell, FeatureModel.EMPTY_MODEL, new HashSet<String>(), segmentationMode, normalizerMode));
			}
			lexicon.add(row);
		}
		lexicons.put("DEFAULT_LEXICON", lexicon);
	}

	private void parse(CharSequence strings) {

		Set<String> reservedSymbols = new HashSet<String>();
		VariableStore variables = new VariableStore(FeatureModel.EMPTY_MODEL, segmentationMode, normalizerMode);

		for (String string : WHITESPACE_PATTERN.split(strings)) {
			if (!string.startsWith(COMMENT_STRING) && !string.isEmpty()) {
				String command = COMMENT_PATTERN.matcher(string).replaceAll("");

				if (command.contains("=")) {
					variables.add(command);
				} else if (command.contains(">")) {
					SequenceFactory factory = new SequenceFactory(FeatureModel.EMPTY_MODEL, new VariableStore(variables), reservedSymbols, segmentationMode, normalizerMode);
					commands.add(new Rule(command, lexicons, factory));
				} else if (command.startsWith(RESERVE)) {
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
