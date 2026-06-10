/**
 * sidebar.js — 側欄模組
 * ─────────────────────────────────────────────────────────────
 * 功能：
 *  1. 桌面版側欄展開/收合（持久化至 localStorage）
 *  2. 1-3 層手風琴選單（同層只展一個）
 *  3. 手機版側欄滑入/滑出 + 遮罩
 *  4. 收合狀態下 tooltip 顯示選單名稱
 *  5. 頁面載入時自動展開當前頁面的祖先層選單
 *
 * 依賴：jQuery（由 layouts/main.html 在此檔之前載入）
 * ─────────────────────────────────────────────────────────────
 */
(function(Gov, $) {
	'use strict';

	/* ── 常數 ────────────────────────────────────────── */
	var STORAGE_KEY = 'gov_sidebar_collapsed';
	var CSS_COLLAPSED = 'sidebar-collapsed';
	var CSS_MOBILE_OPEN = 'is-open';
	var CSS_EXPANDED = 'is-expanded';
	var TRANSITION_MS = 240;

	/* ── DOM 快取（在 init() 後才賦值） ─────────────── */
	var $wrapper, $sidebar, $backdrop, $collapseBtn, $toggleBtn;

	/* ═══════════════════════════════════════════════
	   PUBLIC API
	═══════════════════════════════════════════════ */
	Gov.Sidebar = {

		init: function() {
			$wrapper = $('#appWrapper');
			$sidebar = $('#appSidebar');
			$backdrop = $('#sidebarBackdrop');
			$collapseBtn = $('#sidebarCollapseBtn');   // 側欄內折疊鈕
			$toggleBtn = $('#sidebarToggleBtn');      // topbar 漢堡鈕

			_restoreState();
			_bindCollapseBtn();
			_bindToggleBtn();
			_bindBackdrop();
			_bindMenuAccordion();
			_autoExpandActivePath();
			_initCollapsedTooltips();

			// 視窗大小變化時處理響應式
			$(window).on('resize.sidebar', _debounce(_onResize, 150));
		}
	};

	/* ═══════════════════════════════════════════════
	   PRIVATE — 收合/展開
	═══════════════════════════════════════════════ */

	function _restoreState() {
		var collapsed = localStorage.getItem(STORAGE_KEY) === 'true';
		_setCollapsed(collapsed, false); // false = 不存 storage（已從 storage 讀）
	}

	function _setCollapsed(collapsed, save) {
		$wrapper.toggleClass(CSS_COLLAPSED, collapsed);
		_updateCollapseIcon(collapsed);
		if (save !== false) {
			localStorage.setItem(STORAGE_KEY, String(collapsed));
		}
	}

	function _bindCollapseBtn() {
		$collapseBtn.on('click.sidebar', function() {
			// 桌面版：toggle 收合
			if (_isDesktop()) {
				_setCollapsed(!$wrapper.hasClass(CSS_COLLAPSED));
			}
			// 手機版：此鈕不出現（topbar hamburger 負責）
		});
	}

	function _bindToggleBtn() {
		$toggleBtn.on('click.sidebar', function() {
			if (_isDesktop()) {
				// 桌面版：同收合鈕
				_setCollapsed(!$wrapper.hasClass(CSS_COLLAPSED));
			} else {
				// 手機版：滑入 sidebar
				_openMobile();
			}
		});
	}

	function _updateCollapseIcon(collapsed) {
		var $icon = $collapseBtn.find('i');
		$icon
			.toggleClass('bi-layout-sidebar-reverse', !collapsed)
			.toggleClass('bi-layout-sidebar', collapsed);
	}

	/* ═══════════════════════════════════════════════
	   PRIVATE — 手機版
	═══════════════════════════════════════════════ */

	function _openMobile() {
		$sidebar.addClass(CSS_MOBILE_OPEN);
		$backdrop.addClass('is-visible');
		$('body').css('overflow', 'hidden');
	}

	function _closeMobile() {
		$sidebar.removeClass(CSS_MOBILE_OPEN);
		$backdrop.removeClass('is-visible');
		$('body').css('overflow', '');
	}

	function _bindBackdrop() {
		$backdrop.on('click.sidebar', _closeMobile);
		// ESC 鍵關閉
		$(document).on('keyup.sidebar', function(e) {
			if (e.key === 'Escape') _closeMobile();
		});
	}

	function _onResize() {
		if (_isDesktop()) {
			_closeMobile(); // 恢復桌面時關閉手機選單
		}
	}

	/* ═══════════════════════════════════════════════
	   PRIVATE — 手風琴選單（1-3 層）
	═══════════════════════════════════════════════ */

	function _bindMenuAccordion() {
		// 委派事件：點擊有子選單的父連結
		$('#sidebarNav').on('click.sidebar', '.sidebar-link--parent', function(e) {
			e.preventDefault();

			var $link = $(this);
			var targetId = $link.attr('data-target');
			if (!targetId) return;

			var $sub = $('#' + targetId);
			if (!$sub.length) return;

			var isOpen = $sub.hasClass(CSS_EXPANDED);

			// 同層折疊（同一 ul 內的兄弟 submenu）
			var $siblingLinks = $link.closest('ul').find('> li > .sidebar-link--parent');
			$siblingLinks.not($link).each(function() {
				var sibTargetId = $(this).attr('data-target');
				if (sibTargetId) {
					$('#' + sibTargetId).removeClass(CSS_EXPANDED);
					$(this).attr('aria-expanded', 'false');
				}
			});

			// 切換當前
			$sub.toggleClass(CSS_EXPANDED, !isOpen);
			$link.attr('aria-expanded', String(!isOpen));
		});
	}

	/* 頁面載入時自動展開包含 is-active 子項目的祖先層 */
	function _autoExpandActivePath() {
		$('.sidebar-submenu').each(function() {
			var $sub = $(this);
			// 若此子選單中有 is-active 的項目，展開它
			if ($sub.find('.is-active').length > 0) {
				$sub.addClass(CSS_EXPANDED);
				// 找對應父連結，更新 aria-expanded
				var subId = $sub.attr('id');
				if (subId) {
					$('[data-target="' + subId + '"]').attr('aria-expanded', 'true');
				}
			}
		});
	}

	/* ═══════════════════════════════════════════════
	   PRIVATE — 收合時 Bootstrap Tooltip
	═══════════════════════════════════════════════ */

	function _initCollapsedTooltips() {
		// 收合時，level-1 連結顯示 tooltip（選單名稱由 title attr 提供）
		// Bootstrap 5 自動讀取 title attr，僅需在收合時啟用
		// Sidebar CSS 的 .sidebar-collapsed .sidebar-link 已設 pointer-events 讓 title 可見
	}

	/* ═══════════════════════════════════════════════
	   PRIVATE — 工具
	═══════════════════════════════════════════════ */

	function _isDesktop() {
		return window.innerWidth >= 992;
	}

	function _debounce(fn, delay) {
		var timer;
		return function() {
			clearTimeout(timer);
			timer = setTimeout(fn, delay);
		};
	}

}(window.Gov = window.Gov || {}, jQuery));
