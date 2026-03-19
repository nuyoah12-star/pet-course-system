<!-- eslint-disable vue/multi-word-component-names -->
<template>
  <div class="stats-page">
    <!-- 顶部装饰栏 -->
    <div class="page-header">
      <h2 class="page-title">
        <i class="icon-chart"></i> 宠物学习数据统计
      </h2>
      <div class="decor-line"></div>
    </div>

    <!-- 宠物选择区域（美化版） -->
    <div class="pet-select-container">
      <div class="select-label">
        <i class="icon-pet"></i> 选择查看宠物：
      </div>
      <select 
        v-model="selectedPetId" 
        @change="loadAllChartData" 
        class="custom-select"
      >
        <option value="-1">🐾 全部宠物</option>
        <option value="1">🐶 可乐</option>
        <option value="2">🐱 糯米</option>
        <option value="3">🐕 旺财</option>
      </select>
    </div>
    
    <!-- 响应式网格布局（优化间距和排版） -->
    <div class="chart-grid">
      <!-- 课程统计柱状图卡片（带装饰） -->
      <div class="chart-card course-card">
        <div class="card-header">
          <h3 class="card-title">{{ courseTitle }}</h3>
          <div class="card-badge">学习频次</div>
        </div>
        <div class="card-content">
          <div ref="chartRef" class="chart-container"></div>
        </div>
        <div class="card-decoration"></div>
      </div>

      <!-- 经验成长折线图卡片 -->
      <div class="chart-card exp-card">
        <div class="card-header">
          <h3 class="card-title">{{ expTitle }}</h3>
          <div class="card-badge">成长趋势</div>
        </div>
        <div class="card-content">
          <div ref="lineChartRef" class="chart-container"></div>
        </div>
        <div class="card-decoration"></div>
      </div>

      <!-- 任务完成饼图卡片 -->
      <div class="chart-card task-card">
        <div class="card-header">
          <h3 class="card-title">{{ taskTitle }}</h3>
          <div class="card-badge">完成占比</div>
        </div>
        <div class="card-content">
          <div ref="taskChartRef" class="chart-container"></div>
        </div>
        <div class="card-decoration"></div>
      </div>
    </div>

    <!-- 底部装饰 -->
    <div class="page-footer">
      <div class="footer-text">数据实时更新 · 统计周期：最近4周</div>
    </div>
  </div>
</template>

<script>
/* eslint-disable vue/multi-word-component-names */
import * as echarts from 'echarts'
import axios from 'axios'
import { eventBus } from '@/utils/eventBus'

// 配置axios默认请求头，解决跨域和缓存问题
axios.defaults.headers.common['Content-Type'] = 'application/json'
axios.defaults.headers.common['Cache-Control'] = 'no-cache'
axios.defaults.timeout = 5000
axios.defaults.withCredentials = true

