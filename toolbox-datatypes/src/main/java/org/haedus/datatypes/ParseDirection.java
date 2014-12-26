package org.haedus.datatypes;

/**
 * Created by samantha on 12/24/14.
 */
public enum ParseDirection {
	FORWARD  ("Forward"),
	BACKWARD ("Backward");

	private final String value;

	ParseDirection(String param) { value = param; }
}
