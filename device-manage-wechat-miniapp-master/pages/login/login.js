const app = getApp();

Page({
  data: {
    username: '',
    password: ''
  },

  onUsernameInput(e) { this.setData({ username: e.detail.value }); },
  onPasswordInput(e) { this.setData({ password: e.detail.value }); },

  handleLogin: function() {
    const { username, password } = this.data;

    if (!username || !password) {
      wx.showToast({ title: '请输入完整信息', icon: 'none' });
      return;
    }

    wx.showLoading({ title: '验证中...' });

    // 模拟 Web 登录请求
    wx.request({
      url: app.globalData.serverUrl + '/weixinapi/user/login', // 注意：后端需要实现这个登录接口
      method: 'POST',
      data: {
        username: username,
        password: password
      },
      success: (res) => {
        wx.hideLoading();
        
        // 假设后端验证通过并返回了用户信息，包含 role
        if (res.data.code === 200 && res.data.data) {
          const user = res.data.data;
          const role = user.role; // 获取角色: admin, manager, user

          // 将用户信息存入全局，方便后续页面使用
          app.globalData.userInfo = user;

          // 根据角色执行不同的跳转逻辑
          this.redirectByRole(role);
        } else {
          wx.showToast({ title: res.data.msg || '用户名或密码错误', icon: 'none' });
        }
      },
      fail: () => {
        wx.hideLoading();
        wx.showToast({ title: '服务器连接失败', icon: 'none' });
      }
    });
  },

  // 核心路由分发逻辑
  redirectByRole: function(role) {
    switch(role) {
      case 'admin':
        wx.showToast({
          title: `欢迎回来，管理员`,
          icon: 'success',
          duration: 1500
        });        
        wx.reLaunch({ url: '/pages/index/index' });
        break;
      case 'manager':
        wx.showToast({
          title: `欢迎回来，manager`,
          icon: 'success',
          duration: 1500
        });
        wx.reLaunch({ url: '/pages/manager/manager' });
        break;
      case 'user':
        wx.showToast({
          title: `欢迎回来，user`,
          icon: 'success',
          duration: 1500
        });
        wx.reLaunch({ url: '/pages/myuser/myuser' });
        break;
      default:
        wx.showToast({ title: '未知角色', icon: 'none' });
    }
  }
});