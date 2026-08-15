// utils/auth.js
const { request } = require('./request');

function wxLogin() {
  return new Promise((resolve, reject) => {
    wx.login({
      success: async (loginRes) => {
        if (!loginRes.code) return reject(new Error('wx.login 失败'));
        try {
          const data = await request({
            url: '/api/auth/wx-login',
            method: 'POST',
            data: { code: loginRes.code, nickname: '微信用户', avatar: '' },
            needAuth: false
          });
          getApp().setLogin(data.token, data);
          resolve(data);
        } catch (e) { reject(e); }
      },
      fail: reject
    });
  });
}

module.exports = { wxLogin };
