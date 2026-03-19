<!-- eslint-disable vue/multi-word-component-names -->
<template>
  <div class="pet-page">
    <div class="content">
      <!-- 页面头部：删除“共3只宠物”文字 -->
      <div class="page-header">
        <h2>我的宠物</h2>
      </div>

      <div v-if="loading" class="empty-tip">
        <div class="loading-spinner"></div>
        <p>正在加载宠物数据...</p>
      </div>

      <div v-else-if="pets.length === 0" class="empty-tip">
        <img 
          src="data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTIwIiBoZWlnaHQ9IjEyMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48Y2lyY2xlIGN4PSI2MCIgY3k9IjYwIiByPSI2MCIgZmlsbD0iI2VlZWVlZSIvPjx0ZXh0IHg9IjYwIiB5PSI2NSIgZm9udC1mYW1pbHk9IkFyaWFsIiBmb250LXNpemU9IjEyIiBmaWxsPSIjODk4IiB0ZXh0LWFuY2hvcj0ibWlkZGxlIj7lm57lhbvlo7DmsqE8L3RleHQ+PC9zdmc+" 
          alt="空状态" 
          class="empty-img"
        >
        <p>暂无宠物数据，请先添加宠物</p>
      </div>

      <!-- 紧凑网格布局：多列展示，减少纵向留白 -->
      <div v-else class="pet-card-grid">
        <!-- 给不同卡片添加不同色彩类，实现差异化配色 -->
        <div 
          v-for="(pet, index) in pets" 
          :key="pet.id" 
          class="pet-card"
          :class="`card-${index % 3}`"
          @click="goToPetGrowth(pet.name)"
        >
          <!-- 头像区域：悬浮交互，紧凑布局 -->
          <div class="avatar-wrapper">
            <!-- 核心修复：直接使用pet.avatar，不再拼接路径 -->
            <img 
              :src="pet.avatar ? pet.avatar : defaultAvatar" 
              alt="宠物头像"
              class="pet-avatar"
              @error="handleAvatarError"
            >
            <input 
              type="file"
              accept="image/*"
              @change="uploadAvatar($event, pet.id)"
              @click.stop
              class="avatar-upload"
              :id="`avatar-upload-${pet.id}`"
            >
            <label :for="`avatar-upload-${pet.id}`" class="upload-btn" @click.stop>
              <i class="icon"></i>
            </label>
          </div>

          <!-- 宠物信息：紧凑排版，图标辅助 -->
          <div class="pet-info">
            <h3 class="pet-name">{{ pet.name || '未知' }}</h3>
            <div class="info-row">
              <span class="label">品种：</span>
              <span class="value">{{ pet.breed || '未知' }}</span>
            </div>
            <div class="stats-row">
              <div class="stat-item">
                <span class="label">经验</span>
                <span class="value">{{ pet.exp || 0 }}</span>
              </div>
              <div class="stat-item">
                <span class="label">等级</span>
                <span class="value lv-tag">Lv{{ pet.level || 1 }}</span>
              </div>
            </div>
          </div>

          <!-- 跳转提示：悬浮显示，节省空间 -->
          <div class="action-tip">
            查看成长详情 →
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  data() {
    return {
      pets: [],
      loading: false,
      // 本地Base64默认头像（更可爱的宠物图标）
      defaultAvatar: 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iODAiIGhlaWdodD0iODAiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+PGNpcmNsZSBjeD0iNDAiIGN5PSI0MCIgcj0iNDAiIGZpbGw9IiNmNWY5ZTUiLz48dGV4dCB4PSI0MCIgeT0iNDUiIGZvbnQtZmFtaWx5PSJBcmlhbCIgZm9udC1zaXplPSIxMCIgZmlsbD0iIzUwNTk2MyIgdGV4dC1hbmNob3I9Im1pZGRsZSI+54K554OtPC90ZXh0Pjwvc3ZnPg=='
    }
  },
  mounted() {
    this.loadPets()
    this.$router.afterEach(() => {
      this.loadPets()
    })
  },
  methods: {
    loadPets() {
      this.loading = true
      axios.get('http://localhost:8080/pet', {
        timeout: 5000,
        headers: { 'Cache-Control': 'no-cache' }
      })
      .then(res => {
        this.pets = Array.isArray(res.data) ? res.data : []
        console.log('宠物数据（含头像字段）：', this.pets)
      })
      .catch(err => {
        console.error('加载失败：', err)
        this.$message?.error('加载宠物失败，请检查后端服务') || alert('加载宠物失败，请检查后端服务')
      })
      .finally(() => {
        this.loading = false
      })
    },
    goToPetGrowth(petName) {
      this.$router.push({ path: '/growth', query: { pet: petName } })
    },
    handleAvatarError(e) {
      const imgUrl = e.target.src
      if (imgUrl.includes('uploads/') && !imgUrl.startsWith('data:')) {
        console.error('真实头像加载失败 → 访问路径：', imgUrl)
        e.target.src = this.defaultAvatar
        this.$message?.warning('头像加载失败，已显示默认头像')
      }
    },
    async uploadAvatar(e, petId) {
      const file = e.target.files[0]
      if (!file) return
      if (file.size > 5 * 1024 * 1024) {
        this.$message?.error('请上传小于5MB的图片！') || alert('请上传小于5MB的图片！')
        return
      }
      if (!file.type.startsWith('image/')) {
        this.$message?.error('请选择图片文件！') || alert('请选择图片文件！')
        return
      }
      const formData = new FormData()
      formData.append('file', file)
      try {
        const uploadRes = await axios.post(
          'http://localhost:8080/pet/upload/avatar',
          formData,
          { headers: { 'Content-Type': 'multipart/form-data' }, timeout: 10000 }
        )
        const avatarFileName = uploadRes.data
        if (avatarFileName.includes('失败')) {
          this.$message?.error(avatarFileName) || alert(avatarFileName)
          return
        }
        console.log('上传成功的文件名：', avatarFileName)
        await axios.post('http://localhost:8080/pet/avatar', null, {
          params: { petId, avatar: avatarFileName },
          timeout: 5000
        })
        this.$message?.success('头像上传成功！') || alert('头像上传成功！')
        this.loadPets()
      } catch (err) {
        console.error('上传失败：', err)
        const tip = err.code === 'ECONNABORTED' ? '上传超时！' : '头像上传失败，请重试'
        this.$message?.error(tip) || alert(tip)
      }
    }
  }
}
</script>

