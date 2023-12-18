package com.dolph.blog.repository;

import com.dolph.blog.models.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CommentRepo extends MongoRepository<Comment, String> {
    Page<Comment> findAll(Pageable pageable);
    Page<Comment> findByPostId(String postId, Pageable pageable);
}
