package com.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CommentDTO {
    private int id;
    private long timestamp;
    private String text;
    private UserDTO user;
}
