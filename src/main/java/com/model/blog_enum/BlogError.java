package com.model.blog_enum;

import lombok.Getter;

@Getter
public enum BlogError {
    EMAIL("Этот e-mail уже зарегистрирован"),
    NAME("Имя указано неверно"),
    PASSWORD("Пароль короче 6-ти символов"),
    CAPTCHA("Код с картинки введён неверно"),
    TEXT("Текст публикации слишком короткий"),
    TITLE("Заголовок не установлен"),
    PHOTO("Фото слишком большое, нужно не более 5 Мб"),
    IMAGE("Размер файла превышает допустимый размер"),
    Text("Текст комментария не задан или слишком короткий"),
    CODE("Ссылка для восстановления пароля устарела. " +
            "<a href=\"/auth/restore\">Запросить ссылку снова</a>");

    private final String description;

    BlogError(String description) {
        this.description = description;
    }
}