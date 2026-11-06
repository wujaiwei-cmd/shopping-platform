<template>
  <div class="admin-page">
    <header class="admin-header">
      <div class="header-left">
        <div class="logo">
          <span class="logo-text">控制台</span>
        </div>
      </div>
      <nav class="main-nav">
        <a href="/admin/order" class="nav-item">订单管理</a>
        <a href="/admin/product" class="nav-item">商品管理</a>
        <a href="/admin/user" class="nav-item">用户管理</a>
        <a href="#" @click.prevent="activeTab = 'feedback'" class="nav-item" :class="{ active: activeTab === 'feedback' }">反馈管理</a>
      </nav>
      <div class="header-right">
        <button v-if="activeTab === 'feedback'" class="back-btn" @click="activeTab = 'dashboard'">返回控制台</button>
        <div v-else class="logout-btn" @click="handleLogout">
          <span class="logout-text">退出系统</span>
        </div>
      </div>
    </header>

    <div class="admin-content" v-if="activeTab === 'dashboard'">
      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-info">
            <div class="stat-value">{{ todayOrders }}</div>
            <div class="stat-label">今日订单</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-info">
            <div class="stat-value">{{ totalOrders }}</div>
            <div class="stat-label">总订单数</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-info">
            <div class="stat-value">¥{{ todayRevenue }}</div>
            <div class="stat-label">今日收入</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-info">
            <div class="stat-value">¥{{ totalRevenue }}</div>
            <div class="stat-label">总收入</div>
          </div>
        </div>
        <div class="stat-card" style="cursor: pointer" @click="goToFeedback">
          <div class="stat-info">
            <div class="stat-value feedback-count">{{ pendingFeedbackCount }}</div>
            <div class="stat-label">待处理反馈</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-info">
            <div class="stat-value">{{ totalUsers }}</div>
            <div class="stat-label">总用户数</div>
          </div>
        </div>
      </div>

      <div class="content-row">
        <div class="chart-section">
          <div class="section-header">
            <h3>近七天订单趋势</h3>
          </div>
          <div class="chart-container">
            <div class="chart-bars">
              <div class="bar-item" v-for="(item, index) in chartData" :key="index">
                <div class="bar" :style="{ height: item.percent + '%' }"></div>
                <div class="bar-label">{{ item.day }}</div>
              </div>
            </div>
          </div>
        </div>

        <div class="right-panel">
          <div class="section-header">
            <h3>权限管理</h3>
          </div>
          <div class="panel-list">
            <div class="list-item" @click="showAddAdmin">
              <span class="item-text">添加子管理员</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="admin-content" v-if="activeTab === 'feedback'">
      <div class="feedback-section">
        <div class="section-header">
          <h3>反馈与投诉管理</h3>
          <div class="filter-buttons">
            <button class="filter-btn" :class="{ active: filterStatus === 'all' }" @click="filterStatus = 'all'">全部</button>
            <button class="filter-btn" :class="{ active: filterStatus === 'pending' }" @click="filterStatus = 'pending'">待处理</button>
            <button class="filter-btn" :class="{ active: filterStatus === 'processed' }" @click="filterStatus = 'processed'">已处理</button>
            <button class="refresh-btn" @click="loadFeedbacks">刷新</button>
          </div>
        </div>
        
        <div class="feedback-list">
          <div class="feedback-item" v-for="item in filteredFeedbacks" :key="item.id">
            <div class="feedback-header">
              <div class="feedback-title">
                <span class="feedback-badge" :class="item.type === 'complaint' ? 'complaint' : 'feedback'">
                  {{ item.type === 'complaint' ? '投诉' : '反馈' }}
                </span>
                <span class="title-text">{{ item.title }}</span>
              </div>
              <div class="feedback-status" :class="item.isProcessed ? 'processed' : 'pending'">
                {{ item.isProcessed ? '✓ 已处理' : '⏳ 待处理' }}
              </div>
            </div>
            <div class="feedback-content">{{ item.content }}</div>
            <div class="feedback-meta">
              <span>用户: {{ item.username || '匿名用户' }}</span>
              <span>联系方式: {{ item.contact || '未提供' }}</span>
              <span>提交时间: {{ formatDate(item.createdAt) }}</span>
            </div>
            <div v-if="item.isProcessed && item.processedNote" class="processed-note">
              <span class="note-label">处理备注：</span>
              <span class="note-content">{{ item.processedNote }}</span>
            </div>
            <div class="feedback-actions" v-if="!item.isProcessed">
              <input v-model="processNotes[item.id]" type="text" placeholder="输入处理备注（选填）" class="process-input" />
              <button class="process-btn" @click="processFeedback(item)">标记已处理</button>
              <button class="delete-btn" @click="deleteFeedback(item.id)">删除</button>
            </div>
            <div class="feedback-actions" v-else>
              <button class="delete-btn" @click="deleteFeedback(item.id)">删除</button>
            </div>
          </div>
          
          <div class="empty-state" v-if="filteredFeedbacks.length === 0">
            <div class="empty-icon">📭</div>
            <div class="empty-text">暂无{{ filterStatus === 'pending' ? '待处理' : filterStatus === 'processed' ? '已处理' : '' }}数据</div>
          </div>
        </div>
      </div>
    </div>

    <div v-if="showAddAdminModal" class="modal-overlay" @click="closeAddAdminModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>添加子管理员</h3>
          <span class="modal-close" @click="closeAddAdminModal">×</span>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>用户名 *</label>
            <input v-model="adminForm.username" type="text" placeholder="请输入用户名" class="modal-input" />
          </div>
          <div class="form-group">
            <label>密码 *</label>
            <input v-model="adminForm.password" type="password" placeholder="请输入密码（至少6位）" class="modal-input" />
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn-cancel" @click="closeAddAdminModal">取消</button>
          <button class="btn-submit" @click="createSubAdmin">确认添加</button>
        </div>
      </div>
    </div>

    <!-- 移动端底部导航 -->
    <nav class="mobile-nav">
      <a href="/admin/order" class="mobile-nav-item">
        <span>订单</span>
      </a>
      <a href="/admin/product" class="mobile-nav-item">
        <span>商品</span>
      </a>
      <a href="/admin/user" class="mobile-nav-item">
        <span>用户</span>
      </a>
      <a href="/admin/feedback" class="mobile-nav-item">
        <span>反馈</span>
      </a></nav>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, computed } from "vue";
