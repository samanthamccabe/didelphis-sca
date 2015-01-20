/*******************************************************************************
 * Copyright (c) 2015. Samantha Fiona McCabe
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package org.haedus.soundchange;

import org.apache.commons.io.FileUtils;
import org.haedus.datatypes.FormatterMode;
import org.haedus.datatypes.phonetic.Lexicon;
import org.haedus.exceptions.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: Samantha Fiona Morrigan McCabe
 * Date: 9/28/13
 * Time: 5:23 PM
 * To change this template use File | Settings | File Templates.
 */
public class Main {
	private static final transient Logger LOGGER = LoggerFactory.getLogger(Main.class);

	private Main() {
	}

	public static void main(String[] args) throws IOException, ParseException
	{
		double startTime = System.nanoTime();
		// 1) toolbox example.rule # loads rule; all segmentation/normalization is controlled internally
		// 2) toolbox -b [-m(S|D|C)] input.lex basic.rule output.lex
		//		>> toolbox -b input.lex basic.rule output.lex     # basic mode without normalization
		//		>> toolbox -b -mS input.lex basic.rule output.lex # basic mode with smart segmentation
		//		>> toolbox -b -mD input.lex basic.rule output.lex # basic mode with canonical decomposition, no segmentation

		if (args.length == 0) {
			throw new IllegalArgumentException("No arguments were provided!");
		} else if (args.length == 1) {
			// If there is a single argument, it must be a rule
			String rules = FileUtils.readFileToString(new File(args[0]), "UTF-8");
			SoundChangeScript standardScript = new StandardScript(rules);
			standardScript.process();
		} else if (args[0].equals("-b")) {
			String lexiconPath;
			String rulesPath;
			String outputPath;

			FormatterMode mode;
			if (args[1].startsWith("-m")) {
				lexiconPath = args[2];
				rulesPath   = args[3];
				outputPath  = args[4];
				String modeFlag = args[1].substring(2);
				if (modeFlag.equals("S")) {
					mode = FormatterMode.INTELLIGENT;
				} else if (modeFlag.equals("D")) {
					mode = FormatterMode.DECOMPOSITION;
				} else if (modeFlag.equals("C")) {
					mode = FormatterMode.COMPOSITION;
				} else {
					throw new IllegalArgumentException("Unsupported or unknown format flag '"+modeFlag+"' was provided!");
				}
			} else {
				lexiconPath = args[1];
				rulesPath   = args[2];
				outputPath  = args[3];
				mode = FormatterMode.NONE;
			}

			String lexicon = FileUtils.readFileToString(new File(lexiconPath), "UTF-8");
			String rules   = FileUtils.readFileToString(new File(rulesPath),   "UTF-8");

			SoundChangeScript sca = new BasicScript(rules, lexicon, mode);
			sca.process();

			Lexicon outputLexicon = sca.getLexicon(BasicScript.DEFAULT_LEXICON);
			FileUtils.write(new File(outputPath), "UTF-8", outputLexicon.toString());

		} else {
			throw new IllegalArgumentException("An improper number of arguments were provided!\n" +
					"If running in 'basic' mode, ensure that the first parameter is 'b', followed by INPUT, RULE, and OUTPUT file paths.\n" +
					"If running in 'standard' mode, only provide the path of a rule file.");
		}

		double elapsedTime = System.nanoTime() - startTime;
		double time = (elapsedTime / Math.pow(10,9));
		LOGGER.info("Finished in {} seconds", time);
	}
}
