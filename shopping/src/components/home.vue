<template>
  <!-- 首页主容器 -->
  <div class="home">
    <!-- 顶部导航栏 -->
    <header class="top-bar">
      <!-- 左侧：登录区域 -->
      <div class="left-part">
        <div class="login-box">
          <span @click="handleLogin">{{ nowUser || '登录' }}</span>
          <!-- 登录后显示的操作菜单 -->
          <div v-if="showOpts && nowUser" class="login-options">
            <span @click="changeUser">切换用户</span>
            <span @click="quitLogin">退出登录</span>
          </div>
        </div>
      </div>
      
      <!-- 中间：平台Logo -->
      <div class="center-part">
        <h1>购物平台</h1>
      </div>
      
      <!-- 右侧：导航链接 -->
      <nav class="right-part">
        <a href="#" class="nav-link">首页</a>
        <a href="#" @click.prevent="goProfile" class="nav-link">我的</a>
        <a href="#" @click.prevent="goCart" class="nav-link">购物车</a>
        <a href="#" @click.prevent="goFav" class="nav-link">收藏夹</a>
        <a href="#" @click.prevent="goHelp" class="nav-link">帮助中心</a>
      </nav>
    </header>

    <!-- 搜索框区域 -->
    <div class="search-box-wrap">
      <div class="search-input-box">
        <select class="select-box">
          <option>宝贝</option>
        </select>
        <input 
          type="text" 
          placeholder="搜索商品" 
          class="search-input" 
          v-model="searchKeyword"
          @keyup.enter="handleSearch"
        />
        <button class="search-button" @click="handleSearch">搜索</button>
      </div>
    </div>

    <!-- 搜索结果区域 -->
    <div v-if="isSearchActive" class="search-results-section">
      <div class="search-results-header">
        <h3 class="search-results-title">搜索"{{ searchKeyword }}"的结果</h3>
        <button class="clear-search-btn" @click="clearSearch">清除搜索</button>
      </div>
      <div class="search-results-grid">
        <div v-if="filteredProducts.length === 0" class="empty-tip">没有找到"{{ searchKeyword }}"相关的商品</div>
        <div v-else class="product-card" v-for="product in filteredProducts" :key="product.id">
          <div class="product-img-wrap" @click="showProductDetail(product)">
            <img :src="product.image" :alt="product.name" class="product-img" />
            <div v-if="product.stock < 10 && product.stock > 0" class="stock-tag">仅剩{{ product.stock }}件</div>
            <div v-if="product.stock === 0" class="sold-out-tag">已售罄</div>
            <span 
              :class="['fav-btn', { active: isFavorited(product) }]" 
              @click.stop="toggleFavorite(product)"
            >
              ♥
            </span>
          </div>
          <div class="product-info" @click="showProductDetail(product)">
            <p class="product-title">{{ product.name }}</p>
            <div class="price-row">
              <span class="price-symbol">¥</span>
              <span class="price-value">{{ product.price.toFixed(2) }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 主内容区域 -->
    <div class="content-area">
      <!-- 左侧分类侧边栏 -->
      <aside class="side-bar">
        <h3>分类</h3>
        <div class="cat-row">
          <span>电脑</span><span>/</span><span>办公</span><span>/</span><span>文具</span>
        </div>
        <div class="cat-row">
          <span>工业品</span><span>/</span><span>商业</span><span>/</span><span>农业</span><span>/</span><span>定制</span>
        </div>
        <div class="cat-row">
          <span>家电</span><span>/</span><span>手机</span><span>/</span><span>通信</span><span>/</span><span>数码</span>
        </div>
        <div class="cat-row">
          <span>家具</span><span>/</span><span>家装</span><span>/</span><span>家居</span><span>/</span><span>厨具</span>
        </div>
        <div class="cat-row">
          <span>女装</span><span>/</span><span>男装</span><span>/</span><span>内衣</span><span>/</span><span>配饰</span>
        </div>
        <div class="cat-row">
          <span>女鞋</span><span>/</span><span>男鞋</span><span>/</span><span>运动</span><span>/</span><span>户外</span>
        </div>
        <div class="cat-row">
          <span>汽车</span><span>/</span><span>珠宝</span><span>/</span><span>文玩</span><span>/</span><span>箱包</span>
        </div>
        <div class="cat-row">
          <span>食品</span><span>/</span><span>生鲜</span><span>/</span><span>酒类</span><span>/</span><span>健康</span>
        </div>
        <div class="cat-row">
          <span>母婴</span><span>/</span><span>童装</span><span>/</span><span>玩具</span><span>/</span><span>宠物</span>
        </div>
        <div class="cat-row">
          <span>美妆</span><span>/</span><span>个护</span><span>/</span><span>家清</span><span>/</span><span>香氛</span>
        </div>
        <div class="cat-row">
          <span>娱乐</span><span>/</span><span>图书</span><span>/</span><span>乐器</span><span>/</span><span>鲜花</span>
        </div>
        
        <!-- 猜你喜欢区域 -->
        <div class="guess-area">
          <span class="dot"></span>
          <span>猜你喜欢</span>
          <span class="sub-text">精选好物推荐</span>
        </div>
        
        <!-- 热门分类商品推荐 -->
        <div v-if="topCategoryProducts.length > 0" class="top-category-section">
          <div class="top-category-products">
            <div class="top-cat-item" v-for="product in topCategoryProducts" :key="product.id" @click="showProductDetail(product)">
              <img :src="product.image" :alt="product.name" />
              <p class="top-cat-product-name">{{ product.name }}</p>
            </div>
          </div>
        </div>
      </aside>

      <!-- 右侧主内容区 -->
      <main class="main-part">
        <!-- 轮播图和今日特惠 -->
        <div class="banner-row">
          <!-- 轮播图区域 -->
          <div class="banner">
            <div class="carousel" @mouseenter="handleMouseEnter" @mouseleave="handleMouseLeave">
              <div class="carousel-track" :style="{ transform: `translateX(-${currentSlide * 100}%)` }">
                <div class="carousel-slide" v-for="(slide, index) in slides" :key="index">
                  <img :src="slide.image" :alt="slide.title" />
                  <div class="slide-caption">{{ slide.title }}</div>
                </div>
              </div>
              <button class="carousel-prev" @click="prevSlide">‹</button>
              <button class="carousel-next" @click="nextSlide">›</button>
              <div class="carousel-indicators">
                <span 
                  v-for="(_, index) in slides" 
                  :key="index" 
                  :class="['indicator', { active: currentSlide === index }]"
                  @click="goToSlide(index)"
                ></span>
              </div>
            </div>
          </div>
          
          <!-- 今日特惠 -->
          <div class="daily-deal">
            <h4>今日特惠</h4>
            <div class="deal-item" v-for="item in sideItems" :key="item.id">
              <img :src="item.image" :alt="item.name" />
              <div class="deal-info">
                <p>{{ item.name }}</p>
                <p class="deal-price">{{ item.price }}</p>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 商品展示区域 -->
        <div class="goods-section">
          <div class="section-header">
            <h3 class="section-title">热门商品</h3>
            <a href="#" class="more-link">查看更多 ›</a>
          </div>
          <div class="goods-grid">
            <div v-if="products.length === 0" class="empty-tip">暂无商品数据</div>
            <div v-else class="product-card" v-for="product in products" :key="product.id">
              <div class="product-img-wrap" @click="showProductDetail(product)">
                <img :src="product.image" :alt="product.name" class="product-img" />
                <div v-if="product.stock < 10 && product.stock > 0" class="stock-tag">仅剩{{ product.stock }}件</div>
                <div v-if="product.stock === 0" class="sold-out-tag">已售罄</div>
                <span 
                  :class="['fav-btn', { active: isFavorited(product) }]" 
                  @click.stop="toggleFavorite(product)"
                >
                  ♥
                </span>
              </div>
              <div class="product-info" @click="showProductDetail(product)">
                <p class="product-title">{{ product.name }}</p>
                <div class="price-row">
                  <span class="price-symbol">¥</span>
                  <span class="price-value">{{ product.price.toFixed(2) }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </main>
    </div>

    <!-- 商品详情弹窗 -->
    <div v-if="showDetailModal" class="modal-overlay" @click="closeDetailModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>{{ selectedProduct?.name || '商品详情'}}</h3>
          <span class="modal-close" @click="closeDetailModal">×</span>
        </div>
        <div class="modal-body">
          <div class="product-detail">
            <div class="detail-img-wrap">
              <img :src="selectedProduct?.image" :alt="selectedProduct?.name" class="detail-img" />
            </div>
            <div class="detail-info">
              <p class="detail-price">¥{{ selectedProduct?.price?.toFixed(2) }}</p>
              <p class="detail-stock">库存: {{ selectedProduct?.stock }}</p>
              <p class="detail-category">分类: {{ selectedProduct?.category || '未分类' }}</p>
              <div class="detail-desc">
                <h4>商品描述</h4>
                <p>{{ selectedProduct?.description || '暂无描述' }}</p>
              </div>
              <div class="detail-buttons">
                <button 
                  :class="['btn-fav', { active: isFavorited(selectedProduct) }]" 
                  @click="toggleFavorite(selectedProduct)"
                >
                  {{ isFavorited(selectedProduct) ? '已收藏' : '收藏' }}
                </button>
                <button 
                  :class="['btn-add-cart', { disabled: selectedProduct?.stock <= 0 }]" 
                  @click="selectedProduct?.stock > 0 && addToCart(selectedProduct)"
                >加入购物车</button>
                <button 
                  :class="['btn-buy-now', { disabled: selectedProduct?.stock <= 0 }]" 
                  @click="selectedProduct?.stock > 0 && showOrderConfirmModal(selectedProduct)"
                >立即购买</button>
              </div>
              
              <div class="product-reviews">
                <h4>发表评价</h4>
                <textarea 
                  v-model="reviewContent" 
                  class="review-textarea" 
                  placeholder="说说您对这个商品的看法吧...">
                </textarea>
                <button class="btn-submit-review" @click="submitReview">发表评价</button>
                
                <h4 class="reviews-title">用户评价 ({{ reviews.length }})</h4>
                <div v-if="reviews.length === 0" class="no-reviews">暂无评价，快来发表第一条评价吧~</div>
                <div v-else class="reviews-list">
                  <div class="review-item" v-for="review in reviews" :key="review.id">
                    <div class="review-header">
                      <span class="review-username">{{ review.customerName || review.username || '匿名用户' }}</span>
                      <span class="review-time">{{ formatReviewDate(review.submittedAt || review.createdAt) }}</span>
                    </div>
                    <div class="review-content">{{ review.content || review.reviewText }}</div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 确认订单弹窗 -->
    <div v-if="showConfirmOrderModal" class="modal-overlay" @click="closeOrderConfirmModal">
      <div class="confirm-order-modal" @click.stop>
        <div class="modal-header">
          <h3>确认订单</h3>
          <span class="modal-close" @click="closeOrderConfirmModal">×</span>
        </div>
        <div class="modal-body">
          <div class="order-section">
            <h4 class="section-title">收货地址</h4>
            <div v-if="addresses.length === 0" class="no-address">
              <p>暂无收货地址，请先在"我的"页面添加收货地址</p>
              <button @click="goToProfile" class="add-address-btn">去添加地址</button>
            </div>
            <div v-else class="address-options">
              <label 
                v-for="(addr, index) in addresses" 
                :key="index" 
                class="address-option"
              >
                <input type="radio" v-model="selectedAddressIndex" :value="index" />
                <div class="address-info">
                  <span class="address-name">{{ addr.name }} {{ addr.phone }}</span>
                  <span class="address-detail">{{ addr.province }} {{ addr.city }} {{ addr.district }} {{ addr.detail }}</span>
                </div>
              </label>
            </div>
          </div>

          <div class="order-section">
            <h4 class="section-title">商品清单</h4>
            <div class="order-product">
              <img :src="orderProduct?.image" :alt="orderProduct?.name" class="order-product-img" />
              <div class="order-product-info">
                <p class="order-product-name">{{ orderProduct?.name }}</p>
                <p class="order-product-quantity">数量: 1</p>
              </div>
              <p class="order-product-price">¥{{ orderProduct?.price?.toFixed(2) }}</p>
            </div>
          </div>

          <div class="order-section">
            <h4 class="section-title">支付方式</h4>
            <div class="payment-options">
              <label class="payment-option">
                <input type="radio" name="payment" value="wechat" v-model="selectedPayment" />
                <span>微信支付</span>
              </label>
            </div>
          </div>

          <div class="order-total">
            <span class="total-label">订单总额:</span>
            <span class="total-price">¥{{ orderProduct?.price?.toFixed(2) }}</span>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn-cancel" @click="closeOrderConfirmModal">取消</button>
          <button class="btn-confirm" @click="confirmPayment">确认支付</button>
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
      <a href="#" class="mobile-nav-item" @click.prevent="goFav">
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

// 用户相关
let nowUser = ref("");
let showOpts = ref(false);

// 轮播图
let currentSlide = ref(0);
let autoPlayTimer = null;

// 商品数据
let products = ref([]);

// 搜索功能
let searchKeyword = ref("");
let isSearchActive = ref(false);

const filteredProducts = computed(() => {
  if (!searchKeyword.value.trim()) {
    return [];
  }
  const keyword = searchKeyword.value.toLowerCase();
  return products.value.filter(product => 
    product.name && product.name.toLowerCase().includes(keyword)
  );
});

function handleSearch() {
  if (!searchKeyword.value.trim()) {
    return;
  }
  isSearchActive.value = true;
  console.log("搜索关键词:", searchKeyword.value);
}

function clearSearch() {
  searchKeyword.value = "";
  isSearchActive.value = false;
}

// 商品详情弹窗
let showDetailModal = ref(false);
let selectedProduct = ref(null);

// 评价功能
let reviewContent = ref("");
let reviews = ref([]);

// 确认订单弹窗
let showConfirmOrderModal = ref(false);
let orderProduct = ref(null);
let selectedPayment = ref("wechat");
let selectedAddressIndex = ref(0);

interface Address {
  name: string;
  phone: string;
  province: string;
  city: string;
  district: string;
  detail: string;
}

const addresses = ref<Address[]>([]);

// 收藏数据
let favorites = ref([]);

// 热门分类推荐
let topCategoryName = ref("");
let topCategoryProducts = ref([]);

// 静态资源路径：跟随部署基路径（支持部署在子目录，如 GitHub Pages）
const asset = (path: string) => `${import.meta.env.BASE_URL.replace(/\/$/, "")}${path}`;

// 轮播图配置
const slides = ref([
  { image: asset("/images/qnlss.png"), title: "千年老山参" },
  { image: asset("/images/ysfwj.jpg"), title: "野生蜂王浆" },
  { image: asset("/images/rxhsw.jpg"), title: "人形何首乌" },
  { image: asset("/images/jpyw.jpg"), title: "极品燕窝" },
  { image: asset("/images/jtgj.jpg"), title: "九天贡胶" },
]);

// 侧边推荐数据
const sideItems = ref([
  { id: 1, name: "野生灵芝", price: "¥599", image: asset("/images/qnlss.png") },
  { id: 2, name: "鹿茸片", price: "¥399", image: asset("/images/ysfwj.jpg") },
  { id: 3, name: "冬虫夏草", price: "¥899", image: asset("/images/rxhsw.jpg") },
  { id: 4, name: "铁皮石斛", price: "¥299", image: asset("/images/jpyw.jpg") },
]);

// 加载商品列表
async function loadProducts() {
  try {
    const response = await fetch("http://localhost:8081/api/goods");
    const data = await response.json();
    if (data.code === 200) {
      products.value = data.data;
      calculateTopCategory();
      // 成功加载后，清除 localStorage 中的旧商品缓存，避免显示已删除的商品或乱码数据
      localStorage.removeItem("products");
    }
  } catch (error) {
    console.error("加载商品失败:", error);
    // 失败时不使用 localStorage，避免显示乱码数据
    console.log("后端服务不可用，商品列表为空");
    products.value = [];
  }
}

// 加载收藏列表
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

// 检查商品是否已收藏
function isFavorited(product) {
  if (!product) return false;
  return favorites.value.some(fav => fav.goodsId === product.id);
}

// 切换收藏状态
async function toggleFavorite(product) {
  if (!nowUser.value) {
    alert("请先登录");
    return;
  }

  const token = localStorage.getItem("token");
  const isAlreadyFavorited = isFavorited(product);

  try {
    const method = isAlreadyFavorited ? "DELETE" : "POST";
    const response = await fetch(`http://localhost:8081/api/favorites/${product.id}`, {
      method: method,
      headers: {
        "x-auth-token": token
      }
    });
    const data = await response.json();
    
    if (data.code === 200) {
      if (isAlreadyFavorited) {
        favorites.value = favorites.value.filter(fav => fav.goodsId !== product.id);
        alert("已取消收藏");
      } else {
        favorites.value.push(data.data);
        alert("收藏成功");
      }
    } else {
      alert(data.message || "操作失败");
    }
  } catch (error) {
    console.error("收藏操作失败:", error);
    alert("操作失败，请检查网络连接");
  }
}

// 计算商品数量最多的分类
function calculateTopCategory() {
  if (products.value.length === 0) {
    topCategoryProducts.value = [];
    topCategoryName.value = "";
    return;
  }
  
  const categoryCount: Record<string, any[]> = {};
  
  products.value.forEach(product => {
    const category = product.category || '其他';
    if (!categoryCount[category]) {
      categoryCount[category] = [];
    }
    categoryCount[category].push(product);
  });
  
  let maxCount = 0;
  let topCategory = '';
  
  Object.keys(categoryCount).forEach(category => {
    if (categoryCount[category].length > maxCount) {
      maxCount = categoryCount[category].length;
      topCategory = category;
    }
  });
  
  topCategoryName.value = topCategory;
  topCategoryProducts.value = categoryCount[topCategory];
  console.log("热门分类:", topCategory, "商品数:", maxCount);
}

// 加入购物车
function addCart(product) {
  if (!nowUser.value) {
    alert("请先登录哦");
    return;
  }
  
  let cart = localStorage.getItem("cart");
  cart = cart ? JSON.parse(cart) : [];
  
  let exists = cart.find(item => item.id === product.id);
  if (exists) {
    exists.quantity = (exists.quantity || 1) + 1;
  } else {
    cart.push({ ...product, quantity: 1 });
  }
  
  localStorage.setItem("cart", JSON.stringify(cart));
  alert("已加入购物车");
}

// 轮播图切换
function nextSlide() {
  currentSlide.value = (currentSlide.value + 1) % slides.value.length;
}

function prevSlide() {
  currentSlide.value = (currentSlide.value - 1 + slides.value.length) % slides.value.length;
}

function goToSlide(index) {
  currentSlide.value = index;
}

// 自动播放控制
function startAutoPlay() {
  if (autoPlayTimer) return;
  autoPlayTimer = setInterval(() => {
    nextSlide();
  }, 4000); // 4秒切换一次，不要太快
}

function stopAutoPlay() {
  if (autoPlayTimer) {
    clearInterval(autoPlayTimer);
    autoPlayTimer = null;
  }
}

function handleMouseEnter() {
  stopAutoPlay();
}

function handleMouseLeave() {
  startAutoPlay();
}

// 页面加载时执行
onMounted(() => {
  console.log("=== 首页加载 ===");
  console.log("localStorage内容:", {
    loginUser: localStorage.getItem("loginUser"),
    token: localStorage.getItem("token"),
    isAdmin: localStorage.getItem("isAdmin")
  });
  
  // 检查是否是管理员登录，如果是管理员，跳转到管理员后台
  const isAdminFlag = localStorage.getItem("isAdmin");
  if (isAdminFlag === "true") {
    console.log("检测到管理员登录，跳转到管理员后台");
    window.location.href = "/admin";
    return;
  }
  
  // 先清除旧的商品缓存，确保总是从后端获取最新数据
  localStorage.removeItem("products");
  
  let saved = localStorage.getItem("loginUser");
  if (saved) {
    nowUser.value = saved;
    console.log("已登录用户:", saved);
    loadFavorites();
    loadAddresses();
  } else {
    console.log("未检测到登录状态");
  }
  
  loadProducts();
  startAutoPlay();
});

async function loadAddresses() {
  try {
    const token = localStorage.getItem('token');
    const res = await fetch('http://localhost:8081/api/addresses', {
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token || ''}`
      }
    });
    const data = await res.json();
    if (data.code === 200) {
      addresses.value = data.data.map((addr: any) => ({
        id: addr.id,
        name: addr.name,
        phone: addr.phone,
        province: addr.province || '',
        city: addr.city || '',
        district: addr.district || '',
        detail: addr.address || addr.detail || ''
      })) || [];
    }
  } catch (error) {
    console.error('加载地址失败:', error);
  }
}

function goToProfile() {
  closeOrderConfirmModal();
  router.push("/profile");
}

// 登录相关操作
function handleLogin() {
  if (nowUser.value) {
    showOpts.value = !showOpts.value;
  } else {
    router.push("/login");
  }
}

function changeUser() {
  if (!confirm("确定要切换用户吗？")) return;
  showOpts.value = false;
  clearLogin();
  router.push("/login");
}

function clearLogin() {
  localStorage.removeItem("loginUser");
  localStorage.removeItem("token");
  localStorage.removeItem("isAdmin");
  nowUser.value = "";
}

function quitLogin() {
  showOpts.value = false;
  if (confirm("确定要退出登录吗？")) {
    clearLogin();
    alert("已退出登录");
    router.push("/login");
  }
}

// 页面跳转
function goProfile() {
  router.push("/profile");
}

function goCart() {
  router.push("/cart");
}

function goFav() {
  router.push("/favorites");
}

function goHelp() {
  router.push("/help");
}

// 加入购物车
async function addToCart(product: any) {
  if (!nowUser.value) {
    alert("请先登录哦");
    return;
  }
  
  // 确认加入购物车
  const confirmed = confirm(`确认要将"${product.name}"加入购物车吗？`);
  if (!confirmed) {
    return;
  }
  
  const token = localStorage.getItem("token");
  
  try {
    const response = await fetch("http://localhost:8081/api/cart", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "x-auth-token": token || ""
      },
      body: JSON.stringify({
        goodsId: product.id,
        quantity: 1
      })
    });
    
    const data = await response.json();
    
    if (data.code === 200) {
      alert("已加入购物车");
    } else {
      alert(data.message || "添加失败");
    }
  } catch (error) {
    console.error("添加购物车失败:", error);
    alert("添加失败，请检查网络连接");
  }
}

// 显示确认订单弹窗
function showOrderConfirmModal(product: any) {
  orderProduct.value = product;
  selectedPayment.value = "wechat";
  selectedAddressIndex.value = 0;
  loadAddresses();
  showConfirmOrderModal.value = true;
}

// 关闭确认订单弹窗
function closeOrderConfirmModal() {
  showConfirmOrderModal.value = false;
  orderProduct.value = null;
}

// 确认支付
async function confirmPayment() {
  if (!nowUser.value) {
    alert("请先登录哦");
    return;
  }
  
  if (addresses.value.length === 0) {
    alert("请先添加收货地址");
    return;
  }
  
  const selectedAddr = addresses.value[selectedAddressIndex.value];
  if (!selectedAddr) {
    alert("请选择收货地址");
    return;
  }
  
  const token = localStorage.getItem("token");
  
  try {
    const shippingAddress = selectedAddr.province + " " + selectedAddr.city + " " + selectedAddr.district + " " + selectedAddr.detail;
    const url = `http://localhost:8081/api/orders/direct?goodsId=${orderProduct.value.id}&quantity=1&paymentMethod=${selectedPayment.value}&shippingName=${encodeURIComponent(selectedAddr.name)}&shippingPhone=${encodeURIComponent(selectedAddr.phone)}&shippingAddress=${encodeURIComponent(shippingAddress)}`;
    const response = await fetch(url, {
      method: "POST",
      headers: {
        "x-auth-token": token || ""
      }
    });
    
    const data = await response.json();
    
    if (data.code === 200) {
      closeOrderConfirmModal();
      closeDetailModal();
      alert("支付成功！");
    } else {
      alert(data.message || "支付失败");
    }
  } catch (error) {
    console.error("支付失败:", error);
    alert("支付失败，请检查网络连接");
  }
}

// 商品详情弹窗
function showProductDetail(product) {
  selectedProduct.value = product;
  showDetailModal.value = true;
  reviewContent.value = "";
  loadReviews(product.id);
}

function closeDetailModal() {
  showDetailModal.value = false;
  selectedProduct.value = null;
  reviewContent.value = "";
  reviews.value = [];
}

// 加载评价
async function loadReviews(goodsId) {
  if (!goodsId) return;
  try {
    const response = await fetch(`http://localhost:8081/api/reviews/goods/${goodsId}`);
    const data = await response.json();
    if (data.code === 200) {
      reviews.value = data.data || [];
    } else {
      reviews.value = [];
    }
  } catch (error) {
    console.error("加载评价失败:", error);
    reviews.value = [];
  }
}

// 提交评价
async function submitReview() {
  if (!nowUser.value) {
    alert("请先登录哦");
    return;
  }
  if (!reviewContent.value.trim()) {
    alert("请输入评价内容");
    return;
  }
  const token = localStorage.getItem("token");
  try {
    const response = await fetch("http://localhost:8081/api/reviews", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "x-auth-token": token || ""
      },
      body: JSON.stringify({
        goodsId: selectedProduct.value?.id,
        content: reviewContent.value.trim(),
        rating: 5
      })
    });
    const data = await response.json();
    if (data.code === 200) {
      alert("评价发表成功！");
      reviewContent.value = "";
      loadReviews(selectedProduct.value?.id);
    } else {
      alert("发表失败: " + (data.message || "未知错误"));
    }
  } catch (error) {
    console.error("发表评价失败:", error);
    alert("操作失败，请检查网络连接");
  }
}

// 格式化评价时间
function formatReviewDate(dateStr) {
  if (!dateStr) return "";
  const d = new Date(dateStr);
  return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')} ${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}`;
}
</script>

<style scoped>
/* 首页整体样式 */
.home {
  min-height: 100vh;
  background: #fff;
}

/* 顶部导航栏样式 */
.top-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 30px;
  background: linear-gradient(90deg, #333);
}

/* 左侧区域 */
.left-part {
  flex: 1;
}

/* 登录按钮区域 */
.login-box {
  position: relative;
  display: inline-block;
}

.login-box span {
  display: inline-block;
  padding: 8px 20px;
  background: linear-gradient(135deg, #6b6b6b);
  color: #fff;
  border-radius: 20px;
  font-size: 14px;
  cursor: pointer;
}

/* 登录后操作菜单 */
.login-options {
  position: absolute;
  top: calc(100% + 8px);
  left: 0;
  min-width: 100px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 15px rgba(0,0,0,0.15);
  padding: 5px 0;
  z-index: 100;
}

.login-options span {
  display: block;
  padding: 10px 20px;
  background: none;
  color: #333;
  border-radius: 0;
}

.login-options span:hover {
  background: #f5f5f5;
}

/* 中间Logo区域 */
.center-part h1 {
  margin: 0;
  font-size: 28px;
  color: #fff;
  font-weight: 700;
  text-shadow: 0 1px 2px rgba(0,0,0,0.2);
}

/* 右侧导航区域 */
.right-part {
  flex: 3;
  display: flex;
  justify-content: flex-end;
  gap: 25px;
}

.nav-link {
  text-decoration: none;
  color: #fff;
  font-size: 15px;
}

.nav-link:hover {
  color: #ffe4cc;
}

/* 搜索框区域 */
.search-box-wrap {
  padding: 15px;
  background: transparent;
}

.search-input-box {
  display: flex;
  align-items: center;
  max-width: 650px;
  margin: 0 auto;
  background: #fff;
  border-radius: 0;
  box-shadow: 0 2px 8px rgba(255, 68, 0, 0.2);
  border: 2px solid #7c7b7b;
}

/* 分类选择框 */
.select-box {
  padding: 11px 15px;
  border: none;
  background: #f5f5f5;
  border-radius: 0;
  border-right: 1px solid #ddd;
}

/* 搜索输入框 */
.search-input {
  flex: 1;
  padding: 11px;
  border: none;
  outline: none;
  font-size: 14px;
}

/* 搜索按钮 */
.search-button {
  padding: 11px 35px;
  background: linear-gradient(90deg, #7c7b7b);
  border: none;
  color: #fff;
  border-radius: 0;
  cursor: pointer;
  font-weight: 600;
  font-size: 15px;
}

/* 主内容区域布局 */
.content-area {
  display: flex;
  padding: 25px;
  gap: 25px;
}

/* 分类侧边栏 */
.side-bar {
  width: 200px;
  background: rgba(255,255,255,0.95);
  padding: 15px;
  border-radius: 10px;
}

.side-bar h3 {
  margin: 0 0 15px;
  font-size: 16px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}

/* 分类行 */
.cat-row {
  display: flex;
  flex-wrap: wrap;
  margin-bottom: 10px;
  font-size: 13px;
  color: #666;
}

.cat-row span {
  margin: 0 3px;
}

/* 猜你喜欢区域 */
.guess-area {
  margin-top: 20px;
  padding: 12px 15px;
  background: linear-gradient(135deg, #fff5f0 0%, #fff 100%);
  border-radius: 10px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.3s ease;
}



.dot {
  display: inline-block;
  width: 10px;
  height: 10px;
  background: linear-gradient(135deg, #ff6600 0%, #ff4400 100%);
  border-radius: 50%;
  margin-right: 8px;
  box-shadow: 0 2px 6px rgba(255, 102, 0, 0.3);
}

.sub-text {
  color: #999;
  margin-left: 8px;
  font-size: 12px;
}

/* 热门分类商品推荐 */
.top-category-section {
  margin-top: 20px;
  background: rgba(255,255,255,0.95);
  border-radius: 10px;
  padding: 12px 15px;
}

.top-category-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}

.top-cat-name {
  font-size: 14px;
  font-weight: 600;
  color: #ff4400;
}

.top-cat-count {
  font-size: 12px;
  color: #999;
}

.top-category-products {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.top-cat-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px;
  background: #f8f9fa;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
}



.top-cat-item img {
  width: 40px;
  height: 40px;
  object-fit: cover;
  border-radius: 4px;
}

.top-cat-product-name {
  flex: 1;
  font-size: 12px;
  color: #333;
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.top-cat-product-price {
  font-size: 13px;
  font-weight: 600;
  color: #ff4400;
  margin: 0;
}

/* 主内容区 */
.main-part {
  flex: 1;
}

/* 轮播图和特惠区域 */
.banner-row {
  display: flex;
  justify-content: space-between;
}

/* 轮播图区域 */
.banner {
  background: rgba(255,255,255,0.95);
  width: 1200px;
  height: 400px;
  border-radius: 15px;
  margin-bottom: 25px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0,0,0,0.1);
}

/* 今日特惠区域 */
.daily-deal {
  width: 220px;
  background: rgba(255,255,255,0.95);
  border-radius: 15px;
  padding: 15px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.1);
}

.daily-deal h4 {
  font-size: 16px;
  font-weight: 700;
  color: #ff4400;
  margin: 0 0 15px;
  padding-bottom: 10px;
  border-bottom: 2px solid #ff4400;
}

.deal-item {
  display: flex;
  gap: 10px;
  padding: 12px 0;
  border-bottom: 1px dashed #eee;
  cursor: pointer;
}

.deal-item:last-child {
  border-bottom: none;
}

.deal-item img {
  width: 50px;
  height: 50px;
  object-fit: cover;
  border-radius: 6px;
}

.deal-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.deal-info p:first-child {
  margin: 0 0 5px;
  font-size: 13px;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.deal-price {
  margin: 0;
  font-size: 15px;
  font-weight: 700;
  color: #ff4400;
}
.deal-more:hover {
  color: #ff4400;
}

.carousel {
  position: relative;
  width: 100%;
  height: 100%;
}

.carousel-track {
  display: flex;
  transition: transform 0.5s ease;
  height: 100%;
}

.carousel-slide {
  position: relative;
  min-width: 100%;
  height: 100%;
}

.carousel-slide img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.slide-caption {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: linear-gradient(transparent, rgba(0,0,0,0.6));
  color: #fff;
  padding: 20px 30px;
  font-size: 20px;
  font-weight: bold;
}

.carousel-prev,
.carousel-next {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  background: rgba(255,255,255,0.8);
  border: none;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  font-size: 24px;
  cursor: pointer;
  z-index: 10;
  transition: all 0.3s;
}

.carousel-prev:hover,
.carousel-next:hover {
  background: rgba(255,255,255,1);
  box-shadow: 0 2px 10px rgba(0,0,0,0.2);
}

.carousel-prev {
  left: 15px;
}

.carousel-next {
  right: 15px;
}

.carousel-indicators {
  position: absolute;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 10px;
  z-index: 10;
}

.indicator {
  width: 12px;
  height: 12px;
  background: rgba(255,255,255,0.5);
  border-radius: 50%;
  cursor: pointer;
  transition: all 0.3s;
}

.indicator.active {
  background: #fff;
  transform: scale(1.2);
}

/* 商品展示区域 */
.goods-section {
  background: rgba(255,255,255,0.95);
  padding: 20px;
  border-radius: 15px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.08);
  margin-top: 20px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 10px 15px;
  border-bottom: 2px solid #ff4400;
  margin-bottom: 20px;
}

.section-title {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: #333;
  position: relative;
  padding-left: 12px;
}

.section-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 20px;
  background: linear-gradient(180deg, #ff4400 0%, #ff6600 100%);
  border-radius: 2px;
}

.more-link {
  font-size: 14px;
  color: #999;
  text-decoration: none;
}

.more-link:hover {
  color: #ff4400;
}

.clear-search-btn {
  font-size: 14px;
  color: #ff4400;
  background: none;
  border: none;
  cursor: pointer;
  padding: 0;
}

.clear-search-btn:hover {
  text-decoration: underline;
}

/* 搜索结果区域 */
.search-results-section {
  padding: 20px 80px;
  background: #f5f5f5;
}

.search-results-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.search-results-title {
  font-size: 18px;
  font-weight: bold;
  color: #333;
  margin: 0;
}

.search-results-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 15px;
  min-height: 100px;
}

/* 商品网格 */
.goods-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 15px;
  min-height: 200px;
}

.empty-tip {
  grid-column: 1 / -1;
  text-align: center;
  color: #999;
  font-size: 14px;
  padding: 80px 0;
}

/* 商品卡片 */
.product-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
  transition: all 0.3s ease;
  border: 1px solid #f0f0f0;
}

