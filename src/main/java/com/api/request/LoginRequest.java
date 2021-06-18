package com.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
public class LoginRequest {
    @JsonProperty("e_mail")
    private String email;
    private String password;
}
