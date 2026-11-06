
package com.example.shopping.controller;

import com.example.shopping.dto.ResponseDTO;
import com.example.shopping.entity.Feedback;
import com.example.shopping.entity.User;
import com.example.shopping.service.FeedbackService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/feedback")
@CrossOrigin(origins = "*")
public class FeedbackController {

    private final FeedbackService feedbackService;

    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @PostMapping
    public ResponseEntity<ResponseDTO<Feedback>> createFeedback(
            @AuthenticationPrincipal User user,
            @RequestBody Map<String, String> feedbackData) {
        try {
            String type = feedbackData.get("type");
            String title = feedbackData.get("title");
            String content = feedbackData.get("content");
            String contact = feedbackData.get("contact");

            if (type == null || title == null || content == null) {
                return ResponseEntity.badRequest().body(ResponseDTO.error("类型、标题和内容都不能为空"));
            }

            Feedback feedback = new Feedback();
            feedback.setType(type);
            feedback.setTitle(title);
            feedback.setContent(content);
            feedback.setContact(contact);

            if (user != null) {
                feedback.setUserId(user.getId());
                feedback.setUsername(user.getUsername());
            }

            feedback = feedbackService.createFeedback(feedback);
            return ResponseEntity.ok(ResponseDTO.success("提交成功", feedback));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ResponseDTO.error(e.getMessage()));
        }
    }

    @GetMapping("/my")
    public ResponseEntity<ResponseDTO<List<Feedback>>> getMyFeedbacks(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(401).body(ResponseDTO.unauthorized("请先登录"));
        }
        List<Feedback> feedbacks = feedbackService.getFeedbacksByUserId(user.getId());
        return ResponseEntity.ok(ResponseDTO.success(feedbacks));
    }
}
