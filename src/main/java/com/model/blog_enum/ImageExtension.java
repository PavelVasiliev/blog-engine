package com.model.blog_enum;

import lombok.Getter;

@Getter
public enum ImageExtension {
    JPG("jpg"),
    PNG("png");

    final String extension;

    ImageExtension(String extension) {
        this.extension = extension;
    }
}
