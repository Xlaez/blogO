package com.dolph.blog.repository;

import com.dolph.blog.models.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepo extends MongoRepository<User, String> {

}
