
package com.example.shopping.controller;

import com.example.shopping.dto.CartDTO;
import com.example.shopping.dto.CartItemDTO;
import com.example.shopping.dto.ResponseDTO;
import com.example.shopping.entity.Cart;
import com.example.shopping.entity.User;
import com.example.shopping.service.CartService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "*")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ResponseEntity<ResponseDTO<List<CartItemDTO>>> getCart(@AuthenticationPrincipal User user) {
        List<CartItemDTO> cartItems = cartService.getCartDetailsByUserId(user.getId());
        return ResponseEntity.ok(ResponseDTO.success(cartItems));
    }

    @PostMapping
    public ResponseEntity<ResponseDTO<Cart>> addToCart(@AuthenticationPrincipal User user, 
                                                      @Valid @RequestBody CartDTO cartDTO) {
        try {
            Cart cart = cartService.addToCart(user.getId(), cartDTO.getGoodsId(), 
                    cartDTO.getQuantity() != null ? cartDTO.getQuantity() : 1);
            return ResponseEntity.ok(ResponseDTO.success("添加成功", cart));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ResponseDTO.error(e.getMessage()));
        }
    }

    @PutMapping
    public ResponseEntity<ResponseDTO<Cart>> updateCart(@AuthenticationPrincipal User user, 
                                                       @Valid @RequestBody CartDTO cartDTO) {
        try {
            Cart cart = cartService.updateCart(user.getId(), cartDTO.getGoodsId(), 
                    cartDTO.getQuantity() != null ? cartDTO.getQuantity() : 1);
            return ResponseEntity.ok(ResponseDTO.success("更新成功", cart));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ResponseDTO.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{goodsId}")
    public ResponseEntity<ResponseDTO<Void>> removeFromCart(@AuthenticationPrincipal User user, 
                                                           @PathVariable Long goodsId) {
        cartService.removeFromCart(user.getId(), goodsId);
        return ResponseEntity.ok(ResponseDTO.success("删除成功"));
    }

    @DeleteMapping
    public ResponseEntity<ResponseDTO<Void>> clearCart(@AuthenticationPrincipal User user) {
        cartService.clearCart(user.getId());
        return ResponseEntity.ok(ResponseDTO.success("购物车已清空"));
    }
}

