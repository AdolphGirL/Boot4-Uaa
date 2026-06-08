package com.kd.ci.security.handler;

import java.io.IOException;

import org.jspecify.annotations.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {

	@Override
	public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
			@Nullable Authentication authentication) throws IOException, ServletException {
		
		if (authentication != null) {
			log.info("[+] User: {} logout success ", authentication.getName());
		}
		
		/** 清 session */
		request.getSession().invalidate();
		
		/** 放成功訊息 */
		request.getSession().setAttribute("logoutMsg", "您已成功登出");
		
		/** redirect */
		String contextPath = request.getContextPath();
		String redirectUrl = contextPath + "/login";
		
		response.sendRedirect(redirectUrl);
	}

}
