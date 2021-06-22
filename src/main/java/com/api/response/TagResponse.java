package com.api.response;

import com.dto.TagDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class TagResponse {
    private List<TagDTO> tags;
}