export default {
  name: 'Stats',
  data() {
    return {
      chart: null,
      lineChart: null,
      taskChart: null,
      selectedPetId: '-1',
      courseTitle: '宠物课程学习统计',
      expTitle: '宠物经验成长趋势',
      taskTitle: '任务完成情况统计',
      taskCompletedHandler: null,
      // 新增：预设各宠物经验数据
      petExpData: {
        '-1': [125, 230, 475, 665], // 全部宠物
        '1': [50, 70, 165, 275],    // 可乐
        '2': [40, 85, 175, 205],    // 糯米
        '3': [35, 75, 135, 185]     // 旺财
      },
      // 新增：预设各宠物纵坐标配置
      yAxisConfig: {
        '-1': { max: 900, interval: 150 }, // 全部宠物：0,150,300,450,600,750,900
        '1': { max: 300, interval: 50 },   // 可乐：0,50,100,150,200,250,300
        '2': { max: 300, interval: 50 },   // 糯米：0,50,100,150,200,250,300
        '3': { max: 300, interval: 50 }    // 旺财：0,50,100,150,200,250,300
      }
    }
  },
  mounted() {
    // 关键：等待DOM完全渲染后再初始化图表
    this.$nextTick(() => {
      this.initCharts()
      this.loadAllChartData()
    })
    window.addEventListener('resize', this.resizeCharts)

    this.taskCompletedHandler = (data) => {
      console.log(`【Stats】收到任务完成事件，petId=${data.petId}`);
      if (this.selectedPetId === '-1' || this.selectedPetId === String(data.petId)) {
        this.loadAllChartData();
        console.log(`【Stats】自动刷新${this.selectedPetId === '-1' ? '全部宠物' : '当前选中宠物'}的统计图表`);
      }
    }
    eventBus.on('taskCompleted', this.taskCompletedHandler);
  },
  unmounted() {
    this.chart?.dispose()
    this.lineChart?.dispose()
    this.taskChart?.dispose()
    window.removeEventListener('resize', this.resizeCharts)
    
    if (this.taskCompletedHandler) {
      eventBus.off('taskCompleted', this.taskCompletedHandler);
    }
  },
  methods: {
    initCharts() {
      this.chart?.dispose()
      this.lineChart?.dispose()
      this.taskChart?.dispose()

      const chartDom = this.$refs.chartRef
      const lineChartDom = this.$refs.lineChartRef
      const taskChartDom = this.$refs.taskChartRef
      
      if (chartDom) this.chart = echarts.init(chartDom)
      if (lineChartDom) this.lineChart = echarts.init(lineChartDom)
      if (taskChartDom) this.taskChart = echarts.init(taskChartDom)
    },

    async loadAllChartData() {
      // ========== 核心修复：先等DOM和图表实例准备好，再加载数据 ==========
      await this.$nextTick()
      if (!this.chart || !this.lineChart || !this.taskChart) {
        this.initCharts()
      }

      try {
        const params = this.selectedPetId === '-1' 
          ? {} 
          : { petId: this.selectedPetId }

        const [courseRes, taskRes] = await Promise.all([
          axios.get('http://localhost:8080/stats/courseCount', { params }),
          axios.get('http://localhost:8080/stats/taskCount', { params })
        ])

        console.log('课程数据：', courseRes.data)
        console.log('任务数据：', taskRes.data)

        this.updateChartTitle()

        // 动态获取课程名称和数据
        const courseNames = Object.keys(courseRes.data || {})
        const courseValues = courseNames.map(name => courseRes.data[name] || 0)

        // ========== 柱状图：优化配色和样式 ==========
        if (this.chart) {
          this.chart.setOption({
            tooltip: { 
              trigger: 'axis', 
              axisPointer: { type: 'shadow' },
              backgroundColor: 'rgba(255,255,255,0.9)',
              borderColor: '#e8f4f8',
              borderWidth: 1,
              textStyle: { color: '#2d3748' },
              padding: [10, 15]
            },
            grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
            xAxis: { 
              type: 'category', 
              data: courseNames,
              axisLabel: { fontSize: 12, color: '#4a5568' },
              axisLine: { lineStyle: { color: '#e2e8f0' } },
              axisTick: { show: false }
            },
            yAxis: { 
              type: 'value',
              min: 0,
              max: 6,
              interval: 1,
              axisLabel: { fontSize: 12, color: '#4a5568' },
              axisLine: { show: false },
              axisTick: { show: false },
              splitLine: { lineStyle: { color: '#f7fafc', type: 'dashed' } }
            },
            series: [{ 
              name: '学习次数',
              data: courseValues,
              type: 'bar',
              // 渐变色彩
              itemStyle: { 
                color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                  { offset: 0, color: '#42b983' },
                  { offset: 1, color: '#38a169' }
                ]),
                borderRadius: [6, 6, 0, 0]
              },
              barWidth: '40%',
              // 柱状图动画
              animationDuration: 1000,
              animationEasing: 'cubicOut'
            }]
          })
        }

        // ========== 折线图：优化配色和样式 ==========
        if (this.lineChart) {
          const expData = this.petExpData[this.selectedPetId] || [0, 0, 0, 0]
          const yAxisSetting = this.yAxisConfig[this.selectedPetId] || { max: 300, interval: 50 }
          
          this.lineChart.setOption({
            tooltip: { 
              trigger: 'axis',
              backgroundColor: 'rgba(255,255,255,0.9)',
              borderColor: '#e8f4f8',
              borderWidth: 1,
              textStyle: { color: '#2d3748' },
              padding: [10, 15]
            },
            grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
            xAxis: { 
              type: 'category', 
              data: ['第1周', '第2周', '第3周', '第4周'],
              axisLabel: { fontSize: 12, color: '#4a5568' },
              axisLine: { lineStyle: { color: '#e2e8f0' } },
              axisTick: { show: false }
            },
            yAxis: { 
              type: 'value',
              min: 0,
              max: yAxisSetting.max,
              interval: yAxisSetting.interval,
              axisLabel: { fontSize: 12, color: '#4a5568' },
              axisLine: { show: false },
              axisTick: { show: false },
              splitLine: { lineStyle: { color: '#f7fafc', type: 'dashed' } }
            },
            series: [{ 
              name: '经验值',
              data: expData,
              type: 'line',
              smooth: true,
              // 渐变线条+填充
              itemStyle: { 
                color: '#4299e1',
                borderColor: '#ffffff',
                borderWidth: 2
              },
              lineStyle: { 
                width: 3,
                color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
                  { offset: 0, color: '#4299e1' },
                  { offset: 1, color: '#3182ce' }
                ])
              },
              areaStyle: {
                color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                  { offset: 0, color: 'rgba(66, 153, 225, 0.3)' },
                  { offset: 1, color: 'rgba(66, 153, 225, 0.05)' }
                ])
              },
              symbol: 'circle',
              symbolSize: 8,
              // 折线图动画
              animationDuration: 1200,
              animationEasing: 'cubicOut'
            }]
          })
        }

        // ========== 饼图：优化配色和样式 ==========
        const pieData = Object.keys(taskRes.data).map(k => ({
          name: k,
          value: taskRes.data[k] || 0
        })).filter(item => item.value > 0)

        if (this.taskChart) {
          this.taskChart.setOption({
            tooltip: { 
              trigger: 'item',
              backgroundColor: 'rgba(255,255,255,0.9)',
              borderColor: '#e8f4f8',
              borderWidth: 1,
              textStyle: { color: '#2d3748' },
              padding: [10, 15]
            },
            series: [{ 
              type: 'pie',
              radius: ['40%', '70%'],
              data: pieData.length > 0 ? pieData : [{ name: '暂无数据', value: 1 }],
              label: {
                formatter: '{b}: {c} ({d}%)',
                fontSize: 12,
                color: '#2d3748'
              },
              labelLine: {
                lineStyle: { color: '#cbd5e0' }
              },
              itemStyle: {
                // 丰富的渐变配色
                color: (params) => {
                  const colorList = [
                    new echarts.graphic.LinearGradient(0, 0, 1, 1, [{ offset: 0, color: '#42b983' }, { offset: 1, color: '#38a169' }]),
                    new echarts.graphic.LinearGradient(0, 0, 1, 1, [{ offset: 0, color: '#f6ad55' }, { offset: 1, color: '#dd6b20' }]),
                    new echarts.graphic.LinearGradient(0, 0, 1, 1, [{ offset: 0, color: '#9f7aea' }, { offset: 1, color: '#805ad5' }]),
                    new echarts.graphic.LinearGradient(0, 0, 1, 1, [{ offset: 0, color: '#38b2ac' }, { offset: 1, color: '#319795' }])
                  ]
                  return colorList[params.dataIndex % colorList.length]
                },
                borderRadius: 8,
                shadowColor: 'rgba(0,0,0,0.1)',
                shadowBlur: 10
              },
              // 饼图动画
              animationDuration: 1500,
              animationEasing: 'cubicOut'
            }]
          })
        }
      } catch (err) {
        console.error('加载图表数据失败：', err)
        this.renderEmptyChart()
        alert('统计数据加载失败，请检查：\n1. 后端服务是否启动\n2. 数据库是否有测试数据\n3. 接口地址是否正确')
      }
    },

    renderEmptyChart() {
      this.chart?.setOption({
        xAxis: { type: 'category', data: ['暂无数据'] },
        yAxis: { type: 'value' },
        series: [{ data: [0], type: 'bar' }]
      })
      this.lineChart?.setOption({
        xAxis: { type: 'category', data: ['暂无数据'] },
        yAxis: { type: 'value' },
        series: [{ data: [0], type: 'line' }]
      })
      this.taskChart?.setOption({
        series: [{ 
          type: 'pie',
          data: [{ name: '暂无数据', value: 1 }]
        }]
      })
    },

    updateChartTitle() {
      const petNameMap = {
        '-1': '全部宠物',
        '1': '可乐',
        '2': '糯米',
        '3': '旺财'
      }
      const petName = petNameMap[this.selectedPetId]
      this.courseTitle = `${petName}课程学习统计`
      this.expTitle = `${petName}经验成长趋势`
      this.taskTitle = `${petName}任务完成情况统计`
    },

    resizeCharts() {
      this.chart?.resize()
      this.lineChart?.resize()
      this.taskChart?.resize()
    }
  }
}
</script>

