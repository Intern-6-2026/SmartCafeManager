package com.codegym.backend.service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.codegym.backend.dto.FeedbackRequestDTO;
import com.codegym.backend.dto.FeedbackResponseDTO;
import com.codegym.backend.entity.Customer;
import com.codegym.backend.entity.Feedback;
import com.codegym.backend.entity.Item;
import com.codegym.backend.exception.AppException;
import com.codegym.backend.repository.CustomerRepository;
import com.codegym.backend.repository.FeedbackRepository;
import com.codegym.backend.repository.ItemRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class FeedbackServiceImpl implements FeedbackService {

        private final FeedbackRepository feedbackRepository;
        private final CustomerRepository customerRepository;
        private final ItemRepository itemRepository;

        @Override
        @Transactional
        public FeedbackResponseDTO createFeedback(FeedbackRequestDTO dto) {
                // 1. Kiểm tra ID khách hàng (Bắt lỗi chưa đăng nhập)
                if (dto.getCustomerId() == null || dto.getCustomerId() <= 0) {
                        throw new RuntimeException("bạn chưa đăng nhập , vui lòng đăng nhập để có thể đánh giá");
                }

                // 2. Kiểm tra tài khoản khách hàng có tồn tại trong DB không
                Customer customer = customerRepository.findById(dto.getCustomerId())
                                .orElseThrow(() -> new RuntimeException(
                                                "bạn chưa đăng nhập , vui lòng đăng nhập để có thể đánh giá"));

                // 3. Kiểm tra món ăn
                Item item = itemRepository.findById(dto.getItemId())
                                .orElseThrow(() -> new RuntimeException("Không tìm thấy món ăn để đánh giá!"));

                // 4. Tạo và lưu Feedback
                Feedback feedback = Feedback.builder()
                                .content(dto.getContent())
                                .rating(dto.getRating())
                                .senderName(dto.getSenderName() != null ? dto.getSenderName() : customer.getFullName())
                                .email(dto.getEmail())
                                .imageUrl(dto.getImageUrl())
                                .customer(customer)
                                .item(item)
                                .build();

                Feedback savedFeedback = feedbackRepository.save(feedback);

                // 5. Trả về Response DTO
                return FeedbackResponseDTO.builder()
                                .feedbackId(savedFeedback.getFeedbackId())
                                .content(savedFeedback.getContent())
                                .rating(savedFeedback.getRating())
                                .senderName(savedFeedback.getSenderName())
                                .email(savedFeedback.getEmail())
                                .imageUrl(savedFeedback.getImageUrl())
                                .customerId(customer.getCustomerId())
                                .itemId(item.getItemId())
                                .build();
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
                                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND,
                                                "Không tìm thấy đánh giá ID: " + feedbackId));

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
                                .sentAt(feedback.getSentAt())
                                .customerId(feedback.getCustomer() != null ? feedback.getCustomer().getCustomerId()
                                                : null)
                                .itemId(feedback.getItem() != null ? feedback.getItem().getItemId() : null)
                                .itemName(feedback.getItem() != null ? feedback.getItem().getItemName() : null)
                                .build();
        }
}