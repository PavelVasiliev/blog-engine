package com.service;

import com.api.request.SettingsRequest;
import com.api.response.SettingsResponse;
import com.model.blog_enum.BlogGlobalSettings;
import com.model.entity.GlobalSetting;
import com.repo.SettingsRepository;
import org.apache.tomcat.jni.Global;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SettingsService {
    private final SettingsRepository settingsRepository;

    public SettingsService(SettingsRepository settingsRepository) {
        this.settingsRepository = settingsRepository;
    }

    public SettingsResponse getBlogSettings() {
        SettingsResponse response = new SettingsResponse();
        GlobalSetting setting = getSetting(BlogGlobalSettings.MULTIUSER_MODE);
        response.setMultiuserMode(setting.getBooleanValue());
        setting = getSetting(BlogGlobalSettings.POST_PREMODERATION);
        response.setPostPremoderation(setting.getBooleanValue());
        setting = getSetting(BlogGlobalSettings.STATISTICS_IS_PUBLIC);
        response.setStatisticIsPublic(setting.getBooleanValue());
        return response;
    }

    public GlobalSetting getSetting(BlogGlobalSettings setting){
        return settingsRepository.findByCode(BlogGlobalSettings.valueOf(setting.name()).name());
    }

    public void save(SettingsRequest request) {
        List<GlobalSetting> savedSettings = settingsRepository.findAll();
        boolean[] requestValues = request.getData();

        if(savedSettings.size() != requestValues.length){
            System.out.println("check settings enum and request data"); //ToDo logger
        }
        for(int i = 0; i < BlogGlobalSettings.values().length; i++){
            GlobalSetting s = savedSettings.get(i);
            s.changeValue(requestValues[i]);
            settingsRepository.save(s);
        }
    }
}
