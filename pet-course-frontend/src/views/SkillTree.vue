<template>
  <div class="skill-tree-page">
    <div class="container">
      <!-- 标题 + 宠物切换下拉框 -->
      <div class="page-header">
        <div class="header-left">
          <h2 class="page-title">
            <span class="title-icon">🎯</span>
            宠物技能树
          </h2>
          <div class="subtitle">完成课程即可解锁对应技能</div>
        </div>
        <div class="pet-selector" v-if="petList.length > 0">
          <label for="petSelect" class="selector-label">选择宠物：</label>
          <div class="select-wrapper">
            <select 
              id="petSelect" 
              v-model="currentPetId" 
              @change="handlePetChange"
              class="pet-select"
            >
              <option 
                v-for="pet in petList" 
                :key="pet.id" 
                :value="pet.id"
              >
                {{ pet.name }}
              </option>
            </select>
            <span class="select-arrow">▼</span>
          </div>
        </div>
        <div class="pet-empty" v-else>
          🐶 暂无宠物数据，请先添加宠物
        </div>
      </div>

      <!-- 加载中 -->
      <div v-if="loading" class="loading-wrapper">
        <div class="loading-spinner"></div>
        <p class="loading-text">正在加载技能数据...</p>
      </div>

      <!-- 无技能数据提示 -->
      <div v-else-if="skills.length === 0 && petList.length > 0" class="empty-tip">
        <div class="empty-icon">📭</div>
        <p>暂无技能数据，已自动填充示例技能</p>
      </div>

      <!-- 技能列表 -->
      <div v-if="petList.length > 0 && !loading" class="skill-list">
        <div 
          v-for="(skill, index) in skills" 
          :key="skill.id" 
          class="skill-item"
          :class="{ unlocked: isSkillUnlocked(skill.id) }"
          :style="{ animationDelay: `${index * 0.1}s` }"
        >
          <!-- 技能图标 -->
          <div class="skill-icon" :class="getIconClass(skill.id)">
            <span v-if="skill.parentId === 0">📌</span>
            <span v-else>➡️</span>
          </div>

          <!-- 技能信息 -->
          <div class="skill-info">
            <h3 class="skill-name">{{ skill.name }}</h3>
            <p class="skill-desc">{{ skill.description }}</p>
          </div>

          <!-- 解锁状态 -->
          <div class="skill-status">
            <div v-if="isSkillUnlocked(skill.id)" class="status-badge unlocked">
              <span class="status-icon">✅</span>
              <span class="status-text">已解锁</span>
            </div>
            <div v-else class="status-badge locked">
              <span class="status-icon">🔒</span>
              <span class="status-text">未解锁</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

// 配置axios默认请求头
axios.defaults.headers.post['Content-Type'] = 'application/json';
axios.defaults.withCredentials = true;

// 后端接口地址配置（统一管理，方便修改）
const API_BASE_URL = 'http://localhost:8080';
const API = {
  SKILL_LIST: `${API_BASE_URL}/skill/list`,        // 技能列表接口
  PET_SKILL: `${API_BASE_URL}/pet-skill/`,         // 宠物已解锁技能接口（拼接petId）
  PET_LIST: `${API_BASE_URL}/pet/list`             // 宠物列表接口
};

// 示例技能数据（兜底用：接口返回空时自动填充）
const DEFAULT_SKILLS = [
  { id: 1, name: '基础训练', description: '宠物基础行为训练（入门级）', parentId: 0 },
  { id: 2, name: '坐下', description: '响应指令完成坐下动作', parentId: 1 },
  { id: 3, name: '握手', description: '学会抬起爪子与主人握手', parentId: 1 },
  { id: 4, name: '接飞盘', description: '完成飞盘接取训练', parentId: 1 },
  { id: 5, name: '障碍赛', description: '障碍训练（跨越/钻洞等）', parentId: 1 }
];

