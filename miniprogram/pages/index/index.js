// pages/index/index.js
const { wxLogin } = require('../../utils/auth');
const api = require('../../api/index');

Page({
  data: {
    recipes: [],
    loading: false,
    keyword: ''
  },

  onLoad() {
    this.ensureLogin().then(() => this.loadRecipes());
  },

  onShow() {
    if (getApp().globalData.token) {
      this.loadRecipes();
    }
  },

  onPullDownRefresh() {
    this.loadRecipes().then(() => wx.stopPullDownRefresh());
  },

  async ensureLogin() {
    if (!getApp().globalData.token) {
      try { await wxLogin(); } catch (e) { console.warn('登录失败', e); }
    }
  },

  async loadRecipes() {
    if (!getApp().globalData.token) return;
    this.setData({ loading: true });
    try {
      const page = await api.recipePage({ page: 1, size: 50, keyword: this.data.keyword });
      this.setData({ recipes: page.records || [], loading: false });
    } catch (e) {
      this.setData({ loading: false });
    }
  },

  onSearchInput(e) {
    this.setData({ keyword: e.detail.value });
  },

  onSearchConfirm() {
    this.loadRecipes();
  },

  onCreateRecipe() {
    wx.navigateTo({ url: '/pages/recipe-edit/recipe-edit' });
  },

  onRecipeTap(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({ url: `/pages/recipe-edit/recipe-edit?id=${id}` });
  }
});
