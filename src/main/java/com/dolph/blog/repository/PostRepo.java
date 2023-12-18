package com.dolph.blog.repository;

import com.dolph.blog.models.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PostRepo extends MongoRepository<Post, String> {
    Page<Post> findAll(Pageable pageable);
    Page<Post> findByAuthorId(String authorId, Pageable pageable);
}
