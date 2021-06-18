package com.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.model.entity.Post;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Getter
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

    public static class Builder{
        private PostDTO newPostDTO;;

        public Builder(){
            newPostDTO = new PostDTO();
        }

        public Builder withId(int id){
            newPostDTO.id = id;
            return this;
        }

        public Builder withTimestamp(long timestamp){
            newPostDTO.timestamp = timestamp;
            return this;
        }

        public Builder withUserDTO(UserDTO user){
            newPostDTO.user = user;
            return this;
        }

        public Builder withTitle(String title){
            newPostDTO.title = title;
            return this;
        }

        public Builder withText(String text){
            newPostDTO.text = text;
            return this;
        }

        public Builder withLikeCount(int likeCount){
            newPostDTO.likeCount = likeCount;
            return this;
        }
        public Builder withDislikeCount(int dislikeCount){
            newPostDTO.dislikeCount = dislikeCount;
            return this;
        }

        public Builder withViewCount(int viewCount){
            newPostDTO.viewCount = viewCount;
            return this;
        }

        public Builder withCommentCount(int commentCount){
            newPostDTO.commentCount = commentCount;
            return this;
        }

        public Builder withAnnounce(String text){
            newPostDTO.announce = PostDTO.getAnnounce(text);
            return this;
        }

        public Builder withComments(List<CommentDTO> comments){
            newPostDTO.comments = comments;
            return this;
        }

        public Builder withTags(Set<String> tags){
            newPostDTO.tags = tags;
            return this;
        }

        public Builder withIsActive(boolean active){
            newPostDTO.active = active;
            return this;
        }

        public PostDTO build(){
            return newPostDTO;
        }
    }
}
