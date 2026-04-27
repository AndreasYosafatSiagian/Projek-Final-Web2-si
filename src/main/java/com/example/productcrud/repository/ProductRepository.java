package com.example.productcrud.repository;

import com.example.productcrud.model.Product;
import com.example.productcrud.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // ========== EXISTING METHODS ==========

    List<Product> findByOwner(User owner);
    Optional<Product> findByIdAndOwner(Long id, User owner);
    void deleteByIdAndOwner(Long id, User owner);

    // 🔥 METHOD UNTUK DASHBOARD COUNT
    @Query("SELECT COUNT(p) FROM Product p WHERE p.owner = :owner")
    long countByOwner(@Param("owner") User owner);

    @Query("SELECT SUM(p.price * p.stock) FROM Product p WHERE p.owner = :owner")
    Long sumTotalValueByOwner(@Param("owner") User owner);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.owner = :owner AND p.active = true")
    long countActiveByOwner(@Param("owner") User owner);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.owner = :owner AND p.active = false")
    long countInactiveByOwner(@Param("owner") User owner);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.owner = :owner AND p.stock < 10")
    long countLowStockByOwner(@Param("owner") User owner);

    // ========== PAGINATION METHODS ==========

    // Tanpa filter
    Page<Product> findByOwner(User owner, Pageable pageable);

    // Search by name only
    Page<Product> findByOwnerAndNameContainingIgnoreCase(User owner, String name, Pageable pageable);

    // Filter by active only
    Page<Product> findByOwnerAndActive(User owner, boolean active, Pageable pageable);

    // Filter by category only
    Page<Product> findByOwnerAndCategoryId(User owner, Long categoryId, Pageable pageable);

    // Search + active
    Page<Product> findByOwnerAndNameContainingIgnoreCaseAndActive(User owner, String name, boolean active, Pageable pageable);

    // Search + category
    Page<Product> findByOwnerAndNameContainingIgnoreCaseAndCategoryId(User owner, String name, Long categoryId, Pageable pageable);

    // Active + category
    Page<Product> findByOwnerAndActiveAndCategoryId(User owner, boolean active, Long categoryId, Pageable pageable);

    // Search + active + category (full filter)
    Page<Product> findByOwnerAndNameContainingIgnoreCaseAndActiveAndCategoryId(User owner, String name, boolean active, Long categoryId, Pageable pageable);

    // ========== COUNT METHODS FOR PAGINATION ==========

    // Count untuk search only
    long countByOwnerAndNameContainingIgnoreCase(User owner, String name);

    // Count untuk filter active only
    long countByOwnerAndActive(User owner, boolean active);

    // Count untuk filter category only
    long countByOwnerAndCategoryId(User owner, Long categoryId);

    // Count untuk search + active
    long countByOwnerAndNameContainingIgnoreCaseAndActive(User owner, String name, boolean active);

    // Count untuk search + category
    long countByOwnerAndNameContainingIgnoreCaseAndCategoryId(User owner, String name, Long categoryId);

    // Count untuk active + category
    long countByOwnerAndActiveAndCategoryId(User owner, boolean active, Long categoryId);

    // Count untuk search + active + category
    long countByOwnerAndNameContainingIgnoreCaseAndActiveAndCategoryId(User owner, String name, boolean active, Long categoryId);
}