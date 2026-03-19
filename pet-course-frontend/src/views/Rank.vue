<!-- eslint-disable vue/multi-word-component-names -->
<template>
  <div class="rank-page">
    <div class="content">
      <!-- 顶部标题装饰 -->
      <div class="page-header">
        <h2 class="page-title">🏆 宠物训练排行榜</h2>
        <div class="title-divider"></div>
      </div>

      <!-- 切换标签（美化版） -->
      <div class="tab-header">
        <div 
          class="tab-item"
          :class="{ active: activeTab === 'pet' }"
          @click="switchTab('pet')"
        >
          🐾 宠物排行榜（按经验值）
        </div>
        <div 
          class="tab-item"
          :class="{ active: activeTab === 'course' }"
          @click="switchTab('course')"
        >
          📚 课程排行榜（按评分）
        </div>
      </div>

      <!-- 宠物排行榜内容（统一尺寸布局） -->
      <div v-if="activeTab === 'pet'" class="rank-container">
        <div v-if="petLoading" class="empty-tip">
          正在加载宠物排行榜数据...
        </div>
        <div v-else-if="pets.length === 0" class="empty-tip">
          暂无宠物数据，快去添加宠物吧～
        </div>
        <div v-else class="rank-grid unified-grid">
          <!-- 宠物前四名一行展示（统一尺寸） -->
          <div class="top-four">
            <div v-if="pets[0]" class="rank-card first unified-card" @click="showPetDetail(pets[0])">
              <div class="rank-medal">🥇</div>
              <div class="card-content">
                <h3 class="pet-name">{{ pets[0].name }}</h3>
                <div class="pet-info">
                  <span class="level">Lv{{ pets[0].level || 1 }}</span>
                  <span class="exp">{{ pets[0].exp || 0 }} EXP</span>
                </div>
                <p class="breed">{{ pets[0].breed || '未知' }}</p>
              </div>
            </div>

            <div v-if="pets[1]" class="rank-card second unified-card" @click="showPetDetail(pets[1])">
              <div class="rank-medal">🥈</div>
              <div class="card-content">
                <h3 class="pet-name">{{ pets[1].name }}</h3>
                <div class="pet-info">
                  <span class="level">Lv{{ pets[1].level || 1 }}</span>
                  <span class="exp">{{ pets[1].exp || 0 }} EXP</span>
                </div>
                <p class="breed">{{ pets[1].breed || '未知' }}</p>
              </div>
            </div>

            <div v-if="pets[2]" class="rank-card third unified-card" @click="showPetDetail(pets[2])">
              <div class="rank-medal">🥉</div>
              <div class="card-content">
                <h3 class="pet-name">{{ pets[2].name }}</h3>
                <div class="pet-info">
                  <span class="level">Lv{{ pets[2].level || 1 }}</span>
                  <span class="exp">{{ pets[2].exp || 0 }} EXP</span>
                </div>
                <p class="breed">{{ pets[2].breed || '未知' }}</p>
              </div>
            </div>

            <div v-if="pets[3]" class="rank-card fourth unified-card" @click="showPetDetail(pets[3])">
              <div class="rank-num">4</div>
              <div class="card-content">
                <h3 class="pet-name">{{ pets[3].name }}</h3>
                <div class="pet-info">
                  <span class="level">Lv{{ pets[3].level || 1 }}</span>
                  <span class="exp">{{ pets[3].exp || 0 }} EXP</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 第五名及以后横向排列 -->
          <div class="other-ranks unified-other">
            <div 
              v-for="(pet, index) in pets.slice(4)" 
              :key="pet.id || index"
              class="rank-card other unified-card"
              @click="showPetDetail(pet)"
            >
              <div class="rank-num">{{ index + 5 }}</div>
              <div class="card-content">
                <h3 class="pet-name">{{ pet.name }}</h3>
                <div class="pet-info">
                  <span class="level">Lv{{ pet.level || 1 }}</span>
                  <span class="exp">{{ pet.exp || 0 }} EXP</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 课程排行榜内容（统一尺寸布局） -->
      <div v-if="activeTab === 'course'" class="rank-container">
        <div v-if="courseLoading" class="empty-tip">
          正在加载课程排行榜数据...
        </div>
        <div v-else-if="courses.length === 0" class="empty-tip">
          暂无课程数据，快去添加课程吧～
        </div>
        <div v-else class="rank-grid unified-grid">
          <!-- 课程前四名一行展示（统一尺寸） -->
          <div class="top-four">
            <div v-if="courses[0]" class="rank-card first unified-card" @click="showCourseDetail(courses[0])">
              <div class="rank-medal">🥇</div>
              <div class="card-content">
                <h3 class="course-name">{{ courses[0].name }}</h3>
                <div class="course-info">
                  <span class="trainer">{{ courses[0].trainer || '未知' }}</span>
                  <span class="score">{{ (courses[0].avgScore || 0).toFixed(1) }} ⭐</span>
                </div>
              </div>
            </div>

            <div v-if="courses[1]" class="rank-card second unified-card" @click="showCourseDetail(courses[1])">
              <div class="rank-medal">🥈</div>
              <div class="card-content">
                <h3 class="course-name">{{ courses[1].name }}</h3>
                <div class="course-info">
                  <span class="trainer">{{ courses[1].trainer || '未知' }}</span>
                  <span class="score">{{ (courses[1].avgScore || 0).toFixed(1) }} ⭐</span>
                </div>
              </div>
            </div>

            <div v-if="courses[2]" class="rank-card third unified-card" @click="showCourseDetail(courses[2])">
              <div class="rank-medal">🥉</div>
              <div class="card-content">
                <h3 class="course-name">{{ courses[2].name }}</h3>
                <div class="course-info">
                  <span class="trainer">{{ courses[2].trainer || '未知' }}</span>
                  <span class="score">{{ (courses[2].avgScore || 0).toFixed(1) }} ⭐</span>
                </div>
              </div>
            </div>

            <div v-if="courses[3]" class="rank-card fourth unified-card" @click="showCourseDetail(courses[3])">
              <div class="rank-num">4</div>
              <div class="card-content">
                <h3 class="course-name">{{ courses[3].name }}</h3>
                <div class="course-info">
                  <span class="trainer">{{ courses[3].trainer || '未知' }}</span>
                  <span class="score">{{ (courses[3].avgScore || 0).toFixed(1) }} ⭐</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 第五名及以后横向排列 -->
          <div class="other-ranks unified-other">
            <div 
              v-for="(course, index) in courses.slice(4)" 
              :key="course.id || index"
              class="rank-card other unified-card"
              @click="showCourseDetail(course)"
            >
              <div class="rank-num">{{ index + 5 }}</div>
              <div class="card-content">
                <h3 class="course-name">{{ course.name }}</h3>
                <div class="course-info">
                  <span class="trainer">{{ course.trainer || '未知' }}</span>
                  <span class="score">{{ (course.avgScore || 0).toFixed(1) }} ⭐</span>
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
/* eslint-disable vue/multi-word-component-names */
import axios from "axios";

