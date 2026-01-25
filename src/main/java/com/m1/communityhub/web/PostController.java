package com.m1.communityhub.web;

import com.m1.communityhub.domain.Post;
import com.m1.communityhub.dto.PostCreateRequest;
import com.m1.communityhub.dto.PostDto;
import com.m1.communityhub.dto.PostListResponse;
import com.m1.communityhub.dto.PostUpdateRequest;
import com.m1.communityhub.mapper.PostMapper;
import com.m1.communityhub.security.AuthenticatedUser;
import com.m1.communityhub.security.SecurityUtils;
import com.m1.communityhub.service.PostService;
import com.m1.communityhub.util.CursorUtils;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class PostController {
    private final PostService postService;
    private final PostMapper postMapper;

    public PostController(PostService postService, PostMapper postMapper) {
        this.postService = postService;
        this.postMapper = postMapper;
    }

    @PostMapping("/groups/{groupId}/posts")
    @ResponseStatus(HttpStatus.CREATED)
    public PostDto createPost(
        @PathVariable Long groupId,
        @Valid @RequestBody PostCreateRequest request
    ) {
        AuthenticatedUser user = requireUser();
        Post post = postService.createPost(groupId, user.getId(), request);
        return postMapper.toDto(post);
    }

    @GetMapping("/groups/{groupId}/posts")
    public PostListResponse listPosts(
        @PathVariable Long groupId,
        @RequestParam(required = false) String cursor,
        @RequestParam(defaultValue = "20") int limit
    ) {
        List<Post> posts = postService.listPosts(groupId, cursor, limit);
        String nextCursor = posts.isEmpty()
            ? null
            : CursorUtils.encode(posts.getLast().getCreatedAt(), posts.getLast().getId());
        List<PostDto> items = posts.stream().map(postMapper::toDto).toList();
        return new PostListResponse(items, nextCursor);
    }

    @GetMapping("/posts/{postId}")
    public PostDto getPost(@PathVariable Long postId) {
        return postMapper.toDto(postService.getPost(postId));
    }

    @PatchMapping("/posts/{postId}")
    public PostDto updatePost(
        @PathVariable Long postId,
        @Valid @RequestBody PostUpdateRequest request
    ) {
        AuthenticatedUser user = requireUser();
        return postMapper.toDto(postService.updatePost(postId, user.getId(), request));
    }

    @DeleteMapping("/posts/{postId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePost(@PathVariable Long postId) {
        AuthenticatedUser user = requireUser();
        postService.softDelete(postId, user.getId());
    }

    private AuthenticatedUser requireUser() {
        AuthenticatedUser user = SecurityUtils.currentUser();
        if (user == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }
        return user;
    }

}