export default {
  name: 'SkillTree',
  data() {
    return {
      skills: [],                // 所有技能列表
      unlockedSkillIds: [],      // 当前宠物已解锁技能ID列表
      loading: true,             // 加载状态
      currentPetId: null,        // 当前选中宠物ID
      petList: []                // 宠物列表（接口动态获取）
    }
  },

  async mounted() {
    // 初始化加载顺序：先加载宠物列表 → 再加载技能 → 最后加载当前宠物技能
    await this.loadPetList();
    if (this.petList.length > 0) {
      // 优先从URL参数获取宠物ID，无则选第一个宠物
      this.currentPetId = this.$route.query.petId || this.petList[0].id;
      await this.loadSkills();
      // 兜底：如果接口返回空，使用示例技能数据
      if (this.skills.length === 0) {
        this.skills = [...DEFAULT_SKILLS];
        console.warn('⚠️ 技能接口返回空，已使用示例宠物数据');
      }
      await this.loadUnlockedSkills();
    }
    this.loading = false;
  },

  methods: {
    /**
     * 核心修复：判断技能是否解锁（统一类型转换，避免字符串/数字判断错误）
     * @param {Number} skillId 技能ID
     * @returns {Boolean} 是否解锁
     */
    isSkillUnlocked(skillId) {
      // 统一转换为数字类型后判断
      const numSkillId = Number(skillId);
      return this.unlockedSkillIds.some(id => Number(id) === numSkillId);
    },

    /**
     * 获取技能图标样式类
     */
    getIconClass(skillId) {
      const iconClasses = {
        1: 'icon-root',
        2: 'icon-sit',
        3: 'icon-shake',
        4: 'icon-frisbee',
        5: 'icon-obstacle'
      };
      return iconClasses[skillId] || 'icon-default';
    },

    /**
     * 加载所有宠物列表（接口动态获取）
     */
    async loadPetList() {
      try {
        const res = await axios.get(API.PET_LIST);
        // 兼容接口返回格式：数组 / {data: 数组}
        this.petList = Array.isArray(res.data) ? res.data : (res.data.data || []);
        console.log('宠物列表加载成功：', this.petList);
        // 兜底：如果宠物列表为空，添加示例宠物
        if (this.petList.length === 0) {
          this.petList = [{ id: 1, name: '旺财' }, { id: 2, name: '小白' }];
          console.warn('⚠️ 宠物接口返回空，已使用示例宠物数据');
        }
      } catch (err) {
        console.error('加载宠物列表失败：', err);
        this.$message?.error('加载宠物列表失败，请稍后重试') || alert('加载宠物列表失败，请稍后重试');
        // 兜底：异常时添加示例宠物
        this.petList = [{ id: 1, name: '旺财' }, { id: 2, name: '小白' }];
      }
    },

    /**
     * 加载所有技能列表
     */
    async loadSkills() {
      try {
        const res = await axios.get(API.SKILL_LIST);
        this.skills = Array.isArray(res.data) ? res.data : (res.data.data || []);
        console.log('技能列表加载成功：', this.skills);
      } catch (err) {
        console.error('加载技能列表失败：', err);
        this.$message?.error('加载技能数据失败，请稍后重试') || alert('加载技能数据失败，请稍后重试');
        this.skills = [];
      }
    },

    /**
     * 加载当前宠物已解锁技能（核心修复：统一转换为数字类型）
     */
    async loadUnlockedSkills() {
      if (!this.currentPetId) return;
      try {
        const res = await axios.get(`${API.PET_SKILL}${this.currentPetId}`);
        // 兼容两种返回格式：直接返回数组 / 包含skills字段的对象
        let rawUnlockedIds = [];
        if (Array.isArray(res.data)) {
          rawUnlockedIds = res.data;
        } else {
          rawUnlockedIds = res.data.skills || [];
        }
        // 核心修复：统一转换为数字类型，避免字符串ID判断错误
        this.unlockedSkillIds = rawUnlockedIds.map(id => Number(id));
        console.log(`宠物${this.currentPetId}已解锁技能（数字类型）：`, this.unlockedSkillIds);
      } catch (err) {
        console.error(`加载宠物${this.currentPetId}解锁技能失败：`, err);
        this.unlockedSkillIds = [];
        this.$message?.error('加载解锁技能失败，请稍后重试') || alert('加载解锁技能失败，请稍后重试');
      }
    },

    /**
     * 切换宠物时重新加载数据
     */
    async handlePetChange() {
      this.loading = true;
      // 更新URL参数，保留选中状态（刷新/分享不丢失）
      this.$router.replace({ 
        path: '/skill', 
        query: { petId: this.currentPetId } 
      });
      // 重新加载当前宠物的解锁技能
      await this.loadUnlockedSkills();
      this.loading = false;
    }
  }
}
</script>

<style scoped>
/* 页面整体渐变背景（温和护眼版：低饱和度紫蓝渐变） */
.skill-tree-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #e0e7ff 0%, #c4b5fd 35%, #ddd6fe 100%);
  padding: 40px 20px;
  overflow: visible !important;
  font-family: 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

.container {
  max-width: 750px;
  margin: 0 auto;
  min-height: calc(100vh - 80px);
  overflow: visible !important;
}

