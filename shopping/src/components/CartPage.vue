
<template>
  <div class="cart-page">
    <header class="header">
      <div class="header-center">
        <h1>购物平台</h1>
      </div>
      <nav class="header-right">
        <a href="#" @click.prevent="goHome" class="nav-item">首页</a>
        <a href="#" @click.prevent="goProfile" class="nav-item">我的</a>
        <a href="#" class="nav-item active">购物车</a>
        <a href="#" @click.prevent="goFav" class="nav-item">收藏夹</a>
        <a href="#" @click.prevent="goHelp" class="nav-item">帮助中心</a>
      </nav>
    </header>

    <div class="cart-content">
      <div class="cart-header">
        <h2 class="cart-title"> 购物车</h2>
        <div class="cart-tools">
          <div class="search-box">
            <input 
              type="text" 
              v-model="searchKeyword" 
              placeholder="搜索商品..." 
              class="search-input"
            />
            <button v-if="searchKeyword" class="clear-btn" @click="searchKeyword = ''">×</button>
          </div>
        </div>
      </div>

      <div class="cart-body">
        <div v-if="filteredCartItems.length > 0" class="cart-items">
          <div v-for="(item, index) in filteredCartItems" :key="index" class="cart-item">
            <input type="checkbox" v-model="item.selected" />
            <div class="item-img">
              <img :src="item.image" :alt="item.name" />
            </div>
            <div class="item-info">
              <p class="item-name">{{ item.name }}</p>
              <div class="quantity-control">
                <button @click="updateQuantity(item, -1)" :disabled="item.quantity <= 1">-</button>
                <span>{{ item.quantity }}</span>
                <button @click="updateQuantity(item, 1)" :disabled="item.quantity >= (item.stock || 999)">+</button>
              </div>
            </div>
            <div class="item-total">
              <p>小计: ¥{{ (item.price * item.quantity).toFixed(2) }}</p>
            </div>
            <span class="remove-btn" @click="removeItem(item.goodsId)">删除</span>
          </div>
        </div>
        <el-empty v-else :image-size="100" :description="searchKeyword ? '没有找到匹配的商品' : '购物车是空的'" />
      </div>

      <div class="cart-footer">
        <label class="check-all">
          <input type="checkbox" v-model="selectAll" @change="toggleSelectAll" />
          <span>全选</span>
        </label>
        <div class="right-section">
          <div class="total-area">
            <span class="total-text">已选 {{ selectedCount }} 件商品</span>
            <span class="total-price">合计: ¥{{ totalPrice.toFixed(2) }}</span>
          </div>
          <button class="checkout-btn" @click="checkout">结算</button>
        </div>
      </div>
    </div>

    <div v-if="showPaymentModal" class="payment-modal" @click="closePaymentModal">
      <div class="payment-content" @click.stop>
        <div class="payment-header">
          <h3>确认订单</h3>
          <span class="close-btn" @click="closePaymentModal">×</span>
        </div>
        
        <div class="payment-body">
          <div class="order-summary">
            <h4>商品清单</h4>
            <div class="order-items">
              <div v-for="item in orderItems" :key="item.goodsId" class="order-item">
                <img :src="item.image" :alt="item.name" class="order-item-img" />
                <div class="order-item-info">
                  <p>{{ item.name }}</p>
                  <p>数量: {{ item.quantity }}</p>
                </div>
                <p class="order-item-price">¥{{ (item.price * item.quantity).toFixed(2) }}</p>
              </div>
            </div>
          </div>

          <div class="address-section">
            <h4>收货地址</h4>
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

          <div class="payment-method">
            <h4>支付方式</h4>
            <div class="methods">
              <label 
                v-for="method in paymentMethods" 
                :key="method.value" 
                class="method-label"
              >
                <input type="radio" v-model="selectedPaymentMethod" :value="method.value" />
                <span>{{ method.name }}</span>
              </label>
            </div>
          </div>

          <div class="order-total">
            <span>订单总额:</span>
            <span class="total-amount">¥{{ orderTotal.toFixed(2) }}</span>
          </div>
        </div>

        <div class="payment-footer">
          <button class="cancel-btn" @click="closePaymentModal">取消</button>
          <button class="pay-btn" @click="processPayment">确认支付</button>
        </div>
      </div>
    </div>

    <div v-if="showSuccessModal" class="success-modal" @click="closeSuccessModal">
      <div class="success-content" @click.stop>
        <div class="success-icon">✓</div>
        <h3>支付成功</h3>
        <p>订单已提交，感谢您的购买！</p>
        <button class="continue-btn" @click="continueShopping">继续购物</button>
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
      <a href="#" class="mobile-nav-item active" @click.prevent="goCart">
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
import { ElEmpty, ElMessage } from "element-plus";

const router = useRouter();
const cartItems = ref([]);
const selectAll = ref(false);
const searchKeyword = ref("");

