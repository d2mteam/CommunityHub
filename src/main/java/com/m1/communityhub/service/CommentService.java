package com.m1.communityhub.service;

import com.m1.communityhub.domain.Comment;
import com.m1.communityhub.domain.enums.CommentStatus;
import com.m1.communityhub.domain.enums.PostStatus;
import com.m1.communityhub.domain.Post;
import com.m1.communityhub.domain.UserEntity;
import com.m1.communityhub.dto.CommentDtos;
import com.m1.communityhub.repo.CommentRepository;
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
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final GroupService groupService;
    private final NotificationService notificationService;

    @Transactional
    public Comment createComment(Long postId, UserContext userContext, CommentDtos.CommentCreateRequest request) {
        UUID authorId = requireUserId(userContext);
        Post post = postRepository.findByIdAndStatusNot(postId, PostStatus.DELETED)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Post not found"));
        groupService.ensureActiveMember(post.getGroup().getId(), authorId);
        UserEntity author = userRepository.findById(authorId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        Comment parent = null;
        if (request.getParentId() != null) {
            parent = commentRepository.findByIdAndStatusNot(request.getParentId(), CommentStatus.DELETED)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Parent comment not found"));
            if (!parent.getPost().getId().equals(postId)) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Parent comment does not belong to post");
            }
        }
        Comment comment = new Comment();
        comment.setPost(post);
        comment.setAuthor(author);
        comment.setParent(parent);
        comment.setBody(request.getBody());
        Comment saved = commentRepository.save(comment);

        if (parent != null) {
            notificationService.notifyReplyCreated(saved, parent, post, author);
        } else {
            notificationService.notifyCommentCreated(saved, post, author);
        }
        return saved;
    }

    public List<Comment> listComments(Long postId, String cursor, int limit) {
        CursorUtils.Cursor decoded;
        try {
            decoded = CursorUtils.decode(cursor);
        } catch (IllegalArgumentException ex) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid cursor");
        }
        OffsetDateTime createdAt = decoded == null ? null : decoded.createdAt();
        Long id = decoded == null ? null : decoded.id();
        return commentRepository.findByPostWithCursor(postId, CommentStatus.DELETED, createdAt, id, PageRequest.of(0, limit));
    }

    public Comment getComment(Long commentId) {
        return commentRepository.findByIdAndStatusNot(commentId, CommentStatus.DELETED)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Comment not found"));
    }

    @Transactional
    public Comment updateComment(Long commentId, UserContext userContext, CommentDtos.CommentUpdateRequest request) {
        UUID userId = requireUserId(userContext);
        Comment comment = getComment(commentId);
        if (!comment.getAuthor().getId().equals(userId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Not the comment author");
        }
        comment.setBody(request.getBody());
        return comment;
    }

    @Transactional
    public void softDelete(Long commentId, UserContext userContext) {
        UUID userId = requireUserId(userContext);
        Comment comment = getComment(commentId);
        if (!comment.getAuthor().getId().equals(userId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Not the comment author");
        }
        comment.setStatus(CommentStatus.DELETED);
    }

    private UUID requireUserId(UserContext userContext) {
        try {
            return UUID.fromString(userContext.userId());
        } catch (IllegalArgumentException ex) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid user id");
        }
    }
}
