<template>
  <div class="page-container">
    <div class="page-card">
      <div class="filter-bar">
        <el-input v-model="query.name" placeholder="姓名" clearable @keyup.enter.native="search" />
        <el-input v-model="query.phone" placeholder="手机号" clearable @keyup.enter.native="search" />
        <el-button type="primary" icon="el-icon-search" @click="search">查询</el-button>
        <el-button icon="el-icon-refresh" @click="reset">重置</el-button>
      </div>

      <div class="toolbar">
        <span class="title">住户管理 <span class="count">共 {{ total }} 户</span></span>
        <el-button type="primary" size="small" icon="el-icon-plus" @click="openEdit()">新增住户</el-button>
      </div>

      <el-table :data="list" size="small" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column prop="premiseCount" label="名下房屋" width="100" align="center" />
        <el-table-column label="状态" width="90">
          <template slot-scope="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="mini">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
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

    <el-dialog :title="form.id ? '编辑住户' : '新增住户'" :visible.sync="dialogVisible" width="440px">
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" maxlength="11" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">正常</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
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
import { pageResident, saveResident, updateResident } from '@/api/basic'

const emptyForm = () => ({ id: null, name: '', phone: '', status: 1 })

export default {
  name: 'Resident',
  data() {
    return {
      loading: false, list: [], total: 0,
      query: { page: 1, pageSize: 10, name: '', phone: '' },
      dialogVisible: false, form: emptyForm(),
      rules: {
        name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
        phone: [{ required: true, pattern: /^1\d{10}$/, message: '请输入正确手机号', trigger: 'blur' }]
      }
    }
  },
  created() { this.load() },
  methods: {
    async load() {
      this.loading = true
      try {
        const res = await pageResident(this.query)
        this.list = res.data.records || []
        this.total = res.data.total || 0
      } finally { this.loading = false }
    },
    search() { this.query.page = 1; this.load() },
    reset() { this.query = { page: 1, pageSize: 10, name: '', phone: '' }; this.load() },
    openEdit(row) {
      this.form = row ? { ...row } : emptyForm()
      this.dialogVisible = true
      this.$nextTick(() => this.$refs.form && this.$refs.form.clearValidate())
    },
    submit() {
      this.$refs.form.validate(async valid => {
        if (!valid) return
        if (this.form.id) await updateResident(this.form)
        else await saveResident(this.form)
        this.$message.success('保存成功')
        this.dialogVisible = false
        this.load()
      })
    }
  }
}
</script>

<style scoped>
.count { font-size: 12px; color: #a8b3c9; font-weight: 400; margin-left: 6px; }
</style>
