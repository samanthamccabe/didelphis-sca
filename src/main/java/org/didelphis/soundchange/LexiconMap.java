/*=============================================================================
 = Copyright (c) 2017. Samantha Fiona McCabe (Didelphis)
 =
 = Licensed under the Apache License, Version 2.0 (the "License");
 = you may not use this file except in compliance with the License.
 = You may obtain a copy of the License at
 =     http://www.apache.org/licenses/LICENSE-2.0
 = Unless required by applicable law or agreed to in writing, software
 = distributed under the License is distributed on an "AS IS" BASIS,
 = WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 = See the License for the specific language governing permissions and
 = limitations under the License.
 =============================================================================*/

package org.didelphis.soundchange;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import org.didelphis.language.phonetic.Lexicon;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

@ToString
@EqualsAndHashCode
public class LexiconMap<T> {

	private final Map<String, String> paths;
	private final Map<String, Lexicon<T>> lexicons;

	public LexiconMap(LexiconMap<T> map) {
		paths = map.paths;
		lexicons = map.lexicons;
	}

	public LexiconMap() {
		paths = new LinkedHashMap<>();
		lexicons = new LinkedHashMap<>();
	}

	public void addLexicon(String handle, String path, Lexicon<T> words) {
		paths.put(handle, path);
		lexicons.put(handle, words);
	}

	public void addAll(LexiconMap<T> map) {
		paths.putAll(map.paths);
		lexicons.putAll(map.lexicons);
	}

	public Lexicon<T> getLexicon(String handle) {
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

	public Collection<Lexicon<T>> values() {
		return lexicons.values();
	}

	public Lexicon<T> remove(String handle) {
		paths.remove(handle);
		return lexicons.remove(handle);
	}
}
