package com.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "posts")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column(name = "is_active")
    private int isActive;

    @Enumerated(EnumType.STRING)
    @Column(name = "moderation_status")
    private PostStatus status;

    @OneToOne
    @JoinColumn(name = "moderator_id")
    private User moderator;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "time")
    private Date publicationTime = new Date();

    private String title;
    private String text;
    private int viewCount;
}