<style scoped>
/* 核心：63.jpeg 完全覆盖背景板，无空白 */
.pet-page {
  min-height: 100vh;
  width: 100vw;
  height: 100vh;
  padding: 20px 0;
  margin: 0;
  /* 背景图：63.jpeg */
  background: url('http://localhost:8080/uploads/63.jpeg') no-repeat center center;
  /* 关键修改：width: 100% 强制宽度铺满，height: 100% 高度自适应，完全覆盖 */
  background-size: 100% 100%;
  /* 移除滚动，固定背景 */
  background-attachment: fixed;
  position: relative;
  /* 禁用横向滚动，确保无溢出 */
  overflow-x: hidden;
  /* 移除备用背景色，避免干扰 */
  background-color: transparent;
}

/* 遮罩：保持内容可读性 */
.pet-page::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(255, 255, 255, 0.5);
  pointer-events: none;
  z-index: 0;
}

.content {
  width: 95%;
  max-width: 1200px;
  margin: 0 auto;
  position: relative;
  z-index: 2;
  max-height: 100vh;
  overflow-y: auto;
  padding: 0 10px;
}

/* 页面头部：保留标题样式 */
.page-header {
  display: flex;
  align-items: center;
  margin-bottom: 24px;
}
.page-header h2 {
  color: #2d3748;
  margin: 0;
  padding-bottom: 8px;
  display: inline-block;
  font-size: 28px;
  font-weight: 700;
  position: relative;
}

.page-header h2::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 3px;
  background: #42b983;
  border-radius: 2px;
}

/* 空状态/加载状态 */
.empty-tip {
  text-align: center;
  padding: 40px 0;
  color: #718096;
}
.empty-img {
  width: 80px;
  margin-bottom: 12px;
  opacity: 0.7;
}
.loading-spinner {
  width: 30px;
  height: 30px;
  border: 3px solid #42b983;
  border-top: 3px solid transparent;
  border-radius: 50%;
  margin: 0 auto 10px;
  animation: spin 1s linear infinite;
}
@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 网格布局 */
.pet-card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

