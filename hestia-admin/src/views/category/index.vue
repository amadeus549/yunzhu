<template>
  <div class="page-container">
    <div class="page-card">
      <div class="filter-bar">
        <el-input v-model="query.name" placeholder="类型名称" clearable @keyup.enter.native="search" />
        <el-select v-model="query.bizType" placeholder="业务类型" clearable>
          <el-option label="报修" :value="1" />
          <el-option label="投诉" :value="2" />
          <el-option label="咨询" :value="3" />
          <el-option label="巡检异常" :value="4" />
        </el-select>
        <el-button type="primary" icon="el-icon-search" @click="search">查询</el-button>
        <el-button icon="el-icon-refresh" @click="reset">重置</el-button>
      </div>

      <div class="toolbar">
        <span class="title">工单类型</span>
        <el-button type="primary" size="small" icon="el-icon-plus" @click="openEdit()">新增类型</el-button>
      </div>

      <el-table :data="list" size="small" border stripe v-loading="loading">
        <el-table-column prop="name" label="类型名称" min-width="160" />
        <el-table-column label="业务类型" width="120">
          <template slot-scope="{ row }">
            <el-tag size="mini" effect="plain">{{ bizName(row.bizType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="itemCount" label="关联服务项" width="110" align="center" />
        <el-table-column prop="sort" label="排序" width="80" align="center" />
        <el-table-column label="状态" width="90">
          <template slot-scope="{ row }">
            <el-switch :value="row.status === 1" @change="v => toggle(row, v)" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="90" fixed="right">
          <template slot-scope="{ row }">
            <el-button type="text" @click="openEdit(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <el-pagination background layout="total, prev, pager, next"
          :current-page.sync="query.page" :page-size="query.pageSize" :total="total" @current-change="load" />
      </div>
    </div>

    <el-dialog :title="form.id ? '编辑工单类型' : '新增工单类型'" :visible.sync="dialogVisible" width="460px">
      <el-form ref="form" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="类型名称" prop="name">
          <el-input v-model="form.name" placeholder="如 水电维修" />
        </el-form-item>
        <el-form-item label="业务类型" prop="bizType">
          <el-select v-model="form.bizType" style="width: 100%">
            <el-option label="报修" :value="1" />
            <el-option label="投诉" :value="2" />
            <el-option label="咨询" :value="3" />
            <el-option label="巡检异常" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" />
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
import { pageCategory, saveCategory, updateCategory, updateCategoryStatus } from '@/api/basic'

const emptyForm = () => ({ id: null, name: '', bizType: 1, sort: 0, status: 1 })

export default {
  name: 'Category',
  data() {
    return {
      loading: false, list: [], total: 0,
      query: { page: 1, pageSize: 10, name: '', bizType: null },
      dialogVisible: false, form: emptyForm(),
      rules: {
        name: [{ required: true, message: '请输入类型名称', trigger: 'blur' }],
        bizType: [{ required: true, message: '请选择业务类型', trigger: 'change' }]
      }
    }
  },
  created() { this.load() },
  methods: {
    async load() {
      this.loading = true
      try {
        const res = await pageCategory(this.query)
        this.list = res.data.records || []
        this.total = res.data.total || 0
      } finally { this.loading = false }
    },
    search() { this.query.page = 1; this.load() },
    reset() { this.query = { page: 1, pageSize: 10, name: '', bizType: null }; this.load() },
    openEdit(row) {
      this.form = row ? { ...row } : emptyForm()
      this.dialogVisible = true
      this.$nextTick(() => this.$refs.form && this.$refs.form.clearValidate())
    },
    submit() {
      this.$refs.form.validate(async valid => {
        if (!valid) return
        if (this.form.id) await updateCategory(this.form)
        else await saveCategory(this.form)
        this.$message.success('保存成功')
        this.dialogVisible = false
        this.load()
      })
    },
    async toggle(row, val) {
      await updateCategoryStatus(row.id, val ? 1 : 0)
      row.status = val ? 1 : 0
    },
    bizName(t) { return { 1: '报修', 2: '投诉', 3: '咨询', 4: '巡检异常' }[t] || '—' }
  }
}
</script>
