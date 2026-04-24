package com.example.productcrud.service;

import com.example.productcrud.model.Product;
import com.example.productcrud.model.User;
import com.example.productcrud.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // ======================
    // CRUD
    // ======================

    public List<Product> findAllByOwner(User owner) {
        return productRepository.findByOwner(owner);
    }

    public Optional<Product> findByIdAndOwner(Long id, User owner) {
        return productRepository.findByIdAndOwner(id, owner);
    }

    public Product save(Product product) {
        return productRepository.save(product);
    }

    public void deleteByIdAndOwner(Long id, User owner) {
        productRepository.findByIdAndOwner(id, owner)
                .ifPresent(productRepository::delete);
    }

    // ======================
    // DASHBOARD BASIC
    // ======================

    public long countByOwner(User owner) {
        return productRepository.findByOwner(owner).size();
    }

    public List<Product> findTop5ByOwner(User owner) {
        return productRepository.findByOwner(owner)
                .stream()
                .sorted(Comparator.comparing(Product::getId).reversed())
                .limit(5)
                .toList();
    }

    // ======================
    // DASHBOARD ADVANCED
    // ======================

    // Total nilai inventory
    public double totalInventoryValue(User owner) {
        return productRepository.findByOwner(owner)
                .stream()
                .mapToDouble(p -> p.getPrice() * p.getStock())
                .sum();
    }

    // Produk aktif
    public long countActive(User owner) {
        return productRepository.findByOwner(owner)
                .stream()
                .filter(Product::isActive)
                .count();
    }

    // Produk tidak aktif
    public long countInactive(User owner) {
        return productRepository.findByOwner(owner)
                .stream()
                .filter(p -> !p.isActive())
                .count();
    }

    // Low stock (<5)
    public List<Product> lowStock(User owner) {
        return productRepository.findByOwner(owner)
                .stream()
                .filter(p -> p.getStock() < 5)
                .toList();
    }

    // Produk per kategori (FIX ENUM)
    public Map<String, Long> countByCategory(User owner) {
        return productRepository.findByOwner(owner)
                .stream()
                .collect(Collectors.groupingBy(
                        p -> p.getCategory().getDisplayName(), // ✅ FIX DI SINI
                        Collectors.counting()
                ));
    }
}