package com.example.shopping.repository;

import com.example.shopping.entity.Review;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Query("SELECT r FROM Review r WHERE r.productId = :productId")
    List<Review> findAllForProduct(@Param("productId") Long productId, Sort sort);

    default List<Review> findByProductIdOrderBySubmittedAtDesc(Long productId) {
        return findAllForProduct(productId, Sort.by(Sort.Direction.DESC, "submittedAt"));
    }

    @Query("SELECT COUNT(r) FROM Review r WHERE r.productId = :productId")
    long countReviewsForProduct(@Param("productId") Long productId);

    List<Review> findByCustomerId(Long customerId, Sort sort);

    default List<Review> findByCustomerIdOrderBySubmittedAtDesc(Long customerId) {
        return findByCustomerId(customerId, Sort.by(Sort.Direction.DESC, "submittedAt"));
    }

    @Modifying
    @Query("DELETE FROM Review r WHERE r.customerId = :customerId")
    void deleteByCustomerId(@Param("customerId") Long customerId);
}
