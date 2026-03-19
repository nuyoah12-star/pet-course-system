<!-- eslint-disable vue/multi-word-component-names -->
<template>
  <div class="home-page">
    <!-- 背景装饰 -->
    <div class="bg-decoration"></div>
    
    <div class="home-container">
      <div class="logo-box">🐾</div>
      <h1>宠物训练课程系统</h1>
      <p class="subtitle">用心陪伴，让每只宠物都闪闪发光</p>

      <div class="home-buttons">
        <button @click="$router.push('/courses')" class="btn primary-btn">
          📚 课程列表
        </button>
        <button @click="$router.push('/pet')" class="btn secondary-btn">
          🐶 我的宠物
        </button>
        <button @click="$router.push('/myCourse')" class="btn secondary-btn">
          📝 我的课程
        </button>
        <button @click="$router.push('/rank')" class="btn accent-btn">
          🏆 排行榜
        </button>
        <!-- 训练任务移到前面 -->
        <button @click="goToTask" class="btn task-btn">
          🎯 训练任务
        </button>
        <!-- 宠物成长 -->
        <button @click="$router.push('/growth')" class="btn growth-btn">
          📈 宠物成长
        </button>
        <!-- 宠物勋章 -->
        <button @click="$router.push('/badge')" class="btn badge-btn">
          🎖️ 宠物勋章
        </button>
        <!-- 学习统计 -->
        <button @click="$router.push('/stats')" class="btn stats-btn">
          📊 学习统计
        </button>
        <!-- 宠物技能树：移到最后面 -->
        <button @click="$router.push('/skill')" class="btn skill-btn">
          🌳 宠物技能树
        </button>
        <!-- 新增：课程推荐按钮（放在最后面） -->
        <button @click="goToRecommend" class="btn recommend-btn">
          💡 课程推荐
        </button>
      </div>

      <p class="footer-text">© 2026 宠物训练课程系统 - 用心守护每一份陪伴</p>
    </div>
  </div>
</template>

<script>
export default {
  // eslint-disable-next-line vue/multi-word-component-names
  name: 'Home',
  methods: {
    goToTask() {
      // 提示先选择课程
      alert('请先在课程列表中选择一门课程，再查看对应的训练任务')
      this.$router.push('/courses')
    },
    // 新增：独立的推荐页面跳转方法（便于调试和异常处理）
    goToRecommend() {
      console.log('点击课程推荐按钮，跳转至：/recommend')
      this.$router.push('/recommend').catch(err => {
        // 捕获路由跳转异常（比如重复点击同一路由）
        if (!err.message.includes('Avoided redundant navigation')) {
          console.error('课程推荐页面跳转失败：', err)
          alert('跳转失败，请检查路由配置！')
        }
      })
    }
  }
}
</script>

<style scoped>
/* 全局布局 */
.home-page {
  width: 100%;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4eaf5 100%);
  position: relative;
  overflow: hidden;
}

/* 背景装饰 */
.bg-decoration {
  position: absolute;
  top: -50px;
  right: -50px;
  width: 300px;
  height: 300px;
  background: rgba(66, 185, 131, 0.1);
  border-radius: 50%;
  z-index: 0;
}
.bg-decoration::after {
  content: '';
  position: absolute;
  bottom: -80px;
  left: -80px;
  width: 200px;
  height: 200px;
  background: rgba(255, 193, 7, 0.1);
  border-radius: 50%;
}

/* 内容容器：保持紧凑 */
.home-container {
  text-align: center;
  padding: 40px 30px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  box-shadow: 0 8px 32px rgba(31, 38, 135, 0.1);
  backdrop-filter: blur(8px);
  border: 1px solid rgba(255, 255, 255, 0.8);
  z-index: 1;
  max-width: 900px; /* 进一步缩小宽度，让按钮更紧凑 */
  width: 90%;
}

/* Logo样式 */
.logo-box {
  font-size: 50px;
  margin-bottom: 15px;
  animation: bounce 2s infinite alternate;
}
@keyframes bounce {
  from { transform: translateY(0); }
  to { transform: translateY(-8px); }
}

/* 标题样式 */
h1 {
  font-size: 30px;
  color: #2d3748;
  margin-bottom: 10px;
  font-weight: 600;
}

/* 副标题 */
.subtitle {
  font-size: 15px;
  color: #718096;
  margin-bottom: 30px;
  line-height: 1.5;
}

/* 按钮组：核心修改 - 让按钮不再占满整列，改为自动宽度+居中 */
.home-buttons {
  display: grid;
  grid-template-columns: repeat(5, auto); /* 改为自动宽度，不再均分占满 */
  grid-template-rows: repeat(2, auto);
  gap: 15px; /* 调整间距更舒服 */
  justify-content: center; /* 按钮组整体居中 */
  margin-bottom: 30px;
}

/* 按钮样式：更圆润+缩短宽度+精致 */
.btn {
  padding: 14px 22px; /* 横向内边距控制宽度，不再过长 */
  font-size: 14px;
  border: none;
  border-radius: 24px; /* 大幅加大圆角，更圆润 */
  color: white;
  cursor: pointer;
  transition: all 0.3s ease;
  font-weight: 500;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  box-shadow: 0 3px 10px rgba(0, 0, 0, 0.08);
  user-select: none;
  outline: none;
  height: 50px;
  white-space: nowrap; /* 防止文字换行 */
  min-width: 130px; /* 保证最小宽度，避免太窄 */
}
.btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 5px 14px rgba(0, 0, 0, 0.12);
}
.btn:active {
  transform: translateY(-1px);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}

/* 按钮配色：完全保留原有颜色 */
.primary-btn {
  background: linear-gradient(135deg, #42b983 0%, #359c6d 100%);
}
.secondary-btn {
  background: linear-gradient(135deg, #4299e1 0%, #3883ce 100%);
}
.accent-btn {
  background: linear-gradient(135deg, #f6ad55 0%, #ed8936 100%);
}
.task-btn {
  background: linear-gradient(135deg, #9f7aea 0%, #805ad5 100%);
}
.growth-btn {
  background: linear-gradient(135deg, #38b2ac 0%, #319795 100%);
}
.badge-btn {
  background: linear-gradient(135deg, #ecc94b 0%, #d69e2e 100%);
}
.stats-btn {
  background: linear-gradient(135deg, #4299e1 0%, #3182ce 100%);
}
.skill-btn {
  background: linear-gradient(135deg, #68d391 0%, #48bb78 100%);
}
.recommend-btn {
  background: linear-gradient(135deg, #7472FE 0%, #5753E8 100%);
}

/* 页脚文字 */
.footer-text {
  font-size: 12px;
  color: #a0aec0;
  margin-top: 10px;
}

/* 响应式优化：小屏幕适配 */
@media (max-width: 992px) {
  .home-buttons {
    grid-template-columns: repeat(3, auto);
    grid-template-rows: repeat(4, auto);
    gap: 12px;
  }
  .btn {
    padding: 12px 18px;
    min-width: 110px;
  }
}

@media (max-width: 768px) {
  .home-buttons {
    grid-template-columns: repeat(2, auto);
    grid-template-rows: repeat(5, auto);
  }
  .btn {
    padding: 10px 16px;
    font-size: 13px;
    height: 45px;
    border-radius: 20px;
    min-width: 100px;
  }
  h1 {
    font-size: 26px;
  }
  .logo-box {
    font-size: 45px;
  }
  .home-container {
    max-width: 95%;
    padding: 30px 20px;
  }
}
</style>