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

import org.apache.commons.io.IOUtils;
import org.haedus.enums.FormatterMode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * Samantha Fiona Morrigan McCabe Created: 7/4/2016
 */
public class SpecificationLoader {
	
	public static FeatureSpecification fromStream(InputStream stream,
	                                                  String encoding) throws IOException {
		
		InputStreamReader in = new InputStreamReader(stream, "UTF-8");
		Reader reader = new BufferedReader(in);

		StringBuilder sb = new StringBuilder();
		int i = reader.read();
		while (i >= 0) {
			sb.append((char) i);
			i = reader.read();
		}
		return fromString(sb.toString());
	}
	
	public static FeatureSpecification fromString(String string) {
		
	}
}