// 全局配置axios跨域
axios.defaults.withCredentials = true;

export default {
  name: 'Rank', // 保留单单词名，通过注释忽略ESLint警告
  data() {
    return {
      activeTab: 'pet', // 默认显示宠物排行榜
      // 宠物数据
      pets: [],
      petLoading: false,
      // 课程数据
      courses: [],
      courseLoading: false
    };
  },
  mounted() {
    // 初始加载宠物排行榜
    this.loadPetRankData();
  },
  methods: {
    // 切换标签
    switchTab(tabType) {
      this.activeTab = tabType;
      // 切换时加载对应数据（未加载过则加载）
      if (tabType === 'pet' && this.pets.length === 0) {
        this.loadPetRankData();
      } else if (tabType === 'course' && this.courses.length === 0) {
        this.loadCourseRankData();
      }
    },

    // 加载宠物排行榜数据
    async loadPetRankData() {
      this.petLoading = true;
      try {
        const res = await axios.get("http://localhost:8080/pet/rank", {
          timeout: 5000,
          headers: {
            "Cache-Control": "no-cache",
            Pragma: "no-cache",
            Expires: "0"
          },
          params: { _t: new Date().getTime() }
        });
        console.log("宠物排行榜数据：", res.data);
        this.pets = Array.isArray(res.data) ? res.data : [];
      } catch (err) {
        console.error("获取宠物排行榜失败：", err);
        let errorMsg = "获取宠物排行榜失败！";
        if (err.code === "ECONNABORTED") {
          errorMsg += "请求超时，请检查后端服务是否启动";
        } else if (err.response) {
          errorMsg += `错误码：${err.response.status}，${err.response.statusText}`;
        } else if (err.request) {
          errorMsg += "网络异常，无法连接后端服务";
        } else {
          errorMsg += err.message || "未知错误";
        }
        alert(errorMsg);
        this.pets = [];
      } finally {
        this.petLoading = false;
      }
    },

    // 加载课程排行榜数据
    async loadCourseRankData() {
      this.courseLoading = true;
      try {
        const res = await axios.get("http://localhost:8080/ranking/courses", {
          timeout: 5000,
          headers: {
            "Cache-Control": "no-cache",
            Pragma: "no-cache",
            Expires: "0"
          },
          params: { _t: new Date().getTime() }
        });
        console.log("课程排行榜数据：", res.data);
        this.courses = Array.isArray(res.data) ? res.data : [];
      } catch (err) {
        console.error("获取课程排行榜失败：", err);
        let errorMsg = "获取课程排行榜失败！";
        if (err.code === "ECONNABORTED") {
          errorMsg += "请求超时，请检查后端服务是否启动";
        } else if (err.response) {
          errorMsg += `错误码：${err.response.status}，${err.response.statusText}`;
        } else if (err.request) {
          errorMsg += "网络异常，无法连接后端服务";
        } else {
          errorMsg += err.message || "未知错误";
        }
        alert(errorMsg);
        this.courses = [];
      } finally {
        this.courseLoading = false;
      }
    },

    // 查看宠物详情（可扩展）
    showPetDetail(pet) {
      alert(`宠物【${pet.name}】详情：\n等级：Lv${pet.level || 1}\n经验：${pet.exp || 0} EXP\n品种：${pet.breed || '未知'}`);
    },

    // 查看课程详情（可扩展）
    showCourseDetail(course) {
      alert(`课程【${course.name}】详情：\n训练师：${course.trainer || '未知'}\n评分：${(course.avgScore || 0).toFixed(1)} ⭐\n简介：${course.description || '暂无简介'}`);
    }
  }
};
</script>

