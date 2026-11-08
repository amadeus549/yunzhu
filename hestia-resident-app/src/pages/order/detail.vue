<template>
  <view class="page" v-if="d.order">
    <view class="card">
      <view class="head-row">
        <text class="title">{{ d.order.title }}</text>
        <text class="tag">{{ d.order.statusName }}</text>
      </view>
      <text class="no">{{ d.order.orderNo }}</text>
      <view class="kv"><text class="k">类型</text><text>{{ d.order.categoryName }}</text></view>
      <view class="kv"><text class="k">地址</text><text>{{ d.order.premiseAddress }}</text></view>
      <view class="kv"><text class="k">优先级</text><text>{{ d.order.priorityName }}</text></view>
      <view class="kv" v-if="d.order.assigneeName"><text class="k">处理人</text><text>{{ d.order.assigneeName }}</text></view>
      <view class="kv" v-if="d.order.expectTime"><text class="k">期望上门</text><text>{{ fmt(d.order.expectTime) }}</text></view>
      <view class="kv" v-if="d.order.description"><text class="k">描述</text><text>{{ d.order.description }}</text></view>
    </view>

    <view class="card" v-if="d.items && d.items.length">
      <view class="sec">服务项</view>
      <view v-for="it in d.items" :key="it.id" class="item-row">
        <text>{{ it.itemName || ('服务项 #' + it.itemId) }}</text>
        <text class="qty">x{{ it.quantity }}</text>
      </view>
    </view>

    <view class="card" v-if="d.logs && d.logs.length">
      <view class="sec">处理进度</view>
      <view v-for="l in d.logs" :key="l.id" class="log">
        <view class="dot" :class="{ done: true }"></view>
        <view class="log-body">
          <text class="log-title">{{ l.actionName || l.action || '处理记录' }}</text>
          <text class="log-time">{{ fmt(l.createTime) }}{{ l.operatorName ? ' · ' + l.operatorName : '' }}</text>
          <text v-if="l.remark" class="log-remark">{{ l.remark }}</text>
        </view>
      </view>
    </view>

    <view class="card" v-if="d.order.rating">
      <view class="sec">我的评价</view>
      <view class="stars">
        <text v-for="n in 5" :key="n" class="star" :class="{ on: n <= d.order.rating }">★</text>
      </view>
      <text v-if="d.order.ratingComment" class="remark">{{ d.order.ratingComment }}</text>
    </view>

    <view class="ops" v-if="actions.length">
      <view
        v-for="a in actions" :key="a.key"
        class="op" :class="a.danger ? 'danger' : 'plain'"
        @click="a.run"
      >{{ a.label }}</view>
    </view>

    <!-- 验收弹层 -->
    <view class="mask" v-if="ratingVisible" @click="ratingVisible = false">
      <view class="dialog" @click.stop>
        <view class="d-title">验收评价</view>
        <view class="stars big">
          <text v-for="n in 5" :key="n" class="star" :class="{ on: n <= form.rating }" @click="form.rating = n">★</text>
        </view>
        <textarea v-model="form.comment" class="ipt" placeholder="说说这次服务体验（可选）" />
        <view class="btn-primary" style="margin-top: 26rpx" @click="doConfirm">确认验收</view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { orderDetail, confirmOrder, rejectOrder, cancelOrder, urgeOrder } from '@/api'

const d = ref({ order: null, items: [], logs: [], attachments: [] })
const orderId = ref(null)
const ratingVisible = ref(false)
const form = ref({ rating: 5, comment: '' })

const fmt = (s) => (s ? String(s).replace('T', ' ').slice(0, 16) : '')

const load = async () => {
  d.value = await orderDetail(orderId.value)
}

const st = computed(() => (d.value.order ? d.value.order.status : null))
const actions = computed(() => {
  const s = st.value
  const arr = []
  if (s === 1 || s === 2) {
    arr.push({ key: 'cancel', label: '取消工单', danger: true, run: cancel })
    arr.push({ key: 'urge', label: '催一催', danger: false, run: urge })
  } else if (s === 3 || s === 4 || s === 5) {
    arr.push({ key: 'urge', label: '催一催', danger: false, run: urge })
  } else if (s === 6) {
    arr.push({ key: 'reject', label: '验收驳回', danger: true, run: reject })
    arr.push({ key: 'confirm', label: '验收通过', danger: false, run: () => (ratingVisible.value = true) })
  }
  return arr
})

