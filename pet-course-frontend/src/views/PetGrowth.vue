<template>
  <div class="growth-page">
    <div class="growth-container">
      <!-- 宠物选择栏：增加图标感 -->
      <div class="pet-select">
        <label>
          <span class="icon-label">🐾</span> 选择宠物：
        </label>
        <select v-model="selectedPetId" @change="getPetGrowth">
          <option value="1">可乐 🐶</option>
          <option value="2">糯米 🐱</option>
          <option value="3">旺财 🐕</option>
        </select>
      </div>

      <!-- 成长档案卡片：增加背景点缀 -->
      <div class="growth-info">
        <div class="card-header">
          <span class="pet-emoji">{{ selectedPetId === '1' ? '🐶' : (selectedPetId === '2' ? '🐱' : '🐕') }}</span>
          <h2>{{ petMap[selectedPetId] }}的成长档案</h2>
        </div>

        <div class="detail-row">
          <span class="label">当前等级：</span>
          <strong class="level-value">{{ growth.level || 1 }}</strong>
        </div>

        <div class="detail-row">
          <span class="label">当前经验：</span>
          <span class="exp-value">{{ growth.exp || 0 }} / 1000</span>
        </div>
        
        <!-- 经验条：可爱圆角 -->
        <div class="exp-bar-container">
          <div class="exp-bar-bg">
            <div 
              class="exp-bar-fill" 
              :style="{ width: `${Math.min(100, (growth.exp || 0) / 10)}%` }"
            ></div>
          </div>
          <div class="exp-text">{{ growth.exp || 0 }} / 1000</div>
        </div>

        <!-- 刷新按钮：圆润风格 -->
        <button class="refresh-btn" @click="getPetGrowth">
          <span class="btn-icon">🔄</span> 刷新成长数据
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import axios from 'axios'
import { ref, watch, onMounted } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

// 宠物ID <-> 名称 映射
const petMap = {
  '1': '可乐',
  '2': '糯米',
  '3': '旺财'
}
const nameToIdMap = {
  '可乐': '1',
  '糯米': '2',
  '旺财': '3'
}

const selectedPetId = ref('1')
const growth = ref({})

const getPetGrowth = () => {
  const petId = Number(selectedPetId.value)
  axios.get(`http://localhost:8080/pet/${petId}`)
    .then(res => {
      growth.value = res.data
    })
    .catch(err => {
      console.error("获取数据失败：", err)
      growth.value = { level: 1, exp: 0 }
    })
}

onMounted(() => {
  if (route.query.pet) {
    const petName = route.query.pet
    const targetPetId = nameToIdMap[petName]
    if (targetPetId) {
      selectedPetId.value = targetPetId
    }
  }
  getPetGrowth()
})

watch(
  () => route.query,
  (newQuery) => {
    if (newQuery.pet) {
      const petName = newQuery.pet
      const targetPetId = nameToIdMap[petName]
      if (targetPetId) {
        selectedPetId.value = targetPetId
        getPetGrowth()
      }
    }
  },
  { deep: true }
)

watch(selectedPetId, () => {
  getPetGrowth()
})
</script>

<style scoped>
/* 页面整体：更透明的薄荷蓝渐变背景（无小点点） */
.growth-page {
  min-height: 100vh;
  /* 使用rgba实现透明效果，薄荷蓝调清晰可见但更通透 */
  background: linear-gradient(120deg, 
    rgba(184, 224, 240, 0.7) 0%, 
    rgba(224, 232, 240, 0.7) 60%, 
    rgba(232, 238, 245, 0.7) 100%);
  padding: 50px 20px;
  position: relative;
  /* 可选：如果需要白色底层衬托透明效果 */
  /* background-color: #ffffff; */
}

/* 移除深色遮罩层 */
.growth-page::before {
  display: none;
}

