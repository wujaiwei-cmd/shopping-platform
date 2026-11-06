
package com.example.shopping.repository;

import com.example.shopping.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    List<Favorite> findByUserId(Long userId);

    Optional<Favorite> findByUserIdAndGoodsId(Long userId, Long goodsId);

    void deleteByUserIdAndGoodsId(Long userId, Long goodsId);

    boolean existsByUserIdAndGoodsId(Long userId, Long goodsId);

    @Modifying
    @Query("DELETE FROM Favorite f WHERE f.userId = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
