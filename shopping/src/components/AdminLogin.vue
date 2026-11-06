<template>
  <div class="admin-login-container">
    <div class="login-box">
      <h2>管理员登录</h2>
      <form @submit.prevent="login">
        <div class="form-group">
          <label>管理员账号</label>
          <input v-model="username" type="text" placeholder="请输入管理员账号" />
        </div>
        <div class="form-group">
          <label>密码</label>
          <input v-model="password" type="password" placeholder="请输入密码" />
        </div>
        <button type="submit" class="login-btn">登录</button>
        <button type="button" class="back-btn" @click="goToUserLogin">返回普通用户登录</button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();
let username = ref('');
let password = ref('');

async function login() {
  if (!username.value.trim() || !password.value.trim()) {
    alert('账号密码都不能空');
    return;
  }
  
  // 清除之前的登录状态，防止状态混乱
  localStorage.removeItem("token");
  localStorage.removeItem("loginUser");
  localStorage.removeItem("isAdmin");
  
  try {
    const response = await fetch('http://localhost:8081/api/admin/login', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        username: username.value,
        password: password.value
      })
    });
    
    const data = await response.json();
    
    if (data.code === 200) {
      localStorage.setItem('token', data.data.token);
      localStorage.setItem('loginUser', data.data.user.username);
      localStorage.setItem('isAdmin', String(data.data.user.isAdmin));
      router.push('/admin');
    } else {
      alert(data.message || '登录失败');
    }
  } catch (error) {
    alert('登录失败，请确保后端服务已启动');
  }
}

function goToUserLogin() {
  router.push('/login');
}
</script>

<style scoped>
.admin-login-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: #f5f5f5;
}
.login-box {
  background: white;
  padding: 40px;
  border-radius: 16px;
  box-shadow: 0 10px 40px rgba(0,0,0,0.2);
  width: 360px;
}
.login-box h2 {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
}
.form-group {
  margin-bottom: 20px;
}
.form-group label {
  display: block;
  margin-bottom: 8px;
  color: #666;
}
.form-group input {
  width: 100%;
  padding: 12px;
  border: 2px solid #eee;
  border-radius: 8px;
  font-size: 14px;
  transition: border-color 0.3s;
}
.form-group input:focus {
  outline: none;
  border-color: #667eea;
}
.login-btn {
  width: 100%;
  padding: 14px;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}
.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 5px 20px rgba(102, 126, 234, 0.4);
}
.back-btn {
  width: 100%;
  padding: 12px;
  background: #f5f5f5;
  color: #666;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  margin-top: 10px;
  transition: all 0.3s;
}
.back-btn:hover {
  background: #eee;
  border-color: #ccc;
  color: #333;
}
</style>
