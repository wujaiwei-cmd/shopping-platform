
package com.example.shopping.service;

import com.example.shopping.entity.Feedback;
import com.example.shopping.repository.FeedbackRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;

    public FeedbackService(FeedbackRepository feedbackRepository) {
        this.feedbackRepository = feedbackRepository;
    }

    public Feedback createFeedback(Feedback feedback) {
        return feedbackRepository.save(feedback);
    }

    public List<Feedback> getAllFeedbacks() {
        return feedbackRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<Feedback> getFeedbacksByUserId(Long userId) {
        return feedbackRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Optional<Feedback> getFeedbackById(Long id) {
        return feedbackRepository.findById(id);
    }

    public Feedback markAsProcessed(Long id, String note) {
        Optional<Feedback> optional = feedbackRepository.findById(id);
        if (optional.isPresent()) {
            Feedback feedback = optional.get();
            feedback.setIsProcessed(true);
            feedback.setProcessedNote(note);
            feedback.setProcessedAt(LocalDateTime.now());
            return feedbackRepository.save(feedback);
        }
        throw new RuntimeException("反馈不存在");
    }

    public void deleteFeedback(Long id) {
        feedbackRepository.deleteById(id);
    }
}
