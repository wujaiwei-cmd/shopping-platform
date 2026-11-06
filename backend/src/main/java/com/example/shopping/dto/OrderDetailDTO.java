
package com.example.shopping.dto;

import com.example.shopping.entity.Order;
import com.example.shopping.entity.OrderItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDetailDTO {
    private Order order;
    private List<OrderItem> items;
    private String username;
}
