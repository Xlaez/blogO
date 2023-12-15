package com.dolph.blog.services;

import com.dolph.blog.helpers.TimestampUtil;
import com.dolph.blog.models.Post;
import com.dolph.blog.repository.PostRepo;
import com.dolph.blog.utils.EmailSender;
import com.mongodb.client.result.UpdateResult;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class PostService {
    private final MongoTemplate mongoTemplate;
    private final PostRepo postRepo;

    @Autowired
    private EmailSender emailSender;

    public PostService(PostRepo postRepo, MongoTemplate mongoTemplate){
        this.postRepo = postRepo;
        this.mongoTemplate=  mongoTemplate;
    }

    public String createPost(com.dolph.blog.dto.post.NewPostRequest newPostRequest){
        Post post = Post.builder()
                .authorId(newPostRequest.getAuthorId())
                .img(newPostRequest.getImg())
                .content(newPostRequest.getContent())
                .descr(newPostRequest.getDescr())
                .title(newPostRequest.getTitle())
                .category(newPostRequest.getCategory())
                .published(false)
                .createdAt(TimestampUtil.getTimestamp())
                .updatedAt(TimestampUtil.getTimestamp())
                .build();
        this.postRepo.save(post);
        return post.getId();
    }

    public Optional<Post> getPostById(String id){
        Post post = mongoTemplate.findOne(new Query(Criteria.where("_id").is(id)), Post.class);
        return Optional.ofNullable(post);
    }

    public long deletePostId(String id){
        Query query = new Query(Criteria.where("_id").is(id));
        return mongoTemplate.remove(query, Post.class).getDeletedCount();
    }

    public UpdateResult deletePost(Query query, Update update){
        return mongoTemplate.updateFirst(query, update, Post.class);
    }

    public Page<Post> fetchUserPosts(String id,int page, int size){
        Pageable pageable = PageRequest.of(page, size);
        return postRepo.findByAuthorId(id, pageable);
    }

    public void sendEmail(String recipient, String subject, Map<String, Object> variables) throws MessagingException {
        emailSender.sendEmail(recipient, subject, variables);
    }
}
