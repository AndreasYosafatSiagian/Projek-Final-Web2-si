package com.example.productcrud.controller;

import com.example.productcrud.model.User;
import com.example.productcrud.repository.UserRepository;
import com.example.productcrud.service.ProductService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final ProductService productService;
    private final UserRepository userRepository;

    public HomeController(ProductService productService, UserRepository userRepository) {
        this.productService = productService;
        this.userRepository = userRepository;
    }

    @GetMapping({"/", "/home"})
    public String home(Authentication auth, Model model) {

        // Jika user sudah login, tampilkan statistik
        if (auth != null && auth.isAuthenticated()) {
            User user = userRepository.findByUsername(auth.getName())
                    .orElse(null);

            if (user != null) {
                model.addAttribute("totalProduct", productService.countByOwner(user));
                model.addAttribute("active", productService.countActive(user));
                model.addAttribute("lowStock", productService.lowStock(user));
            }
        } else {
            // Jika belum login, set default 0
            model.addAttribute("totalProduct", 0);
            model.addAttribute("active", 0);
            model.addAttribute("lowStock", 0);
        }

        return "home";
    }
}