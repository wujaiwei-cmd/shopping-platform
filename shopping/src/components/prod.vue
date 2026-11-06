<template>
  <div class="product-page">
    <header class="admin-header">
      <div class="header-left">
        <div class="logo">
          <span class="logo-text">控制台</span>
        </div>
      </div>
      <nav class="main-nav">
        <a href="/admin/order" class="nav-item">订单管理</a>
        <a href="#" class="nav-item active">商品管理</a>
        <a href="/admin/user" class="nav-item">用户管理</a>
        <a href="/admin/feedback" class="nav-item">反馈管理</a>
      </nav>
      <div class="header-right">
        <button class="back-btn" @click="goBack">返回控制台</button>
      </div>
    </header>

    <div class="page-content">
      <div class="search-box">
        <input type="text" class="search-input" placeholder="搜索商品名称、描述或分类" v-model="searchKeyword" />
        <button v-if="searchKeyword" class="clear-btn" @click="searchKeyword = ''">×</button>
        <button class="add-btn" @click="showAddModal = true">添加商品</button>
      </div>
      <div class="section-title">商品列表</div>
      
      <div class="product-list">
        <div v-if="filteredProducts.length === 0" class="empty-tip">
          {{ searchKeyword.trim() ? '没有找到匹配的商品' : '暂无商品数据' }}
        </div>
        <div v-else class="product-grid">
          <div class="product-card" v-for="product in filteredProducts" :key="product.id">
            <div class="card-img">
              <img :src="product.image" :alt="product.name" />
            </div>
            <div class="card-info">
              <h3 class="card-title">{{ product.name }}</h3>
              <p class="card-price">¥{{ product.price.toFixed(2) }}</p>
              <p class="card-stock">库存: {{ product.stock }}</p>
              <p class="card-category">分类: {{ product.category }}</p>
            </div>
            <div class="card-actions">
              <button class="edit-btn" @click="editProduct(product)">编辑</button>
              <button class="delete-btn" @click="deleteProduct(product.id)">删除</button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 添加商品模态框 -->
    <div v-if="showAddModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal-content">
        <div class="modal-header">
          <h3>{{ editingProduct ? '编辑商品' : '添加商品' }}</h3>
          <button class="close-btn" @click="closeModal">×</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>商品名称</label>
            <input v-model="formData.name" type="text" placeholder="请输入商品名称" />
          </div>
          <div class="form-group">
            <label>商品价格</label>
            <input v-model.number="formData.price" type="number" placeholder="请输入商品价格" />
          </div>
          <div class="form-group">
            <label>库存数量</label>
            <input v-model.number="formData.stock" type="number" placeholder="请输入库存数量" />
          </div>
          <div class="form-group">
            <label>商品分类</label>
            <select v-model="formData.category">
              <option value="电子产品">电子产品</option>
              <option value="工业制品">工业制品</option>
              <option value="家电">家电</option>
              <option value="服装">服装</option>
              <option value="食品">食品</option>
              <option value="美妆">美妆</option>
              <option value="娱乐">娱乐</option>
              <option value="其他">其他</option>
            </select>
          </div>
          <div class="form-group">
            <label>商品描述</label>
            <textarea v-model="formData.description" placeholder="请输入商品描述"></textarea>
          </div>
          <div class="form-group">
            <label>商品图片URL</label>
            <input v-model="formData.image" type="text" placeholder="请输入图片URL" />
          </div>
        </div>
        <div class="modal-footer">
          <button class="cancel-btn" @click="closeModal">取消</button>
          <button class="submit-btn" @click="saveProduct">保存</button>
        </div>
      </div>
    </div>

    <!-- 移动端底部导航 -->
    <nav class="mobile-nav">
      <a href="/admin/order" class="mobile-nav-item">
        <span>订单</span>
      </a>
      <a href="/admin/product" class="mobile-nav-item active">
        <span>商品</span>
      </a>
      <a href="/admin/user" class="mobile-nav-item">
        <span>用户</span>
      </a>
      <a href="/admin/feedback" class="mobile-nav-item">
        <span>反馈</span>
      </a>
    </nav>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from "vue";
import { useRouter } from "vue-router";

const router = useRouter();

// 搜索关键词
const searchKeyword = ref("");

// 商品列表（初始为空）
const products = ref<Array<{
  id: number;
  name: string;
  price: number;
  stock: number;
  category: string;
  description: string;
  image: string;
}>>([]);

// 过滤后的商品列表
const filteredProducts = computed(() => {
  if (!searchKeyword.value.trim()) {
    return products.value;
  }
  const keyword = searchKeyword.value.toLowerCase();
  return products.value.filter(product => 
    product.name.toLowerCase().includes(keyword) ||
    product.description.toLowerCase().includes(keyword) ||
    product.category.toLowerCase().includes(keyword)
  );
});

// 模态框状态
const showAddModal = ref(false);
const editingProduct = ref<typeof products.value[0] | null>(null);

