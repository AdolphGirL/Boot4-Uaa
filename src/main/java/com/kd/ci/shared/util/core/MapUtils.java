package com.kd.ci.shared.util.core;

import java.util.Map;

public final class MapUtils {

	private MapUtils() {
	}

	public static boolean isEmpty(Map<?, ?> map) {
		return map == null || map.isEmpty();
	}

	public static boolean isNotEmpty(Map<?, ?> map) {
		return !isEmpty(map);
	}

	public static <K, V> V getOrDefault(Map<K, V> map, K key, V def) {

		if (map == null)
			return def;

		return map.getOrDefault(key, def);
	}
}
