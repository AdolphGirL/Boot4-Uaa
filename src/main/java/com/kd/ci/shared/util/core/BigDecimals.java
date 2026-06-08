package com.kd.ci.shared.util.core;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class BigDecimals {

	public static final BigDecimal ZERO = BigDecimal.ZERO;

	private BigDecimals() {
	}

	public static BigDecimal of(String val) {

		if (val == null || val.isBlank())
			return ZERO;

		return new BigDecimal(val);
	}

	public static BigDecimal add(BigDecimal a, BigDecimal b) {
		return defaultIfNull(a).add(defaultIfNull(b));
	}

	public static BigDecimal subtract(BigDecimal a, BigDecimal b) {
		return defaultIfNull(a).subtract(defaultIfNull(b));
	}

	public static BigDecimal multiply(BigDecimal a, BigDecimal b) {
		return defaultIfNull(a).multiply(defaultIfNull(b));
	}

	public static BigDecimal divide(BigDecimal a, BigDecimal b, int scale) {

		if (b == null || b.compareTo(ZERO) == 0)
			return ZERO;

		return defaultIfNull(a).divide(b, scale, RoundingMode.HALF_UP);
	}

	public static BigDecimal defaultIfNull(BigDecimal v) {
		return v == null ? ZERO : v;
	}

}
