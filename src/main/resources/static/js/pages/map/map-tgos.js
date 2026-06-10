/**
 * map-tgos.js — TGOS 
 * ─────────────────────────────────────────────────────────────
 * 功能：
 *  測試地圖功能腳本
 *
 * 依賴：jQuery
 * ─────────────────────────────────────────────────────────────
 */
(function($) {
	'use strict';

	$(function() {

		// ==================== TGOS 地圖變數 ====================
		let messageBox;
		let pMap;
		const markerPoint = new TGOS.TGPoint(303895, 2773227);
		const infotext = '<B>內政部資訊中心</B><br>台北市松江路469巷4號';
		const imgUrl = "http://api.tgos.tw/TGOS_API/images/marker2.png";

		// ==================== 初始化地圖 ====================
		function initMap() {
			const pOMap = document.getElementById("OMap");

			const mapOptions = {
				scaleControl: false,
				navigationControl: true,
				navigationControlOptions: {
					controlPosition: TGOS.TGControlPosition.TOP_LEFT,
					navigationControlStyle: TGOS.TGNavigationControlStyle.SMALL
				},
				mapTypeControl: false
			};

			pMap = new TGOS.TGOnlineMap(pOMap, TGOS.TGCoordSys.EPSG3826, mapOptions);
			pMap.setZoom(15);
			pMap.setCenter(markerPoint);

			// 建立標記點圖示
			const markerImg = new TGOS.TGImage(imgUrl,
				new TGOS.TGSize(38, 33),
				new TGOS.TGPoint(0, 0),
				new TGOS.TGPoint(10, 33)
			);

			const pTGMarker = new TGOS.TGMarker(pMap, markerPoint, '', markerImg);

			// 建立訊息視窗
			const infoWindowOptions = {
				maxWidth: 400,
				pixelOffset: new TGOS.TGSize(5, -35),
				zIndex: 99
			};

			messageBox = new TGOS.TGInfoWindow(infotext, markerPoint, infoWindowOptions);

			// 事件監聽
			TGOS.TGEvent.addListener(pTGMarker, "mouseover", () => messageBox.open(pMap));
			TGOS.TGEvent.addListener(pTGMarker, "mouseout", () => messageBox.close());
			TGOS.TGEvent.addListener(pTGMarker, "click", () => messageBox.open(pMap));
		}

		// ==================== 重置地圖 ====================
		$('#resetMapBtn').on('click', function() {
			if (pMap) {
				pMap.setCenter(markerPoint);
				pMap.setZoom(15);

				const $btn = $(this);
				const originalHTML = $btn.html();
				$btn.html('<i class="bi bi-check2"></i> 已重置').prop('disabled', true);

				setTimeout(() => {
					$btn.html(originalHTML).prop('disabled', false);
				}, 1800);
			}
		});

		// ==================== 頁面初始化 ====================
		// TGOS API 需在 DOM ready 且腳本載入後執行
		if (typeof TGOS !== 'undefined' && typeof TGOS.TGOnlineMap !== 'undefined') {
			initMap();
		} else {
			console.error('TGOS MAP API 載入失敗，請檢查網路或 API Key');
		}

	});

}(jQuery));
