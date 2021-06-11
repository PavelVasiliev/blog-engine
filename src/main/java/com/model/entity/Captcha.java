package com.model.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "captcha_codes")
public class Captcha {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(nullable = false)
    private Date time;
    @Column(nullable = false, columnDefinition = "tinytext")
    private String code;
    @Column(name = "secret_code", nullable = false)
    private String secretCode;

    public Captcha(Date time, String code, String secretCode) {
        this.time = time;
        this.code = code;
        this.secretCode = secretCode;
    }
}
