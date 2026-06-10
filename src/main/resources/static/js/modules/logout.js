/**
 * logout.js — 登出模組
 * ─────────────────────────────────────────────────────────────
 * 功能：
 * 1. 處理登出連結點擊事件（避免 inline onclick 被 CSP 阻擋）
 * 2. 使用 Gov.Toast 顯示美觀的確認提示
 * 3. 確認後安全提交 logout form（帶 CSRF token）
 *
 * 依賴：jQuery、Gov.Toast、Bootstrap Toast
 * ─────────────────────────────────────────────────────────────
 */
(function(Gov, $) {
	'use strict';

	/* ── 常數 ────────────────────────────────────────── */
	var LOGOUT_FORM_ID = 'logoutForm';
	var LOGOUT_LINK_ID = 'logoutLink';

	/* ═══════════════════════════════════════════════
	   PUBLIC API
	═══════════════════════════════════════════════ */
	Gov.Logout = {

		init: function() {
			_bindLogoutLink();
		}
	};

	/* ═══════════════════════════════════════════════
	   PRIVATE — 事件綁定
	═══════════════════════════════════════════════ */
	
	function _bindLogoutLink() {
		var $logoutLink = $('#' + LOGOUT_LINK_ID);
		var $logoutForm = $('#' + LOGOUT_FORM_ID);

		if (!$logoutForm.length || !$logoutLink.length) return;

		$logoutLink.on('click.logout', function(e) {
			e.preventDefault();

			Gov.Dialog.confirm({
				type: 'warning',
				title: '登出系統',
				message: '確定要登出系統嗎？'
			}).then(function(confirmed) {
				if (confirmed) {
					$logoutForm.submit();
				}
			});
		});
	}
	
	/* ═══════════════════════════════════════════════
	   自動初始化，改交由 app.js 統一生成
	═══════════════════════════════════════════════ 
	$(function() {
		if (typeof Gov !== 'undefined' && Gov.Logout) {
			Gov.Logout.init();
		}
	});*/

}(window.Gov = window.Gov || {}, jQuery));