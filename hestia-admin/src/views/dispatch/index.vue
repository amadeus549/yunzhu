<template>
  <div class="page-container">
    <el-row :gutter="14">
      <!-- 待派单工单 -->
      <el-col :span="9">
        <div class="page-card">
          <div class="toolbar">
            <span class="title">待派单工单 <span class="count">{{ orders.length }}</span></span>
            <el-button type="text" icon="el-icon-refresh" @click="loadOrders">刷新</el-button>
          </div>
          <div class="order-list" v-loading="orderLoading">
            <el-empty v-if="!orders.length" description="暂无待派单工单" :image-size="70" />
            <div
              v-for="o in orders"
              :key="o.id"
              class="order-item"
              :class="{ active: current && current.id === o.id }"
              @click="select(o)"
            >
              <div class="row1">
                <span class="no">{{ o.orderNo }}</span>
                <el-tag :type="priorityType(o.priority)" size="mini" effect="plain">
                  {{ o.priorityName }}
                </el-tag>
              </div>
              <div class="title">{{ o.title }}</div>
              <div class="meta">
                <span>{{ o.categoryName }}</span>
                <span>{{ o.premiseAddress }}</span>
              </div>
              <div class="meta2">
                <span class="sla" :class="'sla-' + (o.slaStatus || 'normal').toLowerCase()">
                  {{ slaText(o) }}
                </span>
                <span class="time">{{ o.createTime }}</span>
              </div>
            </div>
          </div>
        </div>
      </el-col>

      <!-- 候选人 -->
      <el-col :span="15">
        <div class="page-card">
          <div class="toolbar">
            <span class="title">派单候选人</span>
            <div v-if="current">
              <el-button size="small" @click="openManual">手动选择</el-button>
              <el-button size="small" type="primary" @click="autoAssign">智能派单</el-button>
            </div>
          </div>

          <div v-if="!current" class="empty-tip">
            <i class="el-icon-s-promotion"></i>
            <p>从左侧选择一张工单，查看系统推荐的工程师</p>
          </div>

          <template v-else>
            <div class="current-order">
              <div class="co-title">{{ current.title }}</div>
              <div class="co-meta">
                {{ current.orderNo }} · {{ current.categoryName }} · {{ current.premiseAddress }}
              </div>
            </div>

            <div class="cand-list" v-loading="candLoading">
              <el-empty v-if="!candidates.length" description="暂无可用工程师" :image-size="70" />
              <div v-for="(c, i) in candidates" :key="c.staffId" class="cand-card" :class="{ top: i === 0 }">
                <div class="rank" :class="{ first: i === 0 }">{{ i + 1 }}</div>
                <div class="avatar">{{ c.staffName.charAt(0) }}</div>
                <div class="info">
                  <div class="name">
                    {{ c.staffName }}
                    <el-tag v-if="i === 0" size="mini" type="success" effect="dark">推荐</el-tag>
                    <el-tag v-if="!c.matched" size="mini" type="info">技能不匹配</el-tag>
                  </div>
                  <div class="skills">
                    <el-tag v-for="s in (c.skillTags || '').split(',').filter(Boolean)" :key="s" size="mini" effect="plain">
                      {{ s }}
                    </el-tag>
                  </div>
                  <div class="reason">{{ c.reason }}</div>
                </div>
                <div class="metrics">
                  <div class="m">
                    <span class="m-val">{{ c.activeCount }}/{{ c.workloadLimit }}</span>
                    <span class="m-label">在手工单</span>
                  </div>
                  <div class="score-ring">
                    <el-progress
                      type="circle"
                      :percentage="c.matchScore"
                      :width="58"
                      :stroke-width="6"
                      :color="scoreColor(c.matchScore)"
                    />
                  </div>
                </div>
                <el-button type="primary" size="small" @click="assign(c)">派单</el-button>
              </div>
            </div>
          </template>
        </div>
      </el-col>
    </el-row>

    <!-- 手动选择工程师 -->
    <el-dialog title="手动派单" :visible.sync="manualVisible" width="420px">
      <el-select v-model="manualStaffId" placeholder="选择工程师" filterable style="width: 100%">
        <el-option
          v-for="s in allEngineers"
          :key="s.id"
          :label="s.name + '（' + (s.skillTags || '无技能') + '）'"
          :value="s.id"
        />
      </el-select>
      <span slot="footer">
        <el-button @click="manualVisible = false">取消</el-button>
        <el-button type="primary" :disabled="!manualStaffId" @click="manualAssign">确定派单</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { pageWorkOrder, dispatchCandidates, dispatchWorkOrder, autoDispatch } from '@/api/workOrder'
import { listEngineers } from '@/api/basic'

