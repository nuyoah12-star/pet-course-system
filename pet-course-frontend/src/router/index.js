import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'
import Courses from '../views/Courses.vue'
import Pet from '../views/Pet.vue'
import MyCourse from '../views/MyCourse.vue'
import Rank from '../views/Rank.vue'
import Task from '../views/Task.vue'
import PetGrowth from '../views/PetGrowth.vue'
import Badge from '../views/Badge.vue'
import Stats from '../views/Stats.vue'
import SkillTree from '../views/SkillTree.vue'
// 关键优化：明确指定文件后缀，避免导入歧义
import Recommend from '../views/Recommend.vue'

// 增强版验证：打印每个组件的实际路径（排查导入问题）
console.log('=== 路由组件导入验证 ===')
console.log('Home:', Home)
console.log('Courses:', Courses)
console.log('Recommend:', Recommend) // 重点检查这个是否为undefined

const routes = [
  { path: '/', name: 'home', component: Home },
  { path: '/courses', name: 'courses', component: Courses },
  { path: '/pet', name: 'pet', component: Pet },
  { path: '/myCourse', name: 'myCourse', component: MyCourse },
  { path: '/rank', name: 'rank', component: Rank },
  { path: '/task', name: 'task', component: Task },
  { path: '/growth', name: 'growth', component: PetGrowth },
  { path: '/badge', name: 'badge', component: Badge },
  { path: '/stats', name: 'stats', component: Stats },
  { path: '/skill', name: 'skill', component: SkillTree },
  // 优化：提升recommend路由优先级，避免被兜底逻辑误判
  { 
    path: '/recommend', 
    name: 'recommend', 
    component: Recommend,
    // 新增：明确匹配规则，确保精准匹配
    props: false,
    alias: '/tuijian' // 可选：添加别名，方便测试
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
  // 新增：路由跳转滚动行为，提升体验
  scrollBehavior() {
    return { top: 0 }
  }
})

// 优化路由守卫：修复404兜底逻辑，排除合法路由
router.beforeEach((to, from, next) => {
  console.log('🚦 路由跳转：', { 
    from: from.path, 
    to: to.path, 
    matched: to.matched.length 
  })
  
  // 关键修复：只对真正的404路径兜底，避免误判合法路由
  const validPaths = routes.map(item => item.path)
  if (to.matched.length === 0 && !validPaths.includes(to.path)) {
    console.warn('⚠️ 路由未匹配到，跳转到首页：', to.path)
    next('/')
  } else {
    // 确保recommend路由能正常通过
    next()
  }
})

// 优化错误捕获：更精准的错误处理
router.onError((error) => {
  console.error('❌ 路由加载错误：', error.message)
  // 只在组件加载失败时跳转，避免正常路由被拦截
  if (error.message.includes('Failed to fetch dynamically imported module')) {
    alert('页面加载失败，请检查组件文件是否存在！')
    router.push('/')
  }
})

export default router