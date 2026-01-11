const app = getApp()

Page({
  data: {
    wxUserInfo: null,
    userInfo: null, // 存储从登录页传来的用户对象
    showEmptyView: false,
    devices: [],
    allDevices: [],
    pendingCount: 0, // 待审批单数
    isBind: true,    // 既然能进到这里，默认已登录
    brandMap: { // 建立品牌与图片名的映射
      '华为': 'huawei', '联想': 'lenovo', '苹果': 'apple', 
      'vivo': 'vivo', '小米': 'xiaomi', '三星': 'sanxing'
    }
  },

  onShow: function () {
    // 每次页面显示都刷新数据
    this.getDevices();
    this.getPendingApprovalCount();
  },

  onLoad: function () {
    wx.setNavigationBarTitle({ title: '实验室设备管理系统' });

    // 从全局变量获取登录成功后的用户信息
    const userInfo = app.globalData.userInfo;
    
    if (!userInfo) {
      // 安全检查：如果没有用户信息，说明没登录，打回登录页
      wx.reLaunch({ url: '/pages/login/login' });
      return;
    }

    this.setData({
      userInfo: userInfo,
      wxUserInfo: app.globalData.wxUserInfo
    });
  },

  // --- 1. 获取所有设备列表 ---
  getDevices: function () {
    var that = this;
    wx.request({
      url: app.globalData.serverUrl + '/weixinapi/devices/all',
      method: 'GET',
      success: (res) => {
        wx.stopPullDownRefresh();
        let list = res.data.data || [];
        that.setData({
          showEmptyView: list.length <= 0,
          allDevices: list,
          devices: list
        });
      },
      fail: (err) => {
        console.error("请求后端失败：", err);
        wx.showToast({ title: '服务器连接失败', icon: 'none' });
      }
    });
  },

  // --- 2. 获取待审批的数量 ---
  getPendingApprovalCount: function () {
    var that = this;
    wx.request({
      url: app.globalData.serverUrl + '/weixinapi/devices/pendingCount', // 需后端配合此接口
      method: 'GET',
      success: (res) => {
        if (res.data.code === 200) {
          that.setData({ pendingCount: res.data.data || 0 });
        }
      }
    });
  },

  bindTapExpand: function (e) {
    // 获取点击的设备索引
    const index = e.currentTarget.dataset.index;
    const list = this.data.devices;

    // 切换当前点击项的展开状态 (取反)
    list[index].isExpand = !list[index].isExpand;

    // 重新渲染页面
    this.setData({
      devices: list
    });
  },

  // 2. 删除请求逻辑
  handleDelete: function(e) {
    const { id, name } = e.currentTarget.dataset;
    const that = this;
  
    wx.showModal({
      title: '确认删除',
      content: `确定删除 ${name} 吗？`,
      success(res) {
        if (res.confirm) {
          wx.request({
            // URL 保持干净，不再拼接 ID
            url: app.globalData.serverUrl + '/weixinapi/devices/delete', 
            method: 'GET',
            data: {
              id: id // 参数放在 data 里，会自动拼成 ?id=xxx
            },
            success: (res) => {
              if (res.data.code === 200) {
                wx.showToast({ title: '已删除' });
                that.getDevices();
              }
            }
          });
        }
      }
    });
  },
  //改
  handleEdit: function(e) {
    const device = e.currentTarget.dataset.item;
    // 跳转到录入页面，但带上当前设备 ID，让该页面进入“编辑模式”
    wx.navigateTo({
      url: `/pages/addDevice/addDevice?id=${device.id}&mode=edit`
    });
  },
  
  // --- 3. 跳转：录入设备 (原 bindMyDevices) ---
  bindAddDevice: function () {
    wx.navigateTo({
      url: '/pages/addDevice/addDevice' // 需要你创建这个页面
    });
  },
  // --- 4. 跳转：审批设备 (原 bindBorrowedDevices) ---
  bindApproveDevices: function () {
    wx.navigateTo({
      url: '/pages/approveList/approveList' // 需要你创建这个页面
    });
  },
  //报修跳转
  bindApproveRepair: function() {
    wx.navigateTo({
      url: '/pages/approveRepair/approveRepair', // 确保这个路径存在
    });
  },
  // --- 5. 搜索逻辑 (保持不变，已适配 admin 视角) ---
  bindSearchInput: function (e) {
    this.searchContent(e.detail.value);
  },

  searchContent: function (content) {
    var that = this;
    if (content == "") {
      this.setData({ devices: that.data.allDevices });
      return;
    }
    content = content.toUpperCase();
    var filtered = this.data.allDevices.filter(item => {
      return (item.name && item.name.toUpperCase().includes(content)) ||
             (item.employeeName && item.employeeName.toUpperCase().includes(content)) ||
             (item.deviceid && item.deviceid.toUpperCase().includes(content));
    });
    this.setData({ devices: filtered });
  },

  // --- 6. 扫码识别逻辑 ---
  scanClick: function () {
    wx.scanCode({
      success: (res) => {
        // 扫码后可以跳转到详情页进行修改或审批
        const deviceID = res.result; 
        wx.navigateTo({
          url: '/pages/deviceDetail/deviceDetail?id=' + deviceID
        });
      }
    });
  },

  // --- 7. 更多/个人设置 ---
  bindMore: function () {
    wx.navigateTo({
      url: '/pages/menu/menu'
    });
  },

  onPullDownRefresh: function () {
    this.getDevices();
  }
})