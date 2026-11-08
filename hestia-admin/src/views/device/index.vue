<template>
  <div class="page-container">
    <el-alert
      v-if="dueCount > 0"
      :title="`有 ${dueCount} 台设备已到维保日期，建议尽快安排巡检或维保`"
      type="warning"
      show-icon
      :closable="false"
      class="mb12"
    />

    <div class="page-card">
      <div class="filter-bar">
        <el-input v-model="query.deviceNo" placeholder="设备编号" clearable @keyup.enter.native="search" />
        <el-input v-model="query.name" placeholder="设备名称" clearable @keyup.enter.native="search" />
        <el-select v-model="query.category" placeholder="设备类别" clearable>
          <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
        </el-select>
        <el-select v-model="query.status" placeholder="状态" clearable>
          <el-option label="正常" :value="1" />
          <el-option label="维修中" :value="2" />
          <el-option label="停用" :value="3" />
          <el-option label="报废" :value="4" />
        </el-select>
        <el-select v-model="query.maintainDue" placeholder="维保" clearable>
          <el-option label="待维保" :value="1" />
        </el-select>
        <el-button type="primary" icon="el-icon-search" @click="search">查询</el-button>
        <el-button icon="el-icon-refresh" @click="reset">重置</el-button>
      </div>

      <div class="toolbar">
        <span class="title">设备台账 <span class="count">共 {{ total }} 台</span></span>
        <el-button type="primary" size="small" icon="el-icon-plus" @click="openEdit()">新增设备</el-button>
      </div>

      <el-table :data="list" size="small" border stripe v-loading="loading">
        <el-table-column prop="deviceNo" label="设备编号" width="120" />
        <el-table-column prop="name" label="设备名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="category" label="类别" width="80" />
        <el-table-column label="品牌型号" min-width="130">
          <template slot-scope="{ row }">{{ row.brand || '—' }} {{ row.model || '' }}</template>
        </el-table-column>
        <el-table-column prop="location" label="安装位置" min-width="140" show-overflow-tooltip />
        <el-table-column label="状态" width="90">
          <template slot-scope="{ row }">
            <el-tag :type="statusType(row.status)" size="mini">{{ row.statusName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="质保" width="120">
          <template slot-scope="{ row }">
            <span :class="{ expired: row.warrantyExpired }">{{ row.warrantyEnd || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="下次维保" width="130">
          <template slot-scope="{ row }">
            <span :class="{ 'due': row.maintainDue }">{{ row.nextMaintainDate || '—' }}</span>
            <el-tag v-if="row.maintainDue" size="mini" type="warning" class="ml4">待维保</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template slot-scope="{ row }">
            <el-button type="text" @click="openEdit(row)">编辑</el-button>
            <el-button type="text" class="danger" @click="remove(row)">删除</el-button>
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

    <el-dialog :title="form.id ? '编辑设备' : '新增设备'" :visible.sync="dialogVisible" width="620px">
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="设备编号" prop="deviceNo">
              <el-input v-model="form.deviceNo" placeholder="如 DV-LIFT-004" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="设备名称" prop="name">
              <el-input v-model="form.name" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="类别" prop="category">
              <el-select v-model="form.category" allow-create filterable style="width: 100%">
                <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-select v-model="form.status" style="width: 100%">
                <el-option label="正常" :value="1" />
                <el-option label="维修中" :value="2" />
                <el-option label="停用" :value="3" />
                <el-option label="报废" :value="4" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="品牌">
              <el-input v-model="form.brand" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="型号">
              <el-input v-model="form.model" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="安装点位">
              <el-select v-model="form.premiseId" clearable filterable style="width: 100%">
                <el-option v-for="p in premises" :key="p.id" :label="p.fullAddress" :value="p.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="位置描述">
              <el-input v-model="form.location" placeholder="如 1栋1单元电梯井" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="安装日期">
              <el-date-picker v-model="form.installDate" type="date" value-format="yyyy-MM-dd" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="质保到期">
              <el-date-picker v-model="form.warrantyEnd" type="date" value-format="yyyy-MM-dd" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="维保周期">
              <el-input-number v-model="form.maintainCycleDays" :min="1" :max="3650" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注">
              <el-input v-model="form.remark" type="textarea" :rows="2" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <span slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">保存</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import {
  pageDevice, saveDevice, updateDevice, deleteDevice, deviceCategories, maintainDueCount
} from '@/api/device'
import { listPremise } from '@/api/basic'

const emptyForm = () => ({
  id: null, deviceNo: '', name: '', category: '', brand: '', model: '',
  premiseId: null, location: '', installDate: null, warrantyEnd: null,
  maintainCycleDays: 90, status: 1, remark: ''
})

export default {
  name: 'Device',
  data() {
    return {
      loading: false,
      list: [],
      total: 0,
      dueCount: 0,
      categories: [],
      premises: [],
      query: { page: 1, pageSize: 10, deviceNo: '', name: '', category: null, status: null, maintainDue: null },
      dialogVisible: false,
      form: emptyForm(),
      rules: {
        deviceNo: [{ required: true, message: '请输入设备编号', trigger: 'blur' }],
        name: [{ required: true, message: '请输入设备名称', trigger: 'blur' }],
        category: [{ required: true, message: '请选择类别', trigger: 'change' }]
      }
    }
  },
  created() {
    this.loadMeta()
    this.load()
  },
  methods: {
    async loadMeta() {
      const [c, p, d] = await Promise.all([deviceCategories(), listPremise(), maintainDueCount()])
      this.categories = c.data || []
      this.premises = p.data || []
      this.dueCount = d.data || 0
    },
    async load() {
      this.loading = true
      try {
        const res = await pageDevice(this.query)
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
      this.query = { page: 1, pageSize: 10, deviceNo: '', name: '', category: null, status: null, maintainDue: null }
      this.load()
    },
    openEdit(row) {
      this.form = row ? { ...row } : emptyForm()
      this.dialogVisible = true
      this.$nextTick(() => this.$refs.form && this.$refs.form.clearValidate())
    },
    submit() {
      this.$refs.form.validate(async valid => {
        if (!valid) return
        if (this.form.id) {
          await updateDevice(this.form)
        } else {
          await saveDevice(this.form)
        }
        this.$message.success('保存成功')
        this.dialogVisible = false
        this.load()
        this.loadMeta()
      })
    },
    async remove(row) {
      await this.$confirm(`确认删除设备「${row.name}」？`, '提示', { type: 'warning' })
      await deleteDevice(row.id)
      this.$message.success('已删除')
      this.load()
    },
    statusType(s) {
      return { 1: 'success', 2: 'warning', 3: 'info', 4: 'danger' }[s] || ''
    }
  }
}
</script>

<style lang="scss" scoped>
.mb12 { margin-bottom: 12px; }
.ml4 { margin-left: 4px; }
.count { font-size: 12px; color: #a8b3c9; font-weight: 400; margin-left: 6px; }
.expired { color: #f56c6c; }
.due { color: #f5a524; font-weight: 600; }
.danger { color: #f56c6c; }
</style>
