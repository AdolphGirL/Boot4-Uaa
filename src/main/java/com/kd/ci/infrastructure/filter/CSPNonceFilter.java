package com.kd.ci.infrastructure.filter;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CSPNonceFilter extends OncePerRequestFilter {

	private static final int NONCE_SIZE = 32;
	private final SecureRandom secureRandom = new SecureRandom();

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		
		/** 生成 nonce */
		byte[] nonceBytes = new byte[NONCE_SIZE];
		secureRandom.nextBytes(nonceBytes);
		
		/** 可選：移除 = 讓它更乾淨 */
		String nonce = Base64.getEncoder().encodeToString(nonceBytes).replace("=", "");

		/** 給 Thymeleaf / <script nonce="${cspNonce}"> 使用 */
		request.setAttribute("cspNonce", nonce);
		
		/** 使用自訂 Wrapper 來攔截並替換 CSP header */
		CSPNonceResponseWrapper wrappedResponse = new CSPNonceResponseWrapper(response, nonce);

		try {
			filterChain.doFilter(request, wrappedResponse);
		} finally {
			// 如果有需要緩存 body 的需求，再包一層 ContentCachingResponseWrapper
			// 但大多數情況下不需要在這裡 copy（除非你要 log body）
			wrappedResponse.copyBodyToResponseIfNeeded();
		}
	}
	
	/** 內部 Wrapper 類別 */
	private static class CSPNonceResponseWrapper extends HttpServletResponseWrapper {

		private final String nonce;

		public CSPNonceResponseWrapper(HttpServletResponse response, String nonce) {
			super(response);
			this.nonce = nonce;
		}

		@Override
		public void setHeader(String name, String value) {
			if ("Content-Security-Policy".equalsIgnoreCase(name) && value != null) {
				String finalCsp = value.replace("{nonce}", nonce);
				super.setHeader(name, finalCsp);
				return;
			}
			super.setHeader(name, value);
		}

		@Override
		public void addHeader(String name, String value) {
			if ("Content-Security-Policy".equalsIgnoreCase(name) && value != null) {
				String finalCsp = value.replace("{nonce}", nonce);
				super.addHeader(name, finalCsp);
				return;
			}
			super.addHeader(name, value);
		}
		
		/** 如果你原本需要緩存 body，可以在這裡處理 */
		public void copyBodyToResponseIfNeeded() {
			/** 若不需要 body caching，可留空或只呼叫 super 的 flush */
		}
	}

}
