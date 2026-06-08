package com.kd.ci.application.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.kd.ci.application.dto.adapter.MenuTreeDto;
import com.kd.ci.application.dto.menu.MenuTree;
import com.kd.ci.domain.first.Menuitem;
import com.kd.ci.infrastructure.persistence.mapper.first.MenuitemMapper;
import com.kd.ci.security.authentication.SecurityUtils;
import com.kd.ci.shared.util.core.CollectionUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** 「查詢選單」（純讀取操作，如取得樹狀選單、根據角色取選單等）的類別，
 * 通常應該放在 Application Layer（應用層）*/
@Slf4j
@Service
@RequiredArgsConstructor
public class MenuService {
	
	private static final int MAX_LEVEL = 3;
	
	private final MenuitemMapper menuItemMapper;
	
	private List<MenuTree> convertToMenuTree(List<Menuitem> menuItems) {
		if (CollectionUtils.isEmpty(menuItems)) {
			return Collections.emptyList();
		}
		
		/** 使用 MenuTreeDto 進行封裝轉換 */
		return menuItems.stream()
				.map(MenuTreeDto::new)
				.collect(Collectors.toList());
	}
	
	public List<MenuTree> findMenuTreeForUserRole() {
		List<BigDecimal> actsIds = SecurityUtils.getCurrentRoles().stream()
				.map(act -> act.getRoleId())
				.collect(Collectors.toList());
		
		if (actsIds.isEmpty()) {
			return Collections.emptyList();
		}
		
		List<Menuitem> flatList = this.menuItemMapper.findMenuItemsByActsIds(actsIds);
		List<Menuitem> trees = buildTree(flatList);
		return convertToMenuTree(trees);
	}
	
	public List<Menuitem> buildTree(List<Menuitem> flatList) {
		return buildTree(flatList, MAX_LEVEL);
	}
	
	/**
	 * 將平坦的 Menuitem 清單轉換為樹狀選單結構
	 * 
	 * @param flatList		平坦的選單清單
	 * @param maxLevel		最大允許層數（例如 3 表示最多到第 3 層）
	 * @return 樹狀結構的根節點清單
	 * 
	 * * 規則：
	 *	- parentId == null	→ 第 1 層（根節點）
	 *	- position 			數值越小越排在上面
	 *	- location			為 null 或空字串 → 表示為「資料夾」
	 */
	public List<Menuitem> buildTree(List<Menuitem> flatList, int maxLevel) {
		if (flatList == null || flatList.isEmpty()) {
			return Collections.emptyList();
		}
		
		/** 防呆 maxLevel 至少為 1 */
		if (maxLevel < 1) {
			maxLevel = MAX_LEVEL;
		}
		
		/** 先依照 position 排序（同一層內越小越上面） */
		flatList.sort(Comparator.comparing(Menuitem::getPosition, 
				Comparator.nullsLast(BigDecimal::compareTo)));
		
		/** 建立 id -> Menuitem 對照表，並初始化 children */
		Map<BigDecimal, Menuitem> itemMap = new LinkedHashMap<>();

		/** 初始化 children 清單 */
		for (Menuitem item : flatList) {
			item.setChildren(new ArrayList<>());
			itemMap.put(item.getId(), item);
		}
		
		/** 建立樹狀結構（限制最大層數） */
		List<Menuitem> roots = new ArrayList<>();

		for (Menuitem item : flatList) {
			BigDecimal parentId = item.getParentId();
			
			if (parentId == null) {		/** 第 1 層 */
				roots.add(item);
			} else {
				Menuitem parent = itemMap.get(parentId);
				if (parent != null) {
					int parentLevel = getLevel(parent, itemMap);	/** 計算 parent 所在的層級 */
					
					/** 只有當 parentLevel < maxLevel 時，才允許加入子節點 */
					if (parentLevel < maxLevel) {
						parent.getChildren().add(item);
					}
					/** 若 parentLevel >= maxLevel，則忽略此子節點（不再接受子節點） */
				} 
				/** 找不到父節點 → 當作根節點處理 */
				else {
					roots.add(item);
				}
			}
		}
		
		/** 對每一層的 children 重新排序 */
		sortChildrenRecursively(roots);
		
		return roots;
	}

	/**
	 * 計算某節點目前所在的層級（從 1 開始）(進入此方法，表示已經存在Parent)
	 */
	private int getLevel(Menuitem item, Map<BigDecimal, Menuitem> itemMap) {
		int level = 1;
		Menuitem current = item;

		while (current.getParentId() != null) {
			level++;
			Menuitem parent = itemMap.get(current.getParentId());
			
			/** 防止潛在循環，設定較高上限 */
			if (parent == null || level > 10) {
				break;
			}
			
			current = parent;
		}
		
		return level;
	}

	/**
	 * 遞迴對所有層級的 children 按照 position 排序
	 */
	private void sortChildrenRecursively(List<Menuitem> items) {
		for (Menuitem item : items) {
			List<Menuitem> children = item.getChildren();
			if (children != null && !children.isEmpty()) {
				children.sort(Comparator.comparing(Menuitem::getPosition, Comparator.nullsLast(BigDecimal::compareTo)));
				sortChildrenRecursively(children);
			}
		}
	}
	
}
