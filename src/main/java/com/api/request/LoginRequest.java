package com.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class LoginRequest {
    @JsonProperty("e_mail")
    private String email;
    private String password;
}
