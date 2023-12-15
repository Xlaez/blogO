package com.dolph.blog.controllers.user;

import com.dolph.blog.dto.post.NewPostRequest;
import com.dolph.blog.dto.user.ResponseBody;
import com.dolph.blog.services.PostService;
import com.dolph.blog.utils.ApiResponse;
import com.dolph.blog.utils.FileUploader;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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
}
