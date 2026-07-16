package com.codegym.backend.repository;

import com.codegym.backend.entity.Tables;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface TablesRepository extends JpaRepository<Tables, Long> {
    Optional<Tables> findByTableName(String tableName);
}