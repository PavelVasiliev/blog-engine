package com.model.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Date;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "post_comments")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Comment parent;

    @ManyToOne
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Date time;
    @Column(nullable = false, columnDefinition = "text")
    private String text;

    public long getTime() {
        return time.getTime() / 1000;
    }

    public static Comment makeComment(User user, Post post, Comment parent, String text) {
        Comment comment = new Comment();
        comment.user = user;
        comment.post = post;
        comment.parent = parent;
        comment.text = text;
        comment.time = new Date();
        return comment;
    }
}