const urge = () => urgeOrder(orderId.value).then(() => {
  uni.showToast({ title: '已发出催单提醒', icon: 'none' })
  load()
})

const cancel = () => {
  uni.showModal({
    title: '取消工单',
    editable: true,
    placeholderText: '取消原因（可选）',
    success: (r) => {
      if (r.confirm) {
        cancelOrder(orderId.value, r.content || '').then(() => {
          uni.showToast({ title: '已取消', icon: 'none' })
          load()
        })
      }
    }
  })
}

const reject = () => {
  uni.showModal({
    title: '验收驳回',
    editable: true,
    placeholderText: '请填写驳回原因',
    success: (r) => {
      if (r.confirm) {
        if (!r.content) return uni.showToast({ title: '请填写驳回原因', icon: 'none' })
        rejectOrder(orderId.value, r.content).then(() => {
          uni.showToast({ title: '已驳回，物业将再次处理', icon: 'none' })
          load()
        })
      }
    }
  })
}

const doConfirm = () => {
  confirmOrder(orderId.value, form.value.rating, form.value.comment).then(() => {
    ratingVisible.value = false
    uni.showToast({ title: '感谢您的评价', icon: 'success' })
    load()
  })
}

onLoad((q) => { orderId.value = Number(q.id) })
onShow(() => { if (orderId.value) load() })
</script>

<style scoped>
.head-row { display: flex; justify-content: space-between; align-items: center; }
.title { font-size: 34rpx; font-weight: 500; flex: 1; }
.no { display: block; font-size: 22rpx; color: #a8b3c9; margin: 12rpx 0 20rpx; }
.kv { display: flex; margin-top: 14rpx; font-size: 26rpx; line-height: 1.6; }
.k { width: 140rpx; color: #9099a8; flex-shrink: 0; }
.sec { font-size: 28rpx; font-weight: 500; margin-bottom: 16rpx; }
.item-row {
  display: flex; justify-content: space-between; padding: 12rpx 0;
  font-size: 26rpx; border-bottom: 1rpx solid #eef1f6;
}
.qty { color: #9099a8; }
.log { display: flex; gap: 16rpx; padding: 10rpx 0; }
.dot {
  width: 14rpx; height: 14rpx; border-radius: 50%;
  background: #3b6ef0; margin-top: 12rpx; flex-shrink: 0;
}
.log-body { flex: 1; }
.log-title { display: block; font-size: 26rpx; }
.log-time { display: block; font-size: 22rpx; color: #a8b3c9; margin-top: 4rpx; }
.log-remark { display: block; font-size: 24rpx; color: #6b7789; margin-top: 6rpx; }
.stars .star { font-size: 36rpx; color: #d3d1c7; margin-right: 8rpx; }
.stars .star.on { color: #f59a23; }
.stars.big .star { font-size: 52rpx; }
.remark { display: block; font-size: 26rpx; color: #6b7789; margin-top: 10rpx; }
.ops { display: flex; gap: 20rpx; padding: 30rpx 20rpx; }
.op { flex: 1; text-align: center; border-radius: 12rpx; font-size: 28rpx; padding: 20rpx 0; }
.op.plain { background: #fff; color: #3b6ef0; border: 1rpx solid #3b6ef0; }
.op.danger { background: #fff; color: #f56c6c; border: 1rpx solid #f56c6c; }
.mask {
  position: fixed; inset: 0; background: rgba(0, 0, 0, 0.45);
  display: flex; align-items: center; justify-content: center; z-index: 99;
}
.dialog { width: 600rpx; background: #fff; border-radius: 20rpx; padding: 36rpx; }
.d-title { text-align: center; font-size: 32rpx; font-weight: 500; margin-bottom: 24rpx; }
.ipt {
  background: #f5f7fa; border-radius: 12rpx; padding: 20rpx 24rpx;
  font-size: 28rpx; height: 140rpx; margin-top: 20rpx;
}
</style>
