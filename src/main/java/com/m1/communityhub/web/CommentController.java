package com.m1.communityhub.web;

import com.m1.communityhub.domain.Comment;
import com.m1.communityhub.dto.CommentDtos;
import com.m1.communityhub.config.security.SecurityUtils;
import com.m1.communityhub.config.security.UserContext;
import com.m1.communityhub.service.CommentService;
import com.m1.communityhub.util.CursorUtils;
import jakarta.validation.Valid;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @PostMapping("/posts/{postId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDtos.CommentResponse createComment(
        @PathVariable Long postId,
        @Valid @RequestBody CommentDtos.CommentCreateRequest request
    ) {
        UserContext user = requireUser();
        Comment comment = commentService.createComment(postId, user, request);
        return toResponse(comment);
    }

    @GetMapping("/posts/{postId}/comments")
    public CommentDtos.CommentListResponse listComments(
        @PathVariable Long postId,
        @RequestParam(required = false) String cursor,
        @RequestParam(defaultValue = "50") int limit
    ) {
        List<Comment> comments = commentService.listComments(postId, cursor, limit);
        String nextCursor = comments.isEmpty()
            ? null
            : CursorUtils.encode(comments.getLast().getCreatedAt(), comments.getLast().getId());
        List<CommentDtos.CommentResponse> items = comments.stream().map(this::toResponse).toList();
        return new CommentDtos.CommentListResponse(items, nextCursor);
    }

    @PatchMapping("/comments/{commentId}")
    public CommentDtos.CommentResponse updateComment(
        @PathVariable Long commentId,
        @Valid @RequestBody CommentDtos.CommentUpdateRequest request
    ) {
        UserContext user = requireUser();
        return toResponse(commentService.updateComment(commentId, user, request));
    }

    @DeleteMapping("/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable Long commentId) {
        UserContext user = requireUser();
        commentService.softDelete(commentId, user);
    }

    private UserContext requireUser() {
        UserContext user = SecurityUtils.currentUser();
        if (user == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }
        return user;
    }

    private CommentDtos.CommentResponse toResponse(Comment comment) {
        return new CommentDtos.CommentResponse(
            comment.getId(),
            comment.getPost().getId(),
            comment.getAuthor().getId().toString(),
            comment.getParent() == null ? null : comment.getParent().getId(),
            comment.getBody(),
            comment.getStatus().name(),
            comment.getCreatedAt(),
            comment.getUpdatedAt()
        );
    }
}
