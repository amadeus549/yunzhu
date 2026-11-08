<template>
  <div class="page-container" v-loading="loading">
    <div class="page-card" v-if="detail.order">
      <!-- 头部 -->
      <div class="head">
        <div class="head-left">
          <el-button icon="el-icon-back" size="mini" @click="$router.back()">返回</el-button>
          <span class="order-no">{{ detail.order.orderNo }}</span>
          <el-tag :type="priorityType(detail.order.priority)" size="small" effect="plain">
            {{ detail.order.priorityName }}
          </el-tag>
          <el-tag :type="statusType(detail.order.status)" size="small">{{ detail.order.statusName }}</el-tag>
          <span class="sla" :class="'sla-' + (detail.order.slaStatus || 'normal').toLowerCase()">
            SLA {{ slaText(detail.order) }}
          </span>
        </div>
        <div class="head-actions">
          <el-button v-if="can(2)" type="primary" size="small" @click="onAccept">受理</el-button>
          <el-button v-if="can(3)" type="primary" size="small" @click="openDispatch">派单</el-button>
          <el-button v-if="can(3)" size="small" class="auto-btn" @click="onAutoDispatch">智能派单</el-button>
          <el-button v-if="can(4)" type="primary" size="small" @click="onTake">接单</el-button>
          <el-button v-if="can(3)" size="small" @click="onRejectDispatch">拒单</el-button>
          <el-button v-if="can(5)" type="primary" size="small" @click="onStart">开始处理</el-button>
          <el-button v-if="can(6)" type="primary" size="small" @click="onFinish">提交完工</el-button>
          <el-button v-if="can(7)" type="success" size="small" @click="onConfirm">验收通过</el-button>
          <el-button v-if="can(8)" type="warning" size="small" @click="onReject">验收驳回</el-button>
          <el-button v-if="can(0)" type="danger" size="small" plain @click="onCancel">取消工单</el-button>
        </div>
      </div>

      <el-divider />

      <!-- 基本信息 -->
      <el-descriptions :column="3" size="small" border>
        <el-descriptions-item label="工单类型">{{ detail.order.categoryName }}</el-descriptions-item>
        <el-descriptions-item label="房屋位置">{{ detail.order.premiseAddress || '—' }}</el-descriptions-item>
        <el-descriptions-item label="报修住户">
          {{ detail.order.residentName || '—' }}
          <span class="phone" v-if="detail.order.residentPhone">{{ detail.order.residentPhone }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="来源">{{ detail.order.sourceName }}</el-descriptions-item>
        <el-descriptions-item label="期望上门">{{ detail.order.expectTime || '—' }}</el-descriptions-item>
        <el-descriptions-item label="当前处理人">{{ detail.order.assigneeName || '未派单' }}</el-descriptions-item>
        <el-descriptions-item label="响应截止">{{ detail.order.responseDeadline || '—' }}</el-descriptions-item>
        <el-descriptions-item label="解决截止">{{ detail.order.resolveDeadline || '—' }}</el-descriptions-item>
        <el-descriptions-item label="费用合计">¥{{ detail.order.totalAmount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detail.order.createTime }}</el-descriptions-item>
        <el-descriptions-item label="返工次数">{{ detail.order.reworkCount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="满意度">
          <el-rate v-if="detail.order.rating" :value="detail.order.rating" disabled show-score text-color="#f5a524" />
          <span v-else>未评价</span>
        </el-descriptions-item>
      </el-descriptions>

      <div class="section">
        <div class="section-title">问题描述</div>
        <div class="desc">{{ detail.order.description || '无' }}</div>
        <div class="attachments" v-if="detail.attachments && detail.attachments.length">
          <el-image
            v-for="(url, i) in detail.attachments"
            :key="i"
            :src="url"
            :preview-src-list="detail.attachments"
            fit="cover"
            class="attach-img"
          />
        </div>
      </div>

      <div class="section">
        <div class="section-title">服务项明细</div>
        <el-table :data="detail.items" size="small" border>
          <el-table-column prop="itemName" label="服务项" min-width="160" />
          <el-table-column prop="specName" label="规格" width="120">
            <template slot-scope="{ row }">{{ row.specName || '—' }}</template>
          </el-table-column>
          <el-table-column prop="price" label="单价" width="100" />
          <el-table-column prop="quantity" label="数量" width="80" />
          <el-table-column prop="amount" label="小计" width="100" />
        </el-table>
      </div>

      <div class="section">
        <div class="section-title">流转记录</div>
        <el-timeline>
          <el-timeline-item
            v-for="log in detail.logs"
            :key="log.id"
            :timestamp="log.createTime"
            placement="top"
            :color="logColor(log.action)"
          >
            <div class="log-line">
              <b>{{ log.actionName }}</b>
              <span class="operator">{{ log.operatorName || '系统' }}</span>
              <span class="status-change" v-if="log.fromStatus !== null">
                {{ statusName(log.fromStatus) }} → {{ statusName(log.toStatus) }}
              </span>
            </div>
            <div class="log-remark" v-if="log.remark">{{ log.remark }}</div>
          </el-timeline-item>
        </el-timeline>
      </div>
    </div>

    <!-- 派单 -->
    <el-dialog title="派单" :visible.sync="dispatchVisible" width="720px">
      <el-table :data="candidates" size="small" border v-loading="candidateLoading" max-height="360">
        <el-table-column prop="staffName" label="工程师" width="100" />
        <el-table-column prop="skillTags" label="技能" min-width="140" />
        <el-table-column label="在手工单" width="100" align="center">
          <template slot-scope="{ row }">{{ row.activeCount }} / {{ row.workloadLimit }}</template>
        </el-table-column>
        <el-table-column prop="matchScore" label="匹配分" width="90" align="center" sortable />
        <el-table-column prop="reason" label="评分说明" min-width="230" show-overflow-tooltip />
        <el-table-column label="操作" width="80" fixed="right">
          <template slot-scope="{ row }">
            <el-button type="text" @click="doDispatch(row)">选择</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script>
import {
  getWorkOrder, acceptWorkOrder, dispatchCandidates, dispatchWorkOrder,
  autoDispatch, cancelWorkOrder
} from '@/api/workOrder'
import request from '@/utils/request'

const STATUS_NAME = {
  0: '已取消', 1: '待受理', 2: '待派单', 3: '已派单',
  4: '已接单', 5: '处理中', 6: '待验收', 7: '已完成', 8: '已驳回'
}

export default {
  name: 'WorkOrderDetail',
  data() {
    return {
      loading: false,
      detail: { order: null, items: [], logs: [], attachments: [] },
      dispatchVisible: false,
      candidateLoading: false,
      candidates: []
    }
  },
  created() {
    this.load()
  },
  methods: {
    async load() {
      this.loading = true
      try {
        const res = await getWorkOrder(this.$route.params.id)
        this.detail = res.data
      } finally {
        this.loading = false
      }
    },
    can(statusCode) {
      const list = this.detail.order && this.detail.order.allowedNext
      return Array.isArray(list) && list.indexOf(statusCode) >= 0
    },
    async onAccept() {
      await this.$confirm('确认受理该工单？', '提示', { type: 'info' })
      await acceptWorkOrder(this.detail.order.id)
      this.$message.success('已受理')
      this.load()
    },
    async openDispatch() {
      this.dispatchVisible = true
      this.candidateLoading = true
      try {
        const res = await dispatchCandidates(this.detail.order.id)
        this.candidates = res.data || []
      } finally {
        this.candidateLoading = false
      }
    },
    async doDispatch(row) {
      await dispatchWorkOrder(this.detail.order.id, { orderId: this.detail.order.id, staffId: row.staffId })
      this.$message.success('已派单给 ' + row.staffName)
      this.dispatchVisible = false
      this.load()
    },
    async onAutoDispatch() {
      await this.$confirm('按「技能 × 负载 × 间隔」自动选择工程师？', '智能派单', { type: 'info' })
      await autoDispatch(this.detail.order.id)
      this.$message.success('智能派单成功')
      this.load()
    },
    async onTake() {
      await request({ url: `/admin/work-order/${this.detail.order.id}/take`, method: 'put' })
      this.$message.success('接单成功')
      this.load()
    },
    async onRejectDispatch() {
      const { value } = await this.$prompt('请填写拒单原因', '拒单', { inputPlaceholder: '如：不在负责区域' })
      await request({
        url: `/admin/work-order/${this.detail.order.id}/reject`,
        method: 'put',
        params: { reason: value }
      })
      this.$message.success('已退回派单池')
      this.load()
    },
    async onStart() {
      await request({ url: `/admin/work-order/${this.detail.order.id}/start`, method: 'put' })
      this.$message.success('已开始处理')
      this.load()
    },
    async onFinish() {
      const { value } = await this.$prompt('完工说明', '提交完工', {
        inputPlaceholder: '如：已更换角阀并试水正常'
      })
      await request({
        url: `/admin/work-order/${this.detail.order.id}/finish`,
        method: 'put',
        params: { remark: value }
      })
      this.$message.success('已提交完工，等待住户验收')
      this.load()
    },
    async onConfirm() {
      await this.$confirm('确认验收通过？', '验收', { type: 'success' })
      await request({
        url: `/admin/work-order/${this.detail.order.id}/confirm`,
        method: 'put',
        data: { rating: 5, comment: '管家代验收' }
      })
      this.$message.success('工单已完成')
      this.load()
    },
    async onReject() {
      const { value } = await this.$prompt('驳回原因', '验收驳回', { inputPlaceholder: '如：问题未解决' })
      await request({
        url: `/admin/work-order/${this.detail.order.id}/reject-accept`,
        method: 'put',
        data: { reason: value }
      })
      this.$message.success('已驳回，等待返工')
      this.load()
    },
    async onCancel() {
      const { value } = await this.$prompt('取消原因', '取消工单', { inputPlaceholder: '如：住户自行处理' })
      await cancelWorkOrder(this.detail.order.id, { reason: value })
      this.$message.success('已取消')
      this.load()
    },
    priorityType(p) {
      return { 4: 'danger', 3: 'warning', 2: '', 1: 'info' }[p] || ''
    },
    statusType(s) {
      return { 0: 'info', 1: 'warning', 2: 'warning', 3: '', 4: '', 5: '', 6: 'warning', 7: 'success', 8: 'danger' }[s] || ''
    },
    statusName(s) {
      return STATUS_NAME[s] || '—'
    },
    slaText(o) {
      if (o.slaStatus === 'OVERDUE') return '已超时'
      if (o.slaRemainMinutes === null || o.slaRemainMinutes === undefined) return '—'
      return '剩余 ' + o.slaRemainMinutes + ' 分钟'
    },
    logColor(action) {
      if (action === 'CREATE') return '#3b6ef0'
      if (action === 'CONFIRM') return '#22b07d'
      if (action === 'REJECT' || action === 'REJECT_DISPATCH') return '#f56c6c'
      if (action === 'CANCEL') return '#c0c4cc'
      return '#e6a23c'
    }
  }
}
</script>

<style lang="scss" scoped>
.head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;

  .head-left {
    display: flex;
    align-items: center;
    gap: 10px;
  }

  .order-no {
    font-size: 16px;
    font-weight: 600;
    color: #1f2733;
  }

  .sla { font-size: 12.5px; }
  .sla-normal { color: #22b07d; }
  .sla-warning { color: #f5a524; }
  .sla-overdue { color: #f56c6c; font-weight: 600; }

  .head-actions { display: flex; gap: 8px; flex-wrap: wrap; }
  .auto-btn { color: #7c5cff; border-color: #c9b8ff; }
}

.phone { color: #9099a8; font-size: 12px; margin-left: 6px; }

.section {
  margin-top: 24px;

  .section-title {
    font-size: 14px;
    font-weight: 600;
    color: #1f2733;
    margin-bottom: 12px;
    padding-left: 8px;
    border-left: 3px solid #3b6ef0;
  }

  .desc {
    font-size: 13px;
    color: #4a5568;
    line-height: 1.8;
    background: #f7f9fc;
    padding: 12px 14px;
    border-radius: 6px;
  }

  .attachments {
    margin-top: 10px;
    display: flex;
    gap: 10px;

    .attach-img {
      width: 96px;
      height: 96px;
      border-radius: 6px;
    }
  }
}

.log-line {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 13px;
  color: #1f2733;

  .operator { color: #6b7789; font-size: 12px; }
  .status-change { color: #a8b3c9; font-size: 12px; }
}

.log-remark {
  font-size: 12.5px;
  color: #6b7789;
  margin-top: 4px;
}
</style>
