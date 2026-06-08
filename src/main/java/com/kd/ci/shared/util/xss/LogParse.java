package com.kd.ci.shared.util.xss;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.regex.Pattern;

import com.kd.ci.shared.util.xss.security.SecurityLog;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

public class LogParse extends ClassicConverter {
	
	private static final int MAX_SECURITY_LOG_LENGTH = 300;
	
	/** 控制字元（CR, LF, TAB, Unicode line separators） */
	private static final Pattern CONTROL_CHARS = Pattern.compile("[\\r\\n\\t\\u2028\\u2029]");
	
	/** ASCII control chars (0x00–0x1F, 0x7F) */
	private static final Pattern ASCII_CONTROL = Pattern.compile("[\\x00-\\x1F\\x7F]");

	private static final int MAX_LOG_LENGTH = 2000;

	public static String sanitize(String value) {
		if (value == null || value.isEmpty()) {
			return "";
		}
		
		String original = value;
		String sanitized = value;
		
		/** Unicode 正規化（防混淆字元） */
		sanitized = Normalizer.normalize(sanitized, Normalizer.Form.NFKC);
		
		/** URL Decode（一次，避免 %0a / %0d / %250a），URL decode 只在「疑似編碼」時才做 */
		try {
			if (sanitized.contains("%")) {
				sanitized = URLDecoder.decode(sanitized, StandardCharsets.UTF_8.name());
			}
		} catch (IllegalArgumentException e) {
			/** Detect-only：異常 URL encoding */
			SecurityLog.warn("LOG_SANITIZE_URL_DECODE_FAIL", "value=" + truncateForLog(original), e);
			/** 降級：維持未 decode 的字串 */
			sanitized = original;
		} catch (Exception e) {
			/** 極端防禦，理論上不該發生 */
			SecurityLog.error("LOG_SANITIZE_UNEXPECTED_ERROR", e);
			return "[LOG_SANITIZE_ERROR]";
		}
		
		/** 移除 CRLF / Unicode Line Separator */
		sanitized = CONTROL_CHARS.matcher(sanitized).replaceAll(" ");
		
		/** 移除其他不可見控制字元 */
		sanitized = ASCII_CONTROL.matcher(sanitized).replaceAll("");
		
		/** 壓縮空白 */
		sanitized = sanitized.replaceAll("\\s{2,}", " ").trim();
		
		/** 長度限制（防止 log flooding） */
		if (sanitized.length() > MAX_LOG_LENGTH) {
			sanitized = sanitized.substring(0, MAX_LOG_LENGTH) + "...(truncated)";
		}

		return sanitized;
	}

	@Override
	public String convert(ILoggingEvent event) {
		try {
			return sanitize(event.getFormattedMessage());
		} catch (Exception e) {
			SecurityLog.error("LOG_CONVERT_FATAL", e);
			return "[LOG_CONVERT_ERROR]";
		}
	}
	
	public static String truncateForLog(String value) {
		if (value == null) {
			return "null";
		}
		
		/** 移除 CRLF，避免影響 security log 結構 */
		String result = value.replace("\r", "").replace("\n", "");
		
		/** 長度限制 */
		if (result.length() > MAX_SECURITY_LOG_LENGTH) {
			result = result.substring(0, MAX_SECURITY_LOG_LENGTH) + "...(truncated)";
		}
		
		return result;
	}
	
}
