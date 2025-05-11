package com.fst.first.controller;

import com.fst.first.model.Category;
import com.fst.first.repository.CategoryRepository;
import com.fst.first.util.CsvUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@Controller
@RequestMapping("/admin/categories")
public class CategoryController {
    
    private static final String REDIRECT_CATEGORIES = "redirect:/admin/categories";
    private static final String CATEGORIES_FORM = "categories/form";
    private static final String IMPORT_FORM = "categories/import-form";
    
    private final CategoryRepository categoryRepository;
    
    public CategoryController(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @GetMapping
    public String listCategories(Model model) {
        model.addAttribute("categories", categoryRepository.findAll());
        return "categories/list";
    }

    @GetMapping("/add")
    public String showCategoryForm(Model model) {
        model.addAttribute("category", new Category());
        return CATEGORIES_FORM;
    }

    @PostMapping("/save")
    public String saveCategory(@ModelAttribute Category category) {
        categoryRepository.save(category);
        return REDIRECT_CATEGORIES;
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Invalid category id: " + id));
        model.addAttribute("category", category);
        return CATEGORIES_FORM;
    }

    @PostMapping("/delete/{id}")
    public String deleteCategory(@PathVariable Long id) {
        categoryRepository.deleteById(id);
        return REDIRECT_CATEGORIES;
    }
    
    @GetMapping("/export")
    public void exportToCsv(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=categories.csv");
        
        try (PrintWriter writer = response.getWriter()) {
            writer.println("ID,Nom,Description,Nombre de médicaments");
            
            categoryRepository.findAll().forEach(category -> 
                writer.println(String.format("%d,\"%s\",\"%s\",%d",
                    category.getId(),
                    category.getName(),
                    category.getDescription(),
                    category.getMedicaments() != null ? category.getMedicaments().size() : 0))
            );
        }
    }
    
    @GetMapping("/import")
    public String showImportForm() {
        return IMPORT_FORM;
    }
    
    @PostMapping("/import")
    public String importFromCsv(@RequestParam("file") MultipartFile file,
                              RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Veuillez sélectionner un fichier à importer");
            return "redirect:/admin/categories/import";
        }

        try {
            List<Category> categories = CsvUtil.parseCategories(file.getInputStream());
            categoryRepository.saveAll(categories);
            redirectAttributes.addFlashAttribute("success", 
                String.format("%d catégories importées avec succès", categories.size()));
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", 
                "Échec de l'importation: " + e.getMessage());
        }

        return REDIRECT_CATEGORIES;
    }
}