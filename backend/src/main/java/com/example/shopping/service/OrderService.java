
package com.example.shopping.service;

import com.example.shopping.dto.OrderDetailDTO;
import com.example.shopping.entity.Cart;
import com.example.shopping.entity.Goods;
import com.example.shopping.entity.Order;
import com.example.shopping.entity.OrderItem;
import com.example.shopping.entity.User;
import com.example.shopping.repository.CartRepository;
import com.example.shopping.repository.GoodsRepository;
import com.example.shopping.repository.OrderItemRepository;
import com.example.shopping.repository.OrderRepository;
import com.example.shopping.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;
    private final GoodsRepository goodsRepository;
    private final UserRepository userRepository;

    public OrderService(OrderRepository orderRepository, OrderItemRepository orderItemRepository,
                        CartRepository cartRepository, GoodsRepository goodsRepository,
                        UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.cartRepository = cartRepository;
        this.goodsRepository = goodsRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Order createOrder(Long userId, String shippingName, String shippingPhone, String shippingAddress, List<Long> goodsIds) {
        List<Cart> cartItems = cartRepository.findByUserId(userId);

        if (cartItems.isEmpty()) {
            throw new RuntimeException("购物车为空");
        }

        List<Cart> selectedItems;
        if (goodsIds != null && !goodsIds.isEmpty()) {
            selectedItems = cartItems.stream()
                    .filter(cart -> goodsIds.contains(cart.getGoodsId()))
                    .collect(java.util.stream.Collectors.toList());
        } else {
            selectedItems = cartItems;
        }

        if (selectedItems.isEmpty()) {
            throw new RuntimeException("没有选中任何商品");
        }

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (Cart cart : selectedItems) {
            Goods goods = goodsRepository.findById(cart.getGoodsId())
                    .orElseThrow(() -> new RuntimeException("商品不存在"));
            if (goods.getStock() < cart.getQuantity()) {
                throw new RuntimeException("商品 " + goods.getName() + " 库存不足");
            }
            totalAmount = totalAmount.add(goods.getPrice().multiply(BigDecimal.valueOf(cart.getQuantity())));
        }

        String orderNo = generateOrderNo();

        Order order = Order.builder()
                .userId(userId)
                .orderNo(orderNo)
                .totalAmount(totalAmount)
                .status("pending")
                .shippingName(shippingName)
                .shippingPhone(shippingPhone)
                .shippingAddress(shippingAddress)
                .build();

        order = orderRepository.save(order);

        for (Cart cart : selectedItems) {
            Goods goods = goodsRepository.findById(cart.getGoodsId()).orElse(null);
            
            OrderItem orderItem = OrderItem.builder()
                    .orderId(order.getId())
                    .goodsId(cart.getGoodsId())
                    .goodsName(goods != null ? goods.getName() : "未知商品")
                    .price(goods != null ? goods.getPrice() : BigDecimal.ZERO)
                    .quantity(cart.getQuantity())
                    .build();

            orderItemRepository.save(orderItem);

            if (goods != null) {
                goods.setStock(goods.getStock() - cart.getQuantity());
                goodsRepository.save(goods);
            }

            cartRepository.delete(cart);
        }

        return order;
    }

    public List<OrderDetailDTO> getOrdersByUserId(Long userId) {
        List<Order> orders = orderRepository.findByUserId(userId);
        return orders.stream()
                .map(order -> {
                    List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
                    return OrderDetailDTO.builder()
                            .order(order)
                            .items(items)
                            .build();
                })
                .collect(Collectors.toList());
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc();
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("订单不存在"));
    }

    @Transactional
    public Order createDirectOrder(Long userId, Long goodsId, Integer quantity, String paymentMethod, 
                                   String shippingName, String shippingPhone, String shippingAddress) {
        Goods goods = goodsRepository.findById(goodsId)
                .orElseThrow(() -> new RuntimeException("商品不存在"));

        if (goods.getStock() < quantity) {
            throw new RuntimeException("商品 " + goods.getName() + " 库存不足");
        }

        BigDecimal totalAmount = goods.getPrice().multiply(BigDecimal.valueOf(quantity));

        String orderNo = generateOrderNo();

        Order order = Order.builder()
                .userId(userId)
                .orderNo(orderNo)
                .totalAmount(totalAmount)
                .status("completed")
                .paymentMethod(paymentMethod)
                .shippingName(shippingName)
                .shippingPhone(shippingPhone)
                .shippingAddress(shippingAddress)
                .build();

        order = orderRepository.save(order);

        OrderItem orderItem = OrderItem.builder()
                .orderId(order.getId())
                .goodsId(goodsId)
                .goodsName(goods.getName())
                .price(goods.getPrice())
                .quantity(quantity)
                .build();

        orderItemRepository.save(orderItem);

        goods.setStock(goods.getStock() - quantity);
        goodsRepository.save(goods);

        return order;
    }

    public Order updateOrderStatus(Long id, String status) {
        Order order = getOrderById(id);
        order.setStatus(status);
        return orderRepository.save(order);
    }

    public Order requestRefund(Long orderId, Long userId, String reason) {
        Order order = getOrderById(orderId);
        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("只能申请自己订单的退款");
        }
        if ("refund_pending".equals(order.getRefundStatus()) || 
            "refund_approved".equals(order.getRefundStatus())) {
            throw new RuntimeException("该订单已在退款流程中");
        }
        order.setRefundStatus("refund_pending");
        order.setRefundReason(reason);
        order.setRefundRequestedAt(LocalDateTime.now());
        return orderRepository.save(order);
    }

    public Order confirmReceive(Long orderId, Long userId) {
        Order order = getOrderById(orderId);
        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("只能确认自己订单的收货");
        }
        if (!"shipped".equals(order.getStatus())) {
            throw new RuntimeException("该订单状态不支持确认收货");
        }
        order.setStatus("completed");
        return orderRepository.save(order);
    }

    public Order processRefund(Long orderId, Boolean approved, String note) {
        Order order = getOrderById(orderId);
        if (!"refund_pending".equals(order.getRefundStatus())) {
            throw new RuntimeException("该订单没有待处理的退款申请");
        }
        if (approved) {
            order.setRefundStatus("refund_approved");
        } else {
            order.setRefundStatus("refund_rejected");
        }
        order.setRefundNote(note);
        order.setRefundProcessedAt(LocalDateTime.now());
        return orderRepository.save(order);
    }

    public OrderDetailDTO getOrderDetail(Long orderId) {
        Order order = getOrderById(orderId);
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        User user = userRepository.findById(order.getUserId()).orElse(null);
        return OrderDetailDTO.builder()
                .order(order)
                .items(items)
                .username(user != null ? user.getUsername() : "未知用户")
                .build();
    }

    @Transactional
    public void deleteOrder(Long orderId) {
        // 先删除订单项
        orderItemRepository.deleteOrderItemsByOrderId(orderId);
        
        // 再删除订单
        orderRepository.deleteOrderById(orderId);
    }

    public BigDecimal getTodayRevenue() {
        LocalDateTime todayStart = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime todayEnd = LocalDateTime.now().toLocalDate().atTime(23, 59, 59);
        List<Order> todayOrders = orderRepository.findByCreatedAtBetween(todayStart, todayEnd);
        return todayOrders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public long getTodayOrderCount() {
        LocalDateTime todayStart = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime todayEnd = LocalDateTime.now().toLocalDate().atTime(23, 59, 59);
        return orderRepository.countByCreatedAtBetween(todayStart, todayEnd);
    }

    public long getTotalOrderCount() {
        return orderRepository.count();
    }

    public BigDecimal getTotalRevenue() {
        List<Order> allOrders = orderRepository.findAll();
        return allOrders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<Map<String, Object>> getLastSevenDaysOrders() {
        List<Map<String, Object>> result = new ArrayList<>();
        LocalDate today = LocalDate.now();
        long maxCount = 0;
        
        List<Map<String, Object>> tempData = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = date.atTime(23, 59, 59);
            long count = orderRepository.countByCreatedAtBetween(start, end);
            
            String dayLabel;
            if (i == 0) {
                dayLabel = "今天";
            } else if (i == 1) {
                dayLabel = "昨天";
            } else {
                dayLabel = date.getMonthValue() + "/" + date.getDayOfMonth();
            }
            
            Map<String, Object> item = new HashMap<>();
            item.put("day", dayLabel);
            item.put("count", count);
            tempData.add(item);
            
            if (count > maxCount) {
                maxCount = count;
            }
        }
        
        for (Map<String, Object> item : tempData) {
            long count = (long) item.get("count");
            int percent = maxCount > 0 ? (int) (count * 100 / maxCount) : 0;
            if (percent == 0 && count > 0) {
                percent = 10;
            }
            item.put("percent", percent);
            result.add(item);
        }
        
        return result;
    }

    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.format("%04d", (int) (Math.random() * 10000));
        return "ORD" + timestamp + random;
    }
}
