
package com.example.shopping.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CartDTO {

    @NotNull(message = "商品ID不能为空")
    private Long goodsId;

    @Positive(message = "数量必须为正整数")
    private Integer quantity = 1;
}
