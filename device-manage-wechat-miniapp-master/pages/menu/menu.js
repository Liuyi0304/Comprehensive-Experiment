// pages/menu/menu.js
const app = getApp()

Page({
  /**
   * 页面的初始数据
   */
  data: {
    employeeId: null, // 对应 MySQL 中的员工 ID
    routers: [
      {
        name: '共享记录',
        url: '../log/log',
        icon: '../images/record.png'
      },
      {
        name: '组别',
        url: '../group/group',
        icon: '../images/group2.png'
      },
      {
        name: '',
        url: '',
        icon: ''
      },
    ]
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad: function (options) {
    // 接收从 index 传过来的员工 ID
    this.setData({
      employeeId: options.employeeObjectID || options.employeeId,
    })
  },

  /**
   * 生命周期函数--监听页面初次渲染完成
   */
  onReady: function () {
    this.checkAdminStatus();
  },

  /**
   * 检查管理员权限
   */
  checkAdminStatus: function () {
    var that = this;
    if (!this.data.employeeId) return;

    wx.request({
      url: 'http://192.168.98.211:8080/api/employee/isAdmin', // 你的 Spring Boot 接口
      method: 'GET',
      data: {
        employeeId: that.data.employeeId
      },
      success: function (res) {
        // 假设后端返回 { code: 200, data: true } 表示是管理员
        if (res.data && res.data.data === true) {
          console.log('确认管理员权限');
          var routers = that.data.routers;
          
          // 动态修改菜单项
          routers[1] = {
            name: '管理员设置',
            url: '../admin/admin',
            icon: '../images/guanliyuan.png'
          };

          that.setData({
            routers: routers,
          });
        }
      },
      fail: function () {
        console.error('权限检查接口请求失败');
      }
    });
  }
})