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

import org.didelphis.common.language.enums.FormatterMode;
import org.didelphis.common.language.phonetic.LexiconMap;
import org.didelphis.common.language.phonetic.SequenceFactory;
import org.didelphis.common.language.phonetic.VariableStore;
import org.didelphis.common.language.phonetic.model.FeatureModel;
import org.didelphis.common.language.phonetic.model.StandardFeatureModel;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by samantha on 11/8/16.
 */
public class ParserMemory {
	private final LexiconMap lexicons;
	private final VariableStore variables;
	private final Set<String> reserved;

	private FormatterMode formatterMode;
	private FeatureModel featureModel;

	public ParserMemory() {
		lexicons = new LexiconMap();
		variables = new VariableStore(FormatterMode.NONE);
		reserved = new HashSet<>();
		formatterMode = FormatterMode.NONE;
		featureModel = StandardFeatureModel.EMPTY_MODEL;
	}

	public SequenceFactory factorySnapshot() {
		return new SequenceFactory(
			  featureModel,
			  new VariableStore(variables), 
			  new HashSet<>(reserved),
			  formatterMode
		);
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

	public FeatureModel getFeatureModel() {
		return featureModel;
	}

	public void setFeatureModel(FeatureModel featureModel) {
		this.featureModel = featureModel;
	}

	@Override
	public String toString() {
		return "ParserMemory{" +
				"lexicons=" + lexicons +
				", variables=" + variables +
				", reserved=" + reserved +
				", formatterMode=" + formatterMode +
				", featureModel=" + featureModel +
				'}';
	}
}
