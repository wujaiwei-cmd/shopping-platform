<template>
  <div class="help-page">
    <header class="header">
      <div class="header-center">
        <h1>购物平台</h1>
      </div>
      <nav class="header-right">
        <a href="#" @click.prevent="goHome" class="nav-item">首页</a>
        <a href="#" @click.prevent="goProfile" class="nav-item">我的</a>
        <a href="#" @click.prevent="goCart" class="nav-item">购物车</a>
        <a href="#" @click.prevent="goFav" class="nav-item">收藏夹</a>
        <a href="#" class="nav-item active">帮助中心</a>
      </nav>
    </header>

    <div class="help-content">
      <h2>帮助中心</h2>
      
      <div class="help-nav">
        <span class="nav-btn" @click="openFeedback('feedback')">反馈</span>
        <span class="nav-btn" @click="openFeedback('complaint')">投诉</span>
        <span class="nav-btn" @click="openRefundSection()" :class="{ active: activeSection === 'refund' }">退款售后</span>
      </div>

      <div v-if="activeSection !== 'refund'" class="faq-box">
        <h3>常见问题</h3>
        <div class="faq-items">
          <p @click="openFeedbackWithTitle('无法退货退款怎么办？')">无法退货退款怎么办？</p>
          <p @click="openFeedbackWithTitle('无法下单怎么办？')">无法下单怎么办？</p>
          <p @click="openFeedbackWithTitle('网站无响应怎么办？')">网站无响应怎么办？</p>
        </div>
      </div>

      <div v-if="activeSection === 'refund'" class="refund-section">
        <h3 class="section-title">我的订单 - 退款售后</h3>
        <div class="order-list">
          <div class="order-card" v-for="order in userOrders" :key="order.id">
            <div class="order-header">
              <span class="order-no">订单号: {{ order.orderNo }}</span>
              <span class="order-status" :class="order.status">{{ getOrderStatusText(order.status) }}</span>
            </div>
            <div class="order-info">
              <span class="order-amount">金额: ¥{{ order.totalAmount }}</span>
              <span class="order-time">下单时间: {{ formatDate(order.createdAt) }}</span>
            </div>
            <div v-if="order.refundStatus" class="refund-status" :class="order.refundStatus">
              退款状态: {{ getRefundStatusText(order.refundStatus) }}
            </div>
            <div v-if="order.refundReason" class="refund-reason">
              退款原因: {{ order.refundReason }}
            </div>
            <div class="order-actions">
              <button 
                v-if="!order.refundStatus || ['refund_rejected'].includes(order.refundStatus)" 
                class="btn-refund" 
                @click="openRefundModal(order)"
              >
                申请退款
              </button>
              <span v-else-if="order.refundStatus === 'refund_pending'" class="status-pending">
                ⏳ 退款审核中...
              </span>
              <span v-else-if="order.refundStatus === 'refund_approved'" class="status-approved">
                ✓ 退款已通过
              </span>
            </div>
          </div>
          <div v-if="userOrders.length === 0" class="empty-orders">
            <div class="empty-icon">📦</div>
            <p>暂无订单记录</p>
          </div>
        </div>
      </div>
    </div>

    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal-content">
        <div class="modal-header">
          <h3>{{ modalTitle }}</h3>
          <span class="close-btn" @click="closeModal">&times;</span>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>标题 *</label>
            <input v-model="form.title" type="text" placeholder="请输入标题" maxlength="50" />
          </div>
          <div class="form-group">
            <label>内容 *</label>
            <textarea v-model="form.content" placeholder="请详细描述您的问题或建议..." rows="6" maxlength="1000"></textarea>
          </div>
          <div class="form-group">
            <label>联系方式 (选填)</label>
            <input v-model="form.contact" type="text" placeholder="手机号或邮箱，方便我们联系您" />
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn-cancel" @click="closeModal">取消</button>
          <button class="btn-submit" @click="submitFeedback" :disabled="submitting">
            {{ submitting ? '提交中...' : '提交' }}
          </button>
        </div>
      </div>
    </div>

    <div v-if="showRefundModal" class="modal-overlay" @click.self="closeRefundModal">
      <div class="modal-content modal-small">
        <div class="modal-header">
          <h3>申请退款</h3>
          <span class="close-btn" @click="closeRefundModal">&times;</span>
        </div>
        <div class="modal-body">
          <div class="refund-order-info">
            <p>订单号: {{ selectedOrder?.orderNo }}</p>
            <p>金额: ¥{{ selectedOrder?.totalAmount }}</p>
          </div>
          <div class="form-group">
            <label>退款原因 *</label>
            <textarea 
              v-model="refundReason" 
              placeholder="请详细描述退款原因..." 
              rows="4"
            ></textarea>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn-cancel" @click="closeRefundModal">取消</button>
          <button class="btn-submit" @click="submitRefund" :disabled="refundSubmitting">
            {{ refundSubmitting ? '提交中...' : '确认申请退款' }}
          </button>
        </div>
      </div>
    </div>

    <div v-if="showSuccess" class="toast-success">
      {{ successMessage }}
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
      <a href="#" class="mobile-nav-item active" @click.prevent="goHelp">
        <span>帮助</span>
      </a>
    </nav>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from "vue-router";
