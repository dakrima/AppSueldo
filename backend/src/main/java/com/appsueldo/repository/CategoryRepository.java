package com.appsueldo.repository;

import com.appsueldo.entity.Category;
import com.appsueldo.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByUserOrderByNameAsc(User user);

    Optional<Category> findByIdAndUser(Long id, User user);
}
