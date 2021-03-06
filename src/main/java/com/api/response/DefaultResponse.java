package com.api.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class DefaultResponse {
    private boolean result;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Map<String, String> errors;
}
