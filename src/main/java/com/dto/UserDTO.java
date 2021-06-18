package com.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDTO {
    private int id;
    private String name;
    private String email;
    private String photo;
    private boolean moderation;
    private int moderationCount;
    private final boolean settings = true;

    public static class Builder {
        private UserDTO newUserDTO;

        public Builder() {
            newUserDTO = new UserDTO();
        }

        public Builder withId(int id) {
            newUserDTO.id = id;
            return this;
        }

        public Builder withName(String name) {
            newUserDTO.name = name;
            return this;
        }

        public Builder withPhoto(String photo) {
            newUserDTO.photo = photo;
            return this;
        }

        public Builder withEmail(String email) {
            newUserDTO.email = email;
            return this;
        }

        public Builder withIsModerator(boolean moderation) {
            newUserDTO.moderation = moderation;
            return this;
        }

        public Builder withModerationCount(int moderationCount) {
            newUserDTO.moderationCount = moderationCount;
            return this;
        }

        public UserDTO build() {
            return newUserDTO;
        }
    }
}
