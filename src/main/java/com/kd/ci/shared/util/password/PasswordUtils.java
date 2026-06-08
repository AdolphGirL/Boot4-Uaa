package com.kd.ci.shared.util.password;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class PasswordUtils {
	
	private static final SecureRandom RANDOM = new SecureRandom();
	
	private static final char[] CHARSET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@*".toCharArray();
	
	/**
	 * 產生指定長度的亂數密碼（使用 SecureRandom）
	 */
	public static String generateRandomPassword(int length) {
		if (length <= 0) {
			throw new IllegalArgumentException("length must be > 0");
		}
		
		StringBuilder sb = new StringBuilder(length);
		for (int i = 0; i < length; i++) {
			int idx = RANDOM.nextInt(CHARSET.length);
			sb.append(CHARSET[idx]);
		}
		
		return sb.toString();
	}

	/**
	 * 對輸入字串做 SHA-256 雜湊，並回傳小寫十六進位表示
	 */
	public static String sha256Hex(String input) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] digest = md.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
			return bytesToHex(digest);
		} catch (NoSuchAlgorithmException e) {
			/** SHA-256 一定存在；若發生異常則轉成 RuntimeException */
			throw new RuntimeException(e);
		}
	}

	private static String bytesToHex(byte[] bytes) {
		StringBuilder sb = new StringBuilder(bytes.length * 2);
		for (byte b : bytes) {
			// 處理成兩位十六進位（包含前導 0）
			int v = b & 0xFF;
			if (v < 16)
				sb.append('0');
			sb.append(Integer.toHexString(v));
		}
		return sb.toString();
	}

	/**
	 * 產生一組 length 長度的隨機密碼，並回傳 PlainText 與 SHA-256 雜湊值
	 */
	public static PasswordResult generateAndHash(int length) {
		String plain = generateRandomPassword(length);
		String hash = sha256Hex(plain);
		return new PasswordResult(plain, hash);
	}

	public static class PasswordResult {
		
		private final String plain;
		private final String sha256;

		public PasswordResult(String plain, String sha256) {
			this.plain = plain;
			this.sha256 = sha256;
		}

		public String getPlain() {
			return plain;
		}

		public String getSha256() {
			return sha256;
		}
	}

	/** 範例 main
	public static void main(String[] args) {
		PasswordResult r = generateAndHash(12);
		System.out.println("Plain:  " + r.getPlain());
		System.out.println("SHA256: " + r.getSha256());
	}
	*/
}
