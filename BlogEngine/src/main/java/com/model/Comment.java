package com.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "post_comments")
public class Comment {

    @Id
    private int id;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Comment parent;

    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post postId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User userId;

    private Date time;
    private String text;
}
