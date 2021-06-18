package com.api.response;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@Component
public class CalendarResponse {
    private List<Integer> years;
    private Map<String, Integer> posts;
}
