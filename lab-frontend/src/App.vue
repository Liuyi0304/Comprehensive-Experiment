<template>
  <!-- 使用 el-config-provider 包裹路由出口，并绑定中文包 -->
  <el-config-provider :locale="locale">
    <router-view />
  </el-config-provider>
</template>

<script setup>
import { ref } from 'vue'
// 1. 引入 ConfigProvider 组件
import { ElConfigProvider } from 'element-plus'
// 2. 引入中文语言包
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'

// 3. 定义语言变量
const locale = ref(zhCn)
</script>

<style>
body, html {
  margin: 0;
  padding: 0;
  height: 100%;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', '微软雅黑', Arial, sans-serif;
}
#app {
  height: 100%;
}
</style>