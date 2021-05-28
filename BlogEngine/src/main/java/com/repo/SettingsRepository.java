package com.repo;

import com.model.entity.GlobalSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SettingsRepository extends JpaRepository<GlobalSetting, Integer> {

    GlobalSetting findByCode(String code);
}
