package com.model.image;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Setter
@Getter
@NoArgsConstructor
public class BlogImage {
    public static final int MAX_SIZE = 5242880;
    private String name;
    private String path;
    private byte[] content;

    public BlogImage(String name) {
        this.name = name;
    }
}