.product-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 6px 20px rgba(0,0,0,0.1);
  border-color: #ffd6b8;
}

.product-img-wrap {
  position: relative;
  padding-top: 100%;
  background: linear-gradient(135deg, #fafafa 0%, #f5f5f5 100%);
}

.product-img {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: contain;
  padding: 10px;
}

.fav-btn {
  position: absolute;
  top: 10px;
  right: 10px;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.9);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  cursor: pointer;
  color: #999;
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  z-index: 10;
}

.fav-btn:hover {
  color: #ff4d4f;
  transform: scale(1.1);
}

.fav-btn.active {
  color: #ff4d4f;
  background: rgba(255, 77, 79, 0.1);
}

.stock-tag,
.sold-out-tag {
  position: absolute;
  top: 8px;
  left: 8px;
  padding: 3px 8px;
  font-size: 12px;
  border-radius: 3px;
}

.stock-tag {
  background: rgba(255, 68, 0, 0.9);
  color: #fff;
}

.sold-out-tag {
  background: rgba(153, 153, 153, 0.9);
  color: #fff;
}

.product-info {
  padding: 12px;
}

.product-title {
  margin: 0 0 10px;
  font-size: 13px;
  color: #333;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 39px;
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
  font-size: 16px;
  color: #ff4400;
  font-weight: 700;
  font-family: Arial, sans-serif;
}

