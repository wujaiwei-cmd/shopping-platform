<template>
  <div class="feedback-page">
    <!-- 头部 -->
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
        <a href="#" class="nav-item active">反馈管理</a>
      </nav>
      <div class="header-right">
        <button class="back-btn" @click="goBack">返回控制台</button>
      </div>
    </header>

    <!-- 页面内容 -->
    <main class="page-content">
      <!-- 反馈标签页 -->
      <div class="feedback-tabs">
        <button 
          v-for="tab in tabs" 
          :key="tab.value"
          :class="['tab-btn', { active: activeTab === tab.value }]"
          @click="activeTab = tab.value"
        >
          {{ tab.label }}
        </button>
        <button class="tab-btn refresh-btn" @click="loadFeedbacks">刷新</button>
      </div>

      <!-- 反馈列表 -->
      <div class="feedback-list">
        <div v-if="feedbacks.length === 0" class="empty-state">
          <div class="empty-icon">📭</div>
          <div class="empty-text">暂无数据</div>
        </div>

        <div v-for="feedback in filteredFeedbacks" :key="feedback.id" class="feedback-card">
          <div class="card-header">
            <span :class="['feedback-badge', feedback.type === 'complaint' ? 'complaint' : 'feedback']">
              {{ feedback.type === 'complaint' ? '投诉' : '反馈' }}
            </span>
            <span :class="['feedback-status', feedback.isProcessed ? 'processed' : 'pending']">
              {{ feedback.isProcessed ? '✓ 已处理' : '⏳ 待处理' }}
            </span>
          </div>

          <h3 class="title-text">{{ feedback.title }}</h3>
          <p class="feedback-content">{{ feedback.content }}</p>

          <div class="feedback-meta">
            <span>用户: {{ feedback.username || '匿名用户' }}</span>
            <span>联系方式: {{ feedback.contact || '未提供' }}</span>
            <span>时间: {{ formatDate(feedback.createdAt) }}</span>
          </div>

          <div v-if="feedback.isProcessed && feedback.processedNote" class="processed-note">
            <span class="note-label">处理备注:</span>
            <span class="note-content">{{ feedback.processedNote }}</span>
          </div>

          <div v-if="!feedback.isProcessed" class="feedback-actions">
            <input 
              v-model="processNotes[feedback.id]" 
              type="text" 
              class="process-input" 
              placeholder="请输入处理结果..." 
            />
            <div class="btn-group">
              <button class="process-btn" @click="processFeedback(feedback.id)">处理</button>
              <button class="delete-btn" @click="deleteFeedback(feedback.id)">删除</button>
            </div>
          </div>

          <div v-if="feedback.isProcessed" class="feedback-actions">
            <div class="btn-group">
              <button class="delete-btn" @click="deleteFeedback(feedback.id)">删除</button>
            </div>
          </div>
        </div>
      </div>
    </main>

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
      <a href="/admin/feedback" class="mobile-nav-item active">
        <span>反馈</span>
      </a>
    </nav>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import { useRouter } from "vue-router";

const router = useRouter();
const activeTab = ref('all');
const feedbacks = ref<any[]>([]);
const processNotes = ref<Record<number, string>>({});

const tabs = [
  { label: '全部', value: 'all' },
  { label: '待处理', value: 'pending' },
  { label: '已处理', value: 'processed' },
];

const filteredFeedbacks = computed(() => {
  if (activeTab.value === 'all') return feedbacks.value;
  if (activeTab.value === 'pending') return feedbacks.value.filter(f => !f.isProcessed);
  if (activeTab.value === 'processed') return feedbacks.value.filter(f => f.isProcessed);
  return feedbacks.value;
});

const loadFeedbacks = async () => {
  try {
    const token = localStorage.getItem('token');
    const res = await fetch('http://localhost:8081/api/admin/feedbacks', {
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token || ''}`
      }
    });
    const data = await res.json();
    if (data.code === 200) {
      feedbacks.value = data.data || [];
    }
  } catch (error) {
    console.error('加载反馈失败:', error);
  }
};

const processFeedback = async (id: number) => {
  const note = processNotes.value[id];
  if (!note) {
    alert('请输入处理结果');
    return;
  }

  try {
    const token = localStorage.getItem('token');
    const res = await fetch(`http://localhost:8081/api/admin/feedbacks/${id}/process`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token || ''}`
      },
      body: JSON.stringify({ note })
    });
    const data = await res.json();
    if (data.code === 200) {
      alert('处理成功');
      processNotes.value[id] = '';
      await loadFeedbacks();
    }
  } catch (error) {
    console.error('处理反馈失败:', error);
  }
};

const deleteFeedback = async (id: number) => {
  if (!confirm('确定要删除这条反馈吗？')) return;

  try {
    const token = localStorage.getItem('token');
    const res = await fetch(`http://localhost:8081/api/admin/feedbacks/${id}`, {
      method: 'DELETE',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token || ''}`
      }
    });
    const data = await res.json();
    if (data.code === 200) {
      alert('删除成功');
      await loadFeedbacks();
    }
  } catch (error) {
    console.error('删除反馈失败:', error);
  }
};

const formatDate = (dateStr: string) => {
  if (!dateStr) return '';
  return new Date(dateStr).toLocaleString('zh-CN');
};

const goBack = () => {
  router.push('/admin');
};

const logout = () => {
  localStorage.removeItem('token');
  localStorage.removeItem('isAdmin');
  router.push('/admin/login');
};

onMounted(() => {
  loadFeedbacks();
});
</script>

<style scoped>
.feedback-page {
  min-height: 100vh;
  background: #f5f7fa;
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
  background: linear-gradient(90deg, #4a90d9, #3a7fc8);
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.header-left h2 {
  margin: 0;
  font-size: 24px;
  color: #fff;
}

.header-right {
  display: flex;
  gap: 15px;
}

.logout-btn:hover {
  background: #ff4444;
}

.page-content {
  padding: 30px;
}

.feedback-tabs {
  display: flex;
  gap: 10px;
  margin-bottom: 25px;
}

.tab-btn {
  padding: 10px 20px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  background: #fff;
  color: #666;
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.tab-btn.active {
  background: linear-gradient(135deg, #4a90d9, #3a7fc8);
  color: #fff;
}

.tab-btn:hover {
  transform: translateY(-2px);
}

.refresh-btn {
  margin-left: auto;
}

.feedback-list {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 20px;
}

.feedback-card {
  background: #fff;
  padding: 25px;
  border-radius: 12px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.08);
  transition: transform 0.3s;
}

.feedback-card:hover {
  transform: translateY(-3px);
}

.card-header {
  display: flex;
  gap: 10px;
  margin-bottom: 15px;
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
  margin: 0 0 12px 0;
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
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.feedback-actions .btn-group {
  display: flex;
  gap: 10px;
  margin-left: auto;
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

.process-btn,
.delete-btn {
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
  grid-column: 1 / -1;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.empty-text {
  font-size: 15px;
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

  .feedback-tabs {
    flex-wrap: wrap;
    gap: 8px;
  }

  .tab-btn {
    padding: 8px 14px;
    font-size: 12px;
  }

  .feedback-list {
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .feedback-card {
    padding: 18px;
  }

  .card-header {
    flex-wrap: wrap;
    gap: 8px;
  }

  .title-text {
    font-size: 15px;
  }

  .feedback-content {
    font-size: 13px;
  }

  .feedback-meta {
    gap: 10px;
    font-size: 11px;
  }

  .feedback-actions {
    flex-direction: row;
    justify-content: space-between;
  }

  .process-input {
    flex: 1;
    margin-right: 10px;
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