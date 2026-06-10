/**
 * app.js — 系統初始化入口
 * ─────────────────────────────────────────────────────────────
 * 職責：
 *  1. 按序初始化各 JS 模組（sidebar, topbar, datatables）
 *  2. 設定 jQuery AJAX 全域預設（CSRF Token、錯誤處理）
 *  3. 提供 Gov.Ajax 工廠方法，供功能頁面發送 POST 請求使用
 *
 * 載入順序（由 layouts/main.html 保證）：
 *   jquery.min.js → bootstrap.bundle.min.js →
 *   dataTables.min.js → dataTables.bootstrap5.min.js →
 *   sidebar.js → topbar.js → datatables.js → app.js
 *
 * 依賴：jQuery、所有 modules/*.js
 * ─────────────────────────────────────────────────────────────
 */
(function(Gov, $) {
	'use strict';

	/* ═══════════════════════════════════════════════
	   CSRF — 從 Spring Security 的 meta 標籤讀取
	═══════════════════════════════════════════════ */
	var _csrf = {
		token: function() { return $('meta[name="_csrf"]').attr('content') || ''; },
		header: function() { return $('meta[name="_csrf_header"]').attr('content') || 'X-CSRF-TOKEN'; },
		param: function() { return $('meta[name="_csrf_parameter"]').attr('content') || '_csrf'; }
	};

	/* ═══════════════════════════════════════════════
	   JQUERY AJAX 全域設定
	═══════════════════════════════════════════════ */
	function _setupAjax() {
		// 所有 AJAX 請求自動附加 CSRF Header
		$.ajaxSetup({
			beforeSend: function(xhr) {
				var header = _csrf.header();
				var token = _csrf.token();
				if (header && token) {
					xhr.setRequestHeader(header, token);
				}
			}
		});

		// 全域 AJAX 錯誤處理
		$(document).ajaxError(function(event, xhr, settings, error) {
			if (xhr.status === 401 || xhr.status === 403) {
				// Session 過期或無權限 → 導回登入頁
				window.location.href = '/auth/login?expired';
				return;
			}
			if (xhr.status === 0) return; // 請求被取消，不處理
			console.error('[Gov.Ajax] Error:', settings.url, error);
		});
	}

	/* ═══════════════════════════════════════════════
	   PUBLIC API — Gov.Ajax
	   提供統一的 jQuery POST 工廠方法
	═══════════════════════════════════════════════ */
	Gov.Ajax = {

		/**
		 * 發送 POST 請求（JSON 回應）
		 *
		 * @param {string}   url      請求路徑
		 * @param {object}   data     請求資料
		 * @param {function} onSuccess  成功回呼 function(responseData)
		 * @param {function} onError    失敗回呼 function(xhr, status, error)，可省略
		 * @returns {jqXHR}
		 *
		 * 使用範例：
		 *   Gov.Ajax.post('/api/user/save', { name: '王大明' }, function(res) {
		 *     if (res.success) Gov.Toast.success(res.message);
		 *   });
		 */
		post: function(url, data, onSuccess, onError) {
			return $.ajax({
				url: url,
				type: 'POST',
				contentType: 'application/json; charset=utf-8',
				data: JSON.stringify(data),
				dataType: 'json',
				success: onSuccess,
				error: onError || _defaultErrorHandler
			});
		},

		/**
		 * 發送 POST（表單格式，適合傳統 form 資料或檔案）
		 */
		postForm: function(url, formData, onSuccess, onError) {
			return $.ajax({
				url: url,
				type: 'POST',
				data: formData,
				dataType: 'json',
				success: onSuccess,
				error: onError || _defaultErrorHandler
			});
		},

		/**
		 * 發送 GET 請求
		 */
		get: function(url, params, onSuccess, onError) {
			return $.ajax({
				url: url,
				type: 'GET',
				data: params || {},
				dataType: 'json',
				success: onSuccess,
				error: onError || _defaultErrorHandler
			});
		}
	};

	/* ═══════════════════════════════════════════════
	   PUBLIC API — Gov.Toast（輕量提示）
	═══════════════════════════════════════════════ */
	Gov.Toast = {

		/**
		 * 顯示 Bootstrap Toast 提示
		 * layouts/main.html 底部已內嵌 #govToastContainer
		 */
		show: function(message, type) {
			type = type || 'info'; // success | danger | warning | info
			var id = 'toast-' + Date.now();
			var icon = _toastIcon(type);
			var html =
				'<div id="' + id + '" class="toast align-items-center text-bg-' + type +
				' border-0" role="alert" aria-live="assertive" aria-atomic="true" ' +
				'data-bs-delay="4000">' +
				'  <div class="d-flex">' +
				'    <div class="toast-body d-flex align-items-center gap-2">' +
				'      <i class="bi ' + icon + '"></i>' +
				'      <span>' + _escHtml(message) + '</span>' +
				'    </div>' +
				'    <button type="button" class="btn-close btn-close-white me-2 m-auto"' +
				'      data-bs-dismiss="toast" aria-label="關閉"></button>' +
				'  </div>' +
				'</div>';

			var $container = $('#govToastContainer');
			if (!$container.length) {
				$container = $('<div id="govToastContainer" class="toast-container position-fixed bottom-0 end-0 p-3 gov-toast-container"></div>');
				$('body').append($container);
			}

			var $toast = $(html).appendTo($container);
			var bsToast = new bootstrap.Toast($toast[0]);
			bsToast.show();
			$toast[0].addEventListener('hidden.bs.toast', function() {
				$toast.remove();
			});
		},

		success: function(msg) { Gov.Toast.show(msg, 'success'); },
		error: function(msg) { Gov.Toast.show(msg, 'danger'); },
		warning: function(msg) { Gov.Toast.show(msg, 'warning'); },
		info: function(msg) { Gov.Toast.show(msg, 'info'); }
	};

	/* ═══════════════════════════════════════════════
	   PRIVATE
	═══════════════════════════════════════════════ */

	function _defaultErrorHandler(xhr, status, error) {
		var msg = '操作失敗，請稍後再試';
		try {
			var body = JSON.parse(xhr.responseText);
			if (body && body.message) msg = body.message;
		} catch (e) { /* ignore */ }
		Gov.Toast.error(msg);
	}

	function _toastIcon(type) {
		var icons = {
			success: 'bi-check-circle-fill',
			danger: 'bi-exclamation-triangle-fill',
			warning: 'bi-exclamation-circle-fill',
			info: 'bi-info-circle-fill'
		};
		return icons[type] || icons.info;
	}

	function _escHtml(str) {
		return String(str)
			.replace(/&/g, '&amp;')
			.replace(/</g, '&lt;')
			.replace(/>/g, '&gt;')
			.replace(/"/g, '&quot;');
	}

	/* ═══════════════════════════════════════════════
	   INIT — DOM Ready
	═══════════════════════════════════════════════ */
	$(function() {
		_setupAjax();
		Gov.Sidebar.init();
		Gov.Topbar.init();
	//	Gov.DataTable.init() 由各頁面在 page-js fragment 中呼叫
		Gov.Logout.init();
		console.debug('[Gov] System initialized.');
	});

}(window.Gov = window.Gov || {}, jQuery));
