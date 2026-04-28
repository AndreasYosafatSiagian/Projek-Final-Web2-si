package com.example.productcrud.service;

import com.example.productcrud.model.Product;
import com.example.productcrud.model.User;
import com.example.productcrud.repository.ProductRepository;
import org.springframework.data.domain.*;
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
    // DASHBOARD
    // ======================

    public long countByOwner(User owner) {
        return productRepository.countByOwner(owner);
    }

    public List<Product> findTop5ByOwner(User owner) {
        return productRepository.findByOwner(owner)
                .stream()
                .sorted(Comparator.comparing(Product::getId).reversed())
                .limit(5)
                .collect(Collectors.toList());
    }

    public long totalInventoryValue(User owner) {
        Long total = productRepository.sumTotalValueByOwner(owner);
        return total != null ? total : 0L;
    }

    public long countActive(User owner) {
        return productRepository.countActiveByOwner(owner);
    }

    public long countInactive(User owner) {
        return productRepository.countInactiveByOwner(owner);
    }

    public long lowStock(User owner) {
        return productRepository.countLowStockByOwner(owner);
    }

    public List<Product> getLowStockProducts(User owner) {
        return productRepository.findByOwner(owner)
                .stream()
                .filter(p -> p.getStock() < 10)
                .collect(Collectors.toList());
    }

    public Map<String, Long> countByCategory(User owner) {
        return productRepository.findByOwner(owner)
                .stream()
                .collect(Collectors.groupingBy(
                        p -> p.getCategory() != null ? p.getCategory().getName() : "Tanpa Category",
                        Collectors.counting()
                ));
    }

    // ======================
    // PAGINATION (FINAL FIX)
    // ======================

    public Page<Product> findPaginated(User owner, int page, int size,
                                       String search, Boolean active, Long categoryId) {

        // 🔥 FIX: JANGAN pakai (page - 1)
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        boolean hasSearch = search != null && !search.trim().isEmpty();

        // 1. ALL FILTER
        if (hasSearch && active != null && categoryId != null) {
            return productRepository.findByOwnerAndNameContainingIgnoreCaseAndActiveAndCategoryId(
                    owner, search, active, categoryId, pageable);
        }

        // 2. SEARCH + ACTIVE
        if (hasSearch && active != null) {
            return productRepository.findByOwnerAndNameContainingIgnoreCaseAndActive(
                    owner, search, active, pageable);
        }

        // 3. SEARCH + CATEGORY
        if (hasSearch && categoryId != null) {
            return productRepository.findByOwnerAndNameContainingIgnoreCaseAndCategoryId(
                    owner, search, categoryId, pageable);
        }

        // 4. ACTIVE + CATEGORY
        if (active != null && categoryId != null) {
            return productRepository.findByOwnerAndActiveAndCategoryId(
                    owner, active, categoryId, pageable);
        }

        // 5. SEARCH ONLY
        if (hasSearch) {
            return productRepository.findByOwnerAndNameContainingIgnoreCase(
                    owner, search, pageable);
        }

        // 6. ACTIVE ONLY
        if (active != null) {
            return productRepository.findByOwnerAndActive(owner, active, pageable);
        }

        // 7. CATEGORY ONLY
        if (categoryId != null) {
            return productRepository.findByOwnerAndCategoryId(owner, categoryId, pageable);
        }

        // 8. NO FILTER
        return productRepository.findByOwner(owner, pageable);
    }
}