package com.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class ModeratePostRequest {
    @JsonProperty("post_id")
    private int postId;
    private String decision;
}
