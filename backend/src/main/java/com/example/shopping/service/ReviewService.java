
package com.example.shopping.service;

import com.example.shopping.entity.Review;
import com.example.shopping.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ReviewService {

    private ReviewRepository reviewRepo;

    @Autowired
    public void setReviewRepository(ReviewRepository rr) {
        this.reviewRepo = rr;
    }

    public Review submitNewReview(Long productId, Long customerId, String customerName, Integer stars, String text) {
        Review newReview = new Review();
        
        newReview.setProductId(productId);
        newReview.setCustomerId(customerId);
        newReview.setCustomerName(customerName);
        
        Integer starRating = stars;
        if (starRating == null) {
            starRating = 5;
        }
        newReview.setStarCount(starRating);
        
        String reviewContent = text;
        if (reviewContent == null) {
            reviewContent = "";
        }
        newReview.setReviewText(reviewContent);

        Review saved = reviewRepo.save(newReview);
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Review> listProductReviews(Long productId) {
        if (productId == null) {
            return new ArrayList<Review>();
        }

        List<Review> foundReviews = reviewRepo.findByProductIdOrderBySubmittedAtDesc(productId);
        
        if (foundReviews == null) {
            return Collections.emptyList();
        }

        List<Review> resultList = new ArrayList<>();
        for (Review rev : foundReviews) {
            resultList.add(rev);
        }
        
        return resultList;
    }

    @Transactional(readOnly = true)
    public List<Review> listCustomerReviews(Long customerId) {
        if (customerId == null) {
            return Collections.emptyList();
        }

        List<Review> userReviews = reviewRepo.findByCustomerIdOrderBySubmittedAtDesc(customerId);
        
        if (userReviews == null || userReviews.isEmpty()) {
            return new ArrayList<>();
        }

        return new ArrayList<>(userReviews);
    }

    public void removeReview(Long reviewId) {
        if (reviewId == null) {
            return;
        }

        boolean exists = reviewRepo.existsById(reviewId);
        if (!exists) {
            return;
        }

        reviewRepo.deleteById(reviewId);
    }

    @Transactional(readOnly = true)
    public long countProductReviews(Long productId) {
        if (productId == null) {
            return 0L;
        }
        
        long count = reviewRepo.countReviewsForProduct(productId);
        return count;
    }
}
