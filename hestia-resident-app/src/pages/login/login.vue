<template>
  <view class="login">
    <view class="logo">
      <view class="mark">H</view>
      <text class="title">云筑住户端</text>
      <text class="sub">报修 · 进度 · 服务</text>
    </view>

    <view class="card form">
      <view class="field-label">手机号</view>
      <input v-model="phone" class="ipt" type="number" maxlength="11" placeholder="请输入手机号" />
      <view class="field-label" style="margin-top: 28rpx">验证码</view>
      <view class="code-row">
        <input v-model="code" class="ipt" type="number" maxlength="6" placeholder="请输入验证码" />
        <view class="code-btn" @click="sendCode">获取验证码</view>
      </view>
      <view class="btn-primary" style="margin-top: 44rpx" @click="doLogin">登 录</view>
      <view class="tip">演示环境任意已录入住户手机号，验证码固定 {{ DEMO_CODE }}</view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { login } from '@/api'
import { TOKEN_KEY, USER_KEY, DEMO_CODE } from '@/config'

const phone = ref('')
const code = ref('')

const sendCode = () => {
  if (!/^1\d{10}$/.test(phone.value)) {
    return uni.showToast({ title: '请输入正确的手机号', icon: 'none' })
  }
  uni.showToast({ title: '演示验证码：' + DEMO_CODE, icon: 'none', duration: 2500 })
  code.value = DEMO_CODE
}

const doLogin = async () => {
  if (!/^1\d{10}$/.test(phone.value)) {
    return uni.showToast({ title: '请输入正确的手机号', icon: 'none' })
  }
  if (!code.value) {
    return uni.showToast({ title: '请输入验证码', icon: 'none' })
  }
  const data = await login(phone.value, code.value)
  uni.setStorageSync(TOKEN_KEY, data.token)
  uni.setStorageSync(USER_KEY, JSON.stringify({ id: data.id, name: data.name, phone: data.phone }))
  uni.showToast({ title: '欢迎，' + data.name, icon: 'none' })
  setTimeout(() => uni.reLaunch({ url: '/pages/index/index' }), 500)
}
</script>

<style scoped>
.login { padding: 100rpx 40rpx; }
.logo { text-align: center; margin-bottom: 60rpx; }
.mark {
  width: 96rpx; height: 96rpx; margin: 0 auto 20rpx;
  background: linear-gradient(135deg, #4b7dff, #2f5be0);
  border-radius: 24rpx; color: #fff; font-size: 52rpx; font-weight: 700;
  line-height: 96rpx;
}
.title { display: block; font-size: 40rpx; font-weight: 500; }
.sub { display: block; font-size: 24rpx; color: #9099a8; margin-top: 10rpx; }
.ipt {
  background: #f5f7fa; border-radius: 12rpx; padding: 20rpx 24rpx; font-size: 28rpx;
}
.code-row { display: flex; gap: 16rpx; }
.code-row .ipt { flex: 1; }
.code-btn {
  padding: 0 26rpx; line-height: 76rpx; font-size: 26rpx;
  color: #3b6ef0; background: #e6f1fb; border-radius: 12rpx;
}
.tip { text-align: center; color: #a8b3c9; font-size: 22rpx; margin-top: 30rpx; }
</style>
