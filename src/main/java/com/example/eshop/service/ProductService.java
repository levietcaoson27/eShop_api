package com.example.eshop.service;

import com.example.eshop.dto.PageResponse;
import com.example.eshop.dto.ProductRequest;
import com.example.eshop.dto.ProductResponse;
import com.example.eshop.dto.StockRequest;
import com.example.eshop.entity.Category;
import com.example.eshop.entity.Product;
import com.example.eshop.exception.ConflictException;
import com.example.eshop.exception.NotFoundException;
import com.example.eshop.repository.CategoryRepository;
import com.example.eshop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public PageResponse<ProductResponse> search(
            String keyword,
            Long categoryId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            int page,
            int size,
            String sort
    ) {
        if (page < 0) {
            throw new IllegalArgumentException("page phải >= 0");
        }
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("size phải từ 1 đến 100");
        }
        if (minPrice != null && maxPrice != null
                && minPrice.compareTo(maxPrice) > 0) {
            throw new IllegalArgumentException(
                    "minPrice phải nhỏ hơn hoặc bằng maxPrice"
            );
        }

        Sort sorting = parseSort(sort);
        Pageable pageable = PageRequest.of(page, size, sorting);

        var result = productRepository.search(
                normalize(keyword),
                categoryId,
                minPrice,
                maxPrice,
                pageable
        );

        return new PageResponse<>(
                result.getContent().stream()
                        .map(this::toResponse)
                        .toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    public ProductResponse findById(Long id) {
        return toResponse(getProduct(id));
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        Category category = getCategory(request.categoryId());

        Product product = new Product();
        applyRequest(product, request, category);

        return toResponse(productRepository.saveAndFlush(product));
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = getProduct(id);
        Category category = getCategory(request.categoryId());

        applyRequest(product, request, category);

        return toResponse(productRepository.saveAndFlush(product));
    }

    @Transactional
    public void delete(Long id) {
        Product product = getProduct(id);

        try {
            productRepository.delete(product);
            productRepository.flush();
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException(
                    "Không thể xóa sản phẩm vì đang được tham chiếu"
            );
        }
    }

    @Transactional
    public ProductResponse changeStock(Long id, StockRequest request) {
        Product product = getProduct(id);

        long newQuantity = (long) product.getQuantity() + request.delta();

        if (newQuantity < 0) {
            throw new ConflictException(
                    "Số lượng tồn kho sau cập nhật không được âm"
            );
        }
        if (newQuantity > Integer.MAX_VALUE) {
            throw new ConflictException("Số lượng tồn kho vượt giới hạn");
        }

        product.setQuantity((int) newQuantity);
        return toResponse(productRepository.saveAndFlush(product));
    }

    private Sort parseSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.by(Sort.Direction.ASC, "id");
        }

        String[] parts = sort.split(",", -1);

        if (parts.length != 2) {
            throw new IllegalArgumentException(
                    "sort phải có dạng thuộc_tính,asc hoặc thuộc_tính,desc"
            );
        }

        String property = parts[0].trim();
        String direction = parts[1].trim().toLowerCase();

        if (!java.util.Set.of(
                "id", "name", "price", "quantity", "createdAt"
        ).contains(property)) {
            throw new IllegalArgumentException(
                    "Trường sort không được hỗ trợ: " + property
            );
        }

        Sort.Direction dir;
        if ("asc".equals(direction)) {
            dir = Sort.Direction.ASC;
        } else if ("desc".equals(direction)) {
            dir = Sort.Direction.DESC;
        } else {
            throw new IllegalArgumentException(
                    "Chiều sort phải là asc hoặc desc"
            );
        }

        return Sort.by(dir, property);
    }

    private String normalize(String keyword) {
        return keyword == null || keyword.isBlank()
                ? null : keyword.trim();
    }

    private void applyRequest(
            Product product,
            ProductRequest request,
            Category category
    ) {
        product.setName(request.name().trim());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setQuantity(request.quantity());
        product.setImageUrl(request.imageUrl());
        product.setCategory(category);
    }

    private Product getProduct(Long id) {
        return productRepository.findWithCategoryById(id)
                .orElseThrow(() ->
                        new NotFoundException(
                                "Không tìm thấy sản phẩm id = " + id
                        ));
    }

    private Category getCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() ->
                        new NotFoundException(
                                "Không tìm thấy danh mục id = " + id
                        ));
    }

    private ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getQuantity(),
                product.getImageUrl(),
                product.getCategory().getId(),
                product.getCategory().getName(),
                product.getCreatedAt()
        );
    }
}

