<!-- eslint-disable vue/multi-word-component-names -->
<template>
  <div class="recommend-page">
    <div class="bg-overlay"></div>
    
    <div class="main-card">
      <div class="header-section">
        <div class="header-icon">🐾</div>
        <h2 class="title">宠物课程推荐</h2>
        <p class="page-desc">根据宠物类型和年龄精准推荐专属训练课程</p>
      </div>

      <div class="filter-section">
        <div class="select-area">
          <select v-model="petType" class="pet-type-select">
            <option value="狗">🐶 狗狗课程</option>
            <option value="猫">🐱 猫咪课程</option>
          </select>

          <input 
            v-model.number="petAge" 
            type="number" 
            min="0" 
            max="20" 
            placeholder="请输入宠物年龄（岁）" 
            class="age-input"
          />

          <button @click="load" class="recommend-btn" :disabled="loading">
            <span v-if="!loading" class="btn-text">🔍 立即推荐</span>
            <span v-if="loading" class="loading-text">加载中...</span>
            <span class="loading-spinner" v-if="loading"></span>
          </button>
        </div>
      </div>

      <div class="content-area">
        <div v-if="courses.length === 0 && !loading" class="empty-section">
          <div class="empty-icon">🐾</div>
          <div class="empty-text">暂无匹配课程</div>
          <div class="empty-subtext">请更换宠物类型或年龄试试～</div>
        </div>

        <div v-else-if="courses.length > 0" class="courses-section">
          <div class="section-title">
            <h3>🐾 精选课程 ({{ courses.length }})</h3>
          </div>
          
          <div class="course-grid">
            <div 
              v-for="course in courses" 
              :key="course.id" 
              class="course-card"
              :style="{ '--card-gradient': getCourseGradient(course.difficulty) }"
            >
              <div class="course-header">
                <h4 class="course-name">{{ course.name }}</h4>
                <div class="course-badges">
                  <span class="pet-tag">{{ course.petType }}专属</span>
                  <span class="hot-tag" v-if="isHotCourse(course.name)">🔥 热门</span>
                </div>
              </div>
              
              <div class="course-info">
                <p class="course-desc">{{ course.description || '暂无课程描述' }}</p>
                
                <div class="course-meta">
                  <div class="meta-row">
                    <div class="meta-item age-item">
                      <div class="meta-label">适合年龄：</div>
                      <div class="meta-value">{{ course.minAge }}~{{ course.maxAge }}岁</div>
                    </div>
                    
                    <div class="meta-item trainer-item">
                      <div class="meta-label">专属训练师：</div>
                      <div class="meta-value">{{ course.trainer || '暂无' }}</div>
                    </div>
                  </div>
                  
                  <div class="meta-row">
                    <div class="meta-item level-item">
                      <div class="meta-label">课程难度：</div>
                      <div class="meta-value difficulty-{{ course.difficulty }}">{{ getDifficultyText(course.difficulty) }}</div>
                    </div>
                    
                    <div class="meta-item rating-item">
                      <div class="meta-label">用户评分：</div>
                      <div class="meta-value">
                        <span v-for="star in Math.round(course.avgScore || 0)" :key="star" class="star filled">★</span>
                        <span v-for="star in 5 - Math.round(course.avgScore || 0)" :key="star" class="star">☆</span>
                        <span class="score-num">{{ course.avgScore }}</span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from "axios";

