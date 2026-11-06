
package com.example.shopping.controller;

import com.example.shopping.dto.LoginDTO;
import com.example.shopping.dto.OrderDetailDTO;
import com.example.shopping.dto.ResponseDTO;
import com.example.shopping.entity.Feedback;
import com.example.shopping.entity.Order;
import com.example.shopping.entity.User;
import com.example.shopping.security.JwtUtil;
import com.example.shopping.service.FeedbackService;
import com.example.shopping.service.OrderService;
import com.example.shopping.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    private final UserService userService;
    private final OrderService orderService;
    private final FeedbackService feedbackService;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;

    public AdminController(UserService userService, OrderService orderService, FeedbackService feedbackService, JwtUtil jwtUtil, BCryptPasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.orderService = orderService;
        this.feedbackService = feedbackService;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<ResponseDTO> adminLogin(@RequestBody LoginDTO loginDTO) {
        User user = userService.findByUsername(loginDTO.getUsername());
        
        if (user == null) {
            return ResponseEntity.badRequest().body(ResponseDTO.error("管理员账号不存在"));
        }
        
        if (!user.getIsAdmin()) {
            return ResponseEntity.badRequest().body(ResponseDTO.error("该账号不是管理员"));
        }
        
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            return ResponseEntity.badRequest().body(ResponseDTO.error("密码错误"));
        }
        
        String token = jwtUtil.generateToken(user.getId(), true);
        
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        Map<String, Object> userData = new HashMap<>();
        userData.put("id", user.getId());
        userData.put("username", user.getUsername());
        userData.put("email", user.getEmail());
        userData.put("isAdmin", true);
        data.put("user", userData);
        
        return ResponseEntity.ok(ResponseDTO.success(data));
    }

    @GetMapping("/users")
    public ResponseEntity<ResponseDTO<List<User>>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(ResponseDTO.success(users));
    }

    @PutMapping("/users/{id}/status")
    public ResponseEntity<ResponseDTO<User>> toggleUserStatus(@PathVariable Long id) {
        try {
            User user = userService.getUserById(id);
            if ("admin".equals(user.getUsername())) {
                return ResponseEntity.badRequest().body(ResponseDTO.error("主管理员不可禁用"));
            }
            user = userService.toggleUserStatus(id);
            return ResponseEntity.ok(ResponseDTO.success("用户状态已更新", user));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ResponseDTO.error(e.getMessage()));
        }
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<ResponseDTO<Void>> deleteUser(@PathVariable Long id) {
        try {
            User user = userService.getUserById(id);
            if ("admin".equals(user.getUsername())) {
                return ResponseEntity.badRequest().body(ResponseDTO.error("主管理员不可删除"));
            }
            userService.deleteUser(id);
            return ResponseEntity.ok(ResponseDTO.success("用户已删除"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ResponseDTO.error(e.getMessage()));
        }
    }

    @GetMapping("/orders")
    public ResponseEntity<ResponseDTO<List<Order>>> getAllOrders() {
        List<Order> orders = orderService.getAllOrders();
        return ResponseEntity.ok(ResponseDTO.success(orders));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ResponseDTO<Map<String, Object>>> getDashboardData() {
        Map<String, Object> data = new HashMap<>();
        data.put("todayOrders", orderService.getTodayOrderCount());
        data.put("todayRevenue", orderService.getTodayRevenue());
        data.put("totalOrders", orderService.getTotalOrderCount());
        data.put("totalRevenue", orderService.getTotalRevenue());
        data.put("totalUsers", userService.getAllUsers().size());
        data.put("lastSevenDays", orderService.getLastSevenDaysOrders());
        return ResponseEntity.ok(ResponseDTO.success(data));
    }

    @PutMapping("/orders/{id}/status")
    public ResponseEntity<ResponseDTO<Order>> updateOrderStatus(@PathVariable Long id, @RequestParam String status) {
        try {
            Order order = orderService.updateOrderStatus(id, status);
            return ResponseEntity.ok(ResponseDTO.success("订单状态已更新", order));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ResponseDTO.error(e.getMessage()));
        }
    }

    @PutMapping("/orders/{id}/refund")
    public ResponseEntity<ResponseDTO<Order>> processRefund(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        try {
            Boolean approved = (Boolean) body.get("approved");
            String note = (String) body.get("note");
            if (approved == null) {
                return ResponseEntity.badRequest().body(ResponseDTO.error("请指定是否通过退款"));
            }
            Order order = orderService.processRefund(id, approved, note);
            return ResponseEntity.ok(ResponseDTO.success(approved ? "已通过退款" : "已拒绝退款", order));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ResponseDTO.error(e.getMessage()));
        }
    }

    @GetMapping("/orders/{id}/detail")
    public ResponseEntity<ResponseDTO<OrderDetailDTO>> getOrderDetail(@PathVariable Long id) {
        try {
            OrderDetailDTO detail = orderService.getOrderDetail(id);
            return ResponseEntity.ok(ResponseDTO.success(detail));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ResponseDTO.error(e.getMessage()));
        }
    }

    @DeleteMapping("/orders/{id}")
    public ResponseEntity<ResponseDTO<Void>> deleteOrder(@PathVariable Long id) {
        try {
            System.out.println("正在删除订单 ID: " + id);
            orderService.deleteOrder(id);
            System.out.println("订单删除成功");
            return ResponseEntity.ok(ResponseDTO.success("订单已删除"));
        } catch (Exception e) {
            System.out.println("订单删除失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(ResponseDTO.error(e.getMessage()));
        }
    }

    @PutMapping("/password")
    public ResponseEntity<ResponseDTO<Void>> changePassword(
            @AuthenticationPrincipal User currentUser,
            @RequestBody Map<String, String> passwordData) {
        try {
            String oldPassword = passwordData.get("oldPassword");
            String newPassword = passwordData.get("newPassword");
            
            if (oldPassword == null || newPassword == null || oldPassword.isEmpty() || newPassword.isEmpty()) {
                return ResponseEntity.badRequest().body(ResponseDTO.error("旧密码和新密码都不能为空"));
            }
            
            userService.changePassword(currentUser.getId(), oldPassword, newPassword);
            return ResponseEntity.ok(ResponseDTO.success("密码修改成功"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ResponseDTO.error(e.getMessage()));
        }
    }

    @PostMapping("/subadmin")
    public ResponseEntity<ResponseDTO<User>> createSubAdmin(@RequestBody Map<String, String> adminData) {
        try {
            String username = adminData.get("username");
            String password = adminData.get("password");
            String email = adminData.get("email");
            String phone = adminData.get("phone");
            
            if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
                return ResponseEntity.badRequest().body(ResponseDTO.error("用户名和密码不能为空"));
            }
            
            if (password.length() < 6) {
                return ResponseEntity.badRequest().body(ResponseDTO.error("密码长度不能少于6位"));
            }
            
            User admin = userService.createSubAdmin(username, password, email, phone);
            return ResponseEntity.ok(ResponseDTO.success("子管理员创建成功", admin));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ResponseDTO.error(e.getMessage()));
        }
    }

    @GetMapping("/feedbacks")
    public ResponseEntity<ResponseDTO<List<Feedback>>> getAllFeedbacks() {
        List<Feedback> feedbacks = feedbackService.getAllFeedbacks();
        return ResponseEntity.ok(ResponseDTO.success(feedbacks));
    }

    @PutMapping("/feedbacks/{id}/process")
    public ResponseEntity<ResponseDTO<Feedback>> processFeedback(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> noteData) {
        try {
            String note = noteData != null ? noteData.get("note") : null;
            Feedback feedback = feedbackService.markAsProcessed(id, note);
            return ResponseEntity.ok(ResponseDTO.success("反馈已处理", feedback));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ResponseDTO.error(e.getMessage()));
        }
    }

    @DeleteMapping("/feedbacks/{id}")
    public ResponseEntity<ResponseDTO<Void>> deleteFeedback(@PathVariable Long id) {
        try {
            feedbackService.deleteFeedback(id);
            return ResponseEntity.ok(ResponseDTO.success("反馈已删除"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ResponseDTO.error(e.getMessage()));
        }
    }
}
