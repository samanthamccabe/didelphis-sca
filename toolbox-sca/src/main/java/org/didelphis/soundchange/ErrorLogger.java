/******************************************************************************
 * Copyright (c) 2016 Samantha Fiona McCabe                                   *
 *                                                                            *
 * This software is not licensed for any purpose                              *
 ******************************************************************************/

package org.didelphis.soundchange;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Samantha Fiona Morrigan McCabe
 * Created: 11/5/2016
 */
public class ErrorLogger implements Iterable<ErrorLogger.Error> {
	
	private final List<Error> errors;
	
	public ErrorLogger() {
		errors = new ArrayList<Error>();
	}
	
	public void add(String filePath, int line, String data, String message) {
		errors.add(new Error(filePath, line, data, message));
	}
	
	public void clear() {
		errors.clear();
	}

	public boolean isEmpty() {
		return errors.isEmpty();
	}

	@Override
	public Iterator<Error> iterator() {
		return errors.iterator();
	}
		
	@Override
	public String toString() {
		return "ErrorLogger{" + "errors=" + errors + '}';
	}
	
	public static class Error {
		private final String script;
		private final int line;
		private final String data;
		private final String message;
		
		public Error(String script, int line, String data, String message) {
			this.script = script;
			this.line = line;
			this.data = data;
			this.message = message;
		}

		@Override
		public String toString() {
			return new StringBuilder()
					.append(line)
					.append(script)
					.toString();
		}

		public String getScript() {
			return script;
		}

		public int getLine() {
			return line;
		}

		public String getData() {
			return data;
		}

		public String getMessage() {
			return message;
		}
	}
}
