<template>
  <div class="order-page">
    <header class="admin-header">
      <div class="header-left">
        <div class="logo">
          <span class="logo-text">控制台</span>
        </div>
      </div>
      <nav class="main-nav">
        <a href="#" class="nav-item active">订单管理</a>
        <a href="/admin/product" class="nav-item">商品管理</a>
        <a href="/admin/user" class="nav-item">用户管理</a>
        <a href="/admin/feedback" class="nav-item">反馈管理</a>
      </nav>
      <div class="header-right">
        <button class="back-btn" @click="goBack">返回控制台</button>
      </div>
    </header>

    <div class="page-content">
      <div class="filter-bar">
        <select class="filter-select" v-model="statusFilter" @change="handleFilter">
          <option value="">全部状态</option>
          <option value="pending">待处理</option>
          <option value="paid">已付款</option>
          <option value="shipped">已发货</option>
          <option value="completed">已完成</option>
          <option value="cancelled">已取消</option>
        </select>
        <select class="filter-select" v-model="refundFilter" @change="handleFilter">
          <option value="">全部退款状态</option>
          <option value="refund_pending">待审核退款</option>
          <option value="refund_approved">已通过退款</option>
          <option value="refund_rejected">已拒绝退款</option>
        </select>
        <input type="text" class="search-input" placeholder="搜索订单号/收货人/电话" v-model="searchKeyword" @input="handleSearch" />
      </div>
      <div class="order-table-container">
        <table class="order-table">
          <thead>
            <tr>
              <th>订单号</th>
              <th>商品金额</th>
              <th>收货人</th>
              <th>收货电话</th>
              <th>支付方式</th>
              <th>订单状态</th>
              <th>退款状态</th>
              <th>创建时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="order in filteredOrders" :key="order.id">
              <td class="order-no">{{ order.orderNo }}</td>
              <td class="amount">¥{{ order.totalAmount }}</td>
              <td>{{ order.shippingName || '-' }}</td>
              <td>{{ order.shippingPhone || '-' }}</td>
              <td>{{ getPaymentMethodText(order.paymentMethod) }}</td>
              <td>
                <span class="status-badge" :class="order.status">
                  {{ getStatusText(order.status) }}
                </span>
              </td>
              <td>
                <span v-if="order.refundStatus" class="refund-badge" :class="order.refundStatus">
                  {{ getRefundStatusText(order.refundStatus) }}
                </span>
                <span v-else class="no-refund">-</span>
              </td>
              <td class="time">{{ formatDate(order.createdAt) }}</td>
              <td>
                <div class="action-buttons">
                  <button class="btn-view" @click="viewOrderDetail(order.id)">详情</button>
                  <select 
                    class="status-select" 
                    :value="order.status" 
                    @change="updateOrderStatus(order.id, $event.target.value)"
                  >
                    <option value="pending">待处理</option>
                    <option value="paid">已付款</option>
                    <option value="shipped">已发货</option>
                    <option value="completed">已完成</option>
                    <option value="cancelled">已取消</option>
                  </select>
                  <button 
                    v-if="order.refundStatus === 'refund_pending'" 
                    class="btn-refund" 
                    @click="processRefund(order.id, true)"
                  >
                    通过退款
                  </button>
                  <button 
                    v-if="order.refundStatus === 'refund_pending'" 
                    class="btn-reject" 
                    @click="processRefund(order.id, false)"
                  >
                    拒绝退款
                  </button>
                  <button class="btn-delete" @click="deleteOrder(order.id)">删除</button>
                </div>
              </td>
            </tr>
            <tr v-if="filteredOrders.length === 0">
              <td colspan="9" class="empty-cell">暂无订单数据</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div class="modal-overlay" v-if="showDetailModal" @click.self="closeDetailModal">
      <div class="modal-content">
        <div class="modal-header">
          <h3>订单详情</h3>
          <button class="close-btn" @click="closeDetailModal">×</button>
        </div>
        <div class="modal-body" v-if="orderDetail">
          <div class="order-info-section">
            <h4>基本信息</h4>
            <div class="info-row">
              <span class="label">订单号：</span>
              <span class="value">{{ orderDetail.order.orderNo }}</span>
            </div>
            <div class="info-row">
              <span class="label">下单</span>
              <span class="value">{{ orderDetail.username }}</span>
            </div>
            <div class="info-row">
              <span class="label">订单金额：</span>
              <span class="value amount">¥{{ orderDetail.order.totalAmount }}</span>
            </div>
            <div class="info-row">
              <span class="label">订单状态：</span>
              <span class="status-badge" :class="orderDetail.order.status">
                {{ getStatusText(orderDetail.order.status) }}
              </span>
            </div>
            <div class="info-row">
              <span class="label">支付方式：</span>
              <span class="value">{{ getPaymentMethodText(orderDetail.order.paymentMethod) }}</span>
            </div>
            <div class="info-row">
              <span class="label">下单时间：</span>
              <span class="value">{{ formatDate(orderDetail.order.createdAt) }}</span>
            </div>
            <div v-if="orderDetail.order.refundStatus" class="info-row">
              <span class="label">退款状态：</span>
              <span class="status-badge" :class="orderDetail.order.refundStatus">
                {{ getRefundStatusText(orderDetail.order.refundStatus) }}
              </span>
            </div>
            <div v-if="orderDetail.order.refundReason" class="info-row">
              <span class="label">退款原因：</span>
              <span class="value">{{ orderDetail.order.refundReason }}</span>
            </div>
            <div v-if="orderDetail.order.refundNote" class="info-row">
              <span class="label">处理备注：</span>
              <span class="value">{{ orderDetail.order.refundNote }}</span>
            </div>
          </div>
          <div class="shipping-info-section" v-if="orderDetail.order.shippingName">
            <h4>收货信息</h4>
            <div class="info-row">
              <span class="label">收货人：</span>
              <span class="value">{{ orderDetail.order.shippingName }}</span>
            </div>
            <div class="info-row">
              <span class="label">联系电话：</span>
              <span class="value">{{ orderDetail.order.shippingPhone }}</span>
            </div>
            <div class="info-row">
              <span class="label">收货地址：</span>
              <span class="value">{{ orderDetail.order.shippingAddress }}</span>
            </div>
          </div>
          <div class="order-items-section">
            <h4>商品列表</h4>
            <table class="items-table">
              <thead>
                <tr>
                  <th>商品名称</th>
                  <th>单价</th>
                  <th>数量</th>
                  <th>小计</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="item in orderDetail.items" :key="item.id">
                  <td>{{ item.goodsName }}</td>
                  <td>¥{{ item.price }}</td>
                  <td>{{ item.quantity }}</td>
                  <td>¥{{ (item.price * item.quantity).toFixed(2) }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>

    <!-- 移动端底部导航 -->
    <nav class="mobile-nav">
      <a href="/admin/order" class="mobile-nav-item active">
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
      </a>
    </nav>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from "vue";
import { useRouter } from "vue-router";

const router = useRouter();
const orders = ref([]);
const searchKeyword = ref("");
const statusFilter = ref("");
const refundFilter = ref("");
const showDetailModal = ref(false);
const orderDetail = ref<any>(null);

const filteredOrders = computed(() => {
  let result = orders.value;
  if (statusFilter.value) {
    result = result.filter((order: any) => order.status === statusFilter.value);
  }
  if (refundFilter.value) {
    result = result.filter((order: any) => order.refundStatus === refundFilter.value);
  }
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase();
    result = result.filter((order: any) => {
      return order.orderNo?.toLowerCase().includes(keyword) ||
             order.shippingName?.toLowerCase().includes(keyword) ||
             order.shippingPhone?.includes(keyword);
    });
  }
  return result;
});

