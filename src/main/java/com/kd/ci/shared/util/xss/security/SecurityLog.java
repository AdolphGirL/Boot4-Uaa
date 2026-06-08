package com.kd.ci.shared.util.xss.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.extern.slf4j.Slf4j;

/** SecurityLog 使用「獨立 logger name / appender」 */
/** sanitize → SecurityLog → logback → 再 sanitize → 無限遞迴 */
@Slf4j
public class SecurityLog {
	
	/** 獨立 security logger（避免走一般 sanitize 流程） */
	private static final Logger SECURITY_LOGGER = LoggerFactory.getLogger("SECURITY_LOG");

	private SecurityLog() {
	}

	public static void warn(String code, String msg) {
		SECURITY_LOGGER.warn("[-] SECURITY LOG xss defense，{} {} ", code, msg);
	}

	public static void warn(String code, String msg, Throwable t) {
		SECURITY_LOGGER.warn("[-] SECURITY LOG xss defense，{} {} ", code, msg, t);
	}

	public static void error(String code, Throwable t) {
		SECURITY_LOGGER.error("[-] SECURITY LOG xss defense，{} ", code, t);
	}
	
}
