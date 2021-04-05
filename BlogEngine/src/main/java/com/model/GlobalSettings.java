package com.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "global_settings")
public class GlobalSettings {

    @Id
    private int id;
    private String code;
    private String name;
    private String value;
}
