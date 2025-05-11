package com.fst.first.controller;

import com.fst.first.model.User;
import com.fst.first.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;

@Controller
@RequestMapping("/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private static final String USERS_LIST_VIEW = "users/list";
    private static final String USER_ADD_VIEW = "users/add";
    private static final String USER_EDIT_VIEW = "users/edit";
    private static final String REDIRECT_USERS = "redirect:/admin/users";

    private static final String SUCCESS_ADD = "Utilisateur ajouté avec succès!";
    private static final String SUCCESS_UPDATE = "Utilisateur mis à jour avec succès!";
    private static final String SUCCESS_DELETE = "Utilisateur supprimé avec succès!";
    private static final String ERROR_PREFIX = "Erreur: ";

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String listUsers(Model model) {
        model.addAttribute("users", userService.findAllUsers());
        model.addAttribute("roles", User.Role.values());
        return USERS_LIST_VIEW;
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("roles", User.Role.values());
        return USER_ADD_VIEW;
    }

    @PostMapping("/add")
    public String addUser( @ModelAttribute User user, 
                        BindingResult bindingResult,
                        RedirectAttributes redirectAttributes,
                        Model model) {
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("roles", User.Role.values());
            return USER_ADD_VIEW;
        }

        try {
            userService.saveUser(user);
            redirectAttributes.addFlashAttribute("success", SUCCESS_ADD);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", ERROR_PREFIX + e.getMessage());
            model.addAttribute("roles", User.Role.values());
            return USER_ADD_VIEW;
        }
        
        return REDIRECT_USERS;
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        User user = userService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ID utilisateur invalide: " + id));
        
        model.addAttribute("user", user);
        model.addAttribute("roles", User.Role.values());
        return USER_EDIT_VIEW;
    }

    @PostMapping("/update")
    public String updateUser( @ModelAttribute User user,
                           BindingResult bindingResult,
                           RedirectAttributes redirectAttributes,
                           Model model) {
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("roles", User.Role.values());
            return USER_EDIT_VIEW;
        }

        try {
            userService.updateUser(user);
            redirectAttributes.addFlashAttribute("success", SUCCESS_UPDATE);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", ERROR_PREFIX + e.getMessage());
            model.addAttribute("roles", User.Role.values());
            return USER_EDIT_VIEW;
        }
        
        return REDIRECT_USERS;
    }

    @PostMapping("/delete/{id}")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("success", SUCCESS_DELETE);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", ERROR_PREFIX + e.getMessage());
        }
        return REDIRECT_USERS;
    }
}