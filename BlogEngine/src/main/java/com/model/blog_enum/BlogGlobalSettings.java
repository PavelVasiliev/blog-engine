package com.model.blog_enum;


import lombok.Getter;

//ToDo delete
@Getter
public enum BlogGlobalSettings {
    MULTIUSER_MODE("Многопользовательский режим"),
    POST_PREMODERATION("Премодерация постов"),
    STATISTICS_IS_PUBLIC("Показывать всем статистику блога");

    String name;
    boolean value;

    BlogGlobalSettings(String name) {
        this.name = name;
    }
}
