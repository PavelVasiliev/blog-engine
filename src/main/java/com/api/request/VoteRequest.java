package com.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoteRequest {
    @JsonProperty("post_id")
    private String postId;

    public int getPostId() {
        return Integer.parseInt(postId);
    }
}
