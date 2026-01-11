const app = getApp();

Page({
  data: {
    devices: [],      // 页面显示的过滤后列表
    allDevices: [],   // 原始列表备份（搜索用）
    showEmptyView: false,
    // 品牌图标映射
    brandMap: {
      '华为': 'huawei',
      '联想': 'lenovo',
      '苹果': 'apple',
      '小米': 'xiaomi',
      'vivo': 'vivo',
      'oppo': 'oppo'
    }
  },

  onLoad: function () {
    wx.setNavigationBarTitle({ title: '设备报废审批' });
    this.getApprovalList();
  },

  // 1. 获取待审批列表
  getApprovalList: function () {
    wx.showLoading({ title: '获取全量申请列表...' });
    
    wx.request({
      // 直接请求报废申请单列表，无需 labId 过滤
      url: app.globalData.serverUrl + '/weixinapi/scrap/all', 
      method: 'GET',
      // 如果后端接口不需要任何参数即可返回全部，则 data 可以留空或删除
      success: (res) => {
        wx.hideLoading();
        if (res.data.code === 200) {
          // list 现在是跨实验室的所有待审批申请
          // 确保只保留 pending 状态的数据
          const list = (res.data.data || []).filter(item => item.status === 'pending');
          this.setData({
            allDevices: list, 
            devices: list,
            showEmptyView: list.length === 0
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

  // 2. 同意报废处理
  handleApprove: function (e) {
    const { id, name } = e.currentTarget.dataset;
    wx.showModal({
      title: '审批通过确认',
      content: `确定同意【${name}】的报废申请吗？同意后该设备将标记为已报废。`,
      confirmColor: '#07c160',
      success: (res) => {
        if (res.confirm) {
          this.approveRequest(id);
        }
      }
    });
  },

  // 2. 拒绝报废处理：弹出输入框要求填写理由
  handleReject: function (e) {
    const { id, name } = e.currentTarget.dataset;
    wx.showModal({
      title: '驳回申请',
      content: '请输入驳回理由：',
      editable: true,
      placeholderText: '例如：该设备仍有维修价值',
      success: (res) => {
        if (res.confirm) {
          if (!res.content) {
            wx.showToast({ title: '必须填写理由', icon: 'none' });
            return;
          }
          this.rejectRequest(id, res.content);
        }
      }
    });
  },

  // 3. 执行“同意”请求
  approveRequest: function (requestId) {
    wx.showLoading({ title: '处理中...' });
    wx.request({
      url: app.globalData.serverUrl + '/weixinapi/scrap/approve', 
      method: 'POST',
      data: {
        // 必须对应 DTO 中的 requestId
        requestId: requestId, 
        // 必须对应 DTO 中的 status
        status: 'approved',
        // 同意时理由传空字符串或不传
        rejectedReason: '' ,
        operatorId: app.globalData.userInfo.id
      },
      success: (res) => {
        this.handleResponse(res);
      }
    });
  },

  // 4. 执行“拒绝”请求
  rejectRequest: function (requestId, reason) {
    wx.showLoading({ title: '处理中...' });
    wx.request({
      url: app.globalData.serverUrl + '/weixinapi/scrap/reject', 
      method: 'POST',
      data: {
        // 必须对应 DTO 中的 requestId
        requestId: requestId, 
        // 必须对应 DTO 中的 status
        status: 'rejected',
        // 必须对应 DTO 中的 rejectedReason
        rejectedReason: reason ,
        operatorId: app.globalData.userInfo.id
      },
      success: (res) => {
        this.handleResponse(res);
      }
    });
  },

  // 统一处理请求响应
  handleResponse: function (res) {
    wx.hideLoading();
    if (res.data.code === 200) {
      wx.showToast({ title: '操作完成', icon: 'success' });
      this.getApprovalList(); // 成功后刷新列表
    } else {
      wx.showToast({ title: res.data.msg || '操作失败', icon: 'none' });
    }
  },

  // 5. 搜索功能
  bindSearchInput: function (e) {
    const val = e.detail.value.toLowerCase();
    if (!val) {
      this.setData({ devices: this.data.allDevices });
      return;
    }
    const filtered = this.data.allDevices.filter(item => {
      return (item.name && item.name.toLowerCase().includes(val)) ||
             (item.assetNumber && item.assetNumber.toLowerCase().includes(val)) ||
             (item.applicantName && item.applicantName.toLowerCase().includes(val));
    });
    this.setData({ devices: filtered });
  },

  // 下拉刷新
  onPullDownRefresh: function () {
    this.getApprovalList();
    wx.stopPullDownRefresh();
  }
});