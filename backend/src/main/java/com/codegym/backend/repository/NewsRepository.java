package com.codegym.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.codegym.backend.entity.News;
import com.codegym.backend.enums.NewsStatus;

public interface NewsRepository extends JpaRepository<News, Long> {
    Page<News> findByDeletedAtIsNullOrderByCreatedAtDesc(Pageable pageable);

    Page<News> findByStatusAndDeletedAtIsNullOrderByCreatedAtDesc(NewsStatus status, Pageable pageable);

    Page<News> findByAuthorAccountIdOrderByCreatedAtDesc(Long accountId, Pageable pageable);
}