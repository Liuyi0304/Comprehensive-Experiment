/* admin.js */
const app = getApp()

Page({
  data: {
    model: null,
    brands: [],
    brandIndex: null,
  },

  onLoad: function (options) {
    this.getBrands(); // 初始化时从你的 MySQL 获取品牌
    wx.setNavigationBarTitle({
      title: '添加型号',
    })
  },

  // 输入型号
  bindInputModel: function (e) {
    this.setData({
      model: e.detail.value
    })
  },

  // 选择品牌
  bindBrandChange: function (e) {
    if (e.detail.value !== this.data.brandIndex) {
      this.setData({
        brandIndex: e.detail.value,
      })
    }
  },

  // 提交添加
  bindSubmit: function () {
    const that = this;
    if (this.data.brandIndex === null || !this.data.model) {
      wx.showToast({
        title: '请填写完整信息',
        icon: 'none',
      });
      return;
    }

    wx.showLoading({ title: '提交中...' });

    const selectedBrand = this.data.brands[this.data.brandIndex];

    // 发起请求给 SpringBoot
    // 建议逻辑：在后端 SpringBoot 里判断型号是否存在，而不是前端判断
    wx.request({
      url: app.globalData.serverUrl +'/api/models/add', 
      method: 'POST', // 提交数据建议用 POST
      data: {
        modelName: that.data.model,
        brandId: selectedBrand.id // 传品牌ID即可
      },
      success: (res) => {
        wx.hideLoading();
        // 假设后端返回 code 200 表示成功，409 表示已存在
        if (res.data.code === 200) {
          wx.showToast({ title: '添加成功!' });
          that.setData({ model: '', brandIndex: null }); // 清空输入
        } else if (res.data.code === 409) {
          wx.showToast({ title: '型号已存在', icon: 'none' });
        } else {
          wx.showToast({ title: '添加失败', icon: 'none' });
        }
      },
      fail: () => {
        wx.hideLoading();
        wx.showToast({ title: '服务器连接失败', icon: 'none' });
      }
    });
  },

  // 从本地 MySQL 同步品牌列表
  getBrands: function () {
    const that = this;
    wx.request({
      url: app.globalData.serverUrl + '/api/brands/list',
      method: 'GET',
      success: (res) => {
        // 假设后端返回的数据结构是 [{id: 1, brand: "小米"}, {id: 2, brand: "华为"}]
        if (res.data && res.data.data) {
          that.setData({
            brands: res.data.data
          });
        }
      },
      fail: () => {
        console.error("获取品牌失败");
      }
    });
  },
})