/* 宠物卡片样式 */
.pet-card {
  background: #ffffff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  border: 1px solid #f0f0f0;
  cursor: pointer;
  position: relative;
  transition: all 0.3s ease;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(8px);
}
.pet-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1), 0 0 25px rgba(102, 126, 234, 0.2);
}

.pet-card.card-0 {
  border-color: #42b983;
  background: linear-gradient(145deg, rgba(255,255,255,0.9) 0%, rgba(236, 247, 239, 0.8) 100%);
}
.pet-card.card-1 {
  border-color: #f6ad55;
  background: linear-gradient(145deg, rgba(255,255,255,0.9) 0%, rgba(254, 245, 231, 0.8) 100%);
}
.pet-card.card-2 {
  border-color: #667eea;
  background: linear-gradient(145deg, rgba(255,255,255,0.9) 0%, rgba(230, 235, 255, 0.8) 100%);
}

.pet-card.card-0:hover {
  border-color: #38a169;
}
.pet-card.card-1:hover {
  border-color: #ed8936;
}
.pet-card.card-2:hover {
  border-color: #5a67d8;
}

/* 头像区域 */
.avatar-wrapper {
  width: 80px;
  height: 80px;
  margin: 0 auto 16px;
  position: relative;
}
.pet-avatar {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid #f0f0f0;
  background-color: #f5f5f5;
  transition: border-color 0.3s ease;
}
.pet-card.card-0 .pet-avatar {
  border-color: #e6f7ef;
}
.pet-card.card-1 .pet-avatar {
  border-color: #fef5e7;
}
.pet-card.card-2 .pet-avatar {
  border-color: #e6ebff;
}
.pet-card:hover .pet-avatar {
  border-color: inherit;
}
.avatar-upload {
  display: none;
}
.upload-btn {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 24px;
  height: 24px;
  color: #ffffff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}
.pet-card.card-0 .upload-btn {
  background-color: #42b983;
}
.pet-card.card-1 .upload-btn {
  background-color: #f6ad55;
}
.pet-card.card-2 .upload-btn {
  background-color: #667eea;
}
.pet-card:hover .upload-btn {
  opacity: 1;
}
.upload-btn .icon::before {
  content: "+";
  font-size: 16px;
  font-weight: bold;
}

/* 宠物信息排版 */
.pet-info {
  text-align: center;
  margin-bottom: 10px;
}
.pet-name {
  font-size: 20px;
  color: #2d3748;
  margin: 0 0 10px 0;
  font-weight: 700;
}
.info-row {
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 15px;
  margin-bottom: 8px;
  color: #4a5568;
}
.info-row .label {
  font-weight: 500;
  margin-right: 4px;
  color: #718096;
}

.stats-row {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 10px;
}
.stat-item {
  text-align: center;
}
.stat-item .label {
  font-size: 13px;
  color: #718096;
  display: block;
  margin-bottom: 4px;
}
.stat-item .value {
  font-size: 16px;
  color: #2d3748;
  font-weight: 600;
}
.lv-tag {
  padding: 3px 10px;
  border-radius: 14px;
  font-size: 13px;
  font-weight: 600;
}
.pet-card.card-0 .lv-tag {
  background-color: #e6f7ef;
  color: #42b983;
}
.pet-card.card-1 .lv-tag {
  background-color: #fef5e7;
  color: #f6ad55;
}
.pet-card.card-2 .lv-tag {
  background-color: #e6ebff;
  color: #667eea;
}

/* 跳转提示 */
.action-tip {
  position: absolute;
  bottom: 10px;
  left: 0;
  width: 100%;
  text-align: center;
  font-size: 13px;
  opacity: 0;
  transition: opacity 0.3s ease;
}
.pet-card.card-0 .action-tip {
  color: #42b983;
}
.pet-card.card-1 .action-tip {
  color: #f6ad55;
}
.pet-card.card-2 .action-tip {
  color: #667eea;
}
.pet-card:hover .action-tip {
  opacity: 1;
}
</style>