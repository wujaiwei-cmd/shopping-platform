<template>
  <div class="fav-page">
    <header class="header">
      <div class="header-center">
        <h1>购物平台</h1>
      </div>
      <nav class="header-right">
        <a href="#" @click.prevent="goHome" class="nav-item">首页</a>
        <a href="#" @click.prevent="goProfile" class="nav-item">我的</a>
        <a href="#" @click.prevent="goCart" class="nav-item">购物车</a>
        <a href="#" class="nav-item active">收藏夹</a>
        <a href="#" @click.prevent="goHelp" class="nav-item">帮助中心</a>
      </nav>
    </header>

    <div class="fav-content">
      <div class="page-head">
        <h2>我的收藏</h2>
      </div>

      <div class="fav-list">
        <div v-if="favProducts.length === 0" class="empty-tip">暂无收藏商品</div>
        <div v-else class="fav-grid">
          <div class="fav-card" v-for="product in favProducts" :key="product.id">
            <div class="fav-img-wrap" @click="goProductDetail(product)">
              <img :src="product.image" :alt="product.name" class="fav-img" />
            </div>
            <div class="fav-info" @click="goProductDetail(product)">
              <p class="fav-title">{{ product.name }}</p>
              <div class="price-row">
                <span class="price-symbol">¥</span>
                <span class="price-value">{{ product.price.toFixed(2) }}</span>
              </div>
            </div>
            <button class="remove-fav-btn" @click="removeFavorite(product)">
              取消收藏
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 移动端底部导航 -->
    <nav class="mobile-nav">
      <a href="#" class="mobile-nav-item" @click.prevent="goHome">
        <span>首页</span>
      </a>
      <a href="#" class="mobile-nav-item" @click.prevent="goProfile">
        <span>我的</span>
      </a>
      <a href="#" class="mobile-nav-item" @click.prevent="goCart">
        <span>购物车</span>
      </a>
      <a href="#" class="mobile-nav-item active" @click.prevent="goFav">
        <span>收藏夹</span>
      </a>
      <a href="#" class="mobile-nav-item" @click.prevent="goHelp">
        <span>帮助</span>
      </a>
    </nav>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from "vue";
import { useRouter } from "vue-router";

const router = useRouter();

let favorites = ref([]);
let products = ref([]);

const favProducts = computed(() => {
  return products.value.filter(product => 
    favorites.value.some(fav => fav.goodsId === product.id)
  );
});

onMounted(async () => {
  const user = localStorage.getItem("loginUser");
  if (!user) {
    alert("请先登录");
    router.push("/login");
    return;
  }
  await loadProducts();
  await loadFavorites();
});

async function loadProducts() {
  try {
    const response = await fetch("http://localhost:8081/api/goods");
    const data = await response.json();
    if (data.code === 200) {
      products.value = data.data;
    }
  } catch (error) {
    console.error("加载商品失败:", error);
  }
}

async function loadFavorites() {
  const token = localStorage.getItem("token");
  if (!token) return;
  
  try {
    const response = await fetch("http://localhost:8081/api/favorites", {
      headers: {
        "x-auth-token": token
      }
    });
    const data = await response.json();
    if (data.code === 200) {
      favorites.value = data.data;
    }
  } catch (error) {
    console.error("加载收藏失败:", error);
  }
}

async function removeFavorite(product) {
  if (!confirm("确定要取消收藏该商品吗？")) return;

  const token = localStorage.getItem("token");
  try {
    const response = await fetch(`http://localhost:8081/api/favorites/${product.id}`, {
      method: "DELETE",
      headers: {
        "x-auth-token": token
      }
    });
    const data = await response.json();
    
    if (data.code === 200) {
      favorites.value = favorites.value.filter(fav => fav.goodsId !== product.id);
      alert("已取消收藏");
    } else {
      alert(data.message || "操作失败");
    }
  } catch (error) {
    console.error("取消收藏失败:", error);
    alert("操作失败，请检查网络连接");
  }
}

function goProductDetail(product) {
  localStorage.setItem("selectedProduct", JSON.stringify(product));
  router.push("/home");
}

