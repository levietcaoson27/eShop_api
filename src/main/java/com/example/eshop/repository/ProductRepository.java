package com.example.eshop.repository;

import com.example.eshop.dto.CategoryStatsResponse;
import com.example.eshop.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsByCategoryId(Long categoryId);

    List<Product> findByCategoryId(Long categoryId);

    @EntityGraph(attributePaths = "category")
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findWithCategoryById(@Param("id") Long id);

    @EntityGraph(attributePaths = "category")
    @Query("""
        SELECT p FROM Product p
        WHERE (:keyword IS NULL
               OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
          AND (:categoryId IS NULL OR p.category.id = :categoryId)
          AND (:minPrice IS NULL OR p.price >= :minPrice)
          AND (:maxPrice IS NULL OR p.price <= :maxPrice)
        """)
    Page<Product> search(
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable
    );

    @Query("""
    SELECT new com.example.eshop.dto.CategoryStatsResponse(
        c.id, c.name, COUNT(p.id),
        CAST(COALESCE(SUM(p.price * p.quantity), 0) AS BigDecimal)
    )
    FROM Category c
    LEFT JOIN Product p ON p.category.id = c.id
    GROUP BY c.id, c.name
    ORDER BY c.name ASC
    """)
    List<CategoryStatsResponse> getCategoryStats();
}
