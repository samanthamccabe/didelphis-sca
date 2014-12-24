package org.haedus.machines;

/**
 * Created by samantha on 12/24/14.
 */
public enum ParseDirection {
	FORWARD  ("Forward"),
	BACKWARD ("Backward");

	private final String value;

	ParseDirection(String param) { value = param; }
}
