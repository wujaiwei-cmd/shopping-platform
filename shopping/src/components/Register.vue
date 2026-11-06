<template>
  <div class="register-wrap">
    <div class="register-card">
      <div class="card-head">
        <span>新用户注册</span>
      </div>

      <div class="form-item">
        <label><span class="required">*</span>用户名</label>
        <input 
          v-model="userName" 
          type="text" 
          placeholder="请输入4位以上用户名"
          class="input-field"
        />
      </div>

      <div class="form-item">
        <label><span class="required">*</span>设置密码</label>
        <input 
          v-model="userPass" 
          type="password" 
          placeholder="请输入6位以上密码"
          class="input-field"
        />
      </div>

      <div class="form-item">
        <label>确认密码</label>
        <input 
          v-model="userPass2" 
          type="password" 
          placeholder="请再次输入密码"
          class="input-field"
        />
      </div>

      <div class="btn-row">
        <button @click="submitForm" class="reg-btn">立即注册</button>
        <button @click="goBack" class="back-btn">已有账号? 返回登录</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { useRouter } from "vue-router";

const router = useRouter();
const userName = ref("");
const userPass = ref("");
const userPass2 = ref("");

function goBack() {
  router.push("/login");
}

async function submitForm() {
  if (!userName.value.trim()) {
    alert("用户名不能为空");
    return;
  }
  if (userName.value.length < 4) {
    alert("用户名至少要4位哦");
    return;
  }
  if (!userPass.value) {
    alert("密码不能空");
    return;
  }
  if (userPass.value.length < 6) {
    alert("密码至少要6位");
    return;
  }
  if (userPass.value !== userPass2.value) {
    alert("两次输入的密码不一样");
    return;
  }

  try {
    const response = await fetch("http://localhost:8081/api/auth/register", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        username: userName.value,
        password: userPass.value,
        email: userName.value + "@example.com",
        phone: "",
      }),
    });

    const data = await response.json();

    if (data.code === 200) {
      localStorage.setItem("token", data.data.token);
      localStorage.setItem("loginUser", data.data.user.username);
      localStorage.setItem("isAdmin", String(data.data.user.isAdmin));
      alert("注册成功啦，马上跳转到首页");
      setTimeout(() => {
        router.push("/home");
      }, 1000);
    } else {
      alert(data.message || "注册失败");
    }
  } catch (error) {
    alert("注册失败，请确保后端服务已启动\n\n后端地址: http://localhost:8081");
  }
}
</script>

<style scoped>
.register-wrap {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: #f5f5f5;
}

.register-card {
  width: 380px;
  padding: 30px;
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 2px 15px rgba(0,0,0,0.08);
}

.card-head {
  text-align: center;
  font-size: 20px;
  font-weight: bold;
  color: #333;
  padding-bottom: 20px;
  border-bottom: 1px solid #eee;
  margin-bottom: 20px;
}

.form-item {
  margin-bottom: 18px;
}

.form-item label {
  display: block;
  margin-bottom: 8px;
  font-size: 14px;
  color: #555;
}

.required {
  color: #f5222d;
  margin-right: 4px;
}

.input-field {
  width: 100%;
  height: 42px;
  padding: 0 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  box-sizing: border-box;
  outline: none;
}

.input-field:focus {
  border-color: #4a90d9;
}

.input-field::placeholder {
  color: #ccc;
}

.btn-row {
  margin-top: 20px;
}

.reg-btn {
  width: 100%;
  height: 44px;
  background: #4a90d9;
  border: none;
  border-radius: 6px;
  color: #fff;
  font-size: 16px;
  cursor: pointer;
}

.reg-btn:hover {
  background: #3a7fc8;
}

.back-btn {
  width: 100%;
  height: 40px;
  background: transparent;
  border: none;
  color: #4a90d9;
  font-size: 14px;
  cursor: pointer;
  margin-top: 10px;
}

.back-btn:hover {
  text-decoration: underline;
}
</style>
