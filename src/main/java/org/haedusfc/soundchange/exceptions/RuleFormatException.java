package org.haedusfc.soundchange.exceptions;

/**
 * Created with IntelliJ IDEA.
 * User: goats
 * Date: 5/27/13
 * Time: 3:41 PM
 * To change this template use File | Settings | File Templates.
 */
public class RuleFormatException extends Exception {

	public RuleFormatException() {
		super();
	}

	public RuleFormatException(String message) {
		super("Illegal Format in Rule:\n\t"+message);
	}

	public RuleFormatException(String message, Throwable cause) {
		super("Illegal Format in Rule:\n\t"+message, cause);
	}
}
