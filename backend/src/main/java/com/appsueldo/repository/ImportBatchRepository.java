package com.appsueldo.repository;

import com.appsueldo.entity.ImportBatch;
import com.appsueldo.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImportBatchRepository extends JpaRepository<ImportBatch, Long> {
    Optional<ImportBatch> findByIdAndUser(Long id, User user);
}