<style scoped>
/* 全局样式重置和基础配置 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
  font-family: 'Inter', 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

/* 页面容器：高级渐变背景 */
.stats-page {
  padding: 30px 40px;
  background: linear-gradient(135deg, #f0f8fb 0%, #e8f4f8 50%, #f5fafe 100%);
  min-height: 100vh;
  position: relative;
  overflow-x: hidden;
}

/* 页面头部 */
.page-header {
  text-align: center;
  margin-bottom: 40px;
  position: relative;
}

.page-title {
  color: #2d3748;
  font-size: 32px;
  font-weight: 700;
  letter-spacing: 0.5px;
  position: relative;
  padding-bottom: 15px;
}

.page-title::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 80px;
  height: 4px;
  background: linear-gradient(90deg, #42b983, #4299e1);
  border-radius: 2px;
}

.icon-chart {
  margin-right: 10px;
  font-size: 28px;
}

.decor-line {
  position: absolute;
  top: 50%;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, #cbd5e0, transparent);
  z-index: 0;
}

.page-title {
  position: relative;
  z-index: 1;
  background: linear-gradient(135deg, #f0f8fb 0%, #e8f4f8 100%);
  padding: 0 20px;
  display: inline-block;
}

/* 宠物选择容器 */
.pet-select-container {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 35px;
  gap: 12px;
}

.select-label {
  font-size: 18px;
  color: #4a5568;
  font-weight: 500;
}

.icon-pet {
  margin-right: 8px;
  color: #4299e1;
}

/* 自定义下拉框样式 */
.custom-select {
  padding: 12px 20px;
  border-radius: 8px;
  border: 2px solid #e2e8f0;
  font-size: 16px;
  color: #2d3748;
  background-color: #ffffff;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
  min-width: 200px;
}

.custom-select:hover {
  border-color: #4299e1;
  box-shadow: 0 4px 12px rgba(66, 153, 225, 0.15);
}

.custom-select:focus {
  outline: none;
  border-color: #42b983;
  box-shadow: 0 0 0 3px rgba(66, 185, 131, 0.1);
}

/* 图表网格布局：优化间距和响应式 */
.chart-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 30px;
  margin-bottom: 40px;
}

@media (max-width: 1200px) {
  .chart-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .chart-grid {
    grid-template-columns: 1fr;
    gap: 25px;
  }
  .stats-page {
    padding: 20px 15px;
  }
  .page-title {
    font-size: 24px;
  }
}

/* 图表卡片：高级样式 */
.chart-card {
  background: #ffffff;
  border-radius: 16px;
  box-shadow: 0 8px 24px rgba(0,0,0,0.06);
  padding: 0;
  transition: all 0.4s ease;
  position: relative;
  overflow: hidden;
}

.chart-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 32px rgba(0,0,0,0.1);
}

