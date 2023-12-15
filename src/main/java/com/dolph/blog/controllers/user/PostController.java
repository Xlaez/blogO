package com.dolph.blog.controllers.user;

import com.dolph.blog.dto.post.NewPostRequest;
import com.dolph.blog.dto.post.UpdatePostRequest;
import com.dolph.blog.dto.user.ResponseBody;
import com.dolph.blog.helpers.TimestampUtil;
import com.dolph.blog.models.Post;
import com.dolph.blog.services.PostService;
import com.dolph.blog.utils.ApiResponse;
import com.dolph.blog.utils.FileUploader;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


@RestController
@RequestMapping("/v1/post")
@RequiredArgsConstructor
public class PostController {
    private  final FileUploader fileUploader;

    private final PostService postService;

    @PostMapping
    @RequestMapping("/new")
    public ResponseEntity<ResponseBody>createPost(@RequestParam("upload")MultipartFile upload,
                                                  @RequestParam("title") String title,
                                                  @RequestParam("content") String content,
                                                  @RequestParam("descr")String descr,
                                                  @RequestParam("category") String category,
                                                  @AuthenticationPrincipal String id
    ){
        ApiResponse response = new ApiResponse();

        try{
            String fileUrl = fileUploader.uploadFile(upload);

            NewPostRequest newPostRequest = new NewPostRequest();
            newPostRequest.setAuthorId(id);
            newPostRequest.setImg(fileUrl);
            newPostRequest.setCategory(category);
            newPostRequest.setDescr(descr);
            newPostRequest.setTitle(title);
            newPostRequest.setContent(content);

            String postId = postService.createPost(newPostRequest);

            if(postId.length() == 0){
                ResponseBody r =  response.failureResponse("cannot create post", null);
                return new ResponseEntity<>(r, HttpStatus.INTERNAL_SERVER_ERROR);
            }

            ResponseBody r =  response.successResponse("post created for: "+ postId, null);
            return new ResponseEntity<>(r, HttpStatus.CREATED);
        }catch(Exception e){
            ResponseBody r = response.catchHandler(e, "Error creating post: {} ");
            return new ResponseEntity<>(r, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping
    @RequestMapping("/update/{id}")
    public ResponseEntity<ResponseBody>updatePost(@PathVariable("id") String id, @RequestBody UpdatePostRequest request){
        ApiResponse response = new ApiResponse();

        try{

            Query query = new Query(Criteria.where("_id").is(id));
            Update update = new Update();

            if(request.getCategory() != null){
                update.set("category", request.getCategory());
            }

            if(request.getDescr() != null){
                update.set("descr", request.getDescr());
            }

            if(request.getContent() != null){
                update.set("content", request.getContent());
            }

            if(request.getTitle() != null){
                update.set("title", request.getTitle());
            }

            update.set("updatedAt", TimestampUtil.getTimestamp());

            if(postService.updatePost(query, update).getModifiedCount() == 0){
                ResponseBody r =response.failureResponse("cannot update post data", null);
                return new ResponseEntity<>(r, HttpStatus.INTERNAL_SERVER_ERROR);
            }

            Optional<Post> post = postService.getPostById(id);

            if(post.isEmpty()){
                ResponseBody r =response.failureResponse("cannot retrieve updated post", null);
                return new ResponseEntity<>(r, HttpStatus.NO_CONTENT);
            }

            Map<String, Object> updatedPost = new HashMap<>();

            updatedPost.put("id", post.get().getId());
            updatedPost.put("authorId", post.get().getAuthorId());
            updatedPost.put("title", post.get().getTitle());
            updatedPost.put("descr", post.get().getDescr());
            updatedPost.put("img", post.get().getImg());
            updatedPost.put("category", post.get().getCategory());
            updatedPost.put("content", post.get().getContent());
            updatedPost.put("updatedAt", post.get().getUpdatedAt());
            updatedPost.put("createdAt", post.get().getCreatedAt());

            ResponseBody r =response.successResponse("post updated",updatedPost);
            return new ResponseEntity<>(r, HttpStatus.OK);

        }catch(Exception e){
            ResponseBody r = response.catchHandler(e, "Error updating post: {} ");
            return new ResponseEntity<>(r, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
