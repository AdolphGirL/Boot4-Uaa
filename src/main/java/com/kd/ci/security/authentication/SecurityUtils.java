package com.kd.ci.security.authentication;

import java.util.Set;

import org.jspecify.annotations.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.kd.ci.domain.first.OrgunitWithBelong;
import com.kd.ci.domain.first.Personnel;
import com.kd.ci.domain.main.PersonnelUserDetails;
import com.kd.ci.domain.main.roles.AuthRole;
import com.kd.ci.shared.util.core.Strings;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class SecurityUtils {
	
	/** 工具類不允許實例化 */
	private SecurityUtils() {
	}
	
	public static Authentication getAuthentication() {
		return SecurityContextHolder.getContext().getAuthentication();
	}
	
	/**
	 * 取得當前登入的 PersonnelUserDetails 加入完整空值與類型檢查保護
	 */
	public static PersonnelUserDetails getCurrentUserDetails() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null) {
			log.warn("[-] getCurrentUserDetails，There is no Authentication object in the SecurityContext ");
			throw new IllegalStateException("There are currently no logged-in users (Authentication is null) ");
		}

		Object principal = authentication.getPrincipal();

		if (principal == null) {
			log.warn("[-] getCurrentUserDetails，Authentication.principal is null ");
			throw new IllegalStateException("There are currently no logged-in users (Principal is null) ");
		}

		/** 防止有人誤用匿名使用者或其它 UserDetails 實作 */
		if (!(principal instanceof PersonnelUserDetails)) {
			if ("anonymousUser".equals(principal)) {
				throw new IllegalStateException("目前為匿名使用者，未登入");
			}
			
			log.warn("[-] getCurrentUserDetails，Principal type mismatch，actual type: {} ", principal.getClass().getName());
			throw new IllegalStateException(
					"Principal 類型錯誤，預期 PersonnelUserDetails，實際為: " + principal.getClass().getSimpleName());
		}

		return (PersonnelUserDetails) principal;
	}

	/**
	 * 取得當前登入的使用者基本資料（最常用）
	 */
	public static Personnel getCurrentPersonnel() {
		return getCurrentUserDetails().getPersonnel();
	}

	/**
	 * 取得當前登入者的組織單位資訊
	 */
	public static OrgunitWithBelong getCurrentOrgunit() {
		return getCurrentUserDetails().getOrgunit();
	}

	/**
	 * 取得當前登入者的所有角色
	 */
	public static Set<AuthRole> getCurrentRoles() {
		return getCurrentUserDetails().getRoles();
	}

	/**
	 * 取得當前使用者帳號 (idnum)
	 */
	@Nullable
	public static String getCurrentUsername() {
		try {
			return getCurrentUserDetails().getUsername();
		} catch (IllegalStateException e) {
			return Strings.EMPTY;
		}
	}

	/**
	 * 取得當前登入者的組織單位名稱（安全版）
	 */
	@Nullable
	public static String getCurrentOrgunitName() {
		try {
			OrgunitWithBelong orgunit = getCurrentOrgunit();
			return orgunit != null ? orgunit.getShortname() : null;
		} catch (IllegalStateException e) {
			return Strings.EMPTY;
		}
	}

	/**
	 * 取得當前登入者的組織代碼（安全版）
	 */
	@Nullable
	public static String getCurrentOrgCode() {
		try {
			OrgunitWithBelong orgunit = getCurrentOrgunit();
			return orgunit != null ? orgunit.getOrgcode() : null;
		} catch (IllegalStateException e) {
			return Strings.EMPTY;
		}
	}

	/**
	 * 判斷目前是否有使用者登入
	 */
	public static boolean isAuthenticated() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		return authentication != null && authentication.isAuthenticated()
				/** 排除 anonymousUser */
				&& !(authentication.getPrincipal() instanceof String);
	}

	/**
	 * 判斷當前使用者是否擁有某個權限
	 */
	public static boolean hasAuthority(String authority) {
		if (!isAuthenticated() || authority == null) {
			return false;
		}
		try {
			return getCurrentUserDetails().getAuthorities().stream()
					.anyMatch(a -> authority.equals(a.getAuthority()));
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * 判斷當前使用者是否擁有任一權限（支援多個）
	 */
	public static boolean hasAnyAuthority(String... authorities) {
		if (!isAuthenticated() || authorities == null) {
			return false;
		}
		try {
			return getCurrentUserDetails().getAuthorities().stream().anyMatch(a -> {
				String auth = a.getAuthority();
				for (String target : authorities) {
					if (target != null && target.equals(auth)) {
						return true;
					}
				}
				return false;
			});
		} catch (Exception e) {
			return false;
		}
	}
}
