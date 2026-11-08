<template>
  <div class="page-container">
    <div class="page-card">
      <div class="filter-bar">
        <el-input v-model="query.orderNo" placeholder="工单号" clearable @keyup.enter.native="search" />
        <el-input v-model="query.title" placeholder="标题关键词" clearable @keyup.enter.native="search" />
        <el-select v-model="query.status" placeholder="状态" clearable>
          <el-option v-for="s in statusOptions" :key="s.value" :label="s.label" :value="s.value" />
        </el-select>
        <el-select v-model="query.priority" placeholder="优先级" clearable>
          <el-option label="紧急" :value="4" />
          <el-option label="高" :value="3" />
          <el-option label="普通" :value="2" />
          <el-option label="低" :value="1" />
        </el-select>
        <el-select v-model="query.categoryId" placeholder="工单类型" clearable>
          <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
        </el-select>
        <el-select v-model="query.slaBreached" placeholder="SLA" clearable>
          <el-option label="已超时" :value="1" />
          <el-option label="未超时" :value="0" />
        </el-select>
        <el-button type="primary" icon="el-icon-search" @click="search">查询</el-button>
        <el-button icon="el-icon-refresh" @click="reset">重置</el-button>
      </div>

      <div class="toolbar">
        <span class="title">工单列表 <span class="count">共 {{ total }} 条</span></span>
        <el-button type="primary" size="small" icon="el-icon-plus" @click="openCreate">代报工单</el-button>
      </div>

      <el-table :data="list" v-loading="loading" size="small" border stripe>
        <el-table-column prop="orderNo" label="工单号" width="145" />
        <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
        <el-table-column prop="categoryName" label="类型" width="95" />
        <el-table-column prop="premiseAddress" label="位置" min-width="170" show-overflow-tooltip />
        <el-table-column prop="residentName" label="住户" width="85" />
        <el-table-column label="优先级" width="80" align="center">
          <template slot-scope="{ row }">
            <el-tag :type="priorityType(row.priority)" size="mini" effect="plain">
              {{ row.priorityName }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template slot-scope="{ row }">
            <span class="status-dot" :style="{ background: statusColor(row.status) }"></span>
            {{ row.statusName }}
          </template>
        </el-table-column>
        <el-table-column label="SLA" width="105">
          <template slot-scope="{ row }">
            <span :class="'sla-' + (row.slaStatus || 'NORMAL').toLowerCase()">{{ slaText(row) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="assigneeName" label="处理人" width="85">
          <template slot-scope="{ row }">{{ row.assigneeName || '—' }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="150" />
        <el-table-column label="操作" width="200" fixed="right">
          <template slot-scope="{ row }">
            <el-button type="text" @click="$router.push('/work-order/' + row.id)">详情</el-button>
            <el-button v-if="row.status === 1" type="text" @click="accept(row)">受理</el-button>
            <el-button v-if="row.status === 2" type="text" @click="openDispatch(row)">派单</el-button>
            <el-button v-if="row.status === 2" type="text" class="auto" @click="autoDispatchRow(row)">
              智能派单
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <el-pagination
          background
          layout="total, prev, pager, next, sizes"
          :current-page.sync="query.page"
          :page-size.sync="query.pageSize"
          :page-sizes="[10, 20, 50]"
          :total="total"
          @current-change="load"
          @size-change="load"
        />
      </div>
    </div>

    <!-- 派单弹窗 -->
    <el-dialog title="派单" :visible.sync="dispatchVisible" width="720px" @closed="candidates = []">
      <div class="dispatch-hint" v-if="current">
        工单：<b>{{ current.orderNo }}</b>　{{ current.title }}
      </div>
      <el-table :data="candidates" size="small" border v-loading="candidateLoading" max-height="360">
        <el-table-column prop="staffName" label="工程师" width="100" />
        <el-table-column prop="skillTags" label="技能" min-width="140" />
        <el-table-column label="在手工单" width="100" align="center">
          <template slot-scope="{ row }">{{ row.activeCount }} / {{ row.workloadLimit }}</template>
        </el-table-column>
        <el-table-column label="技能匹配" width="90" align="center">
          <template slot-scope="{ row }">
            <el-tag :type="row.matched ? 'success' : 'info'" size="mini">
              {{ row.matched ? '匹配' : '不匹配' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="matchScore" label="匹配分" width="90" align="center" sortable />
        <el-table-column prop="reason" label="评分说明" min-width="220" show-overflow-tooltip />
        <el-table-column label="操作" width="80" fixed="right">
          <template slot-scope="{ row }">
            <el-button type="text" @click="doDispatch(row)">选择</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="candidate-tip">
        评分 = 技能匹配(60) + 负载余量(30) + 派单间隔(10)，分数越高越优先。
      </div>
    </el-dialog>

    <!-- 代报工单 -->
    <el-dialog title="代报工单" :visible.sync="createVisible" width="620px">
      <el-form ref="createForm" :model="createForm" :rules="createRules" label-width="96px">
        <el-form-item label="工单类型" prop="categoryId">
          <el-select v-model="createForm.categoryId" placeholder="请选择" style="width: 100%">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="房屋点位" prop="premiseId">
          <el-select v-model="createForm.premiseId" placeholder="请选择" filterable style="width: 100%">
            <el-option v-for="p in premises" :key="p.id" :label="p.fullAddress" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="住户">
          <el-select v-model="createForm.residentId" placeholder="可选" clearable style="width: 100%">
            <el-option v-for="r in residents" :key="r.id" :label="r.name + ' ' + r.phone" :value="r.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="标题" prop="title">
          <el-input v-model="createForm.title" placeholder="如：厨房水管漏水" />
        </el-form-item>
        <el-form-item label="问题描述">
          <el-input v-model="createForm.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="优先级">
          <el-radio-group v-model="createForm.priority">
            <el-radio :label="1">低</el-radio>
            <el-radio :label="2">普通</el-radio>
            <el-radio :label="3">高</el-radio>
            <el-radio :label="4">紧急</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="期望上门">
          <el-date-picker
            v-model="createForm.expectTime"
            type="datetime"
            value-format="yyyy-MM-dd'T'HH:mm:ss"
            placeholder="可选"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="服务项">
          <div class="svc-picker">
            <el-select v-model="svcToAdd" placeholder="选择服务项加入" filterable style="width: 70%">
              <el-option v-for="i in serviceItems" :key="i.id" :label="i.name + ' ¥' + i.price" :value="i.id" />
            </el-select>
            <el-button @click="addSvc" :disabled="!svcToAdd">加入</el-button>
          </div>
          <div class="svc-list" v-if="createForm.items.length">
            <el-tag
              v-for="(it, idx) in createForm.items"
              :key="idx"
              closable
              size="small"
              @close="createForm.items.splice(idx, 1)"
            >
              {{ it.name }} × {{ it.quantity }}（¥{{ it.price }}）
            </el-tag>
          </div>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" @click="submitCreate">提交</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import {
  pageWorkOrder, acceptWorkOrder, dispatchCandidates, dispatchWorkOrder,
  autoDispatch, createWorkOrder
} from '@/api/workOrder'
import { listCategory, listResident, listPremise } from '@/api/basic'
import { listServiceItem } from '@/api/service'

const STATUS_OPTIONS = [
  { label: '待受理', value: 1 },
  { label: '待派单', value: 2 },
  { label: '已派单', value: 3 },
  { label: '已接单', value: 4 },
  { label: '处理中', value: 5 },
  { label: '待验收', value: 6 },
  { label: '已完成', value: 7 },
  { label: '已驳回', value: 8 },
  { label: '已取消', value: 0 }
]

export default {
  name: 'WorkOrder',
  data() {
    return {
      loading: false,
      list: [],
      total: 0,
      query: { page: 1, pageSize: 10, orderNo: '', title: '', status: null, priority: null, categoryId: null, slaBreached: null },
      statusOptions: STATUS_OPTIONS,
      categories: [],
      residents: [],
      premises: [],
      serviceItems: [],

      dispatchVisible: false,
      candidateLoading: false,
      candidates: [],
      current: null,

      createVisible: false,
      svcToAdd: null,
      createForm: {
        categoryId: null, premiseId: null, residentId: null, title: '', description: '',
        priority: 2, expectTime: null, items: []
      },
      createRules: {
        categoryId: [{ required: true, message: '请选择工单类型', trigger: 'change' }],
        premiseId: [{ required: true, message: '请选择房屋点位', trigger: 'change' }],
        title: [{ required: true, message: '请输入标题', trigger: 'blur' }]
      }
    }
  },
  created() {
    this.loadMeta()
    this.load()
  },
  methods: {
    async loadMeta() {
      const [c, r, p] = await Promise.all([listCategory(), listResident(), listPremise()])
      this.categories = c.data || []
      this.residents = r.data || []
      this.premises = p.data || []
    },
    async load() {
      this.loading = true
      try {
        const res = await pageWorkOrder(this.query)
        this.list = res.data.records || []
        this.total = res.data.total || 0
      } finally {
        this.loading = false
      }
    },
    search() {
      this.query.page = 1
      this.load()
    },
    reset() {
      this.query = { page: 1, pageSize: 10, orderNo: '', title: '', status: null, priority: null, categoryId: null, slaBreached: null }
      this.load()
    },
    async accept(row) {
      await this.$confirm(`确认受理工单 ${row.orderNo}？`, '提示', { type: 'info' })
      await acceptWorkOrder(row.id)
      this.$message.success('已受理，请派单')
      this.load()
    },
    async openDispatch(row) {
      this.current = row
      this.dispatchVisible = true
      this.candidateLoading = true
      try {
        const res = await dispatchCandidates(row.id)
        this.candidates = res.data || []
      } finally {
        this.candidateLoading = false
      }
    },
    async doDispatch(candidate) {
      await dispatchWorkOrder(this.current.id, { orderId: this.current.id, staffId: candidate.staffId })
      this.$message.success('已派单给 ' + candidate.staffName)
      this.dispatchVisible = false
      this.load()
    },
    async autoDispatchRow(row) {
      await this.$confirm(`系统将按「技能 × 负载 × 间隔」自动选择工程师，确认？`, '智能派单', { type: 'info' })
      await autoDispatch(row.id)
      this.$message.success('智能派单成功')
      this.load()
    },
    openCreate() {
      this.createForm = {
        categoryId: null, premiseId: null, residentId: null, title: '', description: '',
        priority: 2, expectTime: null, items: []
      }
      this.svcToAdd = null
      this.createVisible = true
    },
    async onCategoryChange() {},
    async addSvc() {
      const item = this.serviceItems.find(i => i.id === this.svcToAdd)
      if (item) {
        this.createForm.items.push({ itemId: item.id, name: item.name, price: item.price, quantity: 1 })
      }
      this.svcToAdd = null
    },
    async submitCreate() {
      this.$refs.createForm.validate(async valid => {
        if (!valid) return
        await createWorkOrder({
          ...this.createForm,
          source: 2,
          items: this.createForm.items.map(i => ({ itemId: i.itemId, quantity: i.quantity }))
        })
        this.$message.success('工单创建成功')
        this.createVisible = false
        this.load()
      })
    },
    priorityType(p) {
      return { 4: 'danger', 3: 'warning', 2: '', 1: 'info' }[p] || ''
    },
    statusColor(s) {
      return {
        0: '#c0c4cc', 1: '#f5a524', 2: '#e6a23c', 3: '#3b6ef0',
        4: '#7c5cff', 5: '#12b5cb', 6: '#e6a23c', 7: '#22b07d', 8: '#f56c6c'
      }[s] || '#9099a8'
    },
    slaText(row) {
      if (row.slaStatus === 'OVERDUE') return '已超时'
      if (row.slaRemainMinutes === null || row.slaRemainMinutes === undefined) return '—'
      return row.slaRemainMinutes + ' 分钟'
    }
  },
  watch: {
    'createForm.categoryId'(val) {
      if (!val) return
      listServiceItem(val).then(res => {
        this.serviceItems = res.data || []
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.count { font-size: 12px; color: #a8b3c9; font-weight: 400; margin-left: 6px; }
.status-dot {
  display: inline-block;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  margin-right: 5px;
  vertical-align: middle;
}
.sla-normal { color: #22b07d; }
.sla-warning { color: #f5a524; }
.sla-overdue { color: #f56c6c; font-weight: 600; }
.auto { color: #7c5cff; }
.dispatch-hint {
  font-size: 13px;
  color: #6b7789;
  margin-bottom: 12px;
  background: #f7f9fc;
  padding: 10px 12px;
  border-radius: 6px;
}
.candidate-tip {
  margin-top: 10px;
  font-size: 12px;
  color: #a8b3c9;
}
.svc-picker { display: flex; gap: 8px; }
.svc-list { margin-top: 8px; display: flex; flex-wrap: wrap; gap: 8px; }
</style>
