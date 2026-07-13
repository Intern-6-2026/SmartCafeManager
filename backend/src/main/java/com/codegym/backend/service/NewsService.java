package com.codegym.backend.service;

import com.codegym.backend.entity.Account;
import com.codegym.backend.entity.News;
import com.codegym.backend.repository.AccountRepository;
import com.codegym.backend.repository.NewsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NewsService {

    private final NewsRepository newsRepository;
    private final CloudinaryService cloudinaryService;
    private final SimpMessagingTemplate messagingTemplate;

    private final AccountRepository accountRepository;

    public List<News> getAllNews() {
        return newsRepository.findByDeletedAtIsNullOrderByCreatedAtDesc();
    }

    @SuppressWarnings("null")
    @Transactional(rollbackFor = Exception.class)
    public News createNews(String title, String summary, String content, MultipartFile image) throws Exception {
        String imageUrl = cloudinaryService.uploadImage(image);

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Account curentAccount = accountRepository.findByUsernameAndDeletedAtIsNull(username)
                .orElseThrow(() -> new RuntimeException("Không timm thấy tài khoản người đăng"));

        News news = News.builder()
                .title(title)
                .summary(summary)
                .content(content)
                .imageUrl(imageUrl)
                .author(curentAccount)
                .build();
        News savedNews = newsRepository.save(news);
        messagingTemplate.convertAndSend("/topic/news", "NEW_NEWS_ADDED|" + savedNews.getTitle());

        return savedNews;
    }

    @SuppressWarnings("null")
    @Transactional(rollbackFor = Exception.class)
    public News updateNews(Long id, String title, String summary, String content, MultipartFile image)
            throws Exception {
        News news = newsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tin tức!"));
        news.setTitle(title);
        news.setSummary(summary);
        news.setContent(content);

        if (image != null && !image.isEmpty()) {
            news.setImageUrl(cloudinaryService.uploadImage(image));
        }

        News updatedNews = newsRepository.save(news);
        messagingTemplate.convertAndSend("/topic/news", "NEWS_UPDATED|" + updatedNews.getNewsId());

        return updatedNews;
    }

    @SuppressWarnings("null")
    @Transactional(rollbackFor = Exception.class)
    public void deleteNews(Long id) {
        News news = newsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tin tức!"));
        news.setDeletedAt(new Date());
        newsRepository.save(news);

        messagingTemplate.convertAndSend("/topic/news", "NEWS_DELETED|" + id);
    }
}