import { ref } from "vue";

const router = useRouter();

const activeSection = ref('');
const showModal = ref(false);
const showRefundModal = ref(false);
const showSuccess = ref(false);
const submitting = ref(false);
const refundSubmitting = ref(false);
const modalTitle = ref('');
const successMessage = ref('');

const form = ref({
  type: 'feedback',
  title: '',
  content: '',
  contact: ''
});

const userOrders = ref<any[]>([]);
const selectedOrder = ref<any>(null);
const refundReason = ref('');

function openFeedback(type: string) {
  activeSection.value = '';
  form.value.type = type;
  if (type === 'feedback') {
    modalTitle.value = '给我们提点意见';
  } else {
    modalTitle.value = '我要投诉';
  }
  form.value.title = '';
  form.value.content = '';
  form.value.contact = '';
  showModal.value = true;
}

function openFeedbackWithTitle(title: string) {
  activeSection.value = '';
  form.value.type = 'feedback';
  modalTitle.value = '提交反馈';
  form.value.title = title;
  form.value.content = '';
  form.value.contact = '';
  showModal.value = true;
}

async function openRefundSection() {
  activeSection.value = 'refund';
  await loadUserOrders();
}

async function loadUserOrders() {
  const token = localStorage.getItem('token');
  if (!token) {
    alert('请先登录');
    return;
  }
  try {
    const res = await fetch('http://localhost:8081/api/orders', {
      headers: {
        'Content-Type': 'application/json',
        'x-auth-token': token
      }
    });
    const data = await res.json();
    if (data.code === 200) {
      const orderData = data.data || [];
      userOrders.value = orderData.map((item: any) => {
        if (item.order) {
          return {
            ...item.order,
            items: item.items || []
          };
        }
        return item;
      });
    }
  } catch (err) {
    console.error('加载订单失败:', err);
    alert('加载订单失败，请检查后端服务是否启动！');
  }
}

function openRefundModal(order: any) {
  selectedOrder.value = order;
  refundReason.value = '';
  showRefundModal.value = true;
}

function closeRefundModal() {
  showRefundModal.value = false;
  selectedOrder.value = null;
  refundReason.value = '';
}

async function submitRefund() {
  if (!refundReason.value.trim()) {
    alert('请填写退款原因');
    return;
  }
  refundSubmitting.value = true;
  const token = localStorage.getItem('token');
  try {
    const res = await fetch(`http://localhost:8081/api/orders/${selectedOrder.value.id}/refund`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'x-auth-token': token || ''
      },
      body: JSON.stringify({ reason: refundReason.value })
    });
    const data = await res.json();
    if (data.code === 200) {
      closeRefundModal();
      showToast('退款申请已提交，请等待管理员审核');
      loadUserOrders();
    } else {
      alert(data.message || '提交失败');
    }
  } catch (err) {
    alert('提交失败，请稍后重试');
  } finally {
    refundSubmitting.value = false;
  }
}

function closeModal() {
  showModal.value = false;
}

function showToast(msg: string) {
  successMessage.value = msg;
  showSuccess.value = true;
  setTimeout(() => {
    showSuccess.value = false;
  }, 3000);
}

async function submitFeedback() {
  if (!form.value.title.trim()) {
    alert('请输入标题');
    return;
  }
  if (!form.value.content.trim()) {
    alert('请输入内容');
    return;
  }

  submitting.value = true;

  try {
    const token = localStorage.getItem('token');
    const res = await fetch('http://localhost:8081/api/feedback', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        ...(token ? { 'Authorization': `Bearer ${token}` } : {})
      },
      body: JSON.stringify({
        type: form.value.type,
        title: form.value.title,
        content: form.value.content,
        contact: form.value.contact
      })
    });

    const data = await res.json();
    
    if (data.code === 200) {
      closeModal();
      showToast('提交成功！我们会尽快处理您的' + (form.value.type === 'feedback' ? '反馈' : '投诉'));
    } else {
      alert(data.message || '提交失败');
    }
  } catch (err) {
    alert('提交失败，请稍后重试');
  } finally {
    submitting.value = false;
  }
}

