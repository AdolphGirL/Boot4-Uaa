/**
 * datatables.js — DataTable 全域設定模組
 * ─────────────────────────────────────────────────────────────
 * 功能：
 *  1. 繁體中文語系設定
 *  2. 共用 DOM 版面（Bootstrap 5 整合）
 *  3. 共用預設值（分頁、長度選單等）
 *  4. 提供 Gov.DataTable.init() 工廠方法，
 *     功能頁面只需呼叫此方法即可初始化表格
 *
 * 使用範例（在 layout:fragment="page-js" 內）：
 *   $(function() {
 *     Gov.DataTable.init('#myTable', { /* 頁面專屬覆寫 *\/ });
 *   });
 *
 * 依賴：jQuery、DataTables、DataTables Bootstrap 5
 * ─────────────────────────────────────────────────────────────
 */
(function(Gov, $) {
	'use strict';

	/* ── 繁體中文語系 ───────────────────────────── */
	var ZH_TW = {
		emptyTable: '目前沒有資料',
		info: '顯示第 _START_ 至 _END_ 筆，共 _TOTAL_ 筆',
		infoEmpty: '共 0 筆',
		infoFiltered: '（從 _MAX_ 筆中篩選）',
		lengthMenu: '每頁顯示 _MENU_ 筆',
		loadingRecords: '載入中…',
		processing: '處理中…',
		search: '',
		searchPlaceholder: '搜尋…',
		zeroRecords: '查無符合條件的資料',
		paginate: {
			first: '第一頁',
			last: '最後頁',
			next: '›',
			previous: '‹'
		},
		aria: {
			sortAscending: '：升冪排序',
			sortDescending: '：降冪排序'
		}
	};

	/* ── 共用 DOM 版面 ──────────────────────────── */
	/* Bootstrap 5 整合的標準 DOM 字串：
	   - l: 每頁筆數
	   - f: 搜尋框
	   - t: 表格
	   - i: 顯示資訊
	   - p: 分頁
	*/
	var DOM_LAYOUT =
		"<'row align-items-center mb-2'" +
		"<'col-sm-6'l>" +
		"<'col-sm-6 d-flex justify-content-sm-end'f>" +
		">" +
		"<'row'<'col-12'tr>>" +
		"<'row align-items-center mt-2'" +
		"<'col-sm-5'i>" +
		"<'col-sm-7 d-flex justify-content-sm-end'p>" +
		">";

	/* ── 全域預設 ───────────────────────────────── */
	var GLOBAL_DEFAULTS = {
		language: ZH_TW,
		dom: DOM_LAYOUT,
		pageLength: 10,
		lengthMenu: [[10, 25, 50, 100, -1], [10, 25, 50, 100, '全部']],
		responsive: true,
		autoWidth: false,
		stateSave: false,    // 若要記憶搜尋/分頁狀態改 true
		processing: false,
		order: [],       // 預設不排序（由 DB 決定順序）
	};

	/* ═══════════════════════════════════════════════
	   PUBLIC API
	═══════════════════════════════════════════════ */
	Gov.DataTable = {

		/**
		 * 初始化 DataTable（功能頁面入口）
		 *
		 * @param {string|jQuery} selector  表格選擇器或 jQuery 物件
		 * @param {object}        options   頁面專屬覆寫選項（合併至全域預設）
		 * @returns {DataTables.Api}        DataTable API 物件
		 */
		init: function(selector, options) {
			var opts = $.extend(true, {}, GLOBAL_DEFAULTS, options || {});
			return $(selector).DataTable(opts);
		},

		/**
		 * 初始化 Ajax Server-Side DataTable（大量資料分頁）
		 *
		 * @param {string}  selector    表格選擇器
		 * @param {string}  url         Ajax 請求 URL
		 * @param {Array}   columns     欄位定義
		 * @param {object}  options     額外覆寫
		 */
		initAjax: function(selector, url, columns, options) {
			var ajaxOpts = {
				processing: true,
				serverSide: true,
				ajax: {
					url: url,
					type: 'POST',
					data: function(d) {
						// 可在此附加 CSRF token
						d[_csrfParam()] = _csrfToken();
						return d;
					}
				},
				columns: columns
			};
			var opts = $.extend(true, {}, GLOBAL_DEFAULTS, ajaxOpts, options || {});
			return $(selector).DataTable(opts);
		},

		/** 取得 CSRF Token（供 Ajax 請求使用） */
		csrfHeaders: function() {
			var h = {};
			var name = $('meta[name="_csrf_header"]').attr('content');
			var token = $('meta[name="_csrf"]').attr('content');
			if (name && token) h[name] = token;
			return h;
		}
	};

	/* ═══════════════════════════════════════════════
	   PRIVATE
	═══════════════════════════════════════════════ */

	function _csrfToken() {
		return $('meta[name="_csrf"]').attr('content') || '';
	}

	function _csrfParam() {
		return $('meta[name="_csrf_parameter"]').attr('content') || '_csrf';
	}

	/* ── 套用全域預設至 $.fn.DataTable.defaults ── */
	function _applyGlobalDefaults() {
		if (typeof $.fn.DataTable === 'undefined') return;
		$.extend(true, $.fn.DataTable.defaults, GLOBAL_DEFAULTS);
	}

	_applyGlobalDefaults();

}(window.Gov = window.Gov || {}, jQuery));
