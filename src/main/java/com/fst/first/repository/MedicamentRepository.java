package com.fst.first.repository;


import com.fst.first.model.Medicament;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface MedicamentRepository extends CrudRepository<Medicament, Long> {

	List<Medicament> findByNomContainingIgnoreCase(String searchTerm);

	List<Medicament> findByNomContainingIgnoreCaseAndCategoryId(String searchTerm, Long categoryId);
}