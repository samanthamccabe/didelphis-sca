/******************************************************************************
 * Copyright (c) 2016 Samantha Fiona McCabe                                   *
 *                                                                            *
 * This software is not licensed for any purpose                              *
 ******************************************************************************/

package org.didelphis.soundchange;

import java.util.ArrayList;
import java.util.List;

/**
 * Samantha Fiona Morrigan McCabe
 * Created: 11/5/2016
 */
public class ErrorLogger {
	
	private final List<Error> errors;
	
	public ErrorLogger() {
		errors = new ArrayList<Error>();
	}
	
	public void add(String filePath, int line, String data, Exception ex) {
		errors.add(new Error(filePath, line, data, ex));
	}
	
	public void clear() {
		errors.clear();
	}
	
	public static class Error {
		private final String script;
		private final int line;
		private final String data;
		private final Exception exception;
		
		public Error(String script, int line, String data, Exception exception) {
			this.script = script;
			this.line = line;
			this.data = data;
			this.exception = exception;
		}
	}
}
