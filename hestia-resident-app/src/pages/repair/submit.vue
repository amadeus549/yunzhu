<template>
  <view class="page">
    <view class="card">
      <view class="field-label">报修类型</view>
      <picker :range="categoryNames" @change="onCategory">
        <view class="ipt">{{ form.categoryId ? categoryNames[categoryIndex] : '请选择类型' }}</view>
      </picker>

      <view class="field-label" style="margin-top: 26rpx">报修房屋</view>
      <picker :range="premiseNames" @change="onPremise">
        <view class="ipt">{{ form.premiseId ? premiseNames[premiseIndex] : '请选择房屋' }}</view>
      </picker>

      <view class="field-label" style="margin-top: 26rpx">标题</view>
      <input v-model="form.title" class="ipt" maxlength="32" placeholder="一句话描述问题，如：厨房水管漏水" />

      <view class="field-label" style="margin-top: 26rpx">详细描述</view>
      <textarea v-model="form.description" class="ipt area" maxlength="300"
        placeholder="补充位置、现象、影响等信息" />

      <view class="field-label" style="margin-top: 26rpx">紧急程度</view>
      <picker :range="priorityNames" @change="onPriority">
        <view class="ipt">{{ priorityNames[form.priority - 1] }}</view>
      </picker>

      <view class="field-label" style="margin-top: 26rpx">期望上门时间（可选）</view>
      <view class="row2">
        <picker mode="date" :value="date" :start="today" @change="(e) => (date = e.detail.value)">
          <view class="ipt">{{ date || '选择日期' }}</view>
        </picker>
        <picker mode="time" :value="time" @change="(e) => (time = e.detail.value)">
          <view class="ipt">{{ time || '选择时间' }}</view>
        </picker>
      </view>

      <view class="field-label" style="margin-top: 26rpx">附加服务项（可选，数量为 0 不携带）</view>
      <view v-if="!services.length" class="empty" style="padding: 20rpx 0">暂无可选服务项</view>
      <view v-for="s in services" :key="s.id" class="svc-row">
        <view class="svc-info">
          <text class="svc-name">{{ s.name }}</text>
          <text class="svc-price">¥{{ s.price }} / {{ s.unit }}</text>
        </view>
        <view class="stepper">
          <text class="step-btn" @click="step(s, -1)">−</text>
          <text class="step-num">{{ qty[s.id] || 0 }}</text>
          <text class="step-btn" @click="step(s, 1)">＋</text>
        </view>
      </view>
    </view>

    <view class="submit btn-primary" @click="submit">提交报修</view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { categoryList, serviceList, myPremise, submitOrder, cartAdd } from '@/api'
import { USER_KEY } from '@/config'

const categories = ref([])
const premises = ref([])
const services = ref([])
const categoryIndex = ref(-1)
const premiseIndex = ref(-1)
const date = ref('')
const time = ref('')
const qty = ref({})
const today = new Date().toISOString().slice(0, 10)

const form = ref({ categoryId: null, premiseId: null, title: '', description: '', priority: 2 })
const categoryNames = computed(() => categories.value.map((c) => c.name))
const premiseNames = computed(() => premises.value.map((p) => p.fullAddress || p.community))
const priorityNames = ['低', '普通', '高', '紧急']

onShow(async () => {
  const [c, p, u] = await Promise.all([
    categoryList().catch(() => []),
    myPremise().catch(() => []),
    Promise.resolve(JSON.parse(uni.getStorageSync(USER_KEY) || '{}'))
  ])
  categories.value = c || []
  premises.value = p || []
  if (u.id) {
    // 默认选中该住户名下的第一个默认房屋
    const def = (p || []).find((x) => x.isDefault === 1) || (p || [])[0]
    if (def) {
      premiseIndex.value = premises.value.indexOf(def)
      form.value.premiseId = def.id
    }
  }
})

const onCategory = async (e) => {
  categoryIndex.value = Number(e.detail.value)
  form.value.categoryId = categories.value[categoryIndex.value]?.id
  // 类型变化后加载该类型下可加购的服务项
  qty.value = {}
  services.value = form.value.categoryId ? (await serviceList(form.value.categoryId).catch(() => [])) : []
}
const onPremise = (e) => {
  premiseIndex.value = Number(e.detail.value)
  form.value.premiseId = premises.value[premiseIndex.value]?.id
}
const onPriority = (e) => { form.value.priority = Number(e.detail.value) + 1 }

const step = (s, d) => {
  const next = Math.max(0, (qty.value[s.id] || 0) + d)
  qty.value = { ...qty.value, [s.id]: next }
}

const submit = async () => {
  if (!form.value.categoryId) return uni.showToast({ title: '请选择报修类型', icon: 'none' })
  if (!form.value.premiseId) return uni.showToast({ title: '请选择报修房屋', icon: 'none' })
  if (!form.value.title.trim()) return uni.showToast({ title: '请填写标题', icon: 'none' })

  const items = services.value
    .filter((s) => (qty.value[s.id] || 0) > 0)
    .map((s) => ({ itemId: s.id, specId: null, quantity: qty.value[s.id] }))

  const expectTime =
    date.value && time.value ? `${date.value}T${time.value}:00` : null

  const id = await submitOrder({ ...form.value, title: form.value.title.trim(), expectTime, items })
  // 已选服务项同步进服务清单，便于后续查看
  for (const it of items) {
    await cartAdd({ itemId: it.itemId, quantity: it.quantity }).catch(() => {})
  }
  uni.showToast({ title: '报修已提交', icon: 'success' })
  setTimeout(() => uni.redirectTo({ url: '/pages/order/detail?id=' + id }), 600)
}
</script>

<style scoped>
.ipt {
  background: #f5f7fa; border-radius: 12rpx; padding: 20rpx 24rpx; font-size: 28rpx;
}
.area { height: 160rpx; width: auto; }
.row2 { display: flex; gap: 16rpx; }
.row2 picker { flex: 1; }
.svc-row {
  display: flex; align-items: center; justify-content: space-between;
  padding: 16rpx 0; border-bottom: 1rpx solid #eef1f6;
}
.svc-name { display: block; font-size: 28rpx; }
.svc-price { display: block; font-size: 24rpx; color: #f59a23; margin-top: 4rpx; }
.stepper { display: flex; align-items: center; gap: 20rpx; }
.step-btn {
  width: 52rpx; height: 52rpx; line-height: 48rpx; text-align: center;
  border: 1rpx solid #d3d1c7; border-radius: 10rpx; font-size: 32rpx; color: #3b6ef0;
}
.step-num { min-width: 44rpx; text-align: center; font-size: 28rpx; }
.submit { margin: 30rpx 20rpx; }
</style>
