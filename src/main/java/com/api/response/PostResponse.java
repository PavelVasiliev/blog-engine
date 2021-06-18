package com.api.response;

import com.dto.PostDTO;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.List;

@Getter
@Setter
public class PostResponse {
    private int count;
    private List<PostDTO> posts;
}
