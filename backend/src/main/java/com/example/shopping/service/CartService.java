
package com.example.shopping.service;

import com.example.shopping.dto.CartItemDTO;
import com.example.shopping.entity.Cart;
import com.example.shopping.entity.Goods;
import com.example.shopping.repository.CartRepository;
import com.example.shopping.repository.GoodsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final GoodsRepository goodsRepository;

    public CartService(CartRepository cartRepository, GoodsRepository goodsRepository) {
        this.cartRepository = cartRepository;
        this.goodsRepository = goodsRepository;
    }

    public List<CartItemDTO> getCartDetailsByUserId(Long userId) {
        List<Cart> cartItems = cartRepository.findByUserId(userId);
        List<CartItemDTO> result = new ArrayList<>();

        for (Cart cart : cartItems) {
            Optional<Goods> goodsOpt = goodsRepository.findById(cart.getGoodsId());
            if (goodsOpt.isPresent()) {
                Goods goods = goodsOpt.get();
                CartItemDTO dto = CartItemDTO.builder()
                        .id(cart.getId())
                        .userId(cart.getUserId())
                        .goodsId(cart.getGoodsId())
                        .quantity(cart.getQuantity())
                        .name(goods.getName())
                        .price(goods.getPrice())
                        .image(goods.getImage())
                        .category(goods.getCategory())
                        .stock(goods.getStock())
                        .build();
                result.add(dto);
            }
        }

        return result;
    }

    public List<Cart> getCartByUserId(Long userId) {
        return cartRepository.findByUserId(userId);
    }

    public Cart addToCart(Long userId, Long goodsId, Integer quantity) {
        Cart cart = cartRepository.findByUserIdAndGoodsId(userId, goodsId)
                .orElse(null);

        if (cart != null) {
            cart.setQuantity(cart.getQuantity() + quantity);
        } else {
            cart = Cart.builder()
                    .userId(userId)
                    .goodsId(goodsId)
                    .quantity(quantity)
                    .build();
        }

        return cartRepository.save(cart);
    }

    public Cart updateCart(Long userId, Long goodsId, Integer quantity) {
        Cart cart = cartRepository.findByUserIdAndGoodsId(userId, goodsId)
                .orElseThrow(() -> new RuntimeException("购物车商品不存在"));

        if (quantity <= 0) {
            cartRepository.delete(cart);
            return null;
        }

        cart.setQuantity(quantity);
        return cartRepository.save(cart);
    }

    @Transactional
    public void removeFromCart(Long userId, Long goodsId) {
        cartRepository.deleteByUserIdAndGoodsId(userId, goodsId);
    }

    @Transactional
    public void clearCart(Long userId) {
        cartRepository.deleteByUserId(userId);
    }
}

