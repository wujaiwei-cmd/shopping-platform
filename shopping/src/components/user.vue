<template>
  <div class="user-page">
    <header class="admin-header">
      <div class="header-left">
        <div class="logo">
          <span class="logo-text">控制台</span>
        </div>
      </div>
      <nav class="main-nav">
        <a href="/admin/order" class="nav-item">订单管理</a>
        <a href="/admin/product" class="nav-item">商品管理</a>
        <a href="#" class="nav-item active">用户管理</a>
        <a href="/admin/feedback" class="nav-item">反馈管理</a>
      </nav>
      <div class="header-right">
        <button class="back-btn" @click="goBack">返回控制台</button>
      </div>
    </header>

    <div class="page-content">
      <div class="user-list">
        <div class="user-item" v-for="user in users" :key="user.id">
          <div class="user-info">
            <div class="user-header">
              <span class="username">{{ user.username }}</span>
              <span v-if="user.username === 'admin'" class="admin-badge super-admin">
                总管理员
              </span>
              <span v-else :class="['admin-badge', { active: user.isAdmin }]">
                {{ user.isAdmin ? '管理员' : '普通用户' }}
              </span>
            </div>
            <p><span class="label">ID:</span><span class="value">{{ user.id }}</span></p>
            <p><span class="label">密码:</span><span class="value password">{{ user.password || '******' }}</span></p>
            <div class="bottom-info">
              <span class="status">
                状态: <span :class="['status-badge', { active: user.enabled }]">
                  {{ user.enabled ? '正常' : '禁用' }}
                </span>
              </span>
              <span class="reg-time">注册时间: {{ user.createdAt || '未知' }}</span>
            </div>
          </div>
          <div class="user-actions">
            <div 
              v-if="user.username !== 'admin'" 
              @click="toggleUser(user)" 
              class="action-btn"
            >
              {{ user.enabled ? '禁用' : '启用' }}
            </div>
            <div 
              v-else 
              class="action-btn disabled"
              title="主管理员不可禁用"
            >
              {{ user.enabled ? '禁用' : '启用' }}
            </div>
            <div 
              v-if="user.username !== 'admin'" 
              @click="deleteUser(user.id)" 
              class="action-btn delete"
            >删除</div>
            <div 
              v-else 
              class="action-btn delete disabled"
              title="主管理员不可删除"
            >删除</div>
          </div>
        </div>
        
        <div v-if="users.length === 0" class="empty-tip">暂无用户数据</div>
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
      <a href="/admin/user" class="mobile-nav-item active">
        <span>用户</span>
      </a>
      <a href="/admin/feedback" class="mobile-nav-item">
        <span>反馈</span>
      </a>
    </nav>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";

const router = useRouter();

interface User {
  id: number | string;
  username: string;
  password: string;
  email: string;
  phone: string;
  enabled: boolean;
  isAdmin: boolean;
  createdAt?: string;
}

const users = ref<User[]>([]);

onMounted(() => {
  const isAdmin = localStorage.getItem("isAdmin") === "true";
  const token = localStorage.getItem("token");
  
  if (!token || !isAdmin) {
    router.push("/admin/login");
    return;
  }
  
  loadUsers();
});

async function loadUsers() {
  const token = localStorage.getItem("token");
  
  try {
    console.log('正在请求用户列表，token:', token);
    
    const response = await fetch("http://localhost:8081/api/admin/users", {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        "x-auth-token": token || ""
      }
    });
    
    console.log('响应状态:', response.status);
    
    const data = await response.json();
    console.log('响应数据:', data);
    
    if (data.code === 200) {
      users.value = data.data.map((u: User) => ({
        ...u,
        password: '******'
      }));
    } else {
      alert(data.message || "获取用户列表失败");
    }
  } catch (error) {
    console.error('请求错误:', error);
    alert("获取用户列表失败，请确保后端服务已启动\n\n错误信息: " + (error as Error).message);
  }
}

