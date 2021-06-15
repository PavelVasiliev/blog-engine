package com.model.entity;

public enum ImageExtension {
    JPG("jpg"),
    PNG("png");

    private final String extension;

    ImageExtension(String extension) {
        this.extension = extension;
    }

    public String getExtension() {
        return extension;
    }
}