/* 不同卡片的主题色 */
.course-card {
  border-top: 4px solid #42b983;
}

.exp-card {
  border-top: 4px solid #4299e1;
}

.task-card {
  border-top: 4px solid #9f7aea;
}

/* 卡片头部 */
.card-header {
  padding: 20px 25px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #f7fafc;
}

.card-title {
  color: #2d3748;
  font-size: 18px;
  font-weight: 600;
  margin: 0;
}

.card-badge {
  background: rgba(66, 153, 225, 0.1);
  color: #4299e1;
  font-size: 12px;
  padding: 4px 12px;
  border-radius: 20px;
  font-weight: 500;
}

.course-card .card-badge {
  background: rgba(66, 185, 131, 0.1);
  color: #42b983;
}

.task-card .card-badge {
  background: rgba(159, 122, 234, 0.1);
  color: #9f7aea;
}

/* 卡片内容区 */
.card-content {
  padding: 25px;
}

.chart-container {
  width: 100%;
  height: 320px;
  min-height: 300px;
}

/* 卡片装饰元素 */
.card-decoration {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 120px;
  height: 120px;
  background: linear-gradient(135deg, rgba(66, 153, 225, 0.03), rgba(66, 153, 225, 0.08));
  border-radius: 50% 0 0 0;
  z-index: 0;
}

.course-card .card-decoration {
  background: linear-gradient(135deg, rgba(66, 185, 131, 0.03), rgba(66, 185, 131, 0.08));
}

.task-card .card-decoration {
  background: linear-gradient(135deg, rgba(159, 122, 234, 0.03), rgba(159, 122, 234, 0.08));
}

.card-content {
  position: relative;
  z-index: 1;
}

/* 页面底部 */
.page-footer {
  text-align: center;
  margin-top: 20px;
}

.footer-text {
  color: #718096;
  font-size: 14px;
  padding: 15px;
  background: rgba(255, 255, 255, 0.7);
  border-radius: 8px;
  display: inline-block;
}

/* 动画效果 */
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.chart-card {
  animation: fadeIn 0.6s ease forwards;
}

.chart-card:nth-child(1) { animation-delay: 0.1s; }
.chart-card:nth-child(2) { animation-delay: 0.2s; }
.chart-card:nth-child(3) { animation-delay: 0.3s; }
</style>