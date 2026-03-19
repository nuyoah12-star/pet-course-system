<template>
  <div id="app">
    <!-- 导航栏：非首页显示，新增训练任务/技能树等入口 -->
    <nav class="main-nav" v-if="!isHomePage">
      <router-link to="/courses" class="nav-item nav-courses" exact>课程列表</router-link>
      <span class="nav-separator">|</span>
      <router-link to="/pet" class="nav-item nav-pet" exact>我的宠物</router-link>
      <span class="nav-separator">|</span>
      <router-link to="/myCourse" class="nav-item nav-mycourse" exact>我的课程</router-link>
      <span class="nav-separator">|</span>
      <router-link to="/rank" class="nav-item nav-rank" exact>排行榜</router-link>
      <span class="nav-separator">|</span>
      <router-link to="/task" class="nav-item nav-task" exact>训练任务</router-link>
      <span class="nav-separator">|</span>
      <router-link to="/growth" class="nav-item nav-growth" exact>宠物成长</router-link>
      <span class="nav-separator">|</span>
      <router-link to="/badge" class="nav-item nav-badge" exact>宠物勋章</router-link>
      <span class="nav-separator">|</span>
      <router-link to="/stats" class="nav-item nav-stats" exact>学习统计</router-link>
      <span class="nav-separator">|</span>
      <router-link to="/skill" class="nav-item nav-skill" exact>宠物技能树</router-link>
      <span class="nav-separator">|</span>
      <router-link to="/recommend" class="nav-item nav-recommend" exact>课程推荐</router-link>
    </nav>

    <!-- 路由视图 -->
    <RouterView />
  </div>
</template>

<script setup>
import { RouterView, RouterLink, useRoute } from 'vue-router' // 关键：显式导入RouterLink
import { computed, watch } from 'vue'

const route = useRoute()
// 判断当前是否为首页
const isHomePage = computed(() => route.path === '/')

// 新增：监听路由变化，调试用（可删除）
watch(route, (newRoute) => {
  console.log('📌 路由变化：', newRoute.path)
}, { immediate: true })
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body, #app {
  width: 100%;
  height: 100%;
  font-family: "Microsoft YaHei", "PingFang SC", sans-serif;
}

/* 导航栏样式：增加渐变背景与精致阴影 */
.main-nav {
  padding: 18px 24px;
  text-align: center;
  background: linear-gradient(135deg, #ffffff 0%, #f8fafc 100%);
  border-bottom: 1px solid #e2e8f0;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  min-height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-wrap: wrap;
  gap: 10px;
  position: relative;
}

/* 导航项基础样式：增加圆角、内边距和hover动效 */
.nav-item {
  margin: 0 10px;
  color: #4a5568;
  text-decoration: none;
  font-size: 15px;
  font-weight: 500;
  transition: all 0.3s ease;
  padding: 8px 16px;
  min-width: 80px;
  text-align: center;
  border-radius: 20px; /* 圆角让按钮更柔和 */
  position: relative;
}

.nav-item:hover {
  color: #4a5568;
  background: rgba(0, 0, 0, 0.04); /* 通用hover淡灰色背景 */
  transform: translateY(-1px); /* 轻微上浮效果 */
  text-decoration: none;
}

.nav-separator {
  color: #e2e8f0;
  flex-shrink: 0;
  font-weight: 300;
}

/* 各导航项专属激活色（和首页按钮一一对应） */
/* 课程列表 - 绿色 */
.nav-courses.router-link-exact-active {
  color: #ffffff !important;
  background: linear-gradient(135deg, #42b983 0%, #359c6d 100%);
  box-shadow: 0 3px 8px rgba(66, 185, 131, 0.25);
}
.nav-courses:hover {
  background: rgba(66, 185, 131, 0.08);
  color: #42b983 !important;
}

/* 我的宠物/我的课程/学习统计 - 蓝色 */
.nav-pet.router-link-exact-active,
.nav-mycourse.router-link-exact-active,
.nav-stats.router-link-exact-active {
  color: #ffffff !important;
  background: linear-gradient(135deg, #4299e1 0%, #3883ce 100%);
  box-shadow: 0 3px 8px rgba(66, 153, 225, 0.25);
}
.nav-pet:hover,
.nav-mycourse:hover,
.nav-stats:hover {
  background: rgba(66, 153, 225, 0.08);
  color: #4299e1 !important;
}

/* 排行榜 - 橙色 */
.nav-rank.router-link-exact-active {
  color: #ffffff !important;
  background: linear-gradient(135deg, #f6ad55 0%, #ed8936 100%);
  box-shadow: 0 3px 8px rgba(246, 173, 85, 0.25);
}
.nav-rank:hover {
  background: rgba(246, 173, 85, 0.08);
  color: #f6ad55 !important;
}

/* 训练任务 - 紫色 */
.nav-task.router-link-exact-active {
  color: #ffffff !important;
  background: linear-gradient(135deg, #9f7aea 0%, #805ad5 100%);
  box-shadow: 0 3px 8px rgba(159, 122, 234, 0.25);
}
.nav-task:hover {
  background: rgba(159, 122, 234, 0.08);
  color: #9f7aea !important;
}

/* 宠物成长 - 青绿色 */
.nav-growth.router-link-exact-active {
  color: #ffffff !important;
  background: linear-gradient(135deg, #38b2ac 0%, #319795 100%);
  box-shadow: 0 3px 8px rgba(56, 178, 172, 0.25);
}
.nav-growth:hover {
  background: rgba(56, 178, 172, 0.08);
  color: #38b2ac !important;
}

/* 宠物勋章 - 金色 */
.nav-badge.router-link-exact-active {
  color: #ffffff !important;
  background: linear-gradient(135deg, #ecc94b 0%, #d69e2e 100%);
  box-shadow: 0 3px 8px rgba(236, 201, 75, 0.25);
}
.nav-badge:hover {
  background: rgba(236, 201, 75, 0.08);
  color: #ecc94b !important;
}

/* 宠物技能树 - 嫩绿色 */
.nav-skill.router-link-exact-active {
  color: #ffffff !important;
  background: linear-gradient(135deg, #68d391 0%, #48bb78 100%);
  box-shadow: 0 3px 8px rgba(104, 211, 145, 0.25);
}
.nav-skill:hover {
  background: rgba(104, 211, 145, 0.08);
  color: #68d391 !important;
}

/* 课程推荐 - 蓝紫色 */
.nav-recommend.router-link-exact-active {
  color: #ffffff !important;
  background: linear-gradient(135deg, #7472FE 0%, #5753E8 100%);
  box-shadow: 0 3px 8px rgba(116, 114, 254, 0.25);
}
.nav-recommend:hover {
  background: rgba(116, 114, 254, 0.08);
  color: #7472FE !important;
}

/* 通用激活态样式（覆盖原有下划线） */
.nav-item.router-link-exact-active::after {
  display: none;
}

/* 响应式优化 */
@media (max-width: 992px) {
  .main-nav {
    padding: 14px 16px;
    gap: 6px;
  }
  .nav-item {
    margin: 0 6px;
    font-size: 14px;
    padding: 6px 12px;
    min-width: 70px;
  }
}

@media (max-width: 768px) {
  .main-nav {
    padding: 12px 10px;
    gap: 4px;
  }
  .nav-item {
    margin: 0 4px;
    font-size: 13px;
    padding: 5px 10px;
    min-width: 60px;
  }
  .nav-separator {
    display: none; /* 小屏幕隐藏分隔符，避免拥挤 */
  }
}
</style>