package com.dolph.blog.controllers.post;

import com.dolph.blog.dto.post.NewPostRequest;
import com.dolph.blog.dto.post.UpdatePostRequest;
import com.dolph.blog.dto.user.ResponseBody;
import com.dolph.blog.helpers.TimestampUtil;
import com.dolph.blog.interfaces.UserProjection;
import com.dolph.blog.models.Post;
import com.dolph.blog.models.User;
import com.dolph.blog.services.PostService;
import com.dolph.blog.services.UserService;
import com.dolph.blog.utils.ApiResponse;
import com.dolph.blog.utils.FileUploader;
import jakarta.websocket.server.PathParam;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;


@RestController
@RequestMapping("/v1/post")
@RequiredArgsConstructor
public class PostController {
    private  final FileUploader fileUploader;

    private final PostService postService;

    private final UserService userService;

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

            UserProjection user = userService.getUserByIdProjection(id);

            Map<String, Object> variables = new HashMap<>();

            variables.put("username", user.getFullname());
            variables.put("post", newPostRequest.getTitle());

            postService.sendEmail(user.getEmail(), "Post Created", variables);

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

    @GetMapping
    @RequestMapping("/author/{author_id}")
    public ResponseEntity<ResponseBody> getPostByAuthorId(@PathVariable("author_id") String id,
                                                          @RequestParam() int limit,
                                                          @RequestParam() int page){
        ApiResponse response = new ApiResponse();

        try{
            Page<Post> posts = postService.fetchUserPosts(id, page, limit);

            List<Post> postList = posts.getContent();

            if(postList.isEmpty()){
                ResponseBody r =response.failureResponse("cannot retrieve author's posts", null);
                return new ResponseEntity<>(r, HttpStatus.NOT_FOUND);
            }

            Map<String, Object> returnDoc = new HashMap<>();

            long totalPosts = posts.getTotalElements();
            int totalPages = posts.getTotalPages();
            boolean nextPage = posts.hasNext();

            returnDoc.put("totalDocs", totalPosts);
            returnDoc.put("totalPages", totalPages);
            returnDoc.put("hasNextPage", nextPage);
            returnDoc.put("docs", postList);

            ResponseBody r =response.successResponse("author's posts fetched",returnDoc);
            return new ResponseEntity<>(r, HttpStatus.OK);

        }catch(Exception e){
            ResponseBody r = response.catchHandler(e, "Error fetching post: {} ");
            return new ResponseEntity<>(r, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping
    @RequestMapping("/query")
    public ResponseEntity<ResponseBody> queryPost(@RequestParam() int limit,
                                                  @RequestParam() int page,
                                                  @RequestParam(required = false) String keyword,
                                                  @RequestParam(required = false) String category){
        ApiResponse response = new ApiResponse();

        try{
            List<Post> postList = postService.searchPosts(keyword, category, page, limit);

            if(postList.isEmpty()){
                ResponseBody r =response.failureResponse("there are no posts for this query", null);
                return new ResponseEntity<>(r, HttpStatus.NOT_FOUND);
            }

            Map<String, Object> returnDoc = new HashMap<>();

            List<Object> postDocs = new ArrayList<>();

            Map<String, Object> postDoc = new HashMap<>();

            for(Post post: postList){
                Optional<User> user = userService.getUserById(post.getAuthorId());

                if(user.isPresent()) {
                    postDoc.put("post", post);
                    postDoc.put("author", userService.mapUserToUserDTO(user.get()));
                    postDocs.add(postDoc);
                }
            }

            returnDoc.put("docs", postDocs);

            ResponseBody r =response.successResponse("posts fetched",returnDoc);
            return new ResponseEntity<>(r, HttpStatus.OK);

        }catch(Exception e){
            ResponseBody r = response.catchHandler(e, "Error fetching post: {} ");
            return new ResponseEntity<>(r, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping
    @RequestMapping("/")
    public ResponseEntity<ResponseBody> getPostById(@RequestParam() String id){
        ApiResponse response = new ApiResponse();
        try{

            Optional<Post> post = postService.getPostById(id);

            if(post.isEmpty()){
                ResponseBody r =response.failureResponse("cannot find post", null);
                return new ResponseEntity<>(r, HttpStatus.NOT_FOUND);
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

            ResponseBody r =response.successResponse("post fetched",updatedPost);
            return new ResponseEntity<>(r, HttpStatus.OK);

        }catch (Exception e){
            ResponseBody r = response.catchHandler(e, "Error retrieving post: {} ");
            return new ResponseEntity<>(r, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping
    @RequestMapping("/{id}")
    public ResponseEntity<ResponseBody> deletePost(@PathVariable String id,
                                                   @AuthenticationPrincipal String userId){
        ApiResponse response = new ApiResponse();
        try{
            if(postService.deletePost(id, userId) == 0){
                ResponseBody r = response.failureResponse("cannot delete post", null);
                return new ResponseEntity<>(r, HttpStatus.INTERNAL_SERVER_ERROR);
            }

            ResponseBody r = response.successResponse("deleted post", null);
            return new ResponseEntity<>(r, HttpStatus.OK);
        }catch (Exception e){
            ResponseBody r = response.catchHandler(e, "Error deleting post: {} ");
            return new ResponseEntity<>(r, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

// Todo: create likes, comments and develop swagger docs then deploy
// TODO: write tests too
// TODO: implement facebook oauth and github oauth