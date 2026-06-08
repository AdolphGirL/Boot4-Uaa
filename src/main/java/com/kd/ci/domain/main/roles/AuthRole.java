package com.kd.ci.domain.main.roles;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Set;

import com.kd.ci.domain.main.permission.AuthPermission;
import com.kd.ci.shared.util.core.Strings;

/** 抽象化角色 */
public interface AuthRole extends Serializable {
	
	BigDecimal getRoleId();
	
	default String getRoleCode() {
		return Strings.EMPTY;
	}
	
	default String getRoleName() {
		return Strings.EMPTY;
	}
	
	default Set<AuthPermission> getGrantedPermissions() {
		return Set.of();
	};
	
	default String getDescription() {
		return Strings.EMPTY;
	}
	
}