import { useRouter } from "vue-router";

const router = useRouter();
const activeTab = ref('dashboard');
const todayOrders = ref(0);
const totalOrders = ref(0);
const totalUsers = ref(0);
const todayRevenue = ref(0);
const totalRevenue = ref(0);
const pendingFeedbackCount = ref(0);
const feedbacks = ref<any[]>([]);
const filterStatus = ref('all');
const processNotes = ref<Record<number, string>>({});

const chartData = ref([]);

const showAddAdminModal = ref(false);
const adminForm = ref({
  username: "",
  password: "",
  email: "",
  phone: ""
});

const filteredFeedbacks = computed(() => {
  if (filterStatus.value === 'all') return feedbacks.value;
  if (filterStatus.value === 'pending') return feedbacks.value.filter(f => !f.isProcessed);
  return feedbacks.value.filter(f => f.isProcessed);
});

let refreshInterval = null;

onMounted(() => {
  const isAdmin = localStorage.getItem("isAdmin") === "true";
  const token = localStorage.getItem("token");
  
  if (!token || !isAdmin) {
    router.push("/admin/login");
  }
  
  loadDashboardData();
  loadFeedbacks();
  
  refreshInterval = setInterval(() => {
    loadDashboardData();
    loadFeedbacks();
  }, 10000);
  
  document.addEventListener("visibilitychange", () => {
    if (!document.hidden) {
      loadDashboardData();
      loadFeedbacks();
    }
  });
});

onBeforeUnmount(() => {
  if (refreshInterval) {
    clearInterval(refreshInterval);
  }
});

function formatDate(dateStr: string) {
  if (!dateStr) return '-';
  const date = new Date(dateStr);
  return date.toLocaleString('zh-CN');
}

async function loadDashboardData() {
  const token = localStorage.getItem("token");

  try {
    const response = await fetch("http://localhost:8081/api/admin/dashboard", {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${token || ""}`
      }
    });

    const data = await response.json();
    if (data.code === 200) {
      todayOrders.value = data.data.todayOrders || 0;
      totalOrders.value = data.data.totalOrders || 0;
      todayRevenue.value = data.data.todayRevenue || 0;
      totalRevenue.value = data.data.totalRevenue || 0;
      totalUsers.value = data.data.totalUsers || 0;
      chartData.value = data.data.lastSevenDays || [];
    }
  } catch (error) {
    console.error("获取数据失败:", error);
  }
}

async function loadFeedbacks() {
  const token = localStorage.getItem("token");
  try {
    const response = await fetch("http://localhost:8081/api/admin/feedbacks", {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${token || ""}`
      }
    });

    const data = await response.json();
    if (data.code === 200) {
      feedbacks.value = data.data || [];
      pendingFeedbackCount.value = feedbacks.value.filter(f => !f.isProcessed).length;
    }
  } catch (error) {
    console.error("获取反馈失败:", error);
  }
}

