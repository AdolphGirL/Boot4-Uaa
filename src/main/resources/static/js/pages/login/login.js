/**
 * login.js — 登入頁專屬 JS
 * 獨立頁面，不依賴 app.js 或其他模組
 */
(function($) {
	'use strict';

	$(function() {

		// ── 密碼顯示/隱藏 ────────────────────────────────
		$('#togglePwd').on('click', function() {
			var $input = $('#password');
			var $icon = $('#togglePwdIcon');
			var isPass = $input.attr('type') === 'password';
			$input.attr('type', isPass ? 'text' : 'password');
			$icon.toggleClass('bi-eye', !isPass)
				.toggleClass('bi-eye-slash', isPass);
		});

		// ── 登入按鈕 Loading 狀態 ─────────────────────────
		$('#loginForm').on('submit', function() {
			var $btn = $('#loginBtn');
			$btn.find('.login-submit-text').addClass('d-none');
			$btn.find('.login-submit-loading').removeClass('d-none');
			$btn.prop('disabled', true);
		});
	});

}(jQuery));
