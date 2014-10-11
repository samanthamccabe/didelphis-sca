package org.haedus.io;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: Samantha Fiona Morrigan McCabe
 * Created: 10/11/2014
 */
public class DiskFileHandler implements FileHandler {

	private static final transient Logger LOGGER = LoggerFactory.getLogger(DiskFileHandler.class);

	private final String encoding;

	public DiskFileHandler(String encodingParam) {
		encoding = encodingParam;
	}

	public DiskFileHandler() {
		encoding = "UTF-8";
	}

	@Override
	public String readString(String path) {

		String data = "";
		try {
			data = FileUtils.readFileToString(new File(path), encoding);
		} catch (IOException e) {
			LOGGER.error("Error when reading from path \"{}\"!", path);
		}
		return data;
	}

	@Override
	public List<String> readLines(String path) {

		List<String> lines = new ArrayList<String>();
		try {
			lines.addAll(FileUtils.readLines(new File(path), encoding));
		} catch (IOException e) {
			LOGGER.error("Error when reading from path \"{}\"!", path);
		}
		return lines;
	}

	@Override
	public void writeString(String path, String data) {

	}

	@Override
	public void writeLines(String path, Iterable<String> data) {

	}
}
