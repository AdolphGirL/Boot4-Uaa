package com.kd.ci.application.dto.wrap;

import java.util.Collections;
import java.util.List;

import com.kd.ci.application.dto.menu.MenuTree;

/***
 * Menu Application Layer DTO (配合頁面，將MenuTreeDto在進行封裝)
 * 傳遞給 Thymeleaf 模板的選單樹資料
 * Collections.unmodifiableList() 只防止透過這個 List 物件修改。
	如果原本傳進來的 List 本身還被外部持有，且外部直接修改那個 List，還是會影響到這裡（因為 unmodifiableList 是 view）。
	
	所以最佳實務是在建構時就複製一份（如果你非常嚴格）：
	this.tree = tree == null 
		? Collections.emptyList() 
		: Collections.unmodifiableList(new ArrayList<>(tree));  // 深拷貝一層
 */
public class MenuTreeWrap {

	private final List<? extends MenuTree> tree;
	private final String currentUrl;

	/** 這裡把傳進來的 List 包成 unmodifiable，防止外部修改內部樹結構 */
	private MenuTreeWrap(List<? extends MenuTree> tree, String currentUrl) {
		this.tree = tree == null ? Collections.emptyList() : Collections.unmodifiableList(tree);

		this.currentUrl = currentUrl;
	}

	public static MenuTreeWrap of(List<? extends MenuTree> tree, String currentUrl) {
		return new MenuTreeWrap(tree, currentUrl);
	}

	public static MenuTreeWrap empty() {
		return new MenuTreeWrap(Collections.emptyList(), null);
	}

	public boolean isEmpty() {
		return tree.isEmpty();
	}
	
	/**
	 * 返回不可修改的樹列表 外部無法透過這個 List 新增、刪除或修改樹的結構
	 */
	public List<? extends MenuTree> getTree() {
		return tree;
	}
	
	public String getCurrentUrl() {
		return currentUrl;
	}
}
