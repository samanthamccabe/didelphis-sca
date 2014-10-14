package org.haedus.io;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Author: Samantha Fiona Morrigan McCabe
 * Created: 10/13/2014
 */
public class MockFileHandler implements FileHandler {

	private final Map<String, String> mockInput;
	private final Map<String, String> mockOutput;

	public MockFileHandler(Map<String, String> input, Map<String,String> output) {
		mockInput  = input;
		mockOutput = output;
	}

	@Override
	public String readString(String path) {
		return mockInput.get(path);
	}

	@Override
	public List<String> readLines(String path) {
		String data = readString(path);

		List<String> list = new ArrayList<String>();
		Collections.addAll(list, data.split("\r?\n"));
		return list;
	}

	@Override
	public void writeString(String path, String data) {
		mockOutput.put(path, data);
	}

	@Override
	public void writeLines(String path, List<String> data) {

		StringBuilder sb = new StringBuilder();
		for (String line : data) {
			sb.append(line).append("\n");
		}
		mockOutput.put(path, sb.toString());
	}
}
