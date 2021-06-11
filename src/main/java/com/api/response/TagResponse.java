package com.api.response;

import com.dto.TagDTO;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.List;

@Getter
@Setter
@Component
public class TagResponse {
    private List<TagDTO> tags;
}
