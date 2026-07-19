package com.minsun.sample.user;

import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {

    public User findById(Long id) {
        return new User(id, "minsun");
    }
}
