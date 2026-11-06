
package com.example.shopping.service;

import com.example.shopping.entity.Favorite;
import com.example.shopping.repository.FavoriteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;

    public FavoriteService(FavoriteRepository favoriteRepository) {
        this.favoriteRepository = favoriteRepository;
    }

    public List<Favorite> getFavoritesByUserId(Long userId) {
        return favoriteRepository.findByUserId(userId);
    }

    @Transactional
    public Favorite addFavorite(Long userId, Long goodsId) {
        if (favoriteRepository.existsByUserIdAndGoodsId(userId, goodsId)) {
            throw new RuntimeException("该商品已收藏");
        }

        Favorite favorite = Favorite.builder()
                .userId(userId)
                .goodsId(goodsId)
                .build();

        return favoriteRepository.save(favorite);
    }

    @Transactional
    public void removeFavorite(Long userId, Long goodsId) {
        favoriteRepository.deleteByUserIdAndGoodsId(userId, goodsId);
    }
}
