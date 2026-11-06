package com.example.shopping.service;

import com.example.shopping.dto.LoginDTO;
import com.example.shopping.dto.RegisterDTO;
import com.example.shopping.entity.Order;
import com.example.shopping.entity.User;
import com.example.shopping.repository.AddressRepository;
import com.example.shopping.repository.CartRepository;
import com.example.shopping.repository.FavoriteRepository;
import com.example.shopping.repository.FeedbackRepository;
import com.example.shopping.repository.OrderItemRepository;
import com.example.shopping.repository.OrderRepository;
import com.example.shopping.repository.ReviewRepository;
import com.example.shopping.repository.UserRepository;
import com.example.shopping.security.JwtUtil;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户服务类
 * 负责处理用户相关的业务逻辑，包括注册、登录、信息管理等功能
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    
    private final PasswordEncoder passwordEncoder;
    
    private final JwtUtil jwtUtil;

    private final OrderRepository orderRepository;
    
    private final OrderItemRepository orderItemRepository;
    
    private final AddressRepository addressRepository;
    
    private final CartRepository cartRepository;
    
    private final FavoriteRepository favoriteRepository;
    
    private final ReviewRepository reviewRepository;
    
    private final FeedbackRepository feedbackRepository;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil,
                       OrderRepository orderRepository, OrderItemRepository orderItemRepository,
                       AddressRepository addressRepository, CartRepository cartRepository,
                       FavoriteRepository favoriteRepository, ReviewRepository reviewRepository,
                       FeedbackRepository feedbackRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.addressRepository = addressRepository;
        this.cartRepository = cartRepository;
        this.favoriteRepository = favoriteRepository;
        this.reviewRepository = reviewRepository;
        this.feedbackRepository = feedbackRepository;
    }

    /**
     * 用户注册功能
     * 1. 检查用户名是否已存在
     * 2. 创建用户实体并加密密码
     * 3. 保存用户到数据库
     * 4. 生成JWT令牌
     * @param registerDTO 注册请求数据
     * @return 包含token和用户信息的Map
     */
    public Map<String, Object> register(RegisterDTO registerDTO) {
        // 检查用户名唯一性
        if (userRepository.existsByUsername(registerDTO.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }

        // 使用Builder模式创建用户实体
        User user = User.builder()
                .username(registerDTO.getUsername())
                .password(passwordEncoder.encode(registerDTO.getPassword()))  // BCrypt加密
                .email(registerDTO.getEmail())
                .phone(registerDTO.getPhone())
                .build();

        // 保存用户到数据库
        userRepository.save(user);

        // 生成JWT令牌
        String token = jwtUtil.generateToken(user.getId(), user.getIsAdmin());

        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", user);
        return result;
    }

    /**
     * 用户登录功能
     * 1. 根据用户名查找用户
     * 2. 检查是否为管理员（管理员不能通过普通入口登录）
     * 3. 验证密码是否正确
     * 4. 检查用户是否被禁用
     * 5. 生成JWT令牌
     * @param loginDTO 登录请求数据
     * @return 包含token和用户信息的Map
     */
    public Map<String, Object> login(LoginDTO loginDTO) {
        // 根据用户名查询用户，不存在则抛出异常
        User user = userRepository.findByUsername(loginDTO.getUsername())
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 管理员不能通过普通登录入口登录
        if (user.getIsAdmin()) {
            throw new RuntimeException("管理员请通过管理员入口登录");
        }

        // 验证密码是否匹配
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        // 检查用户状态
        if (!user.getEnabled()) {
            throw new RuntimeException("用户已被禁用");
        }

        // 生成JWT令牌
        String token = jwtUtil.generateToken(user.getId(), user.getIsAdmin());

        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", user);
        return result;
    }

    /**
     * 根据用户名查找用户
     * @param username 用户名
     * @return 用户实体，如果不存在返回null
     */
    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    /**
     * 根据用户ID获取用户信息
     * @param id 用户ID
     * @return 用户实体
     */
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    /**
     * 更新用户信息
     * 支持更新用户名、密码、邮箱、手机号
     * @param id 用户ID
     * @param registerDTO 更新数据
     * @return 更新后的用户实体
     */
    public User updateUser(Long id, RegisterDTO registerDTO) {
        // 先获取用户
        User user = getUserById(id);

        // 更新用户名（需检查唯一性）
        if (registerDTO.getUsername() != null && !registerDTO.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsername(registerDTO.getUsername())) {
                throw new RuntimeException("用户名已存在");
            }
            user.setUsername(registerDTO.getUsername());
        }

        // 更新密码（非空时）
        if (registerDTO.getPassword() != null && !registerDTO.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        }

        // 更新邮箱
        if (registerDTO.getEmail() != null) {
            user.setEmail(registerDTO.getEmail());
        }

        // 更新手机号
        if (registerDTO.getPhone() != null) {
            user.setPhone(registerDTO.getPhone());
        }

        // 保存并返回更新后的用户
        return userRepository.save(user);
    }

    /**
     * 获取所有用户列表（管理员权限）
     * @return 用户列表
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * 删除用户（注销账号）
     * @param id 用户ID
     */
    @Transactional
    public void deleteUser(Long id) {
        System.out.println("=== 开始删除用户 ID: " + id + " ===");
        
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("用户不存在");
        }
        
        System.out.println("步骤1: 查询用户的订单...");
        List<Order> orders = orderRepository.findByUserId(id);
        System.out.println("找到 " + orders.size() + " 个订单");
        
        for (Order order : orders) {
            System.out.println("  删除订单 " + order.getId() + " 的订单项...");
            orderItemRepository.deleteOrderItemsByOrderId(order.getId());
            System.out.println("  删除订单 " + order.getId() + "...");
            orderRepository.deleteById(order.getId());
        }
        
        System.out.println("步骤2: 删除地址...");
        addressRepository.deleteByUserId(id);
        
        System.out.println("步骤3: 删除购物车...");
        cartRepository.deleteByUserId(id);
        
        System.out.println("步骤4: 删除收藏...");
        favoriteRepository.deleteByUserId(id);
        
        System.out.println("步骤5: 删除评论...");
        reviewRepository.deleteByCustomerId(id);
        
        System.out.println("步骤6: 删除反馈...");
        feedbackRepository.deleteByUserId(id);
        
        System.out.println("步骤7: 删除用户...");
        userRepository.deleteById(id);
        
        System.out.println("=== 用户删除成功 ID: " + id + " ===");
    }

    /**
     * 切换用户启用/禁用状态（管理员权限）
     * @param id 用户ID
     * @return 更新后的用户实体
     */
    public User toggleUserStatus(Long id) {
        User user = getUserById(id);
        user.setEnabled(!user.getEnabled());
        return userRepository.save(user);
    }

    /**
     * 修改用户密码
     * @param id 用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     */
    public void changePassword(Long id, String oldPassword, String newPassword) {
        User user = getUserById(id);
        
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("旧密码错误");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    /**
     * 创建子管理员
     * @param username 用户名
     * @param password 密码
     * @param email 邮箱
     * @param phone 手机号
     * @return 创建的管理员用户
     */
    public User createSubAdmin(String username, String password, String email, String phone) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("用户名已存在");
        }
        
        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .email(email)
                .phone(phone)
                .enabled(true)
                .isAdmin(true)
                .build();
        
        return userRepository.save(user);
    }
}