async function processFeedback(item: any) {
  const token = localStorage.getItem("token");
  const note = processNotes.value[item.id] || '';
  
  try {
    const response = await fetch(`http://localhost:8081/api/admin/feedbacks/${item.id}/process`, {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${token || ""}`
      },
      body: JSON.stringify({ note })
    });

    const data = await response.json();
    if (data.code === 200) {
      alert("处理成功！");
      loadFeedbacks();
      delete processNotes.value[item.id];
    } else {
      alert(data.message || "处理失败");
    }
  } catch (error) {
    console.error("处理失败:", error);
    alert("网络错误");
  }
}

async function deleteFeedback(id: number) {
  if (!confirm("确定要删除这条反馈吗？")) return;
  
  const token = localStorage.getItem("token");
  try {
    const response = await fetch(`http://localhost:8081/api/admin/feedbacks/${id}`, {
      method: "DELETE",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${token || ""}`
      }
    });

    const data = await response.json();
    if (data.code === 200) {
      alert("删除成功！");
      loadFeedbacks();
    } else {
      alert(data.message || "删除失败");
    }
  } catch (error) {
    console.error("删除失败:", error);
    alert("网络错误");
  }
}

function goToFeedback() {
  router.push("/admin/feedback");
}

function handleLogout() {
  if (confirm("确定要退出系统吗？")) {
    localStorage.removeItem("loginUser");
    localStorage.removeItem("token");
    localStorage.removeItem("isAdmin");
    router.push("/admin/login");
  }
}

function showAddAdmin() {
  showAddAdminModal.value = true;
}

function closeAddAdminModal() {
  showAddAdminModal.value = false;
  adminForm.value = {
    username: "",
    password: "",
    email: "",
    phone: ""
  };
}

async function createSubAdmin() {
  const { username, password, email, phone } = adminForm.value;
  
  if (!username.trim()) {
    alert("请输入用户名");
    return;
  }
  
  if (!password.trim()) {
    alert("请输入密码");
    return;
  }
  
  if (password.length < 6) {
    alert("密码长度不能少于6位");
    return;
  }
  
  const token = localStorage.getItem("token");
  
  try {
    const response = await fetch("http://localhost:8081/api/admin/subadmin", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${token || ""}`
      },
      body: JSON.stringify({
        username,
        password
      })
    });
    
    const data = await response.json();
    
    if (data.code === 200) {
      alert("子管理员创建成功！");
      closeAddAdminModal();
      loadDashboardData();
    } else {
      alert(data.message || "创建失败");
    }
  } catch (error) {
    console.error("网络请求错误:", error);
    alert("网络错误，请检查后端服务是否启动\n\n错误详情: " + (error as Error).message);
  }
}

</script>