onMounted(() => {
  const isAdmin = localStorage.getItem("isAdmin") === "true";
  const token = localStorage.getItem("token");
  
  if (!token || !isAdmin) {
    router.push("/admin/login");
  }
  
  loadOrders();
});

async function loadOrders() {
  const token = localStorage.getItem("token");
  try {
    const response = await fetch("http://localhost:8081/api/admin/orders", {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        "x-auth-token": token || ""
      }
    });
    const data = await response.json();
    if (data.code === 200) {
      orders.value = data.data || [];
    }
  } catch (error) {
    console.error("加载订单失败:", error);
  }
}

async function updateOrderStatus(orderId: number, newStatus: string) {
  const token = localStorage.getItem("token");
  try {
    const response = await fetch(`http://localhost:8081/api/admin/orders/${orderId}/status?status=${newStatus}`, {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
        "x-auth-token": token || ""
      }
    });
    const data = await response.json();
    if (data.code === 200) {
      alert("订单状态更新成功");
      loadOrders();
    } else {
      alert("更新失败: " + (data.message || "未知错误"));
    }
  } catch (error) {
    console.error("更新订单状态失败:", error);
    alert("更新失败");
  }
}

function getStatusText(status: string) {
  const statusMap: Record<string, string> = {
    'pending': '待处理',
    'paid': '已付款',
    'shipped': '已发货',
    'completed': '已完成',
    'cancelled': '已取消'
  };
  return statusMap[status] || status;
}

