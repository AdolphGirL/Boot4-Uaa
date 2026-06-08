package com.kd.ci.domain.main.adapter;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;

import com.kd.ci.domain.first.Act;
import com.kd.ci.domain.main.permission.AuthPermission;
import com.kd.ci.domain.main.roles.AuthRoleAdapter;

public class ActAuthRoleAdapter extends AuthRoleAdapter<Act> {
	
	private final Set<AuthPermission> grantedPermissions;
	
	public ActAuthRoleAdapter(Act source, Set<AuthPermission> grantedPermissions) {
		super(source);
		this.grantedPermissions = grantedPermissions != null ? grantedPermissions : Set.of();
	}

	private static final long serialVersionUID = 1L;

	@Override
	public BigDecimal getRoleId() {
		return Optional.ofNullable(this.source)
				.map(Act::getId)
				.orElse(BigDecimal.ZERO);
	}
	
	@Override
	public Set<AuthPermission> getGrantedPermissions() {
		return this.grantedPermissions;
	}
	
}
