package com.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "users")
public class User implements Account {

    @Column(name = "is_moderator")
    private int isModerator;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    @Column(name = "reg_time")
    private Date regTime;

    private String name;
    private String email;
    private String password;
    private String code;
    private String photo;

    @Override
    public int getId() {
        return id;
    }

    @Override
    public int isModerator() {
        return isModerator;
    }

    @Override
    public Date getRegTime() {
        return regTime;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getMail() {
        return email;
    }

    @Override
    public String getPhoto() {
        return photo;
    }
}
