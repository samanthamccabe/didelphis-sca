/******************************************************************************
 * Copyright (c) 2016 Samantha Fiona McCabe                                   *
 * * This program is free software: you can redistribute it and/or modify * it
 * under the terms of the GNU General Public License as published by * the Free
 * Software Foundation, either version 3 of the License, or * (at your option)
 * any later version. * * This program is distributed in the hope that it will
 * be useful, * but WITHOUT ANY WARRANTY; without even the implied warranty of *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the * GNU General
 * Public License for more details. * * You should have received a copy of the
 * GNU General Public License * along with this program.  If not, see
 * <http://www.gnu.org/licenses/>. *
 ******************************************************************************/

package org.haedus.phonetic.model;

import org.haedus.phonetic.features.FeatureArray;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Samantha Fiona Morrigan McCabe Created: 7/2/2016
 */
public class FeatureSpecification {

	private final int size;
	private final Map<String, FeatureArray<Double>> aliases;
	private final Map<String, Integer> featureIndices;
	private final List<String> featureNames;
	private final List<Constraint> constraints;

	private FeatureSpecification() {
		size = 0;
		aliases = new HashMap<String, FeatureArray<Double>>();
		featureIndices = new HashMap<String, Integer>();
		featureNames = new ArrayList<String>();
		constraints = new ArrayList<Constraint>();
	}

	@Override
	public int hashCode() {
		int code = size;
		code *= 31 + aliases.hashCode();
		code *= 31 + featureIndices.hashCode();
		code *= 31 + featureNames.hashCode();
		code *= 31 + constraints.hashCode();
		return code;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj instanceof FeatureSpecification) {
			FeatureSpecification that = (FeatureSpecification) obj;
			return size == that.size
					&& aliases.equals(that.aliases)
					&& featureIndices.equals(that.featureIndices)
					&& featureNames.equals(that.featureIndices)
					&& constraints.equals(that.constraints);
		}
		return false;
	}
	
	public int getSize() {
		return size;
	}

	public List<String> getFeatureNames() {
		return Collections.unmodifiableList(featureNames);
	}

	public List<Constraint> getConstraints() {
		return Collections.unmodifiableList(constraints);
	}
}
