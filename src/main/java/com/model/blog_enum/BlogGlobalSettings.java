package com.model.blog_enum;

public enum BlogGlobalSettings {
    MULTIUSER_MODE("Многопользовательский режим"),
    POST_PREMODERATION("Премодерация постов"),
    STATISTICS_IS_PUBLIC("Показывать всем статистику блога");

   private final String name;

    BlogGlobalSettings(String name) {
        this.name = name;
    }
}
