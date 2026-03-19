<template>
  <div class="mycourse-page">
    <div class="content">
      <!-- 页面头部 -->
      <div class="page-header">
        <h2>我的课程</h2>
      </div>
      
      <!-- 加载中提示 -->
      <div v-if="loading || petLoading" class="loading-container">
        <div class="loading-spinner"></div>
        <p>正在加载数据...</p>
      </div>

      <div v-else>
        <!-- 宠物选择下拉框 -->
        <div class="pet-select-container">
          <label for="pet-select">选择宠物：</label>
          <select 
            id="pet-select"
            v-model="selectedPetId" 
            @change="loadMyCourses" 
            class="pet-select" 
            :disabled="petLoading"
          >
            <option v-for="pet in pets" :key="pet.id" :value="pet.id">
              {{ pet.name }} (Lv{{ pet.level || 1 }})
            </option>
          </select>
        </div>

        <!-- 无课程提示 -->
        <div v-if="courses.length === 0" class="empty-container">
          <img 
            src="data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTIwIiBoZWlnaHQ9IjEyMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48Y2lyY2xlIGN4PSI2MCIgY3k9IjYwIiByPSI2MCIgZmlsbD0iI2VlZWVlZSIvPjx0ZXh0IHg9IjYwIiB5PSI2NSIgZm9udC1mYW1pbHk9IkFyaWFsIiBmb250LXNpemU9IjEyIiBmaWxsPSIjODk4IiB0ZXh0LWFuY2hvcj0ibWlkZGxlIj7lm57lhbvlo7DmsqE8L3RleHQ+PC9zdmc+" 
            alt="空状态" 
            class="empty-img"
          >
          <p class="empty-text">{{ selectedPetName }}暂无已报名课程，快去选课吧！</p>
          <button @click="goToCourses" class="btn-goto">去选课</button>
        </div>

        <!-- 已报名课程列表 -->
        <div v-else class="course-list">
          <div 
            v-for="(course, index) in courses" 
            :key="course.id" 
            class="course-card"
            :class="`card-theme-${index % 4}`"
          >
            <div class="card-header">
              <h3 class="course-title">{{ course.name }}</h3>
              <!-- 已移除进度标签 -->
            </div>
            <div class="card-body">
              <div class="info-row">
                <span class="info-label">训练师</span>
                <span class="info-value">{{ course.trainer || '暂无' }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">课程描述</span>
                <span class="info-value">
                  {{ course.name === '坐下训练' ? '训练宠物坐下' : (course.description || '暂无') }}
                </span>
              </div>
              <div class="info-row">
                <span class="info-label">报名时间</span>
                <span class="info-value">{{ course.selectTime ? formatTime(course.selectTime) : '暂无' }}</span>
              </div>
            </div>
            <div class="card-actions">
              <button @click="goToTasks(course.id)" class="btn-task" :disabled="loading">
                查看任务
              </button>
              <button @click="cancelCourse(course.id)" class="btn-cancel" :disabled="cancelLoading">
                {{ cancelLoading ? '处理中...' : '取消课程' }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'
import { eventBus } from '@/utils/eventBus'

export default {
  name: 'MyCourse',
  data() {
    return {
      courses: [],
      pets: [],
      selectedPetId: 1,
      selectedPetName: '',
      loading: false,
      petLoading: false,
      cancelLoading: false
    }
  },
  mounted() {
    this.loadPets().then(() => {
      this.loadMyCourses()
    })

    this.taskCompletedHandler = (data) => {
      if (this.selectedPetId === data.petId) {
        this.loadMyCourses()
      }
    }
    eventBus.on('taskCompleted', this.taskCompletedHandler)
  },
  unmounted() {
    eventBus.off('taskCompleted', this.taskCompletedHandler)
  },
  methods: {
    goToTasks(courseId) {
      this.$router.push({
        path: '/task',
        query: {
          courseId: courseId,
          petId: this.selectedPetId
        }
      })
    },
    async loadPets() {
      this.petLoading = true
      try {
        const res = await axios.get('http://localhost:8080/pet', {
          timeout: 5000,
          withCredentials: true,
          headers: {
            'Cache-Control': 'no-cache',
            'Pragma': 'no-cache',
            'Expires': '0'
          }
        })
        this.pets = Array.isArray(res.data) ? res.data.filter(pet => pet.name && ['可乐', '糯米', '旺财'].includes(pet.name)) : []
        if (this.pets.length > 0) {
          this.selectedPetId = this.pets[0].id
          this.selectedPetName = this.pets[0].name || '未知宠物'
        } else {
          this.selectedPetName = '暂无宠物'
        }
      } catch (err) {
        console.error('加载宠物列表失败：', err)
        alert('加载宠物列表失败，请检查后端服务')
        this.pets = []
        this.selectedPetName = '加载失败'
      } finally {
        this.petLoading = false
      }
    },
    loadMyCourses() {
      if (this.pets.length === 0) {
        this.courses = []
        return
      }
      this.loading = true
      const currentPet = this.pets.find(pet => pet.id === this.selectedPetId)
      this.selectedPetName = currentPet?.name || '未知宠物'
      
      axios.get('http://localhost:8080/petCourse/myCourses', {
        params: { petId: this.selectedPetId },
        timeout: 5000,
        withCredentials: true,
        headers: {
          'Cache-Control': 'no-cache',
          'Pragma': 'no-cache',
          'Expires': '0'
        }
      })
      .then(res => {
        this.courses = Array.isArray(res.data) ? res.data : []
      })
      .catch(err => {
        console.error('加载课程失败：', err)
        alert(`加载${this.selectedPetName}的课程失败`)
        this.courses = []
      })
      .finally(() => {
        this.loading = false
      })
    },
    cancelCourse(courseId) {
      if (this.cancelLoading) return
      if (!confirm(`确定要取消${this.selectedPetName}的【${courseId}】号课程吗？`)) return
      
      this.cancelLoading = true
      axios.get('http://localhost:8080/petCourse/cancel', {
        params: {
          petId: this.selectedPetId,
          courseId: courseId
        },
        timeout: 5000,
        withCredentials: true
      })
      .then(res => {
        alert(res.data || '取消课程成功！')
        this.loadMyCourses()
      })
      .catch(err => {
        console.error('取消失败：', err)
        alert('取消课程失败，请重试')
      })
      .finally(() => {
        this.cancelLoading = false
      })
    },
    goToCourses() {
      this.$router.push('/courses')
    },
    formatTime(time) {
      if (!time) return '暂无'
      const timestamp = typeof time === 'number' ? time : new Date(time).getTime()
      if (isNaN(timestamp)) return '格式错误'
      
      const date = new Date(timestamp)
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      })
    }
  }
}
</script>

