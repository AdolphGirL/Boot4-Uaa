package com.kd.ci.infrastructure.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import lombok.experimental.UtilityClass;

/** 審計日誌工具類，提供統一的審計日誌格式和接口。強制統一前綴/格式 **/
@UtilityClass
public class AuditLog {
	
	private static final Logger log = LoggerFactory.getLogger("audit");

	public static final Marker SUCCESS = MarkerFactory.getMarker("SUCCESS");
	public static final Marker FAIL = MarkerFactory.getMarker("FAIL");
	public static final Marker WARN_MARKER = MarkerFactory.getMarker("WARN");
	
	/** 自動抓 class/method (取代 %M/%L 開銷) */
	private static String getCallerInfo() {
		StackTraceElement[] stack = Thread.currentThread().getStackTrace();
		
		/** 深度調整: 0=Thread,1=getCallerInfo,2=success/fail,3=caller */
		if (stack != null && stack.length > 3) {
			StackTraceElement caller = stack[3];
			return caller.getClassName() + "." + caller.getMethodName();
		}
		return "unknown";
	}

	public static void success(String msg, Object... args) {
		String formatted = "[+] " + getCallerInfo() + " - " + msg;
		log.info(SUCCESS, formatted, args);
	}

	public static void success(String msg, Throwable t, Object... args) {
		String formatted = "[+] " + getCallerInfo() + " - " + msg;
		log.info(SUCCESS, formatted, args, t);
	}

	public static void fail(String msg, Object... args) {
		String formatted = "[-] " + getCallerInfo() + " - " + msg;
		log.error(FAIL, formatted, args);
	}

	public static void fail(String msg, Throwable t, Object... args) {
		String formatted = "[-] " + getCallerInfo() + " - " + msg;
		log.error(FAIL, formatted, args, t);
	}

	public static void warn(String msg, Object... args) {
		String formatted = "[-] " + getCallerInfo() + " - " + msg;
		log.warn(WARN_MARKER, formatted, args);
	}

}
