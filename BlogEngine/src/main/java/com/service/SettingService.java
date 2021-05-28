package com.service;

import com.api.response.SettingsResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SettingService {
    private final SettingsResponse settings;

    @Autowired
    public SettingService(SettingsResponse settings) {
        this.settings = settings;
    }
    public SettingsResponse getGlobalSettings() {
        return settings;
    }
}
