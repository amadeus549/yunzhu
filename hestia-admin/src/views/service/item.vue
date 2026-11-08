<template>
  <div class="page-container">
    <div class="page-card">
      <div class="filter-bar">
        <el-input v-model="query.name" placeholder="服务项名称" clearable @keyup.enter.native="search" />
        <el-select v-model="query.categoryId" placeholder="工单类型" clearable>
          <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
        </el-select>
        <el-select v-model="query.status" placeholder="状态" clearable>
          <el-option label="上架" :value="1" />
          <el-option label="下架" :value="0" />
        </el-select>
        <el-button type="primary" icon="el-icon-search" @click="search">查询</el-button>
        <el-button icon="el-icon-refresh" @click="reset">重置</el-button>
      </div>

      <div class="toolbar">
        <span class="title">服务项 <span class="count">共 {{ total }} 项</span></span>
        <el-button type="primary" size="small" icon="el-icon-plus" @click="openEdit()">新增服务项</el-button>
      </div>

      <el-table :data="list" size="small" border stripe v-loading="loading">
        <el-table-column prop="code" label="编码" width="120" />
        <el-table-column prop="name" label="服务名称" min-width="150" />
        <el-table-column prop="categoryName" label="工单类型" width="100" />
        <el-table-column label="价格" width="110">
          <template slot-scope="{ row }">¥{{ row.price }} / {{ row.unit }}</template>
        </el-table-column>
        <el-table-column prop="durationMin" label="耗时(分钟)" width="100" align="center" />
        <el-table-column prop="requiredSkill" label="所需技能" width="100">
          <template slot-scope="{ row }">
            <el-tag v-if="row.requiredSkill" size="mini" effect="plain">{{ row.requiredSkill }}</el-tag>
            <span v-else>—</span>
          </template>
        </el-table-column>
        <el-table-column label="规格" min-width="160">
          <template slot-scope="{ row }">
            <el-tag v-for="s in row.specs" :key="s.id" size="mini" type="info" class="spec-tag">
              {{ s.name }} ¥{{ s.price }}
            </el-tag>
            <span v-if="!row.specs || !row.specs.length">—</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template slot-scope="{ row }">
            <el-switch :value="row.status === 1" @change="v => toggle(row, v)" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="110" fixed="right">
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

    <el-dialog :title="form.id ? '编辑服务项' : '新增服务项'" :visible.sync="dialogVisible" width="640px">
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="服务编码" prop="code">
              <el-input v-model="form.code" placeholder="如 SV-WATER-02" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="服务名称" prop="name">
              <el-input v-model="form.name" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="工单类型" prop="categoryId">
              <el-select v-model="form.categoryId" style="width: 100%">
                <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所需技能">
              <el-input v-model="form.requiredSkill" placeholder="如 水电" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="基础价格" prop="price">
              <el-input-number v-model="form.price" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="计价单位">
              <el-input v-model="form.unit" placeholder="次/㎡/小时" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="耗时(分钟)">
              <el-input-number v-model="form.durationMin" :min="1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="服务说明">
              <el-input v-model="form.description" type="textarea" :rows="2" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="排序">
              <el-input-number v-model="form.sort" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="规格档位">
          <div class="spec-rows">
            <div v-for="(s, i) in form.specs" :key="i" class="spec-row">
              <el-input v-model="s.name" placeholder="规格名，如 1.5匹挂机" style="width: 200px" />
              <el-input-number v-model="s.price" :min="0" :precision="2" placeholder="价格" style="width: 120px" />
              <el-input-number v-model="s.durationMin" :min="1" placeholder="耗时" style="width: 110px" />
              <el-button type="text" class="danger" @click="form.specs.splice(i, 1)">删除</el-button>
            </div>
            <el-button type="dashed" size="small" icon="el-icon-plus" @click="addSpec">添加规格</el-button>
          </div>
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
import {
  pageServiceItem, getServiceItem, saveServiceItem, updateServiceItem,
  deleteServiceItem, updateServiceItemStatus
} from '@/api/service'
import { listCategory } from '@/api/basic'

const emptyForm = () => ({
  id: null, categoryId: null, code: '', name: '', description: '',
  price: 0, unit: '次', durationMin: 60, requiredSkill: '', status: 1, sort: 0, specs: []
})

export default {
  name: 'ServiceItem',
  data() {
    return {
      loading: false,
      list: [],
      total: 0,
      categories: [],
      query: { page: 1, pageSize: 10, name: '', categoryId: null, status: null },
      dialogVisible: false,
      form: emptyForm(),
      rules: {
        code: [{ required: true, message: '请输入服务编码', trigger: 'blur' }],
        name: [{ required: true, message: '请输入服务名称', trigger: 'blur' }],
        categoryId: [{ required: true, message: '请选择工单类型', trigger: 'change' }],
        price: [{ required: true, message: '请输入价格', trigger: 'blur' }]
      }
    }
  },
  created() {
    listCategory().then(res => (this.categories = res.data || []))
    this.load()
  },
  methods: {
    async load() {
      this.loading = true
      try {
        const res = await pageServiceItem(this.query)
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
      this.query = { page: 1, pageSize: 10, name: '', categoryId: null, status: null }
      this.load()
    },
    async openEdit(row) {
      if (row) {
        const res = await getServiceItem(row.id)
        this.form = { ...res.data, specs: (res.data.specs || []).map(s => ({ ...s })) }
      } else {
        this.form = emptyForm()
      }
      this.dialogVisible = true
      this.$nextTick(() => this.$refs.form && this.$refs.form.clearValidate())
    },
    addSpec() {
      this.form.specs.push({ name: '', price: 0, durationMin: 60 })
    },
    submit() {
      this.$refs.form.validate(async valid => {
        if (!valid) return
        if (this.form.id) {
          await updateServiceItem(this.form)
        } else {
          await saveServiceItem(this.form)
        }
        this.$message.success('保存成功')
        this.dialogVisible = false
        this.load()
      })
    },
    async toggle(row, val) {
      await updateServiceItemStatus(row.id, val ? 1 : 0)
      row.status = val ? 1 : 0
      this.$message.success(val ? '已上架' : '已下架')
    },
    async remove(row) {
      await this.$confirm(`确认删除服务项「${row.name}」？`, '提示', { type: 'warning' })
      await deleteServiceItem(row.id)
      this.$message.success('已删除')
      this.load()
    }
  }
}
</script>

<style lang="scss" scoped>
.count { font-size: 12px; color: #a8b3c9; font-weight: 400; margin-left: 6px; }
.spec-tag { margin-right: 4px; }
.danger { color: #f56c6c; }
.spec-rows { width: 100%; }
.spec-row {
  display: flex;
  gap: 8px;
  align-items: center;
  margin-bottom: 8px;
}
</style>