<style scoped>
/* 全局样式重置 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
  font-family: 'Inter', 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

/* 页面容器：高级渐变背景 */
.rank-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #f0f8fb 0%, #e8f4f8 50%, #f5fafe 100%);
  padding: 30px 20px;
}

.content {
  width: 95%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 25px;
  background: #ffffff;
  border-radius: 20px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.08);
}

/* 页面标题装饰 */
.page-header {
  text-align: center;
  margin-bottom: 35px;
}

.page-title {
  font-size: 30px;
  font-weight: 700;
  color: #2d3748;
  position: relative;
  padding-bottom: 12px;
  display: inline-block;
}

.title-divider {
  height: 4px;
  width: 80px;
  background: linear-gradient(90deg, #42b983, #4299e1);
  border-radius: 2px;
  margin: 8px auto 0;
}

/* 标签切换：高级样式 */
.tab-header {
  display: flex;
  gap: 12px;
  margin-bottom: 30px;
  justify-content: center;
}

.tab-item {
  padding: 12px 24px;
  border-radius: 30px;
  background: #f7fafc;
  color: #4a5568;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.tab-item.active {
  background: linear-gradient(135deg, #42b983, #38a169);
  color: #ffffff;
  box-shadow: 0 4px 12px rgba(66, 185, 131, 0.25);
  transform: translateY(-2px);
}

.tab-item:hover:not(.active) {
  background: #edf2f7;
  color: #2d3748;
}

.empty-tip {
  color: #718096;
  text-align: center;
  padding: 60px 0;
  font-size: 16px;
}

/* 排行榜容器：统一布局 */
.rank-container {
  width: 100%;
}

.rank-grid.unified-grid {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 前四名统一一行布局 */
.top-four {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 15px;
  width: 100%;
}

/* 所有卡片统一尺寸 */
.unified-card {
  width: 180px !important;
  height: 200px !important;
}

/* 其他名次统一布局 */
.other-ranks.unified-other {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 15px;
  margin-top: 10px;
}

/* 卡片基础样式 */
.rank-card {
  border-radius: 16px;
  padding: 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
  transition: all 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);
  position: relative;
  overflow: hidden;
  cursor: pointer;
}

/* 名次专属样式（仅颜色区分，尺寸统一） */
.rank-card.first {
  background: linear-gradient(135deg, #fff8e1 0%, #ffecb3 100%);
  border: 3px solid #ffc107;
}

.rank-card.second {
  background: linear-gradient(135deg, #f5f5f5 0%, #e0e0e0 100%);
  border: 3px solid #9e9e9e;
}

.rank-card.third {
  background: linear-gradient(135deg, #fbe9e7 0%, #ffccbc 100%);
  border: 3px solid #795548;
}

.rank-card.fourth {
  background: linear-gradient(135deg, #e8f4f8 0%, #f0f8fb 100%);
  border: 3px solid #4299e1;
}

.rank-card.other {
  background: #ffffff;
  border: 1px solid #e2e8f0;
}

/* 卡片hover效果：立体上浮+阴影增强 */
.rank-card:hover {
  transform: translateY(-8px) scale(1.03);
  box-shadow: 0 15px 30px rgba(0, 0, 0, 0.15);
}

/* 排名图标/数字 */
.rank-medal {
  font-size: 40px;
  margin-bottom: 10px;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.2));
}

.rank-num {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #4299e1;
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 8px;
}

/* 卡片内容 */
.card-content {
  text-align: center;
  width: 100%;
}

.pet-name, .course-name {
  font-size: 16px;
  font-weight: 600;
  color: #2d3748;
  margin-bottom: 8px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.pet-info, .course-info {
  display: flex;
  justify-content: center;
  gap: 8px;
  margin-bottom: 6px;
  flex-wrap: wrap;
}

.level, .trainer {
  color: #e53e3e;
  font-weight: 500;
  font-size: 12px;
}

.exp, .score {
  color: #38a169;
  font-weight: 500;
  font-size: 12px;
}

.breed {
  color: #718096;
  font-size: 12px;
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

.rank-card {
  animation: fadeInUp 0.6s ease forwards;
}

.rank-card.first { animation-delay: 0.1s; }
.rank-card.second { animation-delay: 0.2s; }
.rank-card.third { animation-delay: 0.3s; }
.rank-card.fourth { animation-delay: 0.4s; }
.rank-card.other:nth-child(1) { animation-delay: 0.5s; }
.rank-card.other:nth-child(2) { animation-delay: 0.6s; }
.rank-card.other:nth-child(3) { animation-delay: 0.7s; }

/* 响应式适配 */
@media (max-width: 768px) {
  /* 前四名纵向排列 */
  .top-four {
    flex-direction: column;
    align-items: center;
    gap: 15px;
  }
  
  /* 卡片宽度自适应 */
  .unified-card {
    width: 90% !important;
    height: auto !important;
    min-height: 180px;
  }

  /* 其他名次横向滚动 */
  .other-ranks.unified-other {
    justify-content: flex-start;
    overflow-x: auto;
    flex-wrap: nowrap;
    padding-bottom: 10px;
  }
  
  .rank-card.other {
    min-width: 160px;
    flex-shrink: 0;
  }
  
  .page-title {
    font-size: 24px;
  }
}
</style>