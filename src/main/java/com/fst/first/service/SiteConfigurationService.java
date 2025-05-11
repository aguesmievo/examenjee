package com.fst.first.service;

import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import com.fst.first.repository.SiteConfigurationRepository;
import com.fst.first.model.SiteConfiguration;

@Service
@Transactional
public class SiteConfigurationService {
    
    private final SiteConfigurationRepository repository;
    
    public SiteConfigurationService(SiteConfigurationRepository repository) {
        this.repository = repository;
    }
    
    public SiteConfiguration getCurrentConfiguration() {
        return repository.findLatestConfiguration();
    }
    
    public SiteConfiguration updateConfiguration(SiteConfiguration config) {
        return repository.save(config);
    }
    
    @PostConstruct
    public void initDefaultConfiguration() {
        if (repository.count() == 0) {
            SiteConfiguration defaultConfig = new SiteConfiguration();
            defaultConfig.setSiteName("PharmaGest");
            defaultConfig.setAddress("123 Rue Pharma, Ville");
            defaultConfig.setPhone("+123 456 7890");
            defaultConfig.setEmail("contact@pharmagest.com");
            defaultConfig.setDescription("Système de gestion pharmaceutique professionnel");
            defaultConfig.setCopyrightText("© 2025 PharmaGest. Tous droits réservés.");
            defaultConfig.setVersion("1.0.0");
            repository.save(defaultConfig);
        }
    }
}