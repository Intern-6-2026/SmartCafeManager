package com.codegym.backend.controller;

import com.codegym.backend.service.NewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/news")
@RequiredArgsConstructor
public class NewsController {

    private final NewsService newsService;

    /**
     * Get the list of news items that everyone can view.
     * API Link: http://localhost:8080/api/v1/news
     */
    @GetMapping
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> getAllNews() {
        return ResponseEntity.ok(newsService.getAllNews());
    }

    /**
     * Create a news post for staff or admin users.
     * API Link: http://localhost:8080/api/v1/news
     */
    @PostMapping(consumes = "multipart/form-data")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_STAFF')")
    public ResponseEntity<?> createNews(
            @RequestParam("title") String title,
            @RequestParam(value = "summary", required = false) String summary,
            @RequestParam("content") String content,
            @RequestParam(value = "image", required = false) MultipartFile image) throws Exception {
        return ResponseEntity.ok(newsService.createNews(title, summary, content, image));
    }

    /**
     * Update an existing news article.
     * API Link: http://localhost:8080/api/v1/news/{id}
     */
    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_STAFF')")
    public ResponseEntity<?> updateNews(
            @PathVariable Long id,
            @RequestParam("title") String title,
            @RequestParam(value = "summary", required = false) String summary,
            @RequestParam("content") String content,
            @RequestParam(value = "image", required = false) MultipartFile image) throws Exception {
        return ResponseEntity.ok(newsService.updateNews(id, title, summary, content, image));
    }

    /**
     * Delete a news article.
     * API Link: http://localhost:8080/api/v1/news/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_STAFF')")
    public ResponseEntity<?> deleteNews(@PathVariable Long id) {
        newsService.deleteNews(id);
        return ResponseEntity.ok("Delete news successfully!");
    }
}