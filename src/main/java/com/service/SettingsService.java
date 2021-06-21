package com.service;

import com.api.request.SettingsRequest;
import com.api.response.SettingsResponse;
import com.model.blog_enum.BlogGlobalSettings;
import com.model.entity.GlobalSettings;
import com.repo.SettingsRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SettingsService {
    private static final Logger logger = LogManager.getLogger(SettingsService.class);
    private final SettingsRepository settingsRepository;

    public SettingsService(SettingsRepository settingsRepository) {
        this.settingsRepository = settingsRepository;
    }

    public SettingsResponse getBlogSettings() {
        SettingsResponse response = new SettingsResponse();
        GlobalSettings setting = getSetting(BlogGlobalSettings.MULTIUSER_MODE);
        response.setMultiuserMode(setting.getBooleanValue());
        setting = getSetting(BlogGlobalSettings.POST_PREMODERATION);
        response.setPostPremoderation(setting.getBooleanValue());
        setting = getSetting(BlogGlobalSettings.STATISTICS_IS_PUBLIC);
        response.setStatisticIsPublic(setting.getBooleanValue());
        return response;
    }

    public GlobalSettings getSetting(BlogGlobalSettings setting) {
        return settingsRepository.findByCode(BlogGlobalSettings.valueOf(setting.name()).name());
    }

    public void save(SettingsRequest request) {
        List<GlobalSettings> savedSettings = settingsRepository.findAll();
        boolean[] requestValues = request.getData();

        if (savedSettings.size() != requestValues.length) {
            String log = "Check settings enum and request data! smthgs wrong!\n" + request;
            logger.error(log);
        } else {
            for (int i = 0; i < BlogGlobalSettings.values().length; i++) {
                GlobalSettings s = savedSettings.get(i);
                s.changeValue(requestValues[i]);
                settingsRepository.save(s);
            }
            String log = "Blog settings have been changed. \n" + request;
            logger.warn(log);
        }
    }
}