// 表单数据
const formData = reactive({
  name: "",
  price: 0,
  stock: 0,
  category: "保健品",
  description: "",
  image: ""
});

// 初始化表单
function initForm() {
  formData.name = "";
  formData.price = 0;
  formData.stock = 0;
  formData.category = "保健品";
  formData.description = "";
  formData.image = "";
}

// 打开添加商品模态框
function openAddModal() {
  initForm();
  editingProduct.value = null;
  showAddModal.value = true;
}

// 编辑商品
function editProduct(product: typeof products.value[0]) {
  editingProduct.value = product;
  formData.name = product.name;
  formData.price = product.price;
  formData.stock = product.stock;
  formData.category = product.category;
  formData.description = product.description;
  formData.image = product.image;
  showAddModal.value = true;
}

// 关闭模态框
function closeModal() {
  showAddModal.value = false;
  editingProduct.value = null;
  initForm();
}

// 不再使用 localStorage 缓存商品数据，避免乱码问题

// 保存商品
async function saveProduct() {
  if (!formData.name || formData.price <= 0 || formData.stock < 0) {
    alert("请填写完整的商品信息");
    return;
  }

  const token = localStorage.getItem("token");

  if (editingProduct.value) {
    // 编辑模式
    try {
      const response = await fetch(`http://localhost:8081/api/goods/${editingProduct.value.id}`, {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
          "x-auth-token": token || ""
        },
        body: JSON.stringify(formData)
      });
      const data = await response.json();
      if (data.code === 200) {
          const index = products.value.findIndex(p => p.id === editingProduct.value!.id);
          if (index !== -1) {
            products.value[index] = {
              ...products.value[index],
              ...formData
            };
          }
          alert("商品信息已更新");
        } else {
        alert(data.message || "更新失败");
      }
    } catch (error) {
      console.error("更新商品失败:", error);
      alert("更新失败，请检查网络连接");
    }
  } else {
    // 添加模式
    try {
      const response = await fetch("http://localhost:8081/api/goods", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "x-auth-token": token || ""
        },
        body: JSON.stringify(formData)
      });
      const data = await response.json();
      if (data.code === 200) {
        products.value.push(data.data);
        alert("商品添加成功");
      } else {
        alert(data.message || "添加失败");
      }
    } catch (error) {
      console.error("添加商品失败:", error);
      alert("添加失败，请检查网络连接");
    }
  }

  closeModal();
}

// 删除商品
async function deleteProduct(id: number) {
  if (!confirm("确定要删除这个商品吗？")) {
    return;
  }

  const token = localStorage.getItem("token");
  try {
    const response = await fetch(`http://localhost:8081/api/goods/${id}`, {
      method: "DELETE",
      headers: {
        "x-auth-token": token || ""
      }
    });
    const data = await response.json();
    if (data.code === 200) {
      products.value = products.value.filter(p => p.id !== id);
      // 删除商品后，清除 localStorage 中的缓存，确保首页不会显示旧数据
      localStorage.removeItem("products");
      alert("商品已删除");
    } else {
      alert(data.message || "删除失败");
    }
  } catch (error) {
    console.error("删除商品失败:", error);
    alert("删除失败，请检查网络连接");
  }
}

// 返回控制台
function goBack() {
  router.push("/admin");
}

// 从后端API加载商品数据
async function loadProducts() {
  const token = localStorage.getItem("token");
  try {
    const response = await fetch("http://localhost:8081/api/goods", {
      headers: {
        "x-auth-token": token || ""
      }
    });
    const data = await response.json();
    if (data.code === 200) {
        products.value = data.data;
      }
    } catch (error) {
      console.error("加载商品失败:", error);
      // 不使用 localStorage 缓存，避免乱码问题
    }
  }

// 页面挂载时验证管理员权限并加载商品数据
onMounted(() => {
  const isAdmin = localStorage.getItem("isAdmin") === "true";
  const token = localStorage.getItem("token");
  
  if (!token || !isAdmin) {
    router.push("/admin/login");
  }
  
  // 加载商品数据
  loadProducts();
});
</script>

<style scoped>
.product-page {
  min-height: 100vh;
  background-color: #fff;
}

.admin-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 30px;
  background: rgba(255, 255, 255, 0.95);
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
}

.logo-text {
  font-size: 22px;
  font-weight: bold;
  color: #333;
}

.main-nav {
  display: flex;
  gap: 2px;
  background: #f5f5f5;
  padding: 6px;
  border-radius: 8px;
}

.nav-item {
  padding: 10px 25px;
  text-decoration: none;
  color: #666;
  border-radius: 6px;
  font-size: 14px;
  transition: all 0.3s;
}

.nav-item:hover, .nav-item.active {
  background: #fff;
  color: #667eea;
}

.back-btn {
  padding: 10px 20px;
  background: #667EEA;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s;
  border: none;
  color: #fff;
  font-weight: bold;
  font-size: 14px;
}

