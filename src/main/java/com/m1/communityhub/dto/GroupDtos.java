package com.m1.communityhub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;

public final class GroupDtos {
    private GroupDtos() {
    }

    public record GroupCreateRequest(
        @NotBlank @Size(max = 150) String slug,
        @NotBlank @Size(max = 255) String name
    ) {
    }

    public record GroupResponse(
        Long id,
        String slug,
        String name,
        Long ownerId,
        OffsetDateTime createdAt
    ) {
    }
}
