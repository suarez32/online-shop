/* ============================================================
   公共工具：Toast 提示、格式化、状态字典、通用常量
   单品小铺 · 迭代1 · 邱圣洁
   ============================================================ */
(function (global) {
  'use strict';

  /* ---------- Toast 轻提示 ---------- */
  let toastTimer = null;
  function showToast(message, type) {
    let el = document.getElementById('toast');
    if (!el) {
      el = document.createElement('div');
      el.id = 'toast';
      document.body.appendChild(el);
    }
    el.className = 'toast toast-' + (type || 'info');
    el.textContent = message;
    el.hidden = false;
    clearTimeout(toastTimer);
    toastTimer = setTimeout(function () { el.hidden = true; }, 3200);
  }

  /* ---------- HTML 转义（防 XSS） ---------- */
  function escapeHtml(value) {
    return String(value == null ? '' : value)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#39;');
  }

  /* ---------- 格式化 ---------- */
  function formatPrice(value) {
    var n = Number(value);
    return Number.isFinite(n) ? n.toFixed(2) : String(value == null ? '' : value);
  }

  function pad2(n) { return n < 10 ? '0' + n : '' + n; }

  function formatTime(iso) {
    if (!iso) return '-';
    var d = new Date(iso);
    if (isNaN(d.getTime())) return String(iso);
    return d.getFullYear() + '-' + pad2(d.getMonth() + 1) + '-' + pad2(d.getDate()) +
      ' ' + pad2(d.getHours()) + ':' + pad2(d.getMinutes()) + ':' + pad2(d.getSeconds());
  }

  /* ---------- 状态字典（与《接口设计》Wiki 枚举一致） ---------- */
  var PRODUCT_BADGE = {
    ON_SALE: ['在售', 'badge-on-sale'],
    FROZEN: ['已冻结 · 交易中', 'badge-frozen'],
    OFF_SHELF: ['已下架', 'badge-offshelf']
  };

  var INTENT_BADGE = {
    WAITING: ['排队中', 'badge-waiting'],
    IN_TRADE: ['交易中', 'badge-intrade'],
    TRADED: ['交易成功', 'badge-traded'],
    FAILED: ['交易失败', 'badge-failed'],
    CANCELLED: ['已撤销', 'badge-cancelled']
  };

  function badgeHtml(map, key) {
    var item = map[key] || [key || '未知', 'badge-offshelf'];
    return '<span class="badge ' + item[1] + '">' + escapeHtml(item[0]) + '</span>';
  }

  /* ---------- 图片地址解析 ----------
     后端可能返回：dataURL / http(s) 绝对地址 / /uploads 下的文件名 */
  function resolveImageUrl(img) {
    if (!img) return '';
    if (/^(data:|https?:|\/\/)/i.test(String(img))) return img;
    return global.Api ? global.Api.uploadUrl(img) : String(img);
  }

  /* 无图占位（灰色相机块） */
  var PLACEHOLDER_IMAGE =
    "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 400 300'%3E" +
    "%3Crect width='400' height='300' fill='%23eef0f6'/%3E" +
    "%3Cpath d='M168 118h18l10-16h28l10 16h18a12 12 0 0 1 12 12v76a12 12 0 0 1-12 12H168a12 12 0 0 1-12-12v-76a12 12 0 0 1 12-12z' fill='%23c3c8d9'/%3E" +
    "%3Ccircle cx='200' cy='162' r='19' fill='%23eef0f6'/%3E" +
    "%3C/svg%3E";

  function bindImageFallback(imgEl) {
    imgEl.addEventListener('error', function () {
      imgEl.src = PLACEHOLDER_IMAGE;
    });
  }

  /* ---------- 按钮加载态 ---------- */
  function setLoading(btn, loading, loadingText) {
    if (!btn) return;
    if (loading) {
      if (!btn.dataset.origText) btn.dataset.origText = btn.textContent;
      btn.classList.add('btn-loading');
      if (loadingText) btn.dataset.origText = loadingText, btn.textContent = loadingText;
      btn.disabled = true;
    } else {
      btn.classList.remove('btn-loading');
      if (btn.dataset.origText) btn.textContent = btn.dataset.origText;
      btn.disabled = false;
    }
  }

  /* ---------- 口令码格式（8 位，剔除易混字符 I/O/0/1） ---------- */
  var CODE_PATTERN = /^[A-HJ-NP-Z2-9]{8}$/;

  global.App = {
    showToast: showToast,
    escapeHtml: escapeHtml,
    formatPrice: formatPrice,
    formatTime: formatTime,
    badgeHtml: badgeHtml,
    PRODUCT_BADGE: PRODUCT_BADGE,
    INTENT_BADGE: INTENT_BADGE,
    resolveImageUrl: resolveImageUrl,
    PLACEHOLDER_IMAGE: PLACEHOLDER_IMAGE,
    bindImageFallback: bindImageFallback,
    setLoading: setLoading,
    CODE_PATTERN: CODE_PATTERN
  };
})(window);
