package com.model.blog_enum;

public enum Permission {
    USER("user:write"),
    MODERATOR("moderator:moderate");

    private final String permission;

    Permission(String permission) {
        this.permission = permission;
    }

    public String getPermission() {
        return permission;
    }
}