<style scoped>
/* 核心修改：背景图替换为51.jpeg并适配屏幕 */
.mycourse-page {
  /* 背景图配置：替换为51.jpeg */
  background: url('http://localhost:8080/uploads/51.jpeg') no-repeat center center;
  /* 保证图片完整显示且适配屏幕，等比例缩放覆盖整个页面 */
  background-size: cover;
  /* 固定背景，滚动页面时不移动 */
  background-attachment: fixed;
  /* 背景色兜底，避免图片加载失败/未覆盖区域空白 */
  background-color: #f8f9fa;
  
  min-height: 100vh;
  padding: 24px 0;
  /* 增加半透明遮罩，提升内容可读性 */
  position: relative;
}

/* 半透明遮罩层，让文字更清晰 */
.mycourse-page::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(255, 255, 255, 0.85); /* 85% 不透明度，平衡背景图显示和内容可读性 */
  pointer-events: none; /* 不影响页面交互 */
  z-index: 0;
}

.content {
  width: 95%;
  max-width: 1200px;
  margin: 0 auto;
  /* 提升层级，显示在遮罩上方 */
  position: relative;
  z-index: 1;
}

.page-header {
  margin-bottom: 24px;
}
.page-header h2 {
  color: #2c3e50;
  margin: 0;
  padding-bottom: 8px;
  border-bottom: 3px solid #66cc8a;
  display: inline-block;
  font-size: 28px;
  font-weight: 700;
}

