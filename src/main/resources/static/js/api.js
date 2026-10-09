/* ============================================================
   API 层：对接《接口设计》Wiki 冻结的 18 个 REST 接口
   单品小铺 · 迭代1 · 邱圣洁

   约定：
   - 统一响应体 { code, message, data }，UTF-8，时间 ISO-8601
   - 卖家鉴权：请求头 X-Token（登录后存 localStorage）
   - 买家无账号体系，口令码是唯一凭证（仅提交时返回一次）
   ============================================================ */
(function (global) {
  'use strict';

  var TOKEN_KEY = 'seller_token';
  var USERNAME_KEY = 'seller_username';

  /* ---------- 错误对象 ---------- */
  function ApiError(status, message) {
    var err = new Error(message);
    err.name = 'ApiError';
    err.status = status;
    return err;
  }

  /* ---------- Token 管理 ---------- */
  function getToken() {
    try { return localStorage.getItem(TOKEN_KEY) || ''; } catch (e) { return ''; }
  }
  function getUsername() {
    try { return localStorage.getItem(USERNAME_KEY) || ''; } catch (e) { return ''; }
  }
  function setToken(token, username) {
    try {
      if (token) {
        localStorage.setItem(TOKEN_KEY, token);
        if (username !== undefined) {
          if (username) { localStorage.setItem(USERNAME_KEY, username); }
          else { localStorage.removeItem(USERNAME_KEY); }
        }
      } else {
        localStorage.removeItem(TOKEN_KEY);
        localStorage.removeItem(USERNAME_KEY);
      }
    } catch (e) { /* 隐私模式下忽略 */ }
  }

  /* ---------- 默认错误文案（按 HTTP 状态码） ---------- */
  function defaultErrorMessage(status) {
    switch (status) {
      case 400: return '请求参数有误，或当前状态不允许该操作';
      case 401: return '未登录或登录已失效';
      case 404: return '请求的资源不存在';
      case 409: return '状态冲突，请刷新后重试';
      default:  return '服务器开小差了，请稍后重试';
    }
  }

  /* ---------- 核心请求 ----------
     兼容两种后端返回：
     a) HTTP 200 + { code:200, message, data }   （统一包装）
     b) HTTP 200 + { ...业务字段 }               （未包装）        */
  async function request(path, options) {
    var opt = options || {};
    var method = opt.method || 'GET';
    var headers = {};
    if (opt.body !== undefined) headers['Content-Type'] = 'application/json';
    if (opt.auth) {
      var t = getToken();
      if (t) headers['X-Token'] = t;
    }

    var resp;
    try {
      resp = await fetch(path, {
        method: method,
        headers: headers,
        body: opt.body === undefined ? undefined : JSON.stringify(opt.body)
      });
    } catch (err) {
      throw ApiError(0, '无法连接服务器：请确认后端已启动（当前为静态骨架模式）');
    }

    var payload = null;
    var text = await resp.text().catch(function () { return ''; });
    if (text) {
      try { payload = JSON.parse(text); } catch (e) { payload = null; }
    }

    if (!resp.ok) {
      var msg1 = (payload && payload.message) || defaultErrorMessage(resp.status);
      throw ApiError(resp.status, msg1);
    }

    if (payload && typeof payload === 'object' && Object.prototype.hasOwnProperty.call(payload, 'code')) {
      if (typeof payload.code === 'number' && payload.code >= 400) {
        throw ApiError(payload.code, payload.message || defaultErrorMessage(payload.code));
      }
      return Object.prototype.hasOwnProperty.call(payload, 'data') ? payload.data : payload;
    }
    return payload;
  }

  /* ---------- 接口清单（与 Wiki《接口设计》一一对应） ---------- */
  var api = {
    ApiError: ApiError,
    getToken: getToken,
    getUsername: getUsername,
    setToken: setToken,
    isAuthError: function (e) { return !!e && e.status === 401; },

    /* 静态资源：商品图片 */
    uploadUrl: function (file) { return '/uploads/' + encodeURIComponent(file); },

    /* ==================== 买家端 ==================== */

    /* 1. 当前在售商品（无则 product 为 null） */
    getCurrentProduct: function () {
      return request('/api/product');
    },

    /* 2. 提交购买意向，返回口令码（仅此一次） */
    submitIntent: function (name, phone) {
      return request('/api/intents', { method: 'POST', body: { name: name, phone: phone } });
    },

    /* 3. 凭口令码查询排队位次与状态 */
    getIntent: function (code) {
      return request('/api/intents/' + encodeURIComponent(code));
    },

    /* 4. 修改意向姓名/电话（位次不变）—— 后端 10-15 评审后就绪 */
    updateIntent: function (code, name, phone) {
      return request('/api/intents/' + encodeURIComponent(code), {
        method: 'PUT', body: { name: name, phone: phone }
      });
    },

    /* 5. 撤销意向（交易中不可撤）—— 后端 10-15 评审后就绪 */
    cancelIntent: function (code) {
      return request('/api/intents/' + encodeURIComponent(code) + '/cancel', { method: 'POST' });
    },

    /* ==================== 卖家端 ==================== */

    /* 6. 登录，返回 { token, username } */
    login: function (username, password) {
      return request('/api/seller/login', {
        method: 'POST', body: { username: username, password: password }
      });
    },

    /* 7. 退出登录，token 失效 */
    logout: function () {
      return request('/api/seller/logout', { method: 'POST', auth: true });
    },

    /* 8. 修改密码 —— 后端 10-15 评审后就绪 */
    changePassword: function (oldPassword, newPassword) {
      return request('/api/seller/password', {
        method: 'POST', auth: true, body: { oldPassword: oldPassword, newPassword: newPassword }
      });
    },

    /* 9. 后台首页聚合：{ product, queue[], canPublish } */
    getOverview: function () {
      return request('/api/seller/overview', { auth: true });
    },

    /* 10. 发布商品 { name, description, price, image? } */
    publishProduct: function (product) {
      return request('/api/seller/products', { method: 'POST', auth: true, body: product });
    },

    /* 11. 在售与历史商品列表 */
    listProducts: function () {
      return request('/api/seller/products', { auth: true });
    },

    /* 12. 手动冻结 —— 后端 10-15 评审后就绪 */
    freezeProduct: function (id) {
      return request('/api/seller/products/' + id + '/freeze', { method: 'POST', auth: true });
    },

    /* 13. 手动解冻（仅 MANUAL 可解）—— 后端 10-15 评审后就绪 */
    unfreezeProduct: function (id) {
      return request('/api/seller/products/' + id + '/unfreeze', { method: 'POST', auth: true });
    },

    /* 14. 选定买家发起交易 { intentId } —— 后端 10-15 评审后就绪 */
    dealIntent: function (productId, intentId) {
      return request('/api/seller/products/' + productId + '/deal', {
        method: 'POST', auth: true, body: { intentId: intentId }
      });
    },

    /* 15. 标记交易结果 SUCCESS / FAIL —— 后端 10-15 评审后就绪 */
    markResult: function (productId, result) {
      return request('/api/seller/products/' + productId + '/result', {
        method: 'POST', auth: true, body: { result: result }
      });
    },

    /* 16. 手动下架 —— 后端 10-15 评审后就绪 */
    offshelfProduct: function (id) {
      return request('/api/seller/products/' + id + '/offshelf', { method: 'POST', auth: true });
    },

    /* 17. 历史商品（已售 + 交易失败）—— 后端 10-15 评审后就绪 */
    getHistory: function () {
      return request('/api/seller/history', { auth: true });
    }
  };

  global.Api = api;
})(window);
