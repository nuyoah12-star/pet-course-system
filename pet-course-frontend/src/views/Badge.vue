<template>
  <div class="badge-page">
    <div class="badge-header">
      <h1 class="badge-title">🎖️ 宠物勋章中心</h1>
      <p class="badge-desc">查看所有宠物的勋章获取情况</p>
    </div>

    <div class="pet-badge-container">
      <div class="pet-card" v-for="pet in petBadgeList" :key="pet.id">
        <div class="pet-top">
          <img 
            class="pet-avatar" 
            :src="pet.avatar || defaultAvatar" 
            alt="宠物头像"
            @error="handleImageError"
          />
          <div class="pet-name">{{ pet.name }}</div>
        </div>

        <div class="pet-badges">
          <div class="badge-item" v-for="badge in pet.badges" :key="badge.id">
            <div class="badge-icon">
              <span class="icon-inner">{{ getBadgeIcon(badge.id) }}</span>
            </div>
            <div class="badge-info">
              <div class="badge-name">{{ badge.name }}</div>
              <div class="badge-desc">{{ badge.description }}</div>
            </div>
          </div>
          <div class="no-badge" v-if="pet.badges.length === 0">
            暂无勋章
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from "axios";

export default {
  name: "Badge",
  data() {
    return {
      petBadgeList: [],
      defaultAvatar: "https://i.imgur.com/8KQxQzH.jpg",
      petAvatarMap: {
        "可乐": "https://i.imgur.com/8KQxQzH.jpg",
        "糯米": "https://i.imgur.com/9X7kZ7L.jpg",
        "旺财": "https://i.imgur.com/4ZJ9k8Y.jpg"
      }
    };
  },
  mounted() {
    this.fetchAllPetBadges();
  },
  methods: {
    async fetchAllPetBadges() {
      try {
        const petRes = await axios.get("http://localhost:8080/pet/list");
        const pets = petRes.data;
        const petBadges = [];
        
        for (const pet of pets) {
          const badgeRes = await axios.get(`http://localhost:8080/badge/my?petId=${pet.id}`);
          petBadges.push({
            id: pet.id,
            name: pet.name,
            avatar: pet.avatar || this.petAvatarMap[pet.name] || this.defaultAvatar,
            badges: badgeRes.data
          });
        }

        this.petBadgeList = petBadges;
      } catch (err) {
        console.error("获取宠物勋章失败:", err);
        alert("获取数据失败，请重试");
      }
    },
    getBadgeIcon(id) {
      const icons = {
        1: "🥉",
        2: "🥈",
        3: "🥇"
      };
      return icons[id] || "🏅";
    },
    handleImageError(e) {
      e.target.src = this.defaultAvatar;
    }
  }
};
</script>

<style scoped>
/* 页面整体：背景图浅化至60%，进一步弱化背景 */
.badge-page {
  min-height: 100vh;
  padding: 30px 20px;
  font-family: "Microsoft YaHei", "PingFang SC", sans-serif;
  /* 背景图叠加60%白色，实现更浅的效果 */
  background: 
    linear-gradient(rgba(255, 255, 255, 0.6), rgba(255, 255, 255, 0.6)),
    url('@/assets/73.jpeg') no-repeat center center fixed;
  background-size: cover;
  position: relative;
}

/* 头部标题区域：继续弱化 */
.badge-header {
  text-align: center;
  margin-bottom: 40px;
  position: relative;
  z-index: 1;
  opacity: 0.75; /* 进一步弱化标题 */
}
.badge-title {
  font-size: 30px;
  color: #64748b;
  margin-bottom: 8px;
  font-weight: 500;
  text-shadow: none;
}
.badge-desc {
  font-size: 14px;
  color: #718096;
  font-style: italic;
  text-shadow: none;
}

/* 宠物卡片容器：保持突出效果 */
.pet-badge-container {
  max-width: 1100px;
  margin: 0 auto;
  display: flex;
  gap: 32px;
  flex-wrap: wrap;
  justify-content: center;
  position: relative;
  z-index: 2;
  padding: 20px 0;
}

