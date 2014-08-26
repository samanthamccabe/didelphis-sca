package org.haedus.exceptions;

/**
 *  Author: Samantha Fiona Morrigan McCabe
 * Created: 8/25/2014
 */
public class ParseException extends Exception {

	public ParseException() {
		super();
	}

	public ParseException(String message) {
		super(message);
	}

	public ParseException(String message, Throwable cause) {
		super(message, cause);
	}
}
