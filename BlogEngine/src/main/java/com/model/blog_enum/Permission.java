package com.model.blog_enum;

import lombok.Getter;

@Getter
public enum Permission {
    USER("user:write"),
    MODERATOR("moderator:moderate");

    private final String permission;

    Permission(String permission) {
        this.permission = permission;
    }
}
