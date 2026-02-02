package com.m1.communityhub.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public final class CommentDtos {
    private CommentDtos() {
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommentCreateRequest {
        @NotBlank
        private String body;
        private Long parentId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommentUpdateRequest {
        @NotBlank
        private String body;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommentResponse {
        private Long id;
        private Long postId;
        private String authorId;
        private Long parentId;
        private String body;
        private String status;
        private OffsetDateTime createdAt;
        private OffsetDateTime updatedAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommentListResponse {
        private List<CommentResponse> items;
        private String nextCursor;
    }
}
