package com.example.productcrud.controller;

import com.example.productcrud.model.User;
import com.example.productcrud.repository.UserRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProfileController {

    private final UserRepository userRepository;

    // Constructor Injection
    public ProfileController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // ======================
    // PROFILE PAGE
    // ======================
    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal UserDetails userDetails, Model model) {

        // kalau belum login → redirect ke login
        if (userDetails == null) {
            return "redirect:/login";
        }

        // ambil user dari database berdasarkan username
        return userRepository.findByUsername(userDetails.getUsername())
                .map(user -> {
                    model.addAttribute("user", user);
                    return "profile"; // arah ke profile.html
                })
                .orElse("redirect:/login"); // kalau user tidak ditemukan
    }
}