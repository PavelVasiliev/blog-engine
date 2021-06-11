package com.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentRequest {
    @JsonProperty("parent_id")
    private String parentId;
    @JsonProperty("post_id")
    private String postId;
    private String text;
}