<style scoped>
.admin-page {
  min-height: 100vh;
  background: #fff;
  padding-bottom: 30px;
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

.logout-btn {
  padding: 10px 20px;
  background: #ff6666;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s;
}

.logout-btn:hover {
  background: #ff4444;
}

.logout-text {
  color: #fff;
  font-weight: bold;
  font-size: 14px;
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

.back-text {
  color: #fff;           /* 白色文字 */
  font-weight: bold;
  font-size: 14px;
}
.admin-content {
  padding: 30px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 15px;
  background: #fff;
  padding: 25px;
  border-radius: 12px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #333;
}

.stat-value.feedback-count {
  color: #ff6b6b;
}

.stat-label {
  font-size: 13px;
  color: #999;
  margin-top: 4px;
}

.content-row {
  display: flex;
  gap: 20px;
}

.chart-section {
  flex: 2;
  background: #fff;
  border-radius: 12px;
  padding: 25px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.section-header {
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.section-header h3 {
  margin: 0;
  font-size: 18px;
  color: #333;
}

.chart-container {
  height: 280px;
  display: flex;
  align-items: flex-end;
  justify-content: center;
  padding-bottom: 30px;
}

.chart-bars {
  display: flex;
  gap: 30px;
  align-items: flex-end;
  height: 100%;
}

.bar-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  width: 50px;
}

.bar {
  width: 100%;
  background: linear-gradient(180deg, #667eea 0%, #764ba2 100%);
  border-radius: 8px 8px 0 0;
  transition: height 0.5s;
}

.bar-label {
  font-size: 12px;
  color: #666;
}

.right-panel {
  flex: 1;
  background: #fff;
  border-radius: 12px;
  padding: 25px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.panel-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.list-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 15px 20px;
  background: #f8f9fa;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.list-item:hover {
  background: #e9ecef;
}

.item-text {
  font-size: 15px;
  color: #333;
}

.feedback-section {
  background: #fff;
  border-radius: 12px;
  padding: 25px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.filter-buttons {
  display: flex;
  gap: 10px;
}

.filter-btn, .refresh-btn {
  padding: 8px 16px;
  border: 1px solid #ddd;
  background: #fff;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
  transition: all 0.2s;
}

.filter-btn:hover, .refresh-btn:hover {
  border-color: #667eea;
  color: #667eea;
}

.filter-btn.active {
  background: #667eea;
  border-color: #667eea;
  color: #fff;
}

.refresh-btn {
  background: #f8f9fa;
}

.feedback-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.feedback-item {
  border: 1px solid #eee;
  border-radius: 12px;
  padding: 20px;
  transition: all 0.2s;
}

.feedback-item:hover {
  border-color: #ddd;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

.feedback-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.feedback-title {
  display: flex;
  align-items: center;
  gap: 10px;
}

.feedback-badge {
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.feedback-badge.feedback {
  background: #e3f2fd;
  color: #1976d2;
}

.feedback-badge.complaint {
  background: #ffebee;
  color: #c62828;
}

.title-text {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.feedback-status {
  font-size: 13px;
  font-weight: 500;
  padding: 4px 12px;
  border-radius: 12px;
}

.feedback-status.pending {
  background: #fff3e0;
  color: #e65100;
}

.feedback-status.processed {
  background: #e8f5e9;
  color: #2e7d32;
}

.feedback-content {
  color: #555;
  font-size: 14px;
  line-height: 1.6;
  margin-bottom: 12px;
}

.feedback-meta {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
  font-size: 12px;
  color: #999;
  margin-bottom: 12px;
}

.processed-note {
  background: #f1f8e9;
  padding: 10px 14px;
  border-radius: 8px;
  margin-bottom: 12px;
  font-size: 13px;
}

.note-label {
  color: #558b2f;
  font-weight: 500;
  margin-right: 8px;
}

.note-content {
  color: #33691e;
}

.feedback-actions {
  display: flex;
  gap: 10px;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.process-input {
  flex: 1;
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 13px;
  outline: none;
  transition: border-color 0.2s;
}

.process-input:focus {
  border-color: #667eea;
}

.process-btn, .delete-btn {
  padding: 8px 16px;
  border-radius: 6px;
  font-size: 13px;
  cursor: pointer;
  border: none;
  transition: all 0.2s;
}

.process-btn {
  background: linear-gradient(135deg, #4caf50 0%, #43a047 100%);
  color: #fff;
}

.process-btn:hover {
  background: linear-gradient(135deg, #43a047 0%, #388e3c 100%);
}

.delete-btn {
  background: #f5f5f5;
  color: #d32f2f;
}

.delete-btn:hover {
  background: #ffebee;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #999;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.empty-text {
  font-size: 15px;
}

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
  width: 450px;
  max-width: 90%;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
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
  font-size: 24px;
  cursor: pointer;
  color: #999;
  transition: color 0.3s;
}

.modal-close:hover {
  color: #333;
}

.modal-body {
  padding: 24px;
}

.modal-body .form-group {
  margin-bottom: 18px;
}

.modal-body .form-group label {
  display: block;
  margin-bottom: 8px;
  font-size: 14px;
  color: #666;
}

.modal-input {
  width: 100%;
  height: 44px;
  padding: 0 14px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 14px;
  box-sizing: border-box;
  outline: none;
  transition: border-color 0.3s;
}

.modal-input:focus {
  border-color: #667eea;
}

.modal-input::placeholder {
  color: #ccc;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 24px;
  border-top: 1px solid #eee;
}

.btn-cancel {
  padding: 10px 24px;
  background: #f5f5f5;
  border: none;
  border-radius: 8px;
  color: #666;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-cancel:hover {
  background: #eee;
}

.btn-submit {
  padding: 10px 24px;
  background: linear-gradient(135deg, #4a90d9 0%, #3a7fc8 100%);
  border: none;
  border-radius: 8px;
  color: #fff;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
}

/* 移动端底部导航默认隐藏 */
.mobile-nav {
  display: none;
}

/* 移动端响应式样式 */
@media (max-width: 768px) {
  /* 头部样式 */
  .admin-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 10px 15px;
    background: linear-gradient(90deg, #333);
  }

  .logo-text {
    font-size: 22px;
    color: #fff;
    font-weight: 700;
  }

  .main-nav {
    display: none;
  }

  .logout-btn {
    background: #ff6666;
    padding: 8px 16px;
  }

  .logout-text {
    font-size: 13px;
  }

  /* 管理员内容 */
  .admin-content {
    padding: 15px;
    padding-bottom: 80px;
  }

  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }

  .stat-card {
    padding: 18px;
  }

  .stat-value {
    font-size: 24px;
  }

  .stat-label {
    font-size: 12px;
  }

  .content-row {
    flex-direction: column;
  }

  .chart-section,
  .right-panel {
    width: 100%;
    margin-bottom: 15px;
  }

  .chart-bars {
    gap: 15px;
  }

  .bar-item {
    width: 35px;
  }

  /* 反馈列表 */
  .feedback-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .feedback-meta {
    gap: 10px;
    font-size: 11px;
  }

  .feedback-actions {
    flex-direction: column;
  }

  .process-input {
    width: 100%;
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
