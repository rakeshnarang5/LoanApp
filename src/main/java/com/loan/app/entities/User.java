package com.loan.app.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.loan.app.enums.UserType;

import java.io.Serializable;

public class User implements Serializable {

    private final String username;
    private final String email;
    private final String encryptedPassword;
    private final UserType type;

    public User(String username, String email, String encryptedPassword, UserType type) {
        this.username = username;
        this.email = email;
        this.encryptedPassword = encryptedPassword;
        this.type = type;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    @JsonIgnore
    public String getEncryptedPassword() {
        return encryptedPassword;
    }

    public UserType getType() {
        return type;
    }
}
