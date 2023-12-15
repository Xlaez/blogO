package com.dolph.blog.repository;

import com.dolph.blog.interfaces.UserProjection;
import com.dolph.blog.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface UserRepo extends MongoRepository<User, String> {

    @Query(value = "{ 'email' : ?0 }", fields = "{ 'createdAt': 1, 'fullname': 1, 'email': 1, 'bio': 1, 'twitter': 1, 'pics': 1, '_id': 1, 'isEmailVerified': 1 }")
    UserProjection findUserProjectionByEmail(String email);

    @Query(value = "{ '_id' : ?0 }", fields = "{ 'createdAt': 1, 'fullname': 1, 'email': 1, 'bio': 1, 'twitter': 1, 'pics': 1, '_id': 1, 'password': 1, 'isEmailVerified': 1 }")
    UserProjection findUserProjectionById(String _id);

    Page<User> findAll(Pageable pageable);
}
