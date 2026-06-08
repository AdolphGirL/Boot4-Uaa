package com.kd.ci.shared.util.core;

/** RateLimiter（簡單版） */
public class RateLimiter {

	private final long capacity;
	private long tokens;
	private long lastRefill;

	public RateLimiter(long capacity) {

		this.capacity = capacity;
		this.tokens = capacity;
		this.lastRefill = System.currentTimeMillis();
	}

	public synchronized boolean tryAcquire() {

		long now = System.currentTimeMillis();

		if (now - lastRefill > 1000) {
			tokens = capacity;
			lastRefill = now;
		}

		if (tokens > 0) {
			tokens--;
			return true;
		}

		return false;
	}
}
