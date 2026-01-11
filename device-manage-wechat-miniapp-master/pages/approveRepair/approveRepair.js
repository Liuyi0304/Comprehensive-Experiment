const app = getApp();

Page({
  data: {
    repairList: []
  },

  onShow: function () {
    this.fetchRepairTasks();
  },

  // 获取待处理报修记录
  fetchRepairTasks: function () {
    wx.showLoading({ title: '拉取数据中' });
    // 1. 获取当前登录的用户信息（通常在登录时存入 app.globalData）
    const userInfo = app.globalData.employeeInfo || app.globalData.userInfo;
    if (!userInfo || !userInfo.id) {
      wx.hideLoading();
      wx.showToast({ title: '未获取到用户信息', icon: 'none' });
      return;
    }
    wx.request({
      url: app.globalData.serverUrl + '/weixinapi/repair/list',
      method: 'GET', // 注意：如果是 GET 请求，参数会拼接在 URL 后
      data: {
        userId: userInfo.id, // 2. 显式传入当前用户 ID
        // 如果有搜索框，也可以加上 keyword: this.data.keyword
      },
      success: (res) => {
        if (res.data.code === 200) {
          this.setData({
            repairList: res.data.data
          });
        } else {
          wx.showToast({ title: res.data.msg || '加载失败', icon: 'none' });
        }
      },
      fail: () => {
        wx.showToast({ title: '网络请求失败', icon: 'none' });
      },
      complete: () => wx.hideLoading()
    });
  },
  // 指派操作
  handleAssign: function (e) {
    const { id, deviceid, name } = e.currentTarget.dataset; // 确保 WXML 传了 deviceid
    const that = this;
  
    wx.showLoading({ title: '获取管理员信息...' });
  
    // 第一步：根据设备 ID 获取所属实验室的管理员 ID
    wx.request({
      url: app.globalData.serverUrl + '/weixinapi/devices/getManager',
      method: 'GET',
      data: { deviceId: deviceid },
      success: (res) => {
        wx.hideLoading();
        if (res.data.code === 200 && res.data.data) {
          const managerId = res.data.data; // 假设后端直接返回 ID 数字
  
          // 第二步：确认指派
          wx.showModal({
            title: '审批确认',
            content: `该设备属于实验室负责人(ID:${managerId})，确认指派他给处理吗？`,
            confirmColor: '#07c160',
            success: (modalRes) => {
              if (modalRes.confirm) {
                that.submitAssign(id, managerId); // 调用提交函数
              }
            }
          });
        } else {
          wx.showToast({ title: '未找到该设备对应的实验室管理员', icon: 'none' });
        }
      }
    });
  },
  
  // 抽离出来的提交函数
  submitAssign: function (recordId, handlerId) {
    wx.showLoading({ title: '提交中' });
    wx.request({
      url: app.globalData.serverUrl + '/weixinapi/repair/assign',
      method: 'POST',
      data: {
        id: recordId,
        handlerId: handlerId,
        status: 'approved'
      },
      success: (res) => {
        if (res.data.code === 200) {
          wx.showToast({ title: '指派成功', icon: 'success' });
          this.fetchRepairTasks(); // 刷新列表
        } else {
          wx.showToast({ title: res.data.msg || '操作失败', icon: 'none' });
        }
      }
    });
  },

  handleAssignreject: function (e) {
    const { id, deviceid, name } = e.currentTarget.dataset; // 确保 WXML 传了 deviceid
    const that = this;
  
    wx.showLoading({ title: '获取管理员信息...' });
  
    // 第一步：根据设备 ID 获取所属实验室的管理员 ID
    wx.request({
      url: app.globalData.serverUrl + '/weixinapi/devices/getManager',
      method: 'GET',
      data: { deviceId: deviceid },
      success: (res) => {
        wx.hideLoading();
        if (res.data.code === 200 && res.data.data) {
          const managerId = res.data.data; // 假设后端直接返回 ID 数字
  
          // 第二步：确认指派
          wx.showModal({
            title: '审批确认',
            content: `确定拒绝吗`,
            confirmColor: '#07c160',
            success: (modalRes) => {
              if (modalRes.confirm) {
                that.submitrejectAssign(id, managerId); // 调用提交函数
              }
            }
          });
        } else {
          wx.showToast({ title: '未找到该设备对应的实验室管理员', icon: 'none' });
        }
      }
    });
  },
  
  // 抽离出来的提交函数
  submitrejectAssign: function (recordId, handlerId) {
    wx.showLoading({ title: '拒绝中' });
    wx.request({
      url: app.globalData.serverUrl + '/weixinapi/repair/assign',
      method: 'POST',
      data: {
        id: recordId,
        handlerId: handlerId,
        status: 'rejected'
      },
      success: (res) => {
        if (res.data.code === 200) {
          wx.showToast({ title: '指派成功', icon: 'success' });
          this.fetchRepairTasks(); // 刷新列表
        } else {
          wx.showToast({ title: res.data.msg || '操作失败', icon: 'none' });
        }
      }
    });
  }


});