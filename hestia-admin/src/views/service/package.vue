<template>
  <div class="page-container">
    <div class="page-card">
      <div class="filter-bar">
        <el-input v-model="query.name" placeholder="套餐名称" clearable @keyup.enter.native="search" />
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
        <span class="title">服务包 <span class="count">共 {{ total }} 个</span></span>
        <el-button type="primary" size="small" icon="el-icon-plus" @click="openEdit()">新增服务包</el-button>
      </div>

      <el-table :data="list" size="small" border stripe v-loading="loading">
        <el-table-column prop="name" label="套餐名称" min-width="160" />
        <el-table-column prop="description" label="说明" min-width="200" show-overflow-tooltip />
        <el-table-column label="价格" width="150">
          <template slot-scope="{ row }">
            <span class="price">¥{{ row.price }}</span>
            <span v-if="row.originalPrice" class="origin">¥{{ row.originalPrice }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="validDays" label="有效期(天)" width="100" align="center" />
        <el-table-column label="包含服务项" width="110" align="center">
          <template slot-scope="{ row }">{{ (row.items || []).length }} 项</template>
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
          layout="total, prev, pager, next"
          :current-page.sync="query.page"
          :page-size="query.pageSize"
          :total="total"
          @current-change="load"
        />
      </div>
    </div>

    <el-dialog :title="form.id ? '编辑服务包' : '新增服务包'" :visible.sync="dialogVisible" width="680px">
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="套餐编码" prop="code">
              <el-input v-model="form.code" placeholder="如 PKG-HOME-02" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="套餐名称" prop="name">
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
            <el-form-item label="有效期(天)">
              <el-input-number v-model="form.validDays" :min="1" :max="3650" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="套餐价" prop="price">
              <el-input-number v-model="form.price" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="原价">
              <el-input-number v-model="form.originalPrice" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="套餐说明">
              <el-input v-model="form.description" type="textarea" :rows="2" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="包含服务项">
          <div class="pkg-picker">
            <el-select v-model="itemToAdd" placeholder="选择服务项" filterable style="width: 320px">
              <el-option v-for="i in allItems" :key="i.id" :label="i.name" :value="i.id" />
            </el-select>
            <el-button @click="addItem" :disabled="!itemToAdd">加入</el-button>
          </div>
          <el-table :data="form.items" size="mini" border class="pkg-table">
            <el-table-column label="服务项" min-width="180">
              <template slot-scope="{ row }">{{ itemName(row.itemId) }}</template>
            </el-table-column>
            <el-table-column label="单次数量" width="120">
              <template slot-scope="{ row }">
                <el-input-number v-model="row.quantity" :min="1" size="mini" />
              </template>
            </el-table-column>
            <el-table-column label="包含次数" width="120">
              <template slot-scope="{ row }">
                <el-input-number v-model="row.times" :min="1" size="mini" />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="70">
              <template slot-scope="{ $index }">
                <el-button type="text" class="danger" @click="form.items.splice($index, 1)">移除</el-button>
              </template>
            </el-table-column>
          </el-table>
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
  pageServicePackage, getServicePackage, saveServicePackage, updateServicePackage,
  deleteServicePackage, updateServicePackageStatus, pageServiceItem
} from '@/api/service'
import { listCategory } from '@/api/basic'

const emptyForm = () => ({
  id: null, categoryId: null, code: '', name: '', description: '',
  price: 0, originalPrice: 0, validDays: 365, status: 1, items: []
})

export default {
  name: 'ServicePackage',
  data() {
    return {
      loading: false,
      list: [],
      total: 0,
      categories: [],
      allItems: [],
      itemToAdd: null,
      query: { page: 1, pageSize: 10, name: '', categoryId: null, status: null },
      dialogVisible: false,
      form: emptyForm(),
      rules: {
        code: [{ required: true, message: '请输入套餐编码', trigger: 'blur' }],
        name: [{ required: true, message: '请输入套餐名称', trigger: 'blur' }],
        categoryId: [{ required: true, message: '请选择工单类型', trigger: 'change' }],
        price: [{ required: true, message: '请输入套餐价', trigger: 'blur' }]
      }
    }
  },
  created() {
    listCategory().then(res => (this.categories = res.data || []))
    pageServiceItem({ page: 1, pageSize: 200 }).then(res => {
      this.allItems = (res.data && res.data.records) || []
    })
    this.load()
  },
  methods: {
    async load() {
      this.loading = true
      try {
        const res = await pageServicePackage(this.query)
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
        const res = await getServicePackage(row.id)
        this.form = { ...res.data, items: (res.data.items || []).map(i => ({ ...i })) }
      } else {
        this.form = emptyForm()
      }
      this.itemToAdd = null
      this.dialogVisible = true
      this.$nextTick(() => this.$refs.form && this.$refs.form.clearValidate())
    },
    itemName(id) {
      const it = this.allItems.find(i => i.id === id)
      return it ? it.name : '服务项 #' + id
    },
    addItem() {
      if (this.form.items.some(i => i.itemId === this.itemToAdd)) {
        this.$message.warning('该服务项已加入')
        return
      }
      this.form.items.push({ itemId: this.itemToAdd, quantity: 1, times: 1 })
      this.itemToAdd = null
    },
    submit() {
      this.$refs.form.validate(async valid => {
        if (!valid) return
        if (this.form.id) {
          await updateServicePackage(this.form)
        } else {
          await saveServicePackage(this.form)
        }
        this.$message.success('保存成功')
        this.dialogVisible = false
        this.load()
      })
    },
    async toggle(row, val) {
      await updateServicePackageStatus(row.id, val ? 1 : 0)
      row.status = val ? 1 : 0
      this.$message.success(val ? '已上架' : '已下架')
    },
    async remove(row) {
      await this.$confirm(`确认删除服务包「${row.name}」？`, '提示', { type: 'warning' })
      await deleteServicePackage(row.id)
      this.$message.success('已删除')
      this.load()
    }
  }
}
</script>

<style lang="scss" scoped>
.count { font-size: 12px; color: #a8b3c9; font-weight: 400; margin-left: 6px; }
.price { color: #f56c6c; font-weight: 600; font-size: 14px; }
.origin { color: #a8b3c9; text-decoration: line-through; font-size: 12px; margin-left: 6px; }
.danger { color: #f56c6c; }
.pkg-picker { display: flex; gap: 8px; margin-bottom: 10px; }
.pkg-table { width: 100%; }
</style>
