/**
 * topbar.js — 頂部導覽列模組
 * ─────────────────────────────────────────────────────────────
 * 功能：
 *  1. 字體大小控制（縮小 / 還原 / 放大）持久化至 localStorage
 *  2. 使用者頭像縮寫（取顯示名稱第一字）
 *  3. 即時時鐘顯示
 *
 * ★ 字體大小設定說明 ★
 * ─────────────────────────────────────────────────────────────
 *  只需修改 tokens.css 的 :root 變數即可改變字體大小：
 *    --font-size-sm: 12px  ← 最小
 *    --font-size-md: 14px  ← 預設（FONT_DEFAULT 對應此值）
 *    --font-size-lg: 16px
 *    --font-size-xl: 18px  ← 最大
 *
 *  本檔不硬寫任何 px 數值，全部從 CSS 變數讀取。
 * ─────────────────────────────────────────────────────────────
 * 依賴：jQuery
 */
(function(Gov, $) {
	'use strict';

	/* ─── 步進設定 ─────────────────────────────────────────
	   FONT_STEPS    : 步進名稱，順序對應 data-font-step -1/0/1/2
	   FONT_DEFAULT  : 還原時回到的步進，需與 tokens.css --font-size-md 對應
	   FONT_KEY      : localStorage 鍵名
	─────────────────────────────────────────────────────── */
	var FONT_STEPS = ['sm', 'md', 'lg', 'xl'];
	var FONT_DEFAULT = 'md';
	var FONT_KEY = 'gov_font_size';

	/* 步進名稱 → data-font-step 整數（sm=-1, md=0, lg=1, xl=2） */
	var STEP_TO_INT = { sm: -1, md: 0, lg: 1, xl: 2 };

	/* ════════════════════════════════════════════════════════
	   PUBLIC API
	════════════════════════════════════════════════════════ */
	Gov.Topbar = {
		init: function() {
			_initFontSize();
			_initUserAvatar();
			_initClock();
		}
	};

	/* ════════════════════════════════════════════════════════
	   PRIVATE — 字體大小
	════════════════════════════════════════════════════════ */

	function _initFontSize() {
		var saved = localStorage.getItem(FONT_KEY) || FONT_DEFAULT;
		_applyFont(saved);

		$('#fontIncrease').on('click.topbar', function() {
			var idx = FONT_STEPS.indexOf(_currentStep());
			if (idx < FONT_STEPS.length - 1) _applyFont(FONT_STEPS[idx + 1]);
		});

		$('#fontDecrease').on('click.topbar', function() {
			var idx = FONT_STEPS.indexOf(_currentStep());
			if (idx > 0) _applyFont(FONT_STEPS[idx - 1]);
		});

		$('#fontReset').on('click.topbar', function() {
			_applyFont(FONT_DEFAULT);
		});
	}

	function _currentStep() {
		return localStorage.getItem(FONT_KEY) || FONT_DEFAULT;
	}

	function _applyFont(step) {
		if (FONT_STEPS.indexOf(step) === -1) step = FONT_DEFAULT;

		/* 1. 寫入 data-font-step → 觸發 tokens.css 的 [attr] 選擇器 */
		document.documentElement.setAttribute('data-font-step', STEP_TO_INT[step]);

		/* 2. 讀取 tokens.css 對應的 CSS 變數，直接設 fontSize 確保即時生效
			  --font-size-sm / --font-size-md / --font-size-lg / --font-size-xl
			  這樣字體數值完全由 tokens.css 控制，本檔不寫死任何 px 值  */
		var cssVarName = '--font-size-' + step;
		var pxValue = getComputedStyle(document.documentElement)
			.getPropertyValue(cssVarName).trim();
		if (pxValue) {
			document.documentElement.style.fontSize = pxValue;
		}

		/* 3. 持久化 */
		localStorage.setItem(FONT_KEY, step);

		/* 4. 更新按鈕 disabled 狀態 */
		$('#fontDecrease').prop('disabled', step === FONT_STEPS[0]);
		$('#fontIncrease').prop('disabled', step === FONT_STEPS[FONT_STEPS.length - 1]);
	}

	/* ════════════════════════════════════════════════════════
	   PRIVATE — 使用者頭像縮寫
	════════════════════════════════════════════════════════ */

	function _initUserAvatar() {
		var $avatar = $('#topbarAvatar');
		if ($avatar.length) {
			var name = ($avatar.attr('data-name') || '').trim();
			$avatar.text(name ? name.charAt(0) : 'U');
		}

		var $ddAvatar = $('#dropdownAvatar');
		if ($ddAvatar.length) {
			var ddName = ($('#topbarAvatar').attr('data-name') || '').trim();
			$ddAvatar.text(ddName ? ddName.charAt(0) : 'U');
		}
	}

	/* ════════════════════════════════════════════════════════
	   PRIVATE — 即時時鐘
	════════════════════════════════════════════════════════ */

	function _initClock() {
		var $el = $('#liveClock');
		if (!$el.length) return;

		function tick() {
			var now = new Date();
			$el.text(_pad(now.getHours()) + ':' +
				_pad(now.getMinutes()) + ':' +
				_pad(now.getSeconds()));
		}
		tick();
		setInterval(tick, 1000);
	}

	function _pad(n) {
		return String(n).padStart(2, '0');
	}

}(window.Gov = window.Gov || {}, jQuery));
