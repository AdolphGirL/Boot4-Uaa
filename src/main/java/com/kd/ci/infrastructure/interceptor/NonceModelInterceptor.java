package com.kd.ci.infrastructure.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class NonceModelInterceptor implements HandlerInterceptor {
	
	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		
		if (modelAndView == null) {
			return;
		}
		
		/** 強制覆蓋，確保 nonce 存在 */
		String nonce = (String) request.getAttribute("cspNonce");
		if (nonce != null && !nonce.isBlank()) {
			modelAndView.addObject("cspNonce", nonce);
		}
	}

}
