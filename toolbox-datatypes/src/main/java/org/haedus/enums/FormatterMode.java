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

package org.haedus.enums;

import java.text.Normalizer;

/**
 * This type is to succeed the earlier SegmentationMode and Normalizer mode
 * enums by merging their functionality. We originally supported types that
 * were entirely unnecessary and presented the user with an excess of options,
 * most of where were of no value (compatibility modes, or segmentation with
 * composition e.g.)
 *
 * Samantha Fiona Morrigan McCabe
 * Created: 1/14/2015
 */
public enum FormatterMode{
	// Unicode Canonical Decomposition
	DECOMPOSITION(Normalizer.Form.NFD),
	// Unicode Canonical Decomposition followed by Canonical Composition
	COMPOSITION(Normalizer.Form.NFC),
	// Uses segmentation algorithm with Unicode Canonical Decomposition
	INTELLIGENT(Normalizer.Form.NFD),
	// No change to input strings
	NONE(null);
	
	private final Normalizer.Form form;
	
	FormatterMode(Normalizer.Form param) {
		form = param;
	}
	
	public String normalize(String word) {
		if (form == null) {
			return word;
		} else {
			return Normalizer.normalize(word, form);
		}
	}
}
