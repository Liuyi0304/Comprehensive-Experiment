// 1. 去掉原来的 AV (LeanCloud) 引用，确保引用你的 request.js
//const request = require('../../../utils/request.js')
const app = getApp()

Component({
  properties: {
    showModalStatus: Boolean,
    openid: String,
  },

  data: {
    employeeMobile: null,
    employeeCode: null,
    employeeMobileFocus: false,
    employeeCodeFocus: false,
  },

  methods: {
    bindCloseEvent: function (e) {
      var currentStatu = e.currentTarget.dataset.statu;
      this.util(currentStatu)
    },

    // 确认绑定按钮
    bindOKEvent: function (e) {
      if (!this.data.employeeMobile) {
        wx.showToast({ title: '请填写手机号', icon: 'none' });
        this.setData({ employeeMobileFocus: true });
        return;
      }
      if (!this.data.employeeCode) {
        wx.showToast({ title: '请填写员工编号', icon: 'none' });
        this.setData({ employeeCodeFocus: true });
        return;
      }

      wx.showLoading({ title: '验证中...', mask: true });
      var that = this;

      // --- 逻辑改动：直接调用 SpringBoot 接口进行绑定 ---
      // 这里建议直接调用一个“绑定”接口，把 openid 和用户信息一起传过去
      wx.request({
        url: app.globalData.serverUrl + '/api/user/bind', // 你的后端接口
        method: 'POST',
        data: {
          openid: this.properties.openid,
          mobile: this.data.employeeMobile,
          employeeCode: this.data.employeeCode
        },
        success: function (res) {
          wx.hideLoading();
          if (res.data.code === 200) {
            // 后端验证并写入 MySQL 成功
            wx.showToast({ title: '绑定成功!', icon: 'success' });
            
            // 关闭弹窗
            that.util("close");

            // 将绑定后的信息回调给父页面（index.js）
            // 这样首页就能显示“我的设备”等信息了
            that.triggerEvent('successEvent', res.data.data, null);
          } else {
            // 后端返回错误（如手机号不匹配）
            wx.showToast({
              title: res.data.message || '绑定失败',
              icon: 'none',
              duration: 2000
            });
          }
        },
        fail: function () {
          wx.hideLoading();
          wx.showToast({ title: '服务器连接失败', icon: 'none' });
        }
      });
    },

    // 输入框监听
    bindEmployeeMobileChange: function (e) {
      this.setData({ employeeMobile: e.detail.value })
    },
    bindEmployeeCodeChange: function (e) {
      this.setData({ employeeCode: e.detail.value })
    },

    // 弹窗动画逻辑（保持不变）
    util: function (currentStatu) {
      var animation = wx.createAnimation({
        duration: 200,
        timingFunction: "linear",
        delay: 0
      });
      this.animation = animation;
      animation.opacity(0).rotateX(-100).step();
      this.setData({ animationData: animation.export() })
      setTimeout(function () {
        animation.opacity(1).rotateX(0).step();
        this.setData({ animationData: animation })
        if (currentStatu == "close") {
          this.setData({ showModalStatus: false });
        }
      }.bind(this), 200)
      if (currentStatu == "open") {
        this.setData({ showModalStatus: true });
      }
    },
  }
})