export default {
  name: 'PetCourseRecommend',
  data() {
    return {
      petType: "狗",
      petAge: 3,
      courses: [],
      loading: false,
      hotCourseNames: []
    };
  },
  methods: {
    getCourseGradient(difficulty) {
      const gradients = {
        1: 'linear-gradient(135deg, rgba(230, 250, 230, 0.98) 0%, rgba(200, 240, 200, 0.98) 100%)',
        2: 'linear-gradient(135deg, rgba(230, 245, 255, 0.98) 0%, rgba(200, 230, 250, 0.98) 100%)',
        3: 'linear-gradient(135deg, rgba(245, 235, 255, 0.98) 0%, rgba(225, 210, 245, 0.98) 100%)',
        4: 'linear-gradient(135deg, rgba(255, 235, 235, 0.98) 0%, rgba(250, 210, 210, 0.98) 100%)'
      };
      return gradients[difficulty] || gradients[2];
    },
    isHotCourse(courseName) {
      return this.hotCourseNames.includes(courseName);
    },
    getDifficultyText(difficulty) {
      const diffMap = {
        1: '简单',
        2: '普通',
        3: '中等',
        4: '困难'
      };
      return diffMap[difficulty] || '普通';
    },
    async getHotCourses() {
      try {
        const res = await axios.get("http://localhost:8080/courses/hot-top3", {
          timeout: 5000,
          withCredentials: true
        });
        this.hotCourseNames = res.data.map(item => item.course_name || item.courseName || '');
      } catch (error) {
        console.error('获取热门课程失败：', error);
        this.hotCourseNames = [];
      }
    },
    async load() {
      if (!this.petAge || this.petAge < 0 || this.petAge > 20) {
        alert('请输入有效的宠物年龄（岁）');
        return;
      }

      try {
        this.loading = true;
        const res = await axios.get(`http://localhost:8080/courses/recommend`, {
          params: { petType: this.petType, petAge: this.petAge },
          timeout: 5000,
          withCredentials: true
        });
        this.courses = res.data || [];
        
        if (this.courses.length === 0) {
          this.courses = [
            { id: 1, name: '坐下训练', trainer: '训练师A', description: '基础服从训练，适合新手宠物学习坐下指令', minAge: 0, maxAge: 5, difficulty: 1, avgScore: 4.5, petType: this.petType },
            { id: 2, name: '握手训练', trainer: '训练师B', description: '进阶服从训练，学习握手/击掌指令', minAge: 0, maxAge: 5, difficulty: 2, avgScore: 4.8, petType: this.petType },
            { id: 3, name: '接飞盘', trainer: '训练师C', description: '趣味技能训练，提升宠物反应力', minAge: 1, maxAge: 10, difficulty: 3, avgScore: 4.9, petType: this.petType }
          ];
        }
      } catch (error) {
        console.error('推荐课程加载失败：', error);
        alert('获取推荐课程失败，请检查网络或稍后重试');
        this.courses = [
          { id: 1, name: '坐下训练', trainer: '训练师A', description: '基础服从训练，适合新手宠物学习坐下指令', minAge: 0, maxAge: 5, difficulty: 1, avgScore: 4.5, petType: this.petType },
          { id: 2, name: '握手训练', trainer: '训练师B', description: '进阶服从训练，学习握手/击掌指令', minAge: 0, maxAge: 5, difficulty: 2, avgScore: 4.8, petType: this.petType },
          { id: 3, name: '接飞盘', trainer: '训练师C', description: '趣味技能训练，提升宠物反应力', minAge: 1, maxAge: 10, difficulty: 3, avgScore: 4.9, petType: this.petType }
        ];
      } finally {
        this.loading = false;
      }
    }
  },
  async mounted() {
    await this.getHotCourses();
    await this.load();
  }
};
</script>

<style scoped>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
  font-family: "Microsoft YaHei", "PingFang SC", "Helvetica Neue", sans-serif;
}

.recommend-page {
  height: 100vh;
  width: 100vw;
  overflow: hidden;
  /* 核心修改：添加背景图 */
  background: url('@/assets/51.jpeg') no-repeat center center fixed;
  background-size: cover;
  display: flex;
  justify-content: center;
  align-items: stretch;
  position: relative;
}

/* 新增：背景遮罩层，确保内容可读性 */
.bg-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: 
    linear-gradient(135deg, rgba(255, 255, 255, 0.85) 0%, rgba(249, 251, 255, 0.9) 100%),
    radial-gradient(circle at 10% 20%, rgba(100, 180, 255, 0.03) 0%, transparent 40%),
    radial-gradient(circle at 80% 80%, rgba(255, 180, 200, 0.03) 0%, transparent 40%);
  z-index: 0;
}

