/******************************************************************************
 * Copyright (c) 2016. Samantha Fiona McCabe                                  *
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

package org.haedus.phonetic.model;

import java.util.Collections;
import java.util.Map;

/**
 * Samantha Fiona Morrigan McCabe
 * Created: 3/1/2016
 */
public class Constraint {
	
	private final Map<Integer, Double> source;
	private final Map<Integer, Double> target;
	
	public Constraint(Map<Integer, Double> sourceParam,
					  Map<Integer, Double> targetParam) {
		
		source = Collections.unmodifiableMap(sourceParam);
		target = Collections.unmodifiableMap(targetParam);
	}
	
	@SuppressWarnings("ReturnOfCollectionOrArrayField")
	public Map<Integer, Double> getTarget() {
		return target;
	}

	@SuppressWarnings("ReturnOfCollectionOrArrayField")
	public Map<Integer, Double> getSource() {
		return source;
	}

	@Override
	public String toString() {
		return "Constraint{" +
			"source=" + source +
			", target=" + target +
			'}';
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (obj == this) return true;
		if (!getClass().equals(obj.getClass())) return false;

		Constraint constraint = (Constraint) obj;
		
		return source.equals(constraint.source) && target.equals(constraint.source);
	}
	
	@Override
	public int hashCode() {
		return 31 * source.hashCode() * target.hashCode();
	}
}
