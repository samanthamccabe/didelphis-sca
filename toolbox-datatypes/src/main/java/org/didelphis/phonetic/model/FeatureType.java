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

package org.didelphis.phonetic.model;

import java.util.regex.Pattern;

/**
 * Samantha Fiona Morrigan McCabe
 * Created: 7/21/2016
 */
public enum FeatureType {
	BINARY("(\\+|-|−)?"),
	TERNARY("(\\+|-|−|0)?"),
	NUMERIC("(-?\\d+)?");

	private final Pattern pattern;

	FeatureType(String value) {
		pattern = Pattern.compile(value);
	}

	boolean matches(CharSequence value) {
		return pattern.matcher(value).matches();
	}
}