/* 主容器：完全保留原有卡片样式 */
.growth-container {
  width: 420px;
  margin: 0 auto;
  padding: 25px;
  background: #ffeaf2;
  border-radius: 24px;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.25), 0 0 20px rgba(255, 153, 180, 0.1);
  border: 1px solid #ffd6e0;
  position: relative;
  z-index: 1;
  backdrop-filter: blur(2px);
}

/* 宠物选择框：保持原有样式 */
.pet-select {
  margin-bottom: 30px;
  font-size: 17px;
  display: flex;
  align-items: center;
  color: #5a6c76;
}
.pet-select label {
  display: flex;
  align-items: center;
  font-weight: 500;
}
.icon-label {
  margin-right: 5px;
  font-size: 18px;
  color: #48c9b0;
}
.pet-select select {
  margin-left: 12px;
  padding: 8px 15px;
  border-radius: 12px;
  border: 1px solid #48c9b0;
  font-size: 16px;
  min-width: 150px;
  background: #ffffff;
  transition: all 0.3s ease;
  cursor: pointer;
}
.pet-select select:focus {
  outline: none;
  border-color: #58d68d;
  box-shadow: 0 0 0 3px rgba(88, 214, 141, 0.15);
}

/* 成长信息卡片：保持原有样式 */
.growth-info {
  text-align: center;
}

/* 卡片标题：保持原有样式 */
.card-header {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 25px;
  gap: 10px;
}
.pet-emoji {
  font-size: 28px;
  animation: bounce 2s infinite ease-in-out;
  filter: drop-shadow(0 2px 4px rgba(255, 102, 153, 0.2));
}
@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-5px); }
}
.growth-info h2 {
  color: #34495e;
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  text-shadow: 0 1px 2px rgba(0,0,0,0.05);
}

/* 详情行：保持原有样式 */
.detail-row {
  margin: 18px 0;
  font-size: 17px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 15px;
  background: #ffffff;
  border-radius: 16px;
  margin-left: 10px;
  margin-right: 10px;
}
.detail-row .label {
  color: #7f8c8d;
  font-weight: 500;
  font-size: 18px;
}
.level-value {
  font-size: 20px;
  color: #58d68d;
  font-weight: bold;
  background: #e8f8ef;
  padding: 6px 18px;
  border-radius: 20px;
  border: 1px solid #d1f2e0;
}
.exp-value {
  font-size: 18px;
  color: #f4d03f;
  font-weight: 600;
  text-shadow: 0 1px 3px rgba(244, 208, 63, 0.15);
}

/* 经验条：保持原有样式 */
.exp-bar-container {
  margin: 25px 10px;
  padding: 10px;
  background: #ffffff;
  border-radius: 16px;
}
.exp-bar-bg {
  width: 100%;
  height: 24px;
  background: #f8f9fa;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: inset 0 2px 4px rgba(0,0,0,0.03);
  margin-bottom: 8px;
}
.exp-bar-fill {
  height: 100%;
  background: linear-gradient(90deg, #58d68d 0%, #48c9b0 50%, #85c1e9 100%);
  border-radius: 12px;
  transition: width 0.5s ease;
  position: relative;
  box-shadow: 0 2px 6px rgba(88, 214, 141, 0.2);
}
.exp-bar-fill::after {
  content: "✨";
  position: absolute;
  right: 10px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 14px;
}
.exp-text {
  color: #5a6c76;
  font-size: 14px;
  text-align: right;
  margin-top: 5px;
  font-weight: 500;
}

/* 刷新按钮：保持原有样式 */
.refresh-btn {
  margin-top: 20px;
  padding: 12px 36px;
  background: linear-gradient(135deg, #58d68d 0%, #48c9b0 100%);
  color: white;
  border: none;
  border-radius: 50px;
  cursor: pointer;
  font-size: 16px;
  font-weight: 600;
  transition: all 0.3s ease;
  box-shadow: 0 6px 18px rgba(88, 214, 141, 0.25);
}
.refresh-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 24px rgba(88, 214, 141, 0.35);
}
.btn-icon {
  margin-right: 8px;
  animation: rotate 2s linear infinite;
}
@keyframes rotate {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
</style>