package com.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "post_votes")
public class PostVotes {

    @Id
    private int id;
    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;
    @OneToOne
    @JoinColumn(name = "post_id")
    private Post post;

    private Date time;
    private byte value;

    public static class Key implements Serializable {

        @Id
        private int userId;

        @Id
        private int postId;
    }
}
