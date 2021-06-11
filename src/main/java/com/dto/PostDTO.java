package com.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.model.entity.Post;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
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

    public static PostDTO makePostDTOWithAnnounce(Post post, UserDTO user,
                                                  int likeCount, int dislikeCount, int commentCount) {
        return new PostDTO(
                post.getId(),
                post.getTime(),
                user,
                post.getTitle(),
                post.getText(),
                post.getViewCount(),
                likeCount,
                dislikeCount,
                commentCount);
    }

    public static PostDTO makePostDTOWithTagsComments(Post post, UserDTO user, int likeCount, int dislikeCount,
                                                      List<CommentDTO> comments, Set<String> tags, boolean isActive,
                                                      int viewCount) {
        return new PostDTO(
                post.getId(),
                post.getTime(),
                user,
                post.getTitle(),
                post.getText(),
                likeCount,
                dislikeCount,
                comments,
                tags,
                isActive,
                viewCount);
    }

    private PostDTO(int id, long timestamp, UserDTO user, String title, String text,
                    int viewCount, int likeCount, int dislikeCount, int commentCount) {
        this.id = id;
        this.timestamp = timestamp;
        this.user = user;
        this.title = title;
        this.text = text;
        this.likeCount = likeCount;
        this.dislikeCount = dislikeCount;
        this.viewCount = viewCount;
        this.commentCount = commentCount;
        announce = getAnnounce();
    }

    private PostDTO(int id, long timestamp, UserDTO user, String title, String text, int likeCount,
                    int dislikeCount, List<CommentDTO> comments, Set<String> tags,
                    boolean active, int viewCount) {
        this.id = id;
        this.timestamp = timestamp;
        this.user = user;
        this.title = title;
        this.text = text;
        this.likeCount = likeCount;
        this.dislikeCount = dislikeCount;
        this.active = active;
        this.comments = comments;
        this.tags = tags;
        this.viewCount = viewCount;
    }

    public String getAnnounce() {
        String ellipsis = "...";
        int maxAnnounceLength = 150;
        text = text.replaceAll("<.*?>", "");
        announce = text;
        if (announce.length() > maxAnnounceLength) {
            announce = announce.substring(0, maxAnnounceLength);
        }
        announce = announce.replaceAll("&nbsp;","").concat(ellipsis);
        return announce;
    }
}
