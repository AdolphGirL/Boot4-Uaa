package com.kd.ci.shared.util.core;

public final class EnumUtils {

	private EnumUtils() {
	}

	public static <E extends Enum<E>> E fromString(Class<E> enumClass, String name) {

		if (name == null)
			return null;

		try {
			return Enum.valueOf(enumClass, name);
		} catch (Exception e) {
			return null;
		}
	}

	public static <E extends Enum<E>> E fromStringIgnoreCase(Class<E> enumClass, String name) {

		if (name == null)
			return null;

		for (E e : enumClass.getEnumConstants()) {
			if (e.name().equalsIgnoreCase(name))
				return e;
		}

		return null;
	}
}
