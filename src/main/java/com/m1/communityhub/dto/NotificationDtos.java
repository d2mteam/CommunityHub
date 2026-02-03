package com.m1.communityhub.dto;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.OffsetDateTime;
import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class NotificationDtos {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotificationResponse {
        private Long id;
        private String type;
        private String actorId;
        private String targetUserId;
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
