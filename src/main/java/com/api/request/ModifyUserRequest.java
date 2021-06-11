package com.api.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.model.image.MultipartFileImpl;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ModifyUserRequest {
    private String name;
    private String email;
    private String password;
    private MultipartFileImpl photo;
    private byte removePhoto;

}
