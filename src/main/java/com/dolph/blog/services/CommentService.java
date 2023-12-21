package com.dolph.blog.services;

import com.dolph.blog.helpers.TimestampUtil;
import com.dolph.blog.models.Comment;
import com.dolph.blog.repository.CommentRepo;
import com.mongodb.client.result.UpdateResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CommentService {
    private final MongoTemplate mongoTemplate;
    private final CommentRepo commentRepo;

        public CommentService(CommentRepo commentRepo, MongoTemplate mongoTemplate){
            this.commentRepo = commentRepo;
            this.mongoTemplate = mongoTemplate;
        }

        public String createComment(com.dolph.blog.dto.comment.NewCommentRequest newCommentRequest){
            Comment comment = Comment.builder()
                    .postId(newCommentRequest.getPostId())
                    .userId(newCommentRequest.getUserId())
                    .text(newCommentRequest.getText())
                    .parentId(newCommentRequest.getParentId() != null ? newCommentRequest.getParentId() : null)
                    .createdAt(TimestampUtil.getTimestamp())
                    .updatedAt(TimestampUtil.getTimestamp())
                    .build();
            this.commentRepo.save(comment);
            return comment.getId();
        }

        public Optional<Comment> getCommentById(String id){
            Comment comment = mongoTemplate.findOne(new Query(Criteria.where("_id").is(id)), Comment.class);
            return  Optional.ofNullable(comment);
        }

        public long deleteComment(String id, String userId){
            Query query = new Query();
            Criteria commentIdCriteria = Criteria.where("_id").is(id);
            Criteria userIdCriteria = Criteria.where("userId").is(userId);
            query.addCriteria(commentIdCriteria);
            query.addCriteria(userIdCriteria);
            return mongoTemplate.remove(query, Comment.class).getDeletedCount();
        }

        public UpdateResult updateComment(Query query, Update update){
            return mongoTemplate.updateFirst(query, update, Comment.class);
        }

        public Page<Comment> fetchComments(String id, int page, int size){
            Pageable pageable = PageRequest.of(page, size);
            return  commentRepo.findByPostId(id, pageable);
        }

        public Page<Comment> fetchReplies(String id, int page, int size){
            Pageable pageable = PageRequest.of(page, size);
            return commentRepo.findByParentId(id, pageable);
        }
}
