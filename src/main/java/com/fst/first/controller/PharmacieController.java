package com.fst.first.controller;

import com.fst.first.model.Medicament;
import com.fst.first.model.Category;
import com.fst.first.repository.MedicamentRepository;
import com.fst.first.repository.CategoryRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class PharmacieController {

    private final MedicamentRepository medicamentRepository;
    private final CategoryRepository categoryRepository;

    @Value("${upload.directory}")
    private String uploadDirectory;

    @Autowired
    public PharmacieController(MedicamentRepository medicamentRepository, 
                             CategoryRepository categoryRepository) {
        this.medicamentRepository = medicamentRepository;
        this.categoryRepository = categoryRepository;
    }

    @GetMapping("")
    public String homeRedirect() {
        return "redirect:/admin/medicaments";
    }

    @GetMapping("/medicaments")
    public String listeMedicaments(Model model) {
        model.addAttribute("medicaments", medicamentRepository.findAll());
        model.addAttribute("allCategories", categoryRepository.findAll());
        model.addAttribute("searchForm", new SearchForm());
        return "medicaments/liste";
    }

    @GetMapping("/medicaments/ajouter")
    public String showAddForm(Model model) {
        model.addAttribute("medicament", new Medicament());
        model.addAttribute("categories", categoryRepository.findAll());
        return "medicaments/formulaire-ajout";
    }

    @PostMapping("/medicaments/ajouter")
    public String addMedicament(@ModelAttribute("medicament") Medicament medicament,
                              BindingResult result,
                              @RequestParam("image") MultipartFile file,
                              @RequestParam("categoryId") Long categoryId,
                              Model model) throws IOException {
        
        // Always add categories to model for error case
        model.addAttribute("categories", categoryRepository.findAll());
        
        if (result.hasErrors()) {
            return "medicaments/formulaire-ajout";
        }
        
        try {
            Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Catégorie invalide"));
            medicament.setCategory(category);
            
            if (!file.isEmpty()) {
                String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                Path filePath = Paths.get(uploadDirectory).resolve(fileName);
                Files.copy(file.getInputStream(), filePath);
                medicament.setImagePath(fileName);
            }
            
            if (medicament.getDateValidite() == null) {
                medicament.setDateValidite(LocalDate.now().plusYears(2));
            }
            
            medicamentRepository.save(medicament);
            return "redirect:/admin/medicaments";
            
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "medicaments/formulaire-ajout";
        } catch (IOException e) {
            model.addAttribute("error", "Erreur lors du traitement de l'image");
            return "medicaments/formulaire-ajout";
        }
    }
    @GetMapping("/medicaments/modifier/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Medicament medicament = medicamentRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Médicament invalide"));
        
        model.addAttribute("medicament", medicament);
        model.addAttribute("categories", categoryRepository.findAll());
        return "medicaments/formulaire-ajout";
    }
    
    @PostMapping("/medicaments/modifier/{id}")
    public String updateMedicament(@PathVariable Long id,
                                 @ModelAttribute("medicament") Medicament medicament,
                                 BindingResult result,
                                 @RequestParam(value = "image", required = false) MultipartFile file,
                                 @RequestParam("categoryId") Long categoryId,
                                 Model model) throws IOException {
        
        model.addAttribute("categories", categoryRepository.findAll());
        
        if (result.hasErrors()) {
            return "medicaments/formulaire-ajout";
        }
        
        try {
            Medicament existingMedicament = medicamentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Médicament invalide"));
            
            Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Catégorie invalide"));
            
            // Update fields
            existingMedicament.setNom(medicament.getNom());
            existingMedicament.setQuantite(medicament.getQuantite());
            existingMedicament.setPrix(medicament.getPrix());
            existingMedicament.setDateValidite(medicament.getDateValidite());
            existingMedicament.setCategory(category);
            
            if (file != null && !file.isEmpty()) {
                if (existingMedicament.getImagePath() != null) {
                    try {
                        Path oldFilePath = Paths.get(uploadDirectory).resolve(existingMedicament.getImagePath());
                        Files.deleteIfExists(oldFilePath);
                    } catch (IOException e) {
                        System.err.println("Erreur lors de la suppression de l'ancienne image: " + e.getMessage());
                    }
                }
                
                // Save new image
                String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                Path filePath = Paths.get(uploadDirectory).resolve(fileName);
                Files.copy(file.getInputStream(), filePath);
                existingMedicament.setImagePath(fileName);
            }
            
            if (existingMedicament.getDateValidite() == null) {
                existingMedicament.setDateValidite(LocalDate.now().plusYears(2));
            }
            
            medicamentRepository.save(existingMedicament);
            return "redirect:/admin/medicaments";
            
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "medicaments/formulaire-ajout";
        } catch (IOException e) {
            model.addAttribute("error", "Erreur lors du traitement de l'image");
            return "medicaments/formulaire-ajout";
        }
    }

    @GetMapping("/medicaments/rechercher")
    public String searchMedicaments(@ModelAttribute SearchForm searchForm, Model model) {
        List<Medicament> results;
        
        if (searchForm.getCategoryId() != null) {
            results = medicamentRepository.findByNomContainingIgnoreCaseAndCategoryId(
                searchForm.getSearchTerm(),
                searchForm.getCategoryId()
            );
        } else {
            results = medicamentRepository.findByNomContainingIgnoreCase(searchForm.getSearchTerm());
        }
        
        model.addAttribute("medicaments", results);
        model.addAttribute("allCategories", categoryRepository.findAll());
        return "medicaments/liste";
    }

    @PostMapping("/medicaments/supprimer/{id}")
    public String deleteMedicament(@PathVariable Long id) {
        Medicament medicament = medicamentRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Médicament invalide"));
        
        // Suppression de l'image associée
        if (medicament.getImagePath() != null && !medicament.getImagePath().isEmpty()) {
            try {
                // Correction: Pas besoin de caster, utilisez simplement la String
                Path filePath = Paths.get(uploadDirectory).resolve(medicament.getImagePath());
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                System.err.println("Erreur lors de la suppression de l'image: " + e.getMessage());
            }
        }
        
        medicamentRepository.delete(medicament);
        return "redirect:/admin/medicaments";
    }

    public static class SearchForm {
        private String searchTerm;
        private Long categoryId; 

        public String getSearchTerm() {
            return searchTerm;
        }

        public void setSearchTerm(String searchTerm) {
            this.searchTerm = searchTerm;
        }

        public Long getCategoryId() {
            return categoryId;
        }

        public void setCategoryId(Long categoryId) {
            this.categoryId = categoryId;
        }
    }
}