/* 商品详情弹窗 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: #fff;
  border-radius: 12px;
  width: 850px;
  max-width: 95%;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #eee;
}

.modal-header h3 {
  margin: 0;
  font-size: 18px;
  color: #333;
}

.modal-close {
  font-size: 28px;
  cursor: pointer;
  color: #999;
  transition: color 0.3s;
  line-height: 1;
}

.modal-close:hover {
  color: #333;
}

.modal-body {
  padding: 24px;
}

.product-detail {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.detail-img-wrap {
  display: flex;
  justify-content: center;
}

.detail-img {
  width: 320px;
  height: 320px;
  object-fit: contain;
  border-radius: 10px;
  background: #f8f9fa;
}

.detail-info {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.detail-price {
  font-size: 28px;
  font-weight: 700;
  color: #ff4400;
  margin: 0;
}

.detail-stock,
.detail-category {
  font-size: 14px;
  color: #666;
  margin: 0;
}

.detail-desc {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #eee;
}

.detail-desc h4 {
  margin: 0 0 8px;
  font-size: 15px;
  color: #333;
}

.detail-desc p {
  margin: 0;
  font-size: 14px;
  color: #666;
  line-height: 1.6;
}

.detail-buttons {
  display: flex;
  gap: 12px;
  margin-top: 20px;
}

.btn-fav {
  padding: 12px 20px;
  border: 1px solid #d9d9d9;
  background: #fff;
  color: #666;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-fav:hover {
  border-color: #ff4d4f;
  color: #ff4d4f;
}

.btn-fav.active {
  background: #fff1f0;
  border-color: #ff4d4f;
  color: #ff4d4f;
}

.btn-add-cart {
  flex: 1;
  padding: 12px 20px;
  border: 1px solid #ff4400;
  background: #fff;
  color: #ff4400;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-add-cart:hover {
  background: #fff5f0;
}

.btn-buy-now {
  flex: 1;
  padding: 12px 20px;
  border: none;
  background: linear-gradient(135deg, #ff6600 0%, #ff4400 100%);
  color: #fff;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-buy-now:hover {
  box-shadow: 0 4px 12px rgba(255, 68, 0, 0.4);
  transform: translateY(-2px);
}

.btn-add-cart.disabled,
.btn-buy-now.disabled {
  background: #e0e0e0 !important;
  border-color: #e0e0e0 !important;
  color: #999 !important;
  cursor: not-allowed !important;
  box-shadow: none !important;
  transform: none !important;
}

.btn-add-cart.disabled:hover,
.btn-buy-now.disabled:hover {
  background: #e0e0e0 !important;
  box-shadow: none !important;
  transform: none !important;
}

/* 确认订单弹窗样式 */
.confirm-order-modal {
  background: #fff;
  border-radius: 12px;
  width: 500px;
  max-width: 90%;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
}

