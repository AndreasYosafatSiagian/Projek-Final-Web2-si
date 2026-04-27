package com.example.productcrud.service;

import com.example.productcrud.model.Category;
import com.example.productcrud.model.Product;
import com.example.productcrud.model.User;
import com.example.productcrud.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
        return productRepository.countByOwner(owner);
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
    // PAGINATION WITH SEARCH, ACTIVE & CATEGORY FILTERS
    // ======================

    /**
     * Get paginated products with search, active status, and category filters
     * @param owner Current logged in user
     * @param page Page number (starting from 1)
     * @param size Items per page (default 10)
     * @param search Search keyword for product name (partial match, case-insensitive)
     * @param active Filter by active status (null = all, true = active, false = inactive)
     * @param categoryId Filter by category ID (null = all categories)
     * @return Page of products
     */
    public Page<Product> findPaginated(User owner, int page, int size,
                                       String search, Boolean active, Long categoryId) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        // Case 1: All filters (search + active + category)
        if (search != null && !search.isEmpty() && active != null && categoryId != null) {
            return productRepository.findByOwnerAndNameContainingIgnoreCaseAndActiveAndCategoryId(
                    owner, search, active, categoryId, pageable);
        }
        // Case 2: Search + active (no category)
        else if (search != null && !search.isEmpty() && active != null) {
            return productRepository.findByOwnerAndNameContainingIgnoreCaseAndActive(
                    owner, search, active, pageable);
        }
        // Case 3: Search + category (no active)
        else if (search != null && !search.isEmpty() && categoryId != null) {
            return productRepository.findByOwnerAndNameContainingIgnoreCaseAndCategoryId(
                    owner, search, categoryId, pageable);
        }
        // Case 4: Active + category (no search)
        else if (active != null && categoryId != null) {
            return productRepository.findByOwnerAndActiveAndCategoryId(
                    owner, active, categoryId, pageable);
        }
        // Case 5: Search only
        else if (search != null && !search.isEmpty()) {
            return productRepository.findByOwnerAndNameContainingIgnoreCase(owner, search, pageable);
        }
        // Case 6: Active only
        else if (active != null) {
            return productRepository.findByOwnerAndActive(owner, active, pageable);
        }
        // Case 7: Category only
        else if (categoryId != null) {
            return productRepository.findByOwnerAndCategoryId(owner, categoryId, pageable);
        }
        // Case 8: No filters
        else {
            return productRepository.findByOwner(owner, pageable);
        }
    }

    /**
     * Get total number of pages based on filters
     */
    public int getTotalPages(User owner, int size, String search, Boolean active, Long categoryId) {
        long totalItems;

        // Case 1: All filters
        if (search != null && !search.isEmpty() && active != null && categoryId != null) {
            totalItems = productRepository.countByOwnerAndNameContainingIgnoreCaseAndActiveAndCategoryId(
                    owner, search, active, categoryId);
        }
        // Case 2: Search + active
        else if (search != null && !search.isEmpty() && active != null) {
            totalItems = productRepository.countByOwnerAndNameContainingIgnoreCaseAndActive(owner, search, active);
        }
        // Case 3: Search + category
        else if (search != null && !search.isEmpty() && categoryId != null) {
            totalItems = productRepository.countByOwnerAndNameContainingIgnoreCaseAndCategoryId(owner, search, categoryId);
        }
        // Case 4: Active + category
        else if (active != null && categoryId != null) {
            totalItems = productRepository.countByOwnerAndActiveAndCategoryId(owner, active, categoryId);
        }
        // Case 5: Search only
        else if (search != null && !search.isEmpty()) {
            totalItems = productRepository.countByOwnerAndNameContainingIgnoreCase(owner, search);
        }
        // Case 6: Active only
        else if (active != null) {
            totalItems = productRepository.countByOwnerAndActive(owner, active);
        }
        // Case 7: Category only
        else if (categoryId != null) {
            totalItems = productRepository.countByOwnerAndCategoryId(owner, categoryId);
        }
        // Case 8: No filters
        else {
            totalItems = productRepository.countByOwner(owner);
        }

        return (int) Math.ceil((double) totalItems / size);
    }
}