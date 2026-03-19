<!-- eslint-disable vue/multi-word-component-names -->
<template>
  <div class="courses-page">
    <div class="content">
      <h2>课程列表</h2>

      <!-- 加载中提示 -->
      <div v-if="loading" class="empty-tip">
        正在加载课程数据...
      </div>

      <!-- 无课程提示 -->
      <div v-else-if="courses.length === 0" class="empty-tip">
        暂无课程数据，请先添加课程！
      </div>

      <!-- 课程列表：渲染去重后的课程 -->
      <div v-else class="course-grid">
        <div v-for="(course, index) in uniqueCourses" :key="course.id" class="course-card" :class="`card-theme-${index % 4}`">
          <!-- 核心：课程封面展示（仅对坐下/握手向下偏移60%） -->
          <img 
            :src="getCourseCover(course.name)" 
            alt="课程封面"
            :class="['course-cover', course.name === '坐下训练' || course.name === '握手训练' ? 'cover-center' : '']"
          >
          
          <!-- 卡片头部：紧凑标题区 -->
          <div class="card-header">
            <h3>{{ course.name }}</h3>
            <span class="card-badge">课程</span>
          </div>
          
          <!-- 卡片内容：紧凑信息行 -->
          <div class="card-body">
            <div class="info-row">
              <span class="info-label">训练师：</span>
              <span class="info-value">{{ course.trainer || '暂无' }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">描述：</span>
              <span class="info-value">{{ course.description || '暂无' }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">难度：</span>
              <span class="info-value">{{ getDifficultyText(course.difficulty) }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">评分：</span>
              <span class="info-value rating-stars">
                <span v-for="star in Math.round(course.avgScore || 0)" :key="star" class="star filled">★</span>
                <span v-for="star in 5 - Math.round(course.avgScore || 0)" :key="star" class="star">☆</span>
              </span>
            </div>
          </div>

          <!-- 操作按钮 -->
          <div class="btn-group">
            <button @click="showPetList(course.id)" class="btn btn-primary">
              报名
            </button>
            <button @click="goToTasks(course.id)" class="btn btn-secondary">
              任务
            </button>
          </div>

          <!-- 评分输入 -->
          <div class="rating-input-section">
            <div class="rating-title">课程评分</div>
            <div class="rating-input-wrap">
              <input
                v-model="courseRatings[course.id].comment"
                placeholder="评价内容"
                class="input-comment"
              />
              <input
                v-model.number="courseRatings[course.id].score"
                type="number"
                placeholder="1-5"
                min="1"
                max="5"
                class="input-score"
              />
              <button @click="submitRating(course.id)" class="btn btn-success btn-sm">
                提交
              </button>
            </div>
          </div>

          <!-- 评价列表 -->
          <div v-if="ratingLists[course.id] && ratingLists[course.id].length > 0" class="rating-list-section">
            <div class="rating-title">评价</div>
            <div v-for="(rating, idx) in ratingLists[course.id]" :key="idx" class="rating-item">
              <div class="rating-stars">
                <span v-for="star in rating.score" :key="star" class="star filled">★</span>
                <span v-for="star in 5 - rating.score" :key="star" class="star">☆</span>
              </div>
              <div class="rating-comment">{{ rating.comment }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 宠物选择弹窗 -->
    <div v-if="showModal" class="modal-overlay" @click="hideModal">
      <div class="modal-content" @click.stop>
        <h3>选择要报名的宠物</h3>

        <div v-if="petLoading" class="empty-tip">
          正在加载宠物列表...
        </div>

        <div v-else-if="pets.length === 0" class="empty-tip">
          暂无宠物，请先添加宠物！
        </div>

        <div v-else class="pet-list">
          <div v-for="pet in pets" :key="pet.id" class="pet-item">
            <p>{{ pet.name }} (Lv{{ pet.level || 1 }})</p>
            <button @click="signUpForCourse(courseId, pet.id)" class="btn btn-success btn-sm">
              确认
            </button>
          </div>
        </div>

        <button @click="hideModal" class="btn btn-danger btn-sm modal-close-btn" v-if="pets.length > 0">
          取消
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  data() {
    return {
      courses: [],
      pets: [],
      showModal: false,
      courseId: null,
      loading: false,
      petLoading: false,
      courseRatings: {},
      ratingLists: {}
    }
  },
  computed: {
    uniqueCourses() {
      const nameSet = new Set()
      return this.courses.filter(course => {
        if (!nameSet.has(course.name)) {
          nameSet.add(course.name)
          return true
        }
        return false
      })
    }
  },
  mounted() {
    this.loadCourses()
    this.loadPets()
  },
  methods: {
    getCourseCover(courseName) {
      const coverMap = {
        '坐下训练': 'http://localhost:8080/uploads/41.jpg',
        '握手训练': 'http://localhost:8080/uploads/42.jpg',
        '接飞盘': 'http://localhost:8080/uploads/43.jpg',
        '障碍赛': 'http://localhost:8080/uploads/44.jpg'
      }
      return coverMap[courseName] || '/default-cover.jpg'
    },
    getDifficultyText(difficulty) {
      const diffMap = {
        1: '简单',
        2: '普通',
        3: '中等',
        4: '困难'
      }
      return diffMap[difficulty] || '普通'
    },
    loadCourses() {
      this.loading = true
      axios.get('http://localhost:8080/courses', {
        timeout: 5000,
        withCredentials: true
      })
        .then((response) => {
          this.courses = Array.isArray(response.data) ? response.data : []
          if (this.courses.length === 0) {
            this.courses = [
              { id: 1, name: '坐下训练', trainer: '训练师A', description: '训练宠物坐下', difficulty: 1, avgScore: 4.5 },
              { id: 2, name: '握手训练', trainer: '训练师B', description: '训练宠物握手', difficulty: 2, avgScore: 4.8 },
              { id: 3, name: '接飞盘', trainer: '训练师C', description: '训练宠物接飞盘', difficulty: 3, avgScore: 4.2 },
              { id: 4, name: '障碍赛', trainer: '训练师D', description: '障碍训练', difficulty: 4, avgScore: 4.0 }
            ]
          }
          this.initRatingData()
        })
        .catch(() => {
          alert('加载课程失败')
          this.courses = [
            { id: 1, name: '坐下训练', trainer: '训练师A', description: '训练宠物坐下', difficulty: 1, avgScore: 4.5 },
            { id: 2, name: '握手训练', trainer: '训练师B', description: '训练宠物握手', difficulty: 2, avgScore: 4.8 },
            { id: 3, name: '接飞盘', trainer: '训练师C', description: '训练宠物接飞盘', difficulty: 3, avgScore: 4.2 },
            { id: 4, name: '障碍赛', trainer: '训练师D', description: '障碍训练', difficulty: 4, avgScore: 4.0 }
          ]
        })
        .finally(() => {
          this.loading = false
        })
    },
    loadPets() {
      this.petLoading = true
      axios.get('http://localhost:8080/pet', {
        timeout: 5000,
        withCredentials: true
      })
        .then((response) => {
          this.pets = Array.isArray(response.data) ? response.data : []
        })
        .catch(() => {
          alert('加载宠物失败')
          this.pets = []
        })
        .finally(() => {
          this.petLoading = false
        })
    },
    showPetList(courseId) {
      this.loadPets()
      this.courseId = courseId
      this.showModal = true
    },
    hideModal() {
      this.showModal = false
      this.courseId = null
    },
    signUpForCourse(courseId, petId) {
      const data = { petId, courseId }
      axios.post('http://localhost:8080/petCourse/select', data, {
        timeout: 5000,
        withCredentials: true
      })
        .then(() => {
          alert('报名成功')
          this.hideModal()
          this.$router.push('/myCourse')
        })
        .catch(() => {
          alert('报名失败')
        })
    },
    goToTasks(courseId) {
      this.$router.push(`/task?courseId=${courseId}`)
    },
    initRatingData() {
      const obj = { ...this.courseRatings }
      this.uniqueCourses.forEach(c => {
        obj[c.id] = { comment: '', score: null }
        this.getRatingList(c.id)
      })
      this.courseRatings = obj
    },
    submitRating(courseId) {
      const d = this.courseRatings[courseId]
      if (!d.score || d.score < 1 || d.score > 5) {
        alert('请输入1-5分')
        return
      }
      if (!d.comment.trim()) {
        alert('请输入评价内容')
        return
      }
      axios.post('http://localhost:8080/rating/add', {
        courseId,
        score: d.score,
        comment: d.comment
      }).then(() => {
        alert('评分成功')
        this.courseRatings[courseId] = { comment: '', score: null }
        this.getRatingList(courseId)
      }).catch(() => {
        alert('提交失败')
      })
    },
    getRatingList(courseId) {
      axios.get(`http://localhost:8080/rating/list/${courseId}`)
        .then(({ data }) => {
          this.ratingLists[courseId] = data || []
        })
        .catch(() => {
          this.ratingLists[courseId] = []
        })
    }
  }
}
</script>

<style scoped>
/* 背景替换为51.jpeg，优化显示效果 */
.courses-page {
  width: 100%;
  padding: 15px 0;
  /* 核心修改：背景图片替换为51.jpeg */
  background: url('http://localhost:8080/uploads/51.jpeg') no-repeat center center fixed;
  background-size: cover;
  /* 保留半透明遮罩确保内容可读性 */
  position: relative;
  min-height: 100vh;
  overflow: hidden;
}

/* 新增：背景遮罩层，确保内容文字清晰可见 */
.courses-page::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(255, 255, 255, 0.85); /* 白色半透明遮罩 */
  z-index: 0;
  pointer-events: none;
}

/* 移除原有渐变光晕动画，保留流动光带效果 */
.courses-page::after {
  content: '';
  position: absolute;
  pointer-events: none;
  width: 60%;
  height: 60%;
  bottom: -25%;
  left: -15%;
  background: radial-gradient(circle, rgba(186, 220, 253, 0.1) 0%, transparent 60%);
  animation: floatSmall 25s linear infinite;
  z-index: 1;
}

/* 流动光带1 */
.wave-1 {
  width: 120%;
  height: 100px;
  background: linear-gradient(90deg, transparent, rgba(165, 215, 255, 0.05), transparent);
  top: 20%;
  left: -10%;
  animation: flow 18s linear infinite;
  position: absolute;
  pointer-events: none;
  z-index: 1;
}

/* 流动光带2 */
.wave-2 {
  width: 130%;
  height: 80px;
  background: linear-gradient(90deg, transparent, rgba(147, 205, 255, 0.06), transparent);
  top: 55%;
  left: -20%;
  animation: flow 24s linear reverse infinite;
  position: absolute;
  pointer-events: none;
  z-index: 1;
}

/* 动画：小光圈浮动 */
@keyframes floatSmall {
  0%   { transform: translate(0, 0) scale(1); }
  50%  { transform: translate(-30px, 20px) scale(1.08); }
  100% { transform: translate(0, 0) scale(1); }
}

/* 动画：横向流动 */
@keyframes flow {
  0%   { transform: translateX(-100%); }
  100% { transform: translateX(100%); }
}

.content {
  width: 95%;
  max-width: 1100px;
  margin: 0 auto;
  position: relative;
  z-index: 2; /* 提高层级确保内容在背景之上 */
}

h2 {
  font-size: 24px;
  margin-bottom: 20px;
  color: #3a4a5c;
  font-weight: 600;
  padding-left: 5px;
  text-shadow: 0 1px 2px rgba(255, 255, 255, 0.6);
}

.course-cover {
  width: 100%;
  height: 140px;
  object-fit: cover;
  border-radius: 15px 15px 0 0;
  margin-bottom: 12px;
}

.cover-center {
  height: 140px;
  object-position: center 60%;
}

.course-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
}

.course-card {
  border-radius: 20px;
  padding: 0 16px 16px;
  margin: 0;
  box-shadow: 
    0 8px 20px rgba(0,0,0,0.04),
    0 2px 6px rgba(0,0,0,0.02),
    inset 0 -2px 10px rgba(255,255,255,0.9),
    inset 0 2px 2px rgba(255,255,255,1);
  transition: all 0.4s ease;
  position: relative;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(8px);
}

.card-theme-0 {
  background: linear-gradient(145deg, #e8f5f2 0%, rgba(255,255,255,0.9) 100%);
  border: 1px solid #d0e8e0;
}
.card-theme-1 {
  background: linear-gradient(145deg, #f5e8f0 0%, rgba(255,255,255,0.9) 100%);
  border: 1px solid #e8d7e3;
}
.card-theme-2 {
  background: linear-gradient(145deg, #e8edf5 0%, rgba(255,255,255,0.9) 100%);
  border: 1px solid #d7dde9;
}
.card-theme-3 {
  background: linear-gradient(145deg, #f5f0e8 0%, rgba(255,255,255,0.9) 100%);
  border: 1px solid #e8e0d0;
}

.course-card:hover {
  transform: translateY(-5px) scale(1.01);
  box-shadow: 
    0 15px 30px rgba(0,0,0,0.06),
    0 5px 10px rgba(0,0,0,0.03),
    inset 0 -2px 10px rgba(255,255,255,0.9),
    inset 0 2px 2px rgba(255,255,255,1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  padding-bottom: 6px;
  border-bottom: 1px solid rgba(0,0,0,0.04);
}

.card-header h3 {
  margin: 0;
  font-size: 17px;
  color: #3a4a5c;
  font-weight: 600;
}

.card-badge {
  font-size: 11px;
  padding: 2px 7px;
  border-radius: 20px;
  background: rgba(80, 160, 120, 0.08);
  color: #50a078;
  font-weight: 500;
}

.card-body {
  margin-bottom: 10px;
}

.info-row {
  display: flex;
  margin-bottom: 5px;
  font-size: 13px;
  line-height: 1.3;
}

.info-label {
  color: #6a7888;
  font-weight: 500;
  min-width: 45px;
}

.info-value {
  color: #3a4a5c;
  flex: 1;
  word-break: break-all;
}

.rating-stars {
  color: #e6a650;
  font-size: 12px;
}

.star.filled {
  color: #e6a650;
}

.star {
  color: #e8eaf0;
}

.btn-group {
  display: flex;
  gap: 7px;
  margin-bottom: 12px;
}

.btn {
  padding: 7px 11px;
  border: none;
  border-radius: 12px;
  color: #fff;
  cursor: pointer;
  font-size: 12px;
  font-weight: 500;
  transition: all 0.3s ease;
  box-shadow: 0 4px 8px rgba(0,0,0,0.08);
}

.btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 12px rgba(0,0,0,0.12);
}

.btn-sm {
  padding: 5px 9px;
  font-size: 11px;
}

.btn-primary {
  background: linear-gradient(145deg, #4a80b4 0%, #6a98c8 100%);
}

.btn-secondary {
  background: linear-gradient(145deg, #8a7a9c 0%, #9e8eb4 100%);
}

.btn-success {
  background: linear-gradient(145deg, #50a078 0%, #68b88c 100%);
}

.btn-danger {
  background: linear-gradient(145deg, #b46a6a 0%, #c88080 100%);
}

.rating-input-section {
  margin-top: 0;
  padding: 10px;
  background: rgba(255,255,255,0.6);
  border-radius: 15px;
  box-shadow: inset 0 2px 5px rgba(0,0,0,0.02);
  margin-bottom: 8px;
  backdrop-filter: blur(4px);
}

.rating-title {
  font-size: 12px;
  font-weight: 600;
  margin-bottom: 7px;
  color: #3a4a5c;
}

.rating-input-wrap {
  display: flex;
  gap: 5px;
  align-items: center;
}

.input-comment, .input-score {
  background: rgba(255,255,255,0.8);
  border: 1px solid #d8e0e8;
  border-radius: 10px;
  padding: 6px 9px;
  font-size: 12px;
  transition: all 0.3s ease;
}

.input-comment:focus, .input-score:focus {
  outline: none;
  border-color: #6a98c8;
  box-shadow: 0 0 0 2px rgba(106, 152, 200, 0.1);
  transform: translateY(-1px);
}

.rating-list-section {
  margin-top: 0;
}

.rating-item {
  padding: 7px 9px;
  background: rgba(255,255,255,0.6);
  border-radius: 12px;
  margin-bottom: 5px;
  box-shadow: inset 0 2px 5px rgba(0,0,0,0.02);
  backdrop-filter: blur(4px);
}

.rating-comment {
  font-size: 11px;
  color: #6a7888;
  line-height: 1.2;
}

.empty-tip {
  text-align: center;
  padding: 25px 0;
  color: #6a7888;
  font-size: 14px;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0,0,0,0.3);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 999;
  backdrop-filter: blur(4px);
}

.modal-content {
  background: rgba(255,255,255,0.9);
  padding: 20px;
  border-radius: 25px;
  width: 90%;
  max-width: 380px;
  box-shadow: 
    0 15px 35px rgba(0,0,0,0.1),
    inset 0 -2px 10px rgba(255,255,255,0.9),
    inset 0 2px 2px rgba(255,255,255,1);
  border: 1px solid #e8edf5;
  backdrop-filter: blur(8px);
}

.modal-content h3 {
  margin-top: 0;
  color: #3a4a5c;
  font-weight: 600;
  font-size: 18px;
  text-align: center;
  margin-bottom: 15px;
}

.pet-list {
  max-height: 300px;
  overflow-y: auto;
  margin-bottom: 15px;
}

.pet-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 10px;
  border-bottom: 1px solid #e8edf5;
  border-radius: 10px;
  margin-bottom: 5px;
}

.pet-item p {
  margin: 0;
  color: #3a4a5c;
  font-weight: 500;
  font-size: 14px;
}

.modal-close-btn {
  display: block;
  margin: 0 auto;
  width: 80%;
}
</style>

<!-- 加一层动画容器，不影响布局 -->
<div class="wave-1"></div>
<div class="wave-2"></div>