package com.kd.ci.application.dto.menu;

public abstract class MenuTreeAdapter <T> implements MenuTree {
	
	private static final long serialVersionUID = 9108590239860295332L;
	
	protected final T source;

	public MenuTreeAdapter(T source) {
		this.source = source;
	}

	public T getSource() {
		return source;
	}
	
}
