package com.m1.communityhub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.List;

public final class PostDtos {
    private PostDtos() {
    }

    public record PostCreateRequest(
        @NotBlank @Size(max = 255) String title,
        @NotBlank String body
    ) {
    }

    public record PostUpdateRequest(
        @Size(max = 255) String title,
        String body
    ) {
    }

    public record PostResponse(
        Long id,
        Long groupId,
        Long authorId,
        String title,
        String body,
        String status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
    ) {
    }

    public record PostListResponse(List<PostResponse> items, String nextCursor) {
    }
}
