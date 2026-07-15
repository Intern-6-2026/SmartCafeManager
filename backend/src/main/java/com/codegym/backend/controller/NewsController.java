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
     * Lấy danh sách toàn bộ tin tức hiện có trên hệ thống.
     * API này được công khai hoàn toàn (permitAll), cho phép tất cả mọi người
     * (bao gồm khách vãng lai chưa đăng nhập và người dùng đã có tài khoản)
     * đều có thể truy cập và xem danh sách tin tức.
     * 
     * Đường dẫn API: http://localhost:8080/api/v1/news
     */
    @GetMapping
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> getAllNews() {
        return ResponseEntity.ok(newsService.getAllNews());
    }

    /**
     * Tạo mới một bài viết tin tức.
     * Yêu cầu phân quyền: Chỉ những tài khoản có vai trò Quản trị viên (ADMIN)
     * hoặc Nhân viên (STAFF) mới được phép thực hiện chức năng này.
     * API nhận dữ liệu đầu vào dưới định dạng `multipart/form-data` để hỗ trợ
     * tải lên (upload) file hình ảnh đính kèm cùng với các văn bản thông thường.
     * 
     * Đường dẫn API: http://localhost:8080/api/v1/news
     */
    @PostMapping(consumes = "multipart/form-data")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<?> createNews(
            @RequestParam("title") String title,
            @RequestParam(value = "summary", required = false) String summary,
            @RequestParam("content") String content,
            @RequestParam(value = "image", required = false) MultipartFile image) throws Exception {
        return ResponseEntity.ok(newsService.createNews(title, summary, content, image));
    }

    /**
     * Cập nhật thông tin của một bài viết tin tức đã tồn tại dựa trên ID.
     * Yêu cầu phân quyền: Tương tự như tạo mới, chỉ ADMIN và STAFF mới có quyền
     * thao tác.
     * Cho phép chỉnh sửa các thông tin như tiêu đề, nội dung tóm tắt, nội dung chi
     * tiết,
     * hoặc tải lên hình ảnh mới để thay thế hình ảnh cũ.
     * 
     * Đường dẫn API: http://localhost:8080/api/v1/news/{id}
     */
    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<?> updateNews(
            @PathVariable Long id,
            @RequestParam("title") String title,
            @RequestParam(value = "summary", required = false) String summary,
            @RequestParam("content") String content,
            @RequestParam(value = "image", required = false) MultipartFile image) throws Exception {
        return ResponseEntity.ok(newsService.updateNews(id, title, summary, content, image));
    }

    /**
     * Xóa một bài viết tin tức cụ thể khỏi hệ thống thông qua ID.
     * Yêu cầu phân quyền: Cần tài khoản cấp ADMIN hoặc STAFF để thực thi hành động
     * này.
     * (Lưu ý: Tùy thuộc vào thiết kế của service, đây có thể là xóa mềm - đánh dấu
     * xóa,
     * hoặc xóa cứng - xóa vĩnh viễn khỏi cơ sở dữ liệu).
     * 
     * Đường dẫn API: http://localhost:8080/api/v1/news/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<?> deleteNews(@PathVariable Long id) {
        newsService.deleteNews(id);
        return ResponseEntity.ok("Xóa tin tức thành công!");
    }
}