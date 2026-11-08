<template>
  <div class="page-container">
    <!-- 顶部指标 -->
    <el-row :gutter="14">
      <el-col :span="6" v-for="c in cards" :key="c.label">
        <div class="stat-card" :class="c.tone">
          <div class="label">{{ c.label }}</div>
          <div class="value">{{ c.value }}<span class="unit">{{ c.unit }}</span></div>
          <div class="hint">{{ c.hint }}</div>
          <i :class="c.icon" class="bg-icon"></i>
        </div>
      </el-col>
    </el-row>

    <!-- 次级指标 -->
    <el-row :gutter="14" class="mt14">
      <el-col :span="6" v-for="m in minors" :key="m.label">
        <div class="minor-card">
          <span class="m-label">{{ m.label }}</span>
          <span class="m-value" :class="m.tone">{{ m.value }}</span>
        </div>
      </el-col>
    </el-row>

    <!-- 图表区 -->
    <el-row :gutter="14" class="mt14">
      <el-col :span="16">
        <div class="page-card">
          <div class="toolbar">
            <span class="title">近 7 天工单趋势</span>
            <span class="sub">新增 / 完成</span>
          </div>
          <div ref="trendChart" class="chart"></div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="page-card">
          <div class="toolbar">
            <span class="title">近 30 天状态分布</span>
          </div>
          <div ref="pieChart" class="chart"></div>
        </div>
      </el-col>
    </el-row>

    <!-- 待办列表 -->
    <div class="page-card mt14">
      <div class="toolbar">
        <span class="title">待受理工单</span>
        <el-button type="text" @click="$router.push('/work-order')">查看全部</el-button>
      </div>
      <el-table :data="pendingList" size="small" v-loading="loading">
        <el-table-column prop="orderNo" label="工单号" width="150" />
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="categoryName" label="类型" width="100" />
        <el-table-column prop="premiseAddress" label="位置" min-width="180" show-overflow-tooltip />
        <el-table-column label="优先级" width="90">
          <template slot-scope="{ row }">
            <el-tag :type="priorityType(row.priority)" size="mini">{{ row.priorityName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="SLA" width="110">
          <template slot-scope="{ row }">
            <span :class="'sla-' + (row.slaStatus || 'NORMAL').toLowerCase()">
              {{ slaText(row) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="提交时间" width="160" />
        <el-table-column label="操作" width="90" fixed="right">
          <template slot-scope="{ row }">
            <el-button type="text" @click="$router.push('/work-order/' + row.id)">处理</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import { workspaceOverview, statusDistribution, workOrderTrend } from '@/api/report'
import { pageWorkOrder } from '@/api/workOrder'

const STATUS_NAME = {
  0: '已取消', 1: '待受理', 2: '待派单', 3: '已派单',
  4: '已接单', 5: '处理中', 6: '待验收', 7: '已完成', 8: '已驳回'
}

export default {
  name: 'Dashboard',
  data() {
    return {
      loading: false,
      overview: {},
      trend: [],
      distribution: [],
      pendingList: []
    }
  },
  computed: {
    cards() {
      const o = this.overview || {}
      return [
        { label: '今日新增', value: o.todayNew || 0, unit: '单', hint: '今日住户与管家提交', icon: 'el-icon-document-add', tone: 'blue' },
        { label: '待受理', value: o.pendingAccept || 0, unit: '单', hint: '需要管家尽快受理', icon: 'el-icon-bell', tone: 'orange' },
        { label: '处理中', value: o.processing || 0, unit: '单', hint: '工程师正在处理', icon: 'el-icon-loading', tone: 'purple' },
        { label: '今日完成', value: o.todayCompleted || 0, unit: '单', hint: '验收通过的工单', icon: 'el-icon-circle-check', tone: 'green' }
      ]
    },
    minors() {
      const o = this.overview || {}
      return [
        { label: 'SLA 超时', value: o.slaOverdue || 0, tone: 'danger' },
        { label: '紧急未完成', value: o.urgentCount || 0, tone: 'danger' },
        { label: '忙碌 / 空闲工程师', value: `${o.engineerBusy || 0} / ${o.engineerIdle || 0}`, tone: '' },
        { label: '今日按时完成率', value: (o.onTimeRate || 0) + '%', tone: 'ok' }
      ]
    }
  },
  mounted() {
    this.loadAll()
    window.addEventListener('resize', this.resize)
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.resize)
    this.trendChart && this.trendChart.dispose()
    this.pieChart && this.pieChart.dispose()
  },
  methods: {
    async loadAll() {
      this.loading = true
      try {
        const [ov, tr, dist, list] = await Promise.all([
          workspaceOverview(),
          workOrderTrend(7),
          statusDistribution(),
          pageWorkOrder({ page: 1, pageSize: 6, status: 1 })
        ])
        this.overview = ov.data || {}
        this.trend = tr.data || []
        this.distribution = dist.data || []
        this.pendingList = (list.data && list.data.records) || []
        this.$nextTick(() => {
          this.renderTrend()
          this.renderPie()
        })
      } finally {
        this.loading = false
      }
    },
    renderTrend() {
      if (!this.$refs.trendChart) return
      this.trendChart = echarts.init(this.$refs.trendChart)
      const dates = this.trend.map(t => (t.date || '').slice(5))
      this.trendChart.setOption({
        grid: { left: 40, right: 20, top: 30, bottom: 30 },
        tooltip: { trigger: 'axis' },
        legend: { right: 0, top: 0, itemWidth: 10, itemHeight: 10, textStyle: { fontSize: 11 } },
        xAxis: {
          type: 'category', data: dates, boundaryGap: false,
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
            name: '新增', type: 'line', smooth: true, symbolSize: 5,
            data: this.trend.map(t => t.created || 0),
            itemStyle: { color: '#3b6ef0' },
            areaStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: 'rgba(59,110,240,0.25)' },
                { offset: 1, color: 'rgba(59,110,240,0.02)' }
              ])
            }
          },
          {
            name: '完成', type: 'line', smooth: true, symbolSize: 5,
            data: this.trend.map(t => t.completed || 0),
            itemStyle: { color: '#22b07d' }
          }
        ]
      })
    },
    renderPie() {
      if (!this.$refs.pieChart) return
      this.pieChart = echarts.init(this.$refs.pieChart)
      const data = this.distribution.map(d => ({
        name: d.statusName || STATUS_NAME[d.status] || d.status,
        value: d.count
      }))
      this.pieChart.setOption({
        tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
        legend: {
          orient: 'vertical', right: 0, top: 'middle',
          itemWidth: 8, itemHeight: 8, textStyle: { fontSize: 11, color: '#6b7789' }
        },
        series: [{
          type: 'pie', radius: ['45%', '68%'], center: ['38%', '50%'],
          avoidLabelOverlap: true,
          itemStyle: { borderColor: '#fff', borderWidth: 2 },
          label: { show: false },
          data
        }],
        color: ['#9099a8', '#f5a524', '#3b6ef0', '#7c5cff', '#22b07d', '#12b5cb', '#e6a23c', '#67c23a', '#f56c6c']
      })
    },
    resize() {
      this.trendChart && this.trendChart.resize()
      this.pieChart && this.pieChart.resize()
    },
    priorityType(p) {
      return { 4: 'danger', 3: 'warning', 2: '', 1: 'info' }[p] || ''
    },
    slaText(row) {
      if (row.slaStatus === 'OVERDUE') return '已超时'
      if (row.slaRemainMinutes === null || row.slaRemainMinutes === undefined) return '—'
      return row.slaRemainMinutes + ' 分钟'
    }
  }
}
</script>

