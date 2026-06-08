package com.kd.ci.shared.util.core;

public final class Strings {

	public static final String EMPTY = "";

	private Strings() {
	}

	public static boolean isBlank(String s) {
		return s == null || s.isBlank();
	}
	
	public static boolean isNotBlank(String s) {
		return !isBlank(s);
	}

	public static boolean isEmpty(String s) {
		return s == null || s.isEmpty();
	}
	
	public static boolean isNotEmpty(String s) {
		return !isEmpty(s);
	}

	public static String defaultIfBlank(String s, String def) {
		return isBlank(s) ? def : s;
	}

	public static String defaultIfNull(String s, String def) {
		return s == null ? def : s;
	}

	public static String capitalize(String s) {

		if (isBlank(s))
			return s;

		return s.substring(0, 1).toUpperCase() + s.substring(1);
	}
}
