<template>
  <div class="page-404">
    <h1>404</h1>
    <p>抱歉，您访问的页面不存在。</p>
    <el-button type="primary" @click="$router.push('/')">返回首页</el-button>
  </div>
</template>

<script>
export default {
  name: 'Page404'
}
</script>

<style scoped>
.page-404 {
  text-align: center;
  padding-top: 100px;
}
</style>