const showPaymentModal = ref(false);
const showSuccessModal = ref(false);
const orderItems = ref([]);
const selectedPaymentMethod = ref("wechat");
const paymentPassword = ref("");
const selectedAddressIndex = ref(0);

interface Address {
  name: string;
  phone: string;
  province: string;
  city: string;
  district: string;
  detail: string;
}

const addresses = ref<Address[]>([]);

const paymentMethods = [
  { value: "wechat", name: "微信支付", icon: "" }
];

const filteredCartItems = computed(() => {
  if (!searchKeyword.value.trim()) {
    return cartItems.value;
  }
  const keyword = searchKeyword.value.toLowerCase();
  return cartItems.value.filter(item => 
    item.name.toLowerCase().includes(keyword)
  );
});

const selectedCount = computed(() => {
  return filteredCartItems.value.filter(item => item.selected).length;
});

const totalPrice = computed(() => {
  return filteredCartItems.value
    .filter(item => item.selected)
    .reduce((sum, item) => sum + (item.price || 0) * item.quantity, 0);
});

const orderTotal = computed(() => {
  return orderItems.value
    .reduce((sum, item) => sum + (item.price || 0) * item.quantity, 0);
});

async function loadCart() {
  const token = localStorage.getItem("token");
  if (!token) {
    alert("请先登录");
    router.push("/login");
    return;
  }

  try {
    const response = await fetch("http://localhost:8081/api/cart", {
      headers: {
        "x-auth-token": token
      }
    });
    const data = await response.json();
    
    if (data.code === 200) {
      cartItems.value = data.data.map(item => ({
        ...item,
        selected: false
      }));
    } else {
      ElMessage.error(data.message || "加载失败");
    }
  } catch (error) {
    console.error("加载购物车失败:", error);
    ElMessage.error("加载失败，请检查网络");
  }
}

function updateQuantity(item, delta) {
  const newQty = item.quantity + delta;
  if (newQty < 1) return;
  if (item.stock && newQty > item.stock) {
    ElMessage.warning("库存不足");
    return;
  }
  item.quantity = newQty;
}

async function removeItem(goodsId) {
  if (!confirm("确定要删除这个商品吗？")) return;

  const token = localStorage.getItem("token");
  try {
    const response = await fetch(`http://localhost:8081/api/cart/${goodsId}`, {
      method: "DELETE",
      headers: {
        "x-auth-token": token
      }
    });
    const data = await response.json();
    
    if (data.code === 200) {
      ElMessage.success("删除成功");
      loadCart();
    } else {
      ElMessage.error(data.message || "删除失败");
    }
  } catch (error) {
    console.error("删除失败:", error);
    ElMessage.error("删除失败，请检查网络");
  }
}

function toggleSelectAll() {
  cartItems.value.forEach(item => {
    item.selected = selectAll.value;
  });
}

function checkout() {
  const selectedItems = cartItems.value.filter(item => item.selected);
  if (selectedItems.length === 0) {
    ElMessage.warning("请选择要结算的商品");
    return;
  }
  
  orderItems.value = selectedItems.map(item => ({
    ...item
  }));
  showPaymentModal.value = true;
}

function closePaymentModal() {
  showPaymentModal.value = false;
  selectedPaymentMethod.value = "wechat";
  paymentPassword.value = "";
}

function closeSuccessModal() {
  showSuccessModal.value = false;
}

async function processPayment() {
  if (addresses.value.length === 0) {
    ElMessage.warning("请先添加收货地址");
    return;
  }
  
  const selectedAddr = addresses.value[selectedAddressIndex.value];
  if (!selectedAddr) {
    ElMessage.warning("请选择收货地址");
    return;
  }

  ElMessage.info("正在处理支付...");

  await new Promise(resolve => setTimeout(resolve, 1500));

  const token = localStorage.getItem("token");
  try {
    const params = new URLSearchParams();
    params.append("shippingName", selectedAddr.name);
    params.append("shippingPhone", selectedAddr.phone);
    params.append("shippingAddress", selectedAddr.province + " " + selectedAddr.city + " " + selectedAddr.district + " " + selectedAddr.detail);
    
    console.log("选中的商品:", orderItems.value);
    orderItems.value.forEach(item => {
      console.log("添加商品ID:", item.goodsId);
      params.append("goodsIds", item.goodsId.toString());
    });

    const response = await fetch("http://localhost:8081/api/orders", {
      method: "POST",
      headers: {
        "x-auth-token": token
      },
      body: params
    });
    const data = await response.json();

    if (data.code === 200) {
      showPaymentModal.value = false;
      showSuccessModal.value = true;
      cartItems.value = [];
      loadCart();
    } else {
      ElMessage.error(data.message || "支付失败");
    }
  } catch (error) {
    console.error("支付失败:", error);
    ElMessage.error("支付失败，请检查网络");
  }
}

