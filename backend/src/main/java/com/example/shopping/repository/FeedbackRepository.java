
package com.example.shopping.repository;

import com.example.shopping.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    
    List<Feedback> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    List<Feedback> findByIsProcessedOrderByCreatedAtDesc(Boolean isProcessed);
    
    List<Feedback> findAllByOrderByCreatedAtDesc();
    
    @Modifying
    @Query("DELETE FROM Feedback f WHERE f.userId = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
