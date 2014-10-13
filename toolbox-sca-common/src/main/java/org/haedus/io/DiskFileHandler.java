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
		File file = new File(path);
		String data = "";
		try {
			data = FileUtils.readFileToString(file, encoding);
		} catch (IOException e) {
			LOGGER.error("Error when reading file {} from path \"{}\"!", path, file.getAbsolutePath());
		}
		return data;
	}

	@Override
	public List<String> readLines(String path) {
		File file = new File(path);
		List<String> lines = new ArrayList<String>();
		try {
			lines.addAll(FileUtils.readLines(file, encoding));
		} catch (IOException e) {
			LOGGER.error("Error when reading file {} from path \"{}\"!",path, file.getAbsolutePath());
		}
		return lines;
	}

	@Override
	public void writeString(String path, String data) {
		File file = new File(path);

		try {
			FileUtils.write(file, encoding, data);
		} catch (IOException e) {
			LOGGER.error("Error when writing to \"{}\"!", path);
		}
	}

	@Override
	public void writeLines(String path, List<String> data) {
		File file = new File(path);
		try {
			FileUtils.writeLines(file, encoding, data);
		} catch (IOException e) {
			LOGGER.error("Error when writing to \"{}\"!", path);
		}
	}
}
