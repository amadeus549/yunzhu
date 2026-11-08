<template>
  <div class="page-container">
    <div class="page-card">
      <div class="filter-bar">
        <el-input v-model="query.keyword" placeholder="小区 / 楼栋 / 房号" clearable @keyup.enter.native="search" />
        <el-select v-model="query.premiseType" placeholder="类型" clearable style="width: 130px">
          <el-option label="住宅" :value="1" />
          <el-option label="商铺" :value="2" />
          <el-option label="公共区域" :value="3" />
        </el-select>
        <el-button type="primary" icon="el-icon-search" @click="search">查询</el-button>
        <el-button icon="el-icon-refresh" @click="reset">重置</el-button>
      </div>

      <div class="toolbar">
        <span class="title">房屋点位 <span class="count">共 {{ filtered.length }} 个</span></span>
        <el-button type="primary" size="small" icon="el-icon-plus" @click="openEdit()">新增房屋</el-button>
      </div>

      <el-table :data="paged" size="small" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="fullAddress" label="完整地址" min-width="220" show-overflow-tooltip />
        <el-table-column label="类型" width="100" align="center">
          <template slot-scope="{ row }">
            <el-tag size="mini" :type="{ 1: 'success', 2: 'warning', 3: 'info' }[row.premiseType] || 'info'">
              {{ typeName(row.premiseType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="area" label="面积(㎡)" width="100" align="center" />
        <el-table-column label="默认地址" width="90" align="center">
          <template slot-scope="{ row }">
            <el-tag v-if="row.isDefault === 1" size="mini">默认</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="关联住户" width="120">
          <template slot-scope="{ row }">
            {{ row.residentName || '公共区域' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template slot-scope="{ row }">
            <el-button type="text" @click="openEdit(row)">编辑</el-button>
            <el-button type="text" class="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <el-pagination background layout="total, prev, pager, next"
          :current-page.sync="page" :page-size="10" :total="filtered.length" />
      </div>
    </div>

    <el-dialog :title="form.id ? '编辑房屋' : '新增房屋'" :visible.sync="dialogVisible" width="480px">
      <el-form ref="form" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="关联住户">
          <el-select v-model="form.residentId" placeholder="不选表示公共区域" clearable filterable style="width: 100%">
            <el-option v-for="r in residents" :key="r.id" :label="r.name + '（' + r.phone + '）'" :value="r.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="小区" prop="community">
          <el-input v-model="form.community" />
        </el-form-item>
        <el-form-item label="楼栋" prop="building">
          <el-input v-model="form.building" placeholder="如 3栋 / 地下车库" />
        </el-form-item>
        <el-form-item label="单元">
          <el-input v-model="form.unit" placeholder="如 2单元（可空）" />
        </el-form-item>
        <el-form-item label="房号" prop="roomNo">
          <el-input v-model="form.roomNo" placeholder="如 1503 / B1-01" />
        </el-form-item>
        <el-form-item label="类型" prop="premiseType">
          <el-radio-group v-model="form.premiseType">
            <el-radio :label="1">住宅</el-radio>
            <el-radio :label="2">商铺</el-radio>
            <el-radio :label="3">公共区域</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="面积(㎡)">
          <el-input-number v-model="form.area" :min="0" :precision="2" :controls="false" style="width: 160px" />
        </el-form-item>
        <el-form-item label="默认地址">
          <el-switch v-model="form.isDefault" :active-value="1" :inactive-value="0" />
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
import { listPremise, savePremise, updatePremise, deletePremise, listResident } from '@/api/basic'

const emptyForm = () => ({
  id: null, residentId: null, community: '阳光花园', building: '',
  unit: '', roomNo: '', premiseType: 1, area: null, isDefault: 0
})

export default {
  name: 'Premise',
  data() {
    return {
      loading: false, all: [], residents: [],
      query: { keyword: '', premiseType: null },
      page: 1,
      dialogVisible: false, form: emptyForm(),
      rules: {
        community: [{ required: true, message: '请输入小区名称', trigger: 'blur' }],
        building: [{ required: true, message: '请输入楼栋', trigger: 'blur' }],
        roomNo: [{ required: true, message: '请输入房号', trigger: 'blur' }],
        premiseType: [{ required: true, message: '请选择类型', trigger: 'change' }]
      }
    }
  },
  computed: {
    filtered() {
      const kw = (this.query.keyword || '').trim()
      return this.all.filter(p =>
        (!kw || (p.fullAddress || '').includes(kw)) &&
        (this.query.premiseType == null || p.premiseType === this.query.premiseType)
      )
    },
    paged() {
      const start = (this.page - 1) * 10
      return this.filtered.slice(start, start + 10)
    }
  },
  created() { this.load() },
  methods: {
    typeName(t) { return { 1: '住宅', 2: '商铺', 3: '公共区域' }[t] || '未知' },
    async load() {
      this.loading = true
      try {
        const [p, r] = await Promise.all([listPremise(), listResident()])
        this.all = p.data || []
        this.residents = (r.data || []).filter(x => x.status === 1)
      } finally { this.loading = false }
    },
    search() { this.page = 1 },
    reset() { this.query = { keyword: '', premiseType: null }; this.page = 1 },
    openEdit(row) {
      this.form = row ? { ...row } : emptyForm()
      this.dialogVisible = true
      this.$nextTick(() => this.$refs.form && this.$refs.form.clearValidate())
    },
    submit() {
      this.$refs.form.validate(async valid => {
        if (!valid) return
        const data = { ...this.form }
        if (!data.residentId) data.residentId = null
        if (this.form.id) await updatePremise(data)
        else await savePremise(data)
        this.$message.success('保存成功')
        this.dialogVisible = false
        this.load()
      })
    },
    remove(row) {
      this.$confirm(
        '确认删除「' + (row.fullAddress || '') + '」？若该房屋已被工单引用，历史工单的地址展示将受影响。',
        '提示', { type: 'warning' }
      ).then(async () => {
        await deletePremise(row.id)
        this.$message.success('删除成功')
        this.load()
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.count { font-size: 12px; color: #a8b3c9; font-weight: 400; margin-left: 6px; }
.danger { color: #f56c6c; }
</style>
