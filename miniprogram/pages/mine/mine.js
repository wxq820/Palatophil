const { wxLogin } = require('../../utils/auth');
const api = require('../../api/index');

Page({
  data: {
    user: {}
  },

  onShow() {
    this.setData({ user: getApp().globalData.user || {} });
  },

  async onRefresh() {
    if (!getApp().globalData.token) {
      try { await wxLogin(); } catch (e) { return; }
    }
    try {
      const user = await api.getCurrentUser();
      getApp().setLogin(getApp().globalData.token, user);
      this.setData({ user });
      wx.showToast({ title: '已刷新', icon: 'success' });
    } catch (e) {}
  },

  onLogout() {
    getApp().logout();
    wx.showToast({ title: '已退出', icon: 'success' });
    this.setData({ user: {} });
  }
});
