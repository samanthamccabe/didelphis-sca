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

package org.haedus.datatypes.phonetic;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Samantha Fiona Morrigan McCabe
 * Created: 1/17/2015
 */
public class Lexicon implements Iterable<List<Sequence>> {

	private final List<List<Sequence>> lexicon;

	public Lexicon() {
		lexicon = new ArrayList<List<Sequence>>();
	}

	public void add(Sequence sequence) {
		List<Sequence> row = new ArrayList<Sequence>();
		row.add(sequence);
		lexicon.add(row);
	}

	public void add(List<Sequence> row ) {
		lexicon.add(row);
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		Iterator<List<Sequence>> iterator = lexicon.iterator();
		while (iterator.hasNext()) {
			List<Sequence> line = iterator.next();
			Iterator<Sequence> it = line.iterator();
			while (it.hasNext()) {
				Sequence sequence = it.next();
				sb.append(sequence.toString());
				if (it.hasNext()) {
					sb.append("\\t");
				}
			}
			if (iterator.hasNext()) {
				sb.append("\\n");
			}
		}
		return sb.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null) return false;
		if (getClass() != o.getClass()) return false;

		Lexicon lexicon1 = (Lexicon) o;
		return lexicon.equals(lexicon1.lexicon);
	}

	@Override
	public int hashCode() {
		return 11 * lexicon.hashCode();
	}

	@Override
	public Iterator<List<Sequence>> iterator() {
		return lexicon.iterator();
	}
}
