package com.kd.ci.security.handler;

import java.io.IOException;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response,
			AccessDeniedException accessDeniedException) throws IOException, ServletException {

		String requestUri = request.getRequestURI();
		String username = request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : "anonymous";

		log.warn("[-] Access Denied，user: {}: {} ", username, requestUri);
		
		// 方式 A：轉跳到自訂 403 頁面（推薦給有前端頁面的專案）
		String contextPath = request.getContextPath();
		String redirectUrl = contextPath + "/access-denied";
		response.sendRedirect(redirectUrl);
		
		/**
		方式 B：如果是 REST API，返回 JSON（推薦混合使用）
		response.setStatus(HttpServletResponse.SC_FORBIDDEN);
		response.setContentType("application/json;charset=UTF-8");
		response.getWriter().write("{\"code\":403,\"message\":\"您沒有權限存取此功能\",\"path\":\""
		+ requestUri + "\"}");**/
	}
}
