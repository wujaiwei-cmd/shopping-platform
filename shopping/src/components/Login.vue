<template>
  <!-- 登录页面主容器 -->
  <div class="login-box">
    <!-- 登录卡片 -->
    <div class="login-card">
      <div class="card-title">
        <h2>用户登录</h2>
      </div>

      <!-- 账号输入框 -->
      <div class="form-group">
        <label>账号</label>
        <input 
          v-model="account" 
          type="text" 
          placeholder="请输入用户名"
          class="form-input"
        />
      </div>

      <!-- 密码输入框 -->
      <div class="form-group">
        <label>密码</label>
        <input 
          v-model="password" 
          type="password" 
          placeholder="请输入密码"
          class="form-input"
        />
      </div>

      <!-- 按钮组 -->
      <div class="btn-group">
        <button @click="login" :disabled="isLoading" class="submit-btn">
          {{ isLoading ? '登录中...' : '立即登录' }}
        </button>

        <div class="link-row">
          <span>还没有账号?</span>
          <a href="#" @click.prevent="toRegister" class="reg-link">立即注册</a>
        </div>

        <!-- 管理员登录切换按钮 -->
        <div class="admin-switch">
          <button @click="goAdminLogin" class="admin-btn"> 管理员登录</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
// 导入Vue组合式API和路由模块
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";

// 初始化路由实例
const router = useRouter();

// 响应式变量定义
const account = ref("");      // 用户输入的账号
const password = ref("");     // 用户输入的密码
const isLoading = ref(false); // 登录按钮加载状态

/**
 * 跳转到注册页面
 */
function toRegister() {
  router.push("/register");
}

/**
 * 跳转到管理员登录页面
 */
function goAdminLogin() {
  router.push("/admin/login");
}

/**
 * 用户登录核心逻辑
 * 1. 验证账号密码不为空
 * 2. 发送登录请求到后端API
 * 3. 根据返回结果处理登录成功或失败
 * 4. 管理员用户跳转到管理后台，普通用户跳转到首页
 */
async function login() {
  // 表单验证：账号和密码都不能为空
  if (!account.value.trim() || !password.value.trim()) {
    alert("账号密码都不能空哦");
    return;
  }

  // 清除之前的登录状态，防止状态混乱
  localStorage.removeItem("token");
  localStorage.removeItem("loginUser");
  localStorage.removeItem("isAdmin");

  // 设置加载状态，防止重复提交
  isLoading.value = true;

  try {
    // 调用后端登录接口
    const response = await fetch("http://localhost:8081/api/auth/login", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        username: account.value,
        password: password.value,
      }),
    });

    // 解析响应数据
    const data = await response.json();

    console.log("=== 登录响应 ===");
    console.log("完整响应数据:", data);
    
    // 根据返回码判断登录结果
    if (data.code === 200) {
      console.log("登录成功，准备保存数据");
      console.log("token:", data.data.token);
      console.log("user:", data.data.user);
      
      // 强制清除之前的管理员登录状态，确保普通用户登录后不会继承管理员状态
      localStorage.removeItem("token");
      localStorage.removeItem("loginUser");
      localStorage.removeItem("isAdmin");
      
      // 登录成功：保存用户信息到本地存储
      localStorage.setItem("token", data.data.token);
      localStorage.setItem("loginUser", data.data.user.username);
      localStorage.setItem("isAdmin", String(data.data.user.isAdmin));
      
      // 验证登录用户和输入的账号是否一致
      if (data.data.user.username !== account.value.trim()) {
        alert("登录异常：登录用户与输入账号不一致");
        localStorage.removeItem("token");
        localStorage.removeItem("loginUser");
        localStorage.removeItem("isAdmin");
        return;
      }
      
      // 普通用户登录，强制设置 isAdmin 为 false
      const isAdminValue = data.data.user.isAdmin ? "true" : "false";
      localStorage.setItem("isAdmin", isAdminValue);
      
      // 显示欢迎信息
      alert(`登录成功，欢迎 ${data.data.user.username}`);
      
      // 强制刷新页面，确保状态正确
      if (data.data.user.isAdmin) {
        window.location.href = "/admin";
      } else {
        window.location.href = "/home";
      }
    } else {
      // 登录失败：显示错误信息
      alert(data.message || "登录失败");
    }
  } catch (error) {
    // 网络异常处理
    alert("网络错误，请稍后重试");
  } finally {
    // 恢复按钮状态
    isLoading.value = false;
  }
}

// 组件挂载时执行的初始化逻辑
onMounted(() => {
  // 页面加载完成后的初始化操作
});
</script>

<style scoped>
/* 登录页面背景样式 */
.login-box {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: #f5f5f5;
}

/* 登录卡片样式 */
.login-card {
  width: 360px;
  padding: 30px;
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.1);
}

/* 卡片标题区域 */
.card-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
}

.card-title h2 {
  margin: 0;
  font-size: 22px;
  color: #333;
}

/* 表单组样式 */
.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-size: 14px;
  color: #666;
}

/* 输入框样式 */
.form-input {
  width: 100%;
  height: 42px;
  padding: 0 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  box-sizing: border-box;
  outline: none;
}

.form-input:focus {
  border-color: #4a90d9;
}

.form-input::placeholder {
  color: #ccc;
}

/* 按钮组样式 */
.btn-group {
  margin-top: 10px;
}

.submit-btn {
  width: 100%;
  height: 44px;
  background: #4a90d9;
  border: none;
  border-radius: 6px;
  color: #fff;
  font-size: 16px;
  cursor: pointer;
}

.submit-btn:hover:not(:disabled) {
  background: #3a7fc8;
}

.submit-btn:disabled {
  background: #a0c7e8;
  cursor: not-allowed;
}

/* 注册链接区域 */
.link-row {
  text-align: center;
  margin-top: 18px;
  font-size: 13px;
  color: #999;
}

.reg-link {
  color: #4a90d9;
  margin-left: 5px;
  text-decoration: none;
}

.reg-link:hover {
  text-decoration: underline;
}

/* 管理员登录切换区域 */
.admin-switch {
  text-align: center;
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px dashed #eee;
  font-size: 13px;
  color: #999;
}

.admin-btn {
  margin-left: 8px;
  padding: 6px 14px;
  background: transparent;
  border: 1px solid #667eea;
  border-radius: 20px;
  color: #667eea;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.3s;
}

.admin-btn:hover {
  background: #667eea;
  color: #fff;
}
</style>