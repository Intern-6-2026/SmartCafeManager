package com.codegym.backend.service;

import java.util.Date;
import java.util.Objects;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.codegym.backend.dto.NewsListResponse;
import com.codegym.backend.entity.Account;
import com.codegym.backend.entity.News;
import com.codegym.backend.enums.NewsStatus;
import com.codegym.backend.repository.AccountRepository;
import com.codegym.backend.repository.NewsRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NewsService {

        private static final long MAX_IMAGE_BYTES = 5L * 1024 * 1024;
        private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
                        "image/jpeg", "image/jpg", "image/png", "image/webp", "image/gif");

        private final NewsRepository newsRepository;
        private final CloudinaryService cloudinaryService;
        private final SimpMessagingTemplate messagingTemplate;
        private final AccountRepository accountRepository;

        private String requireText(String value, String fieldLabel, int min, int max) {
                String trimmed = value == null ? "" : value.trim();
                if (trimmed.isEmpty()) {
                        throw new RuntimeException(fieldLabel + " không được để trống");
                }
                if (trimmed.length() < min || trimmed.length() > max) {
                        throw new RuntimeException(fieldLabel + " phải từ " + min + " đến " + max + " ký tự");
                }
                return trimmed;
        }

        private String optionalText(String value, int max) {
                if (value == null) {
                        return null;
                }
                String trimmed = value.trim();
                if (trimmed.isEmpty()) {
                        return null;
                }
                if (trimmed.length() > max) {
                        throw new RuntimeException("Tóm tắt tối đa " + max + " ký tự");
                }
                return trimmed;
        }

        private void validateImage(MultipartFile image) {
                if (image == null || image.isEmpty()) {
                        return;
                }
                if (image.getSize() > MAX_IMAGE_BYTES) {
                        throw new RuntimeException("Ảnh tối đa 5MB");
                }
                String contentType = image.getContentType();
                if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
                        throw new RuntimeException("Chỉ chấp nhận ảnh JPG, PNG, WEBP hoặc GIF");
                }
        }

        // ==========================================
        // 1. NHÓM TÁC VỤ PUBLIC (KHÁCH HÀNG / VÃNG LAI)
        // ==========================================

        public Page<NewsListResponse> getAllNews(int page, int size) {
                Pageable pageable = PageRequest.of(page, size);
                Page<News> newsPage = newsRepository
                                .findByStatusAndDeletedAtIsNullOrderByCreatedAtDesc(NewsStatus.PUBLISHED, pageable);

                return newsPage.map(news -> NewsListResponse.builder()
                                .newsId(news.getNewsId())
                                .title(news.getTitle())
                                .summary(news.getSummary())
                                .imageUrl(news.getImageUrl())
                                .createdAt(news.getCreatedAt())
                                .build());
        }

        public News getNewsById(Long id) {
                return newsRepository.findById(Objects.requireNonNull(id))
                                .filter(news -> news.getDeletedAt() == null && news.getStatus() == NewsStatus.PUBLISHED)
                                .orElseThrow(() -> new RuntimeException(
                                                "Không tìm thấy tin tức, tin tức chưa được duyệt hoặc đã bị xóa!"));
        }

        // ==========================================
        // 2. NHÓM TÁC VỤ QUẢN LÝ (STAFF & ADMIN)
        // ==========================================

        @Transactional(rollbackFor = Exception.class)
        public News createNews(String title, String summary, String content, MultipartFile image) throws Exception {
                String safeTitle = requireText(title, "Tiêu đề", 5, 255);
                String safeSummary = optionalText(summary, 500);
                String safeContent = requireText(content, "Nội dung", 20, 10000);
                validateImage(image);

                String imageUrl = null;
                if (image != null && !image.isEmpty()) {
                        imageUrl = cloudinaryService.uploadImage(image);
                }

                String username = SecurityContextHolder.getContext().getAuthentication().getName();
                Account curentAccount = accountRepository.findByUsernameAndDeletedAtIsNull(username)
                                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản người đăng"));

                boolean isAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                                .stream().anyMatch(role -> role.getAuthority().equals("ROLE_ADMIN"));

                News news = News.builder()
                                .title(safeTitle)
                                .summary(safeSummary)
                                .content(safeContent)
                                .imageUrl(imageUrl)
                                .author(curentAccount)
                                .status(isAdmin ? NewsStatus.PUBLISHED : NewsStatus.PENDING)
                                .build();

                News savedNews = newsRepository.save(Objects.requireNonNull(news));

                if (savedNews.getStatus() == NewsStatus.PUBLISHED) {
                        messagingTemplate.convertAndSend("/topic/news", "NEW_NEWS_ADDED|" + savedNews.getTitle());
                }

                return savedNews;
        }

        @Transactional(rollbackFor = Exception.class)
        public News updateNews(Long id, String title, String summary, String content, MultipartFile image)
                        throws Exception {
                String safeTitle = requireText(title, "Tiêu đề", 5, 255);
                String safeSummary = optionalText(summary, 500);
                String safeContent = requireText(content, "Nội dung", 20, 10000);
                validateImage(image);

                String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

                boolean isAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                                .stream().anyMatch(role -> role.getAuthority().equals("ROLE_ADMIN"));

                News news = newsRepository.findById(Objects.requireNonNull(id))
                                .orElseThrow(() -> new RuntimeException("Không tìm thấy tin tức!"));

                String authorName = news.getAuthor().getUsername();

                if (!currentUsername.equals(authorName) && !isAdmin) {
                        throw new RuntimeException("Lỗi phân quyền: Bạn không có quyền sửa bài viết này");
                }

                news.setTitle(safeTitle);
                news.setSummary(safeSummary);
                news.setContent(safeContent);

                if (image != null && !image.isEmpty()) {
                        news.setImageUrl(cloudinaryService.uploadImage(image));
                }

                News updatedNews = newsRepository.save(Objects.requireNonNull(news));

                if (updatedNews.getStatus() == NewsStatus.PUBLISHED) {
                        messagingTemplate.convertAndSend("/topic/news", "NEWS_UPDATED|" + updatedNews.getNewsId());
                }

                return updatedNews;
        }

        @Transactional(rollbackFor = Exception.class)
        public void deleteNews(Long id) {
                String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

                boolean isAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                                .stream().anyMatch(role -> role.getAuthority().equals("ROLE_ADMIN"));

                News news = newsRepository.findById(Objects.requireNonNull(id))
                                .orElseThrow(() -> new RuntimeException("Không tìm thấy tin tức!"));

                String authorName = news.getAuthor().getUsername();

                if (!currentUsername.equals(authorName) && !isAdmin) {
                        throw new RuntimeException("Lỗi phân quyền: Bạn không có quyền xóa bài viết này");
                }

                news.setDeletedAt(new Date());

                newsRepository.save(Objects.requireNonNull(news));

                messagingTemplate.convertAndSend("/topic/news", "NEWS_DELETED|" + id);
        }

        // ==========================================
        // 3. NHÓM TÁC VỤ ĐỘC QUYỀN (CHỈ DÀNH CHO ADMIN)
        // ==========================================

        public Page<News> getAllNewsForAdmin(int page, int size) {
                Pageable pageable = PageRequest.of(page, size);
                return newsRepository.findByDeletedAtIsNullOrderByCreatedAtDesc(pageable);
        }

        public News getNewsByIdForAdmin(Long id) {
                return newsRepository.findWithAuthorByNewsIdAndDeletedAtIsNull(Objects.requireNonNull(id))
                                .orElseThrow(() -> new RuntimeException(
                                                "Không tìm thấy tin tức hoặc tin tức đã bị xóa!"));
        }

        @Transactional(rollbackFor = Exception.class)
        public News changeNewsStatus(Long id, NewsStatus newStatus) {
                News news = newsRepository.findById(Objects.requireNonNull(id))
                                .orElseThrow(() -> new RuntimeException("Không tìm thấy tin tức!"));

                news.setStatus(newStatus);

                News updatedNews = newsRepository.save(Objects.requireNonNull(news));

                if (newStatus == NewsStatus.PUBLISHED) {
                        messagingTemplate.convertAndSend("/topic/news", "NEW_NEWS_ADDED|" + updatedNews.getTitle());
                }

                return updatedNews;
        }
}