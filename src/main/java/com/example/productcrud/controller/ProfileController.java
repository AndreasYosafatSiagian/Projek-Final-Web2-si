package com.example.productcrud.controller;

import com.example.productcrud.model.User;
import com.example.productcrud.repository.UserRepository;
import com.example.productcrud.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/user")
public class ProfileController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ProductService productService;

    private User getCurrentUser(UserDetails userDetails) {
        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));
    }

    // TAMPILKAN PROFILE - /user/profile
    @GetMapping("/profile")
    public String viewProfile(@AuthenticationPrincipal UserDetails userDetails,
                              Model model,
                              RedirectAttributes redirectAttributes) {

        if (userDetails == null) {
            return "redirect:/login";
        }

        User user = getCurrentUser(userDetails);

        // Statistik user
        long totalProducts = productService.countByOwner(user);
        long activeProducts = productService.countActive(user);
        long totalStock = productService.totalInventoryValue(user);

        model.addAttribute("user", user);
        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("activeProducts", activeProducts);
        model.addAttribute("totalStock", totalStock);

        return "profile/view";
    }

    // FORM EDIT PROFILE - /user/profile/edit
    @GetMapping("/profile/edit")
    public String showEditForm(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        User user = getCurrentUser(userDetails);
        model.addAttribute("user", user);
        return "profile/edit";
    }

    // PROSES UPDATE PROFILE - /user/profile/edit (POST)
    @PostMapping("/profile/edit")
    public String updateProfile(@AuthenticationPrincipal UserDetails userDetails,
                                @RequestParam(required = false) String fullName,
                                @RequestParam(required = false) String email,
                                @RequestParam(required = false) String phoneNumber,
                                @RequestParam(required = false) String address,
                                @RequestParam(required = false) String bio,
                                RedirectAttributes redirectAttributes) {

        if (userDetails == null) {
            return "redirect:/login";
        }

        try {
            User user = getCurrentUser(userDetails);

            // Update profile fields
            if (fullName != null && !fullName.isEmpty()) {
                user.setFullName(fullName);
            }
            if (email != null && !email.isEmpty()) {
                user.setEmail(email);
            }
            if (phoneNumber != null && !phoneNumber.isEmpty()) {
                user.setPhoneNumber(phoneNumber);
            }
            if (address != null && !address.isEmpty()) {
                user.setAddress(address);
            }
            if (bio != null && !bio.isEmpty()) {
                user.setBio(bio);
            }

            userRepository.save(user);
            redirectAttributes.addFlashAttribute("successMessage", "Profile berhasil diupdate!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Gagal mengupdate profile: " + e.getMessage());
        }

        return "redirect:/user/profile";
    }

    // CHANGE PASSWORD - /user/profile/change-password
    @PostMapping("/profile/change-password")
    public String changePassword(@AuthenticationPrincipal UserDetails userDetails,
                                 @RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 RedirectAttributes redirectAttributes) {

        if (userDetails == null) {
            return "redirect:/login";
        }

        User user = getCurrentUser(userDetails);

        // Cek password lama
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Password saat ini salah!");
            return "redirect:/user/profile";
        }

        // Cek password baru dan konfirmasi
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Password baru dan konfirmasi tidak cocok!");
            return "redirect:/user/profile";
        }

        // Cek panjang password minimal 6 karakter
        if (newPassword.length() < 6) {
            redirectAttributes.addFlashAttribute("errorMessage", "Password baru minimal 6 karakter!");
            return "redirect:/user/profile";
        }

        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        redirectAttributes.addFlashAttribute("successMessage", "Password berhasil diubah!");
        return "redirect:/user/profile";
    }
}