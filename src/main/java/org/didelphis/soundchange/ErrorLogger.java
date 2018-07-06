/******************************************************************************
 * Copyright (c) 2016 Samantha Fiona McCabe                                   *
 *                                                                            *
 * This software is not licensed for any purpose                              *
 ******************************************************************************/

package org.didelphis.soundchange;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * @author Samantha Fiona McCabe
 * @date 2016-11-05
 */
@Data
public class ErrorLogger implements Iterable<ErrorLogger.Error> {

	private final Collection<Error> errors;

	public ErrorLogger() {
		errors = new ArrayList<>();
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
	public @NotNull Iterator<Error> iterator() {
		return errors.iterator();
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
			return line + script;
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
