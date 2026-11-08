<template>
  <div class="not-found">
    <div class="code">404</div>
    <p>页面走丢了</p>
    <el-button type="primary" @click="$router.replace('/dashboard')">返回工作台</el-button>
  </div>
</template>

<script>
export default { name: 'NotFound' }
</script>

<style scoped>
.not-found {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 14px;
  background: #f5f7fa;
}
.code { font-size: 96px; font-weight: 700; color: #d9e2f2; letter-spacing: 4px; }
p { color: #9099a8; font-size: 14px; margin: 0; }
</style>
