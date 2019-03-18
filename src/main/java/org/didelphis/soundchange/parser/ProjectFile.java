package org.didelphis.soundchange.parser;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Class {@code FileImport}
 */
@Data
@FieldDefaults (level = AccessLevel.PRIVATE)
public class ProjectFile {

	FileType fileType;

	String fileData;
	String fileName;
	String relativePath;

	List<ProjectFile> children;

	public List<ProjectFile> getChildren() {
		return children != null ? children : Collections.emptyList();
	}

	public boolean hasChildren() {
		return children != null && !children.isEmpty();
	}

	public boolean addChild(@NonNull ProjectFile projectFile) {
		if (children == null) {
			children = new ArrayList<>();
		}

		return children.add(projectFile);
	}
}
