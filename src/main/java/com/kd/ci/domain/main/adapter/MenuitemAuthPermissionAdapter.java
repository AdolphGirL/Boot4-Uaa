package com.kd.ci.domain.main.adapter;

import java.math.BigDecimal;
import java.util.Optional;

import com.kd.ci.domain.first.Menuitem;
import com.kd.ci.domain.main.permission.AuthPermissionAdapter;
import com.kd.ci.shared.util.core.Strings;

public class MenuitemAuthPermissionAdapter extends AuthPermissionAdapter<Menuitem> {
	
	private static final long serialVersionUID = 6703608708475450179L;

	public MenuitemAuthPermissionAdapter(Menuitem source) {
		super(source);
	}

	@Override
	public BigDecimal getPermissionId() {
		return Optional.ofNullable(this.source)
				.map(Menuitem::getId)
				.orElse(BigDecimal.ZERO);
	}

	@Override
	public String getPermissionCode() {
		return Optional.ofNullable(this.source)
				.map(Menuitem::getLocation)
				.filter(Strings::isNotBlank)
				.orElse(Strings.EMPTY);
	}
	
}
