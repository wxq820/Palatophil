// api/index.js
const { request } = require('../utils/request');

module.exports = {
  // 用户
  getCurrentUser() {
    return request({ url: '/api/auth/me' });
  },
  wxLogin(code, nickname) {
    return request({ url: '/api/auth/wx-login', method: 'POST', data: { code, nickname }, needAuth: false });
  },
  health() {
    return request({ url: '/api/health', needAuth: false });
  },

  // 食材库
  ingredientPage({ page = 1, size = 20, keyword, category, systemOnly, auditStatus } = {}) {
    return request({
      url: '/api/ingredients',
      data: { page, size, keyword, category, systemOnly, auditStatus }
    });
  },
  ingredientDetail(id) {
    return request({ url: `/api/ingredients/${id}` });
  },
  ingredientCreate(data) {
    return request({ url: '/api/ingredients', method: 'POST', data });
  },
  ingredientUpdate(id, data) {
    return request({ url: `/api/ingredients/${id}`, method: 'PUT', data });
  },
  ingredientDelete(id) {
    return request({ url: `/api/ingredients/${id}`, method: 'DELETE' });
  },

  // 食谱
  recipePage({ page = 1, size = 20, keyword, visibility, auditStatus } = {}) {
    return request({
      url: '/api/recipes',
      data: { page, size, keyword, visibility, auditStatus }
    });
  },
  recipeDetail(id) {
    return request({ url: `/api/recipes/${id}` });
  },
  recipeCreate(data) {
    return request({ url: '/api/recipes', method: 'POST', data });
  },
  recipeUpdate(id, data) {
    return request({ url: `/api/recipes/${id}`, method: 'PUT', data });
  },
  recipeDelete(id) {
    return request({ url: `/api/recipes/${id}`, method: 'DELETE' });
  },
  recipeCopy(id) {
    return request({ url: `/api/recipes/${id}/copy`, method: 'POST' });
  }
};
