package com.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
public class CommentRequest {
    @JsonProperty("parent_id")
    private String parentId;
    @JsonProperty("post_id")
    private String postId;
    private String text;
}
