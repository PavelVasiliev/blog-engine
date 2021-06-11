package com.api.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostRequest {
    private long timestamp;
    private byte active;
    private String title;
    private List<String> tags;
    private String text;
}
