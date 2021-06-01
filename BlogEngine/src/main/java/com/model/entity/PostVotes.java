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
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.Date;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "post_votes")
public class PostVotes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @ManyToOne
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;
    @Column(nullable = false)
    private Date time;
    @Column(nullable = false)
    private byte value;

    public PostVotes(Post post, User user, byte value) {
        this.post = post;
        this.user = user;
        this.value = value;
        time = new Date();
    }

    public void changeOpinion(byte value) {
        this.value = value;
        time = new Date();
    }

    @Override
    public String toString() {
        return "PostVotes{" +
                "id=" + id +
                ", user=" + user +
                ", post=" + post +
                ", time=" + time +
                ", value=" + value +
                '}';
    }
}