async function toggleUser(user: User) {
  const token = localStorage.getItem("token");
  
  try {
    const response = await fetch(`http://localhost:8081/api/admin/users/${user.id}/status`, {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
        "x-auth-token": token || ""
      },
      body: JSON.stringify({ enabled: !user.enabled })
    });
    
    const data = await response.json();
    
    if (data.code === 200) {
      user.enabled = !user.enabled;
      alert(`用户 ${user.username} 已${user.enabled ? '启用' : '禁用'}`);
    } else {
      alert(data.message || "操作失败");
    }
  } catch (error) {
    alert("操作失败，请确保后端服务已启动");
  }
}

async function deleteUser(userId: number | string) {
  if (!confirm(`确定要删除用户吗？此操作不可恢复。`)) {
    return;
  }
  
  const token = localStorage.getItem("token");
  
  try {
    const response = await fetch(`http://localhost:8081/api/admin/users/${userId}`, {
      method: "DELETE",
      headers: {
        "x-auth-token": token || ""
      }
    });
    
    const data = await response.json();
    
    if (data.code === 200) {
      users.value = users.value.filter(u => u.id !== userId);
      alert('用户已删除');
    } else {
      alert(data.message || "删除失败");
    }
  } catch (error) {
    alert("删除失败，请确保后端服务已启动");
  }
}

function goBack() {
  router.push("/admin");
}
</script>

<style scoped>
.user-page {
  min-height: 100vh;
  background: #fff;
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
  background: #fff;
}

.page-header h2 {
  margin: 0;
  font-size: 28px;
  color: #333;
}

.user-count {
  font-size: 14px;
  color: #999;
  margin-left: 15px;
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

.user-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.user-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  background: #fff;
  padding: 20px;
  border-radius: 10px;
  border: 1px solid #e5e5e5;
}

.user-info {
  flex: 1;
}

.user-info p {
  margin: 8px 0;
  font-size: 14px;
}

.label {
  color: #999;
  width: 80px;
  display: inline-block;
}

.value {
  color: #333;
  font-weight: 500;
}

.value.password {
  color: #666;
  font-family: monospace;
}

.user-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.username {
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.admin-badge {
  padding: 3px 10px;
  background: #f0f0f0;
  color: #666;
  border-radius: 20px;
  font-size: 12px;
}

.admin-badge.active {
  background: #ffe8dc;
  color: #ff4400;
}

.admin-badge.super-admin {
  background: linear-gradient(135deg, red);
  color: #fff;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.4);
}

.status-badge {
  padding: 3px 10px;
  background: #f0f0f0;
  color: #999;
  border-radius: 20px;
  font-size: 12px;
}

.status-badge.active {
  background: #e8f5e9;
  color: #2e7d32;
}

.reg-time {
  color: #999;
  font-size: 13px;
}

.bottom-info {
  display: flex;
  align-items: center;
  gap: 30px;
}

.status {
  color: #666;
}

.user-actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.action-btn {
  padding: 8px 15px;
  background: #f0f0f0;
  border-radius: 6px;
  font-size: 13px;
  cursor: pointer;
  text-align: center;
  color: #333;
}

.action-btn:hover {
}

.action-btn.delete {
  background: #fff0f0;
  color: #ff6666;
}

.action-btn.delete:hover {
}

.action-btn.disabled {
  background: #f5f5f5;
  color: #ccc;
  cursor: not-allowed;
  pointer-events: none;
}

.action-btn.delete.disabled {
  background: #f5f5f5;
  color: #ccc;
  cursor: not-allowed;
  pointer-events: none;
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

  .user-list {
    gap: 12px;
  }

  .user-item {
    flex-direction: column;
    padding: 15px;
    gap: 15px;
  }

  .user-header {
    flex-wrap: wrap;
  }

  .username {
    font-size: 16px;
  }

  .admin-badge {
    padding: 3px 8px;
    font-size: 11px;
  }

  .user-info p {
    margin: 6px 0;
    font-size: 13px;
  }

  .label {
    width: 60px;
    font-size: 12px;
  }

  .value {
    font-size: 13px;
  }

  .bottom-info {
    flex-direction: column;
    gap: 8px;
  }

  .reg-time {
    font-size: 12px;
  }

  .user-actions {
    flex-direction: row;
    justify-content: flex-end;
    padding-top: 10px;
    border-top: 1px solid #f0f0f0;
  }

  .action-btn {
    padding: 8px 16px;
    font-size: 12px;
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
