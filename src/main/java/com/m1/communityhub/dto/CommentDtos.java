package com.m1.communityhub.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.OffsetDateTime;
import java.util.List;

public final class CommentDtos {
    private CommentDtos() {
    }

    public record CommentCreateRequest(
        @NotBlank String body,
        Long parentId
    ) {
    }

    public record CommentUpdateRequest(
        @NotBlank String body
    ) {
    }

    public record CommentResponse(
        Long id,
        Long postId,
        Long authorId,
        Long parentId,
        String body,
        String status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
    ) {
    }

    public record CommentListResponse(List<CommentResponse> items, String nextCursor) {
    }
}
