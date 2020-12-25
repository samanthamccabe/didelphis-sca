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

package org.didelphis.soundchange.parser;

import org.didelphis.io.NullFileHandler;
import org.didelphis.language.parsing.FormatterMode;
import org.didelphis.language.phonetic.SequenceFactory;
import org.didelphis.language.phonetic.model.FeatureMapping;
import org.didelphis.language.phonetic.model.FeatureModelLoader;
import org.didelphis.soundchange.LexiconMap;
import org.didelphis.soundchange.VariableStore;

import java.util.HashSet;
import java.util.Set;

/**
 * @since 0.2.0
 */
public class ParserMemory {
	private final LexiconMap lexicons;
	private final VariableStore variables;
	private final Set<String> reserved;

	private FormatterMode formatterMode;
	private FeatureMapping featureMapping;

	public ParserMemory() {
		lexicons = new LexiconMap();
		variables = new VariableStore(FormatterMode.NONE);
		reserved = new HashSet<>();
		formatterMode = FormatterMode.NONE;
		FeatureModelLoader loader = new FeatureModelLoader(
				NullFileHandler.INSTANCE,
				""
		);
		featureMapping = loader.getFeatureMapping();
	}

	public ParserMemory(ParserMemory memory) {
		lexicons = new LexiconMap(memory.lexicons);
		variables = new VariableStore(memory.getVariables());
		reserved = new HashSet<>(memory.reserved);
		formatterMode = memory.formatterMode;
		featureMapping = memory.featureMapping;
	}

	public SequenceFactory factorySnapshot() {
		HashSet<String> set = new HashSet<>(reserved);
		set.addAll(variables.getKeys());
		return new SequenceFactory(featureMapping, set, formatterMode);
	}

	public LexiconMap getLexicons() {
		return lexicons;
	}

	public VariableStore getVariables() {
		return variables;
	}

	public Set<String> getReserved() {
		return reserved;
	}

	public FormatterMode getFormatterMode() {
		return formatterMode;
	}

	public void setFormatterMode(FormatterMode formatterMode) {
		this.formatterMode = formatterMode;
		variables.setSegmenter(this.formatterMode);
	}

	public FeatureMapping getFeatureMapping() {
		return featureMapping;
	}

	public void setFeatureMapping(FeatureMapping featureMapping) {
		this.featureMapping = featureMapping;
	}

	@Override
	public String toString() {
		return "ParserMemory{" + "lexicons=" + lexicons + ", variables=" +
				variables + ", reserved=" + reserved + ", formatterMode=" +
				formatterMode + ", featureMapping=" + featureMapping + '}';
	}
}
