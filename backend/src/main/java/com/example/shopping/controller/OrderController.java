
package com.example.shopping.controller;

import com.example.shopping.dto.ResponseDTO;
import com.example.shopping.entity.Order;
import com.example.shopping.entity.User;
import com.example.shopping.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<ResponseDTO<List<com.example.shopping.dto.OrderDetailDTO>>> getOrders(@AuthenticationPrincipal User user) {
        List<com.example.shopping.dto.OrderDetailDTO> orders = orderService.getOrdersByUserId(user.getId());
        return ResponseEntity.ok(ResponseDTO.success(orders));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseDTO<Order>> getOrderById(@PathVariable Long id) {
        try {
            Order order = orderService.getOrderById(id);
            return ResponseEntity.ok(ResponseDTO.success(order));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ResponseDTO.error(e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<ResponseDTO<Order>> createOrder(@AuthenticationPrincipal User user,
                                                        @RequestParam String shippingName,
                                                        @RequestParam String shippingPhone,
                                                        @RequestParam String shippingAddress,
                                                        @RequestParam(required = false) List<Long> goodsIds) {
        try {
            Order order = orderService.createOrder(user.getId(), shippingName, shippingPhone, shippingAddress, goodsIds);
            return ResponseEntity.ok(ResponseDTO.success("订单创建成功", order));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ResponseDTO.error(e.getMessage()));
        }
    }

    @PostMapping("/direct")
    public ResponseEntity<ResponseDTO<Order>> createDirectOrder(@AuthenticationPrincipal User user,
                                                              @RequestParam Long goodsId,
                                                              @RequestParam(defaultValue = "1") Integer quantity,
                                                              @RequestParam(defaultValue = "wechat") String paymentMethod,
                                                              @RequestParam(required = false) String shippingName,
                                                              @RequestParam(required = false) String shippingPhone,
                                                              @RequestParam(required = false) String shippingAddress) {
        try {
            // 验证用户不是管理员
            if (user.getIsAdmin()) {
                return ResponseEntity.badRequest().body(ResponseDTO.error("管理员不能购买商品"));
            }
            
            System.out.println("订单创建用户: " + user.getId() + " - " + user.getUsername());
            
            Order order = orderService.createDirectOrder(user.getId(), goodsId, quantity, paymentMethod, 
                    shippingName, shippingPhone, shippingAddress);
            return ResponseEntity.ok(ResponseDTO.success("支付成功", order));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ResponseDTO.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ResponseDTO<Order>> updateOrderStatus(@PathVariable Long id, 
                                                              @RequestParam String status) {
        try {
            Order order = orderService.updateOrderStatus(id, status);
            return ResponseEntity.ok(ResponseDTO.success("状态更新成功", order));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ResponseDTO.error(e.getMessage()));
        }
    }

    @PostMapping("/{id}/refund")
    public ResponseEntity<ResponseDTO<Order>> requestRefund(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        try {
            String reason = body.get("reason");
            if (reason == null || reason.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(ResponseDTO.error("请填写退款原因"));
            }
            Order order = orderService.requestRefund(id, user.getId(), reason);
            return ResponseEntity.ok(ResponseDTO.success("退款申请已提交", order));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ResponseDTO.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}/receive")
    public ResponseEntity<ResponseDTO<Order>> confirmReceive(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        try {
            Order order = orderService.confirmReceive(id, user.getId());
            return ResponseEntity.ok(ResponseDTO.success("确认收货成功", order));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ResponseDTO.error(e.getMessage()));
        }
    }
}
