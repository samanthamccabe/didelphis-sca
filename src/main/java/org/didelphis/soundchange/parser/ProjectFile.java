package org.didelphis.soundchange.parser;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

/**
 * Class {@code FileImport}
 */
@Data
@FieldDefaults (level = AccessLevel.PRIVATE)
public class ProjectFile {
	
	FileType fileType;
	String   fileData;
	String   fileName;
	String   absolutePath;
	String   relativePath;
}
