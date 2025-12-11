const { defineConfig } = require('@vue/cli-service')

module.exports = defineConfig({
  transpileDependencies: true,
  devServer: {
    port: 8081, // 建议把前端端口改为 8081，避免和后端 8080 冲突
    proxy: {
      '/api': {
        target: 'http://localhost:8080', // 这里填你后端的真实地址和端口
        changeOrigin: true,

      }
    }
  }
})