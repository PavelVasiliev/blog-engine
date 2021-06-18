package com.repo;

import com.model.entity.GlobalSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SettingsRepository extends JpaRepository<GlobalSettings, Integer> {
    GlobalSettings findByCode(String code);
}
