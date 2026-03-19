<!-- eslint-disable vue/multi-word-component-names -->
<template>
  <div class="task-page">
    <div class="content">
      <!-- 顶部标题装饰 -->
      <div class="page-header">
        <h2 class="page-title">🎯 训练任务</h2>
        <div class="title-divider"></div>
      </div>
      
      <!-- 整体加载中提示 -->
      <div v-if="loading" class="empty-tip">
        正在加载任务数据...
      </div>

      <div v-else>
        <!-- 课程+宠物信息展示（高级样式） -->
        <div v-if="courseName || petName" class="course-pet-info">
          <span class="info-icon">📚</span>
          <span class="info-text">当前课程：{{ courseName || '未知课程' }}</span>
          <span class="divider">|</span>
          <span class="info-icon">🐾</span>
          <span class="info-text">训练宠物：{{ petName || `宠物${petId}` }} (Lv{{ petLevel }}，经验：{{ petExp }} EXP)</span>
        </div>

        <!-- 无任务提示 -->
        <div v-if="tasks.length === 0" class="empty-tip">
          暂无该课程的训练任务
        </div>

        <!-- 任务列表（立体卡片+动画） -->
        <div v-else class="task-list">
          <div 
            v-for="(task, index) in tasks" 
            :key="task.id"
            class="task-card"
            :style="{ animationDelay: `${index * 0.1}s` }"
          >
            <div class="task-icon">
              <span v-if="task.exp <= 5">💚</span>
              <span v-else-if="task.exp <= 10">🧡</span>
              <span v-else>❤️</span>
            </div>
            <div class="task-content">
              <h3 class="task-name">{{ task.name }}</h3>
              <div class="task-reward">
                <span class="reward-icon">🎁</span>
                <span class="reward-text">{{ task.exp }} EXP</span>
              </div>
            </div>
            <button 
              @click="completeTask(task)" 
              class="complete-btn"
              :disabled="completeLoading"
            >
              {{ completeLoading ? '处理中...' : '完成任务' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'
import { eventBus } from '@/utils/eventBus'

axios.defaults.withCredentials = true;
axios.defaults.timeout = 5000;
const baseUrl = 'http://localhost:8080';

export default {
  data() {
    return {
      tasks: [],
      courseId: null,
      petId: null,       
      courseName: '',
      petName: '',       
      petLevel: 1,       
      petExp: 0,         
      loading: false,       
      completeLoading: false 
    }
  },
  mounted() {
    this.courseId = parseInt(this.$route.query.courseId);
    this.petId = parseInt(this.$route.query.petId);
    
    if (!this.courseId || !this.petId) {
      alert('缺少必要参数，请从“我的课程”页面选择宠物后进入！');
      this.$router.push('/myCourse'); 
      return;
    }

    Promise.all([
      this.loadCourseName(),
      this.loadPetInfo(),
      this.loadTasks()
    ]).catch(err => {
      console.error('数据加载异常：', err);
      let errorMsg = '数据加载异常！';
      if (err.message) errorMsg += err.message;
      alert(errorMsg);
    });
  },
  methods: {
    loadPetInfo() {
      return axios.get(`${baseUrl}/pet/${this.petId}`)
        .then(res => {
          if (res.data) {
            this.petName = res.data.name || `宠物${this.petId}`;
            this.petLevel = res.data.level || 1;
            this.petExp = res.data.exp || 0;
          } else {
            this.petName = `宠物${this.petId}`;
          }
        })
        .catch(err => {
          console.error('加载宠物信息失败：', err);
          let errorMsg = `加载宠物（ID:${this.petId}）信息失败！`;
          if (err.code === 'ECONNABORTED') {
            errorMsg += '请求超时，请检查后端服务是否启动';
          } else if (err.response) {
            errorMsg += `错误码：${err.response.status}，${err.response.statusText}`;
          } else if (err.request) {
            errorMsg += '网络异常，无法连接后端服务';
          } else if (err.message) {
            errorMsg += err.message;
          } else {
            errorMsg += '未知错误';
          }
          alert(errorMsg);
          this.petName = `宠物${this.petId}`;
          this.petLevel = 1;
          this.petExp = 0;
        });
    },

    loadCourseName() {
      return axios.get(`${baseUrl}/courses/${this.courseId}`)
        .then(res => {
          this.courseName = res.data?.name || '未知课程';
        })
        .catch(err => {
          console.error('加载课程名称失败，尝试从列表获取：', err);
          let errorMsg = `加载课程（ID:${this.courseId}）名称失败！`;
          if (err.message) errorMsg += err.message;
          console.warn(errorMsg);
          return this.getCourseNameFromList();
        });
    },

    getCourseNameFromList() {
      return axios.get(`${baseUrl}/courses`)
        .then(res => {
          const courseList = Array.isArray(res.data) ? res.data : [];
          const currentCourse = courseList.find(c => c.id === this.courseId);
          this.courseName = currentCourse?.name || '未知课程';
        })
        .catch(err => {
          console.error('获取课程列表失败：', err);
          let errorMsg = '获取课程列表失败！';
          if (err.message) errorMsg += err.message;
          console.warn(errorMsg);
          this.courseName = '未知课程';
        });
    },

    loadTasks() {
      this.loading = true;
      return axios.get(`${baseUrl}/task/list`, {
        params: { courseId: this.courseId },
        headers: {
          'Cache-Control': 'no-cache',
          'Pragma': 'no-cache',
          'Expires': '0'
        }
      })
      .then(res => {
        console.log(`课程${this.courseId}的任务：`, res.data);
        this.tasks = Array.isArray(res.data) ? res.data : [];
      })
      .catch(err => {
        console.error('加载训练任务失败：', err);
        let errorMsg = '加载训练任务失败！';
        if (err.code === 'ECONNABORTED') {
          errorMsg += '请求超时，请检查后端服务是否启动';
        } else if (err.response) {
          errorMsg += `错误码：${err.response.status}，${err.response.statusText}`;
        } else if (err.request) {
          errorMsg += '网络异常，无法连接后端服务';
        } else if (err.message) {
          errorMsg += err.message;
        } else {
          errorMsg += '未知错误';
        }
        alert(errorMsg);
        this.tasks = [];
      })
      .finally(() => {
        this.loading = false;
      });
    },

    refreshPetGrowthPage() {
      window.dispatchEvent(new CustomEvent('petExpUpdated', {
        detail: { petName: this.petName }
      }));

      axios.get(`${baseUrl}/growth/${this.petName}`)
        .then(res => {
          console.log(`刷新${this.petName}成长数据：`, res.data);
        })
        .catch(err => {
          console.error('刷新成长数据失败：', err);
          let errorMsg = `刷新${this.petName}成长数据失败！`;
          if (err.message) errorMsg += err.message;
          console.warn(errorMsg);
        });
    },

    completeTask(task) {
      if (this.completeLoading || !task.id) return;
      
      this.completeLoading = true;
      axios.post(`${baseUrl}/task/complete`, {
        taskId: task.id,
        courseId: this.courseId,
        petId: this.petId
      })
      .then(res => {
        alert(
          typeof res.data === 'string'
            ? res.data
            : (res.data?.message || `✅ 任务完成：${task.name}\n🎁 获得经验：${task.exp} EXP`)
        );
        
        this.loadPetInfo(); 
        this.refreshPetGrowthPage();
        eventBus.emit('taskCompleted', { petId: this.petId });
        console.log(`【Task】发送任务完成事件，petId=${this.petId}`);
      })
      .catch(err => {
        console.error('完成任务失败：', err);
        let errorMsg = `完成任务【${task.name}】失败！`;
        
        if (err.response && err.response.data) {
          const errData = err.response.data;
          if (typeof errData === 'object') {
            errorMsg += `\n服务器错误（${errData.status || 500}）：${errData.error || '内部服务异常'}`;
            if (errData.message) errorMsg += `\n原因：${errData.message}`;
          } else {
            errorMsg += `\n${errData}`;
          }
        } else if (err.code === 'ECONNABORTED') {
          errorMsg += '\n请求超时，请检查后端服务是否启动';
        } else if (err.request) {
          errorMsg += '\n网络异常，无法连接后端服务';
        } else if (err.message) {
          errorMsg += `\n${err.message}`;
        } else {
          errorMsg += '\n未知错误';
        }
        
        alert(errorMsg);
      })
      .finally(() => {
        this.completeLoading = false;
      });
    }
  }
}
</script>

<style scoped>
/* 全局样式重置 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
  font-family: 'Inter', 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

/* 页面背景：渐变+柔和质感 */
.task-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #f0f8fb 0%, #e8f4f8 50%, #f5fafe 100%);
  padding: 30px 20px;
}

/* 内容容器：立体+圆角 */
.content {
  width: 95%;
  max-width: 700px;
  margin: 0 auto;
  background: #ffffff;
  padding: 28px;
  border-radius: 20px;
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.08);
  position: relative;
  overflow: hidden;
}

