package com.fst.first.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class Medicament {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nom;
    private int quantite;
    private double prix;
    
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;
    
    private LocalDate dateValidite;
    private String imagePath;
    
    public Medicament() {}
    
    public Medicament(String nom, int quantite, double prix, Category category, LocalDate dateValidite) {
        this.nom = nom;
        this.quantite = quantite;
        this.prix = prix;
        this.category = category;
        this.dateValidite = dateValidite;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public int getQuantite() {
        return quantite;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }

    public double getPrix() {
        return prix;
    }

    public void setPrix(double prix) {
        this.prix = prix;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public LocalDate getDateValidite() {
        return dateValidite;
    }

    public void setDateValidite(LocalDate dateValidite) {
        this.dateValidite = dateValidite;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    @Override
    public String toString() {
        return "Medicament{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", quantite=" + quantite +
                ", prix=" + prix +
                ", category=" + (category != null ? category.getName() : "null") +
                ", dateValidite=" + dateValidite +
                ", imagePath='" + imagePath + '\'' +
                '}';
    }
}