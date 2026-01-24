package com.m1.communityhub.dto;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.OffsetDateTime;
import java.util.List;

public final class NotificationDtos {
    private NotificationDtos() {
    }

    public record NotificationResponse(
        Long id,
        String type,
        Long actorId,
        Long targetUserId,
        String entityType,
        Long entityId,
        JsonNode payload,
        OffsetDateTime createdAt,
        OffsetDateTime readAt
    ) {
    }

    public record NotificationListResponse(List<NotificationResponse> items, String nextCursor) {
    }
}
