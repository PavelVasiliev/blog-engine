package com.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.model.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDTO {
    private int id;
    private String name;
    private String email;
    private String photo;
    private boolean moderation;
    private int moderationCount;
    private final boolean settings = true;

    public static UserDTO makeUserDTOWithPhoto(User user) {
        return new UserDTO(
                user.getId(),
                user.getName(),
                user.getPhoto(),
                user.getEmail());
    }

    public static UserDTO makeModeratorDTO(User user, int moderationCount) {
        if (!user.isModerator()) {
            return makeSimpleUserDTO(user);
        }
        return new UserDTO(
                user.getId(),
                user.getName(),
                user.getPhoto(),
                user.getEmail(),
                user.isModerator(), moderationCount);
    }

    public static UserDTO makeSimpleUserDTO(User user) {
        return new UserDTO(
                user.getId(),
                user.getName(),
                user.getPhoto());
    }

    private UserDTO(int id, String name, String photo) {
        this.id = id;
        this.name = name;
        this.photo = photo;
    }

    private UserDTO(int id, String name, String photo, String email) {
        this.id = id;
        this.name = name;
        this.photo = photo;
        this.email = email;
    }

    private UserDTO(int id, String name, String photo, String email, boolean moderation, int moderationCount) {
        this.id = id;
        this.name = name;
        this.photo = photo;
        this.email = email;
        this.moderation = moderation;
        this.moderationCount = moderationCount;
    }

}
