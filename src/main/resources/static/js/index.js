/* ============================================================
   买家端逻辑：商品展示 / 提交意向 / 口令码展示 / 凭码查询
   对应接口：GET /api/product、POST /api/intents、GET /api/intents/{code}
   单品小铺 · 迭代1 · 邱圣洁
   ============================================================ */
(function () {
  'use strict';

  var App = window.App;
  var Api = window.Api;
  var $ = function (id) { return document.getElementById(id); };

  var els = {
    /* 商品区 */
    productLoading: $('product-loading'),
    productEmpty: $('product-empty'),
    productBody: $('product-body'),
    productImage: $('product-image'),
    productName: $('product-name'),
    productDesc: $('product-desc'),
    productPrice: $('product-price'),
    productStatus: $('product-status'),
    offlineTip: $('offline-tip'),
    /* 意向表单 */
    intentEmptyTip: $('intent-empty-tip'),
    intentFrozenTip: $('intent-frozen-tip'),
    intentForm: $('intent-form'),
    intentName: $('intent-name'),
    intentPhone: $('intent-phone'),
    nameError: $('intent-name-error'),
    phoneError: $('intent-phone-error'),
    intentSubmit: $('intent-submit'),
    /* 口令码 */
    codeCard: $('code-card'),
    codeText: $('code-text'),
    copyBtn: $('copy-btn'),
    toQueryBtn: $('to-query-btn'),
    /* 凭码查询 */
    queryForm: $('query-form'),
    queryCode: $('query-code'),
    queryCodeError: $('query-code-error'),
    querySubmit: $('query-submit'),
    queryResult: $('query-result')
  };

  function show(el, on) { if (el) el.hidden = !on; }

  function fieldError(el, msg) {
    if (!msg) { show(el, false); return; }
    el.textContent = msg;
    show(el, true);
  }

  /* ==================== 商品展示 ==================== */

  async function loadProduct() {
    show(els.productLoading, true);
    show(els.productEmpty, false);
    show(els.productBody, false);
    show(els.offlineTip, false);

    var product = null;
    try {
      var data = await Api.getCurrentProduct();
      product = (data && data.product) || null;
    } catch (e) {
      if (e.status === 0) {
        show(els.offlineTip, true); /* 骨架模式：后端未启动 */
      } else {
        App.showToast(e.message, 'error');
      }
      product = null;
    }
    renderProduct(product);
  }

  function renderProduct(p) {
    show(els.productLoading, false);
    if (!p) {
      show(els.productEmpty, true);
      setIntentState('empty');
      return;
    }

    els.productName.textContent = p.name || '未命名商品';
    els.productDesc.textContent = p.description || '店主还没有写描述～';
    els.productPrice.textContent = App.formatPrice(p.price);
    els.productStatus.innerHTML = App.badgeHtml(App.PRODUCT_BADGE, p.status);

    var img = App.resolveImageUrl(p.imageUrl || p.image);
    els.productImage.src = img || App.PLACEHOLDER_IMAGE;
    App.bindImageFallback(els.productImage);

    show(els.productBody, true);
    setIntentState(p.status === 'ON_SALE' ? 'on_sale' : 'frozen');
  }

  /* 意向卡片三态：可提交 / 商品冻结 / 无商品 */
  function setIntentState(state) {
    show(els.intentForm, state === 'on_sale');
    show(els.intentFrozenTip, state === 'frozen');
    show(els.intentEmptyTip, state === 'empty');
    if (state !== 'on_sale') {
      els.intentName.value = '';
      els.intentPhone.value = '';
      fieldError(els.nameError);
      fieldError(els.phoneError);
    }
  }

  /* ==================== 提交意向 ==================== */

  els.intentForm.addEventListener('submit', async function (e) {
    e.preventDefault();

    var name = els.intentName.value.trim();
    var phone = els.intentPhone.value.trim();
    var valid = true;

    if (!name) { fieldError(els.nameError, '请输入姓名'); valid = false; }
    else fieldError(els.nameError);

    if (!phone) { fieldError(els.phoneError, '请输入联系电话'); valid = false; }
    else fieldError(els.phoneError);

    if (!valid) return;

    App.setLoading(els.intentSubmit, true);
    try {
      var data = await Api.submitIntent(name, phone);
      var code = data && data.code;
      if (!code) throw Api.ApiError(0, '接口未返回口令码，请联系后端确认');

      /* 口令码仅此一次展示 */
      els.codeText.textContent = String(code).toUpperCase();
      show(els.codeCard, true);
      show(els.intentForm, false);
      els.queryCode.value = String(code).toUpperCase(); /* 顺手预填查询框 */
      els.codeCard.scrollIntoView({ behavior: 'smooth', block: 'center' });
      App.showToast('意向提交成功，请保存好口令码', 'success');
    } catch (err) {
      App.showToast(err.message, 'error');
      /* 商品状态可能在填表期间变化，刷新一下 */
      if (err.status === 400) loadProduct();
    } finally {
      App.setLoading(els.intentSubmit, false);
    }
  });

  /* 复制口令码 */
  els.copyBtn.addEventListener('click', async function () {
    var code = els.codeText.textContent.trim();
    try {
      await navigator.clipboard.writeText(code);
      App.showToast('口令码已复制到剪贴板', 'success');
    } catch (e) {
      /* 兼容不支持 Clipboard API 的环境 */
      var ta = document.createElement('textarea');
      ta.value = code;
      document.body.appendChild(ta);
      ta.select();
      try {
        document.execCommand('copy');
        App.showToast('口令码已复制到剪贴板', 'success');
      } catch (e2) {
        App.showToast('复制失败，请手动抄写', 'error');
      }
      document.body.removeChild(ta);
    }
  });

  /* 跳到查询区 */
  els.toQueryBtn.addEventListener('click', function () {
    els.queryCard = els.queryCard || $('query-card');
    els.queryCard.scrollIntoView({ behavior: 'smooth', block: 'center' });
    setTimeout(function () { els.queryCode.focus(); }, 350);
  });

  /* ==================== 凭码查询 ==================== */

  els.queryCode.addEventListener('input', function () {
    this.value = this.value.toUpperCase().replace(/\s+/g, '');
    fieldError(els.queryCodeError);
  });

  els.queryForm.addEventListener('submit', async function (e) {
    e.preventDefault();

    var code = els.queryCode.value.trim().toUpperCase();
    if (!code) {
      fieldError(els.queryCodeError, '请输入 8 位口令码');
      return;
    }
    if (!App.CODE_PATTERN.test(code)) {
      fieldError(els.queryCodeError, '口令码格式不对：应为 8 位大写字母或数字（不含 I / O / 0 / 1）');
      return;
    }

    App.setLoading(els.querySubmit, true);
    try {
      var data = await Api.getIntent(code);
      renderQueryResult(data);
    } catch (err) {
      if (err.status === 404) {
        renderQueryError('口令码无效或已失效：交易成功、交易失败、已撤销的意向无法查询。');
      } else {
        App.showToast(err.message, 'error');
      }
    } finally {
      App.setLoading(els.querySubmit, false);
    }
  });

  function renderQueryResult(data) {
    var intent = (data && data.intent) || {};
    var product = (data && data.product) || {};

    var positionHtml;
    if (intent.status === 'IN_TRADE') {
      positionHtml = '已进入交易环节';
    } else if (intent.position !== null && intent.position !== undefined) {
      positionHtml = '排队第 <span class="position-num">' + App.escapeHtml(intent.position) + '</span> 位';
    } else {
      positionHtml = '排队中';
    }

    els.queryResult.innerHTML =
      '<div class="qr-head">' +
        App.badgeHtml(App.INTENT_BADGE, intent.status) +
        '<div class="qr-position">' + positionHtml + '</div>' +
      '</div>' +
      '<dl class="qr-meta">' +
        '<div><dt>商品</dt><dd>' + App.escapeHtml(product.name || '-') + '</dd></div>' +
        '<div><dt>意向状态</dt><dd>' + App.escapeHtml((App.INTENT_BADGE[intent.status] || [intent.status || '未知'])[0]) + '</dd></div>' +
        '<div><dt>姓名</dt><dd>' + App.escapeHtml(intent.name || '-') + '</dd></div>' +
        '<div><dt>联系电话</dt><dd>' + App.escapeHtml(intent.phone || '-') + '</dd></div>' +
        '<div><dt>提交时间</dt><dd>' + App.escapeHtml(App.formatTime(intent.submittedAt)) + '</dd></div>' +
      '</dl>';
    show(els.queryResult, true);
  }

  function renderQueryError(message) {
    els.queryResult.innerHTML = '<div class="banner banner-error">' + App.escapeHtml(message) + '</div>';
    show(els.queryResult, true);
  }

  /* ==================== 启动 ==================== */
  loadProduct();
})();
