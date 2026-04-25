package com.example.productcrud.controller;

import com.example.productcrud.model.User;
import com.example.productcrud.repository.UserRepository;
import com.example.productcrud.service.ProductService;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ProfileController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProductService productService;

    public ProfileController(UserRepository userRepository,
                             PasswordEncoder passwordEncoder,
                             ProductService productService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.productService = productService;
    }

    @GetMapping("/profile")
    public String showProfile(Authentication auth, Model model, RedirectAttributes redirectAttributes) {

        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }

        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));

        // Statistik user
        long totalProducts = productService.countByOwner(user);
        long activeProducts = productService.countActive(user);
        long totalStock = productService.totalInventoryValue(user);

        model.addAttribute("user", user);
        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("activeProducts", activeProducts);
        model.addAttribute("totalStock", totalStock);

        return "profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(Authentication auth,
                                @RequestParam String name,
                                @RequestParam(required = false) String email,
                                @RequestParam(required = false) String phone,
                                RedirectAttributes redirectAttributes) {

        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }

        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));

        // Update profile
        user.setName(name);
        if (email != null && !email.isEmpty()) {
            user.setEmail(email);
        }
        if (phone != null && !phone.isEmpty()) {
            user.setPhone(phone);
        }

        userRepository.save(user);

        redirectAttributes.addFlashAttribute("successMessage", "Profile berhasil diupdate!");

        return "redirect:/profile";
    }

    @PostMapping("/profile/change-password")
    public String changePassword(Authentication auth,
                                 @RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 RedirectAttributes redirectAttributes) {

        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }

        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));

        // Cek password lama
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Password saat ini salah!");
            return "redirect:/profile";
        }

        // Cek password baru dan konfirmasi
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Password baru dan konfirmasi tidak cocok!");
            return "redirect:/profile";
        }

        // Cek panjang password minimal 6 karakter
        if (newPassword.length() < 6) {
            redirectAttributes.addFlashAttribute("errorMessage", "Password baru minimal 6 karakter!");
            return "redirect:/profile";
        }

        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        redirectAttributes.addFlashAttribute("successMessage", "Password berhasil diubah!");

        return "redirect:/profile";
    }
}