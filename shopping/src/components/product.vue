<template>
  <div class="profile-page">
    <header class="header">
      <div class="header-center">
        <h1>购物平台</h1>
      </div>
      <nav class="header-right">
        <a href="#" @click.prevent="goHome" class="nav-item">首页</a>
        <a href="#" class="nav-item active">我的</a>
        <a href="#" @click.prevent="goCart" class="nav-item">购物车</a>
        <a href="#" @click.prevent="goFav" class="nav-item">收藏夹</a>
        <a href="#" @click.prevent="goHelp" class="nav-item">帮助中心</a>
      </nav>
    </header>

    <div class="profile-content">
      <div class="user-card">
        <div class="user-info">
          <div class="avatar">头像</div>
          <div class="user-detail">
            <span class="nickname">{{ currentUser }}</span>
          </div>
        </div>
        <div class="user-actions">
          <span @click="showAddressListModal = true" class="action-btn">地址</span>
          <span @click="showSettingsModal = true" class="action-btn">设置</span>
          <span @click="openOrderModal()" class="action-btn">订单</span>
        </div>
      </div>

      
    </div>

    <div v-if="showEditProfileModal" class="modal-overlay" @click.self="closeEditProfileModal">
      <div class="edit-profile-modal">
        <div class="modal-header profile-header">
          <h3>编辑个人资料</h3>
          <span class="close-btn" @click="closeEditProfileModal">×</span>
        </div>
        <div class="modal-body profile-body">
          <div class="form-group">
            <label>用户名</label>
            <input v-model="profileForm.username" type="text" placeholder="请输入用户名" class="form-input profile-input" />
          </div>
          
          <div class="form-group">
            <label>邮箱</label>
            <input v-model="profileForm.email" type="email" placeholder="请输入邮箱" class="form-input profile-input" />
          </div>
          
          <div class="form-group">
            <label>联系电话</label>
            <input v-model="profileForm.phone" type="tel" placeholder="请输入联系电话" class="form-input profile-input" />
          </div>
          
          <div class="form-group">
            <label>个人简介</label>
            <textarea v-model="profileForm.bio" placeholder="简单介绍一下自己..." class="form-textarea profile-textarea"></textarea>
          </div>
          
          <div class="btn-group profile-btn-group">
            <button @click="saveProfile" class="save-btn profile-save-btn">保存修改</button>
            <button @click="closeEditProfileModal" class="cancel-btn profile-cancel-btn">取消</button>
          </div>
        </div>
      </div>
    </div>

    <div v-if="showSettingsModal" class="modal-overlay" @click.self="showSettingsModal = false">
      <div class="settings-modal">
        <div class="modal-header">
          <h3>账户设置</h3>
          <span class="close-btn" @click="showSettingsModal = false">×</span>
        </div>
        <div class="modal-body">
          <div class="settings-list">
            <span @click="openEditProfile" class="settings-item">
              <span>编辑个人资料</span>
            </span>
            <span @click="goAddress" class="settings-item">
              <span>收货地址管理</span>
            </span>
            
            <span @click="confirmDeleteAccount" class="settings-item danger">
              <span>注销此账号</span>
            </span>
          </div>
        </div>
      </div>
    </div>

    <div v-if="showAddressListModal" class="modal-overlay" @click.self="showAddressListModal = false">
      <div class="address-list-modal">
        <div class="modal-header">
          <h3>我的收货地址</h3>
          <span class="close-btn" @click="showAddressListModal = false">×</span>
        </div>
        <div class="modal-body">
          <div v-if="addresses.length === 0" class="empty-address">
            <span>暂无收货地址</span>
            <span class="empty-hint">可在设置中添加收货地址</span>
          </div>
          
          <div v-for="(addr, index) in addresses" :key="index" class="address-card">
            <div class="address-tag">
              <span>收货地址 {{ index + 1 }}</span>
            </div>
            <div class="address-main">
              <div class="address-user">
                <span class="user-name">{{ addr.name }}</span>
                <span class="user-phone">{{ addr.phone }}</span>
              </div>
              <div class="address-full">{{ addr.province }} {{ addr.city }} {{ addr.district }} {{ addr.detail }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-if="showAddressModal" class="modal-overlay" @click.self="showAddressModal = false">
      <div class="address-modal">
        <div class="modal-header">
          <h3>收货地址管理</h3>
          <span class="close-btn" @click="showAddressModal = false">×</span>
        </div>
        <div class="modal-body address-body">
          <button @click="showAddAddress = true" class="add-address-btn">
            <span class="add-icon">+</span>
            <span>添加新地址</span>
          </button>
          
          <div v-if="addresses.length === 0" class="empty-address">
            <span>暂无收货地址</span>
          </div>
          
          <div v-for="(addr, index) in addresses" :key="index" class="address-item">
            <div class="address-info">
              <div class="address-name">{{ addr.name }}</div>
              <div class="address-phone">{{ addr.phone }}</div>
              <div class="address-detail">{{ addr.province }} {{ addr.city }} {{ addr.district }} {{ addr.detail }}</div>
            </div>
            <div class="address-actions">
              <span @click="editAddress(index)" class="edit-btn">编辑</span>
              <span @click="deleteAddress(index)" class="delete-btn">删除</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-if="showAddAddress" class="modal-overlay" @click.self="closeAddAddress">
      <div class="add-address-modal">
        <div class="modal-header">
          <h3>{{ editingIndex >= 0 ? '编辑地址' : '添加新地址' }}</h3>
          <span class="close-btn" @click="closeAddAddress">×</span>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>收货人</label>
            <input v-model="addressForm.name" type="text" placeholder="请输入收货人姓名" class="form-input" />
          </div>
          <div class="form-group">
            <label>联系电话</label>
            <input v-model="addressForm.phone" type="tel" placeholder="请输入联系电话" class="form-input" />
          </div>
          <div class="form-group">
            <label>所在地区</label>
            <div class="region-select">
              <select v-model="addressForm.province" @change="onProvinceChange" class="region-select-item">
                <option value="">请选择省份</option>
                <option v-for="p in provinces" :key="p" :value="p">{{ p }}</option>
              </select>
              <select v-model="addressForm.city" @change="onCityChange" class="region-select-item">
                <option value="">请选择城市</option>
                <option v-for="c in cities" :key="c" :value="c">{{ c }}</option>
              </select>
            </div>
          </div>
          <div class="form-group">
            <label>详细地址</label>
            <textarea v-model="addressForm.detail" placeholder="请输入详细地址" class="form-textarea"></textarea>
          </div>
          <div class="btn-group">
            <button @click="saveAddress" class="save-btn">确认保存</button>
            <button @click="closeAddAddress" class="cancel-btn">取消</button>
          </div>
        </div>
      </div>
    </div>

    <div v-if="showOrderModal" class="modal-overlay" @click.self="showOrderModal = false">
      <div class="order-modal">
        <div class="modal-header">
          <h3>我的订单</h3>
          <span class="close-btn" @click="showOrderModal = false">×</span>
        </div>
        <div class="modal-body order-body">
          <div v-if="orders.length === 0" class="empty-orders">
            <span>暂无订单记录</span>
          </div>
          <div v-for="orderDetail in orders" :key="orderDetail.order.id" class="order-card">
            <div class="order-header">
              <span class="order-no">订单号：{{ orderDetail.order.orderNo }}</span>
              <span class="order-status" :class="orderDetail.order.status">{{ getOrderStatusText(orderDetail.order.status) }}</span>
            </div>
            <div class="order-items">
              <div 
                v-for="item in orderDetail.items" 
                :key="item.id" 
                class="order-item"
              >
                <img v-if="item.image" :src="item.image" class="item-img" />
                <div class="item-info">
                  <span class="item-name">{{ item.goodsName }}</span>
                  <span class="item-price">¥{{ item.price }}</span>
                  <span class="item-quantity">×{{ item.quantity }}</span>
                </div>
              </div>
            </div>
            <div class="order-footer">
              <span class="order-total">订单总额：¥{{ orderDetail.order.totalAmount }}</span>
              <div class="order-actions">
                <button 
                  class="order-detail-btn" 
                  @click="showOrderDetail(orderDetail)"
                >
                  订单详情
                </button>
                <button 
                  v-if="orderDetail.order.status === 'shipped'" 
                  class="confirm-receive-btn" 
                  @click="confirmReceive(orderDetail.order.id)"
                >
                  确认收货
                </button>
                <button 
                  v-if="(orderDetail.order.status === 'paid' || orderDetail.order.status === 'shipped') && !orderDetail.order.refundStatus" 
                  class="refund-apply-btn" 
                  @click="showRefundModal(orderDetail.order.id)"
                >
                  申请退款
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-if="showGoodsDetailModal" class="modal-overlay" @click.self="showGoodsDetailModal = false">
      <div class="goods-detail-modal">
        <div class="modal-header">
          <h3>商品详情</h3>
          <span class="close-btn" @click="showGoodsDetailModal = false">×</span>
        </div>
        <div class="modal-body goods-detail-body" v-if="currentGoodsDetail">
          <div class="detail-image">
            <img :src="currentGoodsDetail.image" />
          </div>
          <div class="detail-info">
            <h4 class="detail-name">{{ currentGoodsDetail.goodsName }}</h4>
            <p class="detail-price">¥{{ currentGoodsDetail.price }}</p>
            <p class="detail-description">{{ currentGoodsDetail.description || '暂无商品描述' }}</p>
            <p class="detail-quantity">购买数量：×{{ currentGoodsDetail.quantity }}</p>
            <p class="detail-category">分类：{{ currentGoodsDetail.categoryName || '未分类' }}</p>
          </div>
        </div>
      </div>
    </div>

    <div v-if="showRefundApplyModal" class="modal-overlay" @click.self="showRefundApplyModal = false">
      <div class="refund-modal">
        <div class="modal-header">
          <h3>申请退款</h3>
          <span class="close-btn" @click="showRefundApplyModal = false">×</span>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>退款原因</label>
            <textarea 
              v-model="refundReason" 
              placeholder="请输入退款原因..."
              class="form-textarea"
            ></textarea>
          </div>
          <div class="btn-group">
            <button @click="submitRefund()" class="save-btn refund-submit-btn">提交申请</button>
            <button @click="showRefundApplyModal = false" class="cancel-btn">取消</button>
          </div>
        </div>
      </div>
    </div>

    <div v-if="showOrderDetailModal" class="modal-overlay" @click.self="showOrderDetailModal = false">
      <div class="order-detail-modal">
        <div class="modal-header">
          <h3>订单详情</h3>
          <span class="close-btn" @click="closeOrderDetailModal">×</span>
        </div>
        <div class="modal-body order-detail-body" v-if="currentOrderDetail">
          <div class="order-info">
            <div class="info-row">
              <span class="info-label">订单号：</span>
              <span class="info-value">{{ currentOrderDetail.order.orderNo }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">订单状态：</span>
              <span class="info-value status-badge" :class="currentOrderDetail.order.status">{{ getOrderStatusText(currentOrderDetail.order.status) }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">下单时间：</span>
              <span class="info-value">{{ currentOrderDetail.order.createdAt || '暂无' }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">订单总额：</span>
              <span class="info-value total">¥{{ currentOrderDetail.order.totalAmount }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">收货人：</span>
              <span class="info-value">{{ currentOrderDetail.order.shippingName || '暂无' }} {{ currentOrderDetail.order.shippingPhone || '' }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">收货地址：</span>
              <span class="info-value">{{ currentOrderDetail.order.shippingAddress || '暂无' }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 移动端底部导航 -->
    <nav class="mobile-nav">
      <a href="#" class="mobile-nav-item" @click.prevent="goHome">
        <span>首页</span>
      </a>
      <a href="#" class="mobile-nav-item active" @click.prevent="goProfile">
        <span>我的</span>
      </a>
      <a href="#" class="mobile-nav-item" @click.prevent="goCart">
        <span>购物车</span>
      </a>
      <a href="#" class="mobile-nav-item" @click.prevent="goFav">
        <span>收藏夹</span>
      </a>
      <a href="#" class="mobile-nav-item" @click.prevent="goHelp">
        <span>帮助</span>
      </a>
    </nav>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";

const router = useRouter();
const currentUser = ref("");
const showSettingsModal = ref(false);
const showEditProfileModal = ref(false);
const showAddressModal = ref(false);
const showAddressListModal = ref(false);
const showAddAddress = ref(false);
const editingIndex = ref(-1);
const showOrderModal = ref(false);
const orders = ref<any[]>([]);
const showRefundApplyModal = ref(false);
const currentRefundOrderId = ref<number | null>(null);
const showGoodsDetailModal = ref(false);
const currentGoodsDetail = ref<any>(null);
const refundReason = ref("");
const showOrderDetailModal = ref(false);
const currentOrderDetail = ref<any>(null);

interface Profile {
  username: string;
  email: string;
  phone: string;
  bio: string;
}

const profileForm = ref<Profile>({
  username: "",
  email: "",
  phone: "",
  bio: ""
});

interface Address {
  name: string;
  phone: string;
  province: string;
  city: string;
  district: string;
  detail: string;
}

const addresses = ref<Address[]>([]);

const addressForm = ref<Address>({
  name: "",
  phone: "",
  province: "",
  city: "",
  district: "",
  detail: ""
});

const provinces = [
  "北京市", "天津市", "河北省", "山西省", "内蒙古自治区",
  "辽宁省", "吉林省", "黑龙江省", "上海市", "江苏省",
  "浙江省", "安徽省", "福建省", "江西省", "山东省",
  "河南省", "湖北省", "湖南省", "广东省", "广西壮族自治区",
  "海南省", "重庆市", "四川省", "贵州省", "云南省",
  "西藏自治区", "陕西省", "甘肃省", "青海省", "宁夏回族自治区",
  "新疆维吾尔自治区", "香港特别行政区", "澳门特别行政区", "台湾省"
];

const cityMap: Record<string, string[]> = {
  "北京市": ["东城区", "西城区", "朝阳区", "海淀区", "丰台区", "石景山区", "通州区", "顺义区", "昌平区", "大兴区", "房山区", "门头沟区", "怀柔区", "平谷区", "密云区", "延庆区"],
  "天津市": ["和平区", "河东区", "河西区", "南开区", "河北区", "红桥区", "东丽区", "西青区", "津南区", "北辰区", "武清区", "宝坻区", "滨海新区", "宁河区", "静海区", "蓟州区"],
  "河北省": ["石家庄市", "唐山市", "秦皇岛市", "邯郸市", "邢台市", "保定市", "张家口市", "承德市", "沧州市", "廊坊市", "衡水市"],
  "山西省": ["太原市", "大同市", "阳泉市", "长治市", "晋城市", "朔州市", "晋中市", "运城市", "忻州市", "临汾市", "吕梁市"],
  "内蒙古自治区": ["呼和浩特市", "包头市", "乌海市", "赤峰市", "通辽市", "鄂尔多斯市", "呼伦贝尔市", "巴彦淖尔市", "乌兰察布市", "兴安盟", "锡林郭勒盟", "阿拉善盟"],
  "辽宁省": ["沈阳市", "大连市", "鞍山市", "抚顺市", "本溪市", "丹东市", "锦州市", "营口市", "阜新市", "辽阳市", "盘锦市", "铁岭市", "朝阳市", "葫芦岛市"],
  "吉林省": ["长春市", "吉林市", "四平市", "辽源市", "通化市", "白山市", "松原市", "白城市", "延边朝鲜族自治州"],
  "黑龙江省": ["哈尔滨市", "齐齐哈尔市", "鸡西市", "鹤岗市", "双鸭山市", "大庆市", "伊春市", "佳木斯市", "七台河市", "牡丹江市", "黑河市", "绥化市", "大兴安岭地区"],
  "上海市": ["黄浦区", "徐汇区", "长宁区", "静安区", "普陀区", "虹口区", "杨浦区", "闵行区", "宝山区", "嘉定区", "浦东新区", "金山区", "松江区", "青浦区", "奉贤区", "崇明区"],
  "江苏省": ["南京市", "无锡市", "徐州市", "常州市", "苏州市", "南通市", "连云港市", "淮安市", "盐城市", "扬州市", "镇江市", "泰州市", "宿迁市"],
  "浙江省": ["杭州市", "宁波市", "温州市", "嘉兴市", "湖州市", "绍兴市", "金华市", "衢州市", "舟山市", "台州市", "丽水市"],
  "安徽省": ["合肥市", "芜湖市", "蚌埠市", "淮南市", "马鞍山市", "淮北市", "铜陵市", "安庆市", "黄山市", "滁州市", "阜阳市", "宿州市", "六安市", "亳州市", "池州市", "宣城市"],
  "福建省": ["福州市", "厦门市", "莆田市", "三明市", "泉州市", "漳州市", "南平市", "龙岩市", "宁德市"],
  "江西省": ["南昌市", "景德镇市", "萍乡市", "九江市", "新余市", "鹰潭市", "赣州市", "吉安市", "宜春市", "抚州市", "上饶市"],
  "山东省": ["济南市", "青岛市", "淄博市", "枣庄市", "东营市", "烟台市", "潍坊市", "济宁市", "泰安市", "威海市", "日照市", "临沂市", "德州市", "聊城市", "滨州市", "菏泽市"],
  "河南省": ["郑州市", "开封市", "洛阳市", "平顶山市", "安阳市", "鹤壁市", "新乡市", "焦作市", "濮阳市", "许昌市", "漯河市", "三门峡市", "南阳市", "商丘市", "信阳市", "周口市", "驻马店市", "济源示范区"],
  "湖北省": ["武汉市", "黄石市", "十堰市", "宜昌市", "襄阳市", "鄂州市", "荆门市", "孝感市", "荆州市", "黄冈市", "咸宁市", "随州市", "恩施土家族苗族自治州", "仙桃市", "潜江市", "天门市"],
  "湖南省": ["长沙市", "株洲市", "湘潭市", "衡阳市", "邵阳市", "岳阳市", "常德市", "张家界市", "益阳市", "郴州市", "永州市", "怀化市", "娄底市", "湘西土家族苗族自治州"],
  "广东省": ["广州市", "韶关市", "深圳市", "珠海市", "汕头市", "佛山市", "江门市", "湛江市", "茂名市", "肇庆市", "惠州市", "梅州市", "汕尾市", "河源市", "阳江市", "清远市", "东莞市", "中山市", "潮州市", "揭阳市", "云浮市"],
  "广西壮族自治区": ["南宁市", "柳州市", "桂林市", "梧州市", "北海市", "防城港市", "钦州市", "贵港市", "玉林市", "百色市", "贺州市", "河池市", "来宾市", "崇左市"],
  "海南省": ["海口市", "三亚市", "三沙市", "儋州市", "五指山市", "琼海市", "文昌市", "万宁市", "东方市"],
  "重庆市": ["万州区", "涪陵区", "渝中区", "大渡口区", "江北区", "沙坪坝区", "九龙坡区", "南岸区", "北碚区", "綦江区", "大足区", "渝北区", "巴南区", "黔江区", "长寿区", "江津区", "合川区", "永川区", "南川区"],
  "四川省": ["成都市", "自贡市", "攀枝花市", "泸州市", "德阳市", "绵阳市", "广元市", "遂宁市", "内江市", "乐山市", "南充市", "眉山市", "宜宾市", "广安市", "达州市", "雅安市", "巴中市", "资阳市", "阿坝藏族羌族自治州", "甘孜藏族自治州", "凉山彝族自治州"],
  "贵州省": ["贵阳市", "六盘水市", "遵义市", "安顺市", "毕节市", "铜仁市", "黔西南布依族苗族自治州", "黔东南苗族侗族自治州", "黔南布依族苗族自治州"],
  "云南省": ["昆明市", "曲靖市", "玉溪市", "保山市", "昭通市", "丽江市", "普洱市", "临沧市", "楚雄彝族自治州", "红河哈尼族彝族自治州", "文山壮族苗族自治州", "西双版纳傣族自治州", "大理白族自治州", "德宏傣族景颇族自治州", "怒江傈僳族自治州", "迪庆藏族自治州"],
  "西藏自治区": ["拉萨市", "日喀则市", "昌都市", "林芝市", "山南市", "那曲市", "阿里地区"],
  "陕西省": ["西安市", "铜川市", "宝鸡市", "咸阳市", "渭南市", "延安市", "汉中市", "榆林市", "安康市", "商洛市"],
  "甘肃省": ["兰州市", "嘉峪关市", "金昌市", "白银市", "天水市", "武威市", "张掖市", "平凉市", "酒泉市", "庆阳市", "定西市", "陇南市", "临夏回族自治州", "甘南藏族自治州"],
  "青海省": ["西宁市", "海东市", "海北藏族自治州", "黄南藏族自治州", "海南藏族自治州", "果洛藏族自治州", "玉树藏族自治州", "海西蒙古族藏族自治州"],
  "宁夏回族自治区": ["银川市", "石嘴山市", "吴忠市", "固原市", "中卫市"],
  "新疆维吾尔自治区": ["乌鲁木齐市", "克拉玛依市", "吐鲁番市", "哈密市", "昌吉回族自治州", "博尔塔拉蒙古自治州", "巴音郭楞蒙古自治州", "阿克苏地区", "克孜勒苏柯尔克孜自治州", "喀什地区", "和田地区", "伊犁哈萨克自治州"],
  "香港特别行政区": ["香港岛", "九龙", "新界"],
  "澳门特别行政区": ["澳门半岛", "氹仔岛", "路环岛"],
  "台湾省": ["台北市", "新北市", "桃园市", "台中市", "台南市", "高雄市", "基隆市", "新竹市", "嘉义市"]
};

const districtMap: Record<string, string[]> = {
  "东城区": ["东华门街道", "景山街道", "交道口街道", "安定门街道", "北新桥街道", "东四街道", "朝阳门街道", "建国门街道", "东直门街道", "和平里街道", "前门街道", "崇文门外街道", "东花市街道", "龙潭街道", "体育馆路街道", "天坛街道", "永定门外街道"],
  "西城区": ["西长安街街道", "新街口街道", "月坛街道", "展览路街道", "德胜街道", "金融街街道", "什刹海街道", "大栅栏街道", "天桥街道", "椿树街道", "陶然亭街道", "广安门内街道", "牛街街道", "白纸坊街道", "广安门外街道"],
  "朝阳区": ["建外街道", "朝外街道", "呼家楼街道", "三里屯街道", "左家庄街道", "香河园街道", "和平街街道", "安贞街道", "亚运村街道", "小关街道", "酒仙桥街道", "麦子店街道", "团结湖街道", "六里屯街道", "八里庄街道", "双井街道", "劲松街道", "潘家园街道", "垡头街道", "南磨房乡", "高碑店乡", "将台乡", "太阳宫乡", "大屯街道", "望京街道", "小红门乡", "十八里店乡", "平房乡", "东风乡", "常营回族乡", "管庄乡", "三间房乡", "豆各庄乡", "黑庄户乡", "王四营乡", "东坝乡", "金盏乡", "孙河乡", "崔各庄乡", "来广营乡", "东湖街道", "首都机场街道"],
  "海淀区": ["万寿路街道", "永定路街道", "羊坊店街道", "甘家口街道", "八里庄街道", "紫竹院街道", "北下关街道", "北太平庄街道", "学院路街道", "中关村街道", "海淀街道", "青龙桥街道", "清华园街道", "燕园街道", "香山街道", "花园路街道", "西三旗街道", "马连洼街道", "田村路街道", "上地街道", "万柳地区", "东升地区", "曙光街道", "温泉镇", "四季青镇", "西北旺镇", "苏家坨镇", "上庄镇"],
  "丰台区": ["右安门街道", "太平桥街道", "西罗园街道", "大红门街道", "南苑街道", "东高地街道", "东铁匠营街道", "卢沟桥街道", "丰台街道", "新村街道", "长辛店街道", "云岗街道", "方庄地区", "宛平城地区", "马家堡街道", "和义街道", "卢沟桥乡", "花乡街道", "南苑乡", "长辛店镇", "王佐镇"],
  "上海市": ["黄浦区", "徐汇区", "长宁区", "静安区", "普陀区", "虹口区", "杨浦区", "闵行区", "宝山区", "嘉定区", "浦东新区", "金山区", "松江区", "青浦区", "奉贤区", "崇明区"],
  "广州市": ["天河区", "越秀区", "海珠区", "白云区", "黄埔区", "番禺区", "花都区", "南沙区", "增城区", "从化区"],
  "深圳市": ["福田区", "罗湖区", "南山区", "宝安区", "龙岗区", "龙华区", "坪山区", "光明区", "盐田区", "大鹏新区"],
  "杭州市": ["上城区", "拱墅区", "西湖区", "滨江区", "萧山区", "余杭区", "富阳区", "临安区", "钱塘区", "临平区"],
  "南京市": ["玄武区", "秦淮区", "鼓楼区", "建邺区", "栖霞区", "雨花台区", "江宁区", "浦口区", "六合区", "溧水区", "高淳区"],
  "武汉市": ["江岸区", "江汉区", "硚口区", "汉阳区", "武昌区", "青山区", "洪山区", "东西湖区", "汉南区", "蔡甸区", "江夏区", "黄陂区", "新洲区"],
  "成都市": ["锦江区", "青羊区", "金牛区", "武侯区", "成华区", "龙泉驿区", "青白江区", "新都区", "温江区", "双流区", "郫都区", "新津区", "都江堰市", "彭州市", "邛崃市", "崇州市", "简阳市"],
  "重庆市": ["万州区", "涪陵区", "渝中区", "大渡口区", "江北区", "沙坪坝区", "九龙坡区", "南岸区", "北碚区", "綦江区", "大足区", "渝北区", "巴南区", "黔江区", "长寿区", "江津区", "合川区", "永川区", "南川区", "璧山区", "铜梁区", "潼南区", "荣昌区", "开州区", "梁平区", "武隆区"],
  "天津市": ["和平区", "河东区", "河西区", "南开区", "河北区", "红桥区", "东丽区", "西青区", "津南区", "北辰区", "武清区", "宝坻区", "滨海新区", "宁河区", "静海区", "蓟州区"],
  
  "石家庄市": ["长安区", "桥西区", "新华区", "裕华区", "井陉矿区", "藁城区", "鹿泉区", "栾城区", "井陉县", "正定县", "行唐县", "灵寿县", "高邑县", "深泽县", "赞皇县", "无极县", "平山县", "元氏县", "赵县", "辛集市", "晋州市", "新乐市"],
  "唐山市": ["路南区", "路北区", "古冶区", "开平区", "丰润区", "丰南区", "曹妃甸区", "滦州市", "遵化市", "迁安市", "滦南县", "乐亭县", "迁西县", "玉田县"],
  "秦皇岛市": ["海港区", "山海关区", "北戴河区", "抚宁区", "青龙满族自治县", "昌黎县", "卢龙县"],
  "邯郸市": ["邯山区", "丛台区", "复兴区", "峰峰矿区", "肥乡区", "永年区", "武安市", "临漳县", "成安县", "大名县", "涉县", "磁县", "邱县", "鸡泽县", "广平县", "馆陶县", "魏县", "曲周县"],
  "保定市": ["竞秀区", "莲池区", "满城区", "清苑区", "徐水区", "涿州市", "安国市", "高碑店市", "涞水县", "阜平县", "定兴县", "唐县", "高阳县", "容城县", "涞源县", "望都县", "安新县", "易县", "曲阳县", "蠡县", "顺平县", "博野县", "雄县"],
  "太原市": ["小店区", "迎泽区", "杏花岭区", "尖草坪区", "万柏林区", "晋源区", "清徐县", "阳曲县", "娄烦县", "古交市"],
  "大同市": ["平城区", "云冈区", "新荣区", "云州区", "阳高县", "天镇县", "广灵县", "灵丘县", "浑源县", "左云县"],
  "朔州市": ["朔城区", "平鲁区", "山阴县", "应县", "右玉县", "怀仁市"],
  "呼和浩特市": ["新城区", "回民区", "玉泉区", "赛罕区", "土默特左旗", "托克托县", "和林格尔县", "清水河县", "武川县"],
  "包头市": ["东河区", "昆都仑区", "青山区", "石拐区", "白云鄂博矿区", "九原区", "固阳县", "土默特右旗"],
  "沈阳市": ["和平区", "沈河区", "大东区", "皇姑区", "铁西区", "苏家屯区", "浑南区", "沈北新区", "于洪区", "辽中区", "康平县", "法库县", "新民市"],
  "大连市": ["中山区", "西岗区", "沙河口区", "甘井子区", "旅顺口区", "金州区", "普兰店区", "瓦房店市", "庄河市", "长海县"],
  "鞍山市": ["铁东区", "铁西区", "立山区", "千山区", "海城市", "台安县", "岫岩满族自治县"],
  "长春市": ["南关区", "宽城区", "朝阳区", "二道区", "绿园区", "双阳区", "九台区", "农安县", "榆树市", "德惠市"],
  "吉林市": ["昌邑区", "龙潭区", "船营区", "丰满区", "蛟河市", "桦甸市", "舒兰市", "磐石市", "永吉县"],
  "哈尔滨市": ["道里区", "南岗区", "道外区", "平房区", "松北区", "香坊区", "呼兰区", "阿城区", "双城区", "依兰县", "方正县", "宾县", "巴彦县", "木兰县", "通河县", "延寿县", "尚志市", "五常市"],
  "齐齐哈尔市": ["龙沙区", "建华区", "铁锋区", "昂昂溪区", "富拉尔基区", "碾子山区", "梅里斯达斡尔族区", "讷河市", "龙江县", "依安县", "泰来县", "甘南县", "富裕县", "克山县", "克东县", "拜泉县"],
  "苏州市": ["姑苏区", "虎丘区", "吴中区", "相城区", "吴江区", "昆山市", "常熟市", "张家港市", "太仓市"],
  "无锡市": ["梁溪区", "锡山区", "惠山区", "滨湖区", "新吴区", "江阴市", "宜兴市"],
  "徐州市": ["云龙区", "鼓楼区", "贾汪区", "泉山区", "铜山区", "邳州市", "新沂市", "丰县", "沛县", "睢宁县"],
  "常州市": ["天宁区", "钟楼区", "新北区", "武进区", "金坛区", "溧阳市"],
  "南通市": ["崇川区", "通州区", "海门区", "启东市", "如皋市", "海安市", "如东县"],
  "盐城市": ["亭湖区", "盐都区", "大丰区", "东台市", "响水县", "滨海县", "阜宁县", "射阳县", "建湖县"],
  "扬州市": ["广陵区", "邗江区", "江都区", "仪征市", "高邮市", "宝应县"],
  "镇江市": ["京口区", "润州区", "丹徒区", "丹阳市", "扬中市", "句容市"],
  "泰州市": ["海陵区", "高港区", "姜堰区", "兴化市", "靖江市", "泰兴市"],
  "宿迁市": ["宿城区", "宿豫区", "沭阳县", "泗阳县", "泗洪县"],
  "宁波市": ["海曙区", "江北区", "北仑区", "镇海区", "鄞州区", "奉化区", "余姚市", "慈溪市", "象山县", "宁海县"],
  "温州市": ["鹿城区", "龙湾区", "瓯海区", "洞头区", "瑞安市", "乐清市", "龙港市", "永嘉县", "平阳县", "苍南县", "文成县", "泰顺县"],
  "嘉兴市": ["南湖区", "秀洲区", "海宁市", "平湖市", "桐乡市", "嘉善县", "海盐县"],
  "湖州市": ["吴兴区", "南浔区", "德清县", "长兴县", "安吉县"],
  "绍兴市": ["越城区", "柯桥区", "上虞区", "诸暨市", "嵊州市", "新昌县"],
  "金华市": ["婺城区", "金东区", "兰溪市", "义乌市", "东阳市", "永康市", "武义县", "浦江县", "磐安县"],
  "衢州市": ["柯城区", "衢江区", "江山市", "常山县", "开化县", "龙游县"],
  "台州市": ["椒江区", "黄岩区", "路桥区", "温岭市", "临海市", "玉环市", "三门县", "天台县", "仙居县"],
  "丽水市": ["莲都区", "龙泉市", "青田县", "缙云县", "遂昌县", "松阳县", "云和县", "庆元县", "景宁畲族自治县"],
  "合肥市": ["瑶海区", "庐阳区", "蜀山区", "包河区", "长丰县", "肥东县", "肥西县", "庐江县", "巢湖市"],
  "芜湖市": ["镜湖区", "弋江区", "鸠江区", "三山区", "无为市", "芜湖县", "繁昌县", "南陵县"],
  "蚌埠市": ["龙子湖区", "蚌山区", "禹会区", "淮上区", "怀远县", "五河县", "固镇县"],
  "淮南市": ["大通区", "田家庵区", "谢家集区", "八公山区", "潘集区", "凤台县", "寿县"],
  "马鞍山市": ["花山区", "雨山区", "博望区", "当涂县", "含山县", "和县"],
  "福州市": ["鼓楼区", "台江区", "仓山区", "马尾区", "晋安区", "长乐区", "福清市", "闽侯县", "连江县", "罗源县", "闽清县", "永泰县", "平潭县"],
  "厦门市": ["思明区", "海沧区", "湖里区", "集美区", "同安区", "翔安区"],
  "莆田市": ["城厢区", "涵江区", "荔城区", "秀屿区", "仙游县"],
  "泉州市": ["鲤城区", "丰泽区", "洛江区", "泉港区", "石狮市", "晋江市", "南安市", "惠安县", "安溪县", "永春县", "德化县", "金门县"],
  "漳州市": ["芗城区", "龙文区", "龙海市", "云霄县", "漳浦县", "诏安县", "长泰县", "东山县", "南靖县", "平和县", "华安县"],
  "南昌市": ["东湖区", "西湖区", "青云谱区", "青山湖区", "新建区", "南昌县", "进贤县", "安义县"],
  "九江市": ["浔阳区", "濂溪区", "柴桑区", "瑞昌市", "共青城市", "庐山市", "武宁县", "修水县", "永修县", "德安县", "星子县", "都昌县", "湖口县", "彭泽县"],
  "景德镇市": ["昌江区", "珠山区", "乐平市", "浮梁县"],
  "济南市": ["历下区", "市中区", "槐荫区", "天桥区", "历城区", "长清区", "章丘区", "济阳区", "莱芜区", "钢城区", "平阴县", "商河县"],
  "青岛市": ["市南区", "市北区", "黄岛区", "崂山区", "李沧区", "城阳区", "即墨区", "胶州市", "平度市", "莱西市"],
  "淄博市": ["张店区", "淄川区", "博山区", "临淄区", "周村区", "桓台县", "高青县", "沂源县"],
  "枣庄市": ["市中区", "薛城区", "峄城区", "台儿庄区", "山亭区", "滕州市"],
  "东营市": ["东营区", "河口区", "垦利区", "利津县", "广饶县"],
  "烟台市": ["芝罘区", "福山区", "牟平区", "莱山区", "龙口市", "莱阳市", "莱州市", "蓬莱市", "招远市", "栖霞市", "海阳市", "长岛县"],
  "潍坊市": ["潍城区", "寒亭区", "坊子区", "奎文区", "青州市", "诸城市", "寿光市", "安丘市", "高密市", "昌邑市", "临朐县", "昌乐县"],
  "济宁市": ["任城区", "兖州区", "曲阜市", "邹城市", "微山县", "鱼台县", "金乡县", "嘉祥县", "汶上县", "泗水县", "梁山县"],
  "泰安市": ["泰山区", "岱岳区", "新泰市", "肥城市", "宁阳县", "东平县"],
  "威海市": ["环翠区", "文登区", "荣成市", "乳山市"],
  "日照市": ["东港区", "岚山区", "五莲县", "莒县"],
  "临沂市": ["兰山区", "罗庄区", "河东区", "沂南县", "郯城县", "沂水县", "兰陵县", "费县", "平邑县", "莒南县", "蒙阴县", "临沭县"],
  "德州市": ["德城区", "陵城区", "乐陵市", "禹城市", "宁津县", "庆云县", "临邑县", "齐河县", "平原县", "夏津县", "武城县"],
  "聊城市": ["东昌府区", "临清市", "阳谷县", "莘县", "茌平县", "东阿县", "冠县", "高唐县"],
  "滨州市": ["滨城区", "沾化区", "邹平市", "惠民县", "阳信县", "无棣县", "博兴县"],
  "菏泽市": ["牡丹区", "定陶区", "曹县", "单县", "成武县", "巨野县", "郓城县", "鄄城县", "东明县"],
  "郑州市": ["中原区", "二七区", "管城回族区", "金水区", "上街区", "惠济区", "中牟县", "巩义市", "荥阳市", "新密市", "新郑市", "登封市"],
  "开封市": ["龙亭区", "顺河回族区", "鼓楼区", "禹王台区", "祥符区", "杞县", "通许县", "尉氏县", "兰考县"],
  "洛阳市": ["老城区", "西工区", "瀍河回族区", "涧西区", "吉利区", "洛龙区", "偃师区", "孟津区", "新安县", "栾川县", "嵩县", "汝阳县", "宜阳县", "洛宁县", "伊川县"],
  "平顶山市": ["新华区", "卫东区", "石龙区", "湛河区", "舞钢市", "汝州市", "宝丰县", "叶县", "鲁山县", "郏县"],
  "安阳市": ["文峰区", "北关区", "殷都区", "龙安区", "林州市", "安阳县", "汤阴县", "滑县", "内黄县"],
  "新乡市": ["红旗区", "卫滨区", "凤泉区", "牧野区", "卫辉市", "辉县市", "新乡县", "获嘉县", "原阳县", "延津县", "封丘县", "长垣市"],
  "许昌市": ["魏都区", "建安区", "禹州市", "长葛市", "鄢陵县", "襄城县"],
  "漯河市": ["源汇区", "郾城区", "召陵区", "舞阳县", "临颍县"],
  "南阳市": ["宛城区", "卧龙区", "邓州市", "南召县", "方城县", "西峡县", "镇平县", "内乡县", "淅川县", "社旗县", "唐河县", "新野县", "桐柏县"],
  "商丘市": ["梁园区", "睢阳区", "永城市", "民权县", "睢县", "宁陵县", "柘城县", "虞城县", "夏邑县"],
  "信阳市": ["浉河区", "平桥区", "固始县", "罗山县", "光山县", "新县", "商城县", "潢川县", "淮滨县", "息县"],
  "周口市": ["川汇区", "项城市", "扶沟县", "西华县", "商水县", "沈丘县", "郸城县", "淮阳县", "太康县", "鹿邑县"],
  "驻马店市": ["驿城区", "西平县", "上蔡县", "平舆县", "正阳县", "确山县", "泌阳县", "汝南县", "遂平县", "新蔡县"],
  "襄阳市": ["襄城区", "樊城区", "襄州区", "老河口市", "枣阳市", "宜城市", "南漳县", "谷城县", "保康县"],
  "宜昌市": ["西陵区", "伍家岗区", "点军区", "猇亭区", "夷陵区", "宜都市", "当阳市", "枝江市", "远安县", "兴山县", "秭归县", "长阳土家族自治县", "五峰土家族自治县"],
  "黄石市": ["黄石港区", "西塞山区", "下陆区", "铁山区", "大冶市", "阳新县"],
  "十堰市": ["茅箭区", "张湾区", "丹江口市", "郧阳区", "郧西县", "竹山县", "竹溪县", "房县"],
  "荆州市": ["沙市区", "荆州区", "石首市", "洪湖市", "松滋市", "公安县", "监利市", "江陵县"],
  "黄冈市": ["黄州区", "麻城市", "武穴市", "团风县", "红安县", "罗田县", "英山县", "浠水县", "蕲春县", "黄梅县", "龙感湖管理区"],
  "广州市": ["天河区", "越秀区", "海珠区", "白云区", "黄埔区", "番禺区", "花都区", "南沙区", "增城区", "从化区"],
  "韶关市": ["浈江区", "武江区", "曲江区", "乐昌市", "南雄市", "始兴县", "仁化县", "翁源县", "新丰县", "乳源瑶族自治县"],
  "佛山市": ["禅城区", "南海区", "顺德区", "三水区", "高明区"],
  "江门市": ["蓬江区", "江海区", "新会区", "台山市", "开平市", "鹤山市", "恩平市"],
  "湛江市": ["赤坎区", "霞山区", "坡头区", "麻章区", "廉江市", "雷州市", "吴川市", "遂溪县", "徐闻县"],
  "肇庆市": ["端州区", "鼎湖区", "高要区", "四会市", "广宁县", "怀集县", "封开县", "德庆县"],
  "江门市": ["蓬江区", "江海区", "新会区", "台山市", "开平市", "鹤山市", "恩平市"],
  "茂名市": ["茂南区", "电白区", "高州市", "化州市", "信宜市"],
  "惠州市": ["惠城区", "惠阳区", "博罗县", "惠东县", "龙门县"],
  "梅州市": ["梅江区", "梅县区", "兴宁市", "大埔县", "丰顺县", "五华县", "平远县", "蕉岭县"],
  "汕尾市": ["城区", "海丰县", "陆河县", "陆丰市"],
  "河源市": ["源城区", "紫金县", "龙川县", "连平县", "和平县", "东源县"],
  "阳江市": ["江城区", "阳东区", "阳春市", "阳西县"],
  "清远市": ["清城区", "清新区", "英德市", "连州市", "佛冈县", "阳山县", "连山壮族瑶族自治县", "连南瑶族自治县"],
  "东莞市": ["莞城街道", "南城街道", "东城街道", "万江街道", "石龙镇", "虎门镇", "中堂镇", "望牛墩镇", "麻涌镇", "石碣镇", "高埗镇", "洪梅镇", "道滘镇", "厚街镇", "沙田镇", "长安镇", "寮步镇", "大岭山镇", "大朗镇", "黄江镇", "樟木头镇", "谢岗镇", "塘厦镇", "清溪镇", "凤岗镇", "常平镇", "桥头镇", "横沥镇", "东坑镇", "企石镇", "石排镇", "茶山镇", "松山湖高新区"],
  "中山市": ["石岐街道", "东区街道", "西区街道", "南区街道", "五桂山街道", "火炬开发区", "黄圃镇", "南头镇", "东凤镇", "阜沙镇", "小榄镇", "东升镇", "古镇镇", "横栏镇", "三角镇", "民众镇", "南朗镇", "港口镇", "大涌镇", "沙溪镇", "三乡镇", "板芙镇", "神湾镇", "坦洲镇"],
  "潮州市": ["湘桥区", "潮安区", "饶平县"],
  "揭阳市": ["榕城区", "揭东区", "普宁市", "揭西县", "惠来县"],
  "云浮市": ["云城区", "云安区", "罗定市", "新兴县", "郁南县"],
  "南宁市": ["兴宁区", "青秀区", "江南区", "西乡塘区", "良庆区", "邕宁区", "武鸣区", "隆安县", "马山县", "上林县", "宾阳县", "横州市"],
  "柳州市": ["城中区", "鱼峰区", "柳南区", "柳北区", "柳江区", "柳城县", "鹿寨县", "融安县", "融水苗族自治县", "三江侗族自治县"],
  "桂林市": ["秀峰区", "叠彩区", "象山区", "七星区", "雁山区", "临桂区", "阳朔县", "灵川县", "全州县", "兴安县", "永福县", "灌阳县", "龙胜各族自治县", "资源县", "平乐县", "荔浦市", "恭城瑶族自治县"],
  "梧州市": ["万秀区", "长洲区", "龙圩区", "岑溪市", "苍梧县", "藤县", "蒙山县"],
  "北海市": ["海城区", "银海区", "铁山港区", "合浦县"],
  "防城港市": ["港口区", "防城区", "东兴市", "上思县"],
  "钦州市": ["钦南区", "钦北区", "灵山县", "浦北县"],
  "贵港市": ["港北区", "港南区", "覃塘区", "桂平市", "平南县"],
  "玉林市": ["玉州区", "福绵区", "北流市", "容县", "陆川县", "博白县", "兴业县"],
  "百色市": ["右江区", "田阳区", "靖西市", "平果市", "田东县", "德保县", "那坡县", "凌云县", "乐业县", "田林县", "西林县", "隆林各族自治县"],
  "贺州市": ["八步区", "平桂区", "昭平县", "钟山县", "富川瑶族自治县"],
  "河池市": ["金城江区", "宜州区", "南丹县", "天峨县", "凤山县", "东兰县", "罗城仫佬族自治县", "环江毛南族自治县", "巴马瑶族自治县", "都安瑶族自治县", "大化瑶族自治县"],
  "来宾市": ["兴宾区", "合山市", "忻城县", "象州县", "武宣县", "金秀瑶族自治县"],
  "崇左市": ["江州区", "凭祥市", "扶绥县", "宁明县", "龙州县", "大新县", "天等县"],
  "海口市": ["秀英区", "龙华区", "琼山区", "美兰区"],
  "三亚市": ["海棠区", "吉阳区", "天涯区", "崖州区"],
  "儋州市": ["那大镇", "白马井镇", "中和镇", "排浦镇", "王五镇", "光村镇", "木棠镇", "海头镇", "峨蔓镇", "兰洋镇", "南丰镇", "雅星镇", "大成镇", "新州镇", "东成镇", "光村镇", "长坡镇"],
  "成都市": ["锦江区", "青羊区", "金牛区", "武侯区", "成华区", "龙泉驿区", "青白江区", "新都区", "温江区", "双流区", "郫都区", "新津区", "都江堰市", "彭州市", "邛崃市", "崇州市", "简阳市"],
  "自贡市": ["自流井区", "贡井区", "大安区", "沿滩区", "荣县", "富顺县"],
  "攀枝花市": ["东区", "西区", "仁和区", "米易县", "盐边县"],
  "泸州市": ["江阳区", "纳溪区", "龙马潭区", "泸县", "合江县", "叙永县", "古蔺县"],
  "德阳市": ["旌阳区", "罗江区", "广汉市", "什邡市", "绵竹市", "中江县"],
  "绵阳市": ["涪城区", "游仙区", "安州区", "江油市", "三台县", "盐亭县", "梓潼县", "北川羌族自治县", "平武县"],
  "广元市": ["利州区", "昭化区", "朝天区", "旺苍县", "青川县", "剑阁县", "苍溪县"],
  "遂宁市": ["船山区", "安居区", "射洪市", "蓬溪县", "大英县"],
  "内江市": ["市中区", "东兴区", "隆昌市", "资中县", "威远县"],
  "乐山市": ["市中区", "沙湾区", "五通桥区", "金口河区", "峨眉山市", "犍为县", "井研县", "夹江县", "沐川县", "峨边彝族自治县", "马边彝族自治县"],
  "南充市": ["顺庆区", "高坪区", "嘉陵区", "阆中市", "南部县", "营山县", "蓬安县", "仪陇县", "西充县"],
  "眉山市": ["东坡区", "彭山区", "仁寿县", "洪雅县", "丹棱县", "青神县"],
  "宜宾市": ["翠屏区", "南溪区", "叙州区", "江安县", "长宁县", "高县", "珙县", "筠连县", "兴文县", "屏山县"],
  "广安市": ["广安区", "前锋区", "华蓥市", "岳池县", "武胜县", "邻水县"],
  "达州市": ["通川区", "达川区", "万源市", "宣汉县", "开江县", "大竹县", "渠县"],
  "雅安市": ["雨城区", "名山区", "荥经县", "汉源县", "石棉县", "天全县", "芦山县", "宝兴县"],
  "巴中市": ["巴州区", "恩阳区", "通江县", "南江县", "平昌县"],
  "资阳市": ["雁江区", "安岳县", "乐至县"],
  "贵阳市": ["南明区", "云岩区", "花溪区", "乌当区", "白云区", "观山湖区", "开阳县", "息烽县", "修文县", "清镇市"],
  "六盘水市": ["钟山区", "水城区", "六枝特区", "盘州市"],
  "遵义市": ["红花岗区", "汇川区", "播州区", "赤水市", "仁怀市", "桐梓县", "绥阳县", "正安县", "道真仡佬族苗族自治县", "务川仡佬族苗族自治县", "凤冈县", "湄潭县", "余庆县", "习水县"],
  "安顺市": ["西秀区", "平坝区", "普定县", "镇宁布依族苗族自治县", "关岭布依族苗族自治县", "紫云苗族布依族自治县"],
  "毕节市": ["七星关区", "大方县", "黔西县", "金沙县", "织金县", "纳雍县", "威宁彝族回族苗族自治县", "赫章县"],
  "铜仁市": ["碧江区", "万山区", "江口县", "玉屏侗族自治县", "石阡县", "思南县", "印江土家族苗族自治县", "德江县", "沿河土家族自治县", "松桃苗族自治县"],
  "昆明市": ["五华区", "盘龙区", "官渡区", "西山区", "东川区", "呈贡区", "晋宁区", "富民县", "宜良县", "石林彝族自治县", "嵩明县", "禄劝彝族苗族自治县", "寻甸回族彝族自治县", "安宁市"],
  "曲靖市": ["麒麟区", "沾益区", "马龙区", "宣威市", "富源县", "罗平县", "师宗县", "陆良县", "会泽县"],
  "玉溪市": ["红塔区", "江川区", "澄江市", "通海县", "华宁县", "易门县", "峨山彝族自治县", "新平彝族傣族自治县", "元江哈尼族彝族傣族自治县"],
  "保山市": ["隆阳区", "腾冲市", "施甸县", "龙陵县", "昌宁县"],
  "昭通市": ["昭阳区", "鲁甸县", "巧家县", "盐津县", "大关县", "永善县", "绥江县", "镇雄县", "彝良县", "威信县", "水富市"],
  "丽江市": ["古城区", "玉龙纳西族自治县", "永胜县", "华坪县", "宁蒗彝族自治县"],
  "普洱市": ["思茅区", "宁洱哈尼族彝族自治县", "墨江哈尼族自治县", "景东彝族自治县", "景谷傣族彝族自治县", "镇沅彝族哈尼族拉祜族自治县", "江城哈尼族彝族自治县", "孟连傣族拉祜族佤族自治县", "澜沧拉祜族自治县", "西盟佤族自治县"],
  "临沧市": ["临翔区", "凤庆县", "云县", "永德县", "镇康县", "双江拉祜族佤族布朗族傣族自治县", "耿马傣族佤族自治县", "沧源佤族自治县"],
  "西安市": ["新城区", "碑林区", "莲湖区", "灞桥区", "未央区", "雁塔区", "阎良区", "临潼区", "长安区", "高陵区", "鄠邑区", "蓝田县", "周至县"],
  "铜川市": ["王益区", "印台区", "耀州区", "宜君县"],
  "宝鸡市": ["渭滨区", "金台区", "陈仓区", "凤翔区", "岐山县", "扶风县", "眉县", "陇县", "千阳县", "麟游县", "凤县", "太白县"],
  "咸阳市": ["秦都区", "渭城区", "兴平市", "武功县", "三原县", "泾阳县", "乾县", "礼泉县", "永寿县", "彬州市", "长武县", "旬邑县", "淳化县", "武功县"],
  "渭南市": ["临渭区", "华州区", "韩城市", "华阴市", "潼关县", "大荔县", "合阳县", "澄城县", "蒲城县", "白水县", "富平县"],
  "延安市": ["宝塔区", "安塞区", "延长县", "延川县", "子长市", "志丹县", "吴起县", "甘泉县", "富县", "洛川县", "宜川县", "黄龙县", "黄陵县"],
  "汉中市": ["汉台区", "南郑区", "城固县", "洋县", "西乡县", "勉县", "宁强县", "略阳县", "镇巴县", "留坝县", "佛坪县"],
  "榆林市": ["榆阳区", "横山区", "神木市", "府谷县", "靖边县", "定边县", "绥德县", "米脂县", "佳县", "吴堡县", "清涧县", "子洲县"],
  "安康市": ["汉滨区", "汉阴县", "石泉县", "宁陕县", "紫阳县", "岚皋县", "平利县", "镇坪县", "旬阳县", "白河县"],
  "商洛市": ["商州区", "洛南县", "丹凤县", "商南县", "山阳县", "镇安县", "柞水县"],
  "兰州市": ["城关区", "七里河区", "西固区", "安宁区", "红古区", "永登县", "皋兰县", "榆中县"],
  "嘉峪关市": ["雄关区", "长城区", "镜铁区"],
  "金昌市": ["金川区", "永昌县"],
  "白银市": ["白银区", "平川区", "靖远县", "会宁县", "景泰县"],
  "天水市": ["秦州区", "麦积区", "清水县", "秦安县", "甘谷县", "武山县", "张家川回族自治县"],
  "武威市": ["凉州区", "民勤县", "古浪县", "天祝藏族自治县"],
  "张掖市": ["甘州区", "肃南裕固族自治县", "民乐县", "临泽县", "高台县", "山丹县"],
  "平凉市": ["崆峒区", "泾川县", "灵台县", "崇信县", "华亭市", "庄浪县", "静宁县"],
  "酒泉市": ["肃州区", "玉门市", "敦煌市", "金塔县", "瓜州县", "肃北蒙古族自治县", "阿克塞哈萨克族自治县"],
  "庆阳市": ["西峰区", "庆城县", "环县", "华池县", "合水县", "正宁县", "宁县", "镇原县"],
  "定西市": ["安定区", "通渭县", "陇西县", "渭源县", "临洮县", "漳县", "岷县"],
  "陇南市": ["武都区", "成县", "文县", "宕昌县", "康县", "西和县", "礼县", "徽县", "两当县"],
  "银川市": ["兴庆区", "西夏区", "金凤区", "永宁县", "贺兰县", "灵武市"],
  "石嘴山市": ["大武口区", "惠农区", "平罗县"],
  "吴忠市": ["利通区", "红寺堡区", "盐池县", "同心县", "青铜峡市"],
  "固原市": ["原州区", "西吉县", "隆德县", "泾源县", "彭阳县"],
  "中卫市": ["沙坡头区", "中宁县", "海原县"],
  "乌鲁木齐市": ["天山区", "沙依巴克区", "新市区", "水磨沟区", "头屯河区", "达坂城区", "米东区", "乌鲁木齐县"],
  "克拉玛依市": ["独山子区", "克拉玛依区", "白碱滩区", "乌尔禾区"],
  "吐鲁番市": ["高昌区", "鄯善县", "托克逊县"],
  "哈密市": ["伊州区", "巴里坤哈萨克自治县", "伊吾县"],
  "拉萨市": ["城关区", "堆龙德庆区", "达孜区", "林周县", "当雄县", "尼木县", "曲水县", "墨竹工卡县"],
  
  "长沙市": ["芙蓉区", "天心区", "岳麓区", "开福区", "雨花区", "望城区", "长沙县", "宁乡市", "浏阳市"],
  "株洲市": ["天元区", "荷塘区", "芦淞区", "石峰区", "渌口区", "攸县", "茶陵县", "炎陵县", "醴陵市"],
  "湘潭市": ["雨湖区", "岳塘区", "湘潭县", "韶山市", "湘乡市"],
  "衡阳市": ["珠晖区", "雁峰区", "石鼓区", "蒸湘区", "南岳区", "衡阳县", "衡南县", "衡山县", "衡东县", "祁东县", "耒阳市", "常宁市"],
  "邵阳市": ["双清区", "大祥区", "北塔区", "邵东县", "新邵县", "邵阳县", "隆回县", "洞口县", "绥宁县", "新宁县", "城步苗族自治县", "武冈市"],
  "岳阳市": ["岳阳楼区", "云溪区", "君山区", "岳阳县", "华容县", "湘阴县", "平江县", "汨罗市", "临湘市"],
  "常德市": ["武陵区", "鼎城区", "安乡县", "汉寿县", "澧县", "临澧县", "桃源县", "石门县", "津市市"],
  "张家界市": ["永定区", "武陵源区", "慈利县", "桑植县"],
  "益阳市": ["资阳区", "赫山区", "南县", "桃江县", "安化县", "沅江市"],
  "郴州市": ["北湖区", "苏仙区", "桂阳县", "宜章县", "永兴县", "嘉禾县", "临武县", "汝城县", "桂东县", "安仁县", "资兴市"],
  "永州市": ["零陵区", "冷水滩区", "祁阳县", "东安县", "双牌县", "道县", "江永县", "宁远县", "蓝山县", "新田县", "江华瑶族自治县"],
  "怀化市": ["鹤城区", "中方县", "沅陵县", "辰溪县", "溆浦县", "会同县", "麻阳苗族自治县", "新晃侗族自治县", "芷江侗族自治县", "靖州苗族侗族自治县", "通道侗族自治县", "洪江市"],
  "娄底市": ["娄星区", "双峰县", "新化县", "冷水江市", "涟源市"],
  "湘西土家族苗族自治州": ["吉首市", "泸溪县", "凤凰县", "花垣县", "保靖县", "古丈县", "永顺县", "龙山县"]
};

const cities = ref<string[]>([]);
const districts = ref<string[]>([]);

onMounted(() => {
  const user = localStorage.getItem("loginUser");
  if (!user) {
    alert("请先登录");
    router.push("/login");
    return;
  }
  currentUser.value = user;
  
  const savedProfile = localStorage.getItem("profile");
  if (savedProfile) {
    const profile = JSON.parse(savedProfile);
    profileForm.value = profile;
    if (profile.username.trim()) {
      currentUser.value = profile.username;
    }
  }
  
  loadAddresses();
});

async function loadAddresses() {
  try {
    const token = localStorage.getItem('token');
    const res = await fetch('http://localhost:8081/api/addresses', {
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token || ''}`
      }
    });
    const data = await res.json();
    if (data.code === 200) {
      addresses.value = data.data.map((addr: any) => ({
        id: addr.id,
        name: addr.name,
        phone: addr.phone,
        province: addr.province || '',
        city: addr.city || '',
        district: addr.district || '',
        detail: addr.address || addr.detail || ''
      })) || [];
    }
  } catch (error) {
    console.error('加载地址失败:', error);
  }
}

async function saveAddresses() {
}

function goHome() {
  router.push("/home");
}

function goCart() {
  router.push("/cart");
}

function goFav() {
  router.push("/favorites");
}

function goHelp() {
  router.push("/help");
}

function goProfile() {
  // 已经在个人中心页面，不需要跳转
}

function goAddress() {
  showSettingsModal.value = false;
  showAddressModal.value = true;
}

function closeAddressListModal() {
  showAddressListModal.value = false;
}

function openEditProfile() {
  showSettingsModal.value = false;
  showEditProfileModal.value = true;
}

function closeEditProfileModal() {
  showEditProfileModal.value = false;
}

function saveProfile() {
  localStorage.setItem("profile", JSON.stringify(profileForm.value));
  if (profileForm.value.username.trim()) {
    currentUser.value = profileForm.value.username;
    localStorage.setItem("loginUser", profileForm.value.username);
  }
  alert("个人资料修改成功！");
  closeEditProfileModal();
}

function onProvinceChange() {
  const province = addressForm.value.province;
  cities.value = cityMap[province] || [];
  addressForm.value.city = "";
  districts.value = [];
  addressForm.value.district = "";
}

function onCityChange() {
  const city = addressForm.value.city;
  const province = addressForm.value.province;
  
  if (province === "北京市" || province === "天津市" || province === "上海市") {
    districts.value = [];
    addressForm.value.district = city;
    return;
  }
  
  districts.value = districtMap[city] || [];
  addressForm.value.district = "";
}



function editAddress(index: number) {
  const addr = addresses.value[index];
  addressForm.value = { ...addr };
  editingIndex.value = index;
  onProvinceChange();
  setTimeout(() => {
    if (addr.city) {
      districts.value = districtMap[addr.city] || [];
    }
  }, 0);
  showAddAddress.value = true;
}

async function deleteAddress(index: number) {
  if (confirm("确定要删除这个地址吗？")) {
    const addr = addresses.value[index];
    try {
      const token = localStorage.getItem('token');
      await fetch(`http://localhost:8081/api/addresses/${addr.id}`, {
        method: 'DELETE',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token || ''}`
        }
      });
      addresses.value.splice(index, 1);
      alert("地址删除成功");
    } catch (error) {
      console.error('删除地址失败:', error);
      alert("删除地址失败");
    }
  }
}

async function saveAddress() {
  if (!addressForm.value.name.trim()) {
    alert("请输入收货人姓名");
    return;
  }
  if (!addressForm.value.phone.trim()) {
    alert("请输入联系电话");
    return;
  }
  if (!addressForm.value.province) {
    alert("请选择省份");
    return;
  }
  if (!addressForm.value.city) {
    alert("请选择城市");
    return;
  }
  if (!addressForm.value.detail.trim()) {
    alert("请输入详细地址");
    return;
  }

  try {
    const token = localStorage.getItem('token');
    const addrData = {
      name: addressForm.value.name,
      phone: addressForm.value.phone,
      address: addressForm.value.detail,
      province: addressForm.value.province,
      city: addressForm.value.city,
      district: addressForm.value.district,
      isDefault: false
    };

    if (editingIndex.value >= 0) {
      const addrId = addresses.value[editingIndex.value].id;
      await fetch(`http://localhost:8081/api/addresses/${addrId}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token || ''}`
        },
        body: JSON.stringify(addrData)
      });
      addresses.value[editingIndex.value] = { ...addressForm.value };
    } else {
      const res = await fetch('http://localhost:8081/api/addresses', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token || ''}`
        },
        body: JSON.stringify(addrData)
      });
      const data = await res.json();
      if (data.code === 200) {
        addresses.value.push({
          ...addressForm.value,
          id: data.data.id
        });
      }
    }
    
    closeAddAddress();
    await loadAddresses();
    alert("地址保存成功");
  } catch (error) {
    console.error('保存地址失败:', error);
    alert("保存地址失败");
  }
}

function closeAddAddress() {
  showAddAddress.value = false;
  editingIndex.value = -1;
  addressForm.value = {
    name: "",
    phone: "",
    province: "",
    city: "",
    district: "",
    detail: ""
  };
  cities.value = [];
  districts.value = [];
}

async function loadOrders() {
  const token = localStorage.getItem("token");
  if (!token) {
    alert("请先登录");
    showOrderModal.value = false;
    return;
  }
  
  try {
    const response = await fetch("http://localhost:8081/api/orders", {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        "x-auth-token": token
      }
    });
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    
    const data = await response.json();
    if (data.code === 200) {
      orders.value = data.data || [];
    } else {
      orders.value = [];
      console.warn("加载订单失败:", data.message);
    }
  } catch (error) {
    console.error("加载订单失败:", error);
    orders.value = [];
    alert("加载订单失败，请检查后端服务是否启动！");
  }
}

function getOrderStatusText(status: string) {
  const statusMap: Record<string, string> = {
    'pending': '待处理',
    'paid': '已付款',
    'shipped': '已发货',
    'completed': '已完成',
    'cancelled': '已取消'
  };
  return statusMap[status] || status;
}

async function confirmReceive(orderId: number) {
  if (!confirm("确定要确认收货吗？")) {
    return;
  }
  const token = localStorage.getItem("token");
  try {
    const response = await fetch(`http://localhost:8081/api/orders/${orderId}/receive`, {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
        "x-auth-token": token || ""
      }
    });
    const data = await response.json();
    if (data.code === 200) {
      alert("确认收货成功！");
      orders.value = orders.value.filter(orderDetail => orderDetail.order.id !== orderId);
    } else {
      alert("操作失败: " + (data.message || "未知错误"));
    }
  } catch (error) {
    console.error("确认收货失败:", error);
    alert("操作失败");
  }
}

function showRefundModal(orderId: number) {
  currentRefundOrderId.value = orderId;
  refundReason.value = "";
  showRefundApplyModal.value = true;
}

function showGoodsDetail(item: any) {
  currentGoodsDetail.value = item;
  showGoodsDetailModal.value = true;
}

function showOrderDetail(order: any) {
  currentOrderDetail.value = order;
  showOrderDetailModal.value = true;
}

function closeOrderDetailModal() {
  showOrderDetailModal.value = false;
  currentOrderDetail.value = null;
}

async function submitRefund() {
  if (!refundReason.value.trim()) {
    alert("请填写退款原因");
    return;
  }
  if (!currentRefundOrderId.value) {
    return;
  }
  const token = localStorage.getItem("token");
  try {
    const response = await fetch(`http://localhost:8081/api/orders/${currentRefundOrderId.value}/refund`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "x-auth-token": token || ""
      },
      body: JSON.stringify({
        reason: refundReason.value.trim()
      })
    });
    const data = await response.json();
    if (data.code === 200) {
      alert("退款申请已提交，请等待管理员审核！");
      showRefundApplyModal.value = false;
      loadOrders();
    } else {
      alert("操作失败: " + (data.message || "未知错误"));
    }
  } catch (error) {
    console.error("退款申请失败:", error);
    alert("操作失败");
  }
}

