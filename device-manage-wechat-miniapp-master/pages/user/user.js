// pages/user/user.js
const app = getApp()

Page({
  /**
   * 页面的初始数据
   */
  data: {
    binBtnHide: false,
    isBind: false,
    openid: null,
    employeeID: null,
    employeeMobile: null,
    employeeName: null,
    wxNickName: null,
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad: function (options) {
    console.log("options:", options);

    var isBind = options.isBind === 'true';

    wx.setNavigationBarTitle({
      title: '个人信息',
    })

    this.setData({
      openid: options.openid,
      isBind: isBind,
      wxNickName: options.wxNickName,
    })

    if (isBind) {
      this.setData({
        employeeID: options.employeeID,
        employeeMobile: options.employeeMobile, // 原代码中此处误写为 employeeID，已修正
        employeeName: options.employeeName,
      })
    }
  },

  // 触发解绑弹窗
  bindLogout: function (e) {
    var that = this;
    wx.showModal({
      title: '解除绑定',
      content: '您确定要解除当前微信与员工信息的绑定吗？',
      confirmColor: '#FF0000',
      success: function (res) {
        if (res.confirm) {
          that.deleteBindOpenId(that.data.openid);
        }
      }
    })
  },

  // 执行解绑请求（对接 MySQL 后端）
  deleteBindOpenId: function (openid) {
    var that = this;
    if (!openid) return;

    wx.showLoading({
      title: '正在处理...',
      mask: true,
    })

    wx.request({
      url: 'http://192.168.98.211:8080/api/employee/unbind', // 你的 Spring Boot 接口
      method: 'POST',
      header: {
        'content-type': 'application/x-www-form-urlencoded'
      },
      data: {
        openid: openid
      },
      success: function (res) {
        wx.hideLoading();
        
        // 假设后端返回 code 200 表示成功
        if (res.data && res.data.code === 200) {
          wx.showToast({
            title: '解绑成功',
            icon: 'success',
            duration: 1500
          });

          // 通知首页更新状态
          let pages = getCurrentPages();
          if (pages.length >= 2) {
            let prevPage = pages[pages.length - 2];
            // 调用首页的 cleanBindInfo 方法
            if (prevPage.cleanBindInfo) {
              prevPage.cleanBindInfo();
            }
          }

          // 延迟返回，让 Toast 显示完
          setTimeout(() => {
            wx.navigateBack({ delta: 1 });
          }, 1500);

        } else {
          wx.showToast({
            title: res.data.msg || '解绑失败',
            icon: 'none'
          });
        }
      },
      fail: function (error) {
        wx.hideLoading();
        wx.showToast({
          title: '网络请求失败',
          icon: 'none'
        });
      }
    });
  },
})