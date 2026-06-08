package com.kd.ci.domain.main.permission;

import java.io.Serializable;
import java.math.BigDecimal;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.kd.ci.shared.util.core.Strings;

public interface AuthPermission extends Serializable {
	
	BigDecimal getPermissionId();

	default String getPermissionCode() {
		return Strings.EMPTY;
	}
	
	default String getDescription() {
		return Strings.EMPTY;
	}
	
	default GrantedAuthority toAuthority() {
		String code = getPermissionCode();
		if (Strings.isBlank(code)) {
			return null;
		}
		return new SimpleGrantedAuthority(code);
	}
	
}
