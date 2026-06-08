package com.kd.ci.shared.util.core;

import java.util.UUID;

public final class IdUtils {

	private IdUtils() {
	}

	public static String uuid() {
		return UUID.randomUUID()
			.toString().replace("-", "");
	}
	
}
