package org.haedus.datatypes;

/**
 * Author: goats
 * Created: 11/23/2014
 */
public enum SegmentationMode {
	DEFAULT("Default"),
	NAIVE("NAIVE");

	private final String value;

	SegmentationMode(String value) {
		this.value = value;
	}
}
