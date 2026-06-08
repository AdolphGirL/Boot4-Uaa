package com.kd.ci.shared.util.core;

import java.util.concurrent.Callable;

public final class RetryUtils {

	private RetryUtils() {
	}

	public static <T> T retry(int attempts, Callable<T> task) {

		int count = 0;

		while (true) {

			try {
				return task.call();
			} catch (Exception e) {

				count++;

				if (count >= attempts)
					throw new RuntimeException(e);

				try {
					Thread.sleep(100L * count);
				} catch (InterruptedException ignored) {
				}
			}
		}
	}
}
