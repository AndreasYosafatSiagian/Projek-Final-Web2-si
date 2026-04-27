package com.example.productcrud.service;

import com.example.productcrud.model.Category;
import com.example.productcrud.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    // ── READ ──────────────────────────────────────────────────────

    // Ambil semua category milik user
    public List<Category> getAllByUser(Long userId) {
        return categoryRepository.findByUserId(userId);
    }

    // Ambil 1 category by id, wajib milik user tersebut
    public Optional<Category> getByIdAndUser(Long id, Long userId) {
        return categoryRepository.findByIdAndUserId(id, userId);
    }

    // ── CREATE ──────────────────────────────────────────────

    public Category save(Category category) {
        // Validasi nama tidak boleh duplikat untuk user yang sama
        if (categoryRepository.existsByNameAndUserId(category.getName(), category.getUserId())) {
            throw new IllegalArgumentException("Nama category '" + category.getName() + "' sudah ada.");
        }
        return categoryRepository.save(category);
    }

    // ── UPDATE ────────────────────────────────────────────────────

    public Category update(Long id, Category updated, Long userId) {
        Category existing = categoryRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new IllegalArgumentException("Category tidak ditemukan."));

        // Validasi nama tidak duplikat, exclude id milik sendiri
        if (categoryRepository.existsByNameAndUserIdAndIdNot(updated.getName(), userId, id)) {
            throw new IllegalArgumentException("Nama category '" + updated.getName() + "' sudah ada.");
        }

        existing.setName(updated.getName());
        existing.setDescription(updated.getDescription());

        return categoryRepository.save(existing);
    }

    // ── DELETE ────────────────────────────────────────────────────

    public void delete(Long id, Long userId) {
        Category existing = categoryRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new IllegalArgumentException("Category tidak ditemukan."));

        categoryRepository.delete(existing);
    }
}