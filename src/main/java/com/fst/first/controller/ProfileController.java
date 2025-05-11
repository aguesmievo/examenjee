package com.fst.first.controller;

import com.fst.first.model.User;
import com.fst.first.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private static final String PROFILE_VIEW = "profile/view";
    private static final String PROFILE_EDIT_VIEW = "profile/edit";
    private static final String REDIRECT_PROFILE = "redirect:/profile";
    private static final String SUCCESS_MESSAGE = "Profil mis à jour avec succès";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public ProfileController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public String showProfile(Model model) {
        model.addAttribute("user", getCurrentUser());
        return PROFILE_VIEW;
    }

    @GetMapping("/edit")
    public String showEditProfileForm(Model model) {
        model.addAttribute("user", getCurrentUser());
        return PROFILE_EDIT_VIEW;
    }

    @PostMapping("/edit")
    public String updateProfile(@ModelAttribute("user") User updatedUser,
                              BindingResult result,
                              @RequestParam(required = false) String newPassword,
                              RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
            return PROFILE_EDIT_VIEW;
        }

        User currentUser = getCurrentUser();
        updateUserProfile(currentUser, updatedUser, newPassword);
        
        userRepository.save(currentUser);
        redirectAttributes.addFlashAttribute("successMessage", SUCCESS_MESSAGE);
        return REDIRECT_PROFILE;
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
    }

    private void updateUserProfile(User currentUser, User updatedUser, String newPassword) {
        currentUser.setFullName(updatedUser.getFullName());
        currentUser.setEmail(updatedUser.getEmail());

        if (newPassword != null && !newPassword.trim().isEmpty()) {
            currentUser.setPassword(passwordEncoder.encode(newPassword));
        }
    }
}