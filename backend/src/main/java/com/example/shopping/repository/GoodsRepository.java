
package com.example.shopping.repository;

import com.example.shopping.entity.Goods;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GoodsRepository extends JpaRepository<Goods, Long> {

    List<Goods> findByStatus(String status);

    List<Goods> findByCategory(String category);

    List<Goods> findByStatusAndCategory(String status, String category);

    @Query("SELECT g FROM Goods g WHERE g.status = 'active' AND " +
           "(LOWER(g.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(g.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Goods> searchByKeyword(@Param("keyword") String keyword);
}
