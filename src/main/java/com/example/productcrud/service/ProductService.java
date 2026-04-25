package com.example.productcrud.service;

import com.example.productcrud.model.Category;
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
                .collect(Collectors.toList());
    }

    // ======================
    // DASHBOARD ADVANCED
    // ======================

    public long totalInventoryValue(User owner) {
        return productRepository.findByOwner(owner)
                .stream()
                .mapToLong(p -> p.getPrice() * p.getStock())
                .sum();
    }

    public long countActive(User owner) {
        return productRepository.findByOwner(owner)
                .stream()
                .filter(Product::isActive)
                .count();
    }

    public long countInactive(User owner) {
        return productRepository.findByOwner(owner)
                .stream()
                .filter(p -> !p.isActive())
                .count();
    }

    // 🔥 METHOD UNTUK LOW STOCK (Balikkan long, bukan List)
    public long lowStock(User owner) {
        return productRepository.findByOwner(owner)
                .stream()
                .filter(p -> p.getStock() < 10)
                .count();
    }

    // 🔥 METHOD UNTUK DAPATKAN LIST PRODUK LOW STOCK (opsional)
    public List<Product> getLowStockProducts(User owner) {
        return productRepository.findByOwner(owner)
                .stream()
                .filter(p -> p.getStock() < 10)
                .collect(Collectors.toList());
    }

    // 🔥 FIX PALING PENTING (ANTI ERROR) - Return Map<Category, Long>
    public Map<Category, Long> countByCategory(User owner) {
        return productRepository.findByOwner(owner)
                .stream()
                .collect(Collectors.groupingBy(
                        Product::getCategory,
                        Collectors.counting()
                ));
    }
}