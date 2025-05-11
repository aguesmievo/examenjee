package com.fst.first;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.beans.factory.annotation.Value;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootApplication
public class PhrarmacieApplication {

    @Value("${upload.directory}")
    private String uploadDirectory;

    public static void main(String[] args) {
        SpringApplication.run(PhrarmacieApplication.class, args);
    }

    @PostConstruct
    public void init() {
        try {
            Path uploadPath = Paths.get(uploadDirectory);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
        } catch (Exception e) {
            System.err.println("Could not create upload directory: " + e.getMessage());
        }
    }
}