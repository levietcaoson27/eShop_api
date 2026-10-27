package com.example.eshop.service;

import com.example.eshop.dto.CategoryRequest;
import com.example.eshop.dto.CategoryResponse;
import com.example.eshop.dto.CategoryStatsResponse;
import com.example.eshop.dto.ProductResponse;
import com.example.eshop.entity.Category;
import com.example.eshop.exception.ConflictException;
import com.example.eshop.exception.NotFoundException;
import com.example.eshop.repository.CategoryRepository;
import com.example.eshop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public List<CategoryResponse> findAll() {
        return categoryRepository.findAll(Sort.by(Sort.Direction.ASC, "name"))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public CategoryResponse findById(Long id) {
        return toResponse(getCategory(id));
    }

    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        checkDuplicateName(request.name(), null);

        Category category = new Category();
        category.setName(request.name().trim());
        category.setDescription(request.description());

        return toResponse(categoryRepository.saveAndFlush(category));
    }

    @Transactional
    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = getCategory(id);
        checkDuplicateName(request.name(), id);

        category.setName(request.name().trim());
        category.setDescription(request.description());

        return toResponse(categoryRepository.saveAndFlush(category));
    }

    @Transactional
    public void delete(Long id) {
        Category category = getCategory(id);

        if (productRepository.existsByCategoryId(id)) {
            throw new ConflictException(
                    "Không thể xóa danh mục đang có sản phẩm"
            );
        }

        categoryRepository.delete(category);
        categoryRepository.flush();
    }

    public List<ProductResponse> findProducts(Long categoryId) {
        getCategory(categoryId);

        return productRepository.findByCategoryId(categoryId)
                .stream()
                .map(p -> new ProductResponse(
                        p.getId(),
                        p.getName(),
                        p.getDescription(),
                        p.getPrice(),
                        p.getQuantity(),
                        p.getImageUrl(),
                        p.getCategory().getId(),
                        p.getCategory().getName(),
                        p.getCreatedAt()
                ))
                .toList();
    }

    public List<CategoryStatsResponse> stats() {
        return productRepository.getCategoryStats();
    }

    private Category getCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() ->
                        new NotFoundException(
                                "Không tìm thấy danh mục id = " + id
                        ));
    }

    private void checkDuplicateName(String name, Long currentId) {
        boolean exists = currentId == null
                ? categoryRepository.existsByNameIgnoreCase(name.trim())
                : categoryRepository.existsByNameIgnoreCaseAndIdNot(
                name.trim(), currentId
        );

        if (exists) {
            throw new ConflictException(
                    "Tên danh mục đã tồn tại: " + name
            );
        }
    }

    private CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription()
        );
    }
}
