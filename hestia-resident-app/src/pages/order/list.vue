<template>
  <view class="page">
    <scroll-view scroll-x class="tabs">
      <view
        v-for="t in tabs"
        :key="String(t.value)"
        class="tab"
        :class="{ active: status === t.value }"
        @click="switchTab(t.value)"
      >
        {{ t.label }}
      </view>
    </scroll-view>

    <view v-for="o in list" :key="o.id" class="card order" @click="goDetail(o.id)">
      <view class="o-head">
        <text class="o-no">{{ o.orderNo }}</text>
        <text class="tag">{{ o.statusName }}</text>
      </view>
      <text class="o-title">{{ o.title }}</text>
      <view class="o-meta">
        <text>{{ o.categoryName }}</text>
        <text v-if="o.premiseAddress" class="o-addr">{{ o.premiseAddress }}</text>
      </view>
      <view class="o-foot">
        <text>{{ o.createTime }}</text>
        <text v-if="o.assigneeName">处理人：{{ o.assigneeName }}</text>
      </view>
    </view>

    <view v-if="!list.length && !loading" class="empty">暂无工单，去首页报一单吧</view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow, onReachBottom } from '@dcloudio/uni-app'
import { myOrders } from '@/api'

const tabs = [
  { label: '全部', value: null },
  { label: '待受理', value: 1 },
  { label: '待派单', value: 2 },
  { label: '处理中', value: 5 },
  { label: '待验收', value: 6 },
  { label: '已完成', value: 7 },
  { label: '已取消', value: 0 }
]

const status = ref(null)
const list = ref([])
const page = ref(1)
const total = ref(0)
const loading = ref(false)

const load = async (reset = false) => {
  if (loading.value) return
  if (reset) { page.value = 1; total.value = 0 }
  loading.value = true
  try {
    const data = await myOrders(page.value, 10, status.value)
    const records = data.records || []
    list.value = reset ? records : list.value.concat(records)
    total.value = data.total || 0
    page.value += 1
  } finally { loading.value = false }
}

const switchTab = (v) => { status.value = v; load(true) }
const goDetail = (id) => uni.navigateTo({ url: '/pages/order/detail?id=' + id })

onShow(() => load(true))
onReachBottom(() => {
  if (list.value.length < total.value) load()
})
</script>

<style scoped>
.tabs {
  white-space: nowrap; background: #fff; padding: 16rpx 12rpx;
  border-bottom: 1rpx solid #eef1f6;
}
.tab {
  display: inline-block; padding: 10rpx 26rpx; margin: 0 8rpx;
  font-size: 26rpx; color: #6b7789; border-radius: 28rpx; background: #f5f7fa;
}
.tab.active { background: #3b6ef0; color: #fff; }
.order { margin-top: 20rpx; }
.o-head { display: flex; justify-content: space-between; align-items: center; }
.o-no { font-size: 22rpx; color: #a8b3c9; }
.o-title { display: block; font-size: 30rpx; font-weight: 500; margin-top: 12rpx; }
.o-meta {
  display: flex; gap: 20rpx; margin-top: 12rpx;
  font-size: 24rpx; color: #6b7789;
}
.o-foot {
  display: flex; justify-content: space-between; margin-top: 14rpx;
  font-size: 22rpx; color: #a8b3c9;
}
</style>
