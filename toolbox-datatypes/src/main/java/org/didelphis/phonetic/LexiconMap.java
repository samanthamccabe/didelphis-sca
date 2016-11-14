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

package org.didelphis.phonetic;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Samantha Fiona Morrigan McCabe
 * Created: 1/16/2015
 */
public class LexiconMap {

	private final Map<String, String>  paths;
	private final Map<String, Lexicon> lexicons;

	public LexiconMap() {
		paths = new LinkedHashMap<String, String>();
		lexicons = new LinkedHashMap<String, Lexicon>();
	}

	public void addLexicon(String handle, String path, Lexicon words) {
		paths.put(handle, path);
		lexicons.put(handle, words);
	}

	public void addAll(LexiconMap m) {
		paths.putAll(m.paths);
		lexicons.putAll(m.lexicons);
	}

	public Lexicon getLexicon(String handle) {
		return lexicons.get(handle);
	}

	public String getPath(String handle) {
		return paths.get(handle);
	}

	public boolean hasHandle(String handle) {
		return lexicons.containsKey(handle);
	}

	public Collection<String> getHandles() {
		return lexicons.keySet();
	}

	public Collection<Lexicon> values() {
		return lexicons.values();
	}

	public Lexicon remove(String handle) {
		paths.remove(handle);
		return lexicons.remove(handle);
	}

	@Override
	public String toString() {
		return "LexiconMap:" + lexicons;
	}
}
