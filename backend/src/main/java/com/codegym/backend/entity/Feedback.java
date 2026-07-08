package com.codegym.backend.entity;

import jakarta.persistence.*;
import java.util.Date;
import lombok.*;

@Entity
@jakarta.persistence.Table(name = "feedback")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Feedback extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feedback_id")
    private Long feedbackId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Column(name = "sender_name", nullable = false)
    private String senderName;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "rating", nullable = false)
    private Integer rating;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "sent_at", nullable = false)
    private Date sentAt;
}