package com.m1.communityhub.service;

import com.m1.communityhub.domain.GroupEntity;
import com.m1.communityhub.domain.Post;
import com.m1.communityhub.domain.PostStatus;
import com.m1.communityhub.domain.UserEntity;
import com.m1.communityhub.dto.PostDtos;
import com.m1.communityhub.repo.GroupRepository;
import com.m1.communityhub.repo.PostRepository;
import com.m1.communityhub.repo.UserRepository;
import com.m1.communityhub.util.CursorUtils;
import com.m1.communityhub.web.ApiException;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PostService {
    private final PostRepository postRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final GroupService groupService;

    public PostService(
        PostRepository postRepository,
        GroupRepository groupRepository,
        UserRepository userRepository,
        GroupService groupService
    ) {
        this.postRepository = postRepository;
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.groupService = groupService;
    }

    @Transactional
    public Post createPost(Long groupId, Long authorId, PostDtos.PostCreateRequest request) {
        groupService.ensureActiveMember(groupId, authorId);
        GroupEntity group = groupRepository.findById(groupId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Group not found"));
        UserEntity author = userRepository.findById(authorId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        Post post = new Post();
        post.setGroup(group);
        post.setAuthor(author);
        post.setTitle(request.title());
        post.setBody(request.body());
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
    public Post updatePost(Long postId, Long userId, PostDtos.PostUpdateRequest request) {
        Post post = getPost(postId);
        if (!post.getAuthor().getId().equals(userId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Not the post author");
        }
        if (request.title() != null) {
            post.setTitle(request.title());
        }
        if (request.body() != null) {
            post.setBody(request.body());
        }
        return post;
    }

    @Transactional
    public void softDelete(Long postId, Long userId) {
        Post post = getPost(postId);
        if (!post.getAuthor().getId().equals(userId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Not the post author");
        }
        post.setStatus(PostStatus.DELETED);
    }
}
