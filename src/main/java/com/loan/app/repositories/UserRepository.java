package com.loan.app.repositories;

import com.loan.app.entities.User;
import com.loan.app.enums.UserType;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class UserRepository {

    private static final List<User> USERS = new ArrayList<>();

    static {
        USERS.add(new User("rohan", "rohan@gmail.com","$2a$10$Oc0hR0ZjzqNyxGtNe6GDdO6B1oqQ4QaiPDxwBUqFrzXygmvHG.3h.", UserType.CUSTOMER));
        USERS.add(new User("rahul", "rahul@gmail.com","$2a$10$DriwJfopgetQvRAbQUndNOiMWNh7hl7ILGNmFBNl33QuzyRD5yzoC", UserType.CUSTOMER));
        USERS.add(new User("alpha", "alpha@gmail.com","$2a$10$OHkyq7EGL4hlcpcYhcEzie9tkcDYPI1TE7SL1oZ2X7CEDuvVOK63O", UserType.ADMIN));
    }

    public List<User> findAll() {
        return new ArrayList<>(USERS);
    }

    public User findByEmail(String email) {
        return USERS.stream()
                .filter(user -> user.getEmail().equals(email))
                .findFirst().orElse(null);
    }

    public User findByUsername(String username) {
        return USERS.stream()
                .filter(user -> user.getUsername().equalsIgnoreCase(username))
                .findFirst().orElse(null);
    }

    public void save(User user) {
        USERS.add(user);
    }

    public void deleteByUsername(String username) {
        USERS.removeIf(user -> user.getUsername().equals(username));
    }

}
