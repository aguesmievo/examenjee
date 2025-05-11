package com.fst.first.controller;

import com.fst.first.model.Medicament;
import com.fst.first.model.Category;
import com.fst.first.repository.MedicamentRepository;
import com.fst.first.repository.CategoryRepository;
import com.fst.first.util.CsvUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class PharmacieController {

    private static final String MEDICAMENTS_FORM = "medicaments/formulaire-ajout";
    private static final String MEDICAMENTS_LIST = "medicaments/liste";
    private static final String IMPORT_FORM = "medicaments/import-form";
    private static final String REDIRECT_MEDICAMENTS = "redirect:/admin/medicaments";

    private final MedicamentRepository medicamentRepository;
    private final CategoryRepository categoryRepository;
    private final String uploadDirectory;

    public PharmacieController(MedicamentRepository medicamentRepository,
                             CategoryRepository categoryRepository,
                             @Value("${upload.directory}") String uploadDirectory) {
        this.medicamentRepository = medicamentRepository;
        this.categoryRepository = categoryRepository;
        this.uploadDirectory = uploadDirectory;
    }

    @GetMapping
    public String homeRedirect() {
        return REDIRECT_MEDICAMENTS;
    }

    @GetMapping("/medicaments")
    public String listeMedicaments(Model model) {
        model.addAttribute("medicaments", medicamentRepository.findAll());
        model.addAttribute("allCategories", categoryRepository.findAll());
        model.addAttribute("searchForm", new SearchForm());
        return MEDICAMENTS_LIST;
    }

    @GetMapping("/medicaments/ajouter")
    public String showAddForm(Model model) {
        model.addAttribute("medicament", new Medicament());
        model.addAttribute("categories", categoryRepository.findAll());
        return MEDICAMENTS_FORM;
    }

    @PostMapping("/medicaments/ajouter")
    public String addMedicament(@ModelAttribute("medicament") Medicament medicament,
                              BindingResult result,
                              @RequestParam("image") MultipartFile file,
                              @RequestParam("categoryId") Long categoryId,
                              Model model) throws IOException {
        
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryRepository.findAll());
            return MEDICAMENTS_FORM;
        }
        
        try {
            Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Catégorie invalide"));
            
            medicament.setCategory(category);
            handleImageUpload(medicament, file);
            setDefaultExpiryDateIfNull(medicament);
            
            medicamentRepository.save(medicament);
            return REDIRECT_MEDICAMENTS;
            
        } catch (IllegalArgumentException | IOException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("categories", categoryRepository.findAll());
            return MEDICAMENTS_FORM;
        }
    }

    @GetMapping("/medicaments/modifier/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Medicament medicament = medicamentRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Médicament invalide"));
        
        model.addAttribute("medicament", medicament);
        model.addAttribute("categories", categoryRepository.findAll());
        return MEDICAMENTS_FORM;
    }
    
    @PostMapping("/medicaments/modifier/{id}")
    public String updateMedicament(@PathVariable Long id,
                                 @ModelAttribute("medicament") Medicament medicament,
                                 BindingResult result,
                                 @RequestParam(value = "image", required = false) MultipartFile file,
                                 @RequestParam("categoryId") Long categoryId,
                                 Model model) throws IOException {
        
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryRepository.findAll());
            return MEDICAMENTS_FORM;
        }
        
        try {
            Medicament existingMedicament = medicamentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Médicament invalide"));
            
            Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Catégorie invalide"));
            
            updateMedicamentDetails(existingMedicament, medicament, category);
            handleImageUpload(existingMedicament, file);
            setDefaultExpiryDateIfNull(existingMedicament);
            
            medicamentRepository.save(existingMedicament);
            return REDIRECT_MEDICAMENTS;
            
        } catch (IllegalArgumentException | IOException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("categories", categoryRepository.findAll());
            return MEDICAMENTS_FORM;
        }
    }

    @GetMapping("/medicaments/rechercher")
    public String searchMedicaments(@ModelAttribute SearchForm searchForm, Model model) {
        List<Medicament> results = searchForm.getCategoryId() != null ?
            medicamentRepository.findByNomContainingIgnoreCaseAndCategoryId(
                searchForm.getSearchTerm(),
                searchForm.getCategoryId()) :
            medicamentRepository.findByNomContainingIgnoreCase(searchForm.getSearchTerm());
        
        model.addAttribute("medicaments", results);
        model.addAttribute("allCategories", categoryRepository.findAll());
        return MEDICAMENTS_LIST;
    }

    @PostMapping("/medicaments/supprimer/{id}")
    public String deleteMedicament(@PathVariable Long id) {
        Medicament medicament = medicamentRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Médicament invalide"));
        
        deleteImageIfExists(medicament);
        medicamentRepository.delete(medicament);
        return REDIRECT_MEDICAMENTS;
    }
    
    @GetMapping("/medicaments/export")
    public void exportMedicaments(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=medicaments.csv");
        
        try (PrintWriter writer = response.getWriter()) {
            writer.println("ID,Nom,Quantité,Prix,Catégorie,Date de validité,Image");
            
            medicamentRepository.findAll().forEach(medicament -> 
                writer.println(String.format("%d,\"%s\",%d,%.2f,\"%s\",%s,%s",
                    medicament.getId(),
                    medicament.getNom(),
                    medicament.getQuantite(),
                    medicament.getPrix(),
                    medicament.getCategory() != null ? medicament.getCategory().getName() : "",
                    medicament.getDateValidite(),
                    medicament.getImagePath() != null ? medicament.getImagePath() : ""))
            );
        }
    }
    
    @GetMapping("/medicaments/import")
    public String showMedicamentImportForm(Model model) {
        model.addAttribute("categories", categoryRepository.findAll());
        return IMPORT_FORM;
    }
    
    @PostMapping("/medicaments/import")
    public String importMedicaments(@RequestParam("file") MultipartFile file,
                                  @RequestParam(value = "defaultCategoryId", required = false) Long defaultCategoryId,
                                  RedirectAttributes redirectAttributes) {
        
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Veuillez sélectionner un fichier à importer");
            return "redirect:/admin/medicaments/import";
        }

        try {
            List<Medicament> medicaments = CsvUtil.parseMedicaments(file, categoryRepository, defaultCategoryId);
            medicamentRepository.saveAll(medicaments);
            redirectAttributes.addFlashAttribute("success", 
                String.format("%d médicaments importés avec succès", medicaments.size()));
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", 
                "Échec de l'importation: " + e.getMessage());
        }

        return REDIRECT_MEDICAMENTS;
    }

    private void handleImageUpload(Medicament medicament, MultipartFile file) throws IOException {
        if (file != null && !file.isEmpty()) {
            deleteOldImageIfExists(medicament);
            
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path filePath = Paths.get(uploadDirectory).resolve(fileName);
            Files.copy(file.getInputStream(), filePath);
            medicament.setImagePath(fileName);
        }
    }

    private void deleteOldImageIfExists(Medicament medicament) throws IOException {
        if (medicament.getImagePath() != null) {
            Path oldFilePath = Paths.get(uploadDirectory).resolve(medicament.getImagePath());
            Files.deleteIfExists(oldFilePath);
        }
    }

    private void deleteImageIfExists(Medicament medicament) {
        if (medicament.getImagePath() != null && !medicament.getImagePath().isEmpty()) {
            try {
                Path filePath = Paths.get(uploadDirectory).resolve(medicament.getImagePath());
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                throw new RuntimeException("Erreur lors de la suppression de l'image", e);
            }
        }
    }

    private void setDefaultExpiryDateIfNull(Medicament medicament) {
        if (medicament.getDateValidite() == null) {
            medicament.setDateValidite(LocalDate.now().plusYears(2));
        }
    }

    private void updateMedicamentDetails(Medicament existing, Medicament updated, Category category) {
        existing.setNom(updated.getNom());
        existing.setQuantite(updated.getQuantite());
        existing.setPrix(updated.getPrix());
        existing.setDateValidite(updated.getDateValidite());
        existing.setCategory(category);
    }

    public static class SearchForm {
        private String searchTerm;
        private Long categoryId;

        public String getSearchTerm() { return searchTerm; }
        public void setSearchTerm(String searchTerm) { this.searchTerm = searchTerm; }
        public Long getCategoryId() { return categoryId; }
        public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    }
}