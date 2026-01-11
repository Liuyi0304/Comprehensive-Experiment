// pages/Device/device.js
const app = getApp()

Page({
  data: {
    showTopTips: false,
    employeeId: null, // 对应 MySQL 的 employee 表 id
    deviceId: null,   // 对应 MySQL 的 device 表 id (编辑时用)
    topTips: '',
    models: [],
    brands: [],
    modelIndex: null,
    isEdit: false,
    brandIndex: null,

    // 版本选择器数据
    OSVersions: [[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15], [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15], [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15]],
    systemVersionIndex1: 0,
    systemVersionIndex2: 0,
    systemVersionIndex3: 0,
    companyCode: 'A000',
    deviceCode: null,
    remark: null,
  },

  onLoad: function (options) {
    let title = "新增设备";
    this.setData({
      employeeId: options.employeeId || options.employeeObjectID,
    });

    if (options.isEdit) {
      title = "编辑设备";
      this.setData({ isEdit: true });
    }
    wx.setNavigationBarTitle({ title: title });

    // 1. 初始化品牌列表
    this.getBrands((brands) => {
      this.setData({ brands: brands });

      // 2. 如果是编辑模式，初始化已有数据
      if (options.isEdit && options.device) {
        const device = JSON.parse(options.device);
        this.initEditData(device, brands);
      }
    });
  },

  // 初始化编辑数据逻辑
  initEditData: function (device, brands) {
    this.setData({
      deviceId: device.id, 
      remark: device.remark,
      deviceCode: device.deviceCode,
      companyCode: device.companyCode,
    });

    // 匹配品牌索引
    let bIndex = brands.findIndex(item => item.brand === device.brandName);
    if (bIndex > -1) {
      this.setData({ brandIndex: bIndex });
      // 加载该品牌下的型号
      this.getBrandModels(brands[bIndex].id, (models) => {
        let mIndex = models.findIndex(item => item.id === device.modelId);
        this.setData({
          models: models,
          modelIndex: mIndex > -1 ? mIndex : null
        });
      });
    }

    // 处理版本号回显 (例如 "12.0.1" -> [11, 0, 1])
    if (device.osVersion) {
      const v = device.osVersion.split(".");
      this.setData({
        systemVersionIndex1: parseInt(v[0]) - 1,
        systemVersionIndex2: parseInt(v[1]),
        systemVersionIndex3: parseInt(v[2]),
      });
    }
  },

  // --- 网络请求部分：对接 Spring Boot ---

  // 获取品牌列表
  getBrands: function (callback) {
    wx.request({
      url: 'http://192.168.98.211:8080/api/brands/list',
      method: 'GET',
      success: (res) => {
        if (res.data && res.data.data) {
          callback(res.data.data);
        }
      }
    });
  },

  // 根据品牌 ID 获取型号列表
  getBrandModels: function (brandId, callback) {
    wx.request({
      url: app.globalData.serverUrl + '/api/models/listByBrand',
      data: { brandId: brandId },
      method: 'GET',
      success: (res) => {
        if (res.data && res.data.data) {
          callback(res.data.data);
        }
      }
    });
  },

  // 提交（新增或编辑）
  bindSubmit: function () {
    if (!this.data.deviceCode) return this.showTips('请输入设备编号');
    if (this.data.brandIndex === null) return this.showTips('请选择品牌');
    if (this.data.modelIndex === null) return this.showTips('请选择型号');

    wx.showLoading({ title: '提交中...', mask: true });

    const osVersion = `${this.data.OSVersions[0][this.data.systemVersionIndex1]}.${this.data.OSVersions[1][this.data.systemVersionIndex2]}.${this.data.OSVersions[2][this.data.systemVersionIndex3]}`;
    const selectedModel = this.data.models[this.data.modelIndex];
    
    const postData = {
      id: this.data.deviceId, // 编辑时有值，新增时为 null
      employeeId: this.data.employeeId,
      deviceCode: this.data.deviceCode,
      companyCode: this.data.companyCode,
      modelId: selectedModel.id,
      osVersion: osVersion,
      remark: this.data.remark
    };

    const apiUrl = this.data.isEdit ? '/api/devices/update' : '/api/devices/add';

    wx.request({
      url: app.globalData.serverUrl + apiUrl,
      method: 'POST',
      data: postData,
      success: (res) => {
        wx.hideLoading();
        if (res.data.code === 200) {
          wx.showToast({ title: this.data.isEdit ? '编辑成功' : '添加成功' });
          setTimeout(() => wx.navigateBack(), 1500);
        } else {
          this.showTips(res.data.msg || '操作失败');
        }
      },
      fail: () => {
        wx.hideLoading();
        this.showTips('服务器连接失败');
      }
    });
  },

  // --- 其他交互逻辑 ---

  bindBrandChange: function (e) {
    const value = parseInt(e.detail.value);
    const brand = this.data.brands[value];
    this.setData({
      brandIndex: value,
      modelIndex: null,
      models: []
    });
    this.getBrandModels(brand.id, (models) => {
      this.setData({ models: models });
    });
  },

  bindModelChange: function (e) {
    this.setData({ modelIndex: parseInt(e.detail.value) });
  },

  bindDeviceCodeInput: function (e) { this.setData({ deviceCode: e.detail.value }); },
  bindCompanyCodeInput: function (e) { this.setData({ companyCode: e.detail.value }); },
  bindRemarkInput: function (e) { this.setData({ remark: e.detail.value }); },
  bindSystemVersionChange: function (e) {
    this.setData({
      systemVersionIndex1: e.detail.value[0],
      systemVersionIndex2: e.detail.value[1],
      systemVersionIndex3: e.detail.value[2]
    });
  },

  showTips: function (content) {
    this.setData({ showTopTips: true, topTips: content });
    setTimeout(() => this.setData({ showTopTips: false }), 3000);
  },

  // 扫码逻辑改动：只需根据扫码结果更新 data，剩下的联动由逻辑触发
  bindScanClick: function () {
    wx.scanCode({
      success: (res) => {
        try {
          const param = JSON.parse(res.result.trim());
          // 匹配并设置品牌型号等（此处逻辑同原代码，但需适配 MySQL ID）
          // ... 扫码后的自动匹配逻辑建议在后端提供一个专门的解析接口 ...
        } catch (e) {
          wx.showToast({ title: '二维码格式错误', icon: 'none' });
        }
      }
    });
  }
})