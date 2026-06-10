(function(Gov, $) {
	'use strict';

	Gov.Dialog = {

		confirm: function(options) {
			return _showModal({
				type: options.type || 'warning',
				title: options.title || '系統提示',
				message: options.message || '',
				showCancel: true
			});
		},

		alert: function(options) {
			return _showModal({
				type: options.type || 'info',
				title: options.title || '系統提示',
				message: options.message || '',
				showCancel: false
			});
		}
	};

	/* ── Private ───────────────────────── */

	function _getTypeClass(type) {
		switch(type) {
			case 'success': return 'text-bg-success';
			case 'error': return 'text-bg-danger';
			case 'warning': return 'text-bg-warning';
			default: return 'text-bg-info';
		}
	}

	function _showModal(opts) {
		return new Promise(function(resolve) {

			var id = 'gov-modal-' + Date.now();

			var html =
				'<div class="modal fade" id="' + id + '" tabindex="-1">' +
				'  <div class="modal-dialog modal-dialog-centered">' +
				'    <div class="modal-content">' +
				'      <div class="modal-header ' + _getTypeClass(opts.type) + '">' +
				'        <h5 class="modal-title">' + opts.title + '</h5>' +
				'        <button type="button" class="btn-close" data-bs-dismiss="modal"></button>' +
				'      </div>' +
				'      <div class="modal-body">' +
				'        <p>' + opts.message + '</p>' +
				'      </div>' +
				'      <div class="modal-footer">' +
				(opts.showCancel ? '<button class="btn btn-secondary btn-cancel">取消</button>' : '') +
				'        <button class="btn btn-primary btn-confirm">確定</button>' +
				'      </div>' +
				'    </div>' +
				'  </div>' +
				'</div>';

			var $modal = $(html).appendTo('body');
			var modal = new bootstrap.Modal($modal[0]);

			modal.show();

			// 確定
			$modal.on('click', '.btn-confirm', function() {
				resolve(true);
				modal.hide();
			});

			// 取消
			$modal.on('click', '.btn-cancel', function() {
				resolve(false);
				modal.hide();
			});

			// 關閉
			$modal.on('hidden.bs.modal', function() {
				$modal.remove();
			});
		});
	}

}(window.Gov = window.Gov || {}, jQuery));