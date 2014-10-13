package org.haedus.io;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: Samantha Fiona Morrigan McCabe
 * Created: 10/11/2014
 */
public class ClassPathFileHandler implements FileHandler {

	private static final transient Logger LOGGER = LoggerFactory.getLogger(ClassPathFileHandler.class);

	private final String encoding;

	public ClassPathFileHandler(String encodingParam) {
		encoding = encodingParam;
	}

	public ClassPathFileHandler() {
		encoding = "UTF-8";
	}

	@Override
	public String readString(String path) {

		List<String> lines = readLines(path);

		StringBuilder builder = new StringBuilder();
		for (String line : lines) {
			builder.append(line);
			builder.append("\n");
		}
		return builder.toString();
	}

	@Override
	public List<String> readLines(String path) {

		List<String> lines = new ArrayList<String>();

		Resource resource = new ClassPathResource(path);
		try {
			InputStream inputStream = resource.getInputStream();

			lines.addAll(IOUtils.readLines(inputStream, encoding));
		} catch (IOException e) {
			LOGGER.error("Error when reading from path \"{}\"!", path);
		}

		return lines;
	}

	@Override
	public void writeString(String path, String data) {
		File file = new File(path);
		try {
			LOGGER.info("Writing to working directory at {}", file.getAbsolutePath());
			FileUtils.write(file, encoding, data);
		} catch (IOException e) {
			LOGGER.error("Error when writing to \"{}\"!", path);
		}
	}

	@Override
	public void writeLines(String path, List<String> data) {
		File file = new File(path);
		try {
			LOGGER.info("Writing to working directory at {}", file.getAbsolutePath());
			FileUtils.writeLines(file, encoding, data);
		} catch (IOException e) {
			LOGGER.error("Error when writing to \"{}\"!", path);
		}
	}
}
