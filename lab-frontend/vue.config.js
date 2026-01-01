const { defineConfig } = require('@vue/cli-service')

module.exports = defineConfig({
  transpileDependencies: true,
  lintOnSave: false, // 关闭 ESLint 检查，避免不必要的报错
  devServer: {
    port: 3000,
    open: true,
    // 👇👇👇 核心配置：关闭全屏错误遮罩 👇👇👇
    client: {
      overlay: false, 
    },
    // 👆👆👆 配置结束 👆👆👆
    proxy: {
      '/api': {
        target: 'http://localhost:8080', // 后端接口地址
        changeOrigin: true,
        // 保持注释状态，不要开启 pathRewrite，因为你后端 Controller 包含 /api
        // pathRewrite: {
        //   '^/api': ''
        // }
      }
    }
  }
})