/* 顶部标题装饰 */
.page-header {
  margin-bottom: 25px;
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  color: #2d3748;
  position: relative;
  padding-bottom: 12px;
  display: inline-block;
}

.title-divider {
  height: 3px;
  width: 100%;
  background: linear-gradient(90deg, #42b983, #4299e1);
  border-radius: 3px;
  margin-top: 8px;
}

/* 课程宠物信息栏：渐变+图标+立体 */
.course-pet-info {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 15px 20px;
  margin-bottom: 25px;
  background: linear-gradient(135deg, #f7fafc 0%, #edf2f7 100%);
  border-radius: 12px;
  border-left: 5px solid #42b983;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.05);
}

.info-icon {
  font-size: 20px;
  filter: drop-shadow(0 2px 3px rgba(0, 0, 0, 0.1));
}

.info-text {
  font-size: 16px;
  color: #4a5568;
  font-weight: 500;
}

.divider {
  color: #cbd5e0;
  font-weight: 300;
}

/* 空状态提示 */
.empty-tip {
  color: #718096;
  text-align: center;
  padding: 60px 0;
  font-size: 16px;
}

/* 任务列表：统一间距 */
.task-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 任务卡片：立体+渐变+动画 */
.task-card {
  display: flex;
  align-items: center;
  gap: 18px;
  padding: 20px;
  background: #ffffff;
  border-radius: 16px;
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.06);
  border: 1px solid #e2e8f0;
  transition: all 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);
  animation: fadeInUp 0.6s ease forwards;
  position: relative;
  overflow: hidden;
}

