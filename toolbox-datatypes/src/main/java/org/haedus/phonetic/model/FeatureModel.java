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

package org.haedus.phonetic.model;

import org.haedus.enums.FormatterMode;
import org.haedus.phonetic.Segment;
import org.haedus.phonetic.SpecificationBearer;
import org.haedus.phonetic.features.FeatureArray;

import java.util.Map;
import java.util.Set;

/**
 * Samantha Fiona Morrigan McCabe
 * Created: 7/31/2016
 */
public interface FeatureModel extends SpecificationBearer {
	
	FormatterMode getFormatterMode();

	String getBestSymbol(FeatureArray<Double> featureArray);

	Set<String> getSymbols();

	boolean containsKey(String key);

	Map<String, FeatureArray<Double>> getFeatureMap();

	Map<String, FeatureArray<Double>> getModifiers();

	FeatureArray<Double> getValue(String key);

	// This should be here because how the segment is constructed is a function
	// of what kind of model this is
	Segment getSegment(String head, Iterable<String> diacritics);
}
