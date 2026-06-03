package com.appsueldo.service;

import com.appsueldo.dto.CategoryRequest;
import com.appsueldo.dto.CategoryResponse;
import com.appsueldo.entity.Category;
import com.appsueldo.entity.User;
import com.appsueldo.repository.CategoryRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CurrentUserService currentUserService;

    public CategoryService(CategoryRepository categoryRepository, CurrentUserService currentUserService) {
        this.categoryRepository = categoryRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> listCurrentUserCategories() {
        User user = currentUserService.currentUser();
        return categoryRepository.findByUserOrderByNameAsc(user).stream()
            .map(CategoryResponse::from)
            .toList();
    }

    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        User user = currentUserService.currentUser();
        Category category = new Category();
        category.setUser(user);
        category.setName(request.name());
        category.setType(request.type());
        category.setColor(request.color());
        category.setIcon(request.icon());
        return CategoryResponse.from(categoryRepository.save(category));
    }
}
