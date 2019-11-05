package com.ephuvex.sw_sample.models;

import lombok.Data;

/**
 * A data object for storing two properties, one string and one integer.
 */
@Data
public class TwoPropObject {
	/**
	 * The first property, which is a string.
	 */
	private String prop1;

	/**
	 * The second property, which is an integer.
	 */
	private int prop2;
}
