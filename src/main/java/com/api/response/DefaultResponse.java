package com.api.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DefaultResponse {
    private boolean result;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Map<String, String> errors;
}
