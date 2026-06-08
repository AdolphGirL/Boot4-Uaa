package com.kd.ci.shared.util.core;

import java.util.function.Function;
import java.util.function.Predicate;

public final class PredicateUtils {

	private PredicateUtils() {
	}

	public static <T, R> Predicate<T> notNull(Function<T, R> extractor) {
		return t -> extractor.apply(t) != null;
	}

	public static <T> Predicate<T> notBlank(Function<T, String> extractor) {

		return t -> {
			String v = extractor.apply(t);
			return v != null && !v.isBlank();
		};
	}

	public static <T> Predicate<T> greaterThanZero(Function<T, Number> extractor) {

		return t -> {
			Number v = extractor.apply(t);
			return v != null && v.doubleValue() > 0;
		};
	}

}
