<template>
  <view class="page">
    <view class="readall" @click="readAll">
      <text v-if="unread > 0">{{ unread }} 条未读 · 全部标记已读</text>
      <text v-else>全部已读</text>
    </view>

    <view v-for="n in list" :key="n.id" class="card" :class="{ unread: !n.isRead }">
      <view class="n-title">{{ n.title }}</view>
      <view class="n-content">{{ n.content }}</view>
      <view class="n-time">{{ fmt(n.createTime) }}</view>
    </view>

    <view v-if="!list.length && !loading" class="empty">暂无通知</view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { noticeList, noticeUnread, noticeReadAll } from '@/api'

const list = ref([])
const unread = ref(0)
const loading = ref(false)
const fmt = (s) => (s ? String(s).replace('T', ' ').slice(0, 16) : '')

const load = async () => {
  loading.value = true
  try {
    list.value = (await noticeList()) || []
    unread.value = (await noticeUnread()) || 0
  } finally { loading.value = false }
}

const readAll = async () => {
  await noticeReadAll()
  list.value = list.value.map((n) => ({ ...n, isRead: 1 }))
  unread.value = 0
  uni.showToast({ title: '已全部标记为已读', icon: 'none' })
}

onShow(load)
</script>

<style scoped>
.readall {
  text-align: right; padding: 20rpx; font-size: 24rpx; color: #3b6ef0;
}
.n-title { font-size: 28rpx; font-weight: 500; }
.n-content { font-size: 26rpx; color: #6b7789; margin-top: 10rpx; line-height: 1.6; }
.n-time { font-size: 22rpx; color: #a8b3c9; margin-top: 10rpx; }
.card.unread .n-title::before {
  content: '';
  display: inline-block; width: 12rpx; height: 12rpx; border-radius: 50%;
  background: #f56c6c; margin-right: 10rpx;
}
</style>
