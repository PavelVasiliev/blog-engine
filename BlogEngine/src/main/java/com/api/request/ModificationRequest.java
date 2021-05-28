package com.api.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ModificationRequest {
    private String name;
    private String email;
    private String password;
    private String photo;
    private Byte removePhoto;
}
