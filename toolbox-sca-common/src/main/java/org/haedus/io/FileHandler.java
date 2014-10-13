package org.haedus.io;

import java.util.List;

/**
 * Author: Samantha Fiona Morrigan McCabe
 * Created: 10/11/2014
 */
public interface FileHandler {

	String readString(String path);

	List<String> readLines(String path);

	void writeString(String path, String data);

	void writeLines(String path, List<String> data);
}
