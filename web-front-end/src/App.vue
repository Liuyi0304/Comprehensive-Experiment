<template>
  <!-- 路由出口：页面会根据网址在这里切换显示 Login 或 DeviceList -->
  <router-view />
</template>

<script setup>
// 这里不需要写逻辑，App.vue 只是一个空壳容器
</script>

<style>
/* 全局样式重置 */
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