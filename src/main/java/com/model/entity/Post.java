package com.model.entity;

import com.model.blog_enum.PostStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Proxy;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.Comparator;
import java.util.Date;
import java.util.Objects;
import java.util.Set;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "posts")
@Proxy(lazy = false)
public class Post implements Comparable<Post> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "is_active", nullable = false)
    private byte isActive;

    @Column(name = "moderation_status", columnDefinition = "enum('NEW','ACCEPTED', 'DECLINED')", nullable = false)
    @Enumerated(EnumType.STRING)
    private PostStatus status = PostStatus.NEW;

    @ManyToOne
    @JoinColumn(name = "moderator_id")
    private User moderator;
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "time", nullable = false)
    private Date publicationDate;
    @Column(nullable = false)
    private String title;
    @Column(nullable = false, columnDefinition = "text")
    private String text;
    @Column(nullable = false)
    private int viewCount;

    @Setter
    @ManyToMany(mappedBy = "posts", cascade = CascadeType.ALL)
    private Set<Tag> tags;
    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY)
    private Set<PostVotes> votes;
    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY)
    private Set<Comment> comments;

    public Post(User user, String title, String text, long timestamp, byte isActive) {
        this.user = user;
        this.title = title;
        this.text = text;
        this.isActive = isActive;
        publicationDate = new Date(timestamp * 1000);
        viewCount = 0;
    }

    public boolean isPostActive() {
        return isActive == 1;
    }

    public long getTime() {
        return publicationDate.getTime() / 1000;
    }

    public void moderate(User moderator, PostStatus status) {
        this.moderator = moderator;
        this.status = status;
    }

    public void update(User currentUser, byte isActive, String title, long timestamp, String text) {
        if (currentUser.isModerator()) {
            update(isActive, title, timestamp, text);
        } else if (currentUser.getId() == this.getUser().getId()) {
            this.status = PostStatus.NEW;
            update(isActive, title, timestamp, text);
        }
    }

    public static Comparator<Post> PostCommentsSort
            = (p1, p2) -> Integer.compare(p2.getComments().size(), p1.getComments().size());

    private void update(byte isActive, String title, long timestamp, String text) {
        this.isActive = isActive;
        this.title = title;
        this.text = text;
        publicationDate = new Date(timestamp * 1000);
    }

    private int getScore() {
        int result = 0;
        for (PostVotes vote : votes) {
            result += vote.getValue();
        }
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Post post = (Post) o;
        return id == post.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public int compareTo(Post p) { //by score
        return (this.getScore() > p.getScore()) ? 1 : ((this.getScore() == p.getScore()) ? 0 : -1);
    }
}