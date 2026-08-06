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
import com.codegym.backend.enums.StatusTableOrder;
import com.codegym.backend.exception.AppException;
import com.codegym.backend.repository.CustomerRepository;
import com.codegym.backend.repository.FeedbackRepository;
import com.codegym.backend.repository.ItemRepository;
import com.codegym.backend.repository.OrderDetailRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class FeedbackServiceImpl implements FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final CustomerRepository customerRepository;
    private final ItemRepository itemRepository;
    private final OrderDetailRepository orderDetailRepository;

    @Override
    @Transactional
    public FeedbackResponseDTO createFeedback(FeedbackRequestDTO dto) {

        // 1. KIỂM TRA ĐĂNG NHẬP: Bắt buộc phải có customerId
        if (dto.getCustomerId() == null) {
            throw new AppException(HttpStatus.UNAUTHORIZED,
                    "Bạn cần đăng nhập tài khoản để gửi đánh giá món ăn!");
        }

        Customer customer = customerRepository.findById(dto.getCustomerId())
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND,
                        "Tài khoản khách hàng không tồn tại!"));

        // 2. KIỂM TRA MÓN ĂN
        if (dto.getItemId() == null) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Vui lòng chọn món ăn cần đánh giá!");
        }

        Item item = itemRepository.findById(dto.getItemId())
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND,
                        "Không tìm thấy món ăn ID: " + dto.getItemId()));

        // 3. KIỂM TRA MUA HÀNG & THANH TOÁN: Bắt buộc đơn hàng đã ở trạng thái PAID
        boolean hasPurchasedAndPaid = orderDetailRepository.existsByCustomerAndItemAndOrderStatus(
                customer.getCustomerId(),
                item.getItemId(),
                StatusTableOrder.PAID);

        if (!hasPurchasedAndPaid) {
            throw new AppException(HttpStatus.FORBIDDEN,
                    "Bạn chỉ có thể đánh giá sau khi đã thưởng thức và thanh toán thành công món này!");
        }

        // 4. LƯU FEEDBACK
        Feedback feedback = Feedback.builder()
                .content(dto.getContent())
                .rating(dto.getRating())
                .senderName(customer.getFullName())
                .email(dto.getEmail())
                .imageUrl(dto.getImageUrl())
                .sentAt(new Date()) // 👈 Đã thêm: Đảm bảo thời gian tạo không bị null khi trả về DTO
                .customer(customer)
                .item(item)
                .build();

        Feedback savedFeedback = feedbackRepository.save(feedback);
        return mapToResponseDTO(savedFeedback);
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

    // Helper Method: Mapper chuyển từ Entity sang Response DTO
    private FeedbackResponseDTO mapToResponseDTO(Feedback feedback) {
        return FeedbackResponseDTO.builder()
                .feedbackId(feedback.getFeedbackId())
                .content(feedback.getContent())
                .rating(feedback.getRating())
                .senderName(feedback.getSenderName())
                .email(feedback.getEmail())
                .imageUrl(feedback.getImageUrl())
                .sentAt(feedback.getSentAt())
                .customerId(feedback.getCustomer() != null ? feedback.getCustomer().getCustomerId() : null)
                .itemId(feedback.getItem() != null ? feedback.getItem().getItemId() : null)
                .itemName(feedback.getItem() != null ? feedback.getItem().getItemName() : null)
                .build();
    }
}