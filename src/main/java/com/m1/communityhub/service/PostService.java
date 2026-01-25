package com.m1.communityhub.service;

import com.m1.communityhub.domain.GroupEntity;
import com.m1.communityhub.domain.Post;
import com.m1.communityhub.domain.PostStatus;
import com.m1.communityhub.domain.UserEntity;
import com.m1.communityhub.dto.PostCreateRequest;
import com.m1.communityhub.dto.PostUpdateRequest;
import com.m1.communityhub.mapper.PostMapper;
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
    private final PostMapper postMapper;

    public PostService(
        PostRepository postRepository,
        GroupRepository groupRepository,
        UserRepository userRepository,
        GroupService groupService,
        PostMapper postMapper
    ) {
        this.postRepository = postRepository;
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.groupService = groupService;
        this.postMapper = postMapper;
    }

    @Transactional
    public Post createPost(Long groupId, Long authorId, PostCreateRequest request) {
        groupService.ensureActiveMember(groupId, authorId);
        GroupEntity group = groupRepository.findById(groupId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Group not found"));
        UserEntity author = userRepository.findById(authorId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        Post post = postMapper.toEntity(request);
        post.setGroup(group);
        post.setAuthor(author);
        post.setStatus(PostStatus.ACTIVE);
        post.setCreatedAt(OffsetDateTime.now());
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
    public Post updatePost(Long postId, Long userId, PostUpdateRequest request) {
        Post post = getPost(postId);
        if (!post.getAuthor().getId().equals(userId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Not the post author");
        }
        postMapper.update(post, request);
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
