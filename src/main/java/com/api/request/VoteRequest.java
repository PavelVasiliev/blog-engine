package com.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
public class VoteRequest {
    @JsonProperty("post_id")
    private String postId;

    public int getPostId() {
        return Integer.parseInt(postId);
    }
}