function getRefundStatusText(status: string) {
  const map: Record<string, string> = {
    'refund_pending': '待审核',
    'refund_approved': '已通过',
    'refund_rejected': '已拒绝'
  };
  return map[status] || status;
}

async function processRefund(orderId: number, approved: boolean) {
  const action = approved ? '通过' : '拒绝';
  if (!confirm(`确定要${action}该退款申请吗？`)) {
    return;
  }
  const token = localStorage.getItem("token");
  try {
    const response = await fetch(`http://localhost:8081/api/admin/orders/${orderId}/refund`, {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${token || ""}`
      },
      body: JSON.stringify({
        approved: approved,
        note: approved ? '同意退款' : '拒绝退款'
      })
    });
    const data = await response.json();
    if (data.code === 200) {
      alert(`退款已${action}！`);
      loadOrders();
    } else {
      alert("操作失败: " + (data.message || "未知错误"));
    }
  } catch (error) {
    console.error("处理退款失败:", error);
    alert("操作失败");
  }
}

function getPaymentMethodText(method: string) {
  if (!method) return '-';
  if (method.toLowerCase().includes('wechat') || method.toLowerCase().includes('wx') || method.includes('微信')) {
    return '微信支付';
  }
  if (method.toLowerCase().includes('alipay') || method.includes('支付宝')) {
    return '支付宝';
  }
  return method;
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

function handleSearch() {
}

function handleFilter() {
}

async function viewOrderDetail(orderId: number) {
  const token = localStorage.getItem("token");
  try {
    const response = await fetch(`http://localhost:8081/api/admin/orders/${orderId}/detail`, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        "x-auth-token": token || ""
      }
    });
    const data = await response.json();
    if (data.code === 200) {
      orderDetail.value = data.data;
      showDetailModal.value = true;
    }
  } catch (error) {
    console.error("加载订单详情失败:", error);
  }
}

function closeDetailModal() {
  showDetailModal.value = false;
  orderDetail.value = null;
}

async function deleteOrder(orderId: number) {
  if (!confirm("确定要删除该订单吗？此操作不可恢复！")) {
    return;
  }
  const token = localStorage.getItem("token");
  try {
    const response = await fetch(`http://localhost:8081/api/admin/orders/${orderId}`, {
      method: "DELETE",
      headers: {
        "Content-Type": "application/json",
        "x-auth-token": token || ""
      }
    });
    const data = await response.json();
    if (data.code === 200) {
      alert("订单删除成功");
      loadOrders();
    } else {
      alert("删除失败: " + (data.message || "未知错误"));
    }
  } catch (error) {
    console.error("删除订单失败:", error);
    alert("删除失败");
  }
}

function goBack() {
  router.push("/admin");
}
</script>

<style scoped>
.order-page {
  min-height: 100vh;
  background: #f5f7fa;
}

.admin-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 30px;
  padding-right: 45px;
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
  border-bottom: 1px solid #eaeaea;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}

.page-header h2 {
  margin: 0;
  font-size: 28px;
  color: #333;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.filter-bar {
  display: flex;
  align-items: center;
}

.filter-select {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  outline: none;
  background: #fff;
}

.filter-select:focus {
  border-color: #667eea;
}

.search-input {
  padding: 10px 15px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  width: 250px;
  margin-right: 15px;
  outline: none;
}

.search-input:focus {
  border-color: #667eea;
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

.order-table-container {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);
  overflow-x: auto;
}

.order-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
}

.order-table th {
  background: #f8f9fa;
  padding: 14px 12px;
  text-align: left;
  font-weight: 600;
  color: #333;
  border-bottom: 2px solid #e9ecef;
  white-space: nowrap;
}

.order-table td {
  padding: 14px 12px;
  border-bottom: 1px solid #f0f0f0;
  color: #555;
  max-width: 200px;
}

.order-table tbody tr:hover {
  background: #f8f9fa;
}

.order-no {
  color: #667eea;
  font-weight: 500;
}

.amount {
  color: #e74c3c;
  font-weight: 600;
}

