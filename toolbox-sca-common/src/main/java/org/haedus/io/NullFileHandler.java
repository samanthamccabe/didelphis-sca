package org.haedus.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Author: Samantha Fiona Morrigan McCabe
 * Created: 10/13/2014
 */
public class NullFileHandler implements FileHandler {

	private static final transient Logger LOGGER = LoggerFactory.getLogger(NullFileHandler.class);

	@Override
	public String readString(String path) {
		return null;
	}

	@Override
	public List<String> readLines(String path) {
		return null;
	}

	@Override
	public void writeString(String path, String data) {
		LOGGER.warn("Received data for path {} : {}", path, data);
	}

	@Override
	public void writeLines(String path, List<String> data) {
		LOGGER.warn("Received data for path {} : {}", path, data);
	}
}
