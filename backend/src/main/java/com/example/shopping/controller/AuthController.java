package com.example.shopping.controller;

import com.example.shopping.dto.LoginDTO;
import com.example.shopping.dto.RegisterDTO;
import com.example.shopping.dto.ResponseDTO;
import com.example.shopping.entity.User;
import com.example.shopping.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 认证控制器
 * 处理用户注册、登录、信息管理、账号注销等请求
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    // 用户服务层
    private final UserService userService;

    /**
     * 构造函数依赖注入
     * @param userService 用户服务类
     */
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 用户注册接口
     * POST /api/auth/register
     * @param registerDTO 注册表单数据
     * @return 注册结果
     */
    @PostMapping("/register")
    public ResponseEntity<ResponseDTO<Map<String, Object>>> register(@Valid @RequestBody RegisterDTO registerDTO) {
        try {
            // 调用服务层处理注册逻辑
            Map<String, Object> result = userService.register(registerDTO);
            return ResponseEntity.ok(ResponseDTO.success("注册成功", result));
        } catch (RuntimeException e) {
            // 处理异常情况
            return ResponseEntity.badRequest().body(ResponseDTO.error(e.getMessage()));
        }
    }

    /**
     * 用户登录接口
     * POST /api/auth/login
     * @param loginDTO 登录表单数据
     * @return 登录结果（包含token和用户信息）
     */
    @PostMapping("/login")
    public ResponseEntity<ResponseDTO<Map<String, Object>>> login(@Valid @RequestBody LoginDTO loginDTO) {
        try {
            // 调用服务层处理登录逻辑
            Map<String, Object> result = userService.login(loginDTO);
            return ResponseEntity.ok(ResponseDTO.success("登录成功", result));
        } catch (RuntimeException e) {
            // 处理登录失败情况
            return ResponseEntity.badRequest().body(ResponseDTO.error(e.getMessage()));
        }
    }

    /**
     * 获取当前登录用户信息
     * GET /api/auth/me
     * 需要用户已登录（携带有效token）
     * @param user 当前认证用户（由Spring Security自动注入）
     * @return 用户信息
     */
    @GetMapping("/me")
    public ResponseEntity<ResponseDTO<User>> getCurrentUser(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ResponseDTO.success(user));
    }

    /**
     * 更新当前登录用户信息
     * PUT /api/auth/me
     * 需要用户已登录
     * @param currentUser 当前认证用户
     * @param registerDTO 更新数据
     * @return 更新后的用户信息
     */
    @PutMapping("/me")
    public ResponseEntity<ResponseDTO<User>> updateUser(@AuthenticationPrincipal User currentUser, 
                                                       @Valid @RequestBody RegisterDTO registerDTO) {
        try {
            // 调用服务层更新用户信息
            User updated = userService.updateUser(currentUser.getId(), registerDTO);
            return ResponseEntity.ok(ResponseDTO.success("更新成功", updated));
        } catch (RuntimeException e) {
            // 处理更新失败情况
            return ResponseEntity.badRequest().body(ResponseDTO.error(e.getMessage()));
        }
    }

    /**
     * 注销账号（删除当前用户）
     * DELETE /api/auth/delete
     * 需要用户已登录
     * @param currentUser 当前认证用户
     * @return 操作结果
     */
    @DeleteMapping("/delete")
    public ResponseEntity<ResponseDTO<Void>> deleteAccount(@AuthenticationPrincipal User currentUser) {
        try {
            // 调用服务层删除用户
            userService.deleteUser(currentUser.getId());
            return ResponseEntity.ok(ResponseDTO.success("账号注销成功", null));
        } catch (RuntimeException e) {
            // 处理删除失败情况
            return ResponseEntity.badRequest().body(ResponseDTO.error(e.getMessage()));
        }
    }
}