<template>
  <div class="page-container">
    <div class="page-card">
      <div class="filter-bar">
        <el-input v-model="query.name" placeholder="计划名称" clearable @keyup.enter.native="search" />
        <el-select v-model="query.status" placeholder="状态" clearable>
          <el-option label="启用" :value="1" />
          <el-option label="停用" :value="0" />
        </el-select>
        <el-button type="primary" icon="el-icon-search" @click="search">查询</el-button>
        <el-button icon="el-icon-refresh" @click="reset">重置</el-button>
      </div>

      <div class="toolbar">
        <span class="title">巡检计划</span>
        <el-button type="primary" size="small" icon="el-icon-plus" @click="openEdit()">新增计划</el-button>
      </div>

      <el-table :data="list" size="small" border stripe v-loading="loading">
        <el-table-column prop="name" label="计划名称" min-width="150" />
        <el-table-column prop="cycleDesc" label="巡检周期" width="120" />
        <el-table-column prop="deviceCategory" label="设备范围" width="120">
          <template slot-scope="{ row }">{{ row.deviceCategory || '全部设备' }}</template>
        </el-table-column>
        <el-table-column prop="executorName" label="执行人" width="100" />
        <el-table-column prop="startDate" label="生效日期" width="120" />
        <el-table-column prop="nextGenerateDate" label="下次生成" width="120" />
        <el-table-column label="状态" width="80">
          <template slot-scope="{ row }">
            <el-switch
              :value="row.status === 1"
              @change="v => toggle(row, v)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template slot-scope="{ row }">
            <el-button type="text" @click="openEdit(row)">编辑</el-button>
            <el-button type="text" @click="generate(row)">立即生成</el-button>
            <el-button type="text" @click="$router.push('/inspection/task')">查看任务</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <el-pagination
          background
          layout="total, prev, pager, next"
          :current-page.sync="query.page"
          :page-size="query.pageSize"
          :total="total"
          @current-change="load"
        />
      </div>
    </div>

    <el-dialog :title="form.id ? '编辑巡检计划' : '新增巡检计划'" :visible.sync="dialogVisible" width="560px">
      <el-form ref="form" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="计划名称" prop="name">
          <el-input v-model="form.name" placeholder="如 电梯月度巡检" />
        </el-form-item>
        <el-form-item label="周期类型" prop="cycleType">
          <el-radio-group v-model="form.cycleType">
            <el-radio :label="1">每日</el-radio>
            <el-radio :label="2">每周</el-radio>
            <el-radio :label="3">每月</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="周期值" v-if="form.cycleType !== 1">
          <el-input
            v-model="form.cycleValue"
            :placeholder="form.cycleType === 2 ? '如 MON,WED,FRI' : '如 1,15'"
          />
          <div class="hint">
            每周填 MON/TUE/WED/THU/FRI/SAT/SUN，逗号分隔；每月填 1-28 的日期，逗号分隔。
          </div>
        </el-form-item>
        <el-form-item label="设备范围">
          <el-select v-model="deviceCats" multiple allow-create filterable style="width: 100%">
            <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
          </el-select>
          <div class="hint">不选表示全部在用设备。</div>
        </el-form-item>
        <el-form-item label="执行人" prop="executorId">
          <el-select v-model="form.executorId" filterable style="width: 100%">
            <el-option v-for="e in engineers" :key="e.id" :label="e.name" :value="e.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="生效日期" prop="startDate">
          <el-date-picker v-model="form.startDate" type="date" value-format="yyyy-MM-dd" style="width: 100%" />
        </el-form-item>
        <el-form-item label="提前生成天数">
          <el-input-number v-model="form.generateAheadDays" :min="0" :max="30" />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">保存</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { pagePlan, savePlan, updatePlan, updatePlanStatus, generateTasks } from '@/api/inspection'
import { listEngineers } from '@/api/basic'
import { deviceCategories } from '@/api/device'

const emptyForm = () => ({
  id: null, name: '', cycleType: 3, cycleValue: '1',
  deviceCategory: '', executorId: null, startDate: null, generateAheadDays: 3, status: 1
})

export default {
  name: 'InspectionPlan',
  data() {
    return {
      loading: false,
      list: [],
      total: 0,
      query: { page: 1, pageSize: 10, name: '', status: null },
      engineers: [],
      categories: [],
      deviceCats: [],
      dialogVisible: false,
      form: emptyForm(),
      rules: {
        name: [{ required: true, message: '请输入计划名称', trigger: 'blur' }],
        cycleType: [{ required: true, message: '请选择周期类型', trigger: 'change' }],
        executorId: [{ required: true, message: '请选择执行人', trigger: 'change' }],
        startDate: [{ required: true, message: '请选择生效日期', trigger: 'change' }]
      }
    }
  },
  created() {
    this.loadMeta()
    this.load()
  },
  methods: {
    async loadMeta() {
      const [e, c] = await Promise.all([listEngineers({}), deviceCategories()])
      this.engineers = e.data || []
      this.categories = c.data || []
    },
    async load() {
      this.loading = true
      try {
        const res = await pagePlan(this.query)
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
      this.query = { page: 1, pageSize: 10, name: '', status: null }
      this.load()
    },
    openEdit(row) {
      if (row) {
        this.form = { ...row }
        this.deviceCats = row.deviceCategory ? row.deviceCategory.split(',') : []
      } else {
        this.form = emptyForm()
        this.deviceCats = []
      }
      this.dialogVisible = true
      this.$nextTick(() => this.$refs.form && this.$refs.form.clearValidate())
    },
    async submit() {
      this.$refs.form.validate(async valid => {
        if (!valid) return
        this.form.deviceCategory = this.deviceCats.join(',')
        if (this.form.id) {
          await updatePlan(this.form)
        } else {
          await savePlan(this.form)
        }
        this.$message.success('保存成功')
        this.dialogVisible = false
        this.load()
      })
    },
    async toggle(row, val) {
      await updatePlanStatus(row.id, val ? 1 : 0)
      row.status = val ? 1 : 0
      this.$message.success(val ? '已启用' : '已停用')
    },
    async generate(row) {
      await this.$confirm(`立即按「${row.name}」生成巡检任务？重复执行不会产生重复任务。`, '生成任务', {
        type: 'info'
      })
      const res = await generateTasks(row.id)
      if (res.data > 0) {
        this.$message.success(`已生成 ${res.data} 条巡检任务`)
      } else {
        this.$message.info(`未到生成时间（下次生成：${row.nextGenerateDate}），本次未生成任务`)
      }
      this.load()
    }
  }
}
</script>

<style lang="scss" scoped>
.hint { font-size: 11.5px; color: #a8b3c9; margin-top: 4px; line-height: 1.6; }
</style>
