<template>
  <div class="page-container">
    <div class="page-card">
      <div class="filter-bar">
        <el-input v-model="query.name" placeholder="姓名" clearable @keyup.enter.native="search" />
        <el-select v-model="query.role" placeholder="角色" clearable>
          <el-option label="管家" :value="1" />
          <el-option label="工程师" :value="2" />
          <el-option label="主管" :value="3" />
          <el-option label="客服" :value="4" />
        </el-select>
        <el-select v-model="query.status" placeholder="状态" clearable>
          <el-option label="启用" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>
        <el-button type="primary" icon="el-icon-search" @click="search">查询</el-button>
        <el-button icon="el-icon-refresh" @click="reset">重置</el-button>
      </div>

      <div class="toolbar">
        <span class="title">员工管理 <span class="count">共 {{ total }} 人</span></span>
        <el-button type="primary" size="small" icon="el-icon-plus" @click="openEdit()">新增员工</el-button>
      </div>

      <el-table :data="list" size="small" border stripe v-loading="loading">
        <el-table-column prop="staffNo" label="工号" width="90" />
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column prop="username" label="登录账号" width="110" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column label="角色" width="90">
          <template slot-scope="{ row }">
            <el-tag size="mini" :type="roleType(row.role)" effect="plain">{{ row.roleName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="技能标签" min-width="150">
          <template slot-scope="{ row }">
            <el-tag v-for="s in (row.skillTags || '').split(',').filter(Boolean)" :key="s" size="mini" class="tag">
              {{ s }}
            </el-tag>
            <span v-if="!row.skillTags">—</span>
          </template>
        </el-table-column>
        <el-table-column label="在手工单" width="110" align="center">
          <template slot-scope="{ row }">
            <span :class="{ full: row.activeCount >= row.workloadLimit }">
              {{ row.activeCount }} / {{ row.workloadLimit }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
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

    <el-dialog :title="form.id ? '编辑员工' : '新增员工'" :visible.sync="dialogVisible" width="560px">
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="工号">
              <el-input v-model="form.staffNo" placeholder="留空自动生成" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="姓名" prop="name">
              <el-input v-model="form.name" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="登录账号" prop="username">
              <el-input v-model="form.username" :disabled="!!form.id" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="密码" :prop="form.id ? '' : 'password'">
              <el-input v-model="form.password" show-password :placeholder="form.id ? '留空不修改' : '默认 123456'" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号">
              <el-input v-model="form.phone" maxlength="11" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="角色" prop="role">
              <el-select v-model="form.role" style="width: 100%">
                <el-option label="管家" :value="1" />
                <el-option label="工程师" :value="2" />
                <el-option label="主管" :value="3" />
                <el-option label="客服" :value="4" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12" v-if="form.role === 2">
            <el-form-item label="技能标签">
              <el-input v-model="form.skillTags" placeholder="逗号分隔，如 水电,空调" />
            </el-form-item>
          </el-col>
          <el-col :span="12" v-if="form.role === 2">
            <el-form-item label="在手上限">
              <el-input-number v-model="form.workloadLimit" :min="1" :max="30" style="width: 100%" />
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
import { pageStaff, saveStaff, updateStaff, updateStaffStatus } from '@/api/basic'

const emptyForm = () => ({
  id: null, staffNo: '', name: '', username: '', password: '', phone: '',
  role: 2, skillTags: '', workloadLimit: 6, status: 1
})

export default {
  name: 'Staff',
  data() {
    return {
      loading: false,
      list: [],
      total: 0,
      query: { page: 1, pageSize: 10, name: '', role: null, status: null },
      dialogVisible: false,
      form: emptyForm(),
      rules: {
        name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
        username: [{ required: true, message: '请输入登录账号', trigger: 'blur' }],
        password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
        role: [{ required: true, message: '请选择角色', trigger: 'change' }]
      }
    }
  },
  created() {
    this.load()
  },
  methods: {
    async load() {
      this.loading = true
      try {
        const res = await pageStaff(this.query)
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
      this.query = { page: 1, pageSize: 10, name: '', role: null, status: null }
      this.load()
    },
    openEdit(row) {
      this.form = row ? { ...row, password: '' } : emptyForm()
      this.dialogVisible = true
      this.$nextTick(() => this.$refs.form && this.$refs.form.clearValidate())
    },
    submit() {
      this.$refs.form.validate(async valid => {
        if (!valid) return
        if (this.form.id) {
          await updateStaff(this.form)
        } else {
          await saveStaff(this.form)
        }
        this.$message.success('保存成功')
        this.dialogVisible = false
        this.load()
      })
    },
    async toggle(row, val) {
      await updateStaffStatus(row.id, val ? 1 : 0)
      row.status = val ? 1 : 0
      this.$message.success(val ? '已启用' : '已禁用')
    },
    roleType(r) {
      return { 1: '', 2: 'success', 3: 'danger', 4: 'info' }[r] || ''
    }
  }
}
</script>

<style lang="scss" scoped>
.count { font-size: 12px; color: #a8b3c9; font-weight: 400; margin-left: 6px; }
.tag { margin-right: 4px; }
.full { color: #f56c6c; font-weight: 600; }
</style>
