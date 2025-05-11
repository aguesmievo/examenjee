package com.fst.first.repository;

import com.fst.first.model.Medicament;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MedicamentRepository extends JpaRepository<Medicament, Long> {
    List<Medicament> findByNomContainingIgnoreCase(String searchTerm);
    List<Medicament> findByNomContainingIgnoreCaseAndCategoryId(String searchTerm, Long categoryId);
}