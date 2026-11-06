<template>
  <div class="data-page">
    <header class="page-header">
      <div class="header-left">
        <h2>数据概览</h2>
      </div>
      <div class="header-right">
        <button class="back-btn" @click="goBack">返回控制台</button>
      </div>
    </header>

    <div class="page-content">
      <div class="summary-section">
        <div class="summary-card">
          <div class="card-label">总订单数:</div>
          <div class="card-content">
            <div class="card-value">{{ totalOrders }}</div>
          </div>
        </div>
        <div class="summary-card">
          <div class="card-label">用户总数:</div>
          <div class="card-content">
            <div class="card-value">{{ activeUsers }}</div>
          </div>
        </div>
        <div class="summary-card">
          <div class="card-label">总收入:</div>
          <div class="card-content">
            <div class="card-value">{{ totalRevenue }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";

const router = useRouter();
const totalOrders = ref(0);
const activeUsers = ref(0);
const totalRevenue = ref(0);

onMounted(() => {
  const isAdmin = localStorage.getItem("isAdmin") === "true";
  const token = localStorage.getItem("token");
  
  if (!token || !isAdmin) {
    router.push("/admin/login");
  }
  
  loadData();
});

async function loadData() {
  const token = localStorage.getItem("token");

  try {
    const usersResponse = await fetch("http://localhost:8081/api/admin/users", {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        "x-auth-token": token || ""
      }
    });

    const usersData = await usersResponse.json();
    if (usersData.code === 200) {
      const normalUsers = usersData.data.filter(user => user.role !== 'admin' && user.role !== 'subadmin');
      activeUsers.value = normalUsers.length;
      console.log("普通用户数:", activeUsers.value);
    }
  } catch (error) {
    console.error("获取用户数据失败:", error);
  }
}

function goBack() {
  router.push("/admin");
}
</script>

<style scoped>
.data-page {
  min-height: 100vh;
  background: #fff;
  padding-bottom: 30px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 30px;
  background: rgba(255, 255, 255, 0.95);
}

.page-header h2 {
  margin: 0;
  font-size: 28px;
  color: #333;
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

.summary-section {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 30px;
}

.summary-card {
  display: flex;
  align-items: center;
  gap: 15px;
  background: #fff;
  padding: 25px;
  border-radius: 12px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.card-content {
  flex: 1;
}

.card-value {
  font-size: 28px;
  font-weight: bold;
  color: #333;
}

.card-label {
  font-size: 13px;
  color: #999;
  margin-top: 4px;
}
</style>
