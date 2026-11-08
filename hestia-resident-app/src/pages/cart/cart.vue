<template>
  <view class="page">
    <view class="total card">
      <text>共 {{ list.length }} 项</text>
      <text class="sum">合计 ¥{{ totalAmount }}</text>
      <text class="clear" @click="clear">清空</text>
    </view>

    <view v-for="c in list" :key="c.id" class="card row">
      <view class="info">
        <text class="name">{{ c.itemName }}</text>
        <text v-if="c.specName" class="spec">{{ c.specName }}</text>
        <text class="price">¥{{ c.price }} / {{ c.unit || '次' }}</text>
      </view>
      <view class="right">
        <view class="stepper">
          <text class="step-btn" @click="change(c, -1)">−</text>
          <text class="step-num">{{ c.quantity }}</text>
          <text class="step-btn" @click="change(c, 1)">＋</text>
        </view>
        <text class="del" @click="remove(c)">移除</text>
      </view>
    </view>

    <view v-if="!list.length && !loading" class="empty">服务清单是空的</view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { cartList, cartUpdate, cartRemove, cartClear } from '@/api'

const list = ref([])
const loading = ref(false)

const totalAmount = computed(() =>
  list.value.reduce((s, c) => s + Number(c.price || 0) * (c.quantity || 0), 0).toFixed(2)
)

const load = async () => {
  loading.value = true
  try { list.value = (await cartList()) || [] } finally { loading.value = false }
}

const change = async (c, d) => {
  const next = Math.max(1, (c.quantity || 1) + d)
  await cartUpdate(c.id, next)
  c.quantity = next
}

const remove = async (c) => {
  await cartRemove(c.id)
  list.value = list.value.filter((x) => x.id !== c.id)
}

const clear = () => {
  if (!list.value.length) return
  uni.showModal({
    title: '提示',
    content: '确认清空服务清单？',
    success: async (r) => {
      if (r.confirm) {
        await cartClear()
        list.value = []
      }
    }
  })
}

onShow(load)
</script>

<style scoped>
.total { display: flex; align-items: center; gap: 20rpx; font-size: 26rpx; color: #6b7789; }
.sum { color: #f59a23; font-weight: 500; flex: 1; text-align: right; }
.clear { color: #f56c6c; }
.row { display: flex; justify-content: space-between; align-items: center; }
.name { display: block; font-size: 28rpx; font-weight: 500; }
.spec { display: block; font-size: 22rpx; color: #a8b3c9; margin-top: 4rpx; }
.price { display: block; font-size: 26rpx; color: #f59a23; margin-top: 6rpx; }
.right { display: flex; flex-direction: column; align-items: flex-end; gap: 14rpx; }
.stepper { display: flex; align-items: center; gap: 18rpx; }
.step-btn {
  width: 50rpx; height: 50rpx; line-height: 46rpx; text-align: center;
  border: 1rpx solid #d3d1c7; border-radius: 10rpx; font-size: 30rpx; color: #3b6ef0;
}
.step-num { min-width: 40rpx; text-align: center; }
.del { font-size: 24rpx; color: #f56c6c; }
</style>
