package com.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SettingsRequest {
    @JsonProperty("MULTIUSER_MODE")
    private boolean multiuserMode;
    @JsonProperty("POST_PREMODERATION")
    private boolean postPremoderation;
    @JsonProperty("STATISTICS_IS_PUBLIC")
    private boolean statisticIsPublic;

    public boolean[] getData() {
        boolean[] data = new boolean[3];
        data[0] = multiuserMode;
        data[1] = postPremoderation;
        data[2] = statisticIsPublic;
        return data;
    }
}
