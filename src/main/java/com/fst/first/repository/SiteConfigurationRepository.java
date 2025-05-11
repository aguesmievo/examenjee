package com.fst.first.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.fst.first.model.SiteConfiguration;

public interface SiteConfigurationRepository extends JpaRepository<SiteConfiguration, Long> {
    @Query("SELECT sc FROM SiteConfiguration sc ORDER BY sc.id DESC LIMIT 1")
    SiteConfiguration findLatestConfiguration();
}