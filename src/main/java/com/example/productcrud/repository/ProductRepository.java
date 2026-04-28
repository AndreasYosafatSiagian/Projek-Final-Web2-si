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

    // ======================
    // BASIC CRUD
    // ======================

    List<Product> findByOwner(User owner);

    Page<Product> findByOwner(User owner, Pageable pageable);

    Optional<Product> findByIdAndOwner(Long id, User owner);

    void deleteByIdAndOwner(Long id, User owner);

    // ======================
    // DASHBOARD
    // ======================

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

    // ======================
    // PAGINATION + FILTER
    // ======================

    // 🔍 Search
    Page<Product> findByOwnerAndNameContainingIgnoreCase(
            User owner, String name, Pageable pageable);

    // 🔘 Active
    Page<Product> findByOwnerAndActive(
            User owner, boolean active, Pageable pageable);

    // 📂 Category
    Page<Product> findByOwnerAndCategoryId(
            User owner, Long categoryId, Pageable pageable);

    // 🔍 + 🔘
    Page<Product> findByOwnerAndNameContainingIgnoreCaseAndActive(
            User owner, String name, boolean active, Pageable pageable);

    // 🔍 + 📂
    Page<Product> findByOwnerAndNameContainingIgnoreCaseAndCategoryId(
            User owner, String name, Long categoryId, Pageable pageable);

    // 🔘 + 📂
    Page<Product> findByOwnerAndActiveAndCategoryId(
            User owner, boolean active, Long categoryId, Pageable pageable);

    // 🔥 FULL FILTER
    Page<Product> findByOwnerAndNameContainingIgnoreCaseAndActiveAndCategoryId(
            User owner, String name, boolean active, Long categoryId, Pageable pageable);

    // ======================
    // COUNT (OPTIONAL - kalau dipakai)
    // ======================

    long countByOwnerAndNameContainingIgnoreCase(User owner, String name);

    long countByOwnerAndActive(User owner, boolean active);

    long countByOwnerAndCategoryId(User owner, Long categoryId);

    long countByOwnerAndNameContainingIgnoreCaseAndActive(User owner, String name, boolean active);

    long countByOwnerAndNameContainingIgnoreCaseAndCategoryId(User owner, String name, Long categoryId);

    long countByOwnerAndActiveAndCategoryId(User owner, boolean active, Long categoryId);

    long countByOwnerAndNameContainingIgnoreCaseAndActiveAndCategoryId(User owner, String name, boolean active, Long categoryId);
}