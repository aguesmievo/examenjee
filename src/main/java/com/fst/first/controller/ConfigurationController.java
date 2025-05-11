package com.fst.first.controller;

import com.fst.first.model.SiteConfiguration;
import com.fst.first.service.SiteConfigurationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/configuration")
@PreAuthorize("hasRole('ADMIN')")
public class ConfigurationController {
    
    private static final String CONFIG_FORM_VIEW = "configuration/form";
    private static final String REDIRECT_CONFIG = "redirect:/admin/configuration";
    private static final String SUCCESS_MESSAGE = "Configuration mise à jour avec succès !";
    
    private final SiteConfigurationService configService;
    
    public ConfigurationController(SiteConfigurationService configService) {
        this.configService = configService;
    }
    
    @GetMapping
    public String showConfigForm(Model model) {
        model.addAttribute("config", configService.getCurrentConfiguration());
        return CONFIG_FORM_VIEW;
    }
    
    @PostMapping
    public String updateConfig(@ModelAttribute SiteConfiguration config, 
                             RedirectAttributes redirectAttributes) {
        configService.updateConfiguration(config);
        redirectAttributes.addFlashAttribute("success", SUCCESS_MESSAGE);
        return REDIRECT_CONFIG;
    }
}