/* 卡片hover效果：立体上浮+阴影增强 */
.task-card:hover {
  transform: translateY(-6px) scale(1.02);
  box-shadow: 0 12px 28px rgba(0, 0, 0, 0.12);
  border-color: #42b983;
}

/* 任务图标：根据经验值变色 */
.task-icon {
  font-size: 32px;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.15));
}

/* 任务内容 */
.task-content {
  flex: 1;
}

.task-name {
  font-size: 18px;
  font-weight: 600;
  color: #2d3748;
  margin-bottom: 8px;
}

.task-reward {
  display: flex;
  align-items: center;
  gap: 6px;
}

.reward-icon {
  font-size: 16px;
}

.reward-text {
  font-size: 14px;
  color: #38a169;
  font-weight: 600;
}

/* 完成任务按钮：渐变+立体+动画 */
.complete-btn {
  padding: 12px 24px;
  background: linear-gradient(135deg, #52c41a 0%, #389e0d 100%);
  color: #ffffff;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  font-size: 15px;
  font-weight: 600;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  white-space: nowrap;
}

.complete-btn:hover:not(:disabled) {
  transform: translateY(-3px);
  box-shadow: 0 6px 12px rgba(82, 196, 26, 0.3);
}

.complete-btn:disabled {
  background: #cbd5e0;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

/* 入场动画 */
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 响应式适配 */
@media (max-width: 768px) {
  .content {
    width: 92%;
    padding: 20px;
  }
  
  .page-title {
    font-size: 24px;
  }
  
  .task-card {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .complete-btn {
    width: 100%;
    text-align: center;
  }
  
  .course-pet-info {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
}
</style>