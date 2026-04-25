package com.example.productcrud.controller;

import com.example.productcrud.model.User;
import com.example.productcrud.repository.UserRepository;
import com.example.productcrud.service.ProductService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final ProductService productService;
    private final UserRepository userRepository;

    public DashboardController(ProductService productService, UserRepository userRepository) {
        this.productService = productService;
        this.userRepository = userRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {

        // ❗ 1. wajib cek login
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }

        // ❗ 2. ambil user dengan aman (jangan pakai null)
        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));

        // 🔥 navbar username (aman)
        model.addAttribute("username", auth.getName());

        // 📊 statistik dashboard
        model.addAttribute("totalProduct", productService.countByOwner(user));
        model.addAttribute("latestProducts", productService.findTop5ByOwner(user));
        model.addAttribute("totalValue", productService.totalInventoryValue(user));
        model.addAttribute("active", productService.countActive(user));
        model.addAttribute("inactive", productService.countInactive(user));
        model.addAttribute("lowStock", productService.lowStock(user));
        model.addAttribute("categories", productService.countByCategory(user));

        return "dashboard";
    }
}