function goHome() {
  router.push("/home");
}

function goProfile() {
  router.push("/profile");
}

function goCart() {
  router.push("/cart");
}

function goHelp() {
  router.push("/help");
}

function goFav() {
  // 已经在收藏夹页面，不需要跳转
}
</script>

<style scoped>
.fav-page {
  min-height: 100vh;
  background: #fff;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 30px;
  background: #fff;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
}

.header-center h1 {
  margin: 0;
  font-size: 24px;
  color: #333;
}

.header-right {
  display: flex;
  gap: 25px;
}

.nav-item {
  text-decoration: none;
  color: #666;
  font-size: 15px;
}

.nav-item:hover {
  color: #ff6600;
}

.nav-item.active {
  color: #ff6600;
  font-weight: bold;
}

.fav-content {
  padding: 30px;
}

.page-head {
  margin-bottom: 30px;
}

.page-head h2 {
  margin: 0 0 20px;
  font-size: 32px;
  font-weight: bold;
  color: #333;
  position: relative;
  display: inline-block;
}

.page-head h2::after {
  content: "";
  position: absolute;
  bottom: -5px;
  left: 0;
  width: 100%;
  height: 4px;
  background: #ff6600;
}

.fav-list {
  min-height: 300px;
}

.fav-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 20px;
}

.fav-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);
  transition: all 0.3s ease;
}

.fav-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 6px 20px rgba(0,0,0,0.12);
}

.fav-img-wrap {
  position: relative;
  padding-top: 100%;
  background: linear-gradient(135deg, #fafafa 0%, #f5f5f5 100%);
  cursor: pointer;
}

.fav-img {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: contain;
  padding: 10px;
}

.fav-info {
  padding: 12px;
  cursor: pointer;
}

.fav-title {
  margin: 0 0 10px;
  font-size: 14px;
  color: #333;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 42px;
}

.price-row {
  display: flex;
  align-items: baseline;
}

.price-symbol {
  font-size: 14px;
  color: #ff4400;
  font-weight: 700;
}

.price-value {
  font-size: 20px;
  color: #ff4400;
  font-weight: 700;
  font-family: Arial, sans-serif;
}

.remove-fav-btn {
  width: 100%;
  padding: 12px;
  border: none;
  border-top: 1px solid #eee;
  background: #fff;
  color: #999;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
}

.remove-fav-btn:hover {
  color: #ff4d4f;
  background: #fff5f5;
}

.empty-tip {
  text-align: center;
  color: #999;
  font-size: 16px;
  padding: 100px 0;
}

/* 移动端底部导航默认隐藏 */
.mobile-nav {
  display: none;
}

/* 移动端响应式样式 */
@media (max-width: 768px) {
  /* 头部样式 */
  .header {
    display: flex;
    justify-content: flex-start;
    padding: 10px 15px;
    background: linear-gradient(90deg, #333);
  }
  
  .header-center h1 {
    font-size: 22px;
    color: #fff;
    font-weight: 700;
  }
  
  .header-right {
    display: none;
  }
  
  /* 收藏夹内容 */
  .fav-content {
    padding: 10px 15px;
    padding-bottom: 80px;
  }
  
  .page-head h2 {
    font-size: 24px;
  }
  
  .fav-grid {
    grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
    gap: 12px;
  }
  
  .fav-card {
    border-radius: 10px;
  }
  
  .fav-title {
    font-size: 13px;
    min-height: 39px;
  }
  
  .price-value {
    font-size: 18px;
  }
  
  .remove-fav-btn {
    padding: 10px;
    font-size: 13px;
  }
  
  /* 移动端底部导航 */
  .mobile-nav {
    display: flex !important;
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    background: #fff;
    justify-content: space-around;
    align-items: center;
    padding: 10px 0;
    box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.1);
    z-index: 1000;
  }
  
  .mobile-nav-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 4px;
    color: #666;
    text-decoration: none;
    font-size: 12px;
    transition: color 0.3s;
  }
  
  .mobile-nav-item.active {
    color: #ff4400;
  }
  
  .mobile-nav-item span:first-child {
    font-size: 20px;
  }
}
</style>
