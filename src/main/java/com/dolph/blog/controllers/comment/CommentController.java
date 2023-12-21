package com.dolph.blog.controllers.comment;

import com.dolph.blog.dto.comment.NewCommentRequest;
import com.dolph.blog.dto.user.ResponseBody;
import com.dolph.blog.models.Comment;
import com.dolph.blog.models.User;
import com.dolph.blog.services.CommentService;
import com.dolph.blog.services.UserService;
import com.dolph.blog.utils.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/v1/post/comment")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;
    private final UserService userService;

    @PostMapping
    @RequestMapping("/new")
    public ResponseEntity<ResponseBody>createPost(@RequestBody NewCommentRequest request, @AuthenticationPrincipal String userId){
        ApiResponse response = new ApiResponse();

        try{
            request.setUserId(userId);
            String id = commentService.createComment(request);

            if(id.isEmpty()){
                ResponseBody r = response.failureResponse("cannot add comment to post", null);
                return new ResponseEntity<>(r, HttpStatus.INTERNAL_SERVER_ERROR);
            }

            ResponseBody r = response.successResponse("comment added", null);
            return new ResponseEntity<>(r, HttpStatus.CREATED);

        }catch (Exception e){
            ResponseBody r = response.catchHandler(e, "Error creating comment: {} ");
            return new ResponseEntity<>(r, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping
    @RequestMapping("/all")
    public ResponseEntity<ResponseBody>createPost(@RequestParam() int page,
                                                  @RequestParam() int limit,
                                                  @RequestParam() String postId) {
        ApiResponse response = new ApiResponse();

        try{
            Page<Comment> comments = commentService.fetchComments(postId, page, limit);

            List<Comment> commentList = comments.getContent();

            if(commentList.isEmpty()){
                ResponseBody r =response.failureResponse("there are no comments yet", null);
                return new ResponseEntity<>(r, HttpStatus.NOT_FOUND);
            }

            Map<String, Object> returnDoc = new HashMap<>();

            List<Object> commentDocs = new ArrayList<>();

            Map<String, Object> commentDoc = new HashMap<>();

            for(Comment comment: commentList){
                Optional<User> user = userService.getUserById(comment.getUserId());

                if(user.isPresent()){
                    commentDoc.put("comment", comment);
                    commentDoc.put("author", userService.mapUserToUserDTO(user.get()));
                    commentDocs.add(commentDoc);
                }
            }

            returnDoc.put("docs", commentDocs);
            returnDoc.put("hasNextPage", comments.hasNext());
            returnDoc.put("totalComments", comments.getNumberOfElements());
            returnDoc.put("totalPages", comments.getTotalPages());

            ResponseBody r = response.successResponse("comment fetched successfully", returnDoc);
            return new ResponseEntity<>(r, HttpStatus.OK);

        }catch(Exception e){
            ResponseBody r =response.catchHandler(e, "Error fetching comments: {} ");
            return new ResponseEntity<>(r, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
