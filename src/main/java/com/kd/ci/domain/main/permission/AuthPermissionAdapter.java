package com.kd.ci.domain.main.permission;

/** 抽象化權限中介 */
public abstract class AuthPermissionAdapter<T> implements AuthPermission {

	private static final long serialVersionUID = 7949974540283147741L;
	
	protected final T source;

	public AuthPermissionAdapter(T source) {
		this.source = source;
	}

	public T getSource() {
		return source;
	}
}
