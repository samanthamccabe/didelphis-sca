/*******************************************************************************
 * Copyright (c) 2015. Samantha Fiona McCabe
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package org.didelphis.exceptions;

/**
 *  Author: Samantha Fiona Morrigan McCabe
 * Created: 8/25/2014
 */
public class ParseException extends RuntimeException {

	private final String data;

	public ParseException(String message, String data) {
		super(message);
		this.data = data;
	}
	
	public ParseException(String message, String data, Throwable cause) {
		super(message, cause);
		this.data = data;
	}
	
	@Override
	public String toString() {
		return super.toString() + ' ' + data;
	}
	
	public String getData() {
		return data;
	}
}
