package com.example.productcrud.controller;

import com.example.productcrud.model.Category;
import com.example.productcrud.model.Product;
import com.example.productcrud.model.User;
import com.example.productcrud.repository.UserRepository;
import com.example.productcrud.service.CategoryService;
import com.example.productcrud.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
public class ProductController {

    private final ProductService productService;
    private final UserRepository userRepository;
    private final CategoryService categoryService;

    public ProductController(ProductService productService,
                             UserRepository userRepository,
                             CategoryService categoryService) {
        this.productService = productService;
        this.userRepository = userRepository;
        this.categoryService = categoryService;
    }

    // ======================
    // GET CURRENT USER
    // ======================
    private User getCurrentUser(UserDetails userDetails) {
        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));
    }

    // ======================
    // LIST PRODUCT (FINAL FIX)
    // ======================
    @GetMapping("/products")
    public String listProducts(@AuthenticationPrincipal UserDetails userDetails,
                               @RequestParam(defaultValue = "1") int page,
                               @RequestParam(defaultValue = "") String search,
                               @RequestParam(required = false) Boolean active,
                               @RequestParam(required = false) Long categoryId,
                               Model model) {

        User currentUser = getCurrentUser(userDetails);

        int pageSize = 10;

        // 🔥 convert ke 0-based (WAJIB)
        int currentPage = (page < 1) ? 0 : page - 1;

        Page<Product> productPage = productService.findPaginated(
                currentUser,
                currentPage,
                pageSize,
                search,
                active,
                categoryId
        );

        int totalPages = productPage.getTotalPages();

        // range pagination
        int startPage = Math.max(1, page - 2);
        int endPage = Math.min(totalPages, page + 2);

        List<Category> categories = categoryService.getAllByUser(currentUser.getId());

        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalItems", productPage.getTotalElements());
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);

        // 🔥 penting (biar filter tidak hilang)
        model.addAttribute("search", search);
        model.addAttribute("activeFilter", active);
        model.addAttribute("categoryId", categoryId);

        model.addAttribute("categories", categories);

        return "product/list";
    }

    // ======================
    // DETAIL
    // ======================
    @GetMapping("/products/{id}")
    public String detailProduct(@PathVariable Long id,
                                @AuthenticationPrincipal UserDetails userDetails,
                                Model model,
                                RedirectAttributes redirectAttributes) {

        User currentUser = getCurrentUser(userDetails);

        return productService.findByIdAndOwner(id, currentUser)
                .map(product -> {
                    model.addAttribute("product", product);
                    return "product/detail";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Produk tidak ditemukan");
                    return "redirect:/products";
                });
    }

    // ======================
    // CREATE
    // ======================
    @GetMapping("/products/new")
    public String showCreateForm(@AuthenticationPrincipal UserDetails userDetails,
                                 Model model) {

        User currentUser = getCurrentUser(userDetails);

        Product product = new Product();
        product.setCreatedAt(LocalDate.now());

        model.addAttribute("product", product);
        model.addAttribute("categories",
                categoryService.getAllByUser(currentUser.getId()));

        return "product/form";
    }

    // ======================
    // SAVE
    // ======================
    @PostMapping("/products/save")
    public String saveProduct(@ModelAttribute Product product,
                              @RequestParam(value = "categoryId", required = false) Long categoryId,
                              @AuthenticationPrincipal UserDetails userDetails,
                              RedirectAttributes redirectAttributes) {

        User currentUser = getCurrentUser(userDetails);

        if (product.getId() != null) {
            boolean exists = productService
                    .findByIdAndOwner(product.getId(), currentUser)
                    .isPresent();

            if (!exists) {
                redirectAttributes.addFlashAttribute("errorMessage", "Produk tidak ditemukan");
                return "redirect:/products";
            }
        }

        if (categoryId != null) {
            Category category = categoryService
                    .getByIdAndUser(categoryId, currentUser.getId())
                    .orElse(null);
            product.setCategory(category);
        } else {
            product.setCategory(null);
        }

        product.setOwner(currentUser);
        productService.save(product);

        redirectAttributes.addFlashAttribute("successMessage", "Produk berhasil disimpan!");
        return "redirect:/products";
    }

    // ======================
    // EDIT
    // ======================
    @GetMapping("/products/{id}/edit")
    public String showEditForm(@PathVariable Long id,
                               @AuthenticationPrincipal UserDetails userDetails,
                               Model model,
                               RedirectAttributes redirectAttributes) {

        User currentUser = getCurrentUser(userDetails);

        return productService.findByIdAndOwner(id, currentUser)
                .map(product -> {
                    model.addAttribute("product", product);
                    model.addAttribute("categories",
                            categoryService.getAllByUser(currentUser.getId()));
                    return "product/form";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Produk tidak ditemukan");
                    return "redirect:/products";
                });
    }

    // ======================
    // DELETE
    // ======================
    @PostMapping("/products/{id}/delete")
    public String deleteProduct(@PathVariable Long id,
                                @AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes redirectAttributes) {

        User currentUser = getCurrentUser(userDetails);

        boolean exists = productService
                .findByIdAndOwner(id, currentUser)
                .isPresent();

        if (exists) {
            productService.deleteByIdAndOwner(id, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "Produk berhasil dihapus!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Produk tidak ditemukan");
        }

        return "redirect:/products";
    }
}