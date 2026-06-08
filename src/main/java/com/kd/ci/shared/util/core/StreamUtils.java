package com.kd.ci.shared.util.core;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class StreamUtils {

	private StreamUtils() {
	}
	
	public static <T> Stream<T> stream(Collection<T> collection) {

		if (collection == null)
			return Stream.empty();

		return collection.stream();
	}

	public static <T> Stream<T> stream(T[] array) {

		if (array == null)
			return Stream.empty();

		return Arrays.stream(array);
	}

	public static <T> Stream<T> filterNotNull(Stream<T> stream) {
		return stream.filter(Objects::nonNull);
	}

	public static <T, R> Stream<R> mapNotNull(Stream<T> stream, Function<T, R> mapper) {

		return stream.map(mapper).filter(Objects::nonNull);
	}

	public static <T> List<T> toList(Stream<T> stream) {

		if (stream == null)
			return List.of();

		return stream.toList();
	}
	
	public static <T> Set<T> toSet(Stream<T> stream) {

		if (stream == null)
			return Set.of();

		return stream.collect(Collectors.toSet());
	}
	
	public static <T, K, V> Map<K, V> toMap(Stream<T> stream, Function<T, K> keyMapper, Function<T, V> valueMapper) {
		return stream.collect(Collectors.toMap(keyMapper, valueMapper, (a, b) -> b));
	}

	public static <T, K> Map<K, List<T>> groupBy(Stream<T> stream, Function<T, K> classifier) {

		return stream.collect(Collectors.groupingBy(classifier));
	}

	public static <T, K> Map<K, T> index(Collection<T> list, Function<T, K> keyMapper) {

		if (list == null)
			return Map.of();

		return list.stream().collect(Collectors.toMap(keyMapper, Function.identity(), (a, b) -> b));
	}
	
	public static <T> Predicate<T> distinctByKey(Function<T, ?> keyExtractor) {

		Set<Object> seen = ConcurrentHashMap.newKeySet();

		return t -> seen.add(keyExtractor.apply(t));
	}
}
