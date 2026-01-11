const app = getApp();

Page({
  data: {
    repairTasks: [], // 存放维修记录
    loading: true
  },

  onShow: function () {
    // 每次进入页面都刷新数据
    this.fetchRepairTasks();
  },

  /**
   * 获取维修任务列表
   */
  fetchRepairTasks: function () {
    // 1. 从全局变量获取登录时存入的用户信息
    const userInfo = app.globalData.userInfo;

    if (!userInfo || !userInfo.id) {
      wx.showToast({ title: '未登录或登录失效', icon: 'none' });
      // 如果没登录，跳转回登录页
      setTimeout(() => { wx.redirectTo({ url: '/pages/login/login' }) }, 1500);
      return;
    }

    wx.showLoading({ title: '加载任务中...' });

    // 2. 向后端请求属于该用户的维修任务
    wx.request({
      url: app.globalData.serverUrl + '/weixinapi/repair/listTasks', 
      method: 'GET',
      data: {
        handlerId: userInfo.id // 关键参数：只查询处理人是当前登录用户的记录
      },
      success: (res) => {
        if (res.data.code === 200) {
          this.setData({
            repairTasks: res.data.data,
            loading: false
          });
        } else {
          wx.showToast({ title: res.data.msg || '获取失败', icon: 'none' });
        }
      },
      complete: () => {
        wx.hideLoading();
      }
    });
  },

  handleStartRepair: function (e) {
    const { id, name } = e.currentTarget.dataset;
    wx.showModal({
      title: '确认开始',
      content: `确认开始维修【${name}】吗？`,
      success: (res) => {
        if (res.confirm) {
          wx.showLoading({ title: '提交中' });
          wx.request({
            url: app.globalData.serverUrl + '/weixinapi/repair/start', // 调用 start 接口
            method: 'POST',
            data: { id: id },
            success: (res) => {
              if (res.data.code === 200) {
                wx.showToast({ title: '已开始维修', icon: 'success' });
                this.fetchRepairTasks(); // 刷新列表
              }
            }
          });
        }
      }
    });
  },
  
  /**
   * 按钮点击：完成维修
   */
  handleFinishRepair: function (e) {
    const { id, name } = e.currentTarget.dataset;
    
    // 第一步：输入维修方案
    wx.showModal({
      title: '维修方案',
      editable: true,
      placeholderText: '请输入维修方案内容',
      success: (resSolution) => {
        if (resSolution.confirm && resSolution.content) {
          const solution = resSolution.content;
  
          // 第二步：输入维修金额
          wx.showModal({
            title: '维修金额',
            editable: true,
            placeholderText: '请输入金额 (元)',
            success: (resCost) => {
              if (resCost.confirm) {
                const cost = resCost.content || 0; // 如果没填则默认为0
  
                // 第三步：最终确认并请求后端
                wx.showLoading({ title: '提交中' });
                wx.request({
                  url: app.globalData.serverUrl + '/weixinapi/repair/complete',
                  method: 'POST',
                  header: {
                    'content-type': 'application/x-www-form-urlencoded' // 适配后端的 @RequestParam
                  },
                  data: {
                    repairId: id,      // 对应后端 Long repairId
                    solution: solution, // 用户输入的方案
                    cost: cost,         // 用户输入的金额
                    outcome: 'success'  // 默认成功状态
                  },
                  success: (res) => {
                    wx.hideLoading();
                    if (res.data.code === 200) {
                      wx.showToast({ title: '处理成功', icon: 'success' });
                      this.fetchRepairTasks(); // 刷新列表
                    } else {
                      wx.showToast({ title: res.data.msg || '失败', icon: 'none' });
                    }
                  }
                });
              }
            }
          });
        } else if (resSolution.confirm && !resSolution.content) {
          wx.showToast({ title: '方案不能为空', icon: 'none' });
        }
      }
    });
  }
});