package com.kd.ci.application.dto.menu;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import com.kd.ci.shared.util.core.Strings;

/** 抽象化選單節點 */
public interface MenuTree extends Serializable {
	
	/** 節點 ID */
	BigDecimal getId();

	/** 父節點 ID（可為 null） */
	BigDecimal getParentId();

	/** 顯示名稱 */
	String getTitle();

	/** 目標 URL */
	String getUrl();
	
	/** 子節點列表（遞迴結構） */
	List<MenuTree> getChildren();
	
	/** 開啟方式 target（可選） */
	default String getOrder() {
		return Strings.EMPTY;
	}
	
	/** 開啟方式 target（可選） */
	default String getTarget() {
		return Strings.EMPTY;
	}

	/** 額外參數（可選） */
	default String getParameters() {
		return Strings.EMPTY;
	}
	
	default boolean hasChildren() {
		return getChildren() != null && !getChildren().isEmpty();
	}
	
	default boolean isActive(String currentUrl) {
		if (currentUrl == null) return false;
		
		String menuUrl = getUrl();
		if (menuUrl != null && !menuUrl.isBlank() && currentUrl.startsWith(menuUrl)) return true;
		
		return getChildren() != null && getChildren().stream().anyMatch(c -> c.isActive(currentUrl));
	}
	
	default boolean isFolder() {
		String url = getUrl();
		return url == null || url.isBlank();
	}
}
