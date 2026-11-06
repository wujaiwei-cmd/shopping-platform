
package com.example.shopping.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemDTO {
    private Long id;
    private Long userId;
    private Long goodsId;
    private Integer quantity;
    private String name;
    private BigDecimal price;
    private String image;
    private String category;
    private Integer stock;
}