function goEditProfile() {
  showSettingsModal.value = false;
  alert("编辑个人资料功能开发中");
}

function openOrderModal() {
  showOrderModal.value = true;
  loadOrders();
}



async function confirmDeleteAccount() {
  if (!confirm("⚠️ 警告：注销账号后将无法恢复，所有数据将被删除。确定要注销此账号吗？")) {
    return;
  }
  
  showSettingsModal.value = false;
  
  try {
    const token = localStorage.getItem("token");
    const response = await fetch("http://localhost:8081/api/auth/delete", {
      method: "DELETE",
      headers: {
        "Content-Type": "application/json",
        "x-auth-token": token
      }
    });
    
    const data = await response.json();
    
    if (data.code === 200) {
      localStorage.removeItem("token");
      localStorage.removeItem("loginUser");
      localStorage.removeItem("isAdmin");
      alert("账号注销成功");
      router.push("/login");
    } else {
      alert(data.message || "注销失败");
    }
  } catch (error) {
    alert("网络错误，请稍后重试");
  }
}

</script>

<style scoped>
.profile-page {
  min-height: 100vh;
  background-color: #fff;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 30px;
  background: #fff;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
}

.header-center h1 {
  margin: 0;
  font-size: 24px;
  color: #333;
}

.header-right {
  display: flex;
  gap: 25px;
}