/* 头部标题区域（半透明白色毛玻璃，更柔和） */
.page-header {
  margin-bottom: 40px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 20px;
  background: rgba(255, 255, 255, 0.65);
  padding: 24px 28px;
  border-radius: 20px;
  backdrop-filter: blur(8px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
}

.header-left {
  text-align: left;
}

.page-title {
  font-size: 32px;
  background: linear-gradient(90deg, #4c1d95, #6d28d9);
  /* 修复兼容性警告：补充标准属性 */
  background-clip: text;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  margin: 0 0 10px;
  font-weight: 700;
  display: flex;
  align-items: center;
  gap: 10px;
}

.title-icon {
  font-size: 28px;
}

.subtitle {
  color: #6b7280;
  font-size: 16px;
  margin: 0;
}

/* 宠物选择器美化 */
.pet-selector {
  display: flex;
  align-items: center;
  gap: 12px;
}

.selector-label {
  color: #4c1d95;
  font-size: 15px;
  font-weight: 500;
}

.select-wrapper {
  position: relative;
}

.pet-select {
  padding: 10px 36px 10px 16px;
  border: none;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.9);
  font-size: 15px;
  color: #4c1d95;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.pet-select:hover {
  background: rgba(255, 255, 255, 1);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.pet-select:focus {
  outline: none;
  box-shadow: 0 0 0 3px rgba(109, 40, 217, 0.2);
}

.select-arrow {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: #6d28d9;
  font-size: 12px;
  pointer-events: none;
}

.pet-empty {
  color: #6b7280;
  font-size: 15px;
  padding: 10px 0;
}

/* 加载动画 */
.loading-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 0;
  gap: 16px;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid rgba(109, 40, 217, 0.15);
  border-top: 4px solid #6d28d9;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.loading-text {
  color: #4c1d95;
  font-size: 18px;
  margin: 0;
}

/* 空数据提示 */
.empty-tip {
  text-align: center;
  padding: 60px 0;
  color: #6b7280;
  font-size: 17px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.empty-icon {
  font-size: 40px;
}

/* 技能列表样式 */
.skill-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
  overflow: visible;
  padding-bottom: 30px;
  width: 100%;
}

/* 技能项卡片动画与样式（更通透的白色背景） */
.skill-item {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 24px;
  background: rgba(255, 255, 255, 0.85);
  border-radius: 20px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.05);
  transition: all 0.35s cubic-bezier(0.25, 0.8, 0.25, 1);
  width: 100%;
  box-sizing: border-box;
  overflow: visible !important;
  animation: fadeInUp 0.6s ease forwards;
  opacity: 0;
}

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

.skill-item:hover {
  transform: translateY(-6px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.08);
  background: rgba(255, 255, 255, 0.95);
}

.skill-item.unlocked {
  border-left: 5px solid #42b983;
  background: linear-gradient(90deg, rgba(66, 185, 131, 0.08), rgba(255, 255, 255, 0.85));
}

/* 技能图标样式（低饱和度渐变，更温和） */
.skill-icon {
  font-size: 28px;
  flex-shrink: 0;
  width: 52px;
  height: 52px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 14px;
  background: linear-gradient(135deg, #a5b4fc, #8b5cf6);
  color: #fff;
  box-shadow: 0 3px 10px rgba(139, 92, 246, 0.25);
}

.icon-root {
  background: linear-gradient(135deg, #f9a8d4, #ec4899);
}
.icon-sit {
  background: linear-gradient(135deg, #93c5fd, #3b82f6);
}
.icon-shake {
  background: linear-gradient(135deg, #86efac, #22c55e);
}
.icon-frisbee {
  background: linear-gradient(135deg, #fcd34d, #eab308);
}
.icon-obstacle {
  background: linear-gradient(135deg, #a78bfa, #8b5cf6);
}

/* 技能信息区域 */
.skill-info {
  flex: 1;
}

.skill-name {
  font-size: 20px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 6px;
}

.skill-desc {
  color: #6b7280;
  font-size: 15px;
  line-height: 1.5;
  margin: 0;
}

/* 解锁状态标签（降低饱和度，更柔和） */
.skill-status {
  flex-shrink: 0;
}

.status-badge {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 600;
}

.status-badge.unlocked {
  background: linear-gradient(90deg, #42b983, #51cf66);
  color: #fff;
  box-shadow: 0 3px 10px rgba(66, 185, 131, 0.25);
}

.status-badge.locked {
  background: linear-gradient(90deg, #ef4444, #f87171);
  color: #fff;
  box-shadow: 0 3px 10px rgba(239, 68, 68, 0.25);
}

.status-icon {
  font-size: 12px;
}

.status-text {
  font-size: 14px;
}

/* 响应式适配 */
@media (max-width: 768px) {
  .container {
    max-width: 95%;
    min-height: calc(100vh - 60px);
  }
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    padding: 20px;
  }
  .page-title {
    font-size: 26px;
  }
  .skill-item {
    padding: 20px;
    gap: 16px;
  }
  .skill-name {
    font-size: 18px;
  }
  .skill-desc {
    font-size: 14px;
  }
  .skill-icon {
    width: 46px;
    height: 46px;
    font-size: 24px;
  }
}
</style>