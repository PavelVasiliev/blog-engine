package com.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "captcha_codes")
public class Captcha {

    @Id
    private int id;
    private Date time;
    private String code;

    @Column(name = "secret_code")
    private String secretCode;
}