.order-section {
  margin-bottom: 20px;
}

.section-title {
  font-size: 14px;
  color: #666;
  margin: 0 0 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #eee;
}

.no-address {
  padding: 20px;
  background: #f9f9f9;
  border-radius: 8px;
  text-align: center;
}

.no-address p {
  margin: 0 0 12px 0;
  color: #666;
}

.add-address-btn {
  padding: 8px 16px;
  background: #4a90d9;
  color: #fff;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
}

.add-address-btn:hover {
  background: #3d7bc6;
}

.address-options {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.address-option {
  display: flex;
  align-items: flex-start;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 8px;
  cursor: pointer;
}

.address-option:hover {
  border-color: #ff4400;
}

.address-option input[type="radio"] {
  margin-right: 12px;
  margin-top: 4px;
}

.address-info {
  flex: 1;
}

.address-name {
  display: block;
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
}

.address-detail {
  display: block;
  font-size: 13px;
  color: #666;
  line-height: 1.4;
}

.order-product {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 12px;
  background: #fafafa;
  border-radius: 8px;
}

.order-product-img {
  width: 80px;
  height: 80px;
  object-fit: contain;
  background: #fff;
  border-radius: 6px;
}

.order-product-info {
  flex: 1;
}

.order-product-name {
  margin: 0 0 8px;
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.order-product-quantity {
  margin: 0;
  font-size: 13px;
  color: #999;
}

.order-product-price {
  font-size: 18px;
  font-weight: 700;
  color: #ff4400;
}

.payment-options {
  display: flex;
  gap: 20px;
}

.payment-option {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 10px 15px;
  border: 1px solid #eee;
  border-radius: 6px;
  transition: all 0.3s;
}

.payment-option:hover {
  border-color: #ff4400;
  background: #fff5f0;
}

.payment-option input[type="radio"] {
  display: none;
}

.payment-option input[type="radio"]:checked + .payment-icon {
  color: #ff4400;
}

.payment-icon {
  font-size: 20px;
}

.order-total {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  background: #fff5f0;
  border-radius: 8px;
  margin-top: 10px;
}

.total-label {
  font-size: 16px;
  color: #666;
}

.total-price {
  font-size: 28px;
  font-weight: 700;
  color: #ff4400;
}

.modal-footer {
  display: flex;
  gap: 15px;
  padding: 20px 24px;
  border-top: 1px solid #eee;
}

.btn-cancel {
  flex: 1;
  padding: 12px 20px;
  border: 1px solid #ddd;
  background: #fff;
  color: #666;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-cancel:hover {
  background: #f5f5f5;
}

.btn-confirm {
  flex: 1;
  padding: 12px 20px;
  border: none;
  background: linear-gradient(135deg, #ff6600 0%, #ff4400 100%);
  color: #fff;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-confirm:hover {
  box-shadow: 0 4px 12px rgba(255, 68, 0, 0.4);
  transform: translateY(-2px);
}

/* 商品评价模块样式 */
.product-reviews {
  margin-top: 30px;
  padding-top: 25px;
  padding-bottom: 10px;
  border-top: 1px solid #eee;
}

.product-reviews h4 {
  margin: 0 0 18px;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.review-textarea {
  width: 100%;
  min-height: 100px;
  padding: 15px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
  resize: vertical;
  outline: none;
  box-sizing: border-box;
  transition: border-color 0.3s, box-shadow 0.3s;
  line-height: 1.6;
}

.review-textarea:focus {
  border-color: #ff6600;
  box-shadow: 0 0 0 3px rgba(255, 102, 0, 0.1);
}

.btn-submit-review {
  margin-top: 16px;
  padding: 12px 30px;
  background: linear-gradient(135deg, #ff6600 0%, #ff4400 100%);
  color: #fff;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-submit-review:hover {
  box-shadow: 0 4px 12px rgba(255, 68, 0, 0.35);
  transform: translateY(-1px);
}

.reviews-title {
  margin: 28px 0 18px !important;
  padding-top: 20px;
  border-top: 1px dashed #eee;
}

.no-reviews {
  padding: 40px;
  text-align: center;
  color: #999;
  font-size: 14px;
  background: #fafafa;
  border-radius: 8px;
}

.reviews-list {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.review-item {
  padding: 20px;
  background: #fafafa;
  border-radius: 10px;
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.review-username {
  font-size: 15px;
  font-weight: 600;
  color: #333;
}

.review-time {
  font-size: 13px;
  color: #999;
}

.review-content {
  font-size: 14px;
  color: #555;
  line-height: 1.8;
  margin: 0;
}

/* 移动端底部导航默认隐藏（大屏幕） */
.mobile-nav {
  display: none;
}

/* 移动端响应式样式 */
@media (max-width: 768px) {
  /* 顶部导航栏 */
  .top-bar {
    padding: 10px 15px;
    flex-direction: row;
    justify-content: space-between;
    align-items: center;
  }
  
  .left-part {
    width: auto;
    text-align: left;
  }
  
  .center-part h1 {
    font-size: 18px;
  }
  
  .right-part {
    display: none;
  }
  
  /* 搜索框 */
  .search-box-wrap {
    padding: 10px 15px;
  }
  
  .search-input-box {
    max-width: 100%;
  }
  
  /* 主内容区域 */
  .content-area {
    flex-direction: column;
    padding: 10px 15px;
    gap: 15px;
  }
  
  /* 分类侧边栏 - 移动端隐藏 */
  .side-bar {
    display: none;
  }
  
  /* 轮播图区域 - 移动端显示 */
  .banner-row {
    flex-direction: column;
  }
  
  .banner {
    width: 100%;
    height: 180px;
    margin-bottom: 15px;
  }
  
  .slide-caption {
    font-size: 14px;
    padding: 12px 15px;
  }
  
  /* 今日特惠区域 - 移动端隐藏 */
  .daily-deal {
    display: none;
  }
  
  /* 商品网格 */
  .goods-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }
  
  .search-results-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }
  
  /* 商品卡片 */
  .product-card {
    border-radius: 10px;
  }
  
  .product-title {
    font-size: 12px;
    min-height: 36px;
  }
  
  .price-value {
    font-size: 14px;
  }
  
  /* 商品详情弹窗 */
  .modal-content {
    width: 95%;
    max-width: 95%;
  }
  
  .product-detail {
    flex-direction: column;
  }
  
  .detail-img {
    width: 100%;
    height: auto;
    max-height: 250px;
  }
  
  .detail-buttons {
    flex-direction: column;
  }
  
  /* 确认订单弹窗 */
  .confirm-order-modal {
    width: 95%;
    max-width: 95%;
  }
  
  /* 搜索结果区域 */
  .search-results-section {
    padding: 15px;
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
  
  /* 主内容区域留出底部导航空间 */
  .content-area {
    padding-bottom: 80px;
  }
}

/* 平板端响应式样式 */
@media (min-width: 769px) and (max-width: 1024px) {
  .top-bar {
    padding: 10px 20px;
  }
  
  .center-part h1 {
    font-size: 24px;
  }
  
  .right-part {
    gap: 15px;
  }
  
  .nav-link {
    font-size: 14px;
  }
  
  .banner {
    width: 100%;
    height: 280px;
  }
  
  .goods-grid {
    grid-template-columns: repeat(4, 1fr);
  }
  
  .search-results-grid {
    grid-template-columns: repeat(4, 1fr);
  }
  
  .side-bar {
    width: 160px;
  }
}
</style>