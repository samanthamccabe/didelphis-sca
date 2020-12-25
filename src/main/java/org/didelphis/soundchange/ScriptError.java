package org.didelphis.soundchange;

import lombok.NonNull;

import java.text.DecimalFormat;
import java.util.List;

@SuppressWarnings ("InstanceVariableMayNotBeInitialized")
public final class ScriptError {

	private static final char NW = '=';
	private static final char SW = '=';
	private static final char HR = '=';
	private static final char VR = '|';
	private static final char NJ = '=';
	private static final char SJ = '=';

	private static final int DEFAULT_DIVIDER_SIZE = 40;
	private static final int DEFAULT_CONTEXT_SIZE = 2;

	private int    lineNumber;
	private int    errorPosition;
	private int    errorLength;
	private String message;
	private String scriptName;

	private List<String> scriptData;

	private int dividerSize = DEFAULT_DIVIDER_SIZE;
	private int contextSize = DEFAULT_CONTEXT_SIZE;

	public ScriptError withMessage(String message) {
		this.message = message;
		return this;
	}

	public ScriptError withDividerSize(int dividerSize) {
		this.dividerSize = dividerSize;
		return this;
	}

	public ScriptError withContextSize(int contextSize) {
		this.contextSize = contextSize;
		return this;
	}

	public ScriptError withScriptName(String scriptName) {
		this.scriptName = scriptName;
		return this;
	}

	public ScriptError withLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
		return this;
	}

	public ScriptError withErrorPosition(int errorPosition) {
		this.errorPosition = errorPosition;
		return this;
	}

	public ScriptError withErrorLength(int errorLength) {
		this.errorLength = errorLength;
		return this;
	}

	public ScriptError withScripData(List<String> scriptData) {
		this.scriptData = scriptData;
		return this;
	}

	@Override
	public String toString() {
		return build();
	}

	public String build() {
		String divider = String.valueOf(HR).repeat(dividerSize);
		StringBuilder builder = new StringBuilder();
		builder.append(message).append("\n");
		builder.append(divider).append("\n");
		builder.append(scriptName).append("\n");
		builder.append(generateContext()).append("\n");
		return builder.toString();
	}

	private String generateContext() {
		int length = scriptData.size() - 1;

		int contextStart = Math.max(lineNumber - contextSize, 0);
		int contextEnd   = Math.min(lineNumber + contextSize, length);

		int numDigits = Math.max(
				String.valueOf(contextStart).length(),
				String.valueOf(contextEnd).length()
		);

		DecimalFormat format = new DecimalFormat("0".repeat(numDigits));

		int pos = 3 + numDigits;

		String horizontal = String.valueOf(HR);
		String topDivider = replaceChar(pos, NW + (horizontal.repeat(dividerSize - 1)), NJ);
		String lowDivider = replaceChar(pos, SW + (horizontal.repeat(dividerSize - 1)), SJ);

		StringBuilder builder = new StringBuilder();
		builder.append(topDivider);
		for (int i = contextStart; i <= contextEnd; i++) {
			builder.append("\n");
			builder.append(VR+" ");
			String linePrefix = format.format(i)
					+ " "+VR+" "
					+ (lineNumber == i ? "==>" : "   ")
					+ " ";

			builder.append(linePrefix);
			builder.append(scriptData.get(i));

			if (lineNumber == i && errorLength > 0) {
				builder.append("\n");
				builder.append(VR+" ");
				String linePref = " ".repeat(numDigits) + " "+VR+"     ";
				builder.append(linePref);
				builder.append(" ".repeat(errorPosition));
				builder.append("^".repeat(errorLength));
			}
		}
		builder.append("\n");
		builder.append(lowDivider);
		return builder.toString();
	}

	@NonNull
	private static String replaceChar(int position, String lit, char ch) {
		char[] chars = lit.toCharArray();
		chars[position] = ch;
		return String.copyValueOf(chars);
	}

}