function continueShopping() {
  showSuccessModal.value = false;
  router.push("/home");
}

function goHome() {
  router.push("/home");
}

function goProfile() {
  router.push("/profile");
}

function goFav() {
  router.push("/favorites");
}

function goHelp() {
  router.push("/help");
}

function goCart() {
  // 已经在购物车页面，不需要跳转
}

onMounted(() => {
  loadCart();
  loadAddresses();
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
  closePaymentModal();
  router.push("/profile");
}
</script>

<style scoped>
.cart-page {
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
  color: #4a90d9;
}

.nav-item.active {
  color: #4a90d9;
  font-weight: bold;
}

.cart-content {
  padding: 25px;
}

.cart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.cart-title {
  margin: 0;
  font-size: 28px;
  font-weight: bold;
}

.cart-tools {
  display: flex;
  gap: 20px;
}

.tool-btn {
  color: #666;
  font-size: 14px;
  cursor: pointer;
}

.tool-btn:hover {
  color: #4a90d9;
}

.search-box {
  position: relative;
  display: flex;
  align-items: center;
}

.search-input {
  padding: 8px 30px 8px 12px;
  border: 1px solid #ddd;
  border-radius: 20px;
  font-size: 14px;
  width: 200px;
  outline: none;
  transition: border-color 0.3s;
}

.search-input:focus {
  border-color: #4a90d9;
}

.clear-btn {
  position: absolute;
  right: 8px;
  background: none;
  border: none;
  color: #999;
  font-size: 18px;
  cursor: pointer;
  padding: 0;
  line-height: 1;
}

.clear-btn:hover {
  color: #666;
}

.cart-body {
  background: #fff;
  border-radius: 15px;
  min-height: 400px;
  padding: 20px;
}

.cart-items {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.cart-item {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 20px;
  border: 1px solid #eee;
  border-radius: 10px;
}

.cart-item:hover {
  background: #f9f9f9;
}

.item-img {
  width: 100px;
  height: 100px;
  border-radius: 8px;
  overflow: hidden;
  background: #f0f0f0;
}

.item-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.item-info {
  flex: 1;
}

.item-name {
  font-size: 16px;
  font-weight: 500;
  margin: 0 0 8px 0;
}

.price {
  color: #ff4d4f;
  font-size: 18px;
  font-weight: bold;
  margin: 0 0 12px 0;
}

.quantity-control {
  display: flex;
  align-items: center;
  gap: 10px;
}

.quantity-control button {
  width: 30px;
  height: 30px;
  border: 1px solid #ddd;
  background: #fff;
  border-radius: 4px;
  cursor: pointer;
}

.quantity-control button:hover:not(:disabled) {
  background: #f0f0f0;
}

.quantity-control button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.quantity-control span {
  min-width: 30px;
  text-align: center;
}

.item-total {
  text-align: right;
}

.item-total p {
  color: #ff4d4f;
  font-size: 18px;
  font-weight: bold;
  margin: 0;
}

.remove-btn {
  color: #999;
  cursor: pointer;
  padding: 8px 12px;
}

.remove-btn:hover {
  color: #ff4d4f;
}

.cart-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 15px 20px;
  background: #fff;
  border-radius: 15px;
  margin-top: 15px;
}

.right-section {
  display: flex;
  align-items: center;
  gap: 20px;
}

.check-all {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.total-area {
  display: flex;
  align-items: baseline;
  gap: 15px;
}

.total-text {
  font-size: 14px;
  color: #666;
}

.total-price {
  font-size: 24px;
  font-weight: bold;
  color: #ff4d4f;
}

.checkout-btn {
  padding: 12px 40px;
  background: #ff6600;
  border: none;
  color: #fff;
  border-radius: 6px;
  font-size: 16px;
  cursor: pointer;
}

.checkout-btn:hover {
  background: #e55a00;
}

.payment-modal, .success-modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.payment-content {
  background: #fff;
  border-radius: 12px;
  width: 480px;
  max-height: 80vh;
  overflow: hidden;
}

.payment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #eee;
}

.payment-header h3 {
  margin: 0;
  font-size: 18px;
}

.close-btn {
  font-size: 24px;
  color: #999;
  cursor: pointer;
}

.close-btn:hover {
  color: #666;
}

.payment-body {
  padding: 20px;
}

.order-summary h4 {
  margin: 0 0 15px 0;
  font-size: 14px;
  color: #333;
}

.order-items {
  max-height: 200px;
  overflow-y: auto;
}

.order-item {
  display: flex;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.order-item:last-child {
  border-bottom: none;
}

.order-item-img {
  width: 60px;
  height: 60px;
  border-radius: 6px;
  object-fit: cover;
}

.order-item-info {
  flex: 1;
  padding: 0 15px;
}

.order-item-info p {
  margin: 4px 0;
  font-size: 14px;
}

.order-item-price {
  color: #ff4d4f;
  font-weight: bold;
}

.address-section {
  margin-top: 20px;
}

.address-section h4 {
  margin: 0 0 12px 0;
  font-size: 14px;
  color: #333;
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
  border-color: #4a90d9;
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

.payment-method {
  margin-top: 20px;
}

.payment-method h4 {
  margin: 0 0 12px 0;
  font-size: 14px;
  color: #333;
}

.methods {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.method-label {
  display: flex;
  align-items: center;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 8px;
  cursor: pointer;
}

.method-label:hover {
  border-color: #4a90d9;
}

.method-label input[type="radio"] {
  margin-right: 10px;
}

.method-icon {
  font-size: 20px;
  margin-right: 10px;
}

.payment-password {
  margin-top: 20px;
}

.payment-password h4 {
  margin: 0 0 12px 0;
  font-size: 14px;
  color: #333;
}

.password-input {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 16px;
  box-sizing: border-box;
  text-align: center;
  letter-spacing: 8px;
}

.order-total {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #eee;
  font-size: 16px;
}

.total-amount {
  font-size: 24px;
  font-weight: bold;
  color: #ff4d4f;
}

.payment-footer {
  display: flex;
  gap: 15px;
  padding: 20px;
  border-top: 1px solid #eee;
}

.cancel-btn, .pay-btn {
  flex: 1;
  padding: 12px;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  cursor: pointer;
}

.cancel-btn {
  background: #f5f5f5;
  color: #666;
}

.cancel-btn:hover {
  background: #eee;
}

.pay-btn {
  background: #ff6600;
  color: #fff;
}

.pay-btn:hover {
  background: #e55a00;
}

.success-content {
  background: #fff;
  border-radius: 12px;
  padding: 40px;
  text-align: center;
}

.success-icon {
  width: 80px;
  height: 80px;
  background: #52c41a;
  border-radius: 50%;
  color: #fff;
  font-size: 48px;
  line-height: 80px;
  margin: 0 auto 20px;
}

.success-content h3 {
  margin: 0 0 10px 0;
  font-size: 24px;
}

.success-content p {
  margin: 0 0 25px 0;
  color: #666;
}

.continue-btn {
  padding: 12px 40px;
  background: #4a90d9;
  border: none;
  color: #fff;
  border-radius: 8px;
  font-size: 16px;
  cursor: pointer;
}

.continue-btn:hover {
  background: #3d7bc6;
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
  
  /* 页面背景 - 大盒子 */
  .cart-page {
    background: #f5f5f5;
    min-height: 100vh;
  }
  
  /* 购物车内容 - 小盒子 */
  .cart-content {
    max-width: 500px;
    margin: 15px auto;
    padding: 20px;
    padding-bottom: 180px;
    background: #fff;
    border-radius: 15px;
    box-shadow: 0 2px 20px rgba(0, 0, 0, 0.1);
  }
  
  .cart-header {
    flex-direction: column;
    gap: 10px;
    align-items: flex-start;
  }
  
  .cart-title {
    font-size: 22px;
  }
  
  .search-box {
    width: 100%;
  }
  
  .search-input {
    width: 100%;
  }
  
  /* 购物车项 */
  .cart-item {
    flex-direction: row;
    align-items: center;
    gap: 8px;
    padding: 4px;
    background: #fafafa;
    border-radius: 10px;
    margin-bottom: 8px;
  }
  
  .item-img {
    width: 60px;
    height: 60px;
    flex-shrink: 0;
  }
  
  .item-info {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 6px;
  }
  
  .item-name {
    font-size: 12px;
    font-weight: 500;
  }
  
  .quantity-control {
    flex-shrink: 0;
  }
  
  .item-total {
    text-align: left;
  }
  
  .item-total p {
    font-size: 14px;
    margin: 0;
  }
  
  .remove-btn {
    font-size: 12px;
    color: #999;
  }
  
  /* 购物车底部 - 固定在底部，融入小盒子效果 */
  .cart-footer {
    position: fixed;
    bottom: 60px;
    left: 15px;
    right: 15px;
    flex-direction: column;
    gap: 15px;
    align-items: stretch;
    padding: 15px 20px;
    background: #fff;
    box-shadow: 0 -4px 20px rgba(0, 0, 0, 0.1);
    border-radius: 15px;
    max-width: 400px;
    margin: 0 auto;
  }
  
  .right-section {
    flex-direction: column;
    gap: 10px;
    width: 100%;
  }
  
  .total-area {
    justify-content: space-between;
    width: 100%;
  }
  
  .checkout-btn {
    width: 100%;
  }
  
  /* 支付弹窗 */
  .payment-content {
    width: 95%;
    max-width: 95%;
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

