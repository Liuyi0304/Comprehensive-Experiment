/* app.js */
App({
  onLaunch: function () {
    // 启动时尝试从缓存恢复用户信息，避免每次刷新都要重新登录
    this.restoreUserSession();
  },

  // 从本地缓存恢复用户会话
  restoreUserSession: function () {
    const userInfo = wx.getStorageSync('userInfo');
    if (userInfo) {
      this.globalData.userInfo = userInfo;
      console.log("已从缓存恢复用户信息:", userInfo.realName);
    }
  },

  // 退出登录调用的全局方法
  logout: function() {
    this.globalData.userInfo = null;
    wx.removeStorageSync('userInfo');
    wx.reLaunch({
      url: '/pages/login/login',
    });
  },
  globalData: {
    appid: 'wx3f8fc936d4efaf55',
    openid: wx.getStorageSync('openid') || null,
    serverUrl: "http://192.168.228.253:8080",
    userInfo: null ,
    wxUserInfo: wx.getStorageSync('wxUserInfo') || null
  }
});