/* 宠物卡片：强化视觉效果 */
.pet-card {
  width: 280px;
  height: 280px;
  border-radius: 24px;
  padding: 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15),
              0 4px 12px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
  backdrop-filter: blur(12px);
  border: 2px solid rgba(255, 255, 255, 0.95);
  opacity: 1;
}

/* 卡片颜色：保持原有色系，确保视觉突出 */
.pet-card:nth-child(1) {
  background: linear-gradient(135deg, rgba(224, 242, 254, 0.92) 0%, rgba(186, 230, 253, 0.97) 100%);
}
.pet-card:nth-child(2) {
  background: linear-gradient(135deg, rgba(254, 242, 242, 0.92) 0%, rgba(254, 226, 226, 0.97) 100%);
}
.pet-card:nth-child(3) {
  background: linear-gradient(135deg, rgba(245, 243, 255, 0.92) 0%, rgba(237, 233, 254, 0.97) 100%);
}

/* 卡片hover效果 */
.pet-card:hover {
  transform: translateY(-8px) scale(1.02);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.2),
              0 6px 16px rgba(0, 0, 0, 0.15);
  border-color: #fff;
}

/* 宠物头像区域 */
.pet-top {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}
.pet-avatar {
  width: 70px;
  height: 70px;
  border-radius: 50%;
  object-fit: cover;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
  border: 3px solid #fff;
}
.pet-name {
  font-size: 20px;
  font-weight: 700;
  color: #1e3a8a;
  letter-spacing: 1px;
  text-shadow: 0 1px 2px rgba(255,255,255,0.8);
}

/* 勋章列表 */
.pet-badges {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 8px;
  align-items: center;
}

/* 勋章项 */
.badge-item {
  width: 90%;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 14px;
  border-radius: 18px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.2s ease;
  border: 1px solid rgba(255,255,255,0.9);
  position: relative;
  overflow: hidden;
  backdrop-filter: blur(4px);
}

.badge-item::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  border-radius: 18px;
  background: linear-gradient(
    to right,
    rgba(255,255,255,0.1),
    rgba(255,255,255,0.4),
    rgba(255,255,255,0.1)
  );
  transform: translateX(-100%);
  transition: transform 0.6s ease;
}

.badge-item:hover::before {
  transform: translateX(100%);
}

/* 勋章颜色 */
.badge-item:nth-child(1) {
  background: linear-gradient(135deg, rgba(255, 251, 235, 0.92) 0%, rgba(254, 243, 199, 0.97) 100%);
  border-color: #fde68a;
}
.badge-item:nth-child(2) {
  background: linear-gradient(135deg, rgba(245, 243, 255, 0.92) 0%, rgba(237, 233, 254, 0.97) 100%);
  border-color: #c4b5fd;
}
.badge-item:nth-child(3) {
  background: linear-gradient(135deg, rgba(224, 242, 254, 0.92) 0%, rgba(186, 230, 253, 0.97) 100%);
  border-color: #93c5fd;
}

/* 勋章图标 */
.badge-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: white;
  box-shadow: 0 2px 6px rgba(0,0,0,0.15),
              0 0 6px rgba(255,255,255,0.4) inset;
  position: relative;
  z-index: 1;
}

.badge-item:nth-child(1) .badge-icon {
  background: linear-gradient(135deg, #fbbf24 0%, #f59e0b 100%);
}
.badge-item:nth-child(2) .badge-icon {
  background: linear-gradient(135deg, #a78bfa 0%, #8b5cf6 100%);
}
.badge-item:nth-child(3) .badge-icon {
  background: linear-gradient(135deg, #0ea5e9 0%, #0284c7 100%);
}

/* 勋章文字 */
.badge-info {
  flex: 1;
  position: relative;
  z-index: 1;
}
.badge-name {
  font-size: 14px;
  font-weight: 700;
  color: #1e3a8a;
  margin-bottom: 2px;
}
.badge-desc {
  font-size: 12px;
  color: #475569;
  font-weight: 500;
}

/* 无勋章提示 */
.no-badge {
  padding: 12px;
  color: #64748b;
  font-size: 13px;
  background: rgba(248, 250, 252, 0.97);
  border-radius: 12px;
  width: 90%;
  text-align: center;
  border: 1px solid #e2e8f0;
}
</style>