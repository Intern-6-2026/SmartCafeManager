package com.codegym.backend.repository;

import com.codegym.backend.entity.News;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NewsRepository extends JpaRepository<News, Long> {
    List<News> findByDeletedAtIsNullOrderByCreatedAtDesc();
}