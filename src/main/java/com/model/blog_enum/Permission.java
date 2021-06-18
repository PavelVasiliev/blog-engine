package com.model.blog_enum;

public enum Permission {
    USER("user:write"),
    MODERATOR("moderator:moderate");

    final String permission;

    Permission(String permission) {
        this.permission = permission;
    }
}
