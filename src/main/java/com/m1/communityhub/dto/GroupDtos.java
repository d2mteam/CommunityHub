package com.m1.communityhub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public final class GroupDtos {
    private GroupDtos() {
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GroupCreateRequest {
        @NotBlank
        @Size(max = 150)
        private String slug;

        @NotBlank
        @Size(max = 255)
        private String name;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GroupResponse {
        private Long id;
        private String slug;
        private String name;
        private String ownerId;
        private OffsetDateTime createdAt;
    }
}
