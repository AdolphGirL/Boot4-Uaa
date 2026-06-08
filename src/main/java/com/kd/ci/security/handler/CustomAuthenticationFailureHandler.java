package com.kd.ci.security.handler;

import java.io.IOException;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/** 自訂義登入錯誤處理方式，替代原先，直接用 query string（Spring 預設也這樣） response.sendRedirect("/login?error"); */
/** @Component (後續可以改，如果有需要注入) **/
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, 
			HttpServletResponse response,
			AuthenticationException exception) throws IOException, ServletException {
		
		/** 可以擴充，紀錄登入錯誤次數等等，計數 + 判斷鎖定 + 自訂訊息 */
		
		String errorMessage;

		if (exception instanceof BadCredentialsException) {
			errorMessage = "帳號或密碼錯誤";
		} else if (exception instanceof LockedException) {
			errorMessage = "帳號已被鎖定，請聯絡管理員";
		} else if (exception instanceof DisabledException) {
			errorMessage = "帳號已被停用";
		} else if (exception instanceof CredentialsExpiredException) {
			errorMessage = "密碼已過期，請重設密碼";
		} else {
			errorMessage = "登入失敗：" + exception.getMessage();
		}
		
		/** 方式一：放進 session 當 flash（最常用於 SSR + Thymeleaf） */
		request.getSession().setAttribute("loginError", errorMessage);
		
		/** 方式二：使用 RedirectAttributes（如果有 Spring MVC 的 RedirectAttributes 可用，但 handler 層較少直接拿）
		 * 通常直接用 session 屬性 + 登入頁面讀取即可
		 * */
		
		/** 最後重導向登入頁（不會帶 query string，看起來更乾淨），防止context-path 消失，給出較完整的URL */
		String contextPath = request.getContextPath();
		String redirectUrl = contextPath + "/login";
		
		response.sendRedirect(redirectUrl);
	}

}