<style lang="scss" scoped>
.mt14 { margin-top: 14px; }

.stat-card {
  position: relative;
  background: #fff;
  border-radius: 8px;
  padding: 16px 18px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(16, 24, 40, 0.04);

  .label { font-size: 13px; color: #6b7789; }
  .value {
    font-size: 28px;
    font-weight: 600;
    margin: 8px 0 4px;
    color: #1f2733;
    .unit { font-size: 12px; font-weight: 400; color: #9099a8; margin-left: 4px; }
  }
  .hint { font-size: 11.5px; color: #a8b3c9; }
  .bg-icon {
    position: absolute;
    right: 12px;
    bottom: 10px;
    font-size: 44px;
    opacity: 0.09;
  }

  &.blue .value { color: #3b6ef0; }
  &.orange .value { color: #f5a524; }
  &.purple .value { color: #7c5cff; }
  &.green .value { color: #22b07d; }
}

.minor-card {
  background: #fff;
  border-radius: 8px;
  padding: 12px 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 1px 3px rgba(16, 24, 40, 0.04);

  .m-label { font-size: 12.5px; color: #6b7789; }
  .m-value { font-size: 18px; font-weight: 600; color: #1f2733; }
  .m-value.danger { color: #f56c6c; }
  .m-value.ok { color: #22b07d; }
}

.chart { height: 260px; }
.sub { font-size: 12px; color: #a8b3c9; }

.sla-normal { color: #22b07d; }
.sla-warning { color: #f5a524; }
.sla-overdue { color: #f56c6c; font-weight: 600; }
</style>