.main-card {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.99) 0%, rgba(252, 253, 255, 0.99) 100%);
  border-radius: 0;
  box-shadow: 0 10px 40px rgba(100, 180, 255, 0.05), 0 2px 10px rgba(255, 255, 255, 0.8) inset;
  padding: 20px 25px;
  width: 100%;
  height: 100vh;
  max-width: 1200px;
  max-height: 100vh;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  position: relative;
  display: flex;
  flex-direction: column;
  z-index: 1; /* 确保卡片在遮罩层上方 */
}

.main-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 6px;
  background: linear-gradient(90deg, #81c9ff, #ff99cc, #99ccff, #99e6b3, #ffdd99);
  border-radius: 0;
  box-shadow: 0 2px 10px rgba(100, 180, 255, 0.1);
}

.header-section {
  text-align: center;
  margin-bottom: 15px;
  padding-bottom: 12px;
  border-bottom: 1px solid rgba(100, 180, 255, 0.08);
  flex-shrink: 0;
}

.header-icon {
  font-size: 36px;
  margin-bottom: 8px;
  display: inline-block;
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-6px); }
}

.title {
  font-size: 28px;
  font-weight: 800;
  background: linear-gradient(135deg, #6699ff 0%, #ff6699 100%);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  margin-bottom: 6px;
}

.page-desc {
  color: #557799;
  font-size: 15px;
  line-height: 1.4;
  max-width: 700px;
  margin: 0 auto;
  font-weight: 500;
}

.filter-section {
  margin-bottom: 15px;
  padding: 16px 20px;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.98) 0%, rgba(250, 252, 255, 0.98) 100%);
  border-radius: 20px;
  box-shadow: 0 4px 15px rgba(100, 180, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.9);
  flex-shrink: 0;
}

.select-area {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 15px;
  flex-wrap: wrap;
}

.pet-type-select {
  padding: 12px 22px;
  font-size: 15px;
  border-radius: 50px;
  border: 2px solid #e6f0ff;
  background: rgba(255, 255, 255, 0.98);
  color: #1f2937;
  min-width: 170px;
  appearance: none;
  background-image: url("data:image/svg+xml;charset=UTF-8,%3csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='none' stroke='%236699ff' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3e%3cpolyline points='6 9 12 15 18 9'%3e%3c/polyline%3e%3c/svg%3e");
  background-repeat: no-repeat;
  background-position: right 15px center;
  background-size: 14px;
  padding-right: 45px;
  transition: all 0.3s ease;
}

.pet-type-select:focus {
  outline: none;
  border-color: #6699ff;
  box-shadow: 0 0 0 4px rgba(102, 153, 255, 0.1);
}

.age-input {
  padding: 12px 22px;
  font-size: 15px;
  border-radius: 50px;
  border: 2px solid #e6f0ff;
  background: rgba(255, 255, 255, 0.98);
  color: #1f2937;
  min-width: 170px;
  text-align: center;
}

.age-input::placeholder {
  color: #88aacc;
  text-align: center;
}

.age-input:focus {
  outline: none;
  border-color: #66ccff;
  box-shadow: 0 0 0 4px rgba(102, 204, 255, 0.1);
}

.recommend-btn {
  padding: 12px 32px;
  font-size: 15px;
  background: linear-gradient(135deg, #6699ff 0%, #ff6699 100%);
  color: white;
  border: none;
  border-radius: 50px;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  min-width: 150px;
  font-weight: 600;
}

.recommend-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 153, 255, 0.2);
}

