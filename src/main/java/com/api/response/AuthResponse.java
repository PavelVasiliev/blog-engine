package com.api.response;

import com.dto.UserDTO;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Getter
@Setter
public class AuthResponse {
    private boolean result;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private UserDTO user;
}
