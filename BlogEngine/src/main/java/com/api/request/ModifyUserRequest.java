package com.api.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.model.MultipartFileImpl;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@Component
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ModifyUserRequest {
    private String name;
    private String email;
    private String password;
    private MultipartFileImpl photo;
    private byte removePhoto;

}
