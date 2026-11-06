import { createRouter, createWebHistory } from "vue-router";
import Login from "../components/Login.vue";
import Register from "../components/Register.vue";
import Home from "../components/home.vue";
import Product from "../components/product.vue";
import CartPage from "../components/CartPage.vue";
import Favor from "../components/favor.vue";
import Help from "../components/help.vue";
import Damin from "../components/damin.vue";
import ProductManagePage from "../components/prod.vue";
import Order from "../components/order.vue";
import User from "../components/user.vue";
import Data from "../components/data.vue";
import AdminLogin from "../components/AdminLogin.vue";
import Feedback from "../components/feedback.vue";

const routes = [
  { path: "/", redirect: "/home" },
  { path: "/login", name: "login", component: Login },
  { path: "/register", name: "register", component: Register },
  { path: "/home", name: "home", component: Home },
  { path: "/profile", name: "profile", component: Product },
  { path: "/cart", name: "cart", component: CartPage },
  { path: "/favorites", name: "favorites", component: Favor },
  { path: "/help", name: "help", component: Help },
  { path: "/admin/login", name: "adminLogin", component: AdminLogin },
  { path: "/admin", name: "admin", component: Damin },
  { path: "/admin/product", name: "productManage", component: ProductManagePage },
  { path: "/admin/order", name: "orderManage", component: Order },
  { path: "/admin/user", name: "userManage", component: User },
  { path: "/admin/data", name: "dataOverview", component: Data },
  { path: "/admin/feedback", name: "feedbackManage", component: Feedback },
];

const router = createRouter({
  // 使用构建基路径，部署在子目录（如 GitHub Pages 的 /仓库名/）时依然可用
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
});

// 路由守卫：保护管理员页面
router.beforeEach((to, from) => {
  const isAdmin = localStorage.getItem('isAdmin') === 'true';
  
  // 访问管理员页面需要管理员权限
  if (to.path.startsWith('/admin')) {
    if (to.path === '/admin/login') {
      // 管理员登录页面允许访问
      return true;
    } else if (isAdmin) {
      // 已登录管理员可以访问
      return true;
    } else {
      // 非管理员重定向到管理员登录
      return '/admin/login';
    }
  } else {
    return true;
  }
});

export default router;
