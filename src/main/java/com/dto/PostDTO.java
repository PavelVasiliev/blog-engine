package com.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PostDTO {
    private int id;
    private long timestamp;
    private UserDTO user;
    private String title;
    private String text;
    private int likeCount;
    private int dislikeCount;
    private int viewCount;
    private int commentCount;
    private String announce;
    private List<CommentDTO> comments;
    private Set<String> tags;
    private boolean active;

    public static String getAnnounce(String text) {
        String ellipsis = "...";
        int maxAnnounceLength = 150;
        text = text.replaceAll("<.*?>", "");
        if (text.length() > maxAnnounceLength) {
            text = text.substring(0, maxAnnounceLength);
        }
        text = text.replaceAll("&nbsp;", "").concat(ellipsis);
        return text;
    }
}
