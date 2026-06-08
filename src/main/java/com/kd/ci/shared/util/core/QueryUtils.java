package com.kd.ci.shared.util.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class QueryUtils {

	private QueryUtils() {
	}

	public static <T> Query<T> from(Collection<T> source) {
		return new Query<>(source);
	}

	public static class Query<T> {

		private final Collection<T> source;
		private final List<Predicate<T>> predicates = new ArrayList<>();

		Query(Collection<T> source) {
			this.source = source == null ? List.of() : source;
		}

		public <R> Condition<T, R> where(Function<T, R> extractor) {
			return new Condition<>(this, extractor);
		}

		void add(Predicate<T> predicate) {
			predicates.add(predicate);
		}

		public List<T> list() {

			return source.stream().filter(this::test).collect(Collectors.toList());
		}

		public Optional<T> first() {

			return source.stream().filter(this::test).findFirst();
		}

		public long count() {

			return source.stream().filter(this::test).count();
		}

		public boolean any() {

			return source.stream().anyMatch(this::test);
		}

		private boolean test(T value) {

			for (Predicate<T> p : predicates) {
				if (!p.test(value))
					return false;
			}

			return true;
		}
	}

	public static class Condition<T, R> {

		private final Query<T> query;
		private final Function<T, R> extractor;

		Condition(Query<T> query, Function<T, R> extractor) {
			this.query = query;
			this.extractor = extractor;
		}

		public Query<T> isNull() {
			query.add(t -> extractor.apply(t) == null);
			return query;
		}

		public Query<T> notNull() {
			query.add(t -> extractor.apply(t) != null);
			return query;
		}

		public Query<T> eq(R value) {
			query.add(t -> Objects.equals(extractor.apply(t), value));
			return query;
		}

		public Query<T> notEq(R value) {
			query.add(t -> !Objects.equals(extractor.apply(t), value));
			return query;
		}

		public Query<T> in(Collection<R> values) {
			query.add(t -> values.contains(extractor.apply(t)));
			return query;
		}

		public Query<T> notBlank() {

			query.add(t -> {
				Object v = extractor.apply(t);
				return v instanceof String s && !s.isBlank();
			});

			return query;
		}

		@SuppressWarnings("unchecked")
		public Query<T> gt(R value) {

			query.add(t -> {

				R v = extractor.apply(t);

				if (v == null)
					return false;

				return ((Comparable<R>) v).compareTo(value) > 0;
			});

			return query;
		}

		@SuppressWarnings("unchecked")
		public Query<T> lt(R value) {

			query.add(t -> {

				R v = extractor.apply(t);

				if (v == null)
					return false;

				return ((Comparable<R>) v).compareTo(value) < 0;
			});

			return query;
		}
	}
}