export default {
  name: 'DispatchBoard',
  data() {
    return {
      orderLoading: false,
      orders: [],
      current: null,
      candLoading: false,
      candidates: [],
      manualVisible: false,
      manualStaffId: null,
      allEngineers: []
    }
  },
  created() {
    this.loadOrders()
  },
  methods: {
    async loadOrders() {
      this.orderLoading = true
      try {
        const res = await pageWorkOrder({ page: 1, pageSize: 50, status: 2 })
        this.orders = (res.data && res.data.records) || []
        if (this.current) {
          const still = this.orders.find(o => o.id === this.current.id)
          this.current = still || null
        }
        if (!this.current && this.orders.length) {
          this.select(this.orders[0])
        }
      } finally {
        this.orderLoading = false
      }
    },
    async select(o) {
      this.current = o
      this.candLoading = true
      try {
        const res = await dispatchCandidates(o.id)
        this.candidates = res.data || []
      } finally {
        this.candLoading = false
      }
    },
    async assign(c) {
      await dispatchWorkOrder(this.current.id, { orderId: this.current.id, staffId: c.staffId })
      this.$message.success(`已派单给 ${c.staffName}`)
      this.current = null
      this.candidates = []
      this.loadOrders()
    },
    async autoAssign() {
      await this.$confirm('按「技能 × 负载 × 间隔」自动选人派单？', '智能派单', { type: 'info' })
      await autoDispatch(this.current.id)
      this.$message.success('智能派单成功')
      this.current = null
      this.candidates = []
      this.loadOrders()
    },
    async openManual() {
      if (!this.allEngineers.length) {
        const res = await listEngineers({})
        this.allEngineers = res.data || []
      }
      this.manualStaffId = null
      this.manualVisible = true
    },
    async manualAssign() {
      await dispatchWorkOrder(this.current.id, { orderId: this.current.id, staffId: this.manualStaffId })
      this.$message.success('派单成功')
      this.manualVisible = false
      this.current = null
      this.loadOrders()
    },
    priorityType(p) {
      return { 4: 'danger', 3: 'warning', 2: '', 1: 'info' }[p] || ''
    },
    slaText(o) {
      if (o.slaStatus === 'OVERDUE') return '已超时'
      if (o.slaRemainMinutes === null || o.slaRemainMinutes === undefined) return '—'
      return '剩余 ' + o.slaRemainMinutes + ' 分钟'
    },
    scoreColor(score) {
      if (score >= 80) return '#22b07d'
      if (score >= 60) return '#3b6ef0'
      if (score >= 40) return '#f5a524'
      return '#f56c6c'
    }
  }
}
</script>

<style lang="scss" scoped>
.count {
  font-size: 12px;
  color: #3b6ef0;
  background: #eef3ff;
  border-radius: 10px;
  padding: 1px 8px;
  margin-left: 6px;
}

.order-list {
  max-height: calc(100vh - 200px);
  overflow-y: auto;
}

.order-item {
  border: 1px solid #eef1f6;
  border-radius: 8px;
  padding: 12px 14px;
  margin-bottom: 10px;
  cursor: pointer;
  transition: all 0.15s;

  &:hover { border-color: #b9cdfb; }
  &.active {
    border-color: #3b6ef0;
    background: #f6f9ff;
    box-shadow: 0 2px 8px rgba(59, 110, 240, 0.1);
  }

  .row1 {
    display: flex;
    justify-content: space-between;
    align-items: center;
    .no { font-size: 12px; color: #9099a8; }
  }

  .title {
    font-size: 14px;
    font-weight: 600;
    color: #1f2733;
    margin: 6px 0 4px;
  }

  .meta {
    font-size: 12px;
    color: #6b7789;
    display: flex;
    gap: 10px;
  }

  .meta2 {
    margin-top: 6px;
    display: flex;
    justify-content: space-between;
    font-size: 11.5px;
  }

  .time { color: #a8b3c9; }
}

.sla-normal { color: #22b07d; }
.sla-warning { color: #f5a524; }
.sla-overdue { color: #f56c6c; font-weight: 600; }

.empty-tip {
  text-align: center;
  padding: 80px 0;
  color: #b6bfcc;

  i { font-size: 40px; }
  p { font-size: 13px; margin-top: 10px; }
}

.current-order {
  background: #f7f9fc;
  border-radius: 8px;
  padding: 12px 14px;
  margin-bottom: 14px;

  .co-title { font-size: 15px; font-weight: 600; color: #1f2733; }
  .co-meta { font-size: 12px; color: #6b7789; margin-top: 4px; }
}

.cand-card {
  display: flex;
  align-items: center;
  gap: 14px;
  border: 1px solid #eef1f6;
  border-radius: 10px;
  padding: 14px 16px;
  margin-bottom: 10px;
  transition: all 0.15s;

  &.top { border-color: #b7e2cd; background: #f7fdfa; }
  &:hover { box-shadow: 0 2px 10px rgba(16, 24, 40, 0.06); }

  .rank {
    width: 24px;
    height: 24px;
    border-radius: 50%;
    background: #eef1f6;
    color: #6b7789;
    font-size: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: 600;
    flex-shrink: 0;

    &.first { background: #22b07d; color: #fff; }
  }

  .avatar {
    width: 38px;
    height: 38px;
    border-radius: 50%;
    background: linear-gradient(135deg, #6b93ff, #3b6ef0);
    color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 15px;
    flex-shrink: 0;
  }

  .info {
    flex: 1;
    min-width: 0;

    .name {
      font-size: 14px;
      font-weight: 600;
      color: #1f2733;
      display: flex;
      align-items: center;
      gap: 6px;
    }

    .skills { margin: 5px 0; display: flex; gap: 4px; flex-wrap: wrap; }

    .reason { font-size: 11.5px; color: #9099a8; }
  }

  .metrics {
    display: flex;
    align-items: center;
    gap: 18px;

    .m { text-align: center; }
    .m-val { display: block; font-size: 15px; font-weight: 600; color: #1f2733; }
    .m-label { font-size: 11px; color: #a8b3c9; }
  }
}
</style>
