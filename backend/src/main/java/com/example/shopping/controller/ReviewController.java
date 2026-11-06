
package com.example.shopping.controller;

import com.example.shopping.dto.ResponseDTO;
import com.example.shopping.entity.Review;
import com.example.shopping.entity.User;
import com.example.shopping.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(path = "/api/reviews", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin(origins = "*", maxAge = 3600)
public class ReviewController {

    private ReviewService reviewService;

    @Autowired
    public void setReviewService(ReviewService rs) {
        this.reviewService = rs;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseDTO<Review>> submitReview(
            @AuthenticationPrincipal User loggedInUser,
            @RequestBody Map<String, Object> payload) {

        HttpStatus responseStatus;
        ResponseDTO<Review> resp;

        try {
            if (loggedInUser == null) {
                resp = ResponseDTO.error("需要登录后才可评价");
                responseStatus = HttpStatus.UNAUTHORIZED;
            } else {
                Long goodsIdValue = payload.get("goodsId") != null ? 
                    ((Number) payload.get("goodsId")).longValue() : null;
                
                Integer ratingValue = payload.get("rating") != null ? 
                    ((Number) payload.get("rating")).intValue() : null;

                String contentText = payload.get("content") != null ? 
                    payload.get("content").toString() : "";

                Integer starValue = ratingValue;
                if (starValue == null || starValue < 1 || starValue > 5) {
                    starValue = 5;
                }

                Review savedReview = reviewService.submitNewReview(
                    goodsIdValue,
                    loggedInUser.getId(),
                    loggedInUser.getUsername(),
                    starValue,
                    contentText
                );

                resp = ResponseDTO.success("评价已提交", savedReview);
                responseStatus = HttpStatus.OK;
            }
        }
        catch (Exception anyException) {
            anyException.printStackTrace();
            String errorMsg = anyException.getLocalizedMessage();
            if (errorMsg == null || errorMsg.trim().isEmpty()) {
                errorMsg = "提交失败";
            }
            resp = ResponseDTO.error(errorMsg);
            responseStatus = HttpStatus.BAD_REQUEST;
        }

        return new ResponseEntity<>(resp, responseStatus);
    }

    @GetMapping(path = "/goods/{goodsIdVal}")
    public ResponseEntity<ResponseDTO<List<Review>>> fetchProductReviews(
            @PathVariable("goodsIdVal") Long goodsId) {

        Long productId = goodsId;
        if (productId == null) {
            return ResponseEntity.ok(ResponseDTO.success(java.util.Collections.emptyList()));
        }

        List<Review> reviewsFound = reviewService.listProductReviews(productId);
        
        if (reviewsFound == null) {
            reviewsFound = java.util.Collections.emptyList();
        }

        return ResponseEntity.ok(ResponseDTO.success(reviewsFound));
    }

    @GetMapping(path = "/mine")
    public ResponseEntity<ResponseDTO<List<Review>>> fetchMyReviews(
            @AuthenticationPrincipal User activeUser) {

        if (activeUser == null) {
            ResponseDTO<List<Review>> errorResp = ResponseDTO.error("请先登录");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResp);
        }

        Long userId = activeUser.getId();
        List<Review> userReviews = reviewService.listCustomerReviews(userId);

        if (userReviews == null) {
            userReviews = java.util.Collections.emptyList();
        }

        return ResponseEntity.ok(ResponseDTO.success(userReviews));
    }

    @DeleteMapping(path = "/{reviewIdVal}")
    public ResponseEntity<ResponseDTO<Void>> dropReview(
            @AuthenticationPrincipal User activeUser,
            @PathVariable("reviewIdVal") Long whichReview) {

        if (activeUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ResponseDTO.error("请先登录"));
        }

        reviewService.removeReview(whichReview);
        return ResponseEntity.ok(ResponseDTO.success("已移除", null));
    }
}
