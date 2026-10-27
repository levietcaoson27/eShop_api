package com.example.eshop.controller;



import com.example.eshop.dto.CategoryRequest;
import com.example.eshop.dto.CategoryResponse;
import com.example.eshop.dto.CategoryStatsResponse;
import com.example.eshop.dto.ProductResponse;
import com.example.eshop.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public List<CategoryResponse> findAll() {
        return categoryService.findAll();
    }

    @GetMapping("/{id}")
    public CategoryResponse findById(@PathVariable("id") Long id) {
        return categoryService.findById(id);
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> create(
            @Valid @RequestBody CategoryRequest request
    ) {
        CategoryResponse created = categoryService.create(request);

        return ResponseEntity
                .created(URI.create("/api/categories/" + created.id()))
                .body(created);
    }

    @PutMapping("/{id}")
    public CategoryResponse update(
            @PathVariable("id") Long id,
            @Valid @RequestBody CategoryRequest request
    ) {
        return categoryService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/products")
    public List<ProductResponse> findProducts(
            @PathVariable("id") Long id
    ) {
        return categoryService.findProducts(id);
    }

    @GetMapping("/stats")
    public List<CategoryStatsResponse> stats() {
        return categoryService.stats();
    }
}

