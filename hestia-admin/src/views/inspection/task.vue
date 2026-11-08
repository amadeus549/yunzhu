<template>
  <div class="page-container">
    <div class="page-card">
      <div class="filter-bar">
        <el-select v-model="query.status" placeholder="任务状态" clearable>
          <el-option label="待执行" :value="1" />
          <el-option label="已完成" :value="2" />
          <el-option label="已逾期" :value="3" />
        </el-select>
        <el-select v-model="query.result" placeholder="巡检结果" clearable>
          <el-option label="正常" :value="1" />
          <el-option label="异常" :value="2" />
          <el-option label="未执行" :value="3" />
        </el-select>
        <el-select v-model="query.executorId" placeholder="执行人" clearable>
          <el-option v-for="e in engineers" :key="e.id" :label="e.name" :value="e.id" />
        </el-select>
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          value-format="yyyy-MM-dd"
          start-placeholder="计划开始"
          end-placeholder="计划结束"
          style="width: 240px"
        />
        <el-button type="primary" icon="el-icon-search" @click="search">查询</el-button>
        <el-button icon="el-icon-refresh" @click="reset">重置</el-button>
      </div>

      <div class="toolbar">
        <span class="title">巡检任务 <span class="count">共 {{ total }} 条</span></span>
      </div>

      <el-table :data="list" size="small" border stripe v-loading="loading">
        <el-table-column prop="taskNo" label="任务号" width="130" />
        <el-table-column prop="planName" label="所属计划" min-width="130" />
        <el-table-column prop="deviceName" label="巡检设备" min-width="130" show-overflow-tooltip />
        <el-table-column prop="executorName" label="执行人" width="90" />
        <el-table-column prop="planDate" label="计划日期" width="110" />
        <el-table-column prop="actualTime" label="执行时间" width="150">
          <template slot-scope="{ row }">{{ row.actualTime || '—' }}</template>
        </el-table-column>
        <el-table-column label="结果" width="90">
          <template slot-scope="{ row }">
            <el-tag v-if="row.result" :type="resultType(row.result)" size="mini">{{ row.resultName }}</el-tag>
            <span v-else>—</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template slot-scope="{ row }">
            <el-tag :type="statusType(row.status)" size="mini" effect="plain">{{ row.statusName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="abnormalDesc" label="异常说明" min-width="160" show-overflow-tooltip>
          <template slot-scope="{ row }">{{ row.abnormalDesc || '—' }}</template>
        </el-table-column>
        <el-table-column label="转工单" width="90">
          <template slot-scope="{ row }">
            <el-button v-if="row.orderId" type="text" @click="$router.push('/work-order/' + row.orderId)">
              #{{ row.orderId }}
            </el-button>
            <span v-else>—</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="90" fixed="right">
          <template slot-scope="{ row }">
            <el-button v-if="row.status !== 2" type="text" @click="openExecute(row)">执行</el-button>
            <span v-else class="done">已完成</span>
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

    <!-- 执行巡检 -->
    <el-dialog title="执行巡检任务" :visible.sync="execVisible" width="520px">
      <el-form :model="execForm" label-width="90px">
        <el-form-item label="巡检设备">
          <span class="strong">{{ currentTask && currentTask.deviceName }}</span>
        </el-form-item>
        <el-form-item label="巡检结果">
          <el-radio-group v-model="execForm.result">
            <el-radio :label="1">正常</el-radio>
            <el-radio :label="2">发现异常</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="异常说明" v-if="execForm.result === 2">
          <el-input v-model="execForm.abnormalDesc" type="textarea" :rows="3" placeholder="描述异常现象" />
        </el-form-item>
        <el-form-item label="转工单" v-if="execForm.result === 2">
          <el-switch v-model="execForm.createOrder" :active-value="1" :inactive-value="0" />
          <span class="hint">开启后将自动创建一张高优先级报修工单，并把设备置为维修中</span>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="execVisible = false">取消</el-button>
        <el-button type="primary" @click="submitExecute">提交</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { pageTask, executeTask } from '@/api/inspection'
import { listEngineers } from '@/api/basic'

export default {
  name: 'InspectionTask',
  data() {
    return {
      loading: false,
      list: [],
      total: 0,
      dateRange: [],
      engineers: [],
      query: { page: 1, pageSize: 10, status: null, result: null, executorId: null, beginDate: null, endDate: null },
      execVisible: false,
      currentTask: null,
      execForm: { taskId: null, result: 1, abnormalDesc: '', createOrder: 1 }
    }
  },
  created() {
    this.loadMeta()
    this.load()
  },
  methods: {
    async loadMeta() {
      const res = await listEngineers({})
      this.engineers = res.data || []
    },
    async load() {
      this.loading = true
      try {
        if (this.dateRange && this.dateRange.length === 2) {
          this.query.beginDate = this.dateRange[0]
          this.query.endDate = this.dateRange[1]
        } else {
          this.query.beginDate = null
          this.query.endDate = null
        }
        const res = await pageTask(this.query)
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
      this.dateRange = []
      this.query = { page: 1, pageSize: 10, status: null, result: null, executorId: null, beginDate: null, endDate: null }
      this.load()
    },
    openExecute(row) {
      this.currentTask = row
      this.execForm = { taskId: row.id, result: 1, abnormalDesc: row.abnormalDesc || '', createOrder: 1 }
      this.execVisible = true
    },
    async submitExecute() {
      if (this.execForm.result === 2 && !this.execForm.abnormalDesc) {
        this.$message.warning('请填写异常说明')
        return
      }
      const res = await executeTask(this.execForm)
      this.execVisible = false
      if (res.data) {
        this.$message.success('巡检完成，已生成工单 #' + res.data)
      } else {
        this.$message.success('巡检完成')
      }
      this.load()
    },
    resultType(r) {
      return { 1: 'success', 2: 'danger', 3: 'info' }[r] || ''
    },
    statusType(s) {
      return { 1: 'warning', 2: 'success', 3: 'danger' }[s] || ''
    }
  }
}
</script>

<style lang="scss" scoped>
.count { font-size: 12px; color: #a8b3c9; font-weight: 400; margin-left: 6px; }
.strong { font-weight: 600; color: #1f2733; }
.hint { font-size: 11.5px; color: #a8b3c9; margin-left: 8px; }
.done { font-size: 12px; color: #a8b3c9; }
</style>
