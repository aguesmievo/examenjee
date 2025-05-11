package com.fst.first.util;

import com.fst.first.model.Medicament;
import com.fst.first.model.Category;
import com.fst.first.repository.CategoryRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CsvUtil {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static List<CSVRecord> parseCsv(InputStream inputStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            return CSVFormat.DEFAULT.builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .setIgnoreHeaderCase(true)
                    .setTrim(true)
                    .build()
                    .parse(reader)
                    .getRecords();
        }
    }

    public static List<Category> parseCategories(InputStream inputStream) throws IOException {
        List<Category> categories = new ArrayList<>();
        for (CSVRecord record : parseCsv(inputStream)) {
            Category category = new Category();
            category.setId(getLongValue(record, "ID", "Id"));
            category.setName(getStringValue(record, "Name", "Nom"));
            category.setDescription(getStringValue(record, "Description", "Description"));
            categories.add(category);
        }
        return categories;
    }

    public static List<Medicament> parseMedicaments(MultipartFile file,
                                                   CategoryRepository categoryRepository,
                                                   Long defaultCategoryId) throws IOException {
        List<Medicament> medicaments = new ArrayList<>();
        for (CSVRecord record : parseCsv(file.getInputStream())) {
            try {
                Medicament medicament = new Medicament();
                medicament.setNom(getStringValue(record, "Nom", "Name"));
                medicament.setQuantite(getIntValue(record, "Quantité", "Quantity"));
                medicament.setPrix(getDoubleValue(record, "Prix", "Price"));
                medicament.setDateValidite(getDateValue(record, "Date de validité", "ExpiryDate"));
                medicament.setImagePath(getStringValue(record, "Image", "ImagePath"));

                String categoryName = getStringValue(record, "Catégorie", "Category");
                if (categoryName != null && !categoryName.isEmpty()) {
                    Category category = categoryRepository.findByName(categoryName)
                            .orElse(defaultCategoryId != null ? 
                                   categoryRepository.findById(defaultCategoryId).orElse(null) : 
                                   null);
                    medicament.setCategory(category);
                }

                medicaments.add(medicament);
            } catch (Exception e) {
                System.err.println("Error parsing medicament record: " + e.getMessage());
            }
        }
        return medicaments;
    }

    private static String getStringValue(CSVRecord record, String... possibleHeaders) {
        for (String header : possibleHeaders) {
            try {
                if (record.isSet(header)) {
                    String value = record.get(header);
                    return value != null && !value.isEmpty() ? value : null;
                }
            } catch (IllegalArgumentException e) {
            }
        }
        return null;
    }

    private static Integer getIntValue(CSVRecord record, String... possibleHeaders) {
        String value = getStringValue(record, possibleHeaders);
        return value != null ? Integer.parseInt(value) : null;
    }

    private static Long getLongValue(CSVRecord record, String... possibleHeaders) {
        String value = getStringValue(record, possibleHeaders);
        return value != null ? Long.parseLong(value) : null;
    }

    private static Double getDoubleValue(CSVRecord record, String... possibleHeaders) {
        String value = getStringValue(record, possibleHeaders);
        return value != null ? Double.parseDouble(value) : null;
    }

    private static LocalDate getDateValue(CSVRecord record, String... possibleHeaders) {
        String value = getStringValue(record, possibleHeaders);
        return value != null ? LocalDate.parse(value, DATE_FORMATTER) : null;
    }
}