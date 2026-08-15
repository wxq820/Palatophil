// utils/request.js
// 统一请求封装：自动注入 JWT
const app = getApp();

function request({ url, method = 'GET', data, header = {}, needAuth = true }) {
  return new Promise((resolve, reject) => {
    if (needAuth && app.globalData.token) {
      header.Authorization = 'Bearer ' + app.globalData.token;
    }
    wx.request({
      url: app.globalData.baseUrl + url,
      method,
      data,
      header,
      success: (res) => {
        const body = res.data || {};
        if (res.statusCode === 200 && body.code === 0) {
          resolve(body.data);
        } else if (res.statusCode === 401) {
          app.logout();
          wx.showToast({ title: '请重新登录', icon: 'none' });
          reject(body);
        } else {
          wx.showToast({ title: body.message || '请求失败', icon: 'none' });
          reject(body);
        }
      },
      fail: (err) => {
        wx.showToast({ title: '网络错误', icon: 'none' });
        reject(err);
      }
    });
  });
}

module.exports = { request };
