package com.example.jwtauth.repository;


import com.example.jwtauth.user.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    boolean existsByUsername(String username);


    User findByUsername(String username);

    User findByEmail(String email);
}
