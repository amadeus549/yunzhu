<template>
  <view class="page">
    <view v-for="p in list" :key="p.id" class="card">
      <view class="head-row">
        <text class="addr">{{ p.fullAddress }}</text>
        <text v-if="p.isDefault === 1" class="tag">默认</text>
      </view>
      <view class="meta">
        <text class="tag">{{ typeName(p.premiseType) }}</text>
        <text v-if="p.area" class="area">{{ p.area }} ㎡</text>
      </view>
    </view>
    <view v-if="!list.length && !loading" class="empty">名下暂无房屋，请联系物业登记</view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { myPremise } from '@/api'

const list = ref([])
const loading = ref(false)
const typeName = (t) => ({ 1: '住宅', 2: '商铺', 3: '公共区域' }[t] || '其他')

onShow(async () => {
  loading.value = true
  try { list.value = (await myPremise()) || [] } finally { loading.value = false }
})
</script>

<style scoped>
.head-row { display: flex; justify-content: space-between; align-items: center; }
.addr { font-size: 30rpx; font-weight: 500; }
.meta { display: flex; align-items: center; gap: 20rpx; margin-top: 16rpx; }
.area { font-size: 24rpx; color: #9099a8; }
</style>
