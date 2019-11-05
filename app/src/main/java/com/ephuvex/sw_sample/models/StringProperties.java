package com.ephuvex.sw_sample.models;

import lombok.Data;

/**
 * Data object for storing the properties of a string, including the original value of the string,
 * whether or not the string is a palindrome, and the string reversed.
 */
@Data
public class StringProperties {
	/**
	 * The original value of the string.
	 */
	private String originalString;

	/**
	 * True if the string is a palindrome, false otherwise.
	 */
	private Boolean isPalindrome;

	/**
	 * The value of the string, but reversed.
	 */
	private String reversed;
}
