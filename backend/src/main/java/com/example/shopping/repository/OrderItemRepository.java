
package com.example.shopping.repository;

import com.example.shopping.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrderId(Long orderId);

    @Modifying
    @Query(value = "DELETE FROM order_item WHERE order_id = :orderId", nativeQuery = true)
    void deleteOrderItemsByOrderId(@Param("orderId") Long orderId);

    @Modifying
    @Query(value = "DELETE FROM order_item WHERE order_id IN (SELECT id FROM orders WHERE user_id = :userId)", nativeQuery = true)
    void deleteOrderItemsByUserId(@Param("userId") Long userId);
}
