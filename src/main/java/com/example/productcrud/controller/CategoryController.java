package com.example.productcrud.controller;

import com.example.productcrud.model.Category;
import com.example.productcrud.model.User;
import com.example.productcrud.service.CategoryService;
import com.example.productcrud.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private UserRepository userRepository;

    // Helper method untuk mendapatkan User entity dari UserDetails
    private User getCurrentUser(UserDetails userDetails) {
        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));
    }

    // ── LIST ──────────────────────────────────────────────────────
    @GetMapping
    public String list(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User currentUser = getCurrentUser(userDetails);
        model.addAttribute("categories", categoryService.getAllByUser(currentUser.getId()));
        return "category/list";
    }

    // ── TAMBAH ──
    @GetMapping("/new")
    public String showCreateForm(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User currentUser = getCurrentUser(userDetails);
        Category category = new Category();
        category.setUserId(currentUser.getId());
        model.addAttribute("category", category);
        return "category/form";
    }

    // ── EDIT ──
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id,
                               @AuthenticationPrincipal UserDetails userDetails,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser(userDetails);

        return categoryService.getByIdAndUser(id, currentUser.getId())
                .map(category -> {
                    model.addAttribute("category", category);
                    return "category/form";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Category tidak ditemukan.");
                    return "redirect:/categories";
                });
    }

    // ── SAVE ──
    @PostMapping("/save")
    public String saveCategory(@ModelAttribute Category category,
                               @AuthenticationPrincipal UserDetails userDetails,
                               RedirectAttributes redirectAttributes) {
        try {
            User currentUser = getCurrentUser(userDetails);

            if (category.getId() == null) {
                category.setUserId(currentUser.getId());
                categoryService.save(category);
                redirectAttributes.addFlashAttribute("successMessage", "Category berhasil ditambahkan.");
            } else {
                categoryService.update(category.getId(), category, currentUser.getId());
                redirectAttributes.addFlashAttribute("successMessage", "Category berhasil diupdate.");
            }
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            if (category.getId() == null) {
                return "redirect:/categories/new";
            } else {
                return "redirect:/categories/edit/" + category.getId();
            }
        }
        return "redirect:/categories";
    }

    // ── HAPUS ──
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id,
                         @AuthenticationPrincipal UserDetails userDetails,
                         RedirectAttributes redirectAttributes) {
        try {
            User currentUser = getCurrentUser(userDetails);
            categoryService.delete(id, currentUser.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Category berhasil dihapus.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/categories";
    }
}