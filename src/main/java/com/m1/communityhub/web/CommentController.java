package com.m1.communityhub.web;

import com.m1.communityhub.domain.Comment;
import com.m1.communityhub.dto.CommentCreateRequest;
import com.m1.communityhub.dto.CommentDto;
import com.m1.communityhub.dto.CommentListResponse;
import com.m1.communityhub.dto.CommentUpdateRequest;
import com.m1.communityhub.mapper.CommentMapper;
import com.m1.communityhub.security.AuthenticatedUser;
import com.m1.communityhub.security.SecurityUtils;
import com.m1.communityhub.service.CommentService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CommentController {
    private final CommentService commentService;
    private final CommentMapper commentMapper;

    public CommentController(CommentService commentService, CommentMapper commentMapper) {
        this.commentService = commentService;
        this.commentMapper = commentMapper;
    }

    @PostMapping("/posts/{postId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto createComment(
        @PathVariable Long postId,
        @Valid @RequestBody CommentCreateRequest request
    ) {
        AuthenticatedUser user = requireUser();
        Comment comment = commentService.createComment(postId, user.getId(), request);
        return commentMapper.toDto(comment);
    }

    @GetMapping("/posts/{postId}/comments")
    public CommentListResponse listComments(
        @PathVariable Long postId,
        @RequestParam(required = false) String cursor,
        @RequestParam(defaultValue = "50") int limit
    ) {
        List<Comment> comments = commentService.listComments(postId, cursor, limit);
        String nextCursor = comments.isEmpty()
            ? null
            : CursorUtils.encode(comments.getLast().getCreatedAt(), comments.getLast().getId());
        List<CommentDto> items = comments.stream().map(commentMapper::toDto).toList();
        return new CommentListResponse(items, nextCursor);
    }

    @PatchMapping("/comments/{commentId}")
    public CommentDto updateComment(
        @PathVariable Long commentId,
        @Valid @RequestBody CommentUpdateRequest request
    ) {
        AuthenticatedUser user = requireUser();
        return commentMapper.toDto(commentService.updateComment(commentId, user.getId(), request));
    }

    @DeleteMapping("/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable Long commentId) {
        AuthenticatedUser user = requireUser();
        commentService.softDelete(commentId, user.getId());
    }

    private AuthenticatedUser requireUser() {
        AuthenticatedUser user = SecurityUtils.currentUser();
        if (user == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }
        return user;
    }

}
