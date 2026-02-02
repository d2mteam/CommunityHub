package com.m1.communityhub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public final class PostDtos {
    private PostDtos() {
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PostCreateRequest {
        @NotBlank
        @Size(max = 255)
        private String title;

        @NotBlank
        private String body;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PostUpdateRequest {
        @Size(max = 255)
        private String title;
        private String body;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PostResponse {
        private Long id;
        private Long groupId;
        private String authorId;
        private String title;
        private String body;
        private String status;
        private OffsetDateTime createdAt;
        private OffsetDateTime updatedAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PostListResponse {
        private List<PostResponse> items;
        private String nextCursor;
    }
}
