const app = getApp();

Page({
  data: {
    isEdit: false,
    deviceId: null,
    // 状态配置
    statusRange: ['在库 (In Stock)', '在用 (In Use)', '维修中 (Repairing)', '已报废 (Scrapped)'],
    statusRangeEn: ['in_stock', 'in_use', 'under_repair', 'scrapped'],
    statusIndex: 0,
    // 下拉列表数据
    categoryList: [],
    labList: [],
    categoryIndex: null,
    labIndex: null,
    // 设备对象初始值
    device: {
      status: 'in_stock',
      purchaseDate: '',
      categoryId: null,
      labId: null
    }
  },

  onLoad(options) {
    // 1. 初始化基础数据
    this.initDropdownData();

    // 2. 判断模式
    if (options.id && options.mode === 'edit') {
      this.setData({ isEdit: true, deviceId: options.id });
      wx.setNavigationBarTitle({ title: '修改设备档案' });
      this.fetchDetail(options.id);
    }
  },

  // 获取分类和实验室列表
  initDropdownData() {
    // 这里替换为你真实的后端接口路径
    wx.request({
      url: `${app.globalData.serverUrl}/weixinapi/categories/list`,
      success: (res) => { if(res.data.code === 200) this.setData({ categoryList: res.data.data }) },
      fail: (err) => { console.error('请求失败，网络是否有问题？', err) }
    });
    wx.request({
      url: `${app.globalData.serverUrl}/weixinapi/labs/list`,
      success: (res) => { if(res.data.code === 200) this.setData({ labList: res.data.data }) },
      fail: (err) => { console.error('请求失败，网络是否有问题？', err) }
    });
  },

  // 获取详情回显
  fetchDetail(id) {
    wx.request({
      url: `${app.globalData.serverUrl}/weixinapi/devices/detail?id=${id}`,
      success: (res) => {
        if (res.data.code === 200) {
          const dev = res.data.data;
          // 匹配状态、分类、实验室的索引
          const sIdx = this.data.statusRangeEn.indexOf(dev.status);
          this.setData({ 
            device: dev, 
            statusIndex: sIdx > -1 ? sIdx : 0
          });
          // 列表加载完成后再匹配索引（由于异步，可能需要延时或在回调中处理）
          this.matchDropdownIndexes(dev);
        }
      }
    });
  },

  matchDropdownIndexes(dev) {
    // 简单的延时处理，确保下拉列表已加载
    setTimeout(() => {
      const cIdx = this.data.categoryList.findIndex(i => i.id === dev.categoryId);
      const lIdx = this.data.labList.findIndex(i => i.id === dev.labId);
      this.setData({ categoryIndex: cIdx > -1 ? cIdx : null, labIndex: lIdx > -1 ? lIdx : null });
    }, 500);
  },

  // 事件绑定
  bindDateChange(e) { this.setData({ 'device.purchaseDate': e.detail.value }); },
  bindStatusChange(e) {
    this.setData({ statusIndex: e.detail.value, 'device.status': this.data.statusRangeEn[e.detail.value] });
  },
  bindCategoryChange(e) {
    const idx = e.detail.value;
    this.setData({ categoryIndex: idx, 'device.categoryId': this.data.categoryList[idx].id });
  },
  bindLabChange(e) {
    const idx = e.detail.value;
    this.setData({ labIndex: idx, 'device.labId': this.data.labList[idx].id });
  },

  // 提交表单
  submitForm(e) {
    const formFields = e.detail.value; // 获取 WXML 中所有 input 的最新值
    
    // 1. 先拿旧数据作为底稿
    // 2. 用表单里的最新文字/数字（formFields）覆盖旧数据
    // 3. 确保 id 被包含进去
    const finalData = Object.assign({}, this.data.device, formFields);
    
    if (this.data.isEdit) {
        finalData.id = this.data.deviceId;
    }

    // --- 调试：这里打印出来的一定是修改后的值了 ---
    console.log('修正后的提交数据：', finalData);
    
    // 2. 将表单中的文本字段（name, assetNumber, model 等）覆盖进去
    // 这样既保留了你手动 bindChange 选中的 ID，又拿到了输入框里的新文字
    finalData.name = formFields.name;
    finalData.assetNumber = formFields.assetNumber;
    finalData.model = formFields.model;
    finalData.location = formFields.location; // 假设你有位置字段
  
    if (this.data.isEdit) finalData.id = this.data.deviceId;
  
    // 校验逻辑...
    if (!finalData.name || !finalData.assetNumber) {
      return wx.showToast({ title: '名称和编号必填', icon: 'none' });
    }
  
    console.log('即将提交给后端的数据：', finalData); // 调试必看！
  
    wx.showLoading({ title: '正在保存...' });
    wx.request({
      url: `${app.globalData.serverUrl}/weixinapi/devices/${this.data.isEdit ? 'update' : 'add'}`,
      method: 'POST',
      data: finalData,
      success: (res) => {
        wx.hideLoading();
        if (res.data.code === 200) {
          wx.showToast({ title: '操作成功', icon: 'success' });
          setTimeout(() => wx.navigateBack(), 1000);
        } else {
          wx.showModal({ title: '提示', content: res.data.msg || '保存失败', showCancel: false });
        }
      }
    });
  },

  goBack() { wx.navigateBack(); }
});