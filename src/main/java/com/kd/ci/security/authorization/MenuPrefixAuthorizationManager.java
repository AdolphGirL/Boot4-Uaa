package com.kd.ci.security.authorization;

import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class MenuPrefixAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

	@Override
	public @Nullable AuthorizationResult authorize(Supplier<? extends @Nullable Authentication> authentication,
			RequestAuthorizationContext context) {

		HttpServletRequest request = context.getRequest();
		String uri = request.getRequestURI();						/** 不含 context-path 的相對路徑 */
		String method = request.getMethod();						/** 可選：未來可依 HTTP Method 細分 */
		
		Authentication auth = authentication.get();
		if (auth == null || !auth.isAuthenticated()) {
			return new AuthorizationDecision(false);
		}
		
		/** 使用者擁有的所有 location (authority) */
		/** 就是 Menuitem.getLocation() */
		for (GrantedAuthority ga : auth.getAuthorities()) {
			String location = ga.getAuthority();
			if (location != null && (uri.equals(location) || uri.startsWith(location + "/"))) {
				/** 可加 log 方便除錯 */
				/** log.info("URI {} matched by location prefix: {}", uri, location); */
				return new AuthorizationDecision(true);
			}
		}
		
		return new AuthorizationDecision(false);
	}

}
