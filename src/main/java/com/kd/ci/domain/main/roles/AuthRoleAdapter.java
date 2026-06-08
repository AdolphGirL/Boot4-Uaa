package com.kd.ci.domain.main.roles;

/** 抽象化角色中介 */
public abstract class AuthRoleAdapter<T> implements AuthRole {
	
	private static final long serialVersionUID = -5224859408145535361L;
	
	protected final T source;

	public AuthRoleAdapter(T source) {
		this.source = source;
	}

	public T getSource() {
		return source;
	}
}
