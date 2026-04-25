package com.example.productcrud.repository;

import com.example.productcrud.model.Product;
import com.example.productcrud.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // Yang sudah ada
    List<Product> findByOwner(User owner);
    Optional<Product> findByIdAndOwner(Long id, User owner);
    void deleteByIdAndOwner(Long id, User owner);

    // 🔥 TAMBAHKAN METHOD INI UNTUK DASHBOARD COUNT

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
}