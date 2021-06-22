package com.dto;

import java.util.List;
import java.util.Set;

public class DTOBuilder {
    private int id;

    //for User
    private String name;
    private String photo;
    private String email;
    private boolean moderation;
    private int moderationCount;

    //for Post
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

    public DTOBuilder id(int id) {
        this.id = id;
        return this;
    }

    public DTOBuilder name(String name) {
        this.name = name;
        return this;
    }

    public DTOBuilder photo(String photo) {
        this.photo = photo;
        return this;
    }

    public DTOBuilder email(String email) {
        this.email = email;
        return this;
    }

    public DTOBuilder isModerator(boolean moderation) {
        this.moderation = moderation;
        return this;
    }

    public DTOBuilder moderationCount(int moderationCount) {
        this.moderationCount = moderationCount;
        return this;
    }

    public UserDTO buildUser() {
        return new UserDTO(
                id,
                name,
                email,
                photo,
                moderation,
                moderationCount
        );
    }


    public DTOBuilder timestamp(long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public DTOBuilder userDTO(UserDTO user) {
        this.user = user;
        return this;
    }

    public DTOBuilder title(String title) {
        this.title = title;
        return this;
    }

    public DTOBuilder text(String text) {
        this.text = text;
        return this;
    }

    public DTOBuilder likeCount(int likeCount) {
        this.likeCount = likeCount;
        return this;
    }

    public DTOBuilder dislikeCount(int dislikeCount) {
        this.dislikeCount = dislikeCount;
        return this;
    }

    public DTOBuilder viewCount(int viewCount) {
        this.viewCount = viewCount;
        return this;
    }

    public DTOBuilder commentCount(int commentCount) {
        this.commentCount = commentCount;
        return this;
    }

    public DTOBuilder announce(String text) {
        this.announce = PostDTO.getAnnounce(text);
        return this;
    }

    public DTOBuilder comments(List<CommentDTO> comments) {
        this.comments = comments;
        return this;
    }

    public DTOBuilder tags(Set<String> tags) {
        this.tags = tags;
        return this;
    }

    public DTOBuilder isActive(boolean active) {
        this.active = active;
        return this;
    }

    public PostDTO buildPost() {
        return new PostDTO
                (id, timestamp, user, title, text,
                 likeCount, dislikeCount, viewCount,
                 commentCount, announce, comments,
                 tags, active);
    }
}