.loading-container {
  text-align: center;
  padding: 60px 0;
  color: #5d6d7e;
  position: relative;
  z-index: 1;
}
.loading-spinner {
  width: 32px;
  height: 32px;
  border: 3px solid #66cc8a;
  border-top: 3px solid transparent;
  border-radius: 50%;
  margin: 0 auto 12px;
  animation: spin 1s linear infinite;
}
@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.pet-select-container {
  margin-bottom: 24px;
  display: flex;
  align-items: center;
  background: rgba(255, 255, 255, 0.9); /* 半透明背景，适配背景图 */
  padding: 12px 16px;
  border-radius: 10px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.03);
  position: relative;
  z-index: 1;
}
.pet-select-container label {
  font-size: 16px;
  margin-right: 12px;
  color: #5d6d7e;
  font-weight: 500;
}
.pet-select {
  padding: 8px 16px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 16px;
  min-width: 220px;
  background: #ffffff;
  transition: all 0.3s ease;
}
.pet-select:focus {
  outline: none;
  border-color: #66cc8a;
  box-shadow: 0 0 0 3px rgba(102, 204, 138, 0.1);
}
.pet-select:disabled {
  background-color: #f8f9f9;
  cursor: not-allowed;
}

.empty-container {
  text-align: center;
  padding: 60px 0;
  background: rgba(255, 255, 255, 0.9); /* 半透明背景，适配背景图 */
  border-radius: 12px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.03);
  position: relative;
  z-index: 1;
}
.empty-img {
  width: 80px;
  margin-bottom: 12px;
  opacity: 0.6;
}
.empty-text {
  color: #6c7881;
  font-size: 16px;
  margin-bottom: 20px;
}
.btn-goto {
  padding: 10px 24px;
  background: #66cc8a;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 16px;
  transition: all 0.3s ease;
}
.btn-goto:hover {
  background-color: #55bb7a;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 204, 138, 0.2);
}

.course-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 20px;
  position: relative;
  z-index: 1;
}

.course-card {
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.04);
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
  /* 半透明背景，适配背景图 */
  background-blend-mode: overlay;
}

.card-theme-0 { 
  background: linear-gradient(135deg, rgba(232, 255, 243, 0.9) 0%, rgba(249, 255, 251, 0.9) 100%);
  border: 1px solid #d4f5e1;
}
.card-theme-1 { 
  background: linear-gradient(135deg, rgba(255, 232, 240, 0.9) 0%, rgba(255, 240, 247, 0.9) 100%);
  border: 1px solid #f8d7e3;
}
.card-theme-2 { 
  background: linear-gradient(135deg, rgba(232, 244, 255, 0.9) 0%, rgba(240, 248, 255, 0.9) 100%);
  border: 1px solid #d7e9ff;
}
.card-theme-3 { 
  background: linear-gradient(135deg, rgba(255, 248, 232, 0.9) 0%, rgba(255, 251, 230, 0.9) 100%);
  border: 1px solid #f7e9c8;
}

.course-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.06);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.course-title {
  font-size: 20px;
  color: #2c3e50;
  margin: 0;
  font-weight: 600;
}

.card-body {
  margin-bottom: 20px;
}
.info-row {
  display: flex;
  margin-bottom: 12px;
  align-items: center;
}
.info-label {
  font-size: 14px;
  color: #6b7c93;
  font-weight: 500;
  min-width: 80px;
}
.info-value {
  font-size: 14px;
  color: #4a5568;
  flex: 1;
}

.card-actions {
  display: flex;
  gap: 12px;
}
.btn-task {
  flex: 1;
  padding: 12px;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s ease;
  font-weight: 500;
  background: white;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.04);
}
.card-theme-0 .btn-task { color: #55bb7a; }
.card-theme-1 .btn-task { color: #e87ea1; }
.card-theme-2 .btn-task { color: #66a8ff; }
.card-theme-3 .btn-task { color: #f5c956; }
.btn-task:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.08);
}
.btn-task:disabled {
  background: #f5f5f5;
  color: #999;
  cursor: not-allowed;
}

.btn-cancel {
  flex: 1;
  padding: 12px;
  background: #fff5f7;
  color: #ff7a9c;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s ease;
  font-weight: 500;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.04);
}
.btn-cancel:hover {
  background: #ffe8ed;
  transform: translateY(-2px);
  box-shadow: 0 4px 10px rgba(255, 122, 156, 0.1);
}
.btn-cancel:disabled {
  background: #f5f5f5;
  color: #999;
  cursor: not-allowed;
}
</style>