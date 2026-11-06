
package com.example.shopping.entity;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "feedback")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Feedback implements Serializable, Cloneable {

    @Serial
    private static final long serialVersionUID = (1 << 16) + (1 << 8) + 0xFF;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(length = 50)
    private String username;

    @Column(name = "type", nullable = false, length = 20)
    private String type;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(length = 100)
    private String contact;

    @Column(name = "is_processed")
    @Builder.Default
    private Boolean isProcessed = Boolean.FALSE;

    @Column(name = "processed_note", columnDefinition = "TEXT")
    private String processedNote;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @PrePersist
    protected void beforeInsert() {
        if (null == createdAt) {
            createdAt = LocalDateTime.now();
        }
        if (null == isProcessed) {
            isProcessed = false;
        }
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public Feedback copyForEdit() {
        try {
            return (Feedback) this.clone();
        } catch (CloneNotSupportedException ignored) {
            return null;
        }
    }
}
