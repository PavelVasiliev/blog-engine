package com.model.entity;

import com.model.blog_enum.PostStatus;
import com.model.blog_enum.Role;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(name = "is_moderator", nullable = false)
    private byte isModerator;
    @Column(name = "reg_time", nullable = false)
    private Date regTime;

    @Setter
    @Column(nullable = false)
    private String name;
    @Setter
    @Column(nullable = false)
    private String email;
    @Setter
    @Column(nullable = false)
    private String password;
    @Setter
    private String code;
    @Setter
    @Column(columnDefinition = "text")
    private String photo;

    public static User makeSimpleUser(String name, String email, String password) {
        return new User(name, email, password);
    }

    public boolean isActive() {
        return true;
    }

    public boolean isModerator() {
        return isModerator > 0;
    }

    public Role getRole() {
        return isModerator == 1 ? Role.MODERATOR : Role.USER;
    }

    private User(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
        regTime = new Date();
        isModerator = 0;
    }

    public void moderate(Post post, PostStatus status) {
        if(this.isModerator()){
            post.setModerator(this);
            post.setStatus(status);
        }
    }
}
