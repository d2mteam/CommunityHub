package com.m1.communityhub.web;

import com.m1.communityhub.domain.Post;
import com.m1.communityhub.dto.PostDtos;
import com.m1.communityhub.config.security.SecurityUtils;
import com.m1.communityhub.config.security.UserContext;
import com.m1.communityhub.service.PostService;
import com.m1.communityhub.util.CursorUtils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
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
@RequiredArgsConstructor
@Validated
public class PostController {
    private final PostService postService;


    @PostMapping("/groups/{groupId}/posts")
    @ResponseStatus(HttpStatus.CREATED)
    public PostDtos.PostResponse createPost(
        @PathVariable Long groupId,
        @Valid @RequestBody PostDtos.PostCreateRequest request
    ) {
        UserContext user = requireUser();
        Post post = postService.createPost(groupId, user, request);
        return toResponse(post);
    }

    @GetMapping("/groups/{groupId}/posts")
    public PostDtos.PostListResponse listPosts(
        @PathVariable Long groupId,
        @RequestParam(required = false) String cursor,
        @RequestParam(defaultValue = "20") @Min(1) @Max(100) int limit
    ) {
        List<Post> posts = postService.listPosts(groupId, cursor, limit);
        String nextCursor = posts.isEmpty()
            ? null
            : CursorUtils.encode(posts.getLast().getCreatedAt(), posts.getLast().getId());
        List<PostDtos.PostResponse> items = posts.stream().map(this::toResponse).toList();
        return new PostDtos.PostListResponse(items, nextCursor);
    }

    @GetMapping("/posts/{postId}")
    public PostDtos.PostResponse getPost(@PathVariable Long postId) {
        return toResponse(postService.getPost(postId));
    }

    @PatchMapping("/posts/{postId}")
    public PostDtos.PostResponse updatePost(
        @PathVariable Long postId,
        @Valid @RequestBody PostDtos.PostUpdateRequest request
    ) {
        UserContext user = requireUser();
        return toResponse(postService.updatePost(postId, user, request));
    }

    @DeleteMapping("/posts/{postId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePost(@PathVariable Long postId) {
        UserContext user = requireUser();
        postService.softDelete(postId, user);
    }

    private UserContext requireUser() {
        UserContext user = SecurityUtils.currentUser();
        if (user == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }
        return user;
    }

    private PostDtos.PostResponse toResponse(Post post) {
        return new PostDtos.PostResponse(
            post.getId(),
            post.getGroup().getId(),
            post.getAuthor().getId().toString(),
            post.getTitle(),
            post.getBody(),
            post.getStatus().name(),
            post.getCreatedAt(),
            post.getUpdatedAt()
        );
    }
}
