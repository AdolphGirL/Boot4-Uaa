package com.kd.ci.infrastructure.interceptor;


import java.util.Collections;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.kd.ci.application.dto.menu.MenuTree;
import com.kd.ci.application.service.MenuService;
import com.kd.ci.domain.main.PersonnelUserDetails;
import com.kd.ci.security.authentication.SecurityUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 使用 Thymeleaf Layout Dialect 後， 所有功能頁面共用 layouts/main.html。 layouts/main.html
 * 
 * 裡面有 sidebar fragment，需要 menuTree 資料。 若每個 Controller 方法都要手動
 * model.addAttribute("menuTree",...) 會造成大量重複代碼。
 *
 * 解法： postHandle 在 Controller 執行完、View 渲染前被呼叫， 此時 ModelAndView 已存在，可統一注入
 * menuTree。 所有繼承 layouts/main.html 的頁面，自動擁有選單資料。
 *
 * 注意： - 只注入一次（檢查 model 中是否已有 menuTree） - 登入頁(auth/)與 redirect 不注入 -
 * 
 * 若取得選單失敗，靜默忽略，不影響頁面渲染
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MenuModelInterceptor implements HandlerInterceptor {

	private final MenuService menuApplicationService;
	
	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, 
			Object handler,
			ModelAndView modelAndView) {
		
		if (modelAndView == null) {
			return;
		}
		
		/** 順便替除登入頁面 */
		String viewName = modelAndView.getViewName();
		if (viewName == null || viewName.startsWith("redirect:") || viewName.startsWith("forward:")
				|| viewName.startsWith("auth/")) {
			return;
		}
		
		/** 已注入則跳過（避免 Controller 自行注入時被覆蓋） */
		if (modelAndView.getModel().containsKey("menuTree")) {
			return;
		}
		
		Authentication auth = SecurityUtils.getAuthentication();
		if (!isAuthenticated(auth)) {
			return;
		}
		
		PersonnelUserDetails userDetails = (PersonnelUserDetails) auth.getPrincipal();
		modelAndView.addObject("currentName", userDetails.getUsername());
		modelAndView.addObject("currentUnit", userDetails.getOrgunitName());
		
		modelAndView.addObject("currentUrl", request.getRequestURI());
		
		try {
			List<MenuTree> menuTrees = menuApplicationService.findMenuTreeForUserRole();
			modelAndView.addObject("menuTree", menuTrees);
		} catch (Exception e) {
			log.error("[-] MenuModelInterceptor，The menu failed to load，but the page still renders normally。Reason: {}", 
					e.getMessage());
			
			modelAndView.addObject("menuTree", Collections.emptyList());
		}
	}

	private boolean isAuthenticated(Authentication auth) {
		return auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())
				&& auth.getPrincipal() instanceof PersonnelUserDetails;
	}

}
