package com.m1.communityhub.service;

import com.m1.communityhub.domain.GroupEntity;
import com.m1.communityhub.domain.Post;
import com.m1.communityhub.domain.enums.PostStatus;
import com.m1.communityhub.domain.UserEntity;
import com.m1.communityhub.dto.PostDtos;
import com.m1.communityhub.repo.GroupRepository;
import com.m1.communityhub.repo.PostRepository;
import com.m1.communityhub.repo.UserRepository;
import com.m1.communityhub.config.security.UserContext;
import com.m1.communityhub.util.CursorUtils;
import com.m1.communityhub.web.ApiException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final GroupService groupService;

    @Transactional
    public Post createPost(Long groupId, UserContext userContext, PostDtos.PostCreateRequest request) {
        UUID authorId = requireUserId(userContext);
        groupService.ensureActiveMember(groupId, authorId);
        GroupEntity group = groupRepository.findById(groupId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Group not found"));
        UserEntity author = userRepository.findById(authorId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        Post post = new Post();
        post.setGroup(group);
        post.setAuthor(author);
        post.setTitle(request.getTitle());
        post.setBody(request.getBody());
        return postRepository.save(post);
    }

    public Post getPost(Long postId) {
        return postRepository.findByIdAndStatusNot(postId, PostStatus.DELETED)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Post not found"));
    }

    public List<Post> listPosts(Long groupId, String cursor, int limit) {
        CursorUtils.Cursor decoded;
        try {
            decoded = CursorUtils.decode(cursor);
        } catch (IllegalArgumentException ex) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid cursor");
        }
        OffsetDateTime createdAt = decoded == null ? null : decoded.createdAt();
        Long id = decoded == null ? null : decoded.id();
        return postRepository.findByGroupWithCursor(groupId, PostStatus.DELETED, createdAt, id, PageRequest.of(0, limit));
    }

    @Transactional
    public Post updatePost(Long postId, UserContext userContext, PostDtos.PostUpdateRequest request) {
        UUID userId = requireUserId(userContext);
        Post post = getPost(postId);
        if (!post.getAuthor().getId().equals(userId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Not the post author");
        }
        if (request.getTitle() != null) {
            post.setTitle(request.getTitle());
        }
        if (request.getBody() != null) {
            post.setBody(request.getBody());
        }
        return post;
    }

    @Transactional
    public void softDelete(Long postId, UserContext userContext) {
        UUID userId = requireUserId(userContext);
        Post post = getPost(postId);
        if (!post.getAuthor().getId().equals(userId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Not the post author");
        }
        post.setStatus(PostStatus.DELETED);
    }

    private UUID requireUserId(UserContext userContext) {
        try {
            return UUID.fromString(userContext.userId());
        } catch (IllegalArgumentException ex) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid user id");
        }
    }
}
