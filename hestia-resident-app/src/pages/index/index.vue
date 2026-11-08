<template>
  <view class="home">
    <view class="head">
      <text class="hi">你好，{{ user.name || '住户' }}</text>
      <text class="sub">有事随时报，物业马上办</text>
    </view>

    <view class="quick card" @click="go('/pages/repair/submit')">
      <view>
        <text class="q-title">我要报修</text>
        <text class="q-sub">水电 / 电梯 / 公共设施，一键报修</text>
      </view>
      <text class="q-arrow">›</text>
    </view>

    <view class="grid card">
      <view class="g-item" @click="go('/pages/order/list')">
        <text class="g-num">{{ '' }}</text>
        <text class="g-icon">🧾</text>
        <text class="g-name">我的工单</text>
      </view>
      <view class="g-item" @click="go('/pages/cart/cart')">
        <text class="g-icon">🧺</text>
        <text class="g-name">服务清单</text>
      </view>
      <view class="g-item" @click="go('/pages/premise/premise')">
        <text class="g-icon">🏠</text>
        <text class="g-name">我的房屋</text>
      </view>
      <view class="g-item" @click="go('/pages/notice/notice')">
        <view class="badge-wrap">
          <text class="g-icon">🔔</text>
          <text v-if="unread > 0" class="badge">{{ unread > 99 ? '99+' : unread }}</text>
        </view>
        <text class="g-name">消息通知</text>
      </view>
    </view>

    <view class="card logout" @click="logout">
      <text>退出登录</text>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { noticeUnread } from '@/api'
import { TOKEN_KEY, USER_KEY } from '@/config'

const user = ref({})
const unread = ref(0)

onShow(() => {
  const token = uni.getStorageSync(TOKEN_KEY)
  if (!token) {
    uni.reLaunch({ url: '/pages/login/login' })
    return
  }
  try { user.value = JSON.parse(uni.getStorageSync(USER_KEY) || '{}') } catch (e) { user.value = {} }
  noticeUnread().then((n) => { unread.value = n || 0 }).catch(() => {})
})

const go = (url) => uni.navigateTo({ url })

const logout = () => {
  uni.showModal({
    title: '提示',
    content: '确认退出登录？',
    success: (r) => {
      if (r.confirm) {
        uni.removeStorageSync(TOKEN_KEY)
        uni.removeStorageSync(USER_KEY)
        uni.reLaunch({ url: '/pages/login/login' })
      }
    }
  })
}
</script>

<style scoped>
.head {
  background: linear-gradient(135deg, #4b7dff, #2f5be0);
  padding: 50rpx 40rpx 70rpx;
  color: #fff;
}
.hi { display: block; font-size: 40rpx; font-weight: 500; }
.sub { display: block; font-size: 24rpx; opacity: 0.85; margin-top: 12rpx; }
.quick {
  margin-top: -40rpx;
  display: flex; align-items: center; justify-content: space-between;
}
.q-title { display: block; font-size: 32rpx; font-weight: 500; color: #3b6ef0; }
.q-sub { display: block; font-size: 24rpx; color: #9099a8; margin-top: 8rpx; }
.q-arrow { font-size: 48rpx; color: #c0c8d4; }
.grid { display: flex; }
.g-item { flex: 1; text-align: center; padding: 10rpx 0; }
.g-icon { font-size: 44rpx; }
.g-name { display: block; font-size: 24rpx; color: #6b7789; margin-top: 10rpx; }
.badge-wrap { position: relative; display: inline-block; }
.badge {
  position: absolute; top: -12rpx; right: -24rpx;
  background: #f56c6c; color: #fff; font-size: 18rpx;
  border-radius: 16rpx; padding: 2rpx 10rpx;
}
.logout { text-align: center; color: #f56c6c; }
</style>
