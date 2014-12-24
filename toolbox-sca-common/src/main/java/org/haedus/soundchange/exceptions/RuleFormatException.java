/*******************************************************************************
 * Copyright (c) 2014 Haedus - Fabrica Codicis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package org.haedus.soundchange.exceptions;

import org.haedus.exceptions.ParseException;

/**
 * Created with IntelliJ IDEA.
 * User: Samantha Fiona Morrigan McCabe
 * Date: 5/27/13
 * Time: 3:41 PM
 * To change this template use File | Settings | File Templates.
 */
public class RuleFormatException extends ParseException {

	public RuleFormatException(Throwable throwable) {
		super(throwable);
	}

	public RuleFormatException(String message) {
		super("Illegal Format in Rule:\n\t"+message);
	}

	public RuleFormatException(String message, Throwable cause) {
		super("Illegal Format in Rule:\n\t"+message, cause);
	}
}
