package com.api.request;

import lombok.Getter;

import java.util.List;

@Getter
public class PostRequest {
    private long timestamp;
    private byte active;
    private String title;
    private List<String> tags;
    private String text;
}