.back-btn:hover {
  background: #5568d4;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 30px;
  background: rgba(255, 255, 255, 0.9);
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.header-left h2 {
  margin: 0;
  font-size: 28px;
  font-weight: bold;
  color: #333;
}

.search-box {
  position: relative;
  display: inline-flex;
  align-items: center;
  margin-right: 15px;
}

.search-input {
  padding: 10px 40px 10px 15px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 14px;
  width: 300px;
  transition: border-color 0.3s;
}

.search-input:focus {
  outline: none;
  border-color: #667eea;
}

.clear-btn {
  position: absolute;
  right: 10px;
  background: none;
  border: none;
  font-size: 18px;
  cursor: pointer;
  color: #999;
  padding: 0 5px;
  line-height: 1;
}

.clear-btn:hover {
  color: #333;
}

.add-btn {
  padding: 10px 20px;
  background: #28a745;
  color: #fff;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  margin-right: 15px;
}

.add-btn:hover {
  background: #218838;
}

.back-btn {
  padding: 10px 20px;
  background: #667eea;
  color: #fff;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
}

.back-btn:hover {
  background: #556ee6;
}

.page-content {
  padding: 30px;
}

.section-title {
  font-size: 18px;
  font-weight: bold;
  color: #333;
  margin-bottom: 20px;
}

.product-list {
  min-height: 500px;
}

.empty-tip {
  text-align: center;
  padding: 100px 0;
  color: #999;
  font-size: 16px;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.product-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.product-card:hover {
}

.card-img {
  height: 200px;
  overflow: hidden;
}

.card-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.card-info {
  padding: 15px;
}

.card-title {
  margin: 0 0 10px;
  font-size: 18px;
  font-weight: bold;
  color: #333;
}

.card-price {
  margin: 5px 0;
  font-size: 20px;
  font-weight: bold;
  color: #e74c3c;
}

.card-stock, .card-category {
  margin: 5px 0;
  font-size: 14px;
  color: #666;
}

.card-actions {
  display: flex;
  gap: 10px;
  padding: 0 15px 15px;
}

.edit-btn {
  flex: 1;
  padding: 10px;
  background: #3498db;
  color: #fff;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
}

.edit-btn:hover {
  background: #2980b9;
}

.delete-btn {
  flex: 1;
  padding: 10px;
  background: #e74c3c;
  color: #fff;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
}

.delete-btn:hover {
  background: #c0392b;
}

/* 模态框样式 */
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
  width: 90%;
  max-width: 500px;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #eee;
}

.modal-header h3 {
  margin: 0;
  font-size: 20px;
}

.close-btn {
  background: none;
  border: none;
  font-size: 28px;
  cursor: pointer;
  color: #999;
}

.close-btn:hover {
  color: #333;
}

.modal-body {
  padding: 20px;
}

.form-group {
  margin-bottom: 15px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-size: 14px;
  color: #666;
}

.form-group input,
.form-group select,
.form-group textarea {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  box-sizing: border-box;
}

.form-group textarea {
  min-height: 80px;
  resize: vertical;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 20px;
  border-top: 1px solid #eee;
}

.cancel-btn {
  padding: 10px 20px;
  background: #f5f5f5;
  color: #666;
  border: 1px solid #ddd;
  border-radius: 6px;
  cursor: pointer;
}

.cancel-btn:hover {
  background: #eee;
}

.submit-btn {
  padding: 10px 20px;
  background: #28a745;
  color: #fff;
  border: none;
  border-radius: 6px;
  cursor: pointer;
}

.submit-btn:hover {
  background: #218838;
}

/* 移动端底部导航默认隐藏 */
.mobile-nav {
  display: none;
}

/* 移动端响应式样式 */
@media (max-width: 768px) {
  /* 头部样式 */
  .admin-header {
    padding: 12px 15px;
    justify-content: space-between;
  }

  .logo-text {
    font-size: 18px;
  }

  /* 隐藏中间导航 */
  .main-nav {
    display: none;
  }

  .back-btn {
    padding: 8px 14px;
    font-size: 12px;
  }

  /* 页面内容 */
  .page-content {
    padding: 15px;
    padding-bottom: 80px;
  }

  .section-title {
    font-size: 18px;
  }

  .product-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }

  .product-card {
    border-radius: 10px;
  }

  .card-img {
    height: 140px;
  }

  .card-info {
    padding: 10px;
  }

  .card-title {
    font-size: 14px;
    margin-bottom: 5px;
  }

  .card-price {
    font-size: 16px;
    margin: 3px 0;
  }

  .card-stock,
  .card-category {
    font-size: 12px;
    margin: 3px 0;
  }

  .card-actions {
    padding: 0 10px 10px;
  }

  .edit-btn,
  .delete-btn {
    padding: 8px;
    font-size: 12px;
  }

  /* 模态框 */
  .modal-content {
    width: 95%;
    max-height: 90vh;
  }

  .modal-body {
    padding: 15px;
  }

  .form-group {
    margin-bottom: 12px;
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
    color: #667eea;
  }

  .mobile-nav-item span:first-child {
    font-size: 20px;
  }
}
</style>