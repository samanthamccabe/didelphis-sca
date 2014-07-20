package org.haedusfc.soundchange.exceptions;

/**
 * Class VariableDefinitionFormatException
 * <p/>
 * Description:
 * TODO Add class description
 * <p/>
 * Created: 09/23/2013
 */
public class VariableDefinitionFormatException extends Exception {

	public VariableDefinitionFormatException() {
		super();
	}

	public VariableDefinitionFormatException(String message) {
		super("Illegal Variable Definition:\n\t"+message);
	}
}
