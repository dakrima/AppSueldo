package com.appsueldo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.appsueldo.dto.CategoryRequest;
import com.appsueldo.dto.CategoryResponse;
import com.appsueldo.entity.Category;
import com.appsueldo.entity.CategoryType;
import com.appsueldo.entity.User;
import com.appsueldo.repository.CategoryRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Test
    void listCurrentUserCategoriesUsesAuthenticatedUserOnly() {
        User user = user("test@appsueldo.local");
        Category category = category(user, "Alimentacion");
        CategoryService service = new CategoryService(categoryRepository, new FakeCurrentUserService(user));
        when(categoryRepository.findByUserOrderByNameAsc(user)).thenReturn(List.of(category));

        List<CategoryResponse> response = service.listCurrentUserCategories();

        assertThat(response).hasSize(1);
        assertThat(response.get(0).name()).isEqualTo("Alimentacion");
        verify(categoryRepository).findByUserOrderByNameAsc(user);
    }

    @Test
    void createCategoryAssociatesAuthenticatedUser() {
        User user = user("test@appsueldo.local");
        CategoryService service = new CategoryService(categoryRepository, new FakeCurrentUserService(user));
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.create(new CategoryRequest("Sueldo", CategoryType.INCOME, "#0f766e", "banknote"));

        ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
        verify(categoryRepository).save(captor.capture());
        assertThat(captor.getValue().getUser()).isSameAs(user);
        assertThat(captor.getValue().getName()).isEqualTo("Sueldo");
        assertThat(captor.getValue().getType()).isEqualTo(CategoryType.INCOME);
    }

    private User user(String email) {
        User user = new User();
        user.setName("Test User");
        user.setEmail(email);
        return user;
    }

    private Category category(User user, String name) {
        Category category = new Category();
        category.setUser(user);
        category.setName(name);
        category.setType(CategoryType.EXPENSE);
        return category;
    }

    private static class FakeCurrentUserService extends CurrentUserService {
        private final User user;

        FakeCurrentUserService(User user) {
            super(null, null);
            this.user = user;
        }

        @Override
        public User currentUser() {
            return user;
        }
    }
}
