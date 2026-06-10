package com.kd.ci.interfaces.controller.auth;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class AuthController {

	@GetMapping(value = {"/", "/login"})
	public String loginPage(HttpSession session, Model model) {
		String error = (String) session.getAttribute("loginError");
		String logout = (String) session.getAttribute("logoutMsg");
		
		if (error != null) {
			model.addAttribute("errorMsg", error);
			session.removeAttribute("loginError");
		}
		
		if (error != null) {
			model.addAttribute("logoutMsg", logout);
			session.removeAttribute("logoutMsg");
		}
		
		return "auth/login";
	}

	/** 登入後頁面 */
	@GetMapping({ "/dashboard" })
	public String dashboard(Authentication authentication, Model model) {
		return "dashboard/dashboard";
	}
	
	@GetMapping("/access-denied")
	public String accessDenied() {
		return "error/403";
	}
	
}
