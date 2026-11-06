
package com.example.shopping.controller;

import com.example.shopping.dto.ResponseDTO;
import com.example.shopping.entity.Favorite;
import com.example.shopping.entity.User;
import com.example.shopping.service.FavoriteService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favorites")
@CrossOrigin(origins = "*")
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @GetMapping
    public ResponseEntity<ResponseDTO<List<Favorite>>> getFavorites(@AuthenticationPrincipal User user) {
        List<Favorite> favorites = favoriteService.getFavoritesByUserId(user.getId());
        return ResponseEntity.ok(ResponseDTO.success(favorites));
    }

    @PostMapping("/{goodsId}")
    public ResponseEntity<ResponseDTO<Favorite>> addFavorite(@AuthenticationPrincipal User user, 
                                                           @PathVariable Long goodsId) {
        try {
            Favorite favorite = favoriteService.addFavorite(user.getId(), goodsId);
            return ResponseEntity.ok(ResponseDTO.success("收藏成功", favorite));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ResponseDTO.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{goodsId}")
    public ResponseEntity<ResponseDTO<Void>> removeFavorite(@AuthenticationPrincipal User user, 
                                                          @PathVariable Long goodsId) {
        favoriteService.removeFavorite(user.getId(), goodsId);
        return ResponseEntity.ok(ResponseDTO.success("取消收藏成功"));
    }
}
