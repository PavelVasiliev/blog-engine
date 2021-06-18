package com.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
public class ModeratePostRequest {
    @JsonProperty("post_id")
    private int postId;
    private String decision;
}