.address {
  max-width: 220px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.time {
  color: #888;
  font-size: 13px;
  white-space: nowrap;
}

.status-badge {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.status-badge.pending {
  background: #fff3cd;
  color: #856404;
}

.status-badge.paid {
  background: #d1ecf1;
  color: #0c5460;
}

.status-badge.shipped {
  background: #cce5ff;
  color: #004085;
}

.status-badge.completed {
  background: #d4edda;
  color: #155724;
}

.status-badge.cancelled {
  background: #f8d7da;
  color: #721c24;
}

.action-buttons {
  display: flex;
  gap: 8px;
}

.status-select {
  padding: 6px 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 13px;
  cursor: pointer;
  outline: none;
}

.status-select:focus {
  border-color: #667eea;
}

.empty-cell {
  text-align: center;
  padding: 60px 0 !important;
  color: #999;
  font-size: 16px;
}

.btn-view, .btn-delete {
  padding: 6px 12px;
  border: none;
  border-radius: 4px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-view {
  background: #667eea;
  color: #fff;
}

.btn-view:hover {
  background: #556ee6;
}

.btn-delete {
  background: #f8d7da;
  color: #721c24;
}

.btn-delete:hover {
  background: #f5c6cb;
}

.btn-refund, .btn-reject {
  padding: 6px 12px;
  border: none;
  border-radius: 4px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-refund {
  background: #28a745;
  color: #fff;
}

.btn-refund:hover {
  background: #218838;
}

.btn-reject {
  background: #dc3545;
  color: #fff;
}

.btn-reject:hover {
  background: #c82333;
}

.refund-badge, .no-refund {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.no-refund {
  color: #999;
}

.refund-badge.refund_pending {
  background: #fff3cd;
  color: #856404;
}

.refund-badge.refund_approved,
.status-badge.refund_approved {
  background: #d4edda;
  color: #155724;
}

.refund-badge.refund_rejected,
.status-badge.refund_rejected {
  background: #f8d7da;
  color: #721c24;
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
  width: 90%;
  max-width: 700px;
  max-height: 85vh;
  overflow-y: auto;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.15);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #eee;
  position: sticky;
  top: 0;
  background: #fff;
}

.modal-header h3 {
  margin: 0;
  font-size: 20px;
  color: #333;
}

.close-btn {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  border: none;
  background: #f5f5f5;
  font-size: 20px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #666;
}

.close-btn:hover {
  background: #eee;
}

.modal-body {
  padding: 24px;
}

.order-info-section, .shipping-info-section, .order-items-section {
  margin-bottom: 24px;
}

.order-info-section h4, 
.shipping-info-section h4, 
.order-items-section h4 {
  margin: 0 0 16px 0;
  font-size: 16px;
  color: #333;
  padding-bottom: 8px;
  border-bottom: 2px solid #667eea;
  display: inline-block;
}

.info-row {
  display: flex;
  padding: 8px 0;
  border-bottom: 1px solid #f5f5f5;
}

.info-row .label {
  width: 100px;
  color: #888;
  flex-shrink: 0;
}

.info-row .value {
  color: #333;
  flex: 1;
}

.items-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
}

.items-table th {
  background: #f8f9fa;
  padding: 12px;
  text-align: left;
  font-weight: 600;
  border-bottom: 2px solid #e9ecef;
}

.items-table td {
  padding: 12px;
  border-bottom: 1px solid #f0f0f0;
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

  .filter-bar {
    width: 100%;
    gap: 10px;
  }

  .filter-select {
    flex: 1;
    padding: 10px 12px;
  }

  .search-input {
    flex: 1;
    margin-right: 0;
    padding: 10px 12px;
  }

  /* 页面内容 */
  .page-content {
    padding: 15px;
    padding-bottom: 80px;
  }

  .order-table-container {
    padding: 15px;
    overflow-x: auto;
  }

  .order-table {
    font-size: 12px;
  }

  .order-table th,
  .order-table td {
    padding: 10px 8px;
    white-space: nowrap;
  }

  .action-buttons {
    flex-wrap: wrap;
    gap: 6px;
  }

  .btn-view,
  .btn-delete,
  .btn-refund,
  .btn-reject {
    padding: 5px 10px;
    font-size: 12px;
  }

  .status-select {
    padding: 5px 8px;
    font-size: 12px;
  }

  /* 订单详情弹窗 */
  .modal-content {
    width: 95%;
    max-height: 90vh;
  }

  .modal-body {
    padding: 15px;
  }

  .info-row {
    flex-direction: column;
    gap: 4px;
  }

  .info-row .label {
    width: auto;
    color: #666;
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
