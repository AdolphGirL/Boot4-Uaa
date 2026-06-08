package com.kd.ci.application.dto.adapter;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.kd.ci.application.dto.menu.MenuTree;
import com.kd.ci.application.dto.menu.MenuTreeAdapter;
import com.kd.ci.domain.first.Menuitem;
import com.kd.ci.shared.util.core.Strings;

public class MenuTreeDto extends MenuTreeAdapter<Menuitem> {

	public MenuTreeDto(Menuitem source) {
		super(source);
	}

	private static final long serialVersionUID = 4585669481884895503L;

	@Override
	public BigDecimal getId() {
		return source.getId();
	}

	@Override
	public BigDecimal getParentId() {
		return source.getParentId();
	}

	@Override
	public String getTitle() {
		return source.getTitle();
	}

	@Override
	public String getUrl() {
		return Strings.isNotEmpty(source.getLocation()) ? source.getLocation() : Strings.EMPTY;
	}

	@Override
	public List<MenuTree> getChildren() {
		if (source.getChildren() == null || source.getChildren().isEmpty()) {
			return Collections.emptyList();
		}
		
		return source.getChildren().stream()
				.map(MenuTreeDto::new)
				.collect(Collectors.toList());
	}

}
