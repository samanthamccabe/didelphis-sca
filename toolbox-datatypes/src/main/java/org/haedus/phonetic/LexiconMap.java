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

package org.haedus.phonetic;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Samantha Fiona Morrigan McCabe
 * Created: 1/16/2015
 */
public class LexiconMap {

	private final Map<String, Lexicon> map;

	public LexiconMap() {
		map = new HashMap<String, Lexicon>();
	}

	public void addLexicon(String handle, Lexicon words) {
		map.put(handle, words);
	}

	public Lexicon get(String handle) {
		return map.get(handle);
	}

	public boolean hasHandle(String handle) {
		return map.containsKey(handle);
	}

	public Collection<String> getHandles() {
		return map.keySet();
	}

	public Collection<Lexicon> values() {
		return map.values();
	}

	public Lexicon remove(String handle) {
		return  map.remove(handle);
	}
}
