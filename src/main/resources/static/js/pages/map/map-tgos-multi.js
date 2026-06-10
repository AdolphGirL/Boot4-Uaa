/**
 * map-tgos-multi.js — TGOS 
 * ─────────────────────────────────────────────────────────────
 * 功能：
 *  測試多點地圖功能腳本
 *
 * 依賴：jQuery
 * ─────────────────────────────────────────────────────────────
 */
(function($) {
	'use strict';

	$(function() {

		let pMap;
		let markers = [];
		let infoWindows = [];
		let currentActiveIndex = -1;

		// ==================== 五都資料 ====================
		const cities = [
			{
				name: "臺北市政府",
				point: new TGOS.TGPoint(306954, 2770049),
				info: "<B>臺北市政府</B><br>台北市信義區市府路1號",
				color: "#d32f2f"   // 紅
			},
			{
				name: "新北市政府",
				point: new TGOS.TGPoint(296991, 2767219),
				info: "<B>新北市政府</B><br>新北市板橋區中山路1段161號",
				color: "#1976d2"   // 藍
			},
			{
				name: "臺中市政府",
				point: new TGOS.TGPoint(214192, 2673102),
				info: "<B>臺中市政府</B><br>臺中市西屯區台灣大道三段99號",
				color: "#388e3c"   // 綠
			},
			{
				name: "臺南市政府",
				point: new TGOS.TGPoint(166459, 2543656),
				info: "<B>臺南市政府</B><br>臺南市安平區永華路二段545號",
				color: "#f57c00"   // 橙
			},
			{
				name: "高雄市政府",
				point: new TGOS.TGPoint(179269, 2502465),
				info: "<B>高雄市政府</B><br>高雄市苓雅區四維三路2號",
				color: "#7b1fa2"   // 紫
			}
		];

		function initMap() {
			const pOMap = document.getElementById("OMap");

			pMap = new TGOS.TGOnlineMap(pOMap, TGOS.TGCoordSys.EPSG3826, {
				scaleControl: false,
				navigationControl: true,
				navigationControlOptions: {
					controlPosition: TGOS.TGControlPosition.TOP_LEFT,
					navigationControlStyle: TGOS.TGNavigationControlStyle.SMALL
				},
				mapTypeControl: false
			});

			// 建立標記點
			cities.forEach((city, index) => {
				// 使用不同顏色標記（透過 URL 參數或不同圖示，這裡用官方 marker + 顏色模擬）
				const markerImg = new TGOS.TGImage(
					`http://api.tgos.tw/TGOS_API/images/marker2.png`,
					new TGOS.TGSize(38, 33),
					new TGOS.TGPoint(0, 0),
					new TGOS.TGPoint(10, 33)
				);

				const marker = new TGOS.TGMarker(pMap, city.point, '', markerImg, { flat: false });

				const infoWindow = new TGOS.TGInfoWindow(
					city.info,
					city.point,
					{ maxWidth: 360, pixelOffset: new TGOS.TGSize(8, -38) }
				);

				// 事件
				TGOS.TGEvent.addListener(marker, "mouseover", () => infoWindow.open(pMap, marker));
				TGOS.TGEvent.addListener(marker, "mouseout", () => infoWindow.close());
				TGOS.TGEvent.addListener(marker, "click", () => {
					infoWindow.open(pMap, marker);
					highlightCity(index);
				});

				markers.push(marker);
				infoWindows.push(infoWindow);
			});

			// 初始顯示全部標記（重要修正）
			setTimeout(() => {
				fitAllMarkers();
			}, 800);
		}

		// 顯示全部標記並自動調整縮放
		function fitAllMarkers() {
			if (!pMap || markers.length === 0) return;

			// 計算大致中心點與合理縮放級別（台灣五都範圍）
			pMap.setCenter(new TGOS.TGPoint(230000, 2650000));
			pMap.setZoom(7);   // 7 是比較適合看到五都的層級
		}

		// 點擊側邊列表後定位
		function goToCity(index) {
			if (!pMap || !markers[index]) return;

			const city = cities[index];

			pMap.setCenter(city.point);
			pMap.setZoom(13);                    // 拉近到適合單一城市查看的層級

			// 開啟該點的資訊視窗
			setTimeout(() => {
				infoWindows[index].open(pMap, markers[index]);
			}, 300);

			highlightCity(index);
		}

		// 高亮側邊列表
		function highlightCity(index) {
			currentActiveIndex = index;
			$('.city-item').removeClass('active');
			$(`#city-item-${index}`).addClass('active');
		}

		// ==================== 建立側邊列表 ====================
		function buildCityList() {
			const $list = $('#cityList');
			$list.empty();

			cities.forEach((city, index) => {
				const $item = $(`
                    <div class="list-group-item city-item" id="city-item-${index}">
                        <div class="d-flex align-items-center">
                            <span class="badge me-3" 
                                  style="background-color: ${city.color}; width: 18px; height: 18px; border-radius: 50%;"></span>
                            <div>
                                <strong>${city.name}</strong>
                            </div>
                        </div>
                    </div>
                `);

				$item.on('click', () => goToCity(index));
				$list.append($item);
			});
		}

		// ==================== 按鈕事件 ====================
		$('#resetMapBtn').on('click', () => {
			if (pMap) {
				pMap.setZoom(7);
				pMap.setCenter(new TGOS.TGPoint(230000, 2650000));
			}
		});

		$('#fitAllBtn').on('click', fitAllMarkers);

		// ==================== 初始化 ====================
		buildCityList();

		if (typeof TGOS !== 'undefined') {
			initMap();
		} else {
			console.error('TGOS API 載入失敗');
		}

	});

}(jQuery));