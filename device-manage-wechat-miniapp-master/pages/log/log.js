// pages/log/log.js
const app = getApp()

Page({
  data: {
    devices: [],
  },

  onLoad: function (options) {
    wx.setNavigationBarTitle({
      title: '设备共享记录',
    });
    this.getDeviceLogs();
  },

  // 获取日志记录
  getDeviceLogs: function () {
    var that = this;
    wx.showLoading({ title: '加载中...' });

    wx.request({
      url: 'http://192.168.98.211:8080/api/logs/list', // 你的 Spring Boot 接口
      method: 'GET',
      success: function (res) {
        wx.hideLoading();
        if (res.data && res.data.data) {
          let logs = res.data.data;

          // 处理日志描述文本
          logs.forEach(function (item) {
            let actionText = "";
            const operator = item.statusActionEmployeeName || "未知用户";
            const deviceName = item.modelName || "未知设备";
            const fromUser = item.fromEmployeeName || "所有人";
            const owner = item.deviceOwnerEmployeeName || "公司";

            switch (item.action) {
              case "add":
                actionText = `【${operator}】新增了设备: ${deviceName}`;
                break;
              case "delete":
                actionText = `【${operator}】删除了设备: ${deviceName}`;
                break;
              case "edit":
                actionText = `【${operator}】编辑了设备: ${deviceName}`;
                break;
              case "borrowed":
                actionText = `【${operator}】从【${fromUser}】处借取了【${owner}】的: ${deviceName}`;
                break;
              case "returned":
                actionText = `【${operator}】从【${fromUser}】处收回了【${owner}】的: ${deviceName}`;
                break;
              default:
                actionText = `【${operator}】执行了操作: ${deviceName}`;
            }
            // 将拼接好的文字存入 action 字段供界面渲染
            item.actionDisplay = actionText; 
          });

          that.setData({
            devices: logs,
          });
        }
      },
      fail: function (error) {
        wx.hideLoading();
        wx.showToast({
          title: '获取设备记录失败!',
          icon: 'none',
        });
      }
    });
  },

  /**
   * 下拉刷新
   */
  onPullDownRefresh: function () {
    this.getDeviceLogs();
    wx.stopPullDownRefresh();
  }
})