function getOrderStatusText(status: string) {
  const map: Record<string, string> = {
    'pending': '待处理',
    'paid': '已付款',
    'shipped': '已发货',
    'completed': '已完成',
    'cancelled': '已取消'
  };
  return map[status] || status;
}

function getRefundStatusText(status: string) {
  const map: Record<string, string> = {
    'refund_pending': '待审核',
    'refund_approved': '已通过',
    'refund_rejected': '已拒绝'
  };
  return map[status] || status;
}

function formatDate(dateStr: string) {
  if (!dateStr) return '-';
  const date = new Date(dateStr);
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  const hours = String(date.getHours()).padStart(2, '0');
  const minutes = String(date.getMinutes()).padStart(2, '0');
  return `${year}-${month}-${day} ${hours}:${minutes}`;
}

function goHome() {
  router.push("/home");
}

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
  // 已经在帮助中心页面，不需要跳转
}
</script>

<style scoped>
.help-page {
  min-height: 100vh;
  background-color: #cac8c8;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 30px;
  background: linear-gradient(90deg, #636364);
}

.header-center h1 {
  margin: 0;
  font-size: 28px;
  font-weight: 700;
  color: #fff;
  text-shadow: 0 1px 2px rgba(0,0,0,0.2);
}

.header-right {
  display: flex;
  gap: 30px;
}

.nav-item {
  text-decoration: none;
  color: #fff;
  font-size: 15px;
  font-weight: 500;
  position: relative;
  transition: color 0.3s;
}

.nav-item:hover {
  color: #ffe4cc;
}

.nav-item::after {
  content: '';
  position: absolute;
  bottom: -5px;
  left: 0;
  width: 0;
  height: 2px;
  background: #fff;
  transition: width 0.3s ease;
}

.nav-item:hover::after {
  width: 100%;
}

.nav-item.active {
  color: #fff;
  font-weight: 600;
}

.nav-item.active::after {
  width: 100%;
}

.help-content {
  padding: 40px;
}

.help-content h2 {
  margin: 0 0 30px;
  font-size: 32px;
  font-weight: 600;
  color: #fff;
  text-shadow: 0 2px 10px rgba(0,0,0,0.2);
}

.help-nav {
  display: flex;
  gap: 20px;
  margin-bottom: 40px;
  flex-wrap: wrap;
}

.nav-btn {
  padding: 14px 28px;
  background: rgba(255,255,255,0.2);
  border-radius: 30px;
  color: #fff;
  font-size: 15px;
  cursor: pointer;
  transition: all 0.3s;
  backdrop-filter: blur(5px);
}

.nav-btn:hover, .nav-btn.active {
  background: rgba(255,255,255,0.3);
  transform: translateY(-2px);
}

.faq-box {
  background: rgba(255,255,255,0.98);
  border-radius: 20px;
  padding: 35px;
  max-width: 600px;
  box-shadow: 0 8px 30px rgba(185, 167, 167, 0.08);
}

.faq-box h3 {
  margin: 0 0 30px;
  font-size: 22px;
  font-weight: 600;
  color: #333;
  display: flex;
  align-items: center;
  gap: 12px;
}

.faq-items {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.faq-items p {
  margin: 0;
  font-size: 16px;
  color: #555;
  cursor: pointer;
  padding: 15px 20px;
  border-radius: 12px;
  background: linear-gradient(135deg, #ababac 0%, #e4e8ec 100%);
  transition: all 0.3s;
}

.faq-items p:hover {
  background: linear-gradient(135deg,#292828);
  color: #fff;
  transform: translateX(10px);
}

.refund-section {
  background: rgba(255,255,255,0.98);
  border-radius: 20px;
  padding: 35px;
  box-shadow: 0 8px 30px rgba(185, 167, 167, 0.08);
}

.section-title {
  margin: 0 0 25px;
  font-size: 22px;
  font-weight: 600;
  color: #333;
}

.order-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.order-card {
  border: 1px solid #e8e8e8;
  border-radius: 12px;
  padding: 20px;
  background: #fff;
  transition: all 0.2s;
}

.order-card:hover {
  border-color: #636364;
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.order-no {
  font-weight: 600;
  color: #333;
}

.order-status {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.order-status.completed {
  background: #d4edda;
  color: #155724;
}

.order-status.paid {
  background: #d1ecf1;
  color: #0c5460;
}

.order-info {
  display: flex;
  gap: 20px;
  margin-bottom: 12px;
  font-size: 14px;
  color: #666;
}

.order-amount {
  color: #e74c3c;
  font-weight: 600;
}

.refund-status {
  font-size: 13px;
  padding: 6px 12px;
  border-radius: 6px;
  margin-bottom: 8px;
  display: inline-block;
}

.refund-status.refund_pending {
  background: #fff3cd;
  color: #856404;
}

.refund-status.refund_approved {
  background: #d4edda;
  color: #155724;
}

.refund-status.refund_rejected {
  background: #f8d7da;
  color: #721c24;
}

.refund-reason {
  font-size: 13px;
  color: #888;
  margin-bottom: 12px;
}

.order-actions {
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.btn-refund {
  padding: 8px 20px;
  background: linear-gradient(135deg, #e74c3c 0%, #c0392b 100%);
  color: #fff;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-refund:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(231, 76, 60, 0.3);
}

.status-pending {
  color: #856404;
  font-weight: 500;
}

.status-approved {
  color: #155724;
  font-weight: 500;
}

.empty-orders {
  text-align: center;
  padding: 50px 20px;
  color: #999;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 12px;
}

.refund-order-info {
  background: #f8f9fa;
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.refund-order-info p {
  margin: 5px 0;
  font-size: 14px;
  color: #555;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: #fff;
  border-radius: 16px;
  width: 90%;
  max-width: 500px;
  max-height: 90vh;
  overflow: hidden;
  animation: slideUp 0.3s ease;
}

.modal-small {
  max-width: 420px;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
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
  font-size: 20px;
  color: #333;
}

.close-btn {
  font-size: 28px;
  color: #999;
  cursor: pointer;
  line-height: 1;
  transition: color 0.2s;
}

.close-btn:hover {
  color: #333;
}

.modal-body {
  padding: 24px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.form-group input,
.form-group textarea {
  width: 100%;
  padding: 12px 16px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 14px;
  box-sizing: border-box;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.form-group input:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #636364;
  box-shadow: 0 0 0 3px rgba(99, 99, 100, 0.1);
}

.form-group textarea {
  resize: vertical;
}

.modal-footer {
  display: flex;
  gap: 12px;
  padding: 16px 24px;
  border-top: 1px solid #eee;
  background: #fafafa;
}

.btn-cancel,
.btn-submit {
  flex: 1;
  padding: 12px 24px;
  border-radius: 8px;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-cancel {
  background: #fff;
  border: 1px solid #ddd;
  color: #666;
}

.btn-cancel:hover {
  background: #f5f5f5;
}

.btn-submit {
  background: linear-gradient(135deg, #636364 0%, #4a4a4a 100%);
  border: none;
  color: #fff;
}

.btn-submit:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(99, 99, 100, 0.3);
}

.btn-submit:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.toast-success {
  position: fixed;
  top: 80px;
  left: 50%;
  transform: translateX(-50%);
  background: linear-gradient(135deg, #52c41a 0%, #389e0d 100%);
  color: #fff;
  padding: 12px 24px;
  border-radius: 8px;
  font-size: 14px;
  z-index: 2000;
  animation: fadeInDown 0.3s ease;
  box-shadow: 0 4px 12px rgba(82, 196, 26, 0.3);
}

@keyframes fadeInDown {
  from {
    opacity: 0;
    transform: translate(-50%, -10px);
  }
  to {
    opacity: 1;
    transform: translate(-50%, 0);
  }
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
  
  /* 帮助中心内容 */
  .help-content {
    padding: 10px 15px;
    padding-bottom: 80px;
  }
  
  .help-content h2 {
    font-size: 24px;
  }
  
  .help-nav {
    gap: 10px;
    margin-bottom: 20px;
  }
  
  .nav-btn {
    padding: 10px 20px;
    font-size: 14px;
  }
  
  .faq-box {
    padding: 20px;
    border-radius: 15px;
  }
  
  .faq-box h3 {
    font-size: 18px;
  }
  
  .faq-items p {
    padding: 12px 15px;
    font-size: 14px;
  }
  
  .refund-section {
    padding: 20px;
    border-radius: 15px;
  }
  
  .section-title {
    font-size: 18px;
  }
  
  .order-card {
    padding: 15px;
  }
  
  .order-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .order-info {
    flex-direction: column;
    gap: 5px;
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
