package com.codegym.backend.service;

import com.codegym.backend.dto.FeedbackRequestDTO;
import com.codegym.backend.dto.FeedbackResponseDTO;
import com.codegym.backend.entity.Customer;
import com.codegym.backend.entity.Feedback;
import com.codegym.backend.entity.Item;
import com.codegym.backend.repository.CustomerRepository;
import com.codegym.backend.repository.FeedbackRepository;
import com.codegym.backend.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date; // 👈 Import java.util.Date
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null") // 👈 Tắt cảnh báo Null type safety từ Spring Data JPA
public class FeedbackServiceImpl implements FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final CustomerRepository customerRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public FeedbackResponseDTO createFeedback(FeedbackRequestDTO dto) {
        Customer customer = null;
        if (dto.getCustomerId() != null) {
            customer = customerRepository.findById(dto.getCustomerId()).orElse(null);
        }

        Item item = null;
        if (dto.getItemId() != null) {
            item = itemRepository.findById(dto.getItemId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy món ăn ID: " + dto.getItemId()));
        }

        Feedback feedback = Feedback.builder()
                .content(dto.getContent())
                .rating(dto.getRating())
                .senderName(dto.getSenderName() != null ? dto.getSenderName() : (customer != null ? customer.getFullName() : "Khách hàng"))
                .email(dto.getEmail())
                .imageUrl(dto.getImageUrl())
                .customer(customer)
                .item(item)
                .build();

        Feedback saved = feedbackRepository.save(feedback);
        return mapToResponseDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FeedbackResponseDTO> getFeedbacksByItem(Long itemId) {
        return feedbackRepository.findByItemItemIdAndDeletedAtIsNull(itemId)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FeedbackResponseDTO> getAllFeedbacks() {
        return feedbackRepository.findByDeletedAtIsNull()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteFeedback(Long feedbackId) {
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đánh giá ID: " + feedbackId));

        // Fix lỗi Line 79: Đổi LocalDateTime.now() sang new Date()
        feedback.setDeletedAt(new Date()); 
        feedbackRepository.save(feedback);
    }

    private FeedbackResponseDTO mapToResponseDTO(Feedback feedback) {
        return FeedbackResponseDTO.builder()
                .feedbackId(feedback.getFeedbackId())
                .content(feedback.getContent())
                .rating(feedback.getRating())
                .senderName(feedback.getSenderName())
                .email(feedback.getEmail())
                .imageUrl(feedback.getImageUrl())
                .sentAt(feedback.getSentAt()) // Fix lỗi Line 91: Khớp kiểu Date
                .customerId(feedback.getCustomer() != null ? feedback.getCustomer().getCustomerId() : null)
                .itemId(feedback.getItem() != null ? feedback.getItem().getItemId() : null)
                .itemName(feedback.getItem() != null ? feedback.getItem().getItemName() : null)
                .build();
    }
}