.recommend-btn:disabled {
  background: linear-gradient(135deg, #aabbcc 0%, #ccddee 100%);
  cursor: not-allowed;
  opacity: 0.8;
}

.loading-spinner {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.4);
  border-radius: 50%;
  border-top-color: white;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.content-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.empty-section {
  flex: 1;
  text-align: center;
  padding: 30px 20px;
  color: #475569;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.98) 0%, rgba(250, 252, 255, 0.98) 100%);
  border-radius: 20px;
  box-shadow: 0 4px 15px rgba(100, 180, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.9);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}

.empty-icon {
  font-size: 42px;
  margin-bottom: 12px;
  opacity: 0.85;
  animation: float 4s ease-in-out infinite;
}

.empty-text {
  font-size: 22px;
  font-weight: 700;
  background: linear-gradient(135deg, #66ccff 0%, #66e6aa 100%);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  margin-bottom: 8px;
}

.empty-subtext {
  font-size: 15px;
  color: #557799;
  line-height: 1.5;
}

.courses-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.section-title {
  text-align: center;
  margin-bottom: 15px;
  flex-shrink: 0;
}

.section-title h3 {
  font-size: 20px;
  font-weight: 700;
  background: linear-gradient(135deg, #6699ff 0%, #ff6699 50%, #ffcc66 100%);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}

.course-grid {
  flex: 1;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 18px;
  align-content: start;
  overflow: auto;
  padding: 0 5px;
}

.course-card {
  padding: 20px;
  background: var(--card-gradient);
  border-radius: 20px;
  transition: all 0.3s ease;
  box-shadow: 0 8px 25px rgba(100, 180, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.95);
  position: relative;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  min-height: 320px;
}

.course-card::after {
  content: '';
  position: absolute;
  top: 0;
  right: 0;
  width: 45px;
  height: 45px;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 0 20px 0 60%;
}

.course-card:hover {
  transform: translateY(-4px) scale(1.01);
  box-shadow: 0 10px 25px rgba(100, 180, 255, 0.1);
}

.course-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 14px;
  flex-wrap: wrap;
  gap: 8px;
}

.course-name {
  font-size: 18px;
  font-weight: 700;
  color: #111827;
  line-height: 1.3;
}

.course-badges {
  display: flex;
  gap: 8px;
}

.pet-tag {
  padding: 6px 12px;
  background: rgba(255, 255, 255, 0.95);
  color: #44aaff;
  border-radius: 50px;
  font-size: 12px;
  font-weight: 600;
  white-space: nowrap;
}

.hot-tag {
  padding: 6px 12px;
  background: rgba(255, 255, 255, 0.95);
  color: #ff6666;
  border-radius: 50px;
  font-size: 12px;
  font-weight: 600;
  white-space: nowrap;
}

.course-info {
  font-size: 14px;
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.course-desc {
  color: #374151;
  font-size: 14px;
  line-height: 1.5;
  padding-bottom: 10px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.course-meta {
  display: flex;
  flex-direction: column;
  gap: 6px;
  flex: 1;
  justify-content: center;
}

.meta-row {
  display: flex;
  gap: 6px;
  width: 100%;
}

/* 按钮空白大幅减少 */
.meta-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding: 5px 8px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 12px;
  font-size: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.03);
}

.age-item {
  border-left: 3px solid #44aaff;
}
.trainer-item {
  border-left: 3px solid #9988ff;
}
.level-item {
  border-left: 3px solid #ff66aa;
}
.rating-item {
  border-left: 3px solid #ffcc66;
}

.meta-label {
  color: #557799;
  font-weight: 600;
  font-size: 11px;
  line-height: 1.2;
}

.meta-value {
  color: #1f2937;
  font-weight: 600;
  line-height: 1.2;
  font-size: 12px;
}

.difficulty-1 { color: #22cc88; font-weight: 700; }
.difficulty-2 { color: #44aaff; font-weight: 700; }
.difficulty-3 { color: #9988ff; font-weight: 700; }
.difficulty-4 { color: #ff6666; font-weight: 700; }

.star.filled {
  color: #ffcc33;
  font-size: 12px;
}
.star {
  color: #eef2f8;
  font-size: 12px;
}
.score-num {
  font-size: 11px;
  margin-left: 4px;
  color: #557799;
  font-weight: 700;
}

@media (max-width: 768px) {
  .main-card {
    padding: 15px 20px;
  }
  .select-area {
    flex-direction: column;
    gap: 12px;
  }
  .pet-type-select, .age-input, .recommend-btn {
    width: 100%;
    min-width: unset;
  }
  .course-grid {
    grid-template-columns: 1fr;
  }
  .meta-row {
    flex-direction: column;
    gap: 6px;
  }
}

/* 隐藏滚动条 */
.main-card::-webkit-scrollbar,
.content-area::-webkit-scrollbar,
.course-grid::-webkit-scrollbar {
  display: none;
}
</style>