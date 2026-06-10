/**
 * dashboard.js — 儀表板專屬 JS
 * 在 dashboard/dashboard.html 的 layout:fragment="page-js" 中載入
 * 依賴：jQuery、Chart.js、Gov.DataTable（已由 app.js 初始化全域設定）
 */
(function($) {
	'use strict';

	$(function() {
		_initBarChart();
		_initDoughnutChart();
		_initRecentTable();
	});

	/* ── 月度長條圖 ────────────────────────────────── */
	function _initBarChart() {
		var ctx = document.getElementById('monthlyChart');
		if (!ctx || typeof Chart === 'undefined') return;

		new Chart(ctx, {
			type: 'bar',
			data: {
				labels: ['1月', '2月', '3月', '4月', '5月', '6月',
					'7月', '8月', '9月', '10月', '11月', '12月'],
				datasets: [
					{
						label: '新受理',
						data: [65, 59, 87, 73, 91, 84, 78, 95, 102, 88, 76, 67],
						backgroundColor: 'rgba(26,79,139,.75)',
						borderRadius: 5,
						borderSkipped: false
					},
					{
						label: '已結案',
						data: [55, 50, 72, 68, 85, 79, 70, 88, 94, 82, 70, 60],
						backgroundColor: 'rgba(32,201,151,.65)',
						borderRadius: 5,
						borderSkipped: false
					}
				]
			},
			options: {
				responsive: true,
				maintainAspectRatio: false,
				plugins: {
					legend: {
						position: 'top',
						align: 'end',
						labels: { font: { size: 11 }, boxWidth: 11, padding: 14 }
					},
					tooltip: { mode: 'index', intersect: false }
				},
				scales: {
					x: {
						grid: { display: false },
						ticks: { font: { size: 11 } }
					},
					y: {
						grid: { color: '#f1f3f5' },
						ticks: { font: { size: 11 }, stepSize: 25 },
						beginAtZero: true
					}
				}
			}
		});
	}

	/* ── 案件類別圓環圖 ─────────────────────────────── */
	function _initDoughnutChart() {
		var ctx = document.getElementById('categoryChart');
		if (!ctx || typeof Chart === 'undefined') return;

		var labels = ['人事案件', '採購案件', '公文簽核', '教育訓練', '其他'];
		var data = [32, 18, 27, 12, 11];
		var colors = ['#1a4f8b', '#20c997', '#fd7e14', '#0dcaf0', '#adb5bd'];

		new Chart(ctx, {
			type: 'doughnut',
			data: {
				labels: labels,
				datasets: [{
					data: data,
					backgroundColor: colors,
					borderWidth: 2,
					borderColor: '#fff',
					hoverBorderWidth: 3
				}]
			},
			options: {
				responsive: true,
				cutout: '66%',
				plugins: {
					legend: { display: false },
					tooltip: {
						callbacks: {
							label: function(ctx) {
								return ' ' + ctx.label + ': ' + ctx.parsed + '%';
							}
						}
					}
				}
			}
		});

		// 自訂圖例
		var $legend = $('#categoryLegend');
		labels.forEach(function(label, i) {
			// Use data-color attribute + CSS var() instead of inline style (CSP compliance)
			var $dot = $('<span class="chart-legend-dot"></span>')
				.attr('data-color', colors[i]);
			// Apply color via DOM property (not setAttribute style)
			$dot[0].style.setProperty('background-color', colors[i]);
			var $item = $('<div class="chart-legend-item"></div>');
			$item.append($dot);
			$item.append('<span>' + label + ' (' + data[i] + '%)</span>');
			$legend.append($item);
		});
	}

	/* ── 最近簽核 DataTable ─────────────────────────── */
	function _initRecentTable() {
		if (!$.fn.DataTable || !$('#recentTable').length) return;
		Gov.DataTable.init('#recentTable', {
			pageLength: 5,
			lengthMenu: [[5, 10, 25], [5, 10, 25]],
			searching: false,
			dom:
				"<'row'<'col-12'tr>>" +
				"<'row align-items-center mt-2 px-2'<'col-sm-5'i><'col-sm-7 d-flex justify-content-sm-end'p>>"
		});
	}

}(jQuery));
