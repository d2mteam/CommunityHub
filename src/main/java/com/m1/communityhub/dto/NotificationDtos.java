package com.m1.communityhub.dto;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public final class NotificationDtos {
    private NotificationDtos() {
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotificationResponse {
        private Long id;
        private String type;
        private Long actorId;
        private Long targetUserId;
        private String entityType;
        private Long entityId;
        private JsonNode payload;
        private OffsetDateTime createdAt;
        private OffsetDateTime readAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotificationListResponse {
        private List<NotificationResponse> items;
        private String nextCursor;
    }
}
