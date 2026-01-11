// index.js
const app = getApp()

Page({
  data: {
    wxUserInfo: null,    // 微信基础头像昵称
    employeeInfo: null,  // 存储从全局 globalData 获取的登录用户信息
    devices: [],         // 当前显示的设备列表
    allDevices: [],      // 本地搜索用的全量备份
    showEmptyView: false
  },

  onShow: function () {
    // 1. 每次进入页面时校验登录状态
    this.checkAuth();
  },

  onLoad: function () {
    wx.setNavigationBarTitle({ title: '实验室设备管理' });
    
    // 获取微信头像昵称（如果 app.js 中已经获取过）
    if (app.globalData.wxUserInfo) {
      this.setData({ wxUserInfo: app.globalData.wxUserInfo });
    }
  },

  // --- 1. 登录权限校验 ---
  checkAuth: function () {
    const userInfo = app.globalData.userInfo;

    // 如果全局变量里没有用户信息，说明未登录或内存已释放
    if (!userInfo) {
      wx.showModal({
        title: '提示',
        content: '登录已过期，请重新登录',
        showCancel: false,
        success: () => {
          wx.reLaunch({ url: '/pages/login/login' });
        }
      });
      return;
    }


    // 校验通过，更新页面数据并获取设备列表
    this.setData({
      employeeInfo: {
        id: userInfo.id,
        name: userInfo.name,
        labId: userInfo.labId,
        role: userInfo.role
      }
    });
    
    this.getDevices();
  },

  // --- 2. 获取设备列表 (过滤实验室) ---
  getDevices: function () {
    const that = this;
    const labId = this.data.employeeInfo.labId;

    if (!labId) {
      wx.showToast({ title: '所属实验室信息缺失', icon: 'none' });
      return;
    }

    wx.showLoading({ title: '同步设备中...' });

    wx.request({
      url: app.globalData.serverUrl + '/weixinapi/devices/listByLab',
      method: 'GET',
      data: { labId: labId },
      success: (res) => {
        wx.hideLoading();
        wx.stopPullDownRefresh();
        
        if (res.data.code === 200) {
          let list = res.data.data || [];
          // 初始化：所有设备默认为收起状态
          list = list.map(item => ({ ...item, isExpand: false }));
          
          that.setData({
            showEmptyView: list.length <= 0,
            allDevices: list,
            devices: list
          });
        } else {
          wx.showToast({ title: res.data.msg || '获取失败', icon: 'none' });
        }
      },
      fail: () => {
        wx.hideLoading();
        wx.showToast({ title: '网络请求失败', icon: 'none' });
      }
    });
  },

  // --- 3. 申请报修 ---
  handleRepair: function (e) {
    const { id, name } = e.currentTarget.dataset;
    wx.showModal({
      title: '设备报修申请',
      content: '请输入故障描述：',
      editable: true, // 允许输入报修理由
      placeholderText: '例如：屏幕碎裂、无法开机等',
      success: (res) => {
        if (res.confirm) {
          if (!res.content) {
            wx.showToast({ title: '请填写故障描述', icon: 'none' });
            return;
          }
          // 调用专门的报修申请函数
          this.submitRepairRequest(id, res.content);
        }
      }
    });
  },
  
  // 2. 发起报修申请请求
  submitRepairRequest: function (deviceId, reason) {
    wx.showLoading({ title: '提交中...' });
    wx.request({
      url: app.globalData.serverUrl + '/weixinapi/repair/report', // 报修申请接口
      method: 'POST',
      data: {
        deviceId: deviceId,        // 设备ID
        description: reason,            // 故障原因/描述
        reporterId: app.globalData.userInfo.id // 申请人ID（前端传值）
      },
      success: (res) => {
        wx.hideLoading();
        if (res.data.code === 200) {
          wx.showToast({ title: '报修申请已提交', icon: 'success' });
          // 刷新列表，此时设备状态应变为“维修中”
          this.getApprovalList ? this.getApprovalList() : this.onLoad(); 
        } else {
          wx.showToast({ title: res.data.msg || '提交失败', icon: 'none' });
        }
      },
      fail: () => {
        wx.hideLoading();
        wx.showToast({ title: '网络异常', icon: 'none' });
      }
    });
  },

  handleScrap: function (e) {
    const { id, name } = e.currentTarget.dataset;
    const userInfo = this.data.employeeInfo;
  
    wx.showModal({
      title: '申请报废',
      content: `请输入设备【${name}】的报废原因：`,
      editable: true, // 显示输入框
      placeholderText: '请详细描述报废理由',
      confirmText: '提交申请',
      confirmColor: '#ff4d4f',
      success: (res) => {
        if (res.confirm) {
          const reason = res.content; // 获取用户输入的内容
          if (!reason || reason.trim().length < 5) {
            wx.showToast({ title: '原因不能少于5个字', icon: 'none' });
            return;
          }
          
          // 调用新的接口发送申请数据
          this.submitScrapRequest({
            deviceId: id,
            applicantId: userInfo.id,
            reason: reason
          });
        }
      }
    });
  },
  
  // 新增：提交报废申请的私有方法
  submitScrapRequest: function (data) {
    wx.showLoading({ title: '提交申请中...' });
    wx.request({
      url: app.globalData.serverUrl + '/weixinapi/scrap/submit', // 建议新起一个专门处理申请的路径
      method: 'POST',
      data: data,
      success: (res) => {
        wx.hideLoading();
        if (res.data.code === 200) {
          wx.showModal({
            title: '提交成功',
            content: '报废申请已提交，请等待管理员审核。',
            showCancel: false
          });
          this.getDevices(); // 刷新列表查看状态
        } else {
          wx.showToast({ title: res.data.msg || '提交失败', icon: 'none' });
        }
      }
    });
  },

  handleuse: function (e) {
    const { id, name } = e.currentTarget.dataset;
    const userInfo = this.data.employeeInfo;
    const index = this.data.devices.findIndex(d => d.id === id);
    const status = this.data.devices[index].status;

    // 二次校验状态
    if (status === 'in_use' || status === 'under_repair') {
      wx.showToast({ title: '设备当前不可用', icon: 'none' });
      return;
    }
    
    wx.showModal({
      title: '确认使用',
      content: '请输入使用用途：',
      editable: true, 
      placeholderText: '例如：科研实验、课堂教学',
      success: (res) => {
        if (res.confirm) {
          if (!res.content) {
            wx.showToast({ title: '用途不能为空', icon: 'none' });
            return;
          }
  
          wx.showLoading({ title: '登记中...' });
          wx.request({
            url: app.globalData.serverUrl + '/weixinapi/usage/start',
            method: 'POST',
            data: {
              // 这三个字段都会被后端的 Map 接收
              deviceId: id,           
              purpose: res.content,   
              userId: userInfo.id     
            },
            success: (res) => {
              wx.hideLoading();
              if (res.data.code === 200) {
                wx.showToast({ title: '登记成功', icon: 'success' });
                this.getDevices(); // 刷新设备列表
              } else {
                wx.showToast({ title: res.data.msg || '操作失败', icon: 'none' });
              }
            }
          });
        }
      }
    });
  },
  //归还
  handleguihuan: function (e) {
    const { id, name } = e.currentTarget.dataset;
    const userInfo = this.data.employeeInfo;
    const index = this.data.devices.findIndex(d => d.id === id);
    const status = this.data.devices[index].status;

    // 只有在用状态才能归还
    if (status !== 'in_use') {
      wx.showToast({ title: '设备无需归还', icon: 'none' });
      return;
    }

    wx.showModal({
      title: '确认归还',
      content: `确定归还设备【${name}】并存入实验室吗？`,
      success: (res) => {
        if (res.confirm) {
          wx.showLoading({ title: '归还中...' });
          wx.request({
            url: app.globalData.serverUrl + '/weixinapi/usage/end', 
            method: 'POST', // 统一使用 POST 配合 @RequestBody Map
            data: {
              deviceId: id,    // 对应后端提取的 deviceId
              userId: userInfo.id // 显式传入当前操作人 ID
            },
            success: (res) => {
              wx.hideLoading();
              if (res.data.code === 200) {
                wx.showToast({ title: '归还成功', icon: 'success' });
                // 延迟一秒刷新列表，防止后端数据库写入延迟
                setTimeout(() => {
                  this.getDevices(); 
                }, 1000);
              } else {
                wx.showToast({ title: res.data.msg || '操作失败', icon: 'none' });
              }
            },
            fail: () => {
              wx.hideLoading();
              wx.showToast({ title: '网络请求失败', icon: 'none' });
            }
          });
        }
      }
    });
  },

  // --- 6. 搜索功能 ---
  bindSearchInput: function (e) {
    const val = e.detail.value.toUpperCase();
    if (!val) {
      this.setData({ devices: this.data.allDevices });
      return;
    }
    const filtered = this.data.allDevices.filter(item => {
      return (item.name && item.name.toUpperCase().includes(val)) ||
             (item.deviceID && item.deviceID.toUpperCase().includes(val));
    });
    this.setData({ devices: filtered });
  },

  // --- 7. UI 交互 ---
  bindTapExpand: function (e) {
    const index = e.currentTarget.dataset.index;
    let list = this.data.devices;
    list[index].isExpand = !list[index].isExpand;
    this.setData({ devices: list });
  },

  scanClick: function () {
    wx.scanCode({
      success: (res) => {
        // 后续可在此添加扫码直接定位设备或借还的逻辑
        console.log("扫码结果：", res.result);
      }
    });
  },

  handleapproveRepair: function() {
    wx.navigateTo({
      url: '/pages/handleapproveRepair/handleapproveRepair', // 确保这个路径存在
    });
  },

  onPullDownRefresh: function () {
    this.getDevices();
  }
});