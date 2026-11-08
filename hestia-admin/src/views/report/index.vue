<template>
  <div class="page-container">
    <div class="page-card">
      <div class="filter-bar">
        <el-date-picker
          v-model="range"
          type="daterange"
          value-format="yyyy-MM-dd"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          style="width: 260px"
        />
        <el-button type="primary" icon="el-icon-search" @click="load">查询</el-button>
        <el-button icon="el-icon-download" @click="exportExcel">导出报表</el-button>
        <span class="quick">
          <el-button type="text" @click="quick(7)">近 7 天</el-button>
          <el-button type="text" @click="quick(30)">近 30 天</el-button>
        </span>
      </div>

      <!-- SLA 指标 -->
      <el-row :gutter="14">
        <el-col :span="6" v-for="c in kpis" :key="c.label">
          <div class="kpi">
            <div class="k-label">{{ c.label }}</div>
            <div class="k-value" :class="c.tone">{{ c.value }}</div>
            <div class="k-hint">{{ c.hint }}</div>
          </div>
        </el-col>
      </el-row>
    </div>

    <el-row :gutter="14" class="mt14">
      <el-col :span="15">
        <div class="page-card">
          <div class="toolbar">
            <span class="title">工单趋势</span>
            <span class="sub">新增 / 完成</span>
          </div>
          <div ref="trend" class="chart"></div>
        </div>
      </el-col>
      <el-col :span="9">
        <div class="page-card">
          <div class="toolbar">
            <span class="title">工单类型分布</span>
          </div>
          <div ref="category" class="chart"></div>
        </div>
      </el-col>
    </el-row>

    <div class="page-card mt14">
      <div class="toolbar">
        <span class="title">工程师绩效</span>
        <span class="sub">按接单量排序</span>
      </div>
      <el-table :data="staffList" size="small" border stripe v-loading="loading">
        <el-table-column type="index" label="#" width="50" />
        <el-table-column prop="staffName" label="工程师" width="120" />
        <el-table-column prop="total" label="接单总数" width="100" align="center" />
        <el-table-column prop="completed" label="完成数" width="100" align="center" />
        <el-table-column label="平均解决时长" width="140" align="center">
          <template slot-scope="{ row }">{{ formatMinutes(row.avgResolveMinutes) }}</template>
        </el-table-column>
        <el-table-column label="按时完成率" min-width="180">
          <template slot-scope="{ row }">
            <el-progress
              :percentage="Math.min(100, Math.round(row.onTimeRate || 0))"
              :color="rateColor(row.onTimeRate)"
              :stroke-width="8"
            />
          </template>
        </el-table-column>
        <el-table-column label="平均评分" width="160">
          <template slot-scope="{ row }">
            <el-rate v-if="row.avgRating" :value="row.avgRating" disabled show-score score-template="{value}" />
            <span v-else>—</span>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import { slaReport, reportTrend, categoryReport, staffReport, exportReport } from '@/api/report'

const fmt = d => {
  const p = n => (n < 10 ? '0' + n : '' + n)
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}`
}

export default {
  name: 'Report',
  data() {
    const end = new Date()
    const begin = new Date()
    begin.setDate(begin.getDate() - 29)
    return {
      range: [fmt(begin), fmt(end)],
      loading: false,
      sla: {},
      trendData: [],
      categoryData: [],
      staffList: []
    }
  },
  computed: {
    kpis() {
      const s = this.sla || {}
      return [
        { label: '工单总数', value: s.total || 0, hint: '统计区间内创建', tone: '' },
        { label: '响应达标率', value: (s.responseRate || 0) + '%', hint: `达标 ${s.responseOnTime || 0} 单`, tone: 'blue' },
        { label: '解决达标率', value: (s.resolveRate || 0) + '%', hint: `达标 ${s.resolveOnTime || 0} 单`, tone: 'green' },
        { label: '平均响应时长', value: (s.avgResponseMinutes || 0) + ' 分', hint: '从创建到接单', tone: 'orange' }
      ]
    }
  },
  mounted() {
    this.load()
    window.addEventListener('resize', this.resize)
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.resize)
    this.trendChart && this.trendChart.dispose()
    this.categoryChart && this.categoryChart.dispose()
  },
  methods: {
    params() {
      return { begin: this.range[0], end: this.range[1] }
    },
    quick(days) {
      const end = new Date()
      const begin = new Date()
      begin.setDate(begin.getDate() - (days - 1))
      this.range = [fmt(begin), fmt(end)]
      this.load()
    },
    async load() {
      if (!this.range || this.range.length !== 2) {
        this.$message.warning('请选择日期范围')
        return
      }
      this.loading = true
      try {
        const p = this.params()
        const [s, t, c, st] = await Promise.all([
          slaReport(p), reportTrend(p), categoryReport(p), staffReport(p)
        ])
        this.sla = s.data || {}
        this.trendData = t.data || []
        this.categoryData = c.data || []
        this.staffList = st.data || []
        this.$nextTick(() => {
          this.renderTrend()
          this.renderCategory()
        })
      } finally {
        this.loading = false
      }
    },
    renderTrend() {
      if (!this.$refs.trend) return
      this.trendChart = echarts.init(this.$refs.trend)
      this.trendChart.setOption({
        grid: { left: 45, right: 20, top: 30, bottom: 50 },
        tooltip: { trigger: 'axis' },
        legend: { right: 0, top: 0, itemWidth: 10, itemHeight: 10, textStyle: { fontSize: 11 } },
        dataZoom: [{ type: 'inside' }, { type: 'slider', height: 16, bottom: 8 }],
        xAxis: {
          type: 'category',
          data: this.trendData.map(t => (t.date || '').slice(5)),
          axisLine: { lineStyle: { color: '#e6eaf0' } },
          axisLabel: { color: '#9099a8', fontSize: 11 }
        },
        yAxis: {
          type: 'value', minInterval: 1,
          splitLine: { lineStyle: { color: '#f0f2f6' } },
          axisLabel: { color: '#9099a8', fontSize: 11 }
        },
        series: [
          {
            name: '新增', type: 'bar', barWidth: 10,
            data: this.trendData.map(t => t.created || 0),
            itemStyle: { color: '#3b6ef0', borderRadius: [3, 3, 0, 0] }
          },
          {
            name: '完成', type: 'line', smooth: true, symbolSize: 5,
            data: this.trendData.map(t => t.completed || 0),
            itemStyle: { color: '#22b07d' }
          }
        ]
      })
    },
    renderCategory() {
      if (!this.$refs.category) return
      this.categoryChart = echarts.init(this.$refs.category)
      const data = [...this.categoryData].sort((a, b) => a.count - b.count)
      this.categoryChart.setOption({
        grid: { left: 80, right: 30, top: 20, bottom: 20 },
        tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
        xAxis: {
          type: 'value', minInterval: 1,
          splitLine: { lineStyle: { color: '#f0f2f6' } },
          axisLabel: { color: '#9099a8', fontSize: 11 }
        },
        yAxis: {
          type: 'category',
          data: data.map(d => d.categoryName),
          axisLine: { lineStyle: { color: '#e6eaf0' } },
          axisLabel: { color: '#6b7789', fontSize: 11 }
        },
        series: [{
          type: 'bar', barWidth: 12,
          data: data.map(d => d.count),
          itemStyle: { color: '#6b93ff', borderRadius: [0, 4, 4, 0] },
          label: { show: true, position: 'right', color: '#6b7789', fontSize: 11 }
        }]
      })
    },
    async exportExcel() {
      const res = await exportReport(this.params())
      const blob = new Blob([res.data], {
        type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
      })
      const url = window.URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = `hestia-report-${this.range[0]}_${this.range[1]}.xlsx`
      document.body.appendChild(a)
      a.click()
      document.body.removeChild(a)
      window.URL.revokeObjectURL(url)
      this.$message.success('报表已导出')
    },
    formatMinutes(m) {
      if (!m) return '0 分钟'
      if (m < 60) return m + ' 分钟'
      return (m / 60).toFixed(1) + ' 小时'
    },
    rateColor(v) {
      if (v >= 90) return '#22b07d'
      if (v >= 70) return '#3b6ef0'
      if (v >= 50) return '#f5a524'
      return '#f56c6c'
    },
    resize() {
      this.trendChart && this.trendChart.resize()
      this.categoryChart && this.categoryChart.resize()
    }
  }
}
</script>

<style lang="scss" scoped>
.mt14 { margin-top: 14px; }
.quick { margin-left: 6px; }

.kpi {
  background: #f7f9fc;
  border-radius: 8px;
  padding: 16px 18px;

  .k-label { font-size: 12.5px; color: #6b7789; }
  .k-value { font-size: 26px; font-weight: 600; margin: 8px 0 4px; color: #1f2733; }
  .k-value.blue { color: #3b6ef0; }
  .k-value.green { color: #22b07d; }
  .k-value.orange { color: #f5a524; }
  .k-hint { font-size: 11.5px; color: #a8b3c9; }
}

.chart { height: 300px; }
.sub { font-size: 12px; color: #a8b3c9; }
</style>
