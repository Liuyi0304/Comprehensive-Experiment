// pages/myDevices/myDevices.js
const QRCode = require('../lib/qrcode/weapp-qrcode.js')
const app = getApp()

Page({
  data: {
    employeeId: null, // 对应 MySQL 的主键 ID
    showEmptyView: false,
    devices: [],
    startX: 0,
    slideMenuWidth: 150,
  },

  onShow: function () {
    this.getMyDevices();
  },

  onLoad: function (options) {
    wx.setNavigationBarTitle({ title: '我的设备' });
    this.setData({
      employeeId: options.employeeObjectID || options.employeeId,
    });
  },

  // --- 1. 获取我的设备列表 ---
  getMyDevices: function () {
    var that = this;
    wx.request({
      url: 'http://192.168.98.211:8080/api/devices/my',
      method: 'GET',
      data: {
        employeeId: that.data.employeeId
      },
      success: function (res) {
        wx.stopPullDownRefresh();
        if (res.data && res.data.code === 200) {
          let list = res.data.data || [];
          that.setData({
            showEmptyView: list.length <= 0,
            devices: list
          });
        }
      },
      fail: function () {
        wx.showToast({ title: '获取列表失败', icon: 'none' });
      }
    });
  },

  // --- 2. 删除设备逻辑 ---
  bindDelete: function (e) {
    var index = e.currentTarget.dataset.index;
    var device = this.data.devices[index];
    var that = this;

    // 仅限闲置(status=0)状态删除
    if (!device.status || device.status == 0) {
      wx.showActionSheet({
        itemList: ['确认删除'],
        itemColor: '#FF0000',
        success: function (res) {
          if (res.tapIndex == 0) {
            wx.showLoading({ title: '删除中...' });
            wx.request({
              url: 'http://192.168.98.211:8080/api/devices/delete',
              method: 'POST',
              data: {
                deviceId: device.id, // MySQL 中的自增 ID
                operatorId: that.data.employeeId
              },
              success: function (response) {
                wx.hideLoading();
                if (response.data.code === 200) {
                  wx.showToast({ title: '删除成功' });
                  that.getMyDevices(); // 刷新列表
                } else {
                  wx.showToast({ title: response.data.msg, icon: 'none' });
                }
              }
            });
          }
          that.closeSlide(index);
        },
        fail: function () {
          that.closeSlide(index);
        }
      })
    } else {
      wx.showModal({
        title: '提示',
        content: '请在设备“闲置”状态时，进行删除操作',
        showCancel: false,
        success: () => that.closeSlide(index)
      })
    }
  },

  // --- 3. 生成/展开二维码 ---
  bindTapExpand: function (e) {
    var tapIndex = e.currentTarget.dataset.index;
    var devices = this.data.devices;
    var device = devices[tapIndex];

    // 构建二维码内容 (对应你 MySQL 里的字段名)
    var param = {
      deviceID: device.deviceCode, // 建议数据库字段名为 device_code
      model: device.model,
      brand: device.brand,
      os: device.osVersion,
      remark: device.remark
    };

    var qrcodeText = JSON.stringify(param);

    if (!device.qrcode) {
      // 这里的 device.id 是 canvas-id，确保 wxml 里 canvas-id="{{item.id}}"
      var qrcode = new QRCode(device.id.toString(), {
        text: qrcodeText,
        width: 150,
        height: 150,
        colorDark: "#000000",
        colorLight: "#ffffff",
        correctLevel: QRCode.CorrectLevel.H,
      });
      device.qrcode = qrcode;
    }

    device.isExpand = !device.isExpand;
    this.setData({ devices: devices });
  },

  // --- 4. 跳转编辑/新增 ---
  bindEdit: function (e) {
    var index = e.currentTarget.dataset.index;
    var device = this.data.devices[index];
    if (!device.status || device.status == 0) {
      wx.navigateTo({
        url: `../device/device?isEdit=true&employeeId=${this.data.employeeId}&deviceData=${JSON.stringify(device)}`,
      });
      this.closeSlide(index);
    } else {
      wx.showModal({ title: '提示', content: '仅闲置状态可编辑', showCancel: false });
      this.closeSlide(index);
    }
  },

  bindAddDevice: function () {
    wx.navigateTo({
      url: '../device/device?isEdit=false&employeeId=' + this.data.employeeId,
    })
  },

  // --- 5. 侧滑交互逻辑 (保持不变) ---
  closeSlide: function (index) {
    var devices = this.data.devices;
    devices[index].isSlideMenuOpen = false;
    devices[index].slideStyle = "left:0px";
    this.setData({ devices: devices });
  },

  touchS: function (e) {
    if (e.touches.length == 1) {
      this.setData({ startX: e.touches[0].clientX });
    }
  },

  touchM: function (e) {
    var index = e.currentTarget.dataset.index;
    var device = this.data.devices[index];
    if (device.isExpand) return;
    if (e.touches.length == 1) {
      var moveX = e.touches[0].clientX;
      var disX = this.data.startX - moveX;
      var slideMenuWidth = this.data.slideMenuWidth;
      var slideStyle = disX <= 0 ? "left:0px" : "left:-" + (disX >= slideMenuWidth ? slideMenuWidth : disX) + "px";
      var devices = this.data.devices;
      devices[index].slideStyle = slideStyle;
      this.setData({ devices: devices });
    }
  },

  touchE: function (e) {
    var index = e.currentTarget.dataset.index;
    var devices = this.data.devices;
    if (e.changedTouches.length == 1) {
      var endX = e.changedTouches[0].clientX;
      var disX = this.data.startX - endX;
      if (!devices[index].isSlideMenuOpen && disX == 0) {
        this.bindTapExpand(e);
      } else {
        if (!devices[index].isExpand) {
          let isOpen = disX > this.data.slideMenuWidth / 2;
          devices[index].slideStyle = isOpen ? `left:-${this.data.slideMenuWidth}px` : "left:0px";
          devices[index].isSlideMenuOpen = isOpen;
          this.setData({ devices: devices });
        }
      }
    }
  },

  onPullDownRefresh: function () {
    this.getMyDevices();
  }
})