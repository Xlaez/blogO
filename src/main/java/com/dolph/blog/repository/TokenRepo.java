package com.dolph.blog.repository;

import com.dolph.blog.models.Token;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TokenRepo extends MongoRepository<Token, String> {

}
