package com.model;

import java.util.Date;

public interface Account {
    int getId();
    int isModerator();
    Date getRegTime();
    String getName();
    String getMail();
    String getPhoto();
}
