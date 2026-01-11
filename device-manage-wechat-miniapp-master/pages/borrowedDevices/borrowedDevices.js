/* borrowedDevices.js */
const app = getApp()

Page({
  data: {
    employeeId: null, // 改为更符合 MySQL 风格的名称
    devices: [],
    showEmptyView: false,
  },

  onLoad: function (options) {
    wx.setNavigationBarTitle({
      title: '借入设备',
    });

    // 接收从上个页面传来的员工 ID
    this.setData({
      employeeId: options.employeeId || options.employeeObjectID, 
    })
    this.getBorrowedDevices();
  },

  // 保留原有的时间格式化函数（这是纯逻辑，不需要改）
  formatDateTime: function (inputTime) {
    var date = new Date(inputTime);
    var y = date.getFullYear();
    var m = date.getMonth() + 1;
    m = m < 10 ? ('0' + m) : m;
    var d = date.getDate();
    d = d < 10 ? ('0' + d) : d;
    var h = date.getHours();
    h = h < 10 ? ('0' + h) : h;
    var minute = date.getMinutes();
    minute = minute < 10 ? ('0' + minute) : minute;
    return y + '-' + m + '-' + d + ' ' + h + ':' + minute;
  },

  // 保留原有的折叠/展开逻辑
  bindTapExpand: function (e) {
    var tapIndex = e.currentTarget.dataset.index;
    var devices = this.data.devices;
    var device = devices[tapIndex];
    device.isExpand = !device.isExpand;
    this.setData({
      devices: devices
    });
  },

  // --- 核心修改：改为连接本地 Spring Boot ---
  getBorrowedDevices: function () {
    var that = this;
    
    wx.request({
      url: app.globalData.serverUrl + '/api/devices/borrowed', // 你的 Spring Boot 接口
      method: 'GET',
      data: {
        employeeId: that.data.employeeId // 传递员工 ID 查询他借的设备
      },
      success: function (res) {
        wx.stopPullDownRefresh();
        wx.hideNavigationBarLoading();

        // 假设后端返回的数据格式是 { code: 200, data: [...] }
        if (res.data && res.data.data) {
          var devices = res.data.data;
          
          // 如果后端返回的时间戳需要前端格式化，可以在这里处理
          devices.forEach(item => {
             if(item.borrowTime) {
               item.borrowTimeStr = that.formatDateTime(item.borrowTime);
             }
             item.isExpand = false; // 初始化折叠状态
          });

          that.setData({
            showEmptyView: devices.length <= 0,
            devices: devices
          });
        }
      },
      fail: function (error) {
        wx.stopPullDownRefresh();
        wx.hideNavigationBarLoading();
        wx.showToast({
          title: '获取设备列表失败',
          icon: 'none',
        });
      }
    });
  },

  onPullDownRefresh: function () {
    wx.showNavigationBarLoading();
    this.getBorrowedDevices();
  },
})