package com.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
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
}
