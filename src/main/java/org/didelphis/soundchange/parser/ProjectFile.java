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
	String   filePath;
	String   fileData;
	
	
}
