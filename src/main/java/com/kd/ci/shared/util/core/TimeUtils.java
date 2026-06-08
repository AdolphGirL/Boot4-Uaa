package com.kd.ci.shared.util.core;

import java.time.Duration;
import java.time.Instant;

public final class TimeUtils {

	private TimeUtils() {
	}

	public static StopWatch start() {
		return new StopWatch();
	}

	public static class StopWatch {

		private final Instant start = Instant.now();

		public long millis() {
			return Duration.between(start, Instant.now()).toMillis();
		}

	}

}
