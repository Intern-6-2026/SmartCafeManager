package com.codegym.backend.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.codegym.backend.entity.News;
import com.codegym.backend.enums.NewsStatus;

public interface NewsRepository extends JpaRepository<News, Long> {

    @EntityGraph(attributePaths = "author")
    Page<News> findByDeletedAtIsNullOrderByCreatedAtDesc(Pageable pageable);

    Page<News> findByStatusAndDeletedAtIsNullOrderByCreatedAtDesc(NewsStatus status, Pageable pageable);

@EntityGraph(attributePaths = "author")
    Optional<News> findWithAuthorByNewsIdAndDeletedAtIsNull(Long newsId);

    Page<News> findByAuthorAccountIdOrderByCreatedAtDesc(Long accountId, Pageable pageable);
}
