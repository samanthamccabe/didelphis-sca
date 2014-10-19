package org.haedus.io;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Author: Samantha Fiona Morrigan McCabe
 * Created: 10/13/2014
 * This mock handler simpler uses maps to simulate a crude file-system
 * The map is from path to data, so a test can instantiate the class
 * with this object, either providing it data, or reading from it;
 */
public class MockFileHandler implements FileHandler {

	private final Map<String, String> mockFileSystem;

	public MockFileHandler(Map<String, String> input) {
		mockFileSystem = input;
	}

	@Override
	public String readString(String path) {
		return mockFileSystem.get(path);
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
		mockFileSystem.put(path, data);
	}

	@Override
	public void writeLines(String path, List<String> data) {

		StringBuilder sb = new StringBuilder();
		for (String line : data) {
			sb.append(line).append("\n");
		}
		mockFileSystem.put(path, sb.toString());
	}
}
