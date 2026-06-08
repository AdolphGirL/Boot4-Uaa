package com.kd.ci.shared.util.core;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public final class CollectionUtils {

	private CollectionUtils() {
	}

	public static boolean isEmpty(Collection<?> c) {
		return c == null || c.isEmpty();
	}

	public static boolean isNotEmpty(Collection<?> c) {
		return !isEmpty(c);
	}

	public static <T> List<T> emptyIfNull(List<T> list) {
		return list == null ? List.of() : list;
	}

	public static <T> Set<T> emptyIfNull(Set<T> set) {
		return set == null ? Set.of() : set;
	}
}