.nav-item {
  text-decoration: none;
  color: #666;
  font-size: 15px;
}

.nav-item:hover {
  color: #4a90d9;
}

.nav-item.active {
  color: #4a90d9;
  font-weight: bold;
}

.edit-profile-modal {
  width: 520px;
  background: #fff;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 8px 30px rgba(0,0,0,0.15), 0 2px 8px rgba(0,0,0,0.1);
  border: 1px solid #f0f0f0;
}

.profile-header {
  background: linear-gradient(135deg, #838385) !important;
  padding: 22px 30px !important;
  border-bottom: none !important;
}

.profile-header h3 {
  color: #fff !important;
  font-size: 19px !important;
  font-weight: 600 !important;
}

.profile-header .close-btn {
  color: rgba(255, 255, 255, 0.8) !important;
  font-size: 28px !important;
}

.profile-header .close-btn:hover {
  color: #fff !important;
}

.profile-body {
  padding: 30px;
}

.profile-avatar-section {
  text-align: center;
  margin-bottom: 30px;
}

.avatar-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 15px;
}

.avatar-wrapper .avatar {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  background: linear-gradient(135deg, #707070 );
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 25px rgba(123, 31, 162, 0.3);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.avatar-wrapper .avatar:hover {
  transform: scale(1.05);
  box-shadow: 0 12px 35px rgba(123, 31, 162, 0.4);
}

.avatar-icon {
  font-size: 45px;
}

.change-avatar-btn {
  padding: 10px 25px;
  background: rgba(123, 31, 162, 0.1);
  border: 1px solid rgba(123, 31, 162, 0.3);
  border-radius: 25px;
  color: #838385;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.change-avatar-btn:hover {
  background: rgba(123, 31, 162, 0.15);
  border-color: #838385;
}

.profile-input {
  padding: 14px 18px !important;
  border-radius: 12px !important;
  border: 2px solid #e5e7eb !important;
  font-size: 15px !important;
  transition: all 0.3s ease !important;
}

.profile-input:focus {
  outline: none !important;
  border-color: #6b7280 !important;
  box-shadow: 0 0 0 3px rgba(107, 114, 128, 0.15) !important;
}

.profile-textarea {
  padding: 14px 18px !important;
  border-radius: 12px !important;
  border: 2px solid #e5e7eb !important;
  font-size: 15px !important;
  resize: none !important;
  transition: all 0.3s ease !important;
}

.profile-textarea:focus {
  outline: none !important;
  border-color: #6b7280 !important;
  box-shadow: 0 0 0 3px rgba(107, 114, 128, 0.15) !important;
}

.profile-btn-group {
  display: flex;
  gap: 15px;
  margin-top: 25px;
}

.profile-save-btn {
  flex: 1;
  padding: 14px;
  background: linear-gradient(135deg, #838385); 
  color: #fff;
  border: none;
  border-radius: 12px;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.profile-save-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(123, 31, 162, 0.4);
}

.profile-cancel-btn {
  padding: 14px 30px;
  background: #fff;
  color: #64748b;
  border: 2px solid #e2e8f0;
  border-radius: 12px;
  font-size: 15px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.profile-cancel-btn:hover {
  border-color: #cbd5e1;
  background: #f8fafc;
}

.profile-content {
  padding: 30px 40px;
  background-color: #fff;
}

.user-card {
  background: #fff;
  border-radius: 20px;
  padding: 30px;
  margin-bottom: 30px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.12), 0 1px 3px rgba(0,0,0,0.08);
  border: 1px solid #f0f0f0;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 25px;
}

.avatar {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  background: linear-gradient(135deg, #6b7280 0%, #4b5563 100%);
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 32px;
  color: #fff;
  font-weight: 600;
  box-shadow: 0 8px 25px rgba(107, 114, 128, 0.4);
  transition: transform 0.3s ease;
}

.avatar:hover {
  transform: scale(1.05);
}

.user-detail {
  flex: 1;
}

.nickname {
  font-size: 24px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}

.user-actions {
  display: flex;
  justify-content: flex-end;
  gap: 40px;
  margin-top: 25px;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #666;
  font-size: 15px;
  cursor: pointer;
  transition: all 0.3s;
  padding: 10px 20px;
  border-radius: 20px;
}

.action-btn:hover {
  color: #667eea;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8ec 100%);
}

.order-card {
  background: rgba(255,255,255,0.98);
  border-radius: 20px;
  padding: 30px;
  box-shadow: 0 8px 30px rgba(0,0,0,0.08);
}

.card-title {
  margin: 0 0 25px;
  font-size: 22px;
  font-weight: 600;
  color: #333;
  display: flex;
  align-items: center;
  gap: 10px;
}

.card-title::before {
  content: '';
  width: 4px;
  height: 20px;
  background: linear-gradient(135deg, #838385);   
  border-radius: 2px; 
}

.order-links {
  display: flex;
  gap: 50px;
  flex-wrap: wrap;
}

.order-link {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  font-size: 16px;
  color: #555;
  cursor: pointer;
  padding: 20px 30px;
  border-radius: 15px;
  transition: all 0.3s;
}

.order-link:hover {
  color: #667eea;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8ec 100%);
}

.order-link::before {
  content: '';
  width: 45px;
  height: 45px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8ec 100%);
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 20px;
}

.order-link:nth-child(1)::before {
  content: '💳';
}

.order-link:nth-child(2)::before {
  content: '📦';
}

.order-link:nth-child(3)::before {
  content: '🔄';
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.settings-modal {
  width: 400px;
  background: #fff;
  border-radius: 15px;
  box-shadow: 0 8px 30px rgba(0,0,0,0.15), 0 2px 8px rgba(0,0,0,0.1);
  overflow: hidden;
  border: 1px solid #f0f0f0;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 25px;
  border-bottom: 1px solid #f0f0f0;
}

.modal-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.close-btn {
  font-size: 28px;
  color: #999;
  cursor: pointer;
  line-height: 1;
  transition: color 0.3s;
}

.close-btn:hover {
  color: #666;
}

.modal-body {
  padding: 20px;
}

.settings-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.settings-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 15px 20px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s;
  color: #555;
  font-size: 15px;
}

.settings-item:hover {
  background: #f5f7fa;
}

.settings-item .item-icon {
  font-size: 18px;
}

.settings-item.danger {
  color: #ff4d4f;
}

.settings-item.danger:hover {
  background: #fff2f0;
}

.address-list-modal {
  width: 550px;
  background: #fff;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 8px 30px rgba(0,0,0,0.15), 0 2px 8px rgba(0,0,0,0.1);
  border: 1px solid #f0f0f0;
}

.address-list-modal .modal-header {
  background: linear-gradient(135deg, #6b7280 0%, #4b5563 100%);
  padding: 22px 30px;
  border-bottom: none;
}

.address-list-modal .modal-header h3 {
  color: #f1f5f9;
  font-size: 19px;
  font-weight: 600;
}

.address-list-modal .close-btn {
  color: rgba(255, 255, 255, 0.8);
  font-size: 28px;
}

.address-list-modal .close-btn:hover {
  color: #fff;
}

.address-list-modal .modal-body {
  padding: 25px;
}

.address-list-modal .empty-address {
  text-align: center;
  padding: 50px 30px;
  color: #94a3b8;
}

.address-list-modal .empty-hint {
  display: block;
  margin-top: 10px;
  font-size: 13px;
  color: #cbd5e1;
}

.address-card {
  background: #fff;
  border-radius: 15px;
  padding: 20px;
  margin-bottom: 15px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.06);
}

.address-tag {
  margin-bottom: 12px;
}

.address-tag span {
  font-size: 12px;
  color: #4b5563;
  background: #f3f4f6;
  padding: 4px 12px;
  border-radius: 15px;
}

.address-user {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 10px;
}

.user-name {
  font-size: 17px;
  font-weight: 600;
  color: #1e293b;
}

.user-phone {
  font-size: 15px;
  color: #64748b;
}

.address-full {
  font-size: 14px;
  color: #64748b;
  line-height: 1.6;
}

.address-modal {
  width: 580px;
  max-height: 80vh;
  overflow-y: auto;
  background: #fff;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 8px 30px rgba(0,0,0,0.15), 0 2px 8px rgba(0,0,0,0.1);
  border: 1px solid #f0f0f0;
}

.address-modal .modal-header {
  background: linear-gradient(135deg, #6b7280 0%, #4b5563 100%);
  padding: 22px 30px;
  border-bottom: none;
}

.address-modal .modal-header h3 {
  color: #f1f5f9;
  font-size: 19px;
  font-weight: 600;
}

.address-modal .close-btn {
  color: rgba(255, 255, 255, 0.8);
  font-size: 28px;
}

.address-modal .close-btn:hover {
  color: #fff;
}

.address-body {
  padding: 25px;
}

.add-address-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  width: 100%;
  padding: 18px;
  background: linear-gradient(135deg, #6b7280 0%, #4b5563 100%);
  color: #fff;
  border: none;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  margin-bottom: 25px;
  transition: all 0.3s ease;
}

.add-address-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(75, 85, 99, 0.35);
}

.add-icon {
  font-size: 22px;
  font-weight: 300;
}

.address-modal .empty-address {
  text-align: center;
  padding: 50px 30px;
  color: #94a3b8;
}

.address-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 20px;
  background: #fff;
  border-radius: 12px;
  margin-bottom: 15px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.address-info {
  flex: 1;
}

.address-name {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 6px;
}

.address-phone {
  font-size: 14px;
  color: #64748b;
  margin-bottom: 10px;
}

.address-detail {
  font-size: 14px;
  color: #64748b;
  line-height: 1.6;
}

.address-actions {
  display: flex;
  gap: 12px;
}

.edit-btn, .delete-btn {
  font-size: 14px;
  cursor: pointer;
  padding: 10px 18px;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.edit-btn {
  color: #4b5563;
  background: #f3f4f6;
}

.edit-btn:hover {
  background: #e5e7eb;
}

.delete-btn {
  color: #dc2626;
  background: #fef2f2;
}

.delete-btn:hover {
  background: #fee2e2;
}

.add-address-modal {
  width: 550px;
  background: #fff;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 8px 30px rgba(0,0,0,0.15), 0 2px 8px rgba(0,0,0,0.1);
  border: 1px solid #f0f0f0;
}

.add-address-modal .modal-header {
  background: linear-gradient(135deg, #6b7280 0%, #4b5563 100%);
  padding: 25px 30px;
  border-bottom: none;
}

.add-address-modal .modal-header h3 {
  color: #f1f5f9;
  font-size: 20px;
  font-weight: 600;
}

.add-address-modal .close-btn {
  color: rgba(255, 255, 255, 0.8);
  font-size: 30px;
}

.add-address-modal .close-btn:hover {
  color: #fff;
}

.add-address-modal .modal-body {
  padding: 30px;
}

.add-address-modal .form-group {
  margin-bottom: 25px;
}

.add-address-modal .form-group label {
  display: block;
  margin-bottom: 10px;
  font-size: 15px;
  font-weight: 600;
  color: #4a5568;
}

.add-address-modal .form-input {
  width: 100%;
  height: 48px;
  padding: 0 15px;
  border: 2px solid #e2e8f0;
  border-radius: 12px;
  font-size: 15px;
  transition: all 0.3s ease;
  outline: none;
  box-sizing: border-box;
  background: #fff;
}

.add-address-modal .form-input:focus {
  border-color: #94a3b8;
  box-shadow: 0 0 0 4px rgba(102, 126, 234, 0.1);
}

.region-select {
  display: flex;
  gap: 12px;
}

.region-select-item {
  flex: 1;
  height: 48px;
  padding: 0 12px;
  border: 2px solid #e2e8f0;
  border-radius: 12px;
  outline: none;
  font-size: 14px;
  background: #fff;
  cursor: pointer;
  transition: all 0.3s ease;
}

.region-select-item:focus {
  border-color: #667eea;
  box-shadow: 0 0 0 4px rgba(102, 126, 234, 0.1);
}

.form-textarea {
  width: 100%;
  height: 120px;
  padding: 15px;
  border: 2px solid #e2e8f0;
  border-radius: 12px;
  font-size: 15px;
  resize: none;
  outline: none;
  box-sizing: border-box;
  background: #fff;
  transition: all 0.3s ease;
}

.form-textarea:focus {
  border-color: #667eea;
  box-shadow: 0 0 0 4px rgba(102, 126, 234, 0.1);
}

.btn-group {
  display: flex;
  gap: 15px;
  margin-top: 30px;
}

.save-btn {
  flex: 1;
  height: 50px;
  background: linear-gradient(135deg, #6b7280 0%, #4b5563 100%);
  color: #fff;
  border: none;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.save-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(75, 85, 99, 0.4);
}

.cancel-btn {
  padding: 0 35px;
  height: 50px;
  background: rgba(255, 255, 255, 0.9);
  color: #64748b;
  border: 2px solid #e2e8f0;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.cancel-btn:hover {
  background: #fff;
  border-color: #cbd5e1;
}

.order-modal {
  width: 800px;
  max-height: 85vh;
  overflow-y: auto;
  background: #fff;
  border-radius: 20px;
  overflow-x: hidden;
  box-shadow: 0 8px 30px rgba(0,0,0,0.15), 0 2px 8px rgba(0,0,0,0.1);
  border: 1px solid #f0f0f0;
}

.order-modal .modal-header {
  background: linear-gradient(135deg, #838385);   
  padding: 22px 30px;
  border-bottom: none;
  position: sticky;
  top: 0;
  z-index: 1;
}

.order-modal .modal-header h3 {
  color: #fff;
  font-size: 20px;
  font-weight: 600;
}

.order-modal .close-btn {
  color: rgba(255, 255, 255, 0.8);
  font-size: 28px;
}

.order-modal .close-btn:hover {
  color: #fff;
}

.order-body {
  padding: 25px;
}

.empty-orders {
  text-align: center;
  padding: 60px 30px;
  color: #94a3b8;
  font-size: 16px;
}

.order-card {
  background: #fff;
  border-radius: 15px;
  padding: 25px;
  margin-bottom: 20px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.06);
  border: 1px solid #f0f0f0;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #f0f0f0;
}

.order-no {
  font-size: 14px;
  color: #64748b;
}

.order-status {
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;
}

.order-status.pending {
  background: #fff3cd;
  color: #856404;
}

.order-status.paid {
  background: #d1ecf1;
  color: #0c5460;
}

.order-status.shipped {
  background: #cce5ff;
  color: #004085;
}

.order-status.completed {
  background: #d4edda;
  color: #155724;
}

.order-status.cancelled {
  background: #f8d7da;
  color: #721c24;
}

.order-items {
  margin-bottom: 20px;
}

.order-item {
  display: flex;
  gap: 15px;
  padding: 15px 0;
  border-bottom: 1px solid #f5f5f5;
  cursor: pointer;
  transition: all 0.3s ease;
}

.order-item:hover {
  background: #f8f9fa;
}

.order-item:last-child {
  border-bottom: none;
}

.item-img {
  width: 80px;
  height: 80px;
  object-fit: contain;
  background: #f8f9fa;
  border-radius: 10px;
}

.item-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 8px;
}

.item-name {
  font-size: 15px;
  color: #1e293b;
  font-weight: 500;
}

.item-price {
  font-size: 16px;
  color: #e74c3c;
  font-weight: 600;
}

.item-quantity {
  font-size: 14px;
  color: #64748b;
}

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.order-total {
  font-size: 16px;
  color: #333;
  font-weight: 600;
}

.order-total span {
  color: #e74c3c;
}

.view-detail {
  font-size: 13px;
  color: #667eea;
  align-self: center;
  padding: 6px 12px;
  border: 1px solid #667eea;
  border-radius: 20px;
  transition: all 0.3s ease;
}

.view-detail:hover {
  background: #667eea;
  color: white;
}

.goods-detail-modal {
  width: 600px;
  max-width: 90vw;
}

.goods-detail-body {
  display: flex;
  gap: 25px;
  padding: 20px;
}

.detail-image {
  flex-shrink: 0;
}

.detail-image img {
  width: 200px;
  height: 200px;
  object-fit: contain;
  background: #f8f9fa;
  border-radius: 12px;
}

.detail-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.detail-name {
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
  margin: 0;
}

.detail-price {
  font-size: 24px;
  font-weight: 700;
  color: #ef4444;
  margin: 0;
}

.detail-description {
  font-size: 14px;
  color: #64748b;
  line-height: 1.6;
  margin: 0;
}

.detail-quantity, .detail-category {
  font-size: 14px;
  color: #94a3b8;
  margin: 0;
}

.confirm-receive-btn {
  padding: 10px 25px;
  background: linear-gradient(135deg, #28a745 0%, #20c997 100%);
  color: #fff;
  border: none;
  border-radius: 25px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.confirm-receive-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(40, 167, 69, 0.35);
}

.order-actions {
  display: flex;
  gap: 12px;
}

.refund-apply-btn {
  padding: 10px 25px;
  background: linear-gradient(135deg, #ff7e5f 0%, #feb47b 100%);
  color: #fff;
  border: none;
  border-radius: 25px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.refund-apply-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(255, 126, 95, 0.35);
}

.refund-modal {
  width: 500px;
  background: #fff;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 8px 30px rgba(0,0,0,0.15), 0 2px 8px rgba(0,0,0,0.1);
  border: 1px solid #f0f0f0;
}

.refund-modal .modal-header {
  background: linear-gradient(135deg, #ff7e5f 0%, #feb47b 100%);
  padding: 22px 30px;
  border-bottom: none;
}

.refund-modal .modal-header h3 {
  color: #fff;
  font-size: 19px;
  font-weight: 600;
}

.refund-modal .close-btn {
  color: rgba(255, 255, 255, 0.8);
  font-size: 28px;
}

.refund-modal .close-btn:hover {
  color: #fff;
}

.refund-modal .form-group {
  margin-bottom: 25px;
}

.refund-modal .form-group label {
  display: block;
  margin-bottom: 10px;
  font-size: 15px;
  font-weight: 600;
  color: #4a5568;
}

.refund-modal .form-textarea {
  width: 100%;
  height: 150px;
  padding: 15px;
  border: 2px solid #e2e8f0;
  border-radius: 12px;
  font-size: 15px;
  resize: none;
  outline: none;
  box-sizing: border-box;
  background: #fff;
  transition: all 0.3s ease;
}

.refund-modal .form-textarea:focus {
  border-color: #ff7e5f;
  box-shadow: 0 0 0 4px rgba(255, 126, 95, 0.1);
}

.refund-submit-btn {
  background: linear-gradient(135deg, #ff7e5f 0%, #feb47b 100%);
}

.order-detail-modal {
  width: 650px;
  max-width: 90vw;
  background: #fff;
  border-radius: 15px;
  overflow: hidden;
  box-shadow: 0 8px 30px rgba(0,0,0,0.15);
}

.order-detail-modal .modal-header {
  background: linear-gradient(135deg, #6b7280 0%, #4b5563 100%);
  border-bottom: none;
}

.order-detail-modal .modal-header h3 {
  color: #fff;
  font-size: 19px;
  font-weight: 600;
}

.order-detail-modal .close-btn {
  color: rgba(255, 255, 255, 0.8);
}

.order-detail-modal .close-btn:hover {
  color: #fff;
}

.order-detail-body {
  padding: 25px;
}

.order-info {
  background: #f8fafc;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 25px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #e2e8f0;
}

.info-row:last-child {
  border-bottom: none;
}

.info-label {
  font-size: 14px;
  color: #64748b;
  font-weight: 500;
}

.info-value {
  font-size: 14px;
  color: #1e293b;
  font-weight: 500;
}

.info-value.total {
  color: #e74c3c;
  font-size: 16px;
  font-weight: 600;
}

.status-badge {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;
}

.status-badge.pending {
  background: #fff3cd;
  color: #856404;
}

.status-badge.paid {
  background: #cce5ff;
  color: #004085;
}

.status-badge.shipped {
  background: #d4edda;
  color: #155724;
}

.status-badge.completed {
  background: #d4edda;
  color: #155724;
}

.status-badge.cancelled {
  background: #f8d7da;
  color: #721c24;
}

.order-items-section h4 {
  margin: 0 0 15px 0;
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
}

.order-item-detail {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 15px;
  background: #fafafa;
  border-radius: 12px;
  margin-bottom: 12px;
}

.order-item-detail:last-child {
  margin-bottom: 0;
}

.item-img-detail {
  width: 90px;
  height: 90px;
  object-fit: contain;
  background: #fff;
  border-radius: 10px;
  border: 1px solid #e2e8f0;
}

.item-info-detail {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.item-name-detail {
  font-size: 15px;
  color: #1e293b;
  font-weight: 500;
}

.item-price-detail {
  font-size: 15px;
  color: #e74c3c;
  font-weight: 600;
}

.item-quantity-detail {
  font-size: 13px;
  color: #64748b;
}

.item-subtotal {
  font-size: 16px;
  color: #1e293b;
  font-weight: 600;
  min-width: 80px;
  text-align: right;
}

.order-detail-btn {
  padding: 8px 16px;
  background: #f1f5f9;
  color: #475569;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.order-detail-btn:hover {
  background: #e2e8f0;
  color: #334155;
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
  
  /* 个人中心内容 */
  .profile-content {
    padding: 10px 15px;
    padding-bottom: 80px;
  }
  
  .user-card {
    padding: 20px;
    border-radius: 15px;
  }
  
  .user-info {
    flex-direction: column;
    align-items: center;
    text-align: center;
    gap: 15px;
  }
  
  .avatar {
    width: 80px;
    height: 80px;
    font-size: 28px;
  }
  
  .nickname {
    font-size: 20px;
  }
  
  .user-actions {
    justify-content: space-around;
    gap: 10px;
  }
  
  .action-btn {
    padding: 8px 15px;
    font-size: 14px;
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
