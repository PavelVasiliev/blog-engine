package com.model.blog_enum;

public enum Permission {
    USER("user:write"),
    MODERATOR("moderator:moderate");

    public String getPermission() {
        return permission;
    }

    private final String permission;

    Permission(String permission) {
        